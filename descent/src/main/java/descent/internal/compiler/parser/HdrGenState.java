package descent.internal.compiler.parser;


public class HdrGenState {
	
	public static class FLinitObject {
		public int init;
		public int decl;
	}

	public boolean hdrgen;
	public boolean tpltMember;
	public int console;
	public int ddoc;
	
	public FLinitObject FLinit = new FLinitObject();

}
