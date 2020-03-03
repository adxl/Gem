package sample.syntax_computers;

import com.sun.org.apache.xml.internal.res.XMLErrorResources_tr;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaSyntaxComputer implements SyntaxComputer {

	private final String[] KEYWORDS=new String[] {"abstract","assert","boolean","break","byte","case","catch","char","class","const","continue",
			"default","do","double","else","enum","extends","final","finally","float","for","goto","if","implements","import","instanceof","int",
			"interface","long","native","new","package","private","protected","public","return","short","static","strictfp","super","switch",
			"synchronized","this","throw","throws","transient","try","void","volatile","while"};

	private final String KEYWORD_REGEX_PATTERN="\\b("+String.join("|",KEYWORDS)+")\\b";
	private final String LITERAL_REGEX_PATTERN="\\b(true|false|null)\\b";
	private final String SEMICOLON_REGEX_PATTERN="\\;";
	private final String COMMA_REGEX_PATTERN="\\,";
	private final String STRING_REGEX_PATTERN="\"([^\"\\\\]|\\\\.)*\"";
	private final String COMMENT_REGEX_PATTERN="//[^\n]*"+"|"+"/\\*(.|\\R)*?\\*/";

	private final String[] PATTERNS=new String[] {
			"(?<KEYWORD>"+KEYWORD_REGEX_PATTERN+")",
			"(?<LITERAL>"+LITERAL_REGEX_PATTERN+")",
			"(?<SEMICOLON>"+SEMICOLON_REGEX_PATTERN+")",
			"(?<COMMA>"+COMMA_REGEX_PATTERN+")",
			"(?<STRING>"+STRING_REGEX_PATTERN+")",
			"(?<COMMENT>"+COMMENT_REGEX_PATTERN+")"
	};

	private final Pattern PATTERN=Pattern.compile(String.join("|",PATTERNS));

	@Override
	public StyleSpans<Collection<String>> computeSyntaxHighlight(String text) {
		Matcher matcher=PATTERN.matcher(text);
		int lastKeywordEnd=0;

		StyleSpansBuilder<Collection<String>> spansBuilder=new StyleSpansBuilder<>();

		while(matcher.find())
		{
			String styleClass=matcher.group("KEYWORD")!=null ? "kyw" :
									  matcher.group("LITERAL")!=null ? "lit" :
											  matcher.group("SEMICOLON")!=null ? "smc" :
													  matcher.group("COMMA")!=null ? "com" :
															  matcher.group("STRING")!=null ? "str" :
																	  matcher.group("COMMENT")!=null ? "cmt" : null;
			assert styleClass!=null;
			spansBuilder.add(Collections.emptyList(),matcher.start()-lastKeywordEnd);
			spansBuilder.add(Collections.singleton(styleClass),matcher.end()-matcher.start());
			lastKeywordEnd=matcher.end();
		}
		spansBuilder.add(Collections.emptyList(),text.length()-lastKeywordEnd);
		return spansBuilder.create();
	}
}
