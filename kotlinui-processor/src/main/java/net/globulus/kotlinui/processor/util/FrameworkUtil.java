package net.globulus.kotlinui.processor.util;

import com.squareup.kotlinpoet.ClassName;

public final class FrameworkUtil {

	public static final String PACKAGE_NAME = "net.globulus.kotlinui";
	public static final ClassName KVIEW = new ClassName(PACKAGE_NAME, "KView");
	public static final String BOUND_SUFFIX = "_Bound";

	private FrameworkUtil() { }
}
