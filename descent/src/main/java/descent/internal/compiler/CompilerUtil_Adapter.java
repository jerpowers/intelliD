package descent.internal.compiler;

/*
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import descent.core.DescentCompilerPlugin;
*/
import descent.core.JavaModelException__Common;

public class CompilerUtil_Adapter {

	public static void log(Throwable e) {
		log(e, e.getMessage() == null ? e.getClass().getName() : e.getClass().getName() + ": " + e.getMessage());
	}

	/*
	 * Add a log entry
	 */
	public static void log(Throwable e, String message) {
        // TODO: totally expunge all this OSGI/Eclipse stuff, use pluggable logging etc.
/*
		Throwable nestedException;
		if (e instanceof JavaModelException__Common
				&& (nestedException = ((JavaModelException__Common)e).getException()) != null) {
			e = nestedException;
		}
		IStatus status= new Status(
			IStatus.ERROR,
			DescentCompilerPlugin.PLUGIN_ID,
			IStatus.ERROR,
			message,
			e);
		DescentCompilerPlugin.getInstance().getLog().log(status);
*/
	}
}
