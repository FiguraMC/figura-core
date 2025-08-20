package org.figuramc.figura_core.comptime.lua;

import com.google.auto.service.AutoService;
import org.figuramc.figura_core.comptime.lua.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("org.figuramc.figura_core.comptime.lua.annotations.LuaTypeAPI")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class LuaTypeProcessor extends AbstractProcessor {

    // Mapping sending, for example:
    // ScriptCallback -> CallbackAPI
    // org.joml.Vector3d -> Vec3API
    // Etc, so we know where to call the wrap function
    private final List<TypeMirror[]> apiMap = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var elementUtils = processingEnv.getElementUtils();
        var typeUtils = processingEnv.getTypeUtils();

        // Iterate over all annotated elements
        for (TypeElement annotation : annotations) {

            // Build the API map, so we know where to find wrap() functions
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
                LuaTypeAPI luaTypeAPI = element.getAnnotation(LuaTypeAPI.class);
                TypeMirror userdataClass = null; // The wrappedClass() value for the annotated class
                try {
                    var whyIsThisTheAcceptedSolution = luaTypeAPI.wrappedClass();
                } catch (MirroredTypeException mte) {
                    userdataClass = typeUtils.erasure(mte.getTypeMirror());
                }
                apiMap.add(new TypeMirror[] { userdataClass, element.asType() });
            }

            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {

                LuaTypeAPI luaTypeAPI = element.getAnnotation(LuaTypeAPI.class);
                String typeName = luaTypeAPI.typeName(); // The type name, Lua-side

                TypeElement userdataClass = null;
                try {
                    var whyIsThisTheAcceptedSolution = luaTypeAPI.wrappedClass();
                } catch (MirroredTypeException mte) {
                    userdataClass = (TypeElement) typeUtils.asElement(mte.getTypeMirror());
                }

                // Ensure it's a typeElement
                if (element.getKind() != ElementKind.CLASS) {
                    throw new RuntimeException("Only classes may be annotated with @LuaTypeAPI, but found " + element);
                }
                TypeElement clazz = (TypeElement) element;

                // Log
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing LuaTypeAPI for class " + clazz.getQualifiedName());

                // Begin making the generated class.
                String generatedClassName = "API__" + typeName;
                try {
                    StringBuilder output = new StringBuilder();

                    // Package and imports
                    output.append("package org.figuramc.figura_core.script_languages.lua.generated;\n\n");

                    output.append("import org.figuramc.figura_cobalt.org.squiddev.cobalt.Constants;\n");
                    output.append("import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaState;\n");
                    output.append("import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaUserdata;\n");
                    output.append("import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable;\n");
                    output.append("import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaDouble;\n");
                    output.append("import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaInteger;\n");
                    output.append("import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaBoolean;\n");
                    output.append("import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaString;\n");
                    output.append("import org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaError;\n");
                    output.append("import org.figuramc.figura_cobalt.LuaUncatchableError;\n");
                    output.append("import org.figuramc.figura_cobalt.org.squiddev.cobalt.ErrorFactory;\n");
                    output.append("import org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LibFunction;\n\n");
                    output.append("import org.figuramc.figura_core.script_languages.lua.FiguraMetatables;\n");
                    output.append("import org.figuramc.figura_core.script_languages.lua.LuaRuntime;\n");
                    output.append("import org.jetbrains.annotations.NotNull;\n");

                    output.append("\n");

                    // Class definition
                    output.append("// Auto-generated file from @LuaTypeAPI annotation! Do not edit this file!\n");
                    output.append("public class " + generatedClassName + " {\n\n");

                    // createMetatable method
                    if (luaTypeAPI.hasSuperclass()) {
                        // If there's a superclass, add a param to the signature for it
                        output.append("    public static LuaTable createMetatable(LuaRuntime state, @NotNull LuaTable superclassMetatable) throws LuaError, LuaUncatchableError {\n");
                    } else {
                        output.append("    public static LuaTable createMetatable(LuaRuntime state) throws LuaError, LuaUncatchableError {\n");
                    }

                    output.append("        LuaTable metatable = new LuaTable(state.allocationTracker);\n");

                    // Look for @LuaConstant methods and set them up
                    for (Element enclosed : clazz.getEnclosedElements()) {
                        if (enclosed.getAnnotation(LuaConstant.class) == null) continue; // Only process @LuaConstant methods
                        if (enclosed.getKind() != ElementKind.METHOD) continue;
                        // Write value to the metatable!
                        output.append("        metatable.rawset(\"" + enclosed.getSimpleName() + "\", " + clazz.getQualifiedName() + "." + enclosed.getSimpleName() + "(state, metatable));\n");
                    }

                    // Examine methods, and collect them according to name.
                    Map<String, List<ExecutableElement>> methodsByName = new LinkedHashMap<>();
                    for (Element enclosed : clazz.getEnclosedElements()) {
                        if (enclosed.getKind() != ElementKind.METHOD) continue;
                        if (enclosed.getAnnotation(LuaConstant.class) != null) continue; // Already processed constants

                        ExecutableElement method = (ExecutableElement) enclosed;
                        LuaExpose expose = method.getAnnotation(LuaExpose.class);
                        if (expose == null) continue; // Skip non-exposed methods
                        if (!method.getModifiers().contains(Modifier.STATIC) || !method.getModifiers().contains(Modifier.PUBLIC))
                            throw new RuntimeException("Failed to process " + clazz.getQualifiedName() + "." + method.getSimpleName() + "(). Methods annotated with @LuaExpose must be \"public static\"");
                        String functionName = !expose.name().isEmpty() ? expose.name() : method.getSimpleName().toString();
                        methodsByName.computeIfAbsent(functionName, x -> new ArrayList<>()).add(method);
                    }

                    boolean hasCustomIndex = false;

                    // For each method, add it to the code.
                    for (var entry : methodsByName.entrySet()) {
                        String methodName = entry.getKey();
                        // Save __index for later
                        if (methodName.equals("__index")) {
                            hasCustomIndex = true;
                            continue;
                        }

                        // Check if it's a LuaDirect method
                        if (entry.getValue().getFirst().getAnnotation(LuaDirect.class) != null) {
                            // If so, wire it directly and continue.
                            if (entry.getValue().size() > 1) throw new RuntimeException("Methods annotated with @LuaDirect cannot be overloads!");
                            var method = entry.getValue().getFirst();
                            output.append("        metatable.rawset(\"" + methodName + "\", LibFunction.createV(" + clazz.getQualifiedName() + "::" + method.getSimpleName() + "));\n");
                            continue;
                        }

                        // Check if this is an instance method (Invoked with :), or a static method (Invoked with .)
                        Boolean isInstanceMethodTemp = null;
                        for (var methodImpl : entry.getValue()) {
                            boolean thisMethodIsInstance;
                            if (methodImpl.getAnnotation(LuaPassState.class) != null) {
                                // Check if second arg is of type <userdataClass>
                                thisMethodIsInstance = methodImpl.getParameters().size() >= 2 && typeUtils.isSameType(typeUtils.erasure(methodImpl.getParameters().get(1).asType()), typeUtils.erasure(userdataClass.asType()));
                            } else {
                                // Check if first arg is of type <userdataClass>
                                thisMethodIsInstance = !methodImpl.getParameters().isEmpty() && typeUtils.isSameType(typeUtils.erasure(methodImpl.getParameters().getFirst().asType()), typeUtils.erasure(userdataClass.asType()));
                            }
                            if (isInstanceMethodTemp == null) isInstanceMethodTemp = thisMethodIsInstance;
                            else if (isInstanceMethodTemp != thisMethodIsInstance) {
                                throw new RuntimeException("Inconsistent method-ness for \"" + methodName + "\" in \"" + generatedClassName + "\"");
                            }
                        }
                        boolean isInstanceMethod = Objects.requireNonNull(isInstanceMethodTemp);

                        // Call rawset() on the metatable with a lambda

                        output.append("        metatable.rawset(\"" + methodName + "\", ");
                        writeLuaLambda(
                                output, "            ",
                                methodName, entry.getValue(),
                                clazz, typeName, isInstanceMethod, generatedClassName,
                                typeUtils, elementUtils
                        );
                        output.append(");\n"); // End rawset
                    }

                    // End createMetatable method with some final setup

                    output.append("        metatable.rawset(Constants.NAME, LuaString.valueOfNoAlloc(\"" + typeName + "\"));\n"); // Set __name

                    // Set up indexing
                    if (luaTypeAPI.hasSuperclass()) {
                        if (hasCustomIndex) {
                            output.append("        FiguraMetatables.setupIndexingWithSuperclassAndCustomIndexer(state, metatable, superclassMetatable, ");
                            writeLuaLambda(
                                    output, "            ",
                                    "__index", methodsByName.get("__index"),
                                    clazz, typeName, true, generatedClassName,
                                    typeUtils, elementUtils
                            );
                            output.append(");\n");
                        } else {
                            output.append("        FiguraMetatables.setupIndexingWithSuperclass(state, metatable, superclassMetatable);\n");
                        }
                    } else {
                        if (hasCustomIndex) {
                            output.append("        FiguraMetatables.setupIndexingWithCustomIndexer(state, metatable, ");
                            writeLuaLambda(
                                    output, "            ",
                                    "__index", methodsByName.get("__index"),
                                    clazz, typeName, true, generatedClassName,
                                    typeUtils, elementUtils
                            );
                            output.append(");\n");
                        } else {
                            output.append("        FiguraMetatables.setupIndexing(state, metatable);\n");
                        }
                    }

                    output.append("        return metatable;\n");
                    output.append("    }\n");

                    // End class
                    output.append("}\n");

                    // If we made it this far, write the output.
                    Filer filer = processingEnv.getFiler();
                    JavaFileObject fileObject = filer.createSourceFile("org.figuramc.figura_core.script_languages.lua.generated." + generatedClassName);
                    try (Writer writer = fileObject.openWriter()) { writer.write(output.toString()); }
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to process class " + clazz, ex);
                }
            }
        }

        return true;
    }

    private void writeLuaLambda(
            StringBuilder output, String indent,
            String methodName, List<? extends ExecutableElement> methods,
            TypeElement clazz, String typeName, boolean isInstanceMethod, String generatedClassName,
            Types typeUtils, Elements elementUtils
    ) {

        output.append("LibFunction.createV((s, args) -> switch (args.count()) {\n");

        Set<Integer> argCountsUsed = new HashSet<>();
        for (ExecutableElement method : methods) {
            int argCount = method.getParameters().size();

            int argOffset = 0;

            boolean passState = method.getAnnotation(LuaPassState.class) != null;
            if (passState) {
                argCount--;
                argOffset--;
            }

            if (!(argCountsUsed.add(argCount))) {
                throw new RuntimeException("Multiple overloads of method " + methodName + " have " + argCount + " args!");
            }

            // Run the method. If it's not void, store the result in a temp local for conversion back to Lua.
            if (method.getReturnType().getKind() == TypeKind.VOID) {
                output.append(indent).append("case " + argCount + " -> {\n" + indent + "    " + clazz.getQualifiedName() + "." + method.getSimpleName() + "(");
            } else {
                output.append(indent).append("case " + argCount + " -> {\n" + indent + "    var result = " + clazz.getQualifiedName() + "." + method.getSimpleName() + "(");
            }

            // If we need to pass the state, pass it as the first param
            output.append(passState ? "(LuaRuntime) s, \n" : "\n");

            List<? extends VariableElement> params = method.getParameters();
            for (int i = 0; i < params.size(); i++) {
                if (i == 0 && passState) continue; // Skip first arg if passing state
                VariableElement param = params.get(i);
                // For each param, typecheck the corresponding arg.
                switch (param.asType().getKind()) {
                    case BOOLEAN -> output.append(indent).append("        args.arg(" + (i + 1 + argOffset) + ").checkBoolean(s)");
                    case INT -> output.append(indent).append("        args.arg(" + (i + 1 + argOffset) + ").checkInteger(s)");
                    case LONG -> output.append(indent).append("        args.arg(" + (i + 1 + argOffset) + ").checkLong(s)");
                    case DOUBLE -> output.append(indent).append("        args.arg(" + (i + 1 + argOffset) + ").checkDouble(s)");
                    case FLOAT -> output.append(indent).append("        (float) args.arg(" + (i + 1 + argOffset) + ").checkDouble(s)");
                    case DECLARED -> {
                        // Check for built-in supported types:
                        if (typeUtils.isSameType(elementUtils.getTypeElement("org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaValue").asType(), param.asType())) {
                            output.append(indent).append("        args.arg(" + (i + 1 + argOffset) + ")");
                        } else if (typeUtils.isSameType(elementUtils.getTypeElement("org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaTable").asType(), param.asType())) {
                            output.append(indent).append("        args.arg(" + (i + 1 + argOffset) + ").checkTable(s)");
                        } else if (typeUtils.isSameType(elementUtils.getTypeElement("org.figuramc.figura_cobalt.org.squiddev.cobalt.LuaString").asType(), param.asType())) {
                            output.append(indent).append("        args.arg(" + (i + 1 + argOffset) + ").checkLuaString(s)");
                        } else if (typeUtils.isSameType(elementUtils.getTypeElement("org.figuramc.figura_cobalt.org.squiddev.cobalt.function.LuaFunction").asType(), param.asType())) {
                            output.append(indent).append("        args.arg(" + (i + 1 + argOffset) + ").checkFunction(s)");
                        } else if (typeUtils.isSameType(elementUtils.getTypeElement("java.lang.String").asType(), param.asType())) {
                            output.append(indent).append("        args.arg(" + (i + 1 + argOffset) + ").checkString(s)");
                        } else {
                            // If it's none of those built-ins, assume it's a userdata type.
                            output.append(indent).append("        args.arg(" + (i + 1 + argOffset) + ").checkUserdata(s, " + ((TypeElement) ((DeclaredType) param.asType()).asElement()).getQualifiedName() + ".class)");
                        }
                    }
                    default -> throw new RuntimeException("Unrecognized type for Lua parameter conversion: " + param.asType());
                }
                output.append(i == params.size() - 1 ? "\n" : ",\n");
            }
            // End method call
            output.append(indent).append("    );\n");

            // If return self, return the first param. Otherwise convert result back to Lua
            if (method.getAnnotation(LuaReturnSelf.class) != null) {
                output.append(indent).append("    yield args.first();\n");
            } else switch (method.getReturnType().getKind()) {
                case VOID -> output.append(indent).append("    yield Constants.NONE;\n");
                case BOOLEAN -> output.append(indent).append("    yield LuaBoolean.valueOf(result);\n");
                case FLOAT, DOUBLE, LONG -> output.append(indent).append("    yield LuaDouble.valueOf(result);\n");
                case INT -> output.append(indent).append("    yield LuaInteger.valueOf(result);\n");
                case DECLARED -> {
                    // Check for built-in supported types.
                    if (typeUtils.isSubtype(method.getReturnType(), elementUtils.getTypeElement("org.figuramc.figura_cobalt.org.squiddev.cobalt.Varargs").asType())) {
                        // If it's a subtype of Varargs, return it immediately.
                        output.append(indent).append("    yield result;\n");
                        break;
                    }
                    // If it's null at runtime here, yield nil.
                    output.append(indent).append("    if (result == null) yield Constants.NIL;\n");

                    if (typeUtils.isSameType(elementUtils.getTypeElement("java.lang.String").asType(), method.getReturnType())) {
                        // If it's a string, convert to a LuaString.
                        output.append(indent).append("    yield LuaString.valueOf(result, s.allocationTracker);\n");
                    } else {
                        // If it's not a built-in, assume it's a userdata type...
                        TypeMirror returnType = typeUtils.erasure(method.getReturnType());
                        TypeMirror apiType = null;
                        for (int i = 0; i < apiMap.size(); i++) {
                            if (typeUtils.isSameType(returnType, apiMap.get(i)[0])) {
                                apiType = apiMap.get(i)[1];
                                break;
                            }
                        }
                        if (apiType == null) throw new RuntimeException("Attempt to return unexpected type \"" + method.getReturnType() + "\" from method \"" + methodName + "\" in class \"" + generatedClassName + "\"");

                        // Call the api class's wrap() method
                        output.append(indent).append("    yield " + ((TypeElement) typeUtils.asElement(apiType)).getQualifiedName() + ".wrap(result, (LuaRuntime) s);\n");
                    }
                }
                default -> throw new RuntimeException("Unrecognized type for Lua return value conversion: " + method.getReturnType());
            }
            // End switch case
            output.append(indent).append("}\n");
        }
        // Add a default case, which will error if an incorrect arg count is passed
        String totalName = typeName + (isInstanceMethod ? ":" : ".") + methodName + "()";
        String totalArgCount = "args.count()" + (isInstanceMethod ? " - 1" : "");
        String expectedArgCounts = methods.stream().map(x -> {
            int argCountAdjustment = 0;
            if (isInstanceMethod) argCountAdjustment -= 1; // Ignore self param
            if (x.getAnnotation(LuaPassState.class) != null) argCountAdjustment -= 1; // Ignore LuaState passed arg
            return x.getParameters().size() + argCountAdjustment;
        }).map(i -> ", " + i).collect(Collectors.joining());
        output.append(indent).append("default -> throw ErrorFactory.argCountError(s, \"" + totalName + "\", " + totalArgCount + expectedArgCounts + ");\n");
        // End switch and createV
        output.append(indent.substring(4)).append("})");
    }


}
