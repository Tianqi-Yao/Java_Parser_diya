package plc.homework;

import java.util.regex.Pattern;

/**
 * Contains {@link Pattern} constants, which are compiled regular expressions.
 * See the assignment page for resources on regexes as needed.
 */
public class Regex {

    public static final Pattern
            EMAIL = Pattern.compile("[A-Za-z0-9._\\-]+@[A-Za-z0-9-]*\\.[a-z]{2,3}"),
            EVEN_STRINGS = Pattern.compile("^(.{2}){5,10}$"), //TODO
            INTEGER_LIST = Pattern.compile("\\[(\\d+(,\\s?\\d+)*)?\\]"), //TODO
            NUMBER = Pattern.compile("^(\\+|\\-)?\\d+(.?\\d+)?$"), //TODO
            STRING = Pattern.compile("^\\\"([^\\\\]*(\\\\[bnrt'\"\\\\])*)*\\\"$"); //TODO



}
