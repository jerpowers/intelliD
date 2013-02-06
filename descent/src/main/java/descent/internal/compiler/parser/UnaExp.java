package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKarray;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tstruct;
import descent.internal.compiler.parser.Constfold.UnaExp_fp;


public abstract class UnaExp extends Expression {

	public Expression e1;
	public Expression sourceE1;

	public UnaExp(char[] filename, int lineNumber, TOK op, Expression e1) {
		super(filename, lineNumber, op);
		this.e1 = e1;
		this.sourceE1 = e1;
	}
	
	@Override
	public boolean canThrow(SemanticContext context) {
		return e1.canThrow(context);
	}

	public final Expression interpretCommon(InterState istate,
			UnaExp_fp fp, SemanticContext context) {
		Expression e;
		Expression e1;

		e1 = this.e1.interpret(istate, context);
		if (e1 == EXP_CANT_INTERPRET) {
			// goto Lcant;
			return EXP_CANT_INTERPRET;
		}
		if (!e1.isConst()) {
			// goto Lcant;
			return EXP_CANT_INTERPRET;
		}

		e = fp.call(type, e1, context);
		return e;

		// Lcant:
		// 	return EXP_CANT_INTERPRET;
	}

	public Expression op_overload(Scope sc, SemanticContext context) {
		AggregateDeclaration ad = null;
		Dsymbol fd;
		Type t1 = e1.type.toBasetype(context);

		if (t1.ty == Tclass) {
			ad = ((TypeClass) t1).sym;
		} else if (t1.ty == Tstruct) {
			ad = ((TypeStruct) t1).sym;
		} else {
			return null;
		}

		fd = search_function(ad, opId(context), context);
		if (fd != null) {
			if (op == TOKarray) {
				/* Rewrite op e1[arguments] as:
				 *    e1.fd(arguments)
				 */
				Expression e = new DotIdExp(filename, lineNumber, e1, fd.ident);
				ArrayExp ae = (ArrayExp) this;				
				e = new CallExp(filename, lineNumber, e, ae.arguments);
				e = e.semantic(sc, context);
				return e;
			} else {
				// Rewrite +e1 as e1.add()
				return build_overload(filename, lineNumber, sc, e1, null, fd.ident, context);
			}
		}
		
		if (!context.isD1()) {
			// Didn't find it. Forward to aliasthis
			if (ad.aliasthis != null) {
				/*
				 * Rewrite op(e1) as: op(e1.aliasthis)
				 */
				Expression e1 = new DotIdExp(filename, lineNumber, this.e1, ad.aliasthis.ident);
				Expression e = copy();
				((UnaExp) e).e1 = e1;
				e = e.semantic(sc, context);
				return e;
			}
		}
		
		return null;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		e1 = e1.optimize(result, context);
		return this;
	}

	@Override
	public void scanForNestedRef(Scope sc, SemanticContext context) {
		e1.scanForNestedRef(sc, context);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		e1 = e1.semantic(sc, context);
		
		// Descent: for binding resolution
		sourceE1.setResolvedExpression(e1, context);
		
		return this;
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		UnaExp e;

		e = (UnaExp) copy();
		e.type = null;
		e.e1 = e.e1.syntaxCopy(context);
		return e;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring(op.toString());
		expToCBuffer(buf, hgs, e1, op.precedence, context);
	}
}
