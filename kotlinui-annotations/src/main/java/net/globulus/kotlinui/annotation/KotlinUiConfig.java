package net.globulus.kotlinui.annotation;

public @interface KotlinUiConfig {
    boolean source() default false;
    boolean sink() default false;
}
