package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.TY.Tident;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TypeIdentifier extends TypeQualified {

	public IdentifierExp ident;

	public TypeIdentifier(char[] filename, int lineNumber, char[] ident) {
		this(filename, lineNumber, new IdentifierExp(filename, lineNumber, ident));
	}

	public TypeIdentifier(char[] filename, int lineNumber, IdentifierExp ident) {
		super(filename, lineNumber, TY.Tident);
		this.ident = ident;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, idents);			
		}
		visitor.endVisit(this);
	}

	@Override
	public MATCH deduceType(Scope sc, Type tparam,
			TemplateParameters parameters, Objects dedtypes,
			SemanticContext context) {
		// Extra check
		if (tparam != null && tparam.ty == Tident) {
			TypeIdentifier tp = (TypeIdentifier) tparam;

			for (int i = 0; i < idents.size(); i++) {
				IdentifierExp id1 = idents.get(i);
				IdentifierExp id2 = tp.idents.get(i);

				if (!id1.equals(id2)) {
					return MATCHnomatch;
				}
			}
		}
		return super.deduceType(sc, tparam, parameters, dedtypes, context);
	}

	@Override
	public int getNodeType() {
		return TYPE_IDENTIFIER;
	}

	@Override
	public Type reliesOnTident() {
		return this;
	}

	@Override
	public void resolve(char[] filename, int lineNumber, Scope sc, Expression[] pe, Type[] pt,
			Dsymbol[] ps, SemanticContext context) {
		Dsymbol s;
		Dsymbol[] scopesym = { null };

		s = sc.search(filename, lineNumber, ident, scopesym, context);
		
		// Descent: for binding resolution
		ident.setResolvedSymbol(s, context);
		
		resolveHelper(filename, lineNumber, sc, s, scopesym[0], pe, pt, ps, context);
		
		if (pt != null && pt.length > 0 && pt[0] != null && 
				s != null
				) {
			if (!equals(ident.ident, pt[0].identRep())) {
				pt[0] = pt[0].copy();
				pt[0].alias = s;
			}
		}
		
		if (pt != null && pt[0] != null) {
			pt[0] = pt[0].addMod(mod, context);
		}
	}

	@Override
	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		Type[] t = { null };
		Expression[] e = { null };
		Dsymbol[] s = { null };

		resolve(filename, lineNumber, sc, e, t, s, context);
		
		if (t[0] != null) {
			if (t[0].ty == TY.Ttypedef) {
				TypeTypedef tt = (TypeTypedef) t[0];

				if (tt.sym.sem == 1) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.CircularReferenceOfTypedef, tt.sym.ident, new String[] { tt.sym.ident.toString() }));
					}
				}
			}
			t[0] = t[0].addMod(mod, context);
		} else {
			if (s[0] != null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.UsedAsAType, this, new String[] { toChars(context) }));
				}
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.UsedAsAType, this, new String[] { toChars(context) }));
				}
			}
			t[0] = tvoid;
		}
		
		return t[0];
	}

	@Override
	public Type syntaxCopy(SemanticContext context) {
		TypeIdentifier t;

		t = new TypeIdentifier(filename, lineNumber, ident);
		t.syntaxCopyHelper(this, context);
		t.copySourceRange(this);
		return t;
	}

	@Override
	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod, SemanticContext context) {
	    if (mod != this.mod) {
			toCBuffer3(buf, hgs, mod, context);
			return;
		}
		buf.writestring(this.ident.toChars());
		toCBuffer2Helper(buf, hgs, context);
	}

	@Override
	public char[] toCharArray() {
		if (idents == null || idents.isEmpty()) {
			return ident.ident;
		} else {
			return super.toCharArray();
		}
	}

	@Override
	public void toDecoBuffer(OutBuffer buf, int flag, SemanticContext context) {
		Type_toDecoBuffer(buf, flag, context);
		
		String name = ident.toChars();
		int len = name.length();
		buf.writestring(len);
		buf.writestring(name);
	}

	@Override
	public Dsymbol toDsymbol(Scope sc, SemanticContext context) {
		if (null == sc) {
			return null;
		}

		Dsymbol[] scopesym = { null };
		Dsymbol s = sc.search(filename, lineNumber, ident, scopesym, context);
		
		// Descent: for binding resolution
		context.setResolvedSymbol(ident, s);
		
		if (s != null) {
			if (idents != null) {
				for (int i = 0; i < idents.size(); i++) {
					IdentifierExp id = idents.get(i);
					s = s.searchX(filename, lineNumber, sc, id, context);
					if (null == s) // failed to find a symbol
					{
						break;
					} else {
						context.setResolvedSymbol(id, s);
					}
				}
			}
		}
		return s;
	}

	@Override
	public Expression toExpression() {
		Expression e = new IdentifierExp(filename, lineNumber, ident.ident);
		e.setSourceRange(ident.start, ident.length);
		if (idents != null) {
			for (IdentifierExp id : idents) {
				e = new DotIdExp(filename, lineNumber, e, id);
				e.setSourceRange(ident.start, id.start + id.length
						- ident.start);
			}
		}
		return e;
	}
	
	@Override
	protected void appendSignature0(StringBuilder sb, int options) {
		sb.append(Signature.C_IDENTIFIER);
		ident.appendSignature(sb, options);
		
		if (idents != null) {
			for (int i = 0; i < idents.size(); i++) {
				IdentifierExp ident = idents.get(i);
				if (ident == null) {
					sb.append(0);
				} else {
					ident.appendSignature(sb, options);
				}
				
				if (ident instanceof TemplateInstanceWrapper && i != idents.size() - 1) {
					sb.append(Signature.C_IDENTIFIER);
				}
			}
		}
	}

}
