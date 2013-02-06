package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.TY.Tdelegate;
import static descent.internal.compiler.parser.TY.Tfunction;

public abstract class TypeNext extends Type {
	
	public Type next, sourceNext;

	public TypeNext(TY ty, Type next) {
		super(ty);
		this.next = this.sourceNext = next;
	}
	
	@Override
	public void checkDeprecated(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
	    super.checkDeprecated(filename, lineNumber, sc, context);
	    next.checkDeprecated(filename, lineNumber, sc, context);
	}
	
	@Override
	public MATCH constConv(Type to, SemanticContext context) {
		MATCH m = super.constConv(to, context);

		if (m == MATCHconst && next.constConv(((TypeNext) to).next, context) == MATCHnomatch)
			m = MATCHnomatch;
		return m;
	}
	
	@Override
	public Type makeConst(int startPosition, int length, IStringTableHolder context) {
		if (cto != null) {
			assert (cto.mod == MODconst);
			return cto;
		}    
	    TypeNext t = (TypeNext)super.makeConst(startPosition, length, context);
		if (ty != Tfunction && ty != Tdelegate && next.deco != null && !next.isInvariant() && !next.isConst()) {
			if (next.isShared())
				t.next = next.sharedConstOf(context);
			else
				t.next = next.constOf(context);
		}
		return t;
	}
	
	@Override
	public Type makeInvariant(int startPosition, int length, IStringTableHolder context) {
		if (ito != null) {
			assert (ito.isInvariant());
			return ito;
		}
		TypeNext t = (TypeNext) super.makeInvariant(startPosition, length, context);
		if (ty != Tfunction && ty != Tdelegate && next.deco != null && !next.isInvariant()) {
			t.next = next.invariantOf(context);
		}
		return t;
	}
	
	@Override
	public Type makeShared(IStringTableHolder context) {
		if (sto != null) {
			assert (sto.mod == MODshared);
			return sto;
		}
		TypeNext t = (TypeNext) super.makeShared(context);
		if (ty != Tfunction && ty != Tdelegate && next.deco != null && !next.isInvariant() && !next.isShared()) {
			if (next.isConst())
				t.next = next.sharedConstOf(context);
			else
				t.next = next.sharedOf(context);
		}
		return t;
	}
	
	@Override
	public Type makeSharedConst(IStringTableHolder context) {
		if (scto != null) {
			assert (scto.mod == (MODshared | MODconst));
			return scto;
		}
		TypeNext t = (TypeNext) super.makeSharedConst(context);
		if (ty != Tfunction && ty != Tdelegate && next.deco != null && !next.isInvariant() && !next.isSharedConst()) {
			t.next = next.sharedConstOf(context);
		}
		return t;
	}
	
	@Override
	public final Type nextOf() {
		return next;
	}
	
	@Override
	public Type reliesOnTident() {
	    return next.reliesOnTident();
	}
	
	@Override
	public void toDecoBuffer(OutBuffer buf, int flag, SemanticContext context) {
	    Type_toDecoBuffer(buf, flag, context);
	    assert(next != this);
	    next.toDecoBuffer(buf, (flag & 0x100) != 0 ? 0 : mod, context);
	}
	
	public void transitive(SemanticContext context) {
		/*
		 * Invoke transitivity of type attributes
		 */
		next = next.addMod(mod, context);
	}

}
