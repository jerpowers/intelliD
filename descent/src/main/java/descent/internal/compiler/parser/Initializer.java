package descent.internal.compiler.parser;

import descent.core.compiler.IProblem;


public abstract class Initializer extends ASTDmdNode {
	
	public int lineNumber;
	public char[] filename;
	
	/*
	 * Descent: for code evaluate.
	 */
	public Initializer resolvedInitializer;

	public Initializer(char[] filename, int lineNumber) {
		this.filename = filename;
		this.lineNumber = lineNumber;
	}

	public Initializers arraySyntaxCopy(Initializers ai, SemanticContext context) {
		Initializers a = null;

		if (ai != null) {
			a = new Initializers(ai.size());
			a.setDim(ai.size());
			for (int i = 0; i < a.size(); i++) {
				Initializer e = ai.get(i);
				e = e.syntaxCopy(context);
				a.set(i, e);
			}
		}
		return a;
	}

	public Type inferType(Scope sc, SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.CannotInferTypeFromInitializer, this));
		}
		return Type.terror;
	}
	
	public ArrayInitializer isArrayInitializer() {
		return null;
	}

	public ExpInitializer isExpInitializer() {
		return null;
	}

	public VoidInitializer isVoidInitializer() {
		return null;
	}
	
	public StructInitializer isStructInitializer() {
		return null;
	}

	public Initializer semantic(Scope sc, Type t, SemanticContext context) {
		return this;
	}

	public Initializer syntaxCopy(SemanticContext context) {
		return this;
	}

	public abstract void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context);

	@Override
	public String toChars(SemanticContext context) {
		OutBuffer buf = new OutBuffer();
		HdrGenState hgs = new HdrGenState();
		toCBuffer(buf, hgs, context);
		return buf.toChars();
	}

	public abstract Expression toExpression(SemanticContext context);
	
//	public abstract void toDt(SemanticContext context);
	
	@Override
	public int getLineNumber() {
		return lineNumber;
	}
	
	@Override
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

}
