package descent.internal.compiler.parser.ast;

import descent.internal.compiler.parser.ASTDmdNode;

/** Marker class for dmd nodes that originally didn't extend
 * the ASTnode class, and thus had no range. */
public abstract class ASTRangeLessNode extends ASTDmdNode{

}
