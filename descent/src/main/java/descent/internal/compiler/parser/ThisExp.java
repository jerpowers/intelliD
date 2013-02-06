package descent.internal.compiler.parser;

import melnorme.utilbox.core.Assert;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class ThisExp extends Expression {

	public Declaration var;

	public ThisExp(char[] filename, int lineNumber) {
		super(filename, lineNumber, TOK.TOKthis);
		this.var = null;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return THIS_EXP;
	}

	@Override
	public boolean isBool(boolean result) {
		return result ? true : false;
	}

	@Override
	public boolean isLvalue(SemanticContext context) {
		return true;
	}

	@Override
	public void scanForNestedRef(Scope sc, SemanticContext context) {
		if (var == null) {
			throw new IllegalStateException("assert(var);");
		}
		var.isVarDeclaration().checkNestedReference(sc, null, 0, context);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		FuncDeclaration fd;
		@SuppressWarnings("unused")
		FuncDeclaration fdthis;
		@SuppressWarnings("unused")
		int nested = 0;

		if (type != null) { // assert(global.errors || var);
			return this;
		}

		/*
		 * Special case for typeof(this) and typeof(super) since both should
		 * work even if they are not inside a non-static member function
		 */
		if (sc.intypeof != 0) {
			// Find enclosing struct or class
			for (Dsymbol s = sc.parent; true; s = s.parent) {
				ClassDeclaration cd;
				StructDeclaration sd;

				if (s == null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ThisNotInClassOrStruct, this));
					}
					// goto Lerr;
					return semantic_Lerr(sc, context);
				}
				cd = s.isClassDeclaration();
				if (cd != null) {
					type = cd.type;
					return this;
				}
				sd = s.isStructDeclaration();
				if (sd != null) {
					if (context.isD1()) {
						type = sd.type.pointerTo(context);
					} else if (context.isD2()) {
						if (context.STRUCTTHISREF()) {
							type = sd.type;
						} else {
							type = sd.type.pointerTo(context);
						}
					}
					return this;
				}
			}
		}

		fdthis = sc.parent.isFuncDeclaration();
		fd = hasThis(sc); // fd is the uplevel function with the 'this'
		// variable
		if (fd == null) {
			// goto Lerr;
			return semantic_Lerr(sc, context);
		}

		Assert.isNotNull(fd.vthis());
		var = fd.vthis();
		Assert.isNotNull(var.parent);
		type = var.type;
		var.isVarDeclaration().checkNestedReference(sc, filename, lineNumber, context);
		if (0 == sc.intypeof) {
			sc.callSuper |= Scope.CSXthis;
		}
		return this;
	}

	public Expression semantic_Lerr(Scope sc, SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.ThisOnlyAllowedInNonStaticMemberFunctions, this));
		}
		if (context.isD2()) {
			type = Type.terror;
		} else {
			type = Type.tint32;
		}
		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("this");
	}

	@Override
	public Expression toLvalue(Scope sc, Expression e, SemanticContext context) {
		return this;
	}

	@Override
	public void setResolvedSymbol(Dsymbol symbol, SemanticContext context) {
		context.setResolvedSymbol(this, symbol);
	}

}
