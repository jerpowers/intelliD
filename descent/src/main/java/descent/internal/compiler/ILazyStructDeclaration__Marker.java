package descent.internal.compiler;

import descent.internal.compiler.parser.SemanticContext;
import descent.internal.compiler.parser.StructDeclaration;

/**
 * This is a little helper for removing compile time dependencies from the compiler.
 * CONTRACT: The only class that is allowed to directly extend this one is LazyStructDeclaration.
 */
public interface ILazyStructDeclaration__Marker {

	StructDeclaration unlazy(char[] noChar, SemanticContext context);
	
}
