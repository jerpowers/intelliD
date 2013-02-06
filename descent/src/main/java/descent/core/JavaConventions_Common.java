/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package descent.core;

import descent.core.compiler.IScanner;
import descent.core.compiler.ITerminalSymbols;
import descent.core.compiler.InvalidInputException;
import descent.internal.compiler.parser.Parser;

/**
 * Provides methods for checking Java-specific conventions such as name syntax.
 * <p>
 * This class provides static methods and constants only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 */
public class JavaConventions_Common {

	protected final static IScanner SCANNER = ParserToolFactory.createScanner(true, true, true, false, Parser.DEFAULT_LEVEL);


	/*
	 * Returns the current identifier extracted by the scanner (without unicode
	 * escapes) from the given id.
	 * Returns <code>null</code> if the id was not valid
	 */
	protected static synchronized char[] scannedIdentifier(String id) {
		if (id == null) {
			return null;
		}
		String trimmed = id.trim();
		if (!trimmed.equals(id)) {
			return null;
		}
		try {
			SCANNER.setSource(id.toCharArray());
			int token = SCANNER.getNextToken();
			char[] currentIdentifier;
			try {
				currentIdentifier = SCANNER.getRawTokenSource();
			} catch (ArrayIndexOutOfBoundsException e) {
				return null;
			}
			int nextToken= SCANNER.getNextToken();
			if (token == ITerminalSymbols.TokenNameIdentifier 
				&& nextToken == ITerminalSymbols.TokenNameEOF
				&& SCANNER.getCurrentTokenEndPosition() == id.length()) { // to handle case where we had an ArrayIndexOutOfBoundsException 
																     // while reading the last token
				return currentIdentifier;
			} else {
				return null;
			}
		}
		catch (InvalidInputException e) {
			return null;
		}
	}

	
	public static boolean isValidIdentifier(String id) {
		return scannedIdentifier(id) != null;
	}

}
