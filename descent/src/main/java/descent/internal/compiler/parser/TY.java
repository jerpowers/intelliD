package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.TypeBasic.TFLAGScomplex;
import static descent.internal.compiler.parser.TypeBasic.TFLAGSfloating;
import static descent.internal.compiler.parser.TypeBasic.TFLAGSimaginary;
import static descent.internal.compiler.parser.TypeBasic.TFLAGSintegral;
import static descent.internal.compiler.parser.TypeBasic.TFLAGSreal;
import static descent.internal.compiler.parser.TypeBasic.TFLAGSunsigned;


public enum TY {
	
    Tarray('A'),		// dynamic array
    Tsarray('G'),		// static array
    Taarray('H'),		// associative array
    Tpointer('P'),
    Treference('R'),
    Tfunction('F'),
    Tident('I'),
    Tclass('C'),
    Tstruct('S'),
    Tenum('E'),
    Ttypedef('T'),
    Tdelegate('D'),

    Tnone('n'),
    Tvoid('v', "void", 0),
    Tint8('g', "byte", TFLAGSintegral),
    Tuns8('h', "ubyte", TFLAGSintegral | TFLAGSunsigned),
    Tint16('s', "short", TFLAGSintegral),
    Tuns16('t', "ushort", TFLAGSintegral | TFLAGSunsigned),
    Tint32('i', "int", TFLAGSintegral),
    Tuns32('k', "uint", TFLAGSintegral | TFLAGSunsigned),
    Tint64('l', "long", TFLAGSintegral),
    Tuns64('m', "ulong", TFLAGSintegral | TFLAGSunsigned),
    Tfloat32('f', "float", TFLAGSfloating | TFLAGSreal),
    Tfloat64('d', "double", TFLAGSfloating | TFLAGSreal),
    Tfloat80('e', "real", TFLAGSfloating | TFLAGSreal),

    Timaginary32('o', "ifloat", TFLAGSfloating | TFLAGSimaginary),
    Timaginary64('p', "idouble", TFLAGSfloating | TFLAGSimaginary),
    Timaginary80('j', "ireal", TFLAGSfloating | TFLAGSimaginary),
    Tcomplex32('q', "cfloat", TFLAGSfloating | TFLAGScomplex),
    Tcomplex64('r', "cdouble", TFLAGSfloating | TFLAGScomplex),
    Tcomplex80('c', "creal", TFLAGSfloating | TFLAGScomplex),

    Tbool('b', "bool", TFLAGSintegral | TFLAGSunsigned),
    Tchar('a', "char", TFLAGSintegral | TFLAGSunsigned),
    Twchar('u', "wchar", TFLAGSintegral | TFLAGSunsigned),
    Tdchar('w', "dchar", TFLAGSintegral | TFLAGSunsigned),
    
    Tbit('@', "bit", TFLAGSintegral | TFLAGSunsigned),
    Tinstance('@'),
    Terror('@'),    
    Ttypeof('@'),
    Ttuple('B'),
    Tslice('@'),
    Treturn('@')
    
    ;
    
    public int flags;
    public String name;
    public final char mangleChar;
    
    TY(char mangleChar) {
    	this.mangleChar = mangleChar;
    }
    
    TY(char mangleChar, String name, int flags) {
    	this.mangleChar = mangleChar;
    	this.name = name;
    	this.flags = flags;
    }
    
    public static TY getBasicType(char c) {
    	switch(c) {
    	case 'v': return Tvoid;
    	case 'g': return Tint8;
    	case 'h': return Tuns8;
    	case 's': return Tint16;
    	case 't': return Tuns16;
    	case 'i': return Tint32;
    	case 'k': return Tuns32;
    	case 'l': return Tint64;
    	case 'm': return Tuns64;
    	case 'f': return Tfloat32;
    	case 'd': return Tfloat64;
    	case 'e': return Tfloat80;
    	case 'o': return Timaginary32;
    	case 'p': return Timaginary64;
    	case 'j': return Timaginary80;
    	case 'q': return Tcomplex32;
    	case 'r': return Tcomplex64;
    	case 'c': return Tcomplex80;
    	case 'b': return Tbool;
    	case 'a': return Tchar;
    	case 'u': return Twchar;
    	case 'w': return Tdchar;
    	default: return null;
    	}
    }

}