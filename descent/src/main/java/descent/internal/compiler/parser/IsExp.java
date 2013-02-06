package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.TOK.TOKequal;
import static descent.internal.compiler.parser.TOK.TOKreserved;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class IsExp extends Expression {

	public Type targ, sourceTarg;
	public IdentifierExp id;
	public TOK tok;
	public Type tspec, sourceTspec;
	public TOK tok2;
	public TemplateParameters parameters;

	public IsExp(char[] filename, int lineNumber, Type targ, IdentifierExp id, TOK tok, Type tspec,
			TOK tok2) {
		this(filename, lineNumber, targ, id, tok, tspec, tok2, null);
	}
	
	public IsExp(char[] filename, int lineNumber, Type targ, IdentifierExp id, TOK tok, Type tspec,
			TOK tok2, TemplateParameters parameters) {
		super(filename, lineNumber, TOK.TOKis);
		this.targ = this.sourceTarg = targ;
		this.id = id;
		this.tok = tok;
		this.tspec = this.sourceTspec = tspec;
		this.tok2 = tok2;
		this.parameters = parameters;
	}

	@Override
	public int getNodeType() {
		return IFTYPE_EXP;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceTarg);
			TreeVisitor.acceptChildren(visitor, id);
			TreeVisitor.acceptChildren(visitor, sourceTspec);
		}
		visitor.endVisit(this);
	}

	@Override
	public Expression semantic(Scope sc, SemanticContext context) {
		Type tded = null;

		if (null != id && ((sc.flags & Scope.SCOPEstaticif) == 0)) {
			if (context.acceptsErrors()) {
				context.acceptProblem(Problem.newSemanticTypeError(
						IProblem.CanOnlyDeclareTypeAliasesWithinStaticIfConditionals, this));
			}
		}
		
	    Type t = targ.trySemantic(filename, lineNumber, sc, context);
	    if (null == t) {
	    	// goto Lno;			// errors, so condition is false
	    	return no(context);
	    }
	    targ = t;
	    if (tok2 != TOK.TOKreserved) {
			switch (tok2) {
			case TOKtypedef:
				if (targ.ty != TY.Ttypedef)
					return no(context);
				tded = ((TypeTypedef) targ).sym.basetype;
				break;

			case TOKstruct:
				if (targ.ty != TY.Tstruct)
					return no(context);
				if (null != ((TypeStruct) targ).sym.isUnionDeclaration())
					return no(context);
				tded = targ;
				break;

			case TOKunion:
				if (targ.ty != TY.Tstruct)
					return no(context);
				if (null == ((TypeStruct) targ).sym.isUnionDeclaration())
					return no(context);
				tded = targ;
				break;

			case TOKclass:
				if (targ.ty != TY.Tclass)
					return no(context);
				if (null != ((TypeClass) targ).sym.isInterfaceDeclaration())
					return no(context);
				tded = targ;
				break;

			case TOKinterface:
				if (targ.ty != TY.Tclass)
					return no(context);
				if (null == ((TypeClass) targ).sym.isInterfaceDeclaration())
					return no(context);
				tded = targ;
				break;
				
			case TOKconst:
				if (!context.isD2()) assert (false);
				
				if (!targ.isConst()) {
					return no(context);
				}
				tded = targ;
				break;
			case TOKinvariant:
			case TOKimmutable:
				if (!context.isD2()) assert (false);
				
				if (!targ.isInvariant()) {
					return no(context);
				}
				tded = targ;
				break;

			case TOKsuper:
				// If class or interface, get the base class and interfaces
				if (targ.ty != TY.Tclass)
					return no(context);
				else {
					ClassDeclaration cd = ((TypeClass) targ).sym;
					Arguments args = new Arguments(cd.baseclasses.size());
					for (int i = 0; i < cd.baseclasses.size(); i++) {
						BaseClass b = (BaseClass) cd.baseclasses.get(i);
						args.add(new Argument(STC.STCin, b.type, null, null));
					}
					tded = TypeTuple.newArguments(args);
				}
				break;

			case TOKenum:
				if (targ.ty != TY.Tenum)
					return no(context);
				tded = ((TypeEnum) targ).sym.memtype;
				break;

			case TOKdelegate:
				if (targ.ty != TY.Tdelegate)
					return no(context);
				tded = ((TypeDelegate)targ).next; // the underlying function type
				break;

			case TOKfunction: {
				if (targ.ty != TY.Tfunction)
					return no(context);
				tded = targ;

				/* Generate tuple from function parameter types.
				 */
				assert (tded.ty == TY.Tfunction);
				Arguments params = ((TypeFunction) tded).parameters;
				int dim = params.size();
				Arguments args = new Arguments(dim);
				for (int i = 0; i < dim; i++) {
					Argument arg = params.get(i);
					assert (null != arg && null != arg.type);
					args.add(new Argument(arg.storageClass, arg.type, null,
							null));
				}
				tded = TypeTuple.newArguments(args);
				break;
			}

			case TOKreturn:
				/* Get the 'return type' for the function,
				 * delegate, or pointer to function.
				 */
				if (targ.ty == TY.Tfunction)
					tded = ((TypeFunction)targ).next;
				else if (targ.ty == TY.Tdelegate)
					tded = targ.nextOf().nextOf();
				else if (targ.ty == TY.Tpointer && targ.nextOf().ty == TY.Tfunction)
					tded = targ.nextOf().nextOf();
				else
					return no(context);
				break;

			default:
				assert (false);
			}

			return yes(tded, sc, context); // goto Lyes;
		} else if (null != id && null != tspec) {
			/* Evaluate to TRUE if targ matches tspec.
			 * If TRUE, declare id as an alias for the specialized type.
			 */
			if (context.isD2()) {
				MATCH m;

				Objects dedtypes = new Objects(size(parameters));
				dedtypes.setDim(size(parameters));
				dedtypes.zero();

				m = targ.deduceType(null, tspec, parameters, dedtypes, context);
				if (m == MATCHnomatch || (m != MATCHexact && tok == TOKequal)) {
					return no(context);
				} else {
					tded = (Type) dedtypes.get(0);
					if (null == tded) {
						tded = targ;
					}

					Objects tiargs = new Objects(1);
					tiargs.setDim(1);
					tiargs.set(0, targ);

					for (int i = 1; i < size(parameters); i++) {
						TemplateParameter tp = (TemplateParameter) parameters
								.get(i);
						Declaration[] s = { null };

						m = tp.matchArg(sc, tiargs, i, parameters, dedtypes, s,
								context);
						if (m == MATCHnomatch) {
							return no(context);
						}
						s[0].semantic(sc, context);
						if (null == sc.insert(s[0])) {
							if (context.acceptsErrors()) {
								context.acceptProblem(Problem.newSemanticTypeError(
														IProblem.DeclarationIsAlreadyDefined,
														s[0],
														s[0].toChars(context)));
							}
						}
						if (sc.sd != null)
							s[0].addMember(sc, sc.sd, 1, context);
					}

					return yes(tded, sc, context);
				}
			} else {
				MATCH m;
				TemplateTypeParameter tp = new TemplateTypeParameter(filename, lineNumber, id, null,
						null);
	
				TemplateParameters parameters = new TemplateParameters(1);
				parameters.add(tp);
	
				Objects dedtypes = new Objects(1);
	
				m = targ.deduceType(null, tspec, parameters, dedtypes, context);
				if (m == MATCH.MATCHnomatch
						|| (m != MATCH.MATCHexact && tok == TOK.TOKequal)) {
					return no(context);
				} else {
					assert (dedtypes.size() == 1);
					tded = (Type) dedtypes.get(0);
					if (null == tded)
						tded = targ;
					return yes(tded, sc, context); // goto Lyes;
				}
			}
		} else if (null != id) {
			/* Declare id as an alias for type targ. Evaluate to TRUE
			 */
			tded = targ;
			return yes(tded, sc, context); // goto Lyes;
		} else if (null != tspec) {
			/* Evaluate to TRUE if targ matches tspec
			 */
			tspec = tspec.semantic(filename, lineNumber, sc, context);
			if (tok == TOK.TOKcolon) {
				if (targ.implicitConvTo(tspec, context) != MATCH.MATCHnomatch)
					return yes(tded, sc, context); // goto Lyes;
				else
					return no(context);
			} else /* == */
			{
				if (targ.equals(tspec))
					return yes(tded, sc, context); // goto Lyes;
				else
					return no(context);
			}
		}

		return yes(tded, sc, context);
	}

	 // Lno;
	private Expression no(SemanticContext context) {
		if (context.isD2()) {
			return new IntegerExp(null, 0, 0, Type.tbool);
		} else {
			return new IntegerExp(null, 0, 0);
		}
	}

	// Lyes:
	private Expression yes(Type tded, Scope sc, SemanticContext context) {
		if (null != id) {
			Dsymbol s = new AliasDeclaration(filename, lineNumber, id, tded);
			s.semantic(sc, context);
			if (context.isD2()) {
				if (null == sc.insert(s)) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
									IProblem.DeclarationIsAlreadyDefined,
									s, s.toChars(context)));
					}
				}
			} else {
				sc.insert(s);
			}
			if (null != sc.sd)
				s.addMember(sc, sc.sd, 1, context);
		}
		if (context.isD2()) {
			return new IntegerExp(null, 0, 1, Type.tbool);
		} else {
			return new IntegerExp(null, 0, 1);
		}
	}

	@Override
	public Expression syntaxCopy(SemanticContext context) {
		TemplateParameters p = null;
		
		if (context.isD2()) {
			// This section is identical to that in TemplateDeclaration::syntaxCopy()			
			if (parameters != null) {
				p = new TemplateParameters(parameters.size());
				p.setDim(parameters.size());
				for (int i = 0; i < size(p); i++) {
					TemplateParameter tp = (TemplateParameter) parameters
							.get(i);
					p.set(i, tp.syntaxCopy(context));
				}
			}
		}
		
		return new IsExp(filename, lineNumber, targ.syntaxCopy(context), id, tok,
				null != tspec ? tspec.syntaxCopy(context) : null, tok2, p);
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("is(");
		targ.toCBuffer(buf, id, hgs, context);
		if (tok2 != TOKreserved) {
			buf.data.append(' ');
			buf.data.append(tok.toString());
			buf.data.append(' ');
			buf.data.append(tok2.toString());
		} else if (null != tspec) {
			if (tok == TOK.TOKcolon)
				buf.writestring(" : ");
			else
				buf.writestring(" == ");
			tspec.toCBuffer(buf, null, hgs, context);
		}

		if (context.isD2()) {
			if (parameters != null) { 
				// First parameter is already output, so start with second
				for (int i = 1; i < size(parameters); i++) {
					buf.writeByte(',');
					TemplateParameter tp = parameters.get(i);
					tp.toCBuffer(buf, hgs, context);
				}
			}
		}

		buf.writeByte(')');
	}

}
