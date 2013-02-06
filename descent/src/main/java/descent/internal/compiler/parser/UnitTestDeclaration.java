package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.Signature;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class UnitTestDeclaration extends FuncDeclaration {

	private static int unitTestId;

	private static char[] unitTestId() {
		return ("__unittest" + ++unitTestId).toCharArray();
	}

	public UnitTestDeclaration(char[] filename, int lineNumber) {
		super(filename, lineNumber, new IdentifierExp(unitTestId()), STC.STCundefined,
				null);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceFbody);
		}
		visitor.endVisit(this);
	}

	@Override
	public boolean addPostInvariant(SemanticContext context) {
		return false;
	}

	@Override
	public boolean addPreInvariant(SemanticContext context) {
		return false;
	}

	@Override
	public int getNodeType() {
		return UNIT_TEST_DECLARATION;
	}

	@Override
	public AggregateDeclaration isThis() {
		return null;
	}

	@Override
	public UnitTestDeclaration isUnitTestDeclaration() {
		return this;
	}

	@Override
	public boolean isVirtual(SemanticContext context) {
		return false;
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		if (context.global.params.useUnitTests) {
			// Type tret;
			type = new TypeFunction(null, Type.tvoid, 0, LINK.LINKd);
			Scope sc2 = sc.push();
			sc2.linkage = LINK.LINKd;
			super.semantic(sc2, context);
			sc2.pop();
		}

		// We're going to need ModuleInfo even if the unit tests are not
		// compiled in, because other modules may import this module and refer
		// to this ModuleInfo.
		Module m = getModule();
		if (m == null) {
			m = sc.module;
		}
		if (m != null) {
			m.needmoduleinfo = true;
		}
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		if (s != null) {
			throw new IllegalStateException("assert(!s);");
		}
		UnitTestDeclaration utd = context.newUnitTestDeclaration(filename, lineNumber);
		utd.copySourceRange(this);
		return super.syntaxCopy(utd, context);
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (hgs.hdrgen) {
			return;
		}
		buf.writestring("unittest");
		bodyToCBuffer(buf, hgs, context);
	}
	
	@Override
	public char getSignaturePrefix() {
		return Signature.C_SPECIAL_FUNCTION;
	}

}
