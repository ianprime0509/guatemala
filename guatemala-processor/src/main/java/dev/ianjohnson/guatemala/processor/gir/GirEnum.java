package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import dev.ianjohnson.guatemala.processor.ClassNames;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import dev.ianjohnson.guatemala.processor.Names;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.lang.model.element.Modifier;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.List;

public record GirEnum(String name, String getType, List<GirMember> members, List<GirCallable> functions)
        implements Named {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "enumeration".equals(element.getLocalName());
    }

    public static GirEnum load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        String getType = element.getAttributeNS(NS.GLIB, "get-type");
        List<GirMember> members = new ArrayList<>();
        List<GirCallable> functions = new ArrayList<>();
        for (Node child : Nodes.children(element)) {
            if (child instanceof Element e) {
                if (GirMember.canLoad(e)) {
                    members.add(GirMember.load(e));
                } else if (GirCallable.canLoadFunction(e)) {
                    functions.add(GirCallable.load(e, ns));
                }
            }
        }
        return new GirEnum(name, getType, List.copyOf(members), List.copyOf(functions));
    }

    public TypeSpec binding(CodegenContext ctx) {
        TypeSpec.Builder builder = TypeSpec.enumBuilder(name())
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
        for (GirMember member : members()) {
            builder.addEnumConstant(
                    Names.toJavaSnakeCase(member.name()),
                    TypeSpec.anonymousClassBuilder("$L", member.value()).build());
        }
        return builder.build();
    }
}
