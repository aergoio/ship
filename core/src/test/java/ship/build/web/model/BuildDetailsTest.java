package ship.build.web.model;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import ship.AbstractTestCase;
import ship.ProjectFile;
import ship.build.Resource;
import ship.build.res.Project;

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
    source.setDependencies(new BuildDependency(null));
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

  @Test
  public void testMarshall() throws JsonProcessingException {
    final ProjectFile projectFile = new ProjectFile();
    final Project project = new Project(randomUUID().toString(), projectFile);
    final Resource parent = new Resource(project, randomUUID().toString());
    final BuildDependency parentDependency = new BuildDependency(null);
    final BuildDependency childDependency = new BuildDependency(parent);
    parentDependency.add(childDependency);
    final BuildDetails buildDetails = new BuildDetails();
    buildDetails.setDependencies(parentDependency);

    assertNotNull(new ObjectMapper().writeValueAsString(buildDetails));
  }

}