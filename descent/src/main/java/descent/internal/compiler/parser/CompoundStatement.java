package descent.internal.compiler.parser;

import static descent.internal.compiler.parser.BE.BEfallthru;
import melnorme.utilbox.tree.TreeVisitor;
import descent.core.compiler.IProblem;
import descent.internal.compiler.parser.ast.IASTVisitor;

public class CompoundStatement extends Statement {	

	public boolean manyVars; 	// if true, the block is just to group variable declarations,
								// alias declarations or typedef declarations
	public Statements statements;
	public Statements sourceStatements;

	public CompoundStatement(char[] filename, int lineNumber, Statements statements) {
		super(filename, lineNumber);
		this.statements = statements;
		if (statements != null) {
			this.sourceStatements = new Statements(statements);
		}
	}

	public CompoundStatement(char[] filename, int lineNumber, Statement s1, Statement s2) {
		super(filename, lineNumber);
		this.statements = new Statements(2);
		this.statements.add(s1);
		this.statements.add(s2);
		this.sourceStatements = new Statements(statements);
	}

	@Override
	public void accept0(IASTVisitor visitor) {
		boolean children = visitor.visit(this);
		if (children) {
			TreeVisitor.acceptChildren(visitor, sourceStatements);
		}
		visitor.endVisit(this);
	}
	
	@Override
	public int blockExit(SemanticContext context) {
		int result = BEfallthru;
		for (int i = 0; i < size(statements); i++) {
			Statement s = (Statement) statements.get(i);
			if (s != null) {
				if (0 == (result & BEfallthru) && !s.comeFrom()) {
					if (context.global.params.warnings) {
						if (context.acceptsWarnings()) {
							context.acceptProblem(Problem.newSemanticTypeWarning(IProblem.StatementIsNotReachable, s));
						}
					}
				}

				result &= ~BEfallthru;
				result |= s.blockExit(context);
			}
		}
	    return result;
	}

	@Override
	public boolean comeFrom() {
		boolean comefrom = false;

		for (int i = 0; i < statements.size(); i++) {
			Statement s = statements.get(i);

			if (null == s) {
				continue;
			}

			comefrom |= s.comeFrom();
		}
		return comefrom;
	}

	@Override
	public Statements flatten(Scope sc, SemanticContext context) {
		return statements;
	}

	@Override
	public int getNodeType() {
		return COMPOUND_STATEMENT;
	}

	@Override
	public Expression interpret(InterState istate, SemanticContext context) {
		Expression e = null;

		if (istate.start == this) {
			istate.start = null;
		}
		if (statements != null) {
			for (int i = 0; i < statements.size(); i++) {
				Statement s = statements.get(i);

				if (s != null) {
					e = s.interpret(istate, context);
					if (e != null) {
						break;
					}
				}
			}
		}
		return e;
	}

	@Override
	public ReturnStatement isReturnStatement() {
		int i;
		ReturnStatement rs = null;

		for (i = 0; i < statements.size(); i++) {
			Statement s;

			s = statements.get(i);
			if (s != null) {
				rs = s.isReturnStatement();
				if (rs != null) {
					break;
				}
			}
		}
		return rs;
	}

	@Override
	public Statement semantic(Scope sc, SemanticContext context) {
		Statement s = null;

		if (statements != null) {
			for (int i = 0; i < statements.size();) {
				s = statements.get(i);
				if (s != null) {
					Statements a = s.flatten(sc, context);

					if (a != null) {
						statements.remove(i);
						statements.addAll(i, a);
						continue;
					}
					s = s.semantic(sc, context);
					statements.set(i, s);
					if (s != null) {
						Statement[] sentry = { null };
						Statement[] sexception = { null };
						Statement[] sfinally = { null };

						s.scopeCode(sc, sentry, sexception, sfinally, context);
						if (sentry[0] != null) {
							sentry[0] = sentry[0].semantic(sc, context);
							statements.set(i, sentry[0]);
						}
						if (sexception[0] != null) {
							if (i + 1 == statements.size()
									&& sfinally[0] == null) {
								sexception[0] = sexception[0].semantic(sc,
										context);
							} else {
								/*
								 * Rewrite: s; s1; s2; As: s; try { s1; s2; } catch
								 * (Object __o) { sexception; throw __o; }
								 */
								Statement body;
								Statements a2 = new Statements(statements.size());

								for (int j = i + 1; j < statements.size(); j++) {
									a2.add(statements.get(j));
								}
								body = context.newCompoundStatement(filename, lineNumber, a2);
								body.copySourceRange(a2);
								body = new ScopeStatement(filename, lineNumber, body);
								
								IdentifierExp id = context.uniqueId("__o");

								Statement handler = new ThrowStatement(filename, lineNumber,
										new IdentifierExp(filename, lineNumber, id));
								handler = new CompoundStatement(filename, lineNumber,
										sexception[0], handler);

								Array catches = new Array();
								Catch ctch = new Catch(filename, lineNumber, null,
										new IdentifierExp(filename, lineNumber, id), handler);
								catches.add(ctch);
								s = new TryCatchStatement(filename, lineNumber, body, catches);

								if (sfinally[0] != null) {
									s = new TryFinallyStatement(filename, lineNumber, s,
											sfinally[0]);
								}
								s = s.semantic(sc, context);
								statements.setDim(i + 1);
								statements.add(s);
								break;
							}
						} else if (sfinally[0] != null) {
							if (false && i + 1 == statements.size()) {
								statements.add(sfinally[0]);
							} else {
								/*
								 * Rewrite: s; s1; s2; As: s; try { s1; s2; }
								 * finally { sfinally; }
								 */
								Statement body;
								Statements a2 = new Statements();

								for (int j = i + 1; j < statements.size(); j++) {
									a2.add(statements.get(j));
								}
								body = new CompoundStatement(filename, lineNumber, a2);
								body.copySourceRange(a2.get(0), a2.get(a2.size() - 1));
								
								s = new TryFinallyStatement(filename, lineNumber, body, sfinally[0]);
								s.copySourceRange(body, sfinally[0]);
								s = s.semantic(sc, context);
								statements.setDim(i + 1);
								statements.add(s);
								break;
							}
						}
					}
				}
				i++;
			}
		}
		if (statements != null && statements.size() == 1) {
			return statements.get(0);
		}
		return this;
	}

	@Override
	public Statement syntaxCopy(SemanticContext context) {
		Statements a = new Statements(statements.size());
		a.setDim(statements.size());
		for (int i = 0; i < statements.size(); i++) {
			Statement s = statements.get(i);
			if (s != null) {
				s = s.syntaxCopy(context);
			}
			a.set(i, s);
		}
		CompoundStatement cs = context.newCompoundStatement(filename, lineNumber, a);
		cs.copySourceRange(this);
		return cs;
	}

	@Override
	public void toCBuffer(OutBuffer buf, HdrGenState hgs,
			SemanticContext context) {
		int i;

		for (i = 0; i < statements.size(); i++) {
			Statement s;

			s = statements.get(i);
			if (s != null) {
				s.toCBuffer(buf, hgs, context);
			}
		}
	}

	@Override
	public boolean usesEH(SemanticContext context) {
		for (int i = 0; i < statements.size(); i++) {
			Statement s;

			s = statements.get(i);
			if (s != null && s.usesEH(context)) {
				return true;
			}
		}
		return false;
	}

}
