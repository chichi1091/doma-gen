package org.seasar.doma.extension.gen.internal.util;

import junit.framework.TestCase;

import java.io.File;

/** @author taedium */
public class FileUtilTest extends TestCase {

  public void testCreateJavaFile() throws Exception {
    File file = FileUtil.createSourceFile(new File("."), "aaa.bbb.Ccc", "java");
    assertEquals("Ccc.java", file.getName());
  }
}
