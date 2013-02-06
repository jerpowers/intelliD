package descent.internal.compiler.parser;

public abstract class Statement extends ASTDmdNode {

	public int lineNumber;
	public char[] filename;
	public boolean incontract;

	public Statement(char[] filename, int lineNumber) {
		this.filename = filename;
		this.lineNumber = lineNumber;
	}
	
	public int blockExit(SemanticContext context) {
		// TODO Semantic
//		throw new IllegalStateException("assert(0)");
		return 0;
	}

	public boolean comeFrom() {
		return false;
	}

	public Statements flatten(Scope sc, SemanticContext context) {
		return null;
	}

	public boolean hasBreak() {
		return false;
	}

	public boolean hasContinue() {
		return false;
	}

	public Expression interpret(InterState istate, SemanticContext context) {
		// START()
		if (istate.start != null) {
			if (istate.start != this) {
				return null;
			}
			istate.start = null;
		}
		// START()
		return EXP_CANT_INTERPRET;
	}

	public AsmStatement isAsmStatement() {
		return null;
	}

	public CompoundStatement isCompoundStatement() {
		return null;
	}

	public DeclarationStatement isDeclarationStatement() {
		return null;
	}

	public GotoStatement isGotoStatement() {
		return null;
	}

	public IfStatement isIfStatement() {
		return null;
	}

	public ReturnStatement isReturnStatement() {
		return null;
	}

	public ScopeStatement isScopeStatement() {
		return null;
	}

	public TryCatchStatement isTryCatchStatement() {
		return null;
	}

	public void scopeCode(Scope sc, Statement[] sentry, Statement[] sexception,
			Statement[] sfinally, SemanticContext context) {
		sentry[0] = null;
		sexception[0] = null;
		sfinally[0] = null;
	}

	public Statement semantic(Scope sc, SemanticContext context) {
		return this;
	}

	public Statement semanticScope(Scope sc, Statement sbreak,
			Statement scontinue, SemanticContext context) {
		Scope scd;
		Statement s;

		scd = sc.push();
		if (sbreak != null) {
			scd.sbreak = sbreak;
		}
		if (scontinue != null) {
			scd.scontinue = scontinue;
		}
		s = semantic(scd, context);
		scd.pop();
		return s;
	}

	public Statement syntaxCopy(SemanticContext context) {
		throw new IllegalStateException("assert(0);");
	}

	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("Statement::toCBuffer()");
		buf.writenl();
	}

	@Override
	public String toChars(SemanticContext context) {
		OutBuffer buf = new OutBuffer();
		HdrGenState hgs = new HdrGenState();
		toCBuffer(buf, hgs, context);
		return buf.toChars();
	}

	public boolean usesEH(SemanticContext context) {
		return false;
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
