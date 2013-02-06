package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Mul;
import static descent.internal.compiler.parser.TOK.TOKslice;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class MulExp extends BinExp {

	public MulExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKmul, e1, e2);
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
		return MUL_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretCommon(istate, Mul, context);
	}

	@Override
	public boolean isCommutative() {
		return true;
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.mul;
	}

	@Override
	public char[] opId_r() {
		return Id.mul_r;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		e1 = e1.optimize(result, context);
		e2 = e2.optimize(result, context);
		if (e1.isConst() && e2.isConst()) {
			e = Mul.call(type, e1, e2, context);
		} else {
			e = this;
		}
		return e;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression e;

		if (null != type) {
			return this;
		}

		super.semanticp(sc, context);
		e = op_overload(sc, context);
		if (null != e) {
			return e;
		}

		typeCombine(sc, context);
	    if (e1.op != TOKslice && e2.op != TOKslice)
	    {
			e1.checkArithmetic(context);
			e2.checkArithmetic(context);
		}
		if (type.isfloating()) {
			Type t1 = e1.type;
			Type t2 = e2.type;

			if (t1.isreal()) {
				type = t2;
			} else if (t2.isreal()) {
				type = t1;
			} else if (t1.isimaginary()) {
				if (t2.isimaginary()) {

					switch (t1.ty) {
					case Timaginary32:
						type = Type.tfloat32;
						break;
					case Timaginary64:
						type = Type.tfloat64;
						break;
					case Timaginary80:
						type = Type.tfloat80;
						break;
					default:
						assert (false);
					}

					// iy * iv = -yv
					e1.type = type;
					e2.type = type;
					e = new NegExp(filename, lineNumber, this);
					e = e.semantic(sc, context);
					return e;
				} else {
					type = t2; // t2 is complex
				}
			} else if (t2.isimaginary()) {
				type = t1; // t1 is complex
			}
		}
		
		return this;
	}
	
	@Override
	public void buildArrayIdent(OutBuffer buf, Expressions arguments) {
		/* Evaluate assign expressions left to right
	     */
	    e1.buildArrayIdent(buf, arguments);
	    e2.buildArrayIdent(buf, arguments);
	    buf.writestring("Mul");
	}
	
	@Override
	public Expression buildArrayLoop(Arguments fparams, SemanticContext context) {
		/* Evaluate assign expressions left to right
	     */
	    Expression ex1 = e1.buildArrayLoop(fparams, context);
	    Expression ex2 = e2.buildArrayLoop(fparams, context);
	    Expression e = new MulExp(null, 0, ex1, ex2);
	    return e;
	}

}
