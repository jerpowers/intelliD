package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Neg;
import static descent.internal.compiler.parser.TOK.TOKslice;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class NegExp extends UnaExp {

	public NegExp(char[] filename, int lineNumber, Expression e1) {
		super(filename, lineNumber, TOK.TOKneg, e1);
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
		return NEG_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretCommon(istate, Neg, context);
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.neg;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		e1 = e1.optimize(result, context);
		if (e1.isConst()) {
			e = Neg.call(type, e1, context);
		} else {
			e = this;
		}
		e.copySourceRange(this);
		return e;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression e;

		if (type == null) {
			super.semantic(sc, context);
			e1 = resolveProperties(sc, e1, context);
			e = op_overload(sc, context);
			if (e != null) {
				return e;
			}

			e1.checkNoBool(context);
			if (e1.op != TOKslice) {
				e1.checkArithmetic(context);
			}
			type = e1.type;
		}
		return this;
	}
	
	@Override
	public void buildArrayIdent(OutBuffer buf, Expressions arguments) {
		e1.buildArrayIdent(buf, arguments);
	    buf.writestring("Neg");
	}
	
	@Override
	public Expression buildArrayLoop(Arguments fparams, SemanticContext context) {
		Expression ex1 = e1.buildArrayLoop(fparams, context);
	    Expression e = new NegExp(null, 0, ex1);
	    return e;
	}

}
