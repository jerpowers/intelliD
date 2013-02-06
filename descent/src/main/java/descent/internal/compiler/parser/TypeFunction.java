package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHconvert;
import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.STC.STCalias;
import static descent.internal.compiler.parser.STC.STCauto;
import static descent.internal.compiler.parser.STC.STClazy;
import static descent.internal.compiler.parser.STC.STCnothrow;
import static descent.internal.compiler.parser.STC.STCout;
import static descent.internal.compiler.parser.STC.STCpure;
import static descent.internal.compiler.parser.STC.STCref;
import static descent.internal.compiler.parser.STC.STCscope;
import static descent.internal.compiler.parser.STC.STCstatic;
import static descent.internal.compiler.parser.Scope.SCOPEctor;
import static descent.internal.compiler.parser.TY.Tfunction;
import static descent.internal.compiler.parser.TY.Tident;
import static descent.internal.compiler.parser.TY.Tpointer;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Ttuple;
import static descent.internal.compiler.parser.TY.Tvoid;

import java.util.ArrayList;
import java.util.List;

import melnorme.utilbox.core.Assert;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TypeFunction extends TypeNext implements Cloneable {

	public int inuse;
	public LINK linkage; // calling convention
	public Arguments parameters, sourceParameters;
	public int varargs;
	public char linkageChar;
	public boolean ispure;
	public boolean isnothrow;
	public boolean isref;
	
	public List<Modifier> postModifiers;

	public TypeFunction(Arguments parameters, Type treturn, int varargs,
			LINK linkage) {
		super(Tfunction, treturn);
		
		this.parameters = parameters;
		if (this.parameters != null) {
			this.sourceParameters = new Arguments(parameters);
		}
		this.varargs = varargs;
		this.linkage = linkage;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceNext);
			TreeVisitor.acceptChildren(visitor, sourceParameters);
			TreeVisitor.acceptChildren(visitor, postModifiers);
		}
		visitor.endVisit(this);
	}

	@Override
	public MATCH deduceType(Scope sc, Type tparam,
			TemplateParameters parameters, Objects dedtypes,
			SemanticContext context) {
		boolean L1 = true;

		// Extra check that function characteristics must match
		if (null != tparam && tparam.ty == Tfunction) {

			TypeFunction tp = (TypeFunction) tparam;
			if (varargs != tp.varargs || linkage != tp.linkage) {
				return MATCHnomatch;
			}

			int nfargs = Argument.dim(this.parameters, context);
			int nfparams = Argument.dim(tp.parameters, context);

			boolean repeat = true;
			boolean gotoL1 = false;
			
		loop:
			while(repeat) {
				repeat = false;
				
				/*
				 * See if tuple match
				 */
				if (nfparams > 0 && nfargs >= nfparams - 1) {
					/*
					 * See if 'A' of the template parameter matches 'A' of the
					 * type of the last function parameter.
					 */
					Argument fparam = Argument.getNth(tp.parameters, nfparams - 1, context);
					if (fparam.type.ty != Tident) {
						// goto L1;
						gotoL1 = true;
						break loop;
					}
					TypeIdentifier tid = (TypeIdentifier) fparam.type;
					if (size(tid.idents) > 0) {
						// goto l1
						gotoL1 = true;
						break loop;
					}

					/*
					 * Look through parameters to find tuple matching tid.ident
					 */
					int tupi = 0;
					for (; true; tupi++) {
						if (tupi == parameters.size()) {
							// goto L1;
							gotoL1 = true;
							break loop;
						}
						TemplateParameter t = parameters
								.get(tupi);
						TemplateTupleParameter tup = t
								.isTemplateTupleParameter();
						if (null != tup && equals(tup.ident, tid.ident)) {
							break;
						}
					}

					/*
					 * The types of the function arguments [nfparams - 1 ..
					 * nfargs] now form the tuple argument.
					 */
					int tuple_dim = nfargs - (nfparams - 1);

					/*
					 * See if existing tuple, and whether it matches or not
					 */
					Object o = dedtypes.get(tupi);
					if (null != o) { // Existing deduced argument must be a
										// tuple, and must
						// match
						Tuple t = isTuple((ASTDmdNode) o);
						if (null == t || t.objects.size() != tuple_dim) {
							return MATCHnomatch;
						}
						for (int i = 0; i < tuple_dim; i++) {
							Argument arg = Argument.getNth(this.parameters,
									nfparams - 1 + i, context);
							if (!arg.type.equals(t.objects.get(i))) {
								return MATCHnomatch;
							}
						}
					} else { // Create new tuple
						Tuple t = new Tuple();
						t.objects.setDim(tuple_dim);
						for (int i = 0; i < tuple_dim; i++) {
							Argument arg = Argument.getNth(this.parameters,
									nfparams - 1 + i, context);
							t.objects.set(i, arg.type);
						}
						dedtypes.set(tupi, t);
					}
					nfparams--; // don't consider the last parameter for type
					// deduction
					L1 = false;
				}
				break loop;
			}
			
			if (gotoL1) {
				// L1:
				if (L1 && (nfargs != nfparams)) {
					return MATCHnomatch;
				}
			}
			
			// L2:
			for (int i = 0; i < nfparams; i++) {
				Argument a = Argument.getNth(this.parameters, i, context);
				Argument ap = Argument.getNth(tp.parameters, i, context);
				if (a.storageClass != ap.storageClass
						|| null == a.type.deduceType(sc, ap.type,
								parameters, dedtypes, context)) {
					return MATCHnomatch;
				}
			}
		}
		return super.deduceType(sc, tparam, parameters, dedtypes, context);
	}

	@Override
	public int getNodeType() {
		return TYPE_FUNCTION;
	}

	@Override
	public TypeInfoDeclaration getTypeInfoDeclaration(SemanticContext context) {
		return new TypeInfoFunctionDeclaration(this, context);
	}
	
	/***************************
	 * Examine function signature for parameter p and see if
	 * p can 'escape' the scope of the function.
	 */
	public boolean parameterEscapes(Argument p, SemanticContext context) {
		/*
		 * Scope parameters do not escape. Allow 'lazy' to imply 'scope' - lazy
		 * parameters can be passed along as lazy parameters to the next
		 * function, but that isn't escaping.
		 */
		if ((p.storageClass & (STCscope | STClazy)) != 0)
			return false;

		if (ispure) {
			/*
			 * With pure functions, we need only be concerned if p escapes via
			 * any return statement.
			 */
			Type tret = nextOf().toBasetype(context);
			if (!isref && !tret.hasPointers(context)) {
				/*
				 * The result has no references, so p could not be escaping that
				 * way.
				 */
				return false;
			}
		}

		/*
		 * Assume it escapes in the absence of better information.
		 */
		return true;
	}

	@Override
	public Type reliesOnTident() {

		if (null != parameters) {
			for (int i = 0; i < parameters.size(); i++) {
				Argument arg = parameters.get(i);
				Type t = arg.type.reliesOnTident();
				if (null != t) {
					return t;
				}
			}
		}
		return next.reliesOnTident();
	}

	public RET retStyle() {
		return RET.RETstack;
	}

	@Override
	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		if (deco != null) { // if semantic() already run
			return this;
		}
		
	    TypeFunction tf = copy();
	    if (parameters != null) {
		    tf.parameters = new Arguments(size(parameters));
		    for (int i = 0; i < size(parameters); i++) {
				tf.parameters.add(parameters.get(i).copy());
			}
	    }
	    
	    if (context.isD2()) {
			if ((sc.stc & STCpure) != 0)
				tf.ispure = true;
			if ((sc.stc & STCnothrow) != 0)
				tf.isnothrow = true;
			if ((sc.stc & STCref) != 0)
				tf.isref = true;
	    }

		tf.linkage = sc.linkage;
		if (null == tf.next) {
			tf.next = tvoid;
		}
		tf.next = tf.next.semantic(filename, lineNumber, sc, context);
		if (tf.next.toBasetype(context).ty == Tsarray) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.FunctionsCannotReturnStaticArrays, this));
			}
			tf.next = Type.terror;
		}
		if (tf.next.toBasetype(context).ty == Tfunction) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.FunctionsCannotReturnAFunction, this));
			}
			tf.next = Type.terror;
		}
		if (tf.next.toBasetype(context).ty == Ttuple) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.FunctionsCannotReturnATuple, this));
			}
			tf.next = Type.terror;
		}
		if (tf.next.isauto() && (sc.flags & SCOPEctor) == 0) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(context.isD1() ? IProblem.FunctionsCannotReturnAuto : IProblem.FunctionsCannotReturnScope, this, new String[] { tf.next.toChars(context) }));
			}
		}

		if (tf.parameters != null) {
			int dim = Argument.dim(tf.parameters, context);

			for (int i = 0; i < dim; i++) {
				Argument arg = Argument.getNth(tf.parameters, i, context);
				Type t;

				tf.inuse++;
				arg.type = arg.type.semantic(filename, lineNumber, sc, context);
				if (tf.inuse == 1) tf.inuse--;
				
				if (context.isD1()) {
					t = arg.type.toBasetype(context);
				} else {
					arg.type = arg.type.addStorageClass(arg.storageClass, context);

					if ((arg.storageClass & (STCauto | STCalias | STCstatic)) != 0) {
						if (null == arg.type)
							continue;
					}

					t = arg.type.toBasetype(context);
				}

				if ((arg.storageClass & (STCout | STCref | STClazy)) != 0) {
					if (t.ty == Tsarray) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotHaveOutOrInoutParameterOfTypeStaticArray, t));
						}
					}
					if ((arg.storageClass & STCout) != 0 && arg.type.mod != 0) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.CannotHaveConstInvariantOutParameterOfType, t, t.toChars(context)));
						}
					}
				}
				if ((arg.storageClass & STClazy) == 0 && t.ty == Tvoid) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.CannotHaveParameterOfTypeVoid, arg.sourceType));
					}
				}

				if (arg.defaultArg != null) {
					arg.defaultArg = arg.defaultArg.semantic(sc, context);
					arg.defaultArg = ASTDmdNode.resolveProperties(sc,
							arg.defaultArg, context);
					arg.defaultArg = arg.defaultArg.implicitCastTo(sc,
							arg.type, context);
				}

				/*
				 * If arg turns out to be a tuple, the number of parameters may
				 * change.
				 */
				if (t.ty == Ttuple) {
					dim = Argument.dim(tf.parameters, context);
					i--;
				}
			}
		}
		tf.deco = tf.merge(context).deco;
		
		// Descent: I'm not sure about this, but it seems DMD copies the resolved types to
		// the original arguments with the memcpy
		if (tf.parameters != null && parameters != null) {
			for(int i = 0; i < parameters.size() && i < tf.parameters.size(); i++) {
				parameters.get(i).type = tf.parameters.get(i).type;
			}
		}

		if (tf.inuse != 0) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.RecursiveType, this));
			}
			tf.inuse = 0;
			return terror;
		}

		if (tf.varargs != 0 && tf.linkage != LINK.LINKd
				&& Argument.dim(tf.parameters, context) == 0) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.VariadicFunctionsWithNonDLinkageMustHaveAtLeastOneParameter, this));
			}
		}

		/*
		 * Don't return merge(), because arg identifiers and default args can be
		 * different even though the types match
		 */
		return tf;
	}

	@Override
	public Type syntaxCopy(SemanticContext context) {
		Type treturn = next != null ? next.syntaxCopy(context) : null;
		Arguments params = Dsymbol.arraySyntaxCopy(parameters, context);
		TypeFunction t = new TypeFunction(params, treturn, varargs, linkage);
	    t.mod = mod;
	    t.isnothrow = isnothrow;
	    t.ispure = ispure;
	    t.isref = isref;
		t.copySourceRange(this);
		return t;
	}

	@Override
	public void toDecoBuffer(OutBuffer buf, int flag, SemanticContext context) {
		char mc;

		if (inuse != 0) {
			inuse = 2; // flag error to caller
			return;
		}
		inuse++;
		
		if (context.isD2()) {
			if ((mod & MODshared) != 0)
				buf.writeByte('O');
			if ((mod & MODconst) != 0)
				buf.writeByte('x');
			else if ((mod & MODinvariant) != 0)
				buf.writeByte('y');
		}
		
		// TODO Descent: for now assume everything has D linkage so that deco
		// comparisons work
//		switch (linkage) {
//		case LINKd:
//		case LINKc:
//		case LINKwindows:
//		case LINKpascal:
//		case LINKcpp:
//			mc = linkage.mangleChar;
//			break;
//		// Added for Descent
//		case LINKsystem:
//			mc = context._WIN32 ? LINK.LINKwindows.mangleChar : LINK.LINKc.mangleChar;
//			break;
//		default:
//			throw new IllegalStateException("assert(0);");
//		}
		
		mc = LINK.LINKd.mangleChar;
		
		// For Descent signature
		linkageChar = mc;
		
		buf.writeByte(mc);
		
		if (context.isD2()) {
			if (ispure || isnothrow || isref) {
				if (ispure)
					buf.writestring("Na");
				if (isnothrow)
					buf.writestring("Nb");
				if (isref)
					buf.writestring("Nc");
			}
		}
		
		
		// Write argument types
		Argument.argsToDecoBuffer(buf, parameters, context);
		buf.writeByte((char) ('Z' - varargs)); // mark end of arg list
		
		next.toDecoBuffer(buf, 0, context);
		inuse--;
	}

	public MATCH callMatch(Expression ethis, Expressions args, SemanticContext context) {
		MATCH match = MATCHexact; // assume exact match
		
		if (context.isD2()) {
			if (ethis != null) {
				Type t = ethis.type;
				if (t.toBasetype(context).ty == Tpointer)
					t = t.toBasetype(context).nextOf(); // change struct* to
														// struct
				if (t.mod != mod) {
					if (mod == MODconst)
						match = MATCHconst;
					else
						return MATCHnomatch;
				}
			}
		}

		int nparams = Argument.dim(parameters, context);
		int nargs = null != args ? args.size() : 0;
		if (nparams == nargs) {
			;
		} else if (nargs > nparams) {
			if (varargs == 0) {
				return MATCHnomatch; // goto Nomatch; // too many args; no
			}
										// match
			match = MATCHconvert; // match ... with a "conversion" match level
		}

		for (int u = 0; u < nparams; u++) {
			MATCH m = MATCHnomatch;
			Expression arg;

			// BUG: what about out and ref?

			Argument p = Argument.getNth(parameters, u, context);
			Assert.isTrue(null != p);
			
			boolean gotoL1 = false;
			
			if (u >= nargs) {
				if (null != p.defaultArg) {
					continue;
				}
				if (varargs == 2 && u + 1 == nparams) {
					// goto L1;
					gotoL1 = true;
				} else {
					return MATCHnomatch; // goto Nomatch; // not enough
											// arguments
				}
			}
			
			if (!gotoL1) {
				arg = args.get(u);
				assert (null != arg);
				
				if (context.isD1()) {
					if (0 != (p.storageClass & STClazy) && p.type.ty == Tvoid
							&& arg.type.ty != Tvoid) {
						m = MATCHconvert;
					} else {
						m = arg.implicitConvTo(p.type, context);
					}
				} else {
					// Non-lvalues do not match ref or out parameters
					if ((p.storageClass & (STCref | STCout)) != 0 && !arg.isLvalue(context)) {
						return MATCHnomatch;
					}

					if ((p.storageClass & STClazy) != 0 && p.type.ty == Tvoid && arg.type.ty != Tvoid)
						m = MATCHconvert;
					else
						m = arg.implicitConvTo(p.type, context);
				}
			}
			
			if (gotoL1 || m == MATCHnomatch) // if no match
			{
				// L1:
				if (varargs == 2 && u + 1 == nparams) // if last varargs
				// param
				{
					Type tb = p.type.toBasetype(context);
					TypeSArray tsa;
					integer_t sz;

					switch (tb.ty) {
					case Tsarray:
						tsa = (TypeSArray) tb;
						sz = tsa.dim.toInteger(context);
						if (!sz.equals(nargs - u)) {
							return MATCHnomatch; // goto Nomatch;
						}
					case Tarray:
				    	TypeArray ta = (TypeArray)tb;
						for (; u < nargs; u++) {
							arg = args.get(u);
							assert (null != arg);
							/*
							 * If lazy array of delegates, convert arg(s) to
							 * delegate(s)
							 */
							Type tret = p.isLazyArray(context);
							if (null != tret) {
								if (ta.next.equals(arg.type)) {
									m = MATCHexact;
								} else {
									m = arg.implicitConvTo(tret, context);
									if (m == MATCHnomatch) {
										if (tret.toBasetype(context).ty == Tvoid) {
											m = MATCHconvert;
										}
									}
								}
							} else {
								m = arg.implicitConvTo(ta.next, context);
							}
							if (m == MATCHnomatch) {
								return MATCHnomatch; // goto Nomatch;
							}
							if (m.ordinal() < match.ordinal()) {
								match = m;
							}
						}
						return match; // goto Ldone;

					case Tclass:
						// Should see if there's a constructor match?
						// Or just leave it ambiguous?
						return match; // goto Ldone;

					default:
						return MATCHnomatch; // goto Nomatch;
					}
				}
				return MATCHnomatch; // goto Nomatch;
			}
			if (m.ordinal() < match.ordinal()) {
				match = m; // pick worst match
			}
		}

		return match;
	}
	
	@Override
	public void toCBuffer(OutBuffer buf, IdentifierExp ident, HdrGenState hgs, SemanticContext context) {
		String p = null;

		if (inuse != 0) {
			inuse = 2; // flag error to caller
			return;
		}
		inuse++;
		
		if (context.isD2()) {
			/*
			 * Use 'storage class' style for attributes
			 */
			if ((mod & MODconst) != 0)
				buf.writestring("const ");
			if ((mod & MODinvariant) != 0)
				buf.writestring("immutable ");
			if ((mod & MODshared) != 0)
				buf.writestring("shared ");

			if (ispure)
				buf.writestring("pure ");
			if (isnothrow)
				buf.writestring("nothrow ");
			if (isref)
				buf.writestring("ref ");
		}
		
		if (next != null && (null == ident || equals(ident.toHChars2(), ident.toChars().toCharArray()))) {
			next.toCBuffer2(buf, hgs, 0, context);
		}
		if (hgs.ddoc != 1) {
			switch (linkage) {
			case LINKd:
				p = null;
				break;
			case LINKc:
				p = "C ";
				break;
			case LINKwindows:
				p = "Windows ";
				break;
			case LINKpascal:
				p = "Pascal ";
				break;
			case LINKcpp:
				p = "C++ ";
				break;
			// Added for Descent
			case LINKsystem:
				if (context._WIN32) {
					p = "Windows ";
				} else {
					p = "C ";
				}
				break;
			default:
				throw new IllegalStateException("Unknown linkage: " + linkage);
			}
		}

		if (!hgs.hdrgen && p != null) {
			buf.writestring(p);
		}

		if (ident != null) {
			buf.writeByte(' ');
			buf.writestring(ident.toHChars2());
		}

		Argument.argsToCBuffer(buf, hgs, parameters, varargs, context);
		inuse--;
	}
	
	@Override
	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod, SemanticContext context) {
	    String p = null;

		if (inuse != 0) {
			inuse = 2; // flag error to caller
			return;
		}
		inuse++;
		if (next != null) {
			next.toCBuffer2(buf, hgs, 0, context);
		}
		if (hgs.ddoc != 1) {
			switch (linkage) {
			case LINKd:
				p = null;
				break;
			case LINKc:
				p = "C ";
				break;
			case LINKwindows:
				p = "Windows ";
				break;
			case LINKpascal:
				p = "Pascal ";
				break;
			case LINKcpp:
				p = "C++ ";
				break;
			// Added for Descent
			case LINKsystem:
				if (context._WIN32) {
					p = "Windows ";
				} else {
					p = "C ";
				}
				break;
			default:
				throw new IllegalStateException("Unknown linkage: " + linkage);
			}
		}

		if (!hgs.hdrgen && p != null) {
			buf.writestring(p);
		}
		buf.writestring(" function");
		Argument.argsToCBuffer(buf, hgs, parameters, varargs, context);
		
		if (context.isD2()) {
			/*
			 * Use postfix style for attributes
			 */
			if (mod != this.mod) {
				if ((mod & MODconst) != 0)
					buf.writestring(" const");
				if ((mod & MODinvariant) != 0)
					buf.writestring(" invariant");
				if ((mod & MODshared) != 0)
					buf.writestring(" shared");
			}
			if (ispure)
				buf.writestring(" pure");
			if (isnothrow)
				buf.writestring(" nothrow");
			if (isref)
				buf.writestring(" ref");
		}
		
		inuse--;
	}
	
	@Override
	public TypeFunction copy() {
		try {
			return (TypeFunction) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Type makeConst(int startPosition, int length, IStringTableHolder context) {
		TypeFunction tf = (TypeFunction) super.makeConst(startPosition, length, context);
		if (tf.postModifiers == null) {
			tf.postModifiers = new ArrayList<Modifier>();
		}
		tf.postModifiers.add(new Modifier(TOK.TOKconst, startPosition, length, 0));
		return tf;
	}
	
	@Override
	public Type makeInvariant(int startPosition, int length, IStringTableHolder context) {
		TypeFunction tf = (TypeFunction) super.makeInvariant(startPosition, length, context);
		if (tf.postModifiers == null) {
			tf.postModifiers = new ArrayList<Modifier>();
		}
		tf.postModifiers.add(new Modifier(TOK.TOKinvariant, startPosition, length, 0));
		return tf;
	}

	@SuppressWarnings("serial")
	private static class GotoL1 extends Exception {
	}

	private static final GotoL1 GOTO_L1 = new GotoL1();
	
	@Override
	protected void appendSignature0(StringBuilder sb, int options) {
		sb.append(linkageChar == 0 ? LINK.LINKd.mangleChar : linkageChar);
		if (parameters != null) {
			for(Argument arg : parameters) {
				appendArgumentSignature(arg, options, sb);
			}
		}
		sb.append((char) ('Z' - varargs));
		if (next == null) {
			sb.append(Signature.C_AUTO);
		} else {
			next.appendSignature(sb, options);
		}
	}
	
	private void appendArgumentSignature(Argument arg, int options, StringBuilder sb) {
		if (arg.type instanceof TypeTuple) {
			TypeTuple tuple = (TypeTuple) arg.type;
			for(Argument arg2 : tuple.arguments) {
				appendArgumentSignature(arg2, options, sb);
			}
		} else {
			arg.appendSignature(sb, options);
		}
	}
	
	// PERHAPS type *toCtype();
	// PERHAPS enum RET retStyle();
	// PERHAPS unsigned totym();
}
