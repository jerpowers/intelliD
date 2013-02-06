package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TOK.TOKeof;
import static descent.internal.compiler.parser.TOK.TOKstring;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class CompileExp extends UnaExp {

	public CompileExp(char[] filename, int lineNumber, Expression e) {
		super(filename, lineNumber, TOK.TOKmixin, e);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, e1);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int getNodeType() {
		return COMPILE_EXP;
	}
	
	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		super.semantic(sc, context);
		e1 = resolveProperties(sc, e1, context);
		e1 = e1.optimize(WANTvalue | WANTinterpret, context);
		if (e1.op != TOKstring) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.ArgumentToMixinMustBeString, this, e1.toChars(context)));
			}
			type = Type.terror;
			return this;
		}
		
		StringExp se = (StringExp) e1;
		se = se.toUTF8(sc, context);
		Parser p = new Parser(context.Module_rootModule.apiLevel, se.string);
		p.filename = filename;
		p.lineNumber = lineNumber;
		p.nextToken();
		Expression e = p.parseExpression();
		
		// TODO semantic do this better
		if (p.problems != null) {
			for (int i = 0; i < p.problems.size(); i++) {
				Problem problem = (Problem) p.problems.get(i);
				problem.setSourceStart(start);
				problem.setSourceEnd(start + length - 1);
				context.acceptProblem(problem);
			}
		}
		
		if (p.token.value != TOKeof) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(IProblem.IncompleteMixinDeclaration, this, se.toChars(context)));
			}
		}
		return e.semantic(sc, context);
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs, SemanticContext context) {
		buf.writestring("mixin(");
	    expToCBuffer(buf, hgs, e1, PREC.PREC_assign, context);
	    buf.writeByte(')');
	}

}
