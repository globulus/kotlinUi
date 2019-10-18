package net.globulus.kotlinui.processor.codegen;

import net.globulus.kotlinui.processor.ExposedClass;
import net.globulus.mmap.MergeInput;

import java.util.ArrayList;
import java.util.List;

public class Input implements MergeInput<Input> {

    public final List<ExposedClass> classes;

    public Input(List<ExposedClass> classes) {
//        ProcessorLog.warn(null, "MY FlAVOR " + flavorables.size());
//        for (String t : flavorables) {
//            ProcessorLog.warn(null, "MY FLAVOR " + t);
//        }
//        ProcessorLog.warn(null, "MY fis " + fis.size());
        this.classes = classes;
    }

    @Override
    public Input mergedUp(Input other) {
//        ProcessorLog.warn(null, "AAAA " + other.flavorables.size());
//        ProcessorLog.warn(null, "VVVV " + other.fis.size());
        List<ExposedClass> classes = new ArrayList<>(other.classes);
        classes.addAll(this.classes);
        return new Input(classes);
    }
}
