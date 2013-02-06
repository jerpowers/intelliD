package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TY.Tvoid;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class ArrayExp extends UnaExp {

	public Expressions arguments, sourceArguments;

	public ArrayExp(char[] filename, int lineNumber, Expression e, Expressions arguments) {
		super(filename, lineNumber, TOK.TOKarray, e);
		this.arguments = arguments;
		if (arguments != null) {
			this.sourceArguments = new Expressions(arguments);
		}
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceE1);
			TreeVisitor.acceptChildren(visitor, sourceArguments);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return ARRAY_EXP;
	}
	
	@Override
	public boolean isLvalue(SemanticContext context) {
	    if (type != null && type.toBasetype(context).ty == Tvoid)
			return false;
		return true;
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.index;
	}

	@Override
	public void scanForNestedRef(Scope sc, SemanticContext context) {
		e1.scanForNestedRef(sc, context);
		arrayExpressionScanForNestedRef(sc, arguments, context);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression e;
		Type t1;

		super.semantic(sc, context);
		e1 = resolveProperties(sc, e1, context);
		
		// Added for Descent
		if ((e1 == null || e1.type == null) && context.global.errors > 0) {
			return new IntegerExp(0);
		}

		t1 = e1.type.toBasetype(context);
		if (t1.ty != TY.Tclass && t1.ty != TY.Tstruct) {
			// Convert to IndexExp
			if (arguments.size() != 1) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.OnlyOneIndexAllowedToIndex, this, t1.toChars(context)));
				}
			}
			e = new IndexExp(filename, lineNumber, e1, arguments.get(0));
			e.copySourceRange(this);
			return e.semantic(sc, context);
		}

		// Run semantic() on each argument
		for (int i = 0; i < arguments.size(); i++) {
			e = arguments.get(i);
			e = e.semantic(sc, context);
			if (null == e.type) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolHasNoValue, e, e.toChars(context)));
				}
			}
			arguments.set(i, e);
		}

		expandTuples(arguments, context);
		assert (arguments != null && arguments.size() > 0);

		e = op_overload(sc, context);
		if (null == e) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.NoOpIndexOperatorOverloadForType, this, e1.type.toChars(context)));
			}
			e = e1;
		}

		return e;
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		return new ArrayExp(filename, lineNumber, e1.syntaxCopy(context), arraySyntaxCopy(arguments, context));
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		expToCBuffer(buf, hgs, e1, PREC.PREC_primary, context);
		buf.writeByte('[');
		argsToCBuffer(buf, arguments, hgs, context);
		buf.writeByte(']');
	}

	@Override
	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
		if ((type != null) && (type.toBasetype(context).ty == TY.Tvoid)) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.VoidsHaveNoValue, this));
			}
		}
		return this;
	}

}
