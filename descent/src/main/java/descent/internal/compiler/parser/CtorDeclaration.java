package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class CtorDeclaration extends FuncDeclaration {

	public Arguments arguments;
	public int varargs;
	public int thisStart; // where the "this" keyword starts

	public CtorDeclaration(char[] filename, int lineNumber, Arguments arguments, int varags) {
		super(filename, lineNumber, new IdentifierExp(Id.ctor), STC.STCundefined, null);
		this.arguments = arguments;
		this.varargs = varags;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceType);
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, arguments);
			TreeVisitor.acceptChildren(visitor, sourceFrequire);
			TreeVisitor.acceptChildren(visitor, sourceFbody);
			TreeVisitor.acceptChildren(visitor, outId);
			TreeVisitor.acceptChildren(visitor, sourceFensure);
		}
		visitor.endVisit(this);
	}

	@Override
	public boolean addPostInvariant(SemanticContext context) {
		return (isThis() != null && vthis != null && context.global.params.useInvariants);
	}

	@Override
	public boolean addPreInvariant(SemanticContext context) {
		return false;
	}

	@Override
	public int getNodeType() {
		return CTOR_DECLARATION;
	}

	@Override
	public CtorDeclaration isCtorDeclaration() {
		return this;
	}

	@Override
	public boolean isVirtual(SemanticContext context) {
		return false;
	}

	@Override
	public String kind() {
		return "constructor";
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		AggregateDeclaration cd;
		Type tret;
		
	    if (type != null) {
	    	return;
	    }

		sc = sc.push();
		sc.stc &= ~STC.STCstatic; // not a static constructor

		parent = sc.parent;
		Dsymbol parent;
		
		boolean condition;
		if (context.isD1()) {
			parent = toParent();
			cd = parent.isClassDeclaration();
			condition = cd == null;
		} else {
			parent = toParent2();
			cd = parent.isAggregateDeclaration();
			condition = cd == null || parent.isUnionDeclaration() != null;
		}
		
		if (cd == null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeErrorLoc(
						context.isD1() ? IProblem.ConstructorsOnlyForClass : IProblem.ConstructorsOnlyForClassOrStruct, this));
			}
			tret = Type.tvoid;
		} else {
			if (context.isD1()) {
				tret = cd.type; // .referenceTo();
			} else {
				tret = cd.handle;
			}
		}
		type = new TypeFunction(arguments, tret, varargs, LINK.LINKd);
		
		if (context.STRUCTTHISREF()) {
		    if (cd != null && cd.isStructDeclaration() != null)
		    	((TypeFunction)type).isref = true;
		}
		
		if (null == originalType) {
			originalType = type;
		}

		sc.flags |= Scope.SCOPEctor;
		type = type.semantic(filename, lineNumber, sc, context);
		sc.flags &= ~Scope.SCOPEctor;

		// Append:
		// return this;
		// to the function body
		if (fbody != null) {
			Expression e = new ThisExp(filename, lineNumber);
			Statement s = new ReturnStatement(filename, lineNumber, e);
			fbody = new CompoundStatement(filename, lineNumber, fbody, s);
		}

		super.semantic(sc, context);

		sc.pop();

		// See if it's the default constructor
		if (cd != null && varargs == 0 && Argument.dim(arguments, context) == 0) {
			if (context.isD1()) {
				cd.defaultCtor = this;
			} else {
			    if (cd.isStructDeclaration() != null) {
			    	if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeErrorLoc(
								IProblem.DefaultConstructorNotAllowedForStructs, this));
					}
			    } else {
				    cd.defaultCtor = this;
			    }
			}
		}

	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		CtorDeclaration f;

		f = context.newCtorDeclaration(filename, lineNumber, null, varargs);

		f.outId = outId;
		f.frequire = frequire != null ? frequire.syntaxCopy(context) : null;
		f.fensure = fensure != null ? fensure.syntaxCopy(context) : null;
		f.fbody = fbody != null ? fbody.syntaxCopy(context) : null;

		if (fthrows != null) {
			throw new IllegalStateException("assert(!fhtorws);");
		}

		f.arguments = arraySyntaxCopy(arguments, context);
		f.copySourceRange(this);
		f.javaElement = javaElement;
		return f;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("this");
		Argument.argsToCBuffer(buf, hgs, arguments, varargs, context);
		bodyToCBuffer(buf, hgs, context);
	}

	@Override
	public String toChars(SemanticContext context) {
		return "this";
	}
	
	@Override
	public int getErrorStart() {
		return thisStart;
	}
	
	@Override
	public int getErrorLength() {
		return 4; // "this".length()
	}

}
