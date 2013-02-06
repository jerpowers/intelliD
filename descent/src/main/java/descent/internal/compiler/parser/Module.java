package descent.internal.compiler.parser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.ICompilationUnit__Marker;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.CompilerUtil_Adapter;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class Module extends Package {
	
	private final static boolean FAST_SEARCH = false;

	public int apiLevel;
	public ModuleDeclaration md;
	public List<IProblem> problems;
	public Comment[] comments;
	public Pragma[] pragmas;
	public int[] lineEnds;
	public int semanticstarted; // has semantic() been started?
	public int semanticdone; // has semantic() been done?
	public boolean needmoduleinfo;
	public Module importedFrom;

	public int selfimports;
	public boolean insearch;

	public char[] searchCacheIdent;
	public int searchCacheFlags;
	public Dsymbol searchCacheSymbol;

	public long debuglevel; // debug level
	public HashtableOfCharArrayAndObject debugids; // debug identifiers
	public HashtableOfCharArrayAndObject debugidsNot; // forward referenced debug identifiers

	public long versionlevel;
	public HashtableOfCharArrayAndObject versionids; // version identifiers
	public HashtableOfCharArrayAndObject versionidsNot; // forward referenced version identifiers

	public Array aimports; // all imported modules
	
	public boolean safe;

	public File srcfile; // absolute path
	public char[] arg;
	
	// Very important: this field must be set to the module name
	// if signatures are to be requested
	public String moduleName; // foo.bar 
	public char[] moduleNameChars; // foo.bar
	private String signature; // Descent signature
	protected ICompilationUnit__Marker javaElement;
	
	// Comments associated to nodes (anot just ddoc)
	private Map<ASTDmdNode, List<Comment>> preComments;
	private Map<ASTDmdNode, Comment> postComments;
	
	// Modifiers associated to nodes
	private Map<ASTDmdNode, List<Modifier>> modifiers;

	public Module(String filename, IdentifierExp ident) {
		super(ident);
		importedFrom = this;
	}

	@Override
	public Module isModule() {
		return this;
	}

	@Override
	public int getNodeType() {
		return MODULE;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, md);
			TreeVisitor.acceptChildren(visitor, members);
			
//			acceptSynthetic(visitor);
		}
		visitor.endVisit(this);
	}

	public boolean hasSyntaxErrors() {
		return problems.size() != 0;
	}

	public void semantic(SemanticContext context) {
		semantic(null, context);
		
		// COMMENTED THIS, since when there are syntax errors we would
		// like to have a better recovery. But halt if fatal was signaled.
//		if (context.global.errors > 0) {
//			return;
//		}
		if (context.fatalWasSignaled) {
			return;
		}

		// COMMENTED THIS, since when there are syntax errors we would
		// like to have a better recovery. But halt if fatal was signaled.
		semantic2(null, context);
//		if (context.global.errors > 0) {
//			return;
//		}
		if (context.fatalWasSignaled) {
			return;
		}

		semantic3(null, context);
	
		// Added for Descent
		if (context.global.params.analyzeTemplates && this == context.Module_rootModule) {
			try {
				templateSemantic(context);
			} catch (Exception e) {
				CompilerUtil_Adapter.log(e, "Exception in template semantic in module " + moduleName);
			}
		}
	}	

	@Override
	public void semantic(Scope scope, SemanticContext context) {
		if (semanticstarted != 0) {
			return;
		}
		
		semanticstarted = 1;

		// Note that modules get their own scope, from scratch.
		// This is so regardless of where in the syntax a module
		// gets imported, it is unaffected by context.
		Scope sc = Scope.createGlobal(this, context);
		semanticScope(sc);

		boolean imObject;
		if (ident == null || ident.ident != Id.object) {
			Import im = new Import(null, 0, null,
					new IdentifierExp(Id.object), null, false);
			if (members == null) {
				members = new Dsymbols(1);
			}
			members.add(0, im);
			imObject = false;
		} else {
			imObject = true;
		}

		symtab = new DsymbolTable();
		
		// Add all symbols into module's symbol table
		// But, if this i'm module object, assign to SemanticContext's variables
		// (this only happens the first time object is parsed
		if (imObject) {
			for (int i = 0; i < size(members); i++) {
				Dsymbol s = members.get(i);
				context.checkObjectMember(s);
				s.addMember(null, sc.scopesym, 1, context);
			}
		} else {
			// Add all symbols into module's symbol table
			for (int i = 0; i < size(members); i++) {
				Dsymbol s = members.get(i);
				s.addMember(null, sc.scopesym, 1, context);
			}
		}
		
		int i = 0;
		if (!imObject) {
			// The first symbol should be "import object";
			// If some error happened, singal a fatal error
			int errors = context.global.errors;
			
			Dsymbol s = members.get(i);
			s.semantic(sc, context);
			runDeferredSemantic(context);
			
			if (context.global.errors > errors) {
				context.fatalWasSignaled = true;
				return;
			}
			
			i++;
		}

		// Pass 1 semantic routines: do public side of the definition
		for (; i < size(members); i++) {
			Dsymbol s = members.get(i);
			
			s.semantic(sc, context);
			
			runDeferredSemantic(context);
		}

		sc = sc.pop();
		sc.pop();

		semanticdone = semanticstarted;
		
	}

	@Override
	public void semantic2(Scope scope, SemanticContext context) {
		if (javaElement != null && FAST_SEARCH) {
			return;
		}
		
		if (context.Module_deferred != null
				&& context.Module_deferred.size() > 0) {
			for (Dsymbol sd : context.Module_deferred) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.CannotResolveForwardReference, sd.getLineNumber(), sd.getErrorStart(), sd.getErrorLength()));
				}
			}
			return;
		}
		if (semanticstarted >= 2) {
			return;
		}

		if (semanticstarted != 1) {
			throw new IllegalStateException("assert(semanticstarted == 1);");
		}
		semanticstarted = 2;

		// Note that modules get their own scope, from scratch.
		// This is so regardless of where in the syntax a module
		// gets imported, it is unaffected by context.
		Scope sc = Scope.createGlobal(this, context); // create root scope
		semantic2Scope(sc);

		// Pass 2 semantic routines: do initializers and function bodies
		if (members != null) {
			for (int i = 0; i < members.size(); i++) {
				Dsymbol s;
				s = members.get(i);
				s.semantic2(sc, context);
			}
		}

		sc = sc.pop();
		sc.pop();
		semanticdone = semanticstarted;
	}

	@Override
	public void semantic3(Scope scope, SemanticContext context) {
		if (javaElement != null && FAST_SEARCH) {
			return;
		}
		
		if (semanticstarted >= 3) {
			return;
		}

		if (semanticstarted != 2) {
			// SEMANTIC
			// throw new IllegalStateException("assert(semanticstarted == 2);");
			return;
		}
		semanticstarted = 3;

		// Note that modules get their own scope, from scratch.
		// This is so regardless of where in the syntax a module
		// gets imported, it is unaffected by context.
		Scope sc = Scope.createGlobal(this, context); // create root scope
		semantic3Scope(sc);

		// Pass 3 semantic routines: do initializers and function bodies
		if (members != null) {
			for (int i = 0; i < members.size(); i++) {
				Dsymbol s;
				s = members.get(i);
				s.semantic3(sc, context);
			}
		}

		sc = sc.pop();
		sc.pop();
		semanticdone = semanticstarted;
	}
	
	// This is so that autocompletion, semantic highlighting and go to
	// definition work inside templates. We just make a fake semantic
	// pass over those symbols that are templates
	private void templateSemantic(SemanticContext context) {
		if (members == null) return;
		
		context.templateSemanticStarted = true;
		context.muteProblems++;
		
		Scope sc = Scope.createGlobal(this, context); // create root scope
		for (int semanticPass = 0; semanticPass <= 3; semanticPass++) {
			templateSemantic(sc, context, members, semanticPass);
		}
		
		context.muteProblems--;
	}
	
	private void templateSemantic(Scope sc, SemanticContext context, Dsymbols members, int semanticPass) {
		if (members == null) return;
		
		int count = members.size();
		
		for(int i = 0; i < count; i++) {
			Dsymbol s = members.get(i);
			if (s instanceof AttribDeclaration) {
				AttribDeclaration attrib = (AttribDeclaration) s;
				switch(attrib.getNodeType()) {
				case ASTDmdNode.ALIGN_DECLARATION:
				case ASTDmdNode.ANON_DECLARATION:
				case ASTDmdNode.COMPILE_DECLARATION:
				case ASTDmdNode.LINK_DECLARATION:
				case ASTDmdNode.PRAGMA_DECLARATION:
				case ASTDmdNode.PROT_DECLARATION:
				case ASTDmdNode.STORAGE_CLASS_DECLARATION:
					templateSemantic(sc, context, attrib.decl, semanticPass);
					break;
				case ASTDmdNode.CONDITIONAL_DECLARATION:
					ConditionalDeclaration conditional = (ConditionalDeclaration) attrib;
					if (conditional.condition.inc == 1) {
						templateSemantic(sc, context, conditional.decl, semanticPass);
					} else if (conditional.condition.inc == 2) {
						templateSemantic(sc, context, conditional.elsedecl, semanticPass);
					}
					break;
				}
				continue;
			}
			
			if (s instanceof AggregateDeclaration) {
				templateSemantic(sc, context, ((AggregateDeclaration) s).members, semanticPass);
				continue;
			}
			
			if (s instanceof TemplateDeclaration) {
				TemplateDeclaration td = (TemplateDeclaration) s;
				if (semanticPass == 0) {
					td.symtab = new DsymbolTable();
				}
				sc = sc.push(td);
				
				if (td.members != null) {
					int count2 = td.members.size();
					
					for(int j = 0; j < count2; j++) {
						try {
							Dsymbol s2 = td.members.get(j);
							switch(semanticPass) {
							case 0:
								s2.addMember(sc, td, 1, context);
								break;
							case 1:
								s2.semantic(sc, context);
								break;
							case 2:
								s2.semantic2(sc, context);
								break;
							case 3:
								s2.semantic3(sc, context);
								break;
							}
						} catch (Exception e) {
//							Util.log(e, "Exception in template semantic in module " + moduleName);
						}
					}
				}
				sc = sc.pop();
			}
		}
	}
	
	protected void semanticScope(Scope sc) {
		
	}
	
	protected void semantic2Scope(Scope sc) {
		
	}
	
	protected void semantic3Scope(Scope sc) {
		
	}

	public void addDeferredSemantic(Dsymbol s, SemanticContext context) {
		if (context.Module_deferred == null) {
			context.Module_deferred = new Dsymbols();
		}

		// Don't add it if it is already there
		for (int i = 0; i < context.Module_deferred.size(); i++) {
			Dsymbol sd = context.Module_deferred.get(i);

			if (sd == s) {
				return;
			}
		}

		context.Module_deferred.add(s);
	}

	public void toModuleArray() {
		// TODO semantic
	}

	public void toModuleAssert() {
		// TODO semantic
	}

	public void runDeferredSemantic(SemanticContext context) {
		int len;

		if (context.Module_nested) {
			return;
		}
		context.Module_nested = true;

		do {
			context.Module_dprogress = 0;
			len = size(context.Module_deferred);
			if (0 == len) {
				break;
			}

			Dsymbol[] todo = new Dsymbol[size(context.Module_deferred)];
			for (int i = 0; i < size(context.Module_deferred); i++) {
				todo[i] = context.Module_deferred.get(i);
			}
			context.Module_deferred.clear();

			for (int i = 0; i < len; i++) {
				Dsymbol s = todo[i];

				s.semantic(null, context);
			}
		} while (size(context.Module_deferred) < len
				|| context.Module_dprogress != 0); // while
		// making
		// progress
		context.Module_nested = false;
	}

	@Override
	public String kind() {
		return "module";
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		// TODO Auto-generated method stub
		super.toCBuffer(buf, hgs, context);
	}

	@Override
	public Dsymbol search(char[] filename, int lineNumber, char[] ident, int flags,
			SemanticContext context) {
		/* Since modules can be circularly referenced,
		 * need to stop infinite recursive searches.
		 */
		
		Dsymbol s = null;
		if (insearch) {
			s = null;
		} else if (ASTDmdNode.equals(this.searchCacheIdent, ident)
				&& this.searchCacheFlags == flags) {
			s = this.searchCacheSymbol;
		} else {
			this.insearch = true;
			
			s = super.search(filename, lineNumber, ident, flags, context);
			
			this.insearch = false;

			this.searchCacheIdent = ident;			
			this.searchCacheSymbol = s;
			this.searchCacheFlags = flags;
		}
		
		return s;
	}

	public static Module load(char[] filename, int lineNumber, Identifiers packages,
			IdentifierExp ident, SemanticContext context) {
		
		// Delegate to SemanticContext in order to start resolving the Descent way
		return context.load(filename, lineNumber, packages, ident);
	}
	
	/************************************
	 * Recursively look at every module this module imports,
	 * return TRUE if it imports m.
	 * Can be used to detect circular imports.
	 */
	public int imports(Module m) {
		int aimports_dim = size(aimports);
		for (int i = 0; i < aimports_dim; i++) {
			Module mi = (Module) aimports.get(i);
			if (mi == m)
				return 1;
			if (!mi.insearch) {
				mi.insearch = true;
				int r = mi.imports(m);
				if (r != 0)
					return r;
			}
		}
		return 0;
	}

	/***************************************************************************
	 * Return !=0 if module imports itself.
	 */
	public int selfImports(SemanticContext context) {
		if (0 == selfimports) {
			for (int i = 0; i < size(context.Module_amodules); i++) {
				Module mi = (Module) context.Module_amodules.get(i);
				mi.insearch = false;
			}

			selfimports = imports(this) + 1;

			for (int i = 0; i < size(context.Module_amodules); i++) {
				Module mi = (Module) context.Module_amodules.get(i);
				mi.insearch = false;
			}
		}
		return selfimports - 1;
	}
	
	@Override
	public String getSignature(int options) {
		if (signature == null) {
			StringBuilder sb = new StringBuilder();
			appendSignature(sb, options);
			signature = sb.toString();
		}
		return signature;
	}
	
	@Override
	public void appendSignature(StringBuilder sb, int options) {
		sb.append(Signature.C_MODULE);
		String[] pieces = moduleName.split("\\.");
		for(String piece : pieces) {
			sb.append(piece.length());
			sb.append(piece);
		}
	}
	
	public String getFullyQualifiedName() {
		return moduleName;
	}

	public void setJavaElement(ICompilationUnit__Marker unit) {
		this.javaElement = unit;
	}
	
	@Override
	public ICompilationUnit__Marker getJavaElement() {
		return javaElement;
	}
	
	@Override
	public Module unlazy(char[] prefix, SemanticContext context) {
		return this;
	}
	
	public final void setPreComments(ASTDmdNode node, List<Comment> comms) {
		if (comms != null && !comms.isEmpty()) {
			if (preComments == null)
				preComments = new HashMap<ASTDmdNode, List<Comment>>();
			preComments.put(node, comms);
		}
	}
	
	public final List<Comment> getPreComments(ASTDmdNode node) {
		if (preComments == null)
			return null;
		return preComments.get(node);
	}
	
	public final void setPostComment(ASTDmdNode node, Comment comm) {
		if (postComments == null)
			postComments = new HashMap<ASTDmdNode, Comment>();
		postComments.put(node, comm);
	}
	
	public final Comment getPostComment(ASTDmdNode node) {
		if (postComments == null)
			return null;
		return postComments.get(node);
	}
	
	public final void setModifiers(ASTDmdNode node, List<Modifier> mods) {
		if (mods != null && !mods.isEmpty()) {
			if (modifiers == null)
				modifiers = new HashMap<ASTDmdNode, List<Modifier>>();
			this.modifiers.put(node, mods);
		}
	}
	
	public final List<Modifier> getModifiers(ASTDmdNode node) {
		if (modifiers == null)
			return null;
		return modifiers.get(node);
	}

}
