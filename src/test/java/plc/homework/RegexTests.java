package plc.homework;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. Test structure for steps 1 & 2 are
 * provided, you must create this yourself for step 3.
 *
 * To run tests, either click the run icon on the left margin, which can be used
 * to run all tests or only a specific test. You should make sure your tests are
 * run through IntelliJ (File > Settings > Build, Execution, Deployment > Build
 * Tools > Gradle > Run tests using <em>IntelliJ IDEA</em>). This ensures the
 * name and inputs for the tests are displayed correctly in the run window.
 */
public class RegexTests {

    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     *
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests (see above note if not working).
     */
    @ParameterizedTest
    @MethodSource
    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above.
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("With right symbols", "hello.-11234@gmail.com", true),
                Arguments.of("After dot, there should be 2 to 3 'a-z'", "11234@gmail.azs", true),
                Arguments.of("All alpha letters", "AabcSh@gmail.com", true),

                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false),
                Arguments.of("Wrong domain", "11234@gmail.m", false),
                Arguments.of("No '@'symbol ", "aAh21.gmail.com", false),
                Arguments.of("Choose Wrong domain letters", "thelegend24@gmail.COM", false)



        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                //what has ten letters and starts with gas?
                Arguments.of("10 Characters", "automobile", true),
                Arguments.of("14 Characters", "i<3pancakes10!", true),
                Arguments.of("16 Characters", "i< 3pancakes10!?", true),
                Arguments.of("18 Characters", "i<& 36pancakes10=!", true),
                Arguments.of("20 Characters", "i<^hr3pancakes10\"!", true),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("7 Characters", "6chars!", false),
                Arguments.of("11 Characters", "6chars i!=%", false),
                Arguments.of("13 Characters", "69-!@ ^ychars", false),
                Arguments.of("17 Characters", "i<3pancakes9!= 0?", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        return Stream.of(
                Arguments.of("empty list", "[]", true),
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements Seperate by comma", "[1,2,3]", true),
                Arguments.of("A space before comma", "[1,2, 3]", true),
                Arguments.of("Multiple Elements integer large than 10", "[11,22,33]", true),
                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("Missing comma", "[1 2 3]", false),
                Arguments.of("Trailing Comma", "[1,2,3,]", false),
                Arguments.of("Start with space", "[ 1,2,3]", false),
                Arguments.of("Only a space", "[ ]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        //throw new UnsupportedOperationException(); //TODO
        test(input, Regex.NUMBER, success);
    }

    public static Stream<Arguments> testNumberRegex() {
        //throw new UnsupportedOperationException(); //TODO
        return Stream.of(
                Arguments.of("Single integer", "1", true),
                Arguments.of("Start with + sign", "+1", true),
                Arguments.of("Start with - sign", "-12.0", true),
                Arguments.of("Leading 0", "0.00210", true),
                Arguments.of("Regular decimal", "1.2", true),
                Arguments.of("Start with decimal", ".3", false),
                Arguments.of("More than one decimal", ".0.3", false),
                Arguments.of("Have letter", "h.3", false),
                Arguments.of("Have a special symbol", "2.3#", false),
                Arguments.of("End with decimal", "3.", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testStringRegex(String test, String input, boolean success) {
        //throw new UnsupportedOperationException(); //TODO
        System.out.println(input);
        test(input, Regex.STRING, success);
    }

    public static Stream<Arguments> testStringRegex() {
        //throw new UnsupportedOperationException(); //TODO
        return Stream.of(
                Arguments.of("Regular string", "\"qwe\"", true),
                Arguments.of("Empty string", "\"\"", true),
                Arguments.of("String with symbol", "\"Hello,World!\"", true),
                Arguments.of("String with other symbols and all small letters", "\"what is that?\"", true),
                Arguments.of("String with backslash", "\"1\\t2\"", true),
                Arguments.of("Other letters followed by backslash", "\"1\\e2\"", false),
                Arguments.of("Without '' ", "\"1\\e2", false),
                Arguments.of("Backslash with not bnrt'\"\\", "\"1\\.t2\"", false),
                Arguments.of("Backslash with other symbols", "\"1\\?de2\"", false),
                Arguments.of("Start with backslash but followed by wrong letters", "\\3.", false)
        );
    }

    /**
     * Asserts that the input matches the given pattern. This method doesn't do
     * much now, but you will see this concept in future assignments.
     */
    private static void test(String input, Pattern pattern, boolean success) {
        Assertions.assertEquals(success, pattern.matcher(input).matches());
    }

}
