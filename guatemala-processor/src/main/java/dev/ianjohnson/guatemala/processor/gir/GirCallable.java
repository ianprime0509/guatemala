package dev.ianjohnson.guatemala.processor.gir;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import dev.ianjohnson.guatemala.processor.ClassNames;
import dev.ianjohnson.guatemala.processor.CodegenContext;
import dev.ianjohnson.guatemala.processor.Names;
import org.w3c.dom.Element;

import javax.lang.model.element.Modifier;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record GirCallable(String name, String cIdentifier, ReturnValue returnValue, List<Parameter> parameters)
        implements Named, Layoutable {
    public static boolean canLoad(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "callable".equals(element.getLocalName());
    }

    public static boolean canLoadConstructor(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "constructor".equals(element.getLocalName());
    }

    public static boolean canLoadMethod(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "method".equals(element.getLocalName());
    }

    public static boolean canLoadFunction(Element element) {
        return NS.CORE.equals(element.getNamespaceURI()) && "function".equals(element.getLocalName());
    }

    public static GirCallable load(Element element, String ns) {
        String name = element.getAttributeNS(null, "name");
        String cIdentifier = element.getAttributeNS(NS.C, "identifier");
        List<Parameter> parameters = Nodes.streamChildren(element)
                .filter(child -> child instanceof Element e && Parameters.canLoad(e))
                .map(child -> Parameters.load((Element) child, ns).parameters())
                .findFirst()
                .orElse(List.of());
        ReturnValue returnValue = Nodes.streamChildren(element)
                .filter(child -> child instanceof Element e && ReturnValue.canLoad(e))
                .map(child -> ReturnValue.load((Element) child, ns))
                .findFirst()
                .orElse(null);
        return new GirCallable(name, cIdentifier, returnValue, parameters);
    }

    public boolean isVariadic() {
        return !parameters.isEmpty()
                && GirType.VARARGS.equals(parameters.get(parameters.size() - 1).type());
    }

    public Impl impl(CodegenContext ctx) {
        return new Impl(methodHandleFieldImpl(ctx), methodImpl(ctx));
    }

    @Override
    public CodeBlock memoryLayout(CodegenContext ctx) {
        return CodeBlock.of("$T.ADDRESS", ValueLayout.class);
    }

    private FieldSpec methodHandleFieldImpl(CodegenContext ctx) {
        return FieldSpec.builder(MethodHandle.class, cIdentifier(), Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.lookup($S, $L)", ClassNames.BindingSupport, cIdentifier(), functionDescriptor(ctx))
                .build();
    }

    private CodeBlock functionDescriptor(CodegenContext ctx) {
        List<CodeBlock> parameterLayouts = parameters().stream()
                .map(parameter -> parameter.type().memoryLayout(ctx))
                .toList();
        if (GirType.VOID.equals(returnValue().type())) {
            return CodeBlock.of("$T.ofVoid($L)", FunctionDescriptor.class, CodeBlock.join(parameterLayouts, ", "));
        } else {
            return CodeBlock.of(
                    "$T.of($L)",
                    FunctionDescriptor.class,
                    Stream.concat(Stream.of(returnValue().type().memoryLayout(ctx)), parameterLayouts.stream())
                            .collect(CodeBlock.joining(", ")));
        }
    }

    private MethodSpec methodImpl(CodegenContext ctx) {
        Type returnType = returnValue().type().impl(ctx);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(Names.toJavaCamelCase(name()))
                .addModifiers(Modifier.STATIC)
                .returns(returnType);
        List<CodeBlock> callParameters = new ArrayList<>();
        for (GirCallable.Parameter parameter : parameters()) {
            String name = Names.toJavaCamelCase(parameter.name());
            builder.addParameter(parameter.type().paramImpl(ctx), name);
            callParameters.add(CodeBlock.of("$L", name));
        }

        CodeBlock invocationExpr =
                CodeBlock.of("$L.invokeExact($L)", cIdentifier(), CodeBlock.join(callParameters, ", "));
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

    public record ReturnValue(GirAnyType type) {
        public static boolean canLoad(Element element) {
            return NS.CORE.equals(element.getNamespaceURI()) && "return-value".equals(element.getLocalName());
        }

        public static ReturnValue load(Element element, String ns) {
            GirAnyType type = Nodes.streamChildren(element)
                    .filter(child -> child instanceof Element e && GirAnyType.canLoad(e))
                    .map(child -> GirAnyType.load((Element) child, ns))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("return-value must contain a type"));
            return new ReturnValue(type);
        }
    }

    public record Parameter(String name, GirAnyType type, boolean isInstance) {
        public static boolean canLoad(Element element) {
            return NS.CORE.equals(element.getNamespaceURI())
                    && ("parameter".equals(element.getLocalName())
                            || "instance-parameter".equals(element.getLocalName()));
        }

        public static Parameter load(Element element, String ns) {
            String name = element.getAttributeNS(null, "name");
            GirAnyType type = Nodes.streamChildren(element)
                    .flatMap(child -> {
                        if (child instanceof Element e) {
                            if (GirAnyType.canLoad(e)) {
                                return Stream.of(GirAnyType.load(e, ns));
                            } else if (NS.CORE.equals(e.getNamespaceURI()) && "varargs".equals(e.getLocalName())) {
                                return Stream.of(GirType.VARARGS);
                            }
                        }
                        return Stream.empty();
                    })
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("parameter must contain a type"));
            boolean isInstance = "instance-parameter".equals(element.getLocalName());
            return new Parameter(name, type, isInstance);
        }
    }

    public record Impl(FieldSpec methodHandleField, MethodSpec method) {}

    private record Parameters(List<Parameter> parameters) {
        public static boolean canLoad(Element element) {
            return NS.CORE.equals(element.getNamespaceURI()) && "parameters".equals(element.getLocalName());
        }

        public static Parameters load(Element element, String ns) {
            List<Parameter> parameters = Nodes.streamChildren(element)
                    .filter(child -> child instanceof Element e && Parameter.canLoad(e))
                    .map(child -> Parameter.load((Element) child, ns))
                    .toList();
            return new Parameters(parameters);
        }
    }
}
