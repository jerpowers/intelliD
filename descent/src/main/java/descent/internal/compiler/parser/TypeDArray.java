package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHconvert;
import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tchar;
import static descent.internal.compiler.parser.TY.Tdchar;
import static descent.internal.compiler.parser.TY.Tpointer;
import static descent.internal.compiler.parser.TY.Tvoid;
import static descent.internal.compiler.parser.TY.Twchar;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TypeDArray extends TypeArray {

	public TypeDArray(Type next) {
		super(TY.Tarray, next);
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
	public int alignsize(SemanticContext context) {
		return PTRSIZE;
	}

	@Override
	public boolean builtinTypeInfo(SemanticContext context) {
		if (context.isD2()) {
			return 0 == mod && next.isTypeBasic() != null && 0 == next.mod;	
		} else {
			return next.isTypeBasic() != null;
		}
	}

	@Override
	public boolean checkBoolean(SemanticContext context) {
		return true;
	}

	@Override
	public Expression defaultInit(char[] filename, int lineNumber, SemanticContext context) {
		Expression e;
		e = new NullExp(filename, lineNumber);
		e.type = this;
		return e;
	}

	@Override
	public Expression dotExp(Scope sc, Expression e, IdentifierExp ident,
			SemanticContext context) {
		Expression oe = e;
		
		if (equals(ident, Id.length)) {
			if (e.op == TOKstring) {
				StringExp se = (StringExp) e;
				e = new IntegerExp(se.filename, se.lineNumber, se.len, Type.tindex);
			} else {
				e = new ArrayLengthExp(e.filename, e.lineNumber,  e);
				e.type = Type.tsize_t;
			}
		} else if (equals(ident, Id.ptr)) {
			e = e.castTo(sc, next.pointerTo(context), context);
		} else {
			e = super.dotExp(sc, e, ident, context);
		}
		e.start = oe.start;
		e.length = ident.start + ident.length - oe.start;
		return e;
	}

	@Override
	public int getNodeType() {
		return TYPE_D_ARRAY;
	}

	@Override
	public TypeInfoDeclaration getTypeInfoDeclaration(SemanticContext context) {
		return new TypeInfoArrayDeclaration(this, context);
	}

	@Override
	public boolean hasPointers(SemanticContext context) {
		return true;
	}

	@Override
	public MATCH implicitConvTo(Type to, SemanticContext context) {
		// Allow implicit conversion of array to pointer
		if (context.isD1()) {
			if (context.IMPLICIT_ARRAY_TO_PTR()
					&& to.ty == Tpointer
					&& (to.nextOf().ty == Tvoid || next.equals(to.nextOf()) /*|| to.next.isBaseOf(next)*/)) {
				return MATCHconvert;
			}
	
			if (to.ty == Tarray) {
				int[] offset = { 0 };
	
				if ((to.nextOf().isBaseOf(next, offset, context) && offset[0] == 0)
						|| to.nextOf().ty == Tvoid) {
					return MATCHconvert;
				}
			}
			return super.implicitConvTo(to, context);
		} else {
			if (equals(to))
				return MATCHexact;

			// Allow implicit conversion of array to pointer
			if (context.IMPLICIT_ARRAY_TO_PTR() && to.ty == Tpointer) {
				TypePointer tp = (TypePointer) to;

				/*
				 * Allow conversion to void*
				 */
				if (tp.next.ty == Tvoid && (next.mod == tp.next.mod || tp.next.mod == MODconst)) {
					return MATCHconvert;
				}

				return next.constConv(to, context);
			}

			if (to.ty == Tarray) {
				int[] offset = { 0 };
				TypeDArray ta = (TypeDArray) to;

				if (!(next.mod == ta.next.mod || ta.next.mod == MODconst))
					return MATCHnomatch; // not const-compatible

				/*
				 * Allow conversion to void[]
				 */
				if (next.ty != Tvoid && ta.next.ty == Tvoid) {
					return MATCHconvert;
				}

				MATCH m = next.constConv(ta.next, context);
				if (m != MATCHnomatch) {
					if (m == MATCHexact && mod != to.mod)
						m = MATCHconst;
					return m;
				}

				/*
				 * Allow conversions of T[][] to const(T)[][]
				 */
				if (mod == ta.mod && next.ty == Tarray && ta.next.ty == Tarray) {
					m = next.implicitConvTo(ta.next, context);
					if (m == MATCHconst)
						return m;
				}

				/*
				 * Conversion of array of derived to array of base
				 */
				if (ta.next.isBaseOf(next, offset, context) && offset[0] == 0)
					return MATCHconvert;
			}
			return super.implicitConvTo(to, context);
		}
	}

	@Override
	public boolean isString(SemanticContext context) {
		TY nty = next.toBasetype(context).ty;
		return nty == Tchar || nty == Twchar || nty == Tdchar;
	}

	@Override
	public boolean isZeroInit(char[] filename, int lineNumber, SemanticContext context) {
		return true;
	}

	@Override
	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		Type tn = next;

		tn = next.semantic(filename, lineNumber, sc, context);
		Type tbn = tn.toBasetype(context);
		switch (tbn.ty) {
		case Tfunction:
		case Tnone:
		case Ttuple:
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotHaveArrayOfType, this, new String[] { tbn.toChars(context) }));
			}
			tn = next = tint32;
			break;
		case Tstruct:
			if (context.isD2()) {
				TypeStruct ts = (TypeStruct)tbn;
			    if (ts.sym.isnested) {
			    	if (context.acceptsErrors()) {
			    		context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotHaveArrayOfType, this, "inner structs " + ts.toChars(context)));
			    	}
			    }
			    break;
			}
			break;
		}
		if (tn.isauto()) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.CannotHaveArrayOfAuto, this, new String[] { tn.toChars(context) }));
			}
		}
		if (context.isD1()) {
			if (next != tn) {
				//deco = NULL;			// redo
				return tn.arrayOf(context);
			}
		} else {
		    next = tn;
		    transitive(context);
		}
		return merge(context);
	}

	@Override
	public int size(char[] filename, int lineNumber, SemanticContext context) {
		return PTRSIZE * 2;
	}

	@Override
	public Type syntaxCopy(SemanticContext context) {
		Type t = next.syntaxCopy(context);
		if (same(t, next, context)) {
			t = this;
		} else {
			t = new TypeDArray(t);
			t.mod = mod;
			t.copySourceRange(this);
		}
		return t;
	}

	@Override
	public void toDecoBuffer(OutBuffer buf, int flag, SemanticContext context) {
		Type_toDecoBuffer(buf, flag, context);
		if (next != null) {
			next.toDecoBuffer(buf, (flag & 0x100) != 0 ? 0 : mod, context);
		}
	}
	
	@Override
	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod,
			SemanticContext context) {
		if (mod != this.mod) {
			toCBuffer3(buf, hgs, mod, context);
			return;
		}
		next.toCBuffer2(buf, hgs, this.mod, context);
		buf.writestring("[]");
	}
	
	@Override
	protected void appendSignature0(StringBuilder sb, int options) {
		sb.append('A');
		next.appendSignature(sb, options);
	}

}
