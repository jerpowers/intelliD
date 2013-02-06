package descent.internal.compiler.parser.ast;

import melnorme.utilbox.core.Assert;
import descent.internal.compiler.parser.*;
import descent.internal.compiler.parser.Package;

/**
 * An abstract visitor class that delegates each visit method to the method with the parameter's superclass.
 * Thus it navigates upwards in the AST hierarchy until a concrete methed implementation is executed.
 * This is a cute idea, but it can be a performance issue. 
 */
public class ASTUpTreeVisitor implements IASTVisitor {
	
	@Override
	public boolean preVisit(ASTNode elem) {
		return true; // Default implementation: do nothing
	}
	@Override
	public void postVisit(ASTNode elem) {
		// Default implementation: do nothing
	}
	
	public boolean visit(@SuppressWarnings("unused") IASTNode node) {
		// Default implementation: do nothing
		return true;
	}
	
	public void endVisit(@SuppressWarnings("unused") IASTNode node) {
		// Default implementation: do nothing
		return;
	}
	
	/* ====================================================== */
	
	static {
		// BM: TODO each assert should be separated and put next to the correspoding visit method
		// but it's unlikely the DMD hierarchy will ever change
		Assert.isTrue(ASTDmdNode.class.getSuperclass().equals(ASTNode.class));
		Assert.isTrue(ASTRangeLessNode.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(AddAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AddExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AddrExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(AggregateDeclaration.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(AliasDeclaration.class.getSuperclass().equals(Declaration.class));
		Assert.isTrue(AliasThis.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(AlignDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(AndAndExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AndAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AndExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AnonDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(AnonymousAggregateDeclaration.class.getSuperclass().equals(AggregateDeclaration.class));
		Assert.isTrue(Argument.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(ArrayExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(ArrayInitializer.class.getSuperclass().equals(Initializer.class));
		Assert.isTrue(ArrayLengthExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(ArrayLiteralExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(ArrayScopeSymbol.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(AsmBlock.class.getSuperclass().equals(CompoundStatement.class));
		Assert.isTrue(AsmStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(AssertExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(AssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AssocArrayLiteralExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(AttribDeclaration.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(BaseClass.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(BinExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(BoolExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(BreakStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(CallExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(CaseStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(CaseRangeStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(CastExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(CatAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(Catch.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(CatExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ClassDeclaration.class.getSuperclass().equals(AggregateDeclaration.class));
		Assert.isTrue(ClassInfoDeclaration.class.getSuperclass().equals(VarDeclaration.class));
		Assert.isTrue(CmpExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ComExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(CommaExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(CompileDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(CompileExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(CompileStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ComplexExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(CompoundStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(CondExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(Condition.class.getSuperclass().equals(ASTRangeLessNode.class));
		Assert.isTrue(ConditionalDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(ConditionalStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ContinueStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(CtorDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(DebugCondition.class.getSuperclass().equals(DVCondition.class));
		Assert.isTrue(DebugSymbol.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(Declaration.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(DeclarationExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(DeclarationStatement.class.getSuperclass().equals(ExpStatement.class));
		Assert.isTrue(DefaultStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(DelegateExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DeleteDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(DeleteExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DivAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(DivExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(DollarExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(DoStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(DotExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(DotIdExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DotTemplateExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DotTemplateInstanceExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DotTypeExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DotVarExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(Dsymbol.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(DsymbolExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(DtorDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(EnumDeclaration.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(EnumMember.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(EqualExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ExpInitializer.class.getSuperclass().equals(Initializer.class));
		Assert.isTrue(Expression.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(ExpStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(FileExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(ForeachRangeStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ForeachStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ForStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(FuncAliasDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(FuncDeclaration.class.getSuperclass().equals(Declaration.class));
		Assert.isTrue(FuncExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(FuncLiteralDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(GotoCaseStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(GotoDefaultStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(GotoStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(HaltExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(IdentifierExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(IdentityExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(IfStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(IftypeCondition.class.getSuperclass().equals(Condition.class));
		Assert.isTrue(IsExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(Import.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(IndexExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(InExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(Initializer.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(IntegerExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(InterfaceDeclaration.class.getSuperclass().equals(ClassDeclaration.class));
		Assert.isTrue(InvariantDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(LabelDsymbol.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(LabelStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(LinkDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(MinAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(MinExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ModAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ModExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(Modifier.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(Module.class.getSuperclass().equals(Package.class));
		Assert.isTrue(ModuleDeclaration.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(ModuleInfoDeclaration.class.getSuperclass().equals(VarDeclaration.class));
		Assert.isTrue(MulAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(MulExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(NegExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(NewAnonClassExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(NewDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(NewExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(NotExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(NullExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(OnScopeStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(OrAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(OrExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(OrOrExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(Package.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(PostExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(PragmaDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(PragmaStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ProtDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(PtrExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(RealExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(RemoveExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ReturnStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ScopeDsymbol.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(ScopeExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(ScopeStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ShlAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ShlExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ShrAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ShrExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(SliceExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(Statement.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(StaticAssert.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(StaticAssertStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(StaticCtorDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(StaticDtorDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(StaticIfCondition.class.getSuperclass().equals(Condition.class));
		Assert.isTrue(StaticIfDeclaration.class.getSuperclass().equals(ConditionalDeclaration.class));
		Assert.isTrue(StorageClassDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(StringExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(StructDeclaration.class.getSuperclass().equals(AggregateDeclaration.class));
		Assert.isTrue(StructInitializer.class.getSuperclass().equals(Initializer.class));
		Assert.isTrue(SuperExp.class.getSuperclass().equals(ThisExp.class));
		Assert.isTrue(SwitchStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(SymOffExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(SynchronizedStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(TemplateAliasParameter.class.getSuperclass().equals(TemplateParameter.class));
		Assert.isTrue(TemplateDeclaration.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(TemplateExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(TemplateInstance.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(TemplateInstanceWrapper.class.getSuperclass().equals(IdentifierExp.class));
		Assert.isTrue(TemplateMixin.class.getSuperclass().equals(TemplateInstance.class));
		Assert.isTrue(TemplateParameter.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(TemplateTupleParameter.class.getSuperclass().equals(TemplateParameter.class));
		Assert.isTrue(TemplateTypeParameter.class.getSuperclass().equals(TemplateParameter.class));
		Assert.isTrue(TemplateValueParameter.class.getSuperclass().equals(TemplateParameter.class));
		Assert.isTrue(ThisDeclaration.class.getSuperclass().equals(VarDeclaration.class));
		Assert.isTrue(ThisExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(ThrowStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(TraitsExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(TryCatchStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(TryFinallyStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(Tuple.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(TupleDeclaration.class.getSuperclass().equals(Declaration.class));
		Assert.isTrue(TupleExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(Type.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(TypeAArray.class.getSuperclass().equals(TypeArray.class));
		Assert.isTrue(TypeBasic.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeClass.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeDArray.class.getSuperclass().equals(TypeArray.class));
		Assert.isTrue(TypedefDeclaration.class.getSuperclass().equals(Declaration.class));
		Assert.isTrue(TypeDelegate.class.getSuperclass().equals(TypeNext.class));
		Assert.isTrue(TypeEnum.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(TypeFunction.class.getSuperclass().equals(TypeNext.class));
		Assert.isTrue(TypeIdentifier.class.getSuperclass().equals(TypeQualified.class));
		Assert.isTrue(TypeidExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(TypeInfoArrayDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoAssociativeArrayDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoClassDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoDeclaration.class.getSuperclass().equals(VarDeclaration.class));
		Assert.isTrue(TypeInfoDelegateDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoEnumDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoFunctionDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoInterfaceDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoPointerDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoStaticArrayDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoStructDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoTypedefDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInstance.class.getSuperclass().equals(TypeQualified.class));
		Assert.isTrue(TypePointer.class.getSuperclass().equals(TypeNext.class));
		Assert.isTrue(TypeQualified.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeSArray.class.getSuperclass().equals(TypeArray.class));
		Assert.isTrue(TypeSlice.class.getSuperclass().equals(TypeNext.class));
		Assert.isTrue(TypeStruct.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeTuple.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeTypedef.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeTypeof.class.getSuperclass().equals(TypeQualified.class));
		Assert.isTrue(UAddExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(UnaExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(UnionDeclaration.class.getSuperclass().equals(StructDeclaration.class));
		Assert.isTrue(UnitTestDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(UnrolledLoopStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(UshrAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(UshrExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(VarDeclaration.class.getSuperclass().equals(Declaration.class));
		Assert.isTrue(VarExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(Version.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(VersionCondition.class.getSuperclass().equals(DVCondition.class));
		Assert.isTrue(VersionSymbol.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(VoidInitializer.class.getSuperclass().equals(Initializer.class));
		Assert.isTrue(VolatileStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(WhileStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(WithScopeSymbol.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(WithStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(XorAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(XorExp.class.getSuperclass().equals(BinExp.class));
		
		
		Assert.isTrue(ASTDmdNode.class.getSuperclass().equals(ASTNode.class));
		Assert.isTrue(ASTRangeLessNode.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(AddAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AddExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AddrExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(AggregateDeclaration.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(AliasDeclaration.class.getSuperclass().equals(Declaration.class));
		Assert.isTrue(AliasThis.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(AlignDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(AndAndExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AndAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AndExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AnonDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(AnonymousAggregateDeclaration.class.getSuperclass().equals(AggregateDeclaration.class));
		Assert.isTrue(Argument.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(ArrayExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(ArrayInitializer.class.getSuperclass().equals(Initializer.class));
		Assert.isTrue(ArrayLengthExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(ArrayLiteralExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(ArrayScopeSymbol.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(AsmBlock.class.getSuperclass().equals(CompoundStatement.class));
		Assert.isTrue(AsmStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(AssertExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(AssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(AssocArrayLiteralExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(AttribDeclaration.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(BaseClass.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(BinExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(BoolExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(BreakStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(CallExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(CaseStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(CaseRangeStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(CastExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(CatAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(Catch.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(CatExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ClassDeclaration.class.getSuperclass().equals(AggregateDeclaration.class));
		Assert.isTrue(ClassInfoDeclaration.class.getSuperclass().equals(VarDeclaration.class));
		Assert.isTrue(CmpExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ComExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(CommaExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(CompileDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(CompileExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(CompileStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ComplexExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(CompoundStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(CondExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(Condition.class.getSuperclass().equals(ASTRangeLessNode.class));
		Assert.isTrue(ConditionalDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(ConditionalStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ContinueStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(CtorDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(DebugCondition.class.getSuperclass().equals(DVCondition.class));
		Assert.isTrue(DebugSymbol.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(Declaration.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(DeclarationExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(DeclarationStatement.class.getSuperclass().equals(ExpStatement.class));
		Assert.isTrue(DefaultStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(DelegateExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DeleteDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(DeleteExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DivAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(DivExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(DollarExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(DoStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(DotExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(DotIdExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DotTemplateExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DotTemplateInstanceExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DotTypeExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(DotVarExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(Dsymbol.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(DsymbolExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(DtorDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(EnumDeclaration.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(EnumMember.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(EqualExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ExpInitializer.class.getSuperclass().equals(Initializer.class));
		Assert.isTrue(Expression.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(ExpStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(FileExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(ForeachRangeStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ForeachStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ForStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(FuncAliasDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(FuncDeclaration.class.getSuperclass().equals(Declaration.class));
		Assert.isTrue(FuncExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(FuncLiteralDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(GotoCaseStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(GotoDefaultStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(GotoStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(HaltExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(IdentifierExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(IdentityExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(IfStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(IftypeCondition.class.getSuperclass().equals(Condition.class));
		Assert.isTrue(IsExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(Import.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(IndexExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(InExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(Initializer.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(IntegerExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(InterfaceDeclaration.class.getSuperclass().equals(ClassDeclaration.class));
		Assert.isTrue(InvariantDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(LabelDsymbol.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(LabelStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(LinkDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(MinAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(MinExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ModAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ModExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(Modifier.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(Module.class.getSuperclass().equals(Package.class));
		Assert.isTrue(ModuleDeclaration.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(ModuleInfoDeclaration.class.getSuperclass().equals(VarDeclaration.class));
		Assert.isTrue(MulAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(MulExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(NegExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(NewAnonClassExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(NewDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(NewExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(NotExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(NullExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(OnScopeStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(OrAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(OrExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(OrOrExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(Package.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(PostExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(PragmaDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(PragmaStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ProtDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(PtrExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(RealExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(RemoveExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ReturnStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ScopeDsymbol.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(ScopeExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(ScopeStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(ShlAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ShlExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ShrAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(ShrExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(SliceExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(Statement.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(StaticAssert.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(StaticAssertStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(StaticCtorDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(StaticDtorDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(StaticIfCondition.class.getSuperclass().equals(Condition.class));
		Assert.isTrue(StaticIfDeclaration.class.getSuperclass().equals(ConditionalDeclaration.class));
		Assert.isTrue(StorageClassDeclaration.class.getSuperclass().equals(AttribDeclaration.class));
		Assert.isTrue(StringExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(StructDeclaration.class.getSuperclass().equals(AggregateDeclaration.class));
		Assert.isTrue(StructInitializer.class.getSuperclass().equals(Initializer.class));
		Assert.isTrue(SuperExp.class.getSuperclass().equals(ThisExp.class));
		Assert.isTrue(SwitchStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(SymOffExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(SynchronizedStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(TemplateAliasParameter.class.getSuperclass().equals(TemplateParameter.class));
		Assert.isTrue(TemplateDeclaration.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(TemplateExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(TemplateInstance.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(TemplateInstanceWrapper.class.getSuperclass().equals(IdentifierExp.class));
		Assert.isTrue(TemplateMixin.class.getSuperclass().equals(TemplateInstance.class));
		Assert.isTrue(TemplateParameter.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(TemplateTupleParameter.class.getSuperclass().equals(TemplateParameter.class));
		Assert.isTrue(TemplateTypeParameter.class.getSuperclass().equals(TemplateParameter.class));
		Assert.isTrue(TemplateValueParameter.class.getSuperclass().equals(TemplateParameter.class));
		Assert.isTrue(ThisDeclaration.class.getSuperclass().equals(VarDeclaration.class));
		Assert.isTrue(ThisExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(ThrowStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(TraitsExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(TryCatchStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(TryFinallyStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(Tuple.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(TupleDeclaration.class.getSuperclass().equals(Declaration.class));
		Assert.isTrue(TupleExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(Type.class.getSuperclass().equals(ASTDmdNode.class));
		Assert.isTrue(TypeAArray.class.getSuperclass().equals(TypeArray.class));
		Assert.isTrue(TypeBasic.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeClass.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeDArray.class.getSuperclass().equals(TypeArray.class));
		Assert.isTrue(TypedefDeclaration.class.getSuperclass().equals(Declaration.class));
		Assert.isTrue(TypeDelegate.class.getSuperclass().equals(TypeNext.class));
		Assert.isTrue(TypeEnum.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(TypeFunction.class.getSuperclass().equals(TypeNext.class));
		Assert.isTrue(TypeIdentifier.class.getSuperclass().equals(TypeQualified.class));
		Assert.isTrue(TypeidExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(TypeInfoArrayDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoAssociativeArrayDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoClassDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoDeclaration.class.getSuperclass().equals(VarDeclaration.class));
		Assert.isTrue(TypeInfoDelegateDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoEnumDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoFunctionDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoInterfaceDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoPointerDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoStaticArrayDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoStructDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInfoTypedefDeclaration.class.getSuperclass().equals(TypeInfoDeclaration.class));
		Assert.isTrue(TypeInstance.class.getSuperclass().equals(TypeQualified.class));
		Assert.isTrue(TypePointer.class.getSuperclass().equals(TypeNext.class));
		Assert.isTrue(TypeQualified.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeSArray.class.getSuperclass().equals(TypeArray.class));
		Assert.isTrue(TypeSlice.class.getSuperclass().equals(TypeNext.class));
		Assert.isTrue(TypeStruct.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeTuple.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeTypedef.class.getSuperclass().equals(Type.class));
		Assert.isTrue(TypeTypeof.class.getSuperclass().equals(TypeQualified.class));
		Assert.isTrue(UAddExp.class.getSuperclass().equals(UnaExp.class));
		Assert.isTrue(UnaExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(UnionDeclaration.class.getSuperclass().equals(StructDeclaration.class));
		Assert.isTrue(UnitTestDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(UnrolledLoopStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(UshrAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(UshrExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(VarDeclaration.class.getSuperclass().equals(Declaration.class));
		Assert.isTrue(VarExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(Version.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(VersionCondition.class.getSuperclass().equals(DVCondition.class));
		Assert.isTrue(VersionSymbol.class.getSuperclass().equals(Dsymbol.class));
		Assert.isTrue(VoidInitializer.class.getSuperclass().equals(Initializer.class));
		Assert.isTrue(VolatileStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(WhileStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(WithScopeSymbol.class.getSuperclass().equals(ScopeDsymbol.class));
		Assert.isTrue(WithStatement.class.getSuperclass().equals(Statement.class));
		Assert.isTrue(XorAssignExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(XorExp.class.getSuperclass().equals(BinExp.class));
		Assert.isTrue(FileInitExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(LineInitExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(PostBlitDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(TemplateThisParameter.class.getSuperclass().equals(TemplateTypeParameter.class));
		Assert.isTrue(TypeReturn.class.getSuperclass().equals(TypeQualified.class));
		Assert.isTrue(FileInitExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(LineInitExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(PostBlitDeclaration.class.getSuperclass().equals(FuncDeclaration.class));
		Assert.isTrue(TemplateThisParameter.class.getSuperclass().equals(TemplateTypeParameter.class));
		Assert.isTrue(TypeReturn.class.getSuperclass().equals(TypeQualified.class));
		Assert.isTrue(DefaultInitExp.class.getSuperclass().equals(Expression.class));
		Assert.isTrue(DefaultInitExp.class.getSuperclass().equals(Expression.class));
	}
	
	
	@Override
	public boolean visit(ASTNode node) {
		//return true;
		return visit((IASTNode) node);
	}
	@Override
	public void endVisit(ASTNode node) {
		// Default implementation: do nothing
	}
	
	@Override
	public boolean visit(ASTDmdNode elem) {
		return visit((ASTNode) elem);
	}
	
	public boolean visit(ASTRangeLessNode elem) {
		return visit((ASTDmdNode) elem);
	}
	
/*	public boolean visit(AbstractElement elem) {
		//ensureVisitIsNotDirectVisit(elem);
		return visit((ASTNode) elem);
	}
 */
	@Override
	public boolean visit(AddAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(AddExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(AddrExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(AggregateDeclaration node) {
		return visit((ScopeDsymbol) node);
	}
	@Override
	public boolean visit(AliasDeclaration node) {
		return visit((Declaration) node);
	}
	@Override
	public boolean visit(AliasThis node) {
		return visit((Dsymbol) node);
	}
	@Override
	public boolean visit(AlignDeclaration node) {
		return visit((AttribDeclaration) node);
	}
	@Override
	public boolean visit(AndAndExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(AndAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(AndExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(AnonDeclaration node) {
		return visit((AttribDeclaration) node);
	}
	@Override
	public boolean visit(AnonymousAggregateDeclaration node) {
		return visit((AggregateDeclaration) node);
	}
	@Override
	public boolean visit(Argument node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(ArrayExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(ArrayInitializer node) {
		return visit((Initializer) node);
	}
	@Override
	public boolean visit(ArrayLengthExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(ArrayLiteralExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(ArrayScopeSymbol node) {
		return visit((ScopeDsymbol) node);
	}
	@Override
	public boolean visit(AsmBlock node) {
		return visit((CompoundStatement) node);
	}
	@Override
	public boolean visit(AsmStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(AssertExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(AssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(AssocArrayLiteralExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(AttribDeclaration node) {
		return visit((Dsymbol) node);
	}
	@Override
	public boolean visit(BaseClass node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(BinExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(BoolExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(BreakStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(CallExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(CaseStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(CaseRangeStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(CastExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(CatAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(Catch node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(CatExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(ClassDeclaration node) {
		return visit((AggregateDeclaration) node);
	}
	@Override
	public boolean visit(ClassInfoDeclaration node) {
		return visit((VarDeclaration) node);
	}
	@Override
	public boolean visit(CmpExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(ComExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(CommaExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(CompileDeclaration node) {
		return visit((AttribDeclaration) node);
	}
	@Override
	public boolean visit(CompileExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(CompileStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(ComplexExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(CompoundStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(CondExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(Condition node) {
		return visit((ASTRangeLessNode) node);
	}
	@Override
	public boolean visit(ConditionalDeclaration node) {
		return visit((AttribDeclaration) node);
	}
	@Override
	public boolean visit(ConditionalStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(ContinueStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(CtorDeclaration node) {
		return visit((FuncDeclaration) node);
	}
	@Override
	public boolean visit(DebugCondition node) {
		return visit((DVCondition) node);
	}
	@Override
	public boolean visit(DebugSymbol node) {
		return visit((Dsymbol) node);
	}
	@Override
	public boolean visit(Declaration node) {
		return visit((Dsymbol) node);
	}
	@Override
	public boolean visit(DeclarationExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(DeclarationStatement node) {
		return visit((ExpStatement) node);
	}
	@Override
	public boolean visit(DefaultStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(DelegateExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(DeleteDeclaration node) {
		return visit((FuncDeclaration) node);
	}
	@Override
	public boolean visit(DeleteExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(DivAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(DivExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(DollarExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(DoStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(DotExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(DotIdExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(DotTemplateExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(DotTemplateInstanceExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(DotTypeExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(DotVarExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(Dsymbol node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(DsymbolExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(DtorDeclaration node) {
		return visit((FuncDeclaration) node);
	}
	@Override
	public boolean visit(EnumDeclaration node) {
		return visit((ScopeDsymbol) node);
	}
	@Override
	public boolean visit(EnumMember node) {
		return visit((Dsymbol) node);
	}
	@Override
	public boolean visit(EqualExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(ExpInitializer node) {
		return visit((Initializer) node);
	}
	@Override
	public boolean visit(Expression node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(ExpStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(FileExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(ForeachRangeStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(ForeachStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(ForStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(FuncAliasDeclaration node) {
		return visit((FuncDeclaration) node);
	}
	@Override
	public boolean visit(FuncDeclaration node) {
		return visit((Declaration) node);
	}
	@Override
	public boolean visit(FuncExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(FuncLiteralDeclaration node) {
		return visit((FuncDeclaration) node);
	}
	@Override
	public boolean visit(GotoCaseStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(GotoDefaultStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(GotoStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(HaltExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(IdentifierExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(IdentityExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(IfStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(IftypeCondition node) {
		return visit((Condition) node);
	}
	@Override
	public boolean visit(IsExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(Import node) {
		return visit((Dsymbol) node);
	}
	@Override
	public boolean visit(IndexExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(InExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(Initializer node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(IntegerExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(InterfaceDeclaration node) {
		return visit((ClassDeclaration) node);
	}
	@Override
	public boolean visit(InvariantDeclaration node) {
		return visit((FuncDeclaration) node);
	}
	@Override
	public boolean visit(LabelDsymbol node) {
		return visit((Dsymbol) node);
	}
	@Override
	public boolean visit(LabelStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(LinkDeclaration node) {
		return visit((AttribDeclaration) node);
	}
	@Override
	public boolean visit(MinAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(MinExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(ModAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(ModExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(Modifier node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(Module node) {
		return visit((Package) node);
	}
	@Override
	public boolean visit(ModuleDeclaration node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(ModuleInfoDeclaration node) {
		return visit((VarDeclaration) node);
	}
	@Override
	public boolean visit(MulAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(MulExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(NegExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(NewAnonClassExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(NewDeclaration node) {
		return visit((FuncDeclaration) node);
	}
	@Override
	public boolean visit(NewExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(NotExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(NullExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(OnScopeStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(OrAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(OrExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(OrOrExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(Package node) {
		return visit((ScopeDsymbol) node);
	}
	@Override
	public boolean visit(PostExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(PragmaDeclaration node) {
		return visit((AttribDeclaration) node);
	}
	@Override
	public boolean visit(PragmaStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(ProtDeclaration node) {
		return visit((AttribDeclaration) node);
	}
	@Override
	public boolean visit(PtrExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(RealExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(RemoveExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(ReturnStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(ScopeDsymbol node) {
		return visit((Dsymbol) node);
	}
	@Override
	public boolean visit(ScopeExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(ScopeStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(ShlAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(ShlExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(ShrAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(ShrExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(SliceExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(Statement node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(StaticAssert node) {
		return visit((Dsymbol) node);
	}
	@Override
	public boolean visit(StaticAssertStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(StaticCtorDeclaration node) {
		return visit((FuncDeclaration) node);
	}
	@Override
	public boolean visit(StaticDtorDeclaration node) {
		return visit((FuncDeclaration) node);
	}
	@Override
	public boolean visit(StaticIfCondition node) {
		return visit((Condition) node);
	}
	@Override
	public boolean visit(StaticIfDeclaration node) {
		return visit((ConditionalDeclaration) node);
	}
	@Override
	public boolean visit(StorageClassDeclaration node) {
		return visit((AttribDeclaration) node);
	}
	@Override
	public boolean visit(StringExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(StructDeclaration node) {
		return visit((AggregateDeclaration) node);
	}
	@Override
	public boolean visit(StructInitializer node) {
		return visit((Initializer) node);
	}
	@Override
	public boolean visit(SuperExp node) {
		return visit((ThisExp) node);
	}
	@Override
	public boolean visit(SwitchStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(SymOffExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(SynchronizedStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(TemplateAliasParameter node) {
		return visit((TemplateParameter) node);
	}
	@Override
	public boolean visit(TemplateDeclaration node) {
		return visit((ScopeDsymbol) node);
	}
	@Override
	public boolean visit(TemplateExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(TemplateInstance node) {
		return visit((ScopeDsymbol) node);
	}
	@Override
	public boolean visit(TemplateInstanceWrapper node) {
		return visit((IdentifierExp) node);
	}
	@Override
	public boolean visit(TemplateMixin node) {
		return visit((TemplateInstance) node);
	}
	@Override
	public boolean visit(TemplateParameter node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(TemplateTupleParameter node) {
		return visit((TemplateParameter) node);
	}
	@Override
	public boolean visit(TemplateTypeParameter node) {
		return visit((TemplateParameter) node);
	}
	@Override
	public boolean visit(TemplateValueParameter node) {
		return visit((TemplateParameter) node);
	}
	@Override
	public boolean visit(ThisDeclaration node) {
		return visit((VarDeclaration) node);
	}
	@Override
	public boolean visit(ThisExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(ThrowStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(TraitsExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(TryCatchStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(TryFinallyStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(Tuple node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(TupleDeclaration node) {
		return visit((Declaration) node);
	}
	@Override
	public boolean visit(TupleExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(Type node) {
		return visit((ASTDmdNode) node);
	}
	@Override
	public boolean visit(TypeAArray node) {
		return visit((TypeArray) node);
	}
	@Override
	public boolean visit(TypeBasic node) {
		return visit((Type) node);
	}
	@Override
	public boolean visit(TypeClass node) {
		return visit((Type) node);
	}
	@Override
	public boolean visit(TypeDArray node) {
		return visit((TypeArray) node);
	}
	@Override
	public boolean visit(TypedefDeclaration node) {
		return visit((Declaration) node);
	}
	@Override
	public boolean visit(TypeDelegate node) {
		return visit((TypeNext) node);
	}
	@Override
	public boolean visit(TypeEnum node) {
		return visit((Type) node);
	}
	@Override
	public boolean visit(TypeExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(TypeFunction node) {
		return visit((TypeNext) node);
	}
	@Override
	public boolean visit(TypeIdentifier node) {
		return visit((TypeQualified) node);
	}
	@Override
	public boolean visit(TypeidExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(TypeInfoArrayDeclaration node) {
		return visit((TypeInfoDeclaration) node);
	}
	@Override
	public boolean visit(TypeInfoAssociativeArrayDeclaration node) {
		return visit((TypeInfoDeclaration) node);
	}
	@Override
	public boolean visit(TypeInfoClassDeclaration node) {
		return visit((TypeInfoDeclaration) node);
	}
	@Override
	public boolean visit(TypeInfoDeclaration node) {
		return visit((VarDeclaration) node);
	}
	@Override
	public boolean visit(TypeInfoDelegateDeclaration node) {
		return visit((TypeInfoDeclaration) node);
	}
	@Override
	public boolean visit(TypeInfoEnumDeclaration node) {
		return visit((TypeInfoDeclaration) node);
	}
	@Override
	public boolean visit(TypeInfoFunctionDeclaration node) {
		return visit((TypeInfoDeclaration) node);
	}
	@Override
	public boolean visit(TypeInfoInterfaceDeclaration node) {
		return visit((TypeInfoDeclaration) node);
	}
	@Override
	public boolean visit(TypeInfoPointerDeclaration node) {
		return visit((TypeInfoDeclaration) node);
	}
	@Override
	public boolean visit(TypeInfoStaticArrayDeclaration node) {
		return visit((TypeInfoDeclaration) node);
	}
	@Override
	public boolean visit(TypeInfoStructDeclaration node) {
		return visit((TypeInfoDeclaration) node);
	}
	@Override
	public boolean visit(TypeInfoTypedefDeclaration node) {
		return visit((TypeInfoDeclaration) node);
	}
	@Override
	public boolean visit(TypeInstance node) {
		return visit((TypeQualified) node);
	}
	@Override
	public boolean visit(TypePointer node) {
		return visit((TypeNext) node);
	}
	@Override
	public boolean visit(TypeQualified node) {
		return visit((Type) node);
	}
	@Override
	public boolean visit(TypeSArray node) {
		return visit((TypeArray) node);
	}
	@Override
	public boolean visit(TypeSlice node) {
		return visit((TypeNext) node);
	}
	@Override
	public boolean visit(TypeStruct node) {
		return visit((Type) node);
	}
	@Override
	public boolean visit(TypeTuple node) {
		return visit((Type) node);
	}
	@Override
	public boolean visit(TypeTypedef node) {
		return visit((Type) node);
	}
	@Override
	public boolean visit(TypeTypeof node) {
		return visit((TypeQualified) node);
	}
	@Override
	public boolean visit(UAddExp node) {
		return visit((UnaExp) node);
	}
	@Override
	public boolean visit(UnaExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(UnionDeclaration node) {
		return visit((StructDeclaration) node);
	}
	@Override
	public boolean visit(UnitTestDeclaration node) {
		return visit((FuncDeclaration) node);
	}
	@Override
	public boolean visit(UnrolledLoopStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(UshrAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(UshrExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(VarDeclaration node) {
		return visit((Declaration) node);
	}
	@Override
	public boolean visit(VarExp node) {
		return visit((Expression) node);
	}
	@Override
	public boolean visit(Version node) {
		return visit((Dsymbol) node);
	}
	@Override
	public boolean visit(VersionCondition node) {
		return visit((DVCondition) node);
	}
	@Override
	public boolean visit(VersionSymbol node) {
		return visit((Dsymbol) node);
	}
	@Override
	public boolean visit(VoidInitializer node) {
		return visit((Initializer) node);
	}
	@Override
	public boolean visit(VolatileStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(WhileStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(WithScopeSymbol node) {
		return visit((ScopeDsymbol) node);
	}
	@Override
	public boolean visit(WithStatement node) {
		return visit((Statement) node);
	}
	@Override
	public boolean visit(XorAssignExp node) {
		return visit((BinExp) node);
	}
	@Override
	public boolean visit(XorExp node) {
		return visit((BinExp) node);
	}
	
	
	/* ====================================================== */
	
	public void endVisit(ASTDmdNode elem) {
		endVisit((ASTNode) elem);
	}
	
	public void endVisit(ASTRangeLessNode elem) {
		endVisit((ASTDmdNode) elem);
	}
	
/*	public void endVisit(AbstractElement elem) {
		//ensureVisitIsNotDirectVisit(elem);
		endVisit((ASTNode) elem);
	}
 */
	@Override
	public void endVisit(AddAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(AddExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(AddrExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(AggregateDeclaration node) {
		endVisit((ScopeDsymbol) node);
	}
	@Override
	public void endVisit(AliasDeclaration node) {
		endVisit((Declaration) node);
	}
	@Override
	public void endVisit(AliasThis node) {
		endVisit((Dsymbol) node);
	}
	@Override
	public void endVisit(AlignDeclaration node) {
		endVisit((AttribDeclaration) node);
	}
	@Override
	public void endVisit(AndAndExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(AndAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(AndExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(AnonDeclaration node) {
		endVisit((AttribDeclaration) node);
	}
	@Override
	public void endVisit(AnonymousAggregateDeclaration node) {
		endVisit((AggregateDeclaration) node);
	}
	@Override
	public void endVisit(Argument node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(ArrayExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(ArrayInitializer node) {
		endVisit((Initializer) node);
	}
	@Override
	public void endVisit(ArrayLengthExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(ArrayLiteralExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(ArrayScopeSymbol node) {
		endVisit((ScopeDsymbol) node);
	}
	@Override
	public void endVisit(AsmBlock node) {
		endVisit((CompoundStatement) node);
	}
	@Override
	public void endVisit(AsmStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(AssertExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(AssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(AssocArrayLiteralExp node) {
		endVisit((Expression) node);
	}
	@Override
	
	public void endVisit(AttribDeclaration node) {
		endVisit((Dsymbol) node);
	}
	@Override
	public void endVisit(BaseClass node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(BinExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(BoolExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(BreakStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(CallExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(CaseStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(CaseRangeStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(CastExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(CatAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(Catch node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(CatExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(ClassDeclaration node) {
		endVisit((AggregateDeclaration) node);
	}
	@Override
	public void endVisit(ClassInfoDeclaration node) {
		endVisit((VarDeclaration) node);
	}
	@Override
	public void endVisit(CmpExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(ComExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(CommaExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(CompileDeclaration node) {
		endVisit((AttribDeclaration) node);
	}
	@Override
	public void endVisit(CompileExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(CompileStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(ComplexExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(CompoundStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(CondExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(Condition node) {
		endVisit((ASTRangeLessNode) node);
	}
	@Override
	public void endVisit(ConditionalDeclaration node) {
		endVisit((AttribDeclaration) node);
	}
	@Override
	public void endVisit(ConditionalStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(ContinueStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(CtorDeclaration node) {
		endVisit((FuncDeclaration) node);
	}
	@Override
	public void endVisit(DebugCondition node) {
		endVisit((DVCondition) node);
	}
	@Override
	public void endVisit(DebugSymbol node) {
		endVisit((Dsymbol) node);
	}
	@Override
	public void endVisit(Declaration node) {
		endVisit((Dsymbol) node);
	}
	@Override
	public void endVisit(DeclarationExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(DeclarationStatement node) {
		endVisit((ExpStatement) node);
	}
	@Override
	public void endVisit(DefaultStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(DelegateExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(DeleteDeclaration node) {
		endVisit((FuncDeclaration) node);
	}
	@Override
	public void endVisit(DeleteExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(DivAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(DivExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(DollarExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(DoStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(DotExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(DotIdExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(DotTemplateExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(DotTemplateInstanceExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(DotTypeExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(DotVarExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(Dsymbol node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(DsymbolExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(DtorDeclaration node) {
		endVisit((FuncDeclaration) node);
	}
	@Override
	public void endVisit(EnumDeclaration node) {
		endVisit((ScopeDsymbol) node);
	}
	@Override
	public void endVisit(EnumMember node) {
		endVisit((Dsymbol) node);
	}
	@Override
	public void endVisit(EqualExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(ExpInitializer node) {
		endVisit((Initializer) node);
	}
	@Override
	public void endVisit(Expression node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(ExpStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(FileExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(ForeachRangeStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(ForeachStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(ForStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(FuncAliasDeclaration node) {
		endVisit((FuncDeclaration) node);
	}
	@Override
	public void endVisit(FuncDeclaration node) {
		endVisit((Declaration) node);
	}
	@Override
	public void endVisit(FuncExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(FuncLiteralDeclaration node) {
		endVisit((FuncDeclaration) node);
	}
	@Override
	public void endVisit(GotoCaseStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(GotoDefaultStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(GotoStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(HaltExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(IdentifierExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(IdentityExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(IfStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(IftypeCondition node) {
		endVisit((Condition) node);
	}
	@Override
	public void endVisit(IsExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(Import node) {
		endVisit((Dsymbol) node);
	}
	@Override
	public void endVisit(IndexExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(InExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(Initializer node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(IntegerExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(InterfaceDeclaration node) {
		endVisit((ClassDeclaration) node);
	}
	@Override
	public void endVisit(InvariantDeclaration node) {
		endVisit((FuncDeclaration) node);
	}
	@Override
	public void endVisit(LabelDsymbol node) {
		endVisit((Dsymbol) node);
	}
	@Override
	public void endVisit(LabelStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(LinkDeclaration node) {
		endVisit((AttribDeclaration) node);
	}
	@Override
	public void endVisit(MinAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(MinExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(ModAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(ModExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(Modifier node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(Module node) {
		endVisit((Package) node);
	}
	@Override
	public void endVisit(ModuleDeclaration node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(ModuleInfoDeclaration node) {
		endVisit((VarDeclaration) node);
	}
	@Override
	public void endVisit(MulAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(MulExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(NegExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(NewAnonClassExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(NewDeclaration node) {
		endVisit((FuncDeclaration) node);
	}
	@Override
	public void endVisit(NewExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(NotExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(NullExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(OnScopeStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(OrAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(OrExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(OrOrExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(Package node) {
		endVisit((ScopeDsymbol) node);
	}
	@Override
	public void endVisit(PostExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(PragmaDeclaration node) {
		endVisit((AttribDeclaration) node);
	}
	@Override
	public void endVisit(PragmaStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(ProtDeclaration node) {
		endVisit((AttribDeclaration) node);
	}
	@Override
	public void endVisit(PtrExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(RealExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(RemoveExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(ReturnStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(ScopeDsymbol node) {
		endVisit((Dsymbol) node);
	}
	@Override
	public void endVisit(ScopeExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(ScopeStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(ShlAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(ShlExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(ShrAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(ShrExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(SliceExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(Statement node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(StaticAssert node) {
		endVisit((Dsymbol) node);
	}
	@Override
	public void endVisit(StaticAssertStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(StaticCtorDeclaration node) {
		endVisit((FuncDeclaration) node);
	}
	@Override
	public void endVisit(StaticDtorDeclaration node) {
		endVisit((FuncDeclaration) node);
	}
	@Override
	public void endVisit(StaticIfCondition node) {
		endVisit((Condition) node);
	}
	@Override
	public void endVisit(StaticIfDeclaration node) {
		endVisit((ConditionalDeclaration) node);
	}
	@Override
	public void endVisit(StorageClassDeclaration node) {
		endVisit((AttribDeclaration) node);
	}
	@Override
	public void endVisit(StringExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(StructDeclaration node) {
		endVisit((AggregateDeclaration) node);
	}
	@Override
	public void endVisit(StructInitializer node) {
		endVisit((Initializer) node);
	}
	@Override
	public void endVisit(SuperExp node) {
		endVisit((ThisExp) node);
	}
	@Override
	public void endVisit(SwitchStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(SymOffExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(SynchronizedStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(TemplateAliasParameter node) {
		endVisit((TemplateParameter) node);
	}
	@Override
	public void endVisit(TemplateDeclaration node) {
		endVisit((ScopeDsymbol) node);
	}
	@Override
	public void endVisit(TemplateExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(TemplateInstance node) {
		endVisit((ScopeDsymbol) node);
	}
	@Override
	public void endVisit(TemplateInstanceWrapper node) {
		endVisit((IdentifierExp) node);
	}
	@Override
	public void endVisit(TemplateMixin node) {
		endVisit((TemplateInstance) node);
	}
	@Override
	public void endVisit(TemplateParameter node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(TemplateTupleParameter node) {
		endVisit((TemplateParameter) node);
	}
	@Override
	public void endVisit(TemplateTypeParameter node) {
		endVisit((TemplateParameter) node);
	}
	@Override
	public void endVisit(TemplateValueParameter node) {
		endVisit((TemplateParameter) node);
	}
	@Override
	public void endVisit(ThisDeclaration node) {
		endVisit((VarDeclaration) node);
	}
	@Override
	public void endVisit(ThisExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(ThrowStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(TraitsExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(TryCatchStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(TryFinallyStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(Tuple node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(TupleDeclaration node) {
		endVisit((Declaration) node);
	}
	@Override
	public void endVisit(TupleExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(Type node) {
		endVisit((ASTDmdNode) node);
	}
	@Override
	public void endVisit(TypeAArray node) {
		endVisit((TypeArray) node);
	}
	@Override
	public void endVisit(TypeBasic node) {
		endVisit((Type) node);
	}
	@Override
	public void endVisit(TypeClass node) {
		endVisit((Type) node);
	}
	@Override
	public void endVisit(TypeDArray node) {
		endVisit((TypeArray) node);
	}
	@Override
	public void endVisit(TypedefDeclaration node) {
		endVisit((Declaration) node);
	}
	@Override
	public void endVisit(TypeDelegate node) {
		endVisit((TypeNext) node);
	}
	@Override
	public void endVisit(TypeEnum node) {
		endVisit((Type) node);
	}
	@Override
	public void endVisit(TypeExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(TypeFunction node) {
		endVisit((TypeNext) node);
	}
	@Override
	public void endVisit(TypeIdentifier node) {
		endVisit((TypeQualified) node);
	}
	@Override
	public void endVisit(TypeidExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(TypeInfoArrayDeclaration node) {
		endVisit((TypeInfoDeclaration) node);
	}
	@Override
	public void endVisit(TypeInfoAssociativeArrayDeclaration node) {
		endVisit((TypeInfoDeclaration) node);
	}
	@Override
	public void endVisit(TypeInfoClassDeclaration node) {
		endVisit((TypeInfoDeclaration) node);
	}
	@Override
	public void endVisit(TypeInfoDeclaration node) {
		endVisit((VarDeclaration) node);
	}
	@Override
	public void endVisit(TypeInfoDelegateDeclaration node) {
		endVisit((TypeInfoDeclaration) node);
	}
	@Override
	public void endVisit(TypeInfoEnumDeclaration node) {
		endVisit((TypeInfoDeclaration) node);
	}
	@Override
	public void endVisit(TypeInfoFunctionDeclaration node) {
		endVisit((TypeInfoDeclaration) node);
	}
	@Override
	public void endVisit(TypeInfoInterfaceDeclaration node) {
		endVisit((TypeInfoDeclaration) node);
	}
	@Override
	public void endVisit(TypeInfoPointerDeclaration node) {
		endVisit((TypeInfoDeclaration) node);
	}
	@Override
	public void endVisit(TypeInfoStaticArrayDeclaration node) {
		endVisit((TypeInfoDeclaration) node);
	}
	@Override
	public void endVisit(TypeInfoStructDeclaration node) {
		endVisit((TypeInfoDeclaration) node);
	}
	@Override
	public void endVisit(TypeInfoTypedefDeclaration node) {
		endVisit((TypeInfoDeclaration) node);
	}
	@Override
	public void endVisit(TypeInstance node) {
		endVisit((TypeQualified) node);
	}
	@Override
	public void endVisit(TypePointer node) {
		endVisit((TypeNext) node);
	}
	@Override
	public void endVisit(TypeQualified node) {
		endVisit((Type) node);
	}
	@Override
	public void endVisit(TypeSArray node) {
		endVisit((TypeArray) node);
	}
	@Override
	public void endVisit(TypeSlice node) {
		endVisit((TypeNext) node);
	}
	@Override
	public void endVisit(TypeStruct node) {
		endVisit((Type) node);
	}
	@Override
	public void endVisit(TypeTuple node) {
		endVisit((Type) node);
	}
	@Override
	public void endVisit(TypeTypedef node) {
		endVisit((Type) node);
	}
	@Override
	public void endVisit(TypeTypeof node) {
		endVisit((TypeQualified) node);
	}
	@Override
	public void endVisit(UAddExp node) {
		endVisit((UnaExp) node);
	}
	@Override
	public void endVisit(UnaExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(UnionDeclaration node) {
		endVisit((StructDeclaration) node);
	}
	@Override
	public void endVisit(UnitTestDeclaration node) {
		endVisit((FuncDeclaration) node);
	}
	@Override
	public void endVisit(UnrolledLoopStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(UshrAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(UshrExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(VarDeclaration node) {
		endVisit((Declaration) node);
	}
	@Override
	public void endVisit(VarExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(Version node) {
		endVisit((Dsymbol) node);
	}
	@Override
	public void endVisit(VersionCondition node) {
		endVisit((DVCondition) node);
	}
	@Override
	public void endVisit(VersionSymbol node) {
		endVisit((Dsymbol) node);
	}
	@Override
	public void endVisit(VoidInitializer node) {
		endVisit((Initializer) node);
	}
	@Override
	public void endVisit(VolatileStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(WhileStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(WithScopeSymbol node) {
		endVisit((ScopeDsymbol) node);
	}
	@Override
	public void endVisit(WithStatement node) {
		endVisit((Statement) node);
	}
	@Override
	public void endVisit(XorAssignExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(XorExp node) {
		endVisit((BinExp) node);
	}
	@Override
	public void endVisit(FileInitExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(LineInitExp node) {
		endVisit((Expression) node);
	}
	@Override
	public void endVisit(PostBlitDeclaration node) {
		endVisit((FuncDeclaration) node);
	}
	@Override
	public void endVisit(TemplateThisParameter node) {
		endVisit((TemplateTypeParameter) node);
	}
	
	@Override
	public void endVisit(TypeReturn node) {
		endVisit((TypeQualified) node);
	}
	
	@Override
	public boolean visit(FileInitExp node) {
		return visit((Expression) node);
	}
	
	@Override
	public boolean visit(LineInitExp node) {
		return visit((Expression) node);
	}
	
	@Override
	public boolean visit(PostBlitDeclaration node) {
		return visit((FuncDeclaration) node);
	}
	
	@Override
	public boolean visit(TemplateThisParameter node) {
		return visit((TemplateTypeParameter) node);
	}
	
	@Override
	public boolean visit(TypeReturn node) {
		return visit((TypeQualified) node);
	}
	
	@Override
	public boolean visit(DefaultInitExp node) {
		return visit((Expression) node);
	}
	
	@Override
	public void endVisit(DefaultInitExp node) {
		endVisit((Expression) node);
	}
	
}
