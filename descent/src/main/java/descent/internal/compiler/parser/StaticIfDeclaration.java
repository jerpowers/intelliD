package descent.internal.compiler.parser;

import melnorme.utilbox.core.Assert;

public class StaticIfDeclaration extends ConditionalDeclaration {

	public ScopeDsymbol sd;
	public boolean addisdone;

	public StaticIfDeclaration(Condition condition, Dsymbols decl,
			Dsymbols elsedecl) {
		super(condition, decl, elsedecl);
	}

	@Override
	public int addMember(Scope sc, ScopeDsymbol sd, int memnum,
			SemanticContext context) {
		/* This is deferred until semantic(), so that
		 * expressions in the condition can refer to declarations
		 * in the same scope, such as:
		 *
		 * template Foo(int i)
		 * {
		 *     const int j = i + 1;
		 *     static if (j == 3)
		 *         const int k;
		 * }
		 */
		this.sd = sd;
		int m = 0;

		if (memnum == 0) {
			m = super.addMember(sc, sd, memnum, context);
			addisdone = true;
		}
		return m;
	}

	@Override
	public String kind() {
		return "static if";
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		Dsymbols d = include(sc, sd, context);

		if (d != null) {
			if (!addisdone) {
				super.addMember(sc, sd, 1, context);
				addisdone = true;
			}

			for (Dsymbol s : d) {
				s.semantic(sc, context);
			}
		}
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		StaticIfDeclaration dd;

		Assert.isTrue(s == null);
		dd = context.newStaticIfDeclaration(condition.syntaxCopy(context), Dsymbol
				.arraySyntaxCopy(decl, context), Dsymbol.arraySyntaxCopy(elsedecl, context));
		dd.copySourceRange(this);
		dd.setJavaElement(getJavaElement());
		return dd;
	}

}
