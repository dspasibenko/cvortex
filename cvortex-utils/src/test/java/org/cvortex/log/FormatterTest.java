package org.cvortex.log;

import static org.junit.Assert.*;

import org.junit.Test;

public class FormatterTest {

    @Test
    public void concatEmptyArgs() {
        String result = Formatter.concatArgs();
        assertTrue("Expected empty string, but receives \"" + result + "\"", result.isEmpty());
    }

    @Test
    public void concatNullArgs() {
        String result = Formatter.concatArgs((Object[]) null);
        assertTrue("Expected \"null\" string, but receives \"" + result + "\"", result.equals("null"));
    }

    @Test
    public void concatNullWithArgs() {
        String result = Formatter.concatArgs(null, "a");
        assertTrue("Expected \"nulla\" string, but receives \"" + result + "\"", result.equals("nulla"));
    }

    @Test
    public void concatArgsWithOneNullArg() {
        String result = Formatter.concatArgs(new Object[] { null });
        assertTrue("Expected \"null\" string, but receives \"" + result + "\"", result.equals("null"));
    }

    @Test
    public void concatArgsWithThrowableArg() {
        String result = Formatter.concatArgs(new Throwable());
        assertTrue("Expected stack trace, but receives \"" + result + "\"",
                result.contains(".concatArgsWithThrowableArg("));
    }

    @Test
    public void getThrowableStackTraceForNull() {
        String result = Formatter.getThrowableStackTrace(null);
        assertTrue("Expected empty string, but receives \"" + result + "\"", result.isEmpty());
    }

    @Test
    public void getThrowableStackTrace() {
        String result = Formatter.getThrowableStackTrace(new Throwable());
        assertTrue("Expected stack trace, but receives \"" + result + "\"",
                result.contains(".getThrowableStackTrace("));
    }

    @Test
    public void performanceTest() {
        // first call for class loading...
        measureFormatter(1);
        int strConcatIterations = measureStringConcat(100);
        int sbIterations = measureStringBuilder(100);
        int formatterIterations = measureFormatter(100);

        System.out.println(String.format(
                "Performance test for 1 second: StringBuilder %d, Formatter %d, StrConcat %d iterations ", sbIterations,
                formatterIterations, strConcatIterations));
    }

    private int measureStringBuilder(long periodMs) {
        int iterations = 0;
        long endTime = System.currentTimeMillis() + periodMs;

        while (System.currentTimeMillis() < endTime) {
            concatArgsUsingStringbuilder(iterations).hashCode();
            iterations++;
        }

        return iterations;
    }

    private String concatArgsUsingStringbuilder(int i) {
        StringBuilder sb = new StringBuilder(80);
        sb.append("This ");
        sb.append(i);
        sb.append(3.5);
        sb.append("text");
        sb.append(System.currentTimeMillis());
        sb.append(i);
        return sb.toString();
    }

    private int measureStringConcat(long periodMs) {
        int iterations = 0;
        long endTime = System.currentTimeMillis() + periodMs;

        while (System.currentTimeMillis() < endTime) {
            @SuppressWarnings("unused")
            String s = "This" + iterations + 3.5 + System.currentTimeMillis() + "text" + iterations + 123;
            iterations++;
        }

        return iterations;
    }

    private int measureFormatter(long periodMs) {
        int iterations = 0;
        long endTime = System.currentTimeMillis() + periodMs;

        while (System.currentTimeMillis() < endTime) {
            Formatter.concatArgs("This ", iterations, 3.5, "text", 123, System.currentTimeMillis()).hashCode();
            iterations++;
        }

        return iterations;
    }
}
