package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCin;

import melnorme.utilbox.core.Assert;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class TupleDeclaration extends Declaration {

	public Objects objects;
	public boolean isexp; // true: expression tuple
	public TypeTuple tupletype; // !=NULL if this is a type tuple

	public TupleDeclaration(char[] filename, int lineNumber, IdentifierExp ident, Objects objects) {
		super(ident);
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.type = null;
		this.objects = objects;
		this.isexp = false;
		this.tupletype = null;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return TUPLE_DECLARATION;
	}

	@Override
	public Type getType(SemanticContext context) {
		/*
		 * If this tuple represents a type, return that type
		 */

		if (isexp) {
			return null;
		}
		if (tupletype == null) {
			/*
			 * It's only a type tuple if all the Object's are types
			 */
			for (int i = 0; i < size(objects); i++) {
				ASTDmdNode o = objects.get(i);

				if (o.dyncast() != DYNCAST.DYNCAST_TYPE) {
					return null;
				}
			}

			/*
			 * We know it's a type tuple, so build the TypeTuple
			 */
			int sizeObjects = size(objects);
			Arguments args = new Arguments(sizeObjects);
			args.setDim(sizeObjects);
			for (int i = 0; i < sizeObjects; i++) {
				Type t = (Type) objects.get(i);


				Argument arg;
				if (context.isD2()) {
					arg = new Argument(0, t, null, null);
				} else {
					arg = new Argument(STCin, t, null, null);
				}
				args.set(i, arg);
			}

			tupletype = TypeTuple.newArguments(args);
		}

		return tupletype;
	}

	@Override
	public TupleDeclaration isTupleDeclaration() {
		return this;
	}

	@Override
	public String kind() {
		return "tuple";
	}

	@Override
	public boolean needThis() {
		for (int i = 0; i < size(objects); i++) {
			ASTDmdNode o = objects.get(i);
			if (o.dyncast() == DYNCAST.DYNCAST_EXPRESSION) {
				Expression e = (Expression) o;
				if (e.op == TOK.TOKdsymbol) {
					DsymbolExp ve = (DsymbolExp) e;
					Declaration d = ve.s.isDeclaration();
					if (d != null && d.needThis()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		Assert.isTrue(false);
		return null;
	}

}
