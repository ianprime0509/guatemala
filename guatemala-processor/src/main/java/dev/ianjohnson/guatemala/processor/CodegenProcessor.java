package dev.ianjohnson.guatemala.processor;

import com.squareup.javapoet.*;
import dev.ianjohnson.guatemala.annotation.GirSource;
import dev.ianjohnson.guatemala.annotation.NamespaceBinding;
import dev.ianjohnson.guatemala.annotation.NamespaceDependency;
import dev.ianjohnson.guatemala.processor.gir.*;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SupportedAnnotationTypes("dev.ianjohnson.guatemala.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_19)
@SupportedOptions({CodegenProcessor.OPTION_GIR_DIRECTORY})
public final class CodegenProcessor extends AbstractProcessor {
    public static final String OPTION_GIR_DIRECTORY = "gir.directory";

    private final Map<String, String> namespacePackages = new HashMap<>();
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private GirStore girStore;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        girStore = new GirStore(Path.of(processingEnv.getOptions().getOrDefault(OPTION_GIR_DIRECTORY, ".")));
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(NamespaceBinding.class)) {
            NamespaceBinding namespaceBinding = element.getAnnotation(NamespaceBinding.class);
            if (namespaceBinding != null) {
                processNamespaceBinding(namespaceBinding, (PackageElement) element);
            }
        }

        return true;
    }

    private void processNamespaceBinding(NamespaceBinding namespaceBinding, PackageElement element) {
        loadGir(element);

        GirNamespace ns = girStore.namespace(namespaceBinding.value()).orElse(null);
        if (ns == null) {
            messager.printError("No such namespace after loading GIR: " + namespaceBinding.value());
            return;
        }

        namespacePackages.put(
                namespaceBinding.value(), element.getQualifiedName().toString());
        for (NamespaceDependency dep : namespaceBinding.dependencies()) {
            namespacePackages.put(dep.value(), dep.packageName());
        }

        Set<String> definedTypes = element.getEnclosedElements().stream()
                .map(e -> e.getSimpleName().toString())
                .collect(Collectors.toSet());
        generateImpl(ns, element);
        for (var entry : ns.aliases().entrySet()) {
            if (!definedTypes.contains(entry.getKey())) {
                generateBinding(entry.getValue(), element);
            }
        }
        for (var entry : ns.classes().entrySet()) {
            generateImpl(entry.getValue(), ns, element);
            if (!definedTypes.contains(entry.getKey())) {
                generateBinding(entry.getValue(), ns, element);
            }
        }
        for (var entry : ns.records().entrySet()) {
            if (!ns.classRecords().containsKey(entry.getKey())) {
                generateImpl(entry.getValue(), element);
                if (!definedTypes.contains(entry.getKey())) {
                    generateBinding(entry.getValue(), element);
                }
            }
        }
        for (var entry : ns.unions().entrySet()) {
            generateImpl(entry.getValue(), element);
            if (!definedTypes.contains(entry.getKey())) {
                generateBinding(entry.getValue(), element);
            }
        }
        for (var entry : ns.interfaces().entrySet()) {
            generateImpl(entry.getValue(), element);
            if (!definedTypes.contains(entry.getKey())) {
                generateBinding(entry.getValue(), element);
            }
        }
        for (var entry : ns.bitFields().entrySet()) {
            if (!definedTypes.contains(entry.getKey())) {
                generateBinding(entry.getValue(), element);
            }
        }
        for (var entry : ns.enums().entrySet()) {
            if (!definedTypes.contains(entry.getKey())) {
                generateBinding(entry.getValue(), element);
            }
        }
        for (var entry : ns.callbacks().entrySet()) {
            if (!definedTypes.contains(entry.getKey())) {
                generateBinding(entry.getValue(), element);
            }
        }
    }

    private void loadGir(PackageElement packageElement) {
        for (GirSource girSource : packageElement.getAnnotationsByType(GirSource.class)) {
            try {
                girStore.load(girSource.value());
            } catch (IOException e) {
                messager.printError("Failed to load GIR source '" + girSource.value() + "': " + e, packageElement);
            }
        }
    }

    private void generateBinding(GirAlias type, PackageElement packageElement) {
        if (GirType.VOID.equals(type.type())) {
            messager.printWarning("Not sure what to do with opaque alias type " + type.name());
            return;
        }

        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), binding(type))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write binding output for " + type.name() + " : " + e, packageElement);
        }
    }

    private void generateBinding(GirClass type, GirNamespace ns, PackageElement packageElement) {
        GirRecord classType = ns.classRecords().get(type.name());
        JavaFile javaFile = JavaFile.builder(
                        packageElement.getQualifiedName().toString(), binding(type, classType, packageElement))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write binding output for " + type.name() + " : " + e, packageElement);
        }
    }

    private void generateBinding(GirInterface type, PackageElement packageElement) {
        JavaFile javaFile = JavaFile.builder(
                        packageElement.getQualifiedName().toString(), binding(type, packageElement))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write binding output for " + type.name() + " : " + e, packageElement);
        }
    }

    private void generateBinding(GirRecord type, PackageElement packageElement) {
        JavaFile javaFile = JavaFile.builder(
                        packageElement.getQualifiedName().toString(), binding(type, packageElement))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write binding output for " + type.name() + " : " + e, packageElement);
        }
    }

    private void generateBinding(GirUnion type, PackageElement packageElement) {
        JavaFile javaFile = JavaFile.builder(
                        packageElement.getQualifiedName().toString(), binding(type, packageElement))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write binding output for " + type.name() + " : " + e, packageElement);
        }
    }

    private void generateBinding(GirBitField type, PackageElement packageElement) {
        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), binding(type))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write binding output for " + type.name() + " : " + e, packageElement);
        }
    }

    private void generateBinding(GirEnum type, PackageElement packageElement) {
        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), binding(type))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write binding output for " + type.name() + " : " + e, packageElement);
        }
    }

    private void generateBinding(GirCallback type, PackageElement packageElement) {
        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), binding(type))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write binding output for " + type.name() + " : " + e, packageElement);
        }
    }

    private void generateImpl(GirNamespace ns, PackageElement packageElement) {
        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), impl(ns))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write impl output for " + ns.name() + " : " + e, packageElement);
        }
    }

    private void generateImpl(GirClass type, GirNamespace ns, PackageElement packageElement) {
        GirRecord classType = ns.classRecords().get(type.name());
        JavaFile javaFile = JavaFile.builder(
                        packageElement.getQualifiedName().toString(), impl(type, classType, packageElement))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write impl output for " + type.name() + " : " + e, packageElement);
        }
    }

    private void generateImpl(GirInterface type, PackageElement packageElement) {
        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), impl(type))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write impl output for " + type.name() + " : " + e, packageElement);
        }
    }

    private void generateImpl(GirRecord type, PackageElement packageElement) {
        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), impl(type))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write impl output for " + type.name() + " : " + e, packageElement);
        }
    }

    private void generateImpl(GirUnion type, PackageElement packageElement) {
        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), impl(type))
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write impl output for " + type.name() + " : " + e, packageElement);
        }
    }

    private TypeSpec binding(GirAlias type) {
        Type wrappedType = javaBindingType(type.type(), false);
        // TODO: could be a record once JavaPoet supports it
        return TypeSpec.classBuilder(type.name())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(FieldSpec.builder(
                                MemoryLayout.class, "MEMORY_LAYOUT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer(layout(type.type()))
                        .build())
                .addField(FieldSpec.builder(wrappedType, "raw", Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(wrappedType, "raw")
                        .addCode("this.raw = raw;\n")
                        .build())
                .addMethod(MethodSpec.methodBuilder("raw")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(wrappedType)
                        .addCode("return raw;\n")
                        .build())
                .build();
    }

    private TypeSpec binding(GirClass type, @Nullable GirRecord classType, PackageElement packageElement) {
        ClassName implName = ClassName.get(packageElement.getQualifiedName().toString(), type.name() + "Impl");
        TypeSpec.Builder builder = TypeSpec.classBuilder(type.name())
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(
                                MemoryLayout.class, "MEMORY_LAYOUT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$T.MEMORY_LAYOUT", implName)
                        .build())
                .addType(classBinding(classType, type, packageElement));
        if (type.isAbstract()) {
            builder.addModifiers(Modifier.ABSTRACT);
        } else {
            builder.addField(FieldSpec.builder(
                            classTypeName(type, packageElement),
                            "TYPE",
                            Modifier.PUBLIC,
                            Modifier.STATIC,
                            Modifier.FINAL)
                    .initializer("$T.TYPE", implName)
                    .build());
        }
        if (type.isFinal()) {
            builder.addModifiers(Modifier.FINAL);
        }
        if (type.parent() != null) {
            builder.superclass(className(type.parent()));
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

    private TypeSpec binding(GirInterface type, PackageElement packageElement) {
        ClassName implName = ClassName.get(packageElement.getQualifiedName().toString(), type.name() + "Impl");
        TypeSpec.Builder builder = TypeSpec.classBuilder(type.name())
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

    private TypeSpec binding(GirRecord type, PackageElement packageElement) {
        ClassName implName = ClassName.get(packageElement.getQualifiedName().toString(), type.name() + "Impl");
        TypeSpec.Builder builder = TypeSpec.classBuilder(type.name())
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

    private TypeSpec binding(GirUnion type, PackageElement packageElement) {
        ClassName implName = ClassName.get(packageElement.getQualifiedName().toString(), type.name() + "Impl");
        TypeSpec.Builder builder = TypeSpec.classBuilder(type.name())
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

    private TypeSpec binding(GirBitField type) {
        TypeSpec.Builder builder = TypeSpec.enumBuilder(type.name())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassNames.BitField)
                .addField(FieldSpec.builder(
                                ValueLayout.class, "MEMORY_LAYOUT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$T.GINT", ClassNames.Types)
                        .build())
                .addField(FieldSpec.builder(int.class, "value", Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(int.class, "value")
                        .addCode("this.value = value;\n")
                        .build())
                .addMethod(MethodSpec.methodBuilder("value")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(int.class)
                        .addCode("return value;\n")
                        .build());
        for (GirMember member : type.members()) {
            builder.addEnumConstant(
                    Support.toJavaSnakeCase(member.name()),
                    TypeSpec.anonymousClassBuilder("$L", member.value()).build());
        }
        return builder.build();
    }

    private TypeSpec binding(GirEnum type) {
        TypeSpec.Builder builder = TypeSpec.enumBuilder(type.name())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassNames.Enumeration)
                .addField(FieldSpec.builder(
                                ValueLayout.class, "MEMORY_LAYOUT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$T.GINT", ClassNames.Types)
                        .build())
                .addField(FieldSpec.builder(int.class, "value", Modifier.PRIVATE, Modifier.FINAL)
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(int.class, "value")
                        .addCode("this.value = value;\n")
                        .build())
                .addMethod(MethodSpec.methodBuilder("value")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(int.class)
                        .addCode("return value;\n")
                        .build());
        for (GirMember member : type.members()) {
            builder.addEnumConstant(
                    Support.toJavaSnakeCase(member.name()),
                    TypeSpec.anonymousClassBuilder("$L", member.value()).build());
        }
        return builder.build();
    }

    private TypeSpec binding(GirCallback type) {
        return TypeSpec.interfaceBuilder(type.name())
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(MemoryLayout.class, "MEMORY_LAYOUT")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer(layout(type))
                        .build())
                .build();
    }

    private TypeSpec classBinding(@Nullable GirRecord type, GirClass instanceType, PackageElement packageElement) {
        ClassName instanceImplName =
                ClassName.get(packageElement.getQualifiedName().toString(), instanceType.name() + "Impl");
        ClassName implName = instanceImplName.nestedClass("Class");
        TypeSpec.Builder builder = TypeSpec.classBuilder("Class").addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        if (type != null) {
            builder.addField(FieldSpec.builder(
                            MemoryLayout.class, "MEMORY_LAYOUT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$T.MEMORY_LAYOUT", implName)
                    .build());
        }
        if (instanceType.isAbstract()) {
            builder.addModifiers(Modifier.ABSTRACT);
        }
        if (instanceType.isFinal()) {
            builder.addModifiers(Modifier.FINAL);
        }
        if (instanceType.parent() != null) {
            builder.superclass(className(instanceType.parent()).nestedClass("Class"));
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

    private TypeSpec impl(GirNamespace ns) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(ns.name() + "Impl")
                .addModifiers(Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .build());
        addImpls(builder, ns.functions().values());
        return builder.build();
    }

    private TypeSpec impl(GirClass type, @Nullable GirRecord classType, PackageElement packageElement) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(type.name() + "Impl")
                .addModifiers(Modifier.FINAL)
                .addField(layoutField(type))
                .addType(classImpl(classType));
        if (!type.isAbstract()) {
            // TODO: type field for abstract classes
            builder.addField(classTypeField(type, packageElement));
        }
        addImpls(builder, type.constructors());
        addImpls(builder, type.methods());
        addImpls(builder, type.functions());
        return builder.build();
    }

    private TypeSpec impl(GirInterface type) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(type.name() + "Impl").addModifiers(Modifier.FINAL);
        builder.addField(layoutField(type));
        addImpls(builder, type.constructors());
        addImpls(builder, type.methods());
        addImpls(builder, type.functions());
        return builder.build();
    }

    private TypeSpec impl(GirRecord type) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(type.name() + "Impl").addModifiers(Modifier.FINAL);
        builder.addField(layoutField(type));
        addImpls(builder, type.constructors());
        addImpls(builder, type.methods());
        addImpls(builder, type.functions());
        return builder.build();
    }

    private TypeSpec impl(GirUnion type) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(type.name() + "Impl").addModifiers(Modifier.FINAL);
        builder.addField(layoutField(type));
        addImpls(builder, type.constructors());
        addImpls(builder, type.methods());
        addImpls(builder, type.functions());
        return builder.build();
    }

    private TypeSpec classImpl(@Nullable GirRecord type) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("Class").addModifiers(Modifier.STATIC, Modifier.FINAL);
        if (type != null) {
            builder.addField(layoutField(type));
            addImpls(builder, type.constructors());
            addImpls(builder, type.methods());
            addImpls(builder, type.functions());
        }
        return builder.build();
    }

    private void addImpls(TypeSpec.Builder typeBuilder, Iterable<? extends GirCallable> callables) {
        for (GirCallable callable : callables) {
            if (callable.name().isEmpty()) {
                messager.printWarning("Skipping binding for function with no name: " + callable.cIdentifier());
                continue;
            }
            if (callable.isVariadic()) {
                messager.printWarning("Skipping binding for variadic function: " + callable.cIdentifier());
                continue;
            }
            typeBuilder.addField(bindingField(callable));
            typeBuilder.addMethod(bindingMethod(callable));
        }
    }

    private CodeBlock layout(GirType type) {
        if (GirType.VA_LIST.equals(type) || type.isPointer()) {
            return CodeBlock.of("$T.ADDRESS", ValueLayout.class);
        } else if (type.isPrimitive()) {
            return CodeBlock.of("$T.$N", ClassNames.Types, type.name().local().toUpperCase(Locale.ROOT));
        } else {
            String packageName = packageName(type.name().ns());
            GirNamespace ns = girStore.namespace(type.name().ns()).orElse(null);
            ClassName className;
            if (ns != null && ns.classRecords().containsKey(type.name().local())) {
                String baseTypeName = ns.classRecords().get(type.name().local()).associatedClassName();
                className = ClassName.get(packageName, baseTypeName, "Class");
            } else {
                className = ClassName.get(packageName, type.name().local());
            }
            return CodeBlock.of("$T.MEMORY_LAYOUT", className);
        }
    }

    private CodeBlock layout(GirArrayType type) {
        if (type.fixedSize() != null) {
            return CodeBlock.of(
                    "$T.sequenceLayout($L, $L)", MemoryLayout.class, type.fixedSize(), layout(type.elementType()));
        } else {
            return CodeBlock.of("$T.ADDRESS", ValueLayout.class);
        }
    }

    private CodeBlock layout(GirCallback type) {
        return CodeBlock.of("$T.ADDRESS", ValueLayout.class);
    }

    private CodeBlock layout(GirAnyType type) {
        if (type instanceof GirArrayType arrayType) {
            return layout(arrayType);
        } else {
            return layout((GirType) type);
        }
    }

    private CodeBlock layout(GirField.Type type) {
        if (type instanceof GirCallback) {
            return layout((GirCallback) type);
        } else {
            return layout((GirAnyType) type);
        }
    }

    private CodeBlock structLayout(List<GirField> fields) {
        CodeBlock fieldLayouts = fields.stream()
                .map(field -> CodeBlock.of("$L.withName($S)", layout(field.type()), field.name()))
                .collect(CodeBlock.joining(", "));
        return CodeBlock.of("$T.structLayout($L)", ClassNames.BindingSupport, fieldLayouts);
    }

    private CodeBlock unionLayout(List<GirField> fields) {
        CodeBlock fieldLayouts = fields.stream()
                .map(field -> CodeBlock.of("$L.withName($S)", layout(field.type()), field.name()))
                .collect(CodeBlock.joining(", "));
        return CodeBlock.of("$T.unionLayout($L)", MemoryLayout.class, fieldLayouts);
    }

    private CodeBlock layout(GirClass type) {
        return structLayout(type.fields());
    }

    private CodeBlock layout(GirInterface type) {
        return structLayout(type.fields());
    }

    private CodeBlock layout(GirRecord type) {
        return structLayout(type.fields());
    }

    private CodeBlock layout(GirUnion type) {
        return unionLayout(type.fields());
    }

    private FieldSpec layoutField(GirClass type) {
        return FieldSpec.builder(MemoryLayout.class, "MEMORY_LAYOUT", Modifier.STATIC, Modifier.FINAL)
                .initializer(layout(type))
                .build();
    }

    private FieldSpec layoutField(GirInterface type) {
        return FieldSpec.builder(MemoryLayout.class, "MEMORY_LAYOUT", Modifier.STATIC, Modifier.FINAL)
                .initializer(layout(type))
                .build();
    }

    private FieldSpec layoutField(GirRecord type) {
        return FieldSpec.builder(MemoryLayout.class, "MEMORY_LAYOUT", Modifier.STATIC, Modifier.FINAL)
                .initializer(layout(type))
                .build();
    }

    private FieldSpec layoutField(GirUnion type) {
        return FieldSpec.builder(MemoryLayout.class, "MEMORY_LAYOUT", Modifier.STATIC, Modifier.FINAL)
                .initializer(layout(type))
                .build();
    }

    private FieldSpec classTypeField(GirClass type, PackageElement packageElement) {
        ClassName elemName = ClassName.get(packageElement.getQualifiedName().toString(), type.name());
        ClassName elemClassName = elemName.nestedClass("Class");
        TypeName classTypeName = classTypeName(type, packageElement);
        return FieldSpec.builder(classTypeName, "TYPE", Modifier.STATIC, Modifier.FINAL)
                .initializer(
                        "$T.ofTypeGetter($S, $T::new, $T::new)",
                        ClassNames.ClassType,
                        type.getType(),
                        elemClassName,
                        elemName)
                .build();
    }

    private TypeName classTypeName(GirClass type, PackageElement packageElement) {
        ClassName elemName = ClassName.get(packageElement.getQualifiedName().toString(), type.name());
        ClassName elemClassName = elemName.nestedClass("Class");
        return ParameterizedTypeName.get(ClassNames.ClassType, elemClassName, elemName);
    }

    private FieldSpec bindingField(GirCallable callable) {
        return FieldSpec.builder(MethodHandle.class, callable.cIdentifier(), Modifier.STATIC, Modifier.FINAL)
                .initializer(
                        "$T.lookup($S, $L)",
                        ClassNames.BindingSupport,
                        callable.cIdentifier(),
                        functionDescriptor(callable))
                .build();
    }

    private CodeBlock functionDescriptor(GirCallable callable) {
        List<CodeBlock> parameterLayouts = callable.parameters().stream()
                .map(parameter -> layout(parameter.type()))
                .toList();
        if (GirType.VOID.equals(callable.returnValue().type())) {
            return CodeBlock.of("$T.ofVoid($L)", FunctionDescriptor.class, CodeBlock.join(parameterLayouts, ", "));
        } else {
            return CodeBlock.of(
                    "$T.of($L)",
                    FunctionDescriptor.class,
                    Stream.concat(Stream.of(layout(callable.returnValue().type())), parameterLayouts.stream())
                            .collect(CodeBlock.joining(", ")));
        }
    }

    private MethodSpec bindingMethod(GirCallable callable) {
        Type returnType = javaBindingType(callable.returnValue().type(), false);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(Support.toJavaCamelCase(callable.name()))
                .addModifiers(Modifier.STATIC)
                .returns(returnType);
        List<CodeBlock> callParameters = new ArrayList<>();
        for (GirCallable.Parameter parameter : callable.parameters()) {
            String name = Support.toJavaCamelCase(parameter.name());
            builder.addParameter(javaBindingType(parameter.type(), true), name);
            callParameters.add(CodeBlock.of("$L", name));
        }

        CodeBlock invocationExpr =
                CodeBlock.of("$L.invokeExact($L)", callable.cIdentifier(), CodeBlock.join(callParameters, ", "));
        CodeBlock invocation;
        if (returnType == void.class) {
            invocation = CodeBlock.of("$L;\n", invocationExpr);
        } else {
            invocation = CodeBlock.of("return ($T) $L;\n", returnType, invocationExpr);
        }
        builder.beginControlFlow("try")
                .addCode(invocation)
                .nextControlFlow("catch (Throwable $$t)")
                .addCode("throw $T.sneakyThrow($$t);\n", ClassNames.BindingSupport)
                .endControlFlow();

        return builder.build();
    }

    private Type javaBindingType(GirAnyType type, boolean isParam) {
        if (type instanceof GirArrayType arrayType) {
            if (arrayType.fixedSize() != null) {
                return MemorySegment.class;
            } else {
                return isParam ? Addressable.class : MemoryAddress.class;
            }
        } else {
            GirType simpleType = (GirType) type;
            if (GirType.GCHAR.equals(simpleType)
                    || GirType.GUCHAR.equals(simpleType)
                    || GirType.GINT8.equals(simpleType)
                    || GirType.GUINT8.equals(simpleType)) {
                return byte.class;
            } else if (GirType.GBOOLEAN.equals(simpleType)) {
                return boolean.class;
            } else if (GirType.GUNICHAR2.equals(simpleType)) {
                return char.class;
            } else if (GirType.GSHORT.equals(simpleType)
                    || GirType.GUSHORT.equals(simpleType)
                    || GirType.GINT16.equals(simpleType)
                    || GirType.GUINT16.equals(simpleType)) {
                return short.class;
            } else if (GirType.GINT.equals(simpleType)
                    || GirType.GUINT.equals(simpleType)
                    || GirType.GINT32.equals(simpleType)
                    || GirType.GUINT32.equals(simpleType)
                    || GirType.GUNICHAR.equals(simpleType)) {
                return int.class;
            } else if (GirType.GLONG.equals(simpleType)
                    || GirType.GULONG.equals(simpleType)
                    || GirType.GINT64.equals(simpleType)
                    || GirType.GUINT64.equals(simpleType)
                    || GirType.GSIZE.equals(simpleType)
                    || GirType.GSSIZE.equals(simpleType)
                    || GirType.GTYPE.equals(simpleType)) {
                return long.class;
            } else if (GirType.GFLOAT.equals(simpleType)) {
                return float.class;
            } else if (GirType.GDOUBLE.equals(simpleType)) {
                return double.class;
            } else if (GirType.GPOINTER.cType().equals(simpleType.cType())
                    || GirType.GCONSTPOINTER.cType().equals(simpleType.cType())
                    || simpleType.isPointer()) {
                return isParam ? Addressable.class : MemoryAddress.class;
            } else if (GirType.VOID.equals(simpleType)) {
                return void.class;
            } else if (GirType.VA_LIST.equals(simpleType)) {
                return VaList.class;
            } else {
                GirNamespace ns = girStore.namespace(simpleType.name().ns()).orElse(null);
                if (ns == null) {
                    return MemorySegment.class;
                }
                if (ns.bitFields().containsKey(simpleType.name().local())
                        || ns.enums().containsKey(simpleType.name().local())) {
                    // Bit fields and enums are always internally ints
                    return int.class;
                }
                if (ns.callbacks().containsKey(simpleType.name().local())) {
                    return isParam ? Addressable.class : MemoryAddress.class;
                }
                GirAlias alias = ns.aliases().get(simpleType.name().local());
                if (alias != null) {
                    return javaBindingType(alias.type(), isParam);
                }
                return MemorySegment.class;
            }
        }
    }

    private String packageName(@Nullable String ns) {
        String packageName = namespacePackages.get(ns);
        if (packageName != null) {
            return packageName;
        } else {
            messager.printWarning("No package defined for namespace: " + ns);
            return Objects.requireNonNullElse(ns, "");
        }
    }

    private ClassName className(GirName name) {
        return ClassName.get(packageName(name.ns()), name.local());
    }

    private static final class ClassNames {
        static final ClassName Addressable = ClassName.get("dev.ianjohnson.guatemala.glib", "Addressable");
        static final ClassName BindingSupport = ClassName.get("dev.ianjohnson.guatemala.core", "BindingSupport");
        static final ClassName BitField = ClassName.get("dev.ianjohnson.guatemala.core", "BitField");
        static final ClassName ClassType = ClassName.get("dev.ianjohnson.guatemala.gobject", "ClassType");
        static final ClassName Enumeration = ClassName.get("dev.ianjohnson.guatemala.core", "Enumeration");
        static final ClassName ReferenceCounted = ClassName.get("dev.ianjohnson.guatemala.glib", "ReferenceCounted");
        static final ClassName Types = ClassName.get("dev.ianjohnson.guatemala.glib", "Types");

        private ClassNames() {}
    }
}
