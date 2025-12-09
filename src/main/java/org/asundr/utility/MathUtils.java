package org.asundr.utility;

public final class MathUtils
{
    // Returns the value limited to be no lower than  min or larger than max
    public static <T extends Comparable<T>> T clamp(T val, T min, T max)
    {
        return val.compareTo(min) < 0 ? min : val.compareTo(max) > 0 ? max : val;
    }

    // Returns true if the passed value is inclusively within the range of [min, max]
    public static <T extends Comparable<T>> boolean inRange(T val, T min, T max)
    {
        return val.compareTo(min) >= 0 && val.compareTo(max) <= 0;
    }
}
