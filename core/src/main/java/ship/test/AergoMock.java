package ship.test;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.slf4j.Logger;

public class AergoMock {

  protected final transient Logger logger = getLogger(getClass());

  protected final HashMap<String, LuaValue> items = new HashMap<>();

  public OneArgFunction getItem = new OneArgFunction() {
    @Override
    public LuaValue call(final LuaValue name) {
      logger.trace("system.getItem({})", name);
      final String key = name.tojstring();
      logger.trace("Key: {}", key);
      final LuaValue value = items.get(key);
      logger.trace("Value: {}", value);
      if (null == value) {
        return NIL;
      }
      return value;
    }
  };

  public TwoArgFunction setItem = new TwoArgFunction() {
    @Override
    public LuaValue call(LuaValue name, LuaValue value) {
      logger.info("system.setItem({}, {})", name, value);
      final String key = name.tojstring();
      logger.trace("Key: {}", key);
      items.put(key, value);
      return NIL;
    }
  };

}
