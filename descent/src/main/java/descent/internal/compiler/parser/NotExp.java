package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Not;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class NotExp extends UnaExp {

	public NotExp(char[] filename, int lineNumber, Expression e1) {
		super(filename, lineNumber, TOK.TOKnot, e1);
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
	public int getNodeType() {
		return NOT_EXP;
	}
	
	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretCommon(istate, Not, context);
	}
	
	@Override
	public boolean isBit() {
		return true;
	}
	
	@Override
	public Expression optimize(int result, SemanticContext context)
	{
		Expression e;
		
		e1 = e1.optimize(result, context);
		if(e1.isConst())
		{
			e = Not.call(type, e1, context);
		}
		else
		{
			e = this;
		}
		return e;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		super.semantic(sc, context);
	    e1 = resolveProperties(sc, e1, context);
	    e1 = e1.checkToBoolean(context);
	    type = Type.tboolean;
	    return this;
	}

}
