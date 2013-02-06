package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TY.Ttypeof;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class TypeTypeof extends TypeQualified {

	public Expression exp, sourceExp;
	public int typeofStart;
	public int typeofLength;
	
	// Descent: to improve performance, must be set by Parser or ModuleBuilder
	public ASTNodeEncoder encoder;  

	public TypeTypeof(char[] filename, int lineNumber, Expression exp, ASTNodeEncoder encoder) {
		super(filename, lineNumber, TY.Ttypeof);
		this.exp = this.sourceExp = exp;
		this.encoder = encoder;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceExp);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return TYPE_TYPEOF;
	}

	@Override
	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		Type t;

		{
			sc.intypeof++;
			exp = exp.semantic(sc, context);
			sc.intypeof--;
			t = exp.type;
			if (null == t) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.ExpressionHasNoType, exp, exp.toChars(context)));
				}
				return tvoid;
			}
			if (t.ty == Ttypeof) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForwardReferenceToSymbol, this, toChars(context)));
				}
			}
		}

		if (idents != null && idents.size() != 0) {
			Dsymbol s = t.toDsymbol(sc, context);
			for (int i = 0; i < idents.size(); i++) {
				if (null == s) {
					break;
				}
				IdentifierExp id = idents.get(i);
				s = s.searchX(filename, lineNumber, sc, id, context);
			}
			if (s != null) {
				t = s.getType(context);
				if (null == t) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolNotAType, this, new String[] { s.toChars(context) }));
					}
					return tvoid;
				}
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.CannotResolveDotProperty, this, new String[] { toChars(context) }));
				}
				return tvoid;
			}
		}
		return t;
	}

	public void setTypeofSourceRange(int start, int length) {
		this.typeofStart = start;
		this.typeofLength = length;
	}

	@Override
	public int size(char[] filename, int lineNumber, SemanticContext context) {
		if (exp.type != null) {
			return exp.type.size(filename, lineNumber, context);
		} else {
			return super.size(filename, lineNumber, context);
		}
	}

	@Override
	public Type syntaxCopy(SemanticContext context) {
		TypeTypeof t = new TypeTypeof(filename, lineNumber, exp.syntaxCopy(context), encoder);
		t.syntaxCopyHelper(this, context);
		t.mod = mod;
		t.copySourceRange(this);
		return t;
	}

	@Override
	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod, SemanticContext context) {
	    if (mod != this.mod) {
			toCBuffer3(buf, hgs, mod, context);
			return;
		}
		buf.writestring("typeof(");
		exp.toCBuffer(buf, hgs, context);
		buf.writeByte(')');
		toCBuffer2Helper(buf, hgs, context);
	}

	@Override
	public Dsymbol toDsymbol(Scope sc, SemanticContext context) {
		Type t = semantic(filename, lineNumber, sc, context);
		if (same(t, this, context)) {
			return null;
		}
		return t.toDsymbol(sc, context);
	}
	
	@Override
	protected void appendSignature0(StringBuilder sb, int options) {
		sb.append(Signature.C_TYPEOF);
		char[] expc = encoder.encodeExpression(exp);
		sb.append(expc.length);
		sb.append(Signature.C_TYPEOF);
		sb.append(expc);
	}

}
