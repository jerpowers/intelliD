package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Div;
import static descent.internal.compiler.parser.TOK.TOKslice;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class DivAssignExp extends BinExp {

	public DivAssignExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKdivass, e1, e2);
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
		return DIV_ASSIGN_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretAssignCommon(istate, Div, context);
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.divass;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression e;

		super.semantic(sc, context);
		e2 = resolveProperties(sc, e2, context);

		e = op_overload(sc, context);
		if (null != e) {
			return e;
		}
		
	    if (e1.op == TOKslice) {
	    	// T[] -= ...
			typeCombine(sc, context);
			type = e1.type;
			return arrayOp(sc, context);
		}

		e1 = e1.modifiableLvalue(sc, e1, context);
		e1.checkScalar(context);
		e1.checkNoBool(context);
		type = e1.type;
		typeCombine(sc, context);
		e1.checkArithmetic(context);
		e2.checkArithmetic(context);
		if (e2.type.isimaginary()) {
			Type t1 = null;
			Type t2 = null;

			t1 = e1.type;
			if (t1.isreal()) {
				// x/iv = i(-x/v)
				// Therefore, the result is 0
				e2 = new CommaExp(filename, lineNumber, e2, new RealExp(filename, lineNumber, new char[0],
						real_t.ZERO, t1));
				e2.type = t1;
				e = new AssignExp(filename, lineNumber, e1, e2);
				e.type = t1;
				return e;
			} else if (t1.isimaginary()) {
				switch (t1.ty) {
				case Timaginary32:
					t2 = Type.tfloat32;
					break;
				case Timaginary64:
					t2 = Type.tfloat64;
					break;
				case Timaginary80:
					t2 = Type.tfloat80;
					break;
				default:
					assert (false);
				}
				e2 = e2.castTo(sc, t2, context);
				e = new AssignExp(filename, lineNumber, e1, e2);
				e.type = t1;
				return e;
			}
		}
		
		return this;
	}
	
	@Override
	public void buildArrayIdent(OutBuffer buf, Expressions arguments) {
		/* Evaluate assign expressions right to left
	     */
	    e2.buildArrayIdent(buf, arguments);
	    e1.buildArrayIdent(buf, arguments);
	    buf.writestring("Div");
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
	    Expression e = new DivAssignExp(null, 0, ex1, ex2);
	    return e;	
	}

}
