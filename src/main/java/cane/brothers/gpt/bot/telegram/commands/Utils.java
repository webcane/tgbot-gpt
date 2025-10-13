package cane.brothers.gpt.bot.telegram.commands;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface Utils {

    Map<CharSequence, CharSequence> ESCAPE_MAP = new HashMap<>() {
        {
            put("_", "\\_");
            //
            put("*", "\\*");
//            put("**", "*");
            put("[", "\\[");
            put("]", "\\]");
            put("(", "\\(");
            put(")", "\\)");
            put("~", "\\~");
            //
            put("`", "\\`");
            put(">", "\\>");
            put("<", "\\<");
            //
            put("#", "\\#");
//            put("#", "");
            put("+", "\\+");
            put("-", "\\-");
            put("=", "\\=");
            put("|", "\\|");
            put("{", "\\{");
            put("}", "\\}");
            put(".", "\\.");
            put("!", "\\!");
        }
    };
    AggregateTranslator ESCAPE = new AggregateTranslator(
            new LookupTranslator(Collections.unmodifiableMap(ESCAPE_MAP))
    );

    default String escape(String input) {
        return StringEscapeUtils.builder(ESCAPE).escape(input).toString();
    }
}
