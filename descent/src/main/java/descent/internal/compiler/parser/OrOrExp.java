package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TY.Tvoid;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class OrOrExp extends BinExp {

	public OrOrExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKoror, e1, e2);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, e1);
			TreeVisitor.acceptChildren(visitor, e2);
		}
		visitor.endVisit(this);
	}

	@Override
	public int checkSideEffect(int flag, SemanticContext context) {
		if (flag == 2) {
			return (e1.checkSideEffect(2, context) != 0)
					|| (e2.checkSideEffect(2, context) != 0) ? 1 : 0;
		} else {
			e1.checkSideEffect(1, context);
			return e2.checkSideEffect(flag, context);
		}
	}

	@Override
	public Expression checkToBoolean(SemanticContext context) {
		e2 = e2.checkToBoolean(context);
		return this;
	}

	@Override
	public int getNodeType() {
		return OR_OR_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		Expression e = e1.interpret(istate, context);
		if (e != EXP_CANT_INTERPRET) {
			if (e.isBool(true)) {
				e = new IntegerExp(e1.filename, e1.lineNumber, 1, type);
			} else if (e.isBool(false)) {
				e = e2.interpret(istate, context);
				if (e != EXP_CANT_INTERPRET) {
					if (e.isBool(false)) {
						e = new IntegerExp(e1.filename, e1.lineNumber, 0, type);
					} else if (e.isBool(true)) {
						e = new IntegerExp(e1.filename, e1.lineNumber, 1, type);
					} else {
						e = EXP_CANT_INTERPRET;
					}
				}
			} else {
				e = EXP_CANT_INTERPRET;
			}
		}
		return e;
	}

	@Override
	public boolean isBit() {
		return true;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		e1 = e1.optimize(WANTflags | (result & WANTinterpret), context);
		e = this;
		if (e1.isBool(true)) { // Replace with (e1, 1)
			e = new CommaExp(filename, lineNumber, e1, new IntegerExp(filename, lineNumber, 1, type));
			e.type = type;
			e = e.optimize(result, context);
		} else {
			e2 = e2.optimize(WANTflags | (result & WANTinterpret), context);
			if (result != 0 && e2.type.toBasetype(context).ty == Tvoid
					&& 0 == context.global.errors) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolHasNoValue, this, new String[] { "void" }));
				}
			}
			if (e1.isConst()) {
				if (e2.isConst()) {
					boolean n1 = e1.isBool(true);
					boolean n2 = e2.isBool(true);

					e = new IntegerExp(filename, lineNumber, (n1 || n2) ? 1 : 0, type);
				} else if (e1.isBool(false)) {
					e = new BoolExp(filename, lineNumber, e2, type);
				}
			}
		}
		return e;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		e1 = e1.semantic(sc, context);
		e1 = resolveProperties(sc, e1, context);
		e1 = e1.checkToPointer(context);
		e1 = e1.checkToBoolean(context);
		int cs1 = sc.callSuper;

		if ((sc.flags & Scope.SCOPEstaticif) > 0) {
			//If in static if, don't evaluate e2 if we don't have to.
			e1 = e1.optimize(WANTflags, context);
			if (e1.isBool(true)) {
				return new IntegerExp(filename, lineNumber, 1, Type.tboolean);
			}
		}

		e2 = e2.semantic(sc, context);
		sc.mergeCallSuper(filename, lineNumber, cs1, this, context);
		e2 = resolveProperties(sc, e2, context);
		e2 = e2.checkToPointer(context);

		type = Type.tboolean;
		if (e2.type.ty == TY.Tvoid) {
			type = Type.tvoid;
		}
		if (e2.op == TOK.TOKtype || e2.op == TOK.TOKimport) {
			if (context.acceptsWarnings()) {
				context.acceptProblem(Problem.newSemanticTypeWarning(IProblem.SymbolNotAnExpression, 0, e2.start, e2.length, new String[] { e2.toChars(context) }));
			}
		}

		return this;
	}

}
