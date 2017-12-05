package com.insnergy.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class RegexTest {
  
  // ref: https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
  // ref: https://www.tutorialspoint.com/java/java_regular_expressions.htm
  @Test
  public void test() throws Exception {
    String line = "abc11BA";
    
    String pattern = "^([a-zA-Z]*)(\\d+)([a-zA-Z]*)([AB]{2})$";
    int groupNumber = 4;
    Pattern r = Pattern.compile(pattern);
    
    // Now create matcher object.
    Matcher m = r.matcher(line);
    if (m.find()) {
      for (int i = 0; i <= groupNumber; i++) {
        System.out.println(String.format("Group[%d]: %s", i, m.group(i)));
      }
    } else {
      System.out.println("NO MATCH");
    }
  }
  
}
