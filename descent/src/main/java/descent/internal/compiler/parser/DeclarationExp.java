package descent.internal.compiler.parser;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class DeclarationExp extends Expression {

	public Dsymbol declaration, sourceDeclaration;

	public DeclarationExp(char[] filename, int lineNumber, Dsymbol declaration) {
		super(filename, lineNumber, TOK.TOKdeclaration);
		this.declaration = declaration;
		this.sourceDeclaration = declaration;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
	
	@Override
	public boolean canThrow(SemanticContext context) {
		if (context.isD2()) {
			VarDeclaration v = declaration.isVarDeclaration();
			if (v != null && v.init != null) {
				ExpInitializer ie = v.init.isExpInitializer();
				return ie != null && ie.exp.canThrow(context);
			}
			return false;
		} else {
			return super.canThrow(context);
		}
	}

	@Override
	public int checkSideEffect(int flag, SemanticContext context) {
		return 1;
	}

	@Override
	public int getNodeType() {
		return DECLARATION_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		Expression e = EXP_CANT_INTERPRET;
		VarDeclaration v = declaration.isVarDeclaration();
		if (v != null) {
			Dsymbol s = v.toAlias(context);
			if (s == v && !v.isStatic() && v.init() != null) {
				ExpInitializer ie = v.init().isExpInitializer();
				if (ie != null) {
					e = ie.exp.interpret(istate, context);
				} else if (v.init().isVoidInitializer() != null) {
					e = null;
				}
			} else { 
				boolean condition;
				if (context.isD2()) {
					condition = s == v && (v.isConst() || v.isInvariant(context)) && v.init != null;
				} else {
					condition = s == v && v.isConst() && v.init() != null;
				}
				
				if (condition) {
					e = v.init().toExpression(context);
					if (null == e) {
						e = EXP_CANT_INTERPRET;
					} else if (null == e.type) {
						e.type = v.type;
					}
				} else if (declaration.isAttribDeclaration() != null
						|| declaration.isTemplateMixin() != null
						|| declaration.isTupleDeclaration() != null) { // These can be made to work, too lazy now
					e = EXP_CANT_INTERPRET;
				} else { // Others should not contain executable code, so are trivial to evaluate
					e = null;
				}
			}
		}
		return e;
	}

	@Override
	public void scanForNestedRef(Scope sc, SemanticContext context) {
		declaration.parent = sc.parent;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		if (type != null) {
			return this;
		}

		/* This is here to support extern(linkage) declaration,
		 * where the extern(linkage) winds up being an AttribDeclaration
		 * wrapper.
		 */
		Dsymbol s = declaration;

		AttribDeclaration ad = declaration.isAttribDeclaration();
		if (ad != null) {
			if (ad.decl != null && ad.decl.size() == 1) {
				s = ad.decl.get(0);
			}
		}

		if (s.isVarDeclaration() != null) { // Do semantic() on initializer first, so:
			//	int a = a;
			// will be illegal.
			declaration.semantic(sc, context);
			s.parent = sc.parent;
		}

		// Insert into both local scope and function scope.
		// Must be unique in both.
		if (s.ident != null) {
			if (sc.insert(s) == null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.DeclarationIsAlreadyDefined, s, s.toChars(context)));
				}
			} else if (sc.func != null) {
				//VarDeclaration v = s.isVarDeclaration();
				if ((s.isFuncDeclaration() != null /*|| v && v.storage_class & STCstatic*/)
						&& sc.func.localsymtab.insert(s) == null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.DeclarationIsAlreadyDefinedInAnotherScope, this, s.toPrettyChars(context), sc.func.toChars(context)));
					}
				} else if (!context.global.params.useDeprecated) { // Disallow shadowing

					for (Scope scx = sc.enclosing; scx != null
							&& scx.func == sc.func; scx = scx.enclosing) {
						Dsymbol s2;

						if (scx.scopesym != null
								&& scx.scopesym.symtab != null
								&& (s2 = scx.scopesym.symtab.lookup(s.ident)) != null
								&& s != s2) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeErrorLoc(IProblem.ShadowingDeclarationIsDeprecated, s, s.toPrettyChars(context)));
							}
						}
					}
				}
			}
		}
		if (s.isVarDeclaration() == null) {
			declaration.semantic(sc, context);
			s.parent = sc.parent;
		}
		// Commented this for Descent: we want semantic even if there are errors
//		if (context.global.errors == 0) {
			declaration.semantic2(sc, context);
//			if (context.global.errors == 0) {
				declaration.semantic3(sc, context);
//			}
//		}

		type = Type.tvoid;
		return this;
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		return new DeclarationExp(filename, lineNumber, declaration.syntaxCopy(null, context));
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		declaration.toCBuffer(buf, hgs, context);
	}
	
	@Override
	public String toString() {
		return declaration.toString();
	}

}
