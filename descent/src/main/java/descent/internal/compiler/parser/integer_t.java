package descent.internal.compiler.parser;

import java.math.BigInteger;


// This is also an alias of uinteger_t, as defined by DMD
public class integer_t extends Number {

	private final static long MAX_INT = 0xFFFFFFFFL;
	private final static int MAX_SHORT = 0xFFFF;
	private final static int MAX_BYTE = 0xFF;

	public final static integer_t ZERO = new integer_t(0);
	public final static integer_t ONE = new integer_t(1);

	private static final long serialVersionUID = 1L;

	private final long longValue;
	private final BigInteger bigIntegerValue;
	
	// The type of this number, uns64 by default
	private TY ty;
	
	public integer_t(BigInteger bigIntegerValue) {
		this(bigIntegerValue, TY.Tuns64);
	}

	public integer_t(long intValue) {
		this(intValue, TY.Tuns64);
	}
	
	private integer_t(BigInteger bigIntegerValue, TY ty) {
		this.bigIntegerValue = bigIntegerValue;
		this.longValue = 0;
		this.ty = ty;
	}

	private integer_t(long intValue, TY ty) {
		this.longValue = intValue;
		this.bigIntegerValue = null;
		this.ty = ty;
	}
	
	private integer_t(integer_t other, TY ty) {
		this.longValue = other.longValue;
		this.bigIntegerValue = other.bigIntegerValue;
		this.ty = ty;
	}
	
	private integer_t copy(TY ty) {
		return new integer_t(this, ty);
	}

	// add

	public integer_t add(BigInteger value) {
		if (bigIntegerValue == null) {
			return new integer_t(toBigInteger(longValue).add(value), ty);
		} else {
			return new integer_t(bigIntegerValue.add(value), ty);
		}
	}

	public integer_t add(long value) {
		if (bigIntegerValue == null) {
			// TODO integer_t optimize
			return new integer_t(toBigInteger(longValue).add(
					toBigInteger(value)), ty);
		} else {
			return new integer_t(bigIntegerValue.add(toBigInteger(value)), ty);
		}
	}

	public integer_t add(integer_t value) {
		if (value.bigIntegerValue == null) {
			return add(value.longValue);
		} else {
			return add(value.bigIntegerValue);
		}
	}

	// and

	public integer_t and(BigInteger value) {
		if (bigIntegerValue == null) {
			return new integer_t(toBigInteger(longValue).and(value), ty);
		} else {
			return new integer_t(bigIntegerValue.and(value), ty);
		}
	}

	public integer_t and(long value) {
		if (bigIntegerValue == null) {
			return new integer_t(longValue & value, ty);
		} else {
			return new integer_t(bigIntegerValue.and(toBigInteger(value)), ty);
		}
	}

	public integer_t and(integer_t value) {
		if (value.bigIntegerValue == null) {
			return and(value.longValue);
		} else {
			return and(value.bigIntegerValue);
		}
	}

	// compareTo

	public int compareTo(BigInteger value) {
		if (bigIntegerValue == null) {
			return toBigInteger(longValue).compareTo(value);
		} else {
			return bigIntegerValue.compareTo(value);
		}
	}

	public int compareTo(long value) {
		if (bigIntegerValue == null) {
			return longValue > value ? 1 : (longValue < value ? -1 : 0);
		} else {
			return bigIntegerValue.compareTo(toBigInteger(value));
		}
	}

	public int compareTo(integer_t value) {
		if (value.bigIntegerValue == null) {
			return compareTo(value.longValue);
		} else {
			return compareTo(value.bigIntegerValue);
		}
	}

	// complement

	public integer_t complement() {
		if (bigIntegerValue == null) {
			long value = ~longValue;
			switch(ty) {
			case Tuns8:
				return new integer_t(value < 0 ? value + (1 << 8) : value, ty);
			case Tuns16:
				return new integer_t(value < 0 ? value + (1 << 16) : value, ty);
			case Tuns32:
				return new integer_t(value < 0 ? value + (1 << 32) : value, ty);
			case Tuns64:
				return new integer_t(value < 0 ? value + (1 << 64) : value, ty);
			default:
				return new integer_t(value, ty);
			}
		} else {
			return new integer_t(bigIntegerValue.not(), ty);
		}
	}

	// divide

	public integer_t divide(BigInteger value) {
		if (bigIntegerValue == null) {
			return new integer_t(toBigInteger(longValue).divide(value), ty);
		} else {
			return new integer_t(bigIntegerValue.divide(value), ty);
		}
	}

	public integer_t divide(long value) {
		if (bigIntegerValue == null) {
			return new integer_t(longValue / value, ty);
		} else {
			return new integer_t(bigIntegerValue.divide(toBigInteger(value)), ty);
		}
	}

	public integer_t divide(integer_t value) {
		if (value.bigIntegerValue == null) {
			return divide(value.longValue);
		} else {
			return divide(value.bigIntegerValue);
		}
	}

	// equals

	public boolean equals(BigInteger value) {
		if (bigIntegerValue == null) {
			return toBigInteger(longValue).equals(value);
		} else {
			return bigIntegerValue.equals(value);
		}
	}

	public boolean equals(long value) {
		if (bigIntegerValue == null) {
			return longValue == value;
		} else {
			return bigIntegerValue.equals(toBigInteger(value));
		}
	}

	public boolean equals(integer_t value) {
		if (value.bigIntegerValue == null) {
			return equals(value.longValue);
		} else {
			return equals(value.bigIntegerValue);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Integer) {
			return equals(((Integer) other).longValue());
		} else if (other instanceof Long) {
			return equals(((Long) other).longValue());
		} else if (other instanceof BigInteger) {
			return equals((BigInteger) other);
		} else if (other instanceof integer_t) {
			return equals((integer_t) other);
		} else {
			return false;
		}
	}

	// mod

	public integer_t mod(BigInteger value) {
		if (bigIntegerValue == null) {
			return new integer_t(toBigInteger(longValue).mod(value), ty);
		} else {
			return new integer_t(bigIntegerValue.mod(value), ty);
		}
	}

	public integer_t mod(long value) {
		if (bigIntegerValue == null) {
			return new integer_t(longValue % value, ty);
		} else {
			return new integer_t(bigIntegerValue.mod(toBigInteger(value)), ty);
		}
	}

	public integer_t mod(integer_t value) {
		if (value.bigIntegerValue == null) {
			return mod(value.longValue);
		} else {
			return mod(value.bigIntegerValue);
		}
	}

	// multiply

	public integer_t multiply(BigInteger value) {
		if (bigIntegerValue == null) {
			return new integer_t(toBigInteger(longValue).multiply(value), ty);
		} else {
			return new integer_t(bigIntegerValue.multiply(value), ty);
		}
	}

	public integer_t multiply(long value) {
		if (bigIntegerValue == null) {
			// TODO integer_t optimize
			return new integer_t(toBigInteger(longValue).multiply(
					toBigInteger(value)), ty);
		} else {
			return new integer_t(bigIntegerValue.multiply(toBigInteger(value)), ty);
		}
	}

	public integer_t multiply(integer_t value) {
		if (value.bigIntegerValue == null) {
			return multiply(value.longValue);
		} else {
			return multiply(value.bigIntegerValue);
		}
	}

	// negate

	public integer_t negate() {
		if (bigIntegerValue == null) {
			return new integer_t(-longValue, ty);
		} else {
			return new integer_t(bigIntegerValue.negate(), ty);
		}
	}

	// or

	public integer_t or(BigInteger value) {
		if (bigIntegerValue == null) {
			return new integer_t(toBigInteger(longValue).or(value), ty);
		} else {
			return new integer_t(bigIntegerValue.or(value), ty);
		}
	}

	public integer_t or(long value) {
		if (bigIntegerValue == null) {
			return new integer_t(longValue | value);
		} else {
			return new integer_t(bigIntegerValue.or(toBigInteger(value)), ty);
		}
	}

	public integer_t or(integer_t value) {
		if (value.bigIntegerValue == null) {
			return or(value.longValue);
		} else {
			return or(value.bigIntegerValue);
		}
	}

	// shiftLeft

	public integer_t shiftLeft(BigInteger value) {
		if (bigIntegerValue == null) {
			// TODO integer_t this may not work if value is bigger than int
			return new integer_t(toBigInteger(longValue).shiftLeft(
					value.intValue()), ty);
		} else {
			return new integer_t(bigIntegerValue.shiftLeft(value.intValue()), ty);
		}
	}

	public integer_t shiftLeft(long value) {
		if (bigIntegerValue == null) {
			return new integer_t(longValue << value, ty);
		} else {
			// TODO integer_t this cast may be wrong
			return new integer_t(bigIntegerValue.shiftLeft((int) value), ty);
		}
	}

	public integer_t shiftLeft(integer_t value) {
		if (value.bigIntegerValue == null) {
			return shiftLeft(value.longValue);
		} else {
			return shiftLeft(value.bigIntegerValue);
		}
	}

	// shiftRight

	public integer_t shiftRight(BigInteger value) {
		if (bigIntegerValue == null) {
			// TODO integer_t this may not work if value is bigger than int
			return new integer_t(toBigInteger(longValue).shiftRight(
					value.intValue()), ty);
		} else {
			return new integer_t(bigIntegerValue.shiftRight(value.intValue()), ty);
		}
	}

	public integer_t shiftRight(long value) {
		if (bigIntegerValue == null) {
			// TODO integer_t optimize
			return new integer_t(longValue >> value, ty);
		} else {
			// TODO integer_t this cast may be wrong
			return new integer_t(bigIntegerValue.shiftRight((int) value), ty);
		}
	}

	public integer_t shiftRight(integer_t value) {
		if (value.bigIntegerValue == null) {
			return shiftRight(value.longValue);
		} else {
			return shiftRight(value.bigIntegerValue);
		}
	}

	// subtract

	public integer_t subtract(BigInteger value) {
		if (bigIntegerValue == null) {
			return new integer_t(toBigInteger(longValue).subtract(value), ty);
		} else {
			return new integer_t(bigIntegerValue.subtract(value), ty);
		}
	}

	public integer_t subtract(long value) {
		if (bigIntegerValue == null) {
			// TODO integer_t optimize
			return new integer_t(toBigInteger(longValue).subtract(
					toBigInteger(value)), ty);
		} else {
			return new integer_t(bigIntegerValue.subtract(toBigInteger(value)), ty);
		}
	}

	public integer_t subtract(integer_t value) {
		if (value.bigIntegerValue == null) {
			return subtract(value.longValue);
		} else {
			return subtract(value.bigIntegerValue);
		}
	}

	// unsignedShiftRight

	public integer_t unsignedShiftRight(BigInteger value) {
		if (bigIntegerValue == null) {
			return new integer_t(longValue >>> value.intValue(), ty);
		} else {
			return new integer_t(bigIntegerValue.intValue() >>> value
					.intValue(), ty);
		}
	}

	public integer_t unsignedShiftRight(long value) {
		if (bigIntegerValue == null) {
			return new integer_t(longValue >>> value, ty);
		} else {
			// TODO integer_t implement
			return new integer_t(intValue() >>> value, ty);
		}
	}

	public integer_t unsignedShiftRight(integer_t value) {
		if (value.bigIntegerValue == null) {
			return unsignedShiftRight(value.longValue);
		} else {
			return unsignedShiftRight(value.bigIntegerValue);
		}
	}

	// xor

	public integer_t xor(BigInteger value) {
		if (bigIntegerValue == null) {
			return new integer_t(toBigInteger(longValue).xor(value), ty);
		} else {
			return new integer_t(bigIntegerValue.xor(value), ty);
		}
	}

	public integer_t xor(long value) {
		if (bigIntegerValue == null) {
			return new integer_t(longValue ^ value, ty);
		} else {
			return new integer_t(bigIntegerValue.xor(toBigInteger(value)), ty);
		}
	}

	public integer_t xor(integer_t value) {
		if (value.bigIntegerValue == null) {
			return xor(value.longValue);
		} else {
			return xor(value.bigIntegerValue);
		}
	}

	// casts

	public integer_t castToUns64() {
		if (ty == TY.Tuns64) {
			return this;
		} else {
			return copy(TY.Tuns64);
		}
	}

	public integer_t castToInt64() {
		if (bigIntegerValue == null) {
			if (ty == TY.Tint64) {
				return this;
			} else {
				return copy(TY.Tint64);
			}
		} else {
			return new integer_t(this.bigIntegerValue(), TY.Tint64);
		}
	}

	public integer_t castToUns32() {
		long b = this.longValue();
		if (b < 0) {
			b %= MAX_INT + 1;
			if (b < 0) {
				b += MAX_INT;
			}
		}
		if (b > MAX_INT) {
			b %= MAX_INT + 1;
		}
		return new integer_t(toBigInteger(b), TY.Tuns32);
	}

	public integer_t castToInt32() {
		return new integer_t(this.intValue(), TY.Tint32);
	}

	public integer_t castToUns16() {
		int b = this.intValue();
		if (b < 0) {
			b %= MAX_SHORT + 1;
			if (b < 0) {
				b += MAX_SHORT;
			}
		}
		if (b > MAX_SHORT) {
			b %= MAX_SHORT + 1;
		}
		return new integer_t(b, TY.Tuns16);
	}

	public integer_t castToInt16() {
		return new integer_t(this.shortValue(), TY.Tint16);
	}

	public integer_t castToUns8() {
		short b = this.shortValue();
		if (b < 0) {
			b %= MAX_BYTE + 1;
			if (b < 0) {
				b += MAX_BYTE;
			}
		}
		if (b > MAX_BYTE) {
			b %= MAX_BYTE + 1;
		}
		return new integer_t(b, TY.Tuns8);
	}

	public integer_t castToInt8() {
		return new integer_t(this.byteValue(), TY.Tint8);
	}

	// isTrue

	public boolean isTrue() {
		if (bigIntegerValue == null) {
			return longValue != 0;
		} else {
			return bigIntegerValue.compareTo(BigInteger.ZERO) != 0;
		}
	}

	// xxxValue

	public BigInteger bigIntegerValue() {
		if (bigIntegerValue != null) {
			return bigIntegerValue;
		} else {
			return BigInteger.valueOf(longValue);
		}
	}

	@Override
	public double doubleValue() {
		if (bigIntegerValue != null) {
			return bigIntegerValue.doubleValue();
		} else {
			return longValue;
		}
	}

	@Override
	public float floatValue() {
		if (bigIntegerValue != null) {
			return bigIntegerValue.floatValue();
		} else {
			return longValue;
		}
	}

	@Override
	public int intValue() {
		if (bigIntegerValue != null) {
			return bigIntegerValue.intValue();
		} else {
			return (int) longValue;
		}
	}

	@Override
	public long longValue() {
		if (bigIntegerValue != null) {
			return bigIntegerValue.longValue();
		} else {
			return longValue;
		}
	}

	@Override
	public String toString() {
		if (bigIntegerValue != null) {
			return bigIntegerValue.toString();
		} else {
			return String.valueOf(longValue);
		}
	}

	public integer_t castToSinteger_t() {
		// TODO implement
		return this;
	}

	private static BigInteger toBigInteger(long value) {
		return BigInteger.valueOf(value);
	}

	public boolean isExactly(float f) {
		if (bigIntegerValue != null) {
			return bigIntegerValue.longValue() == f;
		} else {
			return longValue == f;
		}
	}

	public boolean isExactly(double d) {
		if (bigIntegerValue != null) {
			return bigIntegerValue.longValue() == d;
		} else {
			return longValue == d;
		}
	}

}
