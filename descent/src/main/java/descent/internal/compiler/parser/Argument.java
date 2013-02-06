package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCalias;
import static descent.internal.compiler.parser.STC.STCauto;
import static descent.internal.compiler.parser.STC.STCconst;
import static descent.internal.compiler.parser.STC.STCimmutable;
import static descent.internal.compiler.parser.STC.STCin;
import static descent.internal.compiler.parser.STC.STClazy;
import static descent.internal.compiler.parser.STC.STCout;
import static descent.internal.compiler.parser.STC.STCref;
import static descent.internal.compiler.parser.STC.STCscope;
import static descent.internal.compiler.parser.STC.STCshared;
import static descent.internal.compiler.parser.STC.STCstatic;
import static descent.internal.compiler.parser.TY.Tarray;
import static descent.internal.compiler.parser.TY.Tdelegate;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.Type.MODshared;
import melnorme.utilbox.tree.TreeVisitor;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class Argument extends ASTDmdNode implements Cloneable {

	public static void argsToCBuffer(OutBuffer buf, HdrGenState hgs,
			Arguments arguments, int varargs, SemanticContext context) {
		buf.writeByte('(');
		if (arguments != null) {
			int i;
			OutBuffer argbuf = new OutBuffer();

			for (i = 0; i < arguments.size(); i++) {
				Argument arg;

				if (i != 0) {
					buf.writestring(", ");
				}
				arg = arguments.get(i);
				if ((arg.storageClass & STCout) != 0) {
					buf.writestring("out ");
				} else if ((arg.storageClass & STCref) != 0) {
					buf.writestring((context.global.params.Dversion == 1) ? "inout " : "ref ");
				} else if (context.isD2() && (arg.storageClass & STCin) != 0) {
					buf.writestring("in ");
				} else if ((arg.storageClass & STClazy) != 0) {
					buf.writestring("lazy ");
				} else if (context.isD2() && (arg.storageClass & STCalias) != 0) {
					buf.writestring("alias ");
				} else if (context.isD2() && (arg.storageClass & STCauto) != 0) {
					buf.writestring("auto ");
				}
				
				if (context.isD2()) {
					int stc = arg.storageClass;
					if (arg.type != null && (arg.type.mod & MODshared) != 0) {
						stc &= ~STCshared;
					}

					StorageClassDeclaration.stcToCBuffer(buf, (stc & (STCconst | STCimmutable | STCshared | STCscope)), context);
				}
				
				argbuf.reset();
				
				if (context.isD2() && (arg.storageClass & STCalias) != 0) {
					if (arg.ident != null)
						argbuf.writestring(arg.ident.toChars());
				} else {
					arg.type.toCBuffer(argbuf, arg.ident, hgs, context);
				}
				if (arg.defaultArg != null) {
					argbuf.writestring(" = ");
					arg.defaultArg.toCBuffer(argbuf, hgs, context);
				}
				buf.write(argbuf);
			}
			if (varargs != 0) {
				if (i != 0 && varargs == 1) {
					buf.writeByte(',');
				}
				buf.writestring("...");
			}
		}
		buf.writeByte(')');
	}

	public static void argsToDecoBuffer(OutBuffer buf, Arguments arguments,
			SemanticContext context) {
		// Write argument types
		if (arguments != null) {
			int dim = Argument.dim(arguments, context);
			for (int i = 0; i < dim; i++) {
				Argument arg = Argument.getNth(arguments, i, context);
				arg.toDecoBuffer(buf, context);
			}
		}
	}

	public static String argsTypesToChars(Arguments args, int varargs,
			SemanticContext context) {
		OutBuffer buf = new OutBuffer();
		
		if (context.isD1()) {
		    HdrGenState hgs = new HdrGenState();
		    argsToCBuffer(buf, hgs, args, varargs, context);
		} else {
			buf.writeByte('(');
			if (args != null) {
				int i;
				OutBuffer argbuf = new OutBuffer();
				HdrGenState hgs = new HdrGenState();
	
				for (i = 0; i < args.size(); i++) {
					Argument arg;
	
					if (i != 0) {
						buf.writeByte(',');
					}
					arg = args.get(i);
					argbuf.reset();
					arg.type.toCBuffer2(argbuf, hgs, 0, context);
					buf.write(argbuf);
				}
				if (varargs != 0) {
					if (i != 0 && varargs == 1) {
						buf.writeByte(',');
					}
					buf.writestring("...");
				}
			}
			buf.writeByte(')');
		}

		return buf.toChars();
	}

	public static Arguments arraySyntaxCopy(Arguments args, SemanticContext context) {
		Arguments a = null;

		if (args != null) {
			a = new Arguments(args.size());
			a.setDim(args.size());
			for (int i = 0; i < a.size(); i++) {
				Argument arg = args.get(i);
				arg = arg.syntaxCopy(context);
				a.set(i, arg);
			}
		}
		return a;
	}

	public static int dim(Arguments args, SemanticContext context) {
		int n = 0;
		if (args != null) {
			for(int i = 0; i < size(args); i++) {
				Argument arg = args.get(i);
				Type t = arg.type.toBasetype(context);

				if (t.ty == TY.Ttuple) {
					TypeTuple tu = (TypeTuple) t;
					n += dim(tu.arguments, context);
				} else {
					n++;
				}
			}
		}
		return n;
	}

	public static Argument getNth(Arguments args, int nth, int[] pn,
			SemanticContext context) {
		if (args == null) {
			return null;
		}

		int n = 0;
		for(int i = 0; i < size(args); i++) {
			Argument arg = args.get(i);
			Type t = arg.type.toBasetype(context);

			if (t.ty == TY.Ttuple) {
				TypeTuple tu = (TypeTuple) t;
				int[] _pn = { n };
				arg = getNth(tu.arguments, nth - n, _pn, context);
				n = _pn[0];
				if (arg != null) {
					return arg;
				}
			} else if (n == nth) {
				return arg;
			} else {
				n++;
			}
		}

		if (pn != null) {
			pn[0] += n;
		}
		return null;
	}

	public static Argument getNth(Arguments args, int nth,
			SemanticContext context) {
		return getNth(args, nth, null, context);
	}

	public int storageClass;
	public Type type;
	public Type sourceType;
	public IdentifierExp ident;
	public Expression defaultArg, sourceDefaultArg;
	public TypeFunction parentType; // Added field to keep track of the argument's parent
	
	/*
	 * The variable, alias or typedef 
	 * declaration this argument generated. Useful for bindings.
	 */
	public Dsymbol var;

	public Argument(int storageClass, Type type, IdentifierExp ident,
			Expression defaultArg) {
		this.storageClass = storageClass;
		this.type = type;
		this.sourceType = type;
		this.ident = ident;
		this.defaultArg = defaultArg;
		this.sourceDefaultArg = defaultArg;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceType);
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, sourceDefaultArg);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return ARGUMENT;
	}

	public Type isLazyArray(SemanticContext context) {
		{
			Type tb = type.toBasetype(context);
			if (tb.ty == Tsarray || tb.ty == Tarray) {
				Type tel = tb.nextOf().toBasetype(context);
				if (tel.ty == Tdelegate) {
					TypeDelegate td = (TypeDelegate) tel;
					TypeFunction tf = (TypeFunction) td.next;

					if (0 == tf.varargs
							&& Argument.dim(tf.parameters, context) == 0) {
						return tf.next; // return type of delegate
					}
				}
			}
		}
		return null;
	}
	
	/****************************************
	 * Determine if parameter list is really a template parameter list
	 * (i.e. it has auto or alias parameters)
	 */
	public boolean isTPL(Arguments arguments, SemanticContext context) {
		if (arguments != null) {
			int dim = Argument.dim(arguments, context);
			for (int i = 0; i < dim; i++) {
				Argument arg = Argument.getNth(arguments, i, context);
				if ((arg.storageClass & (STCalias | STCauto | STCstatic)) != 0) {
					return true;
				}
			}
		}
		return false;
	}

	public Argument syntaxCopy(SemanticContext context) {
		Argument a = new Argument(storageClass, type != null ? type
				.syntaxCopy(context) : null, ident, defaultArg != null ? defaultArg
				.syntaxCopy(context) : null);
		return a;
	}

	public void toDecoBuffer(OutBuffer buf, SemanticContext context) {
		if (context.isD2()) {
		    if ((storageClass & STCscope) != 0)
		    	buf.writeByte('M');
		}
		
		switch (storageClass & (STCin | STCout | STCref | STClazy)) {
		case 0:
		case STCin:
			break;
		case STCout:
			buf.writeByte('J');
			break;
		case STCref:
			buf.writeByte('K');
			break;
		case STClazy:
			buf.writeByte('L');
			break;
		default:
			throw new IllegalStateException("assert(0);");
		}
		type.toDecoBuffer(buf, 0, context);
	}
	
	public Argument copy() {
		try {
			return (Argument) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public String getSignature() {
		return getSignature(ISignatureOptions.Default);
	}
	
	public String getSignature(int options) {
		StringBuilder sb = new StringBuilder();
		appendSignature(sb, options);
		return sb.toString();
	}
	
	public void appendSignature(StringBuilder sb, int options) {
		switch (storageClass & (STCin | STCout | STCref | STClazy)) {
		case 0:
		case STCin:
			break;
		case STCout:
			sb.append('J');
			break;
		case STCref:
			sb.append('K');
			break;
		case STClazy:
			sb.append('L');
			break;
		}
		type.appendSignature(sb, options);
	}

}
