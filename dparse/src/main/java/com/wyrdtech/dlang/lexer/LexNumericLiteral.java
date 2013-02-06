package com.wyrdtech.dlang.lexer;

import java.io.IOException;

/**
 * Encapsulates logic for lexing out a numerical literal from an input stream.
 *
 * TODO: lexer exception?
 */
public class LexNumericLiteral {

    // Purely static members for now
    private LexNumericLiteral() {}

    public static Token read(LexerStream in_stream) throws IOException, LexerException {
        int start_line = in_stream.getLine();
        int start_col = in_stream.getCol();

        int c = in_stream.read();
        if (c == -1) {
            // end of stream!
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of input stream when parsing numeric literal");
        }

        int n = in_stream.peek();
        if (n == -1) {
            // end of stream!
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Unexpected end of input stream when parsing numeric literal");
        }

        char cur = (char)c;
        if (!Character.isDigit(cur) && cur != '.')
        {
            throw new LexerException(in_stream.getLine(), in_stream.getCol(), "Digit literals can only start with a digit (0-9) or a dot ('.')!");
        }

        // Parsing decisions are made on the not-yet-consumed next value, to
        // avoid consuming character after end of literal
        char next = (char)in_stream.peek();


        // Build up value string
        StringBuilder sb = new StringBuilder();
        sb.append(cur);


        int base = 10;
        int exponent = 1;

        String prefix = "";
        String suffix = "";
        String exp_suffix = "";

/*
        boolean HasDot = false;
//            LiteralSubformat subFmt = 0;
        boolean isFloat = false;
        boolean isImaginary = false;
        boolean isUnsigned = false;
        boolean isLong = false;
*/

        // Check for hex/binary
        if (cur == '0')
        {
            if (next == 'x' || next == 'X') // Hex values
            {
                prefix = "0x";
                base = 16;

                sb.setLength(0); // Remove '0' from the string value

                cur = (char)in_stream.read(); // skip 'x'
                next = (char)in_stream.peek();
            }
            else if (next == 'b' || next == 'B') // Bin values
            {
                prefix = "0b";
                base = 2;

                sb.setLength(0); // Remove '0' from the string value

                cur = (char)in_stream.read(); // skip 'b'
                next = (char)in_stream.peek();
            }
            // else base 10
        }

        // Read literal into string builder
        while (is_legal_digit(next, base)) {
            cur = (char)in_stream.read(); // consume, same char as 'next'
            if (cur != '_') {
                sb.append(cur);
            }
            next = (char)in_stream.peek();
        }


/*
        // Read digits that occur after a comma
        Token nextToken = null; // if we accidently read a 'dot'
        boolean AllowSuffixes = true;
        if ((NumBase == 0 && ch == '.') || next == '.')
        {
            if (ch != '.') read();
            else
            {
                NumBase = 10;
                sb.setLength(0);
                sb.append('0');
            }
            next = (char)peek();
            if (!is_legal_digit(next, NumBase))
            {
                if (next == '.')
                {
                    read();
                    nextToken = new Token(TokenType.DoubleDot, col - 1, line, 2);
                }
                else if (is_identifier_part(next))
                    nextToken = new Token(TokenType.Dot, col - 1, line, 1);

                AllowSuffixes = false;
            }
            else
            {
                HasDot = true;
                sb.append('.');

                do
                {
                    if (next == '_') {
                        read();
                    } else {
                        sb.append((char)read());
                    }
                    next = (char)peek();
                }
                while (is_legal_digit(next, NumBase));
            }
        }
*/


        // Possible exponent part
        if (is_exp_marker(next, base))
        {
            StringBuilder exp = new StringBuilder();
            exp.append(next);

            cur = (char)in_stream.read();
            next = (char)in_stream.peek();

            if (next == '-' || next == '+') {
                exp.append(in_stream.read());
            }

            next = (char)in_stream.peek();
            while (is_legal_digit(next, 10)) { // TODO: other bases?
                cur = (char)in_stream.read();
                if (cur != '_') {
                    exp.append(cur);
                }
                next = (char)in_stream.peek();
            }

            exp_suffix = exp.toString();

            // knock of 'e' or 'p' and parse to int
            exponent = Integer.parseInt(exp.substring(1));

            next = (char)in_stream.peek();
        }

        // Suffixes
/*
        if (!HasDot)
        {
            unsigned:
            if (next == 'u' || next == 'U')
            {
                ReaderRead();
                suffix += "u";
                subFmt |= LiteralSubformat.Unsigned;
                isUnsigned = true;
                next = (char)ReaderPeek();
            }

            if (next == 'L')
            {
                subFmt |= LiteralSubformat.Long;
                ReaderRead();
                suffix += "L";
                isLong = true;
                next = (char)ReaderPeek();
                if (!subFmt.HasFlag(LiteralSubformat.Unsigned) && (next == 'u' || next == 'U'))
                goto unsigned;
            }
        }

        if (HasDot || AllowSuffixes)
        {
            if (next == 'f' || next == 'F')
            { // float value
                ReaderRead();
                suffix += "f";
                isFloat = true;
                subFmt |= LiteralSubformat.Float;
                next = (char)ReaderPeek();
            }
            else if (next == 'L')
            { // real value
                ReaderRead();
                isLong = true;
                suffix += 'L';
                subFmt |= LiteralSubformat.Real;
                next = (char)ReaderPeek();
            }
        }

        if (next == 'i')
        { // imaginary value
            cur = (char)in_stream.read();

            suffix += "i";

            subFmt |= LiteralSubformat.Imaginary;
            isImaginary = true;
        }
*/

/*
    // Parse the read literal value
    var num = ParseFloatValue(sb.ToString(), NumBase);

        try{
            num *= (decimal)Math.Pow(base == 16 ? 2 : 10, exponent);
        }
        catch(OverflowException ox)
        {
            num = decimal.MaxValue;
            //HACK: Don't register these exceptions. The user will notice the issues at least when compiling stuff.
            //LexerErrors.Add(new ParserError(false, "Too huge exponent", DTokens.Literal, new CodeLocation(x,y)));
        }
    }
*/

        // Create token
        Token token = new Token(TokenType.Literal, start_col, start_line, sb.length(), sb.toString());

/*
                                num,*/
/* stringValue,*//*

                          HasDot || isFloat || isImaginary ? (LiteralFormat.FloatingPoint | LiteralFormat.Scalar) : LiteralFormat.Scalar,
                          subFmt);
*/

/*
        if (token != null)
            token.next = nextToken;
*/

        return token;

    }


    private static boolean is_exp_marker(final char c, final int base) {
        if (base == 16) {
            return (c == 'p' || c == 'P');
        }
        return (c == 'e' || c == 'E');
    }

    /**
     * Tests if the given character is a valid representation in the given
     * numerical base.
     * An underscore character ('_') will always return true.
     * @param d character to check
     * @param base base to consider
     * @return true if digit is valid for given base, false otherwise
     */
    private static boolean is_legal_digit(final char d, final int base)
    {
        if (d == '_') {
            return true;
        }
        if (base == 10) {
            return Character.isDigit(d);
        }
        if (base == 2) {
            return is_bin(d);
        }
        if (base == 8) {
            return is_oct(d);
        }
        if (base == 16) {
            return is_hex(d);
        }

        return false;
    }

    private static boolean is_oct(char digit)
    {
        return Character.isDigit(digit) && digit != '9' && digit != '8';
    }

    private static boolean is_hex(char digit)
    {
        return (digit >= '0' && digit <= '9') || ('A' <= digit && digit <= 'F') || ('a' <= digit && digit <= 'f');
    }

    private static boolean is_bin(char digit)
    {
        return digit == '0' || digit == '1';
    }

}
