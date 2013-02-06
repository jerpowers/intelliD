package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.Constfold.ArrayLength;
import static descent.internal.compiler.parser.Constfold.Index;
import static descent.internal.compiler.parser.TOK.TOKarrayliteral;
import static descent.internal.compiler.parser.TOK.TOKstring;

import java.math.BigInteger;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class IndexExp extends BinExp {

	public VarDeclaration lengthVar;
	public int modifiable;

	public IndexExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKindex, e1, e2);
		// THey should implicitly get these values in Java, but this way
		// prettier.
		lengthVar = null;
		modifiable = 0;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return INDEX_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		Expression e;
		Expression e1;
		Expression e2;

		e1 = this.e1.interpret(istate, context);
		if (e1 == EXP_CANT_INTERPRET) {
			// goto Lcant;
		}

		if (e1.op == TOKstring || e1.op == TOKarrayliteral) {
			/* Set the $ variable
			 */
			e = ArrayLength.call(Type.tsize_t, e1, context);
			if (e == EXP_CANT_INTERPRET) {
				// goto Lcant;
			}
			if (lengthVar != null) {
				lengthVar.value = e;
			}
		}

		e2 = this.e2.interpret(istate, context);
		if (e2 == EXP_CANT_INTERPRET) {
			// goto Lcant;
		}
		return Index.call(type, e1, e2, context);
	}
	
	@Override
	public boolean isLvalue(SemanticContext context) {
		return true;
	}

	@Override
	public Expression modifiableLvalue(Scope sc, Expression e,
			SemanticContext context) {
		modifiable = 1;
		if (e1.op == TOK.TOKstring) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.StringLiteralsAreImmutable, this));
			}
		}
		if (!context.isD1()) {
		    if (type != null && !type.isMutable()) {
		    	if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.SymbolIsNotMutable, this, e.toChars(context)));
				}
		    }
		}
		if (e1.type.toBasetype(context).ty == TY.Taarray) {
			e1 = e1.modifiableLvalue(sc, e1, context);
		}
		return toLvalue(sc, e, context);
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		Expression e;

		Expression e1 = this.e1.optimize(WANTvalue | (result & WANTinterpret),
				context);
		if (context.isD1()) {
			if ((result & WANTinterpret) != 0) {
				e1 = fromConstInitializer(e1, context);
			}
		} else {
		    e1 = fromConstInitializer(result, e1, context);
		}
		e2 = e2.optimize(WANTvalue | (result & WANTinterpret), context);
		e = Index.call(type, e1, e2, context);
		if (e == EXP_CANT_INTERPRET) {
			e = this;
		}
		return e;
	}

	@Override
	public void scanForNestedRef(Scope sc, SemanticContext context) {
		e1.scanForNestedRef(sc, context);

		if (lengthVar != null) {
			lengthVar.parent = sc.parent;
		}
		e2.scanForNestedRef(sc, context);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {

		Expression e;
		//BinExp b;
		//UnaExp u;
		Type t1;
		ScopeDsymbol sym;

		if (null != type) {
			return this;
		}

		if (null == e1.type) {
			e1 = e1.semantic(sc, context);
		}
		e = this;

		t1 = e1.type.toBasetype(context);

		if (t1.ty == TY.Tsarray || t1.ty == TY.Tarray || t1.ty == TY.Ttuple) {
			// Create scope for 'length' variable
			sym = new ArrayScopeSymbol(sc, this);
			sym.filename = filename;
			sym.lineNumber = lineNumber;
			sym.parent = sc.scopesym;
			sc = sc.push(sym);
		}

		e2 = e2.semantic(sc, context);
		if (null == e2.type) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.SymbolHasNoValue, e2,
						new String[] { e2.toChars(context) }));
			}
			e2.type = Type.terror;
		}
		e2 = resolveProperties(sc, e2, context);

		if (t1.ty == TY.Tsarray || t1.ty == TY.Tarray || t1.ty == TY.Ttuple) {
			sc = sc.pop();
		}

		switch (t1.ty) {
		case Tpointer:
		case Tarray: {
			e2 = e2.implicitCastTo(sc, Type.tsize_t, context);
			e.type = t1.nextOf();
			break;
		}

		case Tsarray: {
			e2 = e2.implicitCastTo(sc, Type.tsize_t, context);

			//TypeSArray tsa = (TypeSArray) t1;
			e.type = t1.nextOf();
			break;
		}

		case Taarray: {
			TypeAArray taa = (TypeAArray) t1;

			if (context.isD1()) {
				e2 = e2.implicitCastTo(sc, taa.index, context); // type checking
				e2 = e2.implicitCastTo(sc, taa.key, context); // actual argument type
			} else {
			    if (!arrayTypeCompatible(e2.filename, e2.lineNumber, e2.type,
						taa.index, context)) {
					e2 = e2.implicitCastTo(sc, taa.index, context); // type checking
				}
			}
			type = taa.next;
			break;
		}

		case Ttuple: {
			e2 = e2.implicitCastTo(sc, Type.tsize_t, context);
			if (context.isD1()) {
				e2 = e2.optimize(WANTvalue, context);
			} else {
				e2 = e2.optimize(WANTvalue | WANTinterpret, context);
			}
			integer_t index = e2.toUInteger(context);
			BigInteger length = null;
			TupleExp te = null;
			TypeTuple tup = null;

			if (e1.op == TOK.TOKtuple) {
				te = (TupleExp) e1;
				length = BigInteger.valueOf(te.exps.size());
			}

			else if (e1.op == TOK.TOKtype) {
				tup = (TypeTuple) t1;
				length = BigInteger.valueOf(Argument
						.dim(tup.arguments, context));
			} else {
				assert (false);
			}

			if (index.longValue() < length.longValue()) {
				if (e1.op == TOK.TOKtuple) {
					e = te.exps.get((int) index.longValue());
				} else {
					e = new TypeExp(e1.filename, e1.lineNumber, Argument.getNth(tup.arguments,
							(int) index.longValue(), context).type);
				}
			}

			else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ArrayIndexOutOfBounds, this, new String[] { index.toString(),
									length.toString() }));
				}
				e = e1;
			}
			break;
		}

		default: {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.SymbolMustBeAnArrayOfPointerType, this, new String[] { e1.toChars(context),
								e1.type.toChars(context) }));
			}
			type = Type.tint32;
			break;
		}

		}
		return e;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		expToCBuffer(buf, hgs, e1, PREC.PREC_primary, context);
		buf.writeByte('[');
		expToCBuffer(buf, hgs, e2, PREC.PREC_assign, context);
		buf.writeByte(']');
	}

	@Override
	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
		return this;
	}

}
