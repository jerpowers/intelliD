package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.MATCH.MATCHexact;
import static descent.internal.compiler.parser.MATCH.MATCHnomatch;
import static descent.internal.compiler.parser.TOK.TOKcolon;
import static descent.internal.compiler.parser.TOK.TOKequal;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.core.compiler.IftypeDeclarationKind;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class IftypeCondition extends Condition {

	public Type targ;
	public IdentifierExp id;
	public TOK tok;
	public Type tspec;

	public IftypeCondition(char[] filename, int lineNumber, Type targ, IdentifierExp ident, TOK tok,
			Type tspec) {
		super(filename, lineNumber);
		this.targ = targ;
		this.id = ident;
		this.tok = tok;
		this.tspec = tspec;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, targ);
			TreeVisitor.acceptChildren(visitor, id);
			TreeVisitor.acceptChildren(visitor, tspec);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getConditionType() {
		return IFTYPE;
	}

	public IftypeDeclarationKind getKind() {
		if (tok == TOK.TOKreserved) {
			return IftypeDeclarationKind.NONE;
		}
		if (tok == TOK.TOKcolon) {
			return IftypeDeclarationKind.EXTENDS;
		}
		return IftypeDeclarationKind.EQUALS;
	}

	@Override
	public boolean include(Scope sc, ScopeDsymbol sd, SemanticContext context) {
		if (inc == 0) {
			if (null == sc) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.IftypeConditionCannotBeAtGlobalScope, this));
				}
				inc = 2;
				return false;
			}
			
			boolean condition;
			int errors = 0;
			if (context.isD1()) {
				errors = context.global.errors;
				context.global.gag++; // suppress printing of error messages
				targ = targ.semantic(filename, lineNumber, sc, context);
				context.global.gag--;
				
				condition = errors != context.global.errors;
			} else {
				Type t = targ.trySemantic(filename, lineNumber, sc, context);
				if (t != null)
				    targ = t;
				else
				    inc = 2;			// condition is false
				
				condition = null == t;
			}
			
			if (condition) // if any errors happened
			{
				if (context.isD1()) {
					inc = 2; // then condition is false
					context.global.errors = errors;
				}
			} else if (id != null && tspec != null) {
				/* Evaluate to TRUE if targ matches tspec.
				 * If TRUE, declare id as an alias for the specialized type.
				 */

				MATCH m;
				TemplateTypeParameter tp = new TemplateTypeParameter(filename, lineNumber, id,
						null, null);

				TemplateParameters parameters = new TemplateParameters(1);
				parameters.add(tp);

				Objects dedtypes = new Objects(1);

				m = targ.deduceType(null, tspec, parameters, dedtypes, context);
				if (m == MATCHnomatch || (m != MATCHexact && tok == TOKequal)) {
					inc = 2;
				} else {
					inc = 1;
					Type tded = (Type) dedtypes.get(0);
					if (null == tded) {
						tded = targ;
					}
					Dsymbol s = new AliasDeclaration(filename, lineNumber, id, tded);
					s.semantic(sc, context);
					sc.insert(s);
					if (sd != null) {
						s.addMember(sc, sd, 1, context);
					}
				}
			} else if (id != null) {
				/* Declare id as an alias for type targ. Evaluate to TRUE
				 */
				Dsymbol s = new AliasDeclaration(filename, lineNumber, id, targ);
				s.semantic(sc, context);
				sc.insert(s);
				if (sd != null) {
					s.addMember(sc, sd, 1, context);
				}
				inc = 1;
			} else if (tspec != null) {
				/* Evaluate to TRUE if targ matches tspec
				 */
				tspec = tspec.semantic(filename, lineNumber, sc, context);
				if (tok == TOKcolon) {
					if (targ.implicitConvTo(tspec, context) != MATCHnomatch) {
						inc = 1;
					} else {
						inc = 2;
					}
				} else /* == */
				{
					if (targ.equals(tspec)) {
						inc = 1;
					} else {
						inc = 2;
					}
				}
			} else {
				inc = 1;
			}
		}
		return (inc == 1);
	}

	@Override
	public Condition syntaxCopy(SemanticContext context) {
		return new IftypeCondition(filename, lineNumber, targ.syntaxCopy(context), id, tok,
				tspec != null ? tspec.syntaxCopy(context) : null);
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		buf.writestring("iftype(");
		targ.toCBuffer(buf, id, hgs, context);
		if (tspec != null) {
			if (tok == TOK.TOKcolon) {
				buf.writestring(" : ");
			} else {
				buf.writestring(" == ");
			}
			tspec.toCBuffer(buf, null, hgs, context);
		}
		buf.writeByte(')');
	}

}
