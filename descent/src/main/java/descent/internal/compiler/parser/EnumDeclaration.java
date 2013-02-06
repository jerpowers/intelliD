package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.STC.STCdeprecated;

import java.math.BigInteger;

import melnorme.utilbox.tree.TreeVisitor;
import descent.core.Flags;
import descent.core.IType__Marker;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class EnumDeclaration extends ScopeDsymbol {

	private final static int N_2 = 2;
	private final static int N_128 = 128;
	private final static int N_256 = 256;
	private final static int N_0x8000 = 0x8000;
	private final static int N_0x10000 = 0x10000;
	private final static long N_0x80000000 = 0x80000000L;
	private final static long N_0x100000000 = 0x100000000L;
	private final static long N_0x8000000000000000 = 0x8000000000000000L;

	public Type type; // the TypeEnum
	public Type memtype, sourceMemtype; // type of the members
	
	// These three are of type:
	//  - integer_t, if version < 2
	//  - Expression, if version >= 2
	public Object maxval;
	public Object minval;
	public Object defaultval; // default initializer
	public Scope scope;
	public boolean isdeprecated;
	
	private IType__Marker javaElement;
	
	public EnumDeclaration(char[] filename, int lineNumber, IdentifierExp id, Type memtype) {
		super(id);
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.type = new TypeEnum(this);
		this.memtype = this.sourceMemtype = memtype;
		this.maxval = integer_t.ZERO;
		this.minval = integer_t.ZERO;
		this.defaultval = integer_t.ZERO;
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, memtype);
			TreeVisitor.acceptChildren(visitor, members);
			
//			acceptSynthetic(visitor);
		}
		visitor.endVisit(this);
	}

	@Override
	public int getNodeType() {
		return ENUM_DECLARATION;
	}

	@Override
	public Type getType(SemanticContext context) {
		return type;
	}
	
	@Override
	public boolean isDeprecated() {
		return isdeprecated;
	}

	@Override
	public EnumDeclaration isEnumDeclaration() {
		return this;
	}

	@Override
	public String kind() {
		return "enum";
	}

	@Override
	public boolean oneMember(Dsymbol[] ps, SemanticContext context) {
		if (isAnonymous()) {
			return super.oneMembers(members, ps, context);
		}
		return super.oneMember(ps, context);
	}
	
	@Override
	public Dsymbol search(char[] filename, int lineNumber, char[] ident, int flags, SemanticContext context) {
		if (context.isD2()) {
		    if (scope != null) {
				// Try one last time to resolve this enum
				semantic(scope, context);
			}

			if (null == members || null == symtab || scope != null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(IProblem.ForwardReferenceWhenLookingFor, this, new String(this.ident.ident), new String(ident)));
				}
				return null;
			}

			Dsymbol s = super.search(filename, lineNumber, ident, flags, context);
			return s;
		} else {
			return super.search(filename, lineNumber, ident, flags, context);
		}
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		// Better to make a big branch, semantic is totally different
		// before D2 and after it
		if (context.isD2()) {
			Type t = null;
			Scope sce;

			if (null == members) { // enum ident;
				return;
			}

			if (null == memtype && !isAnonymous()) { 
				// Set memtype if we can to reduce fwd reference errors
				memtype = Type.tint32; // case 1) enum ident { ... }
			}

			if (symtab != null) // if already done
			{
				if (null == scope) {
					return; // semantic() already completed
				}
			} else {
				symtab = new DsymbolTable();
			}

			Scope scx = null;
			if (scope != null) {
				sc = scope;
				scx = scope; // save so we don't make redundant copies
				scope = null;
			}
			
		    if ((sc.stc & STCdeprecated) != 0)
		    	isdeprecated = true;

			parent = sc.parent;

			/*
			 * The separate, and distinct, cases are: 
			 * 1. enum { ... } 
			 * 2. enum : memtype { ... } 
			 * 3. enum ident { ... } 
			 * 4. enum ident : memtype { ... }
			 */

			if (memtype != null) {
				memtype = memtype.semantic(filename, lineNumber, sc, context);

				/*
				 * Check to see if memtype is forward referenced
				 */
				if (memtype.ty == TY.Tenum) {
					EnumDeclaration sym = (EnumDeclaration) memtype.toDsymbol(
							sc, context);
					if (null == sym.memtype || null == sym.members
							|| null == sym.symtab || sym.scope != null) { 
						// memtype is forward referenced, so try again later
						scope = scx != null ? scx : new Scope(sc);
						scope.setNoFree();
						scope.module.addDeferredSemantic(this, context);
						return;
					}
				}
			}

			type = type.semantic(filename, lineNumber, sc, context);
			if (isAnonymous()) {
				sce = sc;
			} else {
				sce = sc.push(this);
				sce.parent = this;
			}
			if (members.size() == 0) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeErrorLoc(
							IProblem.EnumMustHaveAtLeastOneMember, this));
				}
			}
			int first = 1;
			Expression elast = null;
			for (int i = 0; i < members.size(); i++) {
				EnumMember em = ((Dsymbol) members.get(i)).isEnumMember();
				Expression e;

				if (null == em) {
					/*
					 * The e.semantic(sce) can insert other symbols, such as
					 * template instances and function literals.
					 */
					continue;
				}

				if (em.type != null) {
					em.type = em.type.semantic(em.filename, em.lineNumber, sce, context);
				}
				e = em.value;
				if (e != null) {
					e = e.semantic(sce, context);
					e = e.optimize(WANTvalue | WANTinterpret, context);
					if (memtype != null) {
						e = e.implicitCastTo(sce, memtype, context);
						e = e.optimize(WANTvalue | WANTinterpret, context);
						if (!isAnonymous()) {
							e = e.castTo(sce, type, context);
						}
						t = memtype;
					} else if (em.type != null) {
						e = e.implicitCastTo(sce, em.type, context);
						e = e.optimize(WANTvalue | WANTinterpret, context);
						t = e.type;
					} else {
						t = e.type;
					}
				} else if (first != 0) {
					if (memtype != null) {
						t = memtype;
					} else if (em.type != null) {
						t = em.type;
					} else {
						t = Type.tint32;
					}
					e = new IntegerExp(em.filename, em.lineNumber, 0, Type.tint32);
					e = e.implicitCastTo(sce, t, context);
					e = e.optimize(WANTvalue | WANTinterpret, context);
					if (!isAnonymous()) {
						e = e.castTo(sce, type, context);
					}
				} else {
					// Set value to (elast + 1).
					// But first check that (elast != t.max)
					e = new EqualExp(em.filename, em.lineNumber, TOK.TOKequal, elast, t
							.getProperty(null, 0, Id.max, 0, 0, context));
					e = e.semantic(sce, context);
					e = e.optimize(WANTvalue | WANTinterpret, context);
					if (e.toInteger(context).isTrue()) {
						enumValueOverflow(em, context);
					}

					// Now set e to (elast + 1)
					e = new AddExp(em.filename, em.lineNumber, elast, new IntegerExp(em.filename, em.lineNumber, 1,
							Type.tint32));
					e = e.semantic(sce, context);
					e = e.castTo(sce, elast.type, context);
					e = e.optimize(WANTvalue | WANTinterpret, context);
				}
				elast = e;
				em.value = e;

				// Add to symbol table only after evaluating 'value'
				if (isAnonymous()) {
					/*
					 * Anonymous enum members get added to enclosing scope.
					 */
					for (Scope scx2 = sce; scx2 != null; scx2 = scx2.enclosing) {
						if (scx2.scopesym != null) {
							if (null == scx2.scopesym.symtab) {
								scx2.scopesym.symtab = new DsymbolTable();
							}
							em.addMember(sce, scx2.scopesym, 1, context);
							break;
						}
					}
				} else {
					em.addMember(sc, this, 1, context);
				}

				/*
				 * Compute .min, .max and .default values. If enum doesn't have
				 * a name, we can never identify the enum type, so there is no
				 * purpose for a .min, .max or .default
				 */
				if (!isAnonymous()) {
					if (first != 0) {
						defaultval = e;
						minval = e;
						maxval = e;
					} else {
						Expression ec;

						/*
						 * In order to work successfully with UDTs, build
						 * expressions to do the comparisons, and let the
						 * semantic analyzer and constant folder give us the
						 * result.
						 */

						// Compute if(e < minval)
						ec = new CmpExp(em.filename, em.lineNumber, TOK.TOKlt, e,
								(Expression) minval);
						ec = ec.semantic(sce, context);
						ec = ec.optimize(WANTvalue | WANTinterpret, context);
						if (ec.toInteger(context).isTrue()) {
							minval = e;
						}

						ec = new CmpExp(em.filename, em.lineNumber, TOK.TOKgt, e,
								(Expression) maxval);
						ec = ec.semantic(sce, context);
						ec = ec.optimize(WANTvalue | WANTinterpret, context);
						if (ec.toInteger(context).isTrue()) {
							maxval = e;
						}
					}
				}
				first = 0;
			}

			if (sc != sce) {
				sce.pop();
			}
		} else {
			Type t;
		    Scope sce;
		    
			integer_t number;
	
			if (symtab != null) { // if already done
				return;
			}
	
			if (memtype == null) {
				memtype = Type.tint32;
			}
			
			if ((sc.stc & STCdeprecated) != 0) {
				isdeprecated = true;
			}
	
			parent = sc.scopesym;
			memtype = memtype.semantic(filename, lineNumber, sc, context);
	
			/*
			 * Check to see if memtype is forward referenced
			 */
			if (memtype.ty == TY.Tenum) {
				EnumDeclaration sym = (EnumDeclaration) memtype.toDsymbol(sc,
						context);
				if (sym.memtype == null) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.BaseEnumIsForwardReference, sourceMemtype));
					}
					memtype = Type.tint32;
				}
			}
	
			if (!memtype.isintegral()) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.EnumBaseTypeMustBeOfIntegralType, sourceMemtype));
				}
				memtype = Type.tint32;
			}
	
			t = isAnonymous() ? memtype : type;
			symtab = new DsymbolTable();
			sce = sc.push(this);
			sce.parent = this;
			number = integer_t.ZERO;
			if (members == null) { // enum ident;
				return;
			}
	
			if (members.size() == 0) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeErrorLoc(
							IProblem.EnumMustHaveAtLeastOneMember, this));
				}
			}
	
			boolean first = true;
			
			int len = members.size();
			for(int i = 0; i < len; i++) {
				Dsymbol sym = members.get(i);
				
				EnumMember em = sym.isEnumMember();
				Expression e;
	
				if (em == null) {
					/*
					 * The e.semantic(sce) can insert other symbols, such as
					 * template instances and function literals.
					 */
					continue;
				}
	
				e = em.value();
				if (e != null) {
					e = e.semantic(sce, context);
					e = e.optimize(ASTDmdNode.WANTvalue, context);
					// Need to copy it because we're going to change the type
					e = e.copy();
					e = e.implicitCastTo(sc, memtype, context);
					e = e.optimize(ASTDmdNode.WANTvalue, context);
					number = e.toInteger(context);
					e.type = t;
				} else { // Default is the previous number plus 1
	
					// Check for overflow
					if (!first) {
						switch (t.toBasetype(context).ty) {
						case Tbool:
							if (number.equals(N_2)) {
								enumValueOverflow(em, context);
							}
							break;
	
						case Tint8:
							if (number.equals(N_128)) {
								enumValueOverflow(em, context);
							}
							break;
	
						case Tchar:
						case Tuns8:
							if (number.equals(N_256)) {
								enumValueOverflow(em, context);
							}
							break;
	
						case Tint16:
							if (number.equals(N_0x8000)) {
								enumValueOverflow(em, context);
							}
							break;
	
						case Twchar:
						case Tuns16:
							if (number.equals(N_0x10000)) {
								enumValueOverflow(em, context);
							}
							break;
	
						case Tint32:
							if (number.equals(N_0x80000000)) {
								enumValueOverflow(em, context);
							}
							break;
	
						case Tdchar:
						case Tuns32:
							if (number.equals(N_0x100000000)) {
								enumValueOverflow(em, context);
							}
							break;
	
						case Tint64:
							if (number.equals(N_0x8000000000000000)) {
								enumValueOverflow(em, context);
							}
							break;
	
						case Tuns64:
							// TODO semantic incorrect comparison in Java
							if (number.equals(BigInteger.ZERO)) {
								enumValueOverflow(em, context);
							}
							break;
	
						default:
							throw new IllegalStateException();
						}
					}
					e = new IntegerExp(em.filename, em.lineNumber, number, t);
				}
				em.value(e);
	
				// Add to symbol table only after evaluating 'value'
				if (isAnonymous()) {
					for (Scope scx = sce.enclosing; scx != null; scx = scx.enclosing) {
						if (scx.scopesym != null) {
							if (scx.scopesym.symtab == null) {
								scx.scopesym.symtab = new DsymbolTable();
							}
							em.addMember(sce, scx.scopesym, 1, context);
							break;
						}
					}
				} else {
					em.addMember(sc, this, 1, context);
				}
	
				if (first) {
					first = false;
					defaultval = number;
					minval = number;
					maxval = number;
				} else if (memtype.isunsigned()) {
					if (number.compareTo((integer_t) minval) < 0) {
						minval = number;
					}
					if (number.compareTo((integer_t)maxval) > 0) {
						maxval = number;
					}
				} else {
					// TODO a cast to sinteger_t is missing (I think long in Java)
					if (number.compareTo((integer_t)minval) < 0) {
						minval = number;
					}
					if (number.compareTo((integer_t)maxval) > 0) {
						maxval = number;
					}
				}
	
				number = number.add(1);
			}
			
			sce.pop();
			
			// This is for Descent to compute the signature
			type.merge(context);
		}
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		Type t = null;
		if (memtype != null) {
			t = memtype.syntaxCopy(context);
		}

		EnumDeclaration ed;
		if (s != null) {
			ed = (EnumDeclaration) s;
			ed.memtype = t;
		} else {
			ed = context.newEnumDeclaration(filename, lineNumber, ident, t);
		}
		super.syntaxCopy(ed, context);
		
		ed.copySourceRange(this);
		ed.javaElement = javaElement;
		
		return ed;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		int i;

		buf.writestring("enum ");
		if (ident != null) {
			buf.writestring(ident.toChars());
			buf.writeByte(' ');
		}
		if (memtype != null) {
			buf.writestring(": ");
			memtype.toCBuffer(buf, null, hgs, context);
		}
		if (members == null) {
			buf.writeByte(';');
			buf.writenl();
			return;
		}
		buf.writenl();
		buf.writeByte('{');
		buf.writenl();
		for (i = 0; i < members.size(); i++) {
			EnumMember em = (members.get(i)).isEnumMember();
			if (em == null) {
				continue;
			}
			em.toCBuffer(buf, hgs, context);
			buf.writeByte(',');
			buf.writenl();
		}
		buf.writeByte('}');
		buf.writenl();
	}
	
	private final void enumValueOverflow(EnumMember em, SemanticContext context) {
		if (context.acceptsErrors()) {
			context.acceptProblem(Problem.newSemanticTypeErrorLoc(
					IProblem.EnumValueOverflow, em));
		}
	}
	
	@Override
	public int getErrorStart() {
		if (ident != null) {
			return ident.getErrorStart();
		} else {
			return start;
		}
	}
	
	@Override
	public int getErrorLength() {
		if (ident != null) {
			return ident.getLength();
		} else {
			return 4; // "enum".length()
		}
	}
	
	@Override
	public char getSignaturePrefix() {
		return Signature.C_ENUM;
	}
	
	public void setJavaElement(IType__Marker javaElement) {
		this.javaElement = javaElement;
	}
	
	@Override
	public IType__Marker getJavaElement() {
		return javaElement;
	}
	
	@Override
	public long getFlags() {
		return super.getFlags() | Flags.AccEnum;
	}

}
