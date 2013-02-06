package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.STC.STCmanifest;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TemplateAliasParameter extends TemplateParameter {

	public ASTDmdNode specAlias, sourceSpecAlias;
	public ASTDmdNode defaultAlias, sourceDefaultAlias;
	public Type specType, sourceSpecType;
	
	public TemplateAliasParameter(char[] filename, int lineNumber, IdentifierExp ident,
			Type specType, ASTDmdNode specAlias, ASTDmdNode defaultAlias) {
		super(filename, lineNumber, ident);
		this.specType = this.sourceSpecType = specType;
		this.specAlias = this.sourceSpecAlias = specAlias;
		this.defaultAlias = this.sourceDefaultAlias = defaultAlias;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, sourceSpecAlias);
			TreeVisitor.acceptChildren(visitor, sourceDefaultAlias);
		}
		visitor.endVisit(this);
	}

	@Override
	public void declareParameter(Scope sc, SemanticContext context) {
		TypeIdentifier ti = new TypeIdentifier(filename, lineNumber, ident);
		sparam = new AliasDeclaration(filename, lineNumber, ident, ti);
		
		// Descent
		((AliasDeclaration) sparam).isTemplateParameter = true;
		
		if (sc.insert(sparam) == null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.ParameterMultiplyDefined, ident, new String[] { new String(ident.ident) }));
			}
		}
	}

	@Override
	public ASTDmdNode defaultArg(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		if (context.isD1()) {
			Dsymbol s = null;
			if (defaultAlias != null) {
				s = ((Type)defaultAlias).toDsymbol(sc, context);
				if (null == s) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.SymbolIsNotASymbol, this, new String[] { defaultAlias.toChars(context) }));
					}
				}
			}
			return s;
		} else {
		    ASTDmdNode o = aliasParameterSemantic(filename, lineNumber, sc, defaultAlias, context);
		    return o;
		}
	}

	@Override
	public ASTDmdNode dummyArg(SemanticContext context) {
		ASTDmdNode s;

		s = specAlias;
		if (null == s) {
			if (null == context.TemplateAliasParameter_sdummy) {
				context.TemplateAliasParameter_sdummy = new Dsymbol();
			}
			s = context.TemplateAliasParameter_sdummy;
		}
		return s;
	}

	@Override
	public int getNodeType() {
		return TEMPLATE_ALIAS_PARAMETER;
	}

	@Override
	public TemplateAliasParameter isTemplateAliasParameter() {
		return this;
	}

	@Override
	public MATCH matchArg(Scope sc, Objects tiargs, int i,
			TemplateParameters parameters, Objects dedtypes,
			Declaration[] psparam, int flags, SemanticContext context) {
		ASTDmdNode sa;
		ASTDmdNode oarg;
		Expression ea = null;

		if (i < size(tiargs)) {
			oarg = tiargs.get(i);
		} else { // Get default argument instead
			oarg = defaultArg(filename, lineNumber, sc, context);
			if (oarg == null) {
				if (i >= size(dedtypes)) {
					throw new IllegalStateException("assert(i < dedtypes.dim);");
				}
				// It might have already been deduced
				oarg = dedtypes.get(i);
				if (null == oarg) {
					// goto Lnomatch;
					psparam[0] = null;
					return MATCHnomatch;
				}
			}
		}

		sa = getDsymbol(oarg, context);
		
		if (context.isD1()) {
			if (null == sa) {
				// goto Lnomatch;
				psparam[0] = null;
				return MATCHnomatch;
			}
		} else {
			if (sa != null) {
				/*
				 * specType means the alias must be a declaration with a type
				 * that matches specType.
				 */
				if (specType != null) {
					Declaration d = ((Dsymbol) sa).isDeclaration();
					if (null == d) {
						// goto Lnomatch;
						psparam[0] = null;
						return MATCHnomatch;
					}
					if (!d.type.equals(specType)) {
						// goto Lnomatch;
						psparam[0] = null;
						return MATCHnomatch;
					}
				}
			} else {
				sa = oarg;
				ea = isExpression(oarg);
				if (ea != null) {
					if (specType != null) {
						if (!ea.type.equals(specType)) {
							// goto Lnomatch;
							psparam[0] = null;
							return MATCHnomatch;
						}
					}
				} else {
					// goto Lnomatch;
					psparam[0] = null;
					return MATCHnomatch;
				}
			}
		}

		if (specAlias != null) {
			if ((context.isD1() && null == sa) || sa == context.TemplateAliasParameter_sdummy) {
				// goto Lnomatch;
				psparam[0] = null;
				return MATCHnomatch;
			}
			if (sa != specAlias) {
				// goto Lnomatch;
				psparam[0] = null;
				return MATCHnomatch;
			}
		} else if (dedtypes.get(i) != null) {
			// Must match already deduced symbol
			ASTDmdNode s = dedtypes.get(i);

			if (null == sa || s != sa) {
				// goto Lnomatch;
				psparam[0] = null;
				return MATCHnomatch;
			}
		}
		dedtypes.set(i, sa);

		if (context.isD1()) {
			psparam[0] = new AliasDeclaration(filename, lineNumber, ident, (Dsymbol) sa);
			// Descent
			((AliasDeclaration) psparam[0]).isTemplateParameter = true;
		} else {
			Dsymbol s = isDsymbol(sa);
			if (s != null) {
				psparam[0] = new AliasDeclaration(filename, lineNumber, ident, s);
				// Descent
				((AliasDeclaration) psparam[0]).isTemplateParameter = true;
			} else {
				// Declare manifest constant
				Initializer init = new ExpInitializer(filename, lineNumber, ea);
				VarDeclaration v = new VarDeclaration(filename, lineNumber, null, ident, init);
				v.storage_class = STCmanifest;
				v.semantic(sc, context);
				psparam[0] = v;
			}
			return MATCHexact;
		}
		
		return MATCHexact;
	}

	@Override
	public int overloadMatch(TemplateParameter tp) {
		TemplateAliasParameter tap = tp.isTemplateAliasParameter();

		if (tap != null) {
			if (specAlias != tap.specAlias) {
				return 0;
			}

			return 1; // match
		}
		return 0;
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		if (context.isD1()) {
			if (specAlias != null) {
				specAlias = ((Type) specAlias).toDsymbol(sc, context);
				if (specAlias == null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.SymbolNotFound, specAlias,
								new String[] { specAlias.toString() }));
					}
				}
			}
		} else {
			if (specType != null) {
				specType = specType.semantic(filename, lineNumber, sc, context);
			}
			specAlias = aliasParameterSemantic(filename, lineNumber, sc, specAlias, context);
		}
	}

	@Override
	public ASTDmdNode specialization() {
		return specAlias;
	}

	@Override
	public TemplateParameter syntaxCopy(SemanticContext context) {
		TemplateAliasParameter tp = new TemplateAliasParameter(filename, lineNumber, ident,
				specType, specAlias, defaultAlias);
		if (tp.specType != null) {
			tp.specType = specType.syntaxCopy(context);
		}
		if (tp.specAlias != null) {
			tp.specAlias = objectSyntaxCopy(specAlias, context);
		}
		if (tp.defaultAlias != null) {
			tp.defaultAlias = objectSyntaxCopy(defaultAlias, context);
		}
		return tp;
	}
	
	public ASTDmdNode aliasParameterSemantic(char[] filename, int lineNumber, Scope sc, ASTDmdNode o, SemanticContext context) {
		if (o != null) {
			Expression ea = isExpression(o);
			Type ta = isType(o);
			if (ta != null) {
				Dsymbol s = ta.toDsymbol(sc, context);
				if (s != null)
					o = s;
				else
					o = ta.semantic(filename, lineNumber, sc, context);
			} else if (ea != null) {
				ea = ea.semantic(sc, context);
				o = ea.optimize(WANTvalue | WANTinterpret, context);
			}
		}
		return o;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (context.isD1()) {
			buf.writestring("alias ");
			buf.writestring(ident.toChars());
			if (specAlias != null) {
				buf.writestring(" : ");
				((Type)specAlias).toCBuffer(buf, null, hgs, context);
			}
			if (defaultAlias != null) {
				buf.writestring(" = ");
				((Type)defaultAlias).toCBuffer(buf, null, hgs, context);
			
			}
		} else {
			buf.writestring("alias ");
			if (specType != null) {
				HdrGenState hgs2 = new HdrGenState();
				specType.toCBuffer(buf, ident, hgs2, context);
			} else
				buf.writestring(ident.toChars());
			if (specAlias != null) {
				buf.writestring(" : ");
				ObjectToCBuffer(buf, hgs, specAlias, context);
			}
			if (defaultAlias != null) {
				buf.writestring(" = ");
				ObjectToCBuffer(buf, hgs, defaultAlias, context);
			}
		}
	}
	
	@Override
	public void appendSignature(StringBuilder sb, int options) {
		sb.append(Signature.C_TEMPLATE_ALIAS_PARAMETER);
		if (specAlias != null) {
			sb.append(Signature.C_TEMPLATE_ALIAS_PARAMETER_SPECIFIC_TYPE);
			if (specAlias instanceof Type) {
				((Type)specAlias).appendSignature(sb, options);
			} else {
				// SEMANTIC signature
			}
		}
	}
	
	@Override
	public char[] getDefaultValue() {
		if (defaultAlias == null) {
			return null;
		}
		if (defaultAlias instanceof Type) {
			return ((Type)defaultAlias).getSignature().toCharArray();
		} else {
			//SEMANTIC signature
			return null;
		}
	}

}
