package descent.internal.compiler.parser;

import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class TypeReturn extends TypeQualified {

	public TypeReturn(char[] filename, int lineNumber) {
		super(filename, lineNumber, TY.Treturn);
	}
	
	@Override
	public Type syntaxCopy(SemanticContext context) {
		TypeReturn t = new TypeReturn(filename, lineNumber);
	    t.syntaxCopyHelper(this, context);
	    t.mod = mod;
	    t.copySourceRange(this);
	    return t;
	}
	
	@Override
	public Dsymbol toDsymbol(Scope sc, SemanticContext context) {
		Type t = semantic(null, 0, sc, context);
		if (same(t, this, context)) {
			return null;
		}
		return t.toDsymbol(sc, context);
	}
	
	@Override
	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		Type t;
		if (null == sc.func) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.TypeofReturnMustBeInsideFunction, this));
			}
			// goto Lerr;
			return terror;
		}
		t = sc.func.type.nextOf();
		t = t.addMod(mod, context);

		if (idents != null && idents.size() != 0) {
			Dsymbol s = t.toDsymbol(sc, context);
			for (int i = 0; i < idents.size(); i++) {
				if (null == s)
					break;
				IdentifierExp id = (IdentifierExp) idents.get(i);
				s = s.searchX(filename, lineNumber, sc, id, context);
			}
			if (s != null) {
				t = s.getType(context);
				if (null == t) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolNotAType, this, new String[] { s.toChars(context) }));
					}
					// goto Lerr;
					return terror;
				}
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotResolveDotProperty, this, new String[] { toChars(context) }));
				}
				// goto Lerr;
				return terror;
			}
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
		buf.writestring("typeof(return)");
		toCBuffer2Helper(buf, hgs, context);
	}

	@Override
	protected void appendSignature0(StringBuilder sb, int options) {
		sb.append(Signature.C_TYPEOF_RETURN);
	}

	@Override
	public int getNodeType() {
		return TYPE_RETURN;
	}

	@Override
	protected void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

}
