package com.wyrdtech.d.intellid.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import descent.internal.compiler.parser.TOK;
import descent.internal.compiler.parser.Token;

import com.wyrdtech.d.intellid.DLanguage;

import java.util.HashMap;
import java.util.Map;

/**
 * Token types for the D language, mapping Descent parser tokens to IElementType objects.
 * Dynamically creates one instance of an IElementType for each Descent token (TOK) as
 * valueOf() is called.  Certain tokens are created ahead of time, to allow for easier
 * reference elsewhere - such as comment and whitespace tokens.
 */
public abstract class DTokenType implements TokenType {

    // Some explicit tokens
//    public static final IElementType UNKNOWN = new DElementType("Unknown");

    public static final IElementType EOF = new DElementType(TOK.TOKeof.name());
    public static final IElementType LINE_COMMENT = new DElementType(TOK.TOKlinecomment.name());
    public static final IElementType DOC_LINE_COMMENT = new DElementType(TOK.TOKdoclinecomment.name());
    public static final IElementType BLOCK_COMMENT = new DElementType(TOK.TOKblockcomment.name());
    public static final IElementType DOC_BLOCK_COMMENT = new DElementType(TOK.TOKdocblockcomment.name());
    public static final IElementType PLUS_COMMENT = new DElementType(TOK.TOKpluscomment.name());
    public static final IElementType DOC_PLUS_COMMENT = new DElementType(TOK.TOKdocpluscomment.name());

    public static final IElementType CHAR_LITERAL = new DElementType(TOK.TOKcharv.name());
    public static final IElementType WCHAR_LITERAL = new DElementType(TOK.TOKwcharv.name());
    public static final IElementType DCHAR_LITERAL = new DElementType(TOK.TOKdcharv.name());
    public static final IElementType STRING_LITERAL = new DElementType(TOK.TOKstring.name());


    // Token sets for special treatment while parsing
    public static final TokenSet WHITESPACES = TokenSet.create(TokenType.WHITE_SPACE, EOF);
    public static final TokenSet COMMENTS = TokenSet.create(LINE_COMMENT,
                                                            DOC_LINE_COMMENT,
                                                            BLOCK_COMMENT,
                                                            DOC_BLOCK_COMMENT,
                                                            PLUS_COMMENT,
                                                            DOC_PLUS_COMMENT);
    public static final TokenSet STRING_LITERALS = TokenSet.create(CHAR_LITERAL,
                                                                   WCHAR_LITERAL,
                                                                   DCHAR_LITERAL,
                                                                   STRING_LITERAL);

    private static Map<TOK, IElementType> tokens = new HashMap<TOK, IElementType>();
    static {
        // Pre-fill the map with the explicit tokens already created
        tokens.put(TOK.TOKwhitespace, TokenType.WHITE_SPACE);
        tokens.put(TOK.TOKeof, EOF);
        tokens.put(TOK.TOKlinecomment, LINE_COMMENT);
        tokens.put(TOK.TOKdoclinecomment, DOC_LINE_COMMENT);
        tokens.put(TOK.TOKblockcomment, BLOCK_COMMENT);
        tokens.put(TOK.TOKdocblockcomment, DOC_BLOCK_COMMENT);
        tokens.put(TOK.TOKpluscomment, PLUS_COMMENT);
        tokens.put(TOK.TOKdocpluscomment, DOC_PLUS_COMMENT);
        tokens.put(TOK.TOKcharv, CHAR_LITERAL);
        tokens.put(TOK.TOKwcharv, WCHAR_LITERAL);
        tokens.put(TOK.TOKdcharv, DCHAR_LITERAL);
        tokens.put(TOK.TOKstring, STRING_LITERAL);
    }

    // Find the DElementType for the given Descent token.
    // Multiple calls with the same token value will result in the same DElementType instance being returned.
    // TODO: examine whether thread safety is actually needed, optimize/remove
    public static synchronized IElementType valueOf(Token token) {
        if (token == null || token.value == null) {
            return null;
        }

        // Check the cache map for a matching token
        TOK tok = token.value;
        IElementType tokenType = tokens.get(tok);

        // If not in the map yet, create one and shove it in
        if (tokenType == null) {
            tokenType = new DElementType(tok.name());
            tokens.put(tok, tokenType);
        }

        return tokenType;
    }


/*
    IElementType INTEGER_LITERAL = new IJavaElementType("INTEGER_LITERAL");
    IElementType LONG_LITERAL = new IJavaElementType("LONG_LITERAL");
    IElementType FLOAT_LITERAL = new IJavaElementType("FLOAT_LITERAL");
    IElementType DOUBLE_LITERAL = new IJavaElementType("DOUBLE_LITERAL");
    IElementType CHARACTER_LITERAL = new IJavaElementType("CHARACTER_LITERAL");
    IElementType STRING_LITERAL = new IJavaElementType("STRING_LITERAL");

    IElementType BOOLEAN_KEYWORD = new IKeywordElementType("BOOLEAN_KEYWORD");
    IElementType BREAK_KEYWORD = new IKeywordElementType("BREAK_KEYWORD");
    IElementType BYTE_KEYWORD = new IKeywordElementType("BYTE_KEYWORD");
    IElementType CASE_KEYWORD = new IKeywordElementType("CASE_KEYWORD");
    IElementType CATCH_KEYWORD = new IKeywordElementType("CATCH_KEYWORD");
    IElementType CHAR_KEYWORD = new IKeywordElementType("CHAR_KEYWORD");
    IElementType CLASS_KEYWORD = new IKeywordElementType("CLASS_KEYWORD");
    IElementType CONST_KEYWORD = new IKeywordElementType("CONST_KEYWORD");
    IElementType CONTINUE_KEYWORD = new IKeywordElementType("CONTINUE_KEYWORD");
    IElementType DEFAULT_KEYWORD = new IKeywordElementType("DEFAULT_KEYWORD");
    IElementType DO_KEYWORD = new IKeywordElementType("DO_KEYWORD");
    IElementType DOUBLE_KEYWORD = new IKeywordElementType("DOUBLE_KEYWORD");
    IElementType ELSE_KEYWORD = new IKeywordElementType("ELSE_KEYWORD");
    IElementType ENUM_KEYWORD = new IKeywordElementType("ENUM_KEYWORD");
    IElementType EXTENDS_KEYWORD = new IKeywordElementType("EXTENDS_KEYWORD");
    IElementType FINAL_KEYWORD = new IKeywordElementType("FINAL_KEYWORD");
    IElementType FINALLY_KEYWORD = new IKeywordElementType("FINALLY_KEYWORD");
    IElementType FLOAT_KEYWORD = new IKeywordElementType("FLOAT_KEYWORD");
    IElementType FOR_KEYWORD = new IKeywordElementType("FOR_KEYWORD");
    IElementType GOTO_KEYWORD = new IKeywordElementType("GOTO_KEYWORD");
    IElementType IF_KEYWORD = new IKeywordElementType("IF_KEYWORD");
    IElementType IMPLEMENTS_KEYWORD = new IKeywordElementType("IMPLEMENTS_KEYWORD");
    IElementType IMPORT_KEYWORD = new IKeywordElementType("IMPORT_KEYWORD");
    IElementType INSTANCEOF_KEYWORD = new IKeywordElementType("INSTANCEOF_KEYWORD");
    IElementType INT_KEYWORD = new IKeywordElementType("INT_KEYWORD");
    IElementType INTERFACE_KEYWORD = new IKeywordElementType("INTERFACE_KEYWORD");
    IElementType LONG_KEYWORD = new IKeywordElementType("LONG_KEYWORD");
    IElementType NATIVE_KEYWORD = new IKeywordElementType("NATIVE_KEYWORD");
    IElementType PACKAGE_KEYWORD = new IKeywordElementType("PACKAGE_KEYWORD");
    IElementType PRIVATE_KEYWORD = new IKeywordElementType("PRIVATE_KEYWORD");
    IElementType PUBLIC_KEYWORD = new IKeywordElementType("PUBLIC_KEYWORD");
    IElementType SHORT_KEYWORD = new IKeywordElementType("SHORT_KEYWORD");
    IElementType SUPER_KEYWORD = new IKeywordElementType("SUPER_KEYWORD");
    IElementType SWITCH_KEYWORD = new IKeywordElementType("SWITCH_KEYWORD");
    IElementType SYNCHRONIZED_KEYWORD = new IKeywordElementType("SYNCHRONIZED_KEYWORD");
    IElementType THIS_KEYWORD = new IKeywordElementType("THIS_KEYWORD");
    IElementType PROTECTED_KEYWORD = new IKeywordElementType("PROTECTED_KEYWORD");
    IElementType TRANSIENT_KEYWORD = new IKeywordElementType("TRANSIENT_KEYWORD");
    IElementType RETURN_KEYWORD = new IKeywordElementType("RETURN_KEYWORD");
    IElementType VOID_KEYWORD = new IKeywordElementType("VOID_KEYWORD");
    IElementType STATIC_KEYWORD = new IKeywordElementType("STATIC_KEYWORD");
    IElementType STRICTFP_KEYWORD = new IKeywordElementType("STRICTFP_KEYWORD");
    IElementType WHILE_KEYWORD = new IKeywordElementType("WHILE_KEYWORD");
    IElementType TRY_KEYWORD = new IKeywordElementType("TRY_KEYWORD");
    IElementType VOLATILE_KEYWORD = new IKeywordElementType("VOLATILE_KEYWORD");
    IElementType THROWS_KEYWORD = new IKeywordElementType("THROWS_KEYWORD");


    IElementType DOUBLE_COLON = new IJavaElementType("DOUBLE_COLON");
    IElementType ARROW = new IJavaElementType("ARROW");

    IElementType DOT = new IJavaElementType("DOT");
    IElementType AT = new IJavaElementType("AT");

    IElementType ABSTRACT_KEYWORD = new IKeywordElementType("ABSTRACT_KEYWORD");

    IElementType IDENTIFIER = new IJavaElementType("IDENTIFIER");
*/


/*
    // Other
    public static final DElementType LPARENTH = new DElementType(TOK.TOKlparen.name()); // (
    public static final DElementType RPARENTH = new DElementType(TOK.TOKrparen.name()); // )
    public static final DElementType LBRACE = new DElementType(TOK.TOKlcurly.name()); // {
    public static final DElementType RBRACE = new DElementType(TOK.TOKrcurly.name()); // }
    public static final DElementType LBRACKET = new DElementType(TOK.TOKlbracket.name()); // [
    public static final DElementType RBRACKET = new DElementType(TOK.TOKrbracket.name()); // ]

    public static final DElementType COLON = new DElementType(TOK.TOKcolon.name()); // :
    public static final DElementType SEMICOLON = new DElementType(TOK.TOKsemicolon.name()); // ;
    public static final DElementType ELLIPSIS = new DElementType(TOK.TOKdotdotdot.name()); // ...

    public static final DElementType EOF = new DElementType(TOK.TOKeof.name()); // EOF
    public static final DElementType CAST = new DElementType(TOK.TOKcast.name()); // cast

    public static final DElementType NULL_KEYWORD = new DElementType(TOK.TOKnull.name()); // null
    public static final DElementType ASSERT_KEYWORD = new DElementType(TOK.TOKassert.name()); // assert
    public static final DElementType TRUE_KEYWORD = new DElementType(TOK.TOKtrue.name()); // true
    public static final DElementType FALSE_KEYWORD = new DElementType(TOK.TOKfalse.name()); // false

    public static final DElementType ARRAY = new DElementType(TOK.TOKarray.name());
    public static final DElementType CALL = new DElementType(TOK.TOKcall.name());

    public static final DElementType ADDRESS = new DElementType(TOK.TOKaddress.name());
    public static final DElementType TYPE_DOT = new DElementType(TOK.TOKtypedot.name());
    public static final DElementType TYPE = new DElementType(TOK.TOKtype.name());

    public static final DElementType THROW_KEYWORD = new DElementType(TOK.TOKthrow.name());
    public static final DElementType NEW_KEYWORD = new DElementType(TOK.TOKnew.name());
    public static final DElementType DELETE = new DElementType(TOK.TOKdelete.name());
    public static final DElementType STAR = new DElementType(TOK.TOKstar.name());

    public static final DElementType SYMOFF = new DElementType(TOK.TOKsymoff.name());
    public static final DElementType VAR = new DElementType(TOK.TOKvar.name());
    public static final DElementType DOT_VAR = new DElementType(TOK.TOKdotvar.name());
    public static final DElementType DOT_TI = new DElementType(TOK.TOKdotti.name());
    public static final DElementType DOT_EXP = new DElementType(TOK.TOKdotexp.name());
    public static final DElementType DOT_TYPE = new DElementType(TOK.TOKdottype.name());

    public static final DElementType SLICE = new DElementType(TOK.TOKslice.name());

    public static final DElementType ARRAY_LENGHT = new DElementType(TOK.TOKarraylength.name());

    public static final DElementType VERSION = new DElementType(TOK.TOKversion.name());
    public static final DElementType MODULE = new DElementType(TOK.TOKmodule.name());

    public static final DElementType DOLLAR = new DElementType(TOK.TOKdollar.name());
    public static final DElementType TEMPLATE = new DElementType(TOK.TOKtemplate.name());

    public static final DElementType DOT_TD = new DElementType(TOK.TOKdottd.name());
    public static final DElementType DECLARATION = new DElementType(TOK.TOKdeclaration.name());

    public static final DElementType TYPEOF = new DElementType(TOK.TOKtypeof.name());
    public static final DElementType PRAGMA = new DElementType(TOK.TOKpragma.name());

    public static final DElementType D_SYMBOL = new DElementType(TOK.TOKdsymbol.name());
    public static final DElementType TYPEID = new DElementType(TOK.TOKtypeid.name());

    public static final DElementType UADD = new DElementType(TOK.TOKuadd.name());

    public static final DElementType IFTYPE = new DElementType(TOK.TOKiftype.name());

    public static final DElementType REMOVE = new DElementType(TOK.TOKremove.name());
    public static final DElementType NEW_ANON_CLASS = new DElementType(TOK.TOKnewanonclass.name());

    public static final DElementType LINE_COMMENT = new DElementType(TOK.TOKlinecomment.name());
    public static final DElementType DOC_LINE_COMMENT = new DElementType(TOK.TOKdoclinecomment.name());
    public static final DElementType BLOCK_COMMENT = new DElementType(TOK.TOKblockcomment.name());
    public static final DElementType DOC_BLOCK_COMMENT = new DElementType(TOK.TOKdocblockcomment.name());
    public static final DElementType PLUS_COMMENT = new DElementType(TOK.TOKpluscomment.name());
    public static final DElementType DOC_PLUS_COMMENT = new DElementType(TOK.TOKdocpluscomment.name());

    public static final DElementType ARRAY_LITERAL = new DElementType(TOK.TOKarrayliteral.name());
*/

/*

    // Operators
//    IElementType EQ = new IJavaElementType("EQ");
    IElementType EXCL = new IJavaElementType("EXCL");
    IElementType TILDE = new IJavaElementType("TILDE");
    IElementType QUEST = new IJavaElementType("QUEST");
    IElementType PLUS = new IJavaElementType("PLUS");
    IElementType MINUS = new IJavaElementType("MINUS");
    IElementType ASTERISK = new IJavaElementType("ASTERISK");
    IElementType DIV = new IJavaElementType("DIV");
    IElementType AND = new IJavaElementType("AND");
    IElementType OR = new IJavaElementType("OR");
    IElementType XOR = new IJavaElementType("XOR");
    IElementType PERC = new IJavaElementType("PERC");

    IElementType ANDAND = new IJavaElementType("ANDAND");
    IElementType OROR = new IJavaElementType("OROR");
    IElementType PLUSPLUS = new IJavaElementType("PLUSPLUS");
    IElementType MINUSMINUS = new IJavaElementType("MINUSMINUS");
    IElementType LTLT = new IJavaElementType("LTLT");
    IElementType GTGT = new IJavaElementType("GTGT");
    IElementType GTGTGT = new IJavaElementType("GTGTGT");
    IElementType PLUSEQ = new IJavaElementType("PLUSEQ");
    IElementType MINUSEQ = new IJavaElementType("MINUSEQ");
    IElementType ASTERISKEQ = new IJavaElementType("ASTERISKEQ");
    IElementType DIVEQ = new IJavaElementType("DIVEQ");
    IElementType ANDEQ = new IJavaElementType("ANDEQ");
    IElementType OREQ = new IJavaElementType("OREQ");
    IElementType XOREQ = new IJavaElementType("XOREQ");
    IElementType PERCEQ = new IJavaElementType("PERCEQ");
    IElementType LTLTEQ = new IJavaElementType("LTLTEQ");
    IElementType GTGTEQ = new IJavaElementType("GTGTEQ");
    IElementType GTGTGTEQ = new IJavaElementType("GTGTGTEQ");

    public static final DElementType LT = new DElementType(TOK.TOKlt.name()); // <
    public static final DElementType GT = new DElementType(TOK.TOKgt.name()); // >
    public static final DElementType LE = new DElementType(TOK.TOKle.name()); // <=
    public static final DElementType GE = new DElementType(TOK.TOKge.name()); // >=
    public static final DElementType EQ = new DElementType(TOK.TOKequal.name()); // ==
    public static final DElementType NE = new DElementType(TOK.TOKnotequal.name()); // !=
    public static final DElementType IDENTITY = new DElementType(TOK.TOKidentity.name()); // ===
    public static final DElementType NOT_IDENTITY = new DElementType(TOK.TOKnotidentity.name()); // !==

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

    IElementType COMMA = new IJavaElementType("COMMA");

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

    */
/**
     * FROM HERE, THIS TOKENS ARE NOT GENERATED BY THE LEXER,
     * THEY ARE USED IN THE SEMANTIC ANALYSIS.
     *//*

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

*/


}
