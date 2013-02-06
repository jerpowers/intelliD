package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class DebugSymbol extends Dsymbol {

	public long level;
	public Version version;

	public DebugSymbol(char[] filename, int lineNumber, IdentifierExp ident, Version version) {
		super(ident);
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.version = version;
	}

	public DebugSymbol(char[] filename, int lineNumber, long level, Version version) {
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.level = level;
		this.version = version;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, version);
		}
		visitor.endVisit(this);
	}

	@Override
	public int addMember(Scope sc, ScopeDsymbol sd, int memnum,
			SemanticContext context) {
		Module m;

		// Do not add the member to the symbol table,
		// just make sure subsequent debug declarations work.
		m = sd.isModule();
		if (ident != null) {
			if (null == m) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.DebugDeclarationMustBeAtModuleLevel, this));
				}
			} else {
				if (findCondition(m.debugidsNot, ident)) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(IProblem.DebugDefinedAfterUse, this, ident.toString()));
					}
				}
				if (null == m.debugids) {
					m.debugids = new HashtableOfCharArrayAndObject();
				}
				m.debugids.put(ident.ident, this);
			}
		} else {
			if (null == m) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.DebugDeclarationMustBeAtModuleLevel, this));
				}
			} else {
				m.debuglevel = level;
			}
		}
		return 0;
	}

	@Override
	public int getNodeType() {
		return DEBUG_SYMBOL;
	}

	@Override
	public String kind() {
		return "debug";
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		// empty
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		if (s != null) {
			throw new IllegalStateException("assert(!s)");
		}
		DebugSymbol ds = context.newDebugSymbol(filename, lineNumber, ident, version);
		ds.level = level;
		ds.copySourceRange(this);
		return ds;
	}
	
	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("debug = ");
		if (ident != null) {
			buf.writestring(ident.toChars());
		} else {
			buf.writestring(level);
		}
		buf.writestring(";");
		buf.writenl();
	}
	
	@Override
	public String getSignature(int options) {
		// TODO Auto-generated method stub
		return null;
	}

}
