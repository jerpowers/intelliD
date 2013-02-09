package com.wyrdtech.dlang.lexer;

import java.io.IOException;

/**
 * No actual tokens, rather reads an escape sequence and returns string
 * representation.
 *
 * TODO: this
 */
public class LexEscape {

    public static String read(final LexerStream in_stream) throws IOException, LexerException
    {

        int next = in_stream.peek();
        if (next == -1) {
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of input stream when inside escape sequence");
        }


        return String.valueOf((char)in_stream.read() + (char)in_stream.read());
/*
        String surrogatePair = null;


        int number;
        char c = (char)next;
        int curPos = 1;
        escapeSequenceBuffer[0] = c;
        switch (c)
        {
            case '\'':
                ch = '\'';
                break;
            case '\"':
                ch = '\"';
                break;
            case '?':
                ch = '?';
                return "\\?"; // Literal question mark
            case '\\':
                ch = '\\';
                break;
				*/
/*case '0':
					ch = '\0';
					break;*//*

            case 'a':
                ch = '\a'; // Bell (alert)
                break;
            case 'b':
                ch = '\b'; // Backspace
                break;
            case 'f':
                ch = '\f'; // Formfeed
                break;
            case 'n':
                ch = '\n';
                break;
            case 'r':
                ch = '\r';
                break;
            case 't':
                ch = '\t';
                break;
            case 'v':
                ch = '\v'; // Vertical tab
                break;
            case 'u':
            case 'x':
                // 16 bit unicode character
                c = (char)ReaderRead();
                number = GetHexNumber(c);
                escapeSequenceBuffer[curPos++] = c;

                if (number < 0)
                {
                    OnError(Line, Col - 1, String.Format("Invalid char in literal : {0}", c));
                }
                for (int i = 0; i < 3; ++i)
                {
                    if (IsHex((char)ReaderPeek()))
                    {
                        c = (char)ReaderRead();
                        int idx = GetHexNumber(c);
                        escapeSequenceBuffer[curPos++] = c;
                        number = 16 * number + idx;
                    }
                    else
                    {
                        break;
                    }
                }
                ch = (char)number;
                break;
            case 'U':
                // 32 bit unicode character
                number = 0;
                for (int i = 0; i < 8; ++i)
                {
                    if (IsHex((char)ReaderPeek()))
                    {
                        c = (char)ReaderRead();
                        int idx = GetHexNumber(c);
                        escapeSequenceBuffer[curPos++] = c;
                        number = 16 * number + idx;
                    }
                    else
                    {
                        OnError(Line, Col - 1, String.Format("Invalid char in literal : {0}", (char)ReaderPeek()));
                        break;
                    }
                }
                if (number > 0xffff)
                {
                    ch = '\0';
                    surrogatePair = char.ConvertFromUtf32(number);
                }
                else
                {
                    ch = (char)number;
                }
                break;

            // NamedCharacterEntities
            case '&':
                string charEntity = "";

                while (true)
                {
                    nextChar = ReaderRead();

                    if (nextChar < 0)
                    {
                        OnError(Line, Col - 1, "EOF reached within named char entity");
                        ch = '\0';
                        return string.Empty;
                    }

                    c = (char)nextChar;

                    if (c == ';')
                        break;

                    if (char.IsLetter(c))
                        charEntity += c;
                    else
                    {
                        OnError(Line, Col - 1, "Unexpected character found in named char entity: " + c);
                        ch = '\0';
                        return string.Empty;
                    }
                }

                if (string.IsNullOrEmpty(charEntity))
                {
                    OnError(Line, Col - 1, "Empty named character entities not allowed");
                    ch = '\0';
                    return string.Empty;
                }

                //TODO: Performance improvement
                //var ret=System.Web.HttpUtility.HtmlDecode("&"+charEntity+";");

                ch = '#';//ret[0];

                return "&" + charEntity + ";";
            default:

                // Max 3 following octal digits
                if (IsOct(c))
                {
                    // Parse+Convert oct to dec integer
                    int oct = GetHexNumber(c);

                    for (int i = 0; i < 2; ++i)
                    {
                        if (IsOct((char)ReaderPeek()))
                        {
                            c = (char)ReaderRead();
                            escapeSequenceBuffer[curPos++] = c;

                            int idx = GetHexNumber(c);
                            oct = 8 * oct + idx;
                        }
                        else
                            break;
                    }

                    // Convert integer to character
                    if (oct > 0xffff)
                    {
                        ch = '\0';
                        surrogatePair = char.ConvertFromUtf32(oct);
                    }
                    else
                    {
                        ch = (char)oct;
                    }

                }
                else
                {
                    OnError(Line, Col, String.Format("Unexpected escape sequence : {0}", c));
                    ch = '\0';
                }
                break;
        }
        return new String(escapeSequenceBuffer, 0, curPos);
*/
    }

}
