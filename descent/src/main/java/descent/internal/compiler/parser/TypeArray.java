package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.TY.Tbit;
import static descent.internal.compiler.parser.TY.Tchar;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Twchar;
import descent.core.compiler.IProblem;

public abstract class TypeArray extends TypeNext {

	private final static char[][] name1 = { "_adReverseChar".toCharArray(),
			"_adReverseWchar".toCharArray() };
	private final static char[][] name2 = { "_adSortChar".toCharArray(),
			"_adSortWchar".toCharArray() };
	private final static char[][] name3 = { "_adSortBit".toCharArray(),
			"_adSort".toCharArray() };

	public TypeArray(TY ty, Type next) {
		super(ty, next);
	}

	@Override
	public Expression dotExp(Scope sc, Expression e, IdentifierExp ident,
			SemanticContext context) {
		Type n = this.next.toBasetype(context); // uncover any typedef's

		if (equals(ident, Id.reverse)
				&& (n.ty == Tchar || n.ty == Twchar)) {
			Expression ec;
			FuncDeclaration fd;
			Expressions arguments;
			char[] nm;

			nm = name1[n.ty == Twchar ? 1 : 0];
			fd = context.genCfunc(Type.tindex, nm);
			ec = new VarExp(null, 0, fd);
			e = e.castTo(sc, n.arrayOf(context), context); // convert to dynamic array
			arguments = new Expressions(1);
			arguments.add(e);
			e = new CallExp(e.filename, e.lineNumber,  ec, arguments);
			e.type = next.arrayOf(context);
		} else if (equals(ident, Id.sort)
				&& (n.ty == Tchar || n.ty == Twchar)) {
			Expression ec;
			FuncDeclaration fd;
			Expressions arguments;
			char[] nm;

			nm = name2[n.ty == Twchar ? 1 : 0];
			fd = context.genCfunc(Type.tindex, nm);
			ec = new VarExp(null, 0, fd);
			e = e.castTo(sc, n.arrayOf(context), context); // convert to dynamic array
			arguments = new Expressions(1);
			arguments.add(e);
			e = new CallExp(e.filename, e.lineNumber,  ec, arguments);
			e.type = next.arrayOf(context);
		} else if (equals(ident, Id.reverse)
				|| equals(ident, Id.dup)
				|| (context.isD2() && equals(ident, Id.idup))) {
			Expression ec;
			FuncDeclaration fd;
			Expressions arguments;
			int size = next.size(e.filename, e.lineNumber,  context);
			boolean dup;

			if (size == 0) {
				throw new IllegalStateException("assert(size);");
			}

			dup = equals(ident, Id.dup) || (context.isD2() && equals(ident, Id.idup));
			fd = context.genCfunc(Type.tindex, dup ? Id.adDup : Id.adReverse);
			ec = new VarExp(null, 0, fd);
			e = e.castTo(sc, n.arrayOf(context), context); // convert to dynamic array
			arguments = new Expressions(3);
			if (dup) {
				arguments.add(getTypeInfo(sc, context));
			}
			arguments.add(e);
			if (!dup) {
				arguments.add(new IntegerExp(null, 0, size, Type.tsize_t));
			}
			e = new CallExp(e.filename, e.lineNumber,  ec, arguments);
			if (context.isD1()) {
				e.type = next.arrayOf(context);
			} else {
				if (equals(ident, Id.idup)) {
					Type einv = next.invariantOf(context);
					if (next.implicitConvTo(einv, context).ordinal() < MATCHconst.ordinal()) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotImplicitlyConvertToImmutable, e, next.toChars(context)));
						}
					}
					e.type = einv.arrayOf(context);
				} else
					e.type = next.mutableOf(context).arrayOf(context);
			}
		} else if (equals(ident, Id.sort)) {
			Expression ec;
			FuncDeclaration fd;
			Expressions arguments;

			if (context.isD1()) {
				fd = context.genCfunc(tint32.arrayOf(context), (n.ty == Tbit ? name3[0] : name3[1]));
			} else {
				fd = context.genCfunc(tint32.arrayOf(context), name3[1]);
			}
			ec = new VarExp(null, 0, fd);
			e = e.castTo(sc, n.arrayOf(context), context);	// convert to dynamic array
			arguments = new Expressions();
			arguments.add(e);
			if (next.ty != Tbit) {
				arguments.add(n.ty == Tsarray ? n.getTypeInfo(sc, context) // don't convert to dynamic array
						: n.getInternalTypeInfo(sc, context));
			}
			e = new CallExp(e.filename, e.lineNumber,  ec, arguments);
			e.type = next.arrayOf(context);
		} else {
			e = super.dotExp(sc, e, ident, context);
		}
		return e;
	}
}
