package descent.internal.compiler.parser;

import descent.core.compiler.CharOperation;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class IdentifierExp extends Expression {
	
	public final static IdentifierExp EMPTY = new IdentifierExp(Id.empty);
	
	public static int count;
	public static char[] generateId(char[] id) {
		StringBuilder s = new StringBuilder();
		s.append(id);
		s.append(count);
		count++;
		if (count < 0) {
			count = 0;
		}
		return s.toString().toCharArray();
	}

	public char[] ident;

	public IdentifierExp(char[] filename, int lineNumber) {
		super(filename, lineNumber, TOK.TOKidentifier);
	}
	
	public IdentifierExp(char[] ident) {
		this(null, 0, ident);
	}

	public IdentifierExp(char[] filename, int lineNumber, char[] ident) {
		this(filename, lineNumber);
		this.ident = ident;
	}

	public IdentifierExp(char[] filename, int lineNumber, IdentifierExp ident) {
		this(filename, lineNumber);
		this.ident = ident.ident;
		this.start = ident.start;
		this.length = ident.length;
	}

	public IdentifierExp(char[] filename, int lineNumber, Token token) {
		this(filename, lineNumber);
		this.ident = token.sourceString;
		this.start = token.ptr;
		this.length = token.sourceLen;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public DYNCAST dyncast() {
		return DYNCAST.DYNCAST_IDENTIFIER;
	}

	@Override
	public boolean equals(Object o, SemanticContext context) {
		if (o instanceof char[]) {
			char[] c = (char[]) o;
			return CharOperation.equals(ident, c);
		}
		
		if (!(o instanceof IdentifierExp)) {
			return false;
		}

		IdentifierExp i = (IdentifierExp) o;
		return equals(this, i);
	}

	@Override
	public int getNodeType() {
		return IDENTIFIER_EXP;
	}

	@Override
	public boolean isLvalue(SemanticContext context) {
		return true;
	}
	
	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Dsymbol s;
		Dsymbol[] scopesym = { null };
		
		s = sc.search(filename, lineNumber, this, scopesym, context);
		
		// Descent: for binding resolution
		context.setResolvedSymbol(this, s);
		
		if (s != null) {
			Expression e;
			WithScopeSymbol withsym;

			/* See if the symbol was a member of an enclosing 'with'
			 */
			withsym = scopesym[0].isWithScopeSymbol();
			if (withsym != null) {
				s = s.toAlias(context);

				// Same as wthis.ident
				if (s.needThis() || s.isTemplateDeclaration() != null) {
					e = new VarExp(filename, lineNumber, withsym.withstate.wthis);
					e = new DotIdExp(filename, lineNumber, e, this);
				} else {
					Type t = withsym.withstate.wthis.type;
					if (t.ty == TY.Tpointer) {
						t = ((TypePointer) t).next;
					}
					e = context.newTypeDotIdExp(filename, lineNumber, t, this);
				}
			} else {
				if (context.isD1()) {
					if (s.parent == null
							&& scopesym[0].isArrayScopeSymbol() != null) { 
						// Kludge to run semantic() here because
						// ArrayScopeSymbol::search() doesn't have access to sc.
						s.semantic(sc, context);
					}
				}
			    /* If f is really a function template,
			     * then replace f with the function template declaration.
			     */
				FuncDeclaration f = s.isFuncDeclaration();
				if (f != null && f.parent != null) {
					TemplateInstance ti = f.parent.isTemplateInstance();

					if (ti != null
							&& ti.isTemplateMixin() == null
							&& (equals(ti.name, f.ident) || 
									equals(ti.toAlias(context).ident, f.ident))
							&& ti.tempdecl != null
							&& ti.tempdecl.onemember != null) {
						TemplateDeclaration tempdecl = ti.tempdecl;
						if (tempdecl.overroot != null) { // if not start of
							// overloaded list of
							// TemplateDeclaration's
							tempdecl = tempdecl.overroot; // then get the
							// start
						}
						e = new TemplateExp(filename, lineNumber, tempdecl);
						e = e.semantic(sc, context);
						return e;
					}
				}
				if (context.isD2()) {
					// Haven't done overload resolution yet, so pass 1
					e = new DsymbolExp(filename, lineNumber, s, true);
				} else {
					e = new DsymbolExp(filename, lineNumber, s);
				}
				e.copySourceRange(this);
			}
			
			// Descent: for binding resolution
			e = e.semantic(sc, context);
			context.setResolvedExp(this, e);
			return e;
		}
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.UndefinedIdentifier, this,
					new String[] { new String(ident) }));
		}
		type = Type.terror;
		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (hgs.hdrgen) {
			buf.writestring(toHChars2());
		} else {
			buf.writestring(ident);
		}
	}
	
	private final static char[] notThis = { '~', 't', 'h', 'i', 's' };
	private final static char[] invariant = { 'i', 'n', 'v', 'a', 'r', 'i', 'a', 'n', 't' };
	private final static char[] unittest = { 'u', 'n', 'i', 't', 't', 'e', 's', 't' };
	private final static char[] staticThis = { 's', 't', 'a', 't', 'i', 'c', ' ', 't', 'h', 'i', 's' };
	private final static char[] staticNotThis = { 's', 't', 'a', 't', 'i', 'c', ' ', '~', 't', 'h', 'i', 's' };
	private final static char[] dollar = { '$' };
	private final static char[] with = { 'w', 'i', 't', 'h' };
	private final static char[] result = { 'r', 'e', 's', 'u', 'l', 't' };
	private final static char[] Return = { 'r', 'e', 't', 'u', 'r', 'n' };

	public char[] toHChars2() {
		char[] p = null;

	    if (equals(ident, Id.ctor)) {
			p = Id.This;
		} else if (equals(ident, Id.dtor)) {
			p = notThis;
		} else if (equals(ident, Id.classInvariant)) {
			p = invariant;
		} else if (equals(ident, Id.unitTest)) {
			p = unittest;
		} else if (equals(ident, Id.staticCtor)) {
			p = staticThis;
		} else if (equals(ident, Id.staticDtor)) {
			p = staticNotThis;
		} else if (equals(ident, Id.dollar)) {
			p = dollar;
		} else if (equals(ident, Id.withSym)) {
			p = with;
		} else if (equals(ident, Id.result)) {
			p = result;
		} else if (equals(ident, Id.returnLabel)) {
			p = Return;
		} else {
			p = ident;
		}

	    return p;
	}

	@Override
	public char[] toCharArray() {
		return ident;
	}
	
	public String toChars() {
//		return new String(ident).intern();
		if (ident == null) {
			return "";
		}
		return new String(ident);
	}

	@Override
	public String toChars(SemanticContext context) {
		return toChars();
	}

	@Override
	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
		return this;
	}
	
	@Override
	public IdentifierExp syntaxCopy(SemanticContext context) {
		return (IdentifierExp) super.syntaxCopy(context);
	}

	@Override
	public String toString() {
		if (ident == null) {
			return "null";
		} else {
			return new String(ident);
		}
	}
	
	@Override
	public void setResolvedSymbol(Dsymbol symbol, SemanticContext context) {
		context.setResolvedSymbol(this, symbol);
	}
	
	@Override
	public void setEvaluatedExpression(Expression exp, SemanticContext context) {
		context.setEvaluated(this, exp);
	}
	
	@Override
	public void setResolvedExpression(Expression exp, SemanticContext context) {
		if (exp instanceof CallExp) {
			context.setResolvedExp(this, ((CallExp) exp).e1);
		} else {
			context.setResolvedExp(this, exp);
		}
	}
	
	protected void appendSignature(StringBuilder sb, int options) {
		sb.append(ident.length);
		sb.append(ident);
	}

}
