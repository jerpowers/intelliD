package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEfallthru;
import static descent.internal.compiler.parser.BE.BEnone;
import static descent.internal.compiler.parser.BE.BEthrow;
import static descent.internal.compiler.parser.TOK.TOKimport;
import static descent.internal.compiler.parser.TOK.TOKtype;
import static descent.internal.compiler.parser.TY.Tstruct;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class WithStatement extends Statement {

	public Expression exp;
	public Expression sourceExp;
	public Statement body;
	public Statement sourceBody;
	
	public VarDeclaration wthis;

	public WithStatement(char[] filename, int lineNumber, Expression exp, Statement body) {
		super(filename, lineNumber);
		this.exp = exp;
		this.sourceExp = exp;
		this.body = body;
		this.sourceBody = body;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, exp);
			TreeVisitor.acceptChildren(visitor, body);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		int result = BEnone;
		if (exp.canThrow(context)) {
			result = BEthrow;
		}
		if (body != null) {
			result |= body.blockExit(context);
		} else {
			result |= BEfallthru;
		}
		return result;
	}

	@Override
	public int getNodeType() {
		return WITH_STATEMENT;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		ScopeDsymbol sym;
		Initializer init;

		exp = exp.semantic(sc, context);
		exp = resolveProperties(sc, exp, context);
		if (exp.op == TOKimport) {
			ScopeExp es = (ScopeExp) exp;

			sym = (ScopeDsymbol) es.sds; // SEMANTIC
		} else if (exp.op == TOKtype) {
			TypeExp es = (TypeExp) exp;

			sym = es.type.toDsymbol(sc, context).isScopeDsymbol();
			if (sym == null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.SymbolHasNoMembers, this, new String[] { es.toChars(context) }));
				}
				body = body.semantic(sc, context);
				return this;
			}
		} else {
			Type t = exp.type;

			if (t == null) {
				throw new IllegalStateException("assert(t);");
			}
			t = t.toBasetype(context);
			if (t.isClassHandle() != null) {
				init = new ExpInitializer(filename, lineNumber, exp);
				wthis = new VarDeclaration(filename, lineNumber, exp.type, Id.withSym, init);
				wthis.semantic(sc, context);

				sym = new WithScopeSymbol(this);
				sym.parent = sc.scopesym;
			} else if (t.ty == Tstruct) {
				Expression e = exp.addressOf(sc, context);
				init = new ExpInitializer(filename, lineNumber, e);
				wthis = new VarDeclaration(filename, lineNumber, e.type, Id.withSym, init);
				wthis.semantic(sc, context);
				sym = new WithScopeSymbol(this);
				sym.parent = sc.scopesym;
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.WithExpressionsMustBeClassObject, sourceExp, new String[] { exp.type.toChars(context) }));
				}
				return null;
			}
		}
		sc = sc.push(sym);

		if (body != null) {
			body = body.semantic(sc, context);
		}

		sc.pop();

		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		WithStatement s = context.newWithStatement(filename, lineNumber, exp.syntaxCopy(context),
				body != null ? body.syntaxCopy(context) : null);
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("with (");
		exp.toCBuffer(buf, hgs, context);
		buf.writestring(")\n");
		if (body != null) {
			body.toCBuffer(buf, hgs, context);
		}
	}
	
	@Override
	public boolean usesEH(SemanticContext context) {
		return body != null ? body.usesEH(context) : false;
	}

}
