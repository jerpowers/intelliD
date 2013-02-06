package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ASTDmdNode.OverloadApply_fp;


public class Pvirtuals implements OverloadApply_fp
{
	public Expression e1;
	public Expressions exps;
	
	@Override
	public int call(Object param, FuncDeclaration f, SemanticContext context)
	{
		Pvirtuals p = (Pvirtuals) param;

	    if(f.isVirtual(context))
	    {
	    	Expression e;

			if (p.e1.op == TOK.TOKdotvar)
			{
				DotVarExp dve = (DotVarExp) p.e1;
			    e = new DotVarExp(null, 0, dve.e1, f);
			}
			else
			    e = new DsymbolExp(null, 0, f);
			p.exps.add(e);
	    }

		return 0;
	}
}
