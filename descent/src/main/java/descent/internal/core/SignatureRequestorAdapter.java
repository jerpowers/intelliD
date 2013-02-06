package descent.internal.core;

import descent.internal.compiler.parser.LINK;
import descent.internal.compiler.parser.TypeBasic;

public class SignatureRequestorAdapter implements ISignatureRequestor {
	@Override
	public void acceptArgumentBreak(char c) {
		
	}
	@Override
	public void acceptArgumentModifier(int stc) {
		
	}
	@Override
	public void acceptAssociativeArray(String signature) {
		
	}
	@Override
	public void acceptDelegate(String signature) {
		
	}
	@Override
	public void acceptDynamicArray(String signature) {
		
	}
	@Override
	public void acceptIdentifier(char[][] compoundName, String signature) {
		
	}
	@Override
	public void acceptModule(char[][] compoundName, String signature) {
		
	}
	@Override
	public void acceptPointer(String signature) {
		
	}
	@Override
	public void acceptPrimitive(TypeBasic type) {
		
	}
	@Override
	public void acceptStaticArray(char[] dimension, String signature) {
		
	}
	@Override
	public void acceptSymbol(char type, char[] name, int startPosition, String signature) {
		
	}
	@Override
	public void enterFunctionType() {
		
	}
	@Override
	public void exitFunctionType(LINK link, char argumentBreak, String signature) {
		
	}
	@Override
	public void acceptTemplateTupleParameter() {
		
	}
	@Override
	public void acceptTemplateValueParameterSpecificValue(char[] exp) {
		
	}
	@Override
	public void enterTemplateAliasParameter() {
		
	}
	@Override
	public void enterTemplateParameters() {
		
	}
	@Override
	public void enterTemplateTypeParameter() {
		
	}
	@Override
	public void enterTemplateValueParameter() {
		
	}
	@Override
	public void exitTemplateAliasParameter(String signature) {
		
	}
	@Override
	public void exitTemplateParameters() {
		
	}
	@Override
	public void exitTemplateTypeParameter(String signature) {
		
	}
	@Override
	public void exitTemplateValueParameter(String signature) {
		
	}
	@Override
	public void enterTemplateInstance() {
		
	}
	@Override
	public void exitTemplateInstance(String signature) {
		
	}
	@Override
	public void enterTemplateInstanceType() {
		
	}
	@Override
	public void exitTemplateInstanceType(String signature) {
		
	}
	@Override
	public void acceptTemplateInstanceValue(char[] exp, String signature) {
		
	}
	@Override
	public void enterTemplateInstanceSymbol() {
		
	}
	@Override
	public void exitTemplateInstanceSymbol(String string) {
		
	}
	@Override
	public void acceptPosition(int localPosition) {
		
	}
	@Override
	public void acceptTypeof(char[] expression, String signature) {
		
	}
	@Override
	public void acceptTypeofReturn() {
		
	}
	@Override
	public void acceptSlice(char[] lwr, char[] upr, String signature) {
		
	}
	@Override
	public void acceptConst(String signature) {
		
	}
	@Override
	public void acceptInvariant(String signature) {
		
	}
	@Override
	public void acceptAutomaticTypeInference() {
		
	}
	@Override
	public void acceptTuple(String signature, int numberOftypes) {
		
	}
	
}
