package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.PROT.PROTpublic;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.ASTNode;
import descent.internal.compiler.parser.ast.AstVisitorAdapter;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TemplateMixin extends TemplateInstance {

	public Identifiers idents;
	public Type tqual;
	public Scope scope;
	public IdentifierExp sourceIdent; // For Descent
	
	public int typeStart;
	public int typeLength;

	public TemplateMixin(char[] filename, int lineNumber, IdentifierExp ident, Type tqual,
			Identifiers idents, Objects tiargs, ASTNodeEncoder encoder) {
		super(filename, lineNumber, idents == null || idents.isEmpty() ? null : idents.get(idents.size() - 1), encoder);
		this.ident = ident;
		this.sourceIdent = ident;
		this.tqual = tqual;
		this.idents = idents;
		this.tiargs(tiargs != null ? tiargs : new Objects(0));
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, idents);
			TreeVisitor.acceptChildren(visitor, tiargs);
			TreeVisitor.acceptChildren(visitor, name);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return TEMPLATE_MIXIN;
	}

	@Override
	public boolean hasPointers(SemanticContext context) {
		for (int i = 0; i < members.size(); i++) {
			Dsymbol s = members.get(i);
			if (s.hasPointers(context)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TemplateMixin isTemplateMixin() {
		return this;
	}

	@Override
	public String kind() {
		return "mixin";
	}

	@Override
	public boolean oneMember(Dsymbol[] ps, SemanticContext context) {
		return super.oneMember(ps, context);
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		if (semanticdone != 0 &&
		// This for when a class/struct contains mixin members, and
				// is done over because of forward references
				(null == parent || null == toParent().isAggregateDeclaration())) {
			return;
		}
		if (0 == semanticdone) {
			semanticdone = 1;
		}

		Scope scx = null;
		if (scope != null) {
			sc = scope;
			scx = scope; // save so we don't make redundant copies
			scope = null;
		}

		// Follow qualifications to find the TemplateDeclaration
		if (null == tempdecl) {
			Dsymbol s;
			int i;
			IdentifierExp id;
			
			// Descent: recovery on "mixin"
			if (idents == null) {
				return;
			}

			if (tqual != null) {
				s = tqual.toDsymbol(sc, context);
				i = 0;
			} else {
				i = 1;
				id = idents.get(0);
				switch (id.dyncast()) {
				case DYNCAST_IDENTIFIER:
					s = sc.search(filename, lineNumber, id, null, context);
					break;

				case DYNCAST_DSYMBOL: {
					TemplateInstance ti = ((TemplateInstanceWrapper) id).tempinst;
					ti.semantic(sc, context);
					s = ti;
					break;
				}
				default:
					throw new IllegalStateException("assert(0);");
				}
			}

			for (; i < idents.size(); i++) {
				if (null == s) {
					break;
				}
				id = idents.get(i);
				s = s.searchX(filename, lineNumber, sc, id, context);
			}
			if (null == s) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolNotDefined, lineNumber, typeStart, typeLength, new String[] { toChars(context) }));
				}
				inst = this;
				return;
			}
			tempdecl = s.toAlias(context).isTemplateDeclaration();
			if (null == tempdecl) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolNotATemplate, lineNumber, typeStart, typeLength, new String[] { s.toChars(context) }));
				}
				inst = this;
				return;
			}
		}

		// Look for forward reference
		if (tempdecl == null) {
			throw new IllegalStateException("assert(tempdecl);");
		}
		for (TemplateDeclaration td = tempdecl; td != null; td = td.overnext) {
			if (null == td.scope) {
				/* Cannot handle forward references if mixin is a struct member,
				 * because addField must happen during struct's semantic, not
				 * during the mixin semantic.
				 * runDeferred will re-run mixin's semantic outside of the struct's
				 * semantic.
				 */
				semanticdone = 0;
				AggregateDeclaration ad = toParent().isAggregateDeclaration();
				if (ad != null) {
					ad.sizeok = 2;
				} else {
					// Forward reference
					scope = scx != null ? scx : new Scope(sc);
					scope.setNoFree();
					scope.module.addDeferredSemantic(this, context);
				}
				return;
			}
		}

		// Run semantic on each argument, place results in tiargs[]
		semanticTiargs(sc, context);

		tempdecl = findBestMatch(sc, context);
		if (null == tempdecl) {
			inst = this;
			return; // error recovery
		}

		if (null == ident) {
			ident = genIdent(context);
		}

		inst = this;
		parent = sc.parent;

		/* Detect recursive mixin instantiations.
		 */
	Lcontinue: 
		for (Dsymbol s = parent; s != null; s = s.parent) {
			TemplateMixin tm = s.isTemplateMixin();
			if (null == tm || tempdecl != tm.tempdecl) {
				continue;
			}
			
			/* Different argument list lengths happen with variadic args
			 */
			if (size(tiargs) != size(tm.tiargs)) {
			    continue;
			}

			for (int i = 0; i < tiargs.size(); i++) {
				ASTDmdNode o = tiargs.get(i);
				Type ta = isType(o);
				Expression ea = isExpression(o);
				Dsymbol sa = isDsymbol(o);
				ASTDmdNode tmo = tm.tiargs.get(i);
				if (ta != null) {
					Type tmta = isType(tmo);
					if (null == tmta) {
						// goto Lcontinue;
						continue Lcontinue;
					}
					if (!ta.equals(tmta)) {
						// goto Lcontinue;
						continue Lcontinue;
					}
				} else if (ea != null) {
					Expression tme = isExpression(tmo);
					if (null == tme || !ea.equals(tme, context)) {
						// goto Lcontinue;
						continue Lcontinue;
					}
				} else if (sa != null) {
					Dsymbol tmsa = isDsymbol(tmo);
					if (sa != tmsa) {
						// goto Lcontinue;
						continue Lcontinue;
					}
				} else {
					throw new IllegalStateException("assert(0);");
				}
			}
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.RecursiveMixinInstantiation, this));
			}
			return;
		}

		// Copy the syntax trees from the TemplateDeclaration
		members = Dsymbol.arraySyntaxCopy(tempdecl.members, context);
		if (null == members) {
			return;
		} else {
			if (context.mustCopySourceRangeForMixins()) {
				for(Dsymbol member : members) {
					member.accept(new AstVisitorAdapter() {
						@Override
						public boolean preVisit(ASTNode node) {
							node.setSourceRange(start, length);
							return true;
						}
					});
				}
			}
		}

		symtab = new DsymbolTable();

		for (Scope sce = sc; true; sce = sce.enclosing) {
			ScopeDsymbol sds = sce.scopesym;
			if (sds != null) {
				sds.importScope(this, PROTpublic);
				break;
			}
		}
		
		Scope scy = sc;
		scy = sc.push(this);
		scy.parent = this;

		argsym = new ScopeDsymbol();
		argsym.parent = scy.parent;
		Scope scope = scy.push(argsym);
		
	    int errorsave = context.global.errors;

		// Declare each template parameter as an alias for the argument type
		declareParameters(scope, context);
		
		// Descent: temporary adjust error position so errors doesn't
		// appear inside templates, but always on the invocation site
		context.startTemplateEvaluation(this.tempdecl, scope);

		try {
	
			// Add members to enclosing scope, as well as this scope
			for (int i = 0; i < members.size(); i++) {
				Dsymbol s;
	
				s = members.get(i);
				s.addMember(scope, this, i, context);
				//sc.insert(s);
			}
	
			// Do semantic() analysis on template instance members
			Scope sc2;
			sc2 = scope.push(this);
			sc2.offset = sc.offset;
			
		    if (++context.TemplateMixin_nest > 500)
		    {
		    	context.global.gag = 0;			// ensure error message gets printed
		    	if (context.acceptsErrors()) {
		    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.RecursiveTemplateExpansion, this));
		    	}
		    	context.fatalWasSignaled = true;
		    }
			
			for (int i = 0; i < members.size(); i++) {
				Dsymbol s = members.get(i);
				s.semantic(sc2, context);
			}
			
		    context.TemplateMixin_nest--;
			
			sc.offset = sc2.offset;
	
			/* The problem is when to parse the initializer for a variable.
			 * Perhaps VarDeclaration::semantic() should do it like it does
			 * for initializers inside a function.
			 */
			//	    if (sc.parent.isFuncDeclaration())
			semantic2(sc2, context);
	
			if (sc.func != null) {
				semantic3(sc2, context);
			}
			
		    // Give additional context info if error occurred during instantiation
		    if (context.global.errors != errorsave)
		    {
		    	if (context.acceptsErrors()) {
		    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.ErrorInstantiating, this));
		    	}
		    }
	
			sc2.pop();
	
			scope.pop();
	
			//	    if (!isAnonymous())
			{
				scy.pop();
			}
		} finally {
			// Descent: temporary adjust error position so errors doesn't
			// appear inside templates, but always on the invocation site
			context.endTemplateEvaluation(this.tempdecl, scope);
		}
	}

	@Override
	public void semantic2(Scope sc, SemanticContext context) {
		int i;

		if (semanticdone >= 2) {
			return;
		}
		semanticdone = 2;
		if (members != null) {
			if (sc == null) {
				throw new IllegalStateException("assert (sc);");
			}
			sc = sc.push(argsym);
			sc = sc.push(this);
			for (i = 0; i < members.size(); i++) {
				Dsymbol s = members.get(i);
				s.semantic2(sc, context);
			}
			sc = sc.pop();
			sc.pop();
		}
	}

	@Override
	public void semantic3(Scope sc, SemanticContext context) {
		int i;

		if (semanticdone >= 3) {
			return;
		}
		semanticdone = 3;
		if (members != null) {
			sc = sc.push(argsym);
			sc = sc.push(this);
			for (i = 0; i < members.size(); i++) {
				Dsymbol s = members.get(i);
				s.semantic3(sc, context);
			}
			sc = sc.pop();
			sc.pop();
		}
	}

	public void setTypeSourceRange(int start, int length) {
		this.typeStart = start;
		this.typeLength = length;
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		TemplateMixin tm;

		Identifiers ids = new Identifiers(idents.size());
		ids.setDim(idents.size());
		for (int i = 0; i < idents.size(); i++) { // Matches TypeQualified::syntaxCopyHelper()
			IdentifierExp id = idents.get(i);
			if (id.dyncast() == DYNCAST.DYNCAST_DSYMBOL) {
				TemplateInstance ti = ((TemplateInstanceWrapper) id).tempinst;

				ti = (TemplateInstance) ti.syntaxCopy(null, context);
				id = new TemplateInstanceWrapper(null, 0, ti);
			}
			ids.set(i, id);
		}

		tm = context.newTemplateMixin(filename, lineNumber, ident, (tqual != null ? tqual.syntaxCopy(context)
				: null), ids, tiargs);
		tm.copySourceRange(this);
		super.syntaxCopy(tm, context);
		return tm;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("mixin ");
		int i;
		for (i = 0; i < idents.size(); i++) {
			IdentifierExp id = idents.get(i);

			if (i != 0) {
				buf.writeByte('.');
			}
			buf.writestring(id.toChars());
		}
		buf.writestring("!(");
		if (tiargs != null) {
			for (i = 0; i < tiargs.size(); i++) {
				if (i != 0) {
					buf.writebyte(',');
				}
				ASTDmdNode oarg = tiargs.get(i);
				Type t = isType(oarg);
				Expression e = isExpression(oarg);
				Dsymbol s = isDsymbol(oarg);
				if (t != null) {
					t.toCBuffer(buf, null, hgs, context);
				} else if (e != null) {
					e.toCBuffer(buf, hgs, context);
				} else if (s != null) {
					String p = s.ident != null ? s.ident.toChars() : s
							.toChars(context);
					buf.writestring(p);
				} else if (null == oarg) {
					buf.writestring("NULL");
				} else {
					throw new IllegalStateException("assert(0);");
				}
			}
		}
		buf.writebyte(')');
		if (ident != null) {
			buf.writebyte(' ');
			buf.writestring(ident.toChars());
		}
		buf.writebyte(';');
		buf.writenl();
	}

	@Override
	public String toChars(SemanticContext context) {
		OutBuffer buf = new OutBuffer();
		HdrGenState hgs = new HdrGenState();

		super.toCBuffer(buf, hgs, context);
		String s = buf.toChars();
		buf.data = null;
		return s;
	}

}
