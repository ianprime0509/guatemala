package dev.ianjohnson.guatemala.processor;

import com.squareup.javapoet.*;
import dev.ianjohnson.guatemala.annotation.*;
import dev.ianjohnson.guatemala.processor.gir.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

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
        for (Element elem : roundEnv.getElementsAnnotatedWith(ClassBinding.class)) {
            ClassBinding classBinding = elem.getAnnotation(ClassBinding.class);
            if (classBinding != null) {
                processClassBinding(classBinding, elem);
            }
        }

        return true;
    }

    private void processClassBinding(ClassBinding classBinding, Element elem) {
        if (elem.getKind() != ElementKind.CLASS) {
            messager.printError("@ClassBinding can only be applied to classes", elem);
            return;
        }
        processClassBinding(classBinding, (TypeElement) elem);
    }

    private void processClassBinding(ClassBinding classBinding, TypeElement elem) {
        PackageElement packageElem = elementUtils.getPackageOf(elem);
        loadGir(packageElem);
        NamespaceBinding namespaceBinding = packageElem.getAnnotation(NamespaceBinding.class);
        if (namespaceBinding == null) {
            messager.printError("Missing @NamespaceBinding", packageElem);
            return;
        }
        processNamespaceBinding(namespaceBinding, packageElem);
        GirNamespace namespace = girStore.namespace(namespaceBinding.value()).orElse(null);
        if (namespace == null) {
            messager.printError("No such namespace after loading GIR: " + namespaceBinding.value());
            return;
        }

        GirRecord girClassRecord = namespace.records().get(elem.getSimpleName() + "Class");
        TypeSpec classImpl = TypeSpec.classBuilder("Class")
                .addModifiers(Modifier.STATIC, Modifier.FINAL)
                .addField(layoutField(girClassRecord))
                .build();

        GirClass girClass = namespace.classes().get(elem.getSimpleName().toString());
        TypeSpec impl = TypeSpec.classBuilder(elem.getSimpleName() + "Impl")
                .addModifiers(Modifier.FINAL)
                .addField(layoutField(girClass))
                .addField(objectTypeField(girClass, elem))
                .addType(classImpl)
                .build();

        JavaFile javaFile = JavaFile.builder(packageElem.getQualifiedName().toString(), impl)
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write binding output: " + e, elem);
        }
    }

    private void processNamespaceBinding(NamespaceBinding namespaceBinding, PackageElement elem) {
        namespacePackages.put(namespaceBinding.value(), elem.getQualifiedName().toString());
        for (NamespaceDependency dep : namespaceBinding.dependencies()) {
            namespacePackages.put(dep.value(), dep.packageName());
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

    private CodeBlock layout(GirType type) {
        if (type.isPointer()) {
            return CodeBlock.of("$T.ADDRESS", ClassNames.ValueLayout);
        } else if (type.isPrimitive()) {
            return CodeBlock.of("$T.$N", ClassNames.Types, type.name().local().toUpperCase(Locale.ROOT));
        } else if (type.isClass()) {
            String baseTypeName =
                    type.name().local().substring(0, type.name().local().length() - 5);
            ClassName className =
                    ClassName.get(namespacePackages.get(type.name().ns()), baseTypeName, "Class");
            return CodeBlock.of("$T.LAYOUT", className);
        } else {
            ClassName className = ClassName.get(
                    namespacePackages.get(type.name().ns()), type.name().local());
            return CodeBlock.of("$T.LAYOUT", className);
        }
    }

    private CodeBlock layout(GirArrayType type) {
        if (type.fixedSize() != null) {
            return CodeBlock.of(
                    "$T.sequenceLayout($L, $L)", ClassNames.MemoryLayout, type.fixedSize(), layout(type.elementType()));
        } else {
            return CodeBlock.of("$T.ADDRESS", ClassNames.ValueLayout);
        }
    }

    private CodeBlock layout(GirAnyType type) {
        if (type instanceof GirArrayType arrayType) {
            return layout(arrayType);
        } else {
            return layout((GirType) type);
        }
    }

    private CodeBlock layout(GirFieldType type) {
        if (type instanceof GirCallback) {
            return CodeBlock.of("$T.ADDRESS", ClassNames.ValueLayout);
        } else {
            return layout((GirAnyType) type);
        }
    }

    private CodeBlock layout(List<GirField> fields) {
        List<CodeBlock> fieldLayouts = fields.stream()
                .map(field -> CodeBlock.of("$L.withName($S)", layout(field.type()), field.name()))
                .toList();
        return CodeBlock.of("$T.structLayout($L)", ClassNames.BindingSupport, CodeBlock.join(fieldLayouts, ", "));
    }

    private CodeBlock layout(GirClass type) {
        return layout(type.fields());
    }

    private CodeBlock layout(GirRecord type) {
        return layout(type.fields());
    }

    private FieldSpec layoutField(GirClass type) {
        return FieldSpec.builder(ClassNames.MemoryLayout, "LAYOUT", Modifier.STATIC, Modifier.FINAL)
                .initializer(layout(type))
                .build();
    }

    private FieldSpec layoutField(GirRecord type) {
        return FieldSpec.builder(ClassNames.MemoryLayout, "LAYOUT", Modifier.STATIC, Modifier.FINAL)
                .initializer(layout(type))
                .build();
    }

    private FieldSpec objectTypeField(GirClass type, TypeElement elem) {
        ClassName elemName = ClassName.get(elem);
        ClassName elemClassName = elemName.nestedClass("Class");
        ParameterizedTypeName objectType = ParameterizedTypeName.get(ClassNames.ObjectType, elemClassName, elemName);
        return FieldSpec.builder(objectType, "TYPE", Modifier.STATIC, Modifier.FINAL)
                .initializer(
                        "$T.ofTypeGetter($S, $T::new, $T::new)",
                        ClassNames.ObjectType,
                        type.getType(),
                        elemClassName,
                        elemName)
                .build();
    }

    private static final class ClassNames {
        static final ClassName BindingSupport = ClassName.get("dev.ianjohnson.guatemala.core", "BindingSupport");
        static final ClassName MemoryLayout = ClassName.get("java.lang.foreign", "MemoryLayout");
        static final ClassName ObjectType = ClassName.get("dev.ianjohnson.guatemala.gobject", "ObjectType");
        static final ClassName Types = ClassName.get("dev.ianjohnson.guatemala.glib", "Types");
        static final ClassName ValueLayout = ClassName.get("java.lang.foreign", "ValueLayout");

        private ClassNames() {}
    }
}
