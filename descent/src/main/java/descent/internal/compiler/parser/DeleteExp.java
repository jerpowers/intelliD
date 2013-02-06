package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKindex;
import static descent.internal.compiler.parser.TY.Taarray;
import static descent.internal.compiler.parser.TY.Tstruct;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class DeleteExp extends UnaExp {

	public DeleteExp(char[] filename, int lineNumber, Expression e1) {
		super(filename, lineNumber, TOK.TOKdelete, e1);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceE1);
		}
		visitor.endVisit(this);
	}

	@Override
	public int checkSideEffect(int flag, SemanticContext context) {
		return 1;
	}

	@Override
	public Expression checkToBoolean(SemanticContext context) {
		if (context.acceptsErrors()) {
			context
					.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ExpressionDoesNotGiveABooleanResult, this));
		}
		return this;
	}

	@Override
	public int getNodeType() {
		return DELETE_EXP;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Type tb;

		super.semantic(sc, context);
		e1 = resolveProperties(sc, e1, context);
		e1 = e1.toLvalue(sc, null, context);
		type = Type.tvoid;

		tb = e1.type.toBasetype(context);
		switch (tb.ty) {
		case Tclass: {
			TypeClass tc = (TypeClass) tb;
			ClassDeclaration cd = tc.sym;

			if (cd.isCOMinterface()) {
			    /* Because COM classes are deleted by IUnknown.Release()
				 */
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.CannotDeleteInstanceOfComInterface, this, cd.toChars(context)));
				}
			}
			break;
		}
		case Tpointer:
			tb = ((TypePointer) tb).next.toBasetype(context);
			if (tb.ty == Tstruct) {
				TypeStruct ts = (TypeStruct) tb;
				StructDeclaration sd = ts.sym;
				FuncDeclaration f = sd.aggDelete(context);
				FuncDeclaration fd = sd.dtor;
				
				Expression ea = null;
				Expression eb = null;
				Expression ec = null;
				VarDeclaration v = null;
				if (!context.isD1()) {
					if (null == f && null == fd)
					    break;

					/* Construct:
					 *	ea = copy e1 to a tmp to do side effects only once
					 *	eb = call destructor
					 *	ec = call deallocator
					 */
					ea = null;
					eb = null;
					ec = null;
					v = null;

					if (fd != null && f != null)
					{   IdentifierExp id = context.uniqueId("__tmp");
					    v = new VarDeclaration(filename, lineNumber, e1.type, id, new ExpInitializer(filename, lineNumber, e1));
					    v.semantic(sc, context);
					    v.parent = sc.parent;
					    ea = new DeclarationExp(filename, lineNumber, v);
					    ea.type = v.type;
					}

					if (fd != null)
					{   Expression e = ea != null ? new VarExp(filename, lineNumber, v) : e1;
					    e = new DotVarExp(null, 0, e, fd, false);
					    eb = new CallExp(filename, lineNumber, e);
					    eb = eb.semantic(sc, context);
					}
				}

				if (f != null) {
					Type tpv = Type.tvoid.pointerTo(context);

					if (context.isD1()) {
						Expression e = e1.castTo(sc, tpv, context);
						ec = new VarExp(filename, lineNumber, f);
						e = new CallExp(filename, lineNumber, ec, e);
						return e.semantic(sc, context);
					} else {
						Expression e = ea != null ? new VarExp(filename, lineNumber, v) : e1.castTo(sc, tpv, context);
					    e = new CallExp(filename, lineNumber, new VarExp(filename, lineNumber, f), e);
					    ec = e.semantic(sc, context);
					}
				}
				
				if (!context.isD1()) {
					ea = combine(ea, eb);
					ea = combine(ea, ec);
					return ea;
				}
			}
			break;

		case Tarray:
		    /* BUG: look for deleting arrays of structs with dtors.
		     */
			break;

		default:
			if (e1.op == TOKindex) {
				IndexExp ae = (IndexExp) (e1);
				Type tb1 = ae.e1.type.toBasetype(context);
				if (tb1.ty == Taarray) {
					break;
				}
			}
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotDeleteType, this, e1.type.toChars(context)));
			}
			break;
		}

		if (e1.op == TOKindex) {
			IndexExp ae = (IndexExp) (e1);
			Type tb1 = ae.e1.type.toBasetype(context);
			if (tb1.ty == Taarray) {
				if (!context.global.params.useDeprecated) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.DeleteAAKeyDeprecated, this));
					}
				}
			}
		}

		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("delete ");
		expToCBuffer(buf, hgs, e1, op.precedence, context);
	}

}
