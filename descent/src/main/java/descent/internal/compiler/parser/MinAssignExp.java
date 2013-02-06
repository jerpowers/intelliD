package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Min;
import static descent.internal.compiler.parser.TOK.TOKslice;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class MinAssignExp extends BinExp {
	
	public boolean isPreDecrement;

	public MinAssignExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKminass, e1, e2);
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
		return MIN_ASSIGN_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretAssignCommon(istate, Min, context);
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.subass;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression e;

		if (null != type) {
			return this;
		}

		super.semantic(sc, context);
		e2 = resolveProperties(sc, e2, context);

		e = op_overload(sc, context);
		if (null != e) {
			return e;
		}
		
	    if (e1.op == TOKslice)
	    {	// T[] -= ...
			typeCombine(sc, context);
			type = e1.type;
			return arrayOp(sc, context);
	    }

		e1 = e1.modifiableLvalue(sc, e1, context);
		e1.checkScalar(context);
		e1.checkNoBool(context);
		if (e1.type.ty == TY.Tpointer && e2.type.isintegral()) {
			e = scaleFactor(sc, context);
		} else {
			e1.checkArithmetic(context);
			e2.checkArithmetic(context);
			type = e1.type;
			typeCombine(sc, context);
			if (type.isreal() || type.isimaginary()) {
				assert (e2.type.isfloating());
				e2 = e2.castTo(sc, e1.type, context);
			}
			e = this;
		}
		
		return e;
	}
	
	@Override
	public void buildArrayIdent(OutBuffer buf, Expressions arguments) {
		/* Evaluate assign expressions right to left
	     */
	    e2.buildArrayIdent(buf, arguments);
	    e1.buildArrayIdent(buf, arguments);
	    buf.writestring("Min");
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
	    Expression e = new MinAssignExp(null, 0, ex1, ex2);
	    return e;	
	}

}
