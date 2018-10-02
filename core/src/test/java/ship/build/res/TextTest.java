package ship.build.res;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import hera.util.IoUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.Test;
import ship.AbstractTestCase;

public class TextTest extends AbstractTestCase {

  protected final String expected = randomUUID().toString();
  
  protected final Text text = new Text(() -> new ByteArrayInputStream(expected.getBytes()));

  @Test
  public void testRead() throws Exception {
    final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    final ByteArrayOutputStream byteOut2 = new ByteArrayOutputStream();
    text.read(() -> byteOut);
    text.read(in -> IoUtils.redirect(in, byteOut2));
    assertArrayEquals(expected.getBytes(), byteOut.toByteArray());
    assertArrayEquals(expected.getBytes(), byteOut2.toByteArray());
  }

  @Test
  public void testGet() throws Exception {
    assertEquals(expected, text.get());
  }

}