package descent.internal.compiler.parser;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import melnorme.utilbox.core.Assert;

import descent.core.IProblemRequestor;
import descent.core.JavaModelException__Common;
import descent.core.compiler.CharOperation;
import descent.core.compiler.IProblem;
import descent.internal.compiler.env.IModuleFinder;

public class SemanticContext implements IStringTableHolder {

	public boolean BREAKABI = true;
	public boolean IN_GCC = false;
	public boolean _DH = true;
	public boolean STRUCTTHISREF() {
		return apiLevel == Parser.D2;
	}

	public boolean IMPLICIT_ARRAY_TO_PTR() {
		return global.params.useDeprecated;
	}

	// If DMD is being run on Win32
	public boolean _WIN32 = true;

	public IProblemRequestor problemRequestor;
	public final Global global;
	public final IModuleFinder moduleFinder;

	// TODO file imports should be selectable in a dialog or something
	public final Map<String, File> fileImports = new HashMap<String, File>();

	public ClassDeclaration ClassDeclaration_object;
	public ClassDeclaration ClassDeclaration_classinfo;
	public ClassDeclaration Type_typeinfo;
	public ClassDeclaration Type_typeinfoclass;
	public ClassDeclaration Type_typeinfointerface;
	public ClassDeclaration Type_typeinfostruct;
	public ClassDeclaration Type_typeinfotypedef;
	public ClassDeclaration Type_typeinfopointer;
	public ClassDeclaration Type_typeinfoarray;
	public ClassDeclaration Type_typeinfostaticarray;
	public ClassDeclaration Type_typeinfoassociativearray;
	public ClassDeclaration Type_typeinfoenum;
	public ClassDeclaration Type_typeinfofunction;
	public ClassDeclaration Type_typeinfodelegate;
	public ClassDeclaration Type_typeinfotypelist;
	public ClassDeclaration Type_typeinfoconst;
	public ClassDeclaration Type_typeinfoinvariant;
	public ClassDeclaration Type_typeinfoshared;

	public Type Type_tvoidptr;

	public final Module Module_rootModule;
	public DsymbolTable Module_modules;
	public Array Module_amodules;
	public Dsymbols Module_deferred;
	public int Module_dprogress;
	public ClassDeclaration Module_moduleinfo;
	public boolean Module_nested = false;
	public int ASTDmdNode_idn;
	public int CompoundStatement_num;
	public Dsymbol TemplateAliasParameter_sdummy = null;
	public Expression TemplateValueParameter_edummy = null;
	public TypeInfoDeclaration[] Type_internalTI = new TypeInfoDeclaration[TY.values().length];
	public StringTable ArrayOp_arrayfuncs = new StringTable();
	public int TemplateInstace_nest;
	public int TemplateMixin_nest;

	public int apiLevel;

	private IStringTableHolder holder;
	public DsymbolTable st;

	/*
	 * If != 0, problems are not reported.
	 */
	public int muteProblems = 0;

	/*
	 * If true, errors no longer are reported.
	 */
	public boolean fatalWasSignaled;
	public boolean templateSemanticStarted = false;

	public final ASTNodeEncoder encoder;
	public boolean alwaysResolveFunctionSemanticRest;
	public List<ASTDmdNode> templateEvaluationStack;

	/*
	 * Once semantic pass is done, the evaluated expression of IdentifierExps are kept in
	 * this variable. Only for compile-time function evaluation.
	 */
	private Map<IdentifierExp, Expression> IdentifierExp_evaluatedExpressions = new HashMap<IdentifierExp, Expression>();

	/*
	 * Once semantic pass is done, if this identifier was the name of
	 * a TemplateInstance, this holds it.
	 */
	private Map<IdentifierExp, TemplateInstance> IdentifierExp_templateInstances = new HashMap<IdentifierExp, TemplateInstance>();

	/*
	 * Once the semantic pass is done, the resolved expression is kept in
	 * this variable. This is useful for linking source with resolution.
	 */
	private Map<Expression, Expression> Expression_resolvedExpressions = new HashMap<Expression, Expression>();

	/*
	 * Same as resolved expression, but holds an Dsymbol.
	 */
	private Map<Expression, Dsymbol> Expression_resolvedSymbols = new HashMap<Expression, Dsymbol>();

	/*
	 * Symbols created from mixins holds a reference
	 * to the CompileStatement or CompileDeclaration that created
	 * them, so we can then go to this creator when doing code-selection.
	 */
	private Map<ASTDmdNode, Dsymbol> ASTDmdNode_creators = new HashMap<ASTDmdNode, Dsymbol>();

	// Modifiers assigned from out parent, to better report problems
	private final Map<ASTDmdNode, List<Modifier>> extraModifiers = new HashMap<ASTDmdNode, List<Modifier>>();

	/*
	 * This is for autocompletion, for suggesting overloads of
	 * aliased symbols.
	 */
	public boolean allowOvernextBySignature = false;

	public SemanticContext(
			IProblemRequestor problemRequestor,
			Module module,
			Global global,
			ASTNodeEncoder encoder,
			IStringTableHolder stringTableHolder,
			IModuleFinder moduleFinder,
			int apiLevel,
			int semanticAnalysisLevel) throws JavaModelException__Common {
		this.problemRequestor = problemRequestor;
		this.Module_rootModule = module;
		this.global = global;
		this.moduleFinder = moduleFinder;
		this.holder = stringTableHolder;
		this.Type_tvoidptr = Type.tvoid.pointerTo(this);
		this.encoder = encoder;
		this.templateEvaluationStack = new LinkedList<ASTDmdNode>();
		this.apiLevel = apiLevel;

		if (semanticAnalysisLevel == 0) {
			muteProblems++;
		}

		// for debugging purposes
//		this.moduleFinder = new DmdModuleFinder(global);

		Module_init();
		afterParse(module);
	}

	@Override
	public StringTable getStringTable() {
		return holder.getStringTable();
	}

	private int uniqueIdCount = 0;
	public IdentifierExp uniqueId(String prefix) {
		return uniqueId(prefix, uniqueIdCount++);
	}

	public IdentifierExp uniqueId(String prefix, int i) {
		return new IdentifierExp((prefix + i).toCharArray());
	}

	private void Module_init() {
		this.Module_modules = new DsymbolTable();
	}

	public void setEvaluated(IdentifierExp exp, Expression evaluated) {
		IdentifierExp_evaluatedExpressions.put(exp, evaluated);
	}

	public Expression getEvaluated(IdentifierExp exp) {
		return IdentifierExp_evaluatedExpressions.get(exp);
	}

	public void setTemplateInstance(IdentifierExp exp, TemplateInstance tinst) {
		IdentifierExp_templateInstances.put(exp, tinst);
	}

	public TemplateInstance getTemplateInstance(IdentifierExp exp) {
		return IdentifierExp_templateInstances.get(exp);
	}

	public void setResolvedExp(Expression exp, Expression resolved) {
		Expression_resolvedExpressions.put(exp, resolved);
	}

	public Expression getResolvedExp(Expression exp) {
		return Expression_resolvedExpressions.get(exp);
	}

	public void setResolvedSymbol(Expression exp, Dsymbol resolved) {
		Expression_resolvedSymbols.put(exp, resolved);
	}

	public Dsymbol getResolvedSymbol(Expression exp) {
		return Expression_resolvedSymbols.get(exp);
	}

	public void setCreator(ASTDmdNode node, Dsymbol creator) {
		ASTDmdNode_creators.put(node, creator);
	}

	public Dsymbol getCreator(ASTDmdNode node) {
		return ASTDmdNode_creators.get(node);
	}

	public final void setExtraModifiers(ASTDmdNode node, List<Modifier> modifiers) {
		if (modifiers != null && !modifiers.isEmpty()) {
			this.extraModifiers.put(node, modifiers);
		}
	}

	public final List<Modifier> getExtraModifiers(ASTDmdNode node) {
		return extraModifiers.get(node);
	}

	/*
	 * This code is invoked by DMD after parsing a module.
	 */
	public void afterParse(Module module) {
		DsymbolTable dst;

		if (module.md != null) {
			module.ident = module.md.id();
			module.safe = module.md.safe;
			Dsymbol[] pparent = { module.parent };
			dst = Package.resolve(module.md.packages(), pparent, null, this);
			module.parent = pparent[0];
		} else {
			dst = Module_modules;
		}

		// Update global list of modules
		if (null == dst.insert(module)) {
			if (module.md != null) {
				if (acceptsErrors()) {
					acceptProblem(Problem.newSemanticTypeError(
							IProblem.ModuleIsInMultiplePackages, module.md, new String[] { module.md.toChars(this) }));
				}
			} else {
				if (module.md == null) {
					if (acceptsErrors()) {
						acceptProblem(Problem.newSemanticTypeError(
								IProblem.ModuleIsInMultipleDefined, 0, 0, 1));
					}
				} else {
					if (acceptsErrors()) {
						acceptProblem(Problem.newSemanticTypeError(
								IProblem.ModuleIsInMultipleDefined, module.md));
					}
				}
			}
		} else {
			if (Module_amodules == null) {
				Module_amodules = new Dsymbols();
			}
			Module_amodules.add(module);
		}
	}

	public final boolean acceptsErrors() {
		if (fatalWasSignaled) {
			return false;
		}

		if (global.gag == 0 && muteProblems == 0 && problemRequestor != null) {
			return true;
		} else {
			// Each acceptProblem is preceded by acceptsProblems, and originaly
			// global.errors is incremented, so...

			if (!templateSemanticStarted) {
				global.errors++;
			}
			return false;
		}
	}

	public final boolean acceptsWarnings() {
		if (fatalWasSignaled) {
			return false;
		}

		if (global.gag == 0 && muteProblems == 0 && problemRequestor != null) {
			return true;
		} else {
			return false;
		}
	}

	public void acceptProblem(Problem problem) {
		// This one is important to see if the user configured Descent correctly
		if (problem.getID() == IProblem.MissingOrCurruptObjectDotD) {
			problemRequestor.acceptProblem(problem);
			if (problem.isError()) {
				if (!templateSemanticStarted) {
					global.errors++;
				}
			}
			return;
		}

		// Don't report more problems if fatal was signaled
		if (fatalWasSignaled) {
			return;
		}

		if (global.gag == 0 && muteProblems == 0 && problemRequestor != null) {
//			System.out.println("~~~" + problem);

			if (!templateEvaluationStack.isEmpty()) {
				ASTDmdNode target = templateEvaluationStack.get(0);
				problem.setSourceStart(target.start);
				problem.setSourceEnd(target.start + target.length - 1);
			}

			problemRequestor.acceptProblem(problem);
		}

		if (problem.isError()) {
			if (!templateSemanticStarted) {
				global.errors++;
			}
		}
	}

	private int generatedIds;

	public IdentifierExp generateId(String prefix) {
		return generateId(prefix, ++generatedIds);
	}

	public IdentifierExp generateId(String prefix, int i) {
		String name = prefix + i;
		char[] id = name.toCharArray();
		return new IdentifierExp(id);
	}

	public FuncDeclaration genCfunc(Type treturn, char[] id) {
		FuncDeclaration fd;
		TypeFunction tf;
		Dsymbol s;

		// See if already in table
		if (st == null)
			st = new DsymbolTable();
		s = st.lookup(id);
		if (s != null) {
			fd = s.isFuncDeclaration();
			Assert.isNotNull(fd);
			Assert.isTrue(fd.type.nextOf().equals(treturn));
		} else {
			tf = new TypeFunction(null, treturn, 0, LINK.LINKc);
			fd = new FuncDeclaration(null, 0, new IdentifierExp(id),
					STC.STCstatic, tf);
			fd.protection = PROT.PROTpublic;
			fd.linkage = LINK.LINKc;

			st.insert(fd);
		}
		return fd;
	}


	public void startTemplateEvaluation(TemplateDeclaration node, Scope sc) {
		this.templateEvaluationStack.add(node);
	}

	public void endTemplateEvaluation(TemplateDeclaration node, Scope sc) {
		this.templateEvaluationStack.remove(this.templateEvaluationStack.size() - 1);
	}

	public void startFunctionInterpret(CallExp fd) {
		this.templateEvaluationStack.add(fd);
	}

	public void endFunctionInterpret(CallExp fd) {
		this.templateEvaluationStack.remove(this.templateEvaluationStack.size() - 1);
	}

	public Module load(char[] filename, int lineNumber, Identifiers packages,
			IdentifierExp ident) {

		// Build the compound module name
		char[][] compoundName = new char[(packages == null ? 0 : packages.size()) + 1][];
		if (packages != null) {
			for(int i = 0; i < packages.size(); i++) {
				compoundName[i] = packages.get(i).ident;
			}
		}
		compoundName[compoundName.length - 1] = ident.ident;

		Module m = moduleFinder.findModule(compoundName, this);
		if (m == null){
			int start = packages == null || packages.size() == 0 ? ident.start : packages.get(0).start;
			int length = ident.start + ident.length - start;

			if (acceptsErrors()) {
				acceptProblem(Problem.newSemanticTypeError(IProblem.ImportCannotBeResolved, ident.getLineNumber(), start, length, new String[] { CharOperation.toString(compoundName) }));
			}
			return null;
		}

		afterParse(m);

		// If we're in object.d, assign the well known class declarations
		if (compoundName.length == 1 && CharOperation.equals(compoundName[0], Id.object)) {
			if (m.members != null) {
				for (Dsymbol symbol : m.members) {
					checkObjectMember(symbol);
				}
			}
		}

		return m;
	}

	public void checkObjectMember(Dsymbol s) {
		if (s.ident == null || s.ident.ident == null) {
			return;
		}

		if (ASTDmdNode.equals(s.ident, Id.Object)) {
			ClassDeclaration_object = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.ClassInfo)) {
			ClassDeclaration_classinfo = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo)) {
			Type_typeinfo = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Class)) {
			Type_typeinfoclass = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Interface)) {
			Type_typeinfointerface = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Struct)) {
			Type_typeinfostruct = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Typedef)) {
			Type_typeinfotypedef = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Pointer)) {
			Type_typeinfopointer = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Array)) {
			Type_typeinfoarray = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_StaticArray)) {
			Type_typeinfostaticarray = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_AssociativeArray)) {
			Type_typeinfoassociativearray = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Enum)) {
			Type_typeinfoenum = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Function)) {
			Type_typeinfofunction = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Delegate)) {
			Type_typeinfodelegate = (ClassDeclaration) s;
		} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Tuple)) {
			Type_typeinfotypelist = (ClassDeclaration) s;
		}

		if (isD2()) {
			if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Const)) {
				Type_typeinfoconst = (ClassDeclaration) s;
			} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Invariant)) {
				Type_typeinfoinvariant = (ClassDeclaration) s;
			} else if (ASTDmdNode.equals(s.ident, Id.TypeInfo_Shared)) {
				Type_typeinfoshared = (ClassDeclaration) s;
			}
		}
	}

	private Map<Type, TypeInfoDeclaration> typeInfoDeclarations = new HashMap<Type, TypeInfoDeclaration>();

	public TypeInfoDeclaration getTypeInfo(Type t) {
		return typeInfoDeclarations.get(t);
	}

	public void setTypeInfo(Type t, TypeInfoDeclaration vtinfo) {
		typeInfoDeclarations.put(t, vtinfo);
	}

	public final boolean isD1() {
		return apiLevel == Parser.D1;
	}

	public final boolean isD2() {
		return apiLevel == Parser.D2;
	}

	public Parser newParser(char[] source) {
		return newParser(apiLevel, source);
	}

	protected Parser newParser(int apiLevel, char[] source) {
		Parser parser = new Parser(apiLevel, source);
		parser.holder = holder;
		return parser;
	}

	protected Parser newParser(char[] source, int offset, int length,
			boolean tokenizeComments, boolean tokenizePragmas,
			boolean tokenizeWhiteSpace, boolean recordLineSeparator,
			int apiLevel,
			char[][] taskTags, char[][] taskPriorities, boolean isTaskCaseSensitive,
			char[] filename) {
		Parser parser = new Parser(source, offset, length, tokenizeComments, tokenizePragmas, tokenizeWhiteSpace, recordLineSeparator, apiLevel, taskTags, taskPriorities, isTaskCaseSensitive, filename);
		parser.holder = holder;
		return parser;
	}

	protected boolean mustCopySourceRangeForMixins() {
		return false;
	}

	protected VarDeclaration newVarDeclaration(char[] filename, int lineNumber, Type type, IdentifierExp exp, Initializer init) {
		return new VarDeclaration(filename, lineNumber, type, exp, init);
	}

	protected ConditionalDeclaration newConditionalDeclaration(Condition condition, Dsymbols a, Dsymbols elseDecl) {
		return new ConditionalDeclaration(condition, a, elseDecl);
	}

	protected StaticIfDeclaration newStaticIfDeclaration(Condition condition, Dsymbols a, Dsymbols aelse) {
		return new StaticIfDeclaration(condition, a, aelse);
	}

	protected CallExp newCallExp(char[] filename, int lineNumber, Expression e, Expressions args) {
		return new CallExp(filename, lineNumber, e, args);
	}

	protected FuncDeclaration newFuncDeclaration(char[] filename, int lineNumber, IdentifierExp ident,
			int storage_class, Type syntaxCopy) {
		return new FuncDeclaration(filename, lineNumber, ident, storage_class, syntaxCopy);
	}

	protected IfStatement newIfStatement(char[] filename, int lineNumber, Argument a, Expression condition, Statement ifbody, Statement elsebody) {
		return new IfStatement(filename, lineNumber, a, condition, ifbody, elsebody);
	}

	protected ReturnStatement newReturnStatement(char[] filename, int lineNumber, Expression e) {
		return new ReturnStatement(filename, lineNumber, e);
	}

	protected DeclarationStatement newDeclarationStatement(char[] filename, int lineNumber, Expression e) {
		return new DeclarationStatement(filename, lineNumber, e);
	}

	protected ExpStatement newExpStatement(char[] filename, int lineNumber, Expression e) {
		return new ExpStatement(filename, lineNumber, e);
	}

	protected BreakStatement newBreakStatement(char[] filename, int lineNumber, IdentifierExp ident) {
		return new BreakStatement(filename, lineNumber, ident);
	}

	protected CaseStatement newCaseStatement(char[] filename, int lineNumber, Expression expression, Statement statement) {
		return new CaseStatement(filename, lineNumber, expression, statement);
	}

	protected CompileStatement newCompileStatement(char[] filename, int lineNumber, Expression e) {
		return new CompileStatement(filename, lineNumber, e);
	}

	protected CompoundStatement newCompoundStatement(char[] filename, int lineNumber, Statements a) {
		return new CompoundStatement(filename, lineNumber, a);
	}

	protected ConditionalStatement newConditionalStatement(char[] filename, int lineNumber, Condition condition, Statement statement, Statement e) {
		return new ConditionalStatement(filename, lineNumber, condition, statement, e);
	}

	protected ContinueStatement newContinueStatement(char[] filename, int lineNumber, IdentifierExp ident) {
		return new ContinueStatement(filename, lineNumber, ident);
	}

	protected DefaultStatement newDefaultStatement(char[] filename, int lineNumber, Statement statement) {
		return new DefaultStatement(filename, lineNumber, statement);
	}

	protected DoStatement newDoStatement(char[] filename, int lineNumber, Statement statement, Expression expression) {
		return new DoStatement(filename, lineNumber, statement, expression);
	}

	protected ForeachRangeStatement newForeachRangeStatement(char[] filename, int lineNumber, TOK op, Argument argument, Expression expression, Expression expression2, Statement statement) {
		return new ForeachRangeStatement(filename, lineNumber, op, argument, expression, expression2, statement);
	}

	protected ForeachStatement newForeachStatement(char[] filename, int lineNumber, TOK op, Arguments args, Expression exp, Statement statement) {
		return new ForeachStatement(filename, lineNumber, op, args, exp, statement);
	}

	protected ForStatement newForStatement(char[] filename, int lineNumber, Statement i, Expression c, Expression inc, Statement statement) {
		return new ForStatement(filename, lineNumber, i, c, inc, statement);
	}

	protected GotoCaseStatement newGotoCaseStatement(char[] filename, int lineNumber, Expression e) {
		return new GotoCaseStatement(filename, lineNumber, e);
	}

	protected GotoDefaultStatement newGotoDefaultStatement(char[] filename, int lineNumber) {
		return new GotoDefaultStatement(filename, lineNumber);
	}

	protected GotoStatement newGotoStatement(char[] filename, int lineNumber, IdentifierExp ident) {
		return new GotoStatement(filename, lineNumber, ident);
	}

	protected LabelStatement newLabelStatement(char[] filename, int lineNumber, IdentifierExp ident, Statement statement) {
		return new LabelStatement(filename, lineNumber, ident, statement);
	}

	protected OnScopeStatement newOnScopeStatement(char[] filename, int lineNumber, TOK tok, Statement statement) {
		return new OnScopeStatement(filename, lineNumber, tok, statement);
	}

	protected PragmaStatement newPragmaStatement(char[] filename, int lineNumber, IdentifierExp ident, Expressions expressions, Statement b) {
		return new PragmaStatement(filename, lineNumber, ident, expressions, b);
	}

	protected ScopeStatement newScopeStatement(char[] filename, int lineNumber, Statement s) {
		return new ScopeStatement(filename, lineNumber, s);
	}

	protected StaticAssertStatement newStaticAssertStatement(StaticAssert assert1) {
		return new StaticAssertStatement(assert1);
	}

	protected SwitchStatement newSwitchStatement(char[] filename, int lineNumber, Expression expression, Statement statement, boolean isfinal) {
		return new SwitchStatement(filename, lineNumber, expression, statement, isfinal);
	}

	protected SynchronizedStatement newSynchronizedStatement(char[] filename, int lineNumber, Expression e, Statement statement) {
		return new SynchronizedStatement(filename, lineNumber, e, statement);
	}

	protected ThrowStatement newThrowStatement(char[] filename, int lineNumber, Expression expression) {
		return new ThrowStatement(filename, lineNumber, expression);
	}

	protected TryCatchStatement newTryCatchStatement(char[] filename, int lineNumber, Statement statement, Array<Catch> a) {
		return new TryCatchStatement(filename, lineNumber, statement, a);
	}

	protected TryFinallyStatement newTryFinallyStatement(char[] filename, int lineNumber, Statement statement, Statement statement2) {
		return new TryFinallyStatement(filename, lineNumber, statement, statement2);
	}

	protected VolatileStatement newVolatileStatement(char[] filename, int lineNumber, Statement statement) {
		return new VolatileStatement(filename, lineNumber, statement);
	}

	protected WhileStatement newWhileStatement(char[] filename, int lineNumber, Expression expression, Statement statement) {
		return new WhileStatement(filename, lineNumber, expression, statement);
	}

	protected WithStatement newWithStatement(char[] filename, int lineNumber, Expression expression, Statement statement) {
		return new WithStatement(filename, lineNumber, expression, statement);
	}

	protected PragmaDeclaration newPragmaDeclaration(char[] filename, int lineNumber, IdentifierExp ident, Expressions expressions, Dsymbols dsymbols) {
		return new PragmaDeclaration(filename, lineNumber, ident, expressions, dsymbols);
	}

	protected AlignDeclaration newAlignDeclaration(int salign, Dsymbols dsymbols) {
		return new AlignDeclaration(salign, dsymbols);
	}

	protected AnonDeclaration newAnonDeclaration(char[] filename, int lineNumber, boolean isunion, Dsymbols dsymbols) {
		return new AnonDeclaration(filename, lineNumber, isunion, dsymbols);
	}

	protected CompileDeclaration newCompileDeclaration(char[] filename, int lineNumber, Expression expression) {
		return new CompileDeclaration(filename, lineNumber, expression);
	}

	protected LinkDeclaration newLinkDeclaration(LINK linkage, Dsymbols dsymbols) {
		return new LinkDeclaration(linkage, dsymbols);
	}

	protected DebugSymbol newDebugSymbol(char[] filename, int lineNumber, IdentifierExp ident, Version version) {
		return new DebugSymbol(filename, lineNumber, ident, version);
	}

	protected AliasDeclaration newAliasDeclaration(char[] filename, int lineNumber, IdentifierExp ident, Type type) {
		return new AliasDeclaration(filename, lineNumber, ident, type);
	}

	protected AliasDeclaration newAliasDeclaration(char[] filename, int lineNumber, IdentifierExp ident, Dsymbol dsymbol) {
		return new AliasDeclaration(filename, lineNumber, ident, dsymbol);
	}

	protected CtorDeclaration newCtorDeclaration(char[] filename, int lineNumber, Arguments arguments, int varargs) {
		return new CtorDeclaration(filename, lineNumber, arguments, varargs);
	}

	protected DeleteDeclaration newDeleteDeclaration(char[] filename, int lineNumber, Arguments arguments) {
		return new DeleteDeclaration(filename, lineNumber, arguments);
	}

	protected DtorDeclaration newDtorDeclaration(char[] filename, int lineNumber, IdentifierExp ident) {
		return new DtorDeclaration(filename, lineNumber, ident);
	}

	protected InvariantDeclaration newInvariantDeclaration(char[] filename, int lineNumber) {
		return new InvariantDeclaration(filename, lineNumber);
	}

	protected NewDeclaration newNewDeclaration(char[] filename, int lineNumber, Arguments arguments, int varargs) {
		return new NewDeclaration(filename, lineNumber, arguments, varargs);
	}

	protected PostBlitDeclaration newPostBlitDeclaration(char[] filename, int lineNumber, IdentifierExp ident) {
		return new PostBlitDeclaration(filename, lineNumber, ident);
	}

	protected StaticCtorDeclaration newStaticCtorDeclaration(char[] filename, int lineNumber) {
		return new StaticCtorDeclaration(filename, lineNumber);
	}

	protected StaticDtorDeclaration newStaticDtorDeclaration(char[] filename, int lineNumber) {
		return new StaticDtorDeclaration(filename, lineNumber);
	}

	protected UnitTestDeclaration newUnitTestDeclaration(char[] filename, int lineNumber) {
		return new UnitTestDeclaration(filename, lineNumber);
	}

	protected TypedefDeclaration newTypedefDeclaration(char[] filename, int lineNumber, IdentifierExp ident, Type basetype, Initializer init) {
		return new TypedefDeclaration(filename, lineNumber, ident, basetype, init);
	}

	protected EnumMember newEnumMember(char[] filename, int lineNumber, IdentifierExp ident, Expression e, Type t) {
		return new EnumMember(filename, lineNumber, ident, e, t);
	}

	protected StaticAssert newStaticAssert(char[] filename, int lineNumber, Expression expression, Expression expression2) {
		return new StaticAssert(filename, lineNumber, expression, expression2);
	}

	protected VersionSymbol newVersionSymbol(char[] filename, int lineNumber, IdentifierExp ident, Version version) {
		return new VersionSymbol(filename, lineNumber, ident, version);
	}

	protected ClassDeclaration newClassDeclaration(char[] filename, int lineNumber, IdentifierExp ident, BaseClasses baseClasses) {
		return new ClassDeclaration(filename, lineNumber, ident, baseClasses);
	}

	protected InterfaceDeclaration newInterfaceDeclaration(char[] filename, int lineNumber, IdentifierExp ident, BaseClasses baseClasses) {
		return new InterfaceDeclaration(filename, lineNumber, ident, baseClasses);
	}

	protected UnionDeclaration newUnionDeclaration(char[] filename, int lineNumber, IdentifierExp ident) {
		return new UnionDeclaration(filename, lineNumber, ident);
	}

	protected StructDeclaration newStructDeclaration(char[] filename, int lineNumber, IdentifierExp ident) {
		return new StructDeclaration(filename, lineNumber, ident);
	}

	protected EnumDeclaration newEnumDeclaration(char[] filename, int lineNumber, IdentifierExp ident, Type t) {
		return new EnumDeclaration(filename, lineNumber, ident, t);
	}

	protected TemplateDeclaration newTemplateDeclaration(char[] filename, int lineNumber, IdentifierExp ident, TemplateParameters p, Expression c, Dsymbols d) {
		return new TemplateDeclaration(filename, lineNumber, ident, p, c, d);
	}

	protected TemplateMixin newTemplateMixin(char[] filename, int lineNumber, IdentifierExp ident, Type type, Identifiers ids, Objects tiargs) {
		return new TemplateMixin(filename, lineNumber, ident, type, ids, tiargs, encoder);
	}

	public TemplateInstance newTemplateInstance(char[] filename, int lineNumber, IdentifierExp name) {
		return new TemplateInstance(filename, lineNumber, name, encoder);
	}

	public Expression newTypeDotIdExp(char[] filename, int lineNumber, Type type, IdentifierExp ident) {
		return new DotIdExp(filename, lineNumber, new TypeExp(filename, lineNumber, type), ident);
	}

	public CompoundDeclarationStatement newCompoundDeclarationStatement(char[] filename, int lineNumber, Statements a) {
		return new CompoundDeclarationStatement(filename, lineNumber, a);
	}

	public CaseRangeStatement newCaseRangeStatement(char[] filename,
			int lineNumber, Expression first, Expression last,
			Statement statement) {
		return new CaseRangeStatement(filename, lineNumber, first, last, statement);
	}

}
