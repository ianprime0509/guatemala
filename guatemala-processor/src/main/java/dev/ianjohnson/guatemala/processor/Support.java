package dev.ianjohnson.guatemala.processor;

import javax.lang.model.SourceVersion;
import java.util.Locale;
import java.util.regex.Pattern;

final class Support {
    private static final Pattern NEW_WITH_PREFIX = Pattern.compile("^new(?:_with)?");
    private static final Pattern UNDERSCORE_NEW_WORD = Pattern.compile("_(.)");

    private Support() {}

    static String toJavaCamelCase(String cName) {
        if ("...".equals(cName)) {
            return "rest";
        }
        cName = NEW_WITH_PREFIX.matcher(cName).replaceFirst("of");
        cName = UNDERSCORE_NEW_WORD.matcher(cName).replaceAll(match -> match.group(1)
                .toUpperCase(Locale.ROOT));
        if (!SourceVersion.isName(cName)) {
            cName = "_" + cName;
        }
        return cName;
    }

    static String toJavaSnakeCase(String cName) {
        cName = cName.toUpperCase(Locale.ROOT);
        if (!SourceVersion.isName(cName)) {
            cName = "_" + cName;
        }
        return cName;
    }
}
