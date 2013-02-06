package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.Cat;
import static descent.internal.compiler.parser.TOK.TOKcatass;
import static descent.internal.compiler.parser.TOK.TOKslice;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tsarray;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class CatAssignExp extends BinExp {

	public CatAssignExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOKcatass, e1, e2);
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
		return CAT_ASSIGN_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretAssignCommon(istate, Cat, context);
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.catass;
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
			SliceExp se = (SliceExp) e1;

			if (se.e1.type.toBasetype(context).ty == Tsarray) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotAppendToStaticArray, this, se.e1.type.toChars(context)));
				}
			}
		}

		e1 = e1.modifiableLvalue(sc, e1, context);

		Type tb1 = e1.type.toBasetype(context);
		Type tb2 = e2.type.toBasetype(context);
		
		e2.rvalue(context);
		
		boolean condition;
		if (context.isD1()) {
			condition = (tb1.ty == Tarray)
				&& (tb2.ty == Tarray || tb2.ty == Tsarray)
				&& MATCH.MATCHnomatch != (e2.implicitConvTo(e1.type, context));
		} else {
		    condition = (tb1.ty == Tarray) &&
		    		(tb2.ty == Tarray || tb2.ty == Tsarray) &&
		    		(MATCH.MATCHnomatch != e2.implicitConvTo(e1.type, context) ||
		    		MATCH.MATCHnomatch != tb2.nextOf().implicitConvTo(tb1.nextOf(), context));
		}

		if (condition) {
			// Append array
			e2 = e2.castTo(sc, e1.type, context);
			type = e1.type;
			e = this;
		}

		else if ((tb1.ty == Tarray)
				&& null != e2.implicitConvTo(tb1.nextOf(), context)) {
			// Append element
			e2 = e2.castTo(sc, tb1.nextOf(), context);
			type = e1.type;
			e = this;
		}

		else {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotAppendTypeToType, this, tb2.toChars(context), tb1
						.toChars(context)));
			}
			type = Type.tint32;
			e = this;
		}

		return e;
	}

}
