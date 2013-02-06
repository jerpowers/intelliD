package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEfallthru;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class Catch extends ASTDmdNode {

	public int lineNumber;
	public char[] filename;
	public Type type, sourceType;
	public IdentifierExp ident;
	public VarDeclaration var;
	public Statement handler, sourceHandler;

	public Catch(char[] filename, int lineNumber, Type type, IdentifierExp id, Statement handler) {
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.type = sourceType = type;
		this.ident = id;
		this.handler = sourceHandler = handler;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceType);
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, sourceHandler);
		}
		visitor.endVisit(this);
	}
	
	public int blockExit(SemanticContext context) {
		return handler != null ? handler.blockExit(context) : BEfallthru;
	}

	@Override
	public int getNodeType() {
		return CATCH;
	}

	public void semantic(Scope sc, SemanticContext context) {
		ScopeDsymbol sym;

		if (context.IN_GCC) {
			if (sc.tf != null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.CannotPutCatchStatementInsideFinallyBlock, this));
				}
			}
		}

		sym = new ScopeDsymbol();
		sym.parent = sc.scopesym;
		sc = sc.push(sym);

		if (type == null) {
			type = new TypeIdentifier(null, 0, Id.Object);
		}
		type = type.semantic(filename, lineNumber, sc, context);
		if (type.toBasetype(context).isClassHandle() == null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.CanOnlyCatchClassObjects, sourceType, type.toChars(context)));
			}
		} else if (ident != null) {
			var = new VarDeclaration(filename, lineNumber, type, ident, null);
			var.parent = sc.parent;
			sc.insert(var);
		}
		if (handler != null) {
			handler = handler.semantic(sc, context);
		}

		sc.pop();
	}

	public Catch syntaxCopy(SemanticContext context) {
		Catch c = new Catch(filename, lineNumber, (type != null ? type.syntaxCopy(context) : null),
				ident, (handler != null ? handler.syntaxCopy(context) : null));
		return c;
	}

	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("catch");
		if (type != null) {
			buf.writebyte('(');
			type.toCBuffer(buf, ident, hgs, context);
			buf.writebyte(')');
		}
		buf.writenl();
		buf.writebyte('{');
		buf.writenl();
		if (handler != null) {
			handler.toCBuffer(buf, hgs, context);
		}
		buf.writebyte('}');
		buf.writenl();
	}
	
	@Override
	public int getErrorStart() {
		return start;
	}
	
	@Override
	public int getErrorLength() {
		return 5; // "catch".length()
	}

}
