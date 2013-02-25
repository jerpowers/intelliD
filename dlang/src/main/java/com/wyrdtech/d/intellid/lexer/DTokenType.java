package com.wyrdtech.d.intellid.lexer;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import com.wyrdtech.parsed.lexer.token.Token;
import com.wyrdtech.parsed.lexer.token.TokenType;

import java.util.HashMap;
import java.util.Map;

/**
 * Type of tokens valid for the D language, mapping ParserD types to
 * DElementType objects.
 * Dynamically creates one instance of a DElementType for each ParserD type as
 * valueOf() is called.  Certain tokens are created ahead of time, to allow for easier
 * reference elsewhere - such as comment and whitespace tokens.
 */
public abstract class DTokenType implements com.intellij.psi.TokenType {

    private static Map<TokenType, DElementType> tokens;


    // Some explicit tokens
//    public static final IElementType UNKNOWN = valueOf("Unknown");
    public static final DElementType EOF = valueOf(TokenType.EOF);

    public static final DElementType LINE_COMMENT = valueOf(TokenType.LineComment);
    public static final DElementType DOC_LINE_COMMENT = valueOf(TokenType.DocLineComment);
    public static final DElementType BLOCK_COMMENT = valueOf(TokenType.BlockComment);
    public static final DElementType BLOCK_COMMENT_NEST = valueOf(TokenType.BlockCommentNest);
    public static final DElementType DOC_COMMENT = valueOf(TokenType.DocComment);
    public static final DElementType DOC_COMMENT_NEST = valueOf(TokenType.DocCommentNest);

    public static final DElementType LITERAL = valueOf(TokenType.Literal);
    public static final DElementType CHAR_LITERAL = valueOf(TokenType.LiteralChar);
    public static final DElementType STRING_LITERAL = valueOf(TokenType.LiteralUtf8);
    public static final DElementType WSTR_LITERAL = valueOf(TokenType.LiteralUtf16);
    public static final DElementType DSTR_LITERAL = valueOf(TokenType.LiteralUtf32);

    public static final DElementType IDENTIFIER = valueOf(TokenType.Identifier);

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

    public static final TokenSet PARENS = TokenSet.create(valueOf(TokenType.OpenParenthesis),
                                                          valueOf(TokenType.CloseParenthesis));
    public static final TokenSet BRACE = TokenSet.create(valueOf(TokenType.OpenCurlyBrace),
                                                         valueOf(TokenType.CloseCurlyBrace));
    public static final TokenSet BRACKET = TokenSet.create(valueOf(TokenType.OpenSquareBracket),
                                                           valueOf(TokenType.CloseSquareBracket));

    public static final TokenSet KEYWORD;
    public static final TokenSet OPERATOR;

    static {
        KEYWORD = TokenSet.create(
            valueOf(TokenType.Abstract),
            valueOf(TokenType.Alias),
            valueOf(TokenType.Align),
            valueOf(TokenType.Asm),
            valueOf(TokenType.Assert),
            valueOf(TokenType.Auto),
            valueOf(TokenType.Body),
            valueOf(TokenType.Bool),
            valueOf(TokenType.Break),
            valueOf(TokenType.Byte),
            valueOf(TokenType.Case),
            valueOf(TokenType.Cast),
            valueOf(TokenType.Catch),
            valueOf(TokenType.Cdouble),
            valueOf(TokenType.Cent),
            valueOf(TokenType.Cfloat),
            valueOf(TokenType.Char),
            valueOf(TokenType.Class),
            valueOf(TokenType.Const),
            valueOf(TokenType.Continue),
            valueOf(TokenType.Creal),
            valueOf(TokenType.Dchar),
            valueOf(TokenType.Debug),
            valueOf(TokenType.Default),
            valueOf(TokenType.Delegate),
            valueOf(TokenType.Delete),
            valueOf(TokenType.Deprecated),
            valueOf(TokenType.Do),
            valueOf(TokenType.Double),
            valueOf(TokenType.Else),
            valueOf(TokenType.Enum),
            valueOf(TokenType.Export),
            valueOf(TokenType.Extern),
            valueOf(TokenType.False),
            valueOf(TokenType.Final),
            valueOf(TokenType.Finally),
            valueOf(TokenType.Float),
            valueOf(TokenType.For),
            valueOf(TokenType.Foreach),
            valueOf(TokenType.Foreach_Reverse),
            valueOf(TokenType.Function),
            valueOf(TokenType.Goto),
            valueOf(TokenType.Idouble),
            valueOf(TokenType.If),
            valueOf(TokenType.Ifloat),
            valueOf(TokenType.Immutable),
            valueOf(TokenType.Import),
            valueOf(TokenType.In),
            valueOf(TokenType.InOut),
            valueOf(TokenType.Int),
            valueOf(TokenType.Interface),
            valueOf(TokenType.Invariant),
            valueOf(TokenType.Ireal),
            valueOf(TokenType.Is),
            valueOf(TokenType.Lazy),
            valueOf(TokenType.Long),
            valueOf(TokenType.Macro),
            valueOf(TokenType.Mixin),
            valueOf(TokenType.Module),
            valueOf(TokenType.New),
            valueOf(TokenType.Nothrow),
            valueOf(TokenType.Null),
            valueOf(TokenType.Out),
            valueOf(TokenType.Override),
            valueOf(TokenType.Package),
            valueOf(TokenType.Pragma),
            valueOf(TokenType.Private),
            valueOf(TokenType.Protected),
            valueOf(TokenType.Public),
            valueOf(TokenType.Pure),
            valueOf(TokenType.Real),
            valueOf(TokenType.Ref),
            valueOf(TokenType.Return),
            valueOf(TokenType.Scope),
            valueOf(TokenType.Shared),
            valueOf(TokenType.Short),
            valueOf(TokenType.Static),
            valueOf(TokenType.Struct),
            valueOf(TokenType.Super),
            valueOf(TokenType.Switch),
            valueOf(TokenType.Synchronized),
            valueOf(TokenType.Template),
            valueOf(TokenType.This),
            valueOf(TokenType.Throw),
            valueOf(TokenType.True),
            valueOf(TokenType.Try),
            valueOf(TokenType.Typedef),
            valueOf(TokenType.Typeid),
            valueOf(TokenType.Typeof),
            valueOf(TokenType.Ubyte),
            valueOf(TokenType.Ucent),
            valueOf(TokenType.Uint),
            valueOf(TokenType.Ulong),
            valueOf(TokenType.Union),
            valueOf(TokenType.Unittest),
            valueOf(TokenType.Ushort),
            valueOf(TokenType.Version),
            valueOf(TokenType.Void),
            valueOf(TokenType.Volatile),
            valueOf(TokenType.Wchar),
            valueOf(TokenType.While),
            valueOf(TokenType.With),
            valueOf(TokenType.__FILE__),
            valueOf(TokenType.__LINE__),
            valueOf(TokenType.__gshared),
            valueOf(TokenType.__traits),
            valueOf(TokenType.__vector),
            valueOf(TokenType.__parameters)
            );
    }

    static {
        OPERATOR = TokenSet.create(
            valueOf(TokenType.Assign),
            valueOf(TokenType.Plus),
            valueOf(TokenType.Minus),
            valueOf(TokenType.Times),
            valueOf(TokenType.Div),
            valueOf(TokenType.Mod),
            valueOf(TokenType.Colon),
            valueOf(TokenType.DoubleDot),
            valueOf(TokenType.Semicolon),
            valueOf(TokenType.Question),
            valueOf(TokenType.Dollar),
            valueOf(TokenType.Comma),
            valueOf(TokenType.Dot),
            valueOf(TokenType.GreaterThan),
            valueOf(TokenType.LessThan),
            valueOf(TokenType.Not),
            valueOf(TokenType.LogicalAnd),
            valueOf(TokenType.LogicalOr),
            valueOf(TokenType.Tilde),
            valueOf(TokenType.BitwiseAnd),
            valueOf(TokenType.BitwiseOr),
            valueOf(TokenType.Xor),
            valueOf(TokenType.Increment),
            valueOf(TokenType.Decrement),
            valueOf(TokenType.Equal),
            valueOf(TokenType.NotEqual),
            valueOf(TokenType.GreaterEqual),
            valueOf(TokenType.LessEqual),
            valueOf(TokenType.ShiftLeft),
            valueOf(TokenType.PlusAssign),
            valueOf(TokenType.MinusAssign),
            valueOf(TokenType.TimesAssign),
            valueOf(TokenType.DivAssign),
            valueOf(TokenType.ModAssign),
            valueOf(TokenType.BitwiseAndAssign),
            valueOf(TokenType.BitwiseOrAssign),
            valueOf(TokenType.XorAssign),
            valueOf(TokenType.ShiftLeftAssign),
            valueOf(TokenType.TildeAssign),
            valueOf(TokenType.ShiftRightAssign),
            valueOf(TokenType.TripleRightShiftAssign),
            valueOf(TokenType.Pow),
            valueOf(TokenType.PowAssign),
            valueOf(TokenType.Unordered),
            valueOf(TokenType.UnorderedOrEqual),
            valueOf(TokenType.LessOrGreater),
            valueOf(TokenType.LessEqualOrGreater),
            valueOf(TokenType.UnorderedGreaterOrEqual),
            valueOf(TokenType.UnorderedOrLess),
            valueOf(TokenType.UnorderedLessOrEqual),
            valueOf(TokenType.UnorderedOrGreater),
            valueOf(TokenType.ShiftRight),
            valueOf(TokenType.ShiftRightUnsigned),
            valueOf(TokenType.TripleDot),
            valueOf(TokenType.At),
            valueOf(TokenType.GoesTo),
            valueOf(TokenType.Hash)
            );
    }


    // Find the DElementType for the given ParseD token type.
    // Multiple calls with the same type will result in the same DElementType instance being returned.
    public static synchronized DElementType valueOf(TokenType type) {
        if (type == null) {
            return null;
        }
        if (tokens == null) {
            tokens = new HashMap<TokenType, DElementType>();
        }

        // Check the cache map for a matching token
        DElementType tokenType = tokens.get(type);

        // If not in the map yet, create one and shove it in
        if (tokenType == null) {
            tokenType = new DElementType(type);
            tokens.put(type, tokenType);
        }

        return tokenType;
    }

    // TODO: less ugliness
    public static TokenSet findSet(IElementType type) {
        TokenSet set = null;
        if (DTokenType.COMMENTS.contains(type)) {
            set = DTokenType.COMMENTS;
        }
        if (DTokenType.PARENS.contains(type)) {
            set = DTokenType.PARENS;
        }
        if (DTokenType.BRACE.contains(type)) {
            set = DTokenType.BRACE;
        }
        if (DTokenType.BRACKET.contains(type)) {
            set = DTokenType.BRACKET;
        }
        if (DTokenType.OPERATOR.contains(type)) {
            set = DTokenType.OPERATOR;
        }
        if (DTokenType.KEYWORD.contains(type)) {
            set = DTokenType.KEYWORD;
        }
        if (DTokenType.STRING_LITERALS.contains(type)) {
            set = DTokenType.STRING_LITERALS;
        }
        return set;
    }

}
