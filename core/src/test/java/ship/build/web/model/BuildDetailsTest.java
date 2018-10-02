package ship.build.web.model;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import ship.AbstractTestCase;

public class BuildDetailsTest extends AbstractTestCase {

  protected BuildDetails source;

  @Before
  public void setUp() {
    source = new BuildDetails();
    source.setElapsedTime(new Random().nextInt());
    source.setError(randomUUID().toString());
    source.setState(new Random().nextInt());
    source.setSequence(new Random().nextInt());
    source.setResult(randomUUID().toString());
    source.setDependencies(new BuildDependency());
    source.setUnitTestReport(new ArrayList<>());
  }


  @Test
  public void testGetSummary() {
    final BuildSummary target = source.getSummary();
    assertNotNull(target);
    assertEquals(source.getElapsedTime(), target.getElapsedTime());
    assertEquals(source.getError(), target.getError());
    assertEquals(source.getState(), target.getState());
  }

  @Test
  public void testCopyFrom() {
    final BuildDetails target = new BuildDetails();

    target.copyFrom(source);
    assertNotEquals(source, target);
    assertEquals(source.getElapsedTime(), target.getElapsedTime());
    assertEquals(source.getError(), target.getError());
    assertEquals(source.getState(), target.getState());
    assertEquals(source.getSequence(), target.getSequence());
    assertEquals(source.getResult(), target.getResult());
    assertEquals(source.getDependencies(), target.getDependencies());
    assertEquals(source.getUnitTestReport(), target.getUnitTestReport());
  }

}