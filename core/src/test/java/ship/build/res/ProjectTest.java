package ship.build.res;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;

import org.junit.Test;
import ship.AbstractTestCase;
import ship.ProjectFile;

public class ProjectTest extends AbstractTestCase {
  @Test
  public void testHashCodeAndEquals() {
    final String location = randomUUID().toString();
    final Project project1 = new Project(location, new ProjectFile());
    final Project project2 = new Project(location, new ProjectFile());

    assertTrue(project1.hashCode() == project2.hashCode());
    assertTrue(project1.equals(project2));
  }

  @Test
  public void testToString() {
    final String location = randomUUID().toString();
    final Project project = new Project(location, new ProjectFile());
    assertNotNull(project.toString());
    assertTrue(0 < project.toString().length());
  }

}