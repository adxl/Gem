package sample.syntax_computers;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTMLSyntaxComputer implements SyntaxComputer {

	private final String[] TAGS=new String[] {
			"<[a-z|A-Z|0-9]+>","<[a-z|A-Z|0-9]+","</[a-z|A-Z|0-9]+>",">"
	};


	private final String TAG_REGEX_PATTERN="("+String.join("|",TAGS)+")";
	private final String VALUE_REGEX_PATTERN="=\".+\"";


	private final String[] PATTERNS=new String[] {
			"(?<TAG>"+TAG_REGEX_PATTERN+")",
			"(?<VALUE>"+VALUE_REGEX_PATTERN+")"
	};

	private final Pattern PATTERN=Pattern.compile(String.join("|",PATTERNS));

	@Override
	public StyleSpans<Collection<String>> computeSyntaxHighlight(String text) {
		Matcher matcher=PATTERN.matcher(text);
		int lastKeywordEnd=0;

		StyleSpansBuilder<Collection<String>> spansBuilder=new StyleSpansBuilder<>();

		while(matcher.find())
		{
			String styleClass=matcher.group("TAG")!=null ? "html_tag" :
									  matcher.group("VALUE")!=null ? "html_value" : null;
			assert styleClass!=null;
			spansBuilder.add(Collections.emptyList(),matcher.start()-lastKeywordEnd);
			spansBuilder.add(Collections.singleton(styleClass),matcher.end()-matcher.start());
			lastKeywordEnd=matcher.end();
		}
		spansBuilder.add(Collections.emptyList(),text.length()-lastKeywordEnd);
		return spansBuilder.create();
	}
}
