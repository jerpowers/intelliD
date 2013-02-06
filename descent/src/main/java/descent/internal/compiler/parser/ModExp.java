package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Mod;
import static descent.internal.compiler.parser.TOK.TOKslice;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class ModExp extends BinExp {

	public ModExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKmod, e1, e2);
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
		return MOD_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretCommon(istate, Mod, context);
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.mod;
	}

	@Override
	public char[] opId_r() {
		return Id.mod_r;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		e1 = e1.optimize(result, context);
		e2 = e2.optimize(result, context);
		if (e1.isConst() && e2.isConst()) {
			e = Mod.call(type, e1, e2, context);
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
		
	    if (e1.op != TOKslice && e2.op != TOKslice) {
			e1.checkArithmetic(context);
			e2.checkArithmetic(context);
	    }
		if (type.isfloating()) {
			type = e1.type;
			if (e2.type.iscomplex()) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.CannotPerformModuloComplexArithmetic, this));
				}
				if (context.isD1())
					return new IntegerExp(null, 0, 0);
				else
					return new ErrorExp();
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
	    buf.writestring("Mod");
	}
	
	@Override
	public Expression buildArrayLoop(Arguments fparams, SemanticContext context) {
		/* Evaluate assign expressions left to right
	     */
	    Expression ex1 = e1.buildArrayLoop(fparams, context);
	    Expression ex2 = e2.buildArrayLoop(fparams, context);
	    Expression e = new ModExp(null, 0, ex1, ex2);
	    return e;
	}

}
