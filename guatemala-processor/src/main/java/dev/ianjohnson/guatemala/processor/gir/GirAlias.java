package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import org.w3c.dom.Element;

import javax.lang.model.element.Modifier;
import java.lang.foreign.MemoryLayout;
import java.lang.reflect.Type;

public record GirAlias(String name, GirType type) implements Named {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "alias".equals(element.getLocalName());
    }

    public static GirAlias load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        GirType type = Nodes.streamChildren(element)
                .filter(child -> child instanceof Element e && GirType.canLoad(e))
                .map(child -> GirType.load((Element) child, ns))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("alias must contain a type"));
        return new GirAlias(name, type);
    }

    public TypeSpec binding(CodegenContext ctx) {
        Type wrappedType = type().impl(ctx);
        // TODO: could be a record once JavaPoet supports it
        return TypeSpec.classBuilder(name())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(FieldSpec.builder(
                                MemoryLayout.class, "MEMORY_LAYOUT", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer(type().memoryLayout(ctx))
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
}
