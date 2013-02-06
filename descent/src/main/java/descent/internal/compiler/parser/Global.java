package descent.internal.compiler.parser;


public class Global {
	
	public int structalign = 8;
	public String version;
	public int gag;
	public int errors;
	public Param params = new Param();
	public long debugLevel;
	public Array<String> path = new Array<String>();
	public Array<String> filePath = new Array<String>();
	public boolean ignoreUnsupportedPragmas;
	
	public Global() {
		path.add("C:\\ary\\programacion\\d\\1.020\\dmd\\src\\phobos");
	}

}
