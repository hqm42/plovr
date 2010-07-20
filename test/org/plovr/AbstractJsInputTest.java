package org.plovr;

import static org.junit.Assert.*;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class AbstractJsInputTest {

  private static class DummyAbstractJsInput extends AbstractJsInput {

    private final String code;

    DummyAbstractJsInput(String name, String code) {
      super(name);
      this.code = code;
    }

    @Override
    public String getCode() {
      return code;
    }
  }

  @Test
  public void testProcessProvideAndRequireParsing() {
    AbstractJsInput jsInputWithTrailingComments = new DummyAbstractJsInput("dummy.js",
        "goog.provide('example.test.Control'); // trailing comment\n" +
        "goog.require('example.test.Config'); // trailing comment\n");
    assertEquals("Call to goog.provide() may be followed by a comment",
        ImmutableList.of("example.test.Control"), jsInputWithTrailingComments.getProvides());
    assertEquals("Call to goog.require() may be followed by a comment",
        ImmutableList.of("example.test.Config"), jsInputWithTrailingComments.getRequires());
  }

  @Test
  public void testWindowsLineEndings() {
    AbstractJsInput jsInputWithWindowsLineEndings = new DummyAbstractJsInput("dummy.js",
        "goog.provide('example.test.Control');\r\n" +
        "goog.require('example.test.Config');\r\n");
    assertEquals("Regex should tolerate lines that end in \\r\\n",
        ImmutableList.of("example.test.Control"), jsInputWithWindowsLineEndings.getProvides());
    assertEquals("Regex should tolerate lines that end in \\r\\n",
        ImmutableList.of("example.test.Config"), jsInputWithWindowsLineEndings.getRequires());
  }
}
