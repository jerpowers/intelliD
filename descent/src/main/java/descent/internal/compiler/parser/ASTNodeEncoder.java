package descent.internal.compiler.parser;

import descent.core.compiler.CharOperation;


/**
 * Encodes ASTNodes into char arrays and viceversa.
 * 
 * <p>This class is not thread safe.</p>
 */
public class ASTNodeEncoder {
	
	public static interface IParserFactory {
		
		Parser newParser(char[] source, int offset, int length,
				boolean tokenizeComments, boolean tokenizePragmas,
				boolean tokenizeWhiteSpace, boolean recordLineSeparator,
				int apiLevel,
				char[][] taskTags, char[][] taskPriorities, boolean isTaskCaseSensitive,
				char[] filename);
		
		IParserFactory Default = new IParserFactory() {
			@Override
			public Parser newParser(char[] source, int offset, int length,
					boolean tokenizeComments, boolean tokenizePragmas, boolean tokenizeWhiteSpace, boolean recordLineSeparator,
					int apiLevel, char[][] taskTags, char[][] taskPriorities, boolean isTaskCaseSensitive,
					char[] filename) {
				return new Parser(source, offset, length, tokenizeComments, tokenizePragmas, tokenizeWhiteSpace, recordLineSeparator, apiLevel, taskTags, taskPriorities, isTaskCaseSensitive, filename);
			}
		};
		
	}
	
	private final static char[] nastyChar = { '/' };
	private final static char[] nastyCharSoltuion = { '*', '!', '_', '!', '*' };
	
	private Parser parser;
	private IParserFactory parserFactory;
	private final int apiLevel;
	private Parser initParser(char[] source) {
		if (parser == null) {
			parser = parserFactory.newParser(source, 0, source.length, false, false, false, false, apiLevel, null, null, false, null);
		} else {
			parser.reset(source, 0, source.length, false, false, false, false);
		}
		parser.nextToken();
		return parser;
	}
	
	public ASTNodeEncoder(int apiLevel) {
		this(apiLevel, IParserFactory.Default);
	}
	
	public ASTNodeEncoder(int apiLevel, IParserFactory parserFactory) {
		this.apiLevel = apiLevel;
		this.parserFactory = parserFactory;
	}
	
	// TODO horrible hack: the indexer may get consufed if the expression has
	// the '/' character in it, so replace it with some very improbable
	// string, and then replace it back later
	
	public static char[] encoderForIndexer(char[] value) {
		if (CharOperation.indexOf('/', value) != -1) {
			return CharOperation.replace(value, nastyChar, nastyCharSoltuion);
		} else {
			return value;
		}
	}
	
	private char[] decodeForIndexer(char[] value) {
		if (CharOperation.indexOf(nastyCharSoltuion, value, false) != -1) {
			return CharOperation.replace(value, nastyCharSoltuion, nastyChar);
		} else {
			return value;
		}
	}
	
	public char[] encodeExpression(Expression value) {
		if (value == null) {
			return null;
		}
		
		char[] result = value.toString().toCharArray();
		return encoderForIndexer(result);
	}
	
	public Expression decodeExpression(char[] value) {
		if (value == null || value.length == 0) {
			return null;
		}
		
		value = decodeForIndexer(value);
		
		return initParser(value).parseExpression();
	}
	
	public Expression decodeDefaultInitExpression(char[] value) {
		if (apiLevel < Parser.D2)
			return decodeExpression(value);
		
		if (value == null || value.length == 0) {
			return null;
		}
		
		value = decodeForIndexer(value);
		
		return initParser(value).parseDefaultInitExp();
	}
	
	public char[] encodeInitializer(Initializer init) {
		if (init == null) {
			return null;
		}
		
		if (init instanceof ExpInitializer) {
			return encodeExpression(((ExpInitializer) init).exp);
		}
		
		char[] result = init.toString().toCharArray();
		return encoderForIndexer(result);
	}
	
	public Initializer decodeInitializer(char[] value) {
		if (value == null || value.length == 0) {
			return null;
		}
		
		// The parser expects "void;", so if there's no semicolon after "void",
		// it tries to parse an expression. This is why this if is here.
		if (CharOperation.equals(value, TOK.TOKvoid.charArrayValue)) {
			return new VoidInitializer(null, 0);
		}
		
		value = decodeForIndexer(value);

		return initParser(value).parseInitializer();
	}
	
	public TemplateMixin decodeTemplateMixin(String source, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("mixin ");
		sb.append(source);
		if (name != null && name.length() > 0) {
			sb.append(" ");
			sb.append(name);
		}
		sb.append(";");
		
		char[] value = new char[sb.length()];
		sb.getChars(0, value.length, value, 0);
		return initParser(value).parseMixin();
	}

}
