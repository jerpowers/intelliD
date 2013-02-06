package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tstruct;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class StructLiteralExp extends Expression {

	public StructDeclaration sd; // which aggregate this is for
	public Expressions elements; // parallels sd->fields[] with
	// NULL entries for fields to skip

	// Symbol *sym;		// back end symbol to initialize with literal
	public int soffset; // offset from start of s
	public int fillHoles; // fill alignment 'holes' with zero

	public StructLiteralExp(char[] filename, int lineNumber, StructDeclaration sd, Expressions elements) {
		super(filename, lineNumber, TOK.TOKstructliteral);
		this.sd = sd;
		this.elements = elements;
		// this.sym = null;
		this.soffset = 0;
		this.fillHoles = 1;
	}

	@Override
	protected void accept0(IASTVisitor visitor) {
	}
	
	@Override
	public boolean canThrow(SemanticContext context) {
		if (context.isD2()) {
			return arrayExpressionCanThrow(elements, context);	
		} else {
			return super.canThrow(context);
		}
	}

	@Override
	public int checkSideEffect(int flag, SemanticContext context) {
		int f = 0;

		for (int i = 0; i < elements.size(); i++) {
			Expression e = elements.get(i);
			if (null == e) {
				continue;
			}

			f |= e.checkSideEffect(2, context);
		}
		if (flag == 0 && f == 0) {
			super.checkSideEffect(0, context);
		}
		return f;
	}

	public Expression getField(Type type, int offset, SemanticContext context) {
		Expression e = null;
		int i = getFieldIndex(type, offset, context);

		if (i != -1) {
			e = elements.get(i);
			if (e != null) {
				e = e.copy();
				e.type = type;
			}
		}
		return e;
	}

	public int getFieldIndex(Type type, int offset, SemanticContext context) {
		boolean condition;
		if (context.isD1()) {
			condition = true;
		} else {
			condition = size(elements) > 0;
		}
		
		/* Find which field offset is by looking at the field offsets
		 */
		if (condition) {
			for (int i = 0; i < sd.fields.size(); i++) {
				Dsymbol s = sd.fields.get(i);
				VarDeclaration v = s.isVarDeclaration();
				if (v == null) {
					throw new IllegalStateException("assert(v);");
				}
	
				if (offset == v.offset()
						&& type.size(context) == v.type.size(context)) {
					Expression e = elements.get(i);
					if (e != null) {
						return i;
					}
					break;
				}
			}
		}
		return -1;
	}

	@Override
	public int getNodeType() {
		return STRUCT_LITERAL_EXP;
	}
	
	@Override
	public MATCH implicitConvTo(Type t, SemanticContext context) {
		if (context.isD2()) {
			MATCH m = super.implicitConvTo(t, context);
			if (m != MATCHnomatch) {
				return m;
			}
			if (type.ty == t.ty && type.ty == Tstruct
					&& ((TypeStruct) type).sym == ((TypeStruct) t).sym) {
				m = MATCHconst;
				for (int i = 0; i < size(elements); i++) {
					Expression e = elements.get(i);
					Type te = e.type;
					if (t.mod == 0) {
						te = te.mutableOf(context);
					} else {
						assert (t.mod == Type.MODinvariant);
						te = te.invariantOf(context);
					}
					MATCH m2 = e.implicitConvTo(te, context);
					if (m2.ordinal() < m.ordinal()) {
						m = m2;
					}
				}
			}
			return m;
		} else {
			return super.implicitConvTo(t, context);
		}
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		Expressions expsx = null;

		/* We don't know how to deal with overlapping fields
		 */
		if (sd.hasUnions != 0) {
			return EXP_CANT_INTERPRET;
		}

		if (elements != null) {
			for (int i = 0; i < elements.size(); i++) {
				Expression e = elements.get(i);
				if (null == e) {
					continue;
				}

				Expression ex = e.interpret(istate, context);
				if (ex == EXP_CANT_INTERPRET) {
					expsx = null;
					return EXP_CANT_INTERPRET;
				}

				/* If any changes, do Copy On Write
				 */
				if (ex != e) {
					if (null == expsx) {
						expsx = new Expressions(elements.size());
						expsx.setDim(elements.size());
						for (int j = 0; j < elements.size(); j++) {
							expsx.set(j, elements.get(j));
						}
					}
					expsx.set(i, ex);
				}
			}
		}
		if (elements != null && expsx != null) {
			expandTuples(expsx, context);
			if (expsx.size() != elements.size()) {
				expsx = null;
				return EXP_CANT_INTERPRET;
			}
			StructLiteralExp se = new StructLiteralExp(filename, lineNumber, sd, expsx);
			se.type = type;
			return se;
		}
		return this;
	}
	
	@Override
	public boolean isLvalue(SemanticContext context) {
		return true;
	}

	@Override
	public Expression optimize(int result, SemanticContext context) {
		if (elements != null) {
			for (int i = 0; i < elements.size(); i++) {
				Expression e = elements.get(i);
				if (null == e) {
					continue;
				}
				e = e.optimize(WANTvalue | (result & WANTinterpret), context);
				elements.set(i, e);
			}
		}
		return this;
	}

	@Override
	public void scanForNestedRef(Scope sc, SemanticContext context) {
		arrayExpressionScanForNestedRef(sc, elements, context);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression e;
	    int nfields = 0;

		// Need to know the real fields, so unlazy here
		sd = (StructDeclaration) sd.unlazy(context);
	    
	    if (context.isD2()) {
	    	nfields = size(sd.fields) - (sd.isnested ? 1 : 0);
	    } else {
	    	nfields = size(sd.fields);
	    }
		
		if (type != null) {
			return this;
		}

		// Run semantic() on each element
		for (int i = 0; i < elements.size(); i++) {
			e = elements.get(i);
			if (null == e) {
				continue;
			}
			e = e.semantic(sc, context);
			elements.set(i, e);
		}
		expandTuples(elements, context);
		
		int offset = 0;
		for (int i = 0; i < elements.size(); i++) {
			e = elements.get(i);
			if (null == e) {
				continue;
			}

			if (null == e.type) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.SymbolHasNoValue, e, new String[] { e.toChars(context) }));
				}
			}
			e = resolveProperties(sc, e, context);
			if (i >= nfields) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.MoreInitiailizersThanFields, this, new String[] { sd.toChars(context) }));
				}
				break;
			}
			Dsymbol s = sd.fields.get(i);
			VarDeclaration v = s.isVarDeclaration();
			if (v == null) {
				throw new IllegalStateException("assert(v);");
			}
			if (v.offset() < offset) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.OverlappingInitiailization, this, new String[] { v.toChars(context) }));
				}
			}
			offset = v.offset() + v.type.size(context);

			Type telem = v.type;
			while (null == e.implicitConvTo(telem, context)
					&& telem.toBasetype(context).ty == Tsarray) { /* Static array initialization, as in:
			 *	T[3][5] = e;
			 */
				telem = telem.toBasetype(context).nextOf();
			}

			e = e.implicitCastTo(sc, telem, context);

			elements.set(i, e);
		}

		/* Fill out remainder of elements[] with default initializers for fields[]
		 */
		for (int i = elements.size(); i < nfields; i++) {
			Dsymbol s = sd.fields.get(i);
			VarDeclaration v = s.isVarDeclaration();
			if (v == null) {
				throw new IllegalStateException("assert(v);");
			}

			if (v.offset() < offset) {
				e = null;
				sd.hasUnions = 1;
			} else {
				if (v.init() != null) {
					e = v.init().toExpression(context);
					if (null == e) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.CannotMakeExpressionOutOfInitializer, this, new String[] { v.toChars(context) }));
						}
					}
				} else {
					e = v.type.defaultInit(context);
					e.filename = filename;
					e.lineNumber = lineNumber;
				}
				offset = v.offset() + v.type.size(context);
			}
			elements.add(e);
		}

		type = sd.type;
		return this;
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		return new StructLiteralExp(filename, lineNumber, sd, arraySyntaxCopy(elements, context));
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring(sd.toChars(context));
		buf.writeByte('(');
		argsToCBuffer(buf, elements, hgs, context);
		buf.writeByte(')');
	}

	@Override
	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
		return this;
	}

	@Override
	public void toMangleBuffer(OutBuffer buf, SemanticContext context) {
		int dim = elements != null ? elements.size() : 0;
		buf.writestring("S");
		buf.writestring(dim);
		for (int i = 0; i < dim; i++) {
			Expression e = elements.get(i);
			if (e != null) {
				e.toMangleBuffer(buf, context);
			} else {
				buf.writeByte('v'); // 'v' for void
			}
		}
	}

}
