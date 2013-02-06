package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEfallthru;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class PragmaStatement extends Statement {

	public IdentifierExp ident;
	public Expressions args, sourceArgs;
	public Statement body, sourceBody;

	public PragmaStatement(char[] filename, int lineNumber, IdentifierExp ident, Expressions args,
			Statement body) {
		super(filename, lineNumber);
		this.ident = ident;
		this.args = args;
		if (args != null) {
			this.sourceArgs = new Expressions(args);
		}
		this.body = this.sourceBody = body;		
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, sourceArgs);
			TreeVisitor.acceptChildren(visitor, sourceBody);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		int result = BEfallthru;
		return result;
	}

	@Override
	public int getNodeType() {
		return PRAGMA_STATEMENT;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {

		// msg and lib char[] instances are reused by Lexer

		if (equals(ident, Id.msg)) {
			if (args != null) {
				for (int i = 0; i < args.size(); i++) {
					Expression e = args.get(i);

					e = e.semantic(sc, context);
					e = e.optimize(WANTvalue | WANTinterpret, context);
					if (e.op == TOK.TOKstring) {
						message(e);
					} else {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.StringExpectedForPragmaMsg, e));
						}
					}
				}
			}
		} else if (equals(ident, Id.lib)) {
			/* Should this be allowed?
			 */
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.PragmaLibNotAllowedAsStatement, this));
			}
			
//			if (args == null || args.size() != 1) {
//				if (context.acceptsErrors()) {
//					context
//							.acceptProblem(Problem
//									.newSemanticTypeErrorLoc(
//											IProblem.LibPragmaMustRecieveASingleArgumentOfTypeString,
//											this));
//				}
//			} else {
//				Expression e = args.get(0);
//				e = e.semantic(sc, context);
//				e = e.optimize(WANTvalue | WANTinterpret, context);
//				args.set(0, e);
//				if (e.op != TOK.TOKstring) {
//					if (context.acceptsErrors()) {
//						context.acceptProblem(Problem.newSemanticTypeError(
//								IProblem.StringExpectedForPragmaLib, e));
//					}
//
//				}
//			}
		} else if (context.isD2() && equals(ident, Id.startaddress)) {
			if (null == args || args.size() != 1) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.FunctionNameExpectedForStartAddress, this));
				}
			} else {
				Expression e = (Expression) args.get(0);
				e = e.semantic(sc, context);
				e = e.optimize(WANTvalue | WANTinterpret, context);
				args.set(0, e);
				Dsymbol sa = getDsymbol(e, context);
				if (null == sa || null == sa.isFuncDeclaration()) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.FunctionNameExpectedForStartAddress, e));
					}
				}
				if (body != null) {
					body = body.semantic(sc, context);
				}
				return this;
			}
		} else {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.UnrecognizedPragma, ident));
			}
		}

		if (body != null) {
			body = body.semantic(sc, context);
		}
		return body;
	}

	protected void message(Expression e) {
		
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("pragma (");
		buf.writestring(ident.toChars());
		if (args != null && args.size() > 0) {
			buf.writestring(", ");
			argsToCBuffer(buf, args, hgs, context);
		}
		buf.writeByte(')');
		if (body != null) {
			buf.writenl();
			buf.writeByte('{');
			buf.writenl();

			body.toCBuffer(buf, hgs, context);

			buf.writeByte('}');
			buf.writenl();
		} else {
			buf.writeByte(';');
			buf.writenl();
		}
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		return body != null && body.usesEH(context);
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		Statement b = null;
		if (body != null)
			b = body.syntaxCopy(context);
		PragmaStatement s = context.newPragmaStatement(filename, lineNumber, ident, Expression
				.arraySyntaxCopy(args, context), b);
		s.copySourceRange(this);
		return s;
	}
	
	@Override
	public int getErrorStart() {
		return start;
	}
	
	@Override
	public int getErrorLength() {
		return 6; // "pragma".length()
	}

}
