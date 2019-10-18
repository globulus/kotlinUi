package net.globulus.kotlinui.processor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExposedClass implements Serializable {

    private static final String GET = "get";
    private static final String SET = "set";
    private static final int NAME_START_INDEX = GET.length();

    public final String packageName;
    public final String name;
    public final List<ExposedMethod> publicMethods;
    public final List<ExposedProperty> publicProperties;

    public ExposedClass(String packageName,
                        String name,
                        List<ExposedMethod> publicMethods) {
        this.packageName = packageName;
        this.name = name;
        this.publicMethods = publicMethods;
        this.publicProperties = dedupeProperties();
        dedupeGetters();
    }

    private List<ExposedProperty> dedupeProperties() {
        List<ExposedProperty> props = new ArrayList<>();
        List<ExposedMethod> toRemove = new ArrayList<>();
        for (int i = 0; i < publicMethods.size() - 1; i++) {
            for (int j = i + 1; j < publicMethods.size(); j++) {
                ExposedMethod m1 = publicMethods.get(i);
                ExposedMethod m2 = publicMethods.get(j);
                ExposedMethod setter = arePropMethods(m1, m2);
                if (setter != null) {
                    toRemove.add(m1);
                    toRemove.add(m2);
                    props.add(new ExposedProperty(stripMethodPrefix(setter.name),
                            setter.params.get(0), true, setter.annotations));
                }
            }
        }
        publicMethods.removeAll(toRemove);
        return props;
    }

    // returns setter
    private ExposedMethod arePropMethods(ExposedMethod m1, ExposedMethod m2) {
        ExposedMethod setter = null;
        if (m1.name.startsWith(SET)) {
            setter = m1;
        } else if (m2.name.startsWith(SET)) {
            setter = m2;
        }
        ExposedMethod getter = null;
        if (m1.name.startsWith(GET)) {
            getter = m1;
        } else if (m2.name.startsWith(GET)) {
            getter = m2;
        }
        if (setter == null || getter == null) {
            return null;
        }
        if (setter.returns() || !getter.returns()) {
            return null;
        }
        if (!m1.name.substring(NAME_START_INDEX).toLowerCase()
                .equals(m2.name.substring(NAME_START_INDEX).toLowerCase())) {
            return null;
        }
        if (setter.params.size() != 2 || !getter.params.isEmpty()) {
            return null;
        }
        if (!getter.returnType.equals(setter.params.get(0))) {
            return null;
        }
        return setter;
    }

    private void dedupeGetters() {
        List<ExposedMethod> toRemove = new ArrayList<>();
        for (ExposedMethod m : publicMethods) {
            if (m.name.startsWith(GET) && m.returns() && m.params.isEmpty()) {
                publicProperties.add(new ExposedProperty(stripMethodPrefix(m.name), m.returnType,
                        false, m.annotations));
                toRemove.add(m);
            }
        }
        publicMethods.removeAll(toRemove);
    }

    private String stripMethodPrefix(String name) {
        return name.substring(NAME_START_INDEX, NAME_START_INDEX + 1).toLowerCase()
                + name.substring(NAME_START_INDEX + 1);
    }
}
