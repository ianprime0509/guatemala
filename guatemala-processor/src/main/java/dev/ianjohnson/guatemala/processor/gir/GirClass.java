package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.*;
import dev.ianjohnson.guatemala.processor.ClassNames;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import dev.ianjohnson.guatemala.processor.Impls;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.lang.model.element.Modifier;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record GirClass(
        String name,
        String getType,
        @Nullable GirName parent,
        boolean isAbstract,
        boolean isFinal,
        String refFunc,
        String unrefFunc,
        List<GirField> fields,
        List<GirCallable> constructors,
        List<GirCallable> functions,
        List<GirCallable> methods)
        implements Named, Layoutable {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "class".equals(element.getLocalName());
    }

    public static GirClass load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        String getType = element.getAttributeNS(NS.GLIB, "get-type");
        String parentValue = element.getAttributeNS(null, "parent");
        GirName parent = !parentValue.isEmpty() ? GirName.parse(parentValue, ns) : null;
        boolean isAbstract = "1".equals(element.getAttributeNS(null, "abstract"));
        boolean isFinal = "1".equals(element.getAttributeNS(null, "final"));
        String refFunc = element.getAttributeNS(NS.GLIB, "ref-func");
        String unrefFunc = element.getAttributeNS(NS.GLIB, "unref-func");
        List<GirField> fields = new ArrayList<>();
        List<GirCallable> constructors = new ArrayList<>();
        List<GirCallable> functions = new ArrayList<>();
        List<GirCallable> methods = new ArrayList<>();
        for (Node child : Nodes.children(element)) {
            if (child instanceof Element e) {
                if (GirField.canLoad(e)) {
                    fields.add(GirField.load(e, ns));
                } else if (GirCallable.canLoadConstructor(e)) {
                    constructors.add(GirCallable.load(e, ns));
                } else if (GirCallable.canLoadFunction(e)) {
                    functions.add(GirCallable.load(e, ns));
                } else if (GirCallable.canLoadMethod(e)) {
                    methods.add(GirCallable.load(e, ns));
                }
            }
        }
        return new GirClass(
                name,
                getType,
                parent,
                isAbstract,
                isFinal,
                refFunc,
                unrefFunc,
                List.copyOf(fields),
                List.copyOf(constructors),
                List.copyOf(functions),
                List.copyOf(methods));
    }

    public TypeSpec binding(CodegenContext ctx) {
        GirRecord record = ctx.activeNamespace().classRecords().get(name());
        ClassName implName =
                ClassName.get(ctx.activePackage().getQualifiedName().toString(), name() + "Impl");
        TypeSpec.Builder builder = TypeSpec.classBuilder(name())
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(
                                MemoryLayout.class, "MEMORY_LAYOUT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$T.MEMORY_LAYOUT", implName)
                        .build())
                .addType(recordBinding(record, ctx));
        if (isAbstract()) {
            builder.addModifiers(Modifier.ABSTRACT);
        } else {
            builder.addField(
                    FieldSpec.builder(classTypeName(ctx), "TYPE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$T.TYPE", implName)
                            .build());
        }
        if (isFinal()) {
            builder.addModifiers(Modifier.FINAL);
        }
        if (parent() != null) {
            builder.superclass(parent().className(ctx));
            builder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PROTECTED)
                    .addParameter(MemoryAddress.class, "address")
                    .addCode("super(address);\n")
                    .build());
        } else {
            builder.addSuperinterface(ClassNames.Addressable);
            builder.addSuperinterface(ClassNames.ReferenceCounted);
            builder.addField(FieldSpec.builder(MemoryAddress.class, "address", Modifier.PRIVATE, Modifier.FINAL)
                    .build());
            builder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PROTECTED)
                    .addParameter(MemoryAddress.class, "address")
                    .addCode("this.address = $T.requireNonNull(address, \"address\");\n", Objects.class)
                    .build());
            builder.addMethod(MethodSpec.methodBuilder("address")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(MemoryAddress.class)
                    .addCode("return address;\n")
                    .build());
            // TODO: correct implementation
            builder.addMethod(MethodSpec.methodBuilder("ref")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .build());
            builder.addMethod(MethodSpec.methodBuilder("unref")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .build());
        }
        return builder.build();
    }

    public TypeSpec impl(CodegenContext ctx) {
        GirRecord record = ctx.activeNamespace().classRecords().get(name());
        TypeSpec recordImpl;
        if (record != null) {
            recordImpl = record.impl("Class", ctx).toBuilder()
                    .addModifiers(Modifier.STATIC)
                    .build();
        } else {
            recordImpl = TypeSpec.classBuilder("Class")
                    .addModifiers(Modifier.STATIC, Modifier.FINAL)
                    .build();
        }
        TypeSpec.Builder builder = TypeSpec.classBuilder(name() + "Impl")
                .addModifiers(Modifier.FINAL)
                .addField(FieldSpec.builder(MemoryLayout.class, "MEMORY_LAYOUT", Modifier.STATIC, Modifier.FINAL)
                        .initializer(memoryLayout(ctx))
                        .build())
                .addType(recordImpl);
        if (!isAbstract()) {
            // TODO: type field for abstract classes
            builder.addField(classTypeField(ctx));
        }
        Impls.addCallables(builder, ctx, constructors());
        Impls.addCallables(builder, ctx, methods());
        Impls.addCallables(builder, ctx, functions());
        return builder.build();
    }

    @Override
    public CodeBlock memoryLayout(CodegenContext ctx) {
        CodeBlock fieldLayouts =
                fields.stream().map(field -> field.memoryLayout(ctx)).collect(CodeBlock.joining(", "));
        return CodeBlock.of("$T.structLayout($L)", MemoryLayout.class, fieldLayouts);
    }

    private FieldSpec classTypeField(CodegenContext ctx) {
        ClassName elemName =
                ClassName.get(ctx.activePackage().getQualifiedName().toString(), name());
        ClassName elemClassName = elemName.nestedClass("Class");
        TypeName classTypeName = classTypeName(ctx);
        return FieldSpec.builder(classTypeName, "TYPE", Modifier.STATIC, Modifier.FINAL)
                .initializer(
                        "$T.ofTypeGetter($S, $T::new, $T::new)",
                        ClassNames.ClassType,
                        getType(),
                        elemClassName,
                        elemName)
                .build();
    }

    private TypeName classTypeName(CodegenContext ctx) {
        ClassName elemName =
                ClassName.get(ctx.activePackage().getQualifiedName().toString(), name());
        ClassName elemClassName = elemName.nestedClass("Class");
        return ParameterizedTypeName.get(ClassNames.ClassType, elemClassName, elemName);
    }

    private TypeSpec recordBinding(@Nullable GirRecord type, CodegenContext ctx) {
        ClassName instanceImplName =
                ClassName.get(ctx.activePackage().getQualifiedName().toString(), name() + "Impl");
        ClassName implName = instanceImplName.nestedClass("Class");
        TypeSpec.Builder builder = TypeSpec.classBuilder("Class").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        if (type != null) {
            builder.addField(FieldSpec.builder(
                            MemoryLayout.class, "MEMORY_LAYOUT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.MEMORY_LAYOUT", implName)
                    .build());
        }
        if (isAbstract()) {
            builder.addModifiers(Modifier.ABSTRACT);
        }
        if (isFinal()) {
            builder.addModifiers(Modifier.FINAL);
        }
        if (parent() != null) {
            builder.superclass(parent().className(ctx).nestedClass("Class"));
            builder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PROTECTED)
                    .addParameter(MemoryAddress.class, "address")
                    .addCode("super(address);\n")
                    .build());
        } else {
            builder.addSuperinterface(ClassNames.Addressable);
            builder.addField(FieldSpec.builder(MemoryAddress.class, "address", Modifier.PRIVATE, Modifier.FINAL)
                    .build());
            builder.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PROTECTED)
                    .addParameter(MemoryAddress.class, "address")
                    .addCode("this.address = Objects.requireNonNull(address, \"address\");\n")
                    .build());
            builder.addMethod(MethodSpec.methodBuilder("address")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .returns(MemoryAddress.class)
                    .addCode("return address;\n")
                    .build());
        }
        return builder.build();
    }
}
