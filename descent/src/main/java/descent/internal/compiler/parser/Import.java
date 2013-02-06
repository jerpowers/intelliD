package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.PROT.PROTprivate;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.IJavaElement__Marker;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;


public class Import extends Dsymbol {

	public boolean first = true; // Is this the first import in a multi?
	public Import next;

	public Identifiers packages;
	public IdentifierExp id;
	public IdentifierExp aliasId;

	public Identifiers names;
	public Identifiers aliases;
	public Module mod;
	public Package pkg;
	public boolean isstatic;
	
	// Added for Descent
	public boolean ispublic;

	public int firstStart;
	public int lastLength;

	public descent.internal.compiler.parser.Array aliasdecls;

	public Import(char[] filename, int lineNumber, Identifiers packages, IdentifierExp id,
			IdentifierExp aliasId, boolean isstatic) {
		super(id);
		this.filename = filename;
		this.lineNumber = lineNumber;
		this.id = id;
		this.packages = packages;
		this.aliasId = aliasId;
		this.isstatic = isstatic;

		if (aliasId != null) {
			this.ident = aliasId;
		} else if (packages != null && packages.size() > 0) {
			this.ident = packages.get(0);
		}
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, packages);
			TreeVisitor.acceptChildren(visitor, id);
			TreeVisitor.acceptChildren(visitor, aliasId);
			TreeVisitor.acceptChildren(visitor, names);
			TreeVisitor.acceptChildren(visitor, aliases);
		}
		visitor.endVisit(this);
	}

	public void addAlias(IdentifierExp name, IdentifierExp alias) {
		if (names == null) {
			names = new Identifiers(3);
			aliases = new Identifiers(3);
		}
		names.add(name);
		aliases.add(alias);
	}

	/*****************************
	 * Add import to sd's symbol table.
	 */
	@Override
	public int addMember(Scope sc, ScopeDsymbol sd, int memnum,
			SemanticContext context) {
		int result = 0;

		if (size(names) == 0) {
			return super.addMember(sc, sd, memnum, context);
		}

		if (null != aliasId) {
			result = super.addMember(sc, sd, memnum, context);
		}

	    /* Instead of adding the import to sd's symbol table,
	     * add each of the alias=name pairs
	     */
		for (int i = 0; i < size(names); i++) {
			IdentifierExp name = names.get(i);
			IdentifierExp alias = aliases.get(i);

			if (null == alias) {
				alias = name;
			}

			TypeIdentifier tname = new TypeIdentifier(filename, lineNumber, name);
			AliasDeclaration ad = new AliasDeclaration(filename, lineNumber, alias, tname);
			ad.isImportAlias = true;
			result |= ad.addMember(sc, sd, memnum, context);

			if (aliasdecls == null) {
				aliasdecls = new Dsymbols(3);
			}
			aliasdecls.add(ad);
		}

		return result;
	}

	@Override
	public int getNodeType() {
		return IMPORT;
	}

	@Override
	public Import isImport() {
		return this;
	}

	@Override
	public String kind() {
		return isstatic ? "static import" : "import";
	}

	public void load(Scope sc, SemanticContext context) {
		DsymbolTable dst;
		Dsymbol s;

		// See if existing module
		Package[] ppkg = { pkg };
		dst = Package.resolve(packages, null, ppkg, context);
		pkg = ppkg[0];

		s = dst.lookup(id);
		if (null != s) {
			if (null != s.isModule()) {
				mod = (Module) s;
			} else {
				if (context.acceptsErrors()) {
					context.acceptProblem(Problem.newSemanticTypeError(
							IProblem.PackageAndModuleHaveTheSameName, this));
				}
			}
		}

		if (null == mod) {
			// Load module
			mod = Module.load(filename, lineNumber, packages, id, context);
			
			// Changed from DMD, since now a module that doesn't load
			// yields a null value
			if (mod == null) {
				return;
			}
			
			dst.insert(id, mod); // id may be different from mod->ident,
			// if so then insert alias

			if (null == mod.importedFrom) {
				mod.importedFrom = null != sc ? sc.module.importedFrom
						: context.Module_rootModule;
			}
		}

		if (null == pkg) {
			pkg = mod;
		}
	}

	@Override
	public boolean overloadInsert(Dsymbol s, SemanticContext context) {
		// Allow multiple imports of the same name
		return s.isImport() != null;
	}

	@Override
	public Dsymbol search(char[] filename, int lineNumber, char[] ident, int flags,
			SemanticContext context) {
		if (null == pkg) {
			load(null, context);
			
			if (mod != null) {
				context.muteProblems++;
				mod.semantic(context);
				context.muteProblems--;
			}
			
			// Added for Descent
			if (null == pkg) {
				return null;
			}
		}

		// Forward it to the package/module
		return pkg.search(filename, lineNumber, ident, flags, context);
	}

	@Override
	public void semantic(Scope sc, SemanticContext context) {
		load(sc, context);

		if (null != mod) {

			if (sc.module.aimports == null) {
				sc.module.aimports = new Array();
			}
			sc.module.aimports.add(mod);

			context.muteProblems++;
			mod.semantic(null, context);
			context.muteProblems--;

			if (!isstatic && null == aliasId && 0 == size(names)) {
				/*
				 * Default to private importing
				 */
				PROT prot = sc.protection;
				if (sc.explicitProtection == 0) {
					prot = PROTprivate;
				}

				// Check added for Descent
				else if (sc.protection == PROT.PROTpublic) {
					this.ispublic = true;
				}

				sc.scopesym.importScope(mod, prot);
			}

			if (mod.needmoduleinfo) {
				sc.module.needmoduleinfo = true;
			}

			sc = sc.push(mod);

			for (int i = 0; i < size(aliasdecls); i++) {
				Dsymbol s = (Dsymbol) aliasdecls.get(i);

				if (null == mod.search(filename, lineNumber, names.get(i), 0, context)) {
					if (context.acceptsErrors()) {
						context.acceptProblem(Problem.newSemanticTypeError(
								IProblem.ImportNotFound, this,
								new String[] { (names.get(i)).toChars() }));
					}
				}

				s.semantic(sc, context);
			}
			sc = sc.pop();
		}
	}

	@Override
	public void semantic2(Scope sc, SemanticContext context) {
		// Descent: mod may be null if there was some problem resolving
		// this import, but semantic is continued.
		if (mod == null) {
			return;
		}
		
		context.muteProblems++;
		mod.semantic2(sc, context);
		context.muteProblems--;
		
		if (mod.needmoduleinfo) {
			sc.module.needmoduleinfo = true;
		}
	}

	@Override
	public Dsymbol syntaxCopy(Dsymbol s, SemanticContext context) {
		assert (null == s);

		Import si;
		si = new Import(filename, lineNumber, packages, id, aliasId, isstatic);

		for (int i = 0; i < size(names); i++) {
			si.addAlias(names.get(i), aliases.get(i));
		}

		return si;
	}

	@Override
	public Dsymbol toAlias(SemanticContext context) {
		if (aliasId != null) {
			return mod;
		}
		return this;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		if (hgs.hdrgen && equals(id, Id.object)) {
			return; // object is imported by default
		}

		if (isstatic) {
			buf.writestring("static ");
		}
		buf.writestring("import ");
		if (null != aliasId) {
			buf.printf(aliasId.toChars() + " = ");
		}
		if (null != packages && packages.size() > 0) {
			for (int i = 0; i < packages.size(); i++) {
				IdentifierExp pid = packages.get(i);
				buf.printf(new String(pid.ident) + ".");
			}
		}
		buf.printf(new String(id.ident) + ";");
		buf.writenl();
	}
	
	public char[] getFQN() {
		return getFQN(packages, id);
	}
	
	@Override
	public String getSignature(int options) {
		if (mod != null && equals(mod.ident, ident)) {
			return mod.getSignature(options);
		}
		return null;
	}
	
	@Override
	public IJavaElement__Marker getJavaElement() {
		if (mod != null && equals(mod.ident, ident)) {
			return mod.getJavaElement();
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		if (aliasId != null) {
			buffer.append(aliasId);
			buffer.append(" = ");
		}
		if (packages != null) {
			for(int i = 0; i < packages.size(); i++) {
				if (i > 0) {
					buffer.append('.');
				}
				buffer.append(packages.get(i));
			}
			buffer.append('.');
		}
		if (id != null) {
			buffer.append(id);
		}
		if (names != null) {
			buffer.append(" : ");
			for(int i = 0; i < names.size(); i++) {
				if (i > 0) {
					buffer.append(", ");
				}
				if (aliases.get(i) != null) {
					buffer.append(aliases.get(i));
					buffer.append(" = ");
				}
				buffer.append(names.get(i));
			}
		}
		return buffer.toString();
	}

}
