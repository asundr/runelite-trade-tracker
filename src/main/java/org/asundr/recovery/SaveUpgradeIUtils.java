package org.asundr.recovery;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SaveUpgradeIUtils {

    static String version1to2json(final String decoded)
    {
        final Pattern nodedIdPattern = Pattern.compile( "\"id\":(\\d+),\"notedID\":(\\d+)");
        Matcher m = nodedIdPattern.matcher(decoded);
        String converted = decoded;
        while (m.find())
        {
            final String originalID = m.group(2).equals("-1") ? m.group(1) : m.group(2);
            converted = converted.replace(m.group(0), "\"id\":" + originalID);
            m = nodedIdPattern.matcher(converted);
        }
//        final Pattern quantityPattern = Pattern.compile("\"quantity\":");
        converted = converted.replaceAll("\"quantity\":", "num:");
        converted = converted.replaceAll("\"geValue\":", "ge:");
        return converted;
    }
}
