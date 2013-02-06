package descent.internal.compiler.parser;

import descent.core.Signature;

/**
 * Groups a bunch of methods that are shared amongst the source hierarchy
 * and resolved hierarchy.
 */
public class SemanticMixin {
	
	public static boolean equals(AggregateDeclaration c1, AggregateDeclaration c2) {
		if (c1 == null && c2 == null) {
			return true;
		}
		if ((c1 == null) != (c2 == null)) {
			return false;
		}
		if (c1 == c2) {
			return true;
		}
		if (c1.type == null || c2.type == null) {
			return false;
		}
		
		return c1.type.getSignature().equals(c2.type.getSignature());
	}
	
	public static String toString(Import node) {
		StringBuilder buffer = new StringBuilder();
		if (node.aliasId != null) {
			buffer.append(node.aliasId);
			buffer.append(" = ");
		}
		if (node.packages != null) {
			for(int i = 0; i < node.packages.size(); i++) {
				if (i > 0) {
					buffer.append('.');
				}
				buffer.append(node.packages.get(i));
			}
			buffer.append('.');
		}
		if (node.id != null) {
			buffer.append(node.id);
		}
		if (node.names != null) {
			buffer.append(" : ");
			for(int i = 0; i < node.names.size(); i++) {
				if (i > 0) {
					buffer.append(", ");
				}
				if (node.aliases.get(i) != null) {
					buffer.append(node.aliases.get(i));
					buffer.append(" = ");
				}
				buffer.append(node.names.get(i));
			}
		}
		return buffer.toString();
	}
	
	public static String getSignature(Dsymbol aThis, int options) {
		if (aThis.effectiveParent() == null) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		appendSignature(aThis, options, sb);		
		return sb.toString();
	}
	
	public static void appendSignature(Dsymbol aThis, int options, StringBuilder sb) {
		if (aThis instanceof TemplateMixin) {
			TemplateMixin mixin = (TemplateMixin) aThis;
			mixin.tempdecl.appendSignature(sb, options);
			mixin.appendInstanceSignature(sb, options);
			return;
		}
		
		Dsymbol parent = aThis.effectiveParent();
		if (parent == null) {
			return;
		}
		
		if (parent instanceof TemplateInstance) {
			TemplateInstance tempinst = (TemplateInstance) parent;
			TemplateDeclaration tempdecl = tempinst.tempdecl;
			while(tempdecl.parent instanceof TemplateInstance) {
				tempinst = (TemplateInstance) tempdecl.parent;
				tempdecl = tempinst.tempdecl;
			}
			tempdecl.parent.appendSignature(sb, options);
		} else if (aThis.templated()) {
			if (parent.effectiveParent() != null) {
				parent.effectiveParent().appendSignature(sb, options);	
			}
		} else {
			parent.appendSignature(sb, options);
		}
		
		appendNameSignature(aThis, options, sb);
	}
	
	public static void appendNameSignature(Dsymbol aThis, int options, StringBuilder sb) {
		if (aThis.ident == null || aThis.ident.ident == null || aThis.ident.ident.length == 0) {
			return;
		}
		
		if (isLocal(aThis)) {
			sb.append('$');
			sb.append(aThis.getStart());
		}
		
		if (aThis instanceof UnitTestDeclaration || 
			aThis instanceof InvariantDeclaration ||
			aThis instanceof StaticCtorDeclaration ||
			aThis instanceof StaticDtorDeclaration) {
			
			// Simplified signature for this symbols
			sb.append('$');
			sb.append(aThis.getStart());
			
			sb.append(aThis.getSignaturePrefix());
			
		} else {
			
			if (aThis instanceof TemplateInstance) {
				aThis = ((TemplateInstance) aThis).tempdecl;
			}
			
			if ((options & ISignatureOptions.TemplateInstanceParameters) == 0 ||
				!(aThis.parent instanceof TemplateInstance)) {
				sb.append(aThis.getSignaturePrefix());
				sb.append(aThis.ident.ident.length);
				sb.append(aThis.ident.ident);
			}
			
			if (aThis instanceof TemplateDeclaration) {
				TemplateDeclaration temp = (TemplateDeclaration) aThis;				
				if (temp.wrapper && temp.members.size() == 1 && temp.members.get(0) instanceof FuncDeclaration) {
					Dsymbol dsymbol = temp.members.get(0);
					dsymbol.type().appendSignature(sb, options);
				}
			}
			
			if ((options & ISignatureOptions.TemplateInstanceParameters) != 0
					&& aThis.parent instanceof TemplateInstance) {
				appendTemplateInstanceSignature(aThis, options, sb);
			} else if (aThis.templated() && aThis.parent instanceof TemplateDeclaration) {
				TemplateDeclaration tempdecl = (TemplateDeclaration) aThis.parent;
				for(TemplateParameter param : tempdecl.parameters) {
					param.appendSignature(sb, options);
				}
				sb.append(Signature.C_TEMPLATE_PARAMETERS_BREAK);
			} else if (aThis.templated() && aThis.parent instanceof TemplateInstance &&
					(options & ISignatureOptions.TemplateInstanceParameters) == 0) {
				TemplateDeclaration tempdecl = ((TemplateInstance) aThis.parent).tempdecl;
				for(TemplateParameter param : tempdecl.parameters) {
					param.appendSignature(sb, options);
				}
				sb.append(Signature.C_TEMPLATE_PARAMETERS_BREAK);
			}
			
			if (aThis instanceof TemplateDeclaration) {
				appendTemplateParameters((TemplateDeclaration) aThis, options, sb);
			}
			
			if (aThis instanceof FuncDeclaration && !(aThis.parent instanceof TemplateInstance)) {
				if (aThis.type() == null) {
					System.out.println(123456);
					return;
				}
				aThis.type().appendSignature(sb, options);
			}
		}
	}
	
	private static void appendTemplateInstanceSignature(Dsymbol aThis, int options, StringBuilder sb) {
		TemplateInstance tempinst = (TemplateInstance) aThis.parent;
		TemplateDeclaration tempdecl = tempinst.tempdecl;
		if (tempdecl.parent instanceof TemplateInstance) {
			appendTemplateInstanceSignature(tempdecl, options, sb);
		}
		
		if (aThis.templated()) {
			sb.append(aThis.getSignaturePrefix());
		} else {
			sb.append(Signature.C_TEMPLATE);
		}
		sb.append(tempinst.name.ident.length);
		sb.append(tempinst.name.ident);
		
		if (aThis instanceof FuncDeclaration) {
			aThis.type().appendSignature(sb, options);
		}
		
		appendTemplateParameters(tempdecl, options, sb);
		
		tempinst.appendInstanceSignature(sb, options);
		
		if (!aThis.templated()) {
			sb.append(aThis.getSignaturePrefix());
			sb.append(aThis.ident.ident.length);
			sb.append(aThis.ident);
		}
	}
	
	private static void appendTemplateParameters(TemplateDeclaration tempdecl, int options, StringBuilder sb) {
		for(TemplateParameter param : tempdecl.parameters) {
			param.appendSignature(sb, options);
		}
		sb.append(Signature.C_TEMPLATE_PARAMETERS_BREAK);
	}
	
	/*
	 * Determines if a symbol is local to a function.
	 */
	private static boolean isLocal(Dsymbol symbol) {
		return symbol.parent instanceof FuncDeclaration;
	}

}
