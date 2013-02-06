package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEbreak;
import static descent.internal.compiler.parser.BE.BEcontinue;
import static descent.internal.compiler.parser.BE.BEfallthru;
import static descent.internal.compiler.parser.BE.BEthrow;
import static descent.internal.compiler.parser.Constfold.Add;
import static descent.internal.compiler.parser.Constfold.Cmp;
import static descent.internal.compiler.parser.Constfold.Min;
import static descent.internal.compiler.parser.TOK.TOKforeach;
import static descent.internal.compiler.parser.TOK.TOKgt;
import static descent.internal.compiler.parser.TOK.TOKlt;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

// DMD 2.003
public class ForeachRangeStatement extends Statement {

	public TOK op;
	public Argument arg;
	public Expression lwr;
	public Expression upr;
	public Statement body;
	public VarDeclaration key;

	public ForeachRangeStatement(char[] filename, int lineNumber, TOK op, Argument arg, Expression lwr,
			Expression upr, Statement body) {
		super(filename, lineNumber);

		this.op = op;
		this.arg = arg;
		this.lwr = lwr;
		this.upr = upr;
		this.body = body;
	}

	@Override
	public int getNodeType() {
		return FOREACH_RANGE_STATEMENT;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, arg);
			TreeVisitor.acceptChildren(visitor, lwr);
			TreeVisitor.acceptChildren(visitor, upr);
			TreeVisitor.acceptChildren(visitor, body);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public boolean hasBreak() {
		return true;
	}
	
	@Override
	public boolean hasContinue() {
		return true;
	}
	
	@Override
	public boolean usesEH(SemanticContext context) {
		return body.usesEH(context);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		int result = BEfallthru;

		if (lwr != null && lwr.canThrow(context))
			result |= BEthrow;
		else if (upr != null && upr.canThrow(context))
			result |= BEthrow;

		if (body != null) {
			result |= body.blockExit(context) & ~(BEbreak | BEcontinue);
		}
		return result;
	}
	
	@Override
	public boolean comeFrom() {
		if (body != null)
			return body.comeFrom();
		return false;
	}
	
	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
		buf.writestring(op.charArrayValue);
		buf.writestring(" (");

		if (arg.type != null)
			arg.type.toCBuffer(buf, arg.ident, hgs, context);
		else
			buf.writestring(arg.ident.toChars());

		buf.writestring("; ");
		lwr.toCBuffer(buf, hgs, context);
		buf.writestring(" .. ");
		upr.toCBuffer(buf, hgs, context);
		buf.writebyte(')');
		buf.writenl();
		buf.writebyte('{');
		buf.writenl();
		if (body != null)
			body.toCBuffer(buf, hgs, context);
		buf.writebyte('}');
		buf.writenl();
	}
	
	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		if (istate.start == this)
			istate.start = null;
		if (istate.start != null)
			return null;

		Expression e = null;
		Expression elwr = lwr.interpret(istate, context);
		if (elwr == EXP_CANT_INTERPRET)
			return EXP_CANT_INTERPRET;

		Expression eupr = upr.interpret(istate, context);
		if (eupr == EXP_CANT_INTERPRET)
			return EXP_CANT_INTERPRET;

		Expression keysave = key.value;

		if (op == TOKforeach) {
			key.value = elwr;

			while (true) {
				e = Cmp.call(TOKlt, key.value.type, key.value, upr, context);
				if (e == EXP_CANT_INTERPRET)
					break;
				if (e.isBool(true) == false) {
					e = null;
					break;
				}

				e = body != null ? body.interpret(istate, context) : null;
				if (e == EXP_CANT_INTERPRET)
					break;
				if (e == EXP_BREAK_INTERPRET) {
					e = null;
					break;
				}
				e = Add.call(key.value.type, key.value, new IntegerExp(filename, lineNumber, 1,
						key.value.type), context);
				if (e == EXP_CANT_INTERPRET)
					break;
				key.value = e;
			}
		} else // TOKforeach_reverse
		{
			key.value = eupr;

			while (true) {
				e = Cmp.call(TOKgt, key.value.type, key.value, lwr, context);
				if (e == EXP_CANT_INTERPRET)
					break;
				if (e.isBool(true) == false) {
					e = null;
					break;
				}

				e = Min.call(key.value.type, key.value, new IntegerExp(filename, lineNumber, 1,
						key.value.type), context);
				if (e == EXP_CANT_INTERPRET)
					break;
				key.value = e;

				e = body != null ? body.interpret(istate, context) : null;
				if (e == EXP_CANT_INTERPRET)
					break;
				if (e == EXP_BREAK_INTERPRET) {
					e = null;
					break;
				}
			}
		}
		key.value = keysave;
		return e;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		ScopeDsymbol sym;
		Statement s = this;

		lwr = lwr.semantic(sc, context);
		lwr = resolveProperties(sc, lwr, context);
		if (null == lwr.type) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.InvalidRangeLowerBound, this, new String[] { lwr.toChars(context) }));
			}
			return this;
		}

		upr = upr.semantic(sc, context);
		upr = resolveProperties(sc, upr, context);
		if (null == upr.type) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.InvalidRangeUpperBound, this, new String[] { upr.toChars(context) }));
			}
			return this;
		}

		if (null != arg.type) {
			arg.type = arg.type.semantic(filename, lineNumber, sc, context);
			lwr = lwr.implicitCastTo(sc, arg.type, context);
			upr = upr.implicitCastTo(sc, arg.type, context);
		} else {
			/* Must infer types from lwr and upr
			 */
			AddExp ea = new AddExp(filename, lineNumber, lwr, upr);
			ea.typeCombine(sc, context);
			arg.type = ea.type.mutableOf(context);
			lwr = ea.e1;
			upr = ea.e2;
		}

		if (!arg.type.isscalar(context)) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.SymbolIsNotAnArithmeticType, this, new String[] { arg.type.toChars(context) }));
			}
		}

		sym = new ScopeDsymbol();
		sym.parent = sc.scopesym;
		sc = sc.push(sym);

		sc.noctor++;

		VarDeclaration key = new VarDeclaration(filename, lineNumber, arg.type, arg.ident, null);
		DeclarationExp de = new DeclarationExp(filename, lineNumber, key);
		de.semantic(sc, context);

		if (0 < key.storage_class) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ForeachRangeKeyCannotHaveStorageClass, this));
			}
		}

		sc.sbreak = this;
		sc.scontinue = this;
		body = body.semantic(sc, context);

		sc.noctor--;
		sc.pop();
		return s;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		ForeachRangeStatement s = context.newForeachRangeStatement(filename, lineNumber, op, arg
				.syntaxCopy(context), lwr.syntaxCopy(context), upr.syntaxCopy(context),
				null != body ? body.syntaxCopy(context) : null);
		s.copySourceRange(this);
		return s;
	}

}
