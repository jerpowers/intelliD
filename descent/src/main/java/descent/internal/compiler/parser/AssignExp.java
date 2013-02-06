package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKadd;
import static descent.internal.compiler.parser.TOK.TOKand;
import static descent.internal.compiler.parser.TOK.TOKassign;
import static descent.internal.compiler.parser.TOK.TOKcomma;
import static descent.internal.compiler.parser.TOK.TOKconstruct;
import static descent.internal.compiler.parser.TOK.TOKdiv;
import static descent.internal.compiler.parser.TOK.TOKdottd;
import static descent.internal.compiler.parser.TOK.TOKmin;
import static descent.internal.compiler.parser.TOK.TOKmod;
import static descent.internal.compiler.parser.TOK.TOKmul;
import static descent.internal.compiler.parser.TOK.TOKneg;
import static descent.internal.compiler.parser.TOK.TOKor;
import static descent.internal.compiler.parser.TOK.TOKquestion;
import static descent.internal.compiler.parser.TOK.TOKslice;
import static descent.internal.compiler.parser.TOK.TOKstar;
import static descent.internal.compiler.parser.TOK.TOKtilde;
import static descent.internal.compiler.parser.TOK.TOKtuple;
import static descent.internal.compiler.parser.TOK.TOKvar;
import static descent.internal.compiler.parser.TOK.TOKxor;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tstruct;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class AssignExp extends BinExp {

	public boolean ismemset;

	public AssignExp(char[] filename, int lineNumber, Expression e1, Expression e2) {
		super(filename, lineNumber, TOK.TOKassign, e1, e2);
		ismemset = false;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceE1);
			TreeVisitor.acceptChildren(visitor, sourceE2);
		}
		visitor.endVisit(this);
	}

	@Override
	public Expression checkToBoolean(SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(IProblem.ExpressionDoesNotGiveABooleanResult, this));
		}
		return this;
	}

	@Override
	public int getNodeType() {
		return ASSIGN_EXP;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		return interpretAssignCommon(istate, null, context);
	}

	@Override
	public char[] opId(SemanticContext context) {
		return Id.assign;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		if (!context.isD1()) {
			if (type != null) {
				return this;
			}

			if (e2.op == TOKcomma) {
				/*
				 * Rewrite to get rid of the comma from rvalue
				 */
				AssignExp ea = new AssignExp(filename, lineNumber, e1, ((CommaExp) e2).e2);
				ea.op = op;
				Expression e = new CommaExp(filename, lineNumber, ((CommaExp) e2).e1, ea);
				return e.semantic(sc, context);
			}
		}
		
		/*
		 * Look for operator overloading of a[i]=value. Do it before semantic()
		 * otherwise the a[i] will have been converted to a.opIndex() already.
		 */
		if (e1.op == TOK.TOKarray) {
			Type t1;
			ArrayExp ae = (ArrayExp) e1;
			AggregateDeclaration ad;
			char[] id = Id.index;

			ae.e1 = ae.e1.semantic(sc, context);
			t1 = ae.e1.type.toBasetype(context);

			if (t1.ty == TY.Tstruct || t1.ty == TY.Tclass) {
				if (t1.ty == TY.Tstruct) {
					ad = ((TypeStruct) t1).sym;
				} else // t1.ty == TY.Tclass
				{
					ad = ((TypeClass) t1).sym;
				}

				// Rewrite (a[i] = value) to (a.opIndexAssign(value, i))
				if (null != search_function(ad, Id.indexass, context)) {
					Expression e = new DotIdExp(filename, lineNumber, ae.e1, Id.indexass);
					Expressions a = new Expressions(ae.arguments);

					a.add(0, e2);
					e = new CallExp(filename, lineNumber, e, a);
					e = e.semantic(sc, context);
					return e;
				} else {
					// Rewrite (a[i] = value) to (a.opIndex(i, value))
					if (null != search_function(ad, id, context)) {
						Expression e = new DotIdExp(filename, lineNumber, ae.e1, id);

						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.OperatorAssignmentOverloadWithOpIndexIllegal, this));
						}

						e = new CallExp(filename, lineNumber, e, ae.arguments.get(0), e2);
						e = e.semantic(sc, context);
						return e;
					}
				}
			}
		}

		/* Look for operator overloading of a[i..j]=value.
		 * Do it before semantic() otherwise the a[i..j] will have been
		 * converted to a.opSlice() already.
		 */
		if (e1.op == TOK.TOKslice) {
			Type t1;
			SliceExp ae = (SliceExp) e1;
			AggregateDeclaration ad;

			ae.e1 = ae.e1.semantic(sc, context);
			ae.e1 = resolveProperties(sc, ae.e1, context);
			t1 = ae.e1.type.toBasetype(context);

			if (t1.ty == TY.Tstruct || t1.ty == TY.Tclass) {
				if (t1.ty == TY.Tstruct) {
					ad = ((TypeStruct) t1).sym;

				} else // t1.ty == TY.Tclass
				{
					ad = ((TypeClass) t1).sym;
				}

				// Rewrite (a[i..j] = value) to (a.opIndexAssign(value, i, j))
				if (null != search_function(ad, Id.sliceass, context)) {
					Expression e = new DotIdExp(filename, lineNumber, ae.e1, Id.sliceass);
					Expressions a = new Expressions(3);

					a.add(e2);
					if (null != ae.lwr) {
						a.add(ae.lwr);
						assert (null != ae.upr);
						a.add(ae.upr);
					} else {
						assert (null == ae.upr);
					}

					e = new CallExp(filename, lineNumber, e, a);
					e = e.semantic(sc, context);
					return e;
				}
			}
		}

		Expression e1old = e1;
		Type t1;

		super.semantic(sc, context);
		
	    if (e1.op == TOKdottd) {	
	    	// Rewrite a.b=e2, when b is a template, as a.b(e2)
	    	Expression e = new CallExp(filename, lineNumber, e1, e2);
	    	e.copySourceRange(e1, e2);
	    	e = e.semantic(sc, context);
	    	return e;
	    }

		
		e2 = resolveProperties(sc, e2, context);
		assert (null != e1.type);
		
	    /* Rewrite tuple assignment as a tuple of assignments.
	     */
	    if (e1.op == TOKtuple && e2.op == TOKtuple)
	    {	TupleExp tup1 = (TupleExp)e1;
		TupleExp tup2 = (TupleExp) e2;
		int dim = size(tup1.exps);
		if (dim != size(tup2.exps))
		{
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.MismatchedTupleLengths, this, String.valueOf(dim), String.valueOf(size(tup2.exps))));
			}
		}
		else
		{   Expressions exps = new Expressions(dim);
		    exps.setDim(dim);

		    for (int i = 0; i < dim; i++)
		    {	
		    	Expression ex1 = (Expression) tup1.exps.get(i);
				Expression ex2 = (Expression) tup2.exps.get(i);
				exps.set(i, new AssignExp(filename, lineNumber, ex1, ex2));
		    }
		    Expression e = new TupleExp(filename, lineNumber, exps);
		    e = e.semantic(sc, context);
		    return e;
		}
	    }


		t1 = e1.type.toBasetype(context);

		if (t1.ty == TY.Tfunction) {
			// Rewrite f=value to f(value)
			Expression e;

			e = new CallExp(filename, lineNumber, e1, e2);
			e = e.semantic(sc, context);
			
			if (e instanceof CallExp) {
				CallExp callExp = (CallExp) e;
				if (callExp.e1 instanceof DotVarExp) {
					DotVarExp dve = (DotVarExp) callExp.e1;
					sourceE1.setResolvedSymbol(dve.var, context);
				} else if (callExp.e1 instanceof VarExp) {
					VarExp dve = (VarExp) callExp.e1;
					sourceE1.setResolvedSymbol(dve.var, context);
				}
			}
			
			return e;
		}
		
		/* If it is an assignment from a 'foreign' type,
		 * check for operator overloading.
		 */
		if (context.isD1()) {
			if (t1.ty == TY.Tclass || t1.ty == TY.Tstruct) {
				if (MATCH.MATCHnomatch == e2.type.implicitConvTo(e1.type, context)) {
					Expression e = op_overload(sc, context);
					if (null != e) {
						return e;
					}
				}
			}
		} else {
			if (context.isD1()) {
				if (t1.ty == TY.Tclass || t1.ty == TY.Tstruct) {
					// Disallow assignment operator overloads for same type
					if (MATCH.MATCHnomatch == e2.type.implicitConvTo(e1.type, context)) {
						Expression e = op_overload(sc, context);
						if (e != null) {
							return e;
						}
					}
				}
			} else {
				if (t1.ty == TY.Tstruct) {
					StructDeclaration sd = ((TypeStruct)t1).sym;
					if (op == TOKassign)
					{
					    Expression e = op_overload(sc, context);
					    if (e != null) {
					    	return e;
					    }
					}
					else if (op == TOKconstruct) {
						Type t2 = e2.type.toBasetype(context);
						if (t2.ty == Tstruct && sd == ((TypeStruct) t2).sym && sd.cpctor != null) {
							/*
							 * We have a copy constructor for this
							 */
							if (e2.op == TOKvar || e2.op == TOKstar) {
								/*
								 * Write as: e1.cpctor(e2);
								 */
								Expression e = new DotVarExp(filename, lineNumber, e1, sd.cpctor, false);
								e = new CallExp(filename, lineNumber, e, e2);
								return e.semantic(sc, context);
							} else if (e2.op == TOKquestion) {
								/*
								 * Write as: a ? e1 = b : e1 = c;
								 */
								CondExp ec = (CondExp) e2;
								AssignExp ea1 = new AssignExp(ec.e1.filename, ec.e1.lineNumber, e1, ec.e1);
								ea1.op = op;
								AssignExp ea2 = new AssignExp(ec.e1.filename, ec.e1.lineNumber, e1, ec.e2);
								ea2.op = op;
								Expression e = new CondExp(filename, lineNumber, ec.econd, ea1, ea2);
								return e.semantic(sc, context);
							}
						}
					}
				} else if (t1.ty == Tclass) { 
					// Disallow assignment operator overloads for same type
					if (MATCH.MATCHnomatch == e2.type.implicitConvTo(e1.type, context)) {
						Expression e = op_overload(sc, context);
						if (e != null) {
							return e;
						}
					}
				}
			}
		}
		
		if (!context.isD1()) {
		    if (t1.ty == Tsarray) {
		    	// Convert e1 to e1[]
				Expression e = new SliceExp(e1.filename, e1.lineNumber, e1, null, null);
				e1 = e.semantic(sc, context);
				t1 = e1.type.toBasetype(context);
			}
		}

		e2.rvalue(context);
		if (e1.op == TOK.TOKarraylength) {
			// e1 is not an lvalue, but we let code generator handle it
			ArrayLengthExp ale = (ArrayLengthExp) e1;
			ale.e1 = ale.e1.modifiableLvalue(sc, e1, context);
		} else if (e1.op == TOK.TOKslice) {
			if (!context.isD1()) {
				Type tn = e1.type.nextOf();
				if (tn != null && !tn.isMutable() && op != TOKconstruct) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.SliceIsNotMutable, this, e1.toChars(context)));
					}
				}
			}
		} else {
			// Try to do a decent error message with the expression
			// before it got constant folded
			if (context.isD1()) {
				e1 = e1.modifiableLvalue(sc, e1old, context);
			} else {
				if (e1.op != TOKvar)
				    e1 = e1.optimize(WANTvalue, context);

				if (op != TOKconstruct)
				    e1 = e1.modifiableLvalue(sc, e1old, context);
			}
		}

	    Type t2 = e2.type;
		if (e1.op == TOK.TOKslice && 
				null != t1.nextOf() && 
				e2.implicitConvTo(t1.nextOf(), context) != MATCH.MATCHnomatch &&
				!(t1.nextOf().equals(e2.type.nextOf()))) { 
			// memset
			ismemset = true;	// make it easy for back end to tell what this is
			e2 = e2.implicitCastTo(sc, t1.nextOf(), context);
		} else if (t1.ty == TY.Tsarray) {
			if (context.isD1()) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotAssignToStaticArray, this, e1.toChars(context)));
				}
			} else {
				/* Should have already converted e1 => e1[]
				 */
				throw new IllegalStateException();
			}
		} else if (context.isD2() && e1.op == TOKslice) {
			e2 = e2.implicitCastTo(sc, e1.type.constOf(context), context);
		} else {
			e2 = e2.implicitCastTo(sc, e1.type, context);
		}
		
	    /* Look for array operations
	     */
	    if (e1.op == TOKslice
				&& !ismemset
				&& (e2.op == TOKadd || e2.op == TOKmin || e2.op == TOKmul
						|| e2.op == TOKdiv || e2.op == TOKmod
						|| e2.op == TOKxor || e2.op == TOKand || e2.op == TOKor
						|| e2.op == TOKtilde || e2.op == TOKneg)) {
			type = e1.type;
			return arrayOp(sc, context);
		}


		type = e1.type;
		assert (null != type);
		
		return this;
	}
	
	@Override
	public void buildArrayIdent(OutBuffer buf, Expressions arguments) {
		/* Evaluate assign expressions right to left
	     */
	    e2.buildArrayIdent(buf, arguments);
	    e1.buildArrayIdent(buf, arguments);
	    buf.writestring("Assign");
	}
	
	@Override
	public Expression buildArrayLoop(Arguments fparams, SemanticContext context) {
		/* Evaluate assign expressions right to left
	     */
	    Expression ex2 = e2.buildArrayLoop(fparams, context);
	    /* Need the cast because:
	     *   b = c + p[i];
	     * where b is a byte fails because (c + p[i]) is an int
	     * which cannot be implicitly cast to byte.
	     */
	    ex2 = new CastExp(null, 0, ex2, e1.type.nextOf());
	    Expression ex1 = e1.buildArrayLoop(fparams, context);
	    Argument param = (Argument) fparams.get(0);
	    param.storageClass = 0;
	    Expression e = new AssignExp(null, 0, ex1, ex2);
	    return e;
	}

}
