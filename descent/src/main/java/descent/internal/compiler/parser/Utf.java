package descent.internal.compiler.parser;

import descent.core.compiler.IProblem;


public class Utf {

	/**
	 * @param input a character array
	 * @param s the index in the character array
	 * @param len the length to decode
	 */
	public static int decodeChar(char[] input, int s, int len, int[] pidx,
			int[] presult) {
		int V;
		int i = pidx[0];
		char u = input[s + i];

		assert (i >= 0 && i < len);

		if ((u & 0x80) != 0) {
			int n;
			char u2;

			/* The following encodings are valid, except for the 5 and 6 byte
			 * combinations:
			 *	0xxxxxxx
			 *	110xxxxx 10xxxxxx
			 *	1110xxxx 10xxxxxx 10xxxxxx
			 *	11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
			 *	111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
			 *	1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
			 */
			for (n = 1;; n++) {
				if (n > 4)
					return decodeChar_Lerr(input, s, i, pidx, presult); // only do the first 4 of 6 encodings
				if (((u << n) & 0x80) == 0) {
					if (n == 1)
						return decodeChar_Lerr(input, s, i, pidx, presult);
					break;
				}
			}

			// Pick off (7 - n) significant bits of B from first byte of octet
			V = (u & ((1 << (7 - n)) - 1));

			if (i + (n - 1) >= len)
				return decodeChar_Lerr(input, s, i, pidx, presult); // off end of string

			/* The following combinations are overlong, and illegal:
			 *	1100000x (10xxxxxx)
			 *	11100000 100xxxxx (10xxxxxx)
			 *	11110000 1000xxxx (10xxxxxx 10xxxxxx)
			 *	11111000 10000xxx (10xxxxxx 10xxxxxx 10xxxxxx)
			 *	11111100 100000xx (10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx)
			 */
			u2 = input[s + i + 1];
			if ((u & 0xFE) == 0xC0 || (u == 0xE0 && (u2 & 0xE0) == 0x80)
					|| (u == 0xF0 && (u2 & 0xF0) == 0x80)
					|| (u == 0xF8 && (u2 & 0xF8) == 0x80)
					|| (u == 0xFC && (u2 & 0xFC) == 0x80))
				return decodeChar_Lerr(input, s, i, pidx, presult); // overlong combination

			for (int j = 1; j != n; j++) {
				u = input[s + i + j];
				if ((u & 0xC0) != 0x80)
					return decodeChar_Lerr(input, s, i, pidx, presult); // trailing bytes are 10xxxxxx
				V = (V << 6) | (u & 0x3F);
			}
			if (!isValidDchar(V))
				return decodeChar_Lerr(input, s, i, pidx, presult);
			i += n;
		} else {
			V = u;
			i++;
		}

		pidx[0] = i;
		presult[0] = V;
		return -1;
	}

	public static boolean isValidDchar(long c) {
		return c < 0xD800
				|| (c > 0xDFFF && c <= 0x10FFFF && c != 0xFFFE && c != 0xFFFF);
	}

	private static int decodeChar_Lerr(char[] input, int s, int i,
			int[] pidx, int[] presult) {
		presult[0] = input[s + i];
		pidx[0] = i + 1;
		return IProblem.InvalidUtf8Sequence2;
	}

	public static int decodeWchar(char[] input, int s, int len, int[] pidx,
			int[] presult) {
		int msg;
		int i = pidx[0];
		int u = input[s + i];

		assert (i >= 0 && i < len);
		if ((u & ~0x7F) != 0) {
			if (u >= 0xD800 && u <= 0xDBFF) {
				int u2;

				if (i + 1 == len) {
					msg = IProblem.Utf16HighValuePastEndOfString; //"surrogate UTF-16 high value past end of string";
					// goto Lerr;
					return decodeWchar_Lerr(input, s, i, pidx, presult, msg);
				}
				u2 = input[s + i + 1];
				if (u2 < 0xDC00 || u2 > 0xDFFF) {
					msg = IProblem.Utf16LowValueOutOfRange; //"surrogate UTF-16 low value out of range";
					// goto Lerr;
					return decodeWchar_Lerr(input, s, i, pidx, presult, msg);
				}
				u = ((u - 0xD7C0) << 10) + (u2 - 0xDC00);
				i += 2;
			} else if (u >= 0xDC00 && u <= 0xDFFF) {
				msg = IProblem.UnpairedUtf16Value; //"unpaired surrogate UTF-16 value";
				// goto Lerr;
				return decodeWchar_Lerr(input, s, i, pidx, presult, msg);
			} else if (u == 0xFFFE || u == 0xFFFF) {
				msg = IProblem.IllegalUtf16Value; //"illegal UTF-16 value";
				// goto Lerr;
				return decodeWchar_Lerr(input, s, i, pidx, presult, msg);
			} else
				i++;
		} else {
			i++;
		}

		assert (Utf.isValidDchar(u));
		pidx[0] = i;
		presult[0] = u;
		return -1;
	}

	private static int decodeWchar_Lerr(char[] input, int s, int i,
			int[] pidx, int[] presult, int msg) {
		presult[0] = input[s + i];
		pidx[0] = i + 1;
		return msg;
	}

}
