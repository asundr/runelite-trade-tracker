package org.asundr.recovery;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SaveUpgradeIUtils {

    static String version1to2json(final String decoded)
    {
        String converted = decoded;
        Pattern p = Pattern.compile( "\"id\":(\\d+),\"notedID\":(\\d+)");
        Matcher m = p.matcher(converted);
        while (m.find())
        {
            final String originalID = m.group(2).equals("-1") ? m.group(1) : m.group(2);
            converted = converted.replace(m.group(0), "\"id\":" + originalID);
            m = p.matcher(converted);
        }
        return converted;
    }
}
