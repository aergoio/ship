package ship.test;

import static org.junit.Assert.*;

import org.junit.Test;
import ship.AbstractTestCase;

public class TestReportNodeTest extends AbstractTestCase {

  @Test
  public void testGetTheNumberOfSuccesses() {
    final TestReportNode<?> parent = new TestReportNode<>();
    final TestReportNode<?> child1 = new TestReportNode<>();
    child1.setResult(TestReportNodeResult.Success);
    final TestReportNode<?> child2 = new TestReportNode<>();
    child2.setResult(TestReportNodeResult.Success);
    parent.addChild(child1);
    parent.addChild(child2);

    assertEquals(1, child1.getTheNumberOfSuccesses());
    assertEquals(2, parent.getChildren().size());
    assertEquals(2, parent.getTheNumberOfSuccesses());
  }
}