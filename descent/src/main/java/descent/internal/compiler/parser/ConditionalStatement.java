package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class ConditionalStatement extends Statement {

	public Condition condition, sourceCondition;
	public Statement ifbody, sourceIfbody;
	public Statement elsebody, sourceElsebody;

	public ConditionalStatement(char[] filename, int lineNumber, Condition condition, Statement ifbody,
			Statement elsebody) {
		super(filename, lineNumber);
		this.condition = this.sourceCondition = condition;
		this.ifbody = this.sourceIfbody = ifbody;
		this.elsebody = this.sourceElsebody = elsebody;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceCondition);
			TreeVisitor.acceptChildren(visitor, sourceIfbody);
			TreeVisitor.acceptChildren(visitor, sourceElsebody);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
	    int result = ifbody.blockExit(context);
		if (elsebody != null)
			result |= elsebody.blockExit(context);
		return result;
	}

	@Override
	public Statements flatten(Scope sc, SemanticContext context) {
		Statement s;

		if (condition.include(sc, null, context)) {
			s = ifbody;
		} else {
			s = elsebody;
		}

		Statements a = new Statements(1);
		if (s != null) {
			a.add(s);
		}
		return a;
	}

	@Override
	public int getNodeType() {
		return CONDITIONAL_STATEMENT;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		if (condition.include(sc, null, context)) {
			ifbody = ifbody.semantic(sc, context);
			return ifbody;
		} else {
			if (elsebody != null) {
				elsebody = elsebody.semantic(sc, context);
			}
			return elsebody;
		}
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		Statement e = null;
		if (elsebody != null) {
			e = elsebody.syntaxCopy(context);
		}
		ConditionalStatement s = context.newConditionalStatement(filename, lineNumber, condition
				.syntaxCopy(context), ifbody.syntaxCopy(context), e);
		s.copySourceRange(this);
		return s;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		condition.toCBuffer(buf, hgs, context);
		buf.writenl();
		buf.writeByte('{');
		buf.writenl();
		if (ifbody != null) {
			ifbody.toCBuffer(buf, hgs, context);
		}
		buf.writeByte('}');
		buf.writenl();
		if (elsebody != null) {
			buf.writestring("else");
			buf.writenl();
			buf.writeByte('{');
			buf.writenl();
			elsebody.toCBuffer(buf, hgs, context);
			buf.writeByte('}');
			buf.writenl();
		}
		buf.writenl();
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		return (ifbody != null && ifbody.usesEH(context))
				|| (elsebody != null && elsebody.usesEH(context));
	}

}
