package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHexact;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TemplateTupleParameter extends TemplateParameter {

	public TemplateTupleParameter(char[] filename, int lineNumber, IdentifierExp ident) {
		super(filename, lineNumber, ident);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
		}
		visitor.endVisit(this);
	}

	@Override
	public void declareParameter(Scope sc, SemanticContext context) {
		TypeIdentifier ti = new TypeIdentifier(filename, lineNumber, ident);
		sparam = new AliasDeclaration(filename, lineNumber, ident, ti);
		
		// Descent
		((AliasDeclaration) sparam).isTemplateParameter = true;
		
		if (null == sc.insert(sparam)) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.ParameterMultiplyDefined, ident, new String[] { new String(ident.ident) }));
			}
		}
	}

	@Override
	public ASTDmdNode defaultArg(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		return null;
	}

	@Override
	public ASTDmdNode dummyArg(SemanticContext context) {
		return null;
	}

	@Override
	public int getNodeType() {
		return TEMPLATE_TUPLE_PARAMETER;
	}

	@Override
	public TemplateTupleParameter isTemplateTupleParameter() {
		return this;
	}

	@Override
	public MATCH matchArg(Scope sc, Objects tiargs, int i,
			TemplateParameters parameters, Objects dedtypes,
			Declaration[] psparam, int flags, SemanticContext context) {
		/* The rest of the actual arguments (tiargs[]) form the match
		 * for the variadic parameter.
		 */
		if (!(i + 1 == size(dedtypes))) {
			throw new IllegalStateException("assert (i + 1 == dedtypes.size());"); // must be the last one
		}
		Tuple ovar;
		if (i + 1 == size(tiargs) && isTuple(tiargs.get(i)) != null) {
			ovar = isTuple(tiargs.get(i));
		} else {
			ovar = new Tuple();
			if (i < size(tiargs)) {
				ovar.objects.setDim(size(tiargs) - i);
				for (int j = 0; j < size(ovar.objects); j++) {
					ovar.objects.set(j, tiargs.get(i + j));
				}
			}
		}
		psparam[0] = new TupleDeclaration(filename, lineNumber, ident, ovar.objects);
		dedtypes.set(i, ovar);
		return MATCHexact;
	}

	@Override
	public int overloadMatch(TemplateParameter tp) {
		TemplateTupleParameter tvp = tp.isTemplateTupleParameter();

		if (tvp != null) {
			return 1; // match
		}

		return 0;
	}
	
	@Override
	public void semantic(Scope sc, SemanticContext context) {

	}
	
	@Override
	public ASTDmdNode specialization() {
		return null;
	}
	
	@Override
	public TemplateParameter syntaxCopy(SemanticContext context) {
		TemplateTupleParameter tp = new TemplateTupleParameter(filename, lineNumber, ident);
		return tp;
	}
	
	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
		buf.writestring(ident.toChars());
	    buf.writestring("...");
	}
	
	@Override
	public void appendSignature(StringBuilder sb, int options) {
		sb.append(Signature.C_TEMPLATE_TUPLE_PARAMETER);
	}
	
	@Override
	public char[] getDefaultValue() {
		return null;
	}

}
