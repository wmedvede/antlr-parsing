// $ANTLR 3.5 src/main/resources/parser/Java.g 2014-02-27 19:24:34

    package parser;
    import util.ParserUtil;
    import parser.metadata.*;
    import parser.metadata.ElementDescriptor.ElementType;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/** A Java 1.5 grammar for ANTLR v3 derived from the spec
 *
 *  This is a very close representation of the spec; the changes
 *  are comestic (remove left recursion) and also fixes (the spec
 *  isn't exactly perfect).  I have run this on the 1.4.2 source
 *  and some nasty looking enums from 1.5, but have not really
 *  tested for 1.5 compatibility.
 *
 *  I built this with: java -Xmx100M org.antlr.Tool java.g
 *  and got two errors that are ok (for now):
 *  java.g:691:9: Decision can match input such as
 *    "'0'..'9'{'E', 'e'}{'+', '-'}'0'..'9'{'D', 'F', 'd', 'f'}"
 *    using multiple alternatives: 3, 4
 *  As a result, alternative(s) 4 were disabled for that input
 *  java.g:734:35: Decision can match input such as "{'$', 'A'..'Z',
 *    '_', 'a'..'z', '\u00C0'..'\u00D6', '\u00D8'..'\u00F6',
 *    '\u00F8'..'\u1FFF', '\u3040'..'\u318F', '\u3300'..'\u337F',
 *    '\u3400'..'\u3D2D', '\u4E00'..'\u9FFF', '\uF900'..'\uFAFF'}"
 *    using multiple alternatives: 1, 2
 *  As a result, alternative(s) 2 were disabled for that input
 *
 *  You can turn enum on/off as a keyword :)
 *
 *  Version 1.0 -- initial release July 5, 2006 (requires 3.0b2 or higher)
 *
 *  Primary author: Terence Parr, July 2006
 *
 *  Version 1.0.1 -- corrections by Koen Vanderkimpen & Marko van Dooren,
 *      October 25, 2006;
 *      fixed normalInterfaceDeclaration: now uses typeParameters instead
 *          of typeParameter (according to JLS, 3rd edition)
 *      fixed castExpression: no longer allows expression next to type
 *          (according to semantics in JLS, in contrast with syntax in JLS)
 *
 *  Version 1.0.2 -- Terence Parr, Nov 27, 2006
 *      java spec I built this from had some bizarre for-loop control.
 *          Looked weird and so I looked elsewhere...Yep, it's messed up.
 *          simplified.
 *
 *  Version 1.0.3 -- Chris Hogue, Feb 26, 2007
 *      Factored out an annotationName rule and used it in the annotation rule.
 *          Not sure why, but typeName wasn't recognizing references to inner
 *          annotations (e.g. @InterfaceName.InnerAnnotation())
 *      Factored out the elementValue section of an annotation reference.  Created
 *          elementValuePair and elementValuePairs rules, then used them in the
 *          annotation rule.  Allows it to recognize annotation references with
 *          multiple, comma separated attributes.
 *      Updated elementValueArrayInitializer so that it allows multiple elements.
 *          (It was only allowing 0 or 1 element).
 *      Updated localVariableDeclaration to allow annotations.  Interestingly the JLS
 *          doesn't appear to indicate this is legal, but it does work as of at least
 *          JDK 1.5.0_06.
 *      Moved the Identifier portion of annotationTypeElementRest to annotationMethodRest.
 *          Because annotationConstantRest already references variableDeclarator which
 *          has the Identifier portion in it, the parser would fail on constants in
 *          annotation definitions because it expected two identifiers.
 *      Added optional trailing ';' to the alternatives in annotationTypeElementRest.
 *          Wouldn't handle an inner interface that has a trailing ';'.
 *      Swapped the expression and type rule reference order in castExpression to
 *          make it check for genericized casts first.  It was failing to recognize a
 *          statement like  "Class<Byte> TYPE = (Class<Byte>)...;" because it was seeing
 *          'Class<Byte' in the cast expression as a less than expression, then failing
 *          on the '>'.
 *      Changed createdName to use typeArguments instead of nonWildcardTypeArguments.
 *         
 *      Changed the 'this' alternative in primary to allow 'identifierSuffix' rather than
 *          just 'arguments'.  The case it couldn't handle was a call to an explicit
 *          generic method invocation (e.g. this.<E>doSomething()).  Using identifierSuffix
 *          may be overly aggressive--perhaps should create a more constrained thisSuffix rule?
 *
 *  Version 1.0.4 -- Hiroaki Nakamura, May 3, 2007
 *
 *  Fixed formalParameterDecls, localVariableDeclaration, forInit,
 *  and forVarControl to use variableModifier* not 'final'? (annotation)?
 *
 *  Version 1.0.5 -- Terence, June 21, 2007
 *  --a[i].foo didn't work. Fixed unaryExpression
 *
 *  Version 1.0.6 -- John Ridgway, March 17, 2008
 *      Made "assert" a switchable keyword like "enum".
 *      Fixed compilationUnit to disallow "annotation importDeclaration ...".
 *      Changed "Identifier ('.' Identifier)*" to "qualifiedName" in more
 *          places.
 *      Changed modifier* and/or variableModifier* to classOrInterfaceModifiers,
 *          modifiers or variableModifiers, as appropriate.
 *      Renamed "bound" to "typeBound" to better match language in the JLS.
 *      Added "memberDeclaration" which rewrites to methodDeclaration or
 *      fieldDeclaration and pulled type into memberDeclaration.  So we parse
 *          type and then move on to decide whether we're dealing with a field
 *          or a method.
 *      Modified "constructorDeclaration" to use "constructorBody" instead of
 *          "methodBody".  constructorBody starts with explicitConstructorInvocation,
 *          then goes on to blockStatement*.  Pulling explicitConstructorInvocation
 *          out of expressions allowed me to simplify "primary".
 *      Changed variableDeclarator to simplify it.
 *      Changed type to use classOrInterfaceType, thus simplifying it; of course
 *          I then had to add classOrInterfaceType, but it is used in several
 *          places.
 *      Fixed annotations, old version allowed "@X(y,z)", which is illegal.
 *      Added optional comma to stop of "elementValueArrayInitializer"; as per JLS.
 *      Changed annotationTypeElementRest to use normalClassDeclaration and
 *          normalInterfaceDeclaration rather than classDeclaration and
 *          interfaceDeclaration, thus getting rid of a couple of grammar ambiguities.
 *      Split localVariableDeclaration into localVariableDeclarationStatement
 *          (includes the terminating semi-colon) and localVariableDeclaration.
 *          This allowed me to use localVariableDeclaration in "forInit" clauses,
 *           simplifying them.
 *      Changed switchBlockStatementGroup to use multiple labels.  This adds an
 *          ambiguity, but if one uses appropriately greedy parsing it yields the
 *           parse that is closest to the meaning of the switch statement.
 *      Renamed "forVarControl" to "enhancedForControl" -- JLS language.
 *      Added semantic predicates to test for shift operations rather than other
 *          things.  Thus, for instance, the string "< <" will never be treated
 *          as a left-shift operator.
 *      In "creator" we rule out "nonWildcardTypeArguments" on arrayCreation,
 *          which are illegal.
 *      Moved "nonWildcardTypeArguments into innerCreator.
 *      Removed 'super' superSuffix from explicitGenericInvocation, since that
 *          is only used in explicitConstructorInvocation at the beginning of a
 *           constructorBody.  (This is part of the simplification of expressions
 *           mentioned earlier.)
 *      Simplified primary (got rid of those things that are only used in
 *          explicitConstructorInvocation).
 *      Lexer -- removed "Exponent?" from FloatingPointLiteral choice 4, since it
 *          led to an ambiguity.
 *
 *      This grammar successfully parses every .java file in the JDK 1.5 source
 *          tree (excluding those whose file names include '-', which are not
 *          valid Java compilation units).
 *
 *  Known remaining problems:
 *      "Letter" and "JavaIDDigit" are wrong.  The actual specification of
 *      "Letter" should be "a character for which the method
 *      Character.isJavaIdentifierStart(int) returns true."  A "Java
 *      letter-or-digit is a character for which the method
 *      Character.isJavaIdentifierPart(int) returns true."
 */
@SuppressWarnings("all")
public class JavaParser extends JavaParserBase {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ABSTRACT", "AMP", "AMPAMP", "AMPEQ", 
		"ASSERT", "BANG", "BANGEQ", "BAR", "BARBAR", "BAREQ", "BOOLEAN", "BREAK", 
		"BYTE", "CARET", "CARETEQ", "CASE", "CATCH", "CHAR", "CHARLITERAL", "CLASS", 
		"COLON", "COMMA", "COMMENT", "CONST", "CONTINUE", "DEFAULT", "DO", "DOT", 
		"DOUBLE", "DOUBLELITERAL", "DoubleSuffix", "ELLIPSIS", "ELSE", "ENUM", 
		"EQ", "EQEQ", "EXTENDS", "EscapeSequence", "Exponent", "FALSE", "FINAL", 
		"FINALLY", "FLOAT", "FLOATLITERAL", "FOR", "FloatSuffix", "GOTO", "GT", 
		"HexDigit", "HexPrefix", "IDENTIFIER", "IF", "IMPLEMENTS", "IMPORT", "INSTANCEOF", 
		"INT", "INTERFACE", "INTLITERAL", "IdentifierPart", "IdentifierStart", 
		"IntegerNumber", "LBRACE", "LBRACKET", "LINE_COMMENT", "LONG", "LONGLITERAL", 
		"LPAREN", "LT", "LongSuffix", "MONKEYS_AT", "NATIVE", "NEW", "NULL", "NonIntegerNumber", 
		"PACKAGE", "PERCENT", "PERCENTEQ", "PLUS", "PLUSEQ", "PLUSPLUS", "PRIVATE", 
		"PROTECTED", "PUBLIC", "QUES", "RBRACE", "RBRACKET", "RETURN", "RPAREN", 
		"SEMI", "SHORT", "SLASH", "SLASHEQ", "STAR", "STAREQ", "STATIC", "STRICTFP", 
		"STRINGLITERAL", "SUB", "SUBEQ", "SUBSUB", "SUPER", "SWITCH", "SYNCHRONIZED", 
		"SurrogateIdentifer", "THIS", "THROW", "THROWS", "TILDE", "TRANSIENT", 
		"TRUE", "TRY", "VOID", "VOLATILE", "WHILE", "WS"
	};
	public static final int EOF=-1;
	public static final int ABSTRACT=4;
	public static final int AMP=5;
	public static final int AMPAMP=6;
	public static final int AMPEQ=7;
	public static final int ASSERT=8;
	public static final int BANG=9;
	public static final int BANGEQ=10;
	public static final int BAR=11;
	public static final int BARBAR=12;
	public static final int BAREQ=13;
	public static final int BOOLEAN=14;
	public static final int BREAK=15;
	public static final int BYTE=16;
	public static final int CARET=17;
	public static final int CARETEQ=18;
	public static final int CASE=19;
	public static final int CATCH=20;
	public static final int CHAR=21;
	public static final int CHARLITERAL=22;
	public static final int CLASS=23;
	public static final int COLON=24;
	public static final int COMMA=25;
	public static final int COMMENT=26;
	public static final int CONST=27;
	public static final int CONTINUE=28;
	public static final int DEFAULT=29;
	public static final int DO=30;
	public static final int DOT=31;
	public static final int DOUBLE=32;
	public static final int DOUBLELITERAL=33;
	public static final int DoubleSuffix=34;
	public static final int ELLIPSIS=35;
	public static final int ELSE=36;
	public static final int ENUM=37;
	public static final int EQ=38;
	public static final int EQEQ=39;
	public static final int EXTENDS=40;
	public static final int EscapeSequence=41;
	public static final int Exponent=42;
	public static final int FALSE=43;
	public static final int FINAL=44;
	public static final int FINALLY=45;
	public static final int FLOAT=46;
	public static final int FLOATLITERAL=47;
	public static final int FOR=48;
	public static final int FloatSuffix=49;
	public static final int GOTO=50;
	public static final int GT=51;
	public static final int HexDigit=52;
	public static final int HexPrefix=53;
	public static final int IDENTIFIER=54;
	public static final int IF=55;
	public static final int IMPLEMENTS=56;
	public static final int IMPORT=57;
	public static final int INSTANCEOF=58;
	public static final int INT=59;
	public static final int INTERFACE=60;
	public static final int INTLITERAL=61;
	public static final int IdentifierPart=62;
	public static final int IdentifierStart=63;
	public static final int IntegerNumber=64;
	public static final int LBRACE=65;
	public static final int LBRACKET=66;
	public static final int LINE_COMMENT=67;
	public static final int LONG=68;
	public static final int LONGLITERAL=69;
	public static final int LPAREN=70;
	public static final int LT=71;
	public static final int LongSuffix=72;
	public static final int MONKEYS_AT=73;
	public static final int NATIVE=74;
	public static final int NEW=75;
	public static final int NULL=76;
	public static final int NonIntegerNumber=77;
	public static final int PACKAGE=78;
	public static final int PERCENT=79;
	public static final int PERCENTEQ=80;
	public static final int PLUS=81;
	public static final int PLUSEQ=82;
	public static final int PLUSPLUS=83;
	public static final int PRIVATE=84;
	public static final int PROTECTED=85;
	public static final int PUBLIC=86;
	public static final int QUES=87;
	public static final int RBRACE=88;
	public static final int RBRACKET=89;
	public static final int RETURN=90;
	public static final int RPAREN=91;
	public static final int SEMI=92;
	public static final int SHORT=93;
	public static final int SLASH=94;
	public static final int SLASHEQ=95;
	public static final int STAR=96;
	public static final int STAREQ=97;
	public static final int STATIC=98;
	public static final int STRICTFP=99;
	public static final int STRINGLITERAL=100;
	public static final int SUB=101;
	public static final int SUBEQ=102;
	public static final int SUBSUB=103;
	public static final int SUPER=104;
	public static final int SWITCH=105;
	public static final int SYNCHRONIZED=106;
	public static final int SurrogateIdentifer=107;
	public static final int THIS=108;
	public static final int THROW=109;
	public static final int THROWS=110;
	public static final int TILDE=111;
	public static final int TRANSIENT=112;
	public static final int TRUE=113;
	public static final int TRY=114;
	public static final int VOID=115;
	public static final int VOLATILE=116;
	public static final int WHILE=117;
	public static final int WS=118;

	// delegates
	public JavaParserBase[] getDelegates() {
		return new JavaParserBase[] {};
	}

	// delegators


	public JavaParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public JavaParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
		this.state.ruleMemo = new HashMap[381+1];


	}

	@Override public String[] getTokenNames() { return JavaParser.tokenNames; }
	@Override public String getGrammarFileName() { return "src/main/resources/parser/Java.g"; }






	// $ANTLR start "compilationUnit"
	// src/main/resources/parser/Java.g:316:1: compilationUnit : ( ( annotations )? packageDeclaration )? ( importDeclaration )* ( typeDeclaration )* ;
	public final void compilationUnit() throws RecognitionException {
		int compilationUnit_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 1) ) { return; }

			// src/main/resources/parser/Java.g:317:5: ( ( ( annotations )? packageDeclaration )? ( importDeclaration )* ( typeDeclaration )* )
			// src/main/resources/parser/Java.g:317:9: ( ( annotations )? packageDeclaration )? ( importDeclaration )* ( typeDeclaration )*
			{
			// src/main/resources/parser/Java.g:317:9: ( ( annotations )? packageDeclaration )?
			int alt2=2;
			int LA2_0 = input.LA(1);
			if ( (LA2_0==MONKEYS_AT) ) {
				int LA2_1 = input.LA(2);
				if ( (synpred2_Java()) ) {
					alt2=1;
				}
			}
			else if ( (LA2_0==PACKAGE) ) {
				alt2=1;
			}
			switch (alt2) {
				case 1 :
					// src/main/resources/parser/Java.g:317:13: ( annotations )? packageDeclaration
					{
					// src/main/resources/parser/Java.g:317:13: ( annotations )?
					int alt1=2;
					int LA1_0 = input.LA(1);
					if ( (LA1_0==MONKEYS_AT) ) {
						alt1=1;
					}
					switch (alt1) {
						case 1 :
							// src/main/resources/parser/Java.g:317:14: annotations
							{
							pushFollow(FOLLOW_annotations_in_compilationUnit127);
							annotations();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					pushFollow(FOLLOW_packageDeclaration_in_compilationUnit156);
					packageDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:321:9: ( importDeclaration )*
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( (LA3_0==IMPORT) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// src/main/resources/parser/Java.g:321:10: importDeclaration
					{
					pushFollow(FOLLOW_importDeclaration_in_compilationUnit178);
					importDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop3;
				}
			}

			// src/main/resources/parser/Java.g:323:9: ( typeDeclaration )*
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( (LA4_0==ABSTRACT||LA4_0==BOOLEAN||LA4_0==BYTE||LA4_0==CHAR||LA4_0==CLASS||LA4_0==DOUBLE||LA4_0==ENUM||LA4_0==FINAL||LA4_0==FLOAT||LA4_0==IDENTIFIER||(LA4_0 >= INT && LA4_0 <= INTERFACE)||LA4_0==LONG||LA4_0==LT||(LA4_0 >= MONKEYS_AT && LA4_0 <= NATIVE)||(LA4_0 >= PRIVATE && LA4_0 <= PUBLIC)||(LA4_0 >= SEMI && LA4_0 <= SHORT)||(LA4_0 >= STATIC && LA4_0 <= STRICTFP)||LA4_0==SYNCHRONIZED||LA4_0==TRANSIENT||(LA4_0 >= VOID && LA4_0 <= VOLATILE)) ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// src/main/resources/parser/Java.g:323:10: typeDeclaration
					{
					pushFollow(FOLLOW_typeDeclaration_in_compilationUnit200);
					typeDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop4;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 1, compilationUnit_StartIndex); }

		}
	}
	// $ANTLR end "compilationUnit"



	// $ANTLR start "packageDeclaration"
	// src/main/resources/parser/Java.g:327:1: packageDeclaration : 'package' qualifiedName ';' ;
	public final void packageDeclaration() throws RecognitionException {
		int packageDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 2) ) { return; }

			// src/main/resources/parser/Java.g:328:5: ( 'package' qualifiedName ';' )
			// src/main/resources/parser/Java.g:328:9: 'package' qualifiedName ';'
			{
			match(input,PACKAGE,FOLLOW_PACKAGE_in_packageDeclaration231); if (state.failed) return;
			pushFollow(FOLLOW_qualifiedName_in_packageDeclaration233);
			qualifiedName();
			state._fsp--;
			if (state.failed) return;
			match(input,SEMI,FOLLOW_SEMI_in_packageDeclaration243); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 2, packageDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "packageDeclaration"



	// $ANTLR start "importDeclaration"
	// src/main/resources/parser/Java.g:332:1: importDeclaration : ( 'import' ( 'static' )? IDENTIFIER '.' '*' ';' | 'import' ( 'static' )? IDENTIFIER ( '.' IDENTIFIER )+ ( '.' '*' )? ';' );
	public final void importDeclaration() throws RecognitionException {
		int importDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 3) ) { return; }

			// src/main/resources/parser/Java.g:333:5: ( 'import' ( 'static' )? IDENTIFIER '.' '*' ';' | 'import' ( 'static' )? IDENTIFIER ( '.' IDENTIFIER )+ ( '.' '*' )? ';' )
			int alt9=2;
			int LA9_0 = input.LA(1);
			if ( (LA9_0==IMPORT) ) {
				int LA9_1 = input.LA(2);
				if ( (LA9_1==STATIC) ) {
					int LA9_2 = input.LA(3);
					if ( (LA9_2==IDENTIFIER) ) {
						int LA9_3 = input.LA(4);
						if ( (LA9_3==DOT) ) {
							int LA9_4 = input.LA(5);
							if ( (LA9_4==STAR) ) {
								alt9=1;
							}
							else if ( (LA9_4==IDENTIFIER) ) {
								alt9=2;
							}

							else {
								if (state.backtracking>0) {state.failed=true; return;}
								int nvaeMark = input.mark();
								try {
									for (int nvaeConsume = 0; nvaeConsume < 5 - 1; nvaeConsume++) {
										input.consume();
									}
									NoViableAltException nvae =
										new NoViableAltException("", 9, 4, input);
									throw nvae;
								} finally {
									input.rewind(nvaeMark);
								}
							}

						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 9, 3, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 9, 2, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA9_1==IDENTIFIER) ) {
					int LA9_3 = input.LA(3);
					if ( (LA9_3==DOT) ) {
						int LA9_4 = input.LA(4);
						if ( (LA9_4==STAR) ) {
							alt9=1;
						}
						else if ( (LA9_4==IDENTIFIER) ) {
							alt9=2;
						}

						else {
							if (state.backtracking>0) {state.failed=true; return;}
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 9, 4, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 9, 3, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 9, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 9, 0, input);
				throw nvae;
			}

			switch (alt9) {
				case 1 :
					// src/main/resources/parser/Java.g:333:9: 'import' ( 'static' )? IDENTIFIER '.' '*' ';'
					{
					match(input,IMPORT,FOLLOW_IMPORT_in_importDeclaration264); if (state.failed) return;
					// src/main/resources/parser/Java.g:334:9: ( 'static' )?
					int alt5=2;
					int LA5_0 = input.LA(1);
					if ( (LA5_0==STATIC) ) {
						alt5=1;
					}
					switch (alt5) {
						case 1 :
							// src/main/resources/parser/Java.g:334:10: 'static'
							{
							match(input,STATIC,FOLLOW_STATIC_in_importDeclaration276); if (state.failed) return;
							}
							break;

					}

					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_importDeclaration297); if (state.failed) return;
					match(input,DOT,FOLLOW_DOT_in_importDeclaration299); if (state.failed) return;
					match(input,STAR,FOLLOW_STAR_in_importDeclaration301); if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_importDeclaration311); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:338:9: 'import' ( 'static' )? IDENTIFIER ( '.' IDENTIFIER )+ ( '.' '*' )? ';'
					{
					match(input,IMPORT,FOLLOW_IMPORT_in_importDeclaration328); if (state.failed) return;
					// src/main/resources/parser/Java.g:339:9: ( 'static' )?
					int alt6=2;
					int LA6_0 = input.LA(1);
					if ( (LA6_0==STATIC) ) {
						alt6=1;
					}
					switch (alt6) {
						case 1 :
							// src/main/resources/parser/Java.g:339:10: 'static'
							{
							match(input,STATIC,FOLLOW_STATIC_in_importDeclaration340); if (state.failed) return;
							}
							break;

					}

					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_importDeclaration361); if (state.failed) return;
					// src/main/resources/parser/Java.g:342:9: ( '.' IDENTIFIER )+
					int cnt7=0;
					loop7:
					while (true) {
						int alt7=2;
						int LA7_0 = input.LA(1);
						if ( (LA7_0==DOT) ) {
							int LA7_1 = input.LA(2);
							if ( (LA7_1==IDENTIFIER) ) {
								alt7=1;
							}

						}

						switch (alt7) {
						case 1 :
							// src/main/resources/parser/Java.g:342:10: '.' IDENTIFIER
							{
							match(input,DOT,FOLLOW_DOT_in_importDeclaration372); if (state.failed) return;
							match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_importDeclaration374); if (state.failed) return;
							}
							break;

						default :
							if ( cnt7 >= 1 ) break loop7;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(7, input);
							throw eee;
						}
						cnt7++;
					}

					// src/main/resources/parser/Java.g:344:9: ( '.' '*' )?
					int alt8=2;
					int LA8_0 = input.LA(1);
					if ( (LA8_0==DOT) ) {
						alt8=1;
					}
					switch (alt8) {
						case 1 :
							// src/main/resources/parser/Java.g:344:10: '.' '*'
							{
							match(input,DOT,FOLLOW_DOT_in_importDeclaration396); if (state.failed) return;
							match(input,STAR,FOLLOW_STAR_in_importDeclaration398); if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_importDeclaration419); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 3, importDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "importDeclaration"



	// $ANTLR start "qualifiedImportName"
	// src/main/resources/parser/Java.g:349:1: qualifiedImportName : IDENTIFIER ( '.' IDENTIFIER )* ;
	public final void qualifiedImportName() throws RecognitionException {
		int qualifiedImportName_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 4) ) { return; }

			// src/main/resources/parser/Java.g:350:5: ( IDENTIFIER ( '.' IDENTIFIER )* )
			// src/main/resources/parser/Java.g:350:9: IDENTIFIER ( '.' IDENTIFIER )*
			{
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_qualifiedImportName439); if (state.failed) return;
			// src/main/resources/parser/Java.g:351:9: ( '.' IDENTIFIER )*
			loop10:
			while (true) {
				int alt10=2;
				int LA10_0 = input.LA(1);
				if ( (LA10_0==DOT) ) {
					alt10=1;
				}

				switch (alt10) {
				case 1 :
					// src/main/resources/parser/Java.g:351:10: '.' IDENTIFIER
					{
					match(input,DOT,FOLLOW_DOT_in_qualifiedImportName450); if (state.failed) return;
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_qualifiedImportName452); if (state.failed) return;
					}
					break;

				default :
					break loop10;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 4, qualifiedImportName_StartIndex); }

		}
	}
	// $ANTLR end "qualifiedImportName"



	// $ANTLR start "typeDeclaration"
	// src/main/resources/parser/Java.g:355:1: typeDeclaration : ( classOrInterfaceDeclaration | ';' );
	public final void typeDeclaration() throws RecognitionException {
		int typeDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 5) ) { return; }

			// src/main/resources/parser/Java.g:356:5: ( classOrInterfaceDeclaration | ';' )
			int alt11=2;
			int LA11_0 = input.LA(1);
			if ( (LA11_0==ABSTRACT||LA11_0==BOOLEAN||LA11_0==BYTE||LA11_0==CHAR||LA11_0==CLASS||LA11_0==DOUBLE||LA11_0==ENUM||LA11_0==FINAL||LA11_0==FLOAT||LA11_0==IDENTIFIER||(LA11_0 >= INT && LA11_0 <= INTERFACE)||LA11_0==LONG||LA11_0==LT||(LA11_0 >= MONKEYS_AT && LA11_0 <= NATIVE)||(LA11_0 >= PRIVATE && LA11_0 <= PUBLIC)||LA11_0==SHORT||(LA11_0 >= STATIC && LA11_0 <= STRICTFP)||LA11_0==SYNCHRONIZED||LA11_0==TRANSIENT||(LA11_0 >= VOID && LA11_0 <= VOLATILE)) ) {
				alt11=1;
			}
			else if ( (LA11_0==SEMI) ) {
				alt11=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}

			switch (alt11) {
				case 1 :
					// src/main/resources/parser/Java.g:356:9: classOrInterfaceDeclaration
					{
					pushFollow(FOLLOW_classOrInterfaceDeclaration_in_typeDeclaration483);
					classOrInterfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:357:9: ';'
					{
					match(input,SEMI,FOLLOW_SEMI_in_typeDeclaration493); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 5, typeDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "typeDeclaration"



	// $ANTLR start "classOrInterfaceDeclaration"
	// src/main/resources/parser/Java.g:360:1: classOrInterfaceDeclaration : ( classDeclaration | interfaceDeclaration );
	public final void classOrInterfaceDeclaration() throws RecognitionException {
		int classOrInterfaceDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 6) ) { return; }

			// src/main/resources/parser/Java.g:361:5: ( classDeclaration | interfaceDeclaration )
			int alt12=2;
			switch ( input.LA(1) ) {
			case MONKEYS_AT:
				{
				int LA12_1 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case PUBLIC:
				{
				int LA12_2 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case PROTECTED:
				{
				int LA12_3 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case PRIVATE:
				{
				int LA12_4 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case STATIC:
				{
				int LA12_5 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case ABSTRACT:
				{
				int LA12_6 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case FINAL:
				{
				int LA12_7 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case NATIVE:
				{
				int LA12_8 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case SYNCHRONIZED:
				{
				int LA12_9 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case TRANSIENT:
				{
				int LA12_10 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case VOLATILE:
				{
				int LA12_11 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case STRICTFP:
				{
				int LA12_12 = input.LA(2);
				if ( (synpred12_Java()) ) {
					alt12=1;
				}
				else if ( (true) ) {
					alt12=2;
				}

				}
				break;
			case CLASS:
			case ENUM:
				{
				alt12=1;
				}
				break;
			case INTERFACE:
				{
				alt12=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 12, 0, input);
				throw nvae;
			}
			switch (alt12) {
				case 1 :
					// src/main/resources/parser/Java.g:361:10: classDeclaration
					{
					pushFollow(FOLLOW_classDeclaration_in_classOrInterfaceDeclaration514);
					classDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:362:9: interfaceDeclaration
					{
					pushFollow(FOLLOW_interfaceDeclaration_in_classOrInterfaceDeclaration524);
					interfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 6, classOrInterfaceDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "classOrInterfaceDeclaration"



	// $ANTLR start "modifiers"
	// src/main/resources/parser/Java.g:366:1: modifiers : ( annotation |s= 'public' |s= 'protected' |s= 'private' |s= 'static' |s= 'abstract' |s= 'final' |s= 'native' |s= 'synchronized' |s= 'transient' |s= 'volatile' |s= 'strictfp' )* ;
	public final void modifiers() throws RecognitionException {
		int modifiers_StartIndex = input.index();

		Token s=null;


		        HasModifiers hasModifiers = null;
		        if (!isBacktracking()) {
		            hasModifiers = peekHasModifiers();
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 7) ) { return; }

			// src/main/resources/parser/Java.g:373:5: ( ( annotation |s= 'public' |s= 'protected' |s= 'private' |s= 'static' |s= 'abstract' |s= 'final' |s= 'native' |s= 'synchronized' |s= 'transient' |s= 'volatile' |s= 'strictfp' )* )
			// src/main/resources/parser/Java.g:374:5: ( annotation |s= 'public' |s= 'protected' |s= 'private' |s= 'static' |s= 'abstract' |s= 'final' |s= 'native' |s= 'synchronized' |s= 'transient' |s= 'volatile' |s= 'strictfp' )*
			{
			// src/main/resources/parser/Java.g:374:5: ( annotation |s= 'public' |s= 'protected' |s= 'private' |s= 'static' |s= 'abstract' |s= 'final' |s= 'native' |s= 'synchronized' |s= 'transient' |s= 'volatile' |s= 'strictfp' )*
			loop13:
			while (true) {
				int alt13=13;
				switch ( input.LA(1) ) {
				case MONKEYS_AT:
					{
					int LA13_2 = input.LA(2);
					if ( (LA13_2==IDENTIFIER) ) {
						alt13=1;
					}

					}
					break;
				case PUBLIC:
					{
					alt13=2;
					}
					break;
				case PROTECTED:
					{
					alt13=3;
					}
					break;
				case PRIVATE:
					{
					alt13=4;
					}
					break;
				case STATIC:
					{
					alt13=5;
					}
					break;
				case ABSTRACT:
					{
					alt13=6;
					}
					break;
				case FINAL:
					{
					alt13=7;
					}
					break;
				case NATIVE:
					{
					alt13=8;
					}
					break;
				case SYNCHRONIZED:
					{
					alt13=9;
					}
					break;
				case TRANSIENT:
					{
					alt13=10;
					}
					break;
				case VOLATILE:
					{
					alt13=11;
					}
					break;
				case STRICTFP:
					{
					alt13=12;
					}
					break;
				}
				switch (alt13) {
				case 1 :
					// src/main/resources/parser/Java.g:374:10: annotation
					{
					pushFollow(FOLLOW_annotation_in_modifiers560);
					annotation();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:375:9: s= 'public'
					{
					s=(Token)match(input,PUBLIC,FOLLOW_PUBLIC_in_modifiers572); if (state.failed) return;
					if ( state.backtracking==0 ) { if (hasModifiers != null) hasModifiers.addModifier(new ModifierDesc((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null))); }
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:376:9: s= 'protected'
					{
					s=(Token)match(input,PROTECTED,FOLLOW_PROTECTED_in_modifiers595); if (state.failed) return;
					if ( state.backtracking==0 ) { if (hasModifiers != null) hasModifiers.addModifier(new ModifierDesc((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null))); }
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:377:9: s= 'private'
					{
					s=(Token)match(input,PRIVATE,FOLLOW_PRIVATE_in_modifiers615); if (state.failed) return;
					if ( state.backtracking==0 ) { if (hasModifiers != null) hasModifiers.addModifier(new ModifierDesc((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null))); }
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:378:9: s= 'static'
					{
					s=(Token)match(input,STATIC,FOLLOW_STATIC_in_modifiers637); if (state.failed) return;
					if ( state.backtracking==0 ) { if (hasModifiers != null) hasModifiers.addModifier(new ModifierDesc((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null))); }
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:379:9: s= 'abstract'
					{
					s=(Token)match(input,ABSTRACT,FOLLOW_ABSTRACT_in_modifiers660); if (state.failed) return;
					if ( state.backtracking==0 ) { if (hasModifiers != null) hasModifiers.addModifier(new ModifierDesc((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null))); }
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:380:9: s= 'final'
					{
					s=(Token)match(input,FINAL,FOLLOW_FINAL_in_modifiers681); if (state.failed) return;
					if ( state.backtracking==0 ) { if (hasModifiers != null) hasModifiers.addModifier(new ModifierDesc((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null))); }
					}
					break;
				case 8 :
					// src/main/resources/parser/Java.g:381:9: s= 'native'
					{
					s=(Token)match(input,NATIVE,FOLLOW_NATIVE_in_modifiers705); if (state.failed) return;
					if ( state.backtracking==0 ) { if (hasModifiers != null) hasModifiers.addModifier(new ModifierDesc((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null))); }
					}
					break;
				case 9 :
					// src/main/resources/parser/Java.g:382:9: s= 'synchronized'
					{
					s=(Token)match(input,SYNCHRONIZED,FOLLOW_SYNCHRONIZED_in_modifiers728); if (state.failed) return;
					if ( state.backtracking==0 ) { if (hasModifiers != null) hasModifiers.addModifier(new ModifierDesc((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null))); }
					}
					break;
				case 10 :
					// src/main/resources/parser/Java.g:383:9: s= 'transient'
					{
					s=(Token)match(input,TRANSIENT,FOLLOW_TRANSIENT_in_modifiers745); if (state.failed) return;
					if ( state.backtracking==0 ) { if (hasModifiers != null) hasModifiers.addModifier(new ModifierDesc((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null))); }
					}
					break;
				case 11 :
					// src/main/resources/parser/Java.g:384:9: s= 'volatile'
					{
					s=(Token)match(input,VOLATILE,FOLLOW_VOLATILE_in_modifiers765); if (state.failed) return;
					if ( state.backtracking==0 ) { if (hasModifiers != null) hasModifiers.addModifier(new ModifierDesc((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null))); }
					}
					break;
				case 12 :
					// src/main/resources/parser/Java.g:385:9: s= 'strictfp'
					{
					s=(Token)match(input,STRICTFP,FOLLOW_STRICTFP_in_modifiers786); if (state.failed) return;
					if ( state.backtracking==0 ) { if (hasModifiers != null) hasModifiers.addModifier(new ModifierDesc((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null))); }
					}
					break;

				default :
					break loop13;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 7, modifiers_StartIndex); }

		}
	}
	// $ANTLR end "modifiers"



	// $ANTLR start "variableModifiers"
	// src/main/resources/parser/Java.g:390:1: variableModifiers : ( 'final' | annotation )* ;
	public final void variableModifiers() throws RecognitionException {
		int variableModifiers_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 8) ) { return; }

			// src/main/resources/parser/Java.g:391:5: ( ( 'final' | annotation )* )
			// src/main/resources/parser/Java.g:391:9: ( 'final' | annotation )*
			{
			// src/main/resources/parser/Java.g:391:9: ( 'final' | annotation )*
			loop14:
			while (true) {
				int alt14=3;
				int LA14_0 = input.LA(1);
				if ( (LA14_0==FINAL) ) {
					alt14=1;
				}
				else if ( (LA14_0==MONKEYS_AT) ) {
					alt14=2;
				}

				switch (alt14) {
				case 1 :
					// src/main/resources/parser/Java.g:391:13: 'final'
					{
					match(input,FINAL,FOLLOW_FINAL_in_variableModifiers827); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:392:13: annotation
					{
					pushFollow(FOLLOW_annotation_in_variableModifiers841);
					annotation();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop14;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 8, variableModifiers_StartIndex); }

		}
	}
	// $ANTLR end "variableModifiers"



	// $ANTLR start "classDeclaration"
	// src/main/resources/parser/Java.g:397:1: classDeclaration : ( normalClassDeclaration | enumDeclaration );
	public final void classDeclaration() throws RecognitionException {
		int classDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 9) ) { return; }

			// src/main/resources/parser/Java.g:398:5: ( normalClassDeclaration | enumDeclaration )
			int alt15=2;
			switch ( input.LA(1) ) {
			case MONKEYS_AT:
				{
				int LA15_1 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case PUBLIC:
				{
				int LA15_2 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case PROTECTED:
				{
				int LA15_3 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case PRIVATE:
				{
				int LA15_4 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case STATIC:
				{
				int LA15_5 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case ABSTRACT:
				{
				int LA15_6 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case FINAL:
				{
				int LA15_7 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case NATIVE:
				{
				int LA15_8 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case SYNCHRONIZED:
				{
				int LA15_9 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case TRANSIENT:
				{
				int LA15_10 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case VOLATILE:
				{
				int LA15_11 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case STRICTFP:
				{
				int LA15_12 = input.LA(2);
				if ( (synpred27_Java()) ) {
					alt15=1;
				}
				else if ( (true) ) {
					alt15=2;
				}

				}
				break;
			case CLASS:
				{
				alt15=1;
				}
				break;
			case ENUM:
				{
				alt15=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}
			switch (alt15) {
				case 1 :
					// src/main/resources/parser/Java.g:398:9: normalClassDeclaration
					{
					pushFollow(FOLLOW_normalClassDeclaration_in_classDeclaration877);
					normalClassDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:399:9: enumDeclaration
					{
					pushFollow(FOLLOW_enumDeclaration_in_classDeclaration887);
					enumDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 9, classDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "classDeclaration"



	// $ANTLR start "normalClassDeclaration"
	// src/main/resources/parser/Java.g:402:1: normalClassDeclaration : modifiers 'class' IDENTIFIER ( typeParameters )? ( 'extends' type )? ( 'implements' typeList )? classBody ;
	public final void normalClassDeclaration() throws RecognitionException {
		int normalClassDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 10) ) { return; }

			// src/main/resources/parser/Java.g:403:5: ( modifiers 'class' IDENTIFIER ( typeParameters )? ( 'extends' type )? ( 'implements' typeList )? classBody )
			// src/main/resources/parser/Java.g:403:9: modifiers 'class' IDENTIFIER ( typeParameters )? ( 'extends' type )? ( 'implements' typeList )? classBody
			{
			pushFollow(FOLLOW_modifiers_in_normalClassDeclaration907);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			match(input,CLASS,FOLLOW_CLASS_in_normalClassDeclaration910); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_normalClassDeclaration912); if (state.failed) return;
			// src/main/resources/parser/Java.g:404:9: ( typeParameters )?
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==LT) ) {
				alt16=1;
			}
			switch (alt16) {
				case 1 :
					// src/main/resources/parser/Java.g:404:10: typeParameters
					{
					pushFollow(FOLLOW_typeParameters_in_normalClassDeclaration923);
					typeParameters();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:406:9: ( 'extends' type )?
			int alt17=2;
			int LA17_0 = input.LA(1);
			if ( (LA17_0==EXTENDS) ) {
				alt17=1;
			}
			switch (alt17) {
				case 1 :
					// src/main/resources/parser/Java.g:406:10: 'extends' type
					{
					match(input,EXTENDS,FOLLOW_EXTENDS_in_normalClassDeclaration945); if (state.failed) return;
					pushFollow(FOLLOW_type_in_normalClassDeclaration947);
					type();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:408:9: ( 'implements' typeList )?
			int alt18=2;
			int LA18_0 = input.LA(1);
			if ( (LA18_0==IMPLEMENTS) ) {
				alt18=1;
			}
			switch (alt18) {
				case 1 :
					// src/main/resources/parser/Java.g:408:10: 'implements' typeList
					{
					match(input,IMPLEMENTS,FOLLOW_IMPLEMENTS_in_normalClassDeclaration969); if (state.failed) return;
					pushFollow(FOLLOW_typeList_in_normalClassDeclaration971);
					typeList();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			pushFollow(FOLLOW_classBody_in_normalClassDeclaration1004);
			classBody();
			state._fsp--;
			if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 10, normalClassDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "normalClassDeclaration"



	// $ANTLR start "typeParameters"
	// src/main/resources/parser/Java.g:414:1: typeParameters : '<' typeParameter ( ',' typeParameter )* '>' ;
	public final void typeParameters() throws RecognitionException {
		int typeParameters_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 11) ) { return; }

			// src/main/resources/parser/Java.g:415:5: ( '<' typeParameter ( ',' typeParameter )* '>' )
			// src/main/resources/parser/Java.g:415:9: '<' typeParameter ( ',' typeParameter )* '>'
			{
			match(input,LT,FOLLOW_LT_in_typeParameters1025); if (state.failed) return;
			pushFollow(FOLLOW_typeParameter_in_typeParameters1039);
			typeParameter();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:417:13: ( ',' typeParameter )*
			loop19:
			while (true) {
				int alt19=2;
				int LA19_0 = input.LA(1);
				if ( (LA19_0==COMMA) ) {
					alt19=1;
				}

				switch (alt19) {
				case 1 :
					// src/main/resources/parser/Java.g:417:14: ',' typeParameter
					{
					match(input,COMMA,FOLLOW_COMMA_in_typeParameters1054); if (state.failed) return;
					pushFollow(FOLLOW_typeParameter_in_typeParameters1056);
					typeParameter();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop19;
				}
			}

			match(input,GT,FOLLOW_GT_in_typeParameters1081); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 11, typeParameters_StartIndex); }

		}
	}
	// $ANTLR end "typeParameters"



	// $ANTLR start "typeParameter"
	// src/main/resources/parser/Java.g:422:1: typeParameter : IDENTIFIER ( 'extends' typeBound )? ;
	public final void typeParameter() throws RecognitionException {
		int typeParameter_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 12) ) { return; }

			// src/main/resources/parser/Java.g:423:5: ( IDENTIFIER ( 'extends' typeBound )? )
			// src/main/resources/parser/Java.g:423:9: IDENTIFIER ( 'extends' typeBound )?
			{
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_typeParameter1101); if (state.failed) return;
			// src/main/resources/parser/Java.g:424:9: ( 'extends' typeBound )?
			int alt20=2;
			int LA20_0 = input.LA(1);
			if ( (LA20_0==EXTENDS) ) {
				alt20=1;
			}
			switch (alt20) {
				case 1 :
					// src/main/resources/parser/Java.g:424:10: 'extends' typeBound
					{
					match(input,EXTENDS,FOLLOW_EXTENDS_in_typeParameter1112); if (state.failed) return;
					pushFollow(FOLLOW_typeBound_in_typeParameter1114);
					typeBound();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 12, typeParameter_StartIndex); }

		}
	}
	// $ANTLR end "typeParameter"



	// $ANTLR start "typeBound"
	// src/main/resources/parser/Java.g:429:1: typeBound : type ( '&' type )* ;
	public final void typeBound() throws RecognitionException {
		int typeBound_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 13) ) { return; }

			// src/main/resources/parser/Java.g:430:5: ( type ( '&' type )* )
			// src/main/resources/parser/Java.g:430:9: type ( '&' type )*
			{
			pushFollow(FOLLOW_type_in_typeBound1146);
			type();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:431:9: ( '&' type )*
			loop21:
			while (true) {
				int alt21=2;
				int LA21_0 = input.LA(1);
				if ( (LA21_0==AMP) ) {
					alt21=1;
				}

				switch (alt21) {
				case 1 :
					// src/main/resources/parser/Java.g:431:10: '&' type
					{
					match(input,AMP,FOLLOW_AMP_in_typeBound1157); if (state.failed) return;
					pushFollow(FOLLOW_type_in_typeBound1159);
					type();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop21;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 13, typeBound_StartIndex); }

		}
	}
	// $ANTLR end "typeBound"



	// $ANTLR start "enumDeclaration"
	// src/main/resources/parser/Java.g:436:1: enumDeclaration : modifiers ( 'enum' ) IDENTIFIER ( 'implements' typeList )? enumBody ;
	public final void enumDeclaration() throws RecognitionException {
		int enumDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 14) ) { return; }

			// src/main/resources/parser/Java.g:437:5: ( modifiers ( 'enum' ) IDENTIFIER ( 'implements' typeList )? enumBody )
			// src/main/resources/parser/Java.g:437:9: modifiers ( 'enum' ) IDENTIFIER ( 'implements' typeList )? enumBody
			{
			pushFollow(FOLLOW_modifiers_in_enumDeclaration1191);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:438:9: ( 'enum' )
			// src/main/resources/parser/Java.g:438:10: 'enum'
			{
			match(input,ENUM,FOLLOW_ENUM_in_enumDeclaration1203); if (state.failed) return;
			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_enumDeclaration1224); if (state.failed) return;
			// src/main/resources/parser/Java.g:441:9: ( 'implements' typeList )?
			int alt22=2;
			int LA22_0 = input.LA(1);
			if ( (LA22_0==IMPLEMENTS) ) {
				alt22=1;
			}
			switch (alt22) {
				case 1 :
					// src/main/resources/parser/Java.g:441:10: 'implements' typeList
					{
					match(input,IMPLEMENTS,FOLLOW_IMPLEMENTS_in_enumDeclaration1235); if (state.failed) return;
					pushFollow(FOLLOW_typeList_in_enumDeclaration1237);
					typeList();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			pushFollow(FOLLOW_enumBody_in_enumDeclaration1258);
			enumBody();
			state._fsp--;
			if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 14, enumDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "enumDeclaration"



	// $ANTLR start "enumBody"
	// src/main/resources/parser/Java.g:447:1: enumBody : '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}' ;
	public final void enumBody() throws RecognitionException {
		int enumBody_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 15) ) { return; }

			// src/main/resources/parser/Java.g:448:5: ( '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}' )
			// src/main/resources/parser/Java.g:448:9: '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_enumBody1283); if (state.failed) return;
			// src/main/resources/parser/Java.g:449:9: ( enumConstants )?
			int alt23=2;
			int LA23_0 = input.LA(1);
			if ( (LA23_0==IDENTIFIER||LA23_0==MONKEYS_AT) ) {
				alt23=1;
			}
			switch (alt23) {
				case 1 :
					// src/main/resources/parser/Java.g:449:10: enumConstants
					{
					pushFollow(FOLLOW_enumConstants_in_enumBody1294);
					enumConstants();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:451:9: ( ',' )?
			int alt24=2;
			int LA24_0 = input.LA(1);
			if ( (LA24_0==COMMA) ) {
				alt24=1;
			}
			switch (alt24) {
				case 1 :
					// src/main/resources/parser/Java.g:451:9: ','
					{
					match(input,COMMA,FOLLOW_COMMA_in_enumBody1316); if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:452:9: ( enumBodyDeclarations )?
			int alt25=2;
			int LA25_0 = input.LA(1);
			if ( (LA25_0==SEMI) ) {
				alt25=1;
			}
			switch (alt25) {
				case 1 :
					// src/main/resources/parser/Java.g:452:10: enumBodyDeclarations
					{
					pushFollow(FOLLOW_enumBodyDeclarations_in_enumBody1329);
					enumBodyDeclarations();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,RBRACE,FOLLOW_RBRACE_in_enumBody1351); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 15, enumBody_StartIndex); }

		}
	}
	// $ANTLR end "enumBody"



	// $ANTLR start "enumConstants"
	// src/main/resources/parser/Java.g:457:1: enumConstants : enumConstant ( ',' enumConstant )* ;
	public final void enumConstants() throws RecognitionException {
		int enumConstants_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 16) ) { return; }

			// src/main/resources/parser/Java.g:458:5: ( enumConstant ( ',' enumConstant )* )
			// src/main/resources/parser/Java.g:458:9: enumConstant ( ',' enumConstant )*
			{
			pushFollow(FOLLOW_enumConstant_in_enumConstants1371);
			enumConstant();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:459:9: ( ',' enumConstant )*
			loop26:
			while (true) {
				int alt26=2;
				int LA26_0 = input.LA(1);
				if ( (LA26_0==COMMA) ) {
					int LA26_1 = input.LA(2);
					if ( (LA26_1==IDENTIFIER||LA26_1==MONKEYS_AT) ) {
						alt26=1;
					}

				}

				switch (alt26) {
				case 1 :
					// src/main/resources/parser/Java.g:459:10: ',' enumConstant
					{
					match(input,COMMA,FOLLOW_COMMA_in_enumConstants1382); if (state.failed) return;
					pushFollow(FOLLOW_enumConstant_in_enumConstants1384);
					enumConstant();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop26;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 16, enumConstants_StartIndex); }

		}
	}
	// $ANTLR end "enumConstants"



	// $ANTLR start "enumConstant"
	// src/main/resources/parser/Java.g:467:1: enumConstant : ( annotations )? IDENTIFIER ( arguments )? ( classBody )? ;
	public final void enumConstant() throws RecognitionException {
		int enumConstant_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 17) ) { return; }

			// src/main/resources/parser/Java.g:468:5: ( ( annotations )? IDENTIFIER ( arguments )? ( classBody )? )
			// src/main/resources/parser/Java.g:468:9: ( annotations )? IDENTIFIER ( arguments )? ( classBody )?
			{
			// src/main/resources/parser/Java.g:468:9: ( annotations )?
			int alt27=2;
			int LA27_0 = input.LA(1);
			if ( (LA27_0==MONKEYS_AT) ) {
				alt27=1;
			}
			switch (alt27) {
				case 1 :
					// src/main/resources/parser/Java.g:468:10: annotations
					{
					pushFollow(FOLLOW_annotations_in_enumConstant1418);
					annotations();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_enumConstant1439); if (state.failed) return;
			// src/main/resources/parser/Java.g:471:9: ( arguments )?
			int alt28=2;
			int LA28_0 = input.LA(1);
			if ( (LA28_0==LPAREN) ) {
				alt28=1;
			}
			switch (alt28) {
				case 1 :
					// src/main/resources/parser/Java.g:471:10: arguments
					{
					pushFollow(FOLLOW_arguments_in_enumConstant1450);
					arguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:473:9: ( classBody )?
			int alt29=2;
			int LA29_0 = input.LA(1);
			if ( (LA29_0==LBRACE) ) {
				alt29=1;
			}
			switch (alt29) {
				case 1 :
					// src/main/resources/parser/Java.g:473:10: classBody
					{
					pushFollow(FOLLOW_classBody_in_enumConstant1472);
					classBody();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 17, enumConstant_StartIndex); }

		}
	}
	// $ANTLR end "enumConstant"



	// $ANTLR start "enumBodyDeclarations"
	// src/main/resources/parser/Java.g:479:1: enumBodyDeclarations : ';' ( classBodyDeclaration )* ;
	public final void enumBodyDeclarations() throws RecognitionException {
		int enumBodyDeclarations_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 18) ) { return; }

			// src/main/resources/parser/Java.g:480:5: ( ';' ( classBodyDeclaration )* )
			// src/main/resources/parser/Java.g:480:9: ';' ( classBodyDeclaration )*
			{
			match(input,SEMI,FOLLOW_SEMI_in_enumBodyDeclarations1513); if (state.failed) return;
			// src/main/resources/parser/Java.g:481:9: ( classBodyDeclaration )*
			loop30:
			while (true) {
				int alt30=2;
				int LA30_0 = input.LA(1);
				if ( (LA30_0==ABSTRACT||LA30_0==BOOLEAN||LA30_0==BYTE||LA30_0==CHAR||LA30_0==CLASS||LA30_0==DOUBLE||LA30_0==ENUM||LA30_0==FINAL||LA30_0==FLOAT||LA30_0==IDENTIFIER||(LA30_0 >= INT && LA30_0 <= INTERFACE)||LA30_0==LBRACE||LA30_0==LONG||LA30_0==LT||(LA30_0 >= MONKEYS_AT && LA30_0 <= NATIVE)||(LA30_0 >= PRIVATE && LA30_0 <= PUBLIC)||(LA30_0 >= SEMI && LA30_0 <= SHORT)||(LA30_0 >= STATIC && LA30_0 <= STRICTFP)||LA30_0==SYNCHRONIZED||LA30_0==TRANSIENT||(LA30_0 >= VOID && LA30_0 <= VOLATILE)) ) {
					alt30=1;
				}

				switch (alt30) {
				case 1 :
					// src/main/resources/parser/Java.g:481:10: classBodyDeclaration
					{
					pushFollow(FOLLOW_classBodyDeclaration_in_enumBodyDeclarations1525);
					classBodyDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop30;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 18, enumBodyDeclarations_StartIndex); }

		}
	}
	// $ANTLR end "enumBodyDeclarations"



	// $ANTLR start "interfaceDeclaration"
	// src/main/resources/parser/Java.g:485:1: interfaceDeclaration : ( normalInterfaceDeclaration | annotationTypeDeclaration );
	public final void interfaceDeclaration() throws RecognitionException {
		int interfaceDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 19) ) { return; }

			// src/main/resources/parser/Java.g:486:5: ( normalInterfaceDeclaration | annotationTypeDeclaration )
			int alt31=2;
			switch ( input.LA(1) ) {
			case MONKEYS_AT:
				{
				int LA31_1 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case PUBLIC:
				{
				int LA31_2 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case PROTECTED:
				{
				int LA31_3 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case PRIVATE:
				{
				int LA31_4 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case STATIC:
				{
				int LA31_5 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case ABSTRACT:
				{
				int LA31_6 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case FINAL:
				{
				int LA31_7 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case NATIVE:
				{
				int LA31_8 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case SYNCHRONIZED:
				{
				int LA31_9 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case TRANSIENT:
				{
				int LA31_10 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case VOLATILE:
				{
				int LA31_11 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case STRICTFP:
				{
				int LA31_12 = input.LA(2);
				if ( (synpred43_Java()) ) {
					alt31=1;
				}
				else if ( (true) ) {
					alt31=2;
				}

				}
				break;
			case INTERFACE:
				{
				alt31=1;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 31, 0, input);
				throw nvae;
			}
			switch (alt31) {
				case 1 :
					// src/main/resources/parser/Java.g:486:9: normalInterfaceDeclaration
					{
					pushFollow(FOLLOW_normalInterfaceDeclaration_in_interfaceDeclaration1556);
					normalInterfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:487:9: annotationTypeDeclaration
					{
					pushFollow(FOLLOW_annotationTypeDeclaration_in_interfaceDeclaration1566);
					annotationTypeDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 19, interfaceDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "interfaceDeclaration"



	// $ANTLR start "normalInterfaceDeclaration"
	// src/main/resources/parser/Java.g:490:1: normalInterfaceDeclaration : modifiers 'interface' IDENTIFIER ( typeParameters )? ( 'extends' typeList )? interfaceBody ;
	public final void normalInterfaceDeclaration() throws RecognitionException {
		int normalInterfaceDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 20) ) { return; }

			// src/main/resources/parser/Java.g:491:5: ( modifiers 'interface' IDENTIFIER ( typeParameters )? ( 'extends' typeList )? interfaceBody )
			// src/main/resources/parser/Java.g:491:9: modifiers 'interface' IDENTIFIER ( typeParameters )? ( 'extends' typeList )? interfaceBody
			{
			pushFollow(FOLLOW_modifiers_in_normalInterfaceDeclaration1590);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			match(input,INTERFACE,FOLLOW_INTERFACE_in_normalInterfaceDeclaration1592); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_normalInterfaceDeclaration1594); if (state.failed) return;
			// src/main/resources/parser/Java.g:492:9: ( typeParameters )?
			int alt32=2;
			int LA32_0 = input.LA(1);
			if ( (LA32_0==LT) ) {
				alt32=1;
			}
			switch (alt32) {
				case 1 :
					// src/main/resources/parser/Java.g:492:10: typeParameters
					{
					pushFollow(FOLLOW_typeParameters_in_normalInterfaceDeclaration1605);
					typeParameters();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:494:9: ( 'extends' typeList )?
			int alt33=2;
			int LA33_0 = input.LA(1);
			if ( (LA33_0==EXTENDS) ) {
				alt33=1;
			}
			switch (alt33) {
				case 1 :
					// src/main/resources/parser/Java.g:494:10: 'extends' typeList
					{
					match(input,EXTENDS,FOLLOW_EXTENDS_in_normalInterfaceDeclaration1627); if (state.failed) return;
					pushFollow(FOLLOW_typeList_in_normalInterfaceDeclaration1629);
					typeList();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			pushFollow(FOLLOW_interfaceBody_in_normalInterfaceDeclaration1650);
			interfaceBody();
			state._fsp--;
			if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 20, normalInterfaceDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "normalInterfaceDeclaration"



	// $ANTLR start "typeList"
	// src/main/resources/parser/Java.g:499:1: typeList : type ( ',' type )* ;
	public final void typeList() throws RecognitionException {
		int typeList_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 21) ) { return; }

			// src/main/resources/parser/Java.g:500:5: ( type ( ',' type )* )
			// src/main/resources/parser/Java.g:500:9: type ( ',' type )*
			{
			pushFollow(FOLLOW_type_in_typeList1670);
			type();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:501:9: ( ',' type )*
			loop34:
			while (true) {
				int alt34=2;
				int LA34_0 = input.LA(1);
				if ( (LA34_0==COMMA) ) {
					alt34=1;
				}

				switch (alt34) {
				case 1 :
					// src/main/resources/parser/Java.g:501:10: ',' type
					{
					match(input,COMMA,FOLLOW_COMMA_in_typeList1681); if (state.failed) return;
					pushFollow(FOLLOW_type_in_typeList1683);
					type();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop34;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 21, typeList_StartIndex); }

		}
	}
	// $ANTLR end "typeList"



	// $ANTLR start "classBody"
	// src/main/resources/parser/Java.g:505:1: classBody : '{' ( classBodyDeclaration )* '}' ;
	public final void classBody() throws RecognitionException {
		int classBody_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 22) ) { return; }

			// src/main/resources/parser/Java.g:506:5: ( '{' ( classBodyDeclaration )* '}' )
			// src/main/resources/parser/Java.g:506:9: '{' ( classBodyDeclaration )* '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_classBody1714); if (state.failed) return;
			// src/main/resources/parser/Java.g:507:9: ( classBodyDeclaration )*
			loop35:
			while (true) {
				int alt35=2;
				int LA35_0 = input.LA(1);
				if ( (LA35_0==ABSTRACT||LA35_0==BOOLEAN||LA35_0==BYTE||LA35_0==CHAR||LA35_0==CLASS||LA35_0==DOUBLE||LA35_0==ENUM||LA35_0==FINAL||LA35_0==FLOAT||LA35_0==IDENTIFIER||(LA35_0 >= INT && LA35_0 <= INTERFACE)||LA35_0==LBRACE||LA35_0==LONG||LA35_0==LT||(LA35_0 >= MONKEYS_AT && LA35_0 <= NATIVE)||(LA35_0 >= PRIVATE && LA35_0 <= PUBLIC)||(LA35_0 >= SEMI && LA35_0 <= SHORT)||(LA35_0 >= STATIC && LA35_0 <= STRICTFP)||LA35_0==SYNCHRONIZED||LA35_0==TRANSIENT||(LA35_0 >= VOID && LA35_0 <= VOLATILE)) ) {
					alt35=1;
				}

				switch (alt35) {
				case 1 :
					// src/main/resources/parser/Java.g:507:10: classBodyDeclaration
					{
					pushFollow(FOLLOW_classBodyDeclaration_in_classBody1726);
					classBodyDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop35;
				}
			}

			match(input,RBRACE,FOLLOW_RBRACE_in_classBody1748); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 22, classBody_StartIndex); }

		}
	}
	// $ANTLR end "classBody"



	// $ANTLR start "interfaceBody"
	// src/main/resources/parser/Java.g:512:1: interfaceBody : '{' ( interfaceBodyDeclaration )* '}' ;
	public final void interfaceBody() throws RecognitionException {
		int interfaceBody_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 23) ) { return; }

			// src/main/resources/parser/Java.g:513:5: ( '{' ( interfaceBodyDeclaration )* '}' )
			// src/main/resources/parser/Java.g:513:9: '{' ( interfaceBodyDeclaration )* '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_interfaceBody1768); if (state.failed) return;
			// src/main/resources/parser/Java.g:514:9: ( interfaceBodyDeclaration )*
			loop36:
			while (true) {
				int alt36=2;
				int LA36_0 = input.LA(1);
				if ( (LA36_0==ABSTRACT||LA36_0==BOOLEAN||LA36_0==BYTE||LA36_0==CHAR||LA36_0==CLASS||LA36_0==DOUBLE||LA36_0==ENUM||LA36_0==FINAL||LA36_0==FLOAT||LA36_0==IDENTIFIER||(LA36_0 >= INT && LA36_0 <= INTERFACE)||LA36_0==LONG||LA36_0==LT||(LA36_0 >= MONKEYS_AT && LA36_0 <= NATIVE)||(LA36_0 >= PRIVATE && LA36_0 <= PUBLIC)||(LA36_0 >= SEMI && LA36_0 <= SHORT)||(LA36_0 >= STATIC && LA36_0 <= STRICTFP)||LA36_0==SYNCHRONIZED||LA36_0==TRANSIENT||(LA36_0 >= VOID && LA36_0 <= VOLATILE)) ) {
					alt36=1;
				}

				switch (alt36) {
				case 1 :
					// src/main/resources/parser/Java.g:514:10: interfaceBodyDeclaration
					{
					pushFollow(FOLLOW_interfaceBodyDeclaration_in_interfaceBody1780);
					interfaceBodyDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop36;
				}
			}

			match(input,RBRACE,FOLLOW_RBRACE_in_interfaceBody1802); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 23, interfaceBody_StartIndex); }

		}
	}
	// $ANTLR end "interfaceBody"



	// $ANTLR start "classBodyDeclaration"
	// src/main/resources/parser/Java.g:519:1: classBodyDeclaration : ( ';' | ( 'static' )? block | memberDecl );
	public final void classBodyDeclaration() throws RecognitionException {
		int classBodyDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 24) ) { return; }

			// src/main/resources/parser/Java.g:520:5: ( ';' | ( 'static' )? block | memberDecl )
			int alt38=3;
			switch ( input.LA(1) ) {
			case SEMI:
				{
				alt38=1;
				}
				break;
			case STATIC:
				{
				int LA38_2 = input.LA(2);
				if ( (LA38_2==LBRACE) ) {
					alt38=2;
				}
				else if ( (LA38_2==ABSTRACT||LA38_2==BOOLEAN||LA38_2==BYTE||LA38_2==CHAR||LA38_2==CLASS||LA38_2==DOUBLE||LA38_2==ENUM||LA38_2==FINAL||LA38_2==FLOAT||LA38_2==IDENTIFIER||(LA38_2 >= INT && LA38_2 <= INTERFACE)||LA38_2==LONG||LA38_2==LT||(LA38_2 >= MONKEYS_AT && LA38_2 <= NATIVE)||(LA38_2 >= PRIVATE && LA38_2 <= PUBLIC)||LA38_2==SHORT||(LA38_2 >= STATIC && LA38_2 <= STRICTFP)||LA38_2==SYNCHRONIZED||LA38_2==TRANSIENT||(LA38_2 >= VOID && LA38_2 <= VOLATILE)) ) {
					alt38=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 38, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LBRACE:
				{
				alt38=2;
				}
				break;
			case ABSTRACT:
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case CLASS:
			case DOUBLE:
			case ENUM:
			case FINAL:
			case FLOAT:
			case IDENTIFIER:
			case INT:
			case INTERFACE:
			case LONG:
			case LT:
			case MONKEYS_AT:
			case NATIVE:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case SHORT:
			case STRICTFP:
			case SYNCHRONIZED:
			case TRANSIENT:
			case VOID:
			case VOLATILE:
				{
				alt38=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 38, 0, input);
				throw nvae;
			}
			switch (alt38) {
				case 1 :
					// src/main/resources/parser/Java.g:520:9: ';'
					{
					match(input,SEMI,FOLLOW_SEMI_in_classBodyDeclaration1822); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:521:9: ( 'static' )? block
					{
					// src/main/resources/parser/Java.g:521:9: ( 'static' )?
					int alt37=2;
					int LA37_0 = input.LA(1);
					if ( (LA37_0==STATIC) ) {
						alt37=1;
					}
					switch (alt37) {
						case 1 :
							// src/main/resources/parser/Java.g:521:10: 'static'
							{
							match(input,STATIC,FOLLOW_STATIC_in_classBodyDeclaration1833); if (state.failed) return;
							}
							break;

					}

					pushFollow(FOLLOW_block_in_classBodyDeclaration1855);
					block();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:524:9: memberDecl
					{
					pushFollow(FOLLOW_memberDecl_in_classBodyDeclaration1865);
					memberDecl();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 24, classBodyDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "classBodyDeclaration"



	// $ANTLR start "memberDecl"
	// src/main/resources/parser/Java.g:527:1: memberDecl : ( fieldDeclaration | methodDeclaration | classDeclaration | interfaceDeclaration );
	public final void memberDecl() throws RecognitionException {
		int memberDecl_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 25) ) { return; }

			// src/main/resources/parser/Java.g:528:5: ( fieldDeclaration | methodDeclaration | classDeclaration | interfaceDeclaration )
			int alt39=4;
			switch ( input.LA(1) ) {
			case MONKEYS_AT:
				{
				int LA39_1 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case PUBLIC:
				{
				int LA39_2 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case PROTECTED:
				{
				int LA39_3 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case PRIVATE:
				{
				int LA39_4 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case STATIC:
				{
				int LA39_5 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case ABSTRACT:
				{
				int LA39_6 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case FINAL:
				{
				int LA39_7 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case NATIVE:
				{
				int LA39_8 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case SYNCHRONIZED:
				{
				int LA39_9 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case TRANSIENT:
				{
				int LA39_10 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case VOLATILE:
				{
				int LA39_11 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case STRICTFP:
				{
				int LA39_12 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}
				else if ( (synpred54_Java()) ) {
					alt39=3;
				}
				else if ( (true) ) {
					alt39=4;
				}

				}
				break;
			case IDENTIFIER:
				{
				int LA39_13 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 39, 13, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				{
				int LA39_14 = input.LA(2);
				if ( (synpred52_Java()) ) {
					alt39=1;
				}
				else if ( (synpred53_Java()) ) {
					alt39=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 39, 14, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LT:
			case VOID:
				{
				alt39=2;
				}
				break;
			case CLASS:
			case ENUM:
				{
				alt39=3;
				}
				break;
			case INTERFACE:
				{
				alt39=4;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 39, 0, input);
				throw nvae;
			}
			switch (alt39) {
				case 1 :
					// src/main/resources/parser/Java.g:528:10: fieldDeclaration
					{
					pushFollow(FOLLOW_fieldDeclaration_in_memberDecl1885);
					fieldDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:529:10: methodDeclaration
					{
					pushFollow(FOLLOW_methodDeclaration_in_memberDecl1896);
					methodDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:530:10: classDeclaration
					{
					pushFollow(FOLLOW_classDeclaration_in_memberDecl1907);
					classDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:531:10: interfaceDeclaration
					{
					pushFollow(FOLLOW_interfaceDeclaration_in_memberDecl1918);
					interfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 25, memberDecl_StartIndex); }

		}
	}
	// $ANTLR end "memberDecl"


	public static class methodDeclaration_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "methodDeclaration"
	// src/main/resources/parser/Java.g:535:1: methodDeclaration : ( modifiers ( typeParameters )? IDENTIFIER formalParameters ( 'throws' qualifiedNameList )? '{' ( explicitConstructorInvocation )? ( blockStatement )* '}' | modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ( block | ';' ) );
	public final JavaParser.methodDeclaration_return methodDeclaration() throws RecognitionException {
		JavaParser.methodDeclaration_return retval = new JavaParser.methodDeclaration_return();
		retval.start = input.LT(1);
		int methodDeclaration_StartIndex = input.index();

		Token IDENTIFIER1=null;


		        MethodDeclarationDesc method = null;
		        if (!isBacktracking()) {
		            log("Start method declaration.");
		            method = new MethodDeclarationDesc(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		            context.push(method);
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 26) ) { return retval; }

			// src/main/resources/parser/Java.g:553:5: ( modifiers ( typeParameters )? IDENTIFIER formalParameters ( 'throws' qualifiedNameList )? '{' ( explicitConstructorInvocation )? ( blockStatement )* '}' | modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ( block | ';' ) )
			int alt49=2;
			switch ( input.LA(1) ) {
			case MONKEYS_AT:
				{
				int LA49_1 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case PUBLIC:
				{
				int LA49_2 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case PROTECTED:
				{
				int LA49_3 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case PRIVATE:
				{
				int LA49_4 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case STATIC:
				{
				int LA49_5 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case ABSTRACT:
				{
				int LA49_6 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case FINAL:
				{
				int LA49_7 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case NATIVE:
				{
				int LA49_8 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case SYNCHRONIZED:
				{
				int LA49_9 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case TRANSIENT:
				{
				int LA49_10 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case VOLATILE:
				{
				int LA49_11 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case STRICTFP:
				{
				int LA49_12 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case LT:
				{
				int LA49_13 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case IDENTIFIER:
				{
				int LA49_14 = input.LA(2);
				if ( (synpred59_Java()) ) {
					alt49=1;
				}
				else if ( (true) ) {
					alt49=2;
				}

				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
			case VOID:
				{
				alt49=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 49, 0, input);
				throw nvae;
			}
			switch (alt49) {
				case 1 :
					// src/main/resources/parser/Java.g:555:10: modifiers ( typeParameters )? IDENTIFIER formalParameters ( 'throws' qualifiedNameList )? '{' ( explicitConstructorInvocation )? ( blockStatement )* '}'
					{
					pushFollow(FOLLOW_modifiers_in_methodDeclaration1973);
					modifiers();
					state._fsp--;
					if (state.failed) return retval;
					// src/main/resources/parser/Java.g:556:9: ( typeParameters )?
					int alt40=2;
					int LA40_0 = input.LA(1);
					if ( (LA40_0==LT) ) {
						alt40=1;
					}
					switch (alt40) {
						case 1 :
							// src/main/resources/parser/Java.g:556:10: typeParameters
							{
							pushFollow(FOLLOW_typeParameters_in_methodDeclaration1984);
							typeParameters();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_methodDeclaration2005); if (state.failed) return retval;
					pushFollow(FOLLOW_formalParameters_in_methodDeclaration2015);
					formalParameters();
					state._fsp--;
					if (state.failed) return retval;
					// src/main/resources/parser/Java.g:560:9: ( 'throws' qualifiedNameList )?
					int alt41=2;
					int LA41_0 = input.LA(1);
					if ( (LA41_0==THROWS) ) {
						alt41=1;
					}
					switch (alt41) {
						case 1 :
							// src/main/resources/parser/Java.g:560:10: 'throws' qualifiedNameList
							{
							match(input,THROWS,FOLLOW_THROWS_in_methodDeclaration2026); if (state.failed) return retval;
							pushFollow(FOLLOW_qualifiedNameList_in_methodDeclaration2028);
							qualifiedNameList();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					match(input,LBRACE,FOLLOW_LBRACE_in_methodDeclaration2049); if (state.failed) return retval;
					// src/main/resources/parser/Java.g:563:9: ( explicitConstructorInvocation )?
					int alt42=2;
					switch ( input.LA(1) ) {
						case LT:
							{
							alt42=1;
							}
							break;
						case THIS:
							{
							int LA42_2 = input.LA(2);
							if ( (synpred57_Java()) ) {
								alt42=1;
							}
							}
							break;
						case LPAREN:
							{
							int LA42_3 = input.LA(2);
							if ( (synpred57_Java()) ) {
								alt42=1;
							}
							}
							break;
						case SUPER:
							{
							int LA42_4 = input.LA(2);
							if ( (synpred57_Java()) ) {
								alt42=1;
							}
							}
							break;
						case IDENTIFIER:
							{
							int LA42_5 = input.LA(2);
							if ( (synpred57_Java()) ) {
								alt42=1;
							}
							}
							break;
						case CHARLITERAL:
						case DOUBLELITERAL:
						case FALSE:
						case FLOATLITERAL:
						case INTLITERAL:
						case LONGLITERAL:
						case NULL:
						case STRINGLITERAL:
						case TRUE:
							{
							int LA42_6 = input.LA(2);
							if ( (synpred57_Java()) ) {
								alt42=1;
							}
							}
							break;
						case NEW:
							{
							int LA42_7 = input.LA(2);
							if ( (synpred57_Java()) ) {
								alt42=1;
							}
							}
							break;
						case BOOLEAN:
						case BYTE:
						case CHAR:
						case DOUBLE:
						case FLOAT:
						case INT:
						case LONG:
						case SHORT:
							{
							int LA42_8 = input.LA(2);
							if ( (synpred57_Java()) ) {
								alt42=1;
							}
							}
							break;
						case VOID:
							{
							int LA42_9 = input.LA(2);
							if ( (synpred57_Java()) ) {
								alt42=1;
							}
							}
							break;
					}
					switch (alt42) {
						case 1 :
							// src/main/resources/parser/Java.g:563:10: explicitConstructorInvocation
							{
							pushFollow(FOLLOW_explicitConstructorInvocation_in_methodDeclaration2061);
							explicitConstructorInvocation();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					// src/main/resources/parser/Java.g:565:9: ( blockStatement )*
					loop43:
					while (true) {
						int alt43=2;
						int LA43_0 = input.LA(1);
						if ( (LA43_0==ABSTRACT||(LA43_0 >= ASSERT && LA43_0 <= BANG)||(LA43_0 >= BOOLEAN && LA43_0 <= BYTE)||(LA43_0 >= CHAR && LA43_0 <= CLASS)||LA43_0==CONTINUE||LA43_0==DO||(LA43_0 >= DOUBLE && LA43_0 <= DOUBLELITERAL)||LA43_0==ENUM||(LA43_0 >= FALSE && LA43_0 <= FINAL)||(LA43_0 >= FLOAT && LA43_0 <= FOR)||(LA43_0 >= IDENTIFIER && LA43_0 <= IF)||(LA43_0 >= INT && LA43_0 <= INTLITERAL)||LA43_0==LBRACE||(LA43_0 >= LONG && LA43_0 <= LT)||(LA43_0 >= MONKEYS_AT && LA43_0 <= NULL)||LA43_0==PLUS||(LA43_0 >= PLUSPLUS && LA43_0 <= PUBLIC)||LA43_0==RETURN||(LA43_0 >= SEMI && LA43_0 <= SHORT)||(LA43_0 >= STATIC && LA43_0 <= SUB)||(LA43_0 >= SUBSUB && LA43_0 <= SYNCHRONIZED)||(LA43_0 >= THIS && LA43_0 <= THROW)||(LA43_0 >= TILDE && LA43_0 <= WHILE)) ) {
							alt43=1;
						}

						switch (alt43) {
						case 1 :
							// src/main/resources/parser/Java.g:565:10: blockStatement
							{
							pushFollow(FOLLOW_blockStatement_in_methodDeclaration2083);
							blockStatement();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

						default :
							break loop43;
						}
					}

					match(input,RBRACE,FOLLOW_RBRACE_in_methodDeclaration2104); if (state.failed) return retval;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:568:9: modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ( block | ';' )
					{
					pushFollow(FOLLOW_modifiers_in_methodDeclaration2114);
					modifiers();
					state._fsp--;
					if (state.failed) return retval;
					// src/main/resources/parser/Java.g:569:9: ( typeParameters )?
					int alt44=2;
					int LA44_0 = input.LA(1);
					if ( (LA44_0==LT) ) {
						alt44=1;
					}
					switch (alt44) {
						case 1 :
							// src/main/resources/parser/Java.g:569:10: typeParameters
							{
							pushFollow(FOLLOW_typeParameters_in_methodDeclaration2125);
							typeParameters();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					// src/main/resources/parser/Java.g:571:9: ( type | 'void' )
					int alt45=2;
					int LA45_0 = input.LA(1);
					if ( (LA45_0==BOOLEAN||LA45_0==BYTE||LA45_0==CHAR||LA45_0==DOUBLE||LA45_0==FLOAT||LA45_0==IDENTIFIER||LA45_0==INT||LA45_0==LONG||LA45_0==SHORT) ) {
						alt45=1;
					}
					else if ( (LA45_0==VOID) ) {
						alt45=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 45, 0, input);
						throw nvae;
					}

					switch (alt45) {
						case 1 :
							// src/main/resources/parser/Java.g:571:10: type
							{
							pushFollow(FOLLOW_type_in_methodDeclaration2147);
							type();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;
						case 2 :
							// src/main/resources/parser/Java.g:572:13: 'void'
							{
							match(input,VOID,FOLLOW_VOID_in_methodDeclaration2161); if (state.failed) return retval;
							}
							break;

					}

					IDENTIFIER1=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_methodDeclaration2181); if (state.failed) return retval;
					if ( state.backtracking==0 ) { method.setName((IDENTIFIER1!=null?IDENTIFIER1.getText():null)); }
					pushFollow(FOLLOW_formalParameters_in_methodDeclaration2193);
					formalParameters();
					state._fsp--;
					if (state.failed) return retval;
					// src/main/resources/parser/Java.g:576:9: ( '[' ']' )*
					loop46:
					while (true) {
						int alt46=2;
						int LA46_0 = input.LA(1);
						if ( (LA46_0==LBRACKET) ) {
							alt46=1;
						}

						switch (alt46) {
						case 1 :
							// src/main/resources/parser/Java.g:576:10: '[' ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_methodDeclaration2204); if (state.failed) return retval;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_methodDeclaration2206); if (state.failed) return retval;
							if ( state.backtracking==0 ) { method.addDimension(); }
							}
							break;

						default :
							break loop46;
						}
					}

					// src/main/resources/parser/Java.g:578:9: ( 'throws' qualifiedNameList )?
					int alt47=2;
					int LA47_0 = input.LA(1);
					if ( (LA47_0==THROWS) ) {
						alt47=1;
					}
					switch (alt47) {
						case 1 :
							// src/main/resources/parser/Java.g:578:10: 'throws' qualifiedNameList
							{
							match(input,THROWS,FOLLOW_THROWS_in_methodDeclaration2232); if (state.failed) return retval;
							pushFollow(FOLLOW_qualifiedNameList_in_methodDeclaration2234);
							qualifiedNameList();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					// src/main/resources/parser/Java.g:580:9: ( block | ';' )
					int alt48=2;
					int LA48_0 = input.LA(1);
					if ( (LA48_0==LBRACE) ) {
						alt48=1;
					}
					else if ( (LA48_0==SEMI) ) {
						alt48=2;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return retval;}
						NoViableAltException nvae =
							new NoViableAltException("", 48, 0, input);
						throw nvae;
					}

					switch (alt48) {
						case 1 :
							// src/main/resources/parser/Java.g:581:13: block
							{
							pushFollow(FOLLOW_block_in_methodDeclaration2289);
							block();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;
						case 2 :
							// src/main/resources/parser/Java.g:582:13: ';'
							{
							match(input,SEMI,FOLLOW_SEMI_in_methodDeclaration2303); if (state.failed) return retval;
							}
							break;

					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        method = popMethod();
			        if (method != null) {
			            method.setText(input.toString(retval.start,input.LT(-1)));
			            method.setStop(stop((CommonToken)(retval.stop)));
			            methods.add(method);
			            log("End of method declaration. : " + method.getName());
			        }
			    }
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 26, methodDeclaration_StartIndex); }

		}
		return retval;
	}
	// $ANTLR end "methodDeclaration"


	public static class fieldDeclaration_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "fieldDeclaration"
	// src/main/resources/parser/Java.g:587:1: fieldDeclaration : modifiers type v1= variableDeclarator ( ',' v2= variableDeclarator )* ';' ;
	public final JavaParser.fieldDeclaration_return fieldDeclaration() throws RecognitionException {
		JavaParser.fieldDeclaration_return retval = new JavaParser.fieldDeclaration_return();
		retval.start = input.LT(1);
		int fieldDeclaration_StartIndex = input.index();

		ParserRuleReturnScope v1 =null;
		ParserRuleReturnScope v2 =null;


		        FieldDeclarationDesc field = null;
		        if (!isBacktracking()) {
		            log("Start field declaration.");
		            field = new FieldDeclarationDesc(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		            context.push(field);
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 27) ) { return retval; }

			// src/main/resources/parser/Java.g:608:5: ( modifiers type v1= variableDeclarator ( ',' v2= variableDeclarator )* ';' )
			// src/main/resources/parser/Java.g:608:9: modifiers type v1= variableDeclarator ( ',' v2= variableDeclarator )* ';'
			{
			pushFollow(FOLLOW_modifiers_in_fieldDeclaration2352);
			modifiers();
			state._fsp--;
			if (state.failed) return retval;
			pushFollow(FOLLOW_type_in_fieldDeclaration2362);
			type();
			state._fsp--;
			if (state.failed) return retval;
			pushFollow(FOLLOW_variableDeclarator_in_fieldDeclaration2374);
			v1=variableDeclarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) { if (field != null) field.addVariableDeclaration((v1!=null?((JavaParser.variableDeclarator_return)v1).varDec:null)); }
			// src/main/resources/parser/Java.g:611:9: ( ',' v2= variableDeclarator )*
			loop50:
			while (true) {
				int alt50=2;
				int LA50_0 = input.LA(1);
				if ( (LA50_0==COMMA) ) {
					alt50=1;
				}

				switch (alt50) {
				case 1 :
					// src/main/resources/parser/Java.g:611:10: ',' v2= variableDeclarator
					{
					match(input,COMMA,FOLLOW_COMMA_in_fieldDeclaration2393); if (state.failed) return retval;
					pushFollow(FOLLOW_variableDeclarator_in_fieldDeclaration2397);
					v2=variableDeclarator();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) { if (field != null) field.addVariableDeclaration((v2!=null?((JavaParser.variableDeclarator_return)v2).varDec:null)); }
					}
					break;

				default :
					break loop50;
				}
			}

			match(input,SEMI,FOLLOW_SEMI_in_fieldDeclaration2421); if (state.failed) return retval;
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        //this can also be done in the finally block for the rule.
			        field = popField();
			        if (field != null) {
			            field.setText(input.toString(retval.start,input.LT(-1)));
			            field.setStop(stop((CommonToken)(retval.stop)));
			            fields.add(field);
			            log("End of field declaration.");
			        } else {
			            log("A FieldDeclarationDesc is expected");
			        }
			    }
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 27, fieldDeclaration_StartIndex); }

		}
		return retval;
	}
	// $ANTLR end "fieldDeclaration"


	public static class variableDeclarator_return extends ParserRuleReturnScope {
		public VariableDeclarationDesc varDec;
	};


	// $ANTLR start "variableDeclarator"
	// src/main/resources/parser/Java.g:616:1: variableDeclarator returns [ VariableDeclarationDesc varDec ] : i= IDENTIFIER ( '[' ']' )* ( '=' v= variableInitializer )? ;
	public final JavaParser.variableDeclarator_return variableDeclarator() throws RecognitionException {
		JavaParser.variableDeclarator_return retval = new JavaParser.variableDeclarator_return();
		retval.start = input.LT(1);
		int variableDeclarator_StartIndex = input.index();

		Token i=null;
		ParserRuleReturnScope v =null;


		        retval.varDec = new VariableDeclarationDesc(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 28) ) { return retval; }

			// src/main/resources/parser/Java.g:625:5: (i= IDENTIFIER ( '[' ']' )* ( '=' v= variableInitializer )? )
			// src/main/resources/parser/Java.g:625:9: i= IDENTIFIER ( '[' ']' )* ( '=' v= variableInitializer )?
			{
			i=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_variableDeclarator2464); if (state.failed) return retval;
			if ( state.backtracking==0 ) { retval.varDec.setIdentifier((i!=null?i.getText():null)); }
			// src/main/resources/parser/Java.g:626:9: ( '[' ']' )*
			loop51:
			while (true) {
				int alt51=2;
				int LA51_0 = input.LA(1);
				if ( (LA51_0==LBRACKET) ) {
					alt51=1;
				}

				switch (alt51) {
				case 1 :
					// src/main/resources/parser/Java.g:626:10: '[' ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_variableDeclarator2477); if (state.failed) return retval;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_variableDeclarator2479); if (state.failed) return retval;
					if ( state.backtracking==0 ) { retval.varDec.addDimension(); }
					}
					break;

				default :
					break loop51;
				}
			}

			// src/main/resources/parser/Java.g:628:9: ( '=' v= variableInitializer )?
			int alt52=2;
			int LA52_0 = input.LA(1);
			if ( (LA52_0==EQ) ) {
				alt52=1;
			}
			switch (alt52) {
				case 1 :
					// src/main/resources/parser/Java.g:628:10: '=' v= variableInitializer
					{
					match(input,EQ,FOLLOW_EQ_in_variableDeclarator2503); if (state.failed) return retval;
					pushFollow(FOLLOW_variableInitializer_in_variableDeclarator2507);
					v=variableInitializer();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) { retval.varDec.setVariableInitializer(new VariableInitializerDesc( (v!=null?input.toString(v.start,v.stop):null), start(((CommonToken)(v!=null?(v.start):null))), stop((CommonToken)(v!=null?(v.stop):null)), line((CommonToken)(v!=null?(v.start):null)), position((CommonToken)(v!=null?(v.start):null)), (v!=null?input.toString(v.start,v.stop):null) ) ); }
					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        retval.varDec.setStop(stop((CommonToken)(retval.stop)));
			        //retval.varDec.setText(variableInitializer.text);
			        //retval.varDec.setInitializerExpr(variableInitializer.text);
			    }
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 28, variableDeclarator_StartIndex); }

		}
		return retval;
	}
	// $ANTLR end "variableDeclarator"



	// $ANTLR start "interfaceBodyDeclaration"
	// src/main/resources/parser/Java.g:635:1: interfaceBodyDeclaration : ( interfaceFieldDeclaration | interfaceMethodDeclaration | interfaceDeclaration | classDeclaration | ';' );
	public final void interfaceBodyDeclaration() throws RecognitionException {
		int interfaceBodyDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 29) ) { return; }

			// src/main/resources/parser/Java.g:636:5: ( interfaceFieldDeclaration | interfaceMethodDeclaration | interfaceDeclaration | classDeclaration | ';' )
			int alt53=5;
			switch ( input.LA(1) ) {
			case MONKEYS_AT:
				{
				int LA53_1 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case PUBLIC:
				{
				int LA53_2 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case PROTECTED:
				{
				int LA53_3 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case PRIVATE:
				{
				int LA53_4 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case STATIC:
				{
				int LA53_5 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case ABSTRACT:
				{
				int LA53_6 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 6, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FINAL:
				{
				int LA53_7 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 7, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NATIVE:
				{
				int LA53_8 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 8, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case SYNCHRONIZED:
				{
				int LA53_9 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 9, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case TRANSIENT:
				{
				int LA53_10 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 10, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case VOLATILE:
				{
				int LA53_11 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 11, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case STRICTFP:
				{
				int LA53_12 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}
				else if ( (synpred70_Java()) ) {
					alt53=3;
				}
				else if ( (synpred71_Java()) ) {
					alt53=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 12, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case IDENTIFIER:
				{
				int LA53_13 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 13, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				{
				int LA53_14 = input.LA(2);
				if ( (synpred68_Java()) ) {
					alt53=1;
				}
				else if ( (synpred69_Java()) ) {
					alt53=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 53, 14, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LT:
			case VOID:
				{
				alt53=2;
				}
				break;
			case INTERFACE:
				{
				alt53=3;
				}
				break;
			case CLASS:
			case ENUM:
				{
				alt53=4;
				}
				break;
			case SEMI:
				{
				alt53=5;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 53, 0, input);
				throw nvae;
			}
			switch (alt53) {
				case 1 :
					// src/main/resources/parser/Java.g:637:9: interfaceFieldDeclaration
					{
					pushFollow(FOLLOW_interfaceFieldDeclaration_in_interfaceBodyDeclaration2548);
					interfaceFieldDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:638:9: interfaceMethodDeclaration
					{
					pushFollow(FOLLOW_interfaceMethodDeclaration_in_interfaceBodyDeclaration2558);
					interfaceMethodDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:639:9: interfaceDeclaration
					{
					pushFollow(FOLLOW_interfaceDeclaration_in_interfaceBodyDeclaration2568);
					interfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:640:9: classDeclaration
					{
					pushFollow(FOLLOW_classDeclaration_in_interfaceBodyDeclaration2578);
					classDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:641:9: ';'
					{
					match(input,SEMI,FOLLOW_SEMI_in_interfaceBodyDeclaration2588); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 29, interfaceBodyDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "interfaceBodyDeclaration"



	// $ANTLR start "interfaceMethodDeclaration"
	// src/main/resources/parser/Java.g:644:1: interfaceMethodDeclaration : modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';' ;
	public final void interfaceMethodDeclaration() throws RecognitionException {
		int interfaceMethodDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 30) ) { return; }

			// src/main/resources/parser/Java.g:645:5: ( modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';' )
			// src/main/resources/parser/Java.g:645:9: modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';'
			{
			pushFollow(FOLLOW_modifiers_in_interfaceMethodDeclaration2608);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:646:9: ( typeParameters )?
			int alt54=2;
			int LA54_0 = input.LA(1);
			if ( (LA54_0==LT) ) {
				alt54=1;
			}
			switch (alt54) {
				case 1 :
					// src/main/resources/parser/Java.g:646:10: typeParameters
					{
					pushFollow(FOLLOW_typeParameters_in_interfaceMethodDeclaration2619);
					typeParameters();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:648:9: ( type | 'void' )
			int alt55=2;
			int LA55_0 = input.LA(1);
			if ( (LA55_0==BOOLEAN||LA55_0==BYTE||LA55_0==CHAR||LA55_0==DOUBLE||LA55_0==FLOAT||LA55_0==IDENTIFIER||LA55_0==INT||LA55_0==LONG||LA55_0==SHORT) ) {
				alt55=1;
			}
			else if ( (LA55_0==VOID) ) {
				alt55=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 55, 0, input);
				throw nvae;
			}

			switch (alt55) {
				case 1 :
					// src/main/resources/parser/Java.g:648:10: type
					{
					pushFollow(FOLLOW_type_in_interfaceMethodDeclaration2641);
					type();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:649:10: 'void'
					{
					match(input,VOID,FOLLOW_VOID_in_interfaceMethodDeclaration2652); if (state.failed) return;
					}
					break;

			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_interfaceMethodDeclaration2672); if (state.failed) return;
			pushFollow(FOLLOW_formalParameters_in_interfaceMethodDeclaration2682);
			formalParameters();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:653:9: ( '[' ']' )*
			loop56:
			while (true) {
				int alt56=2;
				int LA56_0 = input.LA(1);
				if ( (LA56_0==LBRACKET) ) {
					alt56=1;
				}

				switch (alt56) {
				case 1 :
					// src/main/resources/parser/Java.g:653:10: '[' ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_interfaceMethodDeclaration2693); if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_interfaceMethodDeclaration2695); if (state.failed) return;
					}
					break;

				default :
					break loop56;
				}
			}

			// src/main/resources/parser/Java.g:655:9: ( 'throws' qualifiedNameList )?
			int alt57=2;
			int LA57_0 = input.LA(1);
			if ( (LA57_0==THROWS) ) {
				alt57=1;
			}
			switch (alt57) {
				case 1 :
					// src/main/resources/parser/Java.g:655:10: 'throws' qualifiedNameList
					{
					match(input,THROWS,FOLLOW_THROWS_in_interfaceMethodDeclaration2717); if (state.failed) return;
					pushFollow(FOLLOW_qualifiedNameList_in_interfaceMethodDeclaration2719);
					qualifiedNameList();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,SEMI,FOLLOW_SEMI_in_interfaceMethodDeclaration2732); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 30, interfaceMethodDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "interfaceMethodDeclaration"



	// $ANTLR start "interfaceFieldDeclaration"
	// src/main/resources/parser/Java.g:664:1: interfaceFieldDeclaration : modifiers type variableDeclarator ( ',' variableDeclarator )* ';' ;
	public final void interfaceFieldDeclaration() throws RecognitionException {
		int interfaceFieldDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 31) ) { return; }

			// src/main/resources/parser/Java.g:665:5: ( modifiers type variableDeclarator ( ',' variableDeclarator )* ';' )
			// src/main/resources/parser/Java.g:665:9: modifiers type variableDeclarator ( ',' variableDeclarator )* ';'
			{
			pushFollow(FOLLOW_modifiers_in_interfaceFieldDeclaration2754);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_interfaceFieldDeclaration2756);
			type();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_variableDeclarator_in_interfaceFieldDeclaration2758);
			variableDeclarator();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:666:9: ( ',' variableDeclarator )*
			loop58:
			while (true) {
				int alt58=2;
				int LA58_0 = input.LA(1);
				if ( (LA58_0==COMMA) ) {
					alt58=1;
				}

				switch (alt58) {
				case 1 :
					// src/main/resources/parser/Java.g:666:10: ',' variableDeclarator
					{
					match(input,COMMA,FOLLOW_COMMA_in_interfaceFieldDeclaration2769); if (state.failed) return;
					pushFollow(FOLLOW_variableDeclarator_in_interfaceFieldDeclaration2771);
					variableDeclarator();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop58;
				}
			}

			match(input,SEMI,FOLLOW_SEMI_in_interfaceFieldDeclaration2792); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 31, interfaceFieldDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "interfaceFieldDeclaration"


	protected static class type_scope {
		List<String> symbols;
	}
	protected Stack<type_scope> type_stack = new Stack<type_scope>();


	// $ANTLR start "type"
	// src/main/resources/parser/Java.g:672:1: type : ( classOrInterfaceType ( '[' ']' )* | primitiveType ( '[' ']' )* );
	public final void type() throws RecognitionException {
		type_stack.push(new type_scope());
		int type_StartIndex = input.index();


		        if (!isBacktracking()) {
		            type_stack.peek().symbols = new ArrayList<String>();
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 32) ) { return; }

			// src/main/resources/parser/Java.g:684:5: ( classOrInterfaceType ( '[' ']' )* | primitiveType ( '[' ']' )* )
			int alt61=2;
			int LA61_0 = input.LA(1);
			if ( (LA61_0==IDENTIFIER) ) {
				alt61=1;
			}
			else if ( (LA61_0==BOOLEAN||LA61_0==BYTE||LA61_0==CHAR||LA61_0==DOUBLE||LA61_0==FLOAT||LA61_0==INT||LA61_0==LONG||LA61_0==SHORT) ) {
				alt61=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 61, 0, input);
				throw nvae;
			}

			switch (alt61) {
				case 1 :
					// src/main/resources/parser/Java.g:684:9: classOrInterfaceType ( '[' ']' )*
					{
					pushFollow(FOLLOW_classOrInterfaceType_in_type2838);
					classOrInterfaceType();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:685:9: ( '[' ']' )*
					loop59:
					while (true) {
						int alt59=2;
						int LA59_0 = input.LA(1);
						if ( (LA59_0==LBRACKET) ) {
							alt59=1;
						}

						switch (alt59) {
						case 1 :
							// src/main/resources/parser/Java.g:685:10: '[' ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_type2849); if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_type2851); if (state.failed) return;
							if ( state.backtracking==0 ) { if (currentTypeDesc != null) currentTypeDesc.addDimension(); type_stack.peek().symbols.add("[]"); }
							}
							break;

						default :
							break loop59;
						}
					}

					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:687:9: primitiveType ( '[' ']' )*
					{
					pushFollow(FOLLOW_primitiveType_in_type2874);
					primitiveType();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:688:9: ( '[' ']' )*
					loop60:
					while (true) {
						int alt60=2;
						int LA60_0 = input.LA(1);
						if ( (LA60_0==LBRACKET) ) {
							alt60=1;
						}

						switch (alt60) {
						case 1 :
							// src/main/resources/parser/Java.g:688:10: '[' ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_type2885); if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_type2887); if (state.failed) return;
							if ( state.backtracking==0 ) { if (currentTypeDesc != null) currentTypeDesc.addDimension(); type_stack.peek().symbols.add("[]"); }
							}
							break;

						default :
							break loop60;
						}
					}

					}
					break;

			}
			if ( state.backtracking==0 ) {
			        currentTypeDesc = null;
			    }
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 32, type_StartIndex); }

			type_stack.pop();
		}
	}
	// $ANTLR end "type"


	public static class classOrInterfaceType_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "classOrInterfaceType"
	// src/main/resources/parser/Java.g:693:1: classOrInterfaceType : IDENTIFIER ( typeArguments )? ( '.' IDENTIFIER ( typeArguments )? )* ;
	public final JavaParser.classOrInterfaceType_return classOrInterfaceType() throws RecognitionException {
		JavaParser.classOrInterfaceType_return retval = new JavaParser.classOrInterfaceType_return();
		retval.start = input.LT(1);
		int classOrInterfaceType_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 33) ) { return retval; }

			// src/main/resources/parser/Java.g:700:5: ( IDENTIFIER ( typeArguments )? ( '.' IDENTIFIER ( typeArguments )? )* )
			// src/main/resources/parser/Java.g:700:9: IDENTIFIER ( typeArguments )? ( '.' IDENTIFIER ( typeArguments )? )*
			{
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_classOrInterfaceType2929); if (state.failed) return retval;
			// src/main/resources/parser/Java.g:701:9: ( typeArguments )?
			int alt62=2;
			int LA62_0 = input.LA(1);
			if ( (LA62_0==LT) ) {
				int LA62_1 = input.LA(2);
				if ( (LA62_1==BOOLEAN||LA62_1==BYTE||LA62_1==CHAR||LA62_1==DOUBLE||LA62_1==FLOAT||LA62_1==IDENTIFIER||LA62_1==INT||LA62_1==LONG||LA62_1==QUES||LA62_1==SHORT) ) {
					alt62=1;
				}
			}
			switch (alt62) {
				case 1 :
					// src/main/resources/parser/Java.g:701:10: typeArguments
					{
					pushFollow(FOLLOW_typeArguments_in_classOrInterfaceType2940);
					typeArguments();
					state._fsp--;
					if (state.failed) return retval;
					}
					break;

			}

			// src/main/resources/parser/Java.g:703:9: ( '.' IDENTIFIER ( typeArguments )? )*
			loop64:
			while (true) {
				int alt64=2;
				int LA64_0 = input.LA(1);
				if ( (LA64_0==DOT) ) {
					alt64=1;
				}

				switch (alt64) {
				case 1 :
					// src/main/resources/parser/Java.g:703:10: '.' IDENTIFIER ( typeArguments )?
					{
					match(input,DOT,FOLLOW_DOT_in_classOrInterfaceType2962); if (state.failed) return retval;
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_classOrInterfaceType2964); if (state.failed) return retval;
					// src/main/resources/parser/Java.g:704:13: ( typeArguments )?
					int alt63=2;
					int LA63_0 = input.LA(1);
					if ( (LA63_0==LT) ) {
						int LA63_1 = input.LA(2);
						if ( (LA63_1==BOOLEAN||LA63_1==BYTE||LA63_1==CHAR||LA63_1==DOUBLE||LA63_1==FLOAT||LA63_1==IDENTIFIER||LA63_1==INT||LA63_1==LONG||LA63_1==QUES||LA63_1==SHORT) ) {
							alt63=1;
						}
					}
					switch (alt63) {
						case 1 :
							// src/main/resources/parser/Java.g:704:14: typeArguments
							{
							pushFollow(FOLLOW_typeArguments_in_classOrInterfaceType2979);
							typeArguments();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					}
					break;

				default :
					break loop64;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        if ( isFieldOnTop() ) {
			            currentTypeDesc = new ClassTypeDesc(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), stop((CommonToken)(retval.stop)), line((CommonToken)(retval.start)), position((CommonToken)(retval.start)), input.toString(retval.start,input.LT(-1)));
			            ((FieldDeclarationDesc)context.peek()).setType(currentTypeDesc);
			        }
			    }
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 33, classOrInterfaceType_StartIndex); }

		}
		return retval;
	}
	// $ANTLR end "classOrInterfaceType"


	public static class primitiveType_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "primitiveType"
	// src/main/resources/parser/Java.g:709:1: primitiveType : ( 'boolean' | 'char' | 'byte' | 'short' | 'int' | 'long' | 'float' | 'double' );
	public final JavaParser.primitiveType_return primitiveType() throws RecognitionException {
		JavaParser.primitiveType_return retval = new JavaParser.primitiveType_return();
		retval.start = input.LT(1);
		int primitiveType_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 34) ) { return retval; }

			// src/main/resources/parser/Java.g:716:5: ( 'boolean' | 'char' | 'byte' | 'short' | 'int' | 'long' | 'float' | 'double' )
			// src/main/resources/parser/Java.g:
			{
			if ( input.LA(1)==BOOLEAN||input.LA(1)==BYTE||input.LA(1)==CHAR||input.LA(1)==DOUBLE||input.LA(1)==FLOAT||input.LA(1)==INT||input.LA(1)==LONG||input.LA(1)==SHORT ) {
				input.consume();
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        if ( isFieldOnTop() ) {
			            currentTypeDesc = new PrimitiveTypeDesc(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), stop((CommonToken)(retval.stop)), line((CommonToken)(retval.start)), position((CommonToken)(retval.start)), input.toString(retval.start,input.LT(-1)));
			            ((FieldDeclarationDesc)context.peek()).setType(currentTypeDesc);
			        }
			    }
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 34, primitiveType_StartIndex); }

		}
		return retval;
	}
	// $ANTLR end "primitiveType"



	// $ANTLR start "typeArguments"
	// src/main/resources/parser/Java.g:726:1: typeArguments : '<' typeArgument ( ',' typeArgument )* '>' ;
	public final void typeArguments() throws RecognitionException {
		int typeArguments_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 35) ) { return; }

			// src/main/resources/parser/Java.g:727:5: ( '<' typeArgument ( ',' typeArgument )* '>' )
			// src/main/resources/parser/Java.g:727:9: '<' typeArgument ( ',' typeArgument )* '>'
			{
			match(input,LT,FOLLOW_LT_in_typeArguments3123); if (state.failed) return;
			pushFollow(FOLLOW_typeArgument_in_typeArguments3125);
			typeArgument();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:728:9: ( ',' typeArgument )*
			loop65:
			while (true) {
				int alt65=2;
				int LA65_0 = input.LA(1);
				if ( (LA65_0==COMMA) ) {
					alt65=1;
				}

				switch (alt65) {
				case 1 :
					// src/main/resources/parser/Java.g:728:10: ',' typeArgument
					{
					match(input,COMMA,FOLLOW_COMMA_in_typeArguments3136); if (state.failed) return;
					pushFollow(FOLLOW_typeArgument_in_typeArguments3138);
					typeArgument();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop65;
				}
			}

			match(input,GT,FOLLOW_GT_in_typeArguments3160); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 35, typeArguments_StartIndex); }

		}
	}
	// $ANTLR end "typeArguments"



	// $ANTLR start "typeArgument"
	// src/main/resources/parser/Java.g:733:1: typeArgument : ( type | '?' ( ( 'extends' | 'super' ) type )? );
	public final void typeArgument() throws RecognitionException {
		int typeArgument_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 36) ) { return; }

			// src/main/resources/parser/Java.g:734:5: ( type | '?' ( ( 'extends' | 'super' ) type )? )
			int alt67=2;
			int LA67_0 = input.LA(1);
			if ( (LA67_0==BOOLEAN||LA67_0==BYTE||LA67_0==CHAR||LA67_0==DOUBLE||LA67_0==FLOAT||LA67_0==IDENTIFIER||LA67_0==INT||LA67_0==LONG||LA67_0==SHORT) ) {
				alt67=1;
			}
			else if ( (LA67_0==QUES) ) {
				alt67=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 67, 0, input);
				throw nvae;
			}

			switch (alt67) {
				case 1 :
					// src/main/resources/parser/Java.g:734:9: type
					{
					pushFollow(FOLLOW_type_in_typeArgument3180);
					type();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:735:9: '?' ( ( 'extends' | 'super' ) type )?
					{
					match(input,QUES,FOLLOW_QUES_in_typeArgument3190); if (state.failed) return;
					// src/main/resources/parser/Java.g:736:9: ( ( 'extends' | 'super' ) type )?
					int alt66=2;
					int LA66_0 = input.LA(1);
					if ( (LA66_0==EXTENDS||LA66_0==SUPER) ) {
						alt66=1;
					}
					switch (alt66) {
						case 1 :
							// src/main/resources/parser/Java.g:737:13: ( 'extends' | 'super' ) type
							{
							if ( input.LA(1)==EXTENDS||input.LA(1)==SUPER ) {
								input.consume();
								state.errorRecovery=false;
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								throw mse;
							}
							pushFollow(FOLLOW_type_in_typeArgument3258);
							type();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 36, typeArgument_StartIndex); }

		}
	}
	// $ANTLR end "typeArgument"



	// $ANTLR start "qualifiedNameList"
	// src/main/resources/parser/Java.g:744:1: qualifiedNameList : qualifiedName ( ',' qualifiedName )* ;
	public final void qualifiedNameList() throws RecognitionException {
		int qualifiedNameList_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 37) ) { return; }

			// src/main/resources/parser/Java.g:745:5: ( qualifiedName ( ',' qualifiedName )* )
			// src/main/resources/parser/Java.g:745:9: qualifiedName ( ',' qualifiedName )*
			{
			pushFollow(FOLLOW_qualifiedName_in_qualifiedNameList3289);
			qualifiedName();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:746:9: ( ',' qualifiedName )*
			loop68:
			while (true) {
				int alt68=2;
				int LA68_0 = input.LA(1);
				if ( (LA68_0==COMMA) ) {
					alt68=1;
				}

				switch (alt68) {
				case 1 :
					// src/main/resources/parser/Java.g:746:10: ',' qualifiedName
					{
					match(input,COMMA,FOLLOW_COMMA_in_qualifiedNameList3300); if (state.failed) return;
					pushFollow(FOLLOW_qualifiedName_in_qualifiedNameList3302);
					qualifiedName();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop68;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 37, qualifiedNameList_StartIndex); }

		}
	}
	// $ANTLR end "qualifiedNameList"



	// $ANTLR start "formalParameters"
	// src/main/resources/parser/Java.g:750:1: formalParameters : '(' ( formalParameterDecls )? ')' ;
	public final void formalParameters() throws RecognitionException {
		int formalParameters_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 38) ) { return; }

			// src/main/resources/parser/Java.g:751:5: ( '(' ( formalParameterDecls )? ')' )
			// src/main/resources/parser/Java.g:751:9: '(' ( formalParameterDecls )? ')'
			{
			match(input,LPAREN,FOLLOW_LPAREN_in_formalParameters3333); if (state.failed) return;
			// src/main/resources/parser/Java.g:752:9: ( formalParameterDecls )?
			int alt69=2;
			int LA69_0 = input.LA(1);
			if ( (LA69_0==BOOLEAN||LA69_0==BYTE||LA69_0==CHAR||LA69_0==DOUBLE||LA69_0==FINAL||LA69_0==FLOAT||LA69_0==IDENTIFIER||LA69_0==INT||LA69_0==LONG||LA69_0==MONKEYS_AT||LA69_0==SHORT) ) {
				alt69=1;
			}
			switch (alt69) {
				case 1 :
					// src/main/resources/parser/Java.g:752:10: formalParameterDecls
					{
					pushFollow(FOLLOW_formalParameterDecls_in_formalParameters3344);
					formalParameterDecls();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,RPAREN,FOLLOW_RPAREN_in_formalParameters3366); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 38, formalParameters_StartIndex); }

		}
	}
	// $ANTLR end "formalParameters"



	// $ANTLR start "formalParameterDecls"
	// src/main/resources/parser/Java.g:757:1: formalParameterDecls : ( ellipsisParameterDecl | normalParameterDecl ( ',' normalParameterDecl )* | ( normalParameterDecl ',' )+ ellipsisParameterDecl );
	public final void formalParameterDecls() throws RecognitionException {
		int formalParameterDecls_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 39) ) { return; }

			// src/main/resources/parser/Java.g:758:5: ( ellipsisParameterDecl | normalParameterDecl ( ',' normalParameterDecl )* | ( normalParameterDecl ',' )+ ellipsisParameterDecl )
			int alt72=3;
			switch ( input.LA(1) ) {
			case FINAL:
				{
				int LA72_1 = input.LA(2);
				if ( (synpred96_Java()) ) {
					alt72=1;
				}
				else if ( (synpred98_Java()) ) {
					alt72=2;
				}
				else if ( (true) ) {
					alt72=3;
				}

				}
				break;
			case MONKEYS_AT:
				{
				int LA72_2 = input.LA(2);
				if ( (synpred96_Java()) ) {
					alt72=1;
				}
				else if ( (synpred98_Java()) ) {
					alt72=2;
				}
				else if ( (true) ) {
					alt72=3;
				}

				}
				break;
			case IDENTIFIER:
				{
				int LA72_3 = input.LA(2);
				if ( (synpred96_Java()) ) {
					alt72=1;
				}
				else if ( (synpred98_Java()) ) {
					alt72=2;
				}
				else if ( (true) ) {
					alt72=3;
				}

				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				{
				int LA72_4 = input.LA(2);
				if ( (synpred96_Java()) ) {
					alt72=1;
				}
				else if ( (synpred98_Java()) ) {
					alt72=2;
				}
				else if ( (true) ) {
					alt72=3;
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 72, 0, input);
				throw nvae;
			}
			switch (alt72) {
				case 1 :
					// src/main/resources/parser/Java.g:758:9: ellipsisParameterDecl
					{
					pushFollow(FOLLOW_ellipsisParameterDecl_in_formalParameterDecls3386);
					ellipsisParameterDecl();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:759:9: normalParameterDecl ( ',' normalParameterDecl )*
					{
					pushFollow(FOLLOW_normalParameterDecl_in_formalParameterDecls3396);
					normalParameterDecl();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:760:9: ( ',' normalParameterDecl )*
					loop70:
					while (true) {
						int alt70=2;
						int LA70_0 = input.LA(1);
						if ( (LA70_0==COMMA) ) {
							alt70=1;
						}

						switch (alt70) {
						case 1 :
							// src/main/resources/parser/Java.g:760:10: ',' normalParameterDecl
							{
							match(input,COMMA,FOLLOW_COMMA_in_formalParameterDecls3407); if (state.failed) return;
							pushFollow(FOLLOW_normalParameterDecl_in_formalParameterDecls3409);
							normalParameterDecl();
							state._fsp--;
							if (state.failed) return;
							}
							break;

						default :
							break loop70;
						}
					}

					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:762:9: ( normalParameterDecl ',' )+ ellipsisParameterDecl
					{
					// src/main/resources/parser/Java.g:762:9: ( normalParameterDecl ',' )+
					int cnt71=0;
					loop71:
					while (true) {
						int alt71=2;
						switch ( input.LA(1) ) {
						case FINAL:
							{
							int LA71_1 = input.LA(2);
							if ( (synpred99_Java()) ) {
								alt71=1;
							}

							}
							break;
						case MONKEYS_AT:
							{
							int LA71_2 = input.LA(2);
							if ( (synpred99_Java()) ) {
								alt71=1;
							}

							}
							break;
						case IDENTIFIER:
							{
							int LA71_3 = input.LA(2);
							if ( (synpred99_Java()) ) {
								alt71=1;
							}

							}
							break;
						case BOOLEAN:
						case BYTE:
						case CHAR:
						case DOUBLE:
						case FLOAT:
						case INT:
						case LONG:
						case SHORT:
							{
							int LA71_4 = input.LA(2);
							if ( (synpred99_Java()) ) {
								alt71=1;
							}

							}
							break;
						}
						switch (alt71) {
						case 1 :
							// src/main/resources/parser/Java.g:762:10: normalParameterDecl ','
							{
							pushFollow(FOLLOW_normalParameterDecl_in_formalParameterDecls3431);
							normalParameterDecl();
							state._fsp--;
							if (state.failed) return;
							match(input,COMMA,FOLLOW_COMMA_in_formalParameterDecls3441); if (state.failed) return;
							}
							break;

						default :
							if ( cnt71 >= 1 ) break loop71;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(71, input);
							throw eee;
						}
						cnt71++;
					}

					pushFollow(FOLLOW_ellipsisParameterDecl_in_formalParameterDecls3463);
					ellipsisParameterDecl();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 39, formalParameterDecls_StartIndex); }

		}
	}
	// $ANTLR end "formalParameterDecls"



	// $ANTLR start "normalParameterDecl"
	// src/main/resources/parser/Java.g:768:1: normalParameterDecl : variableModifiers type IDENTIFIER ( '[' ']' )* ;
	public final void normalParameterDecl() throws RecognitionException {
		int normalParameterDecl_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 40) ) { return; }

			// src/main/resources/parser/Java.g:769:5: ( variableModifiers type IDENTIFIER ( '[' ']' )* )
			// src/main/resources/parser/Java.g:769:9: variableModifiers type IDENTIFIER ( '[' ']' )*
			{
			pushFollow(FOLLOW_variableModifiers_in_normalParameterDecl3483);
			variableModifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_normalParameterDecl3485);
			type();
			state._fsp--;
			if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_normalParameterDecl3487); if (state.failed) return;
			// src/main/resources/parser/Java.g:770:9: ( '[' ']' )*
			loop73:
			while (true) {
				int alt73=2;
				int LA73_0 = input.LA(1);
				if ( (LA73_0==LBRACKET) ) {
					alt73=1;
				}

				switch (alt73) {
				case 1 :
					// src/main/resources/parser/Java.g:770:10: '[' ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_normalParameterDecl3498); if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_normalParameterDecl3500); if (state.failed) return;
					}
					break;

				default :
					break loop73;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 40, normalParameterDecl_StartIndex); }

		}
	}
	// $ANTLR end "normalParameterDecl"



	// $ANTLR start "ellipsisParameterDecl"
	// src/main/resources/parser/Java.g:774:1: ellipsisParameterDecl : variableModifiers type '...' IDENTIFIER ;
	public final void ellipsisParameterDecl() throws RecognitionException {
		int ellipsisParameterDecl_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 41) ) { return; }

			// src/main/resources/parser/Java.g:775:5: ( variableModifiers type '...' IDENTIFIER )
			// src/main/resources/parser/Java.g:775:9: variableModifiers type '...' IDENTIFIER
			{
			pushFollow(FOLLOW_variableModifiers_in_ellipsisParameterDecl3531);
			variableModifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_ellipsisParameterDecl3541);
			type();
			state._fsp--;
			if (state.failed) return;
			match(input,ELLIPSIS,FOLLOW_ELLIPSIS_in_ellipsisParameterDecl3544); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_ellipsisParameterDecl3554); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 41, ellipsisParameterDecl_StartIndex); }

		}
	}
	// $ANTLR end "ellipsisParameterDecl"



	// $ANTLR start "explicitConstructorInvocation"
	// src/main/resources/parser/Java.g:781:1: explicitConstructorInvocation : ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' | primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';' );
	public final void explicitConstructorInvocation() throws RecognitionException {
		int explicitConstructorInvocation_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 42) ) { return; }

			// src/main/resources/parser/Java.g:782:5: ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' | primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';' )
			int alt76=2;
			switch ( input.LA(1) ) {
			case LT:
				{
				alt76=1;
				}
				break;
			case THIS:
				{
				int LA76_2 = input.LA(2);
				if ( (synpred103_Java()) ) {
					alt76=1;
				}
				else if ( (true) ) {
					alt76=2;
				}

				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case CHARLITERAL:
			case DOUBLE:
			case DOUBLELITERAL:
			case FALSE:
			case FLOAT:
			case FLOATLITERAL:
			case IDENTIFIER:
			case INT:
			case INTLITERAL:
			case LONG:
			case LONGLITERAL:
			case LPAREN:
			case NEW:
			case NULL:
			case SHORT:
			case STRINGLITERAL:
			case TRUE:
			case VOID:
				{
				alt76=2;
				}
				break;
			case SUPER:
				{
				int LA76_4 = input.LA(2);
				if ( (synpred103_Java()) ) {
					alt76=1;
				}
				else if ( (true) ) {
					alt76=2;
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 76, 0, input);
				throw nvae;
			}
			switch (alt76) {
				case 1 :
					// src/main/resources/parser/Java.g:782:9: ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';'
					{
					// src/main/resources/parser/Java.g:782:9: ( nonWildcardTypeArguments )?
					int alt74=2;
					int LA74_0 = input.LA(1);
					if ( (LA74_0==LT) ) {
						alt74=1;
					}
					switch (alt74) {
						case 1 :
							// src/main/resources/parser/Java.g:782:10: nonWildcardTypeArguments
							{
							pushFollow(FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation3576);
							nonWildcardTypeArguments();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					if ( input.LA(1)==SUPER||input.LA(1)==THIS ) {
						input.consume();
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_arguments_in_explicitConstructorInvocation3634);
					arguments();
					state._fsp--;
					if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_explicitConstructorInvocation3636); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:789:9: primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';'
					{
					pushFollow(FOLLOW_primary_in_explicitConstructorInvocation3647);
					primary();
					state._fsp--;
					if (state.failed) return;
					match(input,DOT,FOLLOW_DOT_in_explicitConstructorInvocation3657); if (state.failed) return;
					// src/main/resources/parser/Java.g:791:9: ( nonWildcardTypeArguments )?
					int alt75=2;
					int LA75_0 = input.LA(1);
					if ( (LA75_0==LT) ) {
						alt75=1;
					}
					switch (alt75) {
						case 1 :
							// src/main/resources/parser/Java.g:791:10: nonWildcardTypeArguments
							{
							pushFollow(FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation3668);
							nonWildcardTypeArguments();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SUPER,FOLLOW_SUPER_in_explicitConstructorInvocation3689); if (state.failed) return;
					pushFollow(FOLLOW_arguments_in_explicitConstructorInvocation3699);
					arguments();
					state._fsp--;
					if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_explicitConstructorInvocation3701); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 42, explicitConstructorInvocation_StartIndex); }

		}
	}
	// $ANTLR end "explicitConstructorInvocation"



	// $ANTLR start "qualifiedName"
	// src/main/resources/parser/Java.g:797:1: qualifiedName : IDENTIFIER ( '.' IDENTIFIER )* ;
	public final void qualifiedName() throws RecognitionException {
		int qualifiedName_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 43) ) { return; }

			// src/main/resources/parser/Java.g:798:5: ( IDENTIFIER ( '.' IDENTIFIER )* )
			// src/main/resources/parser/Java.g:798:9: IDENTIFIER ( '.' IDENTIFIER )*
			{
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_qualifiedName3721); if (state.failed) return;
			// src/main/resources/parser/Java.g:799:9: ( '.' IDENTIFIER )*
			loop77:
			while (true) {
				int alt77=2;
				int LA77_0 = input.LA(1);
				if ( (LA77_0==DOT) ) {
					alt77=1;
				}

				switch (alt77) {
				case 1 :
					// src/main/resources/parser/Java.g:799:10: '.' IDENTIFIER
					{
					match(input,DOT,FOLLOW_DOT_in_qualifiedName3732); if (state.failed) return;
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_qualifiedName3734); if (state.failed) return;
					}
					break;

				default :
					break loop77;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 43, qualifiedName_StartIndex); }

		}
	}
	// $ANTLR end "qualifiedName"



	// $ANTLR start "annotations"
	// src/main/resources/parser/Java.g:803:1: annotations : ( annotation )+ ;
	public final void annotations() throws RecognitionException {
		int annotations_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 44) ) { return; }

			// src/main/resources/parser/Java.g:804:5: ( ( annotation )+ )
			// src/main/resources/parser/Java.g:804:9: ( annotation )+
			{
			// src/main/resources/parser/Java.g:804:9: ( annotation )+
			int cnt78=0;
			loop78:
			while (true) {
				int alt78=2;
				int LA78_0 = input.LA(1);
				if ( (LA78_0==MONKEYS_AT) ) {
					alt78=1;
				}

				switch (alt78) {
				case 1 :
					// src/main/resources/parser/Java.g:804:10: annotation
					{
					pushFollow(FOLLOW_annotation_in_annotations3766);
					annotation();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					if ( cnt78 >= 1 ) break loop78;
					if (state.backtracking>0) {state.failed=true; return;}
					EarlyExitException eee = new EarlyExitException(78, input);
					throw eee;
				}
				cnt78++;
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 44, annotations_StartIndex); }

		}
	}
	// $ANTLR end "annotations"



	// $ANTLR start "annotation"
	// src/main/resources/parser/Java.g:812:1: annotation : '@' qualifiedName ( '(' ( elementValuePairs | elementValue )? ')' )? ;
	public final void annotation() throws RecognitionException {
		int annotation_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 45) ) { return; }

			// src/main/resources/parser/Java.g:813:5: ( '@' qualifiedName ( '(' ( elementValuePairs | elementValue )? ')' )? )
			// src/main/resources/parser/Java.g:813:9: '@' qualifiedName ( '(' ( elementValuePairs | elementValue )? ')' )?
			{
			match(input,MONKEYS_AT,FOLLOW_MONKEYS_AT_in_annotation3799); if (state.failed) return;
			pushFollow(FOLLOW_qualifiedName_in_annotation3801);
			qualifiedName();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:814:9: ( '(' ( elementValuePairs | elementValue )? ')' )?
			int alt80=2;
			int LA80_0 = input.LA(1);
			if ( (LA80_0==LPAREN) ) {
				alt80=1;
			}
			switch (alt80) {
				case 1 :
					// src/main/resources/parser/Java.g:814:13: '(' ( elementValuePairs | elementValue )? ')'
					{
					match(input,LPAREN,FOLLOW_LPAREN_in_annotation3815); if (state.failed) return;
					// src/main/resources/parser/Java.g:815:19: ( elementValuePairs | elementValue )?
					int alt79=3;
					int LA79_0 = input.LA(1);
					if ( (LA79_0==IDENTIFIER) ) {
						int LA79_1 = input.LA(2);
						if ( (LA79_1==EQ) ) {
							alt79=1;
						}
						else if ( ((LA79_1 >= AMP && LA79_1 <= AMPAMP)||(LA79_1 >= BANGEQ && LA79_1 <= BARBAR)||LA79_1==CARET||LA79_1==DOT||LA79_1==EQEQ||LA79_1==GT||LA79_1==INSTANCEOF||LA79_1==LBRACKET||(LA79_1 >= LPAREN && LA79_1 <= LT)||LA79_1==PERCENT||LA79_1==PLUS||LA79_1==PLUSPLUS||LA79_1==QUES||LA79_1==RPAREN||LA79_1==SLASH||LA79_1==STAR||LA79_1==SUB||LA79_1==SUBSUB) ) {
							alt79=2;
						}
					}
					else if ( (LA79_0==BANG||LA79_0==BOOLEAN||LA79_0==BYTE||(LA79_0 >= CHAR && LA79_0 <= CHARLITERAL)||(LA79_0 >= DOUBLE && LA79_0 <= DOUBLELITERAL)||LA79_0==FALSE||(LA79_0 >= FLOAT && LA79_0 <= FLOATLITERAL)||LA79_0==INT||LA79_0==INTLITERAL||LA79_0==LBRACE||(LA79_0 >= LONG && LA79_0 <= LPAREN)||LA79_0==MONKEYS_AT||(LA79_0 >= NEW && LA79_0 <= NULL)||LA79_0==PLUS||LA79_0==PLUSPLUS||LA79_0==SHORT||(LA79_0 >= STRINGLITERAL && LA79_0 <= SUB)||(LA79_0 >= SUBSUB && LA79_0 <= SUPER)||LA79_0==THIS||LA79_0==TILDE||LA79_0==TRUE||LA79_0==VOID) ) {
						alt79=2;
					}
					switch (alt79) {
						case 1 :
							// src/main/resources/parser/Java.g:815:23: elementValuePairs
							{
							pushFollow(FOLLOW_elementValuePairs_in_annotation3839);
							elementValuePairs();
							state._fsp--;
							if (state.failed) return;
							}
							break;
						case 2 :
							// src/main/resources/parser/Java.g:816:23: elementValue
							{
							pushFollow(FOLLOW_elementValue_in_annotation3863);
							elementValue();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,RPAREN,FOLLOW_RPAREN_in_annotation3899); if (state.failed) return;
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 45, annotation_StartIndex); }

		}
	}
	// $ANTLR end "annotation"



	// $ANTLR start "elementValuePairs"
	// src/main/resources/parser/Java.g:822:1: elementValuePairs : elementValuePair ( ',' elementValuePair )* ;
	public final void elementValuePairs() throws RecognitionException {
		int elementValuePairs_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 46) ) { return; }

			// src/main/resources/parser/Java.g:823:5: ( elementValuePair ( ',' elementValuePair )* )
			// src/main/resources/parser/Java.g:823:9: elementValuePair ( ',' elementValuePair )*
			{
			pushFollow(FOLLOW_elementValuePair_in_elementValuePairs3931);
			elementValuePair();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:824:9: ( ',' elementValuePair )*
			loop81:
			while (true) {
				int alt81=2;
				int LA81_0 = input.LA(1);
				if ( (LA81_0==COMMA) ) {
					alt81=1;
				}

				switch (alt81) {
				case 1 :
					// src/main/resources/parser/Java.g:824:10: ',' elementValuePair
					{
					match(input,COMMA,FOLLOW_COMMA_in_elementValuePairs3942); if (state.failed) return;
					pushFollow(FOLLOW_elementValuePair_in_elementValuePairs3944);
					elementValuePair();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop81;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 46, elementValuePairs_StartIndex); }

		}
	}
	// $ANTLR end "elementValuePairs"



	// $ANTLR start "elementValuePair"
	// src/main/resources/parser/Java.g:828:1: elementValuePair : IDENTIFIER '=' elementValue ;
	public final void elementValuePair() throws RecognitionException {
		int elementValuePair_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 47) ) { return; }

			// src/main/resources/parser/Java.g:829:5: ( IDENTIFIER '=' elementValue )
			// src/main/resources/parser/Java.g:829:9: IDENTIFIER '=' elementValue
			{
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_elementValuePair3975); if (state.failed) return;
			match(input,EQ,FOLLOW_EQ_in_elementValuePair3977); if (state.failed) return;
			pushFollow(FOLLOW_elementValue_in_elementValuePair3979);
			elementValue();
			state._fsp--;
			if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 47, elementValuePair_StartIndex); }

		}
	}
	// $ANTLR end "elementValuePair"



	// $ANTLR start "elementValue"
	// src/main/resources/parser/Java.g:832:1: elementValue : ( conditionalExpression | annotation | elementValueArrayInitializer );
	public final void elementValue() throws RecognitionException {
		int elementValue_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 48) ) { return; }

			// src/main/resources/parser/Java.g:833:5: ( conditionalExpression | annotation | elementValueArrayInitializer )
			int alt82=3;
			switch ( input.LA(1) ) {
			case BANG:
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case CHARLITERAL:
			case DOUBLE:
			case DOUBLELITERAL:
			case FALSE:
			case FLOAT:
			case FLOATLITERAL:
			case IDENTIFIER:
			case INT:
			case INTLITERAL:
			case LONG:
			case LONGLITERAL:
			case LPAREN:
			case NEW:
			case NULL:
			case PLUS:
			case PLUSPLUS:
			case SHORT:
			case STRINGLITERAL:
			case SUB:
			case SUBSUB:
			case SUPER:
			case THIS:
			case TILDE:
			case TRUE:
			case VOID:
				{
				alt82=1;
				}
				break;
			case MONKEYS_AT:
				{
				alt82=2;
				}
				break;
			case LBRACE:
				{
				alt82=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 82, 0, input);
				throw nvae;
			}
			switch (alt82) {
				case 1 :
					// src/main/resources/parser/Java.g:833:9: conditionalExpression
					{
					pushFollow(FOLLOW_conditionalExpression_in_elementValue3999);
					conditionalExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:834:9: annotation
					{
					pushFollow(FOLLOW_annotation_in_elementValue4009);
					annotation();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:835:9: elementValueArrayInitializer
					{
					pushFollow(FOLLOW_elementValueArrayInitializer_in_elementValue4019);
					elementValueArrayInitializer();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 48, elementValue_StartIndex); }

		}
	}
	// $ANTLR end "elementValue"



	// $ANTLR start "elementValueArrayInitializer"
	// src/main/resources/parser/Java.g:838:1: elementValueArrayInitializer : '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}' ;
	public final void elementValueArrayInitializer() throws RecognitionException {
		int elementValueArrayInitializer_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 49) ) { return; }

			// src/main/resources/parser/Java.g:839:5: ( '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}' )
			// src/main/resources/parser/Java.g:839:9: '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_elementValueArrayInitializer4039); if (state.failed) return;
			// src/main/resources/parser/Java.g:840:9: ( elementValue ( ',' elementValue )* )?
			int alt84=2;
			int LA84_0 = input.LA(1);
			if ( (LA84_0==BANG||LA84_0==BOOLEAN||LA84_0==BYTE||(LA84_0 >= CHAR && LA84_0 <= CHARLITERAL)||(LA84_0 >= DOUBLE && LA84_0 <= DOUBLELITERAL)||LA84_0==FALSE||(LA84_0 >= FLOAT && LA84_0 <= FLOATLITERAL)||LA84_0==IDENTIFIER||LA84_0==INT||LA84_0==INTLITERAL||LA84_0==LBRACE||(LA84_0 >= LONG && LA84_0 <= LPAREN)||LA84_0==MONKEYS_AT||(LA84_0 >= NEW && LA84_0 <= NULL)||LA84_0==PLUS||LA84_0==PLUSPLUS||LA84_0==SHORT||(LA84_0 >= STRINGLITERAL && LA84_0 <= SUB)||(LA84_0 >= SUBSUB && LA84_0 <= SUPER)||LA84_0==THIS||LA84_0==TILDE||LA84_0==TRUE||LA84_0==VOID) ) {
				alt84=1;
			}
			switch (alt84) {
				case 1 :
					// src/main/resources/parser/Java.g:840:10: elementValue ( ',' elementValue )*
					{
					pushFollow(FOLLOW_elementValue_in_elementValueArrayInitializer4050);
					elementValue();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:841:13: ( ',' elementValue )*
					loop83:
					while (true) {
						int alt83=2;
						int LA83_0 = input.LA(1);
						if ( (LA83_0==COMMA) ) {
							int LA83_1 = input.LA(2);
							if ( (LA83_1==BANG||LA83_1==BOOLEAN||LA83_1==BYTE||(LA83_1 >= CHAR && LA83_1 <= CHARLITERAL)||(LA83_1 >= DOUBLE && LA83_1 <= DOUBLELITERAL)||LA83_1==FALSE||(LA83_1 >= FLOAT && LA83_1 <= FLOATLITERAL)||LA83_1==IDENTIFIER||LA83_1==INT||LA83_1==INTLITERAL||LA83_1==LBRACE||(LA83_1 >= LONG && LA83_1 <= LPAREN)||LA83_1==MONKEYS_AT||(LA83_1 >= NEW && LA83_1 <= NULL)||LA83_1==PLUS||LA83_1==PLUSPLUS||LA83_1==SHORT||(LA83_1 >= STRINGLITERAL && LA83_1 <= SUB)||(LA83_1 >= SUBSUB && LA83_1 <= SUPER)||LA83_1==THIS||LA83_1==TILDE||LA83_1==TRUE||LA83_1==VOID) ) {
								alt83=1;
							}

						}

						switch (alt83) {
						case 1 :
							// src/main/resources/parser/Java.g:841:14: ',' elementValue
							{
							match(input,COMMA,FOLLOW_COMMA_in_elementValueArrayInitializer4065); if (state.failed) return;
							pushFollow(FOLLOW_elementValue_in_elementValueArrayInitializer4067);
							elementValue();
							state._fsp--;
							if (state.failed) return;
							}
							break;

						default :
							break loop83;
						}
					}

					}
					break;

			}

			// src/main/resources/parser/Java.g:843:12: ( ',' )?
			int alt85=2;
			int LA85_0 = input.LA(1);
			if ( (LA85_0==COMMA) ) {
				alt85=1;
			}
			switch (alt85) {
				case 1 :
					// src/main/resources/parser/Java.g:843:13: ','
					{
					match(input,COMMA,FOLLOW_COMMA_in_elementValueArrayInitializer4096); if (state.failed) return;
					}
					break;

			}

			match(input,RBRACE,FOLLOW_RBRACE_in_elementValueArrayInitializer4100); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 49, elementValueArrayInitializer_StartIndex); }

		}
	}
	// $ANTLR end "elementValueArrayInitializer"



	// $ANTLR start "annotationTypeDeclaration"
	// src/main/resources/parser/Java.g:850:1: annotationTypeDeclaration : modifiers '@' 'interface' IDENTIFIER annotationTypeBody ;
	public final void annotationTypeDeclaration() throws RecognitionException {
		int annotationTypeDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 50) ) { return; }

			// src/main/resources/parser/Java.g:851:5: ( modifiers '@' 'interface' IDENTIFIER annotationTypeBody )
			// src/main/resources/parser/Java.g:851:9: modifiers '@' 'interface' IDENTIFIER annotationTypeBody
			{
			pushFollow(FOLLOW_modifiers_in_annotationTypeDeclaration4123);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			match(input,MONKEYS_AT,FOLLOW_MONKEYS_AT_in_annotationTypeDeclaration4125); if (state.failed) return;
			match(input,INTERFACE,FOLLOW_INTERFACE_in_annotationTypeDeclaration4135); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_annotationTypeDeclaration4145); if (state.failed) return;
			pushFollow(FOLLOW_annotationTypeBody_in_annotationTypeDeclaration4155);
			annotationTypeBody();
			state._fsp--;
			if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 50, annotationTypeDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "annotationTypeDeclaration"



	// $ANTLR start "annotationTypeBody"
	// src/main/resources/parser/Java.g:858:1: annotationTypeBody : '{' ( annotationTypeElementDeclaration )* '}' ;
	public final void annotationTypeBody() throws RecognitionException {
		int annotationTypeBody_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 51) ) { return; }

			// src/main/resources/parser/Java.g:859:5: ( '{' ( annotationTypeElementDeclaration )* '}' )
			// src/main/resources/parser/Java.g:859:9: '{' ( annotationTypeElementDeclaration )* '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_annotationTypeBody4176); if (state.failed) return;
			// src/main/resources/parser/Java.g:860:9: ( annotationTypeElementDeclaration )*
			loop86:
			while (true) {
				int alt86=2;
				int LA86_0 = input.LA(1);
				if ( (LA86_0==ABSTRACT||LA86_0==BOOLEAN||LA86_0==BYTE||LA86_0==CHAR||LA86_0==CLASS||LA86_0==DOUBLE||LA86_0==ENUM||LA86_0==FINAL||LA86_0==FLOAT||LA86_0==IDENTIFIER||(LA86_0 >= INT && LA86_0 <= INTERFACE)||LA86_0==LONG||LA86_0==LT||(LA86_0 >= MONKEYS_AT && LA86_0 <= NATIVE)||(LA86_0 >= PRIVATE && LA86_0 <= PUBLIC)||(LA86_0 >= SEMI && LA86_0 <= SHORT)||(LA86_0 >= STATIC && LA86_0 <= STRICTFP)||LA86_0==SYNCHRONIZED||LA86_0==TRANSIENT||(LA86_0 >= VOID && LA86_0 <= VOLATILE)) ) {
					alt86=1;
				}

				switch (alt86) {
				case 1 :
					// src/main/resources/parser/Java.g:860:10: annotationTypeElementDeclaration
					{
					pushFollow(FOLLOW_annotationTypeElementDeclaration_in_annotationTypeBody4188);
					annotationTypeElementDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop86;
				}
			}

			match(input,RBRACE,FOLLOW_RBRACE_in_annotationTypeBody4210); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 51, annotationTypeBody_StartIndex); }

		}
	}
	// $ANTLR end "annotationTypeBody"



	// $ANTLR start "annotationTypeElementDeclaration"
	// src/main/resources/parser/Java.g:868:1: annotationTypeElementDeclaration : ( annotationMethodDeclaration | interfaceFieldDeclaration | normalClassDeclaration | normalInterfaceDeclaration | enumDeclaration | annotationTypeDeclaration | ';' );
	public final void annotationTypeElementDeclaration() throws RecognitionException {
		int annotationTypeElementDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 52) ) { return; }

			// src/main/resources/parser/Java.g:869:5: ( annotationMethodDeclaration | interfaceFieldDeclaration | normalClassDeclaration | normalInterfaceDeclaration | enumDeclaration | annotationTypeDeclaration | ';' )
			int alt87=7;
			switch ( input.LA(1) ) {
			case MONKEYS_AT:
				{
				int LA87_1 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case PUBLIC:
				{
				int LA87_2 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case PROTECTED:
				{
				int LA87_3 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case PRIVATE:
				{
				int LA87_4 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case STATIC:
				{
				int LA87_5 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case ABSTRACT:
				{
				int LA87_6 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 6, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FINAL:
				{
				int LA87_7 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 7, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NATIVE:
				{
				int LA87_8 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 8, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case SYNCHRONIZED:
				{
				int LA87_9 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 9, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case TRANSIENT:
				{
				int LA87_10 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 10, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case VOLATILE:
				{
				int LA87_11 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 11, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case STRICTFP:
				{
				int LA87_12 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}
				else if ( (synpred119_Java()) ) {
					alt87=3;
				}
				else if ( (synpred120_Java()) ) {
					alt87=4;
				}
				else if ( (synpred121_Java()) ) {
					alt87=5;
				}
				else if ( (synpred122_Java()) ) {
					alt87=6;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 12, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case IDENTIFIER:
				{
				int LA87_13 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 13, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				{
				int LA87_14 = input.LA(2);
				if ( (synpred117_Java()) ) {
					alt87=1;
				}
				else if ( (synpred118_Java()) ) {
					alt87=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 87, 14, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case CLASS:
				{
				alt87=3;
				}
				break;
			case INTERFACE:
				{
				alt87=4;
				}
				break;
			case ENUM:
				{
				alt87=5;
				}
				break;
			case SEMI:
				{
				alt87=7;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 87, 0, input);
				throw nvae;
			}
			switch (alt87) {
				case 1 :
					// src/main/resources/parser/Java.g:869:9: annotationMethodDeclaration
					{
					pushFollow(FOLLOW_annotationMethodDeclaration_in_annotationTypeElementDeclaration4232);
					annotationMethodDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:870:9: interfaceFieldDeclaration
					{
					pushFollow(FOLLOW_interfaceFieldDeclaration_in_annotationTypeElementDeclaration4242);
					interfaceFieldDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:871:9: normalClassDeclaration
					{
					pushFollow(FOLLOW_normalClassDeclaration_in_annotationTypeElementDeclaration4252);
					normalClassDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:872:9: normalInterfaceDeclaration
					{
					pushFollow(FOLLOW_normalInterfaceDeclaration_in_annotationTypeElementDeclaration4262);
					normalInterfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:873:9: enumDeclaration
					{
					pushFollow(FOLLOW_enumDeclaration_in_annotationTypeElementDeclaration4272);
					enumDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:874:9: annotationTypeDeclaration
					{
					pushFollow(FOLLOW_annotationTypeDeclaration_in_annotationTypeElementDeclaration4282);
					annotationTypeDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:875:9: ';'
					{
					match(input,SEMI,FOLLOW_SEMI_in_annotationTypeElementDeclaration4292); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 52, annotationTypeElementDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "annotationTypeElementDeclaration"



	// $ANTLR start "annotationMethodDeclaration"
	// src/main/resources/parser/Java.g:878:1: annotationMethodDeclaration : modifiers type IDENTIFIER '(' ')' ( 'default' elementValue )? ';' ;
	public final void annotationMethodDeclaration() throws RecognitionException {
		int annotationMethodDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 53) ) { return; }

			// src/main/resources/parser/Java.g:879:5: ( modifiers type IDENTIFIER '(' ')' ( 'default' elementValue )? ';' )
			// src/main/resources/parser/Java.g:879:9: modifiers type IDENTIFIER '(' ')' ( 'default' elementValue )? ';'
			{
			pushFollow(FOLLOW_modifiers_in_annotationMethodDeclaration4312);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_annotationMethodDeclaration4314);
			type();
			state._fsp--;
			if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_annotationMethodDeclaration4316); if (state.failed) return;
			match(input,LPAREN,FOLLOW_LPAREN_in_annotationMethodDeclaration4326); if (state.failed) return;
			match(input,RPAREN,FOLLOW_RPAREN_in_annotationMethodDeclaration4328); if (state.failed) return;
			// src/main/resources/parser/Java.g:880:17: ( 'default' elementValue )?
			int alt88=2;
			int LA88_0 = input.LA(1);
			if ( (LA88_0==DEFAULT) ) {
				alt88=1;
			}
			switch (alt88) {
				case 1 :
					// src/main/resources/parser/Java.g:880:18: 'default' elementValue
					{
					match(input,DEFAULT,FOLLOW_DEFAULT_in_annotationMethodDeclaration4331); if (state.failed) return;
					pushFollow(FOLLOW_elementValue_in_annotationMethodDeclaration4333);
					elementValue();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,SEMI,FOLLOW_SEMI_in_annotationMethodDeclaration4362); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 53, annotationMethodDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "annotationMethodDeclaration"



	// $ANTLR start "block"
	// src/main/resources/parser/Java.g:885:1: block : '{' ( blockStatement )* '}' ;
	public final void block() throws RecognitionException {
		int block_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 54) ) { return; }

			// src/main/resources/parser/Java.g:886:5: ( '{' ( blockStatement )* '}' )
			// src/main/resources/parser/Java.g:886:9: '{' ( blockStatement )* '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_block4386); if (state.failed) return;
			// src/main/resources/parser/Java.g:887:9: ( blockStatement )*
			loop89:
			while (true) {
				int alt89=2;
				int LA89_0 = input.LA(1);
				if ( (LA89_0==ABSTRACT||(LA89_0 >= ASSERT && LA89_0 <= BANG)||(LA89_0 >= BOOLEAN && LA89_0 <= BYTE)||(LA89_0 >= CHAR && LA89_0 <= CLASS)||LA89_0==CONTINUE||LA89_0==DO||(LA89_0 >= DOUBLE && LA89_0 <= DOUBLELITERAL)||LA89_0==ENUM||(LA89_0 >= FALSE && LA89_0 <= FINAL)||(LA89_0 >= FLOAT && LA89_0 <= FOR)||(LA89_0 >= IDENTIFIER && LA89_0 <= IF)||(LA89_0 >= INT && LA89_0 <= INTLITERAL)||LA89_0==LBRACE||(LA89_0 >= LONG && LA89_0 <= LT)||(LA89_0 >= MONKEYS_AT && LA89_0 <= NULL)||LA89_0==PLUS||(LA89_0 >= PLUSPLUS && LA89_0 <= PUBLIC)||LA89_0==RETURN||(LA89_0 >= SEMI && LA89_0 <= SHORT)||(LA89_0 >= STATIC && LA89_0 <= SUB)||(LA89_0 >= SUBSUB && LA89_0 <= SYNCHRONIZED)||(LA89_0 >= THIS && LA89_0 <= THROW)||(LA89_0 >= TILDE && LA89_0 <= WHILE)) ) {
					alt89=1;
				}

				switch (alt89) {
				case 1 :
					// src/main/resources/parser/Java.g:887:10: blockStatement
					{
					pushFollow(FOLLOW_blockStatement_in_block4397);
					blockStatement();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop89;
				}
			}

			match(input,RBRACE,FOLLOW_RBRACE_in_block4418); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 54, block_StartIndex); }

		}
	}
	// $ANTLR end "block"



	// $ANTLR start "blockStatement"
	// src/main/resources/parser/Java.g:916:1: blockStatement : ( localVariableDeclarationStatement | classOrInterfaceDeclaration | statement );
	public final void blockStatement() throws RecognitionException {
		int blockStatement_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 55) ) { return; }

			// src/main/resources/parser/Java.g:917:5: ( localVariableDeclarationStatement | classOrInterfaceDeclaration | statement )
			int alt90=3;
			switch ( input.LA(1) ) {
			case FINAL:
				{
				int LA90_1 = input.LA(2);
				if ( (synpred125_Java()) ) {
					alt90=1;
				}
				else if ( (synpred126_Java()) ) {
					alt90=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 90, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case MONKEYS_AT:
				{
				int LA90_2 = input.LA(2);
				if ( (synpred125_Java()) ) {
					alt90=1;
				}
				else if ( (synpred126_Java()) ) {
					alt90=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 90, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case IDENTIFIER:
				{
				int LA90_3 = input.LA(2);
				if ( (synpred125_Java()) ) {
					alt90=1;
				}
				else if ( (true) ) {
					alt90=3;
				}

				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				{
				int LA90_4 = input.LA(2);
				if ( (synpred125_Java()) ) {
					alt90=1;
				}
				else if ( (true) ) {
					alt90=3;
				}

				}
				break;
			case ABSTRACT:
			case CLASS:
			case ENUM:
			case INTERFACE:
			case NATIVE:
			case PRIVATE:
			case PROTECTED:
			case PUBLIC:
			case STATIC:
			case STRICTFP:
			case TRANSIENT:
			case VOLATILE:
				{
				alt90=2;
				}
				break;
			case SYNCHRONIZED:
				{
				int LA90_11 = input.LA(2);
				if ( (synpred126_Java()) ) {
					alt90=2;
				}
				else if ( (true) ) {
					alt90=3;
				}

				}
				break;
			case ASSERT:
			case BANG:
			case BREAK:
			case CHARLITERAL:
			case CONTINUE:
			case DO:
			case DOUBLELITERAL:
			case FALSE:
			case FLOATLITERAL:
			case FOR:
			case IF:
			case INTLITERAL:
			case LBRACE:
			case LONGLITERAL:
			case LPAREN:
			case NEW:
			case NULL:
			case PLUS:
			case PLUSPLUS:
			case RETURN:
			case SEMI:
			case STRINGLITERAL:
			case SUB:
			case SUBSUB:
			case SUPER:
			case SWITCH:
			case THIS:
			case THROW:
			case TILDE:
			case TRUE:
			case TRY:
			case VOID:
			case WHILE:
				{
				alt90=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 90, 0, input);
				throw nvae;
			}
			switch (alt90) {
				case 1 :
					// src/main/resources/parser/Java.g:917:9: localVariableDeclarationStatement
					{
					pushFollow(FOLLOW_localVariableDeclarationStatement_in_blockStatement4440);
					localVariableDeclarationStatement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:918:9: classOrInterfaceDeclaration
					{
					pushFollow(FOLLOW_classOrInterfaceDeclaration_in_blockStatement4450);
					classOrInterfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:919:9: statement
					{
					pushFollow(FOLLOW_statement_in_blockStatement4460);
					statement();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 55, blockStatement_StartIndex); }

		}
	}
	// $ANTLR end "blockStatement"



	// $ANTLR start "localVariableDeclarationStatement"
	// src/main/resources/parser/Java.g:923:1: localVariableDeclarationStatement : localVariableDeclaration ';' ;
	public final void localVariableDeclarationStatement() throws RecognitionException {
		int localVariableDeclarationStatement_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 56) ) { return; }

			// src/main/resources/parser/Java.g:924:5: ( localVariableDeclaration ';' )
			// src/main/resources/parser/Java.g:924:9: localVariableDeclaration ';'
			{
			pushFollow(FOLLOW_localVariableDeclaration_in_localVariableDeclarationStatement4481);
			localVariableDeclaration();
			state._fsp--;
			if (state.failed) return;
			match(input,SEMI,FOLLOW_SEMI_in_localVariableDeclarationStatement4491); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 56, localVariableDeclarationStatement_StartIndex); }

		}
	}
	// $ANTLR end "localVariableDeclarationStatement"



	// $ANTLR start "localVariableDeclaration"
	// src/main/resources/parser/Java.g:928:1: localVariableDeclaration : variableModifiers type variableDeclarator ( ',' variableDeclarator )* ;
	public final void localVariableDeclaration() throws RecognitionException {
		int localVariableDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 57) ) { return; }

			// src/main/resources/parser/Java.g:929:5: ( variableModifiers type variableDeclarator ( ',' variableDeclarator )* )
			// src/main/resources/parser/Java.g:929:9: variableModifiers type variableDeclarator ( ',' variableDeclarator )*
			{
			pushFollow(FOLLOW_variableModifiers_in_localVariableDeclaration4511);
			variableModifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_localVariableDeclaration4513);
			type();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_variableDeclarator_in_localVariableDeclaration4523);
			variableDeclarator();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:931:9: ( ',' variableDeclarator )*
			loop91:
			while (true) {
				int alt91=2;
				int LA91_0 = input.LA(1);
				if ( (LA91_0==COMMA) ) {
					alt91=1;
				}

				switch (alt91) {
				case 1 :
					// src/main/resources/parser/Java.g:931:10: ',' variableDeclarator
					{
					match(input,COMMA,FOLLOW_COMMA_in_localVariableDeclaration4534); if (state.failed) return;
					pushFollow(FOLLOW_variableDeclarator_in_localVariableDeclaration4536);
					variableDeclarator();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop91;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 57, localVariableDeclaration_StartIndex); }

		}
	}
	// $ANTLR end "localVariableDeclaration"



	// $ANTLR start "statement"
	// src/main/resources/parser/Java.g:935:1: statement : ( block | ( 'assert' ) expression ( ':' expression )? ';' | 'assert' expression ( ':' expression )? ';' | 'if' parExpression statement ( 'else' statement )? | forstatement | 'while' parExpression statement | 'do' statement 'while' parExpression ';' | trystatement | 'switch' parExpression '{' switchBlockStatementGroups '}' | 'synchronized' parExpression block | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( IDENTIFIER )? ';' | 'continue' ( IDENTIFIER )? ';' | expression ';' | IDENTIFIER ':' statement | ';' );
	public final void statement() throws RecognitionException {
		int statement_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 58) ) { return; }

			// src/main/resources/parser/Java.g:936:5: ( block | ( 'assert' ) expression ( ':' expression )? ';' | 'assert' expression ( ':' expression )? ';' | 'if' parExpression statement ( 'else' statement )? | forstatement | 'while' parExpression statement | 'do' statement 'while' parExpression ';' | trystatement | 'switch' parExpression '{' switchBlockStatementGroups '}' | 'synchronized' parExpression block | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( IDENTIFIER )? ';' | 'continue' ( IDENTIFIER )? ';' | expression ';' | IDENTIFIER ':' statement | ';' )
			int alt98=17;
			switch ( input.LA(1) ) {
			case LBRACE:
				{
				alt98=1;
				}
				break;
			case ASSERT:
				{
				int LA98_2 = input.LA(2);
				if ( (synpred130_Java()) ) {
					alt98=2;
				}
				else if ( (synpred132_Java()) ) {
					alt98=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 98, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case IF:
				{
				alt98=4;
				}
				break;
			case FOR:
				{
				alt98=5;
				}
				break;
			case WHILE:
				{
				alt98=6;
				}
				break;
			case DO:
				{
				alt98=7;
				}
				break;
			case TRY:
				{
				alt98=8;
				}
				break;
			case SWITCH:
				{
				alt98=9;
				}
				break;
			case SYNCHRONIZED:
				{
				alt98=10;
				}
				break;
			case RETURN:
				{
				alt98=11;
				}
				break;
			case THROW:
				{
				alt98=12;
				}
				break;
			case BREAK:
				{
				alt98=13;
				}
				break;
			case CONTINUE:
				{
				alt98=14;
				}
				break;
			case BANG:
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case CHARLITERAL:
			case DOUBLE:
			case DOUBLELITERAL:
			case FALSE:
			case FLOAT:
			case FLOATLITERAL:
			case INT:
			case INTLITERAL:
			case LONG:
			case LONGLITERAL:
			case LPAREN:
			case NEW:
			case NULL:
			case PLUS:
			case PLUSPLUS:
			case SHORT:
			case STRINGLITERAL:
			case SUB:
			case SUBSUB:
			case SUPER:
			case THIS:
			case TILDE:
			case TRUE:
			case VOID:
				{
				alt98=15;
				}
				break;
			case IDENTIFIER:
				{
				int LA98_22 = input.LA(2);
				if ( (synpred148_Java()) ) {
					alt98=15;
				}
				else if ( (synpred149_Java()) ) {
					alt98=16;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 98, 22, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case SEMI:
				{
				alt98=17;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 98, 0, input);
				throw nvae;
			}
			switch (alt98) {
				case 1 :
					// src/main/resources/parser/Java.g:936:9: block
					{
					pushFollow(FOLLOW_block_in_statement4567);
					block();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:938:9: ( 'assert' ) expression ( ':' expression )? ';'
					{
					// src/main/resources/parser/Java.g:938:9: ( 'assert' )
					// src/main/resources/parser/Java.g:938:10: 'assert'
					{
					match(input,ASSERT,FOLLOW_ASSERT_in_statement4591); if (state.failed) return;
					}

					pushFollow(FOLLOW_expression_in_statement4611);
					expression();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:940:20: ( ':' expression )?
					int alt92=2;
					int LA92_0 = input.LA(1);
					if ( (LA92_0==COLON) ) {
						alt92=1;
					}
					switch (alt92) {
						case 1 :
							// src/main/resources/parser/Java.g:940:21: ':' expression
							{
							match(input,COLON,FOLLOW_COLON_in_statement4614); if (state.failed) return;
							pushFollow(FOLLOW_expression_in_statement4616);
							expression();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_statement4620); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:941:9: 'assert' expression ( ':' expression )? ';'
					{
					match(input,ASSERT,FOLLOW_ASSERT_in_statement4630); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_statement4633);
					expression();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:941:30: ( ':' expression )?
					int alt93=2;
					int LA93_0 = input.LA(1);
					if ( (LA93_0==COLON) ) {
						alt93=1;
					}
					switch (alt93) {
						case 1 :
							// src/main/resources/parser/Java.g:941:31: ':' expression
							{
							match(input,COLON,FOLLOW_COLON_in_statement4636); if (state.failed) return;
							pushFollow(FOLLOW_expression_in_statement4638);
							expression();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_statement4642); if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:942:9: 'if' parExpression statement ( 'else' statement )?
					{
					match(input,IF,FOLLOW_IF_in_statement4664); if (state.failed) return;
					pushFollow(FOLLOW_parExpression_in_statement4666);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_statement_in_statement4668);
					statement();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:942:38: ( 'else' statement )?
					int alt94=2;
					int LA94_0 = input.LA(1);
					if ( (LA94_0==ELSE) ) {
						int LA94_1 = input.LA(2);
						if ( (synpred133_Java()) ) {
							alt94=1;
						}
					}
					switch (alt94) {
						case 1 :
							// src/main/resources/parser/Java.g:942:39: 'else' statement
							{
							match(input,ELSE,FOLLOW_ELSE_in_statement4671); if (state.failed) return;
							pushFollow(FOLLOW_statement_in_statement4673);
							statement();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:943:9: forstatement
					{
					pushFollow(FOLLOW_forstatement_in_statement4695);
					forstatement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:944:9: 'while' parExpression statement
					{
					match(input,WHILE,FOLLOW_WHILE_in_statement4705); if (state.failed) return;
					pushFollow(FOLLOW_parExpression_in_statement4707);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_statement_in_statement4709);
					statement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:945:9: 'do' statement 'while' parExpression ';'
					{
					match(input,DO,FOLLOW_DO_in_statement4719); if (state.failed) return;
					pushFollow(FOLLOW_statement_in_statement4721);
					statement();
					state._fsp--;
					if (state.failed) return;
					match(input,WHILE,FOLLOW_WHILE_in_statement4723); if (state.failed) return;
					pushFollow(FOLLOW_parExpression_in_statement4725);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_statement4727); if (state.failed) return;
					}
					break;
				case 8 :
					// src/main/resources/parser/Java.g:946:9: trystatement
					{
					pushFollow(FOLLOW_trystatement_in_statement4737);
					trystatement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 9 :
					// src/main/resources/parser/Java.g:947:9: 'switch' parExpression '{' switchBlockStatementGroups '}'
					{
					match(input,SWITCH,FOLLOW_SWITCH_in_statement4747); if (state.failed) return;
					pushFollow(FOLLOW_parExpression_in_statement4749);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					match(input,LBRACE,FOLLOW_LBRACE_in_statement4751); if (state.failed) return;
					pushFollow(FOLLOW_switchBlockStatementGroups_in_statement4753);
					switchBlockStatementGroups();
					state._fsp--;
					if (state.failed) return;
					match(input,RBRACE,FOLLOW_RBRACE_in_statement4755); if (state.failed) return;
					}
					break;
				case 10 :
					// src/main/resources/parser/Java.g:948:9: 'synchronized' parExpression block
					{
					match(input,SYNCHRONIZED,FOLLOW_SYNCHRONIZED_in_statement4765); if (state.failed) return;
					pushFollow(FOLLOW_parExpression_in_statement4767);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_block_in_statement4769);
					block();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 11 :
					// src/main/resources/parser/Java.g:949:9: 'return' ( expression )? ';'
					{
					match(input,RETURN,FOLLOW_RETURN_in_statement4779); if (state.failed) return;
					// src/main/resources/parser/Java.g:949:18: ( expression )?
					int alt95=2;
					int LA95_0 = input.LA(1);
					if ( (LA95_0==BANG||LA95_0==BOOLEAN||LA95_0==BYTE||(LA95_0 >= CHAR && LA95_0 <= CHARLITERAL)||(LA95_0 >= DOUBLE && LA95_0 <= DOUBLELITERAL)||LA95_0==FALSE||(LA95_0 >= FLOAT && LA95_0 <= FLOATLITERAL)||LA95_0==IDENTIFIER||LA95_0==INT||LA95_0==INTLITERAL||(LA95_0 >= LONG && LA95_0 <= LPAREN)||(LA95_0 >= NEW && LA95_0 <= NULL)||LA95_0==PLUS||LA95_0==PLUSPLUS||LA95_0==SHORT||(LA95_0 >= STRINGLITERAL && LA95_0 <= SUB)||(LA95_0 >= SUBSUB && LA95_0 <= SUPER)||LA95_0==THIS||LA95_0==TILDE||LA95_0==TRUE||LA95_0==VOID) ) {
						alt95=1;
					}
					switch (alt95) {
						case 1 :
							// src/main/resources/parser/Java.g:949:19: expression
							{
							pushFollow(FOLLOW_expression_in_statement4782);
							expression();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_statement4787); if (state.failed) return;
					}
					break;
				case 12 :
					// src/main/resources/parser/Java.g:950:9: 'throw' expression ';'
					{
					match(input,THROW,FOLLOW_THROW_in_statement4797); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_statement4799);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_statement4801); if (state.failed) return;
					}
					break;
				case 13 :
					// src/main/resources/parser/Java.g:951:9: 'break' ( IDENTIFIER )? ';'
					{
					match(input,BREAK,FOLLOW_BREAK_in_statement4811); if (state.failed) return;
					// src/main/resources/parser/Java.g:952:13: ( IDENTIFIER )?
					int alt96=2;
					int LA96_0 = input.LA(1);
					if ( (LA96_0==IDENTIFIER) ) {
						alt96=1;
					}
					switch (alt96) {
						case 1 :
							// src/main/resources/parser/Java.g:952:14: IDENTIFIER
							{
							match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_statement4826); if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_statement4843); if (state.failed) return;
					}
					break;
				case 14 :
					// src/main/resources/parser/Java.g:954:9: 'continue' ( IDENTIFIER )? ';'
					{
					match(input,CONTINUE,FOLLOW_CONTINUE_in_statement4853); if (state.failed) return;
					// src/main/resources/parser/Java.g:955:13: ( IDENTIFIER )?
					int alt97=2;
					int LA97_0 = input.LA(1);
					if ( (LA97_0==IDENTIFIER) ) {
						alt97=1;
					}
					switch (alt97) {
						case 1 :
							// src/main/resources/parser/Java.g:955:14: IDENTIFIER
							{
							match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_statement4868); if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_statement4885); if (state.failed) return;
					}
					break;
				case 15 :
					// src/main/resources/parser/Java.g:957:9: expression ';'
					{
					pushFollow(FOLLOW_expression_in_statement4895);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_statement4898); if (state.failed) return;
					}
					break;
				case 16 :
					// src/main/resources/parser/Java.g:958:9: IDENTIFIER ':' statement
					{
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_statement4913); if (state.failed) return;
					match(input,COLON,FOLLOW_COLON_in_statement4915); if (state.failed) return;
					pushFollow(FOLLOW_statement_in_statement4917);
					statement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 17 :
					// src/main/resources/parser/Java.g:959:9: ';'
					{
					match(input,SEMI,FOLLOW_SEMI_in_statement4927); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 58, statement_StartIndex); }

		}
	}
	// $ANTLR end "statement"



	// $ANTLR start "switchBlockStatementGroups"
	// src/main/resources/parser/Java.g:963:1: switchBlockStatementGroups : ( switchBlockStatementGroup )* ;
	public final void switchBlockStatementGroups() throws RecognitionException {
		int switchBlockStatementGroups_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 59) ) { return; }

			// src/main/resources/parser/Java.g:964:5: ( ( switchBlockStatementGroup )* )
			// src/main/resources/parser/Java.g:964:9: ( switchBlockStatementGroup )*
			{
			// src/main/resources/parser/Java.g:964:9: ( switchBlockStatementGroup )*
			loop99:
			while (true) {
				int alt99=2;
				int LA99_0 = input.LA(1);
				if ( (LA99_0==CASE||LA99_0==DEFAULT) ) {
					alt99=1;
				}

				switch (alt99) {
				case 1 :
					// src/main/resources/parser/Java.g:964:10: switchBlockStatementGroup
					{
					pushFollow(FOLLOW_switchBlockStatementGroup_in_switchBlockStatementGroups4949);
					switchBlockStatementGroup();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop99;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 59, switchBlockStatementGroups_StartIndex); }

		}
	}
	// $ANTLR end "switchBlockStatementGroups"



	// $ANTLR start "switchBlockStatementGroup"
	// src/main/resources/parser/Java.g:967:1: switchBlockStatementGroup : switchLabel ( blockStatement )* ;
	public final void switchBlockStatementGroup() throws RecognitionException {
		int switchBlockStatementGroup_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 60) ) { return; }

			// src/main/resources/parser/Java.g:968:5: ( switchLabel ( blockStatement )* )
			// src/main/resources/parser/Java.g:969:9: switchLabel ( blockStatement )*
			{
			pushFollow(FOLLOW_switchLabel_in_switchBlockStatementGroup4978);
			switchLabel();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:970:9: ( blockStatement )*
			loop100:
			while (true) {
				int alt100=2;
				int LA100_0 = input.LA(1);
				if ( (LA100_0==ABSTRACT||(LA100_0 >= ASSERT && LA100_0 <= BANG)||(LA100_0 >= BOOLEAN && LA100_0 <= BYTE)||(LA100_0 >= CHAR && LA100_0 <= CLASS)||LA100_0==CONTINUE||LA100_0==DO||(LA100_0 >= DOUBLE && LA100_0 <= DOUBLELITERAL)||LA100_0==ENUM||(LA100_0 >= FALSE && LA100_0 <= FINAL)||(LA100_0 >= FLOAT && LA100_0 <= FOR)||(LA100_0 >= IDENTIFIER && LA100_0 <= IF)||(LA100_0 >= INT && LA100_0 <= INTLITERAL)||LA100_0==LBRACE||(LA100_0 >= LONG && LA100_0 <= LT)||(LA100_0 >= MONKEYS_AT && LA100_0 <= NULL)||LA100_0==PLUS||(LA100_0 >= PLUSPLUS && LA100_0 <= PUBLIC)||LA100_0==RETURN||(LA100_0 >= SEMI && LA100_0 <= SHORT)||(LA100_0 >= STATIC && LA100_0 <= SUB)||(LA100_0 >= SUBSUB && LA100_0 <= SYNCHRONIZED)||(LA100_0 >= THIS && LA100_0 <= THROW)||(LA100_0 >= TILDE && LA100_0 <= WHILE)) ) {
					alt100=1;
				}

				switch (alt100) {
				case 1 :
					// src/main/resources/parser/Java.g:970:10: blockStatement
					{
					pushFollow(FOLLOW_blockStatement_in_switchBlockStatementGroup4989);
					blockStatement();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop100;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 60, switchBlockStatementGroup_StartIndex); }

		}
	}
	// $ANTLR end "switchBlockStatementGroup"



	// $ANTLR start "switchLabel"
	// src/main/resources/parser/Java.g:974:1: switchLabel : ( 'case' expression ':' | 'default' ':' );
	public final void switchLabel() throws RecognitionException {
		int switchLabel_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 61) ) { return; }

			// src/main/resources/parser/Java.g:975:5: ( 'case' expression ':' | 'default' ':' )
			int alt101=2;
			int LA101_0 = input.LA(1);
			if ( (LA101_0==CASE) ) {
				alt101=1;
			}
			else if ( (LA101_0==DEFAULT) ) {
				alt101=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 101, 0, input);
				throw nvae;
			}

			switch (alt101) {
				case 1 :
					// src/main/resources/parser/Java.g:975:9: 'case' expression ':'
					{
					match(input,CASE,FOLLOW_CASE_in_switchLabel5020); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_switchLabel5022);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,COLON,FOLLOW_COLON_in_switchLabel5024); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:976:9: 'default' ':'
					{
					match(input,DEFAULT,FOLLOW_DEFAULT_in_switchLabel5034); if (state.failed) return;
					match(input,COLON,FOLLOW_COLON_in_switchLabel5036); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 61, switchLabel_StartIndex); }

		}
	}
	// $ANTLR end "switchLabel"



	// $ANTLR start "trystatement"
	// src/main/resources/parser/Java.g:980:1: trystatement : 'try' block ( catches 'finally' block | catches | 'finally' block ) ;
	public final void trystatement() throws RecognitionException {
		int trystatement_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 62) ) { return; }

			// src/main/resources/parser/Java.g:981:5: ( 'try' block ( catches 'finally' block | catches | 'finally' block ) )
			// src/main/resources/parser/Java.g:981:9: 'try' block ( catches 'finally' block | catches | 'finally' block )
			{
			match(input,TRY,FOLLOW_TRY_in_trystatement5057); if (state.failed) return;
			pushFollow(FOLLOW_block_in_trystatement5059);
			block();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:982:9: ( catches 'finally' block | catches | 'finally' block )
			int alt102=3;
			int LA102_0 = input.LA(1);
			if ( (LA102_0==CATCH) ) {
				int LA102_1 = input.LA(2);
				if ( (synpred153_Java()) ) {
					alt102=1;
				}
				else if ( (synpred154_Java()) ) {
					alt102=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 102, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA102_0==FINALLY) ) {
				alt102=3;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 102, 0, input);
				throw nvae;
			}

			switch (alt102) {
				case 1 :
					// src/main/resources/parser/Java.g:982:13: catches 'finally' block
					{
					pushFollow(FOLLOW_catches_in_trystatement5073);
					catches();
					state._fsp--;
					if (state.failed) return;
					match(input,FINALLY,FOLLOW_FINALLY_in_trystatement5075); if (state.failed) return;
					pushFollow(FOLLOW_block_in_trystatement5077);
					block();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:983:13: catches
					{
					pushFollow(FOLLOW_catches_in_trystatement5091);
					catches();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:984:13: 'finally' block
					{
					match(input,FINALLY,FOLLOW_FINALLY_in_trystatement5105); if (state.failed) return;
					pushFollow(FOLLOW_block_in_trystatement5107);
					block();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 62, trystatement_StartIndex); }

		}
	}
	// $ANTLR end "trystatement"



	// $ANTLR start "catches"
	// src/main/resources/parser/Java.g:988:1: catches : catchClause ( catchClause )* ;
	public final void catches() throws RecognitionException {
		int catches_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 63) ) { return; }

			// src/main/resources/parser/Java.g:989:5: ( catchClause ( catchClause )* )
			// src/main/resources/parser/Java.g:989:9: catchClause ( catchClause )*
			{
			pushFollow(FOLLOW_catchClause_in_catches5138);
			catchClause();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:990:9: ( catchClause )*
			loop103:
			while (true) {
				int alt103=2;
				int LA103_0 = input.LA(1);
				if ( (LA103_0==CATCH) ) {
					alt103=1;
				}

				switch (alt103) {
				case 1 :
					// src/main/resources/parser/Java.g:990:10: catchClause
					{
					pushFollow(FOLLOW_catchClause_in_catches5149);
					catchClause();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop103;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 63, catches_StartIndex); }

		}
	}
	// $ANTLR end "catches"



	// $ANTLR start "catchClause"
	// src/main/resources/parser/Java.g:994:1: catchClause : 'catch' '(' formalParameter ')' block ;
	public final void catchClause() throws RecognitionException {
		int catchClause_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 64) ) { return; }

			// src/main/resources/parser/Java.g:995:5: ( 'catch' '(' formalParameter ')' block )
			// src/main/resources/parser/Java.g:995:9: 'catch' '(' formalParameter ')' block
			{
			match(input,CATCH,FOLLOW_CATCH_in_catchClause5180); if (state.failed) return;
			match(input,LPAREN,FOLLOW_LPAREN_in_catchClause5182); if (state.failed) return;
			pushFollow(FOLLOW_formalParameter_in_catchClause5184);
			formalParameter();
			state._fsp--;
			if (state.failed) return;
			match(input,RPAREN,FOLLOW_RPAREN_in_catchClause5194); if (state.failed) return;
			pushFollow(FOLLOW_block_in_catchClause5196);
			block();
			state._fsp--;
			if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 64, catchClause_StartIndex); }

		}
	}
	// $ANTLR end "catchClause"



	// $ANTLR start "formalParameter"
	// src/main/resources/parser/Java.g:999:1: formalParameter : variableModifiers type IDENTIFIER ( '[' ']' )* ;
	public final void formalParameter() throws RecognitionException {
		int formalParameter_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 65) ) { return; }

			// src/main/resources/parser/Java.g:1000:5: ( variableModifiers type IDENTIFIER ( '[' ']' )* )
			// src/main/resources/parser/Java.g:1000:9: variableModifiers type IDENTIFIER ( '[' ']' )*
			{
			pushFollow(FOLLOW_variableModifiers_in_formalParameter5217);
			variableModifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_formalParameter5219);
			type();
			state._fsp--;
			if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_formalParameter5221); if (state.failed) return;
			// src/main/resources/parser/Java.g:1001:9: ( '[' ']' )*
			loop104:
			while (true) {
				int alt104=2;
				int LA104_0 = input.LA(1);
				if ( (LA104_0==LBRACKET) ) {
					alt104=1;
				}

				switch (alt104) {
				case 1 :
					// src/main/resources/parser/Java.g:1001:10: '[' ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_formalParameter5232); if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_formalParameter5234); if (state.failed) return;
					}
					break;

				default :
					break loop104;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 65, formalParameter_StartIndex); }

		}
	}
	// $ANTLR end "formalParameter"



	// $ANTLR start "forstatement"
	// src/main/resources/parser/Java.g:1005:1: forstatement : ( 'for' '(' variableModifiers type IDENTIFIER ':' expression ')' statement | 'for' '(' ( forInit )? ';' ( expression )? ';' ( expressionList )? ')' statement );
	public final void forstatement() throws RecognitionException {
		int forstatement_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 66) ) { return; }

			// src/main/resources/parser/Java.g:1006:5: ( 'for' '(' variableModifiers type IDENTIFIER ':' expression ')' statement | 'for' '(' ( forInit )? ';' ( expression )? ';' ( expressionList )? ')' statement )
			int alt108=2;
			int LA108_0 = input.LA(1);
			if ( (LA108_0==FOR) ) {
				int LA108_1 = input.LA(2);
				if ( (synpred157_Java()) ) {
					alt108=1;
				}
				else if ( (true) ) {
					alt108=2;
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 108, 0, input);
				throw nvae;
			}

			switch (alt108) {
				case 1 :
					// src/main/resources/parser/Java.g:1008:9: 'for' '(' variableModifiers type IDENTIFIER ':' expression ')' statement
					{
					match(input,FOR,FOLLOW_FOR_in_forstatement5283); if (state.failed) return;
					match(input,LPAREN,FOLLOW_LPAREN_in_forstatement5285); if (state.failed) return;
					pushFollow(FOLLOW_variableModifiers_in_forstatement5287);
					variableModifiers();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_type_in_forstatement5289);
					type();
					state._fsp--;
					if (state.failed) return;
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_forstatement5291); if (state.failed) return;
					match(input,COLON,FOLLOW_COLON_in_forstatement5293); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_forstatement5304);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,RPAREN,FOLLOW_RPAREN_in_forstatement5306); if (state.failed) return;
					pushFollow(FOLLOW_statement_in_forstatement5308);
					statement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1012:9: 'for' '(' ( forInit )? ';' ( expression )? ';' ( expressionList )? ')' statement
					{
					match(input,FOR,FOLLOW_FOR_in_forstatement5340); if (state.failed) return;
					match(input,LPAREN,FOLLOW_LPAREN_in_forstatement5342); if (state.failed) return;
					// src/main/resources/parser/Java.g:1013:17: ( forInit )?
					int alt105=2;
					int LA105_0 = input.LA(1);
					if ( (LA105_0==BANG||LA105_0==BOOLEAN||LA105_0==BYTE||(LA105_0 >= CHAR && LA105_0 <= CHARLITERAL)||(LA105_0 >= DOUBLE && LA105_0 <= DOUBLELITERAL)||(LA105_0 >= FALSE && LA105_0 <= FINAL)||(LA105_0 >= FLOAT && LA105_0 <= FLOATLITERAL)||LA105_0==IDENTIFIER||LA105_0==INT||LA105_0==INTLITERAL||(LA105_0 >= LONG && LA105_0 <= LPAREN)||LA105_0==MONKEYS_AT||(LA105_0 >= NEW && LA105_0 <= NULL)||LA105_0==PLUS||LA105_0==PLUSPLUS||LA105_0==SHORT||(LA105_0 >= STRINGLITERAL && LA105_0 <= SUB)||(LA105_0 >= SUBSUB && LA105_0 <= SUPER)||LA105_0==THIS||LA105_0==TILDE||LA105_0==TRUE||LA105_0==VOID) ) {
						alt105=1;
					}
					switch (alt105) {
						case 1 :
							// src/main/resources/parser/Java.g:1013:18: forInit
							{
							pushFollow(FOLLOW_forInit_in_forstatement5362);
							forInit();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_forstatement5383); if (state.failed) return;
					// src/main/resources/parser/Java.g:1015:17: ( expression )?
					int alt106=2;
					int LA106_0 = input.LA(1);
					if ( (LA106_0==BANG||LA106_0==BOOLEAN||LA106_0==BYTE||(LA106_0 >= CHAR && LA106_0 <= CHARLITERAL)||(LA106_0 >= DOUBLE && LA106_0 <= DOUBLELITERAL)||LA106_0==FALSE||(LA106_0 >= FLOAT && LA106_0 <= FLOATLITERAL)||LA106_0==IDENTIFIER||LA106_0==INT||LA106_0==INTLITERAL||(LA106_0 >= LONG && LA106_0 <= LPAREN)||(LA106_0 >= NEW && LA106_0 <= NULL)||LA106_0==PLUS||LA106_0==PLUSPLUS||LA106_0==SHORT||(LA106_0 >= STRINGLITERAL && LA106_0 <= SUB)||(LA106_0 >= SUBSUB && LA106_0 <= SUPER)||LA106_0==THIS||LA106_0==TILDE||LA106_0==TRUE||LA106_0==VOID) ) {
						alt106=1;
					}
					switch (alt106) {
						case 1 :
							// src/main/resources/parser/Java.g:1015:18: expression
							{
							pushFollow(FOLLOW_expression_in_forstatement5403);
							expression();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_forstatement5424); if (state.failed) return;
					// src/main/resources/parser/Java.g:1017:17: ( expressionList )?
					int alt107=2;
					int LA107_0 = input.LA(1);
					if ( (LA107_0==BANG||LA107_0==BOOLEAN||LA107_0==BYTE||(LA107_0 >= CHAR && LA107_0 <= CHARLITERAL)||(LA107_0 >= DOUBLE && LA107_0 <= DOUBLELITERAL)||LA107_0==FALSE||(LA107_0 >= FLOAT && LA107_0 <= FLOATLITERAL)||LA107_0==IDENTIFIER||LA107_0==INT||LA107_0==INTLITERAL||(LA107_0 >= LONG && LA107_0 <= LPAREN)||(LA107_0 >= NEW && LA107_0 <= NULL)||LA107_0==PLUS||LA107_0==PLUSPLUS||LA107_0==SHORT||(LA107_0 >= STRINGLITERAL && LA107_0 <= SUB)||(LA107_0 >= SUBSUB && LA107_0 <= SUPER)||LA107_0==THIS||LA107_0==TILDE||LA107_0==TRUE||LA107_0==VOID) ) {
						alt107=1;
					}
					switch (alt107) {
						case 1 :
							// src/main/resources/parser/Java.g:1017:18: expressionList
							{
							pushFollow(FOLLOW_expressionList_in_forstatement5444);
							expressionList();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,RPAREN,FOLLOW_RPAREN_in_forstatement5465); if (state.failed) return;
					pushFollow(FOLLOW_statement_in_forstatement5467);
					statement();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 66, forstatement_StartIndex); }

		}
	}
	// $ANTLR end "forstatement"



	// $ANTLR start "forInit"
	// src/main/resources/parser/Java.g:1021:1: forInit : ( localVariableDeclaration | expressionList );
	public final void forInit() throws RecognitionException {
		int forInit_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 67) ) { return; }

			// src/main/resources/parser/Java.g:1022:5: ( localVariableDeclaration | expressionList )
			int alt109=2;
			switch ( input.LA(1) ) {
			case FINAL:
			case MONKEYS_AT:
				{
				alt109=1;
				}
				break;
			case IDENTIFIER:
				{
				int LA109_3 = input.LA(2);
				if ( (synpred161_Java()) ) {
					alt109=1;
				}
				else if ( (true) ) {
					alt109=2;
				}

				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				{
				int LA109_4 = input.LA(2);
				if ( (synpred161_Java()) ) {
					alt109=1;
				}
				else if ( (true) ) {
					alt109=2;
				}

				}
				break;
			case BANG:
			case CHARLITERAL:
			case DOUBLELITERAL:
			case FALSE:
			case FLOATLITERAL:
			case INTLITERAL:
			case LONGLITERAL:
			case LPAREN:
			case NEW:
			case NULL:
			case PLUS:
			case PLUSPLUS:
			case STRINGLITERAL:
			case SUB:
			case SUBSUB:
			case SUPER:
			case THIS:
			case TILDE:
			case TRUE:
			case VOID:
				{
				alt109=2;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 109, 0, input);
				throw nvae;
			}
			switch (alt109) {
				case 1 :
					// src/main/resources/parser/Java.g:1022:9: localVariableDeclaration
					{
					pushFollow(FOLLOW_localVariableDeclaration_in_forInit5487);
					localVariableDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1023:9: expressionList
					{
					pushFollow(FOLLOW_expressionList_in_forInit5497);
					expressionList();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 67, forInit_StartIndex); }

		}
	}
	// $ANTLR end "forInit"



	// $ANTLR start "parExpression"
	// src/main/resources/parser/Java.g:1026:1: parExpression : '(' expression ')' ;
	public final void parExpression() throws RecognitionException {
		int parExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 68) ) { return; }

			// src/main/resources/parser/Java.g:1027:5: ( '(' expression ')' )
			// src/main/resources/parser/Java.g:1027:9: '(' expression ')'
			{
			match(input,LPAREN,FOLLOW_LPAREN_in_parExpression5517); if (state.failed) return;
			pushFollow(FOLLOW_expression_in_parExpression5519);
			expression();
			state._fsp--;
			if (state.failed) return;
			match(input,RPAREN,FOLLOW_RPAREN_in_parExpression5521); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 68, parExpression_StartIndex); }

		}
	}
	// $ANTLR end "parExpression"



	// $ANTLR start "expressionList"
	// src/main/resources/parser/Java.g:1030:1: expressionList : expression ( ',' expression )* ;
	public final void expressionList() throws RecognitionException {
		int expressionList_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 69) ) { return; }

			// src/main/resources/parser/Java.g:1031:5: ( expression ( ',' expression )* )
			// src/main/resources/parser/Java.g:1031:9: expression ( ',' expression )*
			{
			pushFollow(FOLLOW_expression_in_expressionList5541);
			expression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1032:9: ( ',' expression )*
			loop110:
			while (true) {
				int alt110=2;
				int LA110_0 = input.LA(1);
				if ( (LA110_0==COMMA) ) {
					alt110=1;
				}

				switch (alt110) {
				case 1 :
					// src/main/resources/parser/Java.g:1032:10: ',' expression
					{
					match(input,COMMA,FOLLOW_COMMA_in_expressionList5552); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_expressionList5554);
					expression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop110;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 69, expressionList_StartIndex); }

		}
	}
	// $ANTLR end "expressionList"



	// $ANTLR start "expression"
	// src/main/resources/parser/Java.g:1037:1: expression : conditionalExpression ( assignmentOperator expression )? ;
	public final void expression() throws RecognitionException {
		int expression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 70) ) { return; }

			// src/main/resources/parser/Java.g:1038:5: ( conditionalExpression ( assignmentOperator expression )? )
			// src/main/resources/parser/Java.g:1038:9: conditionalExpression ( assignmentOperator expression )?
			{
			pushFollow(FOLLOW_conditionalExpression_in_expression5586);
			conditionalExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1039:9: ( assignmentOperator expression )?
			int alt111=2;
			int LA111_0 = input.LA(1);
			if ( (LA111_0==AMPEQ||LA111_0==BAREQ||LA111_0==CARETEQ||LA111_0==EQ||LA111_0==GT||LA111_0==LT||LA111_0==PERCENTEQ||LA111_0==PLUSEQ||LA111_0==SLASHEQ||LA111_0==STAREQ||LA111_0==SUBEQ) ) {
				alt111=1;
			}
			switch (alt111) {
				case 1 :
					// src/main/resources/parser/Java.g:1039:10: assignmentOperator expression
					{
					pushFollow(FOLLOW_assignmentOperator_in_expression5597);
					assignmentOperator();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_expression_in_expression5599);
					expression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 70, expression_StartIndex); }

		}
	}
	// $ANTLR end "expression"



	// $ANTLR start "assignmentOperator"
	// src/main/resources/parser/Java.g:1044:1: assignmentOperator : ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' | '<' '<' '=' | '>' '>' '>' '=' | '>' '>' '=' );
	public final void assignmentOperator() throws RecognitionException {
		int assignmentOperator_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 71) ) { return; }

			// src/main/resources/parser/Java.g:1045:5: ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' | '<' '<' '=' | '>' '>' '>' '=' | '>' '>' '=' )
			int alt112=12;
			switch ( input.LA(1) ) {
			case EQ:
				{
				alt112=1;
				}
				break;
			case PLUSEQ:
				{
				alt112=2;
				}
				break;
			case SUBEQ:
				{
				alt112=3;
				}
				break;
			case STAREQ:
				{
				alt112=4;
				}
				break;
			case SLASHEQ:
				{
				alt112=5;
				}
				break;
			case AMPEQ:
				{
				alt112=6;
				}
				break;
			case BAREQ:
				{
				alt112=7;
				}
				break;
			case CARETEQ:
				{
				alt112=8;
				}
				break;
			case PERCENTEQ:
				{
				alt112=9;
				}
				break;
			case LT:
				{
				alt112=10;
				}
				break;
			case GT:
				{
				int LA112_11 = input.LA(2);
				if ( (LA112_11==GT) ) {
					int LA112_12 = input.LA(3);
					if ( (LA112_12==GT) ) {
						alt112=11;
					}
					else if ( (LA112_12==EQ) ) {
						alt112=12;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 112, 12, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 112, 11, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 112, 0, input);
				throw nvae;
			}
			switch (alt112) {
				case 1 :
					// src/main/resources/parser/Java.g:1045:9: '='
					{
					match(input,EQ,FOLLOW_EQ_in_assignmentOperator5631); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1046:9: '+='
					{
					match(input,PLUSEQ,FOLLOW_PLUSEQ_in_assignmentOperator5641); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1047:9: '-='
					{
					match(input,SUBEQ,FOLLOW_SUBEQ_in_assignmentOperator5651); if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1048:9: '*='
					{
					match(input,STAREQ,FOLLOW_STAREQ_in_assignmentOperator5661); if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1049:9: '/='
					{
					match(input,SLASHEQ,FOLLOW_SLASHEQ_in_assignmentOperator5671); if (state.failed) return;
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:1050:9: '&='
					{
					match(input,AMPEQ,FOLLOW_AMPEQ_in_assignmentOperator5681); if (state.failed) return;
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:1051:9: '|='
					{
					match(input,BAREQ,FOLLOW_BAREQ_in_assignmentOperator5691); if (state.failed) return;
					}
					break;
				case 8 :
					// src/main/resources/parser/Java.g:1052:9: '^='
					{
					match(input,CARETEQ,FOLLOW_CARETEQ_in_assignmentOperator5701); if (state.failed) return;
					}
					break;
				case 9 :
					// src/main/resources/parser/Java.g:1053:9: '%='
					{
					match(input,PERCENTEQ,FOLLOW_PERCENTEQ_in_assignmentOperator5711); if (state.failed) return;
					}
					break;
				case 10 :
					// src/main/resources/parser/Java.g:1054:10: '<' '<' '='
					{
					match(input,LT,FOLLOW_LT_in_assignmentOperator5722); if (state.failed) return;
					match(input,LT,FOLLOW_LT_in_assignmentOperator5724); if (state.failed) return;
					match(input,EQ,FOLLOW_EQ_in_assignmentOperator5726); if (state.failed) return;
					}
					break;
				case 11 :
					// src/main/resources/parser/Java.g:1055:10: '>' '>' '>' '='
					{
					match(input,GT,FOLLOW_GT_in_assignmentOperator5737); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_assignmentOperator5739); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_assignmentOperator5741); if (state.failed) return;
					match(input,EQ,FOLLOW_EQ_in_assignmentOperator5743); if (state.failed) return;
					}
					break;
				case 12 :
					// src/main/resources/parser/Java.g:1056:10: '>' '>' '='
					{
					match(input,GT,FOLLOW_GT_in_assignmentOperator5754); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_assignmentOperator5756); if (state.failed) return;
					match(input,EQ,FOLLOW_EQ_in_assignmentOperator5758); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 71, assignmentOperator_StartIndex); }

		}
	}
	// $ANTLR end "assignmentOperator"



	// $ANTLR start "conditionalExpression"
	// src/main/resources/parser/Java.g:1060:1: conditionalExpression : conditionalOrExpression ( '?' expression ':' conditionalExpression )? ;
	public final void conditionalExpression() throws RecognitionException {
		int conditionalExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 72) ) { return; }

			// src/main/resources/parser/Java.g:1061:5: ( conditionalOrExpression ( '?' expression ':' conditionalExpression )? )
			// src/main/resources/parser/Java.g:1061:9: conditionalOrExpression ( '?' expression ':' conditionalExpression )?
			{
			pushFollow(FOLLOW_conditionalOrExpression_in_conditionalExpression5779);
			conditionalOrExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1062:9: ( '?' expression ':' conditionalExpression )?
			int alt113=2;
			int LA113_0 = input.LA(1);
			if ( (LA113_0==QUES) ) {
				alt113=1;
			}
			switch (alt113) {
				case 1 :
					// src/main/resources/parser/Java.g:1062:10: '?' expression ':' conditionalExpression
					{
					match(input,QUES,FOLLOW_QUES_in_conditionalExpression5790); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_conditionalExpression5792);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,COLON,FOLLOW_COLON_in_conditionalExpression5794); if (state.failed) return;
					pushFollow(FOLLOW_conditionalExpression_in_conditionalExpression5796);
					conditionalExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 72, conditionalExpression_StartIndex); }

		}
	}
	// $ANTLR end "conditionalExpression"



	// $ANTLR start "conditionalOrExpression"
	// src/main/resources/parser/Java.g:1066:1: conditionalOrExpression : conditionalAndExpression ( '||' conditionalAndExpression )* ;
	public final void conditionalOrExpression() throws RecognitionException {
		int conditionalOrExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 73) ) { return; }

			// src/main/resources/parser/Java.g:1067:5: ( conditionalAndExpression ( '||' conditionalAndExpression )* )
			// src/main/resources/parser/Java.g:1067:9: conditionalAndExpression ( '||' conditionalAndExpression )*
			{
			pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression5827);
			conditionalAndExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1068:9: ( '||' conditionalAndExpression )*
			loop114:
			while (true) {
				int alt114=2;
				int LA114_0 = input.LA(1);
				if ( (LA114_0==BARBAR) ) {
					alt114=1;
				}

				switch (alt114) {
				case 1 :
					// src/main/resources/parser/Java.g:1068:10: '||' conditionalAndExpression
					{
					match(input,BARBAR,FOLLOW_BARBAR_in_conditionalOrExpression5838); if (state.failed) return;
					pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression5840);
					conditionalAndExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop114;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 73, conditionalOrExpression_StartIndex); }

		}
	}
	// $ANTLR end "conditionalOrExpression"



	// $ANTLR start "conditionalAndExpression"
	// src/main/resources/parser/Java.g:1072:1: conditionalAndExpression : inclusiveOrExpression ( '&&' inclusiveOrExpression )* ;
	public final void conditionalAndExpression() throws RecognitionException {
		int conditionalAndExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 74) ) { return; }

			// src/main/resources/parser/Java.g:1073:5: ( inclusiveOrExpression ( '&&' inclusiveOrExpression )* )
			// src/main/resources/parser/Java.g:1073:9: inclusiveOrExpression ( '&&' inclusiveOrExpression )*
			{
			pushFollow(FOLLOW_inclusiveOrExpression_in_conditionalAndExpression5871);
			inclusiveOrExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1074:9: ( '&&' inclusiveOrExpression )*
			loop115:
			while (true) {
				int alt115=2;
				int LA115_0 = input.LA(1);
				if ( (LA115_0==AMPAMP) ) {
					alt115=1;
				}

				switch (alt115) {
				case 1 :
					// src/main/resources/parser/Java.g:1074:10: '&&' inclusiveOrExpression
					{
					match(input,AMPAMP,FOLLOW_AMPAMP_in_conditionalAndExpression5882); if (state.failed) return;
					pushFollow(FOLLOW_inclusiveOrExpression_in_conditionalAndExpression5884);
					inclusiveOrExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop115;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 74, conditionalAndExpression_StartIndex); }

		}
	}
	// $ANTLR end "conditionalAndExpression"



	// $ANTLR start "inclusiveOrExpression"
	// src/main/resources/parser/Java.g:1078:1: inclusiveOrExpression : exclusiveOrExpression ( '|' exclusiveOrExpression )* ;
	public final void inclusiveOrExpression() throws RecognitionException {
		int inclusiveOrExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 75) ) { return; }

			// src/main/resources/parser/Java.g:1079:5: ( exclusiveOrExpression ( '|' exclusiveOrExpression )* )
			// src/main/resources/parser/Java.g:1079:9: exclusiveOrExpression ( '|' exclusiveOrExpression )*
			{
			pushFollow(FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression5915);
			exclusiveOrExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1080:9: ( '|' exclusiveOrExpression )*
			loop116:
			while (true) {
				int alt116=2;
				int LA116_0 = input.LA(1);
				if ( (LA116_0==BAR) ) {
					alt116=1;
				}

				switch (alt116) {
				case 1 :
					// src/main/resources/parser/Java.g:1080:10: '|' exclusiveOrExpression
					{
					match(input,BAR,FOLLOW_BAR_in_inclusiveOrExpression5926); if (state.failed) return;
					pushFollow(FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression5928);
					exclusiveOrExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop116;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 75, inclusiveOrExpression_StartIndex); }

		}
	}
	// $ANTLR end "inclusiveOrExpression"



	// $ANTLR start "exclusiveOrExpression"
	// src/main/resources/parser/Java.g:1084:1: exclusiveOrExpression : andExpression ( '^' andExpression )* ;
	public final void exclusiveOrExpression() throws RecognitionException {
		int exclusiveOrExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 76) ) { return; }

			// src/main/resources/parser/Java.g:1085:5: ( andExpression ( '^' andExpression )* )
			// src/main/resources/parser/Java.g:1085:9: andExpression ( '^' andExpression )*
			{
			pushFollow(FOLLOW_andExpression_in_exclusiveOrExpression5959);
			andExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1086:9: ( '^' andExpression )*
			loop117:
			while (true) {
				int alt117=2;
				int LA117_0 = input.LA(1);
				if ( (LA117_0==CARET) ) {
					alt117=1;
				}

				switch (alt117) {
				case 1 :
					// src/main/resources/parser/Java.g:1086:10: '^' andExpression
					{
					match(input,CARET,FOLLOW_CARET_in_exclusiveOrExpression5970); if (state.failed) return;
					pushFollow(FOLLOW_andExpression_in_exclusiveOrExpression5972);
					andExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop117;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 76, exclusiveOrExpression_StartIndex); }

		}
	}
	// $ANTLR end "exclusiveOrExpression"



	// $ANTLR start "andExpression"
	// src/main/resources/parser/Java.g:1090:1: andExpression : equalityExpression ( '&' equalityExpression )* ;
	public final void andExpression() throws RecognitionException {
		int andExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 77) ) { return; }

			// src/main/resources/parser/Java.g:1091:5: ( equalityExpression ( '&' equalityExpression )* )
			// src/main/resources/parser/Java.g:1091:9: equalityExpression ( '&' equalityExpression )*
			{
			pushFollow(FOLLOW_equalityExpression_in_andExpression6003);
			equalityExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1092:9: ( '&' equalityExpression )*
			loop118:
			while (true) {
				int alt118=2;
				int LA118_0 = input.LA(1);
				if ( (LA118_0==AMP) ) {
					alt118=1;
				}

				switch (alt118) {
				case 1 :
					// src/main/resources/parser/Java.g:1092:10: '&' equalityExpression
					{
					match(input,AMP,FOLLOW_AMP_in_andExpression6014); if (state.failed) return;
					pushFollow(FOLLOW_equalityExpression_in_andExpression6016);
					equalityExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop118;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 77, andExpression_StartIndex); }

		}
	}
	// $ANTLR end "andExpression"



	// $ANTLR start "equalityExpression"
	// src/main/resources/parser/Java.g:1096:1: equalityExpression : instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )* ;
	public final void equalityExpression() throws RecognitionException {
		int equalityExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 78) ) { return; }

			// src/main/resources/parser/Java.g:1097:5: ( instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )* )
			// src/main/resources/parser/Java.g:1097:9: instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )*
			{
			pushFollow(FOLLOW_instanceOfExpression_in_equalityExpression6047);
			instanceOfExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1098:9: ( ( '==' | '!=' ) instanceOfExpression )*
			loop119:
			while (true) {
				int alt119=2;
				int LA119_0 = input.LA(1);
				if ( (LA119_0==BANGEQ||LA119_0==EQEQ) ) {
					alt119=1;
				}

				switch (alt119) {
				case 1 :
					// src/main/resources/parser/Java.g:1099:13: ( '==' | '!=' ) instanceOfExpression
					{
					if ( input.LA(1)==BANGEQ||input.LA(1)==EQEQ ) {
						input.consume();
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_instanceOfExpression_in_equalityExpression6124);
					instanceOfExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop119;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 78, equalityExpression_StartIndex); }

		}
	}
	// $ANTLR end "equalityExpression"



	// $ANTLR start "instanceOfExpression"
	// src/main/resources/parser/Java.g:1106:1: instanceOfExpression : relationalExpression ( 'instanceof' type )? ;
	public final void instanceOfExpression() throws RecognitionException {
		int instanceOfExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 79) ) { return; }

			// src/main/resources/parser/Java.g:1107:5: ( relationalExpression ( 'instanceof' type )? )
			// src/main/resources/parser/Java.g:1107:9: relationalExpression ( 'instanceof' type )?
			{
			pushFollow(FOLLOW_relationalExpression_in_instanceOfExpression6155);
			relationalExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1108:9: ( 'instanceof' type )?
			int alt120=2;
			int LA120_0 = input.LA(1);
			if ( (LA120_0==INSTANCEOF) ) {
				alt120=1;
			}
			switch (alt120) {
				case 1 :
					// src/main/resources/parser/Java.g:1108:10: 'instanceof' type
					{
					match(input,INSTANCEOF,FOLLOW_INSTANCEOF_in_instanceOfExpression6166); if (state.failed) return;
					pushFollow(FOLLOW_type_in_instanceOfExpression6168);
					type();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 79, instanceOfExpression_StartIndex); }

		}
	}
	// $ANTLR end "instanceOfExpression"



	// $ANTLR start "relationalExpression"
	// src/main/resources/parser/Java.g:1112:1: relationalExpression : shiftExpression ( relationalOp shiftExpression )* ;
	public final void relationalExpression() throws RecognitionException {
		int relationalExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 80) ) { return; }

			// src/main/resources/parser/Java.g:1113:5: ( shiftExpression ( relationalOp shiftExpression )* )
			// src/main/resources/parser/Java.g:1113:9: shiftExpression ( relationalOp shiftExpression )*
			{
			pushFollow(FOLLOW_shiftExpression_in_relationalExpression6199);
			shiftExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1114:9: ( relationalOp shiftExpression )*
			loop121:
			while (true) {
				int alt121=2;
				int LA121_0 = input.LA(1);
				if ( (LA121_0==LT) ) {
					int LA121_2 = input.LA(2);
					if ( (LA121_2==BANG||LA121_2==BOOLEAN||LA121_2==BYTE||(LA121_2 >= CHAR && LA121_2 <= CHARLITERAL)||(LA121_2 >= DOUBLE && LA121_2 <= DOUBLELITERAL)||LA121_2==EQ||LA121_2==FALSE||(LA121_2 >= FLOAT && LA121_2 <= FLOATLITERAL)||LA121_2==IDENTIFIER||LA121_2==INT||LA121_2==INTLITERAL||(LA121_2 >= LONG && LA121_2 <= LPAREN)||(LA121_2 >= NEW && LA121_2 <= NULL)||LA121_2==PLUS||LA121_2==PLUSPLUS||LA121_2==SHORT||(LA121_2 >= STRINGLITERAL && LA121_2 <= SUB)||(LA121_2 >= SUBSUB && LA121_2 <= SUPER)||LA121_2==THIS||LA121_2==TILDE||LA121_2==TRUE||LA121_2==VOID) ) {
						alt121=1;
					}

				}
				else if ( (LA121_0==GT) ) {
					int LA121_3 = input.LA(2);
					if ( (LA121_3==BANG||LA121_3==BOOLEAN||LA121_3==BYTE||(LA121_3 >= CHAR && LA121_3 <= CHARLITERAL)||(LA121_3 >= DOUBLE && LA121_3 <= DOUBLELITERAL)||LA121_3==EQ||LA121_3==FALSE||(LA121_3 >= FLOAT && LA121_3 <= FLOATLITERAL)||LA121_3==IDENTIFIER||LA121_3==INT||LA121_3==INTLITERAL||(LA121_3 >= LONG && LA121_3 <= LPAREN)||(LA121_3 >= NEW && LA121_3 <= NULL)||LA121_3==PLUS||LA121_3==PLUSPLUS||LA121_3==SHORT||(LA121_3 >= STRINGLITERAL && LA121_3 <= SUB)||(LA121_3 >= SUBSUB && LA121_3 <= SUPER)||LA121_3==THIS||LA121_3==TILDE||LA121_3==TRUE||LA121_3==VOID) ) {
						alt121=1;
					}

				}

				switch (alt121) {
				case 1 :
					// src/main/resources/parser/Java.g:1114:10: relationalOp shiftExpression
					{
					pushFollow(FOLLOW_relationalOp_in_relationalExpression6210);
					relationalOp();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_shiftExpression_in_relationalExpression6212);
					shiftExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop121;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 80, relationalExpression_StartIndex); }

		}
	}
	// $ANTLR end "relationalExpression"



	// $ANTLR start "relationalOp"
	// src/main/resources/parser/Java.g:1118:1: relationalOp : ( '<' '=' | '>' '=' | '<' | '>' );
	public final void relationalOp() throws RecognitionException {
		int relationalOp_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 81) ) { return; }

			// src/main/resources/parser/Java.g:1119:5: ( '<' '=' | '>' '=' | '<' | '>' )
			int alt122=4;
			int LA122_0 = input.LA(1);
			if ( (LA122_0==LT) ) {
				int LA122_1 = input.LA(2);
				if ( (LA122_1==EQ) ) {
					alt122=1;
				}
				else if ( (LA122_1==BANG||LA122_1==BOOLEAN||LA122_1==BYTE||(LA122_1 >= CHAR && LA122_1 <= CHARLITERAL)||(LA122_1 >= DOUBLE && LA122_1 <= DOUBLELITERAL)||LA122_1==FALSE||(LA122_1 >= FLOAT && LA122_1 <= FLOATLITERAL)||LA122_1==IDENTIFIER||LA122_1==INT||LA122_1==INTLITERAL||(LA122_1 >= LONG && LA122_1 <= LPAREN)||(LA122_1 >= NEW && LA122_1 <= NULL)||LA122_1==PLUS||LA122_1==PLUSPLUS||LA122_1==SHORT||(LA122_1 >= STRINGLITERAL && LA122_1 <= SUB)||(LA122_1 >= SUBSUB && LA122_1 <= SUPER)||LA122_1==THIS||LA122_1==TILDE||LA122_1==TRUE||LA122_1==VOID) ) {
					alt122=3;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 122, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA122_0==GT) ) {
				int LA122_2 = input.LA(2);
				if ( (LA122_2==EQ) ) {
					alt122=2;
				}
				else if ( (LA122_2==BANG||LA122_2==BOOLEAN||LA122_2==BYTE||(LA122_2 >= CHAR && LA122_2 <= CHARLITERAL)||(LA122_2 >= DOUBLE && LA122_2 <= DOUBLELITERAL)||LA122_2==FALSE||(LA122_2 >= FLOAT && LA122_2 <= FLOATLITERAL)||LA122_2==IDENTIFIER||LA122_2==INT||LA122_2==INTLITERAL||(LA122_2 >= LONG && LA122_2 <= LPAREN)||(LA122_2 >= NEW && LA122_2 <= NULL)||LA122_2==PLUS||LA122_2==PLUSPLUS||LA122_2==SHORT||(LA122_2 >= STRINGLITERAL && LA122_2 <= SUB)||(LA122_2 >= SUBSUB && LA122_2 <= SUPER)||LA122_2==THIS||LA122_2==TILDE||LA122_2==TRUE||LA122_2==VOID) ) {
					alt122=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 122, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 122, 0, input);
				throw nvae;
			}

			switch (alt122) {
				case 1 :
					// src/main/resources/parser/Java.g:1119:10: '<' '='
					{
					match(input,LT,FOLLOW_LT_in_relationalOp6244); if (state.failed) return;
					match(input,EQ,FOLLOW_EQ_in_relationalOp6246); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1120:10: '>' '='
					{
					match(input,GT,FOLLOW_GT_in_relationalOp6257); if (state.failed) return;
					match(input,EQ,FOLLOW_EQ_in_relationalOp6259); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1121:9: '<'
					{
					match(input,LT,FOLLOW_LT_in_relationalOp6269); if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1122:9: '>'
					{
					match(input,GT,FOLLOW_GT_in_relationalOp6279); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 81, relationalOp_StartIndex); }

		}
	}
	// $ANTLR end "relationalOp"



	// $ANTLR start "shiftExpression"
	// src/main/resources/parser/Java.g:1125:1: shiftExpression : additiveExpression ( shiftOp additiveExpression )* ;
	public final void shiftExpression() throws RecognitionException {
		int shiftExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 82) ) { return; }

			// src/main/resources/parser/Java.g:1126:5: ( additiveExpression ( shiftOp additiveExpression )* )
			// src/main/resources/parser/Java.g:1126:9: additiveExpression ( shiftOp additiveExpression )*
			{
			pushFollow(FOLLOW_additiveExpression_in_shiftExpression6299);
			additiveExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1127:9: ( shiftOp additiveExpression )*
			loop123:
			while (true) {
				int alt123=2;
				int LA123_0 = input.LA(1);
				if ( (LA123_0==LT) ) {
					int LA123_1 = input.LA(2);
					if ( (LA123_1==LT) ) {
						int LA123_4 = input.LA(3);
						if ( (LA123_4==BANG||LA123_4==BOOLEAN||LA123_4==BYTE||(LA123_4 >= CHAR && LA123_4 <= CHARLITERAL)||(LA123_4 >= DOUBLE && LA123_4 <= DOUBLELITERAL)||LA123_4==FALSE||(LA123_4 >= FLOAT && LA123_4 <= FLOATLITERAL)||LA123_4==IDENTIFIER||LA123_4==INT||LA123_4==INTLITERAL||(LA123_4 >= LONG && LA123_4 <= LPAREN)||(LA123_4 >= NEW && LA123_4 <= NULL)||LA123_4==PLUS||LA123_4==PLUSPLUS||LA123_4==SHORT||(LA123_4 >= STRINGLITERAL && LA123_4 <= SUB)||(LA123_4 >= SUBSUB && LA123_4 <= SUPER)||LA123_4==THIS||LA123_4==TILDE||LA123_4==TRUE||LA123_4==VOID) ) {
							alt123=1;
						}

					}

				}
				else if ( (LA123_0==GT) ) {
					int LA123_2 = input.LA(2);
					if ( (LA123_2==GT) ) {
						int LA123_5 = input.LA(3);
						if ( (LA123_5==GT) ) {
							int LA123_7 = input.LA(4);
							if ( (LA123_7==BANG||LA123_7==BOOLEAN||LA123_7==BYTE||(LA123_7 >= CHAR && LA123_7 <= CHARLITERAL)||(LA123_7 >= DOUBLE && LA123_7 <= DOUBLELITERAL)||LA123_7==FALSE||(LA123_7 >= FLOAT && LA123_7 <= FLOATLITERAL)||LA123_7==IDENTIFIER||LA123_7==INT||LA123_7==INTLITERAL||(LA123_7 >= LONG && LA123_7 <= LPAREN)||(LA123_7 >= NEW && LA123_7 <= NULL)||LA123_7==PLUS||LA123_7==PLUSPLUS||LA123_7==SHORT||(LA123_7 >= STRINGLITERAL && LA123_7 <= SUB)||(LA123_7 >= SUBSUB && LA123_7 <= SUPER)||LA123_7==THIS||LA123_7==TILDE||LA123_7==TRUE||LA123_7==VOID) ) {
								alt123=1;
							}

						}
						else if ( (LA123_5==BANG||LA123_5==BOOLEAN||LA123_5==BYTE||(LA123_5 >= CHAR && LA123_5 <= CHARLITERAL)||(LA123_5 >= DOUBLE && LA123_5 <= DOUBLELITERAL)||LA123_5==FALSE||(LA123_5 >= FLOAT && LA123_5 <= FLOATLITERAL)||LA123_5==IDENTIFIER||LA123_5==INT||LA123_5==INTLITERAL||(LA123_5 >= LONG && LA123_5 <= LPAREN)||(LA123_5 >= NEW && LA123_5 <= NULL)||LA123_5==PLUS||LA123_5==PLUSPLUS||LA123_5==SHORT||(LA123_5 >= STRINGLITERAL && LA123_5 <= SUB)||(LA123_5 >= SUBSUB && LA123_5 <= SUPER)||LA123_5==THIS||LA123_5==TILDE||LA123_5==TRUE||LA123_5==VOID) ) {
							alt123=1;
						}

					}

				}

				switch (alt123) {
				case 1 :
					// src/main/resources/parser/Java.g:1127:10: shiftOp additiveExpression
					{
					pushFollow(FOLLOW_shiftOp_in_shiftExpression6310);
					shiftOp();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_additiveExpression_in_shiftExpression6312);
					additiveExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop123;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 82, shiftExpression_StartIndex); }

		}
	}
	// $ANTLR end "shiftExpression"



	// $ANTLR start "shiftOp"
	// src/main/resources/parser/Java.g:1132:1: shiftOp : ( '<' '<' | '>' '>' '>' | '>' '>' );
	public final void shiftOp() throws RecognitionException {
		int shiftOp_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 83) ) { return; }

			// src/main/resources/parser/Java.g:1133:5: ( '<' '<' | '>' '>' '>' | '>' '>' )
			int alt124=3;
			int LA124_0 = input.LA(1);
			if ( (LA124_0==LT) ) {
				alt124=1;
			}
			else if ( (LA124_0==GT) ) {
				int LA124_2 = input.LA(2);
				if ( (LA124_2==GT) ) {
					int LA124_3 = input.LA(3);
					if ( (LA124_3==GT) ) {
						alt124=2;
					}
					else if ( (LA124_3==BANG||LA124_3==BOOLEAN||LA124_3==BYTE||(LA124_3 >= CHAR && LA124_3 <= CHARLITERAL)||(LA124_3 >= DOUBLE && LA124_3 <= DOUBLELITERAL)||LA124_3==FALSE||(LA124_3 >= FLOAT && LA124_3 <= FLOATLITERAL)||LA124_3==IDENTIFIER||LA124_3==INT||LA124_3==INTLITERAL||(LA124_3 >= LONG && LA124_3 <= LPAREN)||(LA124_3 >= NEW && LA124_3 <= NULL)||LA124_3==PLUS||LA124_3==PLUSPLUS||LA124_3==SHORT||(LA124_3 >= STRINGLITERAL && LA124_3 <= SUB)||(LA124_3 >= SUBSUB && LA124_3 <= SUPER)||LA124_3==THIS||LA124_3==TILDE||LA124_3==TRUE||LA124_3==VOID) ) {
						alt124=3;
					}

					else {
						if (state.backtracking>0) {state.failed=true; return;}
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 124, 3, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 124, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 124, 0, input);
				throw nvae;
			}

			switch (alt124) {
				case 1 :
					// src/main/resources/parser/Java.g:1133:10: '<' '<'
					{
					match(input,LT,FOLLOW_LT_in_shiftOp6345); if (state.failed) return;
					match(input,LT,FOLLOW_LT_in_shiftOp6347); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1134:10: '>' '>' '>'
					{
					match(input,GT,FOLLOW_GT_in_shiftOp6358); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_shiftOp6360); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_shiftOp6362); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1135:10: '>' '>'
					{
					match(input,GT,FOLLOW_GT_in_shiftOp6373); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_shiftOp6375); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 83, shiftOp_StartIndex); }

		}
	}
	// $ANTLR end "shiftOp"



	// $ANTLR start "additiveExpression"
	// src/main/resources/parser/Java.g:1139:1: additiveExpression : multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )* ;
	public final void additiveExpression() throws RecognitionException {
		int additiveExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 84) ) { return; }

			// src/main/resources/parser/Java.g:1140:5: ( multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )* )
			// src/main/resources/parser/Java.g:1140:9: multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )*
			{
			pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression6396);
			multiplicativeExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1141:9: ( ( '+' | '-' ) multiplicativeExpression )*
			loop125:
			while (true) {
				int alt125=2;
				int LA125_0 = input.LA(1);
				if ( (LA125_0==PLUS||LA125_0==SUB) ) {
					alt125=1;
				}

				switch (alt125) {
				case 1 :
					// src/main/resources/parser/Java.g:1142:13: ( '+' | '-' ) multiplicativeExpression
					{
					if ( input.LA(1)==PLUS||input.LA(1)==SUB ) {
						input.consume();
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression6473);
					multiplicativeExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop125;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 84, additiveExpression_StartIndex); }

		}
	}
	// $ANTLR end "additiveExpression"



	// $ANTLR start "multiplicativeExpression"
	// src/main/resources/parser/Java.g:1149:1: multiplicativeExpression : unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )* ;
	public final void multiplicativeExpression() throws RecognitionException {
		int multiplicativeExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 85) ) { return; }

			// src/main/resources/parser/Java.g:1150:5: ( unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )* )
			// src/main/resources/parser/Java.g:1151:9: unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )*
			{
			pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression6511);
			unaryExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1152:9: ( ( '*' | '/' | '%' ) unaryExpression )*
			loop126:
			while (true) {
				int alt126=2;
				int LA126_0 = input.LA(1);
				if ( (LA126_0==PERCENT||LA126_0==SLASH||LA126_0==STAR) ) {
					alt126=1;
				}

				switch (alt126) {
				case 1 :
					// src/main/resources/parser/Java.g:1153:13: ( '*' | '/' | '%' ) unaryExpression
					{
					if ( input.LA(1)==PERCENT||input.LA(1)==SLASH||input.LA(1)==STAR ) {
						input.consume();
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression6606);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop126;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 85, multiplicativeExpression_StartIndex); }

		}
	}
	// $ANTLR end "multiplicativeExpression"



	// $ANTLR start "unaryExpression"
	// src/main/resources/parser/Java.g:1165:1: unaryExpression : ( '+' unaryExpression | '-' unaryExpression | '++' unaryExpression | '--' unaryExpression | unaryExpressionNotPlusMinus );
	public final void unaryExpression() throws RecognitionException {
		int unaryExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 86) ) { return; }

			// src/main/resources/parser/Java.g:1166:5: ( '+' unaryExpression | '-' unaryExpression | '++' unaryExpression | '--' unaryExpression | unaryExpressionNotPlusMinus )
			int alt127=5;
			switch ( input.LA(1) ) {
			case PLUS:
				{
				alt127=1;
				}
				break;
			case SUB:
				{
				alt127=2;
				}
				break;
			case PLUSPLUS:
				{
				alt127=3;
				}
				break;
			case SUBSUB:
				{
				alt127=4;
				}
				break;
			case BANG:
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case CHARLITERAL:
			case DOUBLE:
			case DOUBLELITERAL:
			case FALSE:
			case FLOAT:
			case FLOATLITERAL:
			case IDENTIFIER:
			case INT:
			case INTLITERAL:
			case LONG:
			case LONGLITERAL:
			case LPAREN:
			case NEW:
			case NULL:
			case SHORT:
			case STRINGLITERAL:
			case SUPER:
			case THIS:
			case TILDE:
			case TRUE:
			case VOID:
				{
				alt127=5;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 127, 0, input);
				throw nvae;
			}
			switch (alt127) {
				case 1 :
					// src/main/resources/parser/Java.g:1166:9: '+' unaryExpression
					{
					match(input,PLUS,FOLLOW_PLUS_in_unaryExpression6639); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpression6642);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1167:9: '-' unaryExpression
					{
					match(input,SUB,FOLLOW_SUB_in_unaryExpression6652); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpression6654);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1168:9: '++' unaryExpression
					{
					match(input,PLUSPLUS,FOLLOW_PLUSPLUS_in_unaryExpression6664); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpression6666);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1169:9: '--' unaryExpression
					{
					match(input,SUBSUB,FOLLOW_SUBSUB_in_unaryExpression6676); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpression6678);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1170:9: unaryExpressionNotPlusMinus
					{
					pushFollow(FOLLOW_unaryExpressionNotPlusMinus_in_unaryExpression6688);
					unaryExpressionNotPlusMinus();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 86, unaryExpression_StartIndex); }

		}
	}
	// $ANTLR end "unaryExpression"



	// $ANTLR start "unaryExpressionNotPlusMinus"
	// src/main/resources/parser/Java.g:1173:1: unaryExpressionNotPlusMinus : ( '~' unaryExpression | '!' unaryExpression | castExpression | primary ( selector )* ( '++' | '--' )? );
	public final void unaryExpressionNotPlusMinus() throws RecognitionException {
		int unaryExpressionNotPlusMinus_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 87) ) { return; }

			// src/main/resources/parser/Java.g:1174:5: ( '~' unaryExpression | '!' unaryExpression | castExpression | primary ( selector )* ( '++' | '--' )? )
			int alt130=4;
			switch ( input.LA(1) ) {
			case TILDE:
				{
				alt130=1;
				}
				break;
			case BANG:
				{
				alt130=2;
				}
				break;
			case LPAREN:
				{
				int LA130_3 = input.LA(2);
				if ( (synpred202_Java()) ) {
					alt130=3;
				}
				else if ( (true) ) {
					alt130=4;
				}

				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case CHARLITERAL:
			case DOUBLE:
			case DOUBLELITERAL:
			case FALSE:
			case FLOAT:
			case FLOATLITERAL:
			case IDENTIFIER:
			case INT:
			case INTLITERAL:
			case LONG:
			case LONGLITERAL:
			case NEW:
			case NULL:
			case SHORT:
			case STRINGLITERAL:
			case SUPER:
			case THIS:
			case TRUE:
			case VOID:
				{
				alt130=4;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 130, 0, input);
				throw nvae;
			}
			switch (alt130) {
				case 1 :
					// src/main/resources/parser/Java.g:1174:9: '~' unaryExpression
					{
					match(input,TILDE,FOLLOW_TILDE_in_unaryExpressionNotPlusMinus6708); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus6710);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1175:9: '!' unaryExpression
					{
					match(input,BANG,FOLLOW_BANG_in_unaryExpressionNotPlusMinus6720); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus6722);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1176:9: castExpression
					{
					pushFollow(FOLLOW_castExpression_in_unaryExpressionNotPlusMinus6732);
					castExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1177:9: primary ( selector )* ( '++' | '--' )?
					{
					pushFollow(FOLLOW_primary_in_unaryExpressionNotPlusMinus6742);
					primary();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:1178:9: ( selector )*
					loop128:
					while (true) {
						int alt128=2;
						int LA128_0 = input.LA(1);
						if ( (LA128_0==DOT||LA128_0==LBRACKET) ) {
							alt128=1;
						}

						switch (alt128) {
						case 1 :
							// src/main/resources/parser/Java.g:1178:10: selector
							{
							pushFollow(FOLLOW_selector_in_unaryExpressionNotPlusMinus6753);
							selector();
							state._fsp--;
							if (state.failed) return;
							}
							break;

						default :
							break loop128;
						}
					}

					// src/main/resources/parser/Java.g:1180:9: ( '++' | '--' )?
					int alt129=2;
					int LA129_0 = input.LA(1);
					if ( (LA129_0==PLUSPLUS||LA129_0==SUBSUB) ) {
						alt129=1;
					}
					switch (alt129) {
						case 1 :
							// src/main/resources/parser/Java.g:
							{
							if ( input.LA(1)==PLUSPLUS||input.LA(1)==SUBSUB ) {
								input.consume();
								state.errorRecovery=false;
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								throw mse;
							}
							}
							break;

					}

					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 87, unaryExpressionNotPlusMinus_StartIndex); }

		}
	}
	// $ANTLR end "unaryExpressionNotPlusMinus"



	// $ANTLR start "castExpression"
	// src/main/resources/parser/Java.g:1185:1: castExpression : ( '(' primitiveType ')' unaryExpression | '(' type ')' unaryExpressionNotPlusMinus );
	public final void castExpression() throws RecognitionException {
		int castExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 88) ) { return; }

			// src/main/resources/parser/Java.g:1186:5: ( '(' primitiveType ')' unaryExpression | '(' type ')' unaryExpressionNotPlusMinus )
			int alt131=2;
			int LA131_0 = input.LA(1);
			if ( (LA131_0==LPAREN) ) {
				int LA131_1 = input.LA(2);
				if ( (synpred206_Java()) ) {
					alt131=1;
				}
				else if ( (true) ) {
					alt131=2;
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 131, 0, input);
				throw nvae;
			}

			switch (alt131) {
				case 1 :
					// src/main/resources/parser/Java.g:1186:9: '(' primitiveType ')' unaryExpression
					{
					match(input,LPAREN,FOLLOW_LPAREN_in_castExpression6823); if (state.failed) return;
					pushFollow(FOLLOW_primitiveType_in_castExpression6825);
					primitiveType();
					state._fsp--;
					if (state.failed) return;
					match(input,RPAREN,FOLLOW_RPAREN_in_castExpression6827); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_castExpression6829);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1187:9: '(' type ')' unaryExpressionNotPlusMinus
					{
					match(input,LPAREN,FOLLOW_LPAREN_in_castExpression6839); if (state.failed) return;
					pushFollow(FOLLOW_type_in_castExpression6841);
					type();
					state._fsp--;
					if (state.failed) return;
					match(input,RPAREN,FOLLOW_RPAREN_in_castExpression6843); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpressionNotPlusMinus_in_castExpression6845);
					unaryExpressionNotPlusMinus();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 88, castExpression_StartIndex); }

		}
	}
	// $ANTLR end "castExpression"



	// $ANTLR start "primary"
	// src/main/resources/parser/Java.g:1193:1: primary : ( parExpression | 'this' ( '.' IDENTIFIER )* ( identifierSuffix )? | IDENTIFIER ( '.' IDENTIFIER )* ( identifierSuffix )? | 'super' superSuffix | literal | creator | primitiveType ( '[' ']' )* '.' 'class' | 'void' '.' 'class' );
	public final void primary() throws RecognitionException {
		int primary_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 89) ) { return; }

			// src/main/resources/parser/Java.g:1194:5: ( parExpression | 'this' ( '.' IDENTIFIER )* ( identifierSuffix )? | IDENTIFIER ( '.' IDENTIFIER )* ( identifierSuffix )? | 'super' superSuffix | literal | creator | primitiveType ( '[' ']' )* '.' 'class' | 'void' '.' 'class' )
			int alt137=8;
			switch ( input.LA(1) ) {
			case LPAREN:
				{
				alt137=1;
				}
				break;
			case THIS:
				{
				alt137=2;
				}
				break;
			case IDENTIFIER:
				{
				alt137=3;
				}
				break;
			case SUPER:
				{
				alt137=4;
				}
				break;
			case CHARLITERAL:
			case DOUBLELITERAL:
			case FALSE:
			case FLOATLITERAL:
			case INTLITERAL:
			case LONGLITERAL:
			case NULL:
			case STRINGLITERAL:
			case TRUE:
				{
				alt137=5;
				}
				break;
			case NEW:
				{
				alt137=6;
				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				{
				alt137=7;
				}
				break;
			case VOID:
				{
				alt137=8;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 137, 0, input);
				throw nvae;
			}
			switch (alt137) {
				case 1 :
					// src/main/resources/parser/Java.g:1194:9: parExpression
					{
					pushFollow(FOLLOW_parExpression_in_primary6867);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1195:9: 'this' ( '.' IDENTIFIER )* ( identifierSuffix )?
					{
					match(input,THIS,FOLLOW_THIS_in_primary6889); if (state.failed) return;
					// src/main/resources/parser/Java.g:1196:9: ( '.' IDENTIFIER )*
					loop132:
					while (true) {
						int alt132=2;
						int LA132_0 = input.LA(1);
						if ( (LA132_0==DOT) ) {
							int LA132_2 = input.LA(2);
							if ( (LA132_2==IDENTIFIER) ) {
								int LA132_3 = input.LA(3);
								if ( (synpred208_Java()) ) {
									alt132=1;
								}

							}

						}

						switch (alt132) {
						case 1 :
							// src/main/resources/parser/Java.g:1196:10: '.' IDENTIFIER
							{
							match(input,DOT,FOLLOW_DOT_in_primary6900); if (state.failed) return;
							match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_primary6902); if (state.failed) return;
							}
							break;

						default :
							break loop132;
						}
					}

					// src/main/resources/parser/Java.g:1198:9: ( identifierSuffix )?
					int alt133=2;
					switch ( input.LA(1) ) {
						case LBRACKET:
							{
							int LA133_1 = input.LA(2);
							if ( (synpred209_Java()) ) {
								alt133=1;
							}
							}
							break;
						case LPAREN:
							{
							alt133=1;
							}
							break;
						case DOT:
							{
							int LA133_3 = input.LA(2);
							if ( (synpred209_Java()) ) {
								alt133=1;
							}
							}
							break;
					}
					switch (alt133) {
						case 1 :
							// src/main/resources/parser/Java.g:1198:10: identifierSuffix
							{
							pushFollow(FOLLOW_identifierSuffix_in_primary6924);
							identifierSuffix();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1200:9: IDENTIFIER ( '.' IDENTIFIER )* ( identifierSuffix )?
					{
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_primary6945); if (state.failed) return;
					// src/main/resources/parser/Java.g:1201:9: ( '.' IDENTIFIER )*
					loop134:
					while (true) {
						int alt134=2;
						int LA134_0 = input.LA(1);
						if ( (LA134_0==DOT) ) {
							int LA134_2 = input.LA(2);
							if ( (LA134_2==IDENTIFIER) ) {
								int LA134_3 = input.LA(3);
								if ( (synpred211_Java()) ) {
									alt134=1;
								}

							}

						}

						switch (alt134) {
						case 1 :
							// src/main/resources/parser/Java.g:1201:10: '.' IDENTIFIER
							{
							match(input,DOT,FOLLOW_DOT_in_primary6956); if (state.failed) return;
							match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_primary6958); if (state.failed) return;
							}
							break;

						default :
							break loop134;
						}
					}

					// src/main/resources/parser/Java.g:1203:9: ( identifierSuffix )?
					int alt135=2;
					switch ( input.LA(1) ) {
						case LBRACKET:
							{
							int LA135_1 = input.LA(2);
							if ( (synpred212_Java()) ) {
								alt135=1;
							}
							}
							break;
						case LPAREN:
							{
							alt135=1;
							}
							break;
						case DOT:
							{
							int LA135_3 = input.LA(2);
							if ( (synpred212_Java()) ) {
								alt135=1;
							}
							}
							break;
					}
					switch (alt135) {
						case 1 :
							// src/main/resources/parser/Java.g:1203:10: identifierSuffix
							{
							pushFollow(FOLLOW_identifierSuffix_in_primary6980);
							identifierSuffix();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1205:9: 'super' superSuffix
					{
					match(input,SUPER,FOLLOW_SUPER_in_primary7001); if (state.failed) return;
					pushFollow(FOLLOW_superSuffix_in_primary7011);
					superSuffix();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1207:9: literal
					{
					pushFollow(FOLLOW_literal_in_primary7021);
					literal();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:1208:9: creator
					{
					pushFollow(FOLLOW_creator_in_primary7031);
					creator();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:1209:9: primitiveType ( '[' ']' )* '.' 'class'
					{
					pushFollow(FOLLOW_primitiveType_in_primary7041);
					primitiveType();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:1210:9: ( '[' ']' )*
					loop136:
					while (true) {
						int alt136=2;
						int LA136_0 = input.LA(1);
						if ( (LA136_0==LBRACKET) ) {
							alt136=1;
						}

						switch (alt136) {
						case 1 :
							// src/main/resources/parser/Java.g:1210:10: '[' ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_primary7052); if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_primary7054); if (state.failed) return;
							}
							break;

						default :
							break loop136;
						}
					}

					match(input,DOT,FOLLOW_DOT_in_primary7075); if (state.failed) return;
					match(input,CLASS,FOLLOW_CLASS_in_primary7077); if (state.failed) return;
					}
					break;
				case 8 :
					// src/main/resources/parser/Java.g:1213:9: 'void' '.' 'class'
					{
					match(input,VOID,FOLLOW_VOID_in_primary7087); if (state.failed) return;
					match(input,DOT,FOLLOW_DOT_in_primary7089); if (state.failed) return;
					match(input,CLASS,FOLLOW_CLASS_in_primary7091); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 89, primary_StartIndex); }

		}
	}
	// $ANTLR end "primary"



	// $ANTLR start "superSuffix"
	// src/main/resources/parser/Java.g:1217:1: superSuffix : ( arguments | '.' ( typeArguments )? IDENTIFIER ( arguments )? );
	public final void superSuffix() throws RecognitionException {
		int superSuffix_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 90) ) { return; }

			// src/main/resources/parser/Java.g:1218:5: ( arguments | '.' ( typeArguments )? IDENTIFIER ( arguments )? )
			int alt140=2;
			int LA140_0 = input.LA(1);
			if ( (LA140_0==LPAREN) ) {
				alt140=1;
			}
			else if ( (LA140_0==DOT) ) {
				alt140=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 140, 0, input);
				throw nvae;
			}

			switch (alt140) {
				case 1 :
					// src/main/resources/parser/Java.g:1218:9: arguments
					{
					pushFollow(FOLLOW_arguments_in_superSuffix7117);
					arguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1219:9: '.' ( typeArguments )? IDENTIFIER ( arguments )?
					{
					match(input,DOT,FOLLOW_DOT_in_superSuffix7127); if (state.failed) return;
					// src/main/resources/parser/Java.g:1219:13: ( typeArguments )?
					int alt138=2;
					int LA138_0 = input.LA(1);
					if ( (LA138_0==LT) ) {
						alt138=1;
					}
					switch (alt138) {
						case 1 :
							// src/main/resources/parser/Java.g:1219:14: typeArguments
							{
							pushFollow(FOLLOW_typeArguments_in_superSuffix7130);
							typeArguments();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_superSuffix7151); if (state.failed) return;
					// src/main/resources/parser/Java.g:1222:9: ( arguments )?
					int alt139=2;
					int LA139_0 = input.LA(1);
					if ( (LA139_0==LPAREN) ) {
						alt139=1;
					}
					switch (alt139) {
						case 1 :
							// src/main/resources/parser/Java.g:1222:10: arguments
							{
							pushFollow(FOLLOW_arguments_in_superSuffix7162);
							arguments();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 90, superSuffix_StartIndex); }

		}
	}
	// $ANTLR end "superSuffix"



	// $ANTLR start "identifierSuffix"
	// src/main/resources/parser/Java.g:1227:1: identifierSuffix : ( ( '[' ']' )+ '.' 'class' | ( '[' expression ']' )+ | arguments | '.' 'class' | '.' nonWildcardTypeArguments IDENTIFIER arguments | '.' 'this' | '.' 'super' arguments | innerCreator );
	public final void identifierSuffix() throws RecognitionException {
		int identifierSuffix_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 91) ) { return; }

			// src/main/resources/parser/Java.g:1228:5: ( ( '[' ']' )+ '.' 'class' | ( '[' expression ']' )+ | arguments | '.' 'class' | '.' nonWildcardTypeArguments IDENTIFIER arguments | '.' 'this' | '.' 'super' arguments | innerCreator )
			int alt143=8;
			switch ( input.LA(1) ) {
			case LBRACKET:
				{
				int LA143_1 = input.LA(2);
				if ( (LA143_1==RBRACKET) ) {
					alt143=1;
				}
				else if ( (LA143_1==BANG||LA143_1==BOOLEAN||LA143_1==BYTE||(LA143_1 >= CHAR && LA143_1 <= CHARLITERAL)||(LA143_1 >= DOUBLE && LA143_1 <= DOUBLELITERAL)||LA143_1==FALSE||(LA143_1 >= FLOAT && LA143_1 <= FLOATLITERAL)||LA143_1==IDENTIFIER||LA143_1==INT||LA143_1==INTLITERAL||(LA143_1 >= LONG && LA143_1 <= LPAREN)||(LA143_1 >= NEW && LA143_1 <= NULL)||LA143_1==PLUS||LA143_1==PLUSPLUS||LA143_1==SHORT||(LA143_1 >= STRINGLITERAL && LA143_1 <= SUB)||(LA143_1 >= SUBSUB && LA143_1 <= SUPER)||LA143_1==THIS||LA143_1==TILDE||LA143_1==TRUE||LA143_1==VOID) ) {
					alt143=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 143, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case LPAREN:
				{
				alt143=3;
				}
				break;
			case DOT:
				{
				switch ( input.LA(2) ) {
				case CLASS:
					{
					alt143=4;
					}
					break;
				case THIS:
					{
					alt143=6;
					}
					break;
				case SUPER:
					{
					alt143=7;
					}
					break;
				case NEW:
					{
					alt143=8;
					}
					break;
				case LT:
					{
					alt143=5;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 143, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 143, 0, input);
				throw nvae;
			}
			switch (alt143) {
				case 1 :
					// src/main/resources/parser/Java.g:1228:9: ( '[' ']' )+ '.' 'class'
					{
					// src/main/resources/parser/Java.g:1228:9: ( '[' ']' )+
					int cnt141=0;
					loop141:
					while (true) {
						int alt141=2;
						int LA141_0 = input.LA(1);
						if ( (LA141_0==LBRACKET) ) {
							alt141=1;
						}

						switch (alt141) {
						case 1 :
							// src/main/resources/parser/Java.g:1228:10: '[' ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_identifierSuffix7195); if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_identifierSuffix7197); if (state.failed) return;
							}
							break;

						default :
							if ( cnt141 >= 1 ) break loop141;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(141, input);
							throw eee;
						}
						cnt141++;
					}

					match(input,DOT,FOLLOW_DOT_in_identifierSuffix7218); if (state.failed) return;
					match(input,CLASS,FOLLOW_CLASS_in_identifierSuffix7220); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1231:9: ( '[' expression ']' )+
					{
					// src/main/resources/parser/Java.g:1231:9: ( '[' expression ']' )+
					int cnt142=0;
					loop142:
					while (true) {
						int alt142=2;
						int LA142_0 = input.LA(1);
						if ( (LA142_0==LBRACKET) ) {
							int LA142_2 = input.LA(2);
							if ( (synpred224_Java()) ) {
								alt142=1;
							}

						}

						switch (alt142) {
						case 1 :
							// src/main/resources/parser/Java.g:1231:10: '[' expression ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_identifierSuffix7231); if (state.failed) return;
							pushFollow(FOLLOW_expression_in_identifierSuffix7233);
							expression();
							state._fsp--;
							if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_identifierSuffix7235); if (state.failed) return;
							}
							break;

						default :
							if ( cnt142 >= 1 ) break loop142;
							if (state.backtracking>0) {state.failed=true; return;}
							EarlyExitException eee = new EarlyExitException(142, input);
							throw eee;
						}
						cnt142++;
					}

					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1233:9: arguments
					{
					pushFollow(FOLLOW_arguments_in_identifierSuffix7256);
					arguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1234:9: '.' 'class'
					{
					match(input,DOT,FOLLOW_DOT_in_identifierSuffix7266); if (state.failed) return;
					match(input,CLASS,FOLLOW_CLASS_in_identifierSuffix7268); if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1235:9: '.' nonWildcardTypeArguments IDENTIFIER arguments
					{
					match(input,DOT,FOLLOW_DOT_in_identifierSuffix7278); if (state.failed) return;
					pushFollow(FOLLOW_nonWildcardTypeArguments_in_identifierSuffix7280);
					nonWildcardTypeArguments();
					state._fsp--;
					if (state.failed) return;
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_identifierSuffix7282); if (state.failed) return;
					pushFollow(FOLLOW_arguments_in_identifierSuffix7284);
					arguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:1236:9: '.' 'this'
					{
					match(input,DOT,FOLLOW_DOT_in_identifierSuffix7294); if (state.failed) return;
					match(input,THIS,FOLLOW_THIS_in_identifierSuffix7296); if (state.failed) return;
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:1237:9: '.' 'super' arguments
					{
					match(input,DOT,FOLLOW_DOT_in_identifierSuffix7306); if (state.failed) return;
					match(input,SUPER,FOLLOW_SUPER_in_identifierSuffix7308); if (state.failed) return;
					pushFollow(FOLLOW_arguments_in_identifierSuffix7310);
					arguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 8 :
					// src/main/resources/parser/Java.g:1238:9: innerCreator
					{
					pushFollow(FOLLOW_innerCreator_in_identifierSuffix7320);
					innerCreator();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 91, identifierSuffix_StartIndex); }

		}
	}
	// $ANTLR end "identifierSuffix"



	// $ANTLR start "selector"
	// src/main/resources/parser/Java.g:1242:1: selector : ( '.' IDENTIFIER ( arguments )? | '.' 'this' | '.' 'super' superSuffix | innerCreator | '[' expression ']' );
	public final void selector() throws RecognitionException {
		int selector_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 92) ) { return; }

			// src/main/resources/parser/Java.g:1243:5: ( '.' IDENTIFIER ( arguments )? | '.' 'this' | '.' 'super' superSuffix | innerCreator | '[' expression ']' )
			int alt145=5;
			int LA145_0 = input.LA(1);
			if ( (LA145_0==DOT) ) {
				switch ( input.LA(2) ) {
				case IDENTIFIER:
					{
					alt145=1;
					}
					break;
				case THIS:
					{
					alt145=2;
					}
					break;
				case SUPER:
					{
					alt145=3;
					}
					break;
				case NEW:
					{
					alt145=4;
					}
					break;
				default:
					if (state.backtracking>0) {state.failed=true; return;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 145, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}
			}
			else if ( (LA145_0==LBRACKET) ) {
				alt145=5;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 145, 0, input);
				throw nvae;
			}

			switch (alt145) {
				case 1 :
					// src/main/resources/parser/Java.g:1243:9: '.' IDENTIFIER ( arguments )?
					{
					match(input,DOT,FOLLOW_DOT_in_selector7342); if (state.failed) return;
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_selector7344); if (state.failed) return;
					// src/main/resources/parser/Java.g:1244:9: ( arguments )?
					int alt144=2;
					int LA144_0 = input.LA(1);
					if ( (LA144_0==LPAREN) ) {
						alt144=1;
					}
					switch (alt144) {
						case 1 :
							// src/main/resources/parser/Java.g:1244:10: arguments
							{
							pushFollow(FOLLOW_arguments_in_selector7355);
							arguments();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1246:9: '.' 'this'
					{
					match(input,DOT,FOLLOW_DOT_in_selector7376); if (state.failed) return;
					match(input,THIS,FOLLOW_THIS_in_selector7378); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1247:9: '.' 'super' superSuffix
					{
					match(input,DOT,FOLLOW_DOT_in_selector7388); if (state.failed) return;
					match(input,SUPER,FOLLOW_SUPER_in_selector7390); if (state.failed) return;
					pushFollow(FOLLOW_superSuffix_in_selector7400);
					superSuffix();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1249:9: innerCreator
					{
					pushFollow(FOLLOW_innerCreator_in_selector7410);
					innerCreator();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1250:9: '[' expression ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_selector7420); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_selector7422);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_selector7424); if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 92, selector_StartIndex); }

		}
	}
	// $ANTLR end "selector"



	// $ANTLR start "creator"
	// src/main/resources/parser/Java.g:1253:1: creator : ( 'new' nonWildcardTypeArguments classOrInterfaceType classCreatorRest | 'new' classOrInterfaceType classCreatorRest | arrayCreator );
	public final void creator() throws RecognitionException {
		int creator_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 93) ) { return; }

			// src/main/resources/parser/Java.g:1254:5: ( 'new' nonWildcardTypeArguments classOrInterfaceType classCreatorRest | 'new' classOrInterfaceType classCreatorRest | arrayCreator )
			int alt146=3;
			int LA146_0 = input.LA(1);
			if ( (LA146_0==NEW) ) {
				int LA146_1 = input.LA(2);
				if ( (synpred236_Java()) ) {
					alt146=1;
				}
				else if ( (synpred237_Java()) ) {
					alt146=2;
				}
				else if ( (true) ) {
					alt146=3;
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 146, 0, input);
				throw nvae;
			}

			switch (alt146) {
				case 1 :
					// src/main/resources/parser/Java.g:1254:9: 'new' nonWildcardTypeArguments classOrInterfaceType classCreatorRest
					{
					match(input,NEW,FOLLOW_NEW_in_creator7444); if (state.failed) return;
					pushFollow(FOLLOW_nonWildcardTypeArguments_in_creator7446);
					nonWildcardTypeArguments();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_classOrInterfaceType_in_creator7448);
					classOrInterfaceType();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_classCreatorRest_in_creator7450);
					classCreatorRest();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1255:9: 'new' classOrInterfaceType classCreatorRest
					{
					match(input,NEW,FOLLOW_NEW_in_creator7460); if (state.failed) return;
					pushFollow(FOLLOW_classOrInterfaceType_in_creator7462);
					classOrInterfaceType();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_classCreatorRest_in_creator7464);
					classCreatorRest();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1256:9: arrayCreator
					{
					pushFollow(FOLLOW_arrayCreator_in_creator7474);
					arrayCreator();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 93, creator_StartIndex); }

		}
	}
	// $ANTLR end "creator"



	// $ANTLR start "arrayCreator"
	// src/main/resources/parser/Java.g:1259:1: arrayCreator : ( 'new' createdName '[' ']' ( '[' ']' )* arrayInitializer | 'new' createdName '[' expression ']' ( '[' expression ']' )* ( '[' ']' )* );
	public final void arrayCreator() throws RecognitionException {
		int arrayCreator_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 94) ) { return; }

			// src/main/resources/parser/Java.g:1260:5: ( 'new' createdName '[' ']' ( '[' ']' )* arrayInitializer | 'new' createdName '[' expression ']' ( '[' expression ']' )* ( '[' ']' )* )
			int alt150=2;
			int LA150_0 = input.LA(1);
			if ( (LA150_0==NEW) ) {
				int LA150_1 = input.LA(2);
				if ( (synpred239_Java()) ) {
					alt150=1;
				}
				else if ( (true) ) {
					alt150=2;
				}

			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 150, 0, input);
				throw nvae;
			}

			switch (alt150) {
				case 1 :
					// src/main/resources/parser/Java.g:1260:9: 'new' createdName '[' ']' ( '[' ']' )* arrayInitializer
					{
					match(input,NEW,FOLLOW_NEW_in_arrayCreator7494); if (state.failed) return;
					pushFollow(FOLLOW_createdName_in_arrayCreator7496);
					createdName();
					state._fsp--;
					if (state.failed) return;
					match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayCreator7506); if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayCreator7508); if (state.failed) return;
					// src/main/resources/parser/Java.g:1262:9: ( '[' ']' )*
					loop147:
					while (true) {
						int alt147=2;
						int LA147_0 = input.LA(1);
						if ( (LA147_0==LBRACKET) ) {
							alt147=1;
						}

						switch (alt147) {
						case 1 :
							// src/main/resources/parser/Java.g:1262:10: '[' ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayCreator7519); if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayCreator7521); if (state.failed) return;
							}
							break;

						default :
							break loop147;
						}
					}

					pushFollow(FOLLOW_arrayInitializer_in_arrayCreator7542);
					arrayInitializer();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1266:9: 'new' createdName '[' expression ']' ( '[' expression ']' )* ( '[' ']' )*
					{
					match(input,NEW,FOLLOW_NEW_in_arrayCreator7553); if (state.failed) return;
					pushFollow(FOLLOW_createdName_in_arrayCreator7555);
					createdName();
					state._fsp--;
					if (state.failed) return;
					match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayCreator7565); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_arrayCreator7567);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayCreator7577); if (state.failed) return;
					// src/main/resources/parser/Java.g:1269:9: ( '[' expression ']' )*
					loop148:
					while (true) {
						int alt148=2;
						int LA148_0 = input.LA(1);
						if ( (LA148_0==LBRACKET) ) {
							int LA148_1 = input.LA(2);
							if ( (synpred240_Java()) ) {
								alt148=1;
							}

						}

						switch (alt148) {
						case 1 :
							// src/main/resources/parser/Java.g:1269:13: '[' expression ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayCreator7591); if (state.failed) return;
							pushFollow(FOLLOW_expression_in_arrayCreator7593);
							expression();
							state._fsp--;
							if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayCreator7607); if (state.failed) return;
							}
							break;

						default :
							break loop148;
						}
					}

					// src/main/resources/parser/Java.g:1272:9: ( '[' ']' )*
					loop149:
					while (true) {
						int alt149=2;
						int LA149_0 = input.LA(1);
						if ( (LA149_0==LBRACKET) ) {
							int LA149_2 = input.LA(2);
							if ( (LA149_2==RBRACKET) ) {
								alt149=1;
							}

						}

						switch (alt149) {
						case 1 :
							// src/main/resources/parser/Java.g:1272:10: '[' ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayCreator7629); if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayCreator7631); if (state.failed) return;
							}
							break;

						default :
							break loop149;
						}
					}

					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 94, arrayCreator_StartIndex); }

		}
	}
	// $ANTLR end "arrayCreator"


	public static class variableInitializer_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "variableInitializer"
	// src/main/resources/parser/Java.g:1276:1: variableInitializer : ( arrayInitializer | expression );
	public final JavaParser.variableInitializer_return variableInitializer() throws RecognitionException {
		JavaParser.variableInitializer_return retval = new JavaParser.variableInitializer_return();
		retval.start = input.LT(1);
		int variableInitializer_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 95) ) { return retval; }

			// src/main/resources/parser/Java.g:1277:5: ( arrayInitializer | expression )
			int alt151=2;
			int LA151_0 = input.LA(1);
			if ( (LA151_0==LBRACE) ) {
				alt151=1;
			}
			else if ( (LA151_0==BANG||LA151_0==BOOLEAN||LA151_0==BYTE||(LA151_0 >= CHAR && LA151_0 <= CHARLITERAL)||(LA151_0 >= DOUBLE && LA151_0 <= DOUBLELITERAL)||LA151_0==FALSE||(LA151_0 >= FLOAT && LA151_0 <= FLOATLITERAL)||LA151_0==IDENTIFIER||LA151_0==INT||LA151_0==INTLITERAL||(LA151_0 >= LONG && LA151_0 <= LPAREN)||(LA151_0 >= NEW && LA151_0 <= NULL)||LA151_0==PLUS||LA151_0==PLUSPLUS||LA151_0==SHORT||(LA151_0 >= STRINGLITERAL && LA151_0 <= SUB)||(LA151_0 >= SUBSUB && LA151_0 <= SUPER)||LA151_0==THIS||LA151_0==TILDE||LA151_0==TRUE||LA151_0==VOID) ) {
				alt151=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 151, 0, input);
				throw nvae;
			}

			switch (alt151) {
				case 1 :
					// src/main/resources/parser/Java.g:1277:9: arrayInitializer
					{
					pushFollow(FOLLOW_arrayInitializer_in_variableInitializer7662);
					arrayInitializer();
					state._fsp--;
					if (state.failed) return retval;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1278:9: expression
					{
					pushFollow(FOLLOW_expression_in_variableInitializer7672);
					expression();
					state._fsp--;
					if (state.failed) return retval;
					}
					break;

			}
			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 95, variableInitializer_StartIndex); }

		}
		return retval;
	}
	// $ANTLR end "variableInitializer"



	// $ANTLR start "arrayInitializer"
	// src/main/resources/parser/Java.g:1281:1: arrayInitializer : '{' ( variableInitializer ( ',' variableInitializer )* )? ( ',' )? '}' ;
	public final void arrayInitializer() throws RecognitionException {
		int arrayInitializer_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 96) ) { return; }

			// src/main/resources/parser/Java.g:1282:5: ( '{' ( variableInitializer ( ',' variableInitializer )* )? ( ',' )? '}' )
			// src/main/resources/parser/Java.g:1282:9: '{' ( variableInitializer ( ',' variableInitializer )* )? ( ',' )? '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_arrayInitializer7692); if (state.failed) return;
			// src/main/resources/parser/Java.g:1283:13: ( variableInitializer ( ',' variableInitializer )* )?
			int alt153=2;
			int LA153_0 = input.LA(1);
			if ( (LA153_0==BANG||LA153_0==BOOLEAN||LA153_0==BYTE||(LA153_0 >= CHAR && LA153_0 <= CHARLITERAL)||(LA153_0 >= DOUBLE && LA153_0 <= DOUBLELITERAL)||LA153_0==FALSE||(LA153_0 >= FLOAT && LA153_0 <= FLOATLITERAL)||LA153_0==IDENTIFIER||LA153_0==INT||LA153_0==INTLITERAL||LA153_0==LBRACE||(LA153_0 >= LONG && LA153_0 <= LPAREN)||(LA153_0 >= NEW && LA153_0 <= NULL)||LA153_0==PLUS||LA153_0==PLUSPLUS||LA153_0==SHORT||(LA153_0 >= STRINGLITERAL && LA153_0 <= SUB)||(LA153_0 >= SUBSUB && LA153_0 <= SUPER)||LA153_0==THIS||LA153_0==TILDE||LA153_0==TRUE||LA153_0==VOID) ) {
				alt153=1;
			}
			switch (alt153) {
				case 1 :
					// src/main/resources/parser/Java.g:1283:14: variableInitializer ( ',' variableInitializer )*
					{
					pushFollow(FOLLOW_variableInitializer_in_arrayInitializer7708);
					variableInitializer();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:1284:17: ( ',' variableInitializer )*
					loop152:
					while (true) {
						int alt152=2;
						int LA152_0 = input.LA(1);
						if ( (LA152_0==COMMA) ) {
							int LA152_1 = input.LA(2);
							if ( (LA152_1==BANG||LA152_1==BOOLEAN||LA152_1==BYTE||(LA152_1 >= CHAR && LA152_1 <= CHARLITERAL)||(LA152_1 >= DOUBLE && LA152_1 <= DOUBLELITERAL)||LA152_1==FALSE||(LA152_1 >= FLOAT && LA152_1 <= FLOATLITERAL)||LA152_1==IDENTIFIER||LA152_1==INT||LA152_1==INTLITERAL||LA152_1==LBRACE||(LA152_1 >= LONG && LA152_1 <= LPAREN)||(LA152_1 >= NEW && LA152_1 <= NULL)||LA152_1==PLUS||LA152_1==PLUSPLUS||LA152_1==SHORT||(LA152_1 >= STRINGLITERAL && LA152_1 <= SUB)||(LA152_1 >= SUBSUB && LA152_1 <= SUPER)||LA152_1==THIS||LA152_1==TILDE||LA152_1==TRUE||LA152_1==VOID) ) {
								alt152=1;
							}

						}

						switch (alt152) {
						case 1 :
							// src/main/resources/parser/Java.g:1284:18: ',' variableInitializer
							{
							match(input,COMMA,FOLLOW_COMMA_in_arrayInitializer7727); if (state.failed) return;
							pushFollow(FOLLOW_variableInitializer_in_arrayInitializer7729);
							variableInitializer();
							state._fsp--;
							if (state.failed) return;
							}
							break;

						default :
							break loop152;
						}
					}

					}
					break;

			}

			// src/main/resources/parser/Java.g:1287:13: ( ',' )?
			int alt154=2;
			int LA154_0 = input.LA(1);
			if ( (LA154_0==COMMA) ) {
				alt154=1;
			}
			switch (alt154) {
				case 1 :
					// src/main/resources/parser/Java.g:1287:14: ','
					{
					match(input,COMMA,FOLLOW_COMMA_in_arrayInitializer7779); if (state.failed) return;
					}
					break;

			}

			match(input,RBRACE,FOLLOW_RBRACE_in_arrayInitializer7792); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 96, arrayInitializer_StartIndex); }

		}
	}
	// $ANTLR end "arrayInitializer"



	// $ANTLR start "createdName"
	// src/main/resources/parser/Java.g:1292:1: createdName : ( classOrInterfaceType | primitiveType );
	public final void createdName() throws RecognitionException {
		int createdName_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 97) ) { return; }

			// src/main/resources/parser/Java.g:1293:5: ( classOrInterfaceType | primitiveType )
			int alt155=2;
			int LA155_0 = input.LA(1);
			if ( (LA155_0==IDENTIFIER) ) {
				alt155=1;
			}
			else if ( (LA155_0==BOOLEAN||LA155_0==BYTE||LA155_0==CHAR||LA155_0==DOUBLE||LA155_0==FLOAT||LA155_0==INT||LA155_0==LONG||LA155_0==SHORT) ) {
				alt155=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 155, 0, input);
				throw nvae;
			}

			switch (alt155) {
				case 1 :
					// src/main/resources/parser/Java.g:1293:9: classOrInterfaceType
					{
					pushFollow(FOLLOW_classOrInterfaceType_in_createdName7826);
					classOrInterfaceType();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1294:9: primitiveType
					{
					pushFollow(FOLLOW_primitiveType_in_createdName7836);
					primitiveType();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 97, createdName_StartIndex); }

		}
	}
	// $ANTLR end "createdName"



	// $ANTLR start "innerCreator"
	// src/main/resources/parser/Java.g:1297:1: innerCreator : '.' 'new' ( nonWildcardTypeArguments )? IDENTIFIER ( typeArguments )? classCreatorRest ;
	public final void innerCreator() throws RecognitionException {
		int innerCreator_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 98) ) { return; }

			// src/main/resources/parser/Java.g:1298:5: ( '.' 'new' ( nonWildcardTypeArguments )? IDENTIFIER ( typeArguments )? classCreatorRest )
			// src/main/resources/parser/Java.g:1298:9: '.' 'new' ( nonWildcardTypeArguments )? IDENTIFIER ( typeArguments )? classCreatorRest
			{
			match(input,DOT,FOLLOW_DOT_in_innerCreator7857); if (state.failed) return;
			match(input,NEW,FOLLOW_NEW_in_innerCreator7859); if (state.failed) return;
			// src/main/resources/parser/Java.g:1299:9: ( nonWildcardTypeArguments )?
			int alt156=2;
			int LA156_0 = input.LA(1);
			if ( (LA156_0==LT) ) {
				alt156=1;
			}
			switch (alt156) {
				case 1 :
					// src/main/resources/parser/Java.g:1299:10: nonWildcardTypeArguments
					{
					pushFollow(FOLLOW_nonWildcardTypeArguments_in_innerCreator7870);
					nonWildcardTypeArguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_innerCreator7891); if (state.failed) return;
			// src/main/resources/parser/Java.g:1302:9: ( typeArguments )?
			int alt157=2;
			int LA157_0 = input.LA(1);
			if ( (LA157_0==LT) ) {
				alt157=1;
			}
			switch (alt157) {
				case 1 :
					// src/main/resources/parser/Java.g:1302:10: typeArguments
					{
					pushFollow(FOLLOW_typeArguments_in_innerCreator7902);
					typeArguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			pushFollow(FOLLOW_classCreatorRest_in_innerCreator7923);
			classCreatorRest();
			state._fsp--;
			if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 98, innerCreator_StartIndex); }

		}
	}
	// $ANTLR end "innerCreator"



	// $ANTLR start "classCreatorRest"
	// src/main/resources/parser/Java.g:1308:1: classCreatorRest : arguments ( classBody )? ;
	public final void classCreatorRest() throws RecognitionException {
		int classCreatorRest_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 99) ) { return; }

			// src/main/resources/parser/Java.g:1309:5: ( arguments ( classBody )? )
			// src/main/resources/parser/Java.g:1309:9: arguments ( classBody )?
			{
			pushFollow(FOLLOW_arguments_in_classCreatorRest7944);
			arguments();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1310:9: ( classBody )?
			int alt158=2;
			int LA158_0 = input.LA(1);
			if ( (LA158_0==LBRACE) ) {
				alt158=1;
			}
			switch (alt158) {
				case 1 :
					// src/main/resources/parser/Java.g:1310:10: classBody
					{
					pushFollow(FOLLOW_classBody_in_classCreatorRest7955);
					classBody();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 99, classCreatorRest_StartIndex); }

		}
	}
	// $ANTLR end "classCreatorRest"



	// $ANTLR start "nonWildcardTypeArguments"
	// src/main/resources/parser/Java.g:1315:1: nonWildcardTypeArguments : '<' typeList '>' ;
	public final void nonWildcardTypeArguments() throws RecognitionException {
		int nonWildcardTypeArguments_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 100) ) { return; }

			// src/main/resources/parser/Java.g:1316:5: ( '<' typeList '>' )
			// src/main/resources/parser/Java.g:1316:9: '<' typeList '>'
			{
			match(input,LT,FOLLOW_LT_in_nonWildcardTypeArguments7987); if (state.failed) return;
			pushFollow(FOLLOW_typeList_in_nonWildcardTypeArguments7989);
			typeList();
			state._fsp--;
			if (state.failed) return;
			match(input,GT,FOLLOW_GT_in_nonWildcardTypeArguments7999); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 100, nonWildcardTypeArguments_StartIndex); }

		}
	}
	// $ANTLR end "nonWildcardTypeArguments"



	// $ANTLR start "arguments"
	// src/main/resources/parser/Java.g:1320:1: arguments : '(' ( expressionList )? ')' ;
	public final void arguments() throws RecognitionException {
		int arguments_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 101) ) { return; }

			// src/main/resources/parser/Java.g:1321:5: ( '(' ( expressionList )? ')' )
			// src/main/resources/parser/Java.g:1321:9: '(' ( expressionList )? ')'
			{
			match(input,LPAREN,FOLLOW_LPAREN_in_arguments8019); if (state.failed) return;
			// src/main/resources/parser/Java.g:1321:13: ( expressionList )?
			int alt159=2;
			int LA159_0 = input.LA(1);
			if ( (LA159_0==BANG||LA159_0==BOOLEAN||LA159_0==BYTE||(LA159_0 >= CHAR && LA159_0 <= CHARLITERAL)||(LA159_0 >= DOUBLE && LA159_0 <= DOUBLELITERAL)||LA159_0==FALSE||(LA159_0 >= FLOAT && LA159_0 <= FLOATLITERAL)||LA159_0==IDENTIFIER||LA159_0==INT||LA159_0==INTLITERAL||(LA159_0 >= LONG && LA159_0 <= LPAREN)||(LA159_0 >= NEW && LA159_0 <= NULL)||LA159_0==PLUS||LA159_0==PLUSPLUS||LA159_0==SHORT||(LA159_0 >= STRINGLITERAL && LA159_0 <= SUB)||(LA159_0 >= SUBSUB && LA159_0 <= SUPER)||LA159_0==THIS||LA159_0==TILDE||LA159_0==TRUE||LA159_0==VOID) ) {
				alt159=1;
			}
			switch (alt159) {
				case 1 :
					// src/main/resources/parser/Java.g:1321:14: expressionList
					{
					pushFollow(FOLLOW_expressionList_in_arguments8022);
					expressionList();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,RPAREN,FOLLOW_RPAREN_in_arguments8035); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 101, arguments_StartIndex); }

		}
	}
	// $ANTLR end "arguments"



	// $ANTLR start "literal"
	// src/main/resources/parser/Java.g:1325:1: literal : ( INTLITERAL | LONGLITERAL | FLOATLITERAL | DOUBLELITERAL | CHARLITERAL | STRINGLITERAL | TRUE | FALSE | NULL );
	public final void literal() throws RecognitionException {
		int literal_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 102) ) { return; }

			// src/main/resources/parser/Java.g:1326:5: ( INTLITERAL | LONGLITERAL | FLOATLITERAL | DOUBLELITERAL | CHARLITERAL | STRINGLITERAL | TRUE | FALSE | NULL )
			// src/main/resources/parser/Java.g:
			{
			if ( input.LA(1)==CHARLITERAL||input.LA(1)==DOUBLELITERAL||input.LA(1)==FALSE||input.LA(1)==FLOATLITERAL||input.LA(1)==INTLITERAL||input.LA(1)==LONGLITERAL||input.LA(1)==NULL||input.LA(1)==STRINGLITERAL||input.LA(1)==TRUE ) {
				input.consume();
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 102, literal_StartIndex); }

		}
	}
	// $ANTLR end "literal"



	// $ANTLR start "classHeader"
	// src/main/resources/parser/Java.g:1341:1: classHeader : modifiers 'class' IDENTIFIER ;
	public final void classHeader() throws RecognitionException {
		int classHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 103) ) { return; }

			// src/main/resources/parser/Java.g:1342:5: ( modifiers 'class' IDENTIFIER )
			// src/main/resources/parser/Java.g:1342:9: modifiers 'class' IDENTIFIER
			{
			pushFollow(FOLLOW_modifiers_in_classHeader8159);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			match(input,CLASS,FOLLOW_CLASS_in_classHeader8161); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_classHeader8163); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 103, classHeader_StartIndex); }

		}
	}
	// $ANTLR end "classHeader"



	// $ANTLR start "enumHeader"
	// src/main/resources/parser/Java.g:1345:1: enumHeader : modifiers ( 'enum' | IDENTIFIER ) IDENTIFIER ;
	public final void enumHeader() throws RecognitionException {
		int enumHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 104) ) { return; }

			// src/main/resources/parser/Java.g:1346:5: ( modifiers ( 'enum' | IDENTIFIER ) IDENTIFIER )
			// src/main/resources/parser/Java.g:1346:9: modifiers ( 'enum' | IDENTIFIER ) IDENTIFIER
			{
			pushFollow(FOLLOW_modifiers_in_enumHeader8183);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			if ( input.LA(1)==ENUM||input.LA(1)==IDENTIFIER ) {
				input.consume();
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_enumHeader8191); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 104, enumHeader_StartIndex); }

		}
	}
	// $ANTLR end "enumHeader"



	// $ANTLR start "interfaceHeader"
	// src/main/resources/parser/Java.g:1349:1: interfaceHeader : modifiers 'interface' IDENTIFIER ;
	public final void interfaceHeader() throws RecognitionException {
		int interfaceHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 105) ) { return; }

			// src/main/resources/parser/Java.g:1350:5: ( modifiers 'interface' IDENTIFIER )
			// src/main/resources/parser/Java.g:1350:9: modifiers 'interface' IDENTIFIER
			{
			pushFollow(FOLLOW_modifiers_in_interfaceHeader8211);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			match(input,INTERFACE,FOLLOW_INTERFACE_in_interfaceHeader8213); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_interfaceHeader8215); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 105, interfaceHeader_StartIndex); }

		}
	}
	// $ANTLR end "interfaceHeader"



	// $ANTLR start "annotationHeader"
	// src/main/resources/parser/Java.g:1353:1: annotationHeader : modifiers '@' 'interface' IDENTIFIER ;
	public final void annotationHeader() throws RecognitionException {
		int annotationHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 106) ) { return; }

			// src/main/resources/parser/Java.g:1354:5: ( modifiers '@' 'interface' IDENTIFIER )
			// src/main/resources/parser/Java.g:1354:9: modifiers '@' 'interface' IDENTIFIER
			{
			pushFollow(FOLLOW_modifiers_in_annotationHeader8235);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			match(input,MONKEYS_AT,FOLLOW_MONKEYS_AT_in_annotationHeader8237); if (state.failed) return;
			match(input,INTERFACE,FOLLOW_INTERFACE_in_annotationHeader8239); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_annotationHeader8241); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 106, annotationHeader_StartIndex); }

		}
	}
	// $ANTLR end "annotationHeader"



	// $ANTLR start "typeHeader"
	// src/main/resources/parser/Java.g:1357:1: typeHeader : modifiers ( 'class' | 'enum' | ( ( '@' )? 'interface' ) ) IDENTIFIER ;
	public final void typeHeader() throws RecognitionException {
		int typeHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 107) ) { return; }

			// src/main/resources/parser/Java.g:1358:5: ( modifiers ( 'class' | 'enum' | ( ( '@' )? 'interface' ) ) IDENTIFIER )
			// src/main/resources/parser/Java.g:1358:9: modifiers ( 'class' | 'enum' | ( ( '@' )? 'interface' ) ) IDENTIFIER
			{
			pushFollow(FOLLOW_modifiers_in_typeHeader8261);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1358:19: ( 'class' | 'enum' | ( ( '@' )? 'interface' ) )
			int alt161=3;
			switch ( input.LA(1) ) {
			case CLASS:
				{
				alt161=1;
				}
				break;
			case ENUM:
				{
				alt161=2;
				}
				break;
			case INTERFACE:
			case MONKEYS_AT:
				{
				alt161=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return;}
				NoViableAltException nvae =
					new NoViableAltException("", 161, 0, input);
				throw nvae;
			}
			switch (alt161) {
				case 1 :
					// src/main/resources/parser/Java.g:1358:20: 'class'
					{
					match(input,CLASS,FOLLOW_CLASS_in_typeHeader8264); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1358:28: 'enum'
					{
					match(input,ENUM,FOLLOW_ENUM_in_typeHeader8266); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1358:35: ( ( '@' )? 'interface' )
					{
					// src/main/resources/parser/Java.g:1358:35: ( ( '@' )? 'interface' )
					// src/main/resources/parser/Java.g:1358:36: ( '@' )? 'interface'
					{
					// src/main/resources/parser/Java.g:1358:36: ( '@' )?
					int alt160=2;
					int LA160_0 = input.LA(1);
					if ( (LA160_0==MONKEYS_AT) ) {
						alt160=1;
					}
					switch (alt160) {
						case 1 :
							// src/main/resources/parser/Java.g:1358:36: '@'
							{
							match(input,MONKEYS_AT,FOLLOW_MONKEYS_AT_in_typeHeader8269); if (state.failed) return;
							}
							break;

					}

					match(input,INTERFACE,FOLLOW_INTERFACE_in_typeHeader8273); if (state.failed) return;
					}

					}
					break;

			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_typeHeader8277); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 107, typeHeader_StartIndex); }

		}
	}
	// $ANTLR end "typeHeader"



	// $ANTLR start "methodHeader"
	// src/main/resources/parser/Java.g:1361:1: methodHeader : modifiers ( typeParameters )? ( type | 'void' )? IDENTIFIER '(' ;
	public final void methodHeader() throws RecognitionException {
		int methodHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 108) ) { return; }

			// src/main/resources/parser/Java.g:1362:5: ( modifiers ( typeParameters )? ( type | 'void' )? IDENTIFIER '(' )
			// src/main/resources/parser/Java.g:1362:9: modifiers ( typeParameters )? ( type | 'void' )? IDENTIFIER '('
			{
			pushFollow(FOLLOW_modifiers_in_methodHeader8297);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1362:19: ( typeParameters )?
			int alt162=2;
			int LA162_0 = input.LA(1);
			if ( (LA162_0==LT) ) {
				alt162=1;
			}
			switch (alt162) {
				case 1 :
					// src/main/resources/parser/Java.g:1362:19: typeParameters
					{
					pushFollow(FOLLOW_typeParameters_in_methodHeader8299);
					typeParameters();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:1362:35: ( type | 'void' )?
			int alt163=3;
			switch ( input.LA(1) ) {
				case IDENTIFIER:
					{
					int LA163_1 = input.LA(2);
					if ( (LA163_1==DOT||LA163_1==IDENTIFIER||LA163_1==LBRACKET||LA163_1==LT) ) {
						alt163=1;
					}
					}
					break;
				case BOOLEAN:
				case BYTE:
				case CHAR:
				case DOUBLE:
				case FLOAT:
				case INT:
				case LONG:
				case SHORT:
					{
					alt163=1;
					}
					break;
				case VOID:
					{
					alt163=2;
					}
					break;
			}
			switch (alt163) {
				case 1 :
					// src/main/resources/parser/Java.g:1362:36: type
					{
					pushFollow(FOLLOW_type_in_methodHeader8303);
					type();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1362:41: 'void'
					{
					match(input,VOID,FOLLOW_VOID_in_methodHeader8305); if (state.failed) return;
					}
					break;

			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_methodHeader8309); if (state.failed) return;
			match(input,LPAREN,FOLLOW_LPAREN_in_methodHeader8311); if (state.failed) return;
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 108, methodHeader_StartIndex); }

		}
	}
	// $ANTLR end "methodHeader"



	// $ANTLR start "fieldHeader"
	// src/main/resources/parser/Java.g:1365:1: fieldHeader : modifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' ) ;
	public final void fieldHeader() throws RecognitionException {
		int fieldHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 109) ) { return; }

			// src/main/resources/parser/Java.g:1366:5: ( modifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' ) )
			// src/main/resources/parser/Java.g:1366:9: modifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' )
			{
			pushFollow(FOLLOW_modifiers_in_fieldHeader8331);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_fieldHeader8333);
			type();
			state._fsp--;
			if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_fieldHeader8335); if (state.failed) return;
			// src/main/resources/parser/Java.g:1366:35: ( '[' ']' )*
			loop164:
			while (true) {
				int alt164=2;
				int LA164_0 = input.LA(1);
				if ( (LA164_0==LBRACKET) ) {
					alt164=1;
				}

				switch (alt164) {
				case 1 :
					// src/main/resources/parser/Java.g:1366:36: '[' ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_fieldHeader8338); if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_fieldHeader8339); if (state.failed) return;
					}
					break;

				default :
					break loop164;
				}
			}

			if ( input.LA(1)==COMMA||input.LA(1)==EQ||input.LA(1)==SEMI ) {
				input.consume();
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 109, fieldHeader_StartIndex); }

		}
	}
	// $ANTLR end "fieldHeader"



	// $ANTLR start "localVariableHeader"
	// src/main/resources/parser/Java.g:1369:1: localVariableHeader : variableModifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' ) ;
	public final void localVariableHeader() throws RecognitionException {
		int localVariableHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 110) ) { return; }

			// src/main/resources/parser/Java.g:1370:5: ( variableModifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' ) )
			// src/main/resources/parser/Java.g:1370:9: variableModifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' )
			{
			pushFollow(FOLLOW_variableModifiers_in_localVariableHeader8369);
			variableModifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_localVariableHeader8371);
			type();
			state._fsp--;
			if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_localVariableHeader8373); if (state.failed) return;
			// src/main/resources/parser/Java.g:1370:43: ( '[' ']' )*
			loop165:
			while (true) {
				int alt165=2;
				int LA165_0 = input.LA(1);
				if ( (LA165_0==LBRACKET) ) {
					alt165=1;
				}

				switch (alt165) {
				case 1 :
					// src/main/resources/parser/Java.g:1370:44: '[' ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_localVariableHeader8376); if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_localVariableHeader8377); if (state.failed) return;
					}
					break;

				default :
					break loop165;
				}
			}

			if ( input.LA(1)==COMMA||input.LA(1)==EQ||input.LA(1)==SEMI ) {
				input.consume();
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 110, localVariableHeader_StartIndex); }

		}
	}
	// $ANTLR end "localVariableHeader"

	// $ANTLR start synpred2_Java
	public final void synpred2_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:317:13: ( ( annotations )? packageDeclaration )
		// src/main/resources/parser/Java.g:317:13: ( annotations )? packageDeclaration
		{
		// src/main/resources/parser/Java.g:317:13: ( annotations )?
		int alt166=2;
		int LA166_0 = input.LA(1);
		if ( (LA166_0==MONKEYS_AT) ) {
			alt166=1;
		}
		switch (alt166) {
			case 1 :
				// src/main/resources/parser/Java.g:317:14: annotations
				{
				pushFollow(FOLLOW_annotations_in_synpred2_Java127);
				annotations();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		pushFollow(FOLLOW_packageDeclaration_in_synpred2_Java156);
		packageDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred2_Java

	// $ANTLR start synpred12_Java
	public final void synpred12_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:361:10: ( classDeclaration )
		// src/main/resources/parser/Java.g:361:10: classDeclaration
		{
		pushFollow(FOLLOW_classDeclaration_in_synpred12_Java514);
		classDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred12_Java

	// $ANTLR start synpred27_Java
	public final void synpred27_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:398:9: ( normalClassDeclaration )
		// src/main/resources/parser/Java.g:398:9: normalClassDeclaration
		{
		pushFollow(FOLLOW_normalClassDeclaration_in_synpred27_Java877);
		normalClassDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred27_Java

	// $ANTLR start synpred43_Java
	public final void synpred43_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:486:9: ( normalInterfaceDeclaration )
		// src/main/resources/parser/Java.g:486:9: normalInterfaceDeclaration
		{
		pushFollow(FOLLOW_normalInterfaceDeclaration_in_synpred43_Java1556);
		normalInterfaceDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred43_Java

	// $ANTLR start synpred52_Java
	public final void synpred52_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:528:10: ( fieldDeclaration )
		// src/main/resources/parser/Java.g:528:10: fieldDeclaration
		{
		pushFollow(FOLLOW_fieldDeclaration_in_synpred52_Java1885);
		fieldDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred52_Java

	// $ANTLR start synpred53_Java
	public final void synpred53_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:529:10: ( methodDeclaration )
		// src/main/resources/parser/Java.g:529:10: methodDeclaration
		{
		pushFollow(FOLLOW_methodDeclaration_in_synpred53_Java1896);
		methodDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred53_Java

	// $ANTLR start synpred54_Java
	public final void synpred54_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:530:10: ( classDeclaration )
		// src/main/resources/parser/Java.g:530:10: classDeclaration
		{
		pushFollow(FOLLOW_classDeclaration_in_synpred54_Java1907);
		classDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred54_Java

	// $ANTLR start synpred57_Java
	public final void synpred57_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:563:10: ( explicitConstructorInvocation )
		// src/main/resources/parser/Java.g:563:10: explicitConstructorInvocation
		{
		pushFollow(FOLLOW_explicitConstructorInvocation_in_synpred57_Java2061);
		explicitConstructorInvocation();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred57_Java

	// $ANTLR start synpred59_Java
	public final void synpred59_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:555:10: ( modifiers ( typeParameters )? IDENTIFIER formalParameters ( 'throws' qualifiedNameList )? '{' ( explicitConstructorInvocation )? ( blockStatement )* '}' )
		// src/main/resources/parser/Java.g:555:10: modifiers ( typeParameters )? IDENTIFIER formalParameters ( 'throws' qualifiedNameList )? '{' ( explicitConstructorInvocation )? ( blockStatement )* '}'
		{
		pushFollow(FOLLOW_modifiers_in_synpred59_Java1973);
		modifiers();
		state._fsp--;
		if (state.failed) return;
		// src/main/resources/parser/Java.g:556:9: ( typeParameters )?
		int alt169=2;
		int LA169_0 = input.LA(1);
		if ( (LA169_0==LT) ) {
			alt169=1;
		}
		switch (alt169) {
			case 1 :
				// src/main/resources/parser/Java.g:556:10: typeParameters
				{
				pushFollow(FOLLOW_typeParameters_in_synpred59_Java1984);
				typeParameters();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred59_Java2005); if (state.failed) return;
		pushFollow(FOLLOW_formalParameters_in_synpred59_Java2015);
		formalParameters();
		state._fsp--;
		if (state.failed) return;
		// src/main/resources/parser/Java.g:560:9: ( 'throws' qualifiedNameList )?
		int alt170=2;
		int LA170_0 = input.LA(1);
		if ( (LA170_0==THROWS) ) {
			alt170=1;
		}
		switch (alt170) {
			case 1 :
				// src/main/resources/parser/Java.g:560:10: 'throws' qualifiedNameList
				{
				match(input,THROWS,FOLLOW_THROWS_in_synpred59_Java2026); if (state.failed) return;
				pushFollow(FOLLOW_qualifiedNameList_in_synpred59_Java2028);
				qualifiedNameList();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		match(input,LBRACE,FOLLOW_LBRACE_in_synpred59_Java2049); if (state.failed) return;
		// src/main/resources/parser/Java.g:563:9: ( explicitConstructorInvocation )?
		int alt171=2;
		switch ( input.LA(1) ) {
			case LT:
				{
				alt171=1;
				}
				break;
			case THIS:
				{
				int LA171_2 = input.LA(2);
				if ( (synpred57_Java()) ) {
					alt171=1;
				}
				}
				break;
			case LPAREN:
				{
				int LA171_3 = input.LA(2);
				if ( (synpred57_Java()) ) {
					alt171=1;
				}
				}
				break;
			case SUPER:
				{
				int LA171_4 = input.LA(2);
				if ( (synpred57_Java()) ) {
					alt171=1;
				}
				}
				break;
			case IDENTIFIER:
				{
				int LA171_5 = input.LA(2);
				if ( (synpred57_Java()) ) {
					alt171=1;
				}
				}
				break;
			case CHARLITERAL:
			case DOUBLELITERAL:
			case FALSE:
			case FLOATLITERAL:
			case INTLITERAL:
			case LONGLITERAL:
			case NULL:
			case STRINGLITERAL:
			case TRUE:
				{
				int LA171_6 = input.LA(2);
				if ( (synpred57_Java()) ) {
					alt171=1;
				}
				}
				break;
			case NEW:
				{
				int LA171_7 = input.LA(2);
				if ( (synpred57_Java()) ) {
					alt171=1;
				}
				}
				break;
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
				{
				int LA171_8 = input.LA(2);
				if ( (synpred57_Java()) ) {
					alt171=1;
				}
				}
				break;
			case VOID:
				{
				int LA171_9 = input.LA(2);
				if ( (synpred57_Java()) ) {
					alt171=1;
				}
				}
				break;
		}
		switch (alt171) {
			case 1 :
				// src/main/resources/parser/Java.g:563:10: explicitConstructorInvocation
				{
				pushFollow(FOLLOW_explicitConstructorInvocation_in_synpred59_Java2061);
				explicitConstructorInvocation();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		// src/main/resources/parser/Java.g:565:9: ( blockStatement )*
		loop172:
		while (true) {
			int alt172=2;
			int LA172_0 = input.LA(1);
			if ( (LA172_0==ABSTRACT||(LA172_0 >= ASSERT && LA172_0 <= BANG)||(LA172_0 >= BOOLEAN && LA172_0 <= BYTE)||(LA172_0 >= CHAR && LA172_0 <= CLASS)||LA172_0==CONTINUE||LA172_0==DO||(LA172_0 >= DOUBLE && LA172_0 <= DOUBLELITERAL)||LA172_0==ENUM||(LA172_0 >= FALSE && LA172_0 <= FINAL)||(LA172_0 >= FLOAT && LA172_0 <= FOR)||(LA172_0 >= IDENTIFIER && LA172_0 <= IF)||(LA172_0 >= INT && LA172_0 <= INTLITERAL)||LA172_0==LBRACE||(LA172_0 >= LONG && LA172_0 <= LT)||(LA172_0 >= MONKEYS_AT && LA172_0 <= NULL)||LA172_0==PLUS||(LA172_0 >= PLUSPLUS && LA172_0 <= PUBLIC)||LA172_0==RETURN||(LA172_0 >= SEMI && LA172_0 <= SHORT)||(LA172_0 >= STATIC && LA172_0 <= SUB)||(LA172_0 >= SUBSUB && LA172_0 <= SYNCHRONIZED)||(LA172_0 >= THIS && LA172_0 <= THROW)||(LA172_0 >= TILDE && LA172_0 <= WHILE)) ) {
				alt172=1;
			}

			switch (alt172) {
			case 1 :
				// src/main/resources/parser/Java.g:565:10: blockStatement
				{
				pushFollow(FOLLOW_blockStatement_in_synpred59_Java2083);
				blockStatement();
				state._fsp--;
				if (state.failed) return;
				}
				break;

			default :
				break loop172;
			}
		}

		match(input,RBRACE,FOLLOW_RBRACE_in_synpred59_Java2104); if (state.failed) return;
		}

	}
	// $ANTLR end synpred59_Java

	// $ANTLR start synpred68_Java
	public final void synpred68_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:637:9: ( interfaceFieldDeclaration )
		// src/main/resources/parser/Java.g:637:9: interfaceFieldDeclaration
		{
		pushFollow(FOLLOW_interfaceFieldDeclaration_in_synpred68_Java2548);
		interfaceFieldDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred68_Java

	// $ANTLR start synpred69_Java
	public final void synpred69_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:638:9: ( interfaceMethodDeclaration )
		// src/main/resources/parser/Java.g:638:9: interfaceMethodDeclaration
		{
		pushFollow(FOLLOW_interfaceMethodDeclaration_in_synpred69_Java2558);
		interfaceMethodDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred69_Java

	// $ANTLR start synpred70_Java
	public final void synpred70_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:639:9: ( interfaceDeclaration )
		// src/main/resources/parser/Java.g:639:9: interfaceDeclaration
		{
		pushFollow(FOLLOW_interfaceDeclaration_in_synpred70_Java2568);
		interfaceDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred70_Java

	// $ANTLR start synpred71_Java
	public final void synpred71_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:640:9: ( classDeclaration )
		// src/main/resources/parser/Java.g:640:9: classDeclaration
		{
		pushFollow(FOLLOW_classDeclaration_in_synpred71_Java2578);
		classDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred71_Java

	// $ANTLR start synpred96_Java
	public final void synpred96_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:758:9: ( ellipsisParameterDecl )
		// src/main/resources/parser/Java.g:758:9: ellipsisParameterDecl
		{
		pushFollow(FOLLOW_ellipsisParameterDecl_in_synpred96_Java3386);
		ellipsisParameterDecl();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred96_Java

	// $ANTLR start synpred98_Java
	public final void synpred98_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:759:9: ( normalParameterDecl ( ',' normalParameterDecl )* )
		// src/main/resources/parser/Java.g:759:9: normalParameterDecl ( ',' normalParameterDecl )*
		{
		pushFollow(FOLLOW_normalParameterDecl_in_synpred98_Java3396);
		normalParameterDecl();
		state._fsp--;
		if (state.failed) return;
		// src/main/resources/parser/Java.g:760:9: ( ',' normalParameterDecl )*
		loop175:
		while (true) {
			int alt175=2;
			int LA175_0 = input.LA(1);
			if ( (LA175_0==COMMA) ) {
				alt175=1;
			}

			switch (alt175) {
			case 1 :
				// src/main/resources/parser/Java.g:760:10: ',' normalParameterDecl
				{
				match(input,COMMA,FOLLOW_COMMA_in_synpred98_Java3407); if (state.failed) return;
				pushFollow(FOLLOW_normalParameterDecl_in_synpred98_Java3409);
				normalParameterDecl();
				state._fsp--;
				if (state.failed) return;
				}
				break;

			default :
				break loop175;
			}
		}

		}

	}
	// $ANTLR end synpred98_Java

	// $ANTLR start synpred99_Java
	public final void synpred99_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:762:10: ( normalParameterDecl ',' )
		// src/main/resources/parser/Java.g:762:10: normalParameterDecl ','
		{
		pushFollow(FOLLOW_normalParameterDecl_in_synpred99_Java3431);
		normalParameterDecl();
		state._fsp--;
		if (state.failed) return;
		match(input,COMMA,FOLLOW_COMMA_in_synpred99_Java3441); if (state.failed) return;
		}

	}
	// $ANTLR end synpred99_Java

	// $ANTLR start synpred103_Java
	public final void synpred103_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:782:9: ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' )
		// src/main/resources/parser/Java.g:782:9: ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';'
		{
		// src/main/resources/parser/Java.g:782:9: ( nonWildcardTypeArguments )?
		int alt176=2;
		int LA176_0 = input.LA(1);
		if ( (LA176_0==LT) ) {
			alt176=1;
		}
		switch (alt176) {
			case 1 :
				// src/main/resources/parser/Java.g:782:10: nonWildcardTypeArguments
				{
				pushFollow(FOLLOW_nonWildcardTypeArguments_in_synpred103_Java3576);
				nonWildcardTypeArguments();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		if ( input.LA(1)==SUPER||input.LA(1)==THIS ) {
			input.consume();
			state.errorRecovery=false;
			state.failed=false;
		}
		else {
			if (state.backtracking>0) {state.failed=true; return;}
			MismatchedSetException mse = new MismatchedSetException(null,input);
			throw mse;
		}
		pushFollow(FOLLOW_arguments_in_synpred103_Java3634);
		arguments();
		state._fsp--;
		if (state.failed) return;
		match(input,SEMI,FOLLOW_SEMI_in_synpred103_Java3636); if (state.failed) return;
		}

	}
	// $ANTLR end synpred103_Java

	// $ANTLR start synpred117_Java
	public final void synpred117_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:869:9: ( annotationMethodDeclaration )
		// src/main/resources/parser/Java.g:869:9: annotationMethodDeclaration
		{
		pushFollow(FOLLOW_annotationMethodDeclaration_in_synpred117_Java4232);
		annotationMethodDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred117_Java

	// $ANTLR start synpred118_Java
	public final void synpred118_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:870:9: ( interfaceFieldDeclaration )
		// src/main/resources/parser/Java.g:870:9: interfaceFieldDeclaration
		{
		pushFollow(FOLLOW_interfaceFieldDeclaration_in_synpred118_Java4242);
		interfaceFieldDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred118_Java

	// $ANTLR start synpred119_Java
	public final void synpred119_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:871:9: ( normalClassDeclaration )
		// src/main/resources/parser/Java.g:871:9: normalClassDeclaration
		{
		pushFollow(FOLLOW_normalClassDeclaration_in_synpred119_Java4252);
		normalClassDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred119_Java

	// $ANTLR start synpred120_Java
	public final void synpred120_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:872:9: ( normalInterfaceDeclaration )
		// src/main/resources/parser/Java.g:872:9: normalInterfaceDeclaration
		{
		pushFollow(FOLLOW_normalInterfaceDeclaration_in_synpred120_Java4262);
		normalInterfaceDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred120_Java

	// $ANTLR start synpred121_Java
	public final void synpred121_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:873:9: ( enumDeclaration )
		// src/main/resources/parser/Java.g:873:9: enumDeclaration
		{
		pushFollow(FOLLOW_enumDeclaration_in_synpred121_Java4272);
		enumDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred121_Java

	// $ANTLR start synpred122_Java
	public final void synpred122_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:874:9: ( annotationTypeDeclaration )
		// src/main/resources/parser/Java.g:874:9: annotationTypeDeclaration
		{
		pushFollow(FOLLOW_annotationTypeDeclaration_in_synpred122_Java4282);
		annotationTypeDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred122_Java

	// $ANTLR start synpred125_Java
	public final void synpred125_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:917:9: ( localVariableDeclarationStatement )
		// src/main/resources/parser/Java.g:917:9: localVariableDeclarationStatement
		{
		pushFollow(FOLLOW_localVariableDeclarationStatement_in_synpred125_Java4440);
		localVariableDeclarationStatement();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred125_Java

	// $ANTLR start synpred126_Java
	public final void synpred126_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:918:9: ( classOrInterfaceDeclaration )
		// src/main/resources/parser/Java.g:918:9: classOrInterfaceDeclaration
		{
		pushFollow(FOLLOW_classOrInterfaceDeclaration_in_synpred126_Java4450);
		classOrInterfaceDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred126_Java

	// $ANTLR start synpred130_Java
	public final void synpred130_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:938:9: ( ( 'assert' ) expression ( ':' expression )? ';' )
		// src/main/resources/parser/Java.g:938:9: ( 'assert' ) expression ( ':' expression )? ';'
		{
		// src/main/resources/parser/Java.g:938:9: ( 'assert' )
		// src/main/resources/parser/Java.g:938:10: 'assert'
		{
		match(input,ASSERT,FOLLOW_ASSERT_in_synpred130_Java4591); if (state.failed) return;
		}

		pushFollow(FOLLOW_expression_in_synpred130_Java4611);
		expression();
		state._fsp--;
		if (state.failed) return;
		// src/main/resources/parser/Java.g:940:20: ( ':' expression )?
		int alt179=2;
		int LA179_0 = input.LA(1);
		if ( (LA179_0==COLON) ) {
			alt179=1;
		}
		switch (alt179) {
			case 1 :
				// src/main/resources/parser/Java.g:940:21: ':' expression
				{
				match(input,COLON,FOLLOW_COLON_in_synpred130_Java4614); if (state.failed) return;
				pushFollow(FOLLOW_expression_in_synpred130_Java4616);
				expression();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		match(input,SEMI,FOLLOW_SEMI_in_synpred130_Java4620); if (state.failed) return;
		}

	}
	// $ANTLR end synpred130_Java

	// $ANTLR start synpred132_Java
	public final void synpred132_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:941:9: ( 'assert' expression ( ':' expression )? ';' )
		// src/main/resources/parser/Java.g:941:9: 'assert' expression ( ':' expression )? ';'
		{
		match(input,ASSERT,FOLLOW_ASSERT_in_synpred132_Java4630); if (state.failed) return;
		pushFollow(FOLLOW_expression_in_synpred132_Java4633);
		expression();
		state._fsp--;
		if (state.failed) return;
		// src/main/resources/parser/Java.g:941:30: ( ':' expression )?
		int alt180=2;
		int LA180_0 = input.LA(1);
		if ( (LA180_0==COLON) ) {
			alt180=1;
		}
		switch (alt180) {
			case 1 :
				// src/main/resources/parser/Java.g:941:31: ':' expression
				{
				match(input,COLON,FOLLOW_COLON_in_synpred132_Java4636); if (state.failed) return;
				pushFollow(FOLLOW_expression_in_synpred132_Java4638);
				expression();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		match(input,SEMI,FOLLOW_SEMI_in_synpred132_Java4642); if (state.failed) return;
		}

	}
	// $ANTLR end synpred132_Java

	// $ANTLR start synpred133_Java
	public final void synpred133_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:942:39: ( 'else' statement )
		// src/main/resources/parser/Java.g:942:39: 'else' statement
		{
		match(input,ELSE,FOLLOW_ELSE_in_synpred133_Java4671); if (state.failed) return;
		pushFollow(FOLLOW_statement_in_synpred133_Java4673);
		statement();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred133_Java

	// $ANTLR start synpred148_Java
	public final void synpred148_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:957:9: ( expression ';' )
		// src/main/resources/parser/Java.g:957:9: expression ';'
		{
		pushFollow(FOLLOW_expression_in_synpred148_Java4895);
		expression();
		state._fsp--;
		if (state.failed) return;
		match(input,SEMI,FOLLOW_SEMI_in_synpred148_Java4898); if (state.failed) return;
		}

	}
	// $ANTLR end synpred148_Java

	// $ANTLR start synpred149_Java
	public final void synpred149_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:958:9: ( IDENTIFIER ':' statement )
		// src/main/resources/parser/Java.g:958:9: IDENTIFIER ':' statement
		{
		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred149_Java4913); if (state.failed) return;
		match(input,COLON,FOLLOW_COLON_in_synpred149_Java4915); if (state.failed) return;
		pushFollow(FOLLOW_statement_in_synpred149_Java4917);
		statement();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred149_Java

	// $ANTLR start synpred153_Java
	public final void synpred153_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:982:13: ( catches 'finally' block )
		// src/main/resources/parser/Java.g:982:13: catches 'finally' block
		{
		pushFollow(FOLLOW_catches_in_synpred153_Java5073);
		catches();
		state._fsp--;
		if (state.failed) return;
		match(input,FINALLY,FOLLOW_FINALLY_in_synpred153_Java5075); if (state.failed) return;
		pushFollow(FOLLOW_block_in_synpred153_Java5077);
		block();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred153_Java

	// $ANTLR start synpred154_Java
	public final void synpred154_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:983:13: ( catches )
		// src/main/resources/parser/Java.g:983:13: catches
		{
		pushFollow(FOLLOW_catches_in_synpred154_Java5091);
		catches();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred154_Java

	// $ANTLR start synpred157_Java
	public final void synpred157_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1008:9: ( 'for' '(' variableModifiers type IDENTIFIER ':' expression ')' statement )
		// src/main/resources/parser/Java.g:1008:9: 'for' '(' variableModifiers type IDENTIFIER ':' expression ')' statement
		{
		match(input,FOR,FOLLOW_FOR_in_synpred157_Java5283); if (state.failed) return;
		match(input,LPAREN,FOLLOW_LPAREN_in_synpred157_Java5285); if (state.failed) return;
		pushFollow(FOLLOW_variableModifiers_in_synpred157_Java5287);
		variableModifiers();
		state._fsp--;
		if (state.failed) return;
		pushFollow(FOLLOW_type_in_synpred157_Java5289);
		type();
		state._fsp--;
		if (state.failed) return;
		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred157_Java5291); if (state.failed) return;
		match(input,COLON,FOLLOW_COLON_in_synpred157_Java5293); if (state.failed) return;
		pushFollow(FOLLOW_expression_in_synpred157_Java5304);
		expression();
		state._fsp--;
		if (state.failed) return;
		match(input,RPAREN,FOLLOW_RPAREN_in_synpred157_Java5306); if (state.failed) return;
		pushFollow(FOLLOW_statement_in_synpred157_Java5308);
		statement();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred157_Java

	// $ANTLR start synpred161_Java
	public final void synpred161_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1022:9: ( localVariableDeclaration )
		// src/main/resources/parser/Java.g:1022:9: localVariableDeclaration
		{
		pushFollow(FOLLOW_localVariableDeclaration_in_synpred161_Java5487);
		localVariableDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred161_Java

	// $ANTLR start synpred202_Java
	public final void synpred202_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1176:9: ( castExpression )
		// src/main/resources/parser/Java.g:1176:9: castExpression
		{
		pushFollow(FOLLOW_castExpression_in_synpred202_Java6732);
		castExpression();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred202_Java

	// $ANTLR start synpred206_Java
	public final void synpred206_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1186:9: ( '(' primitiveType ')' unaryExpression )
		// src/main/resources/parser/Java.g:1186:9: '(' primitiveType ')' unaryExpression
		{
		match(input,LPAREN,FOLLOW_LPAREN_in_synpred206_Java6823); if (state.failed) return;
		pushFollow(FOLLOW_primitiveType_in_synpred206_Java6825);
		primitiveType();
		state._fsp--;
		if (state.failed) return;
		match(input,RPAREN,FOLLOW_RPAREN_in_synpred206_Java6827); if (state.failed) return;
		pushFollow(FOLLOW_unaryExpression_in_synpred206_Java6829);
		unaryExpression();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred206_Java

	// $ANTLR start synpred208_Java
	public final void synpred208_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1196:10: ( '.' IDENTIFIER )
		// src/main/resources/parser/Java.g:1196:10: '.' IDENTIFIER
		{
		match(input,DOT,FOLLOW_DOT_in_synpred208_Java6900); if (state.failed) return;
		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred208_Java6902); if (state.failed) return;
		}

	}
	// $ANTLR end synpred208_Java

	// $ANTLR start synpred209_Java
	public final void synpred209_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1198:10: ( identifierSuffix )
		// src/main/resources/parser/Java.g:1198:10: identifierSuffix
		{
		pushFollow(FOLLOW_identifierSuffix_in_synpred209_Java6924);
		identifierSuffix();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred209_Java

	// $ANTLR start synpred211_Java
	public final void synpred211_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1201:10: ( '.' IDENTIFIER )
		// src/main/resources/parser/Java.g:1201:10: '.' IDENTIFIER
		{
		match(input,DOT,FOLLOW_DOT_in_synpred211_Java6956); if (state.failed) return;
		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred211_Java6958); if (state.failed) return;
		}

	}
	// $ANTLR end synpred211_Java

	// $ANTLR start synpred212_Java
	public final void synpred212_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1203:10: ( identifierSuffix )
		// src/main/resources/parser/Java.g:1203:10: identifierSuffix
		{
		pushFollow(FOLLOW_identifierSuffix_in_synpred212_Java6980);
		identifierSuffix();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred212_Java

	// $ANTLR start synpred224_Java
	public final void synpred224_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1231:10: ( '[' expression ']' )
		// src/main/resources/parser/Java.g:1231:10: '[' expression ']'
		{
		match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred224_Java7231); if (state.failed) return;
		pushFollow(FOLLOW_expression_in_synpred224_Java7233);
		expression();
		state._fsp--;
		if (state.failed) return;
		match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred224_Java7235); if (state.failed) return;
		}

	}
	// $ANTLR end synpred224_Java

	// $ANTLR start synpred236_Java
	public final void synpred236_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1254:9: ( 'new' nonWildcardTypeArguments classOrInterfaceType classCreatorRest )
		// src/main/resources/parser/Java.g:1254:9: 'new' nonWildcardTypeArguments classOrInterfaceType classCreatorRest
		{
		match(input,NEW,FOLLOW_NEW_in_synpred236_Java7444); if (state.failed) return;
		pushFollow(FOLLOW_nonWildcardTypeArguments_in_synpred236_Java7446);
		nonWildcardTypeArguments();
		state._fsp--;
		if (state.failed) return;
		pushFollow(FOLLOW_classOrInterfaceType_in_synpred236_Java7448);
		classOrInterfaceType();
		state._fsp--;
		if (state.failed) return;
		pushFollow(FOLLOW_classCreatorRest_in_synpred236_Java7450);
		classCreatorRest();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred236_Java

	// $ANTLR start synpred237_Java
	public final void synpred237_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1255:9: ( 'new' classOrInterfaceType classCreatorRest )
		// src/main/resources/parser/Java.g:1255:9: 'new' classOrInterfaceType classCreatorRest
		{
		match(input,NEW,FOLLOW_NEW_in_synpred237_Java7460); if (state.failed) return;
		pushFollow(FOLLOW_classOrInterfaceType_in_synpred237_Java7462);
		classOrInterfaceType();
		state._fsp--;
		if (state.failed) return;
		pushFollow(FOLLOW_classCreatorRest_in_synpred237_Java7464);
		classCreatorRest();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred237_Java

	// $ANTLR start synpred239_Java
	public final void synpred239_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1260:9: ( 'new' createdName '[' ']' ( '[' ']' )* arrayInitializer )
		// src/main/resources/parser/Java.g:1260:9: 'new' createdName '[' ']' ( '[' ']' )* arrayInitializer
		{
		match(input,NEW,FOLLOW_NEW_in_synpred239_Java7494); if (state.failed) return;
		pushFollow(FOLLOW_createdName_in_synpred239_Java7496);
		createdName();
		state._fsp--;
		if (state.failed) return;
		match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred239_Java7506); if (state.failed) return;
		match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred239_Java7508); if (state.failed) return;
		// src/main/resources/parser/Java.g:1262:9: ( '[' ']' )*
		loop193:
		while (true) {
			int alt193=2;
			int LA193_0 = input.LA(1);
			if ( (LA193_0==LBRACKET) ) {
				alt193=1;
			}

			switch (alt193) {
			case 1 :
				// src/main/resources/parser/Java.g:1262:10: '[' ']'
				{
				match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred239_Java7519); if (state.failed) return;
				match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred239_Java7521); if (state.failed) return;
				}
				break;

			default :
				break loop193;
			}
		}

		pushFollow(FOLLOW_arrayInitializer_in_synpred239_Java7542);
		arrayInitializer();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred239_Java

	// $ANTLR start synpred240_Java
	public final void synpred240_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1269:13: ( '[' expression ']' )
		// src/main/resources/parser/Java.g:1269:13: '[' expression ']'
		{
		match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred240_Java7591); if (state.failed) return;
		pushFollow(FOLLOW_expression_in_synpred240_Java7593);
		expression();
		state._fsp--;
		if (state.failed) return;
		match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred240_Java7607); if (state.failed) return;
		}

	}
	// $ANTLR end synpred240_Java

	// Delegated rules

	public final boolean synpred43_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred43_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred98_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred98_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred157_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred157_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred224_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred224_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred211_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred211_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred121_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred121_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred239_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred239_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred69_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred69_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred202_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred202_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred154_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred154_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred71_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred71_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred133_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred133_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred125_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred125_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred132_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred132_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred119_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred119_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred54_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred54_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred148_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred148_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred117_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred117_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred2_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred2_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred130_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred130_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred126_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred126_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred59_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred59_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred212_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred212_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred161_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred161_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred57_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred57_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred209_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred209_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred68_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred68_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred53_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred53_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred52_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred52_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred236_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred236_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred12_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred12_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred149_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred149_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred120_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred120_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred122_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred122_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred240_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred240_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred206_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred206_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred70_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred70_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred27_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred27_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred96_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred96_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred153_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred153_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred99_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred99_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred103_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred103_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred237_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred237_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred118_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred118_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred208_Java() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred208_Java_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}



	public static final BitSet FOLLOW_annotations_in_compilationUnit127 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_packageDeclaration_in_compilationUnit156 = new BitSet(new long[]{0x1200102000800012L,0x0011040C10700600L});
	public static final BitSet FOLLOW_importDeclaration_in_compilationUnit178 = new BitSet(new long[]{0x1200102000800012L,0x0011040C10700600L});
	public static final BitSet FOLLOW_typeDeclaration_in_compilationUnit200 = new BitSet(new long[]{0x1000102000800012L,0x0011040C10700600L});
	public static final BitSet FOLLOW_PACKAGE_in_packageDeclaration231 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedName_in_packageDeclaration233 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_packageDeclaration243 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IMPORT_in_importDeclaration264 = new BitSet(new long[]{0x0040000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_STATIC_in_importDeclaration276 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_importDeclaration297 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_DOT_in_importDeclaration299 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_STAR_in_importDeclaration301 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_importDeclaration311 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IMPORT_in_importDeclaration328 = new BitSet(new long[]{0x0040000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_STATIC_in_importDeclaration340 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_importDeclaration361 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_DOT_in_importDeclaration372 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_importDeclaration374 = new BitSet(new long[]{0x0000000080000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_DOT_in_importDeclaration396 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_STAR_in_importDeclaration398 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_importDeclaration419 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_qualifiedImportName439 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_DOT_in_qualifiedImportName450 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_qualifiedImportName452 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_typeDeclaration483 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_typeDeclaration493 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_classOrInterfaceDeclaration514 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceDeclaration_in_classOrInterfaceDeclaration524 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotation_in_modifiers560 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_PUBLIC_in_modifiers572 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_PROTECTED_in_modifiers595 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_PRIVATE_in_modifiers615 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_STATIC_in_modifiers637 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_ABSTRACT_in_modifiers660 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_FINAL_in_modifiers681 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_NATIVE_in_modifiers705 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_SYNCHRONIZED_in_modifiers728 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_TRANSIENT_in_modifiers745 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_VOLATILE_in_modifiers765 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_STRICTFP_in_modifiers786 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_FINAL_in_variableModifiers827 = new BitSet(new long[]{0x0000100000000002L,0x0000000000000200L});
	public static final BitSet FOLLOW_annotation_in_variableModifiers841 = new BitSet(new long[]{0x0000100000000002L,0x0000000000000200L});
	public static final BitSet FOLLOW_normalClassDeclaration_in_classDeclaration877 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumDeclaration_in_classDeclaration887 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_normalClassDeclaration907 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_normalClassDeclaration910 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_normalClassDeclaration912 = new BitSet(new long[]{0x0100010000000000L,0x0000000000000082L});
	public static final BitSet FOLLOW_typeParameters_in_normalClassDeclaration923 = new BitSet(new long[]{0x0100010000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_EXTENDS_in_normalClassDeclaration945 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_normalClassDeclaration947 = new BitSet(new long[]{0x0100000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_IMPLEMENTS_in_normalClassDeclaration969 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_typeList_in_normalClassDeclaration971 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_classBody_in_normalClassDeclaration1004 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LT_in_typeParameters1025 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_typeParameter_in_typeParameters1039 = new BitSet(new long[]{0x0008000002000000L});
	public static final BitSet FOLLOW_COMMA_in_typeParameters1054 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_typeParameter_in_typeParameters1056 = new BitSet(new long[]{0x0008000002000000L});
	public static final BitSet FOLLOW_GT_in_typeParameters1081 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_typeParameter1101 = new BitSet(new long[]{0x0000010000000002L});
	public static final BitSet FOLLOW_EXTENDS_in_typeParameter1112 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_typeBound_in_typeParameter1114 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_typeBound1146 = new BitSet(new long[]{0x0000000000000022L});
	public static final BitSet FOLLOW_AMP_in_typeBound1157 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_typeBound1159 = new BitSet(new long[]{0x0000000000000022L});
	public static final BitSet FOLLOW_modifiers_in_enumDeclaration1191 = new BitSet(new long[]{0x0000002000000000L});
	public static final BitSet FOLLOW_ENUM_in_enumDeclaration1203 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_enumDeclaration1224 = new BitSet(new long[]{0x0100000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_IMPLEMENTS_in_enumDeclaration1235 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_typeList_in_enumDeclaration1237 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_enumBody_in_enumDeclaration1258 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_enumBody1283 = new BitSet(new long[]{0x0040000002000000L,0x0000000011000200L});
	public static final BitSet FOLLOW_enumConstants_in_enumBody1294 = new BitSet(new long[]{0x0000000002000000L,0x0000000011000000L});
	public static final BitSet FOLLOW_COMMA_in_enumBody1316 = new BitSet(new long[]{0x0000000000000000L,0x0000000011000000L});
	public static final BitSet FOLLOW_enumBodyDeclarations_in_enumBody1329 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_RBRACE_in_enumBody1351 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumConstant_in_enumConstants1371 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_enumConstants1382 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_enumConstant_in_enumConstants1384 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_annotations_in_enumConstant1418 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_enumConstant1439 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000042L});
	public static final BitSet FOLLOW_arguments_in_enumConstant1450 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
	public static final BitSet FOLLOW_classBody_in_enumConstant1472 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_enumBodyDeclarations1513 = new BitSet(new long[]{0x1840502100A14012L,0x0019040C30700692L});
	public static final BitSet FOLLOW_classBodyDeclaration_in_enumBodyDeclarations1525 = new BitSet(new long[]{0x1840502100A14012L,0x0019040C30700692L});
	public static final BitSet FOLLOW_normalInterfaceDeclaration_in_interfaceDeclaration1556 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotationTypeDeclaration_in_interfaceDeclaration1566 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_normalInterfaceDeclaration1590 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_INTERFACE_in_normalInterfaceDeclaration1592 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_normalInterfaceDeclaration1594 = new BitSet(new long[]{0x0000010000000000L,0x0000000000000082L});
	public static final BitSet FOLLOW_typeParameters_in_normalInterfaceDeclaration1605 = new BitSet(new long[]{0x0000010000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_EXTENDS_in_normalInterfaceDeclaration1627 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_typeList_in_normalInterfaceDeclaration1629 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceBody_in_normalInterfaceDeclaration1650 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_typeList1670 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_typeList1681 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_typeList1683 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_LBRACE_in_classBody1714 = new BitSet(new long[]{0x1840502100A14010L,0x0019040C31700692L});
	public static final BitSet FOLLOW_classBodyDeclaration_in_classBody1726 = new BitSet(new long[]{0x1840502100A14010L,0x0019040C31700692L});
	public static final BitSet FOLLOW_RBRACE_in_classBody1748 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_interfaceBody1768 = new BitSet(new long[]{0x1840502100A14010L,0x0019040C31700690L});
	public static final BitSet FOLLOW_interfaceBodyDeclaration_in_interfaceBody1780 = new BitSet(new long[]{0x1840502100A14010L,0x0019040C31700690L});
	public static final BitSet FOLLOW_RBRACE_in_interfaceBody1802 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_classBodyDeclaration1822 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STATIC_in_classBodyDeclaration1833 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_classBodyDeclaration1855 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_memberDecl_in_classBodyDeclaration1865 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fieldDeclaration_in_memberDecl1885 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_methodDeclaration_in_memberDecl1896 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_memberDecl1907 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceDeclaration_in_memberDecl1918 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_methodDeclaration1973 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_typeParameters_in_methodDeclaration1984 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_methodDeclaration2005 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_formalParameters_in_methodDeclaration2015 = new BitSet(new long[]{0x0000000000000000L,0x0000400000000002L});
	public static final BitSet FOLLOW_THROWS_in_methodDeclaration2026 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedNameList_in_methodDeclaration2028 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_methodDeclaration2049 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1EF2L});
	public static final BitSet FOLLOW_explicitConstructorInvocation_in_methodDeclaration2061 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_blockStatement_in_methodDeclaration2083 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_RBRACE_in_methodDeclaration2104 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_methodDeclaration2114 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000090L});
	public static final BitSet FOLLOW_typeParameters_in_methodDeclaration2125 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000010L});
	public static final BitSet FOLLOW_type_in_methodDeclaration2147 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_VOID_in_methodDeclaration2161 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_methodDeclaration2181 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_formalParameters_in_methodDeclaration2193 = new BitSet(new long[]{0x0000000000000000L,0x0000400010000006L});
	public static final BitSet FOLLOW_LBRACKET_in_methodDeclaration2204 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_methodDeclaration2206 = new BitSet(new long[]{0x0000000000000000L,0x0000400010000006L});
	public static final BitSet FOLLOW_THROWS_in_methodDeclaration2232 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedNameList_in_methodDeclaration2234 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000002L});
	public static final BitSet FOLLOW_block_in_methodDeclaration2289 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_methodDeclaration2303 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_fieldDeclaration2352 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_fieldDeclaration2362 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_fieldDeclaration2374 = new BitSet(new long[]{0x0000000002000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COMMA_in_fieldDeclaration2393 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_fieldDeclaration2397 = new BitSet(new long[]{0x0000000002000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_fieldDeclaration2421 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_variableDeclarator2464 = new BitSet(new long[]{0x0000004000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_variableDeclarator2477 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_variableDeclarator2479 = new BitSet(new long[]{0x0000004000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_EQ_in_variableDeclarator2503 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1872L});
	public static final BitSet FOLLOW_variableInitializer_in_variableDeclarator2507 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceFieldDeclaration_in_interfaceBodyDeclaration2548 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceMethodDeclaration_in_interfaceBodyDeclaration2558 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceDeclaration_in_interfaceBodyDeclaration2568 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_interfaceBodyDeclaration2578 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_interfaceBodyDeclaration2588 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_interfaceMethodDeclaration2608 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000090L});
	public static final BitSet FOLLOW_typeParameters_in_interfaceMethodDeclaration2619 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000010L});
	public static final BitSet FOLLOW_type_in_interfaceMethodDeclaration2641 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_VOID_in_interfaceMethodDeclaration2652 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_interfaceMethodDeclaration2672 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_formalParameters_in_interfaceMethodDeclaration2682 = new BitSet(new long[]{0x0000000000000000L,0x0000400010000004L});
	public static final BitSet FOLLOW_LBRACKET_in_interfaceMethodDeclaration2693 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_interfaceMethodDeclaration2695 = new BitSet(new long[]{0x0000000000000000L,0x0000400010000004L});
	public static final BitSet FOLLOW_THROWS_in_interfaceMethodDeclaration2717 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedNameList_in_interfaceMethodDeclaration2719 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_interfaceMethodDeclaration2732 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_interfaceFieldDeclaration2754 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_interfaceFieldDeclaration2756 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_interfaceFieldDeclaration2758 = new BitSet(new long[]{0x0000000002000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COMMA_in_interfaceFieldDeclaration2769 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_interfaceFieldDeclaration2771 = new BitSet(new long[]{0x0000000002000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_interfaceFieldDeclaration2792 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_type2838 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_type2849 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_type2851 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_primitiveType_in_type2874 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_type2885 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_type2887 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_IDENTIFIER_in_classOrInterfaceType2929 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_typeArguments_in_classOrInterfaceType2940 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_DOT_in_classOrInterfaceType2962 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_classOrInterfaceType2964 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_typeArguments_in_classOrInterfaceType2979 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_LT_in_typeArguments3123 = new BitSet(new long[]{0x0840400100214000L,0x0000000020800010L});
	public static final BitSet FOLLOW_typeArgument_in_typeArguments3125 = new BitSet(new long[]{0x0008000002000000L});
	public static final BitSet FOLLOW_COMMA_in_typeArguments3136 = new BitSet(new long[]{0x0840400100214000L,0x0000000020800010L});
	public static final BitSet FOLLOW_typeArgument_in_typeArguments3138 = new BitSet(new long[]{0x0008000002000000L});
	public static final BitSet FOLLOW_GT_in_typeArguments3160 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_typeArgument3180 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUES_in_typeArgument3190 = new BitSet(new long[]{0x0000010000000002L,0x0000010000000000L});
	public static final BitSet FOLLOW_set_in_typeArgument3214 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_typeArgument3258 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualifiedName_in_qualifiedNameList3289 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_qualifiedNameList3300 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedName_in_qualifiedNameList3302 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_LPAREN_in_formalParameters3333 = new BitSet(new long[]{0x0840500100214000L,0x0000000028000210L});
	public static final BitSet FOLLOW_formalParameterDecls_in_formalParameters3344 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_formalParameters3366 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ellipsisParameterDecl_in_formalParameterDecls3386 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalParameterDecl_in_formalParameterDecls3396 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_formalParameterDecls3407 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_normalParameterDecl_in_formalParameterDecls3409 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_normalParameterDecl_in_formalParameterDecls3431 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_COMMA_in_formalParameterDecls3441 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_ellipsisParameterDecl_in_formalParameterDecls3463 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variableModifiers_in_normalParameterDecl3483 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_normalParameterDecl3485 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_normalParameterDecl3487 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_normalParameterDecl3498 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_normalParameterDecl3500 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_variableModifiers_in_ellipsisParameterDecl3531 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_ellipsisParameterDecl3541 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_ELLIPSIS_in_ellipsisParameterDecl3544 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_ellipsisParameterDecl3554 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation3576 = new BitSet(new long[]{0x0000000000000000L,0x0000110000000000L});
	public static final BitSet FOLLOW_set_in_explicitConstructorInvocation3602 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_explicitConstructorInvocation3634 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_explicitConstructorInvocation3636 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primary_in_explicitConstructorInvocation3647 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_DOT_in_explicitConstructorInvocation3657 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000080L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation3668 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_SUPER_in_explicitConstructorInvocation3689 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_explicitConstructorInvocation3699 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_explicitConstructorInvocation3701 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_qualifiedName3721 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_DOT_in_qualifiedName3732 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_qualifiedName3734 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_annotation_in_annotations3766 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
	public static final BitSet FOLLOW_MONKEYS_AT_in_annotation3799 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedName_in_annotation3801 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_annotation3815 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0280A1A72L});
	public static final BitSet FOLLOW_elementValuePairs_in_annotation3839 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_elementValue_in_annotation3863 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_annotation3899 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_elementValuePair_in_elementValuePairs3931 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_elementValuePairs3942 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_elementValuePair_in_elementValuePairs3944 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_elementValuePair3975 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_elementValuePair3977 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1A72L});
	public static final BitSet FOLLOW_elementValue_in_elementValuePair3979 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_conditionalExpression_in_elementValue3999 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotation_in_elementValue4009 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_elementValueArrayInitializer_in_elementValue4019 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_elementValueArrayInitializer4039 = new BitSet(new long[]{0x2840C80302614200L,0x000A91B0210A1A72L});
	public static final BitSet FOLLOW_elementValue_in_elementValueArrayInitializer4050 = new BitSet(new long[]{0x0000000002000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_COMMA_in_elementValueArrayInitializer4065 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1A72L});
	public static final BitSet FOLLOW_elementValue_in_elementValueArrayInitializer4067 = new BitSet(new long[]{0x0000000002000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_COMMA_in_elementValueArrayInitializer4096 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_RBRACE_in_elementValueArrayInitializer4100 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_annotationTypeDeclaration4123 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_MONKEYS_AT_in_annotationTypeDeclaration4125 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_INTERFACE_in_annotationTypeDeclaration4135 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_annotationTypeDeclaration4145 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_annotationTypeBody_in_annotationTypeDeclaration4155 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_annotationTypeBody4176 = new BitSet(new long[]{0x1840502100A14010L,0x0011040C31700610L});
	public static final BitSet FOLLOW_annotationTypeElementDeclaration_in_annotationTypeBody4188 = new BitSet(new long[]{0x1840502100A14010L,0x0011040C31700610L});
	public static final BitSet FOLLOW_RBRACE_in_annotationTypeBody4210 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotationMethodDeclaration_in_annotationTypeElementDeclaration4232 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceFieldDeclaration_in_annotationTypeElementDeclaration4242 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalClassDeclaration_in_annotationTypeElementDeclaration4252 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalInterfaceDeclaration_in_annotationTypeElementDeclaration4262 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumDeclaration_in_annotationTypeElementDeclaration4272 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotationTypeDeclaration_in_annotationTypeElementDeclaration4282 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_annotationTypeElementDeclaration4292 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_annotationMethodDeclaration4312 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_annotationMethodDeclaration4314 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_annotationMethodDeclaration4316 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_annotationMethodDeclaration4326 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_annotationMethodDeclaration4328 = new BitSet(new long[]{0x0000000020000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_DEFAULT_in_annotationMethodDeclaration4331 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1A72L});
	public static final BitSet FOLLOW_elementValue_in_annotationMethodDeclaration4333 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_annotationMethodDeclaration4362 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_block4386 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_blockStatement_in_block4397 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_RBRACE_in_block4418 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_localVariableDeclarationStatement_in_blockStatement4440 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_blockStatement4450 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_statement_in_blockStatement4460 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_localVariableDeclaration_in_localVariableDeclarationStatement4481 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_localVariableDeclarationStatement4491 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variableModifiers_in_localVariableDeclaration4511 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_localVariableDeclaration4513 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_localVariableDeclaration4523 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_localVariableDeclaration4534 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_localVariableDeclaration4536 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_block_in_statement4567 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASSERT_in_statement4591 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_statement4611 = new BitSet(new long[]{0x0000000001000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COLON_in_statement4614 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_statement4616 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement4620 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASSERT_in_statement4630 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_statement4633 = new BitSet(new long[]{0x0000000001000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COLON_in_statement4636 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_statement4638 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement4642 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IF_in_statement4664 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_parExpression_in_statement4666 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_statement4668 = new BitSet(new long[]{0x0000001000000002L});
	public static final BitSet FOLLOW_ELSE_in_statement4671 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_statement4673 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_forstatement_in_statement4695 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WHILE_in_statement4705 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_parExpression_in_statement4707 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_statement4709 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DO_in_statement4719 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_statement4721 = new BitSet(new long[]{0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_WHILE_in_statement4723 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_parExpression_in_statement4725 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement4727 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_trystatement_in_statement4737 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SWITCH_in_statement4747 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_parExpression_in_statement4749 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_statement4751 = new BitSet(new long[]{0x0000000020080000L,0x0000000001000000L});
	public static final BitSet FOLLOW_switchBlockStatementGroups_in_statement4753 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_RBRACE_in_statement4755 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SYNCHRONIZED_in_statement4765 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_parExpression_in_statement4767 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_statement4769 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_RETURN_in_statement4779 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0300A1870L});
	public static final BitSet FOLLOW_expression_in_statement4782 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement4787 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_THROW_in_statement4797 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_statement4799 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement4801 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BREAK_in_statement4811 = new BitSet(new long[]{0x0040000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_statement4826 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement4843 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CONTINUE_in_statement4853 = new BitSet(new long[]{0x0040000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_statement4868 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement4885 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expression_in_statement4895 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement4898 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_statement4913 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_statement4915 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_statement4917 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_statement4927 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_switchBlockStatementGroup_in_switchBlockStatementGroups4949 = new BitSet(new long[]{0x0000000020080002L});
	public static final BitSet FOLLOW_switchLabel_in_switchBlockStatementGroup4978 = new BitSet(new long[]{0x38C1D82350E1C312L,0x003FB7BC347A1E72L});
	public static final BitSet FOLLOW_blockStatement_in_switchBlockStatementGroup4989 = new BitSet(new long[]{0x38C1D82350E1C312L,0x003FB7BC347A1E72L});
	public static final BitSet FOLLOW_CASE_in_switchLabel5020 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_switchLabel5022 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_switchLabel5024 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DEFAULT_in_switchLabel5034 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_switchLabel5036 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TRY_in_trystatement5057 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_trystatement5059 = new BitSet(new long[]{0x0000200000100000L});
	public static final BitSet FOLLOW_catches_in_trystatement5073 = new BitSet(new long[]{0x0000200000000000L});
	public static final BitSet FOLLOW_FINALLY_in_trystatement5075 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_trystatement5077 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_catches_in_trystatement5091 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FINALLY_in_trystatement5105 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_trystatement5107 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_catchClause_in_catches5138 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_catchClause_in_catches5149 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_CATCH_in_catchClause5180 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_catchClause5182 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_formalParameter_in_catchClause5184 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_catchClause5194 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_catchClause5196 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variableModifiers_in_formalParameter5217 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_formalParameter5219 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_formalParameter5221 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_formalParameter5232 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_formalParameter5234 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_FOR_in_forstatement5283 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_forstatement5285 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_variableModifiers_in_forstatement5287 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_forstatement5289 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_forstatement5291 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_forstatement5293 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_forstatement5304 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_forstatement5306 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_forstatement5308 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FOR_in_forstatement5340 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_forstatement5342 = new BitSet(new long[]{0x2840D80300614200L,0x000A91B0300A1A70L});
	public static final BitSet FOLLOW_forInit_in_forstatement5362 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_forstatement5383 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0300A1870L});
	public static final BitSet FOLLOW_expression_in_forstatement5403 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_forstatement5424 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0280A1870L});
	public static final BitSet FOLLOW_expressionList_in_forstatement5444 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_forstatement5465 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_forstatement5467 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_localVariableDeclaration_in_forInit5487 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expressionList_in_forInit5497 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_parExpression5517 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_parExpression5519 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_parExpression5521 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expression_in_expressionList5541 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_expressionList5552 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_expressionList5554 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_conditionalExpression_in_expression5586 = new BitSet(new long[]{0x0008004000042082L,0x0000004280050080L});
	public static final BitSet FOLLOW_assignmentOperator_in_expression5597 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_expression5599 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_EQ_in_assignmentOperator5631 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PLUSEQ_in_assignmentOperator5641 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUBEQ_in_assignmentOperator5651 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STAREQ_in_assignmentOperator5661 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SLASHEQ_in_assignmentOperator5671 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AMPEQ_in_assignmentOperator5681 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BAREQ_in_assignmentOperator5691 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CARETEQ_in_assignmentOperator5701 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PERCENTEQ_in_assignmentOperator5711 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LT_in_assignmentOperator5722 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_LT_in_assignmentOperator5724 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_assignmentOperator5726 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_assignmentOperator5737 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_assignmentOperator5739 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_assignmentOperator5741 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_assignmentOperator5743 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_assignmentOperator5754 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_assignmentOperator5756 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_assignmentOperator5758 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_conditionalOrExpression_in_conditionalExpression5779 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
	public static final BitSet FOLLOW_QUES_in_conditionalExpression5790 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_conditionalExpression5792 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_conditionalExpression5794 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_conditionalExpression_in_conditionalExpression5796 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression5827 = new BitSet(new long[]{0x0000000000001002L});
	public static final BitSet FOLLOW_BARBAR_in_conditionalOrExpression5838 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression5840 = new BitSet(new long[]{0x0000000000001002L});
	public static final BitSet FOLLOW_inclusiveOrExpression_in_conditionalAndExpression5871 = new BitSet(new long[]{0x0000000000000042L});
	public static final BitSet FOLLOW_AMPAMP_in_conditionalAndExpression5882 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_inclusiveOrExpression_in_conditionalAndExpression5884 = new BitSet(new long[]{0x0000000000000042L});
	public static final BitSet FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression5915 = new BitSet(new long[]{0x0000000000000802L});
	public static final BitSet FOLLOW_BAR_in_inclusiveOrExpression5926 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression5928 = new BitSet(new long[]{0x0000000000000802L});
	public static final BitSet FOLLOW_andExpression_in_exclusiveOrExpression5959 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_CARET_in_exclusiveOrExpression5970 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_andExpression_in_exclusiveOrExpression5972 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_equalityExpression_in_andExpression6003 = new BitSet(new long[]{0x0000000000000022L});
	public static final BitSet FOLLOW_AMP_in_andExpression6014 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_equalityExpression_in_andExpression6016 = new BitSet(new long[]{0x0000000000000022L});
	public static final BitSet FOLLOW_instanceOfExpression_in_equalityExpression6047 = new BitSet(new long[]{0x0000008000000402L});
	public static final BitSet FOLLOW_set_in_equalityExpression6074 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_instanceOfExpression_in_equalityExpression6124 = new BitSet(new long[]{0x0000008000000402L});
	public static final BitSet FOLLOW_relationalExpression_in_instanceOfExpression6155 = new BitSet(new long[]{0x0400000000000002L});
	public static final BitSet FOLLOW_INSTANCEOF_in_instanceOfExpression6166 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_instanceOfExpression6168 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_shiftExpression_in_relationalExpression6199 = new BitSet(new long[]{0x0008000000000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_relationalOp_in_relationalExpression6210 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_shiftExpression_in_relationalExpression6212 = new BitSet(new long[]{0x0008000000000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_LT_in_relationalOp6244 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_relationalOp6246 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_relationalOp6257 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_relationalOp6259 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LT_in_relationalOp6269 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_relationalOp6279 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_additiveExpression_in_shiftExpression6299 = new BitSet(new long[]{0x0008000000000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_shiftOp_in_shiftExpression6310 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_additiveExpression_in_shiftExpression6312 = new BitSet(new long[]{0x0008000000000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_LT_in_shiftOp6345 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_LT_in_shiftOp6347 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_shiftOp6358 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_shiftOp6360 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_shiftOp6362 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_shiftOp6373 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_shiftOp6375 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression6396 = new BitSet(new long[]{0x0000000000000002L,0x0000002000020000L});
	public static final BitSet FOLLOW_set_in_additiveExpression6423 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression6473 = new BitSet(new long[]{0x0000000000000002L,0x0000002000020000L});
	public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression6511 = new BitSet(new long[]{0x0000000000000002L,0x0000000140008000L});
	public static final BitSet FOLLOW_set_in_multiplicativeExpression6538 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression6606 = new BitSet(new long[]{0x0000000000000002L,0x0000000140008000L});
	public static final BitSet FOLLOW_PLUS_in_unaryExpression6639 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpression6642 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUB_in_unaryExpression6652 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpression6654 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PLUSPLUS_in_unaryExpression6664 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpression6666 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUBSUB_in_unaryExpression6676 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpression6678 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unaryExpressionNotPlusMinus_in_unaryExpression6688 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TILDE_in_unaryExpressionNotPlusMinus6708 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus6710 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BANG_in_unaryExpressionNotPlusMinus6720 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus6722 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_castExpression_in_unaryExpressionNotPlusMinus6732 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primary_in_unaryExpressionNotPlusMinus6742 = new BitSet(new long[]{0x0000000080000002L,0x0000008000080004L});
	public static final BitSet FOLLOW_selector_in_unaryExpressionNotPlusMinus6753 = new BitSet(new long[]{0x0000000080000002L,0x0000008000080004L});
	public static final BitSet FOLLOW_LPAREN_in_castExpression6823 = new BitSet(new long[]{0x0800400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_primitiveType_in_castExpression6825 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_castExpression6827 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_castExpression6829 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_castExpression6839 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_castExpression6841 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_castExpression6843 = new BitSet(new long[]{0x2840C80300614200L,0x000A911020001870L});
	public static final BitSet FOLLOW_unaryExpressionNotPlusMinus_in_castExpression6845 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_parExpression_in_primary6867 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_THIS_in_primary6889 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000044L});
	public static final BitSet FOLLOW_DOT_in_primary6900 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_primary6902 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000044L});
	public static final BitSet FOLLOW_identifierSuffix_in_primary6924 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_primary6945 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000044L});
	public static final BitSet FOLLOW_DOT_in_primary6956 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_primary6958 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000044L});
	public static final BitSet FOLLOW_identifierSuffix_in_primary6980 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUPER_in_primary7001 = new BitSet(new long[]{0x0000000080000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_superSuffix_in_primary7011 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_literal_in_primary7021 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_creator_in_primary7031 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primitiveType_in_primary7041 = new BitSet(new long[]{0x0000000080000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_primary7052 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_primary7054 = new BitSet(new long[]{0x0000000080000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_DOT_in_primary7075 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_primary7077 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VOID_in_primary7087 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_DOT_in_primary7089 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_primary7091 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arguments_in_superSuffix7117 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_superSuffix7127 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_typeArguments_in_superSuffix7130 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_superSuffix7151 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_superSuffix7162 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_identifierSuffix7195 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_identifierSuffix7197 = new BitSet(new long[]{0x0000000080000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_DOT_in_identifierSuffix7218 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_identifierSuffix7220 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_identifierSuffix7231 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_identifierSuffix7233 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_identifierSuffix7235 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_arguments_in_identifierSuffix7256 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_identifierSuffix7266 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_identifierSuffix7268 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_identifierSuffix7278 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_identifierSuffix7280 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_identifierSuffix7282 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_identifierSuffix7284 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_identifierSuffix7294 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_THIS_in_identifierSuffix7296 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_identifierSuffix7306 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_SUPER_in_identifierSuffix7308 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_identifierSuffix7310 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_innerCreator_in_identifierSuffix7320 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_selector7342 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_selector7344 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_selector7355 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_selector7376 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_THIS_in_selector7378 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_selector7388 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_SUPER_in_selector7390 = new BitSet(new long[]{0x0000000080000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_superSuffix_in_selector7400 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_innerCreator_in_selector7410 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_selector7420 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_selector7422 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_selector7424 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_creator7444 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_creator7446 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_creator7448 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_classCreatorRest_in_creator7450 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_creator7460 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_creator7462 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_classCreatorRest_in_creator7464 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arrayCreator_in_creator7474 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_arrayCreator7494 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_createdName_in_arrayCreator7496 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_arrayCreator7506 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_arrayCreator7508 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
	public static final BitSet FOLLOW_LBRACKET_in_arrayCreator7519 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_arrayCreator7521 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
	public static final BitSet FOLLOW_arrayInitializer_in_arrayCreator7542 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_arrayCreator7553 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_createdName_in_arrayCreator7555 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_arrayCreator7565 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_arrayCreator7567 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_arrayCreator7577 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_arrayCreator7591 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_arrayCreator7593 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_arrayCreator7607 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_arrayCreator7629 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_arrayCreator7631 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_arrayInitializer_in_variableInitializer7662 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expression_in_variableInitializer7672 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_arrayInitializer7692 = new BitSet(new long[]{0x2840C80302614200L,0x000A91B0210A1872L});
	public static final BitSet FOLLOW_variableInitializer_in_arrayInitializer7708 = new BitSet(new long[]{0x0000000002000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_COMMA_in_arrayInitializer7727 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1872L});
	public static final BitSet FOLLOW_variableInitializer_in_arrayInitializer7729 = new BitSet(new long[]{0x0000000002000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_COMMA_in_arrayInitializer7779 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_RBRACE_in_arrayInitializer7792 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_createdName7826 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primitiveType_in_createdName7836 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_innerCreator7857 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_NEW_in_innerCreator7859 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_innerCreator7870 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_innerCreator7891 = new BitSet(new long[]{0x0000000000000000L,0x00000000000000C0L});
	public static final BitSet FOLLOW_typeArguments_in_innerCreator7902 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_classCreatorRest_in_innerCreator7923 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arguments_in_classCreatorRest7944 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
	public static final BitSet FOLLOW_classBody_in_classCreatorRest7955 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LT_in_nonWildcardTypeArguments7987 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_typeList_in_nonWildcardTypeArguments7989 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_nonWildcardTypeArguments7999 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_arguments8019 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0280A1870L});
	public static final BitSet FOLLOW_expressionList_in_arguments8022 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_arguments8035 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_classHeader8159 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_classHeader8161 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_classHeader8163 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_enumHeader8183 = new BitSet(new long[]{0x0040002000000000L});
	public static final BitSet FOLLOW_set_in_enumHeader8185 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_enumHeader8191 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_interfaceHeader8211 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_INTERFACE_in_interfaceHeader8213 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_interfaceHeader8215 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_annotationHeader8235 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_MONKEYS_AT_in_annotationHeader8237 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_INTERFACE_in_annotationHeader8239 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_annotationHeader8241 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_typeHeader8261 = new BitSet(new long[]{0x1000002000800000L,0x0000000000000200L});
	public static final BitSet FOLLOW_CLASS_in_typeHeader8264 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_ENUM_in_typeHeader8266 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_MONKEYS_AT_in_typeHeader8269 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_INTERFACE_in_typeHeader8273 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_typeHeader8277 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_methodHeader8297 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000090L});
	public static final BitSet FOLLOW_typeParameters_in_methodHeader8299 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000010L});
	public static final BitSet FOLLOW_type_in_methodHeader8303 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_VOID_in_methodHeader8305 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_methodHeader8309 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_methodHeader8311 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_fieldHeader8331 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_fieldHeader8333 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_fieldHeader8335 = new BitSet(new long[]{0x0000004002000000L,0x0000000010000004L});
	public static final BitSet FOLLOW_LBRACKET_in_fieldHeader8338 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_fieldHeader8339 = new BitSet(new long[]{0x0000004002000000L,0x0000000010000004L});
	public static final BitSet FOLLOW_set_in_fieldHeader8343 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variableModifiers_in_localVariableHeader8369 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_localVariableHeader8371 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_localVariableHeader8373 = new BitSet(new long[]{0x0000004002000000L,0x0000000010000004L});
	public static final BitSet FOLLOW_LBRACKET_in_localVariableHeader8376 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_localVariableHeader8377 = new BitSet(new long[]{0x0000004002000000L,0x0000000010000004L});
	public static final BitSet FOLLOW_set_in_localVariableHeader8381 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotations_in_synpred2_Java127 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_packageDeclaration_in_synpred2_Java156 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_synpred12_Java514 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalClassDeclaration_in_synpred27_Java877 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalInterfaceDeclaration_in_synpred43_Java1556 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fieldDeclaration_in_synpred52_Java1885 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_methodDeclaration_in_synpred53_Java1896 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_synpred54_Java1907 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_explicitConstructorInvocation_in_synpred57_Java2061 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_synpred59_Java1973 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_typeParameters_in_synpred59_Java1984 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred59_Java2005 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_formalParameters_in_synpred59_Java2015 = new BitSet(new long[]{0x0000000000000000L,0x0000400000000002L});
	public static final BitSet FOLLOW_THROWS_in_synpred59_Java2026 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedNameList_in_synpred59_Java2028 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_synpred59_Java2049 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1EF2L});
	public static final BitSet FOLLOW_explicitConstructorInvocation_in_synpred59_Java2061 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_blockStatement_in_synpred59_Java2083 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_RBRACE_in_synpred59_Java2104 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceFieldDeclaration_in_synpred68_Java2548 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceMethodDeclaration_in_synpred69_Java2558 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceDeclaration_in_synpred70_Java2568 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_synpred71_Java2578 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ellipsisParameterDecl_in_synpred96_Java3386 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalParameterDecl_in_synpred98_Java3396 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_synpred98_Java3407 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_normalParameterDecl_in_synpred98_Java3409 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_normalParameterDecl_in_synpred99_Java3431 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_COMMA_in_synpred99_Java3441 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_synpred103_Java3576 = new BitSet(new long[]{0x0000000000000000L,0x0000110000000000L});
	public static final BitSet FOLLOW_set_in_synpred103_Java3602 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_synpred103_Java3634 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_synpred103_Java3636 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotationMethodDeclaration_in_synpred117_Java4232 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceFieldDeclaration_in_synpred118_Java4242 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalClassDeclaration_in_synpred119_Java4252 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalInterfaceDeclaration_in_synpred120_Java4262 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumDeclaration_in_synpred121_Java4272 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotationTypeDeclaration_in_synpred122_Java4282 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_localVariableDeclarationStatement_in_synpred125_Java4440 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_synpred126_Java4450 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASSERT_in_synpred130_Java4591 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred130_Java4611 = new BitSet(new long[]{0x0000000001000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COLON_in_synpred130_Java4614 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred130_Java4616 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_synpred130_Java4620 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASSERT_in_synpred132_Java4630 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred132_Java4633 = new BitSet(new long[]{0x0000000001000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COLON_in_synpred132_Java4636 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred132_Java4638 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_synpred132_Java4642 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ELSE_in_synpred133_Java4671 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_synpred133_Java4673 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expression_in_synpred148_Java4895 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_synpred148_Java4898 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred149_Java4913 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_synpred149_Java4915 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_synpred149_Java4917 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_catches_in_synpred153_Java5073 = new BitSet(new long[]{0x0000200000000000L});
	public static final BitSet FOLLOW_FINALLY_in_synpred153_Java5075 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_synpred153_Java5077 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_catches_in_synpred154_Java5091 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FOR_in_synpred157_Java5283 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_synpred157_Java5285 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_variableModifiers_in_synpred157_Java5287 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_synpred157_Java5289 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred157_Java5291 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_synpred157_Java5293 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred157_Java5304 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_synpred157_Java5306 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_synpred157_Java5308 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_localVariableDeclaration_in_synpred161_Java5487 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_castExpression_in_synpred202_Java6732 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_synpred206_Java6823 = new BitSet(new long[]{0x0800400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_primitiveType_in_synpred206_Java6825 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_synpred206_Java6827 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_synpred206_Java6829 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_synpred208_Java6900 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred208_Java6902 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identifierSuffix_in_synpred209_Java6924 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_synpred211_Java6956 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred211_Java6958 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identifierSuffix_in_synpred212_Java6980 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_synpred224_Java7231 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred224_Java7233 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_synpred224_Java7235 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_synpred236_Java7444 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_synpred236_Java7446 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_synpred236_Java7448 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_classCreatorRest_in_synpred236_Java7450 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_synpred237_Java7460 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_synpred237_Java7462 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_classCreatorRest_in_synpred237_Java7464 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_synpred239_Java7494 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_createdName_in_synpred239_Java7496 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_synpred239_Java7506 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_synpred239_Java7508 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
	public static final BitSet FOLLOW_LBRACKET_in_synpred239_Java7519 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_synpred239_Java7521 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
	public static final BitSet FOLLOW_arrayInitializer_in_synpred239_Java7542 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_synpred240_Java7591 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred240_Java7593 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_synpred240_Java7607 = new BitSet(new long[]{0x0000000000000002L});
}
