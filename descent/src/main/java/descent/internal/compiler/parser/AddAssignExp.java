package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Add;
import static descent.internal.compiler.parser.TOK.TOKslice;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class AddAssignExp extends BinExp {
	
	public boolean isPreIncrement;
	
	public AddAssignExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKaddass, e1, e2);
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
		return ADD_ASSIGN_EXP;
	}
	
	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretAssignCommon(istate, Add, context);
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.addass;
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

	    Type tb1 = e1.type.toBasetype(context);
	    Type tb2 = e2.type.toBasetype(context);

	    if (e1.op == TOKslice) {
			typeCombine(sc, context);
			type = e1.type;
			return arrayOp(sc, context);
		} else {
			e1 = e1.modifiableLvalue(sc, e1, context);
		}

		if ((tb1.ty == TY.Tarray || tb1.ty == TY.Tsarray)
				&& (tb2.ty == TY.Tarray || tb2.ty == TY.Tsarray)
				&& (tb1.nextOf().equals(tb2.nextOf()))) {
			type = e1.type;
			if (!context.isD1()) {
				typeCombine(sc, context);
			}
			e = this;
		} else {
			e1.checkScalar(context);
			e1.checkNoBool(context);
			if (tb1.ty == TY.Tpointer && tb2.isintegral()) {
				e = scaleFactor(sc, context);
			} else if (tb1.ty == TY.Tbit || tb1.ty == TY.Tbool) {
				// Rewrite e1+=e2 to e1=e1+e2
				e = new AddExp(filename, lineNumber, e1, e2);
				e = new CastExp(filename, lineNumber, e, e1.type);
				e = new AssignExp(filename, lineNumber, e1.syntaxCopy(context), e);
				e = e.semantic(sc, context);
			} else {
				type = e1.type;
				typeCombine(sc, context);
				e1.checkArithmetic(context);
				e2.checkArithmetic(context);
				if (type.isreal() || type.isimaginary()) {
					// assert(global.errors || e2.type.isfloating());
					e2 = e2.castTo(sc, e1.type, context);
				}
				e = this;
			}
		}

		return e;
	}
	
	@Override
	public void buildArrayIdent(OutBuffer buf, Expressions arguments) {
		/* Evaluate assign expressions right to left
	     */
	    e2.buildArrayIdent(buf, arguments);
	    e1.buildArrayIdent(buf, arguments);
	    buf.writestring("Add");
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
	    Expression e = new AddAssignExp(null, 0, ex1, ex2);
	    return e;	
	}

}
