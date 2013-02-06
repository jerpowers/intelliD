package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCconst;
import static descent.internal.compiler.parser.STC.STCstatic;
import static descent.internal.compiler.parser.TOK.TOKarrayliteral;
import static descent.internal.compiler.parser.TOK.TOKindex;
import static descent.internal.compiler.parser.TOK.TOKslice;
import static descent.internal.compiler.parser.TOK.TOKstring;
import static descent.internal.compiler.parser.TOK.TOKtuple;
import static descent.internal.compiler.parser.TOK.TOKtype;
import static descent.internal.compiler.parser.TOK.TOKvar;
import static descent.internal.compiler.parser.TY.Ttuple;

import melnorme.utilbox.core.Assert;

import descent.internal.compiler.parser.ast.IASTVisitor;

public class ArrayScopeSymbol extends ScopeDsymbol {

	public Expression exp;
	public TypeTuple type; // for tuple[length]
	public TupleDeclaration td; // for tuples of objects
	public Scope sc;

	public ArrayScopeSymbol(Scope sc, Expression e) {
		Assert.isTrue(e.op == TOK.TOKindex || e.op == TOK.TOKslice);
		this.exp = e;
		this.sc = sc;
	}

	public ArrayScopeSymbol(Scope sc, TupleDeclaration s) {
		this.exp = null;
		this.type = null;
		this.td = s;
		this.sc = sc;
	}

	public ArrayScopeSymbol(Scope sc, TypeTuple t) {
		this.exp = null;
		this.type = t;
		this.td = null;
		this.sc = sc;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return ARRAY_SCOPE_SYMBOL;
	}

	@Override
	public ArrayScopeSymbol isArrayScopeSymbol() {
		return this;
	}

	@Override
	public Dsymbol search(char[] filename, int lineNumber, char[] ident, int flags,
			SemanticContext context) {
		if (equals(ident, Id.length)
				|| equals(ident, Id.dollar)) {
			Expression pvar;
			Expression ce;

			// L1:

			boolean loop = true;
			while (loop) {
				loop = false;

				if (td != null) {
					VarDeclaration v = new VarDeclaration(filename, lineNumber, Type.tsize_t,
							Id.dollar, null);
					Expression e = new IntegerExp(null, 0, td.objects.size(),
							Type.tsize_t);
					v.init = new ExpInitializer(null, 0, e);
					if (context.isD2()) {
						v.storage_class |= STCstatic | STCconst;
						v.semantic(sc, context);
					} else {
						v.storage_class |= STCconst;
					}
					return v;
				}

				if (type != null) {
					VarDeclaration v = new VarDeclaration(filename, lineNumber, Type.tsize_t,
							Id.dollar, null);
					Expression e = new IntegerExp(null, 0, type.arguments
							.size(), Type.tsize_t);
					v.init = new ExpInitializer(null, 0, e);
					if (context.isD2()) {
						v.storage_class |= STCstatic | STCconst;
						v.semantic(sc, context);
					} else {
						v.storage_class |= STCconst;
					}
					return v;
				}

				if (exp.op == TOKindex) {
					IndexExp ie = (IndexExp) exp;

					// TODO semantic I think this logic is ok
					// pvar = &ie.lengthVar;
					pvar = ie;
					ce = ie.e1;
				} else if (exp.op == TOKslice) {
					SliceExp se = (SliceExp) exp;

					// TODO semantic I think this logic is ok
					// pvar = &se.lengthVar;
					pvar = se;
					ce = se.e1;
				} else {
					return null;
				}

				if (ce.op == TOKtype) {
					Type t = ((TypeExp) ce).type;
					if (t.ty == Ttuple) {
						type = (TypeTuple) t;
						// goto L1;
						loop = true;
						continue;
					}
				}

				boolean enterIf;
				if (pvar instanceof IndexExp) {
					enterIf = ((IndexExp) pvar).lengthVar == null;
				} else {
					enterIf = ((SliceExp) pvar).lengthVar == null;
				}
				if (enterIf) {
					VarDeclaration v = new VarDeclaration(filename, lineNumber, Type.tsize_t,
							Id.dollar, null);

					if (context.isD2()) {
						if (ce.op == TOKvar) { // if ce is const, get its initializer
							ce = fromConstInitializer(
									WANTvalue | WANTinterpret, ce, context);
						}
					}

					if (ce.op == TOKstring) {
						Expression e = new IntegerExp(null, 0,
								((StringExp) ce).len, Type.tsize_t);
						v.init = new ExpInitializer(null, 0, e);
						if (context.isD2()) {
							v.storage_class |= STCstatic | STCconst;
						} else {
							v.storage_class |= STCconst;
						}
					} else if (ce.op == TOKarrayliteral) {
						/* It is for an array literal, so the
						 * length will be a const.
						 */
						Expression e = new IntegerExp(null, 0,
								((ArrayLiteralExp) ce).elements.size(),
								Type.tsize_t);
						v.init = new ExpInitializer(null, 0, e);
						if (context.isD2()) {
							v.storage_class |= STCstatic | STCconst;
						} else {
							v.storage_class |= STCconst;
						}
					} else if (ce.op == TOKtuple) {
						Expression e = new IntegerExp(filename, lineNumber, ((TupleExp) ce).exps
								.size(), Type.tsize_t);
						v.init = new ExpInitializer(filename, lineNumber, e);
						if (context.isD2()) {
							v.storage_class |= STCstatic | STCconst;
						} else {
							v.storage_class |= STCconst;
						}
					}

					// TODO semantic I think this logic is ok
					// *pvar = v;
					if (pvar instanceof IndexExp) {
						((IndexExp) pvar).lengthVar = v;
					} else {
						((SliceExp) pvar).lengthVar = v;
					}
				}

				// TODO semantic I think this logic is ok
				// return *pvar;
				if (pvar instanceof IndexExp) {
					if (context.isD2()) {
						((IndexExp) pvar).lengthVar.semantic(sc, context);
					}
					return ((IndexExp) pvar).lengthVar;
				} else {
					if (context.isD2()) {
						((SliceExp) pvar).lengthVar.semantic(sc, context);
					}
					return ((SliceExp) pvar).lengthVar;
				}
			}
		}
		return null;
	}

}
