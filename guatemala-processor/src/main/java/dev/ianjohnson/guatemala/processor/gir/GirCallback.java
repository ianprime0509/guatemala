package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import org.w3c.dom.Element;

import javax.lang.model.element.Modifier;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.ValueLayout;

public record GirCallback(String name) implements GirField.Type, Named, Layoutable {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "callback".equals(element.getLocalName());
    }

    public static GirCallback load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        return new GirCallback(name);
    }

    public TypeSpec binding(CodegenContext ctx) {
        return TypeSpec.interfaceBuilder(name())
                .addModifiers(Modifier.PUBLIC)
                .addField(FieldSpec.builder(MemoryLayout.class, "MEMORY_LAYOUT")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer(memoryLayout(ctx))
                        .build())
                .build();
    }

    @Override
    public CodeBlock memoryLayout(CodegenContext ctx) {
        return CodeBlock.of("$T.ADDRESS", ValueLayout.class);
    }
}
