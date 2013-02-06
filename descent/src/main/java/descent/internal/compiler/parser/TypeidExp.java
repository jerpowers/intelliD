package descent.internal.compiler.parser;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TypeidExp extends Expression {

	public Type typeidType, sourceTypeidType; // This can be null if typeid argument is an expression
	public Expression argumentExp__DDT_ADDITION; // BM: Added for DMD 2.050 code

	public TypeidExp(char[] filename, int lineNumber, Type typeidType) {
		super(filename, lineNumber, TOK.TOKtypeid);
		this.typeidType = this.sourceTypeidType = typeidType;
	}
	
	public TypeidExp(char[] filename, int lineNumber, Object argument) {
		super(filename, lineNumber, TOK.TOKtypeid);
		assertNotNull(argument);
		if(argument instanceof Type) {
			this.typeidType = this.sourceTypeidType = (Type) argument;
		} else {
			assertTrue(argument instanceof Expression);
			this.argumentExp__DDT_ADDITION = (Expression) argument;
		}
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceTypeidType);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return TYPEID_EXP;
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Expression e;
		typeidType = typeidType.semantic(filename, lineNumber, sc, context);
		e = typeidType.getTypeInfo(sc, context);
		if (e.lineNumber == 0) {
			e.lineNumber = lineNumber;		// so there's at least some line number info
		}
		return e;
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		return new TypeidExp(filename, lineNumber, typeidType.syntaxCopy(context));
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("typeid(");
		typeidType.toCBuffer(buf, null, hgs, context);
		buf.writeByte(')');
	}

}
