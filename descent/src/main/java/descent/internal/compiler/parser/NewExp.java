package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKint64;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tstruct;

import java.math.BigInteger;

import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class NewExp extends Expression {

	public Expression thisexp, sourceThisexp;
	public Expressions newargs, sourceNewargs;
	public Type newtype, sourceNewtype;
	public Expressions arguments, sourceArguments;

	public CtorDeclaration member; // constructor function
	public NewDeclaration allocator; // allocator function
	public boolean onstack; // allocate on stack

	public NewExp(char[] filename, int lineNumber, Expression thisexp, Expressions newargs,
			Type newtype, Expressions arguments) {
		super(filename, lineNumber, TOK.TOKnew);
		this.thisexp = thisexp;
		this.sourceThisexp = thisexp;
		this.newargs = newargs;
		this.sourceNewargs = newargs;
		this.newtype = newtype;
		this.sourceNewtype = newtype;
		this.arguments = arguments;
		if (arguments != null) {
			this.sourceArguments = new Expressions(arguments);
		}
		this.member = null;
		this.allocator = null;
		this.onstack = false;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceThisexp);
			TreeVisitor.acceptChildren(visitor, sourceNewargs);
			TreeVisitor.acceptChildren(visitor, sourceNewtype);
			TreeVisitor.acceptChildren(visitor, sourceArguments);
		}
		visitor.endVisit(this);
	}

	@Override
	public boolean canThrow(SemanticContext context) {
		if (context.isD2()) {
			return true;
		} else {
			return super.canThrow(context);
		}
	}

	@Override
	public int checkSideEffect(int flag, SemanticContext context) {
		return 1;
	}

	@Override
	public int getNodeType() {
		return NEW_EXP;
	}

	@Override
	public void scanForNestedRef(Scope sc, SemanticContext context)
	{
		if(null != thisexp)
			thisexp.scanForNestedRef(sc, context);

		arrayExpressionScanForNestedRef(sc, newargs, context);
		arrayExpressionScanForNestedRef(sc, arguments, context);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Type tb;
		ClassDeclaration cdthis = null;

		if (type != null) {
			return this;
		}

		// Descent: May be null if the source code has syntax errors
		if (newtype == null) {
			return this;
		}

		boolean loop = true;
		Lagain: while (loop) {
			loop = false;
			if (thisexp != null) {
				thisexp = thisexp.semantic(sc, context);
				cdthis = thisexp.type.isClassHandle();
				if (cdthis != null) {
					sc = sc.push(cdthis);
					type = newtype.semantic(filename, lineNumber, sc, context);
					sc = sc.pop();
				} else {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ThisForNestedClassMustBeAClassType, this, new String[] { thisexp.type.toChars(context) }));
					}
					type = newtype.semantic(filename, lineNumber, sc, context);
				}
			} else {
				type = newtype.semantic(filename, lineNumber, sc, context);
			}
		    newtype = type;		// in case type gets cast to something else
			tb = type.toBasetype(context);

			arrayExpressionSemantic(newargs, sc, context);
			preFunctionArguments(filename, lineNumber, sc, newargs, context);
			arrayExpressionSemantic(arguments, sc, context);
			preFunctionArguments(filename, lineNumber, sc, arguments, context);

			if (thisexp != null && tb.ty != Tclass) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.ExpressionDotNewIsOnlyForAllocatingNestedClasses, this, new String[] { tb
									.toChars(context) }));
				}
			}

			if (tb.ty == Tclass) {
				TypeFunction tf;

				TypeClass tc = (TypeClass) (tb);
				ClassDeclaration cd = tc.sym.isClassDeclaration();

				// Descent: to get "foo is abtract class" errors ok
				cd = (ClassDeclaration) cd.unlazy(context);

				if (cd.isInterfaceDeclaration() != null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotCreateInstanceOfInterface, sourceNewtype, new String[] { cd.toChars(context) }));
					}
				} else if (cd.isAbstract()) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotCreateInstanceOfAbstractClass, sourceNewtype, new String[] { cd.toChars(context) }));
						for(int i = 0; i < size(cd.vtbl); i++) {
						    FuncDeclaration fd = ((Dsymbol) cd.vtbl.get(i)).isFuncDeclaration();
							if (fd != null && fd.isAbstract()) {
								if (context.acceptsErrors()) {
									context.acceptProblem(Problem.newSemanticTypeError(IProblem.FunctionIsAbstract, this, new String[] { fd.toChars(context)} ));
								}
							}
						}
					}
				}
				checkDeprecated(sc, cd, context);
				if (cd.isNested()) {
				/* We need a 'this' pointer for the nested class.
				 * Ensure we have the right one.
				 */
					Dsymbol s = cd.toParent2();
					ClassDeclaration cdn = s.isClassDeclaration();
					FuncDeclaration fdn = s.isFuncDeclaration();

					if (cdn != null) {
						if (cdthis == null) {
							// Supply an implicit 'this' and try again
							thisexp = new ThisExp(filename, lineNumber);
							for (Dsymbol sp = sc.parent; true; sp = sp.parent) {
								if (sp == null) {
									if (context.acceptsErrors()) {
										context.acceptProblem(Problem.newSemanticTypeError(
												IProblem.OuterClassThisNeededToNewNestedClass, this, new String[] { cdn.toChars(context), cd.toChars(context) }));
									}
									break;
								}
								ClassDeclaration cdp = sp.isClassDeclaration();
								if (cdp == null) {
									continue;
								}
								if (cdp == cdn
										|| cdn.isBaseOf(cdp, null, context)) {
									break;
								}
								// Add a '.outer' and try again
								thisexp = new DotIdExp(filename, lineNumber, thisexp, Id.outer);
							}
							if (context.global.errors == 0) {
								// goto Lagain;
								loop = true;
								continue Lagain;
							}
						}
						if (cdthis != null) {
							if (cdthis != cdn
									&& !cdn.isBaseOf(cdthis, null, context)) {
								if (context.acceptsErrors()) {
									context.acceptProblem(Problem.newSemanticTypeError(
											IProblem.ThisForNestedClassMustBeOfType, this, new String[] { cdn.toChars(context), thisexp.type.toChars(context) }));
								}
							}
						}
					} else {
						if (context.isD2()) {
							if (fdn != null) {
								if (thisexp != null) {
									if (context.acceptsErrors()) {
										context.acceptProblem(Problem.newSemanticTypeError(
												IProblem.ExpressionDotNewIsOnlyForAllocatingNestedClasses, this));
									}
								}
							}
						} else {
							if (thisexp != null) {
								if (context.acceptsErrors()) {
									context.acceptProblem(Problem.newSemanticTypeError(
											IProblem.ExpressionDotNewIsOnlyForAllocatingNestedClasses, this));
								}
							}
						}
					}
				} else if (thisexp != null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ExpressionDotNewIsOnlyForAllocatingNestedClasses, this));
					}
				}

				FuncDeclaration f;

				if (context.isD1()) {
					f = cd.ctor(context);
				} else {
					f = null;
					if (cd.ctor(context) != null) {
					    f = resolveFuncCall(sc, filename, lineNumber, cd.ctor(context), null, null, arguments, 0, context);
					}
				}
				if (f != null) {
					f = f.overloadResolve(filename, lineNumber, null, arguments, context, this);

					checkDeprecated(sc, f, context);
					member = f.isCtorDeclaration();
					Assert.isNotNull(member);

					cd.accessCheck(sc, member, context, this);

					tf = (TypeFunction) f.type;
					if (context.isD1()) {
						type = tf.next;
					}

					if (arguments == null) {
						arguments = new Expressions(0);
					}
					functionArguments(filename, lineNumber, sc, tf, arguments, context);
				} else {
					if (arguments != null && arguments.size() > 0) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.NoConstructorForSymbol, this, new String[] { cd.toChars(context) }));
						}
					}
				}

				if (cd.aggNew(context) != null) {
					Expression e;

					f = cd.aggNew(context);

					// Prepend the uint size argument to newargs[]
					e = new IntegerExp(filename, lineNumber, cd.size(context), Type.tuns32);
					if (newargs == null) {
						newargs = new Expressions(0);
					}
					newargs.add(0, e);

					f = f.overloadResolve(filename, lineNumber, null, newargs, context, this);
					allocator = f.isNewDeclaration();
					Assert.isNotNull(allocator);

					tf = (TypeFunction) f.type;
					functionArguments(filename, lineNumber, sc, tf, newargs, context);
				} else {
					if (newargs != null && newargs.size() > 0) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.NoAllocatorForSymbol, this, new String[] { cd.toChars(context) }));
						}
					}
				}

			} else if (tb.ty == Tstruct) {
				TypeStruct ts = (TypeStruct) tb;
				StructDeclaration sd = ts.sym;
				TypeFunction tf;

				if (arguments != null && arguments.size() > 0) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.NoConstructorForSymbol, this, new String[] { type.toChars(context) }));
					}
				}

				FuncDeclaration f;

				if (context.isD1()) {
					f = sd.aggNew(context);
				} else {
					f = null;
					if (sd.ctor != null) {
					    f = resolveFuncCall(sc, filename, lineNumber, sd.ctor, null, null, arguments, 0, context);
					}
				}

				if (context.isD1()) {
					if (f != null) {
						Expression e;

						// Prepend the uint size argument to newargs[]
						e = new IntegerExp(filename, lineNumber, sd.size(context), Type.tuns32);
						if (newargs == null) {
							newargs = new Expressions(0);
						}
						newargs.add(0, e);

						f = f.overloadResolve(filename, lineNumber, null, newargs, context, this);
						allocator = f.isNewDeclaration();
						Assert.isNotNull(allocator);

						tf = (TypeFunction) f.type;
						functionArguments(filename, lineNumber, sc, tf, newargs, context);

						e = new VarExp(filename, lineNumber, f);
						e = new CallExp(filename, lineNumber, e, newargs);
						e = e.semantic(sc, context);
						e.type = type.pointerTo(context);
						return e;
					}
				} else {
					if (f != null) {
						checkDeprecated(sc, f, context);
						member = f.isCtorDeclaration();

						sd.accessCheck(sc, member, context, this); // SEMANTIC this is caller?

						tf = (TypeFunction) f.type;
						// type = tf.next;

						if (null == arguments) {
							arguments = new Expressions(0);
						}
						functionArguments(filename, lineNumber, sc, tf,
								arguments, context);
					} else {
						if (arguments != null && arguments.size() > 0) {
							if (context.acceptsErrors()) {
								context
										.acceptProblem(Problem
												.newSemanticTypeError(
														IProblem.NoConstructorForSymbol,
														this,
														sd.toChars(context)));
							}
						}
					}

					if (sd.aggNew != null) {
						// Prepend the uint size argument to newargs[]
						Expression e = new IntegerExp(filename, lineNumber, sd
								.size(context), Type.tuns32);
						if (null == newargs)
							newargs = new Expressions();
						newargs.shift(e);

						f = sd.aggNew.overloadResolve(filename, lineNumber,
								null, newargs, context, this); // SEMANTIC this
																// is caller?
						allocator = f.isNewDeclaration();

						tf = (TypeFunction) f.type;
						functionArguments(filename, lineNumber, sc, tf,
								newargs, context);
					} else {
						if (newargs != null && 0 == newargs.size()) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem
										.newSemanticTypeError(
												IProblem.NoAllocatorForSymbol,
												this, sd.toChars(context)));
							}
						}
					}
				}

				type = type.pointerTo(context);
			} else if (tb.ty == Tarray
					&& (arguments != null && arguments.size() > 0)) {
				for (int i = 0; i < arguments.size(); i++) {
					if (tb.ty != Tarray) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.TooManyArgumentsForArray, this));
						}
						arguments.setDim(i);
						break;
					}

					Expression arg = arguments.get(i);
					arg = resolveProperties(sc, arg, context);
					arg = arg.implicitCastTo(sc, Type.tsize_t, context);
					if (context.isD2()) {
						arg = arg.optimize(WANTvalue, context);
					}
					if (arg.op == TOKint64
							&& arg.toInteger(context)
									.compareTo(BigInteger.ZERO) < 0) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.NegativeArrayIndex, this, new String[] { arg.toChars(context) }));
						}
					}
					arguments.set(i, arg);
					tb = ((TypeDArray)tb).next.toBasetype(context);
				}
			} else if (tb.isscalar(context)) {
				if (arguments != null && arguments.size() > 0) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.NoConstructorForSymbol, this, new String[] { type.toChars(context) }));
					}
				}

				type = type.pointerTo(context);
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.NewCanOnlyCreateStructsDynamicArraysAndClassObjects, this, new String[] { type.toChars(context) }));
				}
				type = type.pointerTo(context);
			}
		}
		return this;
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		return new NewExp(filename, lineNumber, thisexp != null ? thisexp.syntaxCopy(context) : null,
				arraySyntaxCopy(newargs, context), newtype.syntaxCopy(context),
				arraySyntaxCopy(arguments, context));
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (thisexp != null) {
			expToCBuffer(buf, hgs, thisexp, PREC.PREC_primary, context);
			buf.writeByte('.');
		}
		buf.writestring("new ");
		if (newargs != null && newargs.size() > 0) {
			buf.writeByte('(');
			argsToCBuffer(buf, newargs, hgs, context);
			buf.writeByte(')');
		}
		// Descent: may be null if source has syntax errors
		if (newtype != null) {
			newtype.toCBuffer(buf, null, hgs, context);
		}
		if (arguments != null && arguments.size() > 0) {
			buf.writeByte('(');
			argsToCBuffer(buf, arguments, hgs, context);
			buf.writeByte(')');
		}
	}

}
