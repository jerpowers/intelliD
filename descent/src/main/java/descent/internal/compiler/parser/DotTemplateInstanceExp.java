package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKdotexp;
import static descent.internal.compiler.parser.TOK.TOKimport;
import static descent.internal.compiler.parser.TOK.TOKtype;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tpointer;
import static descent.internal.compiler.parser.TY.Tstruct;
import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class DotTemplateInstanceExp extends UnaExp {

	public TemplateInstance ti;

	public DotTemplateInstanceExp(char[] filename, int lineNumber, Expression e, TemplateInstance ti) {
		super(filename, lineNumber, TOK.TOKdotti, e);
		this.ti = ti;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceE1);
			TreeVisitor.acceptChildren(visitor, ti);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return DOT_TEMPLATE_INSTANCE_EXP;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Dsymbol s;
		Dsymbol s2;
		TemplateDeclaration td;
		Expression e;
		char[] id;
		Type t1;
		Expression eleft = null;
		Expression eright;

		e1 = e1.semantic(sc, context);
		t1 = e1.type;
		if (t1 != null) {
			t1 = t1.toBasetype(context);
		}

	    /* Extract the following from e1:
	     *	s: the symbol which ti should be a member of
	     *	eleft: if not NULL, it is the 'this' pointer for ti
	     */

		if (e1.op == TOKdotexp) {
			DotExp de = (DotExp) e1;
			eleft = de.e1;
			eright = de.e2;
		} else {
			eleft = null;
			eright = e1;
		}
		if (eright.op == TOKimport) {
			s = ((ScopeExp) eright).sds;
		} else if (e1.op == TOKtype) {
			s = t1.isClassHandle();
			if (s == null) {
				if (t1.ty == Tstruct) {
					s = ((TypeStruct) t1).sym;
				} else {
					// goto L1;
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.TemplateIsNotAMemberOf, this, ti.toChars(context), e1.toChars(context)));
					}
					return new IntegerExp(filename, lineNumber, 0);
				}
			}
		} else if (t1 != null && (t1.ty == Tstruct || t1.ty == Tclass)) {
			s = t1.toDsymbol(sc, context);
			eleft = e1;
		} else if (t1 != null && t1.ty == Tpointer) {
			t1 = ((TypePointer)t1).next.toBasetype(context);
			if (t1.ty != Tstruct) {
				// goto L1;
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.TemplateIsNotAMemberOf, this, ti.toChars(context), e1.toChars(context)));
				}
				return new IntegerExp(filename, lineNumber, 0);
			}
			s = t1.toDsymbol(sc, context);
			eleft = e1;
		} else {
			// L1:
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.TemplateIsNotAMemberOf, this, ti.toChars(context), e1.toChars(context)));
			}
			// goto Lerr;
			return semantic_Lerr(context);
		}

		Assert.isNotNull(s);
		id = ti.name.ident;
		s2 = s.search(filename, lineNumber, id, 0, context);
		if (s2 == null) {
			if (s.ident == null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.TemplateIdentifierIsNotAMemberOfUndefined, this, new String(id), s.kind()));
				}
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.TemplateIdentifierIsNotAMemberOf, this, new String(id), s.kind(), new String(s.ident.ident)));
				}
			}
			// goto Lerr;
			return semantic_Lerr(context);
		}
		s = s2;
		s.semantic(sc, context);
		s = s.toAlias(context);
		td = s.isTemplateDeclaration();
		if (td == null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolNotATemplate, ti.name, new String(id)));
			}
			// goto Lerr;
			return semantic_Lerr(context);
		}
		if (context.global.errors > 0) {
			// goto Lerr;
			return new IntegerExp(filename, lineNumber, 0);
		}

		ti.tempdecl = td;

		if (eleft != null) {
			Declaration v;

			ti.semantic(sc, context);
			s = ti.inst.toAlias(context);
			v = s.isDeclaration();
			if (v != null) {
				e = new DotVarExp(filename, lineNumber, eleft, v);
				e = e.semantic(sc, context);
				return e;
			}
		}

		e = new ScopeExp(filename, lineNumber, ti);
		if (eleft != null) {
			e = new DotExp(filename, lineNumber, eleft, e);
		}
		e = e.semantic(sc, context);
		return e;

		// Lerr: return new IntegerExp(0);
	}

	private Expression semantic_Lerr(SemanticContext context) {
		if (context.isD1()) {
			return new IntegerExp(filename, lineNumber, 0);
		} else {
			return new ErrorExp();
		}
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		DotTemplateInstanceExp de = new DotTemplateInstanceExp(filename, lineNumber, e1
				.syntaxCopy(context), (TemplateInstance) ti.syntaxCopy(null, context));
		return de;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		expToCBuffer(buf, hgs, e1, PREC.PREC_primary, context);
		buf.writeByte('.');
		ti.toCBuffer(buf, hgs, context);
	}

}
