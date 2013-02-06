package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TypeDelegate extends TypeNext {

	public TypeDelegate(Type next) {
		super(TY.Tdelegate, next);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceNext);
		}
		visitor.endVisit(this);
	}

	@Override
	public boolean checkBoolean(SemanticContext context) {
		return true;
	}

	@Override
	public Expression defaultInit(char[] filename, int lineNumber, SemanticContext context) {
		Expression e;
		e = new NullExp(filename, lineNumber);
		e.type = this;
		return e;
	}

	@Override
	public Expression dotExp(Scope sc, Expression e, IdentifierExp ident,
			SemanticContext context) {
		if (equals(ident, Id.ptr)) {
			e.type = context.Type_tvoidptr;
			return e;
		} else if (equals(ident, Id.funcptr)) {
			e = e.addressOf(sc, context);
			e.type = context.Type_tvoidptr;
			e = new AddExp(e.filename, e.lineNumber,  e, new IntegerExp(PTRSIZE));
			e.type = context.Type_tvoidptr;
			e = new PtrExp(e.filename, e.lineNumber,  e);
			e.type = next.pointerTo(context);
			return e;
		} else {
			e = super.dotExp(sc, e, ident, context);
		}
		return e;
	}

	@Override
	public int getNodeType() {
		return TYPE_DELEGATE;
	}

	@Override
	public TypeInfoDeclaration getTypeInfoDeclaration(SemanticContext context) {
		return new TypeInfoDelegateDeclaration(this, context);
	}

	@Override
	public boolean hasPointers(SemanticContext context) {
		return true;
	}

	@Override
	public boolean isZeroInit(char[] filename, int lineNumber, SemanticContext context) {
		return true;
	}

	@Override
	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		if (deco != null) { // if semantic() already run
			return this;
		}
		next = next.semantic(filename, lineNumber, sc, context);
		return merge(context);
	}

	@Override
	public int size(char[] filename, int lineNumber, SemanticContext context) {
		return PTRSIZE * 2;
	}

	@Override
	public Type syntaxCopy(SemanticContext context) {
		Type t = next.syntaxCopy(context);
		if (same(t, next, context)) {
			t = this;
		} else {
			t = new TypeDelegate(t);
			t.mod = mod;
			t.copySourceRange(this);
		}
		return t;
	}

	@Override
	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod,
			SemanticContext context) {
		if (mod != this.mod) {
			toCBuffer3(buf, hgs, mod, context);
			return;
		}
		TypeFunction tf = (TypeFunction) next;

		tf.next.toCBuffer2(buf, hgs, 0, context);
		buf.writestring(" delegate");
		Argument.argsToCBuffer(buf, hgs, tf.parameters, tf.varargs, context);
	}
	
	@Override
	protected void appendSignature0(StringBuilder sb, int options) {
		sb.append('D');
		next.appendSignature(sb, options);
	}
	

}
