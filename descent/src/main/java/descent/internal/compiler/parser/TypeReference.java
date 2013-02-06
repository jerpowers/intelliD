package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TY.Tbit;
import static descent.internal.compiler.parser.TY.Treference;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class TypeReference extends TypeNext {
	
	public TypeReference(Type t, SemanticContext context) {
		super(Treference, t);
		if (t.ty == Tbit) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.CannotMakeReferenceToABit, this));
			}
		}
	}
	
	@Override
	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		if (context.isD1()) {
			return super.semantic(filename, lineNumber, sc, context);
		} else {
		    Type n = next.semantic(filename, lineNumber, sc, context);
		    if (!same(n, next, context)) {
		    	deco = null;
		    }
		    next = n;
		    transitive(context);
		    return merge(context);
		}
	}

	@Override
	public Type syntaxCopy(SemanticContext context) {
		Type t = next.syntaxCopy(context);
		if (same(t, next, context)) {
			t = this;
		} else {
			t = new TypeReference(t, context);
			t.mod = mod;
			t.copySourceRange(this);
		}
		return t;
	}

	@Override
	public int size(char[] filename, int lineNumber, SemanticContext context) {
		return PTRSIZE;
	}

	@Override
	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod, SemanticContext context) {
	    if (mod != this.mod) {
			toCBuffer3(buf, hgs, mod, context);
			return;
		}
		next.toCBuffer2(buf, hgs, this.mod, context);
		buf.writeByte('&');
	}
	
	@Override
	public Expression dotExp(Scope sc, Expression e, IdentifierExp ident, SemanticContext context) {
		// References just forward things along
	    return next.dotExp(sc, e, ident, context);
	}
	
	@Override
	public Expression defaultInit(char[] filename, int lineNumber, SemanticContext context) {
		Expression e;
	    e = new NullExp(filename, lineNumber);
	    e.type = this;
	    return e;
	}
	
	@Override
	public boolean isZeroInit(char[] filename, int lineNumber, SemanticContext context) {
		return true;
	}

	@Override
	public int getNodeType() {
		return TYPE_REFERENCE;
	}

	@Override
	protected void accept0(IASTVisitor visitor) {
	}
	
	@Override
	protected void appendSignature0(StringBuilder sb, int options) {
		// TODO Descent signature		
	}

}
