package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class WithScopeSymbol extends ScopeDsymbol {

	public WithStatement withstate;

	public WithScopeSymbol(WithStatement withstate) {
		this.withstate = withstate;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		melnorme.utilbox.core.Assert.fail("accept0 on a fake Node");
	}

	@Override
	public WithScopeSymbol isWithScopeSymbol() {
		return this;
	}

	@Override
	public Dsymbol search(char[] filename, int lineNumber, char[] ident, int flags,
			SemanticContext context) {
		return withstate.exp.type.toDsymbol(null, context).search(filename, lineNumber, ident,
				0, context);
	}

}
