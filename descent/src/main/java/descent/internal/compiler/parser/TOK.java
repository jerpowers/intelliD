package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.PREC.PREC_add;
import static descent.internal.compiler.parser.PREC.PREC_and;
import static descent.internal.compiler.parser.PREC.PREC_andand;
import static descent.internal.compiler.parser.PREC.PREC_assign;
import static descent.internal.compiler.parser.PREC.PREC_cond;
import static descent.internal.compiler.parser.PREC.PREC_expr;
import static descent.internal.compiler.parser.PREC.PREC_mul;
import static descent.internal.compiler.parser.PREC.PREC_or;
import static descent.internal.compiler.parser.PREC.PREC_oror;
import static descent.internal.compiler.parser.PREC.PREC_primary;
import static descent.internal.compiler.parser.PREC.PREC_rel;
import static descent.internal.compiler.parser.PREC.PREC_shift;
import static descent.internal.compiler.parser.PREC.PREC_unary;
import static descent.internal.compiler.parser.PREC.PREC_xor;
import static descent.internal.compiler.parser.PREC.PREC_zero;
import descent.core.compiler.ITerminalSymbols;


public enum TOK implements ITerminalSymbols {
	TOKreserved,

	// Other
	TOKlparen("(", TokenNameLPAREN), TOKrparen(")", TokenNameRPAREN), 
	TOKlbracket("[", TokenNameLBRACKET), TOKrbracket("]", TokenNameRBRACKET), 
	TOKlcurly("{", TokenNameLBRACE), TOKrcurly("}", TokenNameRBRACE), 
	TOKcolon(":", TokenNameCOLON),
	
	TOKsemicolon(";", TokenNameSEMICOLON), TOKdotdotdot("...", TokenNameDOT_DOT_DOT), 
	TOKeof("EOF", TokenNameEOF), TOKcast("cast", TokenNamecast, PREC_unary), 
	TOKnull("null", TokenNamenull, PREC_primary), TOKassert("assert", TokenNameassert, PREC_primary), 
	TOKtrue("true", TokenNametrue), TOKfalse("false", TokenNamefalse), 
	TOKarray(PREC_primary), TOKcall(PREC_primary), 
	TOKaddress(PREC_unary), TOKtypedot(PREC_primary), TOKtype,
	TOKthrow("throw", TokenNamethrow), TOKnew("new", TokenNamenew, PREC_unary), 
	TOKdelete("delete", TokenNamedelete, PREC_unary), TOKstar("*", TokenNameMULTIPLY, PREC_unary), 
	TOKsymoff, TOKvar(PREC_primary), TOKdotvar(PREC_primary), TOKdotti(PREC_primary), TOKdotexp, TOKdottype, 
	TOKslice("..", TokenNameDOT_DOT, PREC_primary), 
	TOKarraylength, 
	TOKversion("version", TokenNameversion), TOKmodule("module", TokenNamemodule), 
	TOKdollar("$", TokenNameDOLLAR), TOKtemplate("template", TokenNametemplate), 
	TOKdottd, TOKdeclaration, 
	TOKtypeof("typeof", TokenNametypeof), TOKpragma("pragma", TokenNamepragma), 
	TOKdsymbol, 
	TOKtypeid("typeid", TokenNametypeid, PREC_primary), 
	TOKuadd(PREC_unary), 
	TOKiftype("iftype", TokenNameiftype), 
	TOKremove, TOKnewanonclass, 
	TOKlinecomment(TokenNameCOMMENT_LINE), TOKdoclinecomment(TokenNameCOMMENT_DOC_LINE), 
	TOKblockcomment(TokenNameCOMMENT_BLOCK), TOKdocblockcomment(TokenNameCOMMENT_DOC_BLOCK), 
	TOKpluscomment(TokenNameCOMMENT_PLUS), TOKdocpluscomment(TokenNameCOMMENT_DOC_PLUS), 
	TOKarrayliteral(PREC_primary),

	// Operators
	TOKlt("<", TokenNameLESS, PREC_rel), TOKgt(">", TokenNameGREATER, PREC_rel), 
	TOKle("<=", TokenNameLESS_EQUAL, PREC_rel), TOKge(">=", TokenNameGREATER_EQUAL, PREC_rel), 
	TOKequal("==", TokenNameEQUAL_EQUAL, PREC_rel), TOKnotequal("!=", TokenNameNOT_EQUAL, PREC_rel), 
	TOKidentity("===", TokenNameEQUAL_EQUAL_EQUAL, PREC_rel), TOKnotidentity("!==", TokenNameNOT_EQUAL_EQUAL, PREC_rel), 
	TOKindex, 
	TOKis("is", TokenNameis, PREC_primary), 
	TOKtobool(PREC_add),

	// NCEG floating point compares
	// !<>= <> <>= !> !>= !< !<= !<>
	TOKunord("!<>=", TokenNameNOT_LESS_GREATER_EQUAL, PREC_rel), TOKlg("<>", TokenNameLESS_GREATER, PREC_rel), 
	TOKleg("<>=", TokenNameLESS_GREATER_EQUAL, PREC_rel), TOKule("!>", TokenNameNOT_GREATER, PREC_rel), 
	TOKul("!>=", TokenNameNOT_GREATER_EQUAL, PREC_rel), TOKuge("!<", TokenNameNOT_LESS, PREC_rel), 
	TOKug("!<=", TokenNameNOT_LESS_EQUAL, PREC_rel), TOKue("!<>", TokenNameNOT_LESS_GREATER, PREC_rel),

	TOKshl("<<", TokenNameLEFT_SHIFT, PREC_shift), TOKshr(">>", TokenNameRIGHT_SHIFT, PREC_shift), 
	TOKshlass("<<=", TokenNameLEFT_SHIFT_EQUAL, PREC_assign), TOKshrass(">>=", TokenNameRIGHT_SHIFT_EQUAL, PREC_assign), 
	TOKushr(">>>", TokenNameUNSIGNED_RIGHT_SHIFT, PREC_shift), TOKushrass(">>>=", TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL, PREC_assign),
	
	TOKcatass("~=", TokenNameTILDE_EQUAL, PREC_assign), // ~ ~=
	TOKadd("+", TokenNamePLUS, PREC_add), TOKmin("-", TokenNameMINUS, PREC_add), 
	TOKaddass("+=", TokenNamePLUS_EQUAL, PREC_assign), TOKminass("-=", TokenNameMINUS_EQUAL, PREC_assign), 
	TOKmul("*", TokenNameMULTIPLY, PREC_mul), TOKdiv("/", TokenNameDIVIDE, PREC_mul), 
	TOKmod("%", TokenNameREMAINDER, PREC_mul), TOKmulass("*=", TokenNameMULTIPLY_EQUAL, PREC_assign), 
	TOKdivass("/=", TokenNameDIVIDE_EQUAL, PREC_assign), TOKmodass("%=", TokenNameREMAINDER_EQUAL, PREC_assign), 
	TOKand("&", TokenNameAND, PREC_and), TOKor("|", TokenNameOR, PREC_or), 
	TOKxor("^", TokenNameXOR, PREC_xor), TOKandass("&=", TokenNameAND_EQUAL, PREC_assign), 
	TOKorass("|=", TokenNameOR_EQUAL, PREC_assign), TOKxorass("^=", TokenNameXOR_EQUAL, PREC_assign), 
	TOKassign("=", TokenNameEQUAL, PREC_assign), TOKnot("!", TokenNameNOT, PREC_unary), 
	TOKtilde("~", TokenNameTILDE, PREC_unary), TOKplusplus("++", TokenNamePLUS_PLUS, PREC_primary), 
	TOKminusminus("--", TokenNameMINUS_MINUS, PREC_primary), TOKdot(".", TokenNameDOT, PREC_primary), 
	//TOKarrow("->"), 
	TOKcomma(",", TokenNameCOMMA, PREC_expr), TOKquestion("?", TokenNameQUESTION, PREC_cond), 
	TOKandand("&&", TokenNameAND_AND, PREC_andand), TOKoror("||", TokenNameOR_OR, PREC_oror),

	// Numeric literals
	TOKint32v(TokenNameIntegerLiteral), TOKuns32v(TokenNameUnsignedIntegerLiteral), 
	TOKint64v(TokenNameLongLiteral), TOKuns64v(TokenNameUnsignedLongLiteral), 
	TOKfloat32v(TokenNameFloatLiteral), TOKfloat64v(TokenNameDoubleLiteral), 
	TOKfloat80v(TokenNameRealLiteral), TOKimaginary32v(TokenNameImaginaryFloatLiteral), 
	TOKimaginary64v(TokenNameImaginaryDoubleLiteral), TOKimaginary80v(TokenNameImaginaryRealLiteral),

	// Char constants
	TOKcharv(TokenNameCharacterLiteral), TOKwcharv(TokenNameWCharacterLiteral), 
	TOKdcharv(TokenNameDCharacterLiteral),

	// Leaf operators
	TOKidentifier(TokenNameIdentifier, PREC_primary), TOKstring(TokenNameStringLiteral, PREC_primary), 
	TOKthis("this", TokenNamethis, PREC_primary), TOKsuper("super", TokenNamesuper, PREC_primary), 
	TOKhalt, TOKtuple,

	// Basic types
	TOKvoid("void", TokenNamevoid), TOKint8("byte", TokenNamebyte), 
	TOKuns8("ubyte", TokenNameubyte), TOKint16("short", TokenNameshort), 
	TOKuns16("ushort", TokenNameushort), TOKint32("int", TokenNameint), 
	TOKuns32("uint", TokenNameuint), TOKint64("long", TokenNamelong, PREC_primary), 
	TOKuns64("ulong", TokenNameulong), TOKfloat32("float", TokenNamefloat), 
	TOKfloat64("double", TokenNamedouble, PREC_primary), TOKfloat80("real", TokenNamereal), 
	TOKimaginary32("ifloat", TokenNameifloat), TOKimaginary64("idouble", TokenNameidouble), 
	TOKimaginary80("ireal", TokenNameireal), TOKcomplex32("cfloat", TokenNamecfloat), 
	TOKcomplex64("cdouble", TokenNamecdouble), TOKcomplex80("creal", TokenNamecreal), 
	TOKchar("char", TokenNamechar), TOKwchar("wchar", TokenNamewchar), 
	TOKdchar("dchar", TokenNamedchar), TOKbit("bit", TokenNamebit), 
	TOKbool("bool", TokenNamebool), TOKcent("cent", TokenNamecent), 
	TOKucent("ucent", TokenNameucent),

	// Aggregates
	TOKstruct("struct", TokenNamestruct), TOKclass("class", TokenNameclass), 
	TOKinterface("interface", TokenNameinterface), TOKunion("union", TokenNameunion), 
	TOKenum("enum", TokenNameenum), TOKimport("import", TokenNameimport, PREC_primary), 
	TOKtypedef("typedef", TokenNametypedef), TOKalias("alias", TokenNamealias), 
	TOKoverride("override", TokenNameoverride), TOKdelegate("delegate", TokenNamedelegate), 
	TOKfunction("function", TokenNamefunction, PREC_primary), TOKmixin("mixin", TokenNamemixin),

	TOKalign("align", TokenNamealign), TOKextern("extern", TokenNameextern), 
	TOKprivate("private", TokenNameprivate), TOKprotected("protected", TokenNameprotected), 
	TOKpublic("public", TokenNamepublic), TOKexport("export", TokenNameexport), 
	TOKstatic("static", TokenNamestatic), 
	// TOKvirtual,
	TOKfinal("final", TokenNamefinal), TOKconst("const", TokenNameconst), 
	TOKabstract("abstract", TokenNameabstract), TOKvolatile("volatile", TokenNamevolatile), 
	TOKdebug("debug", TokenNamedebug), TOKdeprecated("deprecated", TokenNamedeprecated), 
	TOKin("in", TokenNamein, PREC_rel), TOKout("out", TokenNameout), 
	TOKinout("inout", TokenNameinout), TOKlazy("lazy", TokenNamelazy), 
	TOKauto("auto", TokenNameauto), TOKpackage("package", TokenNamepackage),

	// Statements
	TOKif("if", TokenNameif), TOKelse("else", TokenNameelse), 
	TOKwhile("while", TokenNamewhile), TOKfor("for", TokenNamefor), 
	TOKdo("do", TokenNamedo), TOKswitch("switch", TokenNameswitch), 
	TOKcase("case", TokenNamecase), TOKdefault("default", TokenNamedefault), 
	TOKbreak("break", TokenNamebreak), TOKcontinue("continue", TokenNamecontinue), 
	TOKwith("with", TokenNamewith), TOKsynchronized("synchronized", TokenNamesynchronized), 
	TOKreturn("return", TokenNamereturn), TOKgoto("goto", TokenNamegoto), 
	TOKtry("try", TokenNametry), TOKcatch("catch", TokenNamecatch), 
	TOKfinally("finally", TokenNamefinally), TOKasm("asm", TokenNameasm), 
	TOKforeach("foreach", TokenNameforeach), TOKforeach_reverse("foreach_reverse", TokenNameforeach_reverse), 
	TOKscope("scope", TokenNamescope), TOKon_scope_exit("on_scope_exit", TokenNameon_scope_exit), 
	TOKon_scope_failure("on_scope_failure", TokenNameon_scope_failure), TOKon_scope_success("on_scope_success", TokenNameon_scope_success),

	// Contracts
	TOKbody("body", TokenNamebody), TOKinvariant("invariant", TokenNameinvariant),

	// Testing
	TOKunittest("unittest", TokenNameunittest),
	
	// Added after 1.0
	// TODO: assign string, precedence, etc.
	TOKref("ref", TokenNameref),
	TOKmacro("macro", TokenNamemacro),
	
	// In 2.0
	TOKtraits("__traits", TokenName__traits),
	TOKoverloadset("__overloadset", TokenName__overloadset),
	TOKnothrow("nothrow", TokenNamenothrow),
	TOKpure("pure", TokenNamepure),
	TOKshared("shared", TokenNameshared),
	TOKimmutable("immutable", TokenNameimmutable),
	
	// Additional in Descent
	TOKwhitespace(TokenNameWHITESPACE), TOKPRAGMA(TokenNamePRAGMA),
	
	/** 
	 * FROM HERE, THIS TOKENS ARE NOT GENERATED BY THE LEXER,
	 * THEY ARE USED IN THE SEMANTIC ANALYSIS.
	 */
	TOKcat("~", TokenNameTILDE, PREC_add),
	TOKneg("!", TokenNameNOT, PREC_unary),
	
	// TODO: assign string, precedence, etc.
	TOKassocarrayliteral,
	TOKstructliteral,
	
	
	
	TOKnotis,
	TOKconstruct(PREC_assign),
	TOKblit(PREC_assign),
	
	TOKtls("__thread", TokenName__thread), TOKgshared("__gshared", TokenName__gshared),
	TOKline,
	TOKfile,

	//TOKMAX
	;

	public String value;
	public char[] charArrayValue;
	public int terminalSymbol;
	public PREC precedence;

	TOK() {
		value = this.name();
		this.precedence = PREC_zero;
	}
	
	TOK(int terminalSymbol) {
		this.terminalSymbol = terminalSymbol;
		this.precedence = PREC_zero;
	}

	TOK(String value, int terminalSymbol) {
		this.value = value;
		this.charArrayValue = value.toCharArray();
		this.terminalSymbol = terminalSymbol;
		this.precedence = PREC_zero;
	}
	
	TOK(int terminalSymbol, PREC precedence) {
		this.terminalSymbol = terminalSymbol;
		this.precedence = precedence;
	}
	
	TOK(PREC precedence) {
		this.precedence = precedence;
	}
	
	TOK(String value, int terminalSymbol, PREC precedence) {
		this.value = value;
		this.charArrayValue = value.toCharArray();
		this.terminalSymbol = terminalSymbol;
		this.precedence = precedence;
	}
	
	@Override
	public String toString() {
		return value;
	}

}
