package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.CodeBlock;
import dev.ianjohnson.guatemala.processor.CodegenContext;

public interface Layoutable {
    CodeBlock memoryLayout(CodegenContext ctx);
}
