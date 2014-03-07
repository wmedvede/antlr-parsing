// $ANTLR 3.5 src/main/resources/example/CSV.g 2014-03-07 12:01:33

    package example;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class CSVParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "Comma", "LineBreak", "QuotedValue", 
		"SimpleValue"
	};
	public static final int EOF=-1;
	public static final int Comma=4;
	public static final int LineBreak=5;
	public static final int QuotedValue=6;
	public static final int SimpleValue=7;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public CSVParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public CSVParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return CSVParser.tokenNames; }
	@Override public String getGrammarFileName() { return "src/main/resources/example/CSV.g"; }



	// $ANTLR start "file"
	// src/main/resources/example/CSV.g:32:1: file returns [List<List<String>> data] : ( row )+ EOF ;
	public final List<List<String>> file() throws RecognitionException {
		List<List<String>> data = null;


		List<String> row1 =null;

		data = new ArrayList<List<String>>();
		try {
			// src/main/resources/example/CSV.g:34:3: ( ( row )+ EOF )
			// src/main/resources/example/CSV.g:34:6: ( row )+ EOF
			{
			// src/main/resources/example/CSV.g:34:6: ( row )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( ((LA1_0 >= QuotedValue && LA1_0 <= SimpleValue)) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// src/main/resources/example/CSV.g:34:7: row
					{
					pushFollow(FOLLOW_row_in_file46);
					row1=row();
					state._fsp--;

					data.add(row1);
					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			match(input,EOF,FOLLOW_EOF_in_file52); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return data;
	}
	// $ANTLR end "file"



	// $ANTLR start "row"
	// src/main/resources/example/CSV.g:37:1: row returns [List<String> list] : a= value ( Comma b= value )* ( LineBreak | EOF ) ;
	public final List<String> row() throws RecognitionException {
		List<String> list = null;


		String a =null;
		String b =null;

		list = new ArrayList<String>();
		try {
			// src/main/resources/example/CSV.g:39:3: (a= value ( Comma b= value )* ( LineBreak | EOF ) )
			// src/main/resources/example/CSV.g:39:6: a= value ( Comma b= value )* ( LineBreak | EOF )
			{
			pushFollow(FOLLOW_value_in_row77);
			a=value();
			state._fsp--;

			list.add(a);
			// src/main/resources/example/CSV.g:39:34: ( Comma b= value )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==Comma) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// src/main/resources/example/CSV.g:39:35: Comma b= value
					{
					match(input,Comma,FOLLOW_Comma_in_row82); 
					pushFollow(FOLLOW_value_in_row86);
					b=value();
					state._fsp--;

					list.add(b);
					}
					break;

				default :
					break loop2;
				}
			}

			if ( input.LA(1)==EOF||input.LA(1)==LineBreak ) {
				input.consume();
				state.errorRecovery=false;
			}
			else {
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
		}
		return list;
	}
	// $ANTLR end "row"



	// $ANTLR start "value"
	// src/main/resources/example/CSV.g:42:1: value returns [String val] : ( SimpleValue | QuotedValue );
	public final String value() throws RecognitionException {
		String val = null;


		Token SimpleValue2=null;
		Token QuotedValue3=null;

		try {
			// src/main/resources/example/CSV.g:43:3: ( SimpleValue | QuotedValue )
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==SimpleValue) ) {
				alt3=1;
			}
			else if ( (LA3_0==QuotedValue) ) {
				alt3=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}

			switch (alt3) {
				case 1 :
					// src/main/resources/example/CSV.g:43:6: SimpleValue
					{
					SimpleValue2=(Token)match(input,SimpleValue,FOLLOW_SimpleValue_in_value116); 
					val = (SimpleValue2!=null?SimpleValue2.getText():null);
					}
					break;
				case 2 :
					// src/main/resources/example/CSV.g:44:6: QuotedValue
					{
					QuotedValue3=(Token)match(input,QuotedValue,FOLLOW_QuotedValue_in_value125); 

					                        val = (QuotedValue3!=null?QuotedValue3.getText():null);
					                        val = val.substring(1, val.length()-1); // remove leading- and trailing quotes
					                        val = val.replace("\"\"", "\""); // replace all `""` with `"`
					                 
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
		}
		return val;
	}
	// $ANTLR end "value"

	// Delegated rules



	public static final BitSet FOLLOW_row_in_file46 = new BitSet(new long[]{0x00000000000000C0L});
	public static final BitSet FOLLOW_EOF_in_file52 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_value_in_row77 = new BitSet(new long[]{0x0000000000000030L});
	public static final BitSet FOLLOW_Comma_in_row82 = new BitSet(new long[]{0x00000000000000C0L});
	public static final BitSet FOLLOW_value_in_row86 = new BitSet(new long[]{0x0000000000000030L});
	public static final BitSet FOLLOW_set_in_row92 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SimpleValue_in_value116 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_QuotedValue_in_value125 = new BitSet(new long[]{0x0000000000000002L});
}
