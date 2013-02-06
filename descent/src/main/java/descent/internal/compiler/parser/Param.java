package descent.internal.compiler.parser;

import java.util.List;


public class Param {

	public char obj;		// write object file
	public char link;		// perform link
	public char trace;		// insert profiling hooks
	public boolean quiet;		// suppress non-error messages
	public boolean verbose;	// verbose compile
	public char symdebug;	// insert debug symbolic information
	public char optimize;	// run optimizer
	public char cpu;		// target CPU
	public boolean isX86_64;	// generate X86_64 bit code
	public char isLinux;	// generate code for linux
	public char isWindows;	// generate code for Windows
	public char scheduler;	// which scheduler to use
	public boolean useDeprecated = false;	// allow use of deprecated features
	public boolean useAssert = true;	// generate runtime code for assert()'s
	public boolean useInvariants = true;	// generate class invariant checks
	public boolean useIn = true;		// generate precondition checks
	public boolean useOut = true;	// generate postcondition checks
	public boolean useArrayBounds = true; // generate array bounds checks
	public boolean useSwitchError = true; // check for switches without a default
	public boolean useUnitTests = true;	// generate unittest code
	public boolean useInline;	// inline expand functions
	public boolean release;	// build release version
	public boolean preservePaths;	// !=0 means don't strip path from source file
	public boolean warnings = true;	// enable warnings
	public boolean analyzeTemplates = true;	// analyze templates (Descent)
	public char pic;		// generate position-independent-code for shared libs
	public char cov;		// generate code coverage data
	public char nofloat;	// code should not pull in floating point support
	public int Dversion;	// D version number
	public boolean safe;

	public String argv0;	// program name
	public List<String> imppath; // array of char*'s of where to look for import modules
	public List<String> fileImppath;	// array of char*'s of where to look for file import modules

	public long debuglevel;	// debug level
	public HashtableOfCharArrayAndObject debugids;		// debug identifiers

	public long versionlevel;	// version level
	public HashtableOfCharArrayAndObject versionids;		// version identifiers

	public char run;		// run resulting executable
	public int runargs_length;
	public  String[] runargs;	// arguments for executable
	
	public Param() {
		versionids = new HashtableOfCharArrayAndObject();
		debugids = new HashtableOfCharArrayAndObject();
	}
	
}
