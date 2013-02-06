package descent.internal.compiler.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

/*
 * PERHAPS this is a prime candidate for a weighty operation (disk I/O) that 
 *      could be done only if needed by the IDE. For example, if it's reading
 *      in an image file that's shown at runtime, there's no reason to do this
 *      operation. If, on the other hand, the contents of the file are being
 *      used in a string mixin, we'd have to run this. Maybe it could be moved
 *      into optimize(WANTvalue) or otherwise marked as necessary/unnecessary?
 */

public class FileExp extends UnaExp {

	public FileExp(char[] filename, int lineNumber, Expression e) {
		super(filename, lineNumber, TOK.TOKmixin, e);
	}
	
	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
		buf.writestring("import(");
	    expToCBuffer(buf, hgs, e1, PREC.PREC_assign, context);
	    buf.writeByte(')');
	}
	
	@Override
	public int getNodeType() {
		return FILE_EXP;
	}
	
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, e1);
		}
		visitor.endVisit(this);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context)
	{   
	    super.semantic(sc, context);
	    e1 = resolveProperties(sc, e1, context);
	    e1 = e1.optimize(WANTvalue, context);
	    if (e1.op != TOK.TOKstring)
	    {
	    	if (context.acceptsErrors()) {
		    	context.acceptProblem(Problem.newSemanticTypeError(
		    			IProblem.FileNameMustBeString,
		    			e1,
		    			new String[] { e1.toChars(context) }));
	    	}
			return (new StringExp(filename, lineNumber, Id.empty, 0)).semantic(sc, context);
	    }
	    
	    String filename = new String(((StringExp) e1).string);
	    
	    File file = null;
	    for(String filePath : context.global.filePath) {
	    	File aFile = new File(filePath, filename);
	    	if (aFile.exists()) {
	    		file = aFile;
	    		break;
	    	}
	    }
	    
	    // TODO remove this error?
//	    if(null == file)
//	    {
//	    	context.acceptProblem(Problem.newSemanticTypeError(
//	    			IProblem.FileImportsMustBeSpecified,
//	    			e1,
//	    			new String[] { filename }));
//			return (new StringExp(filename, lineNumber, Id.empty, 0)).semantic(sc, context);
//	    }
	    
	    if(file == null)
	    {
	    	if (context.acceptsErrors()) {
		    	context.acceptProblem(Problem.newSemanticTypeError(
		    			IProblem.FileNotFound,
		    			e1,
		    			new String[] { filename }));
	    	}
			return (new StringExp(this.filename, lineNumber, Id.empty, 0)).semantic(sc, context);
	    }
	    
	    try
	    {
	    	char[] data = getFile(file);
	    	return (new StringExp(this.filename, lineNumber, data, data.length)).semantic(sc, context);
	    }
	    catch(IOException e)
	    {
	    	if (context.acceptsErrors()) {
		    	context.acceptProblem(Problem.newSemanticTypeError(
		    			IProblem.ErrorReadingFile,
		    			e1,
		    			new String[] { file.getAbsolutePath() }));
	    	}
	    	return (new StringExp(this.filename, lineNumber, Id.empty, 0)).semantic(sc, context);
	    }
	}
	
	private static char[] getFile(File file) throws IOException
	{
		CachedFile cachedFile = cache.get(file);
		if(null != cachedFile)
		{
			return cachedFile.getData();
		}
		else
		{
			cachedFile = new CachedFile(file);
			cache.put(file, cachedFile);
			return cachedFile.getData();
		}
	}
	
	private static final Map<File, CachedFile> cache = 
		new HashMap<File, CachedFile>();
	
	private static final class CachedFile
	{
		final File file;
		long lastModified;
		WeakReference<char[]> data;
		
		CachedFile(File $file)
		{
			assert(null != $file);
			file = $file;
		}
		
		char[] getData() throws IOException
		{
			if(null == data)
				return readData();
			if(file.lastModified() > lastModified)
				return readData();
			
			char[] cachedData = data.get();
			if(cachedData == null)
				return readData();
			return cachedData;
		}
		
		private char[] readData() throws IOException
		{
			InputStream in = new BufferedInputStream(new FileInputStream(file));
	        long size = file.length();
	        assert(size <= Integer.MAX_VALUE);
	        char[] buf = new char[(int) size];
	        int next, i;
	        // TODO make a dialog box suggesting our users go get a cup of
        	// tea every time they save... or just speed this up
	        for(i = 0; (next = in.read()) >= 0; i++)
	        {
	        	buf[i] = (char) next;
	        }
	        in.close();
	        
	        lastModified = file.lastModified();
	        data = new WeakReference<char[]>(buf);
	        
	        return buf;
			
		}
	}
}
