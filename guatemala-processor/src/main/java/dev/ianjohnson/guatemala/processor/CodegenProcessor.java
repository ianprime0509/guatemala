package dev.ianjohnson.guatemala.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import dev.ianjohnson.guatemala.annotation.NamespaceBinding;
import dev.ianjohnson.guatemala.annotation.NamespaceDependency;
import dev.ianjohnson.guatemala.processor.gir.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("dev.ianjohnson.guatemala.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_19)
@SupportedOptions({CodegenProcessor.OPTION_GIR_DIRECTORY})
public final class CodegenProcessor extends AbstractProcessor {
    public static final String OPTION_GIR_DIRECTORY = "gir.directory";

    private Filer filer;
    private Messager messager;
    private Map<Path, GirRepository> repositories;

    private static Map<Path, GirRepository> loadRepositories(Path girDirectory) throws IOException {
        Map<Path, GirRepository> repositories = new HashMap<>();
        Files.walkFileTree(girDirectory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.getFileName().toString().endsWith(".gir")) {
                    repositories.put(file, GirRepository.load(file));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return Map.copyOf(repositories);
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        Path girDirectory = Path.of(processingEnv.getOptions().getOrDefault(OPTION_GIR_DIRECTORY, "."));
        try {
            repositories = loadRepositories(girDirectory);
        } catch (IOException e) {
            messager.printError("Error loading GIR repositories: " + e);
        }
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

    private CodegenContext codegenContext(NamespaceBinding namespaceBinding, PackageElement packageElement) {
        Map<String, GirNamespace> namespaces = repositories.values().stream()
                .flatMap(repository -> repository.namespaces().values().stream())
                .collect(Collectors.toMap(GirNamespace::name, Function.identity()));
        Map<String, String> namespacePackages = new HashMap<>();
        namespacePackages.put(
                namespaceBinding.value(), packageElement.getQualifiedName().toString());
        for (NamespaceDependency dependency : namespaceBinding.dependencies()) {
            namespacePackages.put(dependency.value(), dependency.packageName());
        }
        GirNamespace activeNamespace = namespaces.get(namespaceBinding.value());
        if (activeNamespace == null) {
            throw new IllegalStateException("No such namespace after loading GIR: " + namespaceBinding.value());
        }
        return new CodegenContext(namespaces, namespacePackages, activeNamespace, packageElement);
    }

    private void processNamespaceBinding(NamespaceBinding namespaceBinding, PackageElement packageElement) {
        CodegenContext ctx = codegenContext(namespaceBinding, packageElement);
        GirNamespace ns = ctx.activeNamespace();
        Set<String> definedTypes = packageElement.getEnclosedElements().stream()
                .map(e -> e.getSimpleName().toString())
                .collect(Collectors.toSet());
        writeOutput(ns, GirNamespace::impl, ctx);
        for (var entry : ns.aliases().entrySet()) {
            if (!GirType.VOID.equals(entry.getValue().type()) && !definedTypes.contains(entry.getKey())) {
                writeOutput(entry.getValue(), GirAlias::binding, ctx);
            }
        }
        for (var entry : ns.classes().entrySet()) {
            writeOutput(entry.getValue(), GirClass::impl, ctx);
            if (!definedTypes.contains(entry.getKey())) {
                writeOutput(entry.getValue(), GirClass::binding, ctx);
            }
        }
        for (var entry : ns.records().entrySet()) {
            if (!ns.classRecords().containsKey(entry.getKey())) {
                writeOutput(entry.getValue(), GirRecord::impl, ctx);
                if (!definedTypes.contains(entry.getKey())) {
                    writeOutput(entry.getValue(), GirRecord::binding, ctx);
                }
            }
        }
        for (var entry : ns.unions().entrySet()) {
            writeOutput(entry.getValue(), GirUnion::impl, ctx);
            if (!definedTypes.contains(entry.getKey())) {
                writeOutput(entry.getValue(), GirUnion::binding, ctx);
            }
        }
        for (var entry : ns.interfaces().entrySet()) {
            writeOutput(entry.getValue(), GirInterface::impl, ctx);
            if (!definedTypes.contains(entry.getKey())) {
                writeOutput(entry.getValue(), GirInterface::binding, ctx);
            }
        }
        for (var entry : ns.bitFields().entrySet()) {
            if (!definedTypes.contains(entry.getKey())) {
                writeOutput(entry.getValue(), GirBitField::binding, ctx);
            }
        }
        for (var entry : ns.enums().entrySet()) {
            if (!definedTypes.contains(entry.getKey())) {
                writeOutput(entry.getValue(), GirEnum::binding, ctx);
            }
        }
        for (var entry : ns.callbacks().entrySet()) {
            if (!definedTypes.contains(entry.getKey())) {
                writeOutput(entry.getValue(), GirCallback::binding, ctx);
            }
        }
    }

    private <T extends Named> void writeOutput(
            T type, BiFunction<T, CodegenContext, TypeSpec> generator, CodegenContext ctx) {
        TypeSpec generated;
        try {
            generated = generator.apply(type, ctx);
        } catch (Exception e) {
            messager.printError("Failed to generate binding output for " + type.name() + ": " + e, ctx.activePackage());
            return;
        }

        JavaFile javaFile = JavaFile.builder(
                        ctx.activePackage().getQualifiedName().toString(), generated)
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printError("Failed to write binding output for " + type.name() + ": " + e, ctx.activePackage());
        }
    }
}
