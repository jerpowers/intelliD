package descent.internal.compiler.parser;


public class STC {
	
	public final static int STCundefined    = 0;
	public final static int STCstatic	    = 1;
	public final static int STCextern	    = 2;
	public final static int STCconst	    = 4;
	public final static int STCfinal	    = 8;
	public final static int STCabstract     = 0x10;
	public final static int STCparameter    = 0x20;
	public final static int STCfield	    = 0x40;
	public final static int STCoverride	    = 0x80;
	public final static int STCauto         = 0x100;
	public final static int STCsynchronized = 0x200;
	public final static int STCdeprecated   = 0x400;
	public final static int STCin           = 0x800;		// in parameter
	public final static int STCout          = 0x1000;		// out parameter
	public final static int STClazy	    = 0x2000;		// lazy parameter
	public final static int STCforeach      = 0x4000;		// variable for foreach loop
	public final static int STCcomdat       = 0x8000;		// should go into COMDAT record
	public final static int STCvariadic     = 0x10000;		// variadic function argument
	public final static int STCctorinit     = 0x20000;		// can only be set inside constructor
	public final static int STCtemplateparameter = 0x40000;	// template parameter
	public final static int STCscope	    = 0x80000;		// template parameter
	public final static int STCinvariant	= 0x100000;
	public final static int STCimmutable	= 0x100000;
	public final static int STCref	    = 0x200000;
	public final static int STCinit	    = 0x400000;		// has explicit initializer
	public final static int STCmanifest	    = 0x800000;		// manifest constant
	public final static int STCnodtor	    = 0x1000000;	// don't run destructor
	public final static int STCnothrow	    = 0x2000000;	// never throws exceptions
	public final static int STCpure	    = 0x4000000;	// pure function
	public final static int STCtls	    = 0x8000000;	// thread local
	public final static int STCalias	    = 0x10000000;	// alias parameter
	public final static int STCshared	    = 0x20000000;	// accessible from multiple threads
    public final static int STCgshared      = 0x40000000;	// accessible from multiple threads
    public final static int STC_TYPECTOR    = (STCconst | STCimmutable | STCshared);

	public static int fromTOK(TOK tok) {
		switch(tok) {
		case TOKstatic: return STCstatic;
		case TOKconst: return STCconst;
		case TOKfinal: return STCfinal;
		case TOKauto: return STCauto;
		case TOKscope: return STCscope;
		case TOKoverride: return STCoverride;
		case TOKabstract: return STCabstract;
		case TOKsynchronized: return STCsynchronized;
		case TOKdeprecated: return STCdeprecated;
		case TOKextern: return STCextern;
		case TOKinvariant: return STCinvariant;
		case TOKref: return STCref;
		case TOKnothrow: return STCnothrow;
		case TOKpure: return STCpure;
		case TOKenum: return STCmanifest;
		case TOKtls: return STCtls;
		case TOKshared: return STCshared;
		case TOKgshared: return STCgshared;
		case TOKimmutable: return STCimmutable;
		}
		throw new IllegalStateException();
	}

}
