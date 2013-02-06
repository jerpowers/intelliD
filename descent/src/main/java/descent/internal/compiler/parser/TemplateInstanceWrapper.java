package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TemplateInstanceWrapper extends IdentifierExp {

	public TemplateInstance tempinst;

	public TemplateInstanceWrapper(char[] filename, int lineNumber, TemplateInstance tempinst) {
		super(filename, lineNumber);
		this.tempinst = tempinst;
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, tempinst);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public DYNCAST dyncast() {
		return DYNCAST.DYNCAST_DSYMBOL;
	}
	
	@Override
	public int getNodeType() {
		return TEMPLATE_INSTANCE_WRAPPER;
	}
	
	@Override
	protected void appendSignature(StringBuilder sb, int options) {
		tempinst.appendSignature(sb, options);
	}

}
