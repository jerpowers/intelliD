package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKstring;

import melnorme.utilbox.core.Assert;
import melnorme.utilbox.tree.TreeVisitor;


import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class PragmaDeclaration extends AttribDeclaration {

	public Expressions args, sourceArgs;

	public PragmaDeclaration(char[] filename, int lineNumber, IdentifierExp ident, Expressions args,
			Dsymbols decl) {
		super(decl);
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.ident = ident;
		this.args = args;
		if (args != null) {
			sourceArgs = new Expressions(args);
		}
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceArgs);
			TreeVisitor.acceptChildren(visitor, decl);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return PRAGMA_DECLARATION;
	}

	@Override
	public String kind() {
		return "pragma";
	}

	@Override
	public boolean oneMember(Dsymbol[] ps, SemanticContext context) {
		ps[0] = null;
		return true;
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) { // Should be
		// merged with PragmaStatement

		if (equals(ident, Id.msg)) {
			if (args != null) {
				for (int i = 0; i < args.size(); i++) {
					Expression e = args.get(i);

					e = e.semantic(sc, context);
					e = e.optimize(WANTvalue | WANTinterpret, context);
					if (e.op == TOKstring) {
						message(e);
					} else {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.StringExpectedForPragmaMsg, e));
						}
					}
				}
			}
			semantic_Lnodecl(context);
			return;
		} else if (equals(ident, Id.lib)) {
			if (args == null || args.size() != 1) {
				if (context.acceptsErrors()) {
					context
							.acceptProblem(Problem
									.newSemanticTypeErrorLoc(
											IProblem.LibPragmaMustRecieveASingleArgumentOfTypeString,
											this));
				}
			} else {
				Expression e = args.get(0);

				e = e.semantic(sc, context);
				e = e.optimize(WANTvalue | WANTinterpret, context);
				args.set(0, e);
				if (e.op != TOKstring) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.StringExpectedForPragmaLib, e));
					}
				}
			}
			semantic_Lnodecl(context);
			return;
		} else if (context.isD2() && equals(ident, Id.startaddress)) {
			if (args == null || args.size() != 1) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.FunctionNameExpectedForStartAddress, this));
				}
			} else {
				Expression e = (Expression) args.get(0);
				e = e.semantic(sc, context);
				e = e.optimize(WANTvalue | WANTinterpret, context);
				args.set(0, e);
				Dsymbol sa = getDsymbol(e, context);
				if (null == sa || null == sa.isFuncDeclaration()) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.FunctionNameExpectedForStartAddress, e));
					}
				}
			}
			semantic_Lnodecl(context);
			return;
		} else {
			if (!context.global.ignoreUnsupportedPragmas) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.UnrecognizedPragma, ident));
				}
			}
		}

		if (decl != null) {
			for (Dsymbol s : decl) {
				s.semantic(sc, context);
			}
		}
		return;
	}

	protected void message(Expression e) {

	}

	private void semantic_Lnodecl(SemanticContext context) {
		if (decl != null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeErrorLoc(
						IProblem.PragmaIsMissingClosingSemicolon, this));
			}
		}
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		PragmaDeclaration pd;

		Assert.isTrue(s == null);
		pd = context.newPragmaDeclaration(filename, lineNumber, ident, Expression.arraySyntaxCopy(args,
				context), arraySyntaxCopy(decl, context));
		pd.copySourceRange(this);
		return pd;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("pragma(");
		buf.writestring(ident.toChars());
		if (args != null && args.size() != 0) {
			if (context.isD2()) {
		        buf.writestring(", ");
		        argsToCBuffer(buf, args, hgs, context);
			} else {
				for (Expression e : args) {
					buf.writestring(", ");
					e.toCBuffer(buf, hgs, context);
				}
			}
		}
		buf.writestring(")");
		super.toCBuffer(buf, hgs, context);
	}

	@Override
	public int getErrorStart() {
		return start;
	}

	@Override
	public int getErrorLength() {
		return 6; // "pragma".length()
	}

	@Override
	public String getSignature(int options) {
		return parent.getSignature(options);
	}

}
