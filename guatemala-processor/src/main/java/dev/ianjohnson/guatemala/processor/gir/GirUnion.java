package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.*;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import dev.ianjohnson.guatemala.processor.Impls;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import java.lang.foreign.MemoryAddress;
import java.lang.foreign.MemoryLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record GirUnion(
        String name,
        @Nullable String getType,
        List<GirField> fields,
        List<GirCallable> constructors,
        List<GirCallable> functions,
        List<GirCallable> methods)
        implements Named, Layoutable {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "union".equals(element.getLocalName());
    }

    public static GirUnion load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        String getType = element.getAttributeNS(NS.GLIB, "get-type");
        if (getType.isEmpty()) {
            getType = null;
        }
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
        return new GirUnion(
                name,
                getType,
                List.copyOf(fields),
                List.copyOf(constructors),
                List.copyOf(functions),
                List.copyOf(methods));
    }

    public TypeSpec binding(CodegenContext ctx) {
        ClassName implName = ClassName.get(ctx.activePackage().getQualifiedName().toString(), name() + "Impl");
        TypeSpec.Builder builder = TypeSpec.classBuilder(name())
            .addModifiers(Modifier.PUBLIC)
            .addField(FieldSpec.builder(
                    MemoryLayout.class, "MEMORY_LAYOUT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.MEMORY_LAYOUT", implName)
                .build())
            .addField(FieldSpec.builder(MemoryAddress.class, "address", Modifier.PRIVATE, Modifier.FINAL)
                .build())
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(MemoryAddress.class, "address")
                .addCode("this.address = $T.requireNonNull(address, \"address\");\n", Objects.class)
                .build())
            .addMethod(MethodSpec.methodBuilder("address")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(MemoryAddress.class)
                .addCode("return address;\n")
                .build());
        return builder.build();
    }

    public TypeSpec impl(CodegenContext ctx) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name() + "Impl").addModifiers(Modifier.FINAL);
        builder.addField(FieldSpec.builder(MemoryLayout.class, "MEMORY_LAYOUT", Modifier.STATIC, Modifier.FINAL)
            .initializer(memoryLayout(ctx)).build());
        Impls.addCallables(builder, ctx, constructors());
        Impls.addCallables(builder, ctx, methods());
        Impls.addCallables(builder, ctx, functions());
        return builder.build();
    }

    @Override
    public CodeBlock memoryLayout(CodegenContext ctx) {
        CodeBlock fieldLayouts =
            fields.stream().map(field -> field.memoryLayout(ctx)).collect(CodeBlock.joining(", "));
        return CodeBlock.of("$T.unionLayout($L)", MemoryLayout.class, fieldLayouts);
    }
}
