package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import dev.ianjohnson.guatemala.processor.Impls;
import org.w3c.dom.Element;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Map;

public record GirNamespace(
        String name,
        Map<String, GirAlias> aliases,
        Map<String, GirClass> classes,
        Map<String, GirRecord> classRecords,
        Map<String, GirInterface> interfaces,
        Map<String, GirRecord> records,
        Map<String, GirUnion> unions,
        Map<String, GirBitField> bitFields,
        Map<String, GirEnum> enums,
        Map<String, GirCallback> callbacks,
        Map<String, GirCallable> functions) implements Named {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "namespace".equals(element.getLocalName());
    }

    public static GirNamespace load(Element namespace) {
        String name = namespace.getAttributeNS(null, "name");
        Map<String, GirAlias> aliases = new HashMap<>();
        Map<String, GirClass> classes = new HashMap<>();
        Map<String, GirRecord> classRecords = new HashMap<>();
        Map<String, GirRecord> records = new HashMap<>();
        Map<String, GirUnion> unions = new HashMap<>();
        Map<String, GirInterface> interfaces = new HashMap<>();
        Map<String, GirBitField> bitFields = new HashMap<>();
        Map<String, GirEnum> enums = new HashMap<>();
        Map<String, GirCallback> callbacks = new HashMap<>();
        Map<String, GirCallable> functions = new HashMap<>();
        Nodes.stream(namespace.getChildNodes()).forEach(child -> {
            if (child instanceof Element e) {
                if (GirAlias.canLoad(e)) {
                    GirAlias loaded = GirAlias.load(e, name);
                    aliases.put(loaded.name(), loaded);
                } else if (GirClass.canLoad(e)) {
                    GirClass loaded = GirClass.load(e, name);
                    classes.put(loaded.name(), loaded);
                } else if (GirInterface.canLoad(e)) {
                    GirInterface loaded = GirInterface.load(e, name);
                    interfaces.put(loaded.name(), loaded);
                } else if (GirRecord.canLoad(e)) {
                    GirRecord loaded = GirRecord.load(e, name);
                    if (loaded.associatedClassName() != null) {
                        classRecords.put(loaded.associatedClassName(), loaded);
                    }
                    records.put(loaded.name(), loaded);
                } else if (GirUnion.canLoad(e)) {
                    GirUnion loaded = GirUnion.load(e, name);
                    unions.put(loaded.name(), loaded);
                } else if (GirBitField.canLoad(e)) {
                    GirBitField loaded = GirBitField.load(e, name);
                    bitFields.put(loaded.name(), loaded);
                } else if (GirEnum.canLoad(e)) {
                    GirEnum loaded = GirEnum.load(e, name);
                    enums.put(loaded.name(), loaded);
                } else if (GirCallback.canLoad(e)) {
                    GirCallback loaded = GirCallback.load(e, name);
                    callbacks.put(loaded.name(), loaded);
                } else if (GirCallable.canLoadFunction(e)) {
                    GirCallable loaded = GirCallable.load(e, name);
                    functions.put(loaded.name(), loaded);
                }
            }
        });
        return new GirNamespace(
                name,
                Map.copyOf(aliases),
                Map.copyOf(classes),
                Map.copyOf(classRecords),
                Map.copyOf(interfaces),
                Map.copyOf(records),
                Map.copyOf(unions),
                Map.copyOf(bitFields),
                Map.copyOf(enums),
                Map.copyOf(callbacks),
                Map.copyOf(functions));
    }

    public TypeSpec impl(CodegenContext ctx) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(name() + "Impl")
                .addModifiers(Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .build());
        Impls.addCallables(builder, ctx, functions().values());
        return builder.build();
    }
}
