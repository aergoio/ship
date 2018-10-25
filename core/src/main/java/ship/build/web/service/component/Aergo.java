package ship.build.web.service.component;

import hera.api.model.Fee;
import ship.util.AergoPool;

public interface Aergo {
  AergoPool getAergoPool();

  Fee getFee();
}
