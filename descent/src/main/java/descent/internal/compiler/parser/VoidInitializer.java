package descent.internal.compiler.parser;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class VoidInitializer extends Initializer {

	public Type type;

	public VoidInitializer(char[] filename, int lineNumber) {
		super(filename, lineNumber);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return VOID_INITIALIZER;
	}

	@Override
	public VoidInitializer isVoidInitializer() {
		return this;
	}

	@Override
	public Initializer semantic(Scope sc, Type t, SemanticContext context) {
		type = t;
		return this;
	}

	@Override
	public Initializer syntaxCopy(SemanticContext context) {
		return new VoidInitializer(filename, lineNumber);
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("void");
	}

	@Override
	public Expression toExpression(SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolHasNoValue, this, new String[] { "void initializer" }));
		}
		return new IntegerExp(filename, lineNumber, 0, Type.tint32);
	}

//	@Override
//	public void toDt(SemanticContext context) {
//		// Nothing
//	}

}
