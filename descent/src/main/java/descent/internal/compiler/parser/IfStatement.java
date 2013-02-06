package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEfallthru;
import static descent.internal.compiler.parser.BE.BEnone;
import static descent.internal.compiler.parser.BE.BEthrow;
import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class IfStatement extends Statement {

	public Argument arg;
	public Expression condition, sourceCondition;
	public Statement ifbody, sourceIfbody;
	public Statement elsebody, sourceElsebody;

	public VarDeclaration match; // for MatchExpression results

	public IfStatement(char[] filename, int lineNumber, Argument arg, Expression condition,
			Statement ifbody, Statement elsebody) {
		super(filename, lineNumber);
		this.arg = arg;
		this.condition = this.sourceCondition = condition;
		this.ifbody = this.sourceIfbody = ifbody;
		this.elsebody = this.sourceElsebody = elsebody;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, arg);
			TreeVisitor.acceptChildren(visitor, sourceCondition);
			TreeVisitor.acceptChildren(visitor, sourceIfbody);
			TreeVisitor.acceptChildren(visitor, sourceElsebody);
		}
		visitor.endVisit(this);
	}

	@Override
	public int blockExit(SemanticContext context) {
		int result = BEnone;
	    if (condition.canThrow(context)) {
	    	result |= BEthrow;
	    }
	    if (ifbody != null) {
	    	result |= ifbody.blockExit(context);
	    } else {
	    	result |= BEfallthru;
	    }
	    if (elsebody != null) {
	    	result |= elsebody.blockExit(context);
	    } else {
	    	result |= BEfallthru;
	    }
	    return result;
	}

	@Override
	public int getNodeType() {
		return IF_STATEMENT;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		if (istate.start == this) {
			istate.start = null;
		}
		if (istate.start != null) {
			Expression e = null;
			if (ifbody != null) {
				e = ifbody.interpret(istate, context);
			}
			if (istate.start != null && elsebody != null) {
				e = elsebody.interpret(istate, context);
			}
			return e;
		}

		Expression e = condition.interpret(istate, context);
		if (e == null) {
			throw new IllegalStateException("assert(e);");
		}
		if (e != EXP_CANT_INTERPRET) {
			if (e.isBool(true)) {
				e = ifbody != null ? ifbody.interpret(istate, context)
						: null;
			} else if (e.isBool(false)) {
				e = elsebody != null ? elsebody.interpret(istate, context)
						: null;
			} else {
				e = EXP_CANT_INTERPRET;
			}
		}
		return e;
	}

	@Override
	public IfStatement isIfStatement() {
		return this;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		condition = condition.semantic(sc, context);
		condition = resolveProperties(sc, condition, context);
		condition = condition.checkToBoolean(context);

		// If we can short-circuit evaluate the if statement, don't do the
		// semantic analysis of the skipped code.
		// This feature allows a limited form of conditional compilation.
		condition = condition.optimize(WANTflags, context);

		// Evaluate at runtime
		int cs0 = sc.callSuper;
		int cs1;

		Scope scd;
		if (arg != null) {
			/*
			 * Declare arg, which we will set to be the result
			 * of condition.
			 */
			ScopeDsymbol sym = new ScopeDsymbol();
			sym.parent = sc.scopesym;
			scd = sc.push(sym);

			Type t = arg.type != null ? arg.type : condition.type;
			match = new VarDeclaration(filename, lineNumber, t, arg.ident, null);
			arg.var = match;

			match.noauto = true;
			match.semantic(scd, context);
			if (scd.insert(match) == null) {
				Assert.isTrue(false);
			}
			match.parent = sc.func;

			/*
			 * Generate: (arg = condition)
			 */
			VarExp v = new VarExp(filename, lineNumber, match);
			condition = new AssignExp(filename, lineNumber, v, condition);
			condition = condition.semantic(scd, context);
		} else {
			scd = sc.push();
		}

		// ifbody may be null if there is a syntaxis error
		if (ifbody == null) {
			return this;
		}

		ifbody = ifbody.semantic(scd, context);
		scd.pop();

		cs1 = sc.callSuper;
		sc.callSuper = cs0;
		if (elsebody != null) {
			elsebody = elsebody.semanticScope(sc, null, null, context);
		}
		sc.mergeCallSuper(filename, lineNumber, cs1, this, context);

		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		Statement i = null;
		if (ifbody != null) {
			i = ifbody.syntaxCopy(context);
		}

		Statement e = null;
		if (elsebody != null) {
			e = elsebody.syntaxCopy(context);
		}

		Argument a = arg != null ? arg.syntaxCopy(context) : null;
		IfStatement s = context.newIfStatement(filename, lineNumber, a, condition.syntaxCopy(context), i, e);
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("if (");
		if (arg != null) {
			if (arg.type != null) {
				arg.type.toCBuffer(buf, arg.ident, hgs, context);
			} else {
				buf.writestring("auto ");
				buf.writestring(arg.ident.toChars());
			}
			buf.writestring(" = ");
		}
		condition.toCBuffer(buf, hgs, context);
		buf.writebyte(')');
		buf.writenl();
		ifbody.toCBuffer(buf, hgs, context);
		if (elsebody != null) {
			buf.writestring("else");
			buf.writenl();
			elsebody.toCBuffer(buf, hgs, context);
		}
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		return (ifbody != null && ifbody.usesEH(context))
				|| (elsebody != null && elsebody.usesEH(context));
	}

}
