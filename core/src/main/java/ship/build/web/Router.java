/*
 * @copyright defined in LICENSE.txt
 */

package ship.build.web;

import static hera.util.ExceptionUtils.getStackTraceOf;
import static hera.util.ObjectUtils.nvl;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.Setter;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import ship.build.web.exception.HttpException;
import ship.build.web.exception.ResourceNotFoundException;
import ship.build.web.model.BuildDetails;
import ship.build.web.model.BuildSummary;
import ship.build.web.model.ContractInput;
import ship.build.web.model.DeploymentResult;
import ship.build.web.model.ExecutionResult;
import ship.build.web.model.QueryResult;
import ship.build.web.model.ServiceError;
import ship.build.web.service.BuildService;
import ship.build.web.service.ContractService;
import ship.build.web.service.LiveUpdateService;

@RestController
@ControllerAdvice
public class Router {

  protected final transient Logger logger = getLogger(getClass());

  @Inject
  @Setter
  protected BuildService buildService;

  @Inject
  @Setter
  protected ContractService contractService;

  @Inject
  @Setter
  protected LiveUpdateService liveUpdateService;

  @PostConstruct
  public void initialize() {
    buildService.addListener(liveUpdateService::notifyChange);
  }

  @GetMapping("contract")
  public DeploymentResult getLatestContract() {
    return contractService.getLatestContractInformation();
  }

  @GetMapping("builds")
  public List<BuildSummary> getLatestBuilds() {
    return buildService.list(null, 5);
  }

  @GetMapping("build/{uuid}")
  public BuildDetails getBuild(@PathVariable("uuid") final String buildUuid) {
    return buildService.get(buildUuid).orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Deploy result of build with {@code buildUuid}.
   *
   * @param buildUuid build uuid
   *
   * @return deployment result
   *
   * @throws Exception Fail to deploy
   */
  @PostMapping("build/{uuid}/deploy")
  public DeploymentResult deploy(@PathVariable("uuid") final String buildUuid) throws Exception {
    final BuildDetails buildDetails = buildService.get(buildUuid)
        .orElseThrow(() -> new ResourceNotFoundException(buildUuid + " not found"));
    return contractService.deploy(buildDetails);
  }

  @PostMapping(value = "contract/{tx}/{function}")
  public ExecutionResult execute(
      @PathVariable("tx") final String contractTransactionHash,
      @PathVariable("function") final String functionName,
      @RequestBody final ContractInput contractInput) throws IOException {
    final String[] arguments = contractInput.getArguments();
    return contractService.tryExecute(contractTransactionHash, functionName, arguments);
  }

  /**
   * Query contract.
   *
   * @param contractTransactionHash transaction hash
   * @param functionName            function'name to call
   * @param arguments               argument for function
   *
   * @return function's result
   *
   * @throws IOException Fail to invoke function
   */
  @GetMapping(value = "contract/{tx}/{function}")
  public QueryResult query(
      @PathVariable("tx") final String contractTransactionHash,
      @PathVariable("function") final String functionName,
      @RequestParam(value = "arguments", required = false) final String[] arguments
  ) {
    logger.trace("Transaction Hash: {}, Function: {}, Arguments: {}",
        contractTransactionHash, functionName, arguments);
    final String[] safeArguments = nvl(arguments, new String[0]);
    return contractService.tryQuery(contractTransactionHash, functionName, safeArguments);
  }

  @ExceptionHandler(value = { HttpException.class })
  protected ResponseEntity handleHttpException(final HttpException ex, final WebRequest request) {
    logger.warn("Unexpected exception:", ex);
    return ResponseEntity.status(ex.getStatusCode()).body(handleThrowable(ex, request));
  }

  @ExceptionHandler(value = { Throwable.class })
  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ResponseBody
  protected Object handleThrowable(Throwable ex, WebRequest request) {
    logger.warn("Unexpected exception:", ex);
    return new ServiceError(ex.getMessage(), getStackTraceOf(ex));
  }
}
