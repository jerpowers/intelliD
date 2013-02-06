package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHconvert;
import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.TY.Taarray;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tdelegate;
import static descent.internal.compiler.parser.TY.Tpointer;
import static descent.internal.compiler.parser.TY.Ttypedef;
import static descent.internal.compiler.parser.TY.Tvoid;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class NullExp extends Expression {

	public boolean committed; // !=0 if type is committed

	public NullExp(char[] filename, int lineNumber) {
		super(filename, lineNumber, TOK.TOKnull);
		this.committed = false;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public Expression castTo(Scope sc, Type t, SemanticContext context) {
		NullExp e;
		Type tb;

		if (same(type, t, context)) {
			committed = true;
			return this;
		}
		e = (NullExp) copy();
		e.committed = true;
		tb = t.toBasetype(context);
		e.type = type.toBasetype(context);
		if (!same(tb, e.type, context)) {
			// NULL implicitly converts to any pointer type or dynamic array
			if (e.type.ty == Tpointer
					&& e.type.nextOf().ty == Tvoid
					&& (tb.ty == Tpointer || tb.ty == Tarray
							|| tb.ty == Taarray || tb.ty == Tdelegate)) {
			} else {
				return e.Expression_castTo(sc, t, context);
			}
		}
		e.type = t;
		return e;
	}

	@Override
	public int getNodeType() {
		return NULL_EXP;
	}

	@Override
	public MATCH implicitConvTo(Type t, SemanticContext context) {
		// TODO Descent this check is not in DMD, see why it can be null here (bug in Descent, probably)
		if (type == null) {
			type = Type.tvoid.pointerTo(context);
		}

		if (this.type.equals(t)) {
			return MATCHexact;
		}
		
		if (context.isD2()) {
		    /* Allow implicit conversions from invariant to mutable|const,
		     * and mutable to invariant. It works because, after all, a null
		     * doesn't actually point to anything.
		     */
		    if (t.invariantOf(context).equals(type.invariantOf(context))) {
		    	return MATCHconst;
		    }
		}
		
		// NULL implicitly converts to any pointer type or dynamic array
		if (type.ty == Tpointer && type.nextOf().ty == Tvoid) {
			if (t.ty == Ttypedef) {
				t = ((TypeTypedef) t).sym.basetype;
			}
			if (t.ty == Tpointer || t.ty == Tarray || t.ty == Taarray
					|| t.ty == Tclass || t.ty == Tdelegate) {
				return committed ? MATCHconvert : MATCHexact;
			}
		}
		return super.implicitConvTo(t, context);
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return this;
	}

	@Override
	public boolean isBool(boolean result) {
		return result ? false : true;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		// NULL is the same as (void *)0
		if (type == null) {
			type = Type.tvoid.pointerTo(context);
		}
		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("null");
	}

	@Override
	public void toMangleBuffer(OutBuffer buf, SemanticContext context) {
		buf.writeByte('n');
	}
}
