package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TY.Tsarray;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class DsymbolExp extends Expression {

	public Dsymbol s;
	public boolean hasOverloads;
	
	public DsymbolExp(char[] filename, int lineNumber, Dsymbol s) {
		this(filename, lineNumber, s, false);
	}

	public DsymbolExp(char[] filename, int lineNumber, Dsymbol s, boolean hasOverloads) {
		super(filename, lineNumber, TOK.TOKdsymbol);
		this.s = s;
		this.hasOverloads = hasOverloads;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return DSYMBOL_EXP;
	}
	
	@Override
	public boolean isLvalue(SemanticContext context) {
		return true;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		// Lagain:
		EnumMember em;
		Expression e;
		VarDeclaration v;
		FuncDeclaration f;
		FuncLiteralDeclaration fld;
		OverloadSet o;
		// Declaration d;
		ClassDeclaration cd;
		ClassDeclaration thiscd = null;
		Import imp;
		Package pkg;
		Type t;

		boolean loop = true;
		Lagain: while (loop) {
			loop = false;

			if (type != null) {
				return this;
			}
			
			// Added for Descent
			if (s == null && context.global.errors > 0) {
				return this;
			}
			
			if (s.isFuncDeclaration() == null) {
				checkDeprecated(sc, s, context);
			}
			s = s.toAlias(context);
			
			// Added for Descent
			if (s == null && context.global.errors > 0) {
				return this;
			}
			
			if (s.isFuncDeclaration() == null) {
				checkDeprecated(sc, s, context);
			}

			if (sc.func != null) {
				thiscd = sc.func.parent.isClassDeclaration();
			}

			// BUG: This should happen after overload resolution for functions, not before
			if (s.needThis()) {
				boolean condition;
				if (context.isD2()) {
					condition = hasThis(sc) != null /*&& !s.isFuncDeclaration()*/;
				} else {
					condition = hasThis(sc) != null && null == s.isFuncDeclaration();
				}	
				if (hasThis(sc) != null /*&& !s.isFuncDeclaration()*/) {
					// Supply an implicit 'this', as in
					//	  this.ident

					DotVarExp de;

					de = new DotVarExp(filename, lineNumber, new ThisExp(filename, lineNumber), s.isDeclaration());
					return de.semantic(sc, context);
				}
			}

			em = s.isEnumMember();
			if (em != null) {
				if (context.isD1()) {
					e = em.value().copy();
				} else {
					e = em.value();
				}
				e = e.semantic(sc, context);
				return e;
			}
			v = s.isVarDeclaration();
			if (v != null) {
				if (type == null) {
					type = v.type;
					if (v.type == null) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.ForwardReferenceOfSymbol, this,
									v.toString()));
						}
						type = Type.terror;
					}
				}
				if (context.isD1()) {
					if (v.isConst() && type.toBasetype(context).ty != Tsarray) {
						if (v.init() != null) {
							if (v.inuse() != 0) {
								if (context.acceptsErrors()) {
									context.acceptProblem(Problem.newSemanticTypeError(IProblem.CircularReferenceTo, this, v.toChars(context)));
								}
								type = Type.tint32;
								return this;
							}
							ExpInitializer ei = v.init().isExpInitializer();
							if (ei != null) {
								e = ei.exp.copy(); // make copy so we can change loc
								if (e.op == TOKstring || e.type == null) {
									e = e.semantic(sc, context);
								}
								e = e.implicitCastTo(sc, type, context);
								e.filename = filename;
								e.lineNumber = lineNumber;
								e.copySourceRange(this);
								return e;
							}
						} else {
							e = type.defaultInit(context);
							e.filename = filename;
							e.lineNumber = lineNumber;
							return e;
						}
					}
				}
				e = new VarExp(filename, lineNumber, v);
				e.copySourceRange(this);
				e.type = type;
				e = e.semantic(sc, context);
				return e.deref();
			}
			fld = s.isFuncLiteralDeclaration();
			if (fld != null) {
				e = new FuncExp(filename, lineNumber, fld);
				return e.semantic(sc, context);
			}
			f = s.isFuncDeclaration();
			if (f != null) {
				VarExp ve;
				if (context.isD1()) {
					ve = new VarExp(filename, lineNumber, f);
				} else {
					ve = new VarExp(filename, lineNumber, f, hasOverloads);
				}
				ve.copySourceRange(this);
				return ve;
			}
			if (context.isD2()) {
				o = s.isOverloadSet();
				if (o != null) {
					return new OverExp(o);
				}
			}
			cd = s.isClassDeclaration();
			if (cd != null && thiscd != null
					&& cd.isBaseOf(thiscd, null, context) && sc.func.needThis()) {
				// We need to add an implicit 'this' if cd is this class or a base class.
				DotTypeExp dte;

				dte = new DotTypeExp(filename, lineNumber, new ThisExp(filename, lineNumber), s, context);
				return dte.semantic(sc, context);
			}
			imp = s.isImport();
			if (imp != null) {
				if (imp.pkg == null)
				{   
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForwardReferenceOfImport, this, imp.toChars(context)));
					}
				    return this;
				}
				
				ScopeExp ie = new ScopeExp(filename, lineNumber, imp.pkg);
				ie.copySourceRange(this);
				return ie.semantic(sc, context);
			}
			pkg = s.isPackage();
			if (pkg != null) {
				ScopeExp ie;

				ie = new ScopeExp(filename, lineNumber, pkg);
				ie.copySourceRange(this);
				return ie.semantic(sc, context);
			}
			Module mod = s.isModule();
			if (mod != null) {
				ScopeExp ie;

				ie = new ScopeExp(filename, lineNumber, mod);
				return ie.semantic(sc, context);
			}

			t = s.getType(context);
			if (t != null) {
				return new TypeExp(filename, lineNumber, t);
			}

			TupleDeclaration tup = s.isTupleDeclaration();
			if (tup != null) {
				Expressions exps = new Expressions(tup.objects
						.size());
				for (int i = 0; i < tup.objects.size(); i++) {
					ASTDmdNode o2 = tup.objects.get(i);
					if (o2.dyncast() != DYNCAST.DYNCAST_EXPRESSION) {
						if (context.acceptsWarnings()) {
							context.acceptProblem(Problem.newSemanticTypeWarning(IProblem.SymbolNotAnExpression, 0, o2.getStart(), o2.getLength(), o2.toChars(context)));
						}
					} else {
						Expression e2 = (Expression) o2;
						e2 = e2.syntaxCopy(context);
						exps.add(e2);
					}
				}
				e = new TupleExp(filename, lineNumber, exps);
				e = e.semantic(sc, context);
				return e;
			}

			TemplateInstance ti = s.isTemplateInstance();
			if (ti != null && context.global.errors == 0) {
				if (0 == ti.semanticdone) {
					ti.semantic(sc, context);
				}
				s = ti.inst.toAlias(context);
				if (s.isTemplateInstance() == null) {
					// goto Lagain;
					loop = true;
					continue Lagain;
				}
				e = new ScopeExp(filename, lineNumber, ti);
				e = e.semantic(sc, context);
				return e;
			}

			TemplateDeclaration td = s.isTemplateDeclaration();
			if (td != null) {
				e = new TemplateExp(filename, lineNumber, td);
				e = e.semantic(sc, context);
				return e;
			}

		}

		// Lerr:
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolIsNotAVariable, s, s.kind(), s.toChars(context)));
		}
		type = Type.terror;
		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (s == null) return;
		
		buf.writestring(s.toChars(context));
	}

	@Override
	public String toChars(SemanticContext context) {
		if (s == null) return "";
		
		return s.toChars(context);
	}

	@Override
	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
		return this;
	}

}
