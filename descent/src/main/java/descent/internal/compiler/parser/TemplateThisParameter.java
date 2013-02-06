package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class TemplateThisParameter extends TemplateTypeParameter {

	public TemplateThisParameter(char[] filename, int lineNumber, IdentifierExp ident, Type specType,
			Type defaultType) {
		super(filename, lineNumber, ident, specType, defaultType);
	}
	
	@Override
	public int getNodeType() {
		return TEMPLATE_THIS_PARAMETER;
	}

	@Override
	public TemplateThisParameter isTemplateThisParameter() {
		return this;
	}

	@Override
	public TemplateParameter syntaxCopy(SemanticContext context) {
		TemplateThisParameter tp = new TemplateThisParameter(filename, lineNumber, ident,
				specType, defaultType);
		if (tp.specType != null)
			tp.specType = specType.syntaxCopy(context);
		if (defaultType != null)
			tp.defaultType = defaultType.syntaxCopy(context);
		tp.copySourceRange(this);
		return tp;
	}
	
	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
	    buf.writestring("this ");
	    super.toCBuffer(buf, hgs, context);
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, sourceSpecType);
			TreeVisitor.acceptChildren(visitor, sourceDefaultType);
		}
		visitor.endVisit(this);
	}

}
