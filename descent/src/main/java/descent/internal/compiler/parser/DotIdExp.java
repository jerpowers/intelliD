package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKdotexp;
import static descent.internal.compiler.parser.TOK.TOKdottd;
import static descent.internal.compiler.parser.TOK.TOKimport;
import static descent.internal.compiler.parser.TOK.TOKsuper;
import static descent.internal.compiler.parser.TOK.TOKthis;
import static descent.internal.compiler.parser.TOK.TOKtuple;
import static descent.internal.compiler.parser.TY.Taarray;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tpointer;
import static descent.internal.compiler.parser.TY.Tsarray;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class DotIdExp extends UnaExp {

	public IdentifierExp ident;
	
	public DotIdExp(char[] filename, int lineNumber, Expression e, char[] id) {
		this(filename, lineNumber, e, new IdentifierExp(id));
	}

	public DotIdExp(char[] filename, int lineNumber, Expression e, IdentifierExp id) {
		super(filename, lineNumber, TOK.TOKdot, e);
		this.ident = id;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceE1);
			TreeVisitor.acceptChildren(visitor, ident);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int getNodeType() {
		return DOT_ID_EXP;
	}
	
	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression e = semantic0(sc, context);
		
		// Descent: for binding resolution
		context.setResolvedExp(this, e);
		if (ident != null) {
			ident.setResolvedExpression(e, context);
		}
		
		return e;
	}
	
	public Expression semantic0(Scope sc, SemanticContext context) {
		Expression e;
		Expression eleft;
		Expression eright;

		/* Special case: rewrite this.id and super.id
		 * to be classtype.id and baseclasstype.id
		 * if we have no this pointer.
		 */
		if ((e1.op == TOKthis || e1.op == TOKsuper) && hasThis(sc) == null) {
			ClassDeclaration cd;
			StructDeclaration sd;
			AggregateDeclaration ad;

			ad = sc.getStructClassScope();
			if (ad != null) {
				cd = ad.isClassDeclaration();
				if (cd != null) {
					if (e1.op == TOKthis) {
						e = context.newTypeDotIdExp(filename, lineNumber, cd.type, ident);
						return e.semantic(sc, context);
					} else if (cd.baseClass != null && e1.op == TOKsuper) {
						e = context.newTypeDotIdExp(filename, lineNumber, cd.baseClass.type, ident);
						return e.semantic(sc, context);
					}
				} else {
					sd = ad.isStructDeclaration();
					if (sd != null) {
						if (e1.op == TOKthis) {
							e = context.newTypeDotIdExp(filename, lineNumber, sd.type, ident);
							return e.semantic(sc, context);
						}
					}
				}
			}
		}

		super.semantic(sc, context);
		
		// TODO: see where to reassign source range to the member
		// e1.start = ident.start;
		// e1.length = ident.length;

		if (e1.op == TOKdotexp) {
			DotExp de = (DotExp) e1;
			eleft = de.e1;
			eright = de.e2;
		} else {
			e1 = resolveProperties(sc, e1, context);
			eleft = null;
			eright = e1;
		}
		
		if (context.isD2()) {
			if (e1.op == TOKtuple && equals(ident, Id.offsetof)) {
				/*
				 * 'distribute' the .offsetof to each of the tuple elements.
				 */
				TupleExp te = (TupleExp) e1;
				Expressions exps = new Expressions(size(te.exps));
				exps.setDim(size(te.exps));
				for (int i = 0; i < size(exps); i++) {
					Expression e2 = (Expression) te.exps.get(i);
					e2 = e2.semantic(sc, context);
					e2 = new DotIdExp(e2.filename, e2.lineNumber, e2, Id.offsetof);
					exps.set(i, e2);
				}
				e = new TupleExp(filename, lineNumber, exps);
				e = e.semantic(sc, context);
				return e;
			}
		}

		if (e1.op == TOKtuple && !equals(ident, Id.length)) {
			TupleExp te = (TupleExp) e1;
			e = new IntegerExp(filename, lineNumber, te.exps.size(), Type.tsize_t);
			return e;
		}
		
	    if (e1.op == TOKdottd) {
	    	if (context.acceptsErrors()) {
	    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.TemplateDoesNotHaveProperty, this, e1.toChars(context), ident.toChars()));
	    	}
			return e1;
		}

		if (null == e1.type) {
			if (context.acceptsErrors()) {
	    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.ExpressionDoesNotHaveProperty, this, e1.toChars(context), ident.toChars()));
	    	}
			return e1;
		}
		
		Type t1b = context.isD2() ? e1.type.toBasetype(context) : null;

		if (eright.op == TOKimport) // also used for template alias's
		{
			ScopeExp ie = (ScopeExp) eright;
			
			// Descent: if it's null, problems were reported
			if (ie.sds == null) {
				return this;
			}
			
			/* Disable access to another module's private imports.
			 * The check for 'is sds our current module' is because
			 * the current module should have access to its own imports.
			 */
			Dsymbol s = ie.sds.search(filename, lineNumber, ident,
			    (ie.sds.isModule() != null && ie.sds != sc.module) ? 1 : 0, context);
			
			// Descent: for binding resolution
			context.setResolvedSymbol(ident, s);
			
			if (s != null) {
				s = s.toAlias(context);
				checkDeprecated(sc, s, context);

				EnumMember em = s.isEnumMember();
				if (em != null) {
					e = em.value();
					e = e.semantic(sc, context);
					return e;
				}

				VarDeclaration v = s.isVarDeclaration();
				if (v != null) {
					if (v.inuse() != 0) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.CircularReferenceTo, this, v.toChars(context)));
						}
						type = Type.tint32;
						return this;
					}
					type = v.type;
					
					if (context.isD1()) {
						if (v.isConst()) {
							if (v.init() != null) {
								ExpInitializer ei = v.init().isExpInitializer();
								if (ei != null) {
									if (same(ei.exp.type, type, context)) {
										e = ei.exp.copy(); // make copy so we can change loc
										e.filename = filename;
										e.lineNumber = lineNumber;
										return e;
									}
								}
							} else if (type.isscalar(context)) {
								e = type.defaultInit(context);
								e.filename = filename;
								e.lineNumber = lineNumber;
								return e;
							}
						}
					}
					if (v.needThis()) {
						if (eleft == null) {
							eleft = new ThisExp(filename, lineNumber);
						}
						e = new DotVarExp(filename, lineNumber, eleft, v);
						e = e.semantic(sc, context);
					} else {
						e = new VarExp(filename, lineNumber, v);
						if (eleft != null) {
							e = new CommaExp(filename, lineNumber, eleft, e);
							e.type = v.type;
						}
					}
					return e.deref();
				}

				FuncDeclaration f = s.isFuncDeclaration();
				if (f != null) {
					if (f.needThis()) {
						if (eleft == null) {
							eleft = new ThisExp(filename, lineNumber);
						}
						if (context.isD2()) {
							e = new DotVarExp(filename, lineNumber, eleft, f, true);
						} else {
							e = new DotVarExp(filename, lineNumber, eleft, f);
						}
						e = e.semantic(sc, context);
					} else {
						e = new VarExp(filename, lineNumber, f);
						if (eleft != null) {
							e = new CommaExp(filename, lineNumber, eleft, e);
							e.type = f.type;
						}
					}
					return e;
				}
				
				if (context.isD2()) {
					OverloadSet o = s.isOverloadSet();
					if (o != null) {
						return new OverExp(o);
					}
				}

				Type t = s.getType(context);
				if (t != null) {
					return new TypeExp(filename, lineNumber, t);
				}
				
			    TupleDeclaration tup = s.isTupleDeclaration();
				if (tup != null) {
					if (eleft != null) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.CannotHaveEDotTuple, this));
						}
					}
					e = new TupleExp(filename, lineNumber, tup, context);
					e = e.semantic(sc, context);
					return e;
				}

				ScopeDsymbol sds = s.isScopeDsymbol();
				if (sds != null) {
					e = new ScopeExp(filename, lineNumber, sds);
					e.copySourceRange(this);
					e = e.semantic(sc, context);
					if (eleft != null) {
						e = new DotExp(filename, lineNumber, eleft, e);
					}
					return e;
				}

				Import imp = s.isImport();
				if (imp != null) {
					ScopeExp ie2;

					ie2 = new ScopeExp(filename, lineNumber, imp.pkg);
					return ie2.semantic(sc, context);
				}

				throw new IllegalStateException("assert(0);");
			}
			else if (equals(ident, Id.stringof))
			{   String s2 = ie.toChars(context);
			    e = new StringExp(filename, lineNumber, s2.toCharArray(), 'c');
			    e = e.semantic(sc, context);
			    return e;
			}

			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.UndefinedIdentifier, this, toChars(context)));
			}
			type = Type.tvoid;
			return this;
		} else {
			// Added for Descent
			if (e1.type == null && context.global.errors > 0) {
				return this;
			}
			
			if (e1.type.ty == Tpointer && !equals(ident, Id.init)
					&& !equals(ident, Id.__sizeof) && !equals(ident, Id.alignof)
					&& !equals(ident, Id.offsetof) && !equals(ident, Id.mangleof)
					&& !equals(ident, Id.stringof)) {
				e = new PtrExp(filename, lineNumber, e1);
				e.type = ((TypePointer) e1.type).next;
				return e.type.dotExp(sc, e, ident, context);
			}     
			else if (context.isD2() && (t1b.ty == Tarray ||
		             t1b.ty == Tsarray ||
		    	     t1b.ty == Taarray))
		        {	
				/* If ident is not a valid property, rewrite:
		    	 *   e1.ident
	             * as:
	             *   .ident(e1)
	             */
		    	int errors = context.global.errors;
		    	context.global.gag++;
		    	e = e1.type.dotExp(sc, e1, ident, context);
		    	context.global.gag--;
		    	if (errors != context.global.errors)	// if failed to find the property
		    	{
		    		context.global.errors = errors;
		    	    e = new DotIdExp(filename, lineNumber, new IdentifierExp(filename, lineNumber, Id.empty), ident);
		    	    e = new CallExp(filename, lineNumber, e, e1);
		    	}
		    	e = e.semantic(sc, context);
		    	return e;
			} else {
				// Ident may be null if completing (Foo).|
				if (ident == null) {
					return e1;
				}
				e = e1.type.dotExp(sc, e1, ident, context);
				e = e.semantic(sc, context);
				return e;
			}
		}
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
		expToCBuffer(buf, hgs, e1, PREC.PREC_primary, context);
	    buf.writeByte('.');
	    buf.writestring(ident.toChars());
	}
	
	@Override
	public void setResolvedSymbol(Dsymbol symbol, SemanticContext context) {
		ident.setResolvedSymbol(symbol, context);
	}
	
	@Override
	public void setEvaluatedExpression(Expression exp, SemanticContext context) {
		ident.setEvaluatedExpression(exp, context);
	}
	
	@Override
	public void setResolvedExpression(Expression exp, SemanticContext context) {
		ident.setResolvedExpression(exp, context);
	}

}
