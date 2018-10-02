package ship.util;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import ship.AbstractTestCase;

public class BeforeAndAfterTest extends AbstractTestCase {

  protected final String item1 = randomUUID().toString();
  protected final String item2 = randomUUID().toString();
  protected final String item3 = randomUUID().toString();
  protected final String item4 = randomUUID().toString();

  protected final Set<String> before = new HashSet<>(asList(item1, item2, item3));
  protected final Set<String> after = new HashSet<>(asList(item2, item3, item4));

  protected final BeforeAndAfter<String> beforeAndAfter = new BeforeAndAfter<>(before, after);
  @Test
  public void testGetAddedItems() {
    assertEquals(new HashSet<>(asList(item4)), beforeAndAfter.getAddedItems());
  }

  @Test
  public void testGetRemovedItems() {
    assertEquals(new HashSet<>(asList(item1)), beforeAndAfter.getRemovedItems());
  }

  @Test
  public void testGetIntersectedItems() {
    assertEquals(new HashSet<>(asList(item2, item3)), beforeAndAfter.getIntersectedItems());
  }

  @Test
  public void testGetUionedItems() {
    assertEquals(new HashSet<>(asList(item1, item2, item3, item4)), beforeAndAfter.getUnionedItems());
  }

}