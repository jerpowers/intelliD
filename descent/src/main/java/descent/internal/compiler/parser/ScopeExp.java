package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class ScopeExp extends Expression {

	public ScopeDsymbol sds, sourceSds;

	public ScopeExp(char[] filename, int lineNumber, ScopeDsymbol pkg) {
		super(filename, lineNumber, TOK.TOKimport);
		this.sds = this.sourceSds = pkg;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceSds);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return SCOPE_EXP;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		// Descent: if it's null, problems were reported
		if (sds == null) {
			return this;
		}
		
		TemplateInstance ti;
		ScopeDsymbol sds2;

		boolean loop = true;
		Lagain: while (loop) {
			loop = false;
			ti = sds.isTemplateInstance();
			if (ti != null && context.global.errors == 0) {
				Dsymbol s;
				if (0 == ti.semanticdone) {
					ti.semantic(sc, context);
				}
				s = ti.inst.toAlias(context);
				sds2 = s.isScopeDsymbol();
				if (sds2 == null) {
					Expression e;

					if (ti.withsym != null) {
						// Same as wthis.s
						e = new VarExp(filename, lineNumber, ti.withsym.withstate.wthis);
						e = new DotVarExp(filename, lineNumber, e, s.isDeclaration());
					} else {
						e = new DsymbolExp(filename, lineNumber, s);
					}
					e = e.semantic(sc, context);
					return e;
				}
				if (sds2 != sds) {
					sds = sds2;
					// goto Lagain;
					loop = true;
					continue Lagain;
				}
			} else {
				sds.semantic(sc, context);
			}
		}

		type = Type.tvoid;
		return this;
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		ScopeExp se = new ScopeExp(filename, lineNumber, (ScopeDsymbol) sds.syntaxCopy(null, context));
		return se;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		// Descent: if it's null, problems were reported
		if (sds == null) {
			return;
		}
		
		if (sds.isTemplateInstance() != null) {
			sds.toCBuffer(buf, hgs, context);
		} else {
			buf.writestring(sds.kind());
			buf.writestring(" ");
			buf.writestring(sds.toChars(context));
		}
	}

}
