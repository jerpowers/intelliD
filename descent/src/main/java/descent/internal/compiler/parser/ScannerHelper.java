/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package descent.internal.compiler.parser;

import descent.core.compiler.InvalidInputException;
import descent.internal.compiler.ast.ASTNodeFromJdt;

public class ScannerHelper {
	
	public final static long[] Bits = { 
		ASTNodeFromJdt.Bit1, ASTNodeFromJdt.Bit2, ASTNodeFromJdt.Bit3, ASTNodeFromJdt.Bit4, ASTNodeFromJdt.Bit5, ASTNodeFromJdt.Bit6,
		ASTNodeFromJdt.Bit7, ASTNodeFromJdt.Bit8, ASTNodeFromJdt.Bit9, ASTNodeFromJdt.Bit10, ASTNodeFromJdt.Bit11, ASTNodeFromJdt.Bit12, 
		ASTNodeFromJdt.Bit13, ASTNodeFromJdt.Bit14, ASTNodeFromJdt.Bit15, ASTNodeFromJdt.Bit16, ASTNodeFromJdt.Bit17, ASTNodeFromJdt.Bit18, 
		ASTNodeFromJdt.Bit19, ASTNodeFromJdt.Bit20, ASTNodeFromJdt.Bit21, ASTNodeFromJdt.Bit22, ASTNodeFromJdt.Bit23, ASTNodeFromJdt.Bit24, 
		ASTNodeFromJdt.Bit25, ASTNodeFromJdt.Bit26, ASTNodeFromJdt.Bit27, ASTNodeFromJdt.Bit28, ASTNodeFromJdt.Bit29, ASTNodeFromJdt.Bit30, 
		ASTNodeFromJdt.Bit31, ASTNodeFromJdt.Bit32, ASTNodeFromJdt.Bit33L, ASTNodeFromJdt.Bit34L, ASTNodeFromJdt.Bit35L, ASTNodeFromJdt.Bit36L, 
		ASTNodeFromJdt.Bit37L, ASTNodeFromJdt.Bit38L, ASTNodeFromJdt.Bit39L, ASTNodeFromJdt.Bit40L, ASTNodeFromJdt.Bit41L, ASTNodeFromJdt.Bit42L, 
		ASTNodeFromJdt.Bit43L, ASTNodeFromJdt.Bit44L, ASTNodeFromJdt.Bit45L, ASTNodeFromJdt.Bit46L, ASTNodeFromJdt.Bit47L, ASTNodeFromJdt.Bit48L, 
		ASTNodeFromJdt.Bit49L, ASTNodeFromJdt.Bit50L, ASTNodeFromJdt.Bit51L, ASTNodeFromJdt.Bit52L, ASTNodeFromJdt.Bit53L, ASTNodeFromJdt.Bit54L, 
		ASTNodeFromJdt.Bit55L, ASTNodeFromJdt.Bit56L, ASTNodeFromJdt.Bit57L, ASTNodeFromJdt.Bit58L, ASTNodeFromJdt.Bit59L, ASTNodeFromJdt.Bit60L, 
		ASTNodeFromJdt.Bit61L, ASTNodeFromJdt.Bit62L, ASTNodeFromJdt.Bit63L, ASTNodeFromJdt.Bit64L,
	};

//	private static final int START_INDEX = 0;
//	private static final int PART_INDEX = 1;

	//private static long[][][] Tables;

	public final static int MAX_OBVIOUS = 128;
	public final static int[] OBVIOUS_IDENT_CHAR_NATURES = new int[MAX_OBVIOUS];

	public final static int C_JLS_SPACE = ASTNodeFromJdt.Bit9;
	public final static int C_SPECIAL = ASTNodeFromJdt.Bit8;
	public final static int C_IDENT_START = ASTNodeFromJdt.Bit7;
	public final static int C_UPPER_LETTER = ASTNodeFromJdt.Bit6;
	public final static int C_LOWER_LETTER = ASTNodeFromJdt.Bit5;
	public final static int C_IDENT_PART = ASTNodeFromJdt.Bit4;
	public final static int C_DIGIT = ASTNodeFromJdt.Bit3;
	public final static int C_SEPARATOR = ASTNodeFromJdt.Bit2;
	public final static int C_SPACE = ASTNodeFromJdt.Bit1;

	static {
		OBVIOUS_IDENT_CHAR_NATURES[0] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[1] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[2] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[3] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[4] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[5] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[6] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[7] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[8] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[14] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[15] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[16] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[17] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[18] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[19] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[20] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[21] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[22] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[23] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[24] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[25] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[26] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[27] = C_IDENT_PART;
		OBVIOUS_IDENT_CHAR_NATURES[127] = C_IDENT_PART;
		
		for (int i = '0'; i <= '9'; i++) 
			OBVIOUS_IDENT_CHAR_NATURES[i] = C_DIGIT | C_IDENT_PART;
		
		for (int i = 'a'; i <= 'z'; i++) 
			OBVIOUS_IDENT_CHAR_NATURES[i] = C_LOWER_LETTER | C_IDENT_PART | C_IDENT_START;
		for (int i = 'A'; i <= 'Z'; i++) 
			OBVIOUS_IDENT_CHAR_NATURES[i] = C_UPPER_LETTER | C_IDENT_PART | C_IDENT_START;

		OBVIOUS_IDENT_CHAR_NATURES['_'] = C_SPECIAL | C_IDENT_PART | C_IDENT_START;
		OBVIOUS_IDENT_CHAR_NATURES['$'] = C_SPECIAL | C_IDENT_PART | C_IDENT_START;
		
		OBVIOUS_IDENT_CHAR_NATURES[9] = C_SPACE | C_JLS_SPACE; // \ u0009: HORIZONTAL TABULATION
		OBVIOUS_IDENT_CHAR_NATURES[10] = C_SPACE | C_JLS_SPACE; // \ u000a: LINE FEED
		OBVIOUS_IDENT_CHAR_NATURES[11] = C_SPACE;
		OBVIOUS_IDENT_CHAR_NATURES[12] = C_SPACE | C_JLS_SPACE; // \ u000c: FORM FEED
		OBVIOUS_IDENT_CHAR_NATURES[13] = C_SPACE | C_JLS_SPACE; //  \ u000d: CARRIAGE RETURN
		OBVIOUS_IDENT_CHAR_NATURES[28] = C_SPACE;
		OBVIOUS_IDENT_CHAR_NATURES[29] = C_SPACE;
		OBVIOUS_IDENT_CHAR_NATURES[30] = C_SPACE;
		OBVIOUS_IDENT_CHAR_NATURES[31] = C_SPACE;
		OBVIOUS_IDENT_CHAR_NATURES[32] = C_SPACE | C_JLS_SPACE; //  \ u0020: SPACE
		
		OBVIOUS_IDENT_CHAR_NATURES['.'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[':'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[';'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[','] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['['] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[']'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['('] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES[')'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['{'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['}'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['+'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['-'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['*'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['/'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['='] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['&'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['|'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['?'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['<'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['>'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['!'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['%'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['^'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['~'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['"'] = C_SEPARATOR;
		OBVIOUS_IDENT_CHAR_NATURES['\''] = C_SEPARATOR;
	}
	
//static {
//	Tables = new long[2][][];
//	Tables[START_INDEX] = new long[2][];
//	Tables[PART_INDEX] = new long[3][];
//	try {
//		DataInputStream inputStream = new DataInputStream(ScannerHelper.class.getResourceAsStream("start1.rsc")); //$NON-NLS-1$
//		long[] readValues = new long[1024];
//		for (int i = 0; i < 1024; i++) {
//			try {
//				readValues[i] = inputStream.readLong();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		inputStream.close();
//		Tables[START_INDEX][0] = readValues;
//	} catch (FileNotFoundException e) {
//		e.printStackTrace();
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	try {
//		DataInputStream inputStream = new DataInputStream(ScannerHelper.class.getResourceAsStream("start2.rsc")); //$NON-NLS-1$
//		long[] readValues = new long[1024];
//		for (int i = 0; i < 1024; i++) {
//			readValues[i] = inputStream.readLong();
//		}
//		inputStream.close();
//		Tables[START_INDEX][1] = readValues;
//	} catch (FileNotFoundException e) {
//		e.printStackTrace();
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	try {
//		DataInputStream inputStream = new DataInputStream(ScannerHelper.class.getResourceAsStream("part1.rsc")); //$NON-NLS-1$
//		long[] readValues = new long[1024];
//		for (int i = 0; i < 1024; i++) {
//			readValues[i] = inputStream.readLong();
//		}
//		inputStream.close();
//		Tables[PART_INDEX][0] = readValues;
//	} catch (FileNotFoundException e) {
//		e.printStackTrace();
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	try {
//		DataInputStream inputStream = new DataInputStream(ScannerHelper.class.getResourceAsStream("part2.rsc")); //$NON-NLS-1$
//		long[] readValues = new long[1024];
//		for (int i = 0; i < 1024; i++) {
//			readValues[i] = inputStream.readLong();
//		}
//		inputStream.close();
//		Tables[PART_INDEX][1] = readValues;
//	} catch (FileNotFoundException e) {
//		e.printStackTrace();
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	try {
//		DataInputStream inputStream = new DataInputStream(ScannerHelper.class.getResourceAsStream("part14.rsc")); //$NON-NLS-1$
//		long[] readValues = new long[1024];
//		for (int i = 0; i < 1024; i++) {
//			readValues[i] = inputStream.readLong();
//		}
//		inputStream.close();
//		Tables[PART_INDEX][2] = readValues;
//	} catch (FileNotFoundException e) {
//		e.printStackTrace();
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//}

//private final static boolean isBitSet(long[] values, int i) {
//	try {
//		return (values[i / 64] & Bits[i % 64]) != 0;
//	} catch (NullPointerException e) {
//		return false;
//	}
//}
public static boolean isJavaIdentifierPart(char c) {
	if (c < MAX_OBVIOUS) {
		return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_IDENT_PART) != 0;
	}
	return Character.isJavaIdentifierPart(c);
}
//public static boolean isJavaIdentifierPart(char high, char low) {
//	int codePoint = toCodePoint(high, low);
//	switch((codePoint & 0x1F0000) >> 16) {
//		case 0 :
//			return Character.isJavaIdentifierPart((char) codePoint);
//		case 1 :
//			return isBitSet(Tables[PART_INDEX][0], codePoint & 0xFFFF);
//		case 2 :
//			return isBitSet(Tables[PART_INDEX][1], codePoint & 0xFFFF);
//		case 14 :
//			return isBitSet(Tables[PART_INDEX][2], codePoint & 0xFFFF);
//	}
//	return false;
//}
public static boolean isJavaIdentifierStart(char c) {
	if (c < MAX_OBVIOUS) {
		return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_IDENT_START) != 0;
	}
	return Character.isJavaIdentifierStart(c);
}	
//public static boolean isJavaIdentifierStart(char high, char low) {
//	int codePoint = toCodePoint(high, low);
//	switch((codePoint & 0x1F0000) >> 16) {
//		case 0 :
//			return Character.isJavaIdentifierStart((char) codePoint);
//		case 1 :
//			return isBitSet(Tables[START_INDEX][0], codePoint & 0xFFFF);
//		case 2 :
//			return isBitSet(Tables[START_INDEX][1], codePoint & 0xFFFF);
//	}
//	return false;
//}

//private static int toCodePoint(char high, char low) {	
//	return (high - Scanner.HIGH_SURROGATE_MIN_VALUE) * 0x400 + (low - Scanner.LOW_SURROGATE_MIN_VALUE) + 0x10000;
//}
public static boolean isDigit(char c) throws InvalidInputException {
	if(c < ScannerHelper.MAX_OBVIOUS) {
		return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_DIGIT) != 0;
	}
	if (Character.isDigit(c)) {
		throw new InvalidInputException(Scanner.INVALID_DIGIT);
	}
	return false;
}
public static int digit(char c, int radix) {
	if (c < ScannerHelper.MAX_OBVIOUS) {
		switch(radix) {
			case 8 :
				if (c >= 48 && c <= 55) {
					return c - 48;
				}
				return -1;
			case 10 :
				if (c >= 48 && c <= 57) {
					return c - 48;
				}
				return -1;
			case 16 :
				if (c >= 48 && c <= 57) {
					return c - 48;
				}
				if (c >= 65 && c <= 70) {
					return c - 65 + 10;
				}
				if (c >= 97 && c <= 102) {
					return c - 97 + 10;
				}
				return -1;
		}
	}
	return Character.digit(c, radix);
}
public static int getNumericValue(char c) {
	if (c < ScannerHelper.MAX_OBVIOUS) {
		switch(ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c]) {
			case C_DIGIT :
				return c - '0';
			case C_LOWER_LETTER :
				return 10 + c - 'a';
			case C_UPPER_LETTER :
				return 10 + c - 'A';
		}
	}
	return Character.getNumericValue(c);
}
public static char toUpperCase(char c) {
	if (c < MAX_OBVIOUS) {
		if ((ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_UPPER_LETTER) != 0) {
			return c;
		} else if ((ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_LOWER_LETTER) != 0) {
			return (char) (c - 32); 
		}
	}
	return Character.toLowerCase(c);
}
public static char toLowerCase(char c) {
	if (c < MAX_OBVIOUS) {
		if ((ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_LOWER_LETTER) != 0) {
			return c;
		} else if ((ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_UPPER_LETTER) != 0) {
			return (char) (32 + c); 
		}
	}
	return Character.toLowerCase(c);
}
public static boolean isLowerCase(char c) {
	if (c < MAX_OBVIOUS) {
		return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_LOWER_LETTER) != 0;
	}
	return Character.isLowerCase(c);
}
public static boolean isUpperCase(char c) {
	if (c < MAX_OBVIOUS) {
		return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_UPPER_LETTER) != 0;
	}
	return Character.isUpperCase(c);
}
/**
 * Include also non JLS whitespaces.
 * 
 * return true if Character.isWhitespace(c) would return true
 */
public static boolean isWhitespace(char c) {
	if (c < MAX_OBVIOUS) {
		return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & ScannerHelper.C_SPACE) != 0;
	}	
	return Character.isWhitespace(c);
}
public static boolean isLetter(char c) {
	if (c < MAX_OBVIOUS) {
		return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & (ScannerHelper.C_UPPER_LETTER | ScannerHelper.C_LOWER_LETTER)) != 0;
	}
	return Character.isLetter(c);
}
public static boolean isLetterOrDigit(char c) {
	if (c < MAX_OBVIOUS) {
		return (ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[c] & (ScannerHelper.C_UPPER_LETTER | ScannerHelper.C_LOWER_LETTER | ScannerHelper.C_DIGIT)) != 0;
	}
	return Character.isLetterOrDigit(c);
}
}
