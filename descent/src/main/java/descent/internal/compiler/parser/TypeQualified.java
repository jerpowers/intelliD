package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.DYNCAST.DYNCAST_DSYMBOL;
import static descent.internal.compiler.parser.TY.Tinstance;
import static descent.internal.compiler.parser.TY.Ttuple;
import descent.core.compiler.IProblem;


public abstract class TypeQualified extends Type {

	public int lineNumber;
	public char[] filename;
	public Identifiers idents = new Identifiers(2);

	public TypeQualified(char[] filename, int lineNumber, TY ty) {
		super(ty);
		this.filename = filename;
		this.lineNumber = lineNumber;
	}

	public void addIdent(IdentifierExp ident) {
		idents.add(ident);
	}
	
	public void resolveHelper(char[] filename, int lineNumber, Scope sc, Dsymbol s,
			Dsymbol scopesym, Expression[] pe, Type[] pt, Dsymbol[] ps, SemanticContext context) {
		VarDeclaration v;
		EnumMember em;
//		TupleDeclaration td;

		Expression e = null;

		pe[0] = null;
		pt[0] = null;
		ps[0] = null;
		
		if (s != null) {
			// TODO check "this" for reference
			s.checkDeprecated(sc, context, this); // check for deprecated aliases
			s = s.toAlias(context);
			for (int i = 0; i < size(idents); i++) {
				IdentifierExp id = (IdentifierExp) idents.get(i);
				Dsymbol sm = s.searchX(filename, lineNumber, sc, id, context);
				
				// Descent: for binding resolution
				context.setResolvedSymbol(id, sm);
				
				boolean gotoL2 = false;
				boolean gotoL3 = false;

				if (null == sm) {
					Type t = null;

					v = s.isVarDeclaration();
					if (v != null && equals(id, Id.length)) {
						if (context.isD1()) {
							if (v.isConst() && v.getExpInitializer(context) != null) {
								e = v.getExpInitializer(context).exp;
							} else {
								e = new VarExp(filename, lineNumber, v);
							}
						} else {
							e = v.getConstInitializer(context);
							if (null == e) {
								e = new VarExp(filename, lineNumber, v);
							}
						}
						t = e.type;
						if (null == t) {
							// goto Lerror;
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(IProblem.IdentifierOfSymbolIsNotDefined, this, id.toChars(), toChars(context)));
							}
						}
						// goto L3;
						gotoL3 = true;
					}
					
					if (!gotoL3) {
						t = s.getType(context);
						if (null == t && s.isDeclaration() != null) {
							t = s.isDeclaration().type;
						}
					}
					if (gotoL3 || t != null) {
						if (!gotoL3) {
							sm = t.toDsymbol(sc, context);
							if (sm != null) {
								sm = sm.search(filename, lineNumber, id, 0, context);
								if (sm != null) {
									
									// Descent: for binding resolution
									context.setResolvedSymbol(id, sm);
									
									// goto L2;
									gotoL2 = true;
								}
							}
							if (!gotoL2) {
								// e = t.getProperty(loc, id);
								e = new TypeExp(filename, lineNumber, t);
								e = t.dotExp(sc, e, id, context);
								i++;
							}
						}
					// L3: 
						if (!gotoL2) {
							for (; i < size(idents); i++) {
								id = (IdentifierExp) idents.get(i);
								if (context.isD2() && equals(id, Id.offsetof)) {
									e = new DotIdExp(e.filename, e.lineNumber, e, id);
									e = e.semantic(sc, context);
								} else {
									e = e.type.dotExp(sc, e, id, context);
								}
							}
							pe[0] = e;
						}
					} else {
						// Lerror:
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.IdentifierOfSymbolIsNotDefined, this, id.toChars(), toChars(context)));
						}
					}
					if (!gotoL2) {
						return;
					}
				}
			// L2:
				s = sm.toAlias(context);
			}

			v = s.isVarDeclaration();
			if (v != null) {
				if (context.isD1()) {
					// It's not a type, it's an expression
					if (v.isConst() && v.getExpInitializer(context) != null) {
						ExpInitializer ei = v.getExpInitializer(context);
						// assert(ei);
						pe[0] = ei.exp.copy(); // make copy so we can change loc
						(pe[0]).filename = filename;
						(pe[0]).lineNumber = lineNumber;
						(pe[0]).copySourceRange(this);
					} else {
						pe[0] = new VarExp(filename, lineNumber, v);
					}
				} else {
					pe[0] = new VarExp(filename, lineNumber, v);
				}
				return;
			}
			em = s.isEnumMember();
			if (em != null) {
				// It's not a type, it's an expression
				pe[0] = em.value.copy();
				return;
			}

		// L1:
			boolean repeat = true;
			while(repeat) {
				repeat = false;
				
				Type t = s.getType(context);
				if (null == t) {
					// If the symbol is an import, try looking inside the import
					Import si;
	
					si = s.isImport();
					if (si != null) {
						s = si.search(filename, lineNumber, s.ident, 0, context);
						if (s != null && s != si) {
							// goto L1;
							repeat = true;
							continue;
						}
						s = si;
					}
					ps[0] = s;
					return;
				}
				if (t.ty == Tinstance && !same(t, this, context) && null == t.deco) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForwardReferenceToSymbol, this, t.toChars(context)));
					}
					return;
				}
	
				if (!same(t, this, context)) {
					if (t.reliesOnTident() != null) {
						Scope scx;
	
						for (scx = sc; true; scx = scx.enclosing) {
							if (null == scx) {
								if (context.acceptsErrors()) {
									context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForwardReferenceToSymbol, this, t.toChars(context)));
								}
								return;
							}
							if (scx.scopesym == scopesym)
								break;
						}
						t = t.semantic(filename, lineNumber, scx, context);
						// ((TypeIdentifier *)t).resolve(loc, scx, pe, &t, ps);
					}
				}
				if (t.ty == Ttuple) {
					pt[0] = t;
				} else {
					pt[0] = t.merge(context);
				}
			}
		}
		if (null == s) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.UndefinedIdentifier, this, toChars(context)));
			}
		}
	}

	public void resolveHelper_Lerror(IdentifierExp id, SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.IdentifierOfSymbolIsNotDefined, this, new String[] { id.toChars(), toChars(context) }));
		}
	}

	@Override
	public int size(char[] filename, int lineNumber, SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.SizeOfTypeIsNotKnown, this, new String[] { toChars(context) }));
		}
		return 1;
	}

	public void syntaxCopyHelper(TypeQualified t, SemanticContext context) {
		if (idents != null && t.idents != null) {
			idents.setDim(t.idents.size());
			for (int i = 0; i < idents.size(); i++) {
				IdentifierExp id = t.idents.get(i);
				if (id.dyncast() == DYNCAST_DSYMBOL) {
					TemplateInstance ti = ((TemplateInstanceWrapper) id).tempinst;
	
					ti = (TemplateInstance) ti.syntaxCopy(null, context);
					id = new TemplateInstanceWrapper(null, 0, ti);
				}
				idents.set(i, id);
			}
		}
	}

	@Override
	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod, SemanticContext context) {
		int i;

		for (i = 0; i < idents.size(); i++) {
			IdentifierExp id = idents.get(i);

			buf.writeByte('.');

			if (id.dyncast() == DYNCAST.DYNCAST_DSYMBOL) {
				TemplateInstanceWrapper ti = (TemplateInstanceWrapper) id;
				ti.tempinst.toCBuffer(buf, hgs, context);
			} else {
				buf.writestring(id.toChars());
			}
		}
	}

	public void toCBuffer2Helper(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
		int i;

		if (idents != null) {
			for (i = 0; i < idents.size(); i++) {
				IdentifierExp id = idents.get(i);
	
				buf.writeByte('.');
	
				if (id.dyncast() == DYNCAST_DSYMBOL) {
					TemplateInstance ti = ((TemplateInstanceWrapper) id).tempinst;
					ti.toCBuffer(buf, hgs, context);
				} else {
					buf.writestring(id.toChars());
				}
			}
		}
	}
	
	@Override
	public int getLineNumber() {
		return lineNumber;
	}
	
	@Override
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

}
