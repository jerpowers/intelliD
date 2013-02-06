package descent.internal.compiler.parser;


// TODO methods for Real may not work

public class NumberUtils {
	
	public static integer_t castToUns64(real_t value) {
		return value.to_integer_t().castToUns64();
	}

	public static integer_t castToInt64(real_t value) {
		return value.to_integer_t().castToInt64();
	}

	public static  integer_t castToUns32(real_t value) {
		return value.to_integer_t().castToUns32();
	}

	public static  integer_t castToInt32(real_t value) {
		return value.to_integer_t().castToInt32();
	}

	public static  integer_t castToUns16(real_t value) {
		return value.to_integer_t().castToUns16();
	}

	public static  integer_t castToInt16(real_t value) {
		return value.to_integer_t().castToInt16();
	}

	public static  integer_t castToUns8(real_t value) {
		return value.to_integer_t().castToUns8();
	}

	public static  integer_t castToInt8(real_t value) {
		return value.to_integer_t().castToInt8();
	}

}
