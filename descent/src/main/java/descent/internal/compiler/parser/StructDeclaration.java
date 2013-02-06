package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.LINK.LINKd;
import static descent.internal.compiler.parser.PROT.PROTnone;
import static descent.internal.compiler.parser.STC.STC_TYPECTOR;
import static descent.internal.compiler.parser.STC.STCconst;
import static descent.internal.compiler.parser.STC.STCimmutable;
import static descent.internal.compiler.parser.STC.STCin;
import static descent.internal.compiler.parser.STC.STCnodtor;
import static descent.internal.compiler.parser.STC.STCref;
import static descent.internal.compiler.parser.STC.STCshared;
import static descent.internal.compiler.parser.STC.STCstatic;
import static descent.internal.compiler.parser.STC.STCundefined;
import static descent.internal.compiler.parser.TOK.TOKblit;
import static descent.internal.compiler.parser.TY.Tsarray;
import static descent.internal.compiler.parser.TY.Tstruct;

import java.util.ArrayList;

import melnorme.utilbox.tree.TreeVisitor;

import melnorme.utilbox.core.Assert;

import descent.core.Flags;
import descent.core.Signature;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class StructDeclaration extends AggregateDeclaration {

	public boolean zeroInit; // !=0 if initialize with 0 fill
	public int hasIdentityAssign;	// !=0 if has identity opAssign
	public FuncDeclaration cpctor;	// generated copy-constructor, if any
	public Dsymbol ctor;

	public FuncDeclarations postblits;	// Array of postblit functions
	public FuncDeclaration postblit;	// aggregate postblit

	public StructDeclaration(char[] filename, int lineNumber, IdentifierExp id) {
		super(filename, lineNumber, id);
		this.type = new TypeStruct(this);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, ident);
			TreeVisitor.acceptChildren(visitor, members);

//			acceptSynthetic(visitor);
		}
		visitor.endVisit(this);
	}

	@Override
	public PROT getAccess(Dsymbol smember) {
		PROT access_ret = PROTnone;

		Dsymbol p = smember.toParent();
		if (p != null && p.isAggregateDeclaration() != null && SemanticMixin.equals(p.isAggregateDeclaration(), this)) {
			access_ret = smember.prot();
		} else if (smember.isDeclaration().isStatic()) {
			access_ret = smember.prot();
		}
		return access_ret;
	}

	@Override
	public int getNodeType() {
		return STRUCT_DECLARATION;
	}

	@Override
	public StructDeclaration isStructDeclaration() {
		return this;
	}

	@Override
	public String kind() {
		return "struct";
	}

	@Override
	public String mangle(SemanticContext context) {
		return Dsymbol_mangle(context);
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		Scope sc2;

		Assert.isNotNull(type);
		if (members == null) { // if forward reference
			return;
		}

		if (symtab != null) {
			if (scope == null) {
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

		parent = sc.parent;

		if (context.STRUCTTHISREF()) {
			handle = type;
		} else {
			handle = type.pointerTo(context);
		}
		structalign = sc.structalign;
		protection = sc.protection;
		if ((sc.stc & STC.STCdeprecated) != 0) {
			isdeprecated = true;
		}

		if (context.isD2()) {
			 storage_class |= sc.stc;
		}

		assert (!isAnonymous());
		if ((sc.stc & STC.STCabstract) != 0) {
			if (isUnionDeclaration() != null) {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeErrorLoc(
							IProblem.UnionsCannotBeAbstract, this));
				}
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeErrorLoc(
							IProblem.StructsCannotBeAbstract, this));
				}
			}
		}

		if (context.isD2()) {
			if ((storage_class & STCimmutable) != 0) {
		        type = type.invariantOf(context);
			} else if ((storage_class & STCconst) != 0) {
		        type = type.constOf(context);
			} else if ((storage_class & STCshared) != 0) {
		        type = type.sharedOf(context);
			}
		}

		if (sizeok == 0) { // if not already done the addMember step
			boolean hasfunctions = false;
			for (Dsymbol s : members) {
				s.addMember(sc, this, 1, context);
			    if (s.isFuncDeclaration() != null) {
					hasfunctions = true;
			    }
			}

			if (!context.isD1()) {
				// If nested struct, add in hidden 'this' pointer to outer scope
				if (hasfunctions && 0 == (storage_class & STCstatic)) {
					Dsymbol s = toParent2();
					if (s != null) {
						AggregateDeclaration ad = s.isAggregateDeclaration();
						FuncDeclaration fd = s.isFuncDeclaration();

						TemplateInstance ti;
						if (ad != null
								&& (ti = ad.parent.isTemplateInstance()) != null
								&& ti.isnested != null || fd != null) {
							isnested = true;
							Type t;
							if (ad != null)
								t = ad.handle;
							else if (fd != null) {
								AggregateDeclaration ad2 = fd.isMember2();
								if (ad2 != null)
									t = ad2.handle;
								else
									t = context.Type_tvoidptr;
							} else {
								throw new IllegalStateException("assert(0);");
							}
							if (t.ty == Tstruct)
								t = context.Type_tvoidptr; // t should not be a
															// ref type
							assert (null == vthis);
							vthis = new ThisDeclaration(filename, lineNumber, t);
							// vthis.storage_class |= STCref;
							members.add(vthis);
						}
					}
				}
			}
		}

		sizeok = 0;
		sc2 = sc.push(this);
		if (context.isD2()) {
			sc2.stc &= storage_class & STC_TYPECTOR;
		} else {
			sc2.stc = 0;
		}
		sc2.parent = this;
		if (isUnionDeclaration() != null) {
			sc2.inunion = true;
		}
		sc2.protection = PROT.PROTpublic;
		sc2.explicitProtection = 0;

		semanticScope(sc2);

		int members_dim = members.size();
		for (int i = 0; i < members_dim; i++) {
			Dsymbol s = members.get(i);
			s.semantic(sc2, context);
			if (isUnionDeclaration() != null) {
				sc2.offset = 0;
			}

			if (!context.isD1()) {
				Type t;
				if (s.isDeclaration() != null
						&& (t = s.isDeclaration().type) != null
						&& t.toBasetype(context).ty == Tstruct) {
					StructDeclaration sd = (StructDeclaration) t.toDsymbol(sc, context);
					if (sd.isnested) {
						if (context.acceptsErrors()) {
							context.acceptProblem(Problem.newSemanticTypeError(IProblem.InnerStructCannotBeAField, this, sd.toChars(context)));
						}
					}
				}
			}
		}

		/* The TypeInfo_Struct is expecting an opEquals and opCmp with
		 * a parameter that is a pointer to the struct. But if there
		 * isn't one, but is an opEquals or opCmp with a value, write
		 * another that is a shell around the value:
		 *	int opCmp(struct *p) { return opCmp(*p); }
		 */

		TypeFunction tfeqptr;
		{
			Arguments arguments = new Arguments(1);
			Argument arg = new Argument(STCin, handle, new IdentifierExp(filename, lineNumber,
					Id.p), null);

			arguments.add(arg);
			tfeqptr = new TypeFunction(arguments, Type.tint32, 0, LINK.LINKd);
			tfeqptr = (TypeFunction) tfeqptr.semantic(filename, lineNumber, sc, context);
		}

		TypeFunction tfeq;
		{
			Arguments arguments = new Arguments(1);
			Argument arg = new Argument(STCin, type, null, null);

			arguments.add(arg);
			tfeq = new TypeFunction(arguments, Type.tint32, 0, LINK.LINKd);
			tfeq = (TypeFunction) tfeq.semantic(filename, lineNumber, sc, context);
		}

		char[] id = Id.eq;
		for (int j = 0; j < 2; j++) {
			Dsymbol s = ASTDmdNode.search_function(this, id, context);
			FuncDeclaration fdx = s != null ? s.isFuncDeclaration() : null;
			if (fdx != null) {
				FuncDeclaration fd = fdx.overloadExactMatch(tfeqptr, context);
				if (fd == null) {
					fd = fdx.overloadExactMatch(tfeq, context);
					if (fd != null) { // Create the thunk, fdptr
						FuncDeclaration fdptr = new FuncDeclaration(filename, lineNumber,
								fdx.ident, STC.STCundefined, tfeqptr);
						Expression e = new IdentifierExp(filename, lineNumber, Id.p);
						e = new PtrExp(filename, lineNumber, e);
						Expressions args = new Expressions(1);
						args.add(e);
						e = new IdentifierExp(filename, lineNumber, id);
						e = new CallExp(filename, lineNumber, e, args);
						fdptr.fbody = new ReturnStatement(filename, lineNumber, e);
						ScopeDsymbol s2 = fdx.parent.isScopeDsymbol();
						Assert.isNotNull(s2);
						s2.members.add(fdptr);
						fdptr.addMember(sc, s2, 1, context);
						fdptr.semantic(sc2, context);
					}
				}
			}

			id = Id.cmp;
		}

		if (context.isD2()) {
			dtor = buildDtor(sc2, context);
			postblit = buildPostBlit(sc2, context);
			cpctor = buildCpCtor(sc2, context);
			buildOpAssign(sc2, context);
		}

		sc2.pop();

		if (sizeok == 2) { // semantic() failed because of forward references.
			// Unwind what we did, and defer it for later
			fields = new ArrayList<VarDeclaration>(0);
			structsize = 0;
			alignsize = 0;
			structalign = 0;

			scope = scx != null ? scx : new Scope(sc);
			scope.setNoFree();
			scope.module.addDeferredSemantic(this, context);
			return;
		}

		// 0 sized struct's are set to 1 byte
		if (structsize == 0) {
			structsize = 1;
			alignsize = 1;
		}

		// Round struct size up to next alignsize boundary.
		// This will ensure that arrays of structs will get their internals
		// aligned properly.
		structsize = (structsize + alignsize - 1) & ~(alignsize - 1);

		sizeok = 1;

		context.Module_dprogress++;

		// Determine if struct is all zeros or not
		zeroInit = true;
		for (int j = 0; j < this.fields.size(); j++) {
			Dsymbol s = this.fields.get(j);
			VarDeclaration vd = s.isVarDeclaration();
			if (vd != null && !vd.isDataseg(context)) {
				if (vd.init() != null) {
					// Should examine init to see if it is really all 0's
					zeroInit = true;
					break;
				} else {
					if (!vd.type.isZeroInit(filename, lineNumber, context)) {
						zeroInit = false;
						break;
					}
				}
			}
		}

		/* Look for special member functions.
		 */
		if (context.isD2()) {
		    ctor = search(null, 0, Id.ctor, 0, context);
		}
		inv = (InvariantDeclaration) search(filename, lineNumber, Id.classInvariant, 0, context);
		aggNew((NewDeclaration) search(filename, lineNumber, Id.classNew, 0, context));
		aggDelete((DeleteDeclaration) search(filename, lineNumber, Id.classDelete, 0, context));

		if (sc.func != null) {
			semantic2(sc, context);
			semantic3(sc, context);
		}
	}

	public FuncDeclaration buildPostBlit(Scope sc, SemanticContext context) {
		Expression e = null;

		for (int i = 0; i < size(fields); i++) {
			Dsymbol s = (Dsymbol) fields.get(i);
			VarDeclaration v = s.isVarDeclaration();
			if ((v.storage_class & STCref) != 0)
			    continue;
			Type tv = v.type.toBasetype(context);
			int dim = 1;
			while (tv.ty == Tsarray) {
//				TypeSArray ta = (TypeSArray) tv;
				dim *= ((TypeSArray) tv).dim.toInteger(context).intValue();
				tv = tv.nextOf().toBasetype(context);
			}
			if (tv.ty == Tstruct) {
				TypeStruct ts = (TypeStruct) tv;
				StructDeclaration sd = ts.sym;
				if (sd.postblit != null) {
					Expression ex;

					// this.v
					ex = new ThisExp(null, 0);
					ex = new DotVarExp(null, 0, ex, v, false);

					if (dim == 1) { // this.v.dtor()
						ex = new DotVarExp(null, 0, ex, sd.postblit, false);
						ex = new CallExp(null, 0, ex);
					} else {
						// Typeinfo.postblit(cast(void*)&this.v);
						Expression ea = new AddrExp(null, 0, ex);
						ea = new CastExp(null, 0, ea, Type.tvoid
								.pointerTo(context));

						Expression et = v.type.getTypeInfo(sc, context);
						et = new DotIdExp(null, 0, et, Id.postblit);

						ex = new CallExp(null, 0, et, ea);
					}
					e = Expression.combine(e, ex); // combine in forward order
				}
			}
		}

		/*
		 * Build our own "postblit" which executes e
		 */
		if (e != null) {
			PostBlitDeclaration dd = new PostBlitDeclaration(null, 0,
					new IdentifierExp(Id.__fieldPostBlit));
			dd.fbody = new ExpStatement(null, 0, e);
			if (dtors == null) {
				dtors = new FuncDeclarations(1);
			}
			dtors.add(dd);
			if (members == null) {
				members = new Dsymbols(1);
			}
			members.add(dd);
			dd.semantic(sc, context);
		}

		switch (size(postblits)) {
		case 0:
			return null;

		case 1:
			return (FuncDeclaration) postblits.get(0);

		default:
			e = null;
			for (int i = 0; i < size(postblits); i++) {
				FuncDeclaration fd = (FuncDeclaration) postblits.get(i);
				Expression ex = new ThisExp(null, 0);
				ex = new DotVarExp(null, 0, ex, fd, false);
				ex = new CallExp(null, 0, ex);
				e = Expression.combine(e, ex);
			}
			PostBlitDeclaration dd = new PostBlitDeclaration(null, 0,
					new IdentifierExp(Id.__aggrPostBlit));
			dd.fbody = new ExpStatement(null, 0, e);
			if (members == null) {
				members = new Dsymbols(1);
			}
			members.add(dd);
			dd.semantic(sc, context);
			return dd;
		}
	}

	public FuncDeclaration buildCpCtor(Scope sc, SemanticContext context) {
		FuncDeclaration fcp = null;

		/*
		 * Copy constructor is only necessary if there is a postblit function,
		 * otherwise the code generator will just do a bit copy.
		 */
		if (postblit != null) {
			Argument param = new Argument(STCref, type,
					new IdentifierExp(Id.p), null);
			Arguments fparams = new Arguments(1);
			fparams.add(param);
			Type ftype = new TypeFunction(fparams, Type.tvoid, 0, LINKd);

			fcp = new FuncDeclaration(null, 0, new IdentifierExp(Id.cpctor), STCundefined, ftype);

			// Build *this = p;
			Expression e = new ThisExp(null, 0);
			if (!context.STRUCTTHISREF()) {
				e = new PtrExp(null, 0, e);
			}
			AssignExp ea = new AssignExp(null, 0, e, new IdentifierExp(Id.p));
			ea.op = TOKblit;
			Statement s = new ExpStatement(null, 0, ea);

			// Build postBlit();
			e = new VarExp(null, 0, postblit, false);
			e = new CallExp(null, 0, e);

			s = new CompoundStatement(null, 0, s,
					new ExpStatement(null, 0, e));
			fcp.fbody = s;

			if (members == null) {
				members = new Dsymbols(1);
			}
			members.add(fcp);

			sc = sc.push();
			sc.stc = 0;
			sc.linkage = LINKd;

			fcp.semantic(sc, context);

			sc.pop();
		}

		return fcp;
	}

	public FuncDeclaration buildOpAssign(Scope sc, SemanticContext context) {
		if (!needOpAssign(context)) {
			return null;
		}

		FuncDeclaration fop = null;

		Argument param = new Argument(STCnodtor, type, new IdentifierExp(Id.p),
				null);
		Arguments fparams = new Arguments(1);
		fparams.add(param);
		Type ftype = new TypeFunction(fparams, handle, 0, LINKd);

		fop = new FuncDeclaration(null, 0, new IdentifierExp(Id.assign),
				STCundefined, ftype);

		Expression e = null;
		if (postblit != null) {
			/*
			 * Swap: tmp =this;this = s; tmp.dtor();
			 */
			IdentifierExp idtmp = context.uniqueId("__tmp");
			VarDeclaration tmp = null;
			AssignExp ec = null;
			if (dtor != null) {
				tmp = new VarDeclaration(null, 0, type, idtmp,
						new VoidInitializer(null, 0));
				tmp.noauto = true;
				e = new DeclarationExp(null, 0, tmp);
				ec = new AssignExp(null, 0, new VarExp(null, 0, tmp),
						context.STRUCTTHISREF() ?
							new ThisExp(null, 0) :
							new PtrExp(null, 0, new ThisExp(null, 0)));
				ec.op = TOKblit;
				e = Expression.combine(e, ec);
			}
			ec = new AssignExp(null, 0,
					context.STRUCTTHISREF() ?
						new ThisExp(null, 0) :
						new PtrExp(null, 0, new ThisExp(null, 0)),
					new IdentifierExp(Id.p));
			ec.op = TOKblit;
			e = Expression.combine(e, ec);
			if (dtor != null) {
				/*
				 * Instead of running the destructor on s, run it on tmp. This
				 * avoids needing to copy tmp back in to s.
				 */
				Expression ec2 = new DotVarExp(null, 0, new VarExp(null, 0,
						tmp), dtor, false);
				ec2 = new CallExp(null, 0, ec2);
				e = Expression.combine(e, ec2);
			}
		} else {
			/*
			 * Do memberwise copy
			 */
			for (int i = 0; i < size(fields); i++) {
				Dsymbol s = (Dsymbol) fields.get(i);
				VarDeclaration v = s.isVarDeclaration();
				// this.v = s.v;
				AssignExp ec = new AssignExp(null, 0, new DotVarExp(null, 0,
						new ThisExp(null, 0), v, false), new DotVarExp(null, 0,
						new IdentifierExp(Id.p), v, false));
				ec.op = TOKblit;
				e = Expression.combine(e, ec);
			}
		}
		Statement s1 = new ExpStatement(null, 0, e);

		/*
		 * Add: return this;
		 */
		e = new ThisExp(null, 0);
		Statement s2 = new ReturnStatement(null, 0, e);

		fop.fbody = new CompoundStatement(null, 0, s1, s2);

		if (members == null) {
			members = new Dsymbols(1);
		}
		members.add(fop);
		fop.addMember(sc, this, 1, context);

		sc = sc.push();
		sc.stc = 0;
		sc.linkage = LINKd;

		fop.semantic(sc, context);

		sc.pop();

		return fop;
	}

	public boolean needOpAssign(SemanticContext context) {
		if (hasIdentityAssign != 0) {
			// goto Ldontneed;
			return false;
		}

		if (dtor != null || postblit != null) {
			// goto Lneed;
			return true;
		}

		/*
		 * If any of the fields need an opAssign, then we need it too.
		 */
		for (int i = 0; i < size(fields); i++) {
			Dsymbol s = (Dsymbol) fields.get(i);
			VarDeclaration v = s.isVarDeclaration();
			if ((v.storage_class & STCref) != 0)
			    continue;
			Type tv = v.type.toBasetype(context);
			while (tv.ty == Tsarray) {
				// TypeSArray ta = (TypeSArray) tv;
				tv = tv.nextOf().toBasetype(context);
			}
			if (tv.ty == Tstruct) {
				TypeStruct ts = (TypeStruct) tv;
				StructDeclaration sd = ts.sym;
				if (sd.needOpAssign(context)) {
					// goto Lneed;
					return true;
				}
			}
		}
		// Ldontneed:
		return false;

		// Lneed:
		// return 1;
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		StructDeclaration sd;

		if (s != null) {
			sd = (StructDeclaration) s;
		} else {
			sd = context.newStructDeclaration(filename, lineNumber, ident);
		}
		super.syntaxCopy(sd, context);

		sd.copySourceRange(this);
		sd.javaElement = javaElement;
		sd.templated = templated;

		return sd;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		int i;

		buf.writestring(kind());
		if (!isAnonymous()) {
			buf.writestring(toChars(context));
		}
		if (null == members) {
			buf.writeByte(';');
			buf.writenl();
			return;
		}
		buf.writenl();
		buf.writeByte('{');
		buf.writenl();
		for (i = 0; i < members.size(); i++) {
			Dsymbol s = members.get(i);

			buf.writestring("    ");
			s.toCBuffer(buf, hgs, context);
		}
		buf.writeByte('}');
		buf.writenl();
	}

	@Override
	public int getErrorStart() {
		if (ident != null) {
			return ident.start;
		}
		return start;
	}

	@Override
	public int getErrorLength() {
		if (ident != null) {
			return ident.length;
		}
		return 6; // "struct".length()
	}

	@Override
	public char getSignaturePrefix() {
		if (templated) {
			return Signature.C_TEMPLATED_STRUCT;
		} else {
			return Signature.C_STRUCT;
		}
	}

	@Override
	public long getFlags() {
		return super.getFlags() | Flags.AccStruct;
	}

	@Override
	public StructDeclaration unlazy(char[] prefix, SemanticContext context) {
		return this;
	}

}
