package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEfallthru;
import static descent.internal.compiler.parser.BE.BEhalt;
import static descent.internal.compiler.parser.BE.BEthrow;
import static descent.internal.compiler.parser.TOK.TOKassert;
import static descent.internal.compiler.parser.TOK.TOKdeclaration;
import static descent.internal.compiler.parser.TOK.TOKhalt;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class ExpStatement extends Statement {

	public Expression exp, sourceExp;

	public ExpStatement(char[] filename, int lineNumber, Expression exp) {
		super(filename, lineNumber);
		this.exp = exp;
		this.sourceExp = exp;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceExp);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		int result = BEfallthru;

		if (exp != null) {
			if (exp.op == TOKhalt) {
				return BEhalt;
			}
			
			if (exp.op == TOKassert) {
				AssertExp a = (AssertExp) exp;

				if (a.e1.isBool(false)) {// if it's an assert(0)
					return BEhalt;
				}
			}
			if (exp.canThrow(context)) {
				result |= BEthrow;
			}
		}
		return result;
	}

	@Override
	public int getNodeType() {
		return EXP_STATEMENT;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		// START()
		if (istate.start != null) {
			if (istate.start != this) {
				return null;
			}
			istate.start = null;
		}
		// START()
		if (exp != null) {
			Expression e = exp.interpret(istate, context);
			if (e == EXP_CANT_INTERPRET) {
				return EXP_CANT_INTERPRET;
			}
		}
		return null;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		if (exp != null) {
			exp = exp.semantic(sc, context);
			exp = ASTDmdNode.resolveProperties(sc, exp, context);
			exp.copySourceRange(this);			
			exp.checkSideEffect(0, context);
			exp = exp.optimize(0, context);
			if (exp.op == TOKdeclaration && null == isDeclarationStatement())
			{   
				Statement s = new DeclarationStatement(filename, lineNumber, exp);
			    return s;
			}
		}
		
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		Expression e = exp != null ? exp.syntaxCopy(context) : null;
		ExpStatement es = context.newExpStatement(filename, lineNumber, e);
		es.copySourceRange(this);
		return es;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (exp != null) {
			exp.toCBuffer(buf, hgs, context);
		}
		buf.writeByte(';');
		if (0 == hgs.FLinit.init) {
			buf.writenl();
		}
	}

}
