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

package descent.core.compiler;

/**
 * Maps each terminal symbol in the D-grammar into a unique integer. 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see IScanner
 */
public interface ITerminalSymbols {
	
	int TokenNameWHITESPACE = 1000;
	int TokenNameCOMMENT_LINE = 1001;
	int TokenNameCOMMENT_BLOCK = 1002;
	int TokenNameCOMMENT_PLUS = 1003;
	int TokenNameCOMMENT_DOC_LINE = 1004;
	int TokenNameCOMMENT_DOC_BLOCK = 1005;
	int TokenNameCOMMENT_DOC_PLUS = 1006;
	
	int TokenNameLPAREN = 1;
	int TokenNameRPAREN = 2;
	int TokenNameLBRACKET = 3;
	int TokenNameRBRACKET = 4;
	int TokenNameLBRACE = 5;
	int TokenNameRBRACE = 6;
	int TokenNameCOLON = 7;
	int TokenNameNOT = 8;
	int TokenNameSEMICOLON = 9;
	int TokenNameDOT_DOT_DOT = 10;
	int TokenNameEOF = 11;
	int TokenNamecast = 12;
	int TokenNamenull = 13;
	int TokenNameassert = 14;
	int TokenNametrue = 15;
	int TokenNamefalse = 16;
	int TokenNamethrow = 17;
	int TokenNamenew = 18;
	int TokenNamedelete = 19;
	int TokenNameversion = 20;
	int TokenNamemodule = 21;
	int TokenNameDOLLAR = 22;
	int TokenNametemplate = 23;
	int TokenNametypeof = 24;
	int TokenNamepragma = 25;
	int TokenNametypeid = 26;
	int TokenNameiftype = 27;
	int TokenNameLESS = 28;
	int TokenNameGREATER = 29;
	int TokenNameLESS_EQUAL = 30;
	int TokenNameGREATER_EQUAL = 31;
	int TokenNameEQUAL = 32;
	int TokenNameNOT_EQUAL = 33;
	int TokenNameEQUAL_EQUAL_EQUAL = 34;
	int TokenNameNOT_EQUAL_EQUAL = 35;
	int TokenNameis = 36;
	int TokenNameNOT_LESS_GREATER_EQUAL = 37;
	int TokenNameLESS_GREATER = 38;
	int TokenNameLESS_GREATER_EQUAL = 39;
	int TokenNameNOT_GREATER = 40;
	int TokenNameNOT_GREATER_EQUAL = 41;
	int TokenNameNOT_LESS = 42;
	int TokenNameNOT_LESS_EQUAL = 43;
	int TokenNameNOT_LESS_GREATER = 44;
	int TokenNameLEFT_SHIFT = 45;
	int TokenNameRIGHT_SHIFT = 46;
	int TokenNameLEFT_SHIFT_EQUAL = 47;
	int TokenNameRIGHT_SHIFT_EQUAL = 48;
	int TokenNameUNSIGNED_RIGHT_SHIFT = 49;
	int TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 50;
	int TokenNameTILDE = 51;
	int TokenNameTILDE_EQUAL = 52;
	int TokenNamePLUS = 53;
	int TokenNameMINUS = 54;
	int TokenNamePLUS_EQUAL = 55;
	int TokenNameMINUS_EQUAL = 56;
	int TokenNameMULTIPLY = 57;
	int TokenNameDIVIDE = 58;
	int TokenNameREMAINDER = 59;
	int TokenNameMULTIPLY_EQUAL = 60;
	int TokenNameDIVIDE_EQUAL = 61;
	int TokenNameREMAINDER_EQUAL = 62;
	int TokenNameAND = 63;
	int TokenNameOR = 64;
	int TokenNameXOR = 65;
	int TokenNameAND_EQUAL = 66;
	int TokenNameOR_EQUAL = 67;
	int TokenNameXOR_EQUAL = 68;
	int TokenNamePLUS_PLUS = 69;
	int TokenNameMINUS_MINUS = 70;
	int TokenNameDOT = 71;
	int TokenNameCOMMA = 72;
	int TokenNameQUESTION = 73;
	int TokenNameAND_AND = 74;
	int TokenNameOR_OR = 75;
	int TokenNameIntegerLiteral = 76;
	int TokenNameUnsignedIntegerLiteral = 77;
	int TokenNameLongLiteral = 78;
	int TokenNameUnsignedLongLiteral = 79;
	int TokenNameFloatLiteral = 80;
	int TokenNameDoubleLiteral = 81;
	int TokenNameRealLiteral = 82;
	int TokenNameImaginaryFloatLiteral = 83;
	int TokenNameImaginaryDoubleLiteral = 84;
	int TokenNameImaginaryRealLiteral = 85;
	int TokenNameCharacterLiteral = 86;
	int TokenNameWCharacterLiteral = 87;
	int TokenNameDCharacterLiteral = 88;
	int TokenNameIdentifier = 89;
	int TokenNameStringLiteral = 90;
	int TokenNamethis = 91;
	int TokenNamesuper = 92;
	int TokenNamevoid = 93;
	int TokenNamebyte = 94;
	int TokenNameubyte = 95;
	int TokenNameshort = 96;
	int TokenNameushort = 97;
	int TokenNameint = 98;
	int TokenNameuint = 99;
	int TokenNamelong = 100;
	int TokenNameulong = 101;
	int TokenNamefloat = 102;
	int TokenNamedouble = 103;
	int TokenNamereal = 104;
	int TokenNameifloat = 105;
	int TokenNameidouble = 106;
	int TokenNameireal = 107;
	int TokenNamecfloat = 108;
	int TokenNamecdouble = 109;
	int TokenNamecreal = 110;
	int TokenNamechar = 111;
	int TokenNamewchar = 112;
	int TokenNamedchar = 113;
	int TokenNamebit = 114;
	int TokenNamebool = 115;
	int TokenNamecent = 116;
	int TokenNameucent = 117;
	int TokenNamestruct = 118;
	int TokenNameclass = 119;
	int TokenNameinterface = 120;
	int TokenNameunion = 121;
	int TokenNameenum = 122;
	int TokenNameimport = 123;
	int TokenNametypedef = 124;
	int TokenNamealias = 125;
	int TokenNameoverride = 126;
	int TokenNamedelegate = 127;
	int TokenNamefunction = 128;
	int TokenNamemixin = 129;
	int TokenNamealign = 130;
	int TokenNameextern = 131;
	int TokenNameprivate = 132;
	int TokenNameprotected = 133;
	int TokenNamepublic = 134;
	int TokenNameexport = 135;
	int TokenNamestatic = 136;
	int TokenNamefinal = 137;
	int TokenNameconst = 138;
	int TokenNameabstract = 139;
	int TokenNamevolatile = 140;
	int TokenNamedebug = 141;
	int TokenNamedeprecated = 142;
	int TokenNamein = 143;
	int TokenNameout = 144;
	int TokenNameinout = 145;
	int TokenNamelazy = 146;
	int TokenNameauto = 147;
	int TokenNamepackage = 148;
	int TokenNameif = 149;
	int TokenNameelse = 150;
	int TokenNamewhile = 151;
	int TokenNamefor = 152;
	int TokenNamedo = 153;
	int TokenNameswitch = 154;
	int TokenNamecase = 155;
	int TokenNamedefault = 156;
	int TokenNamebreak = 157;
	int TokenNamecontinue = 158;
	int TokenNamewith = 159;
	int TokenNamesynchronized = 160;
	int TokenNamereturn = 161;
	int TokenNamegoto = 162;
	int TokenNametry = 163;
	int TokenNamecatch = 164;
	int TokenNamefinally = 165;
	int TokenNameasm = 166;
	int TokenNameforeach = 167;
	int TokenNameforeach_reverse = 168;
	int TokenNamescope = 169;
	int TokenNameexit = 170;
	int TokenNamefailure = 171;
	int TokenNamesuccess = 172;
	int TokenNamebody = 173;
	int TokenNameinvariant = 174;
	int TokenNameunittest = 175;
	int TokenNameDOT_DOT = 176;
	int TokenNameEQUAL_EQUAL = 177;
	int TokenNameon_scope_exit = 178;
	int TokenNameon_scope_failure = 179;
	int TokenNameon_scope_success = 180;
	int TokenNamePRAGMA = 181;
	int TokenNameref = 182;
	int TokenNamemacro = 183;
	
	int TokenName__traits = 184;
	int TokenName__overloadset = 185;
	int TokenNamenothrow = 186;
	int TokenNamepure = 187;
	int TokenNameshared = 188;
	int TokenNameimmutable = 189;
	int TokenName__gshared = 190;
	int TokenName__thread = 191;

}

