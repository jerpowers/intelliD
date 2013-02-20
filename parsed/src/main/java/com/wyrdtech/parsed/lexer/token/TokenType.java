package com.wyrdtech.parsed.lexer.token;

/**
 *
 */
public enum TokenType {
    //TODO: comment types as sub-token-types?
    LineComment,
    BlockComment,
    DocComment,
    DocLineComment,
    BlockCommentNest,
    DocCommentNest,

    // ----- terminal classes -----
    EOF,
    Identifier,
    Literal,
    LiteralChar,
    LiteralUtf8,
    LiteralUtf16,
    LiteralUtf32,
    LiteralHex,

    // ----- special character -----
    Assign("="),
    Plus("+"),
    Minus("-"),
    Times("*"),
    Div("/"),
    Mod("%"),
    Colon(":"),
    DoubleDot(".."),
    Semicolon(";"),
    Question("?"),
    Dollar("$"),
    Comma(","),
    Dot("."),
    OpenCurlyBrace("{"),
    CloseCurlyBrace("}"),
    OpenSquareBracket("["),
    CloseSquareBracket("]"),
    OpenParenthesis("("),
    CloseParenthesis(")"),
    GreaterThan(">"),
    LessThan("<"),
    Not("!"),
    LogicalAnd("&&"),
    LogicalOr("||"),
    Tilde("~"),
    BitwiseAnd("&"),
    BitwiseOr("|"),
    Xor("^"),
    Increment("++"),
    Decrement("--"),
    Equal("=="),
    NotEqual("!="),
    GreaterEqual(">="),
    LessEqual("<="),
    ShiftLeft("<<"),
    PlusAssign("+="),
    MinusAssign("-="),
    TimesAssign("*="),
    DivAssign("/="),
    ModAssign("%="),
    BitwiseAndAssign("&="),
    BitwiseOrAssign("|="),
    XorAssign("^="),
    ShiftLeftAssign("<<="),
    TildeAssign("~="),
    ShiftRightAssign(">>="),
    TripleRightShiftAssign(">>>="),

    Pow("^^"),
    PowAssign("^^="),
    Unordered("!<>="),
    UnorderedOrEqual("!<>"),
    LessOrGreater("<>"),
    LessEqualOrGreater("<>="),
    UnorderedGreaterOrEqual("!<"),
    UnorderedOrLess("!>="),
    UnorderedLessOrEqual("!>"),
    UnorderedOrGreater("!<="),
    ShiftRight(">>"),
    ShiftRightUnsigned(">>>"),

    TripleDot("..."),
    At("@"),
    GoesTo("=>"), //  (lambda expressions)
    Hash("#"),

    // ----- keywords -----
    //TODO: just have enum name same as keyword?
    Abstract("abstract"),
    Alias("alias"),
    Align("align"),
    Asm("asm"),
    Assert("assert"),
    Auto("auto"),

    Body("body"),
    Bool("bool"),
    Break("break"),
    Byte("byte"),

    Case("case"),
    Cast("cast"),
    Catch("catch"),
    Cdouble("cdouble"),
    Cent("cent"),
    Cfloat("cfloat"),
    Char("char"),
    Class("class"),
    Const("const"),
    Continue("continue"),
    Creal("creal"),

    Dchar("dchar"),
    Debug("debug"),
    Default("default"),
    Delegate("delegate"),
    Delete("delete"),
    Deprecated("deprecated"),
    Do("do"),
    Double("double"),

    Else("else"),
    Enum("enum"),
    Export("export"),
    Extern("extern"),

    False("false"),
    Final("final"),
    Finally("finally"),
    Float("float"),
    For("for"),
    Foreach("foreach"),
    Foreach_Reverse("foreach_reverse"),
    Function("function"),

    Goto("goto"),

    Idouble("idouble"),
    If("if"),
    Ifloat("ifloat"),
    Immutable("immutable"),
    Import("import"),
    In("in"),
    InOut("inout"),
    Int("int"),
    Interface("interface"),
    Invariant("invariant"),
    Ireal("ireal"),
    Is("is"),

    Lazy("lazy"),
    Long("long"),

    Macro("macro"),
    Mixin("mixin"),
    Module("module"),

    New("new"),
    Nothrow("nothrow"),
    Null("null"),

    Out("out"),
    Override("override"),

    Package("package"),
    Pragma("pragma"),
    Private("private"),
    Protected("protected"),
    Public("public"),
    Pure("pure"),

    Real("real"),
    Ref("ref"),
    Return("return"),

    Scope("scope"),
    Shared("shared"),
    Short("short"),
    Static("static"),
    Struct("struct"),
    Super("super"),
    Switch("switch"),
    Synchronized("synchronized"),

    Template("template"),
    This("this"),
    Throw("throw"),
    True("true"),
    Try("try"),
    Typedef("typedef"),
    Typeid("typeid"),
    Typeof("typeof"),

    Ubyte("ubyte"),
    Ucent("ucent"),
    Uint("uint"),
    Ulong("ulong"),
    Union("union"),
    Unittest("unittest"),
    Ushort("ushort"),

    Version("version"),
    Void("void"),
    Volatile("volatile"),

    Wchar("wchar"),
    While("while"),
    With("with"),

    __FILE__("__FILE__"),
    __LINE__("__LINE__"),

    __gshared("__gshared"),
    __traits("__traits"),
    __vector("__vector"),
    __parameters("__parameters"),

    // Meta tokens
/*
    __DATE__("__DATE__"),
    __TIME__("__TIME__"),
    __TIMESTAMP__("__TIMESTAMP__"),
    __VENDOR__("__VENDOR__"),
    __VERSION__("__VERSION__"),
    __EOF__("__EOF__"),
*/
/*
    _unused(""),
    INVALID(""),
*/

    ;


    public final String value;

    private TokenType() {
        this.value = null;
    }
    private TokenType(String val) {
        this.value = val;
    }

    public static TokenType forValue(String str) {
        for (TokenType type : TokenType.values()) {
            if (str.equals(type.value)) {
                return type;
            }
        }
        return null;
    }

}
