// $ANTLR 3.5 src/main/resources/parser/Java.g 2014-03-11 18:17:40

    package parser;
    import parser.util.ParserUtil;
    import parser.descr.*;
    import parser.descr.ElementDescriptor.ElementType;


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


	public static class packageDeclaration_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "packageDeclaration"
	// src/main/resources/parser/Java.g:327:1: packageDeclaration : p= 'package' qualifiedName ';' ;
	public final JavaParser.packageDeclaration_return packageDeclaration() throws RecognitionException {
		JavaParser.packageDeclaration_return retval = new JavaParser.packageDeclaration_return();
		retval.start = input.LT(1);
		int packageDeclaration_StartIndex = input.index();

		Token p=null;


		        PackageDescr packageDescr = null;
		        if (!isBacktracking()) {
		            log("Start package declaration.");
		            packageDescr = new PackageDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		            context.push(packageDescr);
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 2) ) { return retval; }

			// src/main/resources/parser/Java.g:346:5: (p= 'package' qualifiedName ';' )
			// src/main/resources/parser/Java.g:346:9: p= 'package' qualifiedName ';'
			{
			p=(Token)match(input,PACKAGE,FOLLOW_PACKAGE_in_packageDeclaration250); if (state.failed) return retval;
			pushFollow(FOLLOW_qualifiedName_in_packageDeclaration252);
			qualifiedName();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) { packageDescr.setPackageToken(new TextTokenElementDescr((p!=null?p.getText():null), line(p), position(p))); }
			match(input,SEMI,FOLLOW_SEMI_in_packageDeclaration264); if (state.failed) return retval;
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        packageDescr = popPackage();
			        if (packageDescr != null) {
			            updateOnAfter(packageDescr, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
			            processPackage(packageDescr);
			            log("End of package declaration.");
			        } else {
			            log("A PackageDescr is expected");
			        }
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
		return retval;
	}
	// $ANTLR end "packageDeclaration"



	// $ANTLR start "importDeclaration"
	// src/main/resources/parser/Java.g:350:1: importDeclaration : ( 'import' ( 'static' )? IDENTIFIER '.' '*' ';' | 'import' ( 'static' )? IDENTIFIER ( '.' IDENTIFIER )+ ( '.' '*' )? ';' );
	public final void importDeclaration() throws RecognitionException {
		int importDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 3) ) { return; }

			// src/main/resources/parser/Java.g:351:5: ( 'import' ( 'static' )? IDENTIFIER '.' '*' ';' | 'import' ( 'static' )? IDENTIFIER ( '.' IDENTIFIER )+ ( '.' '*' )? ';' )
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
					// src/main/resources/parser/Java.g:351:9: 'import' ( 'static' )? IDENTIFIER '.' '*' ';'
					{
					match(input,IMPORT,FOLLOW_IMPORT_in_importDeclaration285); if (state.failed) return;
					// src/main/resources/parser/Java.g:352:9: ( 'static' )?
					int alt5=2;
					int LA5_0 = input.LA(1);
					if ( (LA5_0==STATIC) ) {
						alt5=1;
					}
					switch (alt5) {
						case 1 :
							// src/main/resources/parser/Java.g:352:10: 'static'
							{
							match(input,STATIC,FOLLOW_STATIC_in_importDeclaration297); if (state.failed) return;
							}
							break;

					}

					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_importDeclaration318); if (state.failed) return;
					match(input,DOT,FOLLOW_DOT_in_importDeclaration320); if (state.failed) return;
					match(input,STAR,FOLLOW_STAR_in_importDeclaration322); if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_importDeclaration332); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:356:9: 'import' ( 'static' )? IDENTIFIER ( '.' IDENTIFIER )+ ( '.' '*' )? ';'
					{
					match(input,IMPORT,FOLLOW_IMPORT_in_importDeclaration349); if (state.failed) return;
					// src/main/resources/parser/Java.g:357:9: ( 'static' )?
					int alt6=2;
					int LA6_0 = input.LA(1);
					if ( (LA6_0==STATIC) ) {
						alt6=1;
					}
					switch (alt6) {
						case 1 :
							// src/main/resources/parser/Java.g:357:10: 'static'
							{
							match(input,STATIC,FOLLOW_STATIC_in_importDeclaration361); if (state.failed) return;
							}
							break;

					}

					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_importDeclaration382); if (state.failed) return;
					// src/main/resources/parser/Java.g:360:9: ( '.' IDENTIFIER )+
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
							// src/main/resources/parser/Java.g:360:10: '.' IDENTIFIER
							{
							match(input,DOT,FOLLOW_DOT_in_importDeclaration393); if (state.failed) return;
							match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_importDeclaration395); if (state.failed) return;
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

					// src/main/resources/parser/Java.g:362:9: ( '.' '*' )?
					int alt8=2;
					int LA8_0 = input.LA(1);
					if ( (LA8_0==DOT) ) {
						alt8=1;
					}
					switch (alt8) {
						case 1 :
							// src/main/resources/parser/Java.g:362:10: '.' '*'
							{
							match(input,DOT,FOLLOW_DOT_in_importDeclaration417); if (state.failed) return;
							match(input,STAR,FOLLOW_STAR_in_importDeclaration419); if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_importDeclaration440); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:367:1: qualifiedImportName : IDENTIFIER ( '.' IDENTIFIER )* ;
	public final void qualifiedImportName() throws RecognitionException {
		int qualifiedImportName_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 4) ) { return; }

			// src/main/resources/parser/Java.g:368:5: ( IDENTIFIER ( '.' IDENTIFIER )* )
			// src/main/resources/parser/Java.g:368:9: IDENTIFIER ( '.' IDENTIFIER )*
			{
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_qualifiedImportName460); if (state.failed) return;
			// src/main/resources/parser/Java.g:369:9: ( '.' IDENTIFIER )*
			loop10:
			while (true) {
				int alt10=2;
				int LA10_0 = input.LA(1);
				if ( (LA10_0==DOT) ) {
					alt10=1;
				}

				switch (alt10) {
				case 1 :
					// src/main/resources/parser/Java.g:369:10: '.' IDENTIFIER
					{
					match(input,DOT,FOLLOW_DOT_in_qualifiedImportName471); if (state.failed) return;
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_qualifiedImportName473); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:373:1: typeDeclaration : ( classOrInterfaceDeclaration | ';' );
	public final void typeDeclaration() throws RecognitionException {
		int typeDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 5) ) { return; }

			// src/main/resources/parser/Java.g:374:5: ( classOrInterfaceDeclaration | ';' )
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
					// src/main/resources/parser/Java.g:374:9: classOrInterfaceDeclaration
					{
					pushFollow(FOLLOW_classOrInterfaceDeclaration_in_typeDeclaration504);
					classOrInterfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:375:9: ';'
					{
					match(input,SEMI,FOLLOW_SEMI_in_typeDeclaration514); if (state.failed) return;
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


	public static class classOrInterfaceDeclaration_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "classOrInterfaceDeclaration"
	// src/main/resources/parser/Java.g:378:1: classOrInterfaceDeclaration : ( classDeclaration | interfaceDeclaration );
	public final JavaParser.classOrInterfaceDeclaration_return classOrInterfaceDeclaration() throws RecognitionException {
		JavaParser.classOrInterfaceDeclaration_return retval = new JavaParser.classOrInterfaceDeclaration_return();
		retval.start = input.LT(1);
		int classOrInterfaceDeclaration_StartIndex = input.index();

		ParserRuleReturnScope classDeclaration1 =null;

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 6) ) { return retval; }

			// src/main/resources/parser/Java.g:379:5: ( classDeclaration | interfaceDeclaration )
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
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 12, 0, input);
				throw nvae;
			}
			switch (alt12) {
				case 1 :
					// src/main/resources/parser/Java.g:379:10: classDeclaration
					{
					if ( state.backtracking==0 ) {
					            if (!isBacktracking()) {
					                increaseClassLevel();
					                if (isDeclaringMainClass()) {
					                    ClassDescr classDescr = new ClassDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
					                    processClass(classDescr);
					                }
					            }
					         }
					pushFollow(FOLLOW_classDeclaration_in_classOrInterfaceDeclaration537);
					classDeclaration1=classDeclaration();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					                if (isDeclaringMainClass()) {
					                    ClassDescr classDescr = popClass();
					                    updateOnAfter(classDescr, (classDeclaration1!=null?input.toString(classDeclaration1.start,classDeclaration1.stop):null), (CommonToken)(classDeclaration1!=null?(classDeclaration1.start):null), (CommonToken)(classDeclaration1!=null?(classDeclaration1.stop):null));
					                }
					                decreaseClassLevel();
					        }
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:394:9: interfaceDeclaration
					{
					pushFollow(FOLLOW_interfaceDeclaration_in_classOrInterfaceDeclaration549);
					interfaceDeclaration();
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
			if ( state.backtracking>0 ) { memoize(input, 6, classOrInterfaceDeclaration_StartIndex); }

		}
		return retval;
	}
	// $ANTLR end "classOrInterfaceDeclaration"


	public static class modifiers_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "modifiers"
	// src/main/resources/parser/Java.g:398:1: modifiers : ( annotation |s= 'public' |s= 'protected' |s= 'private' |s= 'static' |s= 'abstract' |s= 'final' |s= 'native' |s= 'synchronized' |s= 'transient' |s= 'volatile' |s= 'strictfp' )* ;
	public final JavaParser.modifiers_return modifiers() throws RecognitionException {
		JavaParser.modifiers_return retval = new JavaParser.modifiers_return();
		retval.start = input.LT(1);
		int modifiers_StartIndex = input.index();

		Token s=null;


		        ModifierListDescr modifiers = null;
		        if (!isBacktracking()) {
		            log("Start modifier list declaration.");
		            modifiers = new ModifierListDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		            context.push(modifiers);
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 7) ) { return retval; }

			// src/main/resources/parser/Java.g:417:5: ( ( annotation |s= 'public' |s= 'protected' |s= 'private' |s= 'static' |s= 'abstract' |s= 'final' |s= 'native' |s= 'synchronized' |s= 'transient' |s= 'volatile' |s= 'strictfp' )* )
			// src/main/resources/parser/Java.g:418:5: ( annotation |s= 'public' |s= 'protected' |s= 'private' |s= 'static' |s= 'abstract' |s= 'final' |s= 'native' |s= 'synchronized' |s= 'transient' |s= 'volatile' |s= 'strictfp' )*
			{
			// src/main/resources/parser/Java.g:418:5: ( annotation |s= 'public' |s= 'protected' |s= 'private' |s= 'static' |s= 'abstract' |s= 'final' |s= 'native' |s= 'synchronized' |s= 'transient' |s= 'volatile' |s= 'strictfp' )*
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
					// src/main/resources/parser/Java.g:418:10: annotation
					{
					pushFollow(FOLLOW_annotation_in_modifiers594);
					annotation();
					state._fsp--;
					if (state.failed) return retval;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:419:9: s= 'public'
					{
					s=(Token)match(input,PUBLIC,FOLLOW_PUBLIC_in_modifiers606); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null)) );  }
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:420:9: s= 'protected'
					{
					s=(Token)match(input,PROTECTED,FOLLOW_PROTECTED_in_modifiers629); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null)) );  }
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:421:9: s= 'private'
					{
					s=(Token)match(input,PRIVATE,FOLLOW_PRIVATE_in_modifiers649); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null)) );  }
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:422:9: s= 'static'
					{
					s=(Token)match(input,STATIC,FOLLOW_STATIC_in_modifiers671); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null)) );  }
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:423:9: s= 'abstract'
					{
					s=(Token)match(input,ABSTRACT,FOLLOW_ABSTRACT_in_modifiers694); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null)) );  }
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:424:9: s= 'final'
					{
					s=(Token)match(input,FINAL,FOLLOW_FINAL_in_modifiers715); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null)) );  }
					}
					break;
				case 8 :
					// src/main/resources/parser/Java.g:425:9: s= 'native'
					{
					s=(Token)match(input,NATIVE,FOLLOW_NATIVE_in_modifiers739); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null)) );  }
					}
					break;
				case 9 :
					// src/main/resources/parser/Java.g:426:9: s= 'synchronized'
					{
					s=(Token)match(input,SYNCHRONIZED,FOLLOW_SYNCHRONIZED_in_modifiers762); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null)) );  }
					}
					break;
				case 10 :
					// src/main/resources/parser/Java.g:427:9: s= 'transient'
					{
					s=(Token)match(input,TRANSIENT,FOLLOW_TRANSIENT_in_modifiers779); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null)) );  }
					}
					break;
				case 11 :
					// src/main/resources/parser/Java.g:428:9: s= 'volatile'
					{
					s=(Token)match(input,VOLATILE,FOLLOW_VOLATILE_in_modifiers799); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null)) );  }
					}
					break;
				case 12 :
					// src/main/resources/parser/Java.g:429:9: s= 'strictfp'
					{
					s=(Token)match(input,STRICTFP,FOLLOW_STRICTFP_in_modifiers820); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, (s!=null?s.getLine():0), (s!=null?s.getCharPositionInLine():0), (s!=null?s.getText():null)) );  }
					}
					break;

				default :
					break loop13;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        modifiers = popModifierList();
			        if (modifiers != null) {
			            updateOnAfter(modifiers, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
			            processModifiers(modifiers);
			            log("End of modifier list declaration.");
			        } else {
			            log("A ModifierListDescr is expected");
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
		return retval;
	}
	// $ANTLR end "modifiers"


	public static class variableModifiers_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "variableModifiers"
	// src/main/resources/parser/Java.g:434:1: variableModifiers : (s= 'final' | annotation )* ;
	public final JavaParser.variableModifiers_return variableModifiers() throws RecognitionException {
		JavaParser.variableModifiers_return retval = new JavaParser.variableModifiers_return();
		retval.start = input.LT(1);
		int variableModifiers_StartIndex = input.index();

		Token s=null;


		        ModifierListDescr modifiers = null;
		        if (!isBacktracking()) {
		            log("Start variable modifier list declaration.");
		            modifiers = new ModifierListDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		            context.push(modifiers);
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 8) ) { return retval; }

			// src/main/resources/parser/Java.g:453:5: ( (s= 'final' | annotation )* )
			// src/main/resources/parser/Java.g:453:9: (s= 'final' | annotation )*
			{
			// src/main/resources/parser/Java.g:453:9: (s= 'final' | annotation )*
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
					// src/main/resources/parser/Java.g:453:13: s= 'final'
					{
					s=(Token)match(input,FINAL,FOLLOW_FINAL_in_variableModifiers881); if (state.failed) return retval;
					if ( state.backtracking==0 ) { modifiers.add( new ModifierDescr((s!=null?s.getText():null), -1, -1, line(s), position(s), (s!=null?s.getText():null)) ); }
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:454:13: annotation
					{
					pushFollow(FOLLOW_annotation_in_variableModifiers902);
					annotation();
					state._fsp--;
					if (state.failed) return retval;
					}
					break;

				default :
					break loop14;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        modifiers = popModifierList();
			        if (modifiers != null) {
			            updateOnAfter(modifiers, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
			            processModifiers(modifiers);
			            log("End of variable modifiers list declaration.");
			        } else {
			            log("A ModifierListDescr is expected");
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
		return retval;
	}
	// $ANTLR end "variableModifiers"


	public static class classDeclaration_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "classDeclaration"
	// src/main/resources/parser/Java.g:459:1: classDeclaration : ( normalClassDeclaration | enumDeclaration );
	public final JavaParser.classDeclaration_return classDeclaration() throws RecognitionException {
		JavaParser.classDeclaration_return retval = new JavaParser.classDeclaration_return();
		retval.start = input.LT(1);
		int classDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 9) ) { return retval; }

			// src/main/resources/parser/Java.g:460:5: ( normalClassDeclaration | enumDeclaration )
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
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}
			switch (alt15) {
				case 1 :
					// src/main/resources/parser/Java.g:460:9: normalClassDeclaration
					{
					pushFollow(FOLLOW_normalClassDeclaration_in_classDeclaration938);
					normalClassDeclaration();
					state._fsp--;
					if (state.failed) return retval;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:461:9: enumDeclaration
					{
					pushFollow(FOLLOW_enumDeclaration_in_classDeclaration948);
					enumDeclaration();
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
			if ( state.backtracking>0 ) { memoize(input, 9, classDeclaration_StartIndex); }

		}
		return retval;
	}
	// $ANTLR end "classDeclaration"



	// $ANTLR start "normalClassDeclaration"
	// src/main/resources/parser/Java.g:464:1: normalClassDeclaration : modifiers c= 'class' id= IDENTIFIER ( typeParameters )? (e= 'extends' type )? ( 'implements' typeList )? classBody ;
	public final void normalClassDeclaration() throws RecognitionException {
		int normalClassDeclaration_StartIndex = input.index();

		Token c=null;
		Token id=null;
		Token e=null;


		        ClassDescr classDescr = null;
		        if (!isBacktracking()) {
		            if (isDeclaringMainClass()) {
		                classDescr = peekClass();
		            }
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 10) ) { return; }

			// src/main/resources/parser/Java.g:474:5: ( modifiers c= 'class' id= IDENTIFIER ( typeParameters )? (e= 'extends' type )? ( 'implements' typeList )? classBody )
			// src/main/resources/parser/Java.g:474:9: modifiers c= 'class' id= IDENTIFIER ( typeParameters )? (e= 'extends' type )? ( 'implements' typeList )? classBody
			{
			pushFollow(FOLLOW_modifiers_in_normalClassDeclaration977);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			c=(Token)match(input,CLASS,FOLLOW_CLASS_in_normalClassDeclaration981); if (state.failed) return;
			id=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_normalClassDeclaration985); if (state.failed) return;
			if ( state.backtracking==0 ) {
			                                        if (classDescr != null) {
			                                            classDescr.setName((id!=null?id.getText():null));
			                                            classDescr.setClassToken(new TextTokenElementDescr((c!=null?c.getText():null), line(c), position(c)));
			                                        }
			                                    }
			// src/main/resources/parser/Java.g:480:9: ( typeParameters )?
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==LT) ) {
				alt16=1;
			}
			switch (alt16) {
				case 1 :
					// src/main/resources/parser/Java.g:480:10: typeParameters
					{
					pushFollow(FOLLOW_typeParameters_in_normalClassDeclaration998);
					typeParameters();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:482:9: (e= 'extends' type )?
			int alt17=2;
			int LA17_0 = input.LA(1);
			if ( (LA17_0==EXTENDS) ) {
				alt17=1;
			}
			switch (alt17) {
				case 1 :
					// src/main/resources/parser/Java.g:482:11: e= 'extends' type
					{
					e=(Token)match(input,EXTENDS,FOLLOW_EXTENDS_in_normalClassDeclaration1023); if (state.failed) return;
					pushFollow(FOLLOW_type_in_normalClassDeclaration1025);
					type();
					state._fsp--;
					if (state.failed) return;
					if ( state.backtracking==0 ) { if (classDescr != null) classDescr.setExtendsToken(new TextTokenElementDescr((e!=null?e.getText():null), line(e), position(e))); }
					}
					break;

			}

			// src/main/resources/parser/Java.g:484:9: ( 'implements' typeList )?
			int alt18=2;
			int LA18_0 = input.LA(1);
			if ( (LA18_0==IMPLEMENTS) ) {
				alt18=1;
			}
			switch (alt18) {
				case 1 :
					// src/main/resources/parser/Java.g:484:10: 'implements' typeList
					{
					match(input,IMPLEMENTS,FOLLOW_IMPLEMENTS_in_normalClassDeclaration1049); if (state.failed) return;
					pushFollow(FOLLOW_typeList_in_normalClassDeclaration1051);
					typeList();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			pushFollow(FOLLOW_classBody_in_normalClassDeclaration1084);
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
	// src/main/resources/parser/Java.g:490:1: typeParameters : '<' typeParameter ( ',' typeParameter )* '>' ;
	public final void typeParameters() throws RecognitionException {
		int typeParameters_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 11) ) { return; }

			// src/main/resources/parser/Java.g:491:5: ( '<' typeParameter ( ',' typeParameter )* '>' )
			// src/main/resources/parser/Java.g:491:9: '<' typeParameter ( ',' typeParameter )* '>'
			{
			match(input,LT,FOLLOW_LT_in_typeParameters1105); if (state.failed) return;
			pushFollow(FOLLOW_typeParameter_in_typeParameters1119);
			typeParameter();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:493:13: ( ',' typeParameter )*
			loop19:
			while (true) {
				int alt19=2;
				int LA19_0 = input.LA(1);
				if ( (LA19_0==COMMA) ) {
					alt19=1;
				}

				switch (alt19) {
				case 1 :
					// src/main/resources/parser/Java.g:493:14: ',' typeParameter
					{
					match(input,COMMA,FOLLOW_COMMA_in_typeParameters1134); if (state.failed) return;
					pushFollow(FOLLOW_typeParameter_in_typeParameters1136);
					typeParameter();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop19;
				}
			}

			match(input,GT,FOLLOW_GT_in_typeParameters1161); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:498:1: typeParameter : IDENTIFIER ( 'extends' typeBound )? ;
	public final void typeParameter() throws RecognitionException {
		int typeParameter_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 12) ) { return; }

			// src/main/resources/parser/Java.g:499:5: ( IDENTIFIER ( 'extends' typeBound )? )
			// src/main/resources/parser/Java.g:499:9: IDENTIFIER ( 'extends' typeBound )?
			{
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_typeParameter1181); if (state.failed) return;
			// src/main/resources/parser/Java.g:500:9: ( 'extends' typeBound )?
			int alt20=2;
			int LA20_0 = input.LA(1);
			if ( (LA20_0==EXTENDS) ) {
				alt20=1;
			}
			switch (alt20) {
				case 1 :
					// src/main/resources/parser/Java.g:500:10: 'extends' typeBound
					{
					match(input,EXTENDS,FOLLOW_EXTENDS_in_typeParameter1192); if (state.failed) return;
					pushFollow(FOLLOW_typeBound_in_typeParameter1194);
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
	// src/main/resources/parser/Java.g:505:1: typeBound : type ( '&' type )* ;
	public final void typeBound() throws RecognitionException {
		int typeBound_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 13) ) { return; }

			// src/main/resources/parser/Java.g:506:5: ( type ( '&' type )* )
			// src/main/resources/parser/Java.g:506:9: type ( '&' type )*
			{
			pushFollow(FOLLOW_type_in_typeBound1226);
			type();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:507:9: ( '&' type )*
			loop21:
			while (true) {
				int alt21=2;
				int LA21_0 = input.LA(1);
				if ( (LA21_0==AMP) ) {
					alt21=1;
				}

				switch (alt21) {
				case 1 :
					// src/main/resources/parser/Java.g:507:10: '&' type
					{
					match(input,AMP,FOLLOW_AMP_in_typeBound1237); if (state.failed) return;
					pushFollow(FOLLOW_type_in_typeBound1239);
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
	// src/main/resources/parser/Java.g:512:1: enumDeclaration : modifiers ( 'enum' ) IDENTIFIER ( 'implements' typeList )? enumBody ;
	public final void enumDeclaration() throws RecognitionException {
		int enumDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 14) ) { return; }

			// src/main/resources/parser/Java.g:513:5: ( modifiers ( 'enum' ) IDENTIFIER ( 'implements' typeList )? enumBody )
			// src/main/resources/parser/Java.g:513:9: modifiers ( 'enum' ) IDENTIFIER ( 'implements' typeList )? enumBody
			{
			pushFollow(FOLLOW_modifiers_in_enumDeclaration1271);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:514:9: ( 'enum' )
			// src/main/resources/parser/Java.g:514:10: 'enum'
			{
			match(input,ENUM,FOLLOW_ENUM_in_enumDeclaration1283); if (state.failed) return;
			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_enumDeclaration1304); if (state.failed) return;
			// src/main/resources/parser/Java.g:517:9: ( 'implements' typeList )?
			int alt22=2;
			int LA22_0 = input.LA(1);
			if ( (LA22_0==IMPLEMENTS) ) {
				alt22=1;
			}
			switch (alt22) {
				case 1 :
					// src/main/resources/parser/Java.g:517:10: 'implements' typeList
					{
					match(input,IMPLEMENTS,FOLLOW_IMPLEMENTS_in_enumDeclaration1315); if (state.failed) return;
					pushFollow(FOLLOW_typeList_in_enumDeclaration1317);
					typeList();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			pushFollow(FOLLOW_enumBody_in_enumDeclaration1338);
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
	// src/main/resources/parser/Java.g:523:1: enumBody : '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}' ;
	public final void enumBody() throws RecognitionException {
		int enumBody_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 15) ) { return; }

			// src/main/resources/parser/Java.g:524:5: ( '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}' )
			// src/main/resources/parser/Java.g:524:9: '{' ( enumConstants )? ( ',' )? ( enumBodyDeclarations )? '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_enumBody1363); if (state.failed) return;
			// src/main/resources/parser/Java.g:525:9: ( enumConstants )?
			int alt23=2;
			int LA23_0 = input.LA(1);
			if ( (LA23_0==IDENTIFIER||LA23_0==MONKEYS_AT) ) {
				alt23=1;
			}
			switch (alt23) {
				case 1 :
					// src/main/resources/parser/Java.g:525:10: enumConstants
					{
					pushFollow(FOLLOW_enumConstants_in_enumBody1374);
					enumConstants();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:527:9: ( ',' )?
			int alt24=2;
			int LA24_0 = input.LA(1);
			if ( (LA24_0==COMMA) ) {
				alt24=1;
			}
			switch (alt24) {
				case 1 :
					// src/main/resources/parser/Java.g:527:9: ','
					{
					match(input,COMMA,FOLLOW_COMMA_in_enumBody1396); if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:528:9: ( enumBodyDeclarations )?
			int alt25=2;
			int LA25_0 = input.LA(1);
			if ( (LA25_0==SEMI) ) {
				alt25=1;
			}
			switch (alt25) {
				case 1 :
					// src/main/resources/parser/Java.g:528:10: enumBodyDeclarations
					{
					pushFollow(FOLLOW_enumBodyDeclarations_in_enumBody1409);
					enumBodyDeclarations();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,RBRACE,FOLLOW_RBRACE_in_enumBody1431); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:533:1: enumConstants : enumConstant ( ',' enumConstant )* ;
	public final void enumConstants() throws RecognitionException {
		int enumConstants_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 16) ) { return; }

			// src/main/resources/parser/Java.g:534:5: ( enumConstant ( ',' enumConstant )* )
			// src/main/resources/parser/Java.g:534:9: enumConstant ( ',' enumConstant )*
			{
			pushFollow(FOLLOW_enumConstant_in_enumConstants1451);
			enumConstant();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:535:9: ( ',' enumConstant )*
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
					// src/main/resources/parser/Java.g:535:10: ',' enumConstant
					{
					match(input,COMMA,FOLLOW_COMMA_in_enumConstants1462); if (state.failed) return;
					pushFollow(FOLLOW_enumConstant_in_enumConstants1464);
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
	// src/main/resources/parser/Java.g:543:1: enumConstant : ( annotations )? IDENTIFIER ( arguments )? ( classBody )? ;
	public final void enumConstant() throws RecognitionException {
		int enumConstant_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 17) ) { return; }

			// src/main/resources/parser/Java.g:544:5: ( ( annotations )? IDENTIFIER ( arguments )? ( classBody )? )
			// src/main/resources/parser/Java.g:544:9: ( annotations )? IDENTIFIER ( arguments )? ( classBody )?
			{
			// src/main/resources/parser/Java.g:544:9: ( annotations )?
			int alt27=2;
			int LA27_0 = input.LA(1);
			if ( (LA27_0==MONKEYS_AT) ) {
				alt27=1;
			}
			switch (alt27) {
				case 1 :
					// src/main/resources/parser/Java.g:544:10: annotations
					{
					pushFollow(FOLLOW_annotations_in_enumConstant1498);
					annotations();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_enumConstant1519); if (state.failed) return;
			// src/main/resources/parser/Java.g:547:9: ( arguments )?
			int alt28=2;
			int LA28_0 = input.LA(1);
			if ( (LA28_0==LPAREN) ) {
				alt28=1;
			}
			switch (alt28) {
				case 1 :
					// src/main/resources/parser/Java.g:547:10: arguments
					{
					pushFollow(FOLLOW_arguments_in_enumConstant1530);
					arguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:549:9: ( classBody )?
			int alt29=2;
			int LA29_0 = input.LA(1);
			if ( (LA29_0==LBRACE) ) {
				alt29=1;
			}
			switch (alt29) {
				case 1 :
					// src/main/resources/parser/Java.g:549:10: classBody
					{
					pushFollow(FOLLOW_classBody_in_enumConstant1552);
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
	// src/main/resources/parser/Java.g:555:1: enumBodyDeclarations : ';' ( classBodyDeclaration )* ;
	public final void enumBodyDeclarations() throws RecognitionException {
		int enumBodyDeclarations_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 18) ) { return; }

			// src/main/resources/parser/Java.g:556:5: ( ';' ( classBodyDeclaration )* )
			// src/main/resources/parser/Java.g:556:9: ';' ( classBodyDeclaration )*
			{
			match(input,SEMI,FOLLOW_SEMI_in_enumBodyDeclarations1593); if (state.failed) return;
			// src/main/resources/parser/Java.g:557:9: ( classBodyDeclaration )*
			loop30:
			while (true) {
				int alt30=2;
				int LA30_0 = input.LA(1);
				if ( (LA30_0==ABSTRACT||LA30_0==BOOLEAN||LA30_0==BYTE||LA30_0==CHAR||LA30_0==CLASS||LA30_0==DOUBLE||LA30_0==ENUM||LA30_0==FINAL||LA30_0==FLOAT||LA30_0==IDENTIFIER||(LA30_0 >= INT && LA30_0 <= INTERFACE)||LA30_0==LBRACE||LA30_0==LONG||LA30_0==LT||(LA30_0 >= MONKEYS_AT && LA30_0 <= NATIVE)||(LA30_0 >= PRIVATE && LA30_0 <= PUBLIC)||(LA30_0 >= SEMI && LA30_0 <= SHORT)||(LA30_0 >= STATIC && LA30_0 <= STRICTFP)||LA30_0==SYNCHRONIZED||LA30_0==TRANSIENT||(LA30_0 >= VOID && LA30_0 <= VOLATILE)) ) {
					alt30=1;
				}

				switch (alt30) {
				case 1 :
					// src/main/resources/parser/Java.g:557:10: classBodyDeclaration
					{
					pushFollow(FOLLOW_classBodyDeclaration_in_enumBodyDeclarations1605);
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
	// src/main/resources/parser/Java.g:561:1: interfaceDeclaration : ( normalInterfaceDeclaration | annotationTypeDeclaration );
	public final void interfaceDeclaration() throws RecognitionException {
		int interfaceDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 19) ) { return; }

			// src/main/resources/parser/Java.g:562:5: ( normalInterfaceDeclaration | annotationTypeDeclaration )
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
					// src/main/resources/parser/Java.g:562:9: normalInterfaceDeclaration
					{
					pushFollow(FOLLOW_normalInterfaceDeclaration_in_interfaceDeclaration1636);
					normalInterfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:563:9: annotationTypeDeclaration
					{
					pushFollow(FOLLOW_annotationTypeDeclaration_in_interfaceDeclaration1646);
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
	// src/main/resources/parser/Java.g:566:1: normalInterfaceDeclaration : modifiers 'interface' IDENTIFIER ( typeParameters )? ( 'extends' typeList )? interfaceBody ;
	public final void normalInterfaceDeclaration() throws RecognitionException {
		int normalInterfaceDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 20) ) { return; }

			// src/main/resources/parser/Java.g:567:5: ( modifiers 'interface' IDENTIFIER ( typeParameters )? ( 'extends' typeList )? interfaceBody )
			// src/main/resources/parser/Java.g:567:9: modifiers 'interface' IDENTIFIER ( typeParameters )? ( 'extends' typeList )? interfaceBody
			{
			pushFollow(FOLLOW_modifiers_in_normalInterfaceDeclaration1670);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			match(input,INTERFACE,FOLLOW_INTERFACE_in_normalInterfaceDeclaration1672); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_normalInterfaceDeclaration1674); if (state.failed) return;
			// src/main/resources/parser/Java.g:568:9: ( typeParameters )?
			int alt32=2;
			int LA32_0 = input.LA(1);
			if ( (LA32_0==LT) ) {
				alt32=1;
			}
			switch (alt32) {
				case 1 :
					// src/main/resources/parser/Java.g:568:10: typeParameters
					{
					pushFollow(FOLLOW_typeParameters_in_normalInterfaceDeclaration1685);
					typeParameters();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:570:9: ( 'extends' typeList )?
			int alt33=2;
			int LA33_0 = input.LA(1);
			if ( (LA33_0==EXTENDS) ) {
				alt33=1;
			}
			switch (alt33) {
				case 1 :
					// src/main/resources/parser/Java.g:570:10: 'extends' typeList
					{
					match(input,EXTENDS,FOLLOW_EXTENDS_in_normalInterfaceDeclaration1707); if (state.failed) return;
					pushFollow(FOLLOW_typeList_in_normalInterfaceDeclaration1709);
					typeList();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			pushFollow(FOLLOW_interfaceBody_in_normalInterfaceDeclaration1730);
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
	// src/main/resources/parser/Java.g:575:1: typeList : type ( ',' type )* ;
	public final void typeList() throws RecognitionException {
		int typeList_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 21) ) { return; }

			// src/main/resources/parser/Java.g:576:5: ( type ( ',' type )* )
			// src/main/resources/parser/Java.g:576:9: type ( ',' type )*
			{
			pushFollow(FOLLOW_type_in_typeList1750);
			type();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:577:9: ( ',' type )*
			loop34:
			while (true) {
				int alt34=2;
				int LA34_0 = input.LA(1);
				if ( (LA34_0==COMMA) ) {
					alt34=1;
				}

				switch (alt34) {
				case 1 :
					// src/main/resources/parser/Java.g:577:10: ',' type
					{
					match(input,COMMA,FOLLOW_COMMA_in_typeList1761); if (state.failed) return;
					pushFollow(FOLLOW_type_in_typeList1763);
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
	// src/main/resources/parser/Java.g:581:1: classBody : '{' ( classBodyDeclaration )* '}' ;
	public final void classBody() throws RecognitionException {
		int classBody_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 22) ) { return; }

			// src/main/resources/parser/Java.g:582:5: ( '{' ( classBodyDeclaration )* '}' )
			// src/main/resources/parser/Java.g:582:9: '{' ( classBodyDeclaration )* '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_classBody1794); if (state.failed) return;
			// src/main/resources/parser/Java.g:583:9: ( classBodyDeclaration )*
			loop35:
			while (true) {
				int alt35=2;
				int LA35_0 = input.LA(1);
				if ( (LA35_0==ABSTRACT||LA35_0==BOOLEAN||LA35_0==BYTE||LA35_0==CHAR||LA35_0==CLASS||LA35_0==DOUBLE||LA35_0==ENUM||LA35_0==FINAL||LA35_0==FLOAT||LA35_0==IDENTIFIER||(LA35_0 >= INT && LA35_0 <= INTERFACE)||LA35_0==LBRACE||LA35_0==LONG||LA35_0==LT||(LA35_0 >= MONKEYS_AT && LA35_0 <= NATIVE)||(LA35_0 >= PRIVATE && LA35_0 <= PUBLIC)||(LA35_0 >= SEMI && LA35_0 <= SHORT)||(LA35_0 >= STATIC && LA35_0 <= STRICTFP)||LA35_0==SYNCHRONIZED||LA35_0==TRANSIENT||(LA35_0 >= VOID && LA35_0 <= VOLATILE)) ) {
					alt35=1;
				}

				switch (alt35) {
				case 1 :
					// src/main/resources/parser/Java.g:583:10: classBodyDeclaration
					{
					pushFollow(FOLLOW_classBodyDeclaration_in_classBody1806);
					classBodyDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop35;
				}
			}

			match(input,RBRACE,FOLLOW_RBRACE_in_classBody1828); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:588:1: interfaceBody : '{' ( interfaceBodyDeclaration )* '}' ;
	public final void interfaceBody() throws RecognitionException {
		int interfaceBody_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 23) ) { return; }

			// src/main/resources/parser/Java.g:589:5: ( '{' ( interfaceBodyDeclaration )* '}' )
			// src/main/resources/parser/Java.g:589:9: '{' ( interfaceBodyDeclaration )* '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_interfaceBody1848); if (state.failed) return;
			// src/main/resources/parser/Java.g:590:9: ( interfaceBodyDeclaration )*
			loop36:
			while (true) {
				int alt36=2;
				int LA36_0 = input.LA(1);
				if ( (LA36_0==ABSTRACT||LA36_0==BOOLEAN||LA36_0==BYTE||LA36_0==CHAR||LA36_0==CLASS||LA36_0==DOUBLE||LA36_0==ENUM||LA36_0==FINAL||LA36_0==FLOAT||LA36_0==IDENTIFIER||(LA36_0 >= INT && LA36_0 <= INTERFACE)||LA36_0==LONG||LA36_0==LT||(LA36_0 >= MONKEYS_AT && LA36_0 <= NATIVE)||(LA36_0 >= PRIVATE && LA36_0 <= PUBLIC)||(LA36_0 >= SEMI && LA36_0 <= SHORT)||(LA36_0 >= STATIC && LA36_0 <= STRICTFP)||LA36_0==SYNCHRONIZED||LA36_0==TRANSIENT||(LA36_0 >= VOID && LA36_0 <= VOLATILE)) ) {
					alt36=1;
				}

				switch (alt36) {
				case 1 :
					// src/main/resources/parser/Java.g:590:10: interfaceBodyDeclaration
					{
					pushFollow(FOLLOW_interfaceBodyDeclaration_in_interfaceBody1860);
					interfaceBodyDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop36;
				}
			}

			match(input,RBRACE,FOLLOW_RBRACE_in_interfaceBody1882); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:595:1: classBodyDeclaration : ( ';' | ( 'static' )? block | memberDecl );
	public final void classBodyDeclaration() throws RecognitionException {
		int classBodyDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 24) ) { return; }

			// src/main/resources/parser/Java.g:596:5: ( ';' | ( 'static' )? block | memberDecl )
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
					// src/main/resources/parser/Java.g:596:9: ';'
					{
					match(input,SEMI,FOLLOW_SEMI_in_classBodyDeclaration1902); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:597:9: ( 'static' )? block
					{
					// src/main/resources/parser/Java.g:597:9: ( 'static' )?
					int alt37=2;
					int LA37_0 = input.LA(1);
					if ( (LA37_0==STATIC) ) {
						alt37=1;
					}
					switch (alt37) {
						case 1 :
							// src/main/resources/parser/Java.g:597:10: 'static'
							{
							match(input,STATIC,FOLLOW_STATIC_in_classBodyDeclaration1913); if (state.failed) return;
							}
							break;

					}

					pushFollow(FOLLOW_block_in_classBodyDeclaration1935);
					block();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:600:9: memberDecl
					{
					pushFollow(FOLLOW_memberDecl_in_classBodyDeclaration1945);
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
	// src/main/resources/parser/Java.g:603:1: memberDecl : ( fieldDeclaration | methodDeclaration | classDeclaration | interfaceDeclaration );
	public final void memberDecl() throws RecognitionException {
		int memberDecl_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 25) ) { return; }

			// src/main/resources/parser/Java.g:604:5: ( fieldDeclaration | methodDeclaration | classDeclaration | interfaceDeclaration )
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
					// src/main/resources/parser/Java.g:604:10: fieldDeclaration
					{
					pushFollow(FOLLOW_fieldDeclaration_in_memberDecl1965);
					fieldDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:605:10: methodDeclaration
					{
					pushFollow(FOLLOW_methodDeclaration_in_memberDecl1976);
					methodDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:606:10: classDeclaration
					{
					if ( state.backtracking==0 ) { if (!isBacktracking()) increaseClassLevel(); }
					pushFollow(FOLLOW_classDeclaration_in_memberDecl1989);
					classDeclaration();
					state._fsp--;
					if (state.failed) return;
					if ( state.backtracking==0 ) { decreaseClassLevel(); }
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:607:10: interfaceDeclaration
					{
					pushFollow(FOLLOW_interfaceDeclaration_in_memberDecl2002);
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
	// src/main/resources/parser/Java.g:611:1: methodDeclaration : ( modifiers ( typeParameters )? IDENTIFIER formalParameters ( 'throws' qualifiedNameList )? '{' ( explicitConstructorInvocation )? ( blockStatement )* '}' | modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters (p1= '[' p2= ']' )* ( 'throws' qualifiedNameList )? ( block | ';' ) );
	public final JavaParser.methodDeclaration_return methodDeclaration() throws RecognitionException {
		JavaParser.methodDeclaration_return retval = new JavaParser.methodDeclaration_return();
		retval.start = input.LT(1);
		int methodDeclaration_StartIndex = input.index();

		Token p1=null;
		Token p2=null;
		Token IDENTIFIER2=null;


		        MethodDescr method = null;
		        if (!isBacktracking()) {
		            log("Start method declaration.");
		            setDeclaringMethodReturnType(false);
		            method = new MethodDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		            context.push(method);
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 26) ) { return retval; }

			// src/main/resources/parser/Java.g:629:5: ( modifiers ( typeParameters )? IDENTIFIER formalParameters ( 'throws' qualifiedNameList )? '{' ( explicitConstructorInvocation )? ( blockStatement )* '}' | modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters (p1= '[' p2= ']' )* ( 'throws' qualifiedNameList )? ( block | ';' ) )
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
					// src/main/resources/parser/Java.g:631:10: modifiers ( typeParameters )? IDENTIFIER formalParameters ( 'throws' qualifiedNameList )? '{' ( explicitConstructorInvocation )? ( blockStatement )* '}'
					{
					pushFollow(FOLLOW_modifiers_in_methodDeclaration2057);
					modifiers();
					state._fsp--;
					if (state.failed) return retval;
					// src/main/resources/parser/Java.g:632:9: ( typeParameters )?
					int alt40=2;
					int LA40_0 = input.LA(1);
					if ( (LA40_0==LT) ) {
						alt40=1;
					}
					switch (alt40) {
						case 1 :
							// src/main/resources/parser/Java.g:632:10: typeParameters
							{
							pushFollow(FOLLOW_typeParameters_in_methodDeclaration2068);
							typeParameters();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_methodDeclaration2089); if (state.failed) return retval;
					pushFollow(FOLLOW_formalParameters_in_methodDeclaration2099);
					formalParameters();
					state._fsp--;
					if (state.failed) return retval;
					// src/main/resources/parser/Java.g:636:9: ( 'throws' qualifiedNameList )?
					int alt41=2;
					int LA41_0 = input.LA(1);
					if ( (LA41_0==THROWS) ) {
						alt41=1;
					}
					switch (alt41) {
						case 1 :
							// src/main/resources/parser/Java.g:636:10: 'throws' qualifiedNameList
							{
							match(input,THROWS,FOLLOW_THROWS_in_methodDeclaration2110); if (state.failed) return retval;
							pushFollow(FOLLOW_qualifiedNameList_in_methodDeclaration2112);
							qualifiedNameList();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					match(input,LBRACE,FOLLOW_LBRACE_in_methodDeclaration2133); if (state.failed) return retval;
					// src/main/resources/parser/Java.g:639:9: ( explicitConstructorInvocation )?
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
							// src/main/resources/parser/Java.g:639:10: explicitConstructorInvocation
							{
							pushFollow(FOLLOW_explicitConstructorInvocation_in_methodDeclaration2145);
							explicitConstructorInvocation();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					// src/main/resources/parser/Java.g:641:9: ( blockStatement )*
					loop43:
					while (true) {
						int alt43=2;
						int LA43_0 = input.LA(1);
						if ( (LA43_0==ABSTRACT||(LA43_0 >= ASSERT && LA43_0 <= BANG)||(LA43_0 >= BOOLEAN && LA43_0 <= BYTE)||(LA43_0 >= CHAR && LA43_0 <= CLASS)||LA43_0==CONTINUE||LA43_0==DO||(LA43_0 >= DOUBLE && LA43_0 <= DOUBLELITERAL)||LA43_0==ENUM||(LA43_0 >= FALSE && LA43_0 <= FINAL)||(LA43_0 >= FLOAT && LA43_0 <= FOR)||(LA43_0 >= IDENTIFIER && LA43_0 <= IF)||(LA43_0 >= INT && LA43_0 <= INTLITERAL)||LA43_0==LBRACE||(LA43_0 >= LONG && LA43_0 <= LT)||(LA43_0 >= MONKEYS_AT && LA43_0 <= NULL)||LA43_0==PLUS||(LA43_0 >= PLUSPLUS && LA43_0 <= PUBLIC)||LA43_0==RETURN||(LA43_0 >= SEMI && LA43_0 <= SHORT)||(LA43_0 >= STATIC && LA43_0 <= SUB)||(LA43_0 >= SUBSUB && LA43_0 <= SYNCHRONIZED)||(LA43_0 >= THIS && LA43_0 <= THROW)||(LA43_0 >= TILDE && LA43_0 <= WHILE)) ) {
							alt43=1;
						}

						switch (alt43) {
						case 1 :
							// src/main/resources/parser/Java.g:641:10: blockStatement
							{
							pushFollow(FOLLOW_blockStatement_in_methodDeclaration2167);
							blockStatement();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

						default :
							break loop43;
						}
					}

					match(input,RBRACE,FOLLOW_RBRACE_in_methodDeclaration2188); if (state.failed) return retval;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:644:9: modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters (p1= '[' p2= ']' )* ( 'throws' qualifiedNameList )? ( block | ';' )
					{
					pushFollow(FOLLOW_modifiers_in_methodDeclaration2198);
					modifiers();
					state._fsp--;
					if (state.failed) return retval;
					// src/main/resources/parser/Java.g:645:9: ( typeParameters )?
					int alt44=2;
					int LA44_0 = input.LA(1);
					if ( (LA44_0==LT) ) {
						alt44=1;
					}
					switch (alt44) {
						case 1 :
							// src/main/resources/parser/Java.g:645:10: typeParameters
							{
							pushFollow(FOLLOW_typeParameters_in_methodDeclaration2209);
							typeParameters();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					// src/main/resources/parser/Java.g:647:9: ( type | 'void' )
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
							// src/main/resources/parser/Java.g:647:11: type
							{
							if ( state.backtracking==0 ) { setDeclaringMethodReturnType(true); }
							pushFollow(FOLLOW_type_in_methodDeclaration2234);
							type();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) { setDeclaringMethodReturnType(false); }
							}
							break;
						case 2 :
							// src/main/resources/parser/Java.g:648:13: 'void'
							{
							match(input,VOID,FOLLOW_VOID_in_methodDeclaration2250); if (state.failed) return retval;
							}
							break;

					}

					IDENTIFIER2=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_methodDeclaration2270); if (state.failed) return retval;
					if ( state.backtracking==0 ) { method.setName((IDENTIFIER2!=null?IDENTIFIER2.getText():null)); }
					pushFollow(FOLLOW_formalParameters_in_methodDeclaration2282);
					formalParameters();
					state._fsp--;
					if (state.failed) return retval;
					// src/main/resources/parser/Java.g:652:9: (p1= '[' p2= ']' )*
					loop46:
					while (true) {
						int alt46=2;
						int LA46_0 = input.LA(1);
						if ( (LA46_0==LBRACKET) ) {
							alt46=1;
						}

						switch (alt46) {
						case 1 :
							// src/main/resources/parser/Java.g:652:10: p1= '[' p2= ']'
							{
							p1=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_methodDeclaration2295); if (state.failed) return retval;
							p2=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_methodDeclaration2299); if (state.failed) return retval;
							if ( state.backtracking==0 ) { method.addDimension(new DimensionDescr((p1!=null?p1.getText():null), line(p1), position(p1), (p2!=null?p2.getText():null), line(p2), position(p2))); }
							}
							break;

						default :
							break loop46;
						}
					}

					// src/main/resources/parser/Java.g:654:9: ( 'throws' qualifiedNameList )?
					int alt47=2;
					int LA47_0 = input.LA(1);
					if ( (LA47_0==THROWS) ) {
						alt47=1;
					}
					switch (alt47) {
						case 1 :
							// src/main/resources/parser/Java.g:654:10: 'throws' qualifiedNameList
							{
							match(input,THROWS,FOLLOW_THROWS_in_methodDeclaration2325); if (state.failed) return retval;
							pushFollow(FOLLOW_qualifiedNameList_in_methodDeclaration2327);
							qualifiedNameList();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					// src/main/resources/parser/Java.g:656:9: ( block | ';' )
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
							// src/main/resources/parser/Java.g:657:13: block
							{
							pushFollow(FOLLOW_block_in_methodDeclaration2382);
							block();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;
						case 2 :
							// src/main/resources/parser/Java.g:658:13: ';'
							{
							match(input,SEMI,FOLLOW_SEMI_in_methodDeclaration2396); if (state.failed) return retval;
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
			            updateOnAfter(method, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
			            processMethod(method);
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
	// src/main/resources/parser/Java.g:663:1: fieldDeclaration : modifiers type v1= variableDeclarator ( ',' v2= variableDeclarator )* ';' ;
	public final JavaParser.fieldDeclaration_return fieldDeclaration() throws RecognitionException {
		JavaParser.fieldDeclaration_return retval = new JavaParser.fieldDeclaration_return();
		retval.start = input.LT(1);
		int fieldDeclaration_StartIndex = input.index();

		ParserRuleReturnScope v1 =null;
		ParserRuleReturnScope v2 =null;


		        FieldDescr field = null;
		        if (!isBacktracking()) {
		            log("Start field declaration.");
		            field = new FieldDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		            context.push(field);
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 27) ) { return retval; }

			// src/main/resources/parser/Java.g:683:5: ( modifiers type v1= variableDeclarator ( ',' v2= variableDeclarator )* ';' )
			// src/main/resources/parser/Java.g:683:9: modifiers type v1= variableDeclarator ( ',' v2= variableDeclarator )* ';'
			{
			pushFollow(FOLLOW_modifiers_in_fieldDeclaration2445);
			modifiers();
			state._fsp--;
			if (state.failed) return retval;
			pushFollow(FOLLOW_type_in_fieldDeclaration2455);
			type();
			state._fsp--;
			if (state.failed) return retval;
			pushFollow(FOLLOW_variableDeclarator_in_fieldDeclaration2467);
			v1=variableDeclarator();
			state._fsp--;
			if (state.failed) return retval;
			if ( state.backtracking==0 ) { if (field != null) field.addVariableDeclaration((v1!=null?((JavaParser.variableDeclarator_return)v1).varDec:null)); }
			// src/main/resources/parser/Java.g:686:9: ( ',' v2= variableDeclarator )*
			loop50:
			while (true) {
				int alt50=2;
				int LA50_0 = input.LA(1);
				if ( (LA50_0==COMMA) ) {
					alt50=1;
				}

				switch (alt50) {
				case 1 :
					// src/main/resources/parser/Java.g:686:10: ',' v2= variableDeclarator
					{
					match(input,COMMA,FOLLOW_COMMA_in_fieldDeclaration2486); if (state.failed) return retval;
					pushFollow(FOLLOW_variableDeclarator_in_fieldDeclaration2490);
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

			match(input,SEMI,FOLLOW_SEMI_in_fieldDeclaration2514); if (state.failed) return retval;
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        //this can also be done in the finally block for the rule.
			        field = popField();
			        if (field != null) {
			            updateOnAfter(field, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
			            processField(field);
			            log("End of field declaration.");
			        } else {
			            log("A FieldDescr is expected");
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
		public VariableDeclarationDescr varDec;
	};


	// $ANTLR start "variableDeclarator"
	// src/main/resources/parser/Java.g:691:1: variableDeclarator returns [ VariableDeclarationDescr varDec ] : i= IDENTIFIER (p1= '[' p2= ']' )* ( '=' v= variableInitializer )? ;
	public final JavaParser.variableDeclarator_return variableDeclarator() throws RecognitionException {
		JavaParser.variableDeclarator_return retval = new JavaParser.variableDeclarator_return();
		retval.start = input.LT(1);
		int variableDeclarator_StartIndex = input.index();

		Token i=null;
		Token p1=null;
		Token p2=null;
		ParserRuleReturnScope v =null;


		        retval.varDec = new VariableDeclarationDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 28) ) { return retval; }

			// src/main/resources/parser/Java.g:700:5: (i= IDENTIFIER (p1= '[' p2= ']' )* ( '=' v= variableInitializer )? )
			// src/main/resources/parser/Java.g:700:9: i= IDENTIFIER (p1= '[' p2= ']' )* ( '=' v= variableInitializer )?
			{
			i=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_variableDeclarator2557); if (state.failed) return retval;
			if ( state.backtracking==0 ) { retval.varDec.setIdentifier((i!=null?i.getText():null)); }
			// src/main/resources/parser/Java.g:701:9: (p1= '[' p2= ']' )*
			loop51:
			while (true) {
				int alt51=2;
				int LA51_0 = input.LA(1);
				if ( (LA51_0==LBRACKET) ) {
					alt51=1;
				}

				switch (alt51) {
				case 1 :
					// src/main/resources/parser/Java.g:701:10: p1= '[' p2= ']'
					{
					p1=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_variableDeclarator2572); if (state.failed) return retval;
					p2=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_variableDeclarator2576); if (state.failed) return retval;
					if ( state.backtracking==0 ) { retval.varDec.addDimension(new DimensionDescr((p1!=null?p1.getText():null), line(p1), position(p1), (p2!=null?p2.getText():null), line(p2), position(p2))); }
					}
					break;

				default :
					break loop51;
				}
			}

			// src/main/resources/parser/Java.g:703:9: ( '=' v= variableInitializer )?
			int alt52=2;
			int LA52_0 = input.LA(1);
			if ( (LA52_0==EQ) ) {
				alt52=1;
			}
			switch (alt52) {
				case 1 :
					// src/main/resources/parser/Java.g:703:10: '=' v= variableInitializer
					{
					match(input,EQ,FOLLOW_EQ_in_variableDeclarator2600); if (state.failed) return retval;
					pushFollow(FOLLOW_variableInitializer_in_variableDeclarator2604);
					v=variableInitializer();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) { retval.varDec.setVariableInitializer(new VariableInitializerDescr( (v!=null?input.toString(v.start,v.stop):null), start(((CommonToken)(v!=null?(v.start):null))), stop((CommonToken)(v!=null?(v.stop):null)), line((CommonToken)(v!=null?(v.start):null)), position((CommonToken)(v!=null?(v.start):null)), (v!=null?input.toString(v.start,v.stop):null) ) ); }
					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        updateOnAfter(retval.varDec, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
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
	// src/main/resources/parser/Java.g:710:1: interfaceBodyDeclaration : ( interfaceFieldDeclaration | interfaceMethodDeclaration | interfaceDeclaration | classDeclaration | ';' );
	public final void interfaceBodyDeclaration() throws RecognitionException {
		int interfaceBodyDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 29) ) { return; }

			// src/main/resources/parser/Java.g:711:5: ( interfaceFieldDeclaration | interfaceMethodDeclaration | interfaceDeclaration | classDeclaration | ';' )
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
					// src/main/resources/parser/Java.g:712:9: interfaceFieldDeclaration
					{
					pushFollow(FOLLOW_interfaceFieldDeclaration_in_interfaceBodyDeclaration2645);
					interfaceFieldDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:713:9: interfaceMethodDeclaration
					{
					pushFollow(FOLLOW_interfaceMethodDeclaration_in_interfaceBodyDeclaration2655);
					interfaceMethodDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:714:9: interfaceDeclaration
					{
					pushFollow(FOLLOW_interfaceDeclaration_in_interfaceBodyDeclaration2665);
					interfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:715:9: classDeclaration
					{
					pushFollow(FOLLOW_classDeclaration_in_interfaceBodyDeclaration2675);
					classDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:716:9: ';'
					{
					match(input,SEMI,FOLLOW_SEMI_in_interfaceBodyDeclaration2685); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:719:1: interfaceMethodDeclaration : modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';' ;
	public final void interfaceMethodDeclaration() throws RecognitionException {
		int interfaceMethodDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 30) ) { return; }

			// src/main/resources/parser/Java.g:720:5: ( modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';' )
			// src/main/resources/parser/Java.g:720:9: modifiers ( typeParameters )? ( type | 'void' ) IDENTIFIER formalParameters ( '[' ']' )* ( 'throws' qualifiedNameList )? ';'
			{
			pushFollow(FOLLOW_modifiers_in_interfaceMethodDeclaration2705);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:721:9: ( typeParameters )?
			int alt54=2;
			int LA54_0 = input.LA(1);
			if ( (LA54_0==LT) ) {
				alt54=1;
			}
			switch (alt54) {
				case 1 :
					// src/main/resources/parser/Java.g:721:10: typeParameters
					{
					pushFollow(FOLLOW_typeParameters_in_interfaceMethodDeclaration2716);
					typeParameters();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:723:9: ( type | 'void' )
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
					// src/main/resources/parser/Java.g:723:10: type
					{
					pushFollow(FOLLOW_type_in_interfaceMethodDeclaration2738);
					type();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:724:10: 'void'
					{
					match(input,VOID,FOLLOW_VOID_in_interfaceMethodDeclaration2749); if (state.failed) return;
					}
					break;

			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_interfaceMethodDeclaration2769); if (state.failed) return;
			pushFollow(FOLLOW_formalParameters_in_interfaceMethodDeclaration2779);
			formalParameters();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:728:9: ( '[' ']' )*
			loop56:
			while (true) {
				int alt56=2;
				int LA56_0 = input.LA(1);
				if ( (LA56_0==LBRACKET) ) {
					alt56=1;
				}

				switch (alt56) {
				case 1 :
					// src/main/resources/parser/Java.g:728:10: '[' ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_interfaceMethodDeclaration2790); if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_interfaceMethodDeclaration2792); if (state.failed) return;
					}
					break;

				default :
					break loop56;
				}
			}

			// src/main/resources/parser/Java.g:730:9: ( 'throws' qualifiedNameList )?
			int alt57=2;
			int LA57_0 = input.LA(1);
			if ( (LA57_0==THROWS) ) {
				alt57=1;
			}
			switch (alt57) {
				case 1 :
					// src/main/resources/parser/Java.g:730:10: 'throws' qualifiedNameList
					{
					match(input,THROWS,FOLLOW_THROWS_in_interfaceMethodDeclaration2814); if (state.failed) return;
					pushFollow(FOLLOW_qualifiedNameList_in_interfaceMethodDeclaration2816);
					qualifiedNameList();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,SEMI,FOLLOW_SEMI_in_interfaceMethodDeclaration2829); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:739:1: interfaceFieldDeclaration : modifiers type variableDeclarator ( ',' variableDeclarator )* ';' ;
	public final void interfaceFieldDeclaration() throws RecognitionException {
		int interfaceFieldDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 31) ) { return; }

			// src/main/resources/parser/Java.g:740:5: ( modifiers type variableDeclarator ( ',' variableDeclarator )* ';' )
			// src/main/resources/parser/Java.g:740:9: modifiers type variableDeclarator ( ',' variableDeclarator )* ';'
			{
			pushFollow(FOLLOW_modifiers_in_interfaceFieldDeclaration2851);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_interfaceFieldDeclaration2853);
			type();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_variableDeclarator_in_interfaceFieldDeclaration2855);
			variableDeclarator();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:741:9: ( ',' variableDeclarator )*
			loop58:
			while (true) {
				int alt58=2;
				int LA58_0 = input.LA(1);
				if ( (LA58_0==COMMA) ) {
					alt58=1;
				}

				switch (alt58) {
				case 1 :
					// src/main/resources/parser/Java.g:741:10: ',' variableDeclarator
					{
					match(input,COMMA,FOLLOW_COMMA_in_interfaceFieldDeclaration2866); if (state.failed) return;
					pushFollow(FOLLOW_variableDeclarator_in_interfaceFieldDeclaration2868);
					variableDeclarator();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop58;
				}
			}

			match(input,SEMI,FOLLOW_SEMI_in_interfaceFieldDeclaration2889); if (state.failed) return;
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


	public static class type_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "type"
	// src/main/resources/parser/Java.g:747:1: type : ( classOrInterfaceType (p1= '[' p2= ']' )* | primitiveType (p1= '[' p2= ']' )* );
	public final JavaParser.type_return type() throws RecognitionException {
		JavaParser.type_return retval = new JavaParser.type_return();
		retval.start = input.LT(1);
		int type_StartIndex = input.index();

		Token p1=null;
		Token p2=null;


		        TypeDescr type = null;
		        if (!isBacktracking()) {
		            log("Start type declaration.");
		            type = new TypeDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		            context.push(type);
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 32) ) { return retval; }

			// src/main/resources/parser/Java.g:765:5: ( classOrInterfaceType (p1= '[' p2= ']' )* | primitiveType (p1= '[' p2= ']' )* )
			int alt61=2;
			int LA61_0 = input.LA(1);
			if ( (LA61_0==IDENTIFIER) ) {
				alt61=1;
			}
			else if ( (LA61_0==BOOLEAN||LA61_0==BYTE||LA61_0==CHAR||LA61_0==DOUBLE||LA61_0==FLOAT||LA61_0==INT||LA61_0==LONG||LA61_0==SHORT) ) {
				alt61=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 61, 0, input);
				throw nvae;
			}

			switch (alt61) {
				case 1 :
					// src/main/resources/parser/Java.g:765:9: classOrInterfaceType (p1= '[' p2= ']' )*
					{
					pushFollow(FOLLOW_classOrInterfaceType_in_type2927);
					classOrInterfaceType();
					state._fsp--;
					if (state.failed) return retval;
					// src/main/resources/parser/Java.g:766:9: (p1= '[' p2= ']' )*
					loop59:
					while (true) {
						int alt59=2;
						int LA59_0 = input.LA(1);
						if ( (LA59_0==LBRACKET) ) {
							alt59=1;
						}

						switch (alt59) {
						case 1 :
							// src/main/resources/parser/Java.g:766:10: p1= '[' p2= ']'
							{
							p1=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_type2940); if (state.failed) return retval;
							p2=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_type2944); if (state.failed) return retval;
							if ( state.backtracking==0 ) { type.addDimension(new DimensionDescr((p1!=null?p1.getText():null), line(p1), position(p1), (p2!=null?p2.getText():null), line(p2), position(p2))); }
							}
							break;

						default :
							break loop59;
						}
					}

					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:768:9: primitiveType (p1= '[' p2= ']' )*
					{
					pushFollow(FOLLOW_primitiveType_in_type2967);
					primitiveType();
					state._fsp--;
					if (state.failed) return retval;
					// src/main/resources/parser/Java.g:769:9: (p1= '[' p2= ']' )*
					loop60:
					while (true) {
						int alt60=2;
						int LA60_0 = input.LA(1);
						if ( (LA60_0==LBRACKET) ) {
							alt60=1;
						}

						switch (alt60) {
						case 1 :
							// src/main/resources/parser/Java.g:769:10: p1= '[' p2= ']'
							{
							p1=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_type2980); if (state.failed) return retval;
							p2=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_type2984); if (state.failed) return retval;
							if ( state.backtracking==0 ) { type.addDimension(new DimensionDescr((p1!=null?p1.getText():null), line(p1), position(p1), (p2!=null?p2.getText():null), line(p2), position(p2))); }
							}
							break;

						default :
							break loop60;
						}
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        type = popType();
			        if (type != null) {
			            updateOnAfter(type, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
			            processType(type);
			        } else {
			            //TODO warning, by construction current type is expected
			        }
			    }
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			if ( state.backtracking>0 ) { memoize(input, 32, type_StartIndex); }

		}
		return retval;
	}
	// $ANTLR end "type"


	public static class classOrInterfaceType_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "classOrInterfaceType"
	// src/main/resources/parser/Java.g:774:1: classOrInterfaceType : id1= IDENTIFIER ( typeArguments )? ( '.' id2= IDENTIFIER ( typeArguments )? )* ;
	public final JavaParser.classOrInterfaceType_return classOrInterfaceType() throws RecognitionException {
		JavaParser.classOrInterfaceType_return retval = new JavaParser.classOrInterfaceType_return();
		retval.start = input.LT(1);
		int classOrInterfaceType_StartIndex = input.index();

		Token id1=null;
		Token id2=null;


		        ClassOrInterfaceTypeDescr classDescr = null;
		        IdentifierWithTypeArgumentsDescr idArguments = null;
		        if (!isBacktracking()) {
		            log("Start ClassOrInterfaceType declaration");
		            classDescr = new ClassOrInterfaceTypeDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		            context.push(classDescr);
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 33) ) { return retval; }

			// src/main/resources/parser/Java.g:796:5: (id1= IDENTIFIER ( typeArguments )? ( '.' id2= IDENTIFIER ( typeArguments )? )* )
			// src/main/resources/parser/Java.g:796:9: id1= IDENTIFIER ( typeArguments )? ( '.' id2= IDENTIFIER ( typeArguments )? )*
			{
			id1=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_classOrInterfaceType3037); if (state.failed) return retval;
			if ( state.backtracking==0 ) {
			                            idArguments = new IdentifierWithTypeArgumentsDescr((id1!=null?id1.getText():null), -1, line(id1), position(id1), (id1!=null?id1.getText():null));
			                            classDescr.addIdentifierWithTypeArguments(idArguments);
			                        }
			// src/main/resources/parser/Java.g:800:9: ( typeArguments )?
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
					// src/main/resources/parser/Java.g:800:11: typeArguments
					{
					if ( state.backtracking==0 ) {context.push(idArguments);}
					pushFollow(FOLLOW_typeArguments_in_classOrInterfaceType3054);
					typeArguments();
					state._fsp--;
					if (state.failed) return retval;
					if ( state.backtracking==0 ) {if (context.size() > 0) context.pop();}
					}
					break;

			}

			// src/main/resources/parser/Java.g:802:9: ( '.' id2= IDENTIFIER ( typeArguments )? )*
			loop64:
			while (true) {
				int alt64=2;
				int LA64_0 = input.LA(1);
				if ( (LA64_0==DOT) ) {
					alt64=1;
				}

				switch (alt64) {
				case 1 :
					// src/main/resources/parser/Java.g:802:10: '.' id2= IDENTIFIER ( typeArguments )?
					{
					match(input,DOT,FOLLOW_DOT_in_classOrInterfaceType3078); if (state.failed) return retval;
					id2=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_classOrInterfaceType3082); if (state.failed) return retval;
					if ( state.backtracking==0 ) {
					                            idArguments = new IdentifierWithTypeArgumentsDescr((id2!=null?id2.getText():null), -1, line(id2), position(id2), (id2!=null?id2.getText():null));
					                            classDescr.addIdentifierWithTypeArguments(idArguments);
					                        }
					// src/main/resources/parser/Java.g:806:13: ( typeArguments )?
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
							// src/main/resources/parser/Java.g:806:15: typeArguments
							{
							if ( state.backtracking==0 ) {context.push(idArguments);}
							pushFollow(FOLLOW_typeArguments_in_classOrInterfaceType3102);
							typeArguments();
							state._fsp--;
							if (state.failed) return retval;
							if ( state.backtracking==0 ) {if (context.size() > 0) context.pop();}
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
			        classDescr = popClassOrInterfaceType();
			        if (classDescr != null) {
			            updateOnAfter(classDescr, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
			            HasClassOrInterfaceType top = peekHasClassOrInterfaceType();
			            if ( top != null) {
			                top.setClassOrInterfaceType(classDescr);
			            }
			        } else {
			            //TODO warning, by construction current classDescr is expected
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
	// src/main/resources/parser/Java.g:811:1: primitiveType : ( 'boolean' | 'char' | 'byte' | 'short' | 'int' | 'long' | 'float' | 'double' );
	public final JavaParser.primitiveType_return primitiveType() throws RecognitionException {
		JavaParser.primitiveType_return retval = new JavaParser.primitiveType_return();
		retval.start = input.LT(1);
		int primitiveType_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 34) ) { return retval; }

			// src/main/resources/parser/Java.g:818:5: ( 'boolean' | 'char' | 'byte' | 'short' | 'int' | 'long' | 'float' | 'double' )
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
			        HasPrimitiveType hasPrimitive = peekHasPrimitiveType();
			        if (hasPrimitive != null) {
			            hasPrimitive.setPrimitiveType(new PrimitiveTypeDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), stop((CommonToken)(retval.stop)), line((CommonToken)(retval.start)), position((CommonToken)(retval.start)), input.toString(retval.start,input.LT(-1))));
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
	// src/main/resources/parser/Java.g:828:1: typeArguments : '<' typeArgument ( ',' typeArgument )* '>' ;
	public final void typeArguments() throws RecognitionException {
		int typeArguments_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 35) ) { return; }

			// src/main/resources/parser/Java.g:830:5: ( '<' typeArgument ( ',' typeArgument )* '>' )
			// src/main/resources/parser/Java.g:830:9: '<' typeArgument ( ',' typeArgument )* '>'
			{
			match(input,LT,FOLLOW_LT_in_typeArguments3249); if (state.failed) return;
			pushFollow(FOLLOW_typeArgument_in_typeArguments3251);
			typeArgument();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:831:9: ( ',' typeArgument )*
			loop65:
			while (true) {
				int alt65=2;
				int LA65_0 = input.LA(1);
				if ( (LA65_0==COMMA) ) {
					alt65=1;
				}

				switch (alt65) {
				case 1 :
					// src/main/resources/parser/Java.g:831:10: ',' typeArgument
					{
					match(input,COMMA,FOLLOW_COMMA_in_typeArguments3262); if (state.failed) return;
					pushFollow(FOLLOW_typeArgument_in_typeArguments3264);
					typeArgument();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop65;
				}
			}

			match(input,GT,FOLLOW_GT_in_typeArguments3286); if (state.failed) return;
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


	public static class typeArgument_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "typeArgument"
	// src/main/resources/parser/Java.g:836:1: typeArgument : ( type | '?' ( ( 'extends' | 'super' ) type )? );
	public final JavaParser.typeArgument_return typeArgument() throws RecognitionException {
		JavaParser.typeArgument_return retval = new JavaParser.typeArgument_return();
		retval.start = input.LT(1);
		int typeArgument_StartIndex = input.index();


		        TypeArgumentDescr typeArgumentDescr = null;
		        if (!isBacktracking()) {
		            log("Start TypeArgumentDescr declaration");
		            typeArgumentDescr = new TypeArgumentDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((CommonToken)(retval.start)), position((CommonToken)(retval.start)));
		            context.push(typeArgumentDescr);
		        }
		    
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 36) ) { return retval; }

			// src/main/resources/parser/Java.g:858:5: ( type | '?' ( ( 'extends' | 'super' ) type )? )
			int alt67=2;
			int LA67_0 = input.LA(1);
			if ( (LA67_0==BOOLEAN||LA67_0==BYTE||LA67_0==CHAR||LA67_0==DOUBLE||LA67_0==FLOAT||LA67_0==IDENTIFIER||LA67_0==INT||LA67_0==LONG||LA67_0==SHORT) ) {
				alt67=1;
			}
			else if ( (LA67_0==QUES) ) {
				alt67=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				NoViableAltException nvae =
					new NoViableAltException("", 67, 0, input);
				throw nvae;
			}

			switch (alt67) {
				case 1 :
					// src/main/resources/parser/Java.g:858:9: type
					{
					pushFollow(FOLLOW_type_in_typeArgument3324);
					type();
					state._fsp--;
					if (state.failed) return retval;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:859:9: '?' ( ( 'extends' | 'super' ) type )?
					{
					match(input,QUES,FOLLOW_QUES_in_typeArgument3334); if (state.failed) return retval;
					// src/main/resources/parser/Java.g:860:9: ( ( 'extends' | 'super' ) type )?
					int alt66=2;
					int LA66_0 = input.LA(1);
					if ( (LA66_0==EXTENDS||LA66_0==SUPER) ) {
						alt66=1;
					}
					switch (alt66) {
						case 1 :
							// src/main/resources/parser/Java.g:861:13: ( 'extends' | 'super' ) type
							{
							if ( input.LA(1)==EXTENDS||input.LA(1)==SUPER ) {
								input.consume();
								state.errorRecovery=false;
								state.failed=false;
							}
							else {
								if (state.backtracking>0) {state.failed=true; return retval;}
								MismatchedSetException mse = new MismatchedSetException(null,input);
								throw mse;
							}
							pushFollow(FOLLOW_type_in_typeArgument3403);
							type();
							state._fsp--;
							if (state.failed) return retval;
							}
							break;

					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			        typeArgumentDescr = popTypeArgument();
			        if (typeArgumentDescr != null) {
			            updateOnAfter(typeArgumentDescr, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
			            HasTypeArguments top = peekHasTypeArguments();
			            if ( top != null) {
			                top.addArgument(typeArgumentDescr);
			            }
			        } else {
			            //TODO warning, by construction current typeArgumentDescr is expected
			        }
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
		return retval;
	}
	// $ANTLR end "typeArgument"



	// $ANTLR start "qualifiedNameList"
	// src/main/resources/parser/Java.g:868:1: qualifiedNameList : qualifiedName ( ',' qualifiedName )* ;
	public final void qualifiedNameList() throws RecognitionException {
		int qualifiedNameList_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 37) ) { return; }

			// src/main/resources/parser/Java.g:869:5: ( qualifiedName ( ',' qualifiedName )* )
			// src/main/resources/parser/Java.g:869:9: qualifiedName ( ',' qualifiedName )*
			{
			pushFollow(FOLLOW_qualifiedName_in_qualifiedNameList3434);
			qualifiedName();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:870:9: ( ',' qualifiedName )*
			loop68:
			while (true) {
				int alt68=2;
				int LA68_0 = input.LA(1);
				if ( (LA68_0==COMMA) ) {
					alt68=1;
				}

				switch (alt68) {
				case 1 :
					// src/main/resources/parser/Java.g:870:10: ',' qualifiedName
					{
					match(input,COMMA,FOLLOW_COMMA_in_qualifiedNameList3445); if (state.failed) return;
					pushFollow(FOLLOW_qualifiedName_in_qualifiedNameList3447);
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
	// src/main/resources/parser/Java.g:874:1: formalParameters : p1= '(' ( formalParameterDecls )? p2= ')' ;
	public final void formalParameters() throws RecognitionException {
		int formalParameters_StartIndex = input.index();

		Token p1=null;
		Token p2=null;

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 38) ) { return; }

			// src/main/resources/parser/Java.g:875:5: (p1= '(' ( formalParameterDecls )? p2= ')' )
			// src/main/resources/parser/Java.g:875:9: p1= '(' ( formalParameterDecls )? p2= ')'
			{
			p1=(Token)match(input,LPAREN,FOLLOW_LPAREN_in_formalParameters3479); if (state.failed) return;
			if ( state.backtracking==0 ) { setFormalParamsStart((p1!=null?p1.getText():null), line(p1), position(p1)); }
			// src/main/resources/parser/Java.g:876:9: ( formalParameterDecls )?
			int alt69=2;
			int LA69_0 = input.LA(1);
			if ( (LA69_0==BOOLEAN||LA69_0==BYTE||LA69_0==CHAR||LA69_0==DOUBLE||LA69_0==FINAL||LA69_0==FLOAT||LA69_0==IDENTIFIER||LA69_0==INT||LA69_0==LONG||LA69_0==MONKEYS_AT||LA69_0==SHORT) ) {
				alt69=1;
			}
			switch (alt69) {
				case 1 :
					// src/main/resources/parser/Java.g:876:10: formalParameterDecls
					{
					pushFollow(FOLLOW_formalParameterDecls_in_formalParameters3493);
					formalParameterDecls();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			p2=(Token)match(input,RPAREN,FOLLOW_RPAREN_in_formalParameters3518); if (state.failed) return;
			if ( state.backtracking==0 ) { setFormalParamsStop((p2!=null?p2.getText():null), line(p2), position(p2)); }
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
	// src/main/resources/parser/Java.g:881:1: formalParameterDecls : ( ellipsisParameterDecl | normalParameterDecl ( ',' normalParameterDecl )* | ( normalParameterDecl ',' )+ ellipsisParameterDecl );
	public final void formalParameterDecls() throws RecognitionException {
		int formalParameterDecls_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 39) ) { return; }

			// src/main/resources/parser/Java.g:882:5: ( ellipsisParameterDecl | normalParameterDecl ( ',' normalParameterDecl )* | ( normalParameterDecl ',' )+ ellipsisParameterDecl )
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
					// src/main/resources/parser/Java.g:882:9: ellipsisParameterDecl
					{
					pushFollow(FOLLOW_ellipsisParameterDecl_in_formalParameterDecls3540);
					ellipsisParameterDecl();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:883:9: normalParameterDecl ( ',' normalParameterDecl )*
					{
					pushFollow(FOLLOW_normalParameterDecl_in_formalParameterDecls3550);
					normalParameterDecl();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:884:9: ( ',' normalParameterDecl )*
					loop70:
					while (true) {
						int alt70=2;
						int LA70_0 = input.LA(1);
						if ( (LA70_0==COMMA) ) {
							alt70=1;
						}

						switch (alt70) {
						case 1 :
							// src/main/resources/parser/Java.g:884:10: ',' normalParameterDecl
							{
							match(input,COMMA,FOLLOW_COMMA_in_formalParameterDecls3561); if (state.failed) return;
							pushFollow(FOLLOW_normalParameterDecl_in_formalParameterDecls3563);
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
					// src/main/resources/parser/Java.g:886:9: ( normalParameterDecl ',' )+ ellipsisParameterDecl
					{
					// src/main/resources/parser/Java.g:886:9: ( normalParameterDecl ',' )+
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
							// src/main/resources/parser/Java.g:886:10: normalParameterDecl ','
							{
							pushFollow(FOLLOW_normalParameterDecl_in_formalParameterDecls3585);
							normalParameterDecl();
							state._fsp--;
							if (state.failed) return;
							match(input,COMMA,FOLLOW_COMMA_in_formalParameterDecls3595); if (state.failed) return;
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

					pushFollow(FOLLOW_ellipsisParameterDecl_in_formalParameterDecls3617);
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


	public static class normalParameterDecl_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "normalParameterDecl"
	// src/main/resources/parser/Java.g:892:1: normalParameterDecl : variableModifiers type IDENTIFIER (p1= '[' p2= ']' )* ;
	public final JavaParser.normalParameterDecl_return normalParameterDecl() throws RecognitionException {
		JavaParser.normalParameterDecl_return retval = new JavaParser.normalParameterDecl_return();
		retval.start = input.LT(1);
		int normalParameterDecl_StartIndex = input.index();

		Token p1=null;
		Token p2=null;
		Token IDENTIFIER3=null;


		         NormalParameterDescr param = null;
		         if (!isBacktracking()) {
		             log("Start NormalParameterDeclaration");
		             param = new NormalParameterDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((retval.start)), position((retval.start)), null);
		             context.push(param);
		         }
		     
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 40) ) { return retval; }

			// src/main/resources/parser/Java.g:910:5: ( variableModifiers type IDENTIFIER (p1= '[' p2= ']' )* )
			// src/main/resources/parser/Java.g:910:9: variableModifiers type IDENTIFIER (p1= '[' p2= ']' )*
			{
			pushFollow(FOLLOW_variableModifiers_in_normalParameterDecl3656);
			variableModifiers();
			state._fsp--;
			if (state.failed) return retval;
			pushFollow(FOLLOW_type_in_normalParameterDecl3658);
			type();
			state._fsp--;
			if (state.failed) return retval;
			IDENTIFIER3=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_normalParameterDecl3660); if (state.failed) return retval;
			if ( state.backtracking==0 ) { param.setName((IDENTIFIER3!=null?IDENTIFIER3.getText():null)); }
			// src/main/resources/parser/Java.g:911:9: (p1= '[' p2= ']' )*
			loop73:
			while (true) {
				int alt73=2;
				int LA73_0 = input.LA(1);
				if ( (LA73_0==LBRACKET) ) {
					alt73=1;
				}

				switch (alt73) {
				case 1 :
					// src/main/resources/parser/Java.g:911:10: p1= '[' p2= ']'
					{
					p1=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_normalParameterDecl3675); if (state.failed) return retval;
					p2=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_normalParameterDecl3679); if (state.failed) return retval;
					if ( state.backtracking==0 ) { param.addDimension(new DimensionDescr((p1!=null?p1.getText():null), line(p1), position(p1), (p2!=null?p2.getText():null), line(p2), position(p2))); }
					}
					break;

				default :
					break loop73;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			         param = popNormalParameter();
			         if (param != null) {
			             updateOnAfter(param, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
			             processParameter(param);
			         } else {
			             //TODO warning, by construction current param is expected
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
		return retval;
	}
	// $ANTLR end "normalParameterDecl"


	public static class ellipsisParameterDecl_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "ellipsisParameterDecl"
	// src/main/resources/parser/Java.g:915:1: ellipsisParameterDecl : variableModifiers type e= '...' IDENTIFIER ;
	public final JavaParser.ellipsisParameterDecl_return ellipsisParameterDecl() throws RecognitionException {
		JavaParser.ellipsisParameterDecl_return retval = new JavaParser.ellipsisParameterDecl_return();
		retval.start = input.LT(1);
		int ellipsisParameterDecl_StartIndex = input.index();

		Token e=null;
		Token IDENTIFIER4=null;


		          EllipsisParameterDescr ellipsisParam = null;
		          if (!isBacktracking()) {
		              log("Start EllipsisParameterDeclarationDesc");
		              ellipsisParam = new EllipsisParameterDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((retval.start)), position((retval.start)), null);
		              context.push(ellipsisParam);
		          }
		      
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 41) ) { return retval; }

			// src/main/resources/parser/Java.g:934:5: ( variableModifiers type e= '...' IDENTIFIER )
			// src/main/resources/parser/Java.g:934:9: variableModifiers type e= '...' IDENTIFIER
			{
			pushFollow(FOLLOW_variableModifiers_in_ellipsisParameterDecl3733);
			variableModifiers();
			state._fsp--;
			if (state.failed) return retval;
			pushFollow(FOLLOW_type_in_ellipsisParameterDecl3743);
			type();
			state._fsp--;
			if (state.failed) return retval;
			e=(Token)match(input,ELLIPSIS,FOLLOW_ELLIPSIS_in_ellipsisParameterDecl3748); if (state.failed) return retval;
			if ( state.backtracking==0 ) { ellipsisParam.setEllipsisToken(new TextTokenElementDescr((e!=null?e.getText():null), line(e), position(e))); }
			IDENTIFIER4=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_ellipsisParameterDecl3760); if (state.failed) return retval;
			if ( state.backtracking==0 ) { ellipsisParam.setName((IDENTIFIER4!=null?IDENTIFIER4.getText():null)); }
			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			         ellipsisParam = popEllipsisParameter();
			         if (ellipsisParam != null) {
			             updateOnAfter(ellipsisParam, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
			             processParameter(ellipsisParam);
			         } else {
			             //TODO warning, by construction current ellipsis parameterDesc is expected
			         }
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
		return retval;
	}
	// $ANTLR end "ellipsisParameterDecl"



	// $ANTLR start "explicitConstructorInvocation"
	// src/main/resources/parser/Java.g:940:1: explicitConstructorInvocation : ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' | primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';' );
	public final void explicitConstructorInvocation() throws RecognitionException {
		int explicitConstructorInvocation_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 42) ) { return; }

			// src/main/resources/parser/Java.g:941:5: ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' | primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';' )
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
					// src/main/resources/parser/Java.g:941:9: ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';'
					{
					// src/main/resources/parser/Java.g:941:9: ( nonWildcardTypeArguments )?
					int alt74=2;
					int LA74_0 = input.LA(1);
					if ( (LA74_0==LT) ) {
						alt74=1;
					}
					switch (alt74) {
						case 1 :
							// src/main/resources/parser/Java.g:941:10: nonWildcardTypeArguments
							{
							pushFollow(FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation3784);
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
					pushFollow(FOLLOW_arguments_in_explicitConstructorInvocation3842);
					arguments();
					state._fsp--;
					if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_explicitConstructorInvocation3844); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:948:9: primary '.' ( nonWildcardTypeArguments )? 'super' arguments ';'
					{
					pushFollow(FOLLOW_primary_in_explicitConstructorInvocation3855);
					primary();
					state._fsp--;
					if (state.failed) return;
					match(input,DOT,FOLLOW_DOT_in_explicitConstructorInvocation3865); if (state.failed) return;
					// src/main/resources/parser/Java.g:950:9: ( nonWildcardTypeArguments )?
					int alt75=2;
					int LA75_0 = input.LA(1);
					if ( (LA75_0==LT) ) {
						alt75=1;
					}
					switch (alt75) {
						case 1 :
							// src/main/resources/parser/Java.g:950:10: nonWildcardTypeArguments
							{
							pushFollow(FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation3876);
							nonWildcardTypeArguments();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SUPER,FOLLOW_SUPER_in_explicitConstructorInvocation3897); if (state.failed) return;
					pushFollow(FOLLOW_arguments_in_explicitConstructorInvocation3907);
					arguments();
					state._fsp--;
					if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_explicitConstructorInvocation3909); if (state.failed) return;
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


	public static class qualifiedName_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "qualifiedName"
	// src/main/resources/parser/Java.g:956:1: qualifiedName : id1= IDENTIFIER ( '.' id2= IDENTIFIER )* ;
	public final JavaParser.qualifiedName_return qualifiedName() throws RecognitionException {
		JavaParser.qualifiedName_return retval = new JavaParser.qualifiedName_return();
		retval.start = input.LT(1);
		int qualifiedName_StartIndex = input.index();

		Token id1=null;
		Token id2=null;


		         QualifiedNameDescr nameDescr = null;
		         if (!isBacktracking()) {
		             log("Start qualifiedName declaration");
		             nameDescr = new QualifiedNameDescr(input.toString(retval.start,input.LT(-1)), start((CommonToken)(retval.start)), -1, line((retval.start)), position((retval.start)));
		             context.push(nameDescr);
		         }
		     
		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 43) ) { return retval; }

			// src/main/resources/parser/Java.g:974:5: (id1= IDENTIFIER ( '.' id2= IDENTIFIER )* )
			// src/main/resources/parser/Java.g:974:9: id1= IDENTIFIER ( '.' id2= IDENTIFIER )*
			{
			id1=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_qualifiedName3950); if (state.failed) return retval;
			if ( state.backtracking==0 ) { nameDescr.addElement( new TextTokenElementDescr((id1!=null?id1.getText():null), line(id1), position(id1)) ); }
			// src/main/resources/parser/Java.g:975:9: ( '.' id2= IDENTIFIER )*
			loop77:
			while (true) {
				int alt77=2;
				int LA77_0 = input.LA(1);
				if ( (LA77_0==DOT) ) {
					alt77=1;
				}

				switch (alt77) {
				case 1 :
					// src/main/resources/parser/Java.g:975:10: '.' id2= IDENTIFIER
					{
					match(input,DOT,FOLLOW_DOT_in_qualifiedName3963); if (state.failed) return retval;
					id2=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_qualifiedName3967); if (state.failed) return retval;
					if ( state.backtracking==0 ) { nameDescr.addElement( new TextTokenElementDescr((id1!=null?id1.getText():null), line(id1), position(id1)) ); }
					}
					break;

				default :
					break loop77;
				}
			}

			}

			retval.stop = input.LT(-1);

			if ( state.backtracking==0 ) {
			         nameDescr = popQualifiedName();
			         if (nameDescr != null) {
			             updateOnAfter(nameDescr, input.toString(retval.start,input.LT(-1)), (CommonToken)(retval.stop));
			             processQualifiedName(nameDescr);
			         } else {
			             //TODO warning, by construction current qualifiedname param is expected
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
		return retval;
	}
	// $ANTLR end "qualifiedName"



	// $ANTLR start "annotations"
	// src/main/resources/parser/Java.g:979:1: annotations : ( annotation )+ ;
	public final void annotations() throws RecognitionException {
		int annotations_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 44) ) { return; }

			// src/main/resources/parser/Java.g:980:5: ( ( annotation )+ )
			// src/main/resources/parser/Java.g:980:9: ( annotation )+
			{
			// src/main/resources/parser/Java.g:980:9: ( annotation )+
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
					// src/main/resources/parser/Java.g:980:10: annotation
					{
					pushFollow(FOLLOW_annotation_in_annotations4001);
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
	// src/main/resources/parser/Java.g:988:1: annotation : '@' qualifiedName ( '(' ( elementValuePairs | elementValue )? ')' )? ;
	public final void annotation() throws RecognitionException {
		int annotation_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 45) ) { return; }

			// src/main/resources/parser/Java.g:989:5: ( '@' qualifiedName ( '(' ( elementValuePairs | elementValue )? ')' )? )
			// src/main/resources/parser/Java.g:989:9: '@' qualifiedName ( '(' ( elementValuePairs | elementValue )? ')' )?
			{
			match(input,MONKEYS_AT,FOLLOW_MONKEYS_AT_in_annotation4034); if (state.failed) return;
			pushFollow(FOLLOW_qualifiedName_in_annotation4036);
			qualifiedName();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:990:9: ( '(' ( elementValuePairs | elementValue )? ')' )?
			int alt80=2;
			int LA80_0 = input.LA(1);
			if ( (LA80_0==LPAREN) ) {
				alt80=1;
			}
			switch (alt80) {
				case 1 :
					// src/main/resources/parser/Java.g:990:13: '(' ( elementValuePairs | elementValue )? ')'
					{
					match(input,LPAREN,FOLLOW_LPAREN_in_annotation4050); if (state.failed) return;
					// src/main/resources/parser/Java.g:991:19: ( elementValuePairs | elementValue )?
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
							// src/main/resources/parser/Java.g:991:23: elementValuePairs
							{
							pushFollow(FOLLOW_elementValuePairs_in_annotation4074);
							elementValuePairs();
							state._fsp--;
							if (state.failed) return;
							}
							break;
						case 2 :
							// src/main/resources/parser/Java.g:992:23: elementValue
							{
							pushFollow(FOLLOW_elementValue_in_annotation4098);
							elementValue();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,RPAREN,FOLLOW_RPAREN_in_annotation4134); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:998:1: elementValuePairs : elementValuePair ( ',' elementValuePair )* ;
	public final void elementValuePairs() throws RecognitionException {
		int elementValuePairs_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 46) ) { return; }

			// src/main/resources/parser/Java.g:999:5: ( elementValuePair ( ',' elementValuePair )* )
			// src/main/resources/parser/Java.g:999:9: elementValuePair ( ',' elementValuePair )*
			{
			pushFollow(FOLLOW_elementValuePair_in_elementValuePairs4166);
			elementValuePair();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1000:9: ( ',' elementValuePair )*
			loop81:
			while (true) {
				int alt81=2;
				int LA81_0 = input.LA(1);
				if ( (LA81_0==COMMA) ) {
					alt81=1;
				}

				switch (alt81) {
				case 1 :
					// src/main/resources/parser/Java.g:1000:10: ',' elementValuePair
					{
					match(input,COMMA,FOLLOW_COMMA_in_elementValuePairs4177); if (state.failed) return;
					pushFollow(FOLLOW_elementValuePair_in_elementValuePairs4179);
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
	// src/main/resources/parser/Java.g:1004:1: elementValuePair : IDENTIFIER '=' elementValue ;
	public final void elementValuePair() throws RecognitionException {
		int elementValuePair_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 47) ) { return; }

			// src/main/resources/parser/Java.g:1005:5: ( IDENTIFIER '=' elementValue )
			// src/main/resources/parser/Java.g:1005:9: IDENTIFIER '=' elementValue
			{
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_elementValuePair4210); if (state.failed) return;
			match(input,EQ,FOLLOW_EQ_in_elementValuePair4212); if (state.failed) return;
			pushFollow(FOLLOW_elementValue_in_elementValuePair4214);
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
	// src/main/resources/parser/Java.g:1008:1: elementValue : ( conditionalExpression | annotation | elementValueArrayInitializer );
	public final void elementValue() throws RecognitionException {
		int elementValue_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 48) ) { return; }

			// src/main/resources/parser/Java.g:1009:5: ( conditionalExpression | annotation | elementValueArrayInitializer )
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
					// src/main/resources/parser/Java.g:1009:9: conditionalExpression
					{
					pushFollow(FOLLOW_conditionalExpression_in_elementValue4234);
					conditionalExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1010:9: annotation
					{
					pushFollow(FOLLOW_annotation_in_elementValue4244);
					annotation();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1011:9: elementValueArrayInitializer
					{
					pushFollow(FOLLOW_elementValueArrayInitializer_in_elementValue4254);
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
	// src/main/resources/parser/Java.g:1014:1: elementValueArrayInitializer : '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}' ;
	public final void elementValueArrayInitializer() throws RecognitionException {
		int elementValueArrayInitializer_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 49) ) { return; }

			// src/main/resources/parser/Java.g:1015:5: ( '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}' )
			// src/main/resources/parser/Java.g:1015:9: '{' ( elementValue ( ',' elementValue )* )? ( ',' )? '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_elementValueArrayInitializer4274); if (state.failed) return;
			// src/main/resources/parser/Java.g:1016:9: ( elementValue ( ',' elementValue )* )?
			int alt84=2;
			int LA84_0 = input.LA(1);
			if ( (LA84_0==BANG||LA84_0==BOOLEAN||LA84_0==BYTE||(LA84_0 >= CHAR && LA84_0 <= CHARLITERAL)||(LA84_0 >= DOUBLE && LA84_0 <= DOUBLELITERAL)||LA84_0==FALSE||(LA84_0 >= FLOAT && LA84_0 <= FLOATLITERAL)||LA84_0==IDENTIFIER||LA84_0==INT||LA84_0==INTLITERAL||LA84_0==LBRACE||(LA84_0 >= LONG && LA84_0 <= LPAREN)||LA84_0==MONKEYS_AT||(LA84_0 >= NEW && LA84_0 <= NULL)||LA84_0==PLUS||LA84_0==PLUSPLUS||LA84_0==SHORT||(LA84_0 >= STRINGLITERAL && LA84_0 <= SUB)||(LA84_0 >= SUBSUB && LA84_0 <= SUPER)||LA84_0==THIS||LA84_0==TILDE||LA84_0==TRUE||LA84_0==VOID) ) {
				alt84=1;
			}
			switch (alt84) {
				case 1 :
					// src/main/resources/parser/Java.g:1016:10: elementValue ( ',' elementValue )*
					{
					pushFollow(FOLLOW_elementValue_in_elementValueArrayInitializer4285);
					elementValue();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:1017:13: ( ',' elementValue )*
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
							// src/main/resources/parser/Java.g:1017:14: ',' elementValue
							{
							match(input,COMMA,FOLLOW_COMMA_in_elementValueArrayInitializer4300); if (state.failed) return;
							pushFollow(FOLLOW_elementValue_in_elementValueArrayInitializer4302);
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

			// src/main/resources/parser/Java.g:1019:12: ( ',' )?
			int alt85=2;
			int LA85_0 = input.LA(1);
			if ( (LA85_0==COMMA) ) {
				alt85=1;
			}
			switch (alt85) {
				case 1 :
					// src/main/resources/parser/Java.g:1019:13: ','
					{
					match(input,COMMA,FOLLOW_COMMA_in_elementValueArrayInitializer4331); if (state.failed) return;
					}
					break;

			}

			match(input,RBRACE,FOLLOW_RBRACE_in_elementValueArrayInitializer4335); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1026:1: annotationTypeDeclaration : modifiers '@' 'interface' IDENTIFIER annotationTypeBody ;
	public final void annotationTypeDeclaration() throws RecognitionException {
		int annotationTypeDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 50) ) { return; }

			// src/main/resources/parser/Java.g:1027:5: ( modifiers '@' 'interface' IDENTIFIER annotationTypeBody )
			// src/main/resources/parser/Java.g:1027:9: modifiers '@' 'interface' IDENTIFIER annotationTypeBody
			{
			pushFollow(FOLLOW_modifiers_in_annotationTypeDeclaration4358);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			match(input,MONKEYS_AT,FOLLOW_MONKEYS_AT_in_annotationTypeDeclaration4360); if (state.failed) return;
			match(input,INTERFACE,FOLLOW_INTERFACE_in_annotationTypeDeclaration4370); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_annotationTypeDeclaration4380); if (state.failed) return;
			pushFollow(FOLLOW_annotationTypeBody_in_annotationTypeDeclaration4390);
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
	// src/main/resources/parser/Java.g:1034:1: annotationTypeBody : '{' ( annotationTypeElementDeclaration )* '}' ;
	public final void annotationTypeBody() throws RecognitionException {
		int annotationTypeBody_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 51) ) { return; }

			// src/main/resources/parser/Java.g:1035:5: ( '{' ( annotationTypeElementDeclaration )* '}' )
			// src/main/resources/parser/Java.g:1035:9: '{' ( annotationTypeElementDeclaration )* '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_annotationTypeBody4411); if (state.failed) return;
			// src/main/resources/parser/Java.g:1036:9: ( annotationTypeElementDeclaration )*
			loop86:
			while (true) {
				int alt86=2;
				int LA86_0 = input.LA(1);
				if ( (LA86_0==ABSTRACT||LA86_0==BOOLEAN||LA86_0==BYTE||LA86_0==CHAR||LA86_0==CLASS||LA86_0==DOUBLE||LA86_0==ENUM||LA86_0==FINAL||LA86_0==FLOAT||LA86_0==IDENTIFIER||(LA86_0 >= INT && LA86_0 <= INTERFACE)||LA86_0==LONG||LA86_0==LT||(LA86_0 >= MONKEYS_AT && LA86_0 <= NATIVE)||(LA86_0 >= PRIVATE && LA86_0 <= PUBLIC)||(LA86_0 >= SEMI && LA86_0 <= SHORT)||(LA86_0 >= STATIC && LA86_0 <= STRICTFP)||LA86_0==SYNCHRONIZED||LA86_0==TRANSIENT||(LA86_0 >= VOID && LA86_0 <= VOLATILE)) ) {
					alt86=1;
				}

				switch (alt86) {
				case 1 :
					// src/main/resources/parser/Java.g:1036:10: annotationTypeElementDeclaration
					{
					pushFollow(FOLLOW_annotationTypeElementDeclaration_in_annotationTypeBody4423);
					annotationTypeElementDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop86;
				}
			}

			match(input,RBRACE,FOLLOW_RBRACE_in_annotationTypeBody4445); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1044:1: annotationTypeElementDeclaration : ( annotationMethodDeclaration | interfaceFieldDeclaration | normalClassDeclaration | normalInterfaceDeclaration | enumDeclaration | annotationTypeDeclaration | ';' );
	public final void annotationTypeElementDeclaration() throws RecognitionException {
		int annotationTypeElementDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 52) ) { return; }

			// src/main/resources/parser/Java.g:1045:5: ( annotationMethodDeclaration | interfaceFieldDeclaration | normalClassDeclaration | normalInterfaceDeclaration | enumDeclaration | annotationTypeDeclaration | ';' )
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
					// src/main/resources/parser/Java.g:1045:9: annotationMethodDeclaration
					{
					pushFollow(FOLLOW_annotationMethodDeclaration_in_annotationTypeElementDeclaration4467);
					annotationMethodDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1046:9: interfaceFieldDeclaration
					{
					pushFollow(FOLLOW_interfaceFieldDeclaration_in_annotationTypeElementDeclaration4477);
					interfaceFieldDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1047:9: normalClassDeclaration
					{
					pushFollow(FOLLOW_normalClassDeclaration_in_annotationTypeElementDeclaration4487);
					normalClassDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1048:9: normalInterfaceDeclaration
					{
					pushFollow(FOLLOW_normalInterfaceDeclaration_in_annotationTypeElementDeclaration4497);
					normalInterfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1049:9: enumDeclaration
					{
					pushFollow(FOLLOW_enumDeclaration_in_annotationTypeElementDeclaration4507);
					enumDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:1050:9: annotationTypeDeclaration
					{
					pushFollow(FOLLOW_annotationTypeDeclaration_in_annotationTypeElementDeclaration4517);
					annotationTypeDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:1051:9: ';'
					{
					match(input,SEMI,FOLLOW_SEMI_in_annotationTypeElementDeclaration4527); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1054:1: annotationMethodDeclaration : modifiers type IDENTIFIER '(' ')' ( 'default' elementValue )? ';' ;
	public final void annotationMethodDeclaration() throws RecognitionException {
		int annotationMethodDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 53) ) { return; }

			// src/main/resources/parser/Java.g:1055:5: ( modifiers type IDENTIFIER '(' ')' ( 'default' elementValue )? ';' )
			// src/main/resources/parser/Java.g:1055:9: modifiers type IDENTIFIER '(' ')' ( 'default' elementValue )? ';'
			{
			pushFollow(FOLLOW_modifiers_in_annotationMethodDeclaration4547);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_annotationMethodDeclaration4549);
			type();
			state._fsp--;
			if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_annotationMethodDeclaration4551); if (state.failed) return;
			match(input,LPAREN,FOLLOW_LPAREN_in_annotationMethodDeclaration4561); if (state.failed) return;
			match(input,RPAREN,FOLLOW_RPAREN_in_annotationMethodDeclaration4563); if (state.failed) return;
			// src/main/resources/parser/Java.g:1056:17: ( 'default' elementValue )?
			int alt88=2;
			int LA88_0 = input.LA(1);
			if ( (LA88_0==DEFAULT) ) {
				alt88=1;
			}
			switch (alt88) {
				case 1 :
					// src/main/resources/parser/Java.g:1056:18: 'default' elementValue
					{
					match(input,DEFAULT,FOLLOW_DEFAULT_in_annotationMethodDeclaration4566); if (state.failed) return;
					pushFollow(FOLLOW_elementValue_in_annotationMethodDeclaration4568);
					elementValue();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,SEMI,FOLLOW_SEMI_in_annotationMethodDeclaration4597); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1061:1: block : '{' ( blockStatement )* '}' ;
	public final void block() throws RecognitionException {
		int block_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 54) ) { return; }

			// src/main/resources/parser/Java.g:1062:5: ( '{' ( blockStatement )* '}' )
			// src/main/resources/parser/Java.g:1062:9: '{' ( blockStatement )* '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_block4621); if (state.failed) return;
			// src/main/resources/parser/Java.g:1063:9: ( blockStatement )*
			loop89:
			while (true) {
				int alt89=2;
				int LA89_0 = input.LA(1);
				if ( (LA89_0==ABSTRACT||(LA89_0 >= ASSERT && LA89_0 <= BANG)||(LA89_0 >= BOOLEAN && LA89_0 <= BYTE)||(LA89_0 >= CHAR && LA89_0 <= CLASS)||LA89_0==CONTINUE||LA89_0==DO||(LA89_0 >= DOUBLE && LA89_0 <= DOUBLELITERAL)||LA89_0==ENUM||(LA89_0 >= FALSE && LA89_0 <= FINAL)||(LA89_0 >= FLOAT && LA89_0 <= FOR)||(LA89_0 >= IDENTIFIER && LA89_0 <= IF)||(LA89_0 >= INT && LA89_0 <= INTLITERAL)||LA89_0==LBRACE||(LA89_0 >= LONG && LA89_0 <= LT)||(LA89_0 >= MONKEYS_AT && LA89_0 <= NULL)||LA89_0==PLUS||(LA89_0 >= PLUSPLUS && LA89_0 <= PUBLIC)||LA89_0==RETURN||(LA89_0 >= SEMI && LA89_0 <= SHORT)||(LA89_0 >= STATIC && LA89_0 <= SUB)||(LA89_0 >= SUBSUB && LA89_0 <= SYNCHRONIZED)||(LA89_0 >= THIS && LA89_0 <= THROW)||(LA89_0 >= TILDE && LA89_0 <= WHILE)) ) {
					alt89=1;
				}

				switch (alt89) {
				case 1 :
					// src/main/resources/parser/Java.g:1063:10: blockStatement
					{
					pushFollow(FOLLOW_blockStatement_in_block4632);
					blockStatement();
					state._fsp--;
					if (state.failed) return;
					}
					break;

				default :
					break loop89;
				}
			}

			match(input,RBRACE,FOLLOW_RBRACE_in_block4653); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1092:1: blockStatement : ( localVariableDeclarationStatement | classOrInterfaceDeclaration | statement );
	public final void blockStatement() throws RecognitionException {
		int blockStatement_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 55) ) { return; }

			// src/main/resources/parser/Java.g:1093:5: ( localVariableDeclarationStatement | classOrInterfaceDeclaration | statement )
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
					// src/main/resources/parser/Java.g:1093:9: localVariableDeclarationStatement
					{
					pushFollow(FOLLOW_localVariableDeclarationStatement_in_blockStatement4675);
					localVariableDeclarationStatement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1094:9: classOrInterfaceDeclaration
					{
					pushFollow(FOLLOW_classOrInterfaceDeclaration_in_blockStatement4685);
					classOrInterfaceDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1095:9: statement
					{
					pushFollow(FOLLOW_statement_in_blockStatement4695);
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
	// src/main/resources/parser/Java.g:1099:1: localVariableDeclarationStatement : localVariableDeclaration ';' ;
	public final void localVariableDeclarationStatement() throws RecognitionException {
		int localVariableDeclarationStatement_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 56) ) { return; }

			// src/main/resources/parser/Java.g:1100:5: ( localVariableDeclaration ';' )
			// src/main/resources/parser/Java.g:1100:9: localVariableDeclaration ';'
			{
			pushFollow(FOLLOW_localVariableDeclaration_in_localVariableDeclarationStatement4716);
			localVariableDeclaration();
			state._fsp--;
			if (state.failed) return;
			match(input,SEMI,FOLLOW_SEMI_in_localVariableDeclarationStatement4726); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1104:1: localVariableDeclaration : variableModifiers type variableDeclarator ( ',' variableDeclarator )* ;
	public final void localVariableDeclaration() throws RecognitionException {
		int localVariableDeclaration_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 57) ) { return; }

			// src/main/resources/parser/Java.g:1105:5: ( variableModifiers type variableDeclarator ( ',' variableDeclarator )* )
			// src/main/resources/parser/Java.g:1105:9: variableModifiers type variableDeclarator ( ',' variableDeclarator )*
			{
			pushFollow(FOLLOW_variableModifiers_in_localVariableDeclaration4746);
			variableModifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_localVariableDeclaration4748);
			type();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_variableDeclarator_in_localVariableDeclaration4758);
			variableDeclarator();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1107:9: ( ',' variableDeclarator )*
			loop91:
			while (true) {
				int alt91=2;
				int LA91_0 = input.LA(1);
				if ( (LA91_0==COMMA) ) {
					alt91=1;
				}

				switch (alt91) {
				case 1 :
					// src/main/resources/parser/Java.g:1107:10: ',' variableDeclarator
					{
					match(input,COMMA,FOLLOW_COMMA_in_localVariableDeclaration4769); if (state.failed) return;
					pushFollow(FOLLOW_variableDeclarator_in_localVariableDeclaration4771);
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
	// src/main/resources/parser/Java.g:1111:1: statement : ( block | ( 'assert' ) expression ( ':' expression )? ';' | 'assert' expression ( ':' expression )? ';' | 'if' parExpression statement ( 'else' statement )? | forstatement | 'while' parExpression statement | 'do' statement 'while' parExpression ';' | trystatement | 'switch' parExpression '{' switchBlockStatementGroups '}' | 'synchronized' parExpression block | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( IDENTIFIER )? ';' | 'continue' ( IDENTIFIER )? ';' | expression ';' | IDENTIFIER ':' statement | ';' );
	public final void statement() throws RecognitionException {
		int statement_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 58) ) { return; }

			// src/main/resources/parser/Java.g:1112:5: ( block | ( 'assert' ) expression ( ':' expression )? ';' | 'assert' expression ( ':' expression )? ';' | 'if' parExpression statement ( 'else' statement )? | forstatement | 'while' parExpression statement | 'do' statement 'while' parExpression ';' | trystatement | 'switch' parExpression '{' switchBlockStatementGroups '}' | 'synchronized' parExpression block | 'return' ( expression )? ';' | 'throw' expression ';' | 'break' ( IDENTIFIER )? ';' | 'continue' ( IDENTIFIER )? ';' | expression ';' | IDENTIFIER ':' statement | ';' )
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
					// src/main/resources/parser/Java.g:1112:9: block
					{
					pushFollow(FOLLOW_block_in_statement4802);
					block();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1114:9: ( 'assert' ) expression ( ':' expression )? ';'
					{
					// src/main/resources/parser/Java.g:1114:9: ( 'assert' )
					// src/main/resources/parser/Java.g:1114:10: 'assert'
					{
					match(input,ASSERT,FOLLOW_ASSERT_in_statement4826); if (state.failed) return;
					}

					pushFollow(FOLLOW_expression_in_statement4846);
					expression();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:1116:20: ( ':' expression )?
					int alt92=2;
					int LA92_0 = input.LA(1);
					if ( (LA92_0==COLON) ) {
						alt92=1;
					}
					switch (alt92) {
						case 1 :
							// src/main/resources/parser/Java.g:1116:21: ':' expression
							{
							match(input,COLON,FOLLOW_COLON_in_statement4849); if (state.failed) return;
							pushFollow(FOLLOW_expression_in_statement4851);
							expression();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_statement4855); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1117:9: 'assert' expression ( ':' expression )? ';'
					{
					match(input,ASSERT,FOLLOW_ASSERT_in_statement4865); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_statement4868);
					expression();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:1117:30: ( ':' expression )?
					int alt93=2;
					int LA93_0 = input.LA(1);
					if ( (LA93_0==COLON) ) {
						alt93=1;
					}
					switch (alt93) {
						case 1 :
							// src/main/resources/parser/Java.g:1117:31: ':' expression
							{
							match(input,COLON,FOLLOW_COLON_in_statement4871); if (state.failed) return;
							pushFollow(FOLLOW_expression_in_statement4873);
							expression();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_statement4877); if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1118:9: 'if' parExpression statement ( 'else' statement )?
					{
					match(input,IF,FOLLOW_IF_in_statement4899); if (state.failed) return;
					pushFollow(FOLLOW_parExpression_in_statement4901);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_statement_in_statement4903);
					statement();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:1118:38: ( 'else' statement )?
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
							// src/main/resources/parser/Java.g:1118:39: 'else' statement
							{
							match(input,ELSE,FOLLOW_ELSE_in_statement4906); if (state.failed) return;
							pushFollow(FOLLOW_statement_in_statement4908);
							statement();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1119:9: forstatement
					{
					pushFollow(FOLLOW_forstatement_in_statement4930);
					forstatement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:1120:9: 'while' parExpression statement
					{
					match(input,WHILE,FOLLOW_WHILE_in_statement4940); if (state.failed) return;
					pushFollow(FOLLOW_parExpression_in_statement4942);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_statement_in_statement4944);
					statement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:1121:9: 'do' statement 'while' parExpression ';'
					{
					match(input,DO,FOLLOW_DO_in_statement4954); if (state.failed) return;
					pushFollow(FOLLOW_statement_in_statement4956);
					statement();
					state._fsp--;
					if (state.failed) return;
					match(input,WHILE,FOLLOW_WHILE_in_statement4958); if (state.failed) return;
					pushFollow(FOLLOW_parExpression_in_statement4960);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_statement4962); if (state.failed) return;
					}
					break;
				case 8 :
					// src/main/resources/parser/Java.g:1122:9: trystatement
					{
					pushFollow(FOLLOW_trystatement_in_statement4972);
					trystatement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 9 :
					// src/main/resources/parser/Java.g:1123:9: 'switch' parExpression '{' switchBlockStatementGroups '}'
					{
					match(input,SWITCH,FOLLOW_SWITCH_in_statement4982); if (state.failed) return;
					pushFollow(FOLLOW_parExpression_in_statement4984);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					match(input,LBRACE,FOLLOW_LBRACE_in_statement4986); if (state.failed) return;
					pushFollow(FOLLOW_switchBlockStatementGroups_in_statement4988);
					switchBlockStatementGroups();
					state._fsp--;
					if (state.failed) return;
					match(input,RBRACE,FOLLOW_RBRACE_in_statement4990); if (state.failed) return;
					}
					break;
				case 10 :
					// src/main/resources/parser/Java.g:1124:9: 'synchronized' parExpression block
					{
					match(input,SYNCHRONIZED,FOLLOW_SYNCHRONIZED_in_statement5000); if (state.failed) return;
					pushFollow(FOLLOW_parExpression_in_statement5002);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_block_in_statement5004);
					block();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 11 :
					// src/main/resources/parser/Java.g:1125:9: 'return' ( expression )? ';'
					{
					match(input,RETURN,FOLLOW_RETURN_in_statement5014); if (state.failed) return;
					// src/main/resources/parser/Java.g:1125:18: ( expression )?
					int alt95=2;
					int LA95_0 = input.LA(1);
					if ( (LA95_0==BANG||LA95_0==BOOLEAN||LA95_0==BYTE||(LA95_0 >= CHAR && LA95_0 <= CHARLITERAL)||(LA95_0 >= DOUBLE && LA95_0 <= DOUBLELITERAL)||LA95_0==FALSE||(LA95_0 >= FLOAT && LA95_0 <= FLOATLITERAL)||LA95_0==IDENTIFIER||LA95_0==INT||LA95_0==INTLITERAL||(LA95_0 >= LONG && LA95_0 <= LPAREN)||(LA95_0 >= NEW && LA95_0 <= NULL)||LA95_0==PLUS||LA95_0==PLUSPLUS||LA95_0==SHORT||(LA95_0 >= STRINGLITERAL && LA95_0 <= SUB)||(LA95_0 >= SUBSUB && LA95_0 <= SUPER)||LA95_0==THIS||LA95_0==TILDE||LA95_0==TRUE||LA95_0==VOID) ) {
						alt95=1;
					}
					switch (alt95) {
						case 1 :
							// src/main/resources/parser/Java.g:1125:19: expression
							{
							pushFollow(FOLLOW_expression_in_statement5017);
							expression();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_statement5022); if (state.failed) return;
					}
					break;
				case 12 :
					// src/main/resources/parser/Java.g:1126:9: 'throw' expression ';'
					{
					match(input,THROW,FOLLOW_THROW_in_statement5032); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_statement5034);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_statement5036); if (state.failed) return;
					}
					break;
				case 13 :
					// src/main/resources/parser/Java.g:1127:9: 'break' ( IDENTIFIER )? ';'
					{
					match(input,BREAK,FOLLOW_BREAK_in_statement5046); if (state.failed) return;
					// src/main/resources/parser/Java.g:1128:13: ( IDENTIFIER )?
					int alt96=2;
					int LA96_0 = input.LA(1);
					if ( (LA96_0==IDENTIFIER) ) {
						alt96=1;
					}
					switch (alt96) {
						case 1 :
							// src/main/resources/parser/Java.g:1128:14: IDENTIFIER
							{
							match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_statement5061); if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_statement5078); if (state.failed) return;
					}
					break;
				case 14 :
					// src/main/resources/parser/Java.g:1130:9: 'continue' ( IDENTIFIER )? ';'
					{
					match(input,CONTINUE,FOLLOW_CONTINUE_in_statement5088); if (state.failed) return;
					// src/main/resources/parser/Java.g:1131:13: ( IDENTIFIER )?
					int alt97=2;
					int LA97_0 = input.LA(1);
					if ( (LA97_0==IDENTIFIER) ) {
						alt97=1;
					}
					switch (alt97) {
						case 1 :
							// src/main/resources/parser/Java.g:1131:14: IDENTIFIER
							{
							match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_statement5103); if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_statement5120); if (state.failed) return;
					}
					break;
				case 15 :
					// src/main/resources/parser/Java.g:1133:9: expression ';'
					{
					pushFollow(FOLLOW_expression_in_statement5130);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,SEMI,FOLLOW_SEMI_in_statement5133); if (state.failed) return;
					}
					break;
				case 16 :
					// src/main/resources/parser/Java.g:1134:9: IDENTIFIER ':' statement
					{
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_statement5148); if (state.failed) return;
					match(input,COLON,FOLLOW_COLON_in_statement5150); if (state.failed) return;
					pushFollow(FOLLOW_statement_in_statement5152);
					statement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 17 :
					// src/main/resources/parser/Java.g:1135:9: ';'
					{
					match(input,SEMI,FOLLOW_SEMI_in_statement5162); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1139:1: switchBlockStatementGroups : ( switchBlockStatementGroup )* ;
	public final void switchBlockStatementGroups() throws RecognitionException {
		int switchBlockStatementGroups_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 59) ) { return; }

			// src/main/resources/parser/Java.g:1140:5: ( ( switchBlockStatementGroup )* )
			// src/main/resources/parser/Java.g:1140:9: ( switchBlockStatementGroup )*
			{
			// src/main/resources/parser/Java.g:1140:9: ( switchBlockStatementGroup )*
			loop99:
			while (true) {
				int alt99=2;
				int LA99_0 = input.LA(1);
				if ( (LA99_0==CASE||LA99_0==DEFAULT) ) {
					alt99=1;
				}

				switch (alt99) {
				case 1 :
					// src/main/resources/parser/Java.g:1140:10: switchBlockStatementGroup
					{
					pushFollow(FOLLOW_switchBlockStatementGroup_in_switchBlockStatementGroups5184);
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
	// src/main/resources/parser/Java.g:1143:1: switchBlockStatementGroup : switchLabel ( blockStatement )* ;
	public final void switchBlockStatementGroup() throws RecognitionException {
		int switchBlockStatementGroup_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 60) ) { return; }

			// src/main/resources/parser/Java.g:1144:5: ( switchLabel ( blockStatement )* )
			// src/main/resources/parser/Java.g:1145:9: switchLabel ( blockStatement )*
			{
			pushFollow(FOLLOW_switchLabel_in_switchBlockStatementGroup5213);
			switchLabel();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1146:9: ( blockStatement )*
			loop100:
			while (true) {
				int alt100=2;
				int LA100_0 = input.LA(1);
				if ( (LA100_0==ABSTRACT||(LA100_0 >= ASSERT && LA100_0 <= BANG)||(LA100_0 >= BOOLEAN && LA100_0 <= BYTE)||(LA100_0 >= CHAR && LA100_0 <= CLASS)||LA100_0==CONTINUE||LA100_0==DO||(LA100_0 >= DOUBLE && LA100_0 <= DOUBLELITERAL)||LA100_0==ENUM||(LA100_0 >= FALSE && LA100_0 <= FINAL)||(LA100_0 >= FLOAT && LA100_0 <= FOR)||(LA100_0 >= IDENTIFIER && LA100_0 <= IF)||(LA100_0 >= INT && LA100_0 <= INTLITERAL)||LA100_0==LBRACE||(LA100_0 >= LONG && LA100_0 <= LT)||(LA100_0 >= MONKEYS_AT && LA100_0 <= NULL)||LA100_0==PLUS||(LA100_0 >= PLUSPLUS && LA100_0 <= PUBLIC)||LA100_0==RETURN||(LA100_0 >= SEMI && LA100_0 <= SHORT)||(LA100_0 >= STATIC && LA100_0 <= SUB)||(LA100_0 >= SUBSUB && LA100_0 <= SYNCHRONIZED)||(LA100_0 >= THIS && LA100_0 <= THROW)||(LA100_0 >= TILDE && LA100_0 <= WHILE)) ) {
					alt100=1;
				}

				switch (alt100) {
				case 1 :
					// src/main/resources/parser/Java.g:1146:10: blockStatement
					{
					pushFollow(FOLLOW_blockStatement_in_switchBlockStatementGroup5224);
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
	// src/main/resources/parser/Java.g:1150:1: switchLabel : ( 'case' expression ':' | 'default' ':' );
	public final void switchLabel() throws RecognitionException {
		int switchLabel_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 61) ) { return; }

			// src/main/resources/parser/Java.g:1151:5: ( 'case' expression ':' | 'default' ':' )
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
					// src/main/resources/parser/Java.g:1151:9: 'case' expression ':'
					{
					match(input,CASE,FOLLOW_CASE_in_switchLabel5255); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_switchLabel5257);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,COLON,FOLLOW_COLON_in_switchLabel5259); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1152:9: 'default' ':'
					{
					match(input,DEFAULT,FOLLOW_DEFAULT_in_switchLabel5269); if (state.failed) return;
					match(input,COLON,FOLLOW_COLON_in_switchLabel5271); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1156:1: trystatement : 'try' block ( catches 'finally' block | catches | 'finally' block ) ;
	public final void trystatement() throws RecognitionException {
		int trystatement_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 62) ) { return; }

			// src/main/resources/parser/Java.g:1157:5: ( 'try' block ( catches 'finally' block | catches | 'finally' block ) )
			// src/main/resources/parser/Java.g:1157:9: 'try' block ( catches 'finally' block | catches | 'finally' block )
			{
			match(input,TRY,FOLLOW_TRY_in_trystatement5292); if (state.failed) return;
			pushFollow(FOLLOW_block_in_trystatement5294);
			block();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1158:9: ( catches 'finally' block | catches | 'finally' block )
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
					// src/main/resources/parser/Java.g:1158:13: catches 'finally' block
					{
					pushFollow(FOLLOW_catches_in_trystatement5308);
					catches();
					state._fsp--;
					if (state.failed) return;
					match(input,FINALLY,FOLLOW_FINALLY_in_trystatement5310); if (state.failed) return;
					pushFollow(FOLLOW_block_in_trystatement5312);
					block();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1159:13: catches
					{
					pushFollow(FOLLOW_catches_in_trystatement5326);
					catches();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1160:13: 'finally' block
					{
					match(input,FINALLY,FOLLOW_FINALLY_in_trystatement5340); if (state.failed) return;
					pushFollow(FOLLOW_block_in_trystatement5342);
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
	// src/main/resources/parser/Java.g:1164:1: catches : catchClause ( catchClause )* ;
	public final void catches() throws RecognitionException {
		int catches_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 63) ) { return; }

			// src/main/resources/parser/Java.g:1165:5: ( catchClause ( catchClause )* )
			// src/main/resources/parser/Java.g:1165:9: catchClause ( catchClause )*
			{
			pushFollow(FOLLOW_catchClause_in_catches5373);
			catchClause();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1166:9: ( catchClause )*
			loop103:
			while (true) {
				int alt103=2;
				int LA103_0 = input.LA(1);
				if ( (LA103_0==CATCH) ) {
					alt103=1;
				}

				switch (alt103) {
				case 1 :
					// src/main/resources/parser/Java.g:1166:10: catchClause
					{
					pushFollow(FOLLOW_catchClause_in_catches5384);
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
	// src/main/resources/parser/Java.g:1170:1: catchClause : 'catch' '(' formalParameter ')' block ;
	public final void catchClause() throws RecognitionException {
		int catchClause_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 64) ) { return; }

			// src/main/resources/parser/Java.g:1171:5: ( 'catch' '(' formalParameter ')' block )
			// src/main/resources/parser/Java.g:1171:9: 'catch' '(' formalParameter ')' block
			{
			match(input,CATCH,FOLLOW_CATCH_in_catchClause5415); if (state.failed) return;
			match(input,LPAREN,FOLLOW_LPAREN_in_catchClause5417); if (state.failed) return;
			pushFollow(FOLLOW_formalParameter_in_catchClause5419);
			formalParameter();
			state._fsp--;
			if (state.failed) return;
			match(input,RPAREN,FOLLOW_RPAREN_in_catchClause5429); if (state.failed) return;
			pushFollow(FOLLOW_block_in_catchClause5431);
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
	// src/main/resources/parser/Java.g:1175:1: formalParameter : variableModifiers type IDENTIFIER ( '[' ']' )* ;
	public final void formalParameter() throws RecognitionException {
		int formalParameter_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 65) ) { return; }

			// src/main/resources/parser/Java.g:1176:5: ( variableModifiers type IDENTIFIER ( '[' ']' )* )
			// src/main/resources/parser/Java.g:1176:9: variableModifiers type IDENTIFIER ( '[' ']' )*
			{
			pushFollow(FOLLOW_variableModifiers_in_formalParameter5452);
			variableModifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_formalParameter5454);
			type();
			state._fsp--;
			if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_formalParameter5456); if (state.failed) return;
			// src/main/resources/parser/Java.g:1177:9: ( '[' ']' )*
			loop104:
			while (true) {
				int alt104=2;
				int LA104_0 = input.LA(1);
				if ( (LA104_0==LBRACKET) ) {
					alt104=1;
				}

				switch (alt104) {
				case 1 :
					// src/main/resources/parser/Java.g:1177:10: '[' ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_formalParameter5467); if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_formalParameter5469); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1181:1: forstatement : ( 'for' '(' variableModifiers type IDENTIFIER ':' expression ')' statement | 'for' '(' ( forInit )? ';' ( expression )? ';' ( expressionList )? ')' statement );
	public final void forstatement() throws RecognitionException {
		int forstatement_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 66) ) { return; }

			// src/main/resources/parser/Java.g:1182:5: ( 'for' '(' variableModifiers type IDENTIFIER ':' expression ')' statement | 'for' '(' ( forInit )? ';' ( expression )? ';' ( expressionList )? ')' statement )
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
					// src/main/resources/parser/Java.g:1184:9: 'for' '(' variableModifiers type IDENTIFIER ':' expression ')' statement
					{
					match(input,FOR,FOLLOW_FOR_in_forstatement5518); if (state.failed) return;
					match(input,LPAREN,FOLLOW_LPAREN_in_forstatement5520); if (state.failed) return;
					pushFollow(FOLLOW_variableModifiers_in_forstatement5522);
					variableModifiers();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_type_in_forstatement5524);
					type();
					state._fsp--;
					if (state.failed) return;
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_forstatement5526); if (state.failed) return;
					match(input,COLON,FOLLOW_COLON_in_forstatement5528); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_forstatement5539);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,RPAREN,FOLLOW_RPAREN_in_forstatement5541); if (state.failed) return;
					pushFollow(FOLLOW_statement_in_forstatement5543);
					statement();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1188:9: 'for' '(' ( forInit )? ';' ( expression )? ';' ( expressionList )? ')' statement
					{
					match(input,FOR,FOLLOW_FOR_in_forstatement5575); if (state.failed) return;
					match(input,LPAREN,FOLLOW_LPAREN_in_forstatement5577); if (state.failed) return;
					// src/main/resources/parser/Java.g:1189:17: ( forInit )?
					int alt105=2;
					int LA105_0 = input.LA(1);
					if ( (LA105_0==BANG||LA105_0==BOOLEAN||LA105_0==BYTE||(LA105_0 >= CHAR && LA105_0 <= CHARLITERAL)||(LA105_0 >= DOUBLE && LA105_0 <= DOUBLELITERAL)||(LA105_0 >= FALSE && LA105_0 <= FINAL)||(LA105_0 >= FLOAT && LA105_0 <= FLOATLITERAL)||LA105_0==IDENTIFIER||LA105_0==INT||LA105_0==INTLITERAL||(LA105_0 >= LONG && LA105_0 <= LPAREN)||LA105_0==MONKEYS_AT||(LA105_0 >= NEW && LA105_0 <= NULL)||LA105_0==PLUS||LA105_0==PLUSPLUS||LA105_0==SHORT||(LA105_0 >= STRINGLITERAL && LA105_0 <= SUB)||(LA105_0 >= SUBSUB && LA105_0 <= SUPER)||LA105_0==THIS||LA105_0==TILDE||LA105_0==TRUE||LA105_0==VOID) ) {
						alt105=1;
					}
					switch (alt105) {
						case 1 :
							// src/main/resources/parser/Java.g:1189:18: forInit
							{
							pushFollow(FOLLOW_forInit_in_forstatement5597);
							forInit();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_forstatement5618); if (state.failed) return;
					// src/main/resources/parser/Java.g:1191:17: ( expression )?
					int alt106=2;
					int LA106_0 = input.LA(1);
					if ( (LA106_0==BANG||LA106_0==BOOLEAN||LA106_0==BYTE||(LA106_0 >= CHAR && LA106_0 <= CHARLITERAL)||(LA106_0 >= DOUBLE && LA106_0 <= DOUBLELITERAL)||LA106_0==FALSE||(LA106_0 >= FLOAT && LA106_0 <= FLOATLITERAL)||LA106_0==IDENTIFIER||LA106_0==INT||LA106_0==INTLITERAL||(LA106_0 >= LONG && LA106_0 <= LPAREN)||(LA106_0 >= NEW && LA106_0 <= NULL)||LA106_0==PLUS||LA106_0==PLUSPLUS||LA106_0==SHORT||(LA106_0 >= STRINGLITERAL && LA106_0 <= SUB)||(LA106_0 >= SUBSUB && LA106_0 <= SUPER)||LA106_0==THIS||LA106_0==TILDE||LA106_0==TRUE||LA106_0==VOID) ) {
						alt106=1;
					}
					switch (alt106) {
						case 1 :
							// src/main/resources/parser/Java.g:1191:18: expression
							{
							pushFollow(FOLLOW_expression_in_forstatement5638);
							expression();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,SEMI,FOLLOW_SEMI_in_forstatement5659); if (state.failed) return;
					// src/main/resources/parser/Java.g:1193:17: ( expressionList )?
					int alt107=2;
					int LA107_0 = input.LA(1);
					if ( (LA107_0==BANG||LA107_0==BOOLEAN||LA107_0==BYTE||(LA107_0 >= CHAR && LA107_0 <= CHARLITERAL)||(LA107_0 >= DOUBLE && LA107_0 <= DOUBLELITERAL)||LA107_0==FALSE||(LA107_0 >= FLOAT && LA107_0 <= FLOATLITERAL)||LA107_0==IDENTIFIER||LA107_0==INT||LA107_0==INTLITERAL||(LA107_0 >= LONG && LA107_0 <= LPAREN)||(LA107_0 >= NEW && LA107_0 <= NULL)||LA107_0==PLUS||LA107_0==PLUSPLUS||LA107_0==SHORT||(LA107_0 >= STRINGLITERAL && LA107_0 <= SUB)||(LA107_0 >= SUBSUB && LA107_0 <= SUPER)||LA107_0==THIS||LA107_0==TILDE||LA107_0==TRUE||LA107_0==VOID) ) {
						alt107=1;
					}
					switch (alt107) {
						case 1 :
							// src/main/resources/parser/Java.g:1193:18: expressionList
							{
							pushFollow(FOLLOW_expressionList_in_forstatement5679);
							expressionList();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,RPAREN,FOLLOW_RPAREN_in_forstatement5700); if (state.failed) return;
					pushFollow(FOLLOW_statement_in_forstatement5702);
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
	// src/main/resources/parser/Java.g:1197:1: forInit : ( localVariableDeclaration | expressionList );
	public final void forInit() throws RecognitionException {
		int forInit_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 67) ) { return; }

			// src/main/resources/parser/Java.g:1198:5: ( localVariableDeclaration | expressionList )
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
					// src/main/resources/parser/Java.g:1198:9: localVariableDeclaration
					{
					pushFollow(FOLLOW_localVariableDeclaration_in_forInit5722);
					localVariableDeclaration();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1199:9: expressionList
					{
					pushFollow(FOLLOW_expressionList_in_forInit5732);
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
	// src/main/resources/parser/Java.g:1202:1: parExpression : '(' expression ')' ;
	public final void parExpression() throws RecognitionException {
		int parExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 68) ) { return; }

			// src/main/resources/parser/Java.g:1203:5: ( '(' expression ')' )
			// src/main/resources/parser/Java.g:1203:9: '(' expression ')'
			{
			match(input,LPAREN,FOLLOW_LPAREN_in_parExpression5752); if (state.failed) return;
			pushFollow(FOLLOW_expression_in_parExpression5754);
			expression();
			state._fsp--;
			if (state.failed) return;
			match(input,RPAREN,FOLLOW_RPAREN_in_parExpression5756); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1206:1: expressionList : expression ( ',' expression )* ;
	public final void expressionList() throws RecognitionException {
		int expressionList_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 69) ) { return; }

			// src/main/resources/parser/Java.g:1207:5: ( expression ( ',' expression )* )
			// src/main/resources/parser/Java.g:1207:9: expression ( ',' expression )*
			{
			pushFollow(FOLLOW_expression_in_expressionList5776);
			expression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1208:9: ( ',' expression )*
			loop110:
			while (true) {
				int alt110=2;
				int LA110_0 = input.LA(1);
				if ( (LA110_0==COMMA) ) {
					alt110=1;
				}

				switch (alt110) {
				case 1 :
					// src/main/resources/parser/Java.g:1208:10: ',' expression
					{
					match(input,COMMA,FOLLOW_COMMA_in_expressionList5787); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_expressionList5789);
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
	// src/main/resources/parser/Java.g:1213:1: expression : conditionalExpression ( assignmentOperator expression )? ;
	public final void expression() throws RecognitionException {
		int expression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 70) ) { return; }

			// src/main/resources/parser/Java.g:1214:5: ( conditionalExpression ( assignmentOperator expression )? )
			// src/main/resources/parser/Java.g:1214:9: conditionalExpression ( assignmentOperator expression )?
			{
			pushFollow(FOLLOW_conditionalExpression_in_expression5821);
			conditionalExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1215:9: ( assignmentOperator expression )?
			int alt111=2;
			int LA111_0 = input.LA(1);
			if ( (LA111_0==AMPEQ||LA111_0==BAREQ||LA111_0==CARETEQ||LA111_0==EQ||LA111_0==GT||LA111_0==LT||LA111_0==PERCENTEQ||LA111_0==PLUSEQ||LA111_0==SLASHEQ||LA111_0==STAREQ||LA111_0==SUBEQ) ) {
				alt111=1;
			}
			switch (alt111) {
				case 1 :
					// src/main/resources/parser/Java.g:1215:10: assignmentOperator expression
					{
					pushFollow(FOLLOW_assignmentOperator_in_expression5832);
					assignmentOperator();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_expression_in_expression5834);
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
	// src/main/resources/parser/Java.g:1220:1: assignmentOperator : ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' | '<' '<' '=' | '>' '>' '>' '=' | '>' '>' '=' );
	public final void assignmentOperator() throws RecognitionException {
		int assignmentOperator_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 71) ) { return; }

			// src/main/resources/parser/Java.g:1221:5: ( '=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '%=' | '<' '<' '=' | '>' '>' '>' '=' | '>' '>' '=' )
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
					// src/main/resources/parser/Java.g:1221:9: '='
					{
					match(input,EQ,FOLLOW_EQ_in_assignmentOperator5866); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1222:9: '+='
					{
					match(input,PLUSEQ,FOLLOW_PLUSEQ_in_assignmentOperator5876); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1223:9: '-='
					{
					match(input,SUBEQ,FOLLOW_SUBEQ_in_assignmentOperator5886); if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1224:9: '*='
					{
					match(input,STAREQ,FOLLOW_STAREQ_in_assignmentOperator5896); if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1225:9: '/='
					{
					match(input,SLASHEQ,FOLLOW_SLASHEQ_in_assignmentOperator5906); if (state.failed) return;
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:1226:9: '&='
					{
					match(input,AMPEQ,FOLLOW_AMPEQ_in_assignmentOperator5916); if (state.failed) return;
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:1227:9: '|='
					{
					match(input,BAREQ,FOLLOW_BAREQ_in_assignmentOperator5926); if (state.failed) return;
					}
					break;
				case 8 :
					// src/main/resources/parser/Java.g:1228:9: '^='
					{
					match(input,CARETEQ,FOLLOW_CARETEQ_in_assignmentOperator5936); if (state.failed) return;
					}
					break;
				case 9 :
					// src/main/resources/parser/Java.g:1229:9: '%='
					{
					match(input,PERCENTEQ,FOLLOW_PERCENTEQ_in_assignmentOperator5946); if (state.failed) return;
					}
					break;
				case 10 :
					// src/main/resources/parser/Java.g:1230:10: '<' '<' '='
					{
					match(input,LT,FOLLOW_LT_in_assignmentOperator5957); if (state.failed) return;
					match(input,LT,FOLLOW_LT_in_assignmentOperator5959); if (state.failed) return;
					match(input,EQ,FOLLOW_EQ_in_assignmentOperator5961); if (state.failed) return;
					}
					break;
				case 11 :
					// src/main/resources/parser/Java.g:1231:10: '>' '>' '>' '='
					{
					match(input,GT,FOLLOW_GT_in_assignmentOperator5972); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_assignmentOperator5974); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_assignmentOperator5976); if (state.failed) return;
					match(input,EQ,FOLLOW_EQ_in_assignmentOperator5978); if (state.failed) return;
					}
					break;
				case 12 :
					// src/main/resources/parser/Java.g:1232:10: '>' '>' '='
					{
					match(input,GT,FOLLOW_GT_in_assignmentOperator5989); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_assignmentOperator5991); if (state.failed) return;
					match(input,EQ,FOLLOW_EQ_in_assignmentOperator5993); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1236:1: conditionalExpression : conditionalOrExpression ( '?' expression ':' conditionalExpression )? ;
	public final void conditionalExpression() throws RecognitionException {
		int conditionalExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 72) ) { return; }

			// src/main/resources/parser/Java.g:1237:5: ( conditionalOrExpression ( '?' expression ':' conditionalExpression )? )
			// src/main/resources/parser/Java.g:1237:9: conditionalOrExpression ( '?' expression ':' conditionalExpression )?
			{
			pushFollow(FOLLOW_conditionalOrExpression_in_conditionalExpression6014);
			conditionalOrExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1238:9: ( '?' expression ':' conditionalExpression )?
			int alt113=2;
			int LA113_0 = input.LA(1);
			if ( (LA113_0==QUES) ) {
				alt113=1;
			}
			switch (alt113) {
				case 1 :
					// src/main/resources/parser/Java.g:1238:10: '?' expression ':' conditionalExpression
					{
					match(input,QUES,FOLLOW_QUES_in_conditionalExpression6025); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_conditionalExpression6027);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,COLON,FOLLOW_COLON_in_conditionalExpression6029); if (state.failed) return;
					pushFollow(FOLLOW_conditionalExpression_in_conditionalExpression6031);
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
	// src/main/resources/parser/Java.g:1242:1: conditionalOrExpression : conditionalAndExpression ( '||' conditionalAndExpression )* ;
	public final void conditionalOrExpression() throws RecognitionException {
		int conditionalOrExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 73) ) { return; }

			// src/main/resources/parser/Java.g:1243:5: ( conditionalAndExpression ( '||' conditionalAndExpression )* )
			// src/main/resources/parser/Java.g:1243:9: conditionalAndExpression ( '||' conditionalAndExpression )*
			{
			pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression6062);
			conditionalAndExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1244:9: ( '||' conditionalAndExpression )*
			loop114:
			while (true) {
				int alt114=2;
				int LA114_0 = input.LA(1);
				if ( (LA114_0==BARBAR) ) {
					alt114=1;
				}

				switch (alt114) {
				case 1 :
					// src/main/resources/parser/Java.g:1244:10: '||' conditionalAndExpression
					{
					match(input,BARBAR,FOLLOW_BARBAR_in_conditionalOrExpression6073); if (state.failed) return;
					pushFollow(FOLLOW_conditionalAndExpression_in_conditionalOrExpression6075);
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
	// src/main/resources/parser/Java.g:1248:1: conditionalAndExpression : inclusiveOrExpression ( '&&' inclusiveOrExpression )* ;
	public final void conditionalAndExpression() throws RecognitionException {
		int conditionalAndExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 74) ) { return; }

			// src/main/resources/parser/Java.g:1249:5: ( inclusiveOrExpression ( '&&' inclusiveOrExpression )* )
			// src/main/resources/parser/Java.g:1249:9: inclusiveOrExpression ( '&&' inclusiveOrExpression )*
			{
			pushFollow(FOLLOW_inclusiveOrExpression_in_conditionalAndExpression6106);
			inclusiveOrExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1250:9: ( '&&' inclusiveOrExpression )*
			loop115:
			while (true) {
				int alt115=2;
				int LA115_0 = input.LA(1);
				if ( (LA115_0==AMPAMP) ) {
					alt115=1;
				}

				switch (alt115) {
				case 1 :
					// src/main/resources/parser/Java.g:1250:10: '&&' inclusiveOrExpression
					{
					match(input,AMPAMP,FOLLOW_AMPAMP_in_conditionalAndExpression6117); if (state.failed) return;
					pushFollow(FOLLOW_inclusiveOrExpression_in_conditionalAndExpression6119);
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
	// src/main/resources/parser/Java.g:1254:1: inclusiveOrExpression : exclusiveOrExpression ( '|' exclusiveOrExpression )* ;
	public final void inclusiveOrExpression() throws RecognitionException {
		int inclusiveOrExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 75) ) { return; }

			// src/main/resources/parser/Java.g:1255:5: ( exclusiveOrExpression ( '|' exclusiveOrExpression )* )
			// src/main/resources/parser/Java.g:1255:9: exclusiveOrExpression ( '|' exclusiveOrExpression )*
			{
			pushFollow(FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression6150);
			exclusiveOrExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1256:9: ( '|' exclusiveOrExpression )*
			loop116:
			while (true) {
				int alt116=2;
				int LA116_0 = input.LA(1);
				if ( (LA116_0==BAR) ) {
					alt116=1;
				}

				switch (alt116) {
				case 1 :
					// src/main/resources/parser/Java.g:1256:10: '|' exclusiveOrExpression
					{
					match(input,BAR,FOLLOW_BAR_in_inclusiveOrExpression6161); if (state.failed) return;
					pushFollow(FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression6163);
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
	// src/main/resources/parser/Java.g:1260:1: exclusiveOrExpression : andExpression ( '^' andExpression )* ;
	public final void exclusiveOrExpression() throws RecognitionException {
		int exclusiveOrExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 76) ) { return; }

			// src/main/resources/parser/Java.g:1261:5: ( andExpression ( '^' andExpression )* )
			// src/main/resources/parser/Java.g:1261:9: andExpression ( '^' andExpression )*
			{
			pushFollow(FOLLOW_andExpression_in_exclusiveOrExpression6194);
			andExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1262:9: ( '^' andExpression )*
			loop117:
			while (true) {
				int alt117=2;
				int LA117_0 = input.LA(1);
				if ( (LA117_0==CARET) ) {
					alt117=1;
				}

				switch (alt117) {
				case 1 :
					// src/main/resources/parser/Java.g:1262:10: '^' andExpression
					{
					match(input,CARET,FOLLOW_CARET_in_exclusiveOrExpression6205); if (state.failed) return;
					pushFollow(FOLLOW_andExpression_in_exclusiveOrExpression6207);
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
	// src/main/resources/parser/Java.g:1266:1: andExpression : equalityExpression ( '&' equalityExpression )* ;
	public final void andExpression() throws RecognitionException {
		int andExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 77) ) { return; }

			// src/main/resources/parser/Java.g:1267:5: ( equalityExpression ( '&' equalityExpression )* )
			// src/main/resources/parser/Java.g:1267:9: equalityExpression ( '&' equalityExpression )*
			{
			pushFollow(FOLLOW_equalityExpression_in_andExpression6238);
			equalityExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1268:9: ( '&' equalityExpression )*
			loop118:
			while (true) {
				int alt118=2;
				int LA118_0 = input.LA(1);
				if ( (LA118_0==AMP) ) {
					alt118=1;
				}

				switch (alt118) {
				case 1 :
					// src/main/resources/parser/Java.g:1268:10: '&' equalityExpression
					{
					match(input,AMP,FOLLOW_AMP_in_andExpression6249); if (state.failed) return;
					pushFollow(FOLLOW_equalityExpression_in_andExpression6251);
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
	// src/main/resources/parser/Java.g:1272:1: equalityExpression : instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )* ;
	public final void equalityExpression() throws RecognitionException {
		int equalityExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 78) ) { return; }

			// src/main/resources/parser/Java.g:1273:5: ( instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )* )
			// src/main/resources/parser/Java.g:1273:9: instanceOfExpression ( ( '==' | '!=' ) instanceOfExpression )*
			{
			pushFollow(FOLLOW_instanceOfExpression_in_equalityExpression6282);
			instanceOfExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1274:9: ( ( '==' | '!=' ) instanceOfExpression )*
			loop119:
			while (true) {
				int alt119=2;
				int LA119_0 = input.LA(1);
				if ( (LA119_0==BANGEQ||LA119_0==EQEQ) ) {
					alt119=1;
				}

				switch (alt119) {
				case 1 :
					// src/main/resources/parser/Java.g:1275:13: ( '==' | '!=' ) instanceOfExpression
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
					pushFollow(FOLLOW_instanceOfExpression_in_equalityExpression6359);
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
	// src/main/resources/parser/Java.g:1282:1: instanceOfExpression : relationalExpression ( 'instanceof' type )? ;
	public final void instanceOfExpression() throws RecognitionException {
		int instanceOfExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 79) ) { return; }

			// src/main/resources/parser/Java.g:1283:5: ( relationalExpression ( 'instanceof' type )? )
			// src/main/resources/parser/Java.g:1283:9: relationalExpression ( 'instanceof' type )?
			{
			pushFollow(FOLLOW_relationalExpression_in_instanceOfExpression6390);
			relationalExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1284:9: ( 'instanceof' type )?
			int alt120=2;
			int LA120_0 = input.LA(1);
			if ( (LA120_0==INSTANCEOF) ) {
				alt120=1;
			}
			switch (alt120) {
				case 1 :
					// src/main/resources/parser/Java.g:1284:10: 'instanceof' type
					{
					match(input,INSTANCEOF,FOLLOW_INSTANCEOF_in_instanceOfExpression6401); if (state.failed) return;
					pushFollow(FOLLOW_type_in_instanceOfExpression6403);
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
	// src/main/resources/parser/Java.g:1288:1: relationalExpression : shiftExpression ( relationalOp shiftExpression )* ;
	public final void relationalExpression() throws RecognitionException {
		int relationalExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 80) ) { return; }

			// src/main/resources/parser/Java.g:1289:5: ( shiftExpression ( relationalOp shiftExpression )* )
			// src/main/resources/parser/Java.g:1289:9: shiftExpression ( relationalOp shiftExpression )*
			{
			pushFollow(FOLLOW_shiftExpression_in_relationalExpression6434);
			shiftExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1290:9: ( relationalOp shiftExpression )*
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
					// src/main/resources/parser/Java.g:1290:10: relationalOp shiftExpression
					{
					pushFollow(FOLLOW_relationalOp_in_relationalExpression6445);
					relationalOp();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_shiftExpression_in_relationalExpression6447);
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
	// src/main/resources/parser/Java.g:1294:1: relationalOp : ( '<' '=' | '>' '=' | '<' | '>' );
	public final void relationalOp() throws RecognitionException {
		int relationalOp_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 81) ) { return; }

			// src/main/resources/parser/Java.g:1295:5: ( '<' '=' | '>' '=' | '<' | '>' )
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
					// src/main/resources/parser/Java.g:1295:10: '<' '='
					{
					match(input,LT,FOLLOW_LT_in_relationalOp6479); if (state.failed) return;
					match(input,EQ,FOLLOW_EQ_in_relationalOp6481); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1296:10: '>' '='
					{
					match(input,GT,FOLLOW_GT_in_relationalOp6492); if (state.failed) return;
					match(input,EQ,FOLLOW_EQ_in_relationalOp6494); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1297:9: '<'
					{
					match(input,LT,FOLLOW_LT_in_relationalOp6504); if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1298:9: '>'
					{
					match(input,GT,FOLLOW_GT_in_relationalOp6514); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1301:1: shiftExpression : additiveExpression ( shiftOp additiveExpression )* ;
	public final void shiftExpression() throws RecognitionException {
		int shiftExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 82) ) { return; }

			// src/main/resources/parser/Java.g:1302:5: ( additiveExpression ( shiftOp additiveExpression )* )
			// src/main/resources/parser/Java.g:1302:9: additiveExpression ( shiftOp additiveExpression )*
			{
			pushFollow(FOLLOW_additiveExpression_in_shiftExpression6534);
			additiveExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1303:9: ( shiftOp additiveExpression )*
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
					// src/main/resources/parser/Java.g:1303:10: shiftOp additiveExpression
					{
					pushFollow(FOLLOW_shiftOp_in_shiftExpression6545);
					shiftOp();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_additiveExpression_in_shiftExpression6547);
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
	// src/main/resources/parser/Java.g:1308:1: shiftOp : ( '<' '<' | '>' '>' '>' | '>' '>' );
	public final void shiftOp() throws RecognitionException {
		int shiftOp_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 83) ) { return; }

			// src/main/resources/parser/Java.g:1309:5: ( '<' '<' | '>' '>' '>' | '>' '>' )
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
					// src/main/resources/parser/Java.g:1309:10: '<' '<'
					{
					match(input,LT,FOLLOW_LT_in_shiftOp6580); if (state.failed) return;
					match(input,LT,FOLLOW_LT_in_shiftOp6582); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1310:10: '>' '>' '>'
					{
					match(input,GT,FOLLOW_GT_in_shiftOp6593); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_shiftOp6595); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_shiftOp6597); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1311:10: '>' '>'
					{
					match(input,GT,FOLLOW_GT_in_shiftOp6608); if (state.failed) return;
					match(input,GT,FOLLOW_GT_in_shiftOp6610); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1315:1: additiveExpression : multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )* ;
	public final void additiveExpression() throws RecognitionException {
		int additiveExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 84) ) { return; }

			// src/main/resources/parser/Java.g:1316:5: ( multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )* )
			// src/main/resources/parser/Java.g:1316:9: multiplicativeExpression ( ( '+' | '-' ) multiplicativeExpression )*
			{
			pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression6631);
			multiplicativeExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1317:9: ( ( '+' | '-' ) multiplicativeExpression )*
			loop125:
			while (true) {
				int alt125=2;
				int LA125_0 = input.LA(1);
				if ( (LA125_0==PLUS||LA125_0==SUB) ) {
					alt125=1;
				}

				switch (alt125) {
				case 1 :
					// src/main/resources/parser/Java.g:1318:13: ( '+' | '-' ) multiplicativeExpression
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
					pushFollow(FOLLOW_multiplicativeExpression_in_additiveExpression6708);
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
	// src/main/resources/parser/Java.g:1325:1: multiplicativeExpression : unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )* ;
	public final void multiplicativeExpression() throws RecognitionException {
		int multiplicativeExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 85) ) { return; }

			// src/main/resources/parser/Java.g:1326:5: ( unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )* )
			// src/main/resources/parser/Java.g:1327:9: unaryExpression ( ( '*' | '/' | '%' ) unaryExpression )*
			{
			pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression6746);
			unaryExpression();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1328:9: ( ( '*' | '/' | '%' ) unaryExpression )*
			loop126:
			while (true) {
				int alt126=2;
				int LA126_0 = input.LA(1);
				if ( (LA126_0==PERCENT||LA126_0==SLASH||LA126_0==STAR) ) {
					alt126=1;
				}

				switch (alt126) {
				case 1 :
					// src/main/resources/parser/Java.g:1329:13: ( '*' | '/' | '%' ) unaryExpression
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
					pushFollow(FOLLOW_unaryExpression_in_multiplicativeExpression6841);
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
	// src/main/resources/parser/Java.g:1341:1: unaryExpression : ( '+' unaryExpression | '-' unaryExpression | '++' unaryExpression | '--' unaryExpression | unaryExpressionNotPlusMinus );
	public final void unaryExpression() throws RecognitionException {
		int unaryExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 86) ) { return; }

			// src/main/resources/parser/Java.g:1342:5: ( '+' unaryExpression | '-' unaryExpression | '++' unaryExpression | '--' unaryExpression | unaryExpressionNotPlusMinus )
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
					// src/main/resources/parser/Java.g:1342:9: '+' unaryExpression
					{
					match(input,PLUS,FOLLOW_PLUS_in_unaryExpression6874); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpression6877);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1343:9: '-' unaryExpression
					{
					match(input,SUB,FOLLOW_SUB_in_unaryExpression6887); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpression6889);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1344:9: '++' unaryExpression
					{
					match(input,PLUSPLUS,FOLLOW_PLUSPLUS_in_unaryExpression6899); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpression6901);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1345:9: '--' unaryExpression
					{
					match(input,SUBSUB,FOLLOW_SUBSUB_in_unaryExpression6911); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpression6913);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1346:9: unaryExpressionNotPlusMinus
					{
					pushFollow(FOLLOW_unaryExpressionNotPlusMinus_in_unaryExpression6923);
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
	// src/main/resources/parser/Java.g:1349:1: unaryExpressionNotPlusMinus : ( '~' unaryExpression | '!' unaryExpression | castExpression | primary ( selector )* ( '++' | '--' )? );
	public final void unaryExpressionNotPlusMinus() throws RecognitionException {
		int unaryExpressionNotPlusMinus_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 87) ) { return; }

			// src/main/resources/parser/Java.g:1350:5: ( '~' unaryExpression | '!' unaryExpression | castExpression | primary ( selector )* ( '++' | '--' )? )
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
					// src/main/resources/parser/Java.g:1350:9: '~' unaryExpression
					{
					match(input,TILDE,FOLLOW_TILDE_in_unaryExpressionNotPlusMinus6943); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus6945);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1351:9: '!' unaryExpression
					{
					match(input,BANG,FOLLOW_BANG_in_unaryExpressionNotPlusMinus6955); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus6957);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1352:9: castExpression
					{
					pushFollow(FOLLOW_castExpression_in_unaryExpressionNotPlusMinus6967);
					castExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1353:9: primary ( selector )* ( '++' | '--' )?
					{
					pushFollow(FOLLOW_primary_in_unaryExpressionNotPlusMinus6977);
					primary();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:1354:9: ( selector )*
					loop128:
					while (true) {
						int alt128=2;
						int LA128_0 = input.LA(1);
						if ( (LA128_0==DOT||LA128_0==LBRACKET) ) {
							alt128=1;
						}

						switch (alt128) {
						case 1 :
							// src/main/resources/parser/Java.g:1354:10: selector
							{
							pushFollow(FOLLOW_selector_in_unaryExpressionNotPlusMinus6988);
							selector();
							state._fsp--;
							if (state.failed) return;
							}
							break;

						default :
							break loop128;
						}
					}

					// src/main/resources/parser/Java.g:1356:9: ( '++' | '--' )?
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
	// src/main/resources/parser/Java.g:1361:1: castExpression : ( '(' primitiveType ')' unaryExpression | '(' type ')' unaryExpressionNotPlusMinus );
	public final void castExpression() throws RecognitionException {
		int castExpression_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 88) ) { return; }

			// src/main/resources/parser/Java.g:1362:5: ( '(' primitiveType ')' unaryExpression | '(' type ')' unaryExpressionNotPlusMinus )
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
					// src/main/resources/parser/Java.g:1362:9: '(' primitiveType ')' unaryExpression
					{
					match(input,LPAREN,FOLLOW_LPAREN_in_castExpression7058); if (state.failed) return;
					pushFollow(FOLLOW_primitiveType_in_castExpression7060);
					primitiveType();
					state._fsp--;
					if (state.failed) return;
					match(input,RPAREN,FOLLOW_RPAREN_in_castExpression7062); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpression_in_castExpression7064);
					unaryExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1363:9: '(' type ')' unaryExpressionNotPlusMinus
					{
					match(input,LPAREN,FOLLOW_LPAREN_in_castExpression7074); if (state.failed) return;
					pushFollow(FOLLOW_type_in_castExpression7076);
					type();
					state._fsp--;
					if (state.failed) return;
					match(input,RPAREN,FOLLOW_RPAREN_in_castExpression7078); if (state.failed) return;
					pushFollow(FOLLOW_unaryExpressionNotPlusMinus_in_castExpression7080);
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
	// src/main/resources/parser/Java.g:1369:1: primary : ( parExpression | 'this' ( '.' IDENTIFIER )* ( identifierSuffix )? | IDENTIFIER ( '.' IDENTIFIER )* ( identifierSuffix )? | 'super' superSuffix | literal | creator | primitiveType ( '[' ']' )* '.' 'class' | 'void' '.' 'class' );
	public final void primary() throws RecognitionException {
		int primary_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 89) ) { return; }

			// src/main/resources/parser/Java.g:1370:5: ( parExpression | 'this' ( '.' IDENTIFIER )* ( identifierSuffix )? | IDENTIFIER ( '.' IDENTIFIER )* ( identifierSuffix )? | 'super' superSuffix | literal | creator | primitiveType ( '[' ']' )* '.' 'class' | 'void' '.' 'class' )
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
					// src/main/resources/parser/Java.g:1370:9: parExpression
					{
					pushFollow(FOLLOW_parExpression_in_primary7102);
					parExpression();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1371:9: 'this' ( '.' IDENTIFIER )* ( identifierSuffix )?
					{
					match(input,THIS,FOLLOW_THIS_in_primary7124); if (state.failed) return;
					// src/main/resources/parser/Java.g:1372:9: ( '.' IDENTIFIER )*
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
							// src/main/resources/parser/Java.g:1372:10: '.' IDENTIFIER
							{
							match(input,DOT,FOLLOW_DOT_in_primary7135); if (state.failed) return;
							match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_primary7137); if (state.failed) return;
							}
							break;

						default :
							break loop132;
						}
					}

					// src/main/resources/parser/Java.g:1374:9: ( identifierSuffix )?
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
							// src/main/resources/parser/Java.g:1374:10: identifierSuffix
							{
							pushFollow(FOLLOW_identifierSuffix_in_primary7159);
							identifierSuffix();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1376:9: IDENTIFIER ( '.' IDENTIFIER )* ( identifierSuffix )?
					{
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_primary7180); if (state.failed) return;
					// src/main/resources/parser/Java.g:1377:9: ( '.' IDENTIFIER )*
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
							// src/main/resources/parser/Java.g:1377:10: '.' IDENTIFIER
							{
							match(input,DOT,FOLLOW_DOT_in_primary7191); if (state.failed) return;
							match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_primary7193); if (state.failed) return;
							}
							break;

						default :
							break loop134;
						}
					}

					// src/main/resources/parser/Java.g:1379:9: ( identifierSuffix )?
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
							// src/main/resources/parser/Java.g:1379:10: identifierSuffix
							{
							pushFollow(FOLLOW_identifierSuffix_in_primary7215);
							identifierSuffix();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1381:9: 'super' superSuffix
					{
					match(input,SUPER,FOLLOW_SUPER_in_primary7236); if (state.failed) return;
					pushFollow(FOLLOW_superSuffix_in_primary7246);
					superSuffix();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1383:9: literal
					{
					pushFollow(FOLLOW_literal_in_primary7256);
					literal();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:1384:9: creator
					{
					pushFollow(FOLLOW_creator_in_primary7266);
					creator();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:1385:9: primitiveType ( '[' ']' )* '.' 'class'
					{
					pushFollow(FOLLOW_primitiveType_in_primary7276);
					primitiveType();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:1386:9: ( '[' ']' )*
					loop136:
					while (true) {
						int alt136=2;
						int LA136_0 = input.LA(1);
						if ( (LA136_0==LBRACKET) ) {
							alt136=1;
						}

						switch (alt136) {
						case 1 :
							// src/main/resources/parser/Java.g:1386:10: '[' ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_primary7287); if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_primary7289); if (state.failed) return;
							}
							break;

						default :
							break loop136;
						}
					}

					match(input,DOT,FOLLOW_DOT_in_primary7310); if (state.failed) return;
					match(input,CLASS,FOLLOW_CLASS_in_primary7312); if (state.failed) return;
					}
					break;
				case 8 :
					// src/main/resources/parser/Java.g:1389:9: 'void' '.' 'class'
					{
					match(input,VOID,FOLLOW_VOID_in_primary7322); if (state.failed) return;
					match(input,DOT,FOLLOW_DOT_in_primary7324); if (state.failed) return;
					match(input,CLASS,FOLLOW_CLASS_in_primary7326); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1393:1: superSuffix : ( arguments | '.' ( typeArguments )? IDENTIFIER ( arguments )? );
	public final void superSuffix() throws RecognitionException {
		int superSuffix_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 90) ) { return; }

			// src/main/resources/parser/Java.g:1394:5: ( arguments | '.' ( typeArguments )? IDENTIFIER ( arguments )? )
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
					// src/main/resources/parser/Java.g:1394:9: arguments
					{
					pushFollow(FOLLOW_arguments_in_superSuffix7352);
					arguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1395:9: '.' ( typeArguments )? IDENTIFIER ( arguments )?
					{
					match(input,DOT,FOLLOW_DOT_in_superSuffix7362); if (state.failed) return;
					// src/main/resources/parser/Java.g:1395:13: ( typeArguments )?
					int alt138=2;
					int LA138_0 = input.LA(1);
					if ( (LA138_0==LT) ) {
						alt138=1;
					}
					switch (alt138) {
						case 1 :
							// src/main/resources/parser/Java.g:1395:14: typeArguments
							{
							pushFollow(FOLLOW_typeArguments_in_superSuffix7365);
							typeArguments();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_superSuffix7386); if (state.failed) return;
					// src/main/resources/parser/Java.g:1398:9: ( arguments )?
					int alt139=2;
					int LA139_0 = input.LA(1);
					if ( (LA139_0==LPAREN) ) {
						alt139=1;
					}
					switch (alt139) {
						case 1 :
							// src/main/resources/parser/Java.g:1398:10: arguments
							{
							pushFollow(FOLLOW_arguments_in_superSuffix7397);
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
	// src/main/resources/parser/Java.g:1403:1: identifierSuffix : ( ( '[' ']' )+ '.' 'class' | ( '[' expression ']' )+ | arguments | '.' 'class' | '.' nonWildcardTypeArguments IDENTIFIER arguments | '.' 'this' | '.' 'super' arguments | innerCreator );
	public final void identifierSuffix() throws RecognitionException {
		int identifierSuffix_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 91) ) { return; }

			// src/main/resources/parser/Java.g:1404:5: ( ( '[' ']' )+ '.' 'class' | ( '[' expression ']' )+ | arguments | '.' 'class' | '.' nonWildcardTypeArguments IDENTIFIER arguments | '.' 'this' | '.' 'super' arguments | innerCreator )
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
					// src/main/resources/parser/Java.g:1404:9: ( '[' ']' )+ '.' 'class'
					{
					// src/main/resources/parser/Java.g:1404:9: ( '[' ']' )+
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
							// src/main/resources/parser/Java.g:1404:10: '[' ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_identifierSuffix7430); if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_identifierSuffix7432); if (state.failed) return;
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

					match(input,DOT,FOLLOW_DOT_in_identifierSuffix7453); if (state.failed) return;
					match(input,CLASS,FOLLOW_CLASS_in_identifierSuffix7455); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1407:9: ( '[' expression ']' )+
					{
					// src/main/resources/parser/Java.g:1407:9: ( '[' expression ']' )+
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
							// src/main/resources/parser/Java.g:1407:10: '[' expression ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_identifierSuffix7466); if (state.failed) return;
							pushFollow(FOLLOW_expression_in_identifierSuffix7468);
							expression();
							state._fsp--;
							if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_identifierSuffix7470); if (state.failed) return;
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
					// src/main/resources/parser/Java.g:1409:9: arguments
					{
					pushFollow(FOLLOW_arguments_in_identifierSuffix7491);
					arguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1410:9: '.' 'class'
					{
					match(input,DOT,FOLLOW_DOT_in_identifierSuffix7501); if (state.failed) return;
					match(input,CLASS,FOLLOW_CLASS_in_identifierSuffix7503); if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1411:9: '.' nonWildcardTypeArguments IDENTIFIER arguments
					{
					match(input,DOT,FOLLOW_DOT_in_identifierSuffix7513); if (state.failed) return;
					pushFollow(FOLLOW_nonWildcardTypeArguments_in_identifierSuffix7515);
					nonWildcardTypeArguments();
					state._fsp--;
					if (state.failed) return;
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_identifierSuffix7517); if (state.failed) return;
					pushFollow(FOLLOW_arguments_in_identifierSuffix7519);
					arguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 6 :
					// src/main/resources/parser/Java.g:1412:9: '.' 'this'
					{
					match(input,DOT,FOLLOW_DOT_in_identifierSuffix7529); if (state.failed) return;
					match(input,THIS,FOLLOW_THIS_in_identifierSuffix7531); if (state.failed) return;
					}
					break;
				case 7 :
					// src/main/resources/parser/Java.g:1413:9: '.' 'super' arguments
					{
					match(input,DOT,FOLLOW_DOT_in_identifierSuffix7541); if (state.failed) return;
					match(input,SUPER,FOLLOW_SUPER_in_identifierSuffix7543); if (state.failed) return;
					pushFollow(FOLLOW_arguments_in_identifierSuffix7545);
					arguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 8 :
					// src/main/resources/parser/Java.g:1414:9: innerCreator
					{
					pushFollow(FOLLOW_innerCreator_in_identifierSuffix7555);
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
	// src/main/resources/parser/Java.g:1418:1: selector : ( '.' IDENTIFIER ( arguments )? | '.' 'this' | '.' 'super' superSuffix | innerCreator | '[' expression ']' );
	public final void selector() throws RecognitionException {
		int selector_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 92) ) { return; }

			// src/main/resources/parser/Java.g:1419:5: ( '.' IDENTIFIER ( arguments )? | '.' 'this' | '.' 'super' superSuffix | innerCreator | '[' expression ']' )
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
					// src/main/resources/parser/Java.g:1419:9: '.' IDENTIFIER ( arguments )?
					{
					match(input,DOT,FOLLOW_DOT_in_selector7577); if (state.failed) return;
					match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_selector7579); if (state.failed) return;
					// src/main/resources/parser/Java.g:1420:9: ( arguments )?
					int alt144=2;
					int LA144_0 = input.LA(1);
					if ( (LA144_0==LPAREN) ) {
						alt144=1;
					}
					switch (alt144) {
						case 1 :
							// src/main/resources/parser/Java.g:1420:10: arguments
							{
							pushFollow(FOLLOW_arguments_in_selector7590);
							arguments();
							state._fsp--;
							if (state.failed) return;
							}
							break;

					}

					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1422:9: '.' 'this'
					{
					match(input,DOT,FOLLOW_DOT_in_selector7611); if (state.failed) return;
					match(input,THIS,FOLLOW_THIS_in_selector7613); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1423:9: '.' 'super' superSuffix
					{
					match(input,DOT,FOLLOW_DOT_in_selector7623); if (state.failed) return;
					match(input,SUPER,FOLLOW_SUPER_in_selector7625); if (state.failed) return;
					pushFollow(FOLLOW_superSuffix_in_selector7635);
					superSuffix();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 4 :
					// src/main/resources/parser/Java.g:1425:9: innerCreator
					{
					pushFollow(FOLLOW_innerCreator_in_selector7645);
					innerCreator();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 5 :
					// src/main/resources/parser/Java.g:1426:9: '[' expression ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_selector7655); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_selector7657);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_selector7659); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1429:1: creator : ( 'new' nonWildcardTypeArguments classOrInterfaceType classCreatorRest | 'new' classOrInterfaceType classCreatorRest | arrayCreator );
	public final void creator() throws RecognitionException {
		int creator_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 93) ) { return; }

			// src/main/resources/parser/Java.g:1430:5: ( 'new' nonWildcardTypeArguments classOrInterfaceType classCreatorRest | 'new' classOrInterfaceType classCreatorRest | arrayCreator )
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
					// src/main/resources/parser/Java.g:1430:9: 'new' nonWildcardTypeArguments classOrInterfaceType classCreatorRest
					{
					match(input,NEW,FOLLOW_NEW_in_creator7679); if (state.failed) return;
					pushFollow(FOLLOW_nonWildcardTypeArguments_in_creator7681);
					nonWildcardTypeArguments();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_classOrInterfaceType_in_creator7683);
					classOrInterfaceType();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_classCreatorRest_in_creator7685);
					classCreatorRest();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1431:9: 'new' classOrInterfaceType classCreatorRest
					{
					match(input,NEW,FOLLOW_NEW_in_creator7695); if (state.failed) return;
					pushFollow(FOLLOW_classOrInterfaceType_in_creator7697);
					classOrInterfaceType();
					state._fsp--;
					if (state.failed) return;
					pushFollow(FOLLOW_classCreatorRest_in_creator7699);
					classCreatorRest();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1432:9: arrayCreator
					{
					pushFollow(FOLLOW_arrayCreator_in_creator7709);
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
	// src/main/resources/parser/Java.g:1435:1: arrayCreator : ( 'new' createdName '[' ']' ( '[' ']' )* arrayInitializer | 'new' createdName '[' expression ']' ( '[' expression ']' )* ( '[' ']' )* );
	public final void arrayCreator() throws RecognitionException {
		int arrayCreator_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 94) ) { return; }

			// src/main/resources/parser/Java.g:1436:5: ( 'new' createdName '[' ']' ( '[' ']' )* arrayInitializer | 'new' createdName '[' expression ']' ( '[' expression ']' )* ( '[' ']' )* )
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
					// src/main/resources/parser/Java.g:1436:9: 'new' createdName '[' ']' ( '[' ']' )* arrayInitializer
					{
					match(input,NEW,FOLLOW_NEW_in_arrayCreator7729); if (state.failed) return;
					pushFollow(FOLLOW_createdName_in_arrayCreator7731);
					createdName();
					state._fsp--;
					if (state.failed) return;
					match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayCreator7741); if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayCreator7743); if (state.failed) return;
					// src/main/resources/parser/Java.g:1438:9: ( '[' ']' )*
					loop147:
					while (true) {
						int alt147=2;
						int LA147_0 = input.LA(1);
						if ( (LA147_0==LBRACKET) ) {
							alt147=1;
						}

						switch (alt147) {
						case 1 :
							// src/main/resources/parser/Java.g:1438:10: '[' ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayCreator7754); if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayCreator7756); if (state.failed) return;
							}
							break;

						default :
							break loop147;
						}
					}

					pushFollow(FOLLOW_arrayInitializer_in_arrayCreator7777);
					arrayInitializer();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1442:9: 'new' createdName '[' expression ']' ( '[' expression ']' )* ( '[' ']' )*
					{
					match(input,NEW,FOLLOW_NEW_in_arrayCreator7788); if (state.failed) return;
					pushFollow(FOLLOW_createdName_in_arrayCreator7790);
					createdName();
					state._fsp--;
					if (state.failed) return;
					match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayCreator7800); if (state.failed) return;
					pushFollow(FOLLOW_expression_in_arrayCreator7802);
					expression();
					state._fsp--;
					if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayCreator7812); if (state.failed) return;
					// src/main/resources/parser/Java.g:1445:9: ( '[' expression ']' )*
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
							// src/main/resources/parser/Java.g:1445:13: '[' expression ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayCreator7826); if (state.failed) return;
							pushFollow(FOLLOW_expression_in_arrayCreator7828);
							expression();
							state._fsp--;
							if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayCreator7842); if (state.failed) return;
							}
							break;

						default :
							break loop148;
						}
					}

					// src/main/resources/parser/Java.g:1448:9: ( '[' ']' )*
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
							// src/main/resources/parser/Java.g:1448:10: '[' ']'
							{
							match(input,LBRACKET,FOLLOW_LBRACKET_in_arrayCreator7864); if (state.failed) return;
							match(input,RBRACKET,FOLLOW_RBRACKET_in_arrayCreator7866); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1452:1: variableInitializer : ( arrayInitializer | expression );
	public final JavaParser.variableInitializer_return variableInitializer() throws RecognitionException {
		JavaParser.variableInitializer_return retval = new JavaParser.variableInitializer_return();
		retval.start = input.LT(1);
		int variableInitializer_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 95) ) { return retval; }

			// src/main/resources/parser/Java.g:1453:5: ( arrayInitializer | expression )
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
					// src/main/resources/parser/Java.g:1453:9: arrayInitializer
					{
					pushFollow(FOLLOW_arrayInitializer_in_variableInitializer7897);
					arrayInitializer();
					state._fsp--;
					if (state.failed) return retval;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1454:9: expression
					{
					pushFollow(FOLLOW_expression_in_variableInitializer7907);
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
	// src/main/resources/parser/Java.g:1457:1: arrayInitializer : '{' ( variableInitializer ( ',' variableInitializer )* )? ( ',' )? '}' ;
	public final void arrayInitializer() throws RecognitionException {
		int arrayInitializer_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 96) ) { return; }

			// src/main/resources/parser/Java.g:1458:5: ( '{' ( variableInitializer ( ',' variableInitializer )* )? ( ',' )? '}' )
			// src/main/resources/parser/Java.g:1458:9: '{' ( variableInitializer ( ',' variableInitializer )* )? ( ',' )? '}'
			{
			match(input,LBRACE,FOLLOW_LBRACE_in_arrayInitializer7927); if (state.failed) return;
			// src/main/resources/parser/Java.g:1459:13: ( variableInitializer ( ',' variableInitializer )* )?
			int alt153=2;
			int LA153_0 = input.LA(1);
			if ( (LA153_0==BANG||LA153_0==BOOLEAN||LA153_0==BYTE||(LA153_0 >= CHAR && LA153_0 <= CHARLITERAL)||(LA153_0 >= DOUBLE && LA153_0 <= DOUBLELITERAL)||LA153_0==FALSE||(LA153_0 >= FLOAT && LA153_0 <= FLOATLITERAL)||LA153_0==IDENTIFIER||LA153_0==INT||LA153_0==INTLITERAL||LA153_0==LBRACE||(LA153_0 >= LONG && LA153_0 <= LPAREN)||(LA153_0 >= NEW && LA153_0 <= NULL)||LA153_0==PLUS||LA153_0==PLUSPLUS||LA153_0==SHORT||(LA153_0 >= STRINGLITERAL && LA153_0 <= SUB)||(LA153_0 >= SUBSUB && LA153_0 <= SUPER)||LA153_0==THIS||LA153_0==TILDE||LA153_0==TRUE||LA153_0==VOID) ) {
				alt153=1;
			}
			switch (alt153) {
				case 1 :
					// src/main/resources/parser/Java.g:1459:14: variableInitializer ( ',' variableInitializer )*
					{
					pushFollow(FOLLOW_variableInitializer_in_arrayInitializer7943);
					variableInitializer();
					state._fsp--;
					if (state.failed) return;
					// src/main/resources/parser/Java.g:1460:17: ( ',' variableInitializer )*
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
							// src/main/resources/parser/Java.g:1460:18: ',' variableInitializer
							{
							match(input,COMMA,FOLLOW_COMMA_in_arrayInitializer7962); if (state.failed) return;
							pushFollow(FOLLOW_variableInitializer_in_arrayInitializer7964);
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

			// src/main/resources/parser/Java.g:1463:13: ( ',' )?
			int alt154=2;
			int LA154_0 = input.LA(1);
			if ( (LA154_0==COMMA) ) {
				alt154=1;
			}
			switch (alt154) {
				case 1 :
					// src/main/resources/parser/Java.g:1463:14: ','
					{
					match(input,COMMA,FOLLOW_COMMA_in_arrayInitializer8014); if (state.failed) return;
					}
					break;

			}

			match(input,RBRACE,FOLLOW_RBRACE_in_arrayInitializer8027); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1468:1: createdName : ( classOrInterfaceType | primitiveType );
	public final void createdName() throws RecognitionException {
		int createdName_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 97) ) { return; }

			// src/main/resources/parser/Java.g:1469:5: ( classOrInterfaceType | primitiveType )
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
					// src/main/resources/parser/Java.g:1469:9: classOrInterfaceType
					{
					pushFollow(FOLLOW_classOrInterfaceType_in_createdName8061);
					classOrInterfaceType();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1470:9: primitiveType
					{
					pushFollow(FOLLOW_primitiveType_in_createdName8071);
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
	// src/main/resources/parser/Java.g:1473:1: innerCreator : '.' 'new' ( nonWildcardTypeArguments )? IDENTIFIER ( typeArguments )? classCreatorRest ;
	public final void innerCreator() throws RecognitionException {
		int innerCreator_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 98) ) { return; }

			// src/main/resources/parser/Java.g:1474:5: ( '.' 'new' ( nonWildcardTypeArguments )? IDENTIFIER ( typeArguments )? classCreatorRest )
			// src/main/resources/parser/Java.g:1474:9: '.' 'new' ( nonWildcardTypeArguments )? IDENTIFIER ( typeArguments )? classCreatorRest
			{
			match(input,DOT,FOLLOW_DOT_in_innerCreator8092); if (state.failed) return;
			match(input,NEW,FOLLOW_NEW_in_innerCreator8094); if (state.failed) return;
			// src/main/resources/parser/Java.g:1475:9: ( nonWildcardTypeArguments )?
			int alt156=2;
			int LA156_0 = input.LA(1);
			if ( (LA156_0==LT) ) {
				alt156=1;
			}
			switch (alt156) {
				case 1 :
					// src/main/resources/parser/Java.g:1475:10: nonWildcardTypeArguments
					{
					pushFollow(FOLLOW_nonWildcardTypeArguments_in_innerCreator8105);
					nonWildcardTypeArguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_innerCreator8126); if (state.failed) return;
			// src/main/resources/parser/Java.g:1478:9: ( typeArguments )?
			int alt157=2;
			int LA157_0 = input.LA(1);
			if ( (LA157_0==LT) ) {
				alt157=1;
			}
			switch (alt157) {
				case 1 :
					// src/main/resources/parser/Java.g:1478:10: typeArguments
					{
					pushFollow(FOLLOW_typeArguments_in_innerCreator8137);
					typeArguments();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			pushFollow(FOLLOW_classCreatorRest_in_innerCreator8158);
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
	// src/main/resources/parser/Java.g:1484:1: classCreatorRest : arguments ( classBody )? ;
	public final void classCreatorRest() throws RecognitionException {
		int classCreatorRest_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 99) ) { return; }

			// src/main/resources/parser/Java.g:1485:5: ( arguments ( classBody )? )
			// src/main/resources/parser/Java.g:1485:9: arguments ( classBody )?
			{
			pushFollow(FOLLOW_arguments_in_classCreatorRest8179);
			arguments();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1486:9: ( classBody )?
			int alt158=2;
			int LA158_0 = input.LA(1);
			if ( (LA158_0==LBRACE) ) {
				alt158=1;
			}
			switch (alt158) {
				case 1 :
					// src/main/resources/parser/Java.g:1486:10: classBody
					{
					pushFollow(FOLLOW_classBody_in_classCreatorRest8190);
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
	// src/main/resources/parser/Java.g:1491:1: nonWildcardTypeArguments : '<' typeList '>' ;
	public final void nonWildcardTypeArguments() throws RecognitionException {
		int nonWildcardTypeArguments_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 100) ) { return; }

			// src/main/resources/parser/Java.g:1492:5: ( '<' typeList '>' )
			// src/main/resources/parser/Java.g:1492:9: '<' typeList '>'
			{
			match(input,LT,FOLLOW_LT_in_nonWildcardTypeArguments8222); if (state.failed) return;
			pushFollow(FOLLOW_typeList_in_nonWildcardTypeArguments8224);
			typeList();
			state._fsp--;
			if (state.failed) return;
			match(input,GT,FOLLOW_GT_in_nonWildcardTypeArguments8234); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1496:1: arguments : '(' ( expressionList )? ')' ;
	public final void arguments() throws RecognitionException {
		int arguments_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 101) ) { return; }

			// src/main/resources/parser/Java.g:1497:5: ( '(' ( expressionList )? ')' )
			// src/main/resources/parser/Java.g:1497:9: '(' ( expressionList )? ')'
			{
			match(input,LPAREN,FOLLOW_LPAREN_in_arguments8254); if (state.failed) return;
			// src/main/resources/parser/Java.g:1497:13: ( expressionList )?
			int alt159=2;
			int LA159_0 = input.LA(1);
			if ( (LA159_0==BANG||LA159_0==BOOLEAN||LA159_0==BYTE||(LA159_0 >= CHAR && LA159_0 <= CHARLITERAL)||(LA159_0 >= DOUBLE && LA159_0 <= DOUBLELITERAL)||LA159_0==FALSE||(LA159_0 >= FLOAT && LA159_0 <= FLOATLITERAL)||LA159_0==IDENTIFIER||LA159_0==INT||LA159_0==INTLITERAL||(LA159_0 >= LONG && LA159_0 <= LPAREN)||(LA159_0 >= NEW && LA159_0 <= NULL)||LA159_0==PLUS||LA159_0==PLUSPLUS||LA159_0==SHORT||(LA159_0 >= STRINGLITERAL && LA159_0 <= SUB)||(LA159_0 >= SUBSUB && LA159_0 <= SUPER)||LA159_0==THIS||LA159_0==TILDE||LA159_0==TRUE||LA159_0==VOID) ) {
				alt159=1;
			}
			switch (alt159) {
				case 1 :
					// src/main/resources/parser/Java.g:1497:14: expressionList
					{
					pushFollow(FOLLOW_expressionList_in_arguments8257);
					expressionList();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			match(input,RPAREN,FOLLOW_RPAREN_in_arguments8270); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1501:1: literal : ( INTLITERAL | LONGLITERAL | FLOATLITERAL | DOUBLELITERAL | CHARLITERAL | STRINGLITERAL | TRUE | FALSE | NULL );
	public final void literal() throws RecognitionException {
		int literal_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 102) ) { return; }

			// src/main/resources/parser/Java.g:1502:5: ( INTLITERAL | LONGLITERAL | FLOATLITERAL | DOUBLELITERAL | CHARLITERAL | STRINGLITERAL | TRUE | FALSE | NULL )
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
	// src/main/resources/parser/Java.g:1517:1: classHeader : modifiers 'class' IDENTIFIER ;
	public final void classHeader() throws RecognitionException {
		int classHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 103) ) { return; }

			// src/main/resources/parser/Java.g:1518:5: ( modifiers 'class' IDENTIFIER )
			// src/main/resources/parser/Java.g:1518:9: modifiers 'class' IDENTIFIER
			{
			pushFollow(FOLLOW_modifiers_in_classHeader8394);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			match(input,CLASS,FOLLOW_CLASS_in_classHeader8396); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_classHeader8398); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1521:1: enumHeader : modifiers ( 'enum' | IDENTIFIER ) IDENTIFIER ;
	public final void enumHeader() throws RecognitionException {
		int enumHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 104) ) { return; }

			// src/main/resources/parser/Java.g:1522:5: ( modifiers ( 'enum' | IDENTIFIER ) IDENTIFIER )
			// src/main/resources/parser/Java.g:1522:9: modifiers ( 'enum' | IDENTIFIER ) IDENTIFIER
			{
			pushFollow(FOLLOW_modifiers_in_enumHeader8418);
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
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_enumHeader8426); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1525:1: interfaceHeader : modifiers 'interface' IDENTIFIER ;
	public final void interfaceHeader() throws RecognitionException {
		int interfaceHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 105) ) { return; }

			// src/main/resources/parser/Java.g:1526:5: ( modifiers 'interface' IDENTIFIER )
			// src/main/resources/parser/Java.g:1526:9: modifiers 'interface' IDENTIFIER
			{
			pushFollow(FOLLOW_modifiers_in_interfaceHeader8446);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			match(input,INTERFACE,FOLLOW_INTERFACE_in_interfaceHeader8448); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_interfaceHeader8450); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1529:1: annotationHeader : modifiers '@' 'interface' IDENTIFIER ;
	public final void annotationHeader() throws RecognitionException {
		int annotationHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 106) ) { return; }

			// src/main/resources/parser/Java.g:1530:5: ( modifiers '@' 'interface' IDENTIFIER )
			// src/main/resources/parser/Java.g:1530:9: modifiers '@' 'interface' IDENTIFIER
			{
			pushFollow(FOLLOW_modifiers_in_annotationHeader8470);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			match(input,MONKEYS_AT,FOLLOW_MONKEYS_AT_in_annotationHeader8472); if (state.failed) return;
			match(input,INTERFACE,FOLLOW_INTERFACE_in_annotationHeader8474); if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_annotationHeader8476); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1533:1: typeHeader : modifiers ( 'class' | 'enum' | ( ( '@' )? 'interface' ) ) IDENTIFIER ;
	public final void typeHeader() throws RecognitionException {
		int typeHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 107) ) { return; }

			// src/main/resources/parser/Java.g:1534:5: ( modifiers ( 'class' | 'enum' | ( ( '@' )? 'interface' ) ) IDENTIFIER )
			// src/main/resources/parser/Java.g:1534:9: modifiers ( 'class' | 'enum' | ( ( '@' )? 'interface' ) ) IDENTIFIER
			{
			pushFollow(FOLLOW_modifiers_in_typeHeader8496);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1534:19: ( 'class' | 'enum' | ( ( '@' )? 'interface' ) )
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
					// src/main/resources/parser/Java.g:1534:20: 'class'
					{
					match(input,CLASS,FOLLOW_CLASS_in_typeHeader8499); if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1534:28: 'enum'
					{
					match(input,ENUM,FOLLOW_ENUM_in_typeHeader8501); if (state.failed) return;
					}
					break;
				case 3 :
					// src/main/resources/parser/Java.g:1534:35: ( ( '@' )? 'interface' )
					{
					// src/main/resources/parser/Java.g:1534:35: ( ( '@' )? 'interface' )
					// src/main/resources/parser/Java.g:1534:36: ( '@' )? 'interface'
					{
					// src/main/resources/parser/Java.g:1534:36: ( '@' )?
					int alt160=2;
					int LA160_0 = input.LA(1);
					if ( (LA160_0==MONKEYS_AT) ) {
						alt160=1;
					}
					switch (alt160) {
						case 1 :
							// src/main/resources/parser/Java.g:1534:36: '@'
							{
							match(input,MONKEYS_AT,FOLLOW_MONKEYS_AT_in_typeHeader8504); if (state.failed) return;
							}
							break;

					}

					match(input,INTERFACE,FOLLOW_INTERFACE_in_typeHeader8508); if (state.failed) return;
					}

					}
					break;

			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_typeHeader8512); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1537:1: methodHeader : modifiers ( typeParameters )? ( type | 'void' )? IDENTIFIER '(' ;
	public final void methodHeader() throws RecognitionException {
		int methodHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 108) ) { return; }

			// src/main/resources/parser/Java.g:1538:5: ( modifiers ( typeParameters )? ( type | 'void' )? IDENTIFIER '(' )
			// src/main/resources/parser/Java.g:1538:9: modifiers ( typeParameters )? ( type | 'void' )? IDENTIFIER '('
			{
			pushFollow(FOLLOW_modifiers_in_methodHeader8532);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			// src/main/resources/parser/Java.g:1538:19: ( typeParameters )?
			int alt162=2;
			int LA162_0 = input.LA(1);
			if ( (LA162_0==LT) ) {
				alt162=1;
			}
			switch (alt162) {
				case 1 :
					// src/main/resources/parser/Java.g:1538:19: typeParameters
					{
					pushFollow(FOLLOW_typeParameters_in_methodHeader8534);
					typeParameters();
					state._fsp--;
					if (state.failed) return;
					}
					break;

			}

			// src/main/resources/parser/Java.g:1538:35: ( type | 'void' )?
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
					// src/main/resources/parser/Java.g:1538:36: type
					{
					pushFollow(FOLLOW_type_in_methodHeader8538);
					type();
					state._fsp--;
					if (state.failed) return;
					}
					break;
				case 2 :
					// src/main/resources/parser/Java.g:1538:41: 'void'
					{
					match(input,VOID,FOLLOW_VOID_in_methodHeader8540); if (state.failed) return;
					}
					break;

			}

			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_methodHeader8544); if (state.failed) return;
			match(input,LPAREN,FOLLOW_LPAREN_in_methodHeader8546); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1541:1: fieldHeader : modifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' ) ;
	public final void fieldHeader() throws RecognitionException {
		int fieldHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 109) ) { return; }

			// src/main/resources/parser/Java.g:1542:5: ( modifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' ) )
			// src/main/resources/parser/Java.g:1542:9: modifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' )
			{
			pushFollow(FOLLOW_modifiers_in_fieldHeader8566);
			modifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_fieldHeader8568);
			type();
			state._fsp--;
			if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_fieldHeader8570); if (state.failed) return;
			// src/main/resources/parser/Java.g:1542:35: ( '[' ']' )*
			loop164:
			while (true) {
				int alt164=2;
				int LA164_0 = input.LA(1);
				if ( (LA164_0==LBRACKET) ) {
					alt164=1;
				}

				switch (alt164) {
				case 1 :
					// src/main/resources/parser/Java.g:1542:36: '[' ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_fieldHeader8573); if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_fieldHeader8574); if (state.failed) return;
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
	// src/main/resources/parser/Java.g:1545:1: localVariableHeader : variableModifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' ) ;
	public final void localVariableHeader() throws RecognitionException {
		int localVariableHeader_StartIndex = input.index();

		try {
			if ( state.backtracking>0 && alreadyParsedRule(input, 110) ) { return; }

			// src/main/resources/parser/Java.g:1546:5: ( variableModifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' ) )
			// src/main/resources/parser/Java.g:1546:9: variableModifiers type IDENTIFIER ( '[' ']' )* ( '=' | ',' | ';' )
			{
			pushFollow(FOLLOW_variableModifiers_in_localVariableHeader8604);
			variableModifiers();
			state._fsp--;
			if (state.failed) return;
			pushFollow(FOLLOW_type_in_localVariableHeader8606);
			type();
			state._fsp--;
			if (state.failed) return;
			match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_localVariableHeader8608); if (state.failed) return;
			// src/main/resources/parser/Java.g:1546:43: ( '[' ']' )*
			loop165:
			while (true) {
				int alt165=2;
				int LA165_0 = input.LA(1);
				if ( (LA165_0==LBRACKET) ) {
					alt165=1;
				}

				switch (alt165) {
				case 1 :
					// src/main/resources/parser/Java.g:1546:44: '[' ']'
					{
					match(input,LBRACKET,FOLLOW_LBRACKET_in_localVariableHeader8611); if (state.failed) return;
					match(input,RBRACKET,FOLLOW_RBRACKET_in_localVariableHeader8612); if (state.failed) return;
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
		// src/main/resources/parser/Java.g:379:10: ( classDeclaration )
		// src/main/resources/parser/Java.g:379:10: classDeclaration
		{
		pushFollow(FOLLOW_classDeclaration_in_synpred12_Java537);
		classDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred12_Java

	// $ANTLR start synpred27_Java
	public final void synpred27_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:460:9: ( normalClassDeclaration )
		// src/main/resources/parser/Java.g:460:9: normalClassDeclaration
		{
		pushFollow(FOLLOW_normalClassDeclaration_in_synpred27_Java938);
		normalClassDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred27_Java

	// $ANTLR start synpred43_Java
	public final void synpred43_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:562:9: ( normalInterfaceDeclaration )
		// src/main/resources/parser/Java.g:562:9: normalInterfaceDeclaration
		{
		pushFollow(FOLLOW_normalInterfaceDeclaration_in_synpred43_Java1636);
		normalInterfaceDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred43_Java

	// $ANTLR start synpred52_Java
	public final void synpred52_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:604:10: ( fieldDeclaration )
		// src/main/resources/parser/Java.g:604:10: fieldDeclaration
		{
		pushFollow(FOLLOW_fieldDeclaration_in_synpred52_Java1965);
		fieldDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred52_Java

	// $ANTLR start synpred53_Java
	public final void synpred53_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:605:10: ( methodDeclaration )
		// src/main/resources/parser/Java.g:605:10: methodDeclaration
		{
		pushFollow(FOLLOW_methodDeclaration_in_synpred53_Java1976);
		methodDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred53_Java

	// $ANTLR start synpred54_Java
	public final void synpred54_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:606:10: ( classDeclaration )
		// src/main/resources/parser/Java.g:606:10: classDeclaration
		{
		pushFollow(FOLLOW_classDeclaration_in_synpred54_Java1989);
		classDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred54_Java

	// $ANTLR start synpred57_Java
	public final void synpred57_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:639:10: ( explicitConstructorInvocation )
		// src/main/resources/parser/Java.g:639:10: explicitConstructorInvocation
		{
		pushFollow(FOLLOW_explicitConstructorInvocation_in_synpred57_Java2145);
		explicitConstructorInvocation();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred57_Java

	// $ANTLR start synpred59_Java
	public final void synpred59_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:631:10: ( modifiers ( typeParameters )? IDENTIFIER formalParameters ( 'throws' qualifiedNameList )? '{' ( explicitConstructorInvocation )? ( blockStatement )* '}' )
		// src/main/resources/parser/Java.g:631:10: modifiers ( typeParameters )? IDENTIFIER formalParameters ( 'throws' qualifiedNameList )? '{' ( explicitConstructorInvocation )? ( blockStatement )* '}'
		{
		pushFollow(FOLLOW_modifiers_in_synpred59_Java2057);
		modifiers();
		state._fsp--;
		if (state.failed) return;
		// src/main/resources/parser/Java.g:632:9: ( typeParameters )?
		int alt169=2;
		int LA169_0 = input.LA(1);
		if ( (LA169_0==LT) ) {
			alt169=1;
		}
		switch (alt169) {
			case 1 :
				// src/main/resources/parser/Java.g:632:10: typeParameters
				{
				pushFollow(FOLLOW_typeParameters_in_synpred59_Java2068);
				typeParameters();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred59_Java2089); if (state.failed) return;
		pushFollow(FOLLOW_formalParameters_in_synpred59_Java2099);
		formalParameters();
		state._fsp--;
		if (state.failed) return;
		// src/main/resources/parser/Java.g:636:9: ( 'throws' qualifiedNameList )?
		int alt170=2;
		int LA170_0 = input.LA(1);
		if ( (LA170_0==THROWS) ) {
			alt170=1;
		}
		switch (alt170) {
			case 1 :
				// src/main/resources/parser/Java.g:636:10: 'throws' qualifiedNameList
				{
				match(input,THROWS,FOLLOW_THROWS_in_synpred59_Java2110); if (state.failed) return;
				pushFollow(FOLLOW_qualifiedNameList_in_synpred59_Java2112);
				qualifiedNameList();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		match(input,LBRACE,FOLLOW_LBRACE_in_synpred59_Java2133); if (state.failed) return;
		// src/main/resources/parser/Java.g:639:9: ( explicitConstructorInvocation )?
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
				// src/main/resources/parser/Java.g:639:10: explicitConstructorInvocation
				{
				pushFollow(FOLLOW_explicitConstructorInvocation_in_synpred59_Java2145);
				explicitConstructorInvocation();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		// src/main/resources/parser/Java.g:641:9: ( blockStatement )*
		loop172:
		while (true) {
			int alt172=2;
			int LA172_0 = input.LA(1);
			if ( (LA172_0==ABSTRACT||(LA172_0 >= ASSERT && LA172_0 <= BANG)||(LA172_0 >= BOOLEAN && LA172_0 <= BYTE)||(LA172_0 >= CHAR && LA172_0 <= CLASS)||LA172_0==CONTINUE||LA172_0==DO||(LA172_0 >= DOUBLE && LA172_0 <= DOUBLELITERAL)||LA172_0==ENUM||(LA172_0 >= FALSE && LA172_0 <= FINAL)||(LA172_0 >= FLOAT && LA172_0 <= FOR)||(LA172_0 >= IDENTIFIER && LA172_0 <= IF)||(LA172_0 >= INT && LA172_0 <= INTLITERAL)||LA172_0==LBRACE||(LA172_0 >= LONG && LA172_0 <= LT)||(LA172_0 >= MONKEYS_AT && LA172_0 <= NULL)||LA172_0==PLUS||(LA172_0 >= PLUSPLUS && LA172_0 <= PUBLIC)||LA172_0==RETURN||(LA172_0 >= SEMI && LA172_0 <= SHORT)||(LA172_0 >= STATIC && LA172_0 <= SUB)||(LA172_0 >= SUBSUB && LA172_0 <= SYNCHRONIZED)||(LA172_0 >= THIS && LA172_0 <= THROW)||(LA172_0 >= TILDE && LA172_0 <= WHILE)) ) {
				alt172=1;
			}

			switch (alt172) {
			case 1 :
				// src/main/resources/parser/Java.g:641:10: blockStatement
				{
				pushFollow(FOLLOW_blockStatement_in_synpred59_Java2167);
				blockStatement();
				state._fsp--;
				if (state.failed) return;
				}
				break;

			default :
				break loop172;
			}
		}

		match(input,RBRACE,FOLLOW_RBRACE_in_synpred59_Java2188); if (state.failed) return;
		}

	}
	// $ANTLR end synpred59_Java

	// $ANTLR start synpred68_Java
	public final void synpred68_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:712:9: ( interfaceFieldDeclaration )
		// src/main/resources/parser/Java.g:712:9: interfaceFieldDeclaration
		{
		pushFollow(FOLLOW_interfaceFieldDeclaration_in_synpred68_Java2645);
		interfaceFieldDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred68_Java

	// $ANTLR start synpred69_Java
	public final void synpred69_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:713:9: ( interfaceMethodDeclaration )
		// src/main/resources/parser/Java.g:713:9: interfaceMethodDeclaration
		{
		pushFollow(FOLLOW_interfaceMethodDeclaration_in_synpred69_Java2655);
		interfaceMethodDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred69_Java

	// $ANTLR start synpred70_Java
	public final void synpred70_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:714:9: ( interfaceDeclaration )
		// src/main/resources/parser/Java.g:714:9: interfaceDeclaration
		{
		pushFollow(FOLLOW_interfaceDeclaration_in_synpred70_Java2665);
		interfaceDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred70_Java

	// $ANTLR start synpred71_Java
	public final void synpred71_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:715:9: ( classDeclaration )
		// src/main/resources/parser/Java.g:715:9: classDeclaration
		{
		pushFollow(FOLLOW_classDeclaration_in_synpred71_Java2675);
		classDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred71_Java

	// $ANTLR start synpred96_Java
	public final void synpred96_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:882:9: ( ellipsisParameterDecl )
		// src/main/resources/parser/Java.g:882:9: ellipsisParameterDecl
		{
		pushFollow(FOLLOW_ellipsisParameterDecl_in_synpred96_Java3540);
		ellipsisParameterDecl();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred96_Java

	// $ANTLR start synpred98_Java
	public final void synpred98_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:883:9: ( normalParameterDecl ( ',' normalParameterDecl )* )
		// src/main/resources/parser/Java.g:883:9: normalParameterDecl ( ',' normalParameterDecl )*
		{
		pushFollow(FOLLOW_normalParameterDecl_in_synpred98_Java3550);
		normalParameterDecl();
		state._fsp--;
		if (state.failed) return;
		// src/main/resources/parser/Java.g:884:9: ( ',' normalParameterDecl )*
		loop175:
		while (true) {
			int alt175=2;
			int LA175_0 = input.LA(1);
			if ( (LA175_0==COMMA) ) {
				alt175=1;
			}

			switch (alt175) {
			case 1 :
				// src/main/resources/parser/Java.g:884:10: ',' normalParameterDecl
				{
				match(input,COMMA,FOLLOW_COMMA_in_synpred98_Java3561); if (state.failed) return;
				pushFollow(FOLLOW_normalParameterDecl_in_synpred98_Java3563);
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
		// src/main/resources/parser/Java.g:886:10: ( normalParameterDecl ',' )
		// src/main/resources/parser/Java.g:886:10: normalParameterDecl ','
		{
		pushFollow(FOLLOW_normalParameterDecl_in_synpred99_Java3585);
		normalParameterDecl();
		state._fsp--;
		if (state.failed) return;
		match(input,COMMA,FOLLOW_COMMA_in_synpred99_Java3595); if (state.failed) return;
		}

	}
	// $ANTLR end synpred99_Java

	// $ANTLR start synpred103_Java
	public final void synpred103_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:941:9: ( ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';' )
		// src/main/resources/parser/Java.g:941:9: ( nonWildcardTypeArguments )? ( 'this' | 'super' ) arguments ';'
		{
		// src/main/resources/parser/Java.g:941:9: ( nonWildcardTypeArguments )?
		int alt176=2;
		int LA176_0 = input.LA(1);
		if ( (LA176_0==LT) ) {
			alt176=1;
		}
		switch (alt176) {
			case 1 :
				// src/main/resources/parser/Java.g:941:10: nonWildcardTypeArguments
				{
				pushFollow(FOLLOW_nonWildcardTypeArguments_in_synpred103_Java3784);
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
		pushFollow(FOLLOW_arguments_in_synpred103_Java3842);
		arguments();
		state._fsp--;
		if (state.failed) return;
		match(input,SEMI,FOLLOW_SEMI_in_synpred103_Java3844); if (state.failed) return;
		}

	}
	// $ANTLR end synpred103_Java

	// $ANTLR start synpred117_Java
	public final void synpred117_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1045:9: ( annotationMethodDeclaration )
		// src/main/resources/parser/Java.g:1045:9: annotationMethodDeclaration
		{
		pushFollow(FOLLOW_annotationMethodDeclaration_in_synpred117_Java4467);
		annotationMethodDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred117_Java

	// $ANTLR start synpred118_Java
	public final void synpred118_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1046:9: ( interfaceFieldDeclaration )
		// src/main/resources/parser/Java.g:1046:9: interfaceFieldDeclaration
		{
		pushFollow(FOLLOW_interfaceFieldDeclaration_in_synpred118_Java4477);
		interfaceFieldDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred118_Java

	// $ANTLR start synpred119_Java
	public final void synpred119_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1047:9: ( normalClassDeclaration )
		// src/main/resources/parser/Java.g:1047:9: normalClassDeclaration
		{
		pushFollow(FOLLOW_normalClassDeclaration_in_synpred119_Java4487);
		normalClassDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred119_Java

	// $ANTLR start synpred120_Java
	public final void synpred120_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1048:9: ( normalInterfaceDeclaration )
		// src/main/resources/parser/Java.g:1048:9: normalInterfaceDeclaration
		{
		pushFollow(FOLLOW_normalInterfaceDeclaration_in_synpred120_Java4497);
		normalInterfaceDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred120_Java

	// $ANTLR start synpred121_Java
	public final void synpred121_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1049:9: ( enumDeclaration )
		// src/main/resources/parser/Java.g:1049:9: enumDeclaration
		{
		pushFollow(FOLLOW_enumDeclaration_in_synpred121_Java4507);
		enumDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred121_Java

	// $ANTLR start synpred122_Java
	public final void synpred122_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1050:9: ( annotationTypeDeclaration )
		// src/main/resources/parser/Java.g:1050:9: annotationTypeDeclaration
		{
		pushFollow(FOLLOW_annotationTypeDeclaration_in_synpred122_Java4517);
		annotationTypeDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred122_Java

	// $ANTLR start synpred125_Java
	public final void synpred125_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1093:9: ( localVariableDeclarationStatement )
		// src/main/resources/parser/Java.g:1093:9: localVariableDeclarationStatement
		{
		pushFollow(FOLLOW_localVariableDeclarationStatement_in_synpred125_Java4675);
		localVariableDeclarationStatement();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred125_Java

	// $ANTLR start synpred126_Java
	public final void synpred126_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1094:9: ( classOrInterfaceDeclaration )
		// src/main/resources/parser/Java.g:1094:9: classOrInterfaceDeclaration
		{
		pushFollow(FOLLOW_classOrInterfaceDeclaration_in_synpred126_Java4685);
		classOrInterfaceDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred126_Java

	// $ANTLR start synpred130_Java
	public final void synpred130_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1114:9: ( ( 'assert' ) expression ( ':' expression )? ';' )
		// src/main/resources/parser/Java.g:1114:9: ( 'assert' ) expression ( ':' expression )? ';'
		{
		// src/main/resources/parser/Java.g:1114:9: ( 'assert' )
		// src/main/resources/parser/Java.g:1114:10: 'assert'
		{
		match(input,ASSERT,FOLLOW_ASSERT_in_synpred130_Java4826); if (state.failed) return;
		}

		pushFollow(FOLLOW_expression_in_synpred130_Java4846);
		expression();
		state._fsp--;
		if (state.failed) return;
		// src/main/resources/parser/Java.g:1116:20: ( ':' expression )?
		int alt179=2;
		int LA179_0 = input.LA(1);
		if ( (LA179_0==COLON) ) {
			alt179=1;
		}
		switch (alt179) {
			case 1 :
				// src/main/resources/parser/Java.g:1116:21: ':' expression
				{
				match(input,COLON,FOLLOW_COLON_in_synpred130_Java4849); if (state.failed) return;
				pushFollow(FOLLOW_expression_in_synpred130_Java4851);
				expression();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		match(input,SEMI,FOLLOW_SEMI_in_synpred130_Java4855); if (state.failed) return;
		}

	}
	// $ANTLR end synpred130_Java

	// $ANTLR start synpred132_Java
	public final void synpred132_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1117:9: ( 'assert' expression ( ':' expression )? ';' )
		// src/main/resources/parser/Java.g:1117:9: 'assert' expression ( ':' expression )? ';'
		{
		match(input,ASSERT,FOLLOW_ASSERT_in_synpred132_Java4865); if (state.failed) return;
		pushFollow(FOLLOW_expression_in_synpred132_Java4868);
		expression();
		state._fsp--;
		if (state.failed) return;
		// src/main/resources/parser/Java.g:1117:30: ( ':' expression )?
		int alt180=2;
		int LA180_0 = input.LA(1);
		if ( (LA180_0==COLON) ) {
			alt180=1;
		}
		switch (alt180) {
			case 1 :
				// src/main/resources/parser/Java.g:1117:31: ':' expression
				{
				match(input,COLON,FOLLOW_COLON_in_synpred132_Java4871); if (state.failed) return;
				pushFollow(FOLLOW_expression_in_synpred132_Java4873);
				expression();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		match(input,SEMI,FOLLOW_SEMI_in_synpred132_Java4877); if (state.failed) return;
		}

	}
	// $ANTLR end synpred132_Java

	// $ANTLR start synpred133_Java
	public final void synpred133_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1118:39: ( 'else' statement )
		// src/main/resources/parser/Java.g:1118:39: 'else' statement
		{
		match(input,ELSE,FOLLOW_ELSE_in_synpred133_Java4906); if (state.failed) return;
		pushFollow(FOLLOW_statement_in_synpred133_Java4908);
		statement();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred133_Java

	// $ANTLR start synpred148_Java
	public final void synpred148_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1133:9: ( expression ';' )
		// src/main/resources/parser/Java.g:1133:9: expression ';'
		{
		pushFollow(FOLLOW_expression_in_synpred148_Java5130);
		expression();
		state._fsp--;
		if (state.failed) return;
		match(input,SEMI,FOLLOW_SEMI_in_synpred148_Java5133); if (state.failed) return;
		}

	}
	// $ANTLR end synpred148_Java

	// $ANTLR start synpred149_Java
	public final void synpred149_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1134:9: ( IDENTIFIER ':' statement )
		// src/main/resources/parser/Java.g:1134:9: IDENTIFIER ':' statement
		{
		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred149_Java5148); if (state.failed) return;
		match(input,COLON,FOLLOW_COLON_in_synpred149_Java5150); if (state.failed) return;
		pushFollow(FOLLOW_statement_in_synpred149_Java5152);
		statement();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred149_Java

	// $ANTLR start synpred153_Java
	public final void synpred153_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1158:13: ( catches 'finally' block )
		// src/main/resources/parser/Java.g:1158:13: catches 'finally' block
		{
		pushFollow(FOLLOW_catches_in_synpred153_Java5308);
		catches();
		state._fsp--;
		if (state.failed) return;
		match(input,FINALLY,FOLLOW_FINALLY_in_synpred153_Java5310); if (state.failed) return;
		pushFollow(FOLLOW_block_in_synpred153_Java5312);
		block();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred153_Java

	// $ANTLR start synpred154_Java
	public final void synpred154_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1159:13: ( catches )
		// src/main/resources/parser/Java.g:1159:13: catches
		{
		pushFollow(FOLLOW_catches_in_synpred154_Java5326);
		catches();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred154_Java

	// $ANTLR start synpred157_Java
	public final void synpred157_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1184:9: ( 'for' '(' variableModifiers type IDENTIFIER ':' expression ')' statement )
		// src/main/resources/parser/Java.g:1184:9: 'for' '(' variableModifiers type IDENTIFIER ':' expression ')' statement
		{
		match(input,FOR,FOLLOW_FOR_in_synpred157_Java5518); if (state.failed) return;
		match(input,LPAREN,FOLLOW_LPAREN_in_synpred157_Java5520); if (state.failed) return;
		pushFollow(FOLLOW_variableModifiers_in_synpred157_Java5522);
		variableModifiers();
		state._fsp--;
		if (state.failed) return;
		pushFollow(FOLLOW_type_in_synpred157_Java5524);
		type();
		state._fsp--;
		if (state.failed) return;
		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred157_Java5526); if (state.failed) return;
		match(input,COLON,FOLLOW_COLON_in_synpred157_Java5528); if (state.failed) return;
		pushFollow(FOLLOW_expression_in_synpred157_Java5539);
		expression();
		state._fsp--;
		if (state.failed) return;
		match(input,RPAREN,FOLLOW_RPAREN_in_synpred157_Java5541); if (state.failed) return;
		pushFollow(FOLLOW_statement_in_synpred157_Java5543);
		statement();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred157_Java

	// $ANTLR start synpred161_Java
	public final void synpred161_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1198:9: ( localVariableDeclaration )
		// src/main/resources/parser/Java.g:1198:9: localVariableDeclaration
		{
		pushFollow(FOLLOW_localVariableDeclaration_in_synpred161_Java5722);
		localVariableDeclaration();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred161_Java

	// $ANTLR start synpred202_Java
	public final void synpred202_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1352:9: ( castExpression )
		// src/main/resources/parser/Java.g:1352:9: castExpression
		{
		pushFollow(FOLLOW_castExpression_in_synpred202_Java6967);
		castExpression();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred202_Java

	// $ANTLR start synpred206_Java
	public final void synpred206_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1362:9: ( '(' primitiveType ')' unaryExpression )
		// src/main/resources/parser/Java.g:1362:9: '(' primitiveType ')' unaryExpression
		{
		match(input,LPAREN,FOLLOW_LPAREN_in_synpred206_Java7058); if (state.failed) return;
		pushFollow(FOLLOW_primitiveType_in_synpred206_Java7060);
		primitiveType();
		state._fsp--;
		if (state.failed) return;
		match(input,RPAREN,FOLLOW_RPAREN_in_synpred206_Java7062); if (state.failed) return;
		pushFollow(FOLLOW_unaryExpression_in_synpred206_Java7064);
		unaryExpression();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred206_Java

	// $ANTLR start synpred208_Java
	public final void synpred208_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1372:10: ( '.' IDENTIFIER )
		// src/main/resources/parser/Java.g:1372:10: '.' IDENTIFIER
		{
		match(input,DOT,FOLLOW_DOT_in_synpred208_Java7135); if (state.failed) return;
		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred208_Java7137); if (state.failed) return;
		}

	}
	// $ANTLR end synpred208_Java

	// $ANTLR start synpred209_Java
	public final void synpred209_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1374:10: ( identifierSuffix )
		// src/main/resources/parser/Java.g:1374:10: identifierSuffix
		{
		pushFollow(FOLLOW_identifierSuffix_in_synpred209_Java7159);
		identifierSuffix();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred209_Java

	// $ANTLR start synpred211_Java
	public final void synpred211_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1377:10: ( '.' IDENTIFIER )
		// src/main/resources/parser/Java.g:1377:10: '.' IDENTIFIER
		{
		match(input,DOT,FOLLOW_DOT_in_synpred211_Java7191); if (state.failed) return;
		match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred211_Java7193); if (state.failed) return;
		}

	}
	// $ANTLR end synpred211_Java

	// $ANTLR start synpred212_Java
	public final void synpred212_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1379:10: ( identifierSuffix )
		// src/main/resources/parser/Java.g:1379:10: identifierSuffix
		{
		pushFollow(FOLLOW_identifierSuffix_in_synpred212_Java7215);
		identifierSuffix();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred212_Java

	// $ANTLR start synpred224_Java
	public final void synpred224_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1407:10: ( '[' expression ']' )
		// src/main/resources/parser/Java.g:1407:10: '[' expression ']'
		{
		match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred224_Java7466); if (state.failed) return;
		pushFollow(FOLLOW_expression_in_synpred224_Java7468);
		expression();
		state._fsp--;
		if (state.failed) return;
		match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred224_Java7470); if (state.failed) return;
		}

	}
	// $ANTLR end synpred224_Java

	// $ANTLR start synpred236_Java
	public final void synpred236_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1430:9: ( 'new' nonWildcardTypeArguments classOrInterfaceType classCreatorRest )
		// src/main/resources/parser/Java.g:1430:9: 'new' nonWildcardTypeArguments classOrInterfaceType classCreatorRest
		{
		match(input,NEW,FOLLOW_NEW_in_synpred236_Java7679); if (state.failed) return;
		pushFollow(FOLLOW_nonWildcardTypeArguments_in_synpred236_Java7681);
		nonWildcardTypeArguments();
		state._fsp--;
		if (state.failed) return;
		pushFollow(FOLLOW_classOrInterfaceType_in_synpred236_Java7683);
		classOrInterfaceType();
		state._fsp--;
		if (state.failed) return;
		pushFollow(FOLLOW_classCreatorRest_in_synpred236_Java7685);
		classCreatorRest();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred236_Java

	// $ANTLR start synpred237_Java
	public final void synpred237_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1431:9: ( 'new' classOrInterfaceType classCreatorRest )
		// src/main/resources/parser/Java.g:1431:9: 'new' classOrInterfaceType classCreatorRest
		{
		match(input,NEW,FOLLOW_NEW_in_synpred237_Java7695); if (state.failed) return;
		pushFollow(FOLLOW_classOrInterfaceType_in_synpred237_Java7697);
		classOrInterfaceType();
		state._fsp--;
		if (state.failed) return;
		pushFollow(FOLLOW_classCreatorRest_in_synpred237_Java7699);
		classCreatorRest();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred237_Java

	// $ANTLR start synpred239_Java
	public final void synpred239_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1436:9: ( 'new' createdName '[' ']' ( '[' ']' )* arrayInitializer )
		// src/main/resources/parser/Java.g:1436:9: 'new' createdName '[' ']' ( '[' ']' )* arrayInitializer
		{
		match(input,NEW,FOLLOW_NEW_in_synpred239_Java7729); if (state.failed) return;
		pushFollow(FOLLOW_createdName_in_synpred239_Java7731);
		createdName();
		state._fsp--;
		if (state.failed) return;
		match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred239_Java7741); if (state.failed) return;
		match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred239_Java7743); if (state.failed) return;
		// src/main/resources/parser/Java.g:1438:9: ( '[' ']' )*
		loop193:
		while (true) {
			int alt193=2;
			int LA193_0 = input.LA(1);
			if ( (LA193_0==LBRACKET) ) {
				alt193=1;
			}

			switch (alt193) {
			case 1 :
				// src/main/resources/parser/Java.g:1438:10: '[' ']'
				{
				match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred239_Java7754); if (state.failed) return;
				match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred239_Java7756); if (state.failed) return;
				}
				break;

			default :
				break loop193;
			}
		}

		pushFollow(FOLLOW_arrayInitializer_in_synpred239_Java7777);
		arrayInitializer();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred239_Java

	// $ANTLR start synpred240_Java
	public final void synpred240_Java_fragment() throws RecognitionException {
		// src/main/resources/parser/Java.g:1445:13: ( '[' expression ']' )
		// src/main/resources/parser/Java.g:1445:13: '[' expression ']'
		{
		match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred240_Java7826); if (state.failed) return;
		pushFollow(FOLLOW_expression_in_synpred240_Java7828);
		expression();
		state._fsp--;
		if (state.failed) return;
		match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred240_Java7842); if (state.failed) return;
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
	public static final BitSet FOLLOW_PACKAGE_in_packageDeclaration250 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedName_in_packageDeclaration252 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_packageDeclaration264 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IMPORT_in_importDeclaration285 = new BitSet(new long[]{0x0040000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_STATIC_in_importDeclaration297 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_importDeclaration318 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_DOT_in_importDeclaration320 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_STAR_in_importDeclaration322 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_importDeclaration332 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IMPORT_in_importDeclaration349 = new BitSet(new long[]{0x0040000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_STATIC_in_importDeclaration361 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_importDeclaration382 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_DOT_in_importDeclaration393 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_importDeclaration395 = new BitSet(new long[]{0x0000000080000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_DOT_in_importDeclaration417 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_STAR_in_importDeclaration419 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_importDeclaration440 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_qualifiedImportName460 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_DOT_in_qualifiedImportName471 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_qualifiedImportName473 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_typeDeclaration504 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_typeDeclaration514 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_classOrInterfaceDeclaration537 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceDeclaration_in_classOrInterfaceDeclaration549 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotation_in_modifiers594 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_PUBLIC_in_modifiers606 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_PROTECTED_in_modifiers629 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_PRIVATE_in_modifiers649 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_STATIC_in_modifiers671 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_ABSTRACT_in_modifiers694 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_FINAL_in_modifiers715 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_NATIVE_in_modifiers739 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_SYNCHRONIZED_in_modifiers762 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_TRANSIENT_in_modifiers779 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_VOLATILE_in_modifiers799 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_STRICTFP_in_modifiers820 = new BitSet(new long[]{0x0000100000000012L,0x0011040C00700600L});
	public static final BitSet FOLLOW_FINAL_in_variableModifiers881 = new BitSet(new long[]{0x0000100000000002L,0x0000000000000200L});
	public static final BitSet FOLLOW_annotation_in_variableModifiers902 = new BitSet(new long[]{0x0000100000000002L,0x0000000000000200L});
	public static final BitSet FOLLOW_normalClassDeclaration_in_classDeclaration938 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumDeclaration_in_classDeclaration948 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_normalClassDeclaration977 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_normalClassDeclaration981 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_normalClassDeclaration985 = new BitSet(new long[]{0x0100010000000000L,0x0000000000000082L});
	public static final BitSet FOLLOW_typeParameters_in_normalClassDeclaration998 = new BitSet(new long[]{0x0100010000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_EXTENDS_in_normalClassDeclaration1023 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_normalClassDeclaration1025 = new BitSet(new long[]{0x0100000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_IMPLEMENTS_in_normalClassDeclaration1049 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_typeList_in_normalClassDeclaration1051 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_classBody_in_normalClassDeclaration1084 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LT_in_typeParameters1105 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_typeParameter_in_typeParameters1119 = new BitSet(new long[]{0x0008000002000000L});
	public static final BitSet FOLLOW_COMMA_in_typeParameters1134 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_typeParameter_in_typeParameters1136 = new BitSet(new long[]{0x0008000002000000L});
	public static final BitSet FOLLOW_GT_in_typeParameters1161 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_typeParameter1181 = new BitSet(new long[]{0x0000010000000002L});
	public static final BitSet FOLLOW_EXTENDS_in_typeParameter1192 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_typeBound_in_typeParameter1194 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_typeBound1226 = new BitSet(new long[]{0x0000000000000022L});
	public static final BitSet FOLLOW_AMP_in_typeBound1237 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_typeBound1239 = new BitSet(new long[]{0x0000000000000022L});
	public static final BitSet FOLLOW_modifiers_in_enumDeclaration1271 = new BitSet(new long[]{0x0000002000000000L});
	public static final BitSet FOLLOW_ENUM_in_enumDeclaration1283 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_enumDeclaration1304 = new BitSet(new long[]{0x0100000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_IMPLEMENTS_in_enumDeclaration1315 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_typeList_in_enumDeclaration1317 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_enumBody_in_enumDeclaration1338 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_enumBody1363 = new BitSet(new long[]{0x0040000002000000L,0x0000000011000200L});
	public static final BitSet FOLLOW_enumConstants_in_enumBody1374 = new BitSet(new long[]{0x0000000002000000L,0x0000000011000000L});
	public static final BitSet FOLLOW_COMMA_in_enumBody1396 = new BitSet(new long[]{0x0000000000000000L,0x0000000011000000L});
	public static final BitSet FOLLOW_enumBodyDeclarations_in_enumBody1409 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_RBRACE_in_enumBody1431 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumConstant_in_enumConstants1451 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_enumConstants1462 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_enumConstant_in_enumConstants1464 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_annotations_in_enumConstant1498 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_enumConstant1519 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000042L});
	public static final BitSet FOLLOW_arguments_in_enumConstant1530 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
	public static final BitSet FOLLOW_classBody_in_enumConstant1552 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_enumBodyDeclarations1593 = new BitSet(new long[]{0x1840502100A14012L,0x0019040C30700692L});
	public static final BitSet FOLLOW_classBodyDeclaration_in_enumBodyDeclarations1605 = new BitSet(new long[]{0x1840502100A14012L,0x0019040C30700692L});
	public static final BitSet FOLLOW_normalInterfaceDeclaration_in_interfaceDeclaration1636 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotationTypeDeclaration_in_interfaceDeclaration1646 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_normalInterfaceDeclaration1670 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_INTERFACE_in_normalInterfaceDeclaration1672 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_normalInterfaceDeclaration1674 = new BitSet(new long[]{0x0000010000000000L,0x0000000000000082L});
	public static final BitSet FOLLOW_typeParameters_in_normalInterfaceDeclaration1685 = new BitSet(new long[]{0x0000010000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_EXTENDS_in_normalInterfaceDeclaration1707 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_typeList_in_normalInterfaceDeclaration1709 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceBody_in_normalInterfaceDeclaration1730 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_typeList1750 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_typeList1761 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_typeList1763 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_LBRACE_in_classBody1794 = new BitSet(new long[]{0x1840502100A14010L,0x0019040C31700692L});
	public static final BitSet FOLLOW_classBodyDeclaration_in_classBody1806 = new BitSet(new long[]{0x1840502100A14010L,0x0019040C31700692L});
	public static final BitSet FOLLOW_RBRACE_in_classBody1828 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_interfaceBody1848 = new BitSet(new long[]{0x1840502100A14010L,0x0019040C31700690L});
	public static final BitSet FOLLOW_interfaceBodyDeclaration_in_interfaceBody1860 = new BitSet(new long[]{0x1840502100A14010L,0x0019040C31700690L});
	public static final BitSet FOLLOW_RBRACE_in_interfaceBody1882 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_classBodyDeclaration1902 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STATIC_in_classBodyDeclaration1913 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_classBodyDeclaration1935 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_memberDecl_in_classBodyDeclaration1945 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fieldDeclaration_in_memberDecl1965 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_methodDeclaration_in_memberDecl1976 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_memberDecl1989 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceDeclaration_in_memberDecl2002 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_methodDeclaration2057 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_typeParameters_in_methodDeclaration2068 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_methodDeclaration2089 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_formalParameters_in_methodDeclaration2099 = new BitSet(new long[]{0x0000000000000000L,0x0000400000000002L});
	public static final BitSet FOLLOW_THROWS_in_methodDeclaration2110 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedNameList_in_methodDeclaration2112 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_methodDeclaration2133 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1EF2L});
	public static final BitSet FOLLOW_explicitConstructorInvocation_in_methodDeclaration2145 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_blockStatement_in_methodDeclaration2167 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_RBRACE_in_methodDeclaration2188 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_methodDeclaration2198 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000090L});
	public static final BitSet FOLLOW_typeParameters_in_methodDeclaration2209 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000010L});
	public static final BitSet FOLLOW_type_in_methodDeclaration2234 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_VOID_in_methodDeclaration2250 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_methodDeclaration2270 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_formalParameters_in_methodDeclaration2282 = new BitSet(new long[]{0x0000000000000000L,0x0000400010000006L});
	public static final BitSet FOLLOW_LBRACKET_in_methodDeclaration2295 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_methodDeclaration2299 = new BitSet(new long[]{0x0000000000000000L,0x0000400010000006L});
	public static final BitSet FOLLOW_THROWS_in_methodDeclaration2325 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedNameList_in_methodDeclaration2327 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000002L});
	public static final BitSet FOLLOW_block_in_methodDeclaration2382 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_methodDeclaration2396 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_fieldDeclaration2445 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_fieldDeclaration2455 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_fieldDeclaration2467 = new BitSet(new long[]{0x0000000002000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COMMA_in_fieldDeclaration2486 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_fieldDeclaration2490 = new BitSet(new long[]{0x0000000002000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_fieldDeclaration2514 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_variableDeclarator2557 = new BitSet(new long[]{0x0000004000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_variableDeclarator2572 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_variableDeclarator2576 = new BitSet(new long[]{0x0000004000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_EQ_in_variableDeclarator2600 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1872L});
	public static final BitSet FOLLOW_variableInitializer_in_variableDeclarator2604 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceFieldDeclaration_in_interfaceBodyDeclaration2645 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceMethodDeclaration_in_interfaceBodyDeclaration2655 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceDeclaration_in_interfaceBodyDeclaration2665 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_interfaceBodyDeclaration2675 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_interfaceBodyDeclaration2685 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_interfaceMethodDeclaration2705 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000090L});
	public static final BitSet FOLLOW_typeParameters_in_interfaceMethodDeclaration2716 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000010L});
	public static final BitSet FOLLOW_type_in_interfaceMethodDeclaration2738 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_VOID_in_interfaceMethodDeclaration2749 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_interfaceMethodDeclaration2769 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_formalParameters_in_interfaceMethodDeclaration2779 = new BitSet(new long[]{0x0000000000000000L,0x0000400010000004L});
	public static final BitSet FOLLOW_LBRACKET_in_interfaceMethodDeclaration2790 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_interfaceMethodDeclaration2792 = new BitSet(new long[]{0x0000000000000000L,0x0000400010000004L});
	public static final BitSet FOLLOW_THROWS_in_interfaceMethodDeclaration2814 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedNameList_in_interfaceMethodDeclaration2816 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_interfaceMethodDeclaration2829 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_interfaceFieldDeclaration2851 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_interfaceFieldDeclaration2853 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_interfaceFieldDeclaration2855 = new BitSet(new long[]{0x0000000002000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COMMA_in_interfaceFieldDeclaration2866 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_interfaceFieldDeclaration2868 = new BitSet(new long[]{0x0000000002000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_interfaceFieldDeclaration2889 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_type2927 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_type2940 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_type2944 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_primitiveType_in_type2967 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_type2980 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_type2984 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_IDENTIFIER_in_classOrInterfaceType3037 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_typeArguments_in_classOrInterfaceType3054 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_DOT_in_classOrInterfaceType3078 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_classOrInterfaceType3082 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_typeArguments_in_classOrInterfaceType3102 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_LT_in_typeArguments3249 = new BitSet(new long[]{0x0840400100214000L,0x0000000020800010L});
	public static final BitSet FOLLOW_typeArgument_in_typeArguments3251 = new BitSet(new long[]{0x0008000002000000L});
	public static final BitSet FOLLOW_COMMA_in_typeArguments3262 = new BitSet(new long[]{0x0840400100214000L,0x0000000020800010L});
	public static final BitSet FOLLOW_typeArgument_in_typeArguments3264 = new BitSet(new long[]{0x0008000002000000L});
	public static final BitSet FOLLOW_GT_in_typeArguments3286 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_in_typeArgument3324 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QUES_in_typeArgument3334 = new BitSet(new long[]{0x0000010000000002L,0x0000010000000000L});
	public static final BitSet FOLLOW_set_in_typeArgument3358 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_typeArgument3403 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualifiedName_in_qualifiedNameList3434 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_qualifiedNameList3445 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedName_in_qualifiedNameList3447 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_LPAREN_in_formalParameters3479 = new BitSet(new long[]{0x0840500100214000L,0x0000000028000210L});
	public static final BitSet FOLLOW_formalParameterDecls_in_formalParameters3493 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_formalParameters3518 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ellipsisParameterDecl_in_formalParameterDecls3540 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalParameterDecl_in_formalParameterDecls3550 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_formalParameterDecls3561 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_normalParameterDecl_in_formalParameterDecls3563 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_normalParameterDecl_in_formalParameterDecls3585 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_COMMA_in_formalParameterDecls3595 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_ellipsisParameterDecl_in_formalParameterDecls3617 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variableModifiers_in_normalParameterDecl3656 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_normalParameterDecl3658 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_normalParameterDecl3660 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_normalParameterDecl3675 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_normalParameterDecl3679 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_variableModifiers_in_ellipsisParameterDecl3733 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_ellipsisParameterDecl3743 = new BitSet(new long[]{0x0000000800000000L});
	public static final BitSet FOLLOW_ELLIPSIS_in_ellipsisParameterDecl3748 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_ellipsisParameterDecl3760 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation3784 = new BitSet(new long[]{0x0000000000000000L,0x0000110000000000L});
	public static final BitSet FOLLOW_set_in_explicitConstructorInvocation3810 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_explicitConstructorInvocation3842 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_explicitConstructorInvocation3844 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primary_in_explicitConstructorInvocation3855 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_DOT_in_explicitConstructorInvocation3865 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000080L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_explicitConstructorInvocation3876 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_SUPER_in_explicitConstructorInvocation3897 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_explicitConstructorInvocation3907 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_explicitConstructorInvocation3909 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_qualifiedName3950 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_DOT_in_qualifiedName3963 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_qualifiedName3967 = new BitSet(new long[]{0x0000000080000002L});
	public static final BitSet FOLLOW_annotation_in_annotations4001 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000200L});
	public static final BitSet FOLLOW_MONKEYS_AT_in_annotation4034 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedName_in_annotation4036 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_annotation4050 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0280A1A72L});
	public static final BitSet FOLLOW_elementValuePairs_in_annotation4074 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_elementValue_in_annotation4098 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_annotation4134 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_elementValuePair_in_elementValuePairs4166 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_elementValuePairs4177 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_elementValuePair_in_elementValuePairs4179 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_elementValuePair4210 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_elementValuePair4212 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1A72L});
	public static final BitSet FOLLOW_elementValue_in_elementValuePair4214 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_conditionalExpression_in_elementValue4234 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotation_in_elementValue4244 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_elementValueArrayInitializer_in_elementValue4254 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_elementValueArrayInitializer4274 = new BitSet(new long[]{0x2840C80302614200L,0x000A91B0210A1A72L});
	public static final BitSet FOLLOW_elementValue_in_elementValueArrayInitializer4285 = new BitSet(new long[]{0x0000000002000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_COMMA_in_elementValueArrayInitializer4300 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1A72L});
	public static final BitSet FOLLOW_elementValue_in_elementValueArrayInitializer4302 = new BitSet(new long[]{0x0000000002000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_COMMA_in_elementValueArrayInitializer4331 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_RBRACE_in_elementValueArrayInitializer4335 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_annotationTypeDeclaration4358 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_MONKEYS_AT_in_annotationTypeDeclaration4360 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_INTERFACE_in_annotationTypeDeclaration4370 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_annotationTypeDeclaration4380 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_annotationTypeBody_in_annotationTypeDeclaration4390 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_annotationTypeBody4411 = new BitSet(new long[]{0x1840502100A14010L,0x0011040C31700610L});
	public static final BitSet FOLLOW_annotationTypeElementDeclaration_in_annotationTypeBody4423 = new BitSet(new long[]{0x1840502100A14010L,0x0011040C31700610L});
	public static final BitSet FOLLOW_RBRACE_in_annotationTypeBody4445 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotationMethodDeclaration_in_annotationTypeElementDeclaration4467 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceFieldDeclaration_in_annotationTypeElementDeclaration4477 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalClassDeclaration_in_annotationTypeElementDeclaration4487 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalInterfaceDeclaration_in_annotationTypeElementDeclaration4497 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumDeclaration_in_annotationTypeElementDeclaration4507 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotationTypeDeclaration_in_annotationTypeElementDeclaration4517 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_annotationTypeElementDeclaration4527 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_annotationMethodDeclaration4547 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_annotationMethodDeclaration4549 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_annotationMethodDeclaration4551 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_annotationMethodDeclaration4561 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_annotationMethodDeclaration4563 = new BitSet(new long[]{0x0000000020000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_DEFAULT_in_annotationMethodDeclaration4566 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1A72L});
	public static final BitSet FOLLOW_elementValue_in_annotationMethodDeclaration4568 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_annotationMethodDeclaration4597 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_block4621 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_blockStatement_in_block4632 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_RBRACE_in_block4653 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_localVariableDeclarationStatement_in_blockStatement4675 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_blockStatement4685 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_statement_in_blockStatement4695 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_localVariableDeclaration_in_localVariableDeclarationStatement4716 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_localVariableDeclarationStatement4726 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variableModifiers_in_localVariableDeclaration4746 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_localVariableDeclaration4748 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_localVariableDeclaration4758 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_localVariableDeclaration4769 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_variableDeclarator_in_localVariableDeclaration4771 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_block_in_statement4802 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASSERT_in_statement4826 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_statement4846 = new BitSet(new long[]{0x0000000001000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COLON_in_statement4849 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_statement4851 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement4855 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASSERT_in_statement4865 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_statement4868 = new BitSet(new long[]{0x0000000001000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COLON_in_statement4871 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_statement4873 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement4877 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IF_in_statement4899 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_parExpression_in_statement4901 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_statement4903 = new BitSet(new long[]{0x0000001000000002L});
	public static final BitSet FOLLOW_ELSE_in_statement4906 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_statement4908 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_forstatement_in_statement4930 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_WHILE_in_statement4940 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_parExpression_in_statement4942 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_statement4944 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DO_in_statement4954 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_statement4956 = new BitSet(new long[]{0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_WHILE_in_statement4958 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_parExpression_in_statement4960 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement4962 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_trystatement_in_statement4972 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SWITCH_in_statement4982 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_parExpression_in_statement4984 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_statement4986 = new BitSet(new long[]{0x0000000020080000L,0x0000000001000000L});
	public static final BitSet FOLLOW_switchBlockStatementGroups_in_statement4988 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_RBRACE_in_statement4990 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SYNCHRONIZED_in_statement5000 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_parExpression_in_statement5002 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_statement5004 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_RETURN_in_statement5014 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0300A1870L});
	public static final BitSet FOLLOW_expression_in_statement5017 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement5022 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_THROW_in_statement5032 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_statement5034 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement5036 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BREAK_in_statement5046 = new BitSet(new long[]{0x0040000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_statement5061 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement5078 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CONTINUE_in_statement5088 = new BitSet(new long[]{0x0040000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_statement5103 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement5120 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expression_in_statement5130 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_statement5133 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_statement5148 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_statement5150 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_statement5152 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SEMI_in_statement5162 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_switchBlockStatementGroup_in_switchBlockStatementGroups5184 = new BitSet(new long[]{0x0000000020080002L});
	public static final BitSet FOLLOW_switchLabel_in_switchBlockStatementGroup5213 = new BitSet(new long[]{0x38C1D82350E1C312L,0x003FB7BC347A1E72L});
	public static final BitSet FOLLOW_blockStatement_in_switchBlockStatementGroup5224 = new BitSet(new long[]{0x38C1D82350E1C312L,0x003FB7BC347A1E72L});
	public static final BitSet FOLLOW_CASE_in_switchLabel5255 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_switchLabel5257 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_switchLabel5259 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DEFAULT_in_switchLabel5269 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_switchLabel5271 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TRY_in_trystatement5292 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_trystatement5294 = new BitSet(new long[]{0x0000200000100000L});
	public static final BitSet FOLLOW_catches_in_trystatement5308 = new BitSet(new long[]{0x0000200000000000L});
	public static final BitSet FOLLOW_FINALLY_in_trystatement5310 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_trystatement5312 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_catches_in_trystatement5326 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FINALLY_in_trystatement5340 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_trystatement5342 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_catchClause_in_catches5373 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_catchClause_in_catches5384 = new BitSet(new long[]{0x0000000000100002L});
	public static final BitSet FOLLOW_CATCH_in_catchClause5415 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_catchClause5417 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_formalParameter_in_catchClause5419 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_catchClause5429 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_catchClause5431 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variableModifiers_in_formalParameter5452 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_formalParameter5454 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_formalParameter5456 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_formalParameter5467 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_formalParameter5469 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_FOR_in_forstatement5518 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_forstatement5520 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_variableModifiers_in_forstatement5522 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_forstatement5524 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_forstatement5526 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_forstatement5528 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_forstatement5539 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_forstatement5541 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_forstatement5543 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FOR_in_forstatement5575 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_forstatement5577 = new BitSet(new long[]{0x2840D80300614200L,0x000A91B0300A1A70L});
	public static final BitSet FOLLOW_forInit_in_forstatement5597 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_forstatement5618 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0300A1870L});
	public static final BitSet FOLLOW_expression_in_forstatement5638 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_forstatement5659 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0280A1870L});
	public static final BitSet FOLLOW_expressionList_in_forstatement5679 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_forstatement5700 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_forstatement5702 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_localVariableDeclaration_in_forInit5722 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expressionList_in_forInit5732 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_parExpression5752 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_parExpression5754 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_parExpression5756 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expression_in_expressionList5776 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_expressionList5787 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_expressionList5789 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_conditionalExpression_in_expression5821 = new BitSet(new long[]{0x0008004000042082L,0x0000004280050080L});
	public static final BitSet FOLLOW_assignmentOperator_in_expression5832 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_expression5834 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_EQ_in_assignmentOperator5866 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PLUSEQ_in_assignmentOperator5876 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUBEQ_in_assignmentOperator5886 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STAREQ_in_assignmentOperator5896 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SLASHEQ_in_assignmentOperator5906 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_AMPEQ_in_assignmentOperator5916 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BAREQ_in_assignmentOperator5926 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CARETEQ_in_assignmentOperator5936 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PERCENTEQ_in_assignmentOperator5946 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LT_in_assignmentOperator5957 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_LT_in_assignmentOperator5959 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_assignmentOperator5961 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_assignmentOperator5972 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_assignmentOperator5974 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_assignmentOperator5976 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_assignmentOperator5978 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_assignmentOperator5989 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_assignmentOperator5991 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_assignmentOperator5993 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_conditionalOrExpression_in_conditionalExpression6014 = new BitSet(new long[]{0x0000000000000002L,0x0000000000800000L});
	public static final BitSet FOLLOW_QUES_in_conditionalExpression6025 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_conditionalExpression6027 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_conditionalExpression6029 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_conditionalExpression_in_conditionalExpression6031 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression6062 = new BitSet(new long[]{0x0000000000001002L});
	public static final BitSet FOLLOW_BARBAR_in_conditionalOrExpression6073 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_conditionalAndExpression_in_conditionalOrExpression6075 = new BitSet(new long[]{0x0000000000001002L});
	public static final BitSet FOLLOW_inclusiveOrExpression_in_conditionalAndExpression6106 = new BitSet(new long[]{0x0000000000000042L});
	public static final BitSet FOLLOW_AMPAMP_in_conditionalAndExpression6117 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_inclusiveOrExpression_in_conditionalAndExpression6119 = new BitSet(new long[]{0x0000000000000042L});
	public static final BitSet FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression6150 = new BitSet(new long[]{0x0000000000000802L});
	public static final BitSet FOLLOW_BAR_in_inclusiveOrExpression6161 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_exclusiveOrExpression_in_inclusiveOrExpression6163 = new BitSet(new long[]{0x0000000000000802L});
	public static final BitSet FOLLOW_andExpression_in_exclusiveOrExpression6194 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_CARET_in_exclusiveOrExpression6205 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_andExpression_in_exclusiveOrExpression6207 = new BitSet(new long[]{0x0000000000020002L});
	public static final BitSet FOLLOW_equalityExpression_in_andExpression6238 = new BitSet(new long[]{0x0000000000000022L});
	public static final BitSet FOLLOW_AMP_in_andExpression6249 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_equalityExpression_in_andExpression6251 = new BitSet(new long[]{0x0000000000000022L});
	public static final BitSet FOLLOW_instanceOfExpression_in_equalityExpression6282 = new BitSet(new long[]{0x0000008000000402L});
	public static final BitSet FOLLOW_set_in_equalityExpression6309 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_instanceOfExpression_in_equalityExpression6359 = new BitSet(new long[]{0x0000008000000402L});
	public static final BitSet FOLLOW_relationalExpression_in_instanceOfExpression6390 = new BitSet(new long[]{0x0400000000000002L});
	public static final BitSet FOLLOW_INSTANCEOF_in_instanceOfExpression6401 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_instanceOfExpression6403 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_shiftExpression_in_relationalExpression6434 = new BitSet(new long[]{0x0008000000000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_relationalOp_in_relationalExpression6445 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_shiftExpression_in_relationalExpression6447 = new BitSet(new long[]{0x0008000000000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_LT_in_relationalOp6479 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_relationalOp6481 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_relationalOp6492 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_EQ_in_relationalOp6494 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LT_in_relationalOp6504 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_relationalOp6514 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_additiveExpression_in_shiftExpression6534 = new BitSet(new long[]{0x0008000000000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_shiftOp_in_shiftExpression6545 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_additiveExpression_in_shiftExpression6547 = new BitSet(new long[]{0x0008000000000002L,0x0000000000000080L});
	public static final BitSet FOLLOW_LT_in_shiftOp6580 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_LT_in_shiftOp6582 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_shiftOp6593 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_shiftOp6595 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_shiftOp6597 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_GT_in_shiftOp6608 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_shiftOp6610 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression6631 = new BitSet(new long[]{0x0000000000000002L,0x0000002000020000L});
	public static final BitSet FOLLOW_set_in_additiveExpression6658 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_multiplicativeExpression_in_additiveExpression6708 = new BitSet(new long[]{0x0000000000000002L,0x0000002000020000L});
	public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression6746 = new BitSet(new long[]{0x0000000000000002L,0x0000000140008000L});
	public static final BitSet FOLLOW_set_in_multiplicativeExpression6773 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_multiplicativeExpression6841 = new BitSet(new long[]{0x0000000000000002L,0x0000000140008000L});
	public static final BitSet FOLLOW_PLUS_in_unaryExpression6874 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpression6877 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUB_in_unaryExpression6887 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpression6889 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PLUSPLUS_in_unaryExpression6899 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpression6901 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUBSUB_in_unaryExpression6911 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpression6913 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_unaryExpressionNotPlusMinus_in_unaryExpression6923 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_TILDE_in_unaryExpressionNotPlusMinus6943 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus6945 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BANG_in_unaryExpressionNotPlusMinus6955 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_unaryExpressionNotPlusMinus6957 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_castExpression_in_unaryExpressionNotPlusMinus6967 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primary_in_unaryExpressionNotPlusMinus6977 = new BitSet(new long[]{0x0000000080000002L,0x0000008000080004L});
	public static final BitSet FOLLOW_selector_in_unaryExpressionNotPlusMinus6988 = new BitSet(new long[]{0x0000000080000002L,0x0000008000080004L});
	public static final BitSet FOLLOW_LPAREN_in_castExpression7058 = new BitSet(new long[]{0x0800400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_primitiveType_in_castExpression7060 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_castExpression7062 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_castExpression7064 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_castExpression7074 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_castExpression7076 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_castExpression7078 = new BitSet(new long[]{0x2840C80300614200L,0x000A911020001870L});
	public static final BitSet FOLLOW_unaryExpressionNotPlusMinus_in_castExpression7080 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_parExpression_in_primary7102 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_THIS_in_primary7124 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000044L});
	public static final BitSet FOLLOW_DOT_in_primary7135 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_primary7137 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000044L});
	public static final BitSet FOLLOW_identifierSuffix_in_primary7159 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_primary7180 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000044L});
	public static final BitSet FOLLOW_DOT_in_primary7191 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_primary7193 = new BitSet(new long[]{0x0000000080000002L,0x0000000000000044L});
	public static final BitSet FOLLOW_identifierSuffix_in_primary7215 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUPER_in_primary7236 = new BitSet(new long[]{0x0000000080000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_superSuffix_in_primary7246 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_literal_in_primary7256 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_creator_in_primary7266 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primitiveType_in_primary7276 = new BitSet(new long[]{0x0000000080000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_primary7287 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_primary7289 = new BitSet(new long[]{0x0000000080000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_DOT_in_primary7310 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_primary7312 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VOID_in_primary7322 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_DOT_in_primary7324 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_primary7326 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arguments_in_superSuffix7352 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_superSuffix7362 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_typeArguments_in_superSuffix7365 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_superSuffix7386 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_superSuffix7397 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_identifierSuffix7430 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_identifierSuffix7432 = new BitSet(new long[]{0x0000000080000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_DOT_in_identifierSuffix7453 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_identifierSuffix7455 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_identifierSuffix7466 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_identifierSuffix7468 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_identifierSuffix7470 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_arguments_in_identifierSuffix7491 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_identifierSuffix7501 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_identifierSuffix7503 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_identifierSuffix7513 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_identifierSuffix7515 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_identifierSuffix7517 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_identifierSuffix7519 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_identifierSuffix7529 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_THIS_in_identifierSuffix7531 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_identifierSuffix7541 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_SUPER_in_identifierSuffix7543 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_identifierSuffix7545 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_innerCreator_in_identifierSuffix7555 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_selector7577 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_selector7579 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_selector7590 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_selector7611 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
	public static final BitSet FOLLOW_THIS_in_selector7613 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_selector7623 = new BitSet(new long[]{0x0000000000000000L,0x0000010000000000L});
	public static final BitSet FOLLOW_SUPER_in_selector7625 = new BitSet(new long[]{0x0000000080000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_superSuffix_in_selector7635 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_innerCreator_in_selector7645 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_selector7655 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_selector7657 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_selector7659 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_creator7679 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_creator7681 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_creator7683 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_classCreatorRest_in_creator7685 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_creator7695 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_creator7697 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_classCreatorRest_in_creator7699 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arrayCreator_in_creator7709 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_arrayCreator7729 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_createdName_in_arrayCreator7731 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_arrayCreator7741 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_arrayCreator7743 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
	public static final BitSet FOLLOW_LBRACKET_in_arrayCreator7754 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_arrayCreator7756 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
	public static final BitSet FOLLOW_arrayInitializer_in_arrayCreator7777 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_arrayCreator7788 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_createdName_in_arrayCreator7790 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_arrayCreator7800 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_arrayCreator7802 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_arrayCreator7812 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_arrayCreator7826 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_arrayCreator7828 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_arrayCreator7842 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_arrayCreator7864 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_arrayCreator7866 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000004L});
	public static final BitSet FOLLOW_arrayInitializer_in_variableInitializer7897 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expression_in_variableInitializer7907 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_arrayInitializer7927 = new BitSet(new long[]{0x2840C80302614200L,0x000A91B0210A1872L});
	public static final BitSet FOLLOW_variableInitializer_in_arrayInitializer7943 = new BitSet(new long[]{0x0000000002000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_COMMA_in_arrayInitializer7962 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1872L});
	public static final BitSet FOLLOW_variableInitializer_in_arrayInitializer7964 = new BitSet(new long[]{0x0000000002000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_COMMA_in_arrayInitializer8014 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
	public static final BitSet FOLLOW_RBRACE_in_arrayInitializer8027 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_createdName8061 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_primitiveType_in_createdName8071 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_innerCreator8092 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_NEW_in_innerCreator8094 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_innerCreator8105 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_innerCreator8126 = new BitSet(new long[]{0x0000000000000000L,0x00000000000000C0L});
	public static final BitSet FOLLOW_typeArguments_in_innerCreator8137 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_classCreatorRest_in_innerCreator8158 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_arguments_in_classCreatorRest8179 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000002L});
	public static final BitSet FOLLOW_classBody_in_classCreatorRest8190 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LT_in_nonWildcardTypeArguments8222 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_typeList_in_nonWildcardTypeArguments8224 = new BitSet(new long[]{0x0008000000000000L});
	public static final BitSet FOLLOW_GT_in_nonWildcardTypeArguments8234 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_arguments8254 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0280A1870L});
	public static final BitSet FOLLOW_expressionList_in_arguments8257 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_arguments8270 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_classHeader8394 = new BitSet(new long[]{0x0000000000800000L});
	public static final BitSet FOLLOW_CLASS_in_classHeader8396 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_classHeader8398 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_enumHeader8418 = new BitSet(new long[]{0x0040002000000000L});
	public static final BitSet FOLLOW_set_in_enumHeader8420 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_enumHeader8426 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_interfaceHeader8446 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_INTERFACE_in_interfaceHeader8448 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_interfaceHeader8450 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_annotationHeader8470 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_MONKEYS_AT_in_annotationHeader8472 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_INTERFACE_in_annotationHeader8474 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_annotationHeader8476 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_typeHeader8496 = new BitSet(new long[]{0x1000002000800000L,0x0000000000000200L});
	public static final BitSet FOLLOW_CLASS_in_typeHeader8499 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_ENUM_in_typeHeader8501 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_MONKEYS_AT_in_typeHeader8504 = new BitSet(new long[]{0x1000000000000000L});
	public static final BitSet FOLLOW_INTERFACE_in_typeHeader8508 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_typeHeader8512 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_methodHeader8532 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000090L});
	public static final BitSet FOLLOW_typeParameters_in_methodHeader8534 = new BitSet(new long[]{0x0840400100214000L,0x0008000020000010L});
	public static final BitSet FOLLOW_type_in_methodHeader8538 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_VOID_in_methodHeader8540 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_methodHeader8544 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_methodHeader8546 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_fieldHeader8566 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_fieldHeader8568 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_fieldHeader8570 = new BitSet(new long[]{0x0000004002000000L,0x0000000010000004L});
	public static final BitSet FOLLOW_LBRACKET_in_fieldHeader8573 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_fieldHeader8574 = new BitSet(new long[]{0x0000004002000000L,0x0000000010000004L});
	public static final BitSet FOLLOW_set_in_fieldHeader8578 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variableModifiers_in_localVariableHeader8604 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_localVariableHeader8606 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_localVariableHeader8608 = new BitSet(new long[]{0x0000004002000000L,0x0000000010000004L});
	public static final BitSet FOLLOW_LBRACKET_in_localVariableHeader8611 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_localVariableHeader8612 = new BitSet(new long[]{0x0000004002000000L,0x0000000010000004L});
	public static final BitSet FOLLOW_set_in_localVariableHeader8616 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotations_in_synpred2_Java127 = new BitSet(new long[]{0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_packageDeclaration_in_synpred2_Java156 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_synpred12_Java537 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalClassDeclaration_in_synpred27_Java938 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalInterfaceDeclaration_in_synpred43_Java1636 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_fieldDeclaration_in_synpred52_Java1965 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_methodDeclaration_in_synpred53_Java1976 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_synpred54_Java1989 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_explicitConstructorInvocation_in_synpred57_Java2145 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_modifiers_in_synpred59_Java2057 = new BitSet(new long[]{0x0040000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_typeParameters_in_synpred59_Java2068 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred59_Java2089 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_formalParameters_in_synpred59_Java2099 = new BitSet(new long[]{0x0000000000000000L,0x0000400000000002L});
	public static final BitSet FOLLOW_THROWS_in_synpred59_Java2110 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_qualifiedNameList_in_synpred59_Java2112 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACE_in_synpred59_Java2133 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1EF2L});
	public static final BitSet FOLLOW_explicitConstructorInvocation_in_synpred59_Java2145 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_blockStatement_in_synpred59_Java2167 = new BitSet(new long[]{0x38C1D82350E1C310L,0x003FB7BC357A1E72L});
	public static final BitSet FOLLOW_RBRACE_in_synpred59_Java2188 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceFieldDeclaration_in_synpred68_Java2645 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceMethodDeclaration_in_synpred69_Java2655 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceDeclaration_in_synpred70_Java2665 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classDeclaration_in_synpred71_Java2675 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ellipsisParameterDecl_in_synpred96_Java3540 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalParameterDecl_in_synpred98_Java3550 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_COMMA_in_synpred98_Java3561 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_normalParameterDecl_in_synpred98_Java3563 = new BitSet(new long[]{0x0000000002000002L});
	public static final BitSet FOLLOW_normalParameterDecl_in_synpred99_Java3585 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_COMMA_in_synpred99_Java3595 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_synpred103_Java3784 = new BitSet(new long[]{0x0000000000000000L,0x0000110000000000L});
	public static final BitSet FOLLOW_set_in_synpred103_Java3810 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_arguments_in_synpred103_Java3842 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_synpred103_Java3844 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotationMethodDeclaration_in_synpred117_Java4467 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_interfaceFieldDeclaration_in_synpred118_Java4477 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalClassDeclaration_in_synpred119_Java4487 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_normalInterfaceDeclaration_in_synpred120_Java4497 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enumDeclaration_in_synpred121_Java4507 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_annotationTypeDeclaration_in_synpred122_Java4517 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_localVariableDeclarationStatement_in_synpred125_Java4675 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_classOrInterfaceDeclaration_in_synpred126_Java4685 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASSERT_in_synpred130_Java4826 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred130_Java4846 = new BitSet(new long[]{0x0000000001000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COLON_in_synpred130_Java4849 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred130_Java4851 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_synpred130_Java4855 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASSERT_in_synpred132_Java4865 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred132_Java4868 = new BitSet(new long[]{0x0000000001000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_COLON_in_synpred132_Java4871 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred132_Java4873 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_synpred132_Java4877 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ELSE_in_synpred133_Java4906 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_synpred133_Java4908 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_expression_in_synpred148_Java5130 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
	public static final BitSet FOLLOW_SEMI_in_synpred148_Java5133 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred149_Java5148 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_synpred149_Java5150 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_synpred149_Java5152 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_catches_in_synpred153_Java5308 = new BitSet(new long[]{0x0000200000000000L});
	public static final BitSet FOLLOW_FINALLY_in_synpred153_Java5310 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000002L});
	public static final BitSet FOLLOW_block_in_synpred153_Java5312 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_catches_in_synpred154_Java5326 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FOR_in_synpred157_Java5518 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_LPAREN_in_synpred157_Java5520 = new BitSet(new long[]{0x0840500100214000L,0x0000000020000210L});
	public static final BitSet FOLLOW_variableModifiers_in_synpred157_Java5522 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_type_in_synpred157_Java5524 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred157_Java5526 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_COLON_in_synpred157_Java5528 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred157_Java5539 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_synpred157_Java5541 = new BitSet(new long[]{0x28C1C8035061C300L,0x002EB7B0340A1872L});
	public static final BitSet FOLLOW_statement_in_synpred157_Java5543 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_localVariableDeclaration_in_synpred161_Java5722 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_castExpression_in_synpred202_Java6967 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LPAREN_in_synpred206_Java7058 = new BitSet(new long[]{0x0800400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_primitiveType_in_synpred206_Java7060 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
	public static final BitSet FOLLOW_RPAREN_in_synpred206_Java7062 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_unaryExpression_in_synpred206_Java7064 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_synpred208_Java7135 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred208_Java7137 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identifierSuffix_in_synpred209_Java7159 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOT_in_synpred211_Java7191 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_IDENTIFIER_in_synpred211_Java7193 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_identifierSuffix_in_synpred212_Java7215 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_synpred224_Java7466 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred224_Java7468 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_synpred224_Java7470 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_synpred236_Java7679 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
	public static final BitSet FOLLOW_nonWildcardTypeArguments_in_synpred236_Java7681 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_synpred236_Java7683 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_classCreatorRest_in_synpred236_Java7685 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_synpred237_Java7695 = new BitSet(new long[]{0x0040000000000000L});
	public static final BitSet FOLLOW_classOrInterfaceType_in_synpred237_Java7697 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_classCreatorRest_in_synpred237_Java7699 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEW_in_synpred239_Java7729 = new BitSet(new long[]{0x0840400100214000L,0x0000000020000010L});
	public static final BitSet FOLLOW_createdName_in_synpred239_Java7731 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_LBRACKET_in_synpred239_Java7741 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_synpred239_Java7743 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
	public static final BitSet FOLLOW_LBRACKET_in_synpred239_Java7754 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_synpred239_Java7756 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000006L});
	public static final BitSet FOLLOW_arrayInitializer_in_synpred239_Java7777 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LBRACKET_in_synpred240_Java7826 = new BitSet(new long[]{0x2840C80300614200L,0x000A91B0200A1870L});
	public static final BitSet FOLLOW_expression_in_synpred240_Java7828 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
	public static final BitSet FOLLOW_RBRACKET_in_synpred240_Java7842 = new BitSet(new long[]{0x0000000000000002L});
}
