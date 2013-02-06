package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.DYNCAST.DYNCAST_IDENTIFIER;
import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.STC.STCref;
import static descent.internal.compiler.parser.TOK.TOKdotexp;
import static descent.internal.compiler.parser.TOK.TOKimport;
import static descent.internal.compiler.parser.TOK.TOKtype;
import static descent.internal.compiler.parser.TY.Tinstance;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tstruct;
import descent.core.IJavaElement__Marker;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TypeStruct extends Type {

	public StructDeclaration sym;

	public TypeStruct(StructDeclaration sym) {
		super(TY.Tstruct);
		this.sym = sym;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int alignsize(SemanticContext context) {
		int sz;

		sym.size(context); // give error for forward references
		sz = sym.alignsize;
		if (sz > sym.structalign)
			sz = sym.structalign;
		return sz;
	}

	@Override
	public boolean checkBoolean(SemanticContext context) {
		return false;
	}
	
	@Override
	public MATCH constConv(Type to, SemanticContext context) {
		if (equals(to))
			return MATCHexact;
		if (ty == to.ty && sym == ((TypeStruct) to).sym && to.mod == MODconst)
			return MATCHconst;
		return MATCHnomatch;
	}

	@Override
	public MATCH deduceType(Scope sc, Type tparam,
			TemplateParameters parameters, Objects dedtypes,
			SemanticContext context) {

		/* If this struct is a template struct, and we're matching
		 * it against a template instance, convert the struct type
		 * to a template instance, too, and try again.
		 */
		TemplateInstance ti = sym.parent.isTemplateInstance();

		if (null != tparam && tparam.ty == Tinstance) {
			if (null != ti && ti.toAlias(context) == sym) {
				TypeInstance t = new TypeInstance(null, 0, ti);
				return t.deduceType(sc, tparam, parameters, dedtypes, context);
			}

			/* Match things like:
			 *  S!(T).foo
			 */
			TypeInstance tpi = (TypeInstance) tparam;
			if (tpi.idents.size() > 0) {
				IdentifierExp id = (IdentifierExp) tpi.idents.get(tpi.idents
						.size() - 1);
				if (id.dyncast() == DYNCAST_IDENTIFIER && equals(sym.ident, id)) {
					Type tparent = sym.parent.getType(context);
					if (null != tparent) {
						/* Slice off the .foo in S!(T).foo
						 */
						/* TODO semantic
						 tpi.idents.dim--;
						 MATCH m = tparent.deduceType(sc, tpi, parameters, dedtypes);
						 tpi.idents.dim++;
						 return m;
						 */
						return MATCHnomatch;
					}
				}
			}
		}

		// Extra check
		if (null != tparam && tparam.ty == Tstruct) {
			TypeStruct tp = (TypeStruct) tparam;

			if (sym != tp.sym)
				return MATCHnomatch;
		}
		return super.deduceType(sc, tparam, parameters, dedtypes, context);
	}

	@Override
	public Expression defaultInit(char[] filename, int lineNumber, SemanticContext context) {
		 Symbol s;
		 Declaration d;

		 s = sym.toInitializer();
		 d = new SymbolDeclaration(sym.filename, sym.lineNumber, s, sym);
		 d.type = this;
		 return new VarExp(sym.filename, sym.lineNumber, d);
	}

	@Override
	public Expression dotExp(Scope sc, Expression e, IdentifierExp ident,
			SemanticContext context) {
		// int offset;

		Expression b;
		VarDeclaration v = null;
		Dsymbol s;
		DotVarExp de;
		Declaration d;

		if (null == sym.members) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.StructIsForwardReferenced, this, new String[] { sym.toChars(context) }));
			}
			if (context.isD1()) {
				return new IntegerExp(e.filename, e.lineNumber,  0, Type.tint32);
			} else {
				return new ErrorExp();
			}
		}

		if (equals(ident, Id.tupleof)) {
			/* Create a TupleExp
			 */
			e = e.semantic(sc, context);	// do this before turning on noaccesscheck
			
			// Added for Descent
			sym = (StructDeclaration) sym.unlazy(context);
			
			Expressions exps = new Expressions(sym.fields.size());
			for (VarDeclaration v_ : sym.fields) {
				Expression fe = new DotVarExp(e.filename, e.lineNumber,  e, v_);
				exps.add(fe);
			}
			e = new TupleExp(e.filename, e.lineNumber,  exps);
			sc = sc.push();
			sc.noaccesscheck = 1;
			e = e.semantic(sc, context);
			sc.pop();
			return e;
		}

		if (e.op == TOKdotexp) {
			DotExp de_ = (DotExp) e;

			if (de_.e1.op == TOKimport) {
			    throw new IllegalStateException();	// cannot find a case where this happens; leave assert in until we do
				
//				ScopeExp se = (ScopeExp) de_.e1;
//
//				s = se.sds.search(e.filename, e.lineNumber,  ident, 0, context);
//				e = de_.e1;
				//goto L1;
			} else {
				s = sym.search(e.filename, e.lineNumber,  ident, 0, context);
			}
		} else {
			// Ident may be null if completing (Foo*).|
			if (ident == null) {
				return e;
			}
			
			s = sym.search(e.filename, e.lineNumber,  ident, 0, context);
		}
		
		// Descent: for binding resolution
		ident.setResolvedSymbol(s, context);

		boolean continueInL1 = true;
	// L1:
		while(continueInL1) {
			continueInL1 = false;
			if (null == s) {
				if (context.isD2()) {
					if (!equals(ident, Id.__sizeof) && !equals(ident, Id.alignof) && !equals(ident, Id.init) && !equals(ident, Id.mangleof)
							&& !equals(ident, Id.stringof) && !equals(ident, Id.offsetof)) {
						/*
						 * See if we should forward to the alias this.
						 */
						if (sym.aliasthis != null) { /*
													 * Rewrite e.ident as:
													 * e.aliasthis.ident
													 */
							e = new DotIdExp(e.filename, e.lineNumber, e, sym.aliasthis.ident);
							e = new DotIdExp(e.filename, e.lineNumber, e, ident);
							return e.semantic(sc, context);
						}

						/*
						 * Look for overloaded opDot() to see if we should
						 * forward request to it.
						 */
						Dsymbol fd = search_function(sym, Id.opDot, context);
						if (fd != null) { 
							/*
							 * Rewrite e.ident as: e.opId().ident
							 */
							e = build_overload(e.filename, e.lineNumber, sc, e, null, fd.ident, context);
							e = new DotIdExp(e.filename, e.lineNumber, e, ident);
							return e.semantic(sc, context);
						}
					}
				}
				return super.dotExp(sc, e, ident, context);
			}
	
			if (null == s.isFuncDeclaration()) {	// because of overloading
				s.checkDeprecated(sc, context, this); // TODO check this for reference
			}
			s = s.toAlias(context);
	
			v = s.isVarDeclaration();
			
			if (context.isD1()) {
				if (null != v && v.isConst() && v.type.toBasetype(context).ty != Tsarray) {
					ExpInitializer ei = v.getExpInitializer(context);
					if (null != ei) {
						e = ei.exp.copy(); // need to copy it if it's a StringExp
						e = e.semantic(sc, context);
						return e;
					}
				}
			} else {
				if (v != null && !v.isDataseg(context)) {
					Expression ei = v.getConstInitializer(context);
					if (null != ei) {
						e = ei.copy(); // need to copy it if it's a StringExp
						e = e.semantic(sc, context);
						return e;
					}
				}
			}
	
			if (null != s.getType(context)) {
				//return new DotTypeExp(e.filename, e.lineNumber,  e, s);
				return new TypeExp(e.filename, e.lineNumber,  s.getType(context));
			}
	
			EnumMember em = s.isEnumMember();
			if (null != em) {
				assert (null != em.value());
				return em.value().copy();
			}
	
			TemplateMixin tm = s.isTemplateMixin();
			if (null != tm) {
				Expression de_ = new DotExp(e.filename, e.lineNumber,  e, new ScopeExp(e.filename, e.lineNumber,  tm));
				de_.type = e.type;
				return de_;
			}
	
			TemplateDeclaration td = s.isTemplateDeclaration();
			if (null != td) {
				e = new DotTemplateExp(e.filename, e.lineNumber,  e, td);
				e.semantic(sc, context);
				return e;
			}
			
		    TemplateInstance ti = s.isTemplateInstance();
			if (ti != null) {
				if (0 == ti.semanticdone)
					ti.semantic(sc, context);
				
				// Added for Descent
				if ((ti == null || ti.inst == null) && context.global.errors > 0) {
					return new IntegerExp(0);
				}
				
				s = ti.inst.toAlias(context);
				if (null == s.isTemplateInstance()) {
					// goto L1;
					continueInL1 = true;
					continue;
				}
				Expression de2 = new DotExp(e.filename, e.lineNumber,  e, new ScopeExp(e.filename, e.lineNumber,  ti));
				de2.type = e.type;
				return de2;
			}
		}
		
	    Import timp = s.isImport();
		if (timp != null) {
			e = new DsymbolExp(e.filename, e.lineNumber, s);
			e = e.semantic(sc, context);
			return e;
		}
		
		if (context.isD2()) {
			OverloadSet o = s.isOverloadSet();
			if (o != null) {
				/* We really should allow this, triggered by:
				 *   template c()
				 *   {
				 *	void a();
				 *	void b () { this.a(); }
				 *   }
				 *   struct S
				 *   {
				 *	mixin c;
				 *	mixin c;
				 *  }
				 *  alias S e;
				 */
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.OverloadSetNotAllowedInStructDeclaration, this, e.toChars(context), ident
							.toChars()));
				}
				return new ErrorExp();
			}
		}

		d = s.isDeclaration();
		
		if (d == null) {
			throw new IllegalStateException("assert(d);");
		}

		if (e.op == TOKtype) {
			FuncDeclaration fd = sc.func;

			if (context.isD1()) {
				if (d.needThis() && null != fd && null != fd.vthis) {
					e = new DotVarExp(e.filename, e.lineNumber,  new ThisExp(e.filename, e.lineNumber), d);
					e = e.semantic(sc, context);
					return e;
				}
			}
			if (null != d.isTupleDeclaration()) {
				e = new TupleExp(e.filename, e.lineNumber,  d.isTupleDeclaration(), context);
				e = e.semantic(sc, context);
				return e;
			}
			if (!context.isD1()) {
				if (d.needThis() && null != fd && null != fd.vthis) {
					e = new DotVarExp(e.filename, e.lineNumber,  new ThisExp(e.filename, e.lineNumber), d);
					e = e.semantic(sc, context);
					return e;
				}
			}
			return new VarExp(e.filename, e.lineNumber,  d);
		}

		if (d.isDataseg(context)) {
			// (e, d)
			VarExp ve;

			accessCheck(sc, e, d, context);
			ve = new VarExp(e.filename, e.lineNumber,  d);
			e = new CommaExp(e.filename, e.lineNumber,  e, ve);
			e.type = d.type;
			return e;
		}

		if (null != v) {
			if (v.toParent() != sym) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.SymbolIsNotAMember, this, new String[] { v.toChars(context) }));
				}
			}

			// *(&e + offset)
			accessCheck(sc, e, d, context);
//			b = new AddrExp(e.filename, e.lineNumber,  e);
//			b.type = e.type.pointerTo(context);
//			b = new AddExp(e.filename, e.lineNumber,  b, new IntegerExp(e.filename, e.lineNumber,  v.offset(),
//					Type.tint32));
//			b.type = v.type.pointerTo(context);
//			e = new PtrExp(e.filename, e.lineNumber,  b);
//			e.type = v.type;
//			return e;
		}

		de = new DotVarExp(e.filename, e.lineNumber,  e, d);
		return de.semantic(sc, context);
	}

	@Override
	public int getNodeType() {
		return TYPE_STRUCT;
	}

	@Override
	public TypeInfoDeclaration getTypeInfoDeclaration(SemanticContext context) {
		return new TypeInfoStructDeclaration(this, context);
	}

	@Override
	public boolean hasPointers(SemanticContext context) {
		StructDeclaration s = sym;

		sym.size(context); // give error for forward references
		if (context.isD1()) {
			if (null != s.members) {
				for (int i = 0; i < s.members.size(); i++) {
					Dsymbol sm = s.members.get(i);
					if (sm.hasPointers(context))
						return true;
				}
			}
		} else {
			for (int i = 0; i < size(s.fields); i++) {
				Dsymbol sm = (Dsymbol) s.fields.get(i);
				Declaration d = sm.isDeclaration();
				if ((d.storage_class & STCref) != 0 || d.hasPointers(context))
					return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isAssignable(SemanticContext context) {
		if (context.isD1()) {
			return super.isAssignable(context);
		} else {
			/*
			 * If any of the fields are const or invariant, then one cannot
			 * assign this struct.
			 */
			for (int i = 0; i < size(sym.fields); i++) {
				VarDeclaration v = (VarDeclaration) sym.fields.get(i);
				if (v.isConst() || v.isInvariant(context))
					return false;
			}
			return true;
		}
	}
	
	@Override
	public MATCH implicitConvTo(Type to, SemanticContext context) {
		if (context.isD1()) {
			return super.implicitConvTo(to, context);
		} else {
			MATCH m;

			if (ty == to.ty && sym == ((TypeStruct) to).sym) {
				m = MATCHexact; // exact match
				if (mod != to.mod) {
					if (to.mod == MODconst)
						m = MATCHconst;
					else {
						/*
						 * Check all the fields. If they can all be converted,
						 * allow the conversion.
						 */
						for (int i = 0; i < size(sym.fields); i++) {
							Dsymbol s = (Dsymbol) sym.fields.get(i);
							VarDeclaration v = s.isVarDeclaration();
							// assert(v && v.storage_class & STCfield);

							// 'from' type
							Type tvf = v.type.addMod(mod, context);

							// 'to' type
							Type tv = v.type.castMod(to.mod, context);

							// printf("\t%s => %s, match = %d\n",
							// v.type.toChars(), tv.toChars(),
							// tvf.implicitConvTo(tv));
							if (tvf.implicitConvTo(tv, context).ordinal() < MATCHconst.ordinal())
								return MATCHnomatch;
						}
						m = MATCHconst;
					}
				}
			} else if (sym.aliasthis != null) {
				m = MATCHnomatch;
				Declaration d = sym.aliasthis.isDeclaration();
				if (d != null) {
					// assert(d.type);
					Type t = d.type.addMod(mod, context);
					m = t.implicitConvTo(to, context);
				}
			} else
				m = MATCHnomatch; // no match
			return m;
		}
	}

	@Override
	public boolean isZeroInit(char[] filename, int lineNumber, SemanticContext context) {
		return sym.zeroInit;
	}

	@Override
	public int memalign(int salign, SemanticContext context) {
		sym.size(context); // give error for forward references
		return sym.structalign;
	}

	@Override
	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		return merge(context);
	}

	@Override
	public int size(char[] filename, int lineNumber, SemanticContext context) {
		return sym.size(context);
	}

	@Override
	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod, SemanticContext context) {
	    if (mod != this.mod) {
			toCBuffer3(buf, hgs, mod, context);
			return;
		}
		TemplateInstance ti = sym.parent.isTemplateInstance();
		if (ti != null && ti.toAlias(context) == sym) {
			buf.writestring(ti.toChars(context));
		} else {
			buf.writestring(sym.toChars(context));
		}
	}

	@Override
	public String toChars(SemanticContext context) {
		TemplateInstance ti = sym.parent.isTemplateInstance();
		if (context.isD2()) {
		    if (mod != 0)
		    	return super.toChars(context);
		}
		if (ti != null && ti.toAlias(context) == sym) {
			return ti.toChars(context);
		}
		return sym.toChars(context);
	}

	@Override
	public void toDecoBuffer(OutBuffer buf, int flag, SemanticContext context) {
		String name = sym.mangle(context);
		Type_toDecoBuffer(buf, flag, context);
		buf.printf(name);
	}

	@Override
	public Dsymbol toDsymbol(Scope sc, SemanticContext context) {
		return sym;
	}
	
	@Override
	public Type toHeadMutable(SemanticContext context) {
	    return this;
	}
	
	@Override
	public IJavaElement__Marker getJavaElement() {
		return sym.getJavaElement();
	}
	
	@Override
	protected void appendSignature0(StringBuilder sb, int options) {
		sym.appendSignature(sb, options);
	}
	
	@Override
	public char[] identRep() {
		return sym.ident == null ? null : sym.ident.ident;
	}
	
	//PERHAPS dt_t **toDt(dt_t **pdt);
	//PERHAPS type *toCtype();
}
