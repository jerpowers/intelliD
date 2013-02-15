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

    public static final IElementType CHAR_LITERAL = new DElementType(TokenType.LiteralChar.name());
    public static final IElementType STRING_LITERAL = new DElementType(TokenType.LiteralUtf8.name());
    public static final IElementType WSTR_LITERAL = new DElementType(TokenType.LiteralUtf16.name());
    public static final IElementType DSTR_LITERAL = new DElementType(TokenType.LiteralUtf32.name());


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
        tokens.put(TokenType.LiteralChar, CHAR_LITERAL);
        tokens.put(TokenType.LiteralUtf8, STRING_LITERAL);
        tokens.put(TokenType.LiteralUtf16, WSTR_LITERAL);
        tokens.put(TokenType.LiteralUtf32, DSTR_LITERAL);
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
