package descent.internal.compiler.parser;

import descent.core.compiler.Linkage;


public enum LINK {
	
    LINKdefault,
    LINKd('F'),
    LINKc('U'),
    LINKcpp('R'),
    LINKwindows('W'),
    LINKpascal('V'),
    LINKsystem;
    
    public char mangleChar;
    
    private LINK() {
    	
    }
    
    private LINK(char mangleChar) {
    	this.mangleChar = mangleChar;
    }
    
    public Linkage getLinkage() {
    	switch(this) {
    	case LINKdefault: return Linkage.DEFAULT;
    	case LINKd: return Linkage.D;
    	case LINKc: return Linkage.C;
    	case LINKcpp: return Linkage.CPP;
    	case LINKwindows: return Linkage.WINDOWS;
    	case LINKpascal: return Linkage.PASCAL;
    	case LINKsystem: return Linkage.SYSTEM;
    	default: throw new RuntimeException("Can't happen?");
    	}
    }

}
