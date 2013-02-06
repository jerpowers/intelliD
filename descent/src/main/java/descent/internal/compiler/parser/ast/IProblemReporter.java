package descent.internal.compiler.parser.ast;


import descent.core.compiler.IProblem;

// XXX: DLTK interface adapter
public interface IProblemReporter {
	void reportProblem(IProblem problem) /*throws CoreException */;
}