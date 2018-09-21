package ship.build.web;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ship.build.web.service.BuildService;
import ship.build.web.service.ContractService;
import ship.build.web.service.LiveUpdateService;

@ComponentScan(
    basePackageClasses = {Router.class},
    useDefaultFilters = false,
    includeFilters = @Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = Router.class)
)
@Configuration
@WebAppConfiguration
@EnableWebMvc
public class RouterRouteTestConfig {
  @MockBean
  protected BuildService buildService;

  @MockBean
  protected ContractService contractService;

  @MockBean
  protected LiveUpdateService liveUpdateService;

}
