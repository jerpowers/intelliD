package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.And;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class AndAssignExp extends BinExp {

	public AndAssignExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKandass, e1, e2);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceE1);
			TreeVisitor.acceptChildren(visitor, sourceE2);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return AND_ASSIGN_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretAssignCommon(istate, And, context);
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.andass;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		return commonSemanticAssignIntegral(sc, context);
	}
	
	@Override
	public void buildArrayIdent(OutBuffer buf, Expressions arguments) {
		/* Evaluate assign expressions right to left
	     */
	    e2.buildArrayIdent(buf, arguments);
	    e1.buildArrayIdent(buf, arguments);
	    buf.writestring("And");
	    buf.writestring("ass");
	}
	
	@Override
	public Expression buildArrayLoop(Arguments fparams, SemanticContext context) {
		/* Evaluate assign expressions right to left
	     */
	    Expression ex2 = e2.buildArrayLoop(fparams, context);
	    Expression ex1 = e1.buildArrayLoop(fparams, context);
	    Argument param = (Argument) fparams.get(0);
	    param.storageClass = 0;
	    Expression e = new AndAssignExp(null, 0, ex1, ex2);
	    return e;	
	}

}
