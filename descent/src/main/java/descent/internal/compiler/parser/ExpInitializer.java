package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class ExpInitializer extends Initializer {

	public Expression exp;
	public Expression sourceExp;

	public ExpInitializer(char[] filename, int lineNumber, Expression exp) {
		super(filename, lineNumber);
		this.exp = exp;
		this.sourceExp = exp;
		this.start = exp.start;
		this.length = exp.length;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceExp);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return EXP_INITIALIZER;
	}

	@Override
	public Type inferType(Scope sc, SemanticContext context) {
		exp = exp.semantic(sc, context);
		exp = ASTDmdNode.resolveProperties(sc, exp, context);
		
		if (context.isD2()) {
			// Give error for overloaded function addresses
		    if (exp.op == TOK.TOKsymoff) {
				SymOffExp se = (SymOffExp) exp;
				if (se.hasOverloads
						&& null == se.var.isFuncDeclaration().isUnique(context)) {
					if (context.acceptsErrors()) {
						context
								.acceptProblem(Problem
										.newSemanticTypeError(
												IProblem.CannotInferTypeFromOverloadedFunctionSymbol,
												this, exp.toChars(context)));
					}
				}
			}
		}
		
	    Type t = exp.type;
	    if (null == t) {
	    	t = super.inferType(sc, context);
	    }
	    return t;

	}

	@Override
	public ExpInitializer isExpInitializer() {
		return this;
	}

	@Override
	public Initializer semantic(Scope sc, Type t, SemanticContext context) {
		exp = exp.semantic(sc, context);
		
		if (context.isD2()) {
		    exp = resolveProperties(sc, exp, context);
			exp = exp.optimize(WANTvalue | WANTinterpret, context);
		}
		
		Type tb = t.toBasetype(context);

		/*
		 * Look for case of initializing a static array with a too-short string
		 * literal, such as: char[5] foo = "abc"; Allow this by doing an
		 * explicit cast, which will lengthen the string literal.
		 */
		if (exp.op == TOK.TOKstring && tb.ty == TY.Tsarray
				&& exp.type.ty == TY.Tsarray) {
			StringExp se = (StringExp) exp;

			if (!se.committed
					&& se.type.ty == TY.Tsarray
					&& ((TypeSArray) se.type).dim.toInteger(context).compareTo(
							((TypeSArray) t).dim.toInteger(context)) < 0) {
				exp = se.castTo(sc, t, context);
				// goto L1;
				exp = exp.optimize(ASTDmdNode.WANTvalue
						| ASTDmdNode.WANTinterpret, context);
				return this;
			}
		}

		// Look for the case of statically initializing an array
		// with a single member.
		if (tb.ty == TY.Tsarray
				&& !tb.nextOf().equals(exp.type.toBasetype(context).nextOf())
				&& exp.implicitConvTo(tb.nextOf(), context) != MATCH.MATCHnomatch) {
			t = tb.nextOf();
		}

		exp = exp.implicitCastTo(sc, t, context);
		// L1:
		exp = exp.optimize(ASTDmdNode.WANTvalue | ASTDmdNode.WANTinterpret,
				context);
		return this;
	}

	@Override
	public Initializer syntaxCopy(SemanticContext context) {
		return new ExpInitializer(filename, lineNumber, exp.syntaxCopy(context));
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		exp.toCBuffer(buf, hgs, context);
	}

	@Override
	public Expression toExpression(SemanticContext context) {
		return exp;
	}

}
