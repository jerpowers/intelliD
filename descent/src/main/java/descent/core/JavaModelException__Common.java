package descent.core;


public abstract class JavaModelException__Common extends Exception {
    public JavaModelException__Common(String msg) {
        super(msg);
    }
    public JavaModelException__Common(String msg, Throwable cause) {
        super(msg, cause);
    }
}

/*
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

*/
/**
 * This is a little helper for removing compile time dependencies from the compiler
 * CONTRACT: The only class that is allowed to directly extend this one is JavaModelException.
 *//*

@SuppressWarnings("serial")
public abstract class JavaModelException__Common extends CoreException {

	protected CoreException nestedCoreException;

	public JavaModelException__Common(IStatus status) {
		super(status);
	}


*/
/**
 * Returns the underlying <code>Throwable</code> that caused the failure.
 *
 * @return the wrappered <code>Throwable</code>, or <code>null</code> if the
 *   direct case of the failure was at the Java model layer
 *//*

public Throwable getException() {
	if (this.nestedCoreException == null) {
		return getStatus().getException();
	} else {
		return this.nestedCoreException;
	}
}
}
*/
