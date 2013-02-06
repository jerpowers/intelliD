package descent.internal.compiler.parser;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TemplateExp extends Expression {
	
	public TemplateDeclaration td;

    public TemplateExp(char[] filename, int lineNumber, TemplateDeclaration td) {
    	super(filename, lineNumber, TOK.TOKtemplate);
		this.td = td;    	
    }
    
    @Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
    
	@Override
	public int getNodeType() {
		return TEMPLATE_EXP;
	}
    
    @Override
    public void rvalue(SemanticContext context) {
    	if (context.acceptsErrors()) {
	    	context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.TemplateHasNoValue, this, new String[] { toChars(context) }));
    	}
    }

	@Override
    public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
    	 buf.writestring(td.toChars(context));
    }

}
