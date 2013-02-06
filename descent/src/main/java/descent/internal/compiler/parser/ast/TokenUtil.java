package descent.internal.compiler.parser.ast;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;

import java.util.HashSet;

import melnorme.utilbox.misc.ArrayUtil;
import melnorme.utilbox.misc.CollectionUtil;
import descent.internal.compiler.parser.TOK;

public class TokenUtil {
	public static final TOK[] operators = { 
		TOK.TOKlt,      TOK.TOKgt, 
		TOK.TOKle,      TOK.TOKge, 
		TOK.TOKequal,   TOK.TOKnotequal, 
		TOK.TOKidentity,TOK.TOKnotidentity, 
		TOK.TOKindex,   
		TOK.TOKtobool, 
		
		TOK.TOKneg,
		
		// NCEG floating point compares 
		// !<>=     <>    <>=    !>     !>=   !<     !<=   !<> 
		TOK.TOKunord,TOK.TOKlg,TOK.TOKleg,TOK.TOKule,TOK.TOKul,TOK.TOKuge,TOK.TOKug,TOK.TOKue, 
		
		TOK.TOKshl,     TOK.TOKshr, 
		TOK.TOKshlass,  TOK.TOKshrass, 
		TOK.TOKushr,    TOK.TOKushrass, 
		TOK.TOKcat,     TOK.TOKcatass,   
		TOK.TOKadd,     TOK.TOKmin,     TOK.TOKaddass,  TOK.TOKminass, 
		TOK.TOKmul,     TOK.TOKdiv,     TOK.TOKmod, 
		TOK.TOKmulass,  TOK.TOKdivass,  TOK.TOKmodass, 
		TOK.TOKand,     TOK.TOKor,      TOK.TOKxor, 
		TOK.TOKandass,  TOK.TOKorass,   TOK.TOKxorass, 
		TOK.TOKassign,  TOK.TOKnot,     TOK.TOKtilde, 
		TOK.TOKplusplus,    TOK.TOKminusminus, 
		TOK.TOKdot,         TOK.TOKcomma, 
		TOK.TOKquestion,    TOK.TOKandand,  TOK.TOKoror,
		
		TOK.TOKdollar,
	}; 
	
	
    public static final TOK[] specialNamedLiterals = {
        TOK.TOKnull,     
        TOK.TOKtrue,    TOK.TOKfalse, 
        TOK.TOKthis,    TOK.TOKsuper,
         
    }; 
    
	
	public static final TOK[] literals = {
		
		// Other Literals 
		TOK.TOKnull,     
		TOK.TOKtrue,    TOK.TOKfalse, 
		TOK.TOKthis,    TOK.TOKsuper,
		
		// Numeric literals 
		TOK.TOKint32v, TOK.TOKuns32v, 
		TOK.TOKint64v, TOK.TOKuns64v, 
		TOK.TOKfloat32v, TOK.TOKfloat64v, TOK.TOKfloat80v, 
		TOK.TOKimaginary32v, TOK.TOKimaginary64v, TOK.TOKimaginary80v, 
		
		// Char constants 
		TOK.TOKcharv, TOK.TOKwcharv, TOK.TOKdcharv, 
	}; 
	
	public static final TOK[] keywords = {
		TOK.TOKnotis, TOK.TOKis, // Operator keywords 
		
		TOK.TOKcast, 
		TOK.TOKnull,
		TOK.TOKassert, 
		TOK.TOKthrow, 
		TOK.TOKnew, 
		TOK.TOKdelete,
		TOK.TOKversion,
		TOK.TOKtypeof,
		TOK.TOKpragma,
		TOK.TOKtypeid, 
		
		TOK.TOKimport,
		
		TOK.TOKmodule, 
		TOK.TOKtemplate, 
		
		TOK.TOKstruct,
		TOK.TOKclass, 
		TOK.TOKinterface,
		TOK.TOKunion, 
		TOK.TOKenum,
		TOK.TOKtypedef,
		TOK.TOKalias, 
		
		TOK.TOKmixin,
		
		
		TOK.TOKfinal,
		TOK.TOKconst, 
		
		TOK.TOKoverride,
		TOK.TOKdelegate, 
		TOK.TOKfunction,
		
		TOK.TOKalign,
		TOK.TOKextern, 
		TOK.TOKprivate,
		TOK.TOKprotected, 
		TOK.TOKpublic,
		TOK.TOKexport, 
		TOK.TOKstatic, 
		// TOK.TOKvirtual,
		TOK.TOKabstract,
		TOK.TOKvolatile, 
		TOK.TOKdebug,
		TOK.TOKdeprecated, 
		TOK.TOKin, 
		TOK.TOKout, 
		TOK.TOKinout,
		TOK.TOKlazy, 
		TOK.TOKauto, 
		TOK.TOKpackage,
		
		// Statements
		TOK.TOKif,
		TOK.TOKelse, 
		TOK.TOKwhile,
		TOK.TOKfor, 
		TOK.TOKdo,
		TOK.TOKswitch, 
		TOK.TOKcase,
		TOK.TOKdefault, 
		TOK.TOKbreak,
		TOK.TOKcontinue, 
		TOK.TOKwith, 
		TOK.TOKsynchronized, 
		TOK.TOKreturn,
		TOK.TOKgoto, 
		TOK.TOKtry,
		TOK.TOKcatch, 
		TOK.TOKfinally,
		TOK.TOKasm, 
		TOK.TOKforeach,
		TOK.TOKforeach_reverse, 
		TOK.TOKscope, 
		TOK.TOKon_scope_exit, 
		TOK.TOKon_scope_failure,
		TOK.TOKon_scope_success,
		
		// Contracts
		TOK.TOKbody, 
		TOK.TOKinvariant,
		
		// Other stuff
		TOK.TOKtraits,
		TOK.TOKoverloadset,
		
		// D2 new type system keywords
		TOK.TOKnothrow,
		TOK.TOKpure,
		TOK.TOKshared,
		TOK.TOKimmutable,
		
		// Testing
		TOK.TOKunittest,
		
		TOK.TOKref,
		TOK.TOKmacro,
		
	};
	
	
	public static final TOK[] basicTypes = { 
		TOK.TOKvoid, 
		TOK.TOKint8, TOK.TOKuns8, 
		TOK.TOKint16, TOK.TOKuns16, 
		TOK.TOKint32, TOK.TOKuns32, 
		TOK.TOKint64, TOK.TOKuns64, 
		TOK.TOKfloat32, TOK.TOKfloat64, TOK.TOKfloat80, 
		TOK.TOKimaginary32, TOK.TOKimaginary64, TOK.TOKimaginary80, 
		TOK.TOKcomplex32, TOK.TOKcomplex64, TOK.TOKcomplex80, 
		TOK.TOKchar, TOK.TOKwchar, TOK.TOKdchar, TOK.TOKbit, TOK.TOKbool, 
		TOK.TOKcent, TOK.TOKucent, 
	};
	
	public static final TOK[] whiteSpace = { 
		TOK.TOKwhitespace,
		TOK.TOKblockcomment,
		TOK.TOKpluscomment,
		TOK.TOKlinecomment,
		TOK.TOKdocblockcomment,
		TOK.TOKdocpluscomment,
		TOK.TOKdoclinecomment,
	};
	
	protected static final TOK[] internalTokens = { 
		TOK.TOKdottype,
		TOK.TOKdotvar,
		TOK.TOKcall,
		TOK.TOKsymoff,
		TOK.TOKarrayliteral,
		TOK.TOKarray,
		TOK.TOKvar,
		TOK.TOKarraylength,
		TOK.TOKrbracket,
		TOK.TOKaddress,
		TOK.TOKrcurly,
		TOK.TOKdotti,
		TOK.TOKstar,
		TOK.TOKremove,
		TOK.TOKdotexp,
		TOK.TOKlcurly,
		TOK.TOKiftype,
		TOK.TOKeof,
		TOK.TOKreserved,
		TOK.TOKstring,
		TOK.TOKsemicolon,
		TOK.TOKlparen,
		TOK.TOKlbracket,
		TOK.TOKidentifier,
		TOK.TOKdsymbol,
		TOK.TOKtype,
		TOK.TOKuadd,
		TOK.TOKdottd,
		TOK.TOKtypedot,
		TOK.TOKrparen,
		TOK.TOKslice,
		TOK.TOKcolon,
		TOK.TOKhalt,
		TOK.TOKPRAGMA,
		TOK.TOKdotdotdot,
		TOK.TOKdeclaration,
		TOK.TOKnewanonclass,
		TOK.TOKtuple,
		
		/*-------------------*/
		
		TOK.TOKassocarrayliteral,
		TOK.TOKstructliteral,
		
		TOK.TOKnotis,
		TOK.TOKconstruct,
		TOK.TOKblit,
		
		TOK.TOKtls, TOK.TOKgshared,
		TOK.TOKline,
		TOK.TOKfile,
	};
	
	
	static {
		HashSet<TOK> tokens = CollectionUtil.createHashSet(TOK.values());
		tokens.removeAll(CollectionUtil.createHashSet(operators));
		tokens.removeAll(CollectionUtil.createHashSet(specialNamedLiterals));
		tokens.removeAll(CollectionUtil.createHashSet(literals));
		tokens.removeAll(CollectionUtil.createHashSet(keywords));
		tokens.removeAll(CollectionUtil.createHashSet(basicTypes));
		tokens.removeAll(CollectionUtil.createHashSet(whiteSpace));
		tokens.removeAll(CollectionUtil.createHashSet(internalTokens));
		for (TOK tok : tokens) {
			System.out.println(tok.name());
		}
		assertTrue(tokens.isEmpty()); // Ensure we have all tokens accounted for
	}
	
	public static boolean isLiteral(TOK tok) {
		return ArrayUtil.contains(literals, tok);
	} 
	
	public static boolean isBasicType(TOK tok) {
		return ArrayUtil.contains(basicTypes, tok);
	} 
	
	public static boolean isOperator(TOK tok) {
		return ArrayUtil.contains(operators, tok);
	} 
	
	public static boolean isKeyword(TOK tok) {
		return ArrayUtil.contains(keywords, tok);
	} 
	
	/** Return whether the token is semantically ignorable (comments, whitespace).*/
	public static boolean isWhiteToken(TOK tok) {
		return tok == TOK.TOKwhitespace
		|| tok == TOK.TOKblockcomment
		|| tok == TOK.TOKpluscomment
		|| tok == TOK.TOKlinecomment
		|| tok == TOK.TOKdocblockcomment
		|| tok == TOK.TOKdocpluscomment
		|| tok == TOK.TOKdoclinecomment;
	}
	
	
	public static boolean isDDocComment(TOK tok) {
		return tok == TOK.TOKdocblockcomment
		|| tok == TOK.TOKdocpluscomment
		|| tok == TOK.TOKdoclinecomment;
	}
	
	public static boolean isSimpleComment(TOK tok) {
		return tok == TOK.TOKblockcomment
		|| tok == TOK.TOKpluscomment
		|| tok == TOK.TOKlinecomment;
	}
	
}
