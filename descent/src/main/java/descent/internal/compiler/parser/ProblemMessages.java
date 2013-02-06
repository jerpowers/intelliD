package descent.internal.compiler.parser;

//import org.eclipse.osgi.util.NLS;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

final class ProblemMessages { //extends NLS {

	private static final String BUNDLE_NAME= ProblemMessages.class.getName();

	private ProblemMessages() {
		// Do not instantiate
	}

	public static String UnterminatedBlockComment;
	public static String UnterminatedPlusBlockComment;
	public static String IncorrectNumberOfHexDigitsInEscapeSequence;
	public static String UndefinedEscapeHexSequence;
	public static String UnterminatedStringConstant;
	public static String OddNumberOfCharactersInHexString;
	public static String NonHexCharacter;
	public static String UnterminatedCharacterConstant;
	public static String BinaryDigitExpected;
	public static String OctalDigitExpected;
	public static String HexDigitExpected;
	public static String UnsupportedCharacter;
	public static String InvalidUtfCharacter;
	public static String ThreeEqualsIsNoLongerLegal;
	public static String NotTwoEqualsIsNoLongerLegal;
	public static String LSuffixDeprecated;
	public static String InvalidPragmaSyntax;
	public static String UnrecognizedCharacterEntity;
	public static String UnterminatedNamedEntity;
	public static String UndefinedEscapeSequence;
	public static String InvalidUtf8Sequence;
	public static String IntegerOverflow;
	public static String SignedIntegerOverflow;
	public static String UnrecognizedToken;
	public static String BinaryExponentPartRequired;
	public static String ExponentExpected;
	public static String ISuffixDeprecated;
	public static String ParsingErrorInsertTokenAfter;
	public static String ParsingErrorDeleteToken;
	public static String ParsingErrorInsertToComplete;
	public static String EnumDeclarationIsInvalid;
	public static String MismatchedStringLiteralPostfixes;
	public static String NoIdentifierForDeclarator;
	public static String AliasCannotHaveInitializer;
	public static String CStyleCastIllegal;
	public static String InvalidLinkageIdentifier;
	public static String VariadicArgumentCannotBeOutOrRef;
	public static String VariadicNotAllowedInDelete;
	public static String NoIdentifierForTemplateValueParameter;
	public static String UnexpectedIdentifierInDeclarator;
	public static String RedundantStorageClass;
	public static String RedundantProtectionAttribute;
	public static String UseBracesForAnEmptyStatement;
	public static String MultipleDeclarationsMustHaveTheSameType;
	public static String RedundantInStatement;
	public static String RedundantOutStatement;
	public static String StatementExpectedToBeCurlies;
	public static String InvalidScopeIdentifier;
	public static String OnScopeDeprecated;
	public static String DollarInvalidOutsideBrackets;
	public static String IftypeDeprecated;
	public static String IfAutoDeprecated;
	public static String VariadicTemplateParameterMustBeTheLastOne;
	public static String NeedSizeOfRightmostArray;
	public static String ConflictingStorageClass;
	public static String ValueIsLargerThanAByte;
	public static String UnterminatedTokenStringConstant;
	public static String HeredocRestOfLineShouldBeBlank;
	public static String IdentifierExpectedForHeredoc;
	public static String DelimitedStringMustEndInValue;
	public static String TypeOnlyAllowedIfAnonymousEnumAndNoEnumType;
	public static String IfTypeThereMustBeAnInitializer;
	public static String InvariantAsAttributeIsOnlySupportedInD2;
	public static String ConstAsAttributeIsOnlySupportedInD2;
	public static String OnlyOneCaseAllowedForStartOfCaseRange;
	public static String SymbolConflictsWithSymbolAtLocation;
	public static String SymbolAtLocationConflictsWithSymbolAtLocation;
	public static String PropertyCanNotBeRedefined;
	public static String CircularDefinition;
	public static String EnumValueOverflow;
	public static String EnumMustHaveAtLeastOneMember;
	public static String EnumBaseTypeMustBeOfIntegralType;
	public static String ForwardReferenceOfSymbol;
	public static String ForwardReferenceOfEnumSymbolDotSymbol;
	public static String ForwardReferenceWhenLookingFor;
	public static String BaseEnumIsForwardReference;
	public static String CannotResolveForwardReference;
	public static String EnumIsForwardReference;
	public static String IntegerConstantExpressionExpected;
	public static String ThisNotInClassOrStruct;
	public static String ThisOnlyAllowedInNonStaticMemberFunctions;
	public static String SuperOnlyAllowedInNonStaticMemberFunctions;
	public static String SuperNotInClass;
	public static String ClassHasNoSuper;
	public static String BaseTypeMustBeInterface;
	public static String MemberIsPrivate;
	public static String UsedAsAType;
	public static String ExternSymbolsCannotHaveInitializers;
	public static String VoidsHaveNoValue;
	public static String CannotInferTypeFromThisArrayInitializer;
	public static String NoDefinition;
	public static String DuplicatedInterfaceInheritance;
	public static String BaseTypeMustBeClassOrInterface;
	public static String FieldsNotAllowedInInterfaces;
	public static String UndefinedIdentifier;
	public static String NotAMember;
	public static String NewAllocatorsOnlyForClassOrStruct;
	public static String DeleteDeallocatorsOnlyForClassOrStruct;
	public static String ConstructorsOnlyForClass;
	public static String ConstructorsOnlyForClassOrStruct;
	public static String DestructorsOnlyForClass;
	public static String InvariantsOnlyForClassStructUnion;
	public static String FunctionDoesNotOverrideAny;
	public static String CannotOverrideFinalFunctions;
	public static String OverrideOnlyForClassMemberFunctions;
	public static String FunctionMustReturnAResultOfType;
	public static String MoreThanOneInvariant;
	public static String ParameterMultiplyDefined;
	public static String SymbolNotFound;
	public static String StatementIsNotReachable;
	public static String VoidFunctionsHaveNoResult;
	public static String ReturnStatementsCannotBeInContracts;
	public static String NotAnAggregateType;
	public static String UnrecognizedPragma;
	public static String AnonCanOnlyBePartOfAnAggregate;
	public static String PragmaIsMissingClosingSemicolon;
	public static String CannotImplicitlyConvert;
	public static String ForbiddenReference;
	public static String DiscouragedReference;
	public static String Task;
	public static String UndefinedType;
	public static String IsClassPathCorrect;
	public static String FunctionsCannotBeConstOrAuto;
	public static String FunctionsCannotBeScopeOrAuto;
	public static String NonVirtualFunctionsCannotBeAbstract;
	public static String CannotBeBothAbstractAndFinal;
	public static String ModifierCannotBeAppliedToVariables;
	public static String StructsCannotBeAbstract;
	public static String UnionsCannotBeAbstract;
	public static String AliasCannotBeConst;
	public static String OneArgumentOfTypeExpected;
	public static String IllegalMainParameters;
	public static String MustReturnIntOrVoidFromMainFunction;
	public static String AtLeastOneArgumentOfTypeExpected;
	public static String FirstArgumentMustBeOfType;
	public static String StringExpectedForPragmaMsg;
	public static String LibPragmaMustRecieveASingleArgumentOfTypeString;
	public static String StringExpectedForPragmaLib;
	public static String CannotHaveOutOrInoutParameterOfTypeStaticArray;
	public static String CannotHaveParameterOfTypeVoid;
	public static String FunctionsCannotReturnStaticArrays;
	public static String UnrecongnizedTrait;
	public static String CanOnlyConcatenateArrays;
	public static String ArrayIndexOutOfBounds;
	public static String ArrayIndexOutOfBounds2;
	public static String AssertionFailed;
	public static String AssertionFailedNoMessage;
	public static String ExpressionIsNotEvaluatableAtCompileTime;
	public static String UndefinedProperty;
	public static String DeprecatedProperty;
	public static String FileNameMustBeString;
	public static String FileImportsMustBeSpecified;
	public static String FileNotFound;
	public static String ErrorReadingFile;
	public static String ExpressionHasNoEffect;
	public static String ConstantIsNotAnLValue;
	public static String VersionIdentifierReserved;
	public static String CannotPutCatchStatementInsideFinallyBlock;
	public static String ExpressionDoesNotGiveABooleanResult;
	public static String BreakIsNotInsideALoopOrSwitch;
	public static String CaseIsNotInSwitch;
	public static String VersionDeclarationMustBeAtModuleLevel;
	public static String DebugDeclarationMustBeAtModuleLevel;
	public static String GotoCaseNotInSwitch;
	public static String GotoDefaultNotInSwitch;
	public static String LazyVariablesCannotBeLvalues;
	public static String DivisionByZero;
	public static String DefaultNotInSwitch;
	public static String SwitchAlreadyHasDefault;
	public static String ContinueNotInLoop;
	public static String ForeachIndexCannotBeRef;
	public static String ParametersDoesNotMatchParameterTypes;
	public static String IncompatibleParameterStorageClass;
	public static String OutCannotBeConst;
	public static String OutCannotBeInvariant;
	public static String ScopeCannotBeRefOrOut;
	public static String IncompatibleTypesForOperator;
	public static String IncompatibleTypesForMinus;
	public static String SymbolNotDefined;
	public static String SymbolNotATemplate;
	public static String CannotDeleteType;
	public static String NotAnLvalue;
	public static String CannotAliasAnExpression;
	public static String CannotAssignToStaticArray;
	public static String CannotChangeReferenceToStaticArray;
	public static String CannotModifyParameterInContract;
	public static String BothOverloadsMuchArgumentList;
	public static String ExpressionHasNoType;
	public static String SymbolNotAnExpression;
	public static String SymbolHasNoValue;
	public static String TooManyInitializers;
	public static String SymbolNotAStaticAndCannotHaveStaticInitializer;
	public static String SymbolNotAType;
	public static String IncompleteMixinDeclaration;
	public static String SymbolNotATemplateItIs;
	public static String SymbolCannotBeDeclaredToBeAFunction;
	public static String CannotHaveArrayOfType;
	public static String SymbolDoesNotMatchAnyTemplateDeclaration;
	public static String SymbolDoesNotMatchTemplateDeclaration;
	public static String SymbolDoesNotMatchAnyFunctionTemplateDeclaration;
	public static String IndexOverflowForStaticArray;
	public static String UnknownSize;
	public static String NoSizeYetForForwardReference;
	public static String SymbolMatchesMoreThanOneTemplateDeclaration;
	public static String ExpressionLeadsToStackOverflowAtCompileTime;
	public static String StringIndexOutOfBounds;
	public static String CannotCreateInstanceOfAbstractClass;
	public static String CannotCreateInstanceOfInterface;
	public static String WithExpressionsMustBeClassObject;
	public static String DeclarationIsAlreadyDefined;
	public static String DeclarationIsAlreadyDefinedInAnotherScope;
	public static String VersionDefinedAfterUse;
	public static String DebugDefinedAfterUse;
	public static String NotEnoughArguments;
	public static String CanOnlySynchronizeOnClassObjects;
	public static String CannotDeduceTemplateFunctionFromArgumentTypes;
	public static String CannotDeduceTemplateFunctionFromArgumentTypes2;
	public static String ArrayDimensionExceedsMax;
	public static String AStructIsNotAValidInitializerFor;
	public static String CannotUseArrayToInitialize;
	public static String CircularReferenceTo;
	public static String ParameterIsAlreadyDefined;
	public static String MemberIsNotAccessible;
	public static String SymbolIsNotAccessible;
	public static String ThisForSymbolNeedsToBeType;
	public static String SymbolHasForwardReferences;
	public static String CannotHaveAssociativeArrayOfKey;
	public static String CannotHaveAssociativeArrayOf;
	public static String CannotHaveArrayOfAuto;
	public static String EnclosingLabelForBreakNotFound;
	public static String EnclosingLabelForContinueNotFound;
	public static String CannotAppendTypeToType;
	public static String CannotAppendToStaticArray;
	public static String ExpressionIsVoidAndHasNoValue;
	public static String NeedMemberFunctionOpCmpForSymbolToCompare;
	public static String CompareNotDefinedForComplexOperands;
	public static String NeedThisForAddressOfSymbol;
	public static String RecursiveMixinInstantiation;
	public static String SymbolIsNotOfIntegralType;
	public static String DeleteAAKeyDeprecated;
	public static String SymbolIsDeprecated;
	public static String ShadowingDeclarationIsDeprecated;
	public static String ReturnStatementsCannotBeInFinallyScopeExitOrScopeSuccessBodies;
	public static String CannotReturnExpressionFromConstructor;
	public static String CaseNotFound;
	public static String CircularInheritanceOfInterface;
	public static String ArgumentToMixinMustBeString;
	public static String CannotAccessFrameOfFunction;
	public static String OperationNotAllowedOnBool;
	public static String SymbolIsNotAScalar;
	public static String ImportCannotBeResolved;
	public static String SymbolIsNotAVariable;
	public static String CatchHidesCatch;
	public static String ArithmeticOrStringTypeExpectedForValueParameter;
	public static String FunctionsCannotReturnAFunction;
	public static String FunctionsCannotReturnATuple;
	public static String FunctionsCannotReturnAuto;
	public static String FunctionsCannotReturnScope;
	public static String RecursiveType;
	public static String VariadicFunctionsWithNonDLinkageMustHaveAtLeastOneParameter;
	public static String SymbolMustBeAFunction;
	public static String FunctionExpectedBeforeCall;
	public static String FunctionExpectedBeforeCallNotSymbolOfType;
	public static String CircularReferenceOfTypedef;
	public static String StringSliceIsOutOfBounds;
	public static String ErrorInstantiating;
	public static String CaseMustBeAnIntegralOrStringConstant;
	public static String DuplicateCaseInSwitchStatement;
	public static String SpecialMemberFunctionsNotAllowedForSymbol;
	public static String SpecialFunctionsNotAllowedInInterface;
	public static String FunctionBodyIsNotAbstractInInterface;
	public static String SuperClassConstructorCallMustBeInAConstructor;
	public static String ClassConstructorCallMustBeInAConstructor;
	public static String NoSuperClassConstructor;
	public static String ConstructorCallsNotAllowedInLoopsOrAfterLabels;
	public static String MultipleConstructorCalls;
	public static String ExpressionIsNotConstantOrDoesNotEvaluateToABool;
	public static String StaticIfConditionalCannotBeAtGlobalScope;
	public static String CannotBreakOutOfFinallyBlock;
	public static String LabelHasNoBreak;
	public static String LabelHasNoContinue;
	public static String CannotGotoInOrOutOfFinallyBlock;
	public static String CalledWithArgumentTypesMatchesBoth;
	public static String SymbolIsNotAnArithmeticType;
	public static String SymbolIsNotAnArithmeticTypeItIs;
	public static String CannotPerformModuloComplexArithmetic;
	public static String OperatorNotAllowedOnBoolExpression;
	public static String ForeachKeyTypeMustBeIntOrUint;
	public static String ForeachKeyTypeMustBeIntOrUintLongOrUlong;
	public static String ForeachKeyCannotBeOutOrRef;
	public static String NoReverseIterationOnAssociativeArrays;
	public static String OnlyOneOrTwoArgumentsForAssociativeArrayForeach;
	public static String OnlyOneOrTwoArgumentsForArrayForeach;
	public static String ForeachTargetIsNotAnArrayOf;
	public static String ForeachKeyCannotBeInout;
	public static String ForeachValueOfUTFConversionCannotBeInout;
	public static String CannotInferTypeForSymbol;
	public static String CannotInferTypeFromInitializer;
	public static String NoStorageClassForSymbol;
	public static String OnlyOneValueOrTwoKeyValueArgumentsForTupleForeach;
	public static String CannotUniquelyInferForeachArgumentTypes;
	public static String InvalidForeachAggregate;
	public static String NotAnAssociativeArrayInitializer;
	public static String ArrayInitializersAsExpressionsNotAllowed;
	public static String IftypeConditionCannotBeAtGlobalScope;
	public static String SymbolIsNotAFieldOfSymbol;
	public static String RecursiveTemplateExpansion;
	public static String RecursiveTemplateExpansionForTemplateArgument;
	public static String IndexIsNotATypeOrExpression;
	public static String CannotHavePointerToSymbol;
	public static String SizeOfTypeIsNotKnown;
	public static String CanOnlySliceTupleTypes;
	public static String NoPropertyForTuple;
	public static String CannotResolveDotProperty;
	public static String CannotTakeAddressOfBitInArray;
	public static String OnlyOneIndexAllowedToIndex;
	public static String NoOpIndexOperatorOverloadForType;
	public static String ArrayDimensionOverflow;
	public static String OperatorAssignmentOverloadWithOpIndexIllegal;
	public static String CannotHaveOutOrInoutArgumentOfBitInArray;
	public static String SymbolIsAliasedToAFunction;
	public static String LinkageDoesNotMatchInterfaceFunction;
	public static String InterfaceFunctionIsNotImplemented;
	public static String ExpectedKeyAsArgumentToRemove;
	public static String CyclicConstructorCall;
	public static String MissingOrCurruptObjectDotD;
	public static String CannotContinueOutOfFinallyBlock;
	public static String ForwardDeclaration;
	public static String CannotFormDelegateDueToCovariantReturnType;
	public static String ForeachRangeKeyCannotHaveStorageClass;
	public static String MultipleOverridesOfSameFunction;
	public static String IdentityAssignmentOperatorOverloadIsIllegal;
	public static String LiteralsCannotBeClassMembers;
	public static String NoMatchForImplicitSuperCallInConstructor;
	public static String NoReturnAtEndOfFunction;
	public static String CanOnlyDeclareTypeAliasesWithinStaticIfConditionals;
	public static String PackageAndModuleHaveTheSameName;
	public static String StringLiteralsAreImmutable;
	public static String ExpressionDotNewIsOnlyForAllocatingNestedClasses;
	public static String TooManyArgumentsForArray;
	public static String ReturnExpressionExpected;
	public static String ReturnWithoutCallingConstructor;
	public static String ModuleIsInMultiplePackages;
	public static String ModuleIsInMultipleDefined;
	public static String NeedUpperAndLowerBoundToSlicePointer;
	public static String NeedUpperAndLowerBoundToSliceTuple;
	public static String CannotConvertStringLiteralToVoidPointer;
	public static String SymbolIsNotAPreInstanceInitializableField;
	public static String NoCaseStatementFollowingGoto;
	public static String SwitchStatementHasNoDefault;
	public static String SymbolIsNotAFunctionTemplate;
	public static String TupleIsNotAValidTemplateValueArgument;
	public static String IncompatibleArgumentsForTemplateInstantiation;
	public static String ThrowStatementsCannotBeInContracts;
	public static String CanOnlyThrowClassObjects;
	public static String StringExpectedAsSecondArgument;
	public static String WrongNumberOfArguments;
	public static String StringMustBeChars;
	public static String InvalidFirstArgument;
	public static String FirstArgumentIsNotAClass;
	public static String ArgumentHasNoMembers;
	public static String SymbolHasNoMembers;
	public static String KindSymbolHasNoMembers;
	public static String DotOffsetDeprecated;
	public static String NoClassInfoForComInterfaceObjects;
	public static String NoClassInfoForCppInterfaceObjects;
	public static String CannotMakeReferenceToABit;
	public static String CannotFormTupleOfTuples;
	public static String MissingInitializerInStaticConstructorForConstVariable;
	public static String GlobalsStaticsFieldsRefAndAutoParametersCannotBeAuto;
	public static String GlobalsStaticsFieldsManifestConstantsRefAndAutoParametersCannotBeScope;
	public static String ReferenceToScopeClassMustBeScope;
	public static String NumberOfKeysMustMatchNumberOfValues;
	public static String ExpectedNumberArguments;
	public static String ArraySliceIfOutOfBounds;
	public static String InvalidUCS32Char;
	public static String TupleIndexExceedsBounds;
	public static String SliceIsOutOfRange;
	public static String CannotTakeAddressOf;
	public static String VariableIsUsedBeforeInitialization;
	public static String EscapingReferenceToLocal;
	public static String EscapingReferenceToAutoLocal;
	public static String EscapingReferenceToScopeLocal;
	public static String EscapingReferenceToLocalVariable;
	public static String EscapingReferenceToVariadicParameter;
	public static String CanOnlyCatchClassObjects;
	public static String BaseClassIsForwardReferenced;
	public static String BaseIsForwardReferenced;
	public static String CannotInheritFromFinalClass;
	public static String StaticClassCannotInheritFromNestedClass;
	public static String SuperClassIsNestedWithin;
	public static String SuperClassIsNotNestedWithin;
	public static String ArrayComparisonTypeMismatch;
	public static String ConditionalExpressionIsNotAModifiableLvalue;
	public static String CannotCastSymbolToSymbol;
	public static String CannotDeleteInstanceOfComInterface;
	public static String TemplateIsNotAMemberOf;
	public static String TemplateIdentifierIsNotAMemberOf;
	public static String TemplateIdentifierIsNotAMemberOfUndefined;
	public static String CanOnlyInitiailizeConstMemberInsideConstructor;
	public static String SymbolIsNotAMember;
	public static String SymbolIsNotATemplate;
	public static String DSymbolHasNoSize;
	public static String ExpressionOfTypeDoesNotHaveABooleanValue;
	public static String ImplicitConversionCanCauseLossOfData;
	public static String ForwardReferenceToType;
	public static String FloatingPointConstantExpressionExpected;
	public static String ExpressionIsNotAValidTemplateValueArgument;
	public static String InvalidRangeLowerBound;
	public static String InvalidRangeUpperBound;
	public static String SymbolIsNotAScalarType;
	public static String ForeachIndexMustBeType;
	public static String ForeachValueMustBeType;
	public static String OpApplyFunctionMustReturnAnInt;
	public static String FunctionOfTypeOverridesButIsNotCovariant;
	public static String CannotOverrideFinalFunction;
	public static String IncompatibleCovariantTypes;
	public static String CannotUseTemplateToAddVirtualFunctionToClass;
	public static String OutResultIsAlreadyDefined;
	public static String MissingInitializerForConstField;
	public static String MissingInitializerForFinalField;
	public static String ImportNotFound;
	public static String SymbolMustBeAnArrayOfPointerType;
	public static String RvalueOfInExpressionMustBeAnAssociativeArray;
	public static String InterfaceInheritsFromDuplicateInterface;
	public static String LabelIsAlreadyDefined;
	public static String CannotSubtractPointerFromSymbol;
	public static String ThisForNestedClassMustBeAClassType;
	public static String CanOnlyDereferenceAPointer;
	public static String OuterClassThisNeededToNewNestedClass;
	public static String ThisForNestedClassMustBeOfType;
	public static String NoConstructorForSymbol;
	public static String NoAllocatorForSymbol;
	public static String NegativeArrayIndex;
	public static String NewCanOnlyCreateStructsDynamicArraysAndClassObjects;
	public static String MismatchedFunctionReturnTypeInference;
	public static String ShiftLeftExceeds;
	public static String SymbolCannotBeSlicedWithBrackets;
	public static String SliceExpressionIsNotAModifiableLvalue;
	public static String SymbolIsNotAMemberOf;
	public static String MoreInitiailizersThanFields;
	public static String OverlappingInitiailization;
	public static String CannotMakeExpressionOutOfInitializer;
	public static String NoDefaultOrCaseInSwitchStatement;
	public static String SymbolIsNotASymbol;
	public static String ForwardReferenceToTemplate;
	public static String ForwardReferenceToTemplateDeclaration;
	public static String SpecializationNotAllowedForDeducedParameter;
	public static String CannotDeclareTemplateAtFunctionScope;
	public static String TemplateHasNoValue;
	public static String CannotUseLocalAsTemplateParameter;
	public static String NoSizeForType;
	public static String SymbolDotSymbolIsNotADeclaration;
	public static String ThisIsRequiredButIsNotABaseClassOf;
	public static String ForwardReferenceToSymbol;
	public static String IdentifierOfSymbolIsNotDefined;
	public static String StructIsForwardReferenced;
	public static String CannotUseTemplateToAddFieldToAggregate;
	public static String CannotModifyFinalVariable;
	public static String InvalidUtf8Sequence2;
	public static String Utf16HighValuePastEndOfString;
	public static String Utf16LowValueOutOfRange;
	public static String UnpairedUtf16Value;
	public static String IllegalUtf16Value;
	public static String StaticConstructorCanOnlyBePartOfStructClassModule;
	public static String ShiftAssignIsOutsideTheRange;
	public static String TemplateTupleParameterMustBeLastOne;
	public static String SymbolIsNestedInBoth;
	public static String FunctionIsAbstract;
	public static String KindSymbolDoesNotOverload;
	public static String MismatchedTupleLengths;
	public static String DoNotUseNullWhenComparingClassTypes;
	public static String UseTokenInsteadOfTokenWhenComparingWithNull;
	public static String VoidDoesNotHaveAnInitializer;
	public static String FunctionNameExpectedForStartAddress;
	public static String TypeofReturnMustBeInsideFunction;
	public static String PostBlitsAreOnlyForStructUnionDefinitions;
	public static String CannotHaveEDotTuple;
	public static String CannotCreateCppClasses;
	public static String SwitchAndCaseAreInDifferentFinallyBlocks;
	public static String SwitchAndDefaultAreInDifferentFinallyBlocks;
	public static String CannotHaveFieldWithSameStructType;
	public static String WithoutThisCannotBeConstInvariant;
	public static String CannotModifySymbol;
	public static String CannotCallPublicExportFunctionFromImmutable;
	public static String TemplateMemberFunctionNotAllowedInInterface;
	public static String ArgumentToTypeofIsNotAnExpression;
	public static String CannotInferTypeFromOverloadedFunctionSymbol;
	public static String SymbolIsNotMutable;
	public static String SymbolForSymbolNeedsToBeType;
	public static String CannotModifyConstImmutable;
	public static String SymbolCanOnlyBeCalledOnAnInvariantObject;
	public static String SymbolCanOnlyBeCalledOnAMutableObject;
	public static String CannotCallMutableMethodOnFinalStruct;
	public static String ForwardReferenceOfImport;
	public static String TemplateDoesNotHaveProperty;
	public static String ExpressionDoesNotHaveProperty;
	public static String RecursiveOpCmpExpansion;
	public static String CtorIsReservedForConstructors;
	public static String FunctionOverridesBaseClassFunctionButIsNotMarkedWithOverride;
	public static String ForwardReferenceOfTypeDotMangleof;
	public static String ArrayLengthHidesOtherLengthNameInOuterScope;
	public static String OnePathSkipsConstructor;
	public static String PragmaLibNotAllowedAsStatement;
	public static String ThereCanBeOnlyOneAliasThis;
	public static String AliasThisCanOnlyAppearInStructOrClassDeclaration;
	public static String FunctionIsOverloaded;
	public static String PureFunctionCannotCallImpure;
	public static String PureFunctionCannotAccessMutableStaticData;
	public static String PureNestedFunctionCannotAccessMutableData;
	public static String ConstraintIsNotConstantOrDoesNotEvaluateToABool;
	public static String EscapeStringLiteralDeprecated;
	public static String DelimiterCannotBeWhitespace;
	public static String ArrayEqualityComparisonTypeMismatch;
	public static String GsharedNotAllowedInSafeMode;
	public static String InnerStructCannotBeAField;
	public static String OnlyParametersOfForeachDeclarationsCanBeRef;
	public static String ManifestConstantsMustHaveInitializers;
	public static String ForwardReferenceToInferredReturnTypeOfFunctionCall;
	public static String CaseRangesNotAllowedInFinalSwitch;
	public static String MoreThan256CasesInCaseRange;
	public static String CannotCastTuple;
	public static String CastNotAllowedInSafeMode;
	public static String SliceIsNotMutable;
	public static String SymbolIsNothrowYetMayThrow;
	public static String DefaultConstructorNotAllowedForStructs;
	public static String VoidDoesNotHaveADefaultInitializer;
	public static String CannotImplicitlyConvertToImmutable;
	public static String CannotHaveConstInvariantOutParameterOfType;
	public static String OverloadSetNotAllowedInStructDeclaration;
	public static String CannotPerformArrayOperationsOnVoidArrays;

	static {
		initializeMessages(BUNDLE_NAME, ProblemMessages.class);
	}

    /***********************************************************************
     * Below is copied from org.eclipse.osgi.util.NLS to avoid dependency
     * on same.
     */

    private static final Object[] EMPTY_ARGS = new Object[0];
    private static final String EXTENSION = ".properties"; //$NON-NLS-1$
    private static String[] nlSuffixes;
    /*
     * NOTE do not change the name of this field; it is set by the Framework using reflection
     */
//    private static FrameworkLog frameworkLog;

    static final int SEVERITY_ERROR = 0x04;
    static final int SEVERITY_WARNING = 0x02;
    /*
     * This object is assigned to the value of a field map to indicate
     * that a translated message has already been assigned to that field.
     */
    static final Object ASSIGNED = new Object();

    /**
     * Initialize the given class with the values from the specified message bundle.
     *
     * @param bundleName fully qualified path of the class name
     * @param clazz the class where the constants will exist
     */
    public static void initializeMessages(final String bundleName, final Class clazz) {
        if (System.getSecurityManager() == null) {
            load(bundleName, clazz);
            return;
        }
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                load(bundleName, clazz);
                return null;
            }
        });
    }

    /*
     * Build an array of property files to search.  The returned array contains
     * the property fields in order from most specific to most generic.
     * So, in the FR_fr locale, it will return file_fr_FR.properties, then
     * file_fr.properties, and finally file.properties.
     */
    private static String[] buildVariants(String root) {
        if (nlSuffixes == null) {
            //build list of suffixes for loading resource bundles
            String nl = Locale.getDefault().toString();
            ArrayList result = new ArrayList(4);
            int lastSeparator;
            while (true) {
                result.add('_' + nl + EXTENSION);
                lastSeparator = nl.lastIndexOf('_');
                if (lastSeparator == -1)
                    break;
                nl = nl.substring(0, lastSeparator);
            }
            //add the empty suffix last (most general)
            result.add(EXTENSION);
            nlSuffixes = (String[]) result.toArray(new String[result.size()]);
        }
        root = root.replace('.', '/');
        String[] variants = new String[nlSuffixes.length];
        for (int i = 0; i < variants.length; i++)
            variants[i] = root + nlSuffixes[i];
        return variants;
    }

    private static void computeMissingMessages(String bundleName, Class clazz, Map fieldMap, Field[] fieldArray, boolean isAccessible) {
        // iterate over the fields in the class to make sure that there aren't any empty ones
        final int MOD_EXPECTED = Modifier.PUBLIC | Modifier.STATIC;
        final int MOD_MASK = MOD_EXPECTED | Modifier.FINAL;
        final int numFields = fieldArray.length;
        for (int i = 0; i < numFields; i++) {
            Field field = fieldArray[i];
            if ((field.getModifiers() & MOD_MASK) != MOD_EXPECTED)
                continue;
            //if the field has a a value assigned, there is nothing to do
            if (fieldMap.get(field.getName()) == ASSIGNED)
                continue;
            try {
                // Set a value for this empty field. We should never get an exception here because
                // we know we have a public static non-final field. If we do get an exception, silently
                // log it and continue. This means that the field will (most likely) be un-initialized and
                // will fail later in the code and if so then we will see both the NPE and this error.
                String value = "NLS missing message: " + field.getName() + " in: " + bundleName; //$NON-NLS-1$ //$NON-NLS-2$
/*
                if (Debug.DEBUG_MESSAGE_BUNDLES)
                    System.out.println(value);
                log(SEVERITY_WARNING, value, null);
*/
                if (!isAccessible)
                    field.setAccessible(true);
                field.set(null, value);
            } catch (Exception e) {
/*
                log(SEVERITY_ERROR, "Error setting the missing message value for: " + field.getName(), e); //$NON-NLS-1$
*/
            }
        }
    }

    /*
	 * Load the given resource bundle using the specified class loader.
	 */
    static void load(final String bundleName, Class clazz) {
        long start = System.currentTimeMillis();
        final Field[] fieldArray = clazz.getDeclaredFields();
        ClassLoader loader = clazz.getClassLoader();

        boolean isAccessible = (clazz.getModifiers() & Modifier.PUBLIC) != 0;

        //build a map of field names to Field objects
        final int len = fieldArray.length;
        Map fields = new HashMap(len * 2);
        for (int i = 0; i < len; i++)
            fields.put(fieldArray[i].getName(), fieldArray[i]);

        // search the variants from most specific to most general, since
        // the MessagesProperties.put method will mark assigned fields
        // to prevent them from being assigned twice
        final String[] variants = buildVariants(bundleName);
        for (int i = 0; i < variants.length; i++) {
            // loader==null if we're launched off the Java boot classpath
            final
            InputStream
                    input = loader == null ? ClassLoader.getSystemResourceAsStream(variants[i]) : loader.getResourceAsStream(variants[i]);
            if (input == null)
                continue;
            try {
                final MessagesProperties properties = new MessagesProperties(fields, bundleName, isAccessible);
                properties.load(input);
            } catch (IOException e) {
/*
                log(SEVERITY_ERROR, "Error loading " + variants[i], e); //$NON-NLS-1$
*/
            } finally {
                if (input != null)
                    try {
                        input.close();
                    } catch (IOException e) {
                        // ignore
                    }
            }
        }
        computeMissingMessages(bundleName, clazz, fields, fieldArray, isAccessible);
/*
        if (Debug.DEBUG_MESSAGE_BUNDLES)
            System.out.println("Time to load message bundle: " + bundleName + " was " + (System.currentTimeMillis() - start) + "ms."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
*/
    }

    /*
     * Class which sub-classes java.util.Properties and uses the #put method
     * to set field values rather than storing the values in the table.
     */
    private static class MessagesProperties extends Properties {

        private static final int MOD_EXPECTED = Modifier.PUBLIC | Modifier.STATIC;
        private static final int MOD_MASK = MOD_EXPECTED | Modifier.FINAL;
        private static final long serialVersionUID = 1L;

        private final String bundleName;
        private final Map fields;
        private final boolean isAccessible;

        public MessagesProperties(Map fieldMap, String bundleName, boolean isAccessible) {
            super();
            this.fields = fieldMap;
            this.bundleName = bundleName;
            this.isAccessible = isAccessible;
        }

        /* (non-Javadoc)
         * @see java.util.Hashtable#put(java.lang.Object, java.lang.Object)
         */
        public synchronized Object put(Object key, Object value) {
            Object fieldObject = fields.put(key, ASSIGNED);
            // if already assigned, there is nothing to do
            if (fieldObject == ASSIGNED)
                return null;
            if (fieldObject == null) {
                final String msg = "NLS unused message: " + key + " in: " + bundleName;//$NON-NLS-1$ //$NON-NLS-2$
/*
                if (Debug.DEBUG_MESSAGE_BUNDLES)
                    System.out.println(msg);
                log(SEVERITY_WARNING, msg, null);
*/
                return null;
            }
            final Field field = (Field) fieldObject;
            //can only set value of public static non-final fields
            if ((field.getModifiers() & MOD_MASK) != MOD_EXPECTED)
                return null;
            try {
                // Check to see if we are allowed to modify the field. If we aren't (for instance
                // if the class is not public) then change the accessible attribute of the field
                // before trying to set the value.
                if (!isAccessible)
                    field.setAccessible(true);
                // Set the value into the field. We should never get an exception here because
                // we know we have a public static non-final field. If we do get an exception, silently
                // log it and continue. This means that the field will (most likely) be un-initialized and
                // will fail later in the code and if so then we will see both the NPE and this error.
                field.set(null, value);
            } catch (Exception e) {
/*
                log(SEVERITY_ERROR, "Exception setting field value.", e); //$NON-NLS-1$
*/
            }
            return null;
        }
    }

}
