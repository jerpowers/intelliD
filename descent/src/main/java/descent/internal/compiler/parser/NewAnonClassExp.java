package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class NewAnonClassExp extends Expression {

	public Expression thisexp;
	public Expressions newargs;
	public ClassDeclaration cd;
	public Expressions arguments;

	public NewAnonClassExp(char[] filename, int lineNumber, Expression thisexp, Expressions newargs,
			ClassDeclaration cd, Expressions arguments) {
		super(filename, lineNumber, TOK.TOKnewanonclass);
		this.thisexp = thisexp;
		this.newargs = newargs;
		this.cd = cd;
		this.arguments = arguments;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, thisexp);
			TreeVisitor.acceptChildren(visitor, newargs);
			TreeVisitor.acceptChildren(visitor, cd);
			TreeVisitor.acceptChildren(visitor, arguments);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public boolean canThrow(SemanticContext context) {
		if (context.isD2()) {
			return true;
		} else {
			return super.canThrow(context);
		}
	}
	
	@Override
	public int checkSideEffect(int flag, SemanticContext context) {
		return 1;
	}

	@Override
	public int getNodeType() {
		return NEW_ANON_CLASS_EXP;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression d = new DeclarationExp(filename, lineNumber, cd);
		d = d.semantic(sc, context);

		Expression n = new NewExp(filename, lineNumber, thisexp, newargs, cd.type, arguments);

		Expression c = new CommaExp(filename, lineNumber, d, n);
		return c.semantic(sc, context);
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		return new NewAnonClassExp(filename, lineNumber, thisexp != null ? thisexp.syntaxCopy(context)
				: null, arraySyntaxCopy(newargs, context), (ClassDeclaration) cd
				.syntaxCopy(null, context), arraySyntaxCopy(arguments, context));
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (thisexp != null) {
			expToCBuffer(buf, hgs, thisexp, PREC.PREC_primary, context);
			buf.writeByte('.');
		}
		buf.writestring("new");
		if (newargs != null && newargs.size() > 0) {
			buf.writeByte('(');
			argsToCBuffer(buf, newargs, hgs, context);
			buf.writeByte(')');
		}
		buf.writestring(" class ");
		if (arguments != null && arguments.size() > 0) {
			buf.writeByte('(');
			argsToCBuffer(buf, arguments, hgs, context);
			buf.writeByte(')');
		}
		if (cd != null) {
			cd.toCBuffer(buf, hgs, context);
		}
	}

}
