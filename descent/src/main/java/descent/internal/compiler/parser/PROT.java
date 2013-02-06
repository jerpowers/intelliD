package descent.internal.compiler.parser;


public enum PROT {
	
	PROTundefined(0),
    PROTnone(1),		// no access
    PROTprivate(2),
    PROTpackage(3),
    PROTprotected(4),
    PROTpublic(5),
    PROTexport(6),
    
    ;
	
	public int level;
	PROT(int level) {
		this.level = level;
	}
	
	public static PROT fromTOK(TOK tok) {
		switch(tok) {
		case TOKprivate: return PROTprivate;
		case TOKpackage: return PROTpackage;
		case TOKprotected: return PROTprotected;
		case TOKpublic: return PROTpublic;
		case TOKexport: return PROTexport;
		}
		return PROTundefined;
	}

}
