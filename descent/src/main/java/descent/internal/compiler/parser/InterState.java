package descent.internal.compiler.parser;


public class InterState
{
	public InterState caller;		// calling function's InterState
	public FuncDeclaration fd;	// function being interpreted
	public Dsymbols vars;		// variables used in this function
	public Statement start;		// if !=null, start execution at this statement
	public Statement gotoTarget;	// target of EXP_GOTO_INTERPRET result
    
	public boolean stackOverflow; // for Descent: tells if the evaluation resulted in a stack overflow
    
}
