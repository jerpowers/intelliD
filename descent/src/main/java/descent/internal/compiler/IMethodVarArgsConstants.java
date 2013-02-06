package descent.internal.compiler;

public interface IMethodVarArgsConstants {
	/**
	 * Constant indicating the method has no varargs.
	 */
	int VARARGS_NO = 0;

	/**
	 * Constant indicating the method has varargs of undefined types. For example
	 * like in a method <code>void foo(...)</code> or <code>void foo(int x, ...)<code>.
	 */
	int VARARGS_UNDEFINED_TYPES = 1;

	/**
	 * Constant indicating the method has varargs of the same type:
	 * the one of the last argument of this method. For example
	 * like in a method <code>void foo(int ...)</code> or <code>void foo(int x, char[] ...)<code>.
	 */
	int VARARGS_SAME_TYPES = 2;

}
