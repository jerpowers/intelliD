package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCgshared;
import static descent.internal.compiler.parser.STC.STCstatic;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class ClassInfoDeclaration extends VarDeclaration {

	public ClassDeclaration cd;

	public ClassInfoDeclaration(ClassDeclaration cd, SemanticContext context) {
		this(null, 0, cd, context);
	}

	public ClassInfoDeclaration(char[] filename, int lineNumber, ClassDeclaration cd,
			SemanticContext context) {
		super(filename, lineNumber, context.ClassDeclaration_classinfo.type, cd.ident, null);
		this.cd = cd;
		if (context.isD1()) {
			this.storage_class = STCstatic;
		} else {
			this.storage_class = STCstatic | STCgshared;
		}
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		// empty
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		throw new IllegalStateException("assert(0);");
	}

}
