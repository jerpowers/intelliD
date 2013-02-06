package descent.internal.compiler.parser;

import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class DeleteDeclaration extends FuncDeclaration {

	public Arguments arguments;
	public int deleteStart; // where the "delete" keyword starts

	public DeleteDeclaration(char[] filename, int lineNumber, Arguments arguments) {
		super(filename, lineNumber, new IdentifierExp(Id.classDelete),
				STC.STCundefined, null);
		this.arguments = arguments;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, type);
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
		return false;
	}

	@Override
	public boolean addPreInvariant(SemanticContext context) {
		return false;
	}

	@Override
	public int getNodeType() {
		return DELETE_DECLARATION;
	}

	@Override
	public boolean isDelete() {
		return true;
	}

	@Override
	public DeleteDeclaration isDeleteDeclaration() {
		return this;
	}

	@Override
	public boolean isVirtual(SemanticContext context) {
		return false;
	}

	@Override
	public String kind() {
		return "deallocator";
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		ClassDeclaration cd;

		parent = sc.parent;
		Dsymbol parent = toParent();
		cd = parent.isClassDeclaration();
		if (cd == null && parent.isStructDeclaration() == null) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeErrorLoc(
						IProblem.DeleteDeallocatorsOnlyForClassOrStruct, this));
			}
		}
		type = new TypeFunction(arguments, Type.tvoid, 0, LINK.LINKd);

		type = type.semantic(filename, lineNumber, sc, context);
		Assert.isTrue(type.ty == TY.Tfunction);

		// Check that there is only one argument of type void*
		TypeFunction tf = (TypeFunction) type;
		if (Argument.dim(tf.parameters, context) != 1) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeErrorLoc(
						IProblem.OneArgumentOfTypeExpected, this,
						"void*"));
			}
		} else {
			Argument a = Argument.getNth(tf.parameters, 0, context);
			if (!a.type.equals(Type.tvoid.pointerTo(context))) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.OneArgumentOfTypeExpected, a.type, "void*"));
				}
			}
		}

		super.semantic(sc, context);
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		DeleteDeclaration f = context.newDeleteDeclaration(filename, lineNumber, null);
		super.syntaxCopy(f, context);
		f.arguments = arraySyntaxCopy(arguments, context);
		f.copySourceRange(this);
		f.javaElement = javaElement;
		return f;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("delete");
		Argument.argsToCBuffer(buf, hgs, arguments, 0, context);
		bodyToCBuffer(buf, hgs, context);
	}

	@Override
	public int getErrorStart() {
		return deleteStart;
	}

	@Override
	public int getErrorLength() {
		return 6;
	}

}
