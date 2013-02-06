package descent.internal.compiler.parser;



public class complex_t {

	public final static complex_t ZERO = new complex_t(real_t.ZERO, real_t.ZERO);

	public real_t re;
	public real_t im;

	public complex_t() {
		this.re = real_t.ZERO;
		this.im = real_t.ZERO;
	}

	public complex_t(real_t re) {
		this.re = re;
		this.im = real_t.ZERO;
	}
	
	public complex_t(real_t re, real_t im) {
		this.re = re;
		this.im = im;
	}

	public complex_t add(complex_t y) {
		return new complex_t(re.add(y.re), im.add(y.im));
	}

	public complex_t subtract(complex_t y) {
		return new complex_t(re.subtract(y.re), im.subtract(y.im));
	}

	public complex_t negate() {
		return new complex_t(re.negate(), im.negate());
	}

	public complex_t multiply(complex_t y) {
		return new complex_t(re.multiply(y.re).subtract(im.multiply(y.im)), im.multiply(y.re).add(re.multiply(y.im)));
	}

	public complex_t divide(complex_t y) {
		real_t abs_y_re = y.re.compareTo(real_t.ZERO) < 0 ? y.re.negate() : y.re;
		real_t abs_y_im = y.im.compareTo(real_t.ZERO) < 0 ? y.im.negate() : y.im;
		real_t r, den;

		if (abs_y_re.compareTo(abs_y_im) < 0) {
			r = y.re.divide(y.im);
			den = y.im.add(r.multiply(y.re));
			return new complex_t(((r.multiply(r)).add(im)).divide(den), ((im.multiply(r)).subtract(r)).divide(den));
		} else {
			r = y.im.divide(y.re);
			den = y.re.add(r.multiply(y.im));
			return new complex_t((r.add(r.multiply(im))).divide(den), (im.subtract(r.multiply(r))).divide(den));
		}
	}

	public boolean isTrue() {
		return re.compareTo(real_t.ZERO) != 0 || im.compareTo(real_t.ZERO) != 0;
	}

	public boolean equals(complex_t y) {
		return re.equals(y.re) && im.equals(y.im);
	}

	public static complex_t multiply(real_t x, complex_t y) {
		return new complex_t(x).multiply(y);
	}

	public static complex_t multiply(complex_t x, real_t y) {
		return x.multiply(new complex_t(y));
	}

	public static complex_t divide(complex_t x, real_t y) {
		return x.divide(new complex_t(y));
	}

	public static real_t creall(complex_t x) {
		return x.re;
	}
	
	public static real_t cimagl(complex_t x) {
		return x.im;
	}
	
	@Override
	public String toString() {
		return re.toString() + im.toString() + "i";
	}
	
}
