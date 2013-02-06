package descent.internal.core;

import java.util.ArrayList;
import java.util.List;

import descent.core.Signature;
import descent.internal.compiler.parser.LINK;
import descent.internal.compiler.parser.STC;
import descent.internal.compiler.parser.TypeBasic;

/**
 * A class for processing a signature.
 * 
 * @see ISignatureRequestor
 */
public class SignatureProcessor {	
	
	/**
	 * Processes the given signature and notifies the requestor about the found
	 * elements.
	 * 
	 * <p>Type elements are notified in a bottom-up fashion. For example,
	 * if the signature is Pi (pointer to int), 
	 * {@link ISignatureRequestor#acceptPrimitive(TypeBasic)} will be called,
	 * followed by {@link ISignatureRequestor#acceptPointer()}.</p>
	 * 
	 * <p>Nested elements are notified in a top-down fashion. For example,
	 * if the signature is for a function A inside a function B, A will be
	 * notified before B.</p>
	 * 
	 * @param signature the signature to process
	 * @param wantSignature whether to report the sub-signatures in the
	 * requestor methods
	 * @param requestor the signature processor requestor 
	 */
	public static void process(String signature, boolean wantSignature, ISignatureRequestor requestor) {
		process0(signature, 0, wantSignature, requestor);
	}

	public static int process0(String signature, int i, boolean wantSignature, ISignatureRequestor requestor) {
		if (signature == null || signature.length() == 0) {
			throw new IllegalArgumentException("Invalid signature: <null or empty>");
		}
		
		int localPosition = -1;
		int start = i;
		
		while(i < signature.length()) {
			char first = signature.charAt(i);
			switch(first) {
			case Signature.C_MODULE: {
				int[] end = { 0 };
				char[][] compoundName = splitSignature(signature, i + 1, end);
				i = end[0];
				requestor.acceptModule(compoundName, substring(signature, start, i, wantSignature));
				continue;
			}
			case Signature.C_CLASS:
			case Signature.C_STRUCT:
			case Signature.C_UNION:
			case Signature.C_INTERFACE:
			case Signature.C_ENUM:
			case Signature.C_ENUM_MEMBER:
			case Signature.C_VARIABLE:
			case Signature.C_ALIAS:
			case Signature.C_TYPEDEF:
			case Signature.C_FUNCTION:
			case Signature.C_TEMPLATE:
			case Signature.C_TEMPLATED_CLASS:
			case Signature.C_TEMPLATED_STRUCT:
			case Signature.C_TEMPLATED_UNION:
			case Signature.C_TEMPLATED_INTERFACE:
			case Signature.C_TEMPLATED_FUNCTION:
				i++;
				char c = signature.charAt(i);
				if (!Character.isDigit(c)) {
					throw new IllegalArgumentException("Invalid signature: " + signature);
				}
				int n = 0;
				while(Character.isDigit(c)) {
					n = 10 * n + (c - '0');
					i++;
					c = signature.charAt(i);
				}
				char[] name = new char[n];
				signature.getChars(i, i + n, name, 0);
				i += n;
				
				if (first == Signature.C_FUNCTION || first == Signature.C_TEMPLATED_FUNCTION) {
					i = process0(signature, i, wantSignature, requestor);
				}
				
				if (first == Signature.C_TEMPLATE || first == Signature.C_TEMPLATED_FUNCTION ||
						first == Signature.C_TEMPLATED_CLASS || first == Signature.C_TEMPLATED_STRUCT ||
						first == Signature.C_TEMPLATED_UNION || first == Signature.C_TEMPLATED_INTERFACE) {
					requestor.enterTemplateParameters();
					
					while(signature.charAt(i) != Signature.C_TEMPLATE_PARAMETERS_BREAK) {
						i = process0(signature, i, wantSignature, requestor);
					}
					i++;
					
					requestor.exitTemplateParameters();
				}
				
				requestor.acceptSymbol(first, name, localPosition, substring(signature, start, i, wantSignature));
				
				localPosition = -1;
				
				// If a symbol does not come next, don't process further
				if (i < signature.length() && !isSymbol(signature.charAt(i)) ) {
					return i;
				} else {
					continue;
				}
			case Signature.C_SPECIAL_FUNCTION:
				i++;
				requestor.acceptSymbol(first, null, localPosition, substring(signature, start, i, wantSignature));
				localPosition = -1;
				continue;
			case Signature.C_DELEGATE: {
				i = process0(signature, i + 1, wantSignature, requestor);
				requestor.acceptDelegate(substring(signature, start, i, wantSignature));
				return i;
			}
			case Signature.C_POINTER: { // pointer
				i = process0(signature, i + 1, wantSignature, requestor);
				requestor.acceptPointer(substring(signature, start, i, wantSignature));
				return i;
			}
			case Signature.C_DYNAMIC_ARRAY: { // dynamic array
				i = process0(signature, i + 1, wantSignature, requestor);
				requestor.acceptDynamicArray(substring(signature, start, i, wantSignature));
				return i;
			}
			case Signature.C_STATIC_ARRAY: { // static array
				i++;
				
				i = process0(signature, i, wantSignature, requestor);
				i++;
				
				c = signature.charAt(i);
				
				n = 0;
				
				while(c != Signature.C_STATIC_ARRAY) {
					n = 10 * n + (c - '0');
					i++;
					c = signature.charAt(i);
				}
				i++;
				
				char[] dimension = signature.substring(i, i + n).toCharArray();
				
				i += n;
				
				requestor.acceptStaticArray(dimension, substring(signature, start, i, wantSignature));
				return i;
			}
			case Signature.C_ASSOCIATIVE_ARRAY: {// associative array
				i = process0(signature, i + 1, wantSignature, requestor);
				i = process0(signature, i, wantSignature, requestor);
				requestor.acceptAssociativeArray(substring(signature, start, i, wantSignature));
				return i;
			}
			case Signature.C_TUPLE: { // Type tuple
				i++;
				
				c = signature.charAt(i);
				
				n = 0;
				
				while(c != Signature.C_TUPLE) {
					n = 10 * n + (c - '0');
					i++;
					c = signature.charAt(i);
				}
				i++;
				
				for (int j = 0; j < n; j++) {
					i = process0(signature, i, wantSignature, requestor);
				}
				
				requestor.acceptTuple(substring(signature, start, i, wantSignature), n);
				return i;
			}
			case Signature.C_D_LINKAGE: // Type function
			case Signature.C_C_LINKAGE:
			case Signature.C_WINDOWS_LINKAGE:
			case Signature.C_PASCAL_LINKAGE:
			case Signature.C_CPP_LINKAGE: {
				requestor.enterFunctionType();
				
				LINK link;
				switch(first) {
				case Signature.C_D_LINKAGE: link = LINK.LINKd; break;
				case Signature.C_C_LINKAGE: link = LINK.LINKc; break;
				case Signature.C_WINDOWS_LINKAGE: link = LINK.LINKwindows; break;
				case Signature.C_PASCAL_LINKAGE: link = LINK.LINKpascal; break;
				case Signature.C_CPP_LINKAGE: link = LINK.LINKcpp; break;
				default: throw new IllegalStateException("Should not happen");
				}
				
				i++;
				while(signature.charAt(i) != Signature.C_FUNCTION_PARAMETERS_BREAK_VARARGS_SAME_TYPE && 
						signature.charAt(i) != Signature.C_FUNCTION_PARAMETERS_BREAK_VARARGS_UNKNOWN_TYPES && 
						signature.charAt(i) != Signature.C_FUNCTION_PARAMTERS_BREAK) {
					i = argumentModifier(signature, i, requestor);
					i = process0(signature, i, wantSignature, requestor);
				}
				
				char argumentBreak = signature.charAt(i);
				requestor.acceptArgumentBreak(argumentBreak);
				i++;
				
				i = process0(signature, i, wantSignature, requestor);
				requestor.exitFunctionType(link, argumentBreak, substring(signature, start, i, wantSignature));
				return i;
			}
			case Signature.C_TEMPLATE_TUPLE_PARAMETER:
				requestor.acceptTemplateTupleParameter();
				return i + 1;
			case Signature.C_TEMPLATE_ALIAS_PARAMETER:
				requestor.enterTemplateAliasParameter();
				
				i++;
				if (i < signature.length() && signature.charAt(i) == Signature.C_TEMPLATE_ALIAS_PARAMETER_SPECIFIC_TYPE) {
					i = process0(signature, i+1, wantSignature, requestor);
				}
				
				requestor.exitTemplateAliasParameter(substring(signature, start, i, wantSignature));
				return i;
			case Signature.C_TEMPLATE_TYPE_PARAMETER:
				requestor.enterTemplateTypeParameter();
				
				i++;
				if (i < signature.length() && signature.charAt(i) == Signature.C_TEMPLATE_TYPE_PARAMETER_SPECIFIC_TYPE) {
					i = process0(signature, i+1, wantSignature, requestor);
				}
				
				requestor.exitTemplateTypeParameter(substring(signature, start, i, wantSignature));
				return i;
			case Signature.C_TEMPLATE_VALUE_PARAMETER:
				requestor.enterTemplateValueParameter();
				
				i = process0(signature, i + 1, wantSignature, requestor);
				
				if (i < signature.length()) {
					c = signature.charAt(i);
					if (c == Signature.C_TEMPLATE_VALUE_PARAMETER_SPECIFIC_VALUE) {
						i++;
						c = signature.charAt(i);
						n = 0;
						
						while(c != Signature.C_TEMPLATE_VALUE_PARAMETER) {
							n = 10 * n + (c - '0');
							i++;
							c = signature.charAt(i);
						}
						i++;
						
						requestor.acceptTemplateValueParameterSpecificValue(signature.substring(i, i + n).toCharArray());
						
						i += n;
					}
				}
				
				requestor.exitTemplateValueParameter(substring(signature, start, i, wantSignature));
				return i;
			case Signature.C_TYPEOF:
				i++;
				c = signature.charAt(i);
				
				n = 0;
				
				while(c != Signature.C_TYPEOF) {
					n = 10 * n + (c - '0');
					i++;
					c = signature.charAt(i);
				}
				i++;
				
				requestor.acceptTypeof(signature.substring(i, i + n).toCharArray(),
								substring(signature, start, i + n, wantSignature));
				
				i += n;
				
				return i;
			case Signature.C_TYPEOF_RETURN:
				i++;
				requestor.acceptTypeofReturn();
				return i;
			case Signature.C_SLICE:
				i = process0(signature, i + 1, wantSignature, requestor);
				i++;
				
				c = signature.charAt(i);
				n = 0;
				
				while(c != Signature.C_SLICE) {
					n = 10 * n + (c - '0');
					i++;
					c = signature.charAt(i);
				}
				i++;
				
				char[] lwr = signature.substring(i, i + n).toCharArray();
				
				i += n;
				
				c = signature.charAt(i);
				n = 0;
				
				while(c != Signature.C_SLICE) {
					n = 10 * n + (c - '0');
					i++;
					c = signature.charAt(i);
				}
				i++;
				
				char[] upr = signature.substring(i, i + n).toCharArray();
				
				requestor.acceptSlice(lwr, upr, substring(signature, start, i + n, wantSignature));
				
				i += n;
				
				return i;
			case Signature.C_MODIFIER_OUT:
			case Signature.C_MODIFIER_REF: 
			case Signature.C_MODIFIER_LAZY:
				i = argumentModifier(signature, i, requestor);
				continue;
			case Signature.C_TEMPLATE_PARAMETERS_BREAK: // Template parameters break
			case Signature.C_FUNCTION_PARAMETERS_BREAK_VARARGS_SAME_TYPE: // Argument break
			case Signature.C_FUNCTION_PARAMETERS_BREAK_VARARGS_UNKNOWN_TYPES:
			case Signature.C_FUNCTION_PARAMTERS_BREAK:
				return i;
			case Signature.C_TEMPLATE_INSTANCE:
				requestor.enterTemplateInstance();

				i++;
				while(signature.charAt(i) != Signature.C_TEMPLATE_PARAMETERS_BREAK) {
					i = process0(signature, i, wantSignature, requestor);
				}
				i++;
				
				requestor.exitTemplateInstance(substring(signature, start, i, wantSignature));
				
				// If a template parameter follows, don't continue waiting for
				// a next template instance or identifier
				if (i < signature.length()) {
					c = signature.charAt(i);
					if (c == Signature.C_TEMPLATE_ALIAS_PARAMETER ||
						c == Signature.C_TEMPLATE_TYPE_PARAMETER ||
						c == Signature.C_TEMPLATE_VALUE_PARAMETER ||
						c == Signature.C_TEMPLATE_TUPLE_PARAMETER ||
						c == Signature.C_TEMPLATE_INSTANCE_SYMBOL_PARAMETER ||
						c == Signature.C_TEMPLATE_INSTANCE_TYPE_PARAMETER ||
						c == Signature.C_TEMPLATE_INSTANCE_VALUE_PARAMETER) {
						return i;
					} else if (c == Signature.C_DOT) {
						i++;
						continue;
					}
				}
				
				continue;
			case Signature.C_TEMPLATE_INSTANCE_TYPE_PARAMETER:
				requestor.enterTemplateInstanceType();
				i++;
				i = process0(signature, i, wantSignature, requestor);
				requestor.exitTemplateInstanceType(substring(signature, start, i, wantSignature));
				return i;
			case Signature.C_TEMPLATE_INSTANCE_VALUE_PARAMETER:
				i++;
				c = signature.charAt(i);
				if (i < signature.length() && Character.isDigit(c)) {
					n = 0;
					
					while(c != Signature.C_TEMPLATE_INSTANCE_VALUE_PARAMETER) {
						n = 10 * n + (c - '0');
						i++;
						c = signature.charAt(i);
					}
					i++;
					
					requestor.acceptTemplateInstanceValue(signature.substring(i, i + n).toCharArray(), substring(signature, start, i + n, wantSignature));
					
					i += n;
				}
				
				return i;
			case Signature.C_TEMPLATE_INSTANCE_SYMBOL_PARAMETER:
				requestor.enterTemplateInstanceSymbol();
				i = process0(signature, i + 1, wantSignature, requestor);
				requestor.exitTemplateInstanceSymbol(substring(signature, start, i, wantSignature));
				return i;
			case Signature.C_IDENTIFIER:
				int[] end = { 0 };
				char[][] compoundName = splitSignature(signature, i + 1, end);
				i = end[0];
				requestor.acceptIdentifier(compoundName, substring(signature, start, i, wantSignature));
				
				// A template instance may follow an identifier,
				// or a dot
				if (i < signature.length() && 
						signature.charAt(i) == Signature.C_TEMPLATE_INSTANCE) {
					continue;
				} else if (i < signature.length() && 
						signature.charAt(i) == Signature.C_DOT) {
					i++;
					continue;
				} else {
					return i;
				}
			case Signature.C_POSITION:
				n = 0;
				i++;
				c = signature.charAt(i);
				while(Character.isDigit(c)) {
					n = 10 * n + (c - '0');
					i++;
					c = signature.charAt(i);
				}
				localPosition = n;
				requestor.acceptPosition(localPosition);
				continue;
			case Signature.C_STATIC_ARRAY2:
			case Signature.C_SLICE2:
				return i;
			case Signature.C_CONST:
				i = process0(signature, i + 1, wantSignature, requestor);
				requestor.acceptConst(substring(signature, start, i, wantSignature));
				return i;
			case Signature.C_IMMUTABLE:
				i = process0(signature, i + 1, wantSignature, requestor);
				requestor.acceptInvariant(substring(signature, start, i, wantSignature));
				return i;
			case Signature.C_AUTO:
				requestor.acceptAutomaticTypeInference();
				return i + 1;
			default:
				// Try with type basic
				TypeBasic type = TypeBasic.fromSignature(first);
				if (type != null) {
					requestor.acceptPrimitive(type);
					return i + 1;
				} else {
					throw new IllegalArgumentException("Invalid signature: " + signature);
				}
			}
		}
		
		return i;
	}
	
	private static String substring(String s, int start, int end, boolean wantSignature) {
		if (wantSignature) {
			return s.substring(start, end);
		} else {
			return null;
		}
	}
	
	private static boolean isSymbol(char c) {
		switch(c) {
		case Signature.C_CLASS:
		case Signature.C_STRUCT:
		case Signature.C_UNION:
		case Signature.C_INTERFACE:
		case Signature.C_ENUM:
		case Signature.C_ENUM_MEMBER:
		case Signature.C_VARIABLE:
		case Signature.C_ALIAS:
		case Signature.C_TYPEDEF:
		case Signature.C_FUNCTION:
		case Signature.C_TEMPLATE:
		case Signature.C_TEMPLATED_CLASS:
		case Signature.C_TEMPLATED_STRUCT:
		case Signature.C_TEMPLATED_UNION:
		case Signature.C_TEMPLATED_INTERFACE:
		case Signature.C_TEMPLATED_FUNCTION:
		case Signature.C_TEMPLATE_INSTANCE:
		case Signature.C_POSITION:
			return true;
		default:
			return false;
		}
	}
	
	private static int argumentModifier(String signature, int i, ISignatureRequestor requestor) {
		char c = signature.charAt(i);
		switch(c) {
		case Signature.C_MODIFIER_OUT: requestor.acceptArgumentModifier(STC.STCout); i++; break;
		case Signature.C_MODIFIER_REF: requestor.acceptArgumentModifier(STC.STCref); i++; break;
		case Signature.C_MODIFIER_LAZY: requestor.acceptArgumentModifier(STC.STClazy); i++; break;
		default: requestor.acceptArgumentModifier(STC.STCin); break;
		}
		return i;
	}
	
	/*
	 * Given a signature like 4test3foo it returns ["test", "foo"].
	 * After the function call, length[0] will return the consumed
	 * signature length.
	 */
	private static char[][] splitSignature(String signature, int start, int[] end) {
		List<char[]> piecesList = new ArrayList<char[]>();
		
		int i;
		for(i = start; i < signature.length(); i++) {
			char c = signature.charAt(i);
			if (!Character.isDigit(c)) {
				break;
			}
			int n = 0;
			while(Character.isDigit(c)) {
				n = 10 * n + (c - '0');
				i++;
				c = signature.charAt(i);
			}
			String name = null;
			try {
				name = signature.substring(i, i + n);
			} catch (StringIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			piecesList.add(name.toCharArray());
			i += n - 1;
		}
		
		end[0] = i;
		
		return (char[][]) piecesList.toArray(new char[piecesList.size()][]);
	}

}
