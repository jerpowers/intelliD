package descent.internal.compiler.parser.ast;

import melnorme.utilbox.core.Assert;
import melnorme.utilbox.tree.IElement;
import melnorme.utilbox.tree.IVisitable;
import descent.internal.compiler.parser.ASTDmdNode;

/**
 * Abstract node class. Only ASTDmdNode is allowed to extends (BM: consider changing this?) 
 */
public abstract class ASTNode 
	implements IASTNode, IElement, IVisitable<IASTVisitor> {
	
	public static final ASTDmdNode[] NO_ELEMENTS = new ASTDmdNode[0]; 
	
	/** AST node parent, null if the node is the tree root. */
	public ASTDmdNode parentNode = null;
	
	/** A character index into the original source string, 
	 * or <code>-1</code> if no source position information is available
	 * for this node; <code>-1</code> by default.
	 */
	public int start = -1;
	/** A character length, or <code>0</code> if no source position
	 * information is recorded for this node; <code>0</code> by default.
	 */
	public int length = 0;

	@Override
	public ASTDmdNode getParent() {
		return parentNode;
	}
	
	/** Set the parent of this node. Can be null. */
	public void setParent(ASTDmdNode parent) {
		this.parentNode = parent;
	}
	
	/** Gets the source range start position, aka offset. */
	@Override
	public final int getStartPos() {
		return start;
	}
	/** Gets the source range start position, aka offset. */
	@Override
	public final int getOffset() {
		return start;
	}
	/** Gets the source range length. */
	@Override
	public final int getLength() {
		return length;
	}
	
	/** Gets the source range end position (start position + length). */
	@Override
	public final int getEndPos() {
		Assert.isTrue(start != -1);
		return start+length;
	}
	/** Sets the source range end position (start position + length). */
	public final void setEndPos(int endPos) {
		Assert.isTrue(endPos >= start);
		Assert.isTrue(start != -1);
		length = endPos - start ;
	}

	/** Sets the source range of the original source file where the source
	 * fragment corresponding to this node was found.
	 */
	public final void setSourceRange(int startPosition, int length) {
		//AssertIn.isTrue(startPosition >= 0 && length > 0);
		// source positions are not considered a structural property
		// but we protect them nevertheless
		//checkModifiable();
		this.start = startPosition;
		this.length = length;
	}
	
	/** Checks if the node has no defined source range info. */
	@Override
	public final boolean hasNoSourceRangeInfo() {
		return start == -1;
	}
	
	@Override
	public boolean hasChildren() {
		return getChildren().length > 0;
	}
	
	/** Returns the node's children, ordered. */
	@Override
	public ASTNode[] getChildren() {
		return (ASTNode[]) ASTChildrenCollector.getChildrenArray(this);
	}
	
	@Override
	public final void accept(IASTVisitor visitor) {
		// begin with the generic pre-visit
		if(visitor.preVisit(this)) {
			// dynamic dispatch to internal method for type-specific visit/endVisit
			this.accept0(visitor);
		}
		// end with the generic post-visit
		visitor.postVisit(this);
	}
	
	/** Accepts the given visitor on a type-specific visit of the current node.
	 * <p>
	 * General template for implementation on each concrete element class:
	 * <pre> <code>
	 * boolean visitChildren = visitor.visit(this);
	 * if (visitChildren) {
	 *    // visit children in normal left to right reading order
	 *    acceptChild(visitor, getProperty1());
	 *    acceptChildren(visitor, rawListProperty);
	 *    acceptChild(visitor, getProperty2());
	 * }
	 * visitor.endVisit(this);
	 * </code> </pre>
	 * </p>
	 */
	protected abstract void accept0(IASTVisitor visitor);


	/** Returns a simple string representation of the node. */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		int p = buffer.length();
		try {
			appendDebugString(buffer);
		} catch (RuntimeException e) {
			e.printStackTrace();
			// since debugger sometimes call toString methods, problems can easily happen when
			// toString is called on an instance that is being initialized
			buffer.setLength(p);
			buffer.append("!"); //$NON-NLS-1$
			buffer.append(standardToString());
		}
		return buffer.toString();
	}
	
	/** Returns a simple string representation of the node as a char array. */
	public char[] toCharArray() {
		return toString().toCharArray();
	}
	
	/**
	 * Appends a debug representation of this node to the given string buffer.
	 * <p>
	 * The <code>ASTNode</code> implementation of this method prints out the entire 
	 * subtree. Subclasses may override to provide a more succinct representation.
	 * </p>
	 * 
	 * @param buffer the string buffer to append to
	 */
	void appendDebugString(StringBuffer buffer) {
		// print the subtree by default
		appendPrintString(buffer);
	}
		
	/**
	 * Appends a standard Java source code representation of this subtree to the given
	 * string buffer.
	 * 
	 * @param buffer the string buffer to append to
	 */
	final void appendPrintString(StringBuffer buffer) {
		NaiveASTFlattener printer = new NaiveASTFlattener();
		this.accept(printer);
		buffer.append(printer.getResult());
	}
	
	/**
	 * Returns the string representation of this node produced by the standard
	 * <code>Object.toString</code> method.
	 * 
	 * @return a debug string 
	 */
	final String standardToString() {
		return super.toString();
	}

	/** Gets the node's classname striped of package qualifier. */
	public final String toStringClassName() {
		String str = this.getClass().getName();
		int lastIx = str.lastIndexOf('.');
		return str.substring(lastIx+1);
	}
	
	/** Gets an extended String representation of given node. (for debugging) */
	@Override
	public String toStringAsNode(boolean printRangeInfo) {
		String str = toStringClassName();

		if(this instanceof ASTDmdNode)
			str = "#" + str;
		if(printRangeInfo)
			str += " ["+ start +"+"+ length +"]";
		return str;
	}
	
	public void setLength(int length) {
		this.length = length;
	}

}
