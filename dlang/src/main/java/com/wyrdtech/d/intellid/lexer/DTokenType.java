package com.wyrdtech.d.intellid.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import com.wyrdtech.parsed.lexer.Token;
import com.wyrdtech.parsed.lexer.TokenType;

import java.util.HashMap;
import java.util.Map;

/**
 * Token types for the D language, mapping ParserD types to IElementType objects.
 * Dynamically creates one instance of an IElementType for each ParserD type as
 * valueOf() is called.  Certain tokens are created ahead of time, to allow for easier
 * reference elsewhere - such as comment and whitespace tokens.
 */
public abstract class DTokenType implements com.intellij.psi.TokenType {

    // Some explicit tokens
//    public static final IElementType UNKNOWN = new DElementType("Unknown");
    public static final IElementType EOF = new DElementType(TokenType.EOF.name());

    public static final IElementType LINE_COMMENT = new DElementType(TokenType.LineComment.name());
    public static final IElementType DOC_LINE_COMMENT = new DElementType(TokenType.DocLineComment.name());
    public static final IElementType BLOCK_COMMENT = new DElementType(TokenType.BlockComment.name());
    public static final IElementType BLOCK_COMMENT_NEST = new DElementType(TokenType.BlockCommentNest.name());
    public static final IElementType DOC_COMMENT = new DElementType(TokenType.DocComment.name());
    public static final IElementType DOC_COMMENT_NEST = new DElementType(TokenType.DocCommentNest.name());

    public static final IElementType LITERAL = new DElementType(TokenType.Literal.name());
    public static final IElementType CHAR_LITERAL = new DElementType(TokenType.LiteralChar.name());
    public static final IElementType STRING_LITERAL = new DElementType(TokenType.LiteralUtf8.name());
    public static final IElementType WSTR_LITERAL = new DElementType(TokenType.LiteralUtf16.name());
    public static final IElementType DSTR_LITERAL = new DElementType(TokenType.LiteralUtf32.name());

    public static final IElementType IDENTIFIER = new DElementType(TokenType.Identifier.name());
    public static final IElementType KEYWORD = new DElementType("Keyword");
    public static final IElementType OPERATOR = new DElementType("Operator");

    public static final IElementType PARENS = new DElementType("Parenthesis");
    public static final IElementType BRACE = new DElementType("Brace");
    public static final IElementType BRACKET = new DElementType("Bracket");

    // Token sets for special treatment while parsing
    public static final TokenSet WHITESPACES = TokenSet.create(com.intellij.psi.TokenType.WHITE_SPACE, EOF);
    public static final TokenSet COMMENTS = TokenSet.create(LINE_COMMENT,
                                                            DOC_LINE_COMMENT,
                                                            BLOCK_COMMENT,
                                                            BLOCK_COMMENT_NEST,
                                                            DOC_COMMENT,
                                                            DOC_COMMENT_NEST);
    public static final TokenSet STRING_LITERALS = TokenSet.create(CHAR_LITERAL,
                                                                   WSTR_LITERAL,
                                                                   DSTR_LITERAL,
                                                                   STRING_LITERAL);

    private static Map<TokenType, IElementType> tokens = new HashMap<TokenType, IElementType>();
    static {
        // Pre-fill the map with the explicit tokens already created
//        tokens.put(TOK.TOKwhitespace, TokenType.WHITE_SPACE);
        tokens.put(TokenType.EOF, EOF);
        tokens.put(TokenType.LineComment, LINE_COMMENT);
        tokens.put(TokenType.DocLineComment, DOC_LINE_COMMENT);
        tokens.put(TokenType.BlockComment, BLOCK_COMMENT);
        tokens.put(TokenType.BlockCommentNest, BLOCK_COMMENT_NEST);
        tokens.put(TokenType.DocComment, DOC_COMMENT);
        tokens.put(TokenType.DocCommentNest, DOC_COMMENT_NEST);
        tokens.put(TokenType.Literal, LITERAL);
        tokens.put(TokenType.LiteralChar, CHAR_LITERAL);
        tokens.put(TokenType.LiteralUtf8, STRING_LITERAL);
        tokens.put(TokenType.LiteralUtf16, WSTR_LITERAL);
        tokens.put(TokenType.LiteralUtf32, DSTR_LITERAL);

        tokens.put(TokenType.Identifier, IDENTIFIER);

        tokens.put(TokenType.OpenCurlyBrace, BRACE);
        tokens.put(TokenType.CloseCurlyBrace, BRACE);
        tokens.put(TokenType.OpenSquareBracket, BRACKET);
        tokens.put(TokenType.CloseSquareBracket, BRACKET);
        tokens.put(TokenType.OpenParenthesis, PARENS);
        tokens.put(TokenType.CloseParenthesis, PARENS);
    }

    static {
        //TODO: not this
        tokens.put(TokenType.Abstract, KEYWORD);
        tokens.put(TokenType.Alias, KEYWORD);
        tokens.put(TokenType.Align, KEYWORD);
        tokens.put(TokenType.Asm, KEYWORD);
        tokens.put(TokenType.Assert, KEYWORD);
        tokens.put(TokenType.Auto, KEYWORD);
        tokens.put(TokenType.Body, KEYWORD);
        tokens.put(TokenType.Bool, KEYWORD);
        tokens.put(TokenType.Break, KEYWORD);
        tokens.put(TokenType.Byte, KEYWORD);
        tokens.put(TokenType.Case, KEYWORD);
        tokens.put(TokenType.Cast, KEYWORD);
        tokens.put(TokenType.Catch, KEYWORD);
        tokens.put(TokenType.Cdouble, KEYWORD);
        tokens.put(TokenType.Cent, KEYWORD);
        tokens.put(TokenType.Cfloat, KEYWORD);
        tokens.put(TokenType.Char, KEYWORD);
        tokens.put(TokenType.Class, KEYWORD);
        tokens.put(TokenType.Const, KEYWORD);
        tokens.put(TokenType.Continue, KEYWORD);
        tokens.put(TokenType.Creal, KEYWORD);
        tokens.put(TokenType.Dchar, KEYWORD);
        tokens.put(TokenType.Debug, KEYWORD);
        tokens.put(TokenType.Default, KEYWORD);
        tokens.put(TokenType.Delegate, KEYWORD);
        tokens.put(TokenType.Delete, KEYWORD);
        tokens.put(TokenType.Deprecated, KEYWORD);
        tokens.put(TokenType.Do, KEYWORD);
        tokens.put(TokenType.Double, KEYWORD);
        tokens.put(TokenType.Else, KEYWORD);
        tokens.put(TokenType.Enum, KEYWORD);
        tokens.put(TokenType.Export, KEYWORD);
        tokens.put(TokenType.Extern, KEYWORD);
        tokens.put(TokenType.False, KEYWORD);
        tokens.put(TokenType.Final, KEYWORD);
        tokens.put(TokenType.Finally, KEYWORD);
        tokens.put(TokenType.Float, KEYWORD);
        tokens.put(TokenType.For, KEYWORD);
        tokens.put(TokenType.Foreach, KEYWORD);
        tokens.put(TokenType.Foreach_Reverse, KEYWORD);
        tokens.put(TokenType.Function, KEYWORD);
        tokens.put(TokenType.Goto, KEYWORD);
        tokens.put(TokenType.Idouble, KEYWORD);
        tokens.put(TokenType.If, KEYWORD);
        tokens.put(TokenType.Ifloat, KEYWORD);
        tokens.put(TokenType.Immutable, KEYWORD);
        tokens.put(TokenType.Import, KEYWORD);
        tokens.put(TokenType.In, KEYWORD);
        tokens.put(TokenType.InOut, KEYWORD);
        tokens.put(TokenType.Int, KEYWORD);
        tokens.put(TokenType.Interface, KEYWORD);
        tokens.put(TokenType.Invariant, KEYWORD);
        tokens.put(TokenType.Ireal, KEYWORD);
        tokens.put(TokenType.Is, KEYWORD);
        tokens.put(TokenType.Lazy, KEYWORD);
        tokens.put(TokenType.Long, KEYWORD);
        tokens.put(TokenType.Macro, KEYWORD);
        tokens.put(TokenType.Mixin, KEYWORD);
        tokens.put(TokenType.Module, KEYWORD);
        tokens.put(TokenType.New, KEYWORD);
        tokens.put(TokenType.Nothrow, KEYWORD);
        tokens.put(TokenType.Null, KEYWORD);
        tokens.put(TokenType.Out, KEYWORD);
        tokens.put(TokenType.Override, KEYWORD);
        tokens.put(TokenType.Package, KEYWORD);
        tokens.put(TokenType.Pragma, KEYWORD);
        tokens.put(TokenType.Private, KEYWORD);
        tokens.put(TokenType.Protected, KEYWORD);
        tokens.put(TokenType.Public, KEYWORD);
        tokens.put(TokenType.Pure, KEYWORD);
        tokens.put(TokenType.Real, KEYWORD);
        tokens.put(TokenType.Ref, KEYWORD);
        tokens.put(TokenType.Return, KEYWORD);
        tokens.put(TokenType.Scope, KEYWORD);
        tokens.put(TokenType.Shared, KEYWORD);
        tokens.put(TokenType.Short, KEYWORD);
        tokens.put(TokenType.Static, KEYWORD);
        tokens.put(TokenType.Struct, KEYWORD);
        tokens.put(TokenType.Super, KEYWORD);
        tokens.put(TokenType.Switch, KEYWORD);
        tokens.put(TokenType.Synchronized, KEYWORD);
        tokens.put(TokenType.Template, KEYWORD);
        tokens.put(TokenType.This, KEYWORD);
        tokens.put(TokenType.Throw, KEYWORD);
        tokens.put(TokenType.True, KEYWORD);
        tokens.put(TokenType.Try, KEYWORD);
        tokens.put(TokenType.Typedef, KEYWORD);
        tokens.put(TokenType.Typeid, KEYWORD);
        tokens.put(TokenType.Typeof, KEYWORD);
        tokens.put(TokenType.Ubyte, KEYWORD);
        tokens.put(TokenType.Ucent, KEYWORD);
        tokens.put(TokenType.Uint, KEYWORD);
        tokens.put(TokenType.Ulong, KEYWORD);
        tokens.put(TokenType.Union, KEYWORD);
        tokens.put(TokenType.Unittest, KEYWORD);
        tokens.put(TokenType.Ushort, KEYWORD);
        tokens.put(TokenType.Version, KEYWORD);
        tokens.put(TokenType.Void, KEYWORD);
        tokens.put(TokenType.Volatile, KEYWORD);
        tokens.put(TokenType.Wchar, KEYWORD);
        tokens.put(TokenType.While, KEYWORD);
        tokens.put(TokenType.With, KEYWORD);
        tokens.put(TokenType.__FILE__, KEYWORD);
        tokens.put(TokenType.__LINE__, KEYWORD);
        tokens.put(TokenType.__gshared, KEYWORD);
        tokens.put(TokenType.__traits, KEYWORD);
        tokens.put(TokenType.__vector, KEYWORD);
        tokens.put(TokenType.__parameters, KEYWORD);
    }

    static {
        //TODO: also not this
        tokens.put(TokenType.Assign, OPERATOR);
        tokens.put(TokenType.Plus, OPERATOR);
        tokens.put(TokenType.Minus, OPERATOR);
        tokens.put(TokenType.Times, OPERATOR);
        tokens.put(TokenType.Div, OPERATOR);
        tokens.put(TokenType.Mod, OPERATOR);
        tokens.put(TokenType.Colon, OPERATOR);
        tokens.put(TokenType.DoubleDot, OPERATOR);
        tokens.put(TokenType.Semicolon, OPERATOR);
        tokens.put(TokenType.Question, OPERATOR);
        tokens.put(TokenType.Dollar, OPERATOR);
        tokens.put(TokenType.Comma, OPERATOR);
        tokens.put(TokenType.Dot, OPERATOR);
        tokens.put(TokenType.GreaterThan, OPERATOR);
        tokens.put(TokenType.LessThan, OPERATOR);
        tokens.put(TokenType.Not, OPERATOR);
        tokens.put(TokenType.LogicalAnd, OPERATOR);
        tokens.put(TokenType.LogicalOr, OPERATOR);
        tokens.put(TokenType.Tilde, OPERATOR);
        tokens.put(TokenType.BitwiseAnd, OPERATOR);
        tokens.put(TokenType.BitwiseOr, OPERATOR);
        tokens.put(TokenType.Xor, OPERATOR);
        tokens.put(TokenType.Increment, OPERATOR);
        tokens.put(TokenType.Decrement, OPERATOR);
        tokens.put(TokenType.Equal, OPERATOR);
        tokens.put(TokenType.NotEqual, OPERATOR);
        tokens.put(TokenType.GreaterEqual, OPERATOR);
        tokens.put(TokenType.LessEqual, OPERATOR);
        tokens.put(TokenType.ShiftLeft, OPERATOR);
        tokens.put(TokenType.PlusAssign, OPERATOR);
        tokens.put(TokenType.MinusAssign, OPERATOR);
        tokens.put(TokenType.TimesAssign, OPERATOR);
        tokens.put(TokenType.DivAssign, OPERATOR);
        tokens.put(TokenType.ModAssign, OPERATOR);
        tokens.put(TokenType.BitwiseAndAssign, OPERATOR);
        tokens.put(TokenType.BitwiseOrAssign, OPERATOR);
        tokens.put(TokenType.XorAssign, OPERATOR);
        tokens.put(TokenType.ShiftLeftAssign, OPERATOR);
        tokens.put(TokenType.TildeAssign, OPERATOR);
        tokens.put(TokenType.ShiftRightAssign, OPERATOR);
        tokens.put(TokenType.TripleRightShiftAssign, OPERATOR);
        tokens.put(TokenType.Pow, OPERATOR);
        tokens.put(TokenType.PowAssign, OPERATOR);
        tokens.put(TokenType.Unordered, OPERATOR);
        tokens.put(TokenType.UnorderedOrEqual, OPERATOR);
        tokens.put(TokenType.LessOrGreater, OPERATOR);
        tokens.put(TokenType.LessEqualOrGreater, OPERATOR);
        tokens.put(TokenType.UnorderedGreaterOrEqual, OPERATOR);
        tokens.put(TokenType.UnorderedOrLess, OPERATOR);
        tokens.put(TokenType.UnorderedLessOrEqual, OPERATOR);
        tokens.put(TokenType.UnorderedOrGreater, OPERATOR);
        tokens.put(TokenType.ShiftRight, OPERATOR);
        tokens.put(TokenType.ShiftRightUnsigned, OPERATOR);
        tokens.put(TokenType.TripleDot, OPERATOR);
        tokens.put(TokenType.At, OPERATOR);
        tokens.put(TokenType.GoesTo, OPERATOR);
        tokens.put(TokenType.Hash, OPERATOR);
    }


    // Find the DElementType for the given ParseD token.
    // Multiple calls with the same token value will result in the same DElementType instance being returned.
    // TODO: examine whether thread safety is actually needed, optimize/remove
    public static synchronized IElementType valueOf(Token token) {
        if (token == null || token.type == null) {
            return null;
        }

        // Check the cache map for a matching token
        IElementType tokenType = tokens.get(token.type);

        // If not in the map yet, create one and shove it in
        if (tokenType == null) {
            tokenType = new DElementType(token.type.name());
            tokens.put(token.type, tokenType);
        }

        return tokenType;
    }

}
