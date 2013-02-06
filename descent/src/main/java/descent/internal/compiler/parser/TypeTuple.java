package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCin;
import static descent.internal.compiler.parser.STC.STCundefined;
import static descent.internal.compiler.parser.TY.Ttuple;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TypeTuple extends Type {

	public Arguments arguments;

	private TypeTuple() {
		super(TY.Ttuple);
	}

	public TypeTuple(Arguments arguments) {
		super(Ttuple);
		this.arguments = arguments;
	}
	
	public TypeTuple(Expressions exps, SemanticContext context) {
		super(Ttuple);
		Arguments arguments = new Arguments(size(exps));
		if (exps != null) {
			arguments.setDim(exps.size());
			for (int i = 0; i < exps.size(); i++) {
				Expression e = exps.get(i);
				if (e.type.ty == Ttuple) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.CannotFormTupleOfTuples, e, new String[] { toChars(context) }));
					}
				}
				Argument arg = new Argument(context.isD1() ? STCin : STCundefined, e.type, null, null);
				arguments.set(i, arg);
			}
		}
		this.arguments = arguments;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (null == o)
			return false;
		if (!(o instanceof Type))
			return false;

		Type t = (Type) o;
		if (t.ty == Ttuple) {
			TypeTuple tt = (TypeTuple) t;

			if (arguments.size() == tt.arguments.size()) {
				for (int i = 0; i < tt.arguments.size(); i++) {
					Argument arg1 = (Argument) arguments.get(i);
					Argument arg2 = (Argument) tt.arguments.get(i);

					if (!arg1.type.equals(arg2.type))
						return false;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public int getNodeType() {
		return TYPE_TUPLE;
	}

	@Override
	public Expression getProperty(char[] filename, int lineNumber, char[] ident, int start, int length,
			SemanticContext context) {
		Expression e;

		if (equals(ident, Id.length)) {
			e = new IntegerExp(filename, lineNumber, arguments.size(), Type.tsize_t);
		} else {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.NoPropertyForTuple, lineNumber, start, length, new String[] { new String(ident),
								toChars(context) }));
			}
			if (context.isD1()) {
				e = new IntegerExp(filename, lineNumber, 1, Type.tint32);
			} else {
				return new ErrorExp();
			}
		}
		return e;
	}

	@Override
	public TypeInfoDeclaration getTypeInfoDeclaration(SemanticContext context) {
		return new TypeInfoTupleDeclaration(this, context);
	}

	@Override
	public Type reliesOnTident() {
		if (null != arguments) {
			for (int i = 0; i < arguments.size(); i++) {
				Argument arg = (Argument) arguments.get(i);
				Type t = arg.type.reliesOnTident();
				if (null != t)
					return t;
			}
		}
		return null;
	}

	@Override
	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		if (null == deco)
			deco = merge(context).deco;

		/* Don't return merge(), because a tuple with one type has the
		 * same deco as that type.
		 */
		return this;
	}

	@Override
	public Type syntaxCopy(SemanticContext context) {
		Arguments args = Argument.arraySyntaxCopy(arguments, context);
		Type t = TypeTuple.newArguments(args);
	    t.mod = mod;
		t.copySourceRange(this);
		return t;
	}

	@Override
	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod, SemanticContext context) {
	    Argument.argsToCBuffer(buf, hgs, arguments, 0, context);
	}

	@Override
	public void toDecoBuffer(OutBuffer buf, int flag, SemanticContext context) {
		Type_toDecoBuffer(buf, flag, context);
		OutBuffer buf2 = new OutBuffer();
		Argument.argsToDecoBuffer(buf2, arguments, context);
		int len = buf2.data.length();
		buf.printf("" + len + buf2.extractData());
	}

	public static TypeTuple newArguments(Arguments arguments) {
		TypeTuple tt = new TypeTuple();
		tt.arguments = arguments;
		return tt;
	}

	public static TypeTuple newExpressions(Expressions exps,
			SemanticContext context) {
		TypeTuple tt = new TypeTuple();
		Arguments arguments = new Arguments(size(exps));
		if (exps != null) {
			arguments.setDim(exps.size());
			for (int i = 0; i < exps.size(); i++) {
				Expression e = exps.get(i);
				if (e.type.ty == TY.Ttuple) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.CannotFormTupleOfTuples, e));
					}
				}
				Argument arg = new Argument(STCin, e.type, null, null);
				arguments.set(i, arg);
			}
		}
		tt.arguments = arguments;
		return tt;
	}
	
	@Override
	protected void appendSignature0(StringBuilder sb, int options) {
		sb.append(Signature.C_TUPLE);
		sb.append(size(arguments));
		sb.append(Signature.C_TUPLE);
		for(Argument argument : arguments) {
			argument.type.appendSignature(sb, options);
		}
	}

}
