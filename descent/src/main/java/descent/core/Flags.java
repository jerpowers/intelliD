/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added constant AccDefault
 *     IBM Corporation - added constants AccBridge and AccVarargs for J2SE 1.5 
 *******************************************************************************/
package descent.core;


/**
 * Utility class for decoding modifier flags in Java elements.
 * <p>
 * This class provides static methods only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 * <p>
 * Note that the numeric values of these flags match the ones for class files
 * as described in the Java Virtual Machine Specification. The AST class
 * <code>Modifier</code> provides the same functionality as this class, only in
 * the <code>descent.core.dom</code> package.
 * </p>
 *
 * @see IMember#getFlags()
 */
public final class Flags {

	/**
	 * Constant representing the absence of any flag
	 * @since 3.0
	 */
	public static final int AccDefault = 0x0000;
	/**
	 * Private access flag.
	 * @since 2.0
	 */
	public static final int AccPrivate = 0x0001;
	/**
	 * Package access flag.
	 * @since 2.0
	 */
	public static final int AccPackage = 0x0002;
	/**
	 * Protected access flag.
	 * @since 2.0
	 */
	public static final int AccProtected = 0x0004;
	/**
	 * Public access flag.
	 * @since 2.0
	 */
	public static final int AccPublic = 0x0008;
	/**
	 * Export access flag.
	 * @since 2.0
	 */
	public static final int AccExport = 0x0010;
	/**
	 * Static property flag.
	 * @since 2.0
	 */
	public static final int AccStatic = 0x0020;
	/**
	 * Final property flag.
	 * @since 2.0
	 */
	public static final int AccFinal = 0x0040;
	/**
	 * Abstract property flag.
	 * @since 2.0
	 */
	public static final int AccAbstract = 0x0080;
	/**
	 * Override property flag.
	 * @since 2.0
	 */
	public static final int AccOverride = 0x0100;
	/**
	 * Auto property flag.
	 * @since 2.0
	 */
	public static final int AccAuto = 0x0200;
	/**
	 * Synchronized property flag.
	 * @since 2.0
	 */
	public static final int AccSynchronized = 0x0400;
	/**
	 * Deprecated property flag.
	 * @since 2.0
	 */
	public static final int AccDeprecated = 0x0800;
	/**
	 * Extern property flag.
	 * @since 2.0
	 */
	public static final int AccExtern = 0x1000;
	/**
	 * Const property flag.
	 * @since 2.0
	 */
	public static final int AccConst = 0x80000000;
	/**
	 * Scope property flag.
	 * @since 2.0
	 */
	public static final int AccScope = 0x4000;
	
	// Extensions for passage modes
	
	/**
	 * In property flag.
	 * @since 2.0
	 */
	public static final int AccIn = 0x00010000;
	
	/**
	 * Out property flag.
	 * @since 2.0
	 */
	public static final int AccOut = 0x00020000;
	
	/**
	 * Inout property flag.
	 * @since 2.0
	 */
	public static final int AccInout = 0x00040000;
	
	/**
	 * Lazy property flag.
	 * @since 2.0
	 */
	public static final int AccLazy = 0x00080000;
	
	/**
	 * Ref property flag.
	 * @since 2.0
	 */
	public static final int AccRef = 0x00100000;
	
	/**
	 * Varargs property flag.
	 * @since 2.0
	 */
	public static final long AccVarargs1 = 0x02000000L;
	
	/**
	 * Varargs property flag.
	 * @since 2.0
	 */
	public static final long AccVarargs2 = 0x04000000L;
	
	// Extensions for types
	
	/**
	 * Enum property flag.
	 * @since 2.0
	 */
	public static final int AccEnum = 0x8000;
	
	/**
	 * Interface property flag.
	 * @since 2.0
	 */
	public static final int AccInterface = 0x00010000;
	/**
	 * Struct property flag.
	 * @since 2.0
	 */
	public static final int AccStruct = 0x00020000;
	/**
	 * Union property flag.
	 * @since 2.0
	 */
	public static final int AccUnion = 0x00040000;
	/**
	 * Template property flag.
	 * @since 2.0
	 */
	public static final int AccTemplate = 0x00100000;
	/**
	 * Class property flag.
	 * @since 2.0
	 */
	public static final int AccClass = 0x00200000;
	
	// Extensions for methods (can reuse flags)
	
	/**
	 * Destructor property flag.
	 * @since 2.0
	 */
	public static final int AccConstructor = 0x00010000;
	
	/**
	 * Destructor property flag.
	 * @since 2.0
	 */
	public static final int AccDestructor = 0x00020000;
	
	/**
	 * New property flag.
	 * @since 2.0
	 */
	public static final int AccNew = 0x00040000;
	
	/**
	 * Delete property flag.
	 * @since 2.0
	 */
	public static final int AccDelete = 0x00080000;
	
	/**
	 * Postblit property flag.
	 * @since 2.0
	 */
	public static final int AccPostBlit = 0x00200000; // 0x00100000 is already taken by AccTemplate
	
	// Extensions for fields (can reuse flags)
	
	/**
	 * Alias property flag.
	 * @since 2.0
	 */
	public static final int AccAlias = 0x00010000;
	/**
	 * Typedef property flag.
	 * @since 2.0
	 */
	public static final int AccTypedef = 0x00020000;
	/**
	 * Template mixin property flag.
	 * @since 2.0
	 */
	public static final int AccTemplateMixin = 0x00040000;	
	
	// Extensions for initializers (can reuse flags)
	
	/**
	 * Mixin property flag.
	 * @since 2.0
	 */
	public static final int AccStaticDestructor = 0x00010000;
	
	/**
	 * Thread property flag.
	 * @since 2.0
	 */
	public static final int AccThread = 0x8000000;
	
	/**
	 * Nothrow property flag.
	 * @since 2.0
	 */
	public static final int AccNothrow = 0x10000000;
	
	/**
	 * Pure property flag.
	 * @since 2.0
	 */
	public static final int AccPure = 0x20000000;
	
	/**
	 * Gshared property flag.
	 * @since 2.0
	 */
	public static final int AccGshared = 0x20000000;
	
	/**
	 * Invariant property flag.
	 * @since 2.0
	 */
	public static final int AccInvariant = 0x40000000;
	
	/**
	 * Shared property flag.
	 * @since 2.0
	 */
	public static final int AccShared = 0x04000000;
	
	/**
	 * Immutable property flag.
	 * @since 2.0
	 */
	public static final int AccImmutable = AccInvariant;
	
	/**
	 * UnitTest property flag.
	 * @since 2.0
	 */
	public static final int AccUnitTest = 0x00040000;
	
	/**
	 * Static assert property flag.
	 * @since 2.0
	 */
	public static final int AccStaticAssert = 0x00080000;
	
	/**
	 * Debug assignment property flag.
	 * @since 2.0
	 */
	public static final int AccDebugAssignment = 0x00100000;
	
	/**
	 * Version assignment property flag.
	 * @since 2.0
	 */
	public static final int AccVersionAssignment = 0x00200000;
	
	/**
	 * Align property flag.
	 * @since 2.0
	 */
	public static final int AccAlign = 0x00400000;
	
	/**
	 * Extern declaration property flag.
	 * @since 2.0
	 */
	public static final int AccExternDeclaration = 0x00800000;
	
	/**
	 * Pragma property flag.
	 * @since 2.0
	 */
	public static final int AccPragma = 0x01000000;
	
	/**
	 * Then property flag.
	 * @since 2.0
	 */
	public static final int AccThen = 0x02000000;
	
	/**
	 * Else property flag.
	 * @since 2.0
	 */
	public static final int AccElse = 0x04000000;
	/**
	 * Mixin property flag.
	 * @since 2.0
	 */
	public static final int AccMixin = 0x08000000;
	
	// Extensions for conditionals (can reuse flags)
	
	/**
	 * Iftype declaration property flag.
	 * @since 2.0
	 */
	public static final int AccIftypeDeclaration = 0x00010000;
	
	/**
	 * Static declaration if property flag.
	 * @since 2.0
	 */
	public static final int AccStaticIfDeclaration = 0x00020000;
	
	/**
	 * Version declaration property flag.
	 * @since 2.0
	 */
	public static final int AccVersionDeclaration = 0x00040000;

	/**
	 * Not instantiable.
	 */
	private Flags() {
		// Not instantiable
	}
	
	/**
	 * Returns whether the given integer includes the <code>private</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>private</code> modifier is included
	 */
	public static boolean isPrivate(long flags) {
		return (flags & AccPrivate) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>package</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>package</code> modifier is included
	 */
	public static boolean isPackage(long flags) {
		return (flags & AccPackage) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>protected</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>protected</code> modifier is included
	 */
	public static boolean isProtected(long flags) {
		return (flags & AccProtected) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>public</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>public</code> modifier is included
	 */
	public static boolean isPublic(long flags) {
		return (flags & AccPublic) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>export</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>export</code> modifier is included
	 */
	public static boolean isExport(long flags) {
		return (flags & AccExport) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>static</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>static</code> modifier is included
	 */
	public static boolean isStatic(long flags) {
		return (flags & AccStatic) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>final</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>final</code> modifier is included
	 */
	public static boolean isFinal(long flags) {
		return (flags & AccFinal) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>abstract</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>abstract</code> modifier is included
	 */
	public static boolean isAbstract(long flags) {
		return (flags & AccAbstract) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>override</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>override</code> modifier is included
	 */
	public static boolean isOverride(long flags) {
		return (flags & AccOverride) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>auto</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>auto</code> modifier is included
	 */
	public static boolean isAuto(long flags) {
		return (flags & AccAuto) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>synchronized</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>synchronized</code> modifier is included
	 */
	public static boolean isSynchronized(long flags) {
		return (flags & AccSynchronized) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>deprecated</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>deprecated</code> modifier is included
	 */
	public static boolean isDeprecated(long flags) {
		return (flags & AccDeprecated) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>extern</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>extern</code> modifier is included
	 */
	public static boolean isExtern(long flags) {
		return (flags & AccExtern) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>const</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>const</code> modifier is included
	 */
	public static boolean isConst(long flags) {
		return (flags & AccConst) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>__thread</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>__thread</code> modifier is included
	 */
	public static boolean isThread(long flags) {
		return (flags & AccThread) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>scope</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>scope</code> modifier is included
	 */
	public static boolean isScope(long flags) {
		return (flags & AccScope) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>enum</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>enum</code> modifier is included
	 */
	public static boolean isEnum(long flags) {
		return (flags & AccEnum) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>interface</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>interface</code> modifier is included
	 */
	public static boolean isInterface(long flags) {
		return (flags & AccInterface) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>struct</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>struct</code> modifier is included
	 */
	public static boolean isStruct(long flags) {
		return (flags & AccStruct) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>union</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>union</code> modifier is included
	 */
	public static boolean isUnion(long flags) {
		return (flags & AccUnion) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>template</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>template</code> modifier is included
	 */
	public static boolean isTemplate(long flags) {
		return (flags & AccTemplate) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>class</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>enum</code> modifier is included
	 */
	public static boolean isClass(long flags) {
		return (flags & AccClass) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>alias</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>alias</code> modifier is included
	 */
	public static boolean isAlias(long flags) {
		return (flags & AccAlias) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>typedef</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>typedef</code> modifier is included
	 */
	public static boolean isTypedef(long flags) {
		return (flags & AccTypedef) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>template mixin</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>template mixin</code> modifier is included
	 */
	public static boolean isTemplateMixin(long flags) {
		return (flags & AccTemplateMixin) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>mixin</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>mixin</code> modifier is included
	 */
	public static boolean isMixin(long flags) {
		return (flags & AccMixin) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>static destructor</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>static destructor</code> modifier is included
	 */
	public static boolean isStaticDestructor(long flags) {
		return (flags & AccStaticDestructor) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>constructor</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>constructor</code> modifier is included
	 */
	public static boolean isConstructor(long flags) {
		return (flags & AccConstructor) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>destructor</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>destructor</code> modifier is included
	 */
	public static boolean isDestructor(long flags) {
		return (flags & AccDestructor) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>new</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>new</code> modifier is included
	 */
	public static boolean isNew(long flags) {
		return (flags & AccNew) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>delete</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>delete</code> modifier is included
	 */
	public static boolean isDelete(long flags) {
		return (flags & AccDelete) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>postblit</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>postblit</code> modifier is included
	 */
	public static boolean isPostBlit(long flags) {
		return (flags & AccPostBlit) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>invariant</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>invariant</code> modifier is included
	 */
	public static boolean isImmutable(long flags) {
		return (flags & AccImmutable) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>nothrow</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>nothrow</code> modifier is included
	 */
	public static boolean isNothrow(long flags) {
		return (flags & AccNothrow) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>pure</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>pure</code> modifier is included
	 */
	public static boolean isPure(long flags) {
		return (flags & AccPure) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>unittest</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>unittest</code> modifier is included
	 */
	public static boolean isUnitTest(long flags) {
		return (flags & AccUnitTest) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>static assert</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>static assert</code> modifier is included
	 */
	public static boolean isStaticAssert(long flags) {
		return (flags & AccStaticAssert) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>version assignment</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>version assignment</code> modifier is included
	 */
	public static boolean isVersionAssignment(long flags) {
		return (flags & AccVersionAssignment) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>varargs</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>varargs</code> modifier is included
	 */
	public static boolean isVarargs1(long flags) {
		return (flags & AccVarargs1) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>varargs</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>varargs</code> modifier is included
	 */
	public static boolean isVarargs2(long flags) {
		return (flags & AccVarargs2) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>debug assignment</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>debug assignment</code> modifier is included
	 */
	public static boolean isDebugAssignment(long flags) {
		return (flags & AccDebugAssignment) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>align</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>align</code> modifier is included
	 */
	public static boolean isAlign(long flags) {
		return (flags & AccAlign) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>extern declaration</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>extern declaration</code> modifier is included
	 */
	public static boolean isExternDeclaration(long flags) {
		return (flags & AccExternDeclaration) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>pragma</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>pragma</code> modifier is included
	 */
	public static boolean isPragma(long flags) {
		return (flags & AccPragma) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>then</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>then</code> modifier is included
	 */
	public static boolean isThen(long flags) {
		return (flags & AccThen) != 0;
	}
	
	/**
	 * Returns whether the given integer includes the <code>else</code> modifier.
	 *
	 * @param flags the flags
	 * @return <code>true</code> if the <code>else</code> modifier is included
	 */
	public static boolean isElse(long flags) {
		return (flags & AccElse) != 0;
	}
	
	public static boolean isIftypeDeclaration(long flags) {
		return (flags & AccIftypeDeclaration) != 0;
	}
	
	public static boolean isStaticIfDeclaration(long flags) {
		return (flags & AccStaticIfDeclaration) != 0;
	}
	
	public static boolean isVersionDeclaration(long flags) {
		return (flags & AccVersionDeclaration) != 0;
	}
	
	/**
	 * Returns a standard string describing the given modifier flags.
	 * @param flags the flags
	 * @return the standard string representation of the given flags
	 */
	public static String toString(long flags) {
		StringBuffer sb = new StringBuffer();

		if (isPrivate(flags))
			sb.append("private "); //$NON-NLS-1$
		if (isPackage(flags))
			sb.append("package "); //$NON-NLS-1$
		if (isProtected(flags))
			sb.append("protected "); //$NON-NLS-1$
		if (isPublic(flags))
			sb.append("public "); //$NON-NLS-1$
		if (isExport(flags))
			sb.append("export "); //$NON-NLS-1$
		if (isStatic(flags))
			sb.append("static "); //$NON-NLS-1$
		if (isFinal(flags))
			sb.append("final "); //$NON-NLS-1$
		if (isAbstract(flags))
			sb.append("abstract "); //$NON-NLS-1$
		if (isOverride(flags))
			sb.append("override "); //$NON-NLS-1
		if (isAuto(flags))
			sb.append("auto "); //$NON-NLS-1$
		if (isSynchronized(flags))
			sb.append("synchronized "); //$NON-NLS-1$
		if (isDeprecated(flags))
			sb.append("deprecated "); //$NON-NLS-1$
		if (isExtern(flags))
			sb.append("extern "); //$NON-NLS-1$
		if (isConst(flags))
			sb.append("const "); //$NON-NLS-1$
		if (isScope(flags))
			sb.append("scope "); //$NON-NLS-1$
		if (isEnum(flags))
			sb.append("enum "); //$NON-NLS-1$
		if (isInterface(flags))
			sb.append("interface "); //$NON-NLS-1$
		if (isStruct(flags))
			sb.append("struct "); //$NON-NLS-1$
		if (isUnion(flags))
			sb.append("union "); //$NON-NLS-1$
		if (isTemplate(flags))
			sb.append("template "); //$NON-NLS-1$
		if (isAlias(flags))
			sb.append("alias "); //$NON-NLS-1$
		if (isTypedef(flags))
			sb.append("typedef "); //$NON-NLS-1$
		if (isTemplateMixin(flags))
			sb.append("mixin "); //$NON-NLS-1$
		if (isStaticDestructor(flags))
			sb.append("~static this() "); //$NON-NLS-1$
		if (isConstructor(flags))
			sb.append("this() "); //$NON-NLS-1$
		if (isDestructor(flags))
			sb.append("~this() "); //$NON-NLS-1$
		if (isNew(flags))
			sb.append("new() "); //$NON-NLS-1$
		if (isDelete(flags))
			sb.append("delete() "); //$NON-NLS-1$
		if (isImmutable(flags))
			sb.append("immutable "); //$NON-NLS-1$
		if (isUnitTest(flags))
			sb.append("unittest "); //$NON-NLS-1$
		if (isStaticAssert(flags))
			sb.append("static assert "); //$NON-NLS-1$
		if (isVersionAssignment(flags))
			sb.append("version= "); //$NON-NLS-1$
		if (isDebugAssignment(flags))
			sb.append("debug= "); //$NON-NLS-1$
		if (isAlign(flags))
			sb.append("align "); //$NON-NLS-1$
		if (isExternDeclaration(flags))
			sb.append("extern declaration "); //$NON-NLS-1$
		if (isPragma(flags))
			sb.append("pragma "); //$NON-NLS-1$

		int len = sb.length();
		if (len == 0)
			return ""; //$NON-NLS-1$
		sb.setLength(len - 1);
		return sb.toString();
	}
}
