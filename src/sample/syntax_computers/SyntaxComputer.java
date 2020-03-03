package sample.syntax_computers;

import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

public interface SyntaxComputer {
	StyleSpans<Collection<String>> computeSyntaxHighlight(String text);
}
