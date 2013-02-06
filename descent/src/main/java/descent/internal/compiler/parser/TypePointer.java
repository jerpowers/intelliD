package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHconvert;
import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.TY.Tfunction;
import static descent.internal.compiler.parser.TY.Tpointer;
import static descent.internal.compiler.parser.TY.Tvoid;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TypePointer extends TypeNext {

	public TypePointer(Type next) {
		super(TY.Tpointer, next);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceNext);
		}
		visitor.endVisit(this);
	}

	@Override
	public Expression defaultInit(char[] filename, int lineNumber, SemanticContext context) {
		Expression e;
		e = new NullExp(filename, lineNumber);
		e.type = this;
		return e;
	}

	@Override
	public int getNodeType() {
		return TYPE_POINTER;
	}

	@Override
	public TypeInfoDeclaration getTypeInfoDeclaration(SemanticContext context) {
		return new TypeInfoPointerDeclaration(this, context);
	}

	@Override
	public boolean hasPointers(SemanticContext context) {
		return true;
	}

	@Override
	public MATCH implicitConvTo(Type to, SemanticContext context) {
		if (context.isD1()) {
			if (same(this, to, context)) {
				return MATCHexact;
			}
			if (to.ty == Tpointer && to.nextOf() != null) {
				if (to.nextOf().ty == Tvoid) {
					return MATCHconvert;
				}
	
				if (next.ty == Tfunction && to.nextOf().ty == Tfunction) {
					TypeFunction tf;
					TypeFunction tfto;
	
					tf = (TypeFunction) next;
					tfto = (TypeFunction) to.nextOf();
					return tfto.equals(tf) ? MATCHexact : MATCHnomatch;
				}
			}
			return MATCHnomatch;
		} else {
			if (equals(to))
				return MATCHexact;
			if (to.ty == Tpointer) {
				TypePointer tp = (TypePointer) to;

				if (!(next.mod == tp.next.mod || tp.next.mod == MODconst))
					return MATCHnomatch; // not const-compatible

				/*
				 * Alloc conversion to void[]
				 */
				if (next.ty != Tvoid && tp.next.ty == Tvoid) {
					return MATCHconvert;
				}

				MATCH m = next.constConv(tp.next, context);
				if (m != MATCHnomatch) {
					if (m == MATCHexact && mod != to.mod)
						m = MATCHconst;
					return m;
				}

				/*
				 * Conversion of ptr to derived to ptr to base
				 */
				int[] offset = { 0 };
				if (tp.next.isBaseOf(next, offset, context) && offset[0] == 0)
					return MATCHconvert;
			}
		    return MATCHnomatch;
		}
	}

	@Override
	public boolean isscalar(SemanticContext context) {
		return true;
	}

	@Override
	public boolean isZeroInit(char[] filename, int lineNumber, SemanticContext context) {
		return true;
	}

	@Override
	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		Type n = next.semantic(filename, lineNumber, sc, context);
		switch (n.toBasetype(context).ty) {
		case Ttuple:
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.CannotHavePointerToSymbol, this, new String[] { n.toChars(context) }));
			}
			n = tint32;
			break;
		}
		if (n != next) {
			deco = null;
		}
		next = n;
		
		if (context.isD2()) {
			transitive(context);
		}
		
		return merge(context);
	}

	@Override
	public int size(char[] filename, int lineNumber, SemanticContext context) {
		return PTRSIZE;
	}

	@Override
	public Type syntaxCopy(SemanticContext context) {
		Type t = next.syntaxCopy(context);
		if (same(t, next, context)) {
			t = this;
		} else {
			t = new TypePointer(t);
			t.mod = mod;
			t.copySourceRange(this);
		}
		return t;
	}

	@Override
	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod,
			SemanticContext context) {
		if (mod != this.mod) {
			toCBuffer3(buf, hgs, mod, context);
			return;
		}
		next.toCBuffer2(buf, hgs, this.mod, context);
		if (next.ty != Tfunction)
			buf.writeByte('*');
	}
	
	@Override
	protected void appendSignature0(StringBuilder sb, int options) {
		sb.append('P');
		next.appendSignature(sb, options);
	}

}
