package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHconst;
import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.STC.STCconst;
import static descent.internal.compiler.parser.STC.STCfield;
import static descent.internal.compiler.parser.STC.STCimmutable;
import static descent.internal.compiler.parser.STC.STCin;
import static descent.internal.compiler.parser.STC.STCscope;
import static descent.internal.compiler.parser.STC.STCshared;
import static descent.internal.compiler.parser.TOK.TOKdotvar;
import static descent.internal.compiler.parser.TOK.TOKvar;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tbit;
import static descent.internal.compiler.parser.TY.Tbool;
import static descent.internal.compiler.parser.TY.Tchar;
import static descent.internal.compiler.parser.TY.Tclass;
import static descent.internal.compiler.parser.TY.Tcomplex32;
import static descent.internal.compiler.parser.TY.Tcomplex64;
import static descent.internal.compiler.parser.TY.Tcomplex80;
import static descent.internal.compiler.parser.TY.Tdchar;
import static descent.internal.compiler.parser.TY.Tdelegate;
import static descent.internal.compiler.parser.TY.Terror;
import static descent.internal.compiler.parser.TY.Tfloat32;
import static descent.internal.compiler.parser.TY.Tfloat64;
import static descent.internal.compiler.parser.TY.Tfloat80;
import static descent.internal.compiler.parser.TY.Tfunction;
import static descent.internal.compiler.parser.TY.Tident;
import static descent.internal.compiler.parser.TY.Timaginary32;
import static descent.internal.compiler.parser.TY.Timaginary64;
import static descent.internal.compiler.parser.TY.Timaginary80;
import static descent.internal.compiler.parser.TY.Tint16;
import static descent.internal.compiler.parser.TY.Tint32;
import static descent.internal.compiler.parser.TY.Tint64;
import static descent.internal.compiler.parser.TY.Tint8;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tuns16;
import static descent.internal.compiler.parser.TY.Tuns32;
import static descent.internal.compiler.parser.TY.Tuns64;
import static descent.internal.compiler.parser.TY.Tuns8;
import static descent.internal.compiler.parser.TY.Tvoid;
import static descent.internal.compiler.parser.TY.Twchar;

import java.nio.channels.IllegalSelectorException;
import java.util.ArrayList;
import java.util.List;


import descent.core.IJavaElement__Marker;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import melnorme.utilbox.core.Assert;


public abstract class Type extends ASTDmdNode implements Cloneable {

	public final static int PTRSIZE = 4;
	public final static int REALSIZE = 16;
	public final static int MODconst = 1; // type is const
	public final static int MODinvariant = 4; // type is invariant
	public final static int MODshared = 2; // type is shared

	public static class Modification {
		public int startPosition;
		public int length;
		public TOK tok;

		public Modification(TOK tok, int startPosition, int length) {
			this.startPosition = startPosition;
			this.length = length;
			this.tok = tok;
		}
	}

	public final static TypeBasic tvoid = new TypeBasic(TY.Tvoid);
	public final static TypeBasic tint8 = new TypeBasic(TY.Tint8);
	public final static TypeBasic tuns8 = new TypeBasic(TY.Tuns8);
	public final static TypeBasic tint16 = new TypeBasic(TY.Tint16);
	public final static TypeBasic tuns16 = new TypeBasic(TY.Tuns16);
	public final static TypeBasic tint32 = new TypeBasic(TY.Tint32);
	public final static TypeBasic tuns32 = new TypeBasic(TY.Tuns32);
	public final static TypeBasic tint64 = new TypeBasic(TY.Tint64);
	public final static TypeBasic tuns64 = new TypeBasic(TY.Tuns64);
	public final static TypeBasic tfloat32 = new TypeBasic(TY.Tfloat32);
	public final static TypeBasic tfloat64 = new TypeBasic(TY.Tfloat64);
	public final static TypeBasic tfloat80 = new TypeBasic(TY.Tfloat80);
	public final static TypeBasic timaginary32 = new TypeBasic(TY.Timaginary32);
	public final static TypeBasic timaginary64 = new TypeBasic(TY.Timaginary64);
	public final static TypeBasic timaginary80 = new TypeBasic(TY.Timaginary80);
	public final static TypeBasic tcomplex32 = new TypeBasic(TY.Tcomplex32);
	public final static TypeBasic tcomplex64 = new TypeBasic(TY.Tcomplex64);
	public final static TypeBasic tcomplex80 = new TypeBasic(TY.Tcomplex80);
	public final static TypeBasic tbit = new TypeBasic(TY.Tbit);
	public final static TypeBasic tbool = new TypeBasic(TY.Tbool);
	public final static TypeBasic tchar = new TypeBasic(TY.Tchar);
	public final static TypeBasic twchar = new TypeBasic(TY.Twchar);
	public final static TypeBasic tdchar = new TypeBasic(TY.Tdchar);
	public final static TypeBasic terror = new TypeBasic(TY.Tint32); // for error recovery
	public final static TypeBasic tindex = tint32;
	public final static TypeBasic tboolean = tbool;
	public final static TypeBasic tsize_t = tuns32;
	public final static TypeBasic tptrdiff_t = tint32;
	public final static TypeBasic tshiftcnt = tint32;

	public static final boolean impcnvWarn[][];
	public static final TY impcnvResult[][];
	public static final TY impcnvType1[][];
	public static final TY impcnvType2[][];
	public static final Type basic[];

	static {
		int TMAX = TY.values().length;

		basic = new Type[TMAX];
		basic[Tvoid.ordinal()] = tvoid; // new TypeBasic(Tvoid);
		basic[Tint8.ordinal()] = tint8; // new TypeBasic(Tint8);
		basic[Tuns8.ordinal()] = tuns8; //new TypeBasic(Tuns8);
		basic[Tint16.ordinal()] = tint16; //new TypeBasic(Tint16);
		basic[Tuns16.ordinal()] = tuns16; //new TypeBasic(Tuns16);
		basic[Tint32.ordinal()] = tint32; //new TypeBasic(Tint32);
		basic[Tuns32.ordinal()] = tuns32; //new TypeBasic(Tuns32);
		basic[Tint64.ordinal()] = tint64; //new TypeBasic(Tint64);
		basic[Tuns64.ordinal()] = tuns64; //new TypeBasic(Tuns64);
		basic[Tfloat32.ordinal()] = tfloat32; //new TypeBasic(Tfloat32);
		basic[Tfloat64.ordinal()] = tfloat64; //new TypeBasic(Tfloat64);
		basic[Tfloat80.ordinal()] = tfloat80; //new TypeBasic(Tfloat80);
		basic[Timaginary32.ordinal()] = timaginary32; //new TypeBasic(Timaginary32);
		basic[Timaginary64.ordinal()] = timaginary64; //new TypeBasic(Timaginary64);
		basic[Timaginary80.ordinal()] = timaginary80; //new TypeBasic(Timaginary80);
		basic[Tcomplex32.ordinal()] = tcomplex32; //new TypeBasic(Tcomplex32);
		basic[Tcomplex64.ordinal()] = tcomplex64; //new TypeBasic(Tcomplex64);
		basic[Tcomplex80.ordinal()] = tcomplex80; //new TypeBasic(Tcomplex80);
		basic[Tbit.ordinal()] = tbit; //new TypeBasic(Tbit);
		basic[Tbool.ordinal()] = tbool; //new TypeBasic(Tbool);
		basic[Tchar.ordinal()] = tchar; //new TypeBasic(Tchar);
		basic[Twchar.ordinal()] = twchar; //new TypeBasic(Twchar);
		basic[Tdchar.ordinal()] = tdchar; //new TypeBasic(Tdchar);

		impcnvResult = new TY[TMAX][];
		impcnvType1 = new TY[TMAX][];
		impcnvType2 = new TY[TMAX][];
		impcnvWarn = new boolean[TMAX][];

		for (int i = 0; i < TMAX; i++) {
			impcnvResult[i] = new TY[TMAX];
			impcnvType1[i] = new TY[TMAX];
			impcnvType2[i] = new TY[TMAX];
			impcnvWarn[i] = new boolean[TMAX];
			for (int j = 0; j < TMAX; j++) {
				impcnvResult[i][j] = Terror;
				impcnvType1[i][j] = Terror;
				impcnvType2[i][j] = Terror;
				impcnvWarn[i][j] = false;
			}
		}

		X(Tbit, Tbit, Tint32, Tint32, Tint32);
		X(Tbit, Tint8, Tint32, Tint32, Tint32);
		X(Tbit, Tuns8, Tint32, Tint32, Tint32);
		X(Tbit, Tint16, Tint32, Tint32, Tint32);
		X(Tbit, Tuns16, Tint32, Tint32, Tint32);
		X(Tbit, Tint32, Tint32, Tint32, Tint32);
		X(Tbit, Tuns32, Tuns32, Tuns32, Tuns32);
		X(Tbit, Tint64, Tint64, Tint64, Tint64);
		X(Tbit, Tuns64, Tuns64, Tuns64, Tuns64);

		X(Tbit, Tfloat32, Tfloat32, Tfloat32, Tfloat32);
		X(Tbit, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tbit, Tfloat80, Tfloat80, Tfloat80, Tfloat80);
		X(Tbit, Timaginary32, Tfloat32, Timaginary32, Tfloat32);
		X(Tbit, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tbit, Timaginary80, Tfloat80, Timaginary80, Tfloat80);
		X(Tbit, Tcomplex32, Tfloat32, Tcomplex32, Tcomplex32);
		X(Tbit, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tbit, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tbool, Tbool, Tbool, Tbool, Tbool);
		X(Tbool, Tint8, Tint32, Tint32, Tint32);
		X(Tbool, Tuns8, Tint32, Tint32, Tint32);
		X(Tbool, Tint16, Tint32, Tint32, Tint32);
		X(Tbool, Tuns16, Tint32, Tint32, Tint32);
		X(Tbool, Tint32, Tint32, Tint32, Tint32);
		X(Tbool, Tuns32, Tuns32, Tuns32, Tuns32);
		X(Tbool, Tint64, Tint64, Tint64, Tint64);
		X(Tbool, Tuns64, Tuns64, Tuns64, Tuns64);

		X(Tbool, Tfloat32, Tfloat32, Tfloat32, Tfloat32);
		X(Tbool, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tbool, Tfloat80, Tfloat80, Tfloat80, Tfloat80);
		X(Tbool, Timaginary32, Tfloat32, Timaginary32, Tfloat32);
		X(Tbool, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tbool, Timaginary80, Tfloat80, Timaginary80, Tfloat80);
		X(Tbool, Tcomplex32, Tfloat32, Tcomplex32, Tcomplex32);
		X(Tbool, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tbool, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tint8, Tint8, Tint32, Tint32, Tint32);
		X(Tint8, Tuns8, Tint32, Tint32, Tint32);
		X(Tint8, Tint16, Tint32, Tint32, Tint32);
		X(Tint8, Tuns16, Tint32, Tint32, Tint32);
		X(Tint8, Tint32, Tint32, Tint32, Tint32);
		X(Tint8, Tuns32, Tuns32, Tuns32, Tuns32);
		X(Tint8, Tint64, Tint64, Tint64, Tint64);
		X(Tint8, Tuns64, Tuns64, Tuns64, Tuns64);

		X(Tint8, Tfloat32, Tfloat32, Tfloat32, Tfloat32);
		X(Tint8, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tint8, Tfloat80, Tfloat80, Tfloat80, Tfloat80);
		X(Tint8, Timaginary32, Tfloat32, Timaginary32, Tfloat32);
		X(Tint8, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tint8, Timaginary80, Tfloat80, Timaginary80, Tfloat80);
		X(Tint8, Tcomplex32, Tfloat32, Tcomplex32, Tcomplex32);
		X(Tint8, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tint8, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tuns8, Tuns8, Tint32, Tint32, Tint32);
		X(Tuns8, Tint16, Tint32, Tint32, Tint32);
		X(Tuns8, Tuns16, Tint32, Tint32, Tint32);
		X(Tuns8, Tint32, Tint32, Tint32, Tint32);
		X(Tuns8, Tuns32, Tuns32, Tuns32, Tuns32);
		X(Tuns8, Tint64, Tint64, Tint64, Tint64);
		X(Tuns8, Tuns64, Tuns64, Tuns64, Tuns64);

		X(Tuns8, Tfloat32, Tfloat32, Tfloat32, Tfloat32);
		X(Tuns8, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tuns8, Tfloat80, Tfloat80, Tfloat80, Tfloat80);
		X(Tuns8, Timaginary32, Tfloat32, Timaginary32, Tfloat32);
		X(Tuns8, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tuns8, Timaginary80, Tfloat80, Timaginary80, Tfloat80);
		X(Tuns8, Tcomplex32, Tfloat32, Tcomplex32, Tcomplex32);
		X(Tuns8, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tuns8, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tint16, Tint16, Tint32, Tint32, Tint32);
		X(Tint16, Tuns16, Tint32, Tint32, Tint32);
		X(Tint16, Tint32, Tint32, Tint32, Tint32);
		X(Tint16, Tuns32, Tuns32, Tuns32, Tuns32);
		X(Tint16, Tint64, Tint64, Tint64, Tint64);
		X(Tint16, Tuns64, Tuns64, Tuns64, Tuns64);

		X(Tint16, Tfloat32, Tfloat32, Tfloat32, Tfloat32);
		X(Tint16, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tint16, Tfloat80, Tfloat80, Tfloat80, Tfloat80);
		X(Tint16, Timaginary32, Tfloat32, Timaginary32, Tfloat32);
		X(Tint16, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tint16, Timaginary80, Tfloat80, Timaginary80, Tfloat80);
		X(Tint16, Tcomplex32, Tfloat32, Tcomplex32, Tcomplex32);
		X(Tint16, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tint16, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tuns16, Tuns16, Tint32, Tint32, Tint32);
		X(Tuns16, Tint32, Tint32, Tint32, Tint32);
		X(Tuns16, Tuns32, Tuns32, Tuns32, Tuns32);
		X(Tuns16, Tint64, Tint64, Tint64, Tint64);
		X(Tuns16, Tuns64, Tuns64, Tuns64, Tuns64);

		X(Tuns16, Tfloat32, Tfloat32, Tfloat32, Tfloat32);
		X(Tuns16, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tuns16, Tfloat80, Tfloat80, Tfloat80, Tfloat80);
		X(Tuns16, Timaginary32, Tfloat32, Timaginary32, Tfloat32);
		X(Tuns16, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tuns16, Timaginary80, Tfloat80, Timaginary80, Tfloat80);
		X(Tuns16, Tcomplex32, Tfloat32, Tcomplex32, Tcomplex32);
		X(Tuns16, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tuns16, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tint32, Tint32, Tint32, Tint32, Tint32);
		X(Tint32, Tuns32, Tuns32, Tuns32, Tuns32);
		X(Tint32, Tint64, Tint64, Tint64, Tint64);
		X(Tint32, Tuns64, Tuns64, Tuns64, Tuns64);

		X(Tint32, Tfloat32, Tfloat32, Tfloat32, Tfloat32);
		X(Tint32, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tint32, Tfloat80, Tfloat80, Tfloat80, Tfloat80);
		X(Tint32, Timaginary32, Tfloat32, Timaginary32, Tfloat32);
		X(Tint32, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tint32, Timaginary80, Tfloat80, Timaginary80, Tfloat80);
		X(Tint32, Tcomplex32, Tfloat32, Tcomplex32, Tcomplex32);
		X(Tint32, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tint32, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tuns32, Tuns32, Tuns32, Tuns32, Tuns32);
		X(Tuns32, Tint64, Tint64, Tint64, Tint64);
		X(Tuns32, Tuns64, Tuns64, Tuns64, Tuns64);

		X(Tuns32, Tfloat32, Tfloat32, Tfloat32, Tfloat32);
		X(Tuns32, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tuns32, Tfloat80, Tfloat80, Tfloat80, Tfloat80);
		X(Tuns32, Timaginary32, Tfloat32, Timaginary32, Tfloat32);
		X(Tuns32, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tuns32, Timaginary80, Tfloat80, Timaginary80, Tfloat80);
		X(Tuns32, Tcomplex32, Tfloat32, Tcomplex32, Tcomplex32);
		X(Tuns32, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tuns32, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tint64, Tint64, Tint64, Tint64, Tint64);
		X(Tint64, Tuns64, Tuns64, Tuns64, Tuns64);

		X(Tint64, Tfloat32, Tfloat32, Tfloat32, Tfloat32);
		X(Tint64, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tint64, Tfloat80, Tfloat80, Tfloat80, Tfloat80);
		X(Tint64, Timaginary32, Tfloat32, Timaginary32, Tfloat32);
		X(Tint64, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tint64, Timaginary80, Tfloat80, Timaginary80, Tfloat80);
		X(Tint64, Tcomplex32, Tfloat32, Tcomplex32, Tcomplex32);
		X(Tint64, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tint64, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tuns64, Tuns64, Tuns64, Tuns64, Tuns64);

		X(Tuns64, Tfloat32, Tfloat32, Tfloat32, Tfloat32);
		X(Tuns64, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tuns64, Tfloat80, Tfloat80, Tfloat80, Tfloat80);
		X(Tuns64, Timaginary32, Tfloat32, Timaginary32, Tfloat32);
		X(Tuns64, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tuns64, Timaginary80, Tfloat80, Timaginary80, Tfloat80);
		X(Tuns64, Tcomplex32, Tfloat32, Tcomplex32, Tcomplex32);
		X(Tuns64, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tuns64, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tfloat32, Tfloat32, Tfloat32, Tfloat32, Tfloat32);
		X(Tfloat32, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tfloat32, Tfloat80, Tfloat80, Tfloat80, Tfloat80);

		X(Tfloat32, Timaginary32, Tfloat32, Timaginary32, Tfloat32);
		X(Tfloat32, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tfloat32, Timaginary80, Tfloat80, Timaginary80, Tfloat80);

		X(Tfloat32, Tcomplex32, Tfloat32, Tcomplex32, Tcomplex32);
		X(Tfloat32, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tfloat32, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tfloat64, Tfloat64, Tfloat64, Tfloat64, Tfloat64);
		X(Tfloat64, Tfloat80, Tfloat80, Tfloat80, Tfloat80);

		X(Tfloat64, Timaginary32, Tfloat64, Timaginary64, Tfloat64);
		X(Tfloat64, Timaginary64, Tfloat64, Timaginary64, Tfloat64);
		X(Tfloat64, Timaginary80, Tfloat80, Timaginary80, Tfloat80);

		X(Tfloat64, Tcomplex32, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tfloat64, Tcomplex64, Tfloat64, Tcomplex64, Tcomplex64);
		X(Tfloat64, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tfloat80, Tfloat80, Tfloat80, Tfloat80, Tfloat80);

		X(Tfloat80, Timaginary32, Tfloat80, Timaginary80, Tfloat80);
		X(Tfloat80, Timaginary64, Tfloat80, Timaginary80, Tfloat80);
		X(Tfloat80, Timaginary80, Tfloat80, Timaginary80, Tfloat80);

		X(Tfloat80, Tcomplex32, Tfloat80, Tcomplex80, Tcomplex80);
		X(Tfloat80, Tcomplex64, Tfloat80, Tcomplex80, Tcomplex80);
		X(Tfloat80, Tcomplex80, Tfloat80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Timaginary32, Timaginary32, Timaginary32, Timaginary32, Timaginary32);
		X(Timaginary32, Timaginary64, Timaginary64, Timaginary64, Timaginary64);
		X(Timaginary32, Timaginary80, Timaginary80, Timaginary80, Timaginary80);

		X(Timaginary32, Tcomplex32, Timaginary32, Tcomplex32, Tcomplex32);
		X(Timaginary32, Tcomplex64, Timaginary64, Tcomplex64, Tcomplex64);
		X(Timaginary32, Tcomplex80, Timaginary80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Timaginary64, Timaginary64, Timaginary64, Timaginary64, Timaginary64);
		X(Timaginary64, Timaginary80, Timaginary80, Timaginary80, Timaginary80);

		X(Timaginary64, Tcomplex32, Timaginary64, Tcomplex64, Tcomplex64);
		X(Timaginary64, Tcomplex64, Timaginary64, Tcomplex64, Tcomplex64);
		X(Timaginary64, Tcomplex80, Timaginary80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Timaginary80, Timaginary80, Timaginary80, Timaginary80, Timaginary80);

		X(Timaginary80, Tcomplex32, Timaginary80, Tcomplex80, Tcomplex80);
		X(Timaginary80, Tcomplex64, Timaginary80, Tcomplex80, Tcomplex80);
		X(Timaginary80, Tcomplex80, Timaginary80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tcomplex32, Tcomplex32, Tcomplex32, Tcomplex32, Tcomplex32);
		X(Tcomplex32, Tcomplex64, Tcomplex64, Tcomplex64, Tcomplex64);
		X(Tcomplex32, Tcomplex80, Tcomplex80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tcomplex64, Tcomplex64, Tcomplex64, Tcomplex64, Tcomplex64);
		X(Tcomplex64, Tcomplex80, Tcomplex80, Tcomplex80, Tcomplex80);

		/* ======================= */

		X(Tcomplex80, Tcomplex80, Tcomplex80, Tcomplex80, Tcomplex80);

		/* ======================= */
		/* ======================= */

		Y(Tint8, Tbit);
		Y(Tuns8, Tbit);
		Y(Tint16, Tbit);
		Y(Tuns16, Tbit);
		Y(Tint32, Tbit);
		Y(Tuns32, Tbit);
		Y(Tint64, Tbit);
		Y(Tuns64, Tbit);

		Y(Tuns8, Tint8);
		Y(Tint16, Tint8);
		Y(Tuns16, Tint8);
		Y(Tint32, Tint8);
		Y(Tuns32, Tint8);
		Y(Tint64, Tint8);
		Y(Tuns64, Tint8);

		Y(Tint8, Tuns8);
		Y(Tint16, Tuns8);
		Y(Tuns16, Tuns8);
		Y(Tint32, Tuns8);
		Y(Tuns32, Tuns8);
		Y(Tint64, Tuns8);
		Y(Tuns64, Tuns8);

		Y(Tuns16, Tint16);
		Y(Tint32, Tint16);
		Y(Tuns32, Tint16);
		Y(Tint64, Tint16);
		Y(Tuns64, Tint16);

		Y(Tint16, Tuns16);
		Y(Tint32, Tuns16);
		Y(Tuns32, Tuns16);
		Y(Tint64, Tuns16);
		Y(Tuns64, Tuns16);

		// Y(Tuns32, Tint32);
		Y(Tint64, Tint32);
		Y(Tuns64, Tint32);

		// Y(Tint32, Tuns32);
		Y(Tint64, Tuns32);
		Y(Tuns64, Tuns32);

		Y(Tint64, Tuns64);
		Y(Tuns64, Tint64);

		/* ======================= */
		/* ======================= */

		for (int i = 0; i < TMAX; i++) {
			for (int j = 0; j < TMAX; j++) {
				if (impcnvResult[i][j] == Terror) {
					impcnvResult[i][j] = impcnvResult[j][i];
					impcnvType1[i][j] = impcnvType2[j][i];
					impcnvType2[i][j] = impcnvType1[j][i];
				}
			}
		}
	}

	private static void X(TY t1, TY t2, TY nt1, TY nt2, TY rt) {
		X(t1.ordinal(), t2.ordinal(), nt1, nt2, rt);
	}

	private static void X(int t1, int t2, TY nt1, TY nt2, TY rt) {
		impcnvResult[t1][t2] = rt;
		impcnvType1[t1][t2] = nt1;
		impcnvType2[t1][t2] = nt2;
	}

	private static void Y(TY t1, TY t2) {
		Y(t1.ordinal(), t2.ordinal());
	}

	private static void Y(int t1, int t2) {
		impcnvWarn[t1][t2] = true;
	}

	public TY ty;
	public int mod; // modifiers (MODconst, MODinvariant)
	public String deco;
	public Type cto; // MODconst ? mutable version of this type : const version
	public Type ito; // MODinvariant ? mutable version of this type : invariant version
	public Type sto;		// MODshared ? mutable version of this type : shared mutable version
	public Type scto;		// MODshared|MODconst ? mutable version of this type : shared const version

	public Type pto; // merged pointer to this type
	public Type rto; // reference to this type
	public Type arrayof; // array of this type
	public TypeInfoDeclaration vtinfo;

	/*
	 * Descent: when a TypeIdentifier referres to an alias, for example:
	 *
	 * alias int foo;
	 *
	 * then foo will be resolved to int. Now, in autocompletion and stuff
	 * we don't want to show int, but foo, so when a TypeIdentifier is
	 * resolved we keep a reference in the type to this alias.
	 */
	public Dsymbol alias;

	// This field is kept in SemanticContext
	// public TypeInfoDeclaration vtinfo; // TypeInfo object for this Type

	/*
	 * In DMD TypeBasic's are only constructed one, so == works.
	 * In Descent, the parser creates different instances. Our
	 * hack is to have a reference to that singleton, and use it
	 * in the == comparison.
	 */
	public Type singleton;

	public List<Modification> modifications;

	public Type(TY ty) {
		this.ty = ty;
		this.singleton = this;
	}

	public Type(TY ty, Type singleton) {
		this.ty = ty;
		this.singleton = singleton;
	}

	@Override
	public DYNCAST dyncast() {
		return DYNCAST.DYNCAST_TYPE;
	}

	public TypeBasic isTypeBasic() {
		return null;
	}

	public boolean isbit() {
		return false;
	}

	public boolean iscomplex() {
		return false;
	}

	public boolean isreal() {
		return false;
	}

	public boolean isimaginary() {
		return false;
	}

	public ClassDeclaration isClassHandle() {
		return null;
	}

	public Type semantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		return merge(context);
	}

	/*******************************
	 * Determine if converting 'this' to 'to' is an identity operation,
	 * a conversion to const operation, or the types aren't the same.
	 * Returns:
	 *	MATCHequal	'this' == 'to'
	 *	MATCHconst	'to' is const
	 *	MATCHnomatch	conversion to mutable or invariant
	 * @param context TODO
	 */
	public MATCH constConv(Type to, SemanticContext context) {
		if (equals(to)) {
			return MATCHexact;
		} else if (ty == to.ty && to.mod == MODconst) {
			return MATCHconst;
		} else {
			return MATCHnomatch;
		}
	}

	public Type trySemantic(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		int errors = context.global.errors;
		context.global.gag++; // suppress printing of error messages
		Type t = semantic(filename, lineNumber, sc, context);
		context.global.gag--;
		if (errors != context.global.errors) // if any errors happened
		{
			context.global.errors = errors;
			t = null;
		}
		return t;
	}

	public Type merge(IStringTableHolder context) {
		Type t;

		t = this;
		if (deco == null) {
			OutBuffer buf = new OutBuffer();
			StringValue sv;

			toDecoBuffer(buf, 0, context instanceof SemanticContext ? (SemanticContext) context : null);
			sv = context.getStringTable().update(buf.toString());
			if (sv.ptrvalue != null) {
				// Descent: changed to have better alias resolution in UI
//				t = (Type) sv.ptrvalue;
//				Assert.isTrue(t.deco != null);
				Type other = (Type) sv.ptrvalue;
				Assert.isTrue(other.deco != null);
				deco = other.deco;
			} else {
				sv.ptrvalue = this;
				deco = sv.lstring;
			}
		}
		return t;
	}

	/*************************************
	 * This version does a merge even if the deco is already computed.
	 * Necessary for types that have a deco, but are not merged.
	 */
	public Type merge2(IStringTableHolder context) {
		Type t = this;
		if (null == t.deco)
			return t.merge(context);

		// Changed for Descent
//		StringValue sv = context.stringTable.lookup(t.deco);
		StringValue sv = context.getStringTable().update(t.deco);
		if (sv.ptrvalue == null) {
			sv.ptrvalue = this;
			deco = sv.lstring;
		}
		if (sv != null && sv.ptrvalue != null) {
			// Descent: changed to have better alias UI resolution
//			t = (Type) sv.ptrvalue;
			Type other = (Type) sv.ptrvalue;
			Assert.isTrue(other.deco != null);
			deco = other.deco;
		} else {
			throw new IllegalStateException();
		}
		return t;
	}

	public void toDecoBuffer(OutBuffer buf, int flag, SemanticContext context) {
		Type_toDecoBuffer(buf, flag, context);
	}

	protected void Type_toDecoBuffer(OutBuffer buf, int flag, SemanticContext context) {
		if (flag != mod && flag != 0x100) {
			if ((mod & MODshared) != 0)
				buf.writeByte('O');

			if ((mod & MODconst) != 0)
				buf.writeByte('x');
			else if ((mod & MODinvariant) != 0)
				buf.writeByte('y');

			// Cannot be both const and invariant
			assert ((mod & (MODconst | MODinvariant)) != (MODconst | MODinvariant));
		}
		buf.writeByte(ty.mangleChar);
	}

	public void resolve(char[] filename, int lineNumber, Scope sc, Expression[] pe, Type[] pt,
			Dsymbol[] ps, SemanticContext context) {
		Type t;

		t = semantic(filename, lineNumber, sc, context);
		pt[0] = t;
		pe[0] = null;
		ps[0] = null;
	}

	public Expression toExpression() {
		return null;
	}

	public Type toBasetype(SemanticContext context) {
		return this;
	}

	public Dsymbol toDsymbol(Scope sc, SemanticContext context) {
		return null;
	}

	public boolean isintegral() {
		return false;
	}

	public boolean isunsigned() {
		return false;
	}

	public boolean isauto() {
		return false;
	}

	public Type pointerTo(SemanticContext context) {
		if (singleton.pto == null) {
			Type t;

			if (singleton.deco == null && this.deco != null) {
				singleton.deco = this.deco;
			}

			t = new TypePointer(singleton);
			singleton.pto = t.merge(context);
		}
		return singleton.pto;
	}

	public Type referenceTo(SemanticContext context) {
		if (singleton.rto == null) {
			Type t;

			if (singleton.deco == null && this.deco != null) {
				singleton.deco = this.deco;
			}

			t = new TypeReference(singleton, context);
			singleton.rto = t.merge(context);
		}
		return rto;
	}

	public Type arrayOf(SemanticContext context) {
		if (singleton.arrayof == null) {
			Type t;

			if (singleton.deco == null && this.deco != null) {
				singleton.deco = this.deco;
			}

			t = new TypeDArray(singleton);
			singleton.arrayof = t.merge(context);
		}
		return singleton.arrayof;
	}

	public Type constOf(IStringTableHolder context) {
		if (isConst()) {
			return this;
		}
		if (cto != null) {
			return cto;
		}
		Type t = makeConst(0, 0, context);
		t = t.merge(context);
		t.fixTo(this);
		cto = t;
		if (ito != null) {
			ito.cto = t;
		}
		return t;
	}

	public Type invariantOf(IStringTableHolder context) {
		if (isInvariant()) {
			return this;
		}
		if (ito != null) {
			return ito;
		}
		Type t = makeInvariant(0, 0, context);
		t = t.merge(context);
		t.fixTo(this);
		ito = t;
		if (cto != null) {
			cto.ito = t;
		}
		return t;
	}

	public Type mutableOf(SemanticContext context) {
		Type t = this;
		if (isConst()) {
		    if (isShared())
			    t = sto;		// shared const => shared
			else
			    t = cto;
		} else if (isInvariant()) {
			t = ito;
		}
		if (null == t) {
			t = copy();
			//		int sz = sizeTy[ty];
			//		t = (Type *)mem.malloc(sz);
			//		memcpy(t, this, sz);
			t.mod = 0;
			t.deco = null;
			t.arrayof = null;
			t.pto = null;
			t.rto = null;
			t.cto = null;
			t.ito = null;
			t.sto = null;
			t.scto = null;
			t.vtinfo = null;
//			if (ty == Tsarray) {
//				TypeSArray ta = (TypeSArray) t;
//				//ta.next = ta.next.mutableOf();
//			}
			t = t.merge(context);
			t.fixTo(this);

			switch (mod) {
			case MODconst:
				t.cto = this;
				break;

			case MODinvariant:
				t.ito = this;
				break;

			case MODshared:
				t.sto = this;
				break;

			case MODshared | MODconst:
				t.scto = this;
				break;

			default:
				throw new IllegalStateException();
			}
		}
		return t;
	}

	public Type sharedOf(IStringTableHolder context) {
		if (mod == MODshared) {
			return this;
		}
		if (sto != null) {
			Assert.isTrue (sto.isShared());
			return sto;
		}
		Type t = makeShared(context);
		t = t.merge(context);
		t.fixTo(this);
		return t;
	}

	public Type sharedConstOf(IStringTableHolder context) {
		if (mod == (MODshared | MODconst)) {
			return this;
		}
		if (scto != null) {
			assert (scto.mod == (MODshared | MODconst));
			return scto;
		}
		Type t = makeSharedConst(context);
		t = t.merge(context);
		t.fixTo(this);
		return t;
	}

	/**********************************
	 * For our new type 'this', which is type-constructed from t,
	 * fill in the cto, ito, sto, scto shortcuts.
	 */
	public void fixTo(Type t)
	{
	    ito = t.ito;

	    Assert.isTrue(mod != t.mod);
	    switch ((mod << 3) | t.mod)
	    {
		case (0 << 3) | MODconst:
		    cto = t;
		    break;

		case (0 << 3) | MODinvariant:
		    ito = t;
		    break;

		case (0 << 3) | MODshared:
		    sto = t;
		    break;

		case (0 << 3) | MODshared | MODconst:
		    scto = t;
		    break;

		case (MODconst << 3) | 0:
		    cto = null;
		    // goto L2;
		 	t.cto = this;
		    break;

		case (MODconst << 3) | MODinvariant:
		    ito = t;
		    // goto L2;
		 	t.cto = this;
		    break;

		case (MODconst << 3) | MODshared:
		    sto = t;
		    // goto L2;
		 	t.cto = this;
		    break;

		case (MODconst << 3) | MODshared | MODconst:
		    scto = t;
			// L2:
		    t.cto = this;
		    break;

		case (MODinvariant << 3) | 0:
		    ito = null;
		    // goto L3;
			t.ito = this;
		    if (t.cto != null) t.cto.ito = this;
		    if (t.sto != null) t.sto.ito = this;
		    if (t.scto != null) t.scto.ito = this;
		    break;

		case (MODinvariant << 3) | MODconst:
		    cto = t;
		    // goto L3;
			t.ito = this;
		    if (t.cto != null) t.cto.ito = this;
		    if (t.sto != null) t.sto.ito = this;
		    if (t.scto != null) t.scto.ito = this;
		    break;

		case (MODinvariant << 3) | MODshared:
		    sto = t;
		    // goto L3;
			t.ito = this;
		    if (t.cto != null) t.cto.ito = this;
		    if (t.sto != null) t.sto.ito = this;
		    if (t.scto != null) t.scto.ito = this;
		    break;

		case (MODinvariant << 3) | MODshared | MODconst:
		    scto = t;
			// L3:
		    t.ito = this;
		    if (t.cto != null) t.cto.ito = this;
		    if (t.sto != null) t.sto.ito = this;
		    if (t.scto != null) t.scto.ito = this;
		    break;

		case (MODshared << 3) | 0:
		    sto = null;
		    // goto L4;
			t.sto = this;
		    break;

		case (MODshared << 3) | MODconst:
		    cto = t;
		    // goto L4;
			t.sto = this;
		    break;

		case (MODshared << 3) | MODinvariant:
		    ito = t;
		    // goto L4;
			t.sto = this;
		    break;

		case (MODshared << 3) | MODshared | MODconst:
		    scto = t;
			// L4:
		    t.sto = this;
		    break;

		case ((MODshared | MODconst) << 3) | 0:
		    scto = null;
		    break;

		case ((MODshared | MODconst) << 3) | MODconst:
		    cto = t;
		    break;

		case ((MODshared | MODconst) << 3) | MODinvariant:
		    ito = t;
		    break;

		case ((MODshared | MODconst) << 3) | MODshared:
		    sto = t;
			// L5:
		    t.scto = this;
		    break;

		default:
		    throw new IllegalStateException();
	    }

	    check();
	    t.check();
	}

	/***************************
	 * Look for bugs in constructing types.
	 */
	public void check() {
		switch (mod) {
		case 0:
			if (cto != null)
				Assert.isTrue (cto.mod == MODconst);
			if (ito != null)
				Assert.isTrue (ito.mod == MODinvariant);
			if (sto != null)
				Assert.isTrue (sto.mod == MODshared);
			if (scto != null)
				Assert.isTrue (scto.mod == (MODshared | MODconst));
			break;

		case MODconst:
			if (cto != null)
				Assert.isTrue (cto.mod == 0);
			if (ito != null)
				Assert.isTrue (ito.mod == MODinvariant);
			if (sto != null)
				Assert.isTrue (sto.mod == MODshared);
			if (scto != null)
				Assert.isTrue (scto.mod == (MODshared | MODconst));
			break;

		case MODinvariant:
			if (cto != null)
				Assert.isTrue (cto.mod == MODconst);
			if (ito != null)
				Assert.isTrue (ito.mod == 0);
			if (sto != null)
				Assert.isTrue (sto.mod == MODshared);
			if (scto != null)
				Assert.isTrue (scto.mod == (MODshared | MODconst));
			break;

		case MODshared:
			if (cto != null)
				Assert.isTrue (cto.mod == MODconst);
			if (ito != null)
				Assert.isTrue (ito.mod == MODinvariant);
			if (sto != null)
				Assert.isTrue (sto.mod == 0);
			if (scto != null)
				Assert.isTrue (scto.mod == (MODshared | MODconst));
			break;

		case MODshared | MODconst:
			if (cto != null)
				Assert.isTrue (cto.mod == MODconst);
			if (ito != null)
				Assert.isTrue (ito.mod == MODinvariant);
			if (sto != null)
				Assert.isTrue (sto.mod == MODshared);
			if (scto != null)
				Assert.isTrue (scto.mod == 0);
			break;

		default:
			throw new IllegalStateException();
		}

		Type tn = nextOf();
		if (tn != null && ty != Tfunction && ty != Tdelegate) { // Verify
																// transitivity
			switch (mod) {
			case 0:
				break;

			case MODconst:
				Assert.isTrue ((tn.mod & MODinvariant) != 0 || (tn.mod & MODconst) != 0);
				break;

			case MODinvariant:
				Assert.isTrue (tn.mod == MODinvariant);
				break;

			case MODshared:
				Assert.isTrue ((tn.mod & MODinvariant) != 0 || (tn.mod & MODshared) != 0);
				break;

			case MODshared | MODconst:
				Assert.isTrue ((tn.mod & MODinvariant) != 0 || (tn.mod & (MODshared | MODconst)) != 0);
				break;

			default:
				throw new IllegalStateException();
			}
			tn.check();
		}
	}

	public final Expression defaultInit(SemanticContext context) {
		return defaultInit(null, 0, context);
	}

	public Expression defaultInit(char[] filename, int lineNumber, SemanticContext context) {
		return null;
	}

	public final Expression getProperty(char[] filename, int lineNumber, IdentifierExp ident,
			SemanticContext context) {
		return getProperty(filename, ident.getLineNumber(), ident.ident, ident.start, ident.length, context);
	}

	public Expression getProperty(char[] filename, int lineNumber, char[] ident, int start, int length,
			SemanticContext context) {
		Expression e = null;

		if (equals(ident, Id.__sizeof)) {
			e = new IntegerExp(filename, lineNumber, size(filename, lineNumber, context), Type.tsize_t);
		} else if (equals(ident, Id.size)) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.DeprecatedProperty, lineNumber, start, length,
						new String[] { ".size", ".sizeof" }));
			}
			if (context.isD1()) {
				e = new IntegerExp(filename, lineNumber, size(filename, lineNumber, context), Type.tsize_t);
			} else {
				e = new ErrorExp();
			}
		} else if (equals(ident, Id.alignof)) {
			e = new IntegerExp(filename, lineNumber, alignsize(context), Type.tsize_t);
		} else if (equals(ident, Id.typeinfo)) {
			if (!context.global.params.useDeprecated) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.DeprecatedProperty, lineNumber, start, length,
							new String[] { "typeinfo", ".typeid(type)" }));
				}
			}
			e = getTypeInfo(null, context);
		} else if (equals(ident, Id.init)) {
			if (ty == Tvoid) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.VoidDoesNotHaveAnInitializer, lineNumber, start, length));
				}
			}
			e = defaultInit(filename, lineNumber, context);
		} else if (equals(ident, Id.mangleof)) {
		    	String s;
			if (null == deco)
			{   s = toChars(context);
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForwardReferenceOfTypeDotMangleof, lineNumber, start, length, s));
				}
			}
			else {
			    s = deco;
			}
			e = new StringExp(filename, lineNumber, s.toCharArray(), s.length(), 'c');
			Scope sc = new Scope(context);
			e = e.semantic(sc, context);
		} else if (equals(ident, Id.stringof)) {
			char[] s = toChars(context).toCharArray();
			e = new StringExp(filename, lineNumber, s, s.length, 'c');
			Scope sc = new Scope(context);
			e = e.semantic(sc, context);
		} else {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.UndefinedProperty, lineNumber, start, length, new String[] {
								new String(ident), toChars(context) }));
			}
			if (context.isD1()) {
				e = new IntegerExp(filename, lineNumber, Id.ONE, 1, Type.tint32);
			} else  {
				e = new ErrorExp();
			}
		}
		return e;
	}

	public Type reliesOnTident() {
		return null;
	}

	public void checkDeprecated(char[] filename, int lineNumber, Scope sc, SemanticContext context) {
		Dsymbol s = toDsymbol(sc, context);
		if (s != null) {
			s.checkDeprecated(sc, context, this); // SEMANTIC this is reference?
		}
	}

	public Type toHeadMutable(SemanticContext context) {
		if (0 == mod)
			return this;
		return mutableOf(context);
	}

	public boolean isConst() {
		return mod == MODconst;
	}

	public boolean isInvariant() {
		return mod == MODinvariant;
	}

	public boolean isMutable() {
		return mod == 0;
	}

	public boolean isShared() {
		return (mod & MODshared) != 0;
	}

	public boolean isSharedConst() {
		return mod == (MODshared | MODconst);
	}

	public Expression dotExp(Scope sc, Expression e, IdentifierExp ident,
			SemanticContext context) {
		VarDeclaration v = null;

		if (e.op == TOKdotvar) {
			DotVarExp dv = (DotVarExp) e;
			v = dv.var.isVarDeclaration();
		} else if (e.op == TOKvar) {
			VarExp ve = (VarExp) e;
			v = ve.var.isVarDeclaration();
		}
		if (null != v) {
			if (equals(ident, Id.offset)) {
				if (!context.global.params.useDeprecated) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.DotOffsetDeprecated, this));
					}
				}
				//goto Loffset;
				if (0 != (v.storage_class & STCfield)) {
					e = new IntegerExp(e.filename, e.lineNumber,  v.offset(), Type.tsize_t);
					return e;
				}
			} else if (equals(ident, Id.offsetof)) {
				//Loffset:
				if (0 != (v.storage_class & STCfield)) {
					e = new IntegerExp(e.filename, e.lineNumber,  v.offset(), Type.tsize_t);
					return e;
				}
			} else if (equals(ident, Id.init)) {
			    Expression ex = defaultInit(e.filename, e.lineNumber, context);
			    return ex;
			}
		}

		if (equals(ident, Id.typeinfo)) {
			if (!context.global.params.useDeprecated) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.DeprecatedProperty, ident, new String[] { ".typeinfo",
									"typeid(type)" }));
				}
			}
			e = getTypeInfo(sc, context);
			return e;
		}

		if (equals(ident, Id.stringof)) {
			char[] s = e.toChars(context).toCharArray();
			e = new StringExp(e.filename, e.lineNumber,  s, 'c');
			Scope _sc = new Scope(context);
			e = e.semantic(_sc, context);
			return e;
		}

		return getProperty(e.filename, e.lineNumber,  ident, context);
	}

	public int size(char[] filename, int lineNumber, SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeError(
					IProblem.NoSizeForType, this, new String[] { toChars(context) }));
		}
		return 1;
	}

	public final int size(SemanticContext context) {
		return size(null, 0, context);
	}

	public int alignsize(SemanticContext context) {
		return size(null, 0, context);
	}

	public int memalign(int salign, SemanticContext context) {
		return salign;
	}

	public boolean isBaseOf(Type type, int[] poffset, SemanticContext context) {
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Type)) {
			return false;
		}

		Type t = (Type) o;

		// deco strings are unique and semantic() has been run
		if (this.singleton == t.singleton ||
				(t != null && deco != null && t.deco != null && deco.equals(t.deco)) ||
				(t != null && singleton.deco != null && singleton.deco.equals(t.singleton.deco))) {
			return true;
		}
		return false;
	}

	// This is a fix for "Type == Type". We can't use that anymore here,
	// because:
	// 1. Each different semantic pass creates different types
	// 2. Later types will be hidden in an interface (I guess)
	public boolean same(Type t) {
		if (this == t) {
			return true;
		}

		if (t == null) {
			return false;
		}

//		if (singleton == t.singleton) {
//			return true;
//		}
//
//		if (singleton.deco != null && t.singleton.deco != null && singleton.deco.equals(t.singleton.deco)) {
//			return true;
//		}

		if (t != null && t.deco != null && this.deco != null && t.deco.equals(this.deco)) {
			return true;
		}

		if (this.getNodeType() == TYPE_BASIC &&  t.getNodeType() == TYPE_BASIC) {
			return this.ty == t.ty;
		}

//		if (getSignature().equals(t.getSignature())) {
//			return true;
//		}

		return false;
	}

	public MATCH implicitConvTo(Type to, SemanticContext context) {
		if (same(this, to, context)) {
			return MATCH.MATCHexact;
		}
		return MATCH.MATCHnomatch;
	}

	private final static int COVARIANT = 1;
	private final static int DISTINCT = 0;
	private final static int NOT_COVARIANT = 2;
	private final static int OTHER = 3;

	public int covariant(Type t, SemanticContext context) {
		boolean inoutmismatch = false;

		if (equals(t)) {
			return COVARIANT;
		}
		if (ty != TY.Tfunction || t.ty != TY.Tfunction) {
			return DISTINCT;
		}

		TypeFunction t1 = (TypeFunction) this;
		TypeFunction t2 = (TypeFunction) t;

		if (t1.varargs != t2.varargs) {
			return DISTINCT;
		}

		if (t1.parameters != null && t2.parameters != null) {
			int dim = Argument.dim(t1.parameters, context);
			if (dim != Argument.dim(t2.parameters, context)) {
				return DISTINCT;
			}

			for (int i = 0; i < dim; i++) {
				Argument arg1 = Argument.getNth(t1.parameters, i, context);
				Argument arg2 = Argument.getNth(t2.parameters, i, context);

				if (!arg1.type.equals(arg2.type)) {
					return DISTINCT;
				}
				if (context.isD1()) {
					if (arg1.storageClass != arg2.storageClass) {
						inoutmismatch = true;
					}
				} else {
					if ((arg1.storageClass & ~STCscope) != (arg2.storageClass & ~STCscope))
						inoutmismatch = true;
					// We can add scope, but not subtract it
					if (0 == (arg1.storageClass & STCscope) && (arg2.storageClass & STCscope) != 0)
						inoutmismatch = true;
				}
			}
		} else if (t1.parameters != t2.parameters) {
			return DISTINCT;
		}

		// The argument lists match
		if (inoutmismatch) {
			return NOT_COVARIANT;
		}
		if (t1.linkage != t2.linkage) {
			return NOT_COVARIANT;
		}

	    // Return types
		Type t1n = t1.next;
		Type t2n = t2.next;

		if (t1n.equals(t2n)) {
			return COVARIANT;
		}

		if (context.isD1()) {
			if (t1n.ty != TY.Tclass || t2n.ty != TY.Tclass) {
				return NOT_COVARIANT;
			}

			// If t1n is forward referenced:
			ClassDeclaration cd = ((TypeClass) t1n).sym;
			if (cd.baseClass == null && cd.baseclasses != null
					&& cd.baseclasses.size() > 0
					&& cd.isInterfaceDeclaration() == null) {
				return OTHER;
			}

			if (t1n.implicitConvTo(t2n, context) != MATCH.MATCHnomatch) {
				return COVARIANT;
			}
			return NOT_COVARIANT;
		} else {
			boolean gotoLcovariant = false;

			if (t1n.ty == Tclass && t2n.ty == Tclass) {
				/*
				 * If same class type, but t2n is const, then it's covariant. Do
				 * this test first because it can work on forward references.
				 */
				if (((TypeClass) t1n).sym == ((TypeClass) t2n).sym && t2n.mod == MODconst) {
					// goto Lcovariant;
					gotoLcovariant = true;
				}

				if (!gotoLcovariant) {
					// If t1n is forward referenced:
					ClassDeclaration cd = ((TypeClass) t1n).sym;
					if (null == cd.baseClass && size(cd.baseclasses) != 0 && null == cd.isInterfaceDeclaration()) {
						return 3;
					}
				}
			}

			if (!gotoLcovariant) {
				if (t1n.implicitConvTo(t2n, context) != null) {
					// goto Lcovariant;
					gotoLcovariant = true;
				}
			}

			if (!gotoLcovariant) {
				return NOT_COVARIANT;
			}

			// Lcovariant:
			/*
			 * Can convert mutable to const
			 */
			if (t1.mod != t2.mod) {
				if (0 == (t1.mod & MODconst) && (t2.mod & MODconst) != 0) {
					return NOT_COVARIANT;
				}
				if (0 == (t1.mod & MODshared) && (t2.mod & MODshared) != 0) {
					return NOT_COVARIANT;
				}
			}

			/*
			 * Can convert pure to impure, and nothrow to throw
			 */
			if (!t1.ispure && t2.ispure) {
				return NOT_COVARIANT;
			}

			if (!t1.isnothrow && t2.isnothrow) {
				return NOT_COVARIANT;
			}

			if (t1.isref != t2.isref) {
				return NOT_COVARIANT;
			}

			return COVARIANT;
		}
	}

	public boolean isfloating() {
		return false;
	}

	public boolean isString(SemanticContext context) {
		return false;
	}

	public boolean isZeroInit(char[] filename, int lineNumber, SemanticContext context) {
		return false;
	}

	public boolean checkBoolean(SemanticContext context) {
		return isscalar(context);
	}

	public boolean isscalar(SemanticContext context) {
		return false;
	}

	public Type syntaxCopy(SemanticContext context) {
		Assert.isTrue(false); // assert(0);
		return this;
	}

	@Override
	public String toChars(SemanticContext context) {
		OutBuffer buf = new OutBuffer();
		HdrGenState hgs = new HdrGenState();
		toCBuffer(buf, null, hgs, context);
		return buf.toChars();
	}

	public void toCBuffer(OutBuffer buf, IdentifierExp ident, HdrGenState hgs,
			SemanticContext context) {
	    toCBuffer2(buf, hgs, 0, context);
		if (ident != null) {
			buf.writeByte(' ');
			buf.writestring(ident.toChars());
		}
	}

	public void toCBuffer2(OutBuffer buf, HdrGenState hgs, int mod, SemanticContext context) {
		if (mod != this.mod) {
			toCBuffer3(buf, hgs, mod, context);
			return;
		}
		buf.writestring(toChars(context));
	}

	public void toCBuffer3(OutBuffer buf, HdrGenState hgs, int mod,
			SemanticContext context) {
		if (mod != this.mod) {
			if (context.isD2()) {
				if ((this.mod & MODshared) != 0)
				    buf.writestring("shared(");
			}

			switch (context.isD1() ? this.mod : (this.mod & (MODconst | MODinvariant))) {
			case 0:
				toCBuffer2(buf, hgs, this.mod, context);
				break;
			case MODconst:
				buf.writestring("const(");
				toCBuffer2(buf, hgs, this.mod, context);
				buf.writeByte(')');
				break;
			case MODinvariant:
				if (context.isD1()) {
					buf.writestring("invariant(");
				} else {
					buf.writestring("immutable(");
				}
				toCBuffer2(buf, hgs, this.mod, context);
				buf.writeByte(')');
				break;
			default:
				throw new IllegalStateException("assert(0)");
			}

			if (context.isD2()) {
				if ((this.mod & MODshared) != 0)
				    buf.writeByte(')');
			}
		}
	}

	public boolean hasPointers(SemanticContext context) {
		return false;
	}

	public char[] getTypeInfoIdent(int internal) {
		// TODO semantic
		return null;
	}

	public Type nextOf() {
		return null;
	}

	public Type makeConst(int startPosition, int length, IStringTableHolder context) {
		if (cto != null) {
			return cto;
		}

		Type t = copy();
		t.mod = MODconst;
	    t.deco = null;
	    t.arrayof = null;
	    t.pto = null;
	    t.rto = null;
	    t.cto = null;
	    t.ito = null;
	    t.sto = null;
		t.scto = null;
		t.vtinfo = null;
		if (t.modifications == null) {
			t.modifications = new ArrayList<Modification>();
		}
		t.modifications.add(new Modification(TOK.TOKconst, startPosition,
				length));
		return t;
	}

	public Type makeInvariant(int startPosition, int length, IStringTableHolder context) {
		if (ito != null) {
			return ito;
		}

		Type t = copy();
		t.mod = MODinvariant;
	    t.deco = null;
	    t.arrayof = null;
	    t.pto = null;
	    t.rto = null;
	    t.cto = null;
	    t.ito = null;
	    t.sto = null;
		t.scto = null;
		t.vtinfo = null;
		if (t.modifications == null) {
			t.modifications = new ArrayList<Modification>();
		}
		t.modifications.add(new Modification(TOK.TOKinvariant,
				startPosition, length));
		return t;
	}

	public Type makeShared(IStringTableHolder context) {
		if (sto != null)
			return sto;
		Type t = copy();
		t.mod = MODshared;
		t.deco = null;
		t.arrayof = null;
		t.pto = null;
		t.rto = null;
		t.cto = null;
		t.ito = null;
		t.sto = null;
		t.scto = null;
		t.vtinfo = null;
		return t;
	}

	public Type makeSharedConst(IStringTableHolder context) {
		if (scto != null)
			return scto;
		Type t = copy();
		t.mod = MODshared | MODconst;
		t.deco = null;
		t.arrayof = null;
		t.pto = null;
		t.rto = null;
		t.cto = null;
		t.ito = null;
		t.sto = null;
		t.scto = null;
		t.vtinfo = null;
		return t;
	}

	public MATCH deduceType(Scope sc, Type tparam,
			TemplateParameters parameters, Objects dedtypes,
			SemanticContext context) {
		if (tparam == null) {
			return MATCHnomatch;
		}

		if (this == tparam) {
			return MATCHexact;
		}

		if (tparam.ty == Tident) {
			// Determine which parameter tparam is
			int i = templateParameterLookup(tparam, parameters);
			if (i == -1) {
				if (sc == null) {
					return MATCHnomatch;
				}

				 /* Need a loc to go with the semantic routine.
			     */
			    char[] filename = null;
			    int lineNumber = 0;
				if (parameters != null && parameters.size() != 0) {
					TemplateParameter tp = (TemplateParameter) parameters
							.get(0);
					filename = tp.filename;
					lineNumber = tp.lineNumber;
				}

				/* BUG: what if tparam is a template instance, that
				 * has as an argument another Tident?
				 */
				tparam = tparam.semantic(filename, lineNumber, sc, context);
				if (tparam.ty == Tident) {
					throw new IllegalStateException(
							"assert(tparam.ty != Tident);");
				}
				return deduceType(sc, tparam, parameters, dedtypes, context);
			}

			TemplateParameter tp = parameters.get(i);

			// Found the corresponding parameter tp
			if (null == tp.isTemplateTypeParameter()) {
				return MATCHnomatch;
			}
			Type tt = this;
			Type at = (Type) dedtypes.get(i);

			if (context.isD1()) {
				if (null == at) {
					dedtypes.set(i, this);
					return MATCHexact;
				}
				if (equals(at)) {
					return MATCHexact;
				} else if (ty == Tclass && at.ty == Tclass) {
					return implicitConvTo(at, context);
				} else if (ty == Tsarray && at.ty == Tarray
						&& nextOf().equals(at.nextOf())) {
					return MATCHexact;
				} else {
					return MATCHnomatch;
				}
			} else {
				// 3*3 == 9 cases
				if (tparam.isMutable())
				{   // foo(U:U) T            => T
				    // foo(U:U) const(T)     => const(T)
				    // foo(U:U) invariant(T) => invariant(T)
				    if (null == at)
				    {   dedtypes.set(i, this);
						return MATCHexact;
				    }
				}
				else if (mod == tparam.mod)
				{   // foo(U:const(U))     const(T)     => T
				    // foo(U:invariant(U)) invariant(T) => T
				    tt = mutableOf(context);
				    if (null == at)
				    {   dedtypes.set(i, tt);
						return MATCHexact;
				    }
				}
				else if (tparam.isConst())
				{   // foo(U:const(U)) T            => T
				    // foo(U:const(U)) invariant(T) => T
				    tt = mutableOf(context);
				    if (null == at)
				    {   dedtypes.set(i, tt);
						return MATCHconst;
				    }
				}
				else
				{   // foo(U:invariant(U)) T        => nomatch
				    // foo(U:invariant(U)) const(T) => nomatch
				    if (null == at)
						return MATCHnomatch;
				}

				if (tt.equals(at))
				    return MATCHexact;
				else if (tt.ty == Tclass && at.ty == Tclass)
				{
				    return tt.implicitConvTo(at, context);
				}
				else if (tt.ty == Tsarray && at.ty == Tarray &&
				    tt.nextOf().implicitConvTo(at.nextOf(), context).ordinal() >= MATCHconst.ordinal())
				{
				    return MATCHexact;
				}
				else
				    return MATCHnomatch;
			}
		}

		if (ty != tparam.ty) {
			return implicitConvTo(tparam, context);
		}

		if (nextOf() != null) {
			return nextOf().deduceType(sc, tparam.nextOf(), parameters,
					dedtypes, context);
		}

		return MATCHexact;
	}

	public TypeInfoDeclaration getTypeInfoDeclaration(SemanticContext context) {
		return new TypeInfoDeclaration(this, 0, context);
	}

	public boolean builtinTypeInfo(SemanticContext context) {
		return false;
	}

	public static char needThisPrefix() {
		return 'M'; // name mangling prefix for functions needing 'this'
	}

	public Expression getTypeInfo(Scope sc, SemanticContext context) {
		Expression e;
		Type t;

		t = merge2(context); // do this since not all Type's are merge'd

		TypeInfoDeclaration vtinfo = context.getTypeInfo(t);
		if (vtinfo == null) {
			if (context.isD2()) {
				if (t.isConst()) {
				    t.vtinfo = new TypeInfoConstDeclaration(t, context);
				} else if (t.isInvariant()) {
					t.vtinfo = new TypeInfoInvariantDeclaration(t, context);
				} else {
					t.vtinfo = t.getTypeInfoDeclaration(context);
				}
			} else {
				t.vtinfo = t.getTypeInfoDeclaration(context);
			}

			if (t.vtinfo == null) {
				throw new IllegalStateException("assert(t.vtinfo);");
			}

			vtinfo = t.vtinfo;

			/* If this has a custom implementation in std/typeinfo, then
			 * do not generate a COMDAT for it.
			 */
			if (!t.builtinTypeInfo(context)) { // Generate COMDAT
				if (sc != null) // if in semantic() pass
				{ // Find module that will go all the way to an object file
					Module m = sc.module.importedFrom;
					m.members.add(vtinfo);
				} else // if in obj generation pass
				{
					// t.vtinfo.toObjFile();
					Assert.isTrue(false);
				}
			}

			context.setTypeInfo(t, vtinfo);
		}
		e = new VarExp(null, 0, vtinfo);
		e = e.addressOf(sc, context);
		e.type = vtinfo.type; // do this so we don't get redundant dereference
		return e;
	}

	public Expression getInternalTypeInfo(Scope sc, SemanticContext context) {
//		TypeInfoDeclaration tid;
//		Expression e;
		Type t;

		t = toBasetype(context);
		switch (t.ty) {
		case Tsarray:
			t = t.nextOf().arrayOf(context); // convert to corresponding dynamic array type
			break;

		case Tclass:
			if (((TypeClass) t).sym.isInterfaceDeclaration() != null) {
				break;
			}
			// goto Linternal;
			return getInternalTypeInfo_Linternal(sc, t, context);

		case Tarray:
			if (context.isD2()) {
				// convert to corresponding dynamic array type
			    t = t.nextOf().mutableOf(context).arrayOf(context);
			    if (t.nextOf().ty != Tclass) {
			    	break;
			    }
			} else {
				if (t.nextOf().ty != Tclass) {
					break;
				}
			}
			//goto Linternal;
			return getInternalTypeInfo_Linternal(sc, t, context);

		case Tfunction:
		case Tdelegate:
		case Tpointer:
			// Linternal:
			return getInternalTypeInfo_Linternal(sc, t, context);

		default:
			break;
		}
		return t.getTypeInfo(sc, context);
	}

	private Expression getInternalTypeInfo_Linternal(Scope sc, Type t, SemanticContext context) {
		TypeInfoDeclaration tid = context.Type_internalTI[t.ty.ordinal()];
		if (null == tid) {
			tid = new TypeInfoDeclaration(t, 1, context);
			context.Type_internalTI[t.ty.ordinal()] = tid;
		}
		Expression e = new VarExp(null, 0, tid);
		e = e.addressOf(sc, context);
		e.type = tid.type; // do this so we don't get redundant dereference
		return e;
	}

	/************************************
	 * Add MODxxxx bits to existing type.
	 * We're adding, not replacing, so adding const to
	 * a shared type => "shared const"
	 */

	public Type addMod(int mod, SemanticContext context) {
		Type t = this;

		/*
		 * Add anything to immutable, and it remains immutable
		 */
		if (!t.isInvariant()) {
			switch (mod) {
			case 0:
			break;

			case MODconst:
				if (isShared())
					t = sharedConstOf(context);
				else
					t = constOf(context);
			break;

			case MODinvariant:
				t = invariantOf(context);
			break;

			case MODshared:
				if (isConst())
					t = sharedConstOf(context);
				else
					t = sharedOf(context);
			break;

			case MODshared | MODconst:
				t = sharedConstOf(context);
			break;

			default:
				throw new IllegalStateException();
			}
		}
		return t;
	}

	/************************************
	 * Add storage class modifiers to type.
	 */
	public Type addStorageClass(int stc, SemanticContext context) {
		/*
		 * Just translate to MOD bits and let addMod() do the work
		 */
		int mod = 0;

		if ((stc & STCimmutable) != 0)
			mod = MODinvariant;
		else {
			if ((stc & (STCconst | STCin)) != 0)
				mod = MODconst;
			if ((stc & STCshared) != 0)
				mod |= MODshared;
		}
		return addMod(mod, context);
	}

	public Type copy() {
		try {
			return (Type) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**************************
	 * Given:
	 *	T a, b;
	 * Can we assign:
	 *	a = b;
	 * ?
	 * @param context TODO
	 */
	public boolean isAssignable(SemanticContext context) {
	    return true;
	}

	public final String getSignature() {
		return getSignature(ISignatureOptions.Default);
	}

	public final String getSignature(int options) {
		String sig = null;
		if ((options & ISignatureOptions.AliasResolution) != 0 && alias != null) {
			sig = alias.getSignature();
		}
		if (sig == null) {
			StringBuilder sb = new StringBuilder();
			appendSignature(sb, options);
			sig = sb.toString();
		}
		return sig;
	}

	protected final void appendSignature(StringBuilder sb, int options) {
		if ((mod & MODinvariant) != 0) {
			sb.append(Signature.C_IMMUTABLE);
		} else if ((mod & MODconst) != 0) {
			sb.append(Signature.C_CONST);
		}

		if ((options & ISignatureOptions.AliasResolution) != 0
				&& alias != null
				&& !(alias instanceof AliasDeclaration && ((AliasDeclaration) alias).isTemplateParameter)
				&& !(alias instanceof Import)) {
			StringBuilder sb2 = new StringBuilder();
			alias.appendSignature(sb2, options);
			if (sb2.length() > 0) {
				sb.append(sb2);
				return;
			}
		}

		appendSignature0(sb, options);
	}

	protected abstract void appendSignature0(StringBuilder sb, int options);

	/**
	 * Returns the java element associated with this type, if any,
	 * or null. This method returns null, subclasses should implement.
	 */
	public IJavaElement__Marker getJavaElement() {
		return null;
	}

	public char[] identRep() {
		return null;
	}

	public Type castMod(int mod2, SemanticContext context) {
		Type t;

		switch (mod) {
		case 0:
			t = mutableOf(context);
			break;

		case MODconst:
			t = constOf(context);
			break;

		case MODinvariant:
			t = invariantOf(context);
			break;

		case MODshared:
			t = sharedOf(context);
			break;

		case MODshared | MODconst:
			t = sharedConstOf(context);
			break;

		default:
			throw new IllegalSelectorException();
		}
		return t;
	}

}
