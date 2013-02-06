package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCgshared;
import static descent.internal.compiler.parser.STC.STCstatic;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class TypeInfoDeclaration extends VarDeclaration {
	
	public Type tinfo;
	
	public TypeInfoDeclaration(Type tinfo, int internal, SemanticContext context) {
		super(null, 0, 
				context.Type_typeinfo.type, 
				tinfo.getTypeInfoIdent(internal), 
				null);
		this.tinfo = tinfo;
		if (context.isD1()) {
			this.storage_class = STCstatic;
		} else {
			this.storage_class = STCstatic | STCgshared;
		}
		this.protection = PROT.PROTpublic;
		this.linkage = LINK.LINKc;
	}
	
	@Override
	public void accept0(IASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
	
	@Override
	public void semantic(Scope sc, SemanticContext context) {
		if (linkage != LINK.LINKc) {
			throw new IllegalStateException("assert(linkage == LINKc);");
		}
	}
	
	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		throw new IllegalStateException("assert(0);"); // should never be produced by syntax
	}

}
