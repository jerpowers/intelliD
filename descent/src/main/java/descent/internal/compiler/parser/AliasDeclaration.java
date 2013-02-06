package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCref;
import static descent.internal.compiler.parser.TOK.TOKfunction;
import static descent.internal.compiler.parser.TOK.TOKvar;
import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.core.IField__Marker;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class AliasDeclaration extends Declaration {

	public boolean first = true; // is this the first declaration in a multi
	public AliasDeclaration next;

	public Type htype;
	public Dsymbol haliassym;
	public Dsymbol aliassym;
	public Dsymbol overnext; // next in overload list
	public int inSemantic;

	public boolean isImportAlias;

	/*
	 * Descent: true if this alias is just for a template parameter. This
	 * is useful to not use these aliases when showing nice stuff.
	 * See Type#alias
	 */
	public boolean isTemplateParameter;

	private IField__Marker javaElement;

	public AliasDeclaration(char[] filename, int lineNumber, IdentifierExp id, Dsymbol s) {
		super(id);

		Assert.isTrue(s != this);

		this.filename =  filename;
		this.lineNumber = lineNumber;
		this.type = null;
		this.aliassym = s;
		this.htype = null;
		this.haliassym = null;
		this.overnext = null;
		this.inSemantic = 0;

		Assert.isNotNull(s);
	}

	public AliasDeclaration(char[] filename, int lineNumber, IdentifierExp id, Type type) {
		super(id);
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.type = type;
		this.sourceType = type;
		this.aliassym = null;
		this.htype = null;
		this.haliassym = null;
		this.overnext = null;
		this.inSemantic = 0;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceType);
			TreeVisitor.acceptChildren(visitor, ident);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return ALIAS_DECLARATION;
	}

	@Override
	public Type getType(SemanticContext context) {
		return type;
	}

	@Override
	public AliasDeclaration isAliasDeclaration() {
		return this;
	}

	@Override
	public String kind() {
		return "alias";
	}

	@Override
	public boolean overloadInsert(Dsymbol s, SemanticContext context) {
		/*
		 * Don't know yet what the aliased symbol is, so assume it can be
		 * overloaded and check later for correctness.
		 */

		if (overnext == null) {
			overnext = s;
			s.overprevious = this;
			return true;
		} else {
			return overnext.overloadInsert(s, context);
		}
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		// For Descent resolution
		if (ident != null) {
			context.setResolvedSymbol(ident, this);
		}

		if (aliassym != null) {
			if (aliassym.isTemplateInstance() != null) {
				aliassym.semantic(sc, context);
			}
			return;
		}
		this.inSemantic = 1;

		if ((storage_class & STC.STCconst) != 0) {
			errorOnModifier(IProblem.AliasCannotBeConst, TOK.TOKconst, context);
		}

		storage_class |= sc.stc & STC.STCdeprecated;

		// Given:
		// alias foo.bar.abc def;
		// it is not knowable from the syntax whether this is an alias
		// for a type or an alias for a symbol. It is up to the semantic()
		// pass to distinguish.
		// If it is a type, then type is set and getType() will return that
		// type. If it is a symbol, then aliassym is set and type is NULL -
		// toAlias() will return aliasssym.

		Dsymbol[] s = { null };
		Type[] t = { null };
		Expression[] e = { null };

		/* This section is needed because resolve() will:
		 *   const x = 3;
		 *   alias x y;
		 * try to alias y to 3.
		 */
		s[0] = type.toDsymbol(sc, context);

		if (context.isD1()) {
			if (s[0] != null) {
				// goto L2;
				semantic_L2(sc, context, s[0]); // it's a symbolic alias
				return;
			}

			type.resolve(filename, lineNumber, sc, e, t, s, context);
			if (s[0] != null) {
				// goto L2;
				semantic_L2(sc, context, s[0]); // it's a symbolic alias
				return;
			} else if (e[0] != null) {
				// Try to convert Expression to Dsymbol
				if (context.isD2()) {
					s[0] = getDsymbol(e[0], context);
					if (s[0] != null) {
					    // goto L2;
						semantic_L2(sc, context, s[0]); // it's a symbolic alias
						return;
					}

					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotAliasAnExpression, sourceType, e[0].toChars(context)));
					}
					t[0] = e[0].type;
				} else {
					if (e[0].op == TOKvar) {
						s[0] = ((VarExp) e[0]).var;
						// goto L2;
						semantic_L2(sc, context, s[0]); // it's a symbolic alias
						return;
					} else if (e[0].op == TOKfunction) {
						s[0] = ((FuncExp) e[0]).fd;
						// goto L2;
						semantic_L2(sc, context, s[0]); // it's a symbolic alias
						return;
					} else {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotAliasAnExpression, sourceType, e[0].toChars(context)));
						}
						t[0] = e[0].type;
					}
				}
			} else if (t[0] != null) {
				type = t[0];
			}
		} else {
		    if (s[0] != null
					&& ((s[0].getType(context) != null && type.equals(s[0]
							.getType(context))) || s[0].isEnumMember() != null)) {
				// it's a symbolic alias
				// goto L2;
				semantic_L2(sc, context, s[0]); // it's a symbolic alias
				return;
			}

			if ((storage_class & STCref) != 0) {
				// For 'ref' to be attached to
				// function types, and
				// picked
				// up by Type::resolve(), it has to go into sc.
				sc = sc.push();
				sc.stc |= STCref;
				type.resolve(filename, lineNumber, sc, e, t, s, context);
				sc = sc.pop();
			} else
				type.resolve(filename, lineNumber, sc, e, t, s, context);
			if (s[0] != null) {
				// goto L2;
				semantic_L2(sc, context, s[0]); // it's a symbolic alias
				return;
			} else if (e[0] != null) {
				// Try to convert Expression to Dsymbol
				s[0] = getDsymbol(e[0], context);
				if (s[0] != null) {
					// goto L2;
					semantic_L2(sc, context, s[0]); // it's a symbolic alias
					return;
				}

				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.CannotAliasAnExpression, sourceType, e[0]
									.toChars(context)));
				}

				t[0] = e[0].type;
			} else if (t[0] != null) {
				type = t[0];
			}
		}
		if (overnext != null) {
			ScopeDsymbol.multiplyDefined(null, 0, this, overnext, context);
		}
		this.inSemantic = 0;
		return;
	}

	public void semantic_L1(Scope sc, SemanticContext context) {
		if (overnext != null) {
			ScopeDsymbol.multiplyDefined(null, 0, this, overnext, context);
		}
		type = type.semantic(filename, lineNumber, sc, context);
		this.inSemantic = 0;
		return;
	}

	public void semantic_L2(Scope sc, SemanticContext context, Dsymbol s) {
		Type tempType = type;
		type = null;
		VarDeclaration v = s.isVarDeclaration();
		if (v != null && v.linkage == LINK.LINKdefault) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.ForwardReferenceOfSymbol, tempType, tempType.toString()));
				context
						.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ForwardReferenceOfSymbol, v.ident, new String(v.ident.ident)));
			}
			s = null;
		} else {
			FuncDeclaration f = s.toAlias(context).isFuncDeclaration();
			if (f != null) {
				if (overnext != null) {
					FuncAliasDeclaration fa = new FuncAliasDeclaration(filename, lineNumber, f);
					if (!fa.overloadInsert(overnext, context)) {
						ScopeDsymbol.multiplyDefined(null, 0, f, overnext, context);
					}
					overnext = null;
					s = fa;
					s.parent = sc.parent;
				}
			}
			if (overnext != null) {
				ScopeDsymbol.multiplyDefined(null, 0, s, overnext, context);
			}
			if (s == this) {
				s = null;
			}
		}
		aliassym = s;
		this.inSemantic = 0;
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		Assert.isTrue(s == null);
		AliasDeclaration sa;
		if (type != null) {
			sa = context.newAliasDeclaration(filename, lineNumber, ident, type.syntaxCopy(context));
		} else {
			sa = context.newAliasDeclaration(filename, lineNumber, ident, aliassym.syntaxCopy(null, context));
		}
		// Syntax copy for header file
		if (htype == null) // Don't overwrite original
		{
			if (type != null) // Make copy for both old and new instances
			{
				htype = type.syntaxCopy(context);
				sa.htype = type.syntaxCopy(context);
			}
		} else {
			// Make copy of original for new instance
			sa.htype = htype.syntaxCopy(context);
		}
		if (haliassym == null) {
			if (aliassym != null) {
				haliassym = aliassym.syntaxCopy(s, context);
				sa.haliassym = aliassym.syntaxCopy(s, context);
			}
		} else {
			sa.haliassym = haliassym.syntaxCopy(s, context);
		}
		sa.copySourceRange(this);
		sa.javaElement = javaElement;
		return sa;
	}

	@Override
	public Dsymbol toAlias(SemanticContext context) {
		Assert.isTrue(this != aliassym);
		if (inSemantic != 0) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.CircularDefinition, ident, toChars(context)));
			}
			aliassym = new TypedefDeclaration(filename, lineNumber, ident, Type.terror, null);
		}
		Dsymbol s = aliassym != null ? aliassym.toAlias(context) : this;
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("alias ");
		if (aliassym != null) {
			aliassym.toCBuffer(buf, hgs, context);
			buf.writeByte(' ');
			buf.writestring(ident.toChars());
		} else {
			type.toCBuffer(buf, ident, hgs, context);
		}
		buf.writeByte(';');
		buf.writenl();
	}

	@Override
	public char getSignaturePrefix() {
		return Signature.C_ALIAS;
	}

	public void setJavaElement(IField__Marker field) {
		this.javaElement = field;
	}

	@Override
	public IField__Marker getJavaElement() {
		return javaElement;
	}

}
