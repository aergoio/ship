package ship.test;

import org.junit.Test;
import org.luaj.vm2.LuaValue;
import ship.AbstractTestCase;

public class AthenaTest extends AbstractTestCase {

  @Test
  public void testCall() {
    Athena athena = new Athena();
    AthenaContext.clear();
    athena.call(null, LuaValue.tableOf());
  }

}