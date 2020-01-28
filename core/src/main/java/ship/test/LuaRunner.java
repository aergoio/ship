/*
 * @copyright defined in LICENSE.txt
 */

package ship.test;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.StringWriter;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Arrays;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.luaj.vm2.LuaError;
import org.slf4j.Logger;
import ship.exception.CommandException;
import ship.util.Messages;

public class LuaRunner {

  protected final transient Logger logger = getLogger(getClass());

  protected static final String NL_0 = LuaRunner.class.getName() + ".0";

  /**
   * Execute lua script.
   *
   * @param source lua script to run
   *
   * @return result to be run
   */
  public TestResult run(final LuaSource source) {
    final StringWriter stringWriter = new StringWriter();
    final TestResult testResult = new TestResult();
    try {
      logger.trace("Lua Script:\n{}", source);
      final ScriptEngineManager mgr = new ScriptEngineManager();
      logger.debug("engine factories:\n{}", mgr.getEngineFactories());
      final String engineName = "lua";
      final ScriptEngine engine = mgr.getEngineByName(engineName);
      if (null == engine) {
          throw new CommandException(Messages.bind(NL_0, engineName));
      }
      engine.getContext().setWriter(stringWriter);
      final Object result = engine.eval(source.getScript());
      logger.debug("Result: {}", result);
      testResult.setResult(result);
      return testResult;
    } catch (final ScriptException e) {
      logger.trace("=== ScriptException ===", e);
      final String rawMessage = e.getMessage();
      final String pattern =
          "eval threw javax.script.ScriptException: [string \"script\"]:{0}: {1}";
      final MessageFormat messageFormat = new MessageFormat(pattern);
      LuaErrorInformation errorInformation = null;
      try {
        final Object[] args = messageFormat.parse(rawMessage);
        final String rowStr = (String) args[0];
        final String message = (String) args[1];
        final int lineNumber = Integer.parseInt(rowStr);
        errorInformation = new LuaErrorInformation(message, lineNumber, -1);
      } catch (final ParseException e1) {
        final String message = e.getMessage();
        final int lineNumber = e.getLineNumber();
        final int columnNumber = e.getColumnNumber();
        errorInformation = new LuaErrorInformation(message, lineNumber, columnNumber);
      }
      return apply(testResult, source, errorInformation);
    } catch (final LuaError e) {
      logger.trace("=== LuaError ===", e);
      final LuaErrorInformation errorInformation = new LuaErrorInformation(e.getMessage());
      return apply(testResult, source, errorInformation);
    } finally {
      final String output = stringWriter.toString();
      logger.debug("Output: {}", output);
      testResult.setOutput(output);
    }
  }

  protected TestResult apply(
      final TestResult testResult,
      final LuaSource source,
      final LuaErrorInformation errorInformation) {
    logger.debug("Error: {}", errorInformation);
    final int lineNumber = errorInformation.getLineNumber();
    testResult.setError(errorInformation);
    if (0 < lineNumber) {
      final String codeSnippet =
          source.toString(lineNumber - 10, lineNumber + 10, Arrays.asList(lineNumber));
      testResult.setCodeSnippet(codeSnippet);
    }

    return testResult;
  }
}
