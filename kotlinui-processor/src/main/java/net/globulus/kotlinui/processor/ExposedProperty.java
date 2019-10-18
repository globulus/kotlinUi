package net.globulus.kotlinui.processor;

import com.squareup.kotlinpoet.ClassName;
import com.squareup.kotlinpoet.PropertySpec;

import java.lang.annotation.Annotation;
import java.util.List;

public class ExposedProperty {

    public final String name;
    public final String type;
    public final boolean mutable;
    public final List<Class<? extends Annotation>> annotations;
    public final ClassName className;

    public ExposedProperty(String name,
                           String type,
                           boolean mutable,
                           List<Class<? extends Annotation>> annotations) {
        this.name = name;
        this.type = type;
        this.mutable = mutable;
        this.annotations = annotations;
        this.className = ClassName.bestGuess(type);
    }

    public PropertySpec.Builder toPropertySpecBuilder() {
        return new PropertySpec.Builder(name, ClassName.bestGuess(type))
                .mutable(mutable);
    }
}
