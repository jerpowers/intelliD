package com.wyrdtech.dlang.lexer;

/**
 *
 */
public enum TokenType {

    // ----- terminal classes -----
    EOF,
    Identifier,
    Literal,

    // ----- special character -----
    Assign,
    Plus,
    Minus,
    Times,
    Div,
    Mod,
    Colon,
    DoubleDot, // ..
    Semicolon,
    Question,
    Dollar,
    Comma,
    Dot,
    OpenCurlyBrace,
    CloseCurlyBrace,
    OpenSquareBracket,
    CloseSquareBracket,
    OpenParenthesis,
    CloseParenthesis,
    GreaterThan,
    LessThan,
    Not,
    LogicalAnd,
    LogicalOr,
    Tilde,
    BitwiseAnd,
    BitwiseOr,
    Xor,
    Increment,
    Decrement,
    Equal,
    NotEqual,
    GreaterEqual,
    LessEqual,
    ShiftLeft,
    PlusAssign,
    MinusAssign,
    TimesAssign,
    DivAssign,
    ModAssign,
    BitwiseAndAssign,
    BitwiseOrAssign,
    XorAssign,
    ShiftLeftAssign,
    TildeAssign,
    ShiftRightAssign,
    TripleRightShiftAssign,

    // ----- keywords -----
    Align,
    Asm,
    Assert,
    Auto,
    Body,
    Bool,
    Break,
    Byte,
    Case,
    Cast,
    Catch,
    Cdouble,
    Cent,
    Cfloat,
    Char,
    Class,
    Const,
    Continue,
    Creal,
    Dchar,
    Debug,
    Default,
    Delegate,
    Delete,
    Deprecated,
    Do,
    Double,
    Else,
    Enum,
    Export,
    Extern,
    False,
    Final,
    Finally,
    Float,
    For,
    Foreach,
    Foreach_Reverse,
    Function,
    Goto,
    Idouble,
    If,
    Ifloat,
    Import,
    Immutable,
    In,
    InOut,
    Int,
    Interface,
    Invariant,
    Ireal,
    Is,
    Lazy,
    Long,
    empty1,
    Mixin,
    Module,
    New,
    Nothrow,
    Null,
    Out,
    Override,
    Package,
    Pragma,
    Private,
    Protected,
    Public,
    Pure,
    Real,
    Ref,
    Return,
    Scope,
    Shared,
    Short,
    Static,
    Struct,
    Super,
    Switch,
    Synchronized,
    Template,
    This,
    Throw,
    True,
    Try,
    Typedef,
    Typeid,
    Typeof,
    Ubyte,
    Ucent,
    Uint,
    Ulong,
    Union,
    Unittest,
    Ushort,
    Version,
    Void,
    Volatile,
    Wchar,
    While,
    With,
    __gshared,
    /// <summary>
    /// @
    /// </summary>
    At,
    __traits,
    Abstract,
    Alias,
    _unused,
    GoesTo, // =>  (lambda expressions)
    INVALID,
    __vector,

    // Additional operators
    /// <summary>
    /// ^^=
    /// </summary>
    PowAssign,
    /// <summary>
    /// !&lt;&gt;=
    /// </summary>
    Unordered,
    /// <summary>
    /// !&lt;&gt;
    /// </summary>
    UnorderedOrEqual,
    LessOrGreater, // <>
    LessEqualOrGreater, // <>=
    UnorderedGreaterOrEqual, // !<
    UnorderedOrLess, // !>=
    /// <summary>
    /// !&gt;
    /// </summary>
    UnorderedLessOrEqual, // !>
    UnorderedOrGreater, // !<=
    /// <summary>
    /// &gt;&gt;
    /// </summary>
    ShiftRight, // >>
    /// <summary>
    /// &gt;&gt;&gt;
    /// </summary>
    ShiftRightUnsigned,
    Pow, // ^^

    TripleDot, // ...

    // Meta tokens
    __VERSION__,
    __FILE__,
    __LINE__,
    __EOF__,

    __DATE__,
    __TIME__,
    __TIMESTAMP__,
    __VENDOR__,

}
