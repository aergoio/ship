package ship.util;

import static hera.util.StringUtils.removeSuffix;
import static hera.util.ValidationUtils.assertEquals;
import static hera.util.ValidationUtils.assertNotNull;
import static hera.util.ValidationUtils.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;

public class AnsiMessagePrinter implements MessagePrinter {

  public static final String COLOR_RESET   = "\u001B[0m";

  public static final String COLOR_BLACK   = "\u001B[30m";
  public static final String COLOR_RED     = "\u001B[31m";
  public static final String COLOR_GREEN   = "\u001B[32m";
  public static final String COLOR_YELLOW  = "\u001B[33m";
  public static final String COLOR_BLUE    = "\u001B[34m";
  public static final String COLOR_MAGENTA = "\u001B[35m";
  public static final String COLOR_CYAN    = "\u001B[36m";
  public static final String COLOR_WHITE   = "\u001B[37m";

  public static final String COLOR_BRIGHT_BLACK    = removeSuffix(COLOR_BLACK, "m") + ";1m";
  public static final String COLOR_BRIGHT_RED      = removeSuffix(COLOR_RED, "m") + ";1m";
  public static final String COLOR_BRIGHT_GREEN    = removeSuffix(COLOR_GREEN, "m") + ";1m";
  public static final String COLOR_BRIGHT_YELLOW   = removeSuffix(COLOR_YELLOW, "m") + ";1m";
  public static final String COLOR_BRIGHT_BLUE     = removeSuffix(COLOR_BLUE, "m") + ";1m";
  public static final String COLOR_BRIGHT_MAGENTA  = removeSuffix(COLOR_MAGENTA, "m") + ";1m";
  public static final String COLOR_BRIGHT_CYAN     = removeSuffix(COLOR_CYAN, "m") + ";1m";
  public static final String COLOR_BRIGHT_WHITE    = removeSuffix(COLOR_WHITE, "m") + ";1m";

  public static final String BG_BLACK = "\u001B[40m";
  public static final String BG_RED = "\u001B[41m";
  public static final String BG_GREEN = "\u001B[42m";
  public static final String BG_YELLOW = "\u001B[43m";
  public static final String BG_BLUE = "\u001B[44m";
  public static final String BG_MAGENTA = "\u001B[45m";
  public static final String BG_CYAN = "\u001B[46m";
  public static final String BG_WHITE = "\u001B[47m";

  public static final String BG_BRIGHT_BLACK = removeSuffix(BG_BLACK, "m") + ";1m";
  public static final String BG_BRIGHT_RED = removeSuffix(BG_RED, "m") + ";1m";
  public static final String BG_BRIGHT_GREEN = removeSuffix(BG_GREEN, "m") + ";1m";
  public static final String BG_BRIGHT_YELLOW = removeSuffix(BG_YELLOW, "m") + ";1m";
  public static final String BG_BRIGHT_BLUE = removeSuffix(BG_BLUE, "m") + ";1m";
  public static final String BG_BRIGHT_MAGENTA = removeSuffix(BG_MAGENTA, "m") + ";1m";
  public static final String BG_BRIGHT_CYAN = removeSuffix(BG_CYAN, "m") + ";1m";
  public static final String BG_BRIGHT_WHITE = removeSuffix(BG_WHITE, "m") + ";1m";

  protected final transient Logger logger = getLogger(getClass());

  @Getter
  @Setter
  protected String resetCode = COLOR_RESET;

  @Getter
  @Setter
  protected Map<String, String> colors = new HashMap<>();

  protected final PrintStream out;

  /**
   * Constructor with stream.
   *
   * @param out stream to print out
   */
  public AnsiMessagePrinter(final PrintStream out) {
    this.out = out;
    colors.put("black", COLOR_BLACK);
    colors.put("red", COLOR_RED);
    colors.put("green", COLOR_GREEN);
    colors.put("yellow", COLOR_YELLOW);
    colors.put("blue", COLOR_BLUE);
    colors.put("magenta", COLOR_MAGENTA);
    colors.put("cyan", COLOR_CYAN);
    colors.put("white", COLOR_WHITE);

    colors.put("bright_black", COLOR_BRIGHT_BLACK);
    colors.put("bright_red", COLOR_BRIGHT_RED);
    colors.put("bright_green", COLOR_BRIGHT_GREEN);
    colors.put("bright_yellow", COLOR_BRIGHT_YELLOW);
    colors.put("bright_blue", COLOR_BRIGHT_BLUE);
    colors.put("bright_magenta", COLOR_BRIGHT_MAGENTA);
    colors.put("bright_cyan", COLOR_BRIGHT_CYAN);
    colors.put("bright_white", COLOR_BRIGHT_WHITE);

    colors.put("bg_black", BG_BLACK);
    colors.put("bg_red", BG_RED);
    colors.put("bg_green", BG_GREEN);
    colors.put("bg_yellow", BG_YELLOW);
    colors.put("bg_blue", BG_BLUE);
    colors.put("bg_magenta", BG_MAGENTA);
    colors.put("bg_cyan", BG_CYAN);
    colors.put("bg_white", BG_WHITE);

    colors.put("bg_bright_black", BG_BRIGHT_BLACK);
    colors.put("bg_bright_red", BG_BRIGHT_RED);
    colors.put("bg_bright_green", BG_BRIGHT_GREEN);
    colors.put("bg_bright_yellow", BG_BRIGHT_YELLOW);
    colors.put("bg_bright_blue", BG_BRIGHT_BLUE);
    colors.put("bg_bright_magenta", BG_BRIGHT_MAGENTA);
    colors.put("bg_bright_cyan", BG_BRIGHT_CYAN);
    colors.put("bg_bright_white", BG_BRIGHT_WHITE);
  }

  @RequiredArgsConstructor
  abstract class State {
    @Getter
    protected final Stack<String> tags;

    @Getter
    protected final StringWriter writer;

    abstract State next(int ch);
  }

  class Normal extends State {

    protected static final int CH_TAG_START = '<';
    protected static final int CH_ESCAPE = '!';

    public Normal(final Stack<String> tags, final StringWriter writer) {
      super(tags, writer);
    }

    @Override
    public State next(int ch) {
      if (CH_ESCAPE == ch) {
        return new Escape(tags, writer);
      } else if (CH_TAG_START == ch) {
        return new TagOpen(tags, writer);
      } else {
        writer.write((char) ch);
        return this;
      }
    }
  }

  class Escape extends State {

    public Escape(final Stack<String> tags, final StringWriter writer) {
      super(tags, writer);
    }

    @Override
    public State next(int ch) {
      writer.write((char) ch);
      return new Normal(tags, writer);
    }
  }

  class TagOpen extends State {
    protected static final int CH_TAG_CLOSE = '/';

    public TagOpen(final Stack<String> tags, final StringWriter writer) {
      super(tags, writer);
    }

    @Override
    public State next(int ch) {
      if (ch == CH_TAG_CLOSE) {
        return new CloseColor(tags, writer);
      } else {
        return new OpenColor(tags, writer).next(ch);
      }
    }
  }

  class OpenColor extends State {
    protected static final int CH_TAG_END = '>';

    protected final StringBuilder color = new StringBuilder();

    public OpenColor(final Stack<String> tags, final StringWriter writer) {
      super(tags, writer);
    }

    @Override
    public State next(int ch) {
      if (ch == CH_TAG_END) {
        final String colorName = color.toString();
        // check valid
        tags.push(colorName);
        logger.debug("Tags: {}", tags);
        final String colorCode = colors.get(colorName);
        assertNotNull(colorCode, "Unknown color: " + colorName);
        writer.write(colorCode);
        return new Normal(tags, writer);
      } else {
        color.append((char) ch);
        return this;
      }

    }
  }

  class CloseColor extends State {

    protected static final int CH_TAG_END = '>';

    protected final StringBuilder color = new StringBuilder();

    public CloseColor(final Stack<String> tags, final StringWriter writer) {
      super(tags, writer);
    }

    @Override
    public State next(int ch) {
      if (ch == CH_TAG_END) {
        final String colorName = color.toString();
        // check valid
        final String openColorName = tags.pop();
        logger.debug("Tags: {}", tags);
        assertEquals(openColorName, colorName,
            "The closing tag not matched: " + openColorName + ", " + colorName);
        color.delete(0, color.length());
        if (tags.isEmpty()) {
          writer.write(resetCode);
        } else {
          writer.write(colors.get(tags.peek()));
        }
        return new Normal(tags, writer);
      } else {
        color.append((char) ch);
        return this;
      }
    }
  }

  /**
   * Format tagged message with ansi color.
   *
   * @param message message to format
   *
   * @return text with ansi code
   */
  public String format(final String message) {
    if (null == message) {
      return null;
    }
    final StringReader reader = new StringReader(message);

    int ch = 0;
    State state = new Normal(new Stack<>(), new StringWriter());
    try {
      while (0 <= (ch = reader.read())) {
        logger.trace("Char: {}", (char) ch);
        final State old = state;
        state = state.next(ch);
        if (old != state) {
          logger.debug("{} -> {}", old, state);
        }
      }
    } catch (final IOException ex) {
      throw new IllegalStateException();
    }
    logger.debug("State: {}", state);
    logger.debug("Tags: {}", state.getTags());
    assertTrue(state instanceof Normal, "Expression is invalid[" + state + ": " + message);
    assertTrue(state.getTags().isEmpty(),
        "The closing tag[" + state.getTags() + "] missed: " + message);
    return state.getWriter().toString();
  }

  /**
   * Escape all special char in {@code str}.
   *
   * @param str string to escape
   *
   * @return escaped string
   */
  public String escape(final String str) {
    final StringBuilder writer = new StringBuilder();
    final StringReader reader = new StringReader(str);
    int ch = 0;
    try {
      while (0 < (ch = reader.read())) {
        if (ch == '<' | ch == '!') {
          writer.append('!');
        }
        writer.append((char) ch);
      }
    } catch (final IOException e) {
      throw new IllegalStateException(e);
    }
    return writer.toString();
  }

  protected String bind(final String messageId, Object... args) {
    return Messages.bind(
        messageId,
        Arrays.stream(args).map(arg -> escape((null == arg) ? null : arg.toString())).toArray());
  }

  /**
   * Print message with {@code messageId} and {@code args}.
   *
   * @param messageId message id
   * @param args argument
   */
  public void print(final String messageId, Object...args) {
    out.print(this.format(bind(messageId, args)));
  }

  /**
   * Insert empty line.
   */
  public void println() {
    out.println();
  }

  /**
   * Print message with {@code messageId} and {@code args} and append line ending.
   *
   * @param messageId message id
   * @param args argument
   */
  public void println(final String messageId, Object... args) {
    out.println(this.format(bind(messageId, args)));
  }

  @Override
  public void flush() {
    out.flush();
  }
}
