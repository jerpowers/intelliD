package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCauto;
import static descent.internal.compiler.parser.STC.STCgshared;
import static descent.internal.compiler.parser.STC.STCscope;
import static descent.internal.compiler.parser.STC.STCstatic;
import static descent.internal.compiler.parser.STC.STCtls;
import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.core.IType__Marker;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class AnonDeclaration extends AttribDeclaration {

	public boolean isunion;
	public Scope scope; // !=NULL means context to use
	public int sem; // 1 if successful semantic()

	private IType__Marker javaElement;

	public AnonDeclaration(char[] filename, int lineNumber, boolean isunion, Dsymbols decl) {
		super(decl);
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.isunion = isunion;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, decl);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return ANON_DECLARATION;
	}

	@Override
	public AttribDeclaration isAttribDeclaration() {
		return this;
	}

	@Override
	public String kind() {
		return isunion ? "anonymous union" : "anonymous struct";
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		Scope scx = null;
		if (scope != null) {
			sc = scope;
			scx = scope;
			scope = null;
		}

		Assert.isNotNull(sc.parent);

		Dsymbol parent = sc.parent.pastMixin();
		AggregateDeclaration ad = parent.isAggregateDeclaration();

		if (ad == null
				|| (ad.isStructDeclaration() == null && ad.isClassDeclaration() == null)) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeErrorLoc(
						IProblem.AnonCanOnlyBePartOfAnAggregate, this));
			}
			return;
		}

		if (decl != null) {
			AnonymousAggregateDeclaration aad = new AnonymousAggregateDeclaration(
					filename, lineNumber);
			boolean adisunion;

			if (sc.anonAgg != null) {
				ad = sc.anonAgg;
				adisunion = sc.inunion;
			} else {
				adisunion = ad.isUnionDeclaration() != null;
			}

			sc = sc.push();

			sc.stc &= ~(STCauto | STCscope | STCstatic | STCtls | STCgshared);
			sc.inunion = isunion;
			sc.offset = 0;
			sc.flags = 0;
			aad.structalign = sc.structalign;
			aad.parent = ad;

			for (int i = 0; i < decl.size(); i++) {
				Dsymbol s = decl.get(i);

				s.semantic(sc, context);
				if (isunion) {
					sc.offset = 0;
				}
				if (aad.sizeok == 2) {
					break;
				}
			}
			sc = sc.pop();

			// If failed due to forward references, unwind and try again later
			if (aad.sizeok == 2) {
				ad.sizeok = 2;
				if (sc.anonAgg == null) {
					scope = scx != null ? scx : new Scope(sc);
					scope.setNoFree();
					scope.module.addDeferredSemantic(this, context);
				}
				return;
			}
			if (sem == 0) {
				context.Module_dprogress++;
				sem = 1;
			} else {
				;
			}

			// 0 sized structs are set to 1 byte
			if (aad.structsize == 0) {
				aad.structsize = 1;
				aad.alignsize = 1;
			}

			// Align size of anonymous aggregate
			int[] sc_offset = { sc.offset };
			ad.alignmember(aad.structalign, aad.alignsize, sc_offset);
			sc.offset = sc_offset[0];
			// ad.structsize = sc.offset;

			// Add members of aad to ad
			for (int i = 0; i < aad.fields.size(); i++) {
				VarDeclaration v = aad.fields.get(i);

				v.offset(v.offset() + sc.offset);
				ad.fields.add(v);
			}

			// Add size of aad to ad
			if (adisunion) {
				if (aad.structsize > ad.structsize) {
					ad.structsize = aad.structsize;
				}
				sc.offset = 0;
			} else {
				ad.structsize = sc.offset + aad.structsize;
				sc.offset = ad.structsize;
			}

			if (ad.alignsize < aad.alignsize) {
				ad.alignsize = aad.alignsize;
			}

			sc.anonAgg = aad;
		}
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		AnonDeclaration ad;

		Assert.isTrue(s == null);
		ad = context.newAnonDeclaration(filename, lineNumber, isunion, Dsymbol.arraySyntaxCopy(decl, context));
		ad.javaElement = javaElement;
		return ad;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring(isunion ? "union" : "struct");
		buf.writestring("\n{\n");
		if (decl != null) {
			for (Dsymbol s : decl) {
				s.toCBuffer(buf, hgs, context);
			}
		}
		buf.writestring("}\n");
	}

	@Override
	public int getErrorStart() {
		return start;
	}

	@Override
	public int getErrorLength() {
		if (isunion) {
			return 5; // "union".length()
		} else {
			return 6; // "struct".length()
		}
	}

	@Override
	public int getLineNumber() {
		return lineNumber;
	}

	@Override
	public String getSignature(int options) {
		return parent.getSignature(options);
	}

	public void setJavaElement(IType__Marker javaElement) {
		this.javaElement = javaElement;
	}

	@Override
	public IType__Marker getJavaElement() {
		return javaElement;
	}

}
