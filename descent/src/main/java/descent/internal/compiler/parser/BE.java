package descent.internal.compiler.parser;

/* 
 * How a statement exits
 */
public class BE {
    public final static int BEnone =	 0;
    public final static int BEfallthru = 1;
    public final static int BEthrow =    2;
    public final static int BEreturn =   4;
    public final static int BEgoto =     8;
    public final static int BEhalt =	 0x10;
    public final static int BEbreak =	 0x20;
    public final static int BEcontinue = 0x40;
    public final static int BEany = (BEfallthru | BEthrow | BEreturn | BEgoto | BEhalt);
}
