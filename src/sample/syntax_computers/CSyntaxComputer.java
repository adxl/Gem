package sample.syntax_computers;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSyntaxComputer implements SyntaxComputer {

	private final String[] DIRECTIVES=new String[] {"include","define","undef","if","ifdef","ifndef","error"};

	private final String[] KEYWORDS=new String[] {"auto","break","case","char","const","continue","default","do","double","else","enum","extern",
			"float","for","goto","if","int","long","register","return","short","signed","sizeof","static","struct","switch","typedef","union",
			"unsigned","void","volatile","while"};

	private final String DIRECTIVE_REGEX_PATTERN="\\B[##]("+String.join("|",DIRECTIVES)+")\\b";
	private final String HEADER_REGEX_PATTERN="\\B<[^\\s]+[.]h>\\B";
	private final String KEYWORD_REGEX_PATTERN="\\b("+String.join("|",KEYWORDS)+")\\b";
	private final String LITERAL_REGEX_PATTERN="\\b(true|false|null)\\b";
	private final String SEMICOLON_REGEX_PATTERN="\\;";
	private final String COMMA_REGEX_PATTERN="\\,";
	private final String STRING_REGEX_PATTERN="\"([^\"\\\\]|\\\\.)*\"";
	private final String COMMENT_REGEX_PATTERN="//[^\n]*"+"|"+"/\\*(.|\\R)*?\\*/";

	private final String[] PATTERNS=new String[] {
			"(?<DIRECTIVE>"+DIRECTIVE_REGEX_PATTERN+")",
			"(?<HEADER>"+HEADER_REGEX_PATTERN+")",
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
			String styleClass=matcher.group("KEYWORD")!=null ? "c_keyword" :
									  matcher.group("DIRECTIVE")!=null ? "c_directive" :
											  matcher.group("LITERAL")!=null ? "c_literal" :
													  matcher.group("SEMICOLON")!=null ? "c_semicolon" :
															  matcher.group("COMMA")!=null ? "c_comma" :
																	  matcher.group("STRING")!=null ? "c_string" :
																			  matcher.group("HEADER")!=null ? "c_header" :
																					  matcher.group("COMMENT")!=null ? "c_comment" : null;
			assert styleClass!=null;
			spansBuilder.add(Collections.emptyList(),matcher.start()-lastKeywordEnd);
			spansBuilder.add(Collections.singleton(styleClass),matcher.end()-matcher.start());
			lastKeywordEnd=matcher.end();
		}
		spansBuilder.add(Collections.emptyList(),text.length()-lastKeywordEnd);
		return spansBuilder.create();
	}
}
