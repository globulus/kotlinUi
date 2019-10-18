package net.globulus.kotlinui.processor;

import com.squareup.kotlinpoet.ClassName;

import net.globulus.kotlinui.annotation.KotlinUiConfig;
import net.globulus.kotlinui.annotation.State;
import net.globulus.kotlinui.processor.codegen.Input;
import net.globulus.kotlinui.processor.codegen.KViewCodeGen;
import net.globulus.kotlinui.processor.util.FrameworkUtil;
import net.globulus.kotlinui.processor.util.ProcessorLog;
import net.globulus.mmap.MergeManager;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class Processor extends AbstractProcessor {

	private static final String NAME = "KotlinUi";
	private static final List<Class<? extends Annotation>> ANNOTATIONS = Arrays.asList(
			State.class
	);

	private Types mTypeUtils;
	private Filer mFiler;

	private long mTimestamp;

	private List<ExposedClass> mClasses = new ArrayList<>();
	private boolean mWroteOutput = false;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);

		ProcessorLog.init(env);

		mTypeUtils = env.getTypeUtils();
		mFiler = env.getFiler();

		mTimestamp = System.currentTimeMillis();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> types = new LinkedHashSet<>();
		for (Class<? extends Annotation> annotation : ANNOTATIONS) {
			types.add(annotation.getCanonicalName());
		}
		return types;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (mWroteOutput) {
			return true;
		}

		Boolean shouldMerge = null;
		Boolean foundSink = null;
		for (Element element : roundEnv.getElementsAnnotatedWith(KotlinUiConfig.class)) {
			KotlinUiConfig annotation = element.getAnnotation(KotlinUiConfig.class);
			if (annotation.source() && shouldMerge == null) {
				shouldMerge = false;
			}
			if (annotation.sink() && foundSink == null) {
				foundSink = true;
			}
		}

		Set<Element> classesForAnalysis = new HashSet<>();

		for (Element element : roundEnv.getElementsAnnotatedWith(State.class)) {
//			if (!isValidFlavorable(element)) {
//				continue;
//			}
            ProcessorLog.warn(element, "GOOD");
			classesForAnalysis.add(element.getEnclosingElement());
		}
		for (Element element : classesForAnalysis) {
			if (!isValidKViewClass(element)) {
				continue;
			}
			TypeElement typeElement = (TypeElement) element;
			List<ExposedMethod> publicMethods = analyzePublicMethods(typeElement);
			ClassName className = ClassName.bestGuess(typeElement.getQualifiedName().toString());
			mClasses.add(new ExposedClass(className.getPackageName(), className.getSimpleName(), publicMethods));
		}

		final boolean shouldMergeResolution = (shouldMerge == null);
		Input input = new Input(mClasses);
//		ProcessorLog.warn(null, "should merge " + shouldMergeResolution);
		MergeManager<Input> mergeManager = new MergeManager<Input>(mFiler, mTimestamp,
				FrameworkUtil.PACKAGE_NAME, NAME,
				() -> shouldMergeResolution)
				.setProcessorLog(new net.globulus.mmap.ProcessorLog() {
					@Override
					public void note(Element element, String s, Object... objects) {
						ProcessorLog.note(element, s, objects);
					}

					@Override
					public void warn(Element element, String s, Object... objects) {
						ProcessorLog.warn(element, s, objects);
					}

					@Override
					public void error(Element element, String s, Object... objects) {
						ProcessorLog.error(element, s, objects);
					}
				});
		input = mergeManager.manageMerging(input);

		if (foundSink != null) {
//			ProcessorLog.warn(null, "WRITING OUTPUT");
			KViewCodeGen codeGen = new KViewCodeGen();
			for (ExposedClass clazz : input.classes) {
				codeGen.generate(mFiler, clazz);
			}
//			new KotlinUiCodeGen().generate(mFiler, input);
			mWroteOutput = true;
		}

		return true;
	}

	private List<ExposedMethod> analyzePublicMethods(TypeElement element) {
		List<ExposedMethod> publicMethods = new ArrayList<>();
		for (Element enclosed : element.getEnclosedElements()) {
			if (enclosed.getKind() != ElementKind.METHOD
					|| !enclosed.getModifiers().contains(Modifier.PUBLIC)) {
				continue;
			}
			publicMethods.add(new ExposedMethod(enclosed, true));
		}
		return publicMethods;
	}

	private boolean isValidKViewClass(Element element) {
		if (element.getKind() == ElementKind.CLASS) {
			if (element.getModifiers().contains(Modifier.PRIVATE)) {
				ProcessorLog.error(element, "The private class %s's member is annotated with @%s. "
								+ "Private classes are not supported because of lacking visibility.",
						element.getSimpleName(), State.class.getSimpleName());
				return false;
			}
			if (element.getModifiers().contains(Modifier.FINAL)) {
				ProcessorLog.error(element, "The final class %s's member is annotated with @%s. "
								+ "Final classes are not supported.",
						element.getSimpleName(), State.class.getSimpleName());
				return false;
			}
			if (!isKViewSubclass(element)) {
				return false;
			}
			return true;
		}
		ProcessorLog.error(element, "%s is not a class. Only class fields can be annotated"
						+ " with %s.",
				element.getSimpleName(), State.class.getSimpleName());
		return false;
	}

	private boolean isKViewSubclass(Element element) {
		String kViewName = FrameworkUtil.KVIEW.getCanonicalName();
		for (TypeMirror directSuperType : mTypeUtils.directSupertypes(element.asType())) {
			List<TypeMirror> allSuperTypes = new ArrayList<>(mTypeUtils.directSupertypes(directSuperType));
			allSuperTypes.add(directSuperType);
			for (TypeMirror superType : allSuperTypes) {
				if (superType.toString().equals(kViewName)) {
					return true;
				}
			}
		}
		ProcessorLog.error(element,
				"Element %s does not extend the %s type.",
				element.getSimpleName(), kViewName);
		return false;
	}
}
