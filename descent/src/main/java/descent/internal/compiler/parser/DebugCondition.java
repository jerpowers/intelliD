package descent.internal.compiler.parser;

import descent.internal.compiler.parser.ast.IASTVisitor;


public class DebugCondition extends DVCondition {

	public DebugCondition(Module mod, char[] filename, int lineNumber, long level, char[] id) {
		super(mod, filename, lineNumber, level, id);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
		}
		visitor.endVisit(this);
	}

	public void addGlobalIdent(IdentifierExp ident, SemanticContext context) {
		if (null == context.global.params.debugids) {
			context.global.params.debugids = new HashtableOfCharArrayAndObject();
		}
		context.global.params.debugids.put(ident.ident, this);
	}

	@Override
	public int getConditionType() {
		return DEBUG;
	}

	@Override
	public boolean include(Scope sc, ScopeDsymbol s, SemanticContext context) {
		if (inc == 0) {
			inc = 2;
			if (ident != null) {
				if (findCondition(mod.debugids, ident)) {
					inc = 1;
				} else if (findCondition(context.global.params.debugids, ident)) {
					inc = 1;
				} else {
					if (null == mod.debugidsNot) {
						mod.debugidsNot = new HashtableOfCharArrayAndObject();
					}
					mod.debugidsNot.put(ident, this);
				}
			} else if (level <= context.global.params.debuglevel
					|| level <= mod.debuglevel) {
				inc = 1;
			}
		}
		return (inc == 1);
	}

	public void setGlobalLevel(long level, SemanticContext context) {
		context.global.debugLevel = level;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (ident != null) {
			buf.writestring("debug (");
			buf.writestring(ident);
			buf.writestring(")");
		} else {
			buf.writestring("debug (");
			buf.writestring(level);
			buf.writestring(")");
		}
	}

	@Override
	public char[] toCharArray() {
		if (ident != null) {
			return ident;
		} else {
			return String.valueOf(level).toCharArray();
		}
	}

}
