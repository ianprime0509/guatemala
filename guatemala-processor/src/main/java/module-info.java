module dev.ianjohnson.guatemala.processor {
    requires static dev.ianjohnson.guatemala.annotation;
    requires static org.jetbrains.annotations;

    requires java.compiler;
    requires java.xml;
    requires com.squareup.javapoet;

    provides javax.annotation.processing.Processor with
            dev.ianjohnson.guatemala.processor.CodegenProcessor;
}
