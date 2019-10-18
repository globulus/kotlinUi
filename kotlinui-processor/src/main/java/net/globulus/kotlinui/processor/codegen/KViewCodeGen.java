package net.globulus.kotlinui.processor.codegen;

import com.squareup.kotlinpoet.ClassName;
import com.squareup.kotlinpoet.FileSpec;
import com.squareup.kotlinpoet.FunSpec;
import com.squareup.kotlinpoet.KModifier;
import com.squareup.kotlinpoet.PropertySpec;
import com.squareup.kotlinpoet.TypeSpec;

import net.globulus.kotlinui.annotation.State;
import net.globulus.kotlinui.processor.ExposedClass;
import net.globulus.kotlinui.processor.ExposedMethod;
import net.globulus.kotlinui.processor.ExposedProperty;
import net.globulus.kotlinui.processor.util.ProcessorLog;

import java.io.IOException;
import java.io.Writer;
import java.util.stream.Collectors;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;

import static javax.tools.StandardLocation.SOURCE_OUTPUT;

public class KViewCodeGen {

    public void generate(Filer filer, ExposedClass clazz) {
        try {
            final String instance = "instance";
            final String observables = "observables";
            String subclassName = clazz.name + "_Bound";
            String fileName = subclassName + ".kt";
            FileObject fo = filer.createResource(SOURCE_OUTPUT, clazz.packageName, fileName);
            ;
            ClassName originalClass = new ClassName(clazz.packageName, clazz.name);
            ClassName subclass = new ClassName(clazz.packageName, subclassName);

            ClassName context = new ClassName("android.content", "Context");
            ClassName behaviorSubject = new ClassName("io.reactivex.rxjava3.subjects", "BehaviorSubject");

            FunSpec.Builder constrBuilder =  FunSpec.constructorBuilder()
                    .addParameter("context", context)
                    .addParameter(instance, originalClass);

            for (ExposedProperty p : clazz.publicProperties.stream()
                    .filter(p -> p.annotations.contains(State.class))
                    .collect(Collectors.toList())) {
                constrBuilder.addStatement("%L.%L[%S] = %T.create<%T>()", instance, observables,
                        p.name, behaviorSubject, p.className);
            }

            TypeSpec.Builder mainBuilder = TypeSpec.classBuilder(subclassName)
                    .superclass(originalClass)
                    .addSuperclassConstructorParameter("context")
                    .primaryConstructor(constrBuilder.build())
                    .addProperty(PropertySpec
                            .builder(instance, originalClass, KModifier.PRIVATE)
                            .initializer(instance)
                            .build()
                    );

            for (ExposedProperty p : clazz.publicProperties) {
                PropertySpec.Builder propBuilder = p.toPropertySpecBuilder()
                        .addModifiers(KModifier.OVERRIDE)
                        .getter(FunSpec.getterBuilder()
                                .addStatement("return %L.%L", instance, p.name)
                                .build());
                if (p.mutable) {
                    FunSpec.Builder setterBuilder = FunSpec.setterBuilder()
                            .addParameter("value", p.className)
                            .addStatement("%L.%L = value", instance, p.name);
                    if (p.annotations.contains(State.class)) {
                        setterBuilder.addStatement("%L.triggerObserver(%S, value)", instance, p.name);
                    }
                    propBuilder.setter(setterBuilder.build());
                }
                mainBuilder.addProperty(propBuilder.build());
            }
            for (ExposedMethod m : clazz.publicMethods) {
                FunSpec.Builder funBuilder = m.toFunSpecBuilder();
                String invocation = String.format("instance.%s(%s)", m.name,
                        String.join(", ", m.getParamNames()));
                if (m.returns()) {
                    invocation = "return " + invocation;
                }
                funBuilder.addStatement(invocation);
                FunSpec f = funBuilder.build();
                ProcessorLog.warn(null, "Method " + f.toString());
                mainBuilder.addFunction(f);
            }
            ProcessorLog.warn(null, "All " + mainBuilder.build().toString());

            FileSpec fil =  FileSpec.builder(clazz.packageName, fileName)
                    .addType(mainBuilder.build())
                    .build();
            ProcessorLog.warn(null, fil.toString());
            try (Writer writer = fo.openWriter()) {
                writer.write(fil.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
