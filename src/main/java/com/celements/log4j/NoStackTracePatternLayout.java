package com.celements.log4j;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.xwiki.configuration.ConfigurationSource;

import com.xpn.xwiki.web.Utils;

/**
 * this pattern suppresses the stack trace for all levels below the one configured. defaults to
 * {@link Level#ERROR}
 */
@NotThreadSafe
public class NoStackTracePatternLayout extends PatternLayout {

  private static final String CFG_SRC_KEY_LEVEL = "celements.logging.noStackTrace.level";

  /**
   * sadly, {@link #ignoresThrowable()} does not give us the {@link LoggingEvent}, so we have to
   * store the {@link Level} from the last {@link #format(LoggingEvent)} call. This works because
   * all log4j appenders call the latter first. see e.g. {@link WriterAppender}
   */
  private Level level = null;

  @Override
  public String format(LoggingEvent event) {
    level = event.getLevel();
    return super.format(event);
  }

  /**
   * NoStackTracePatterLayout ignores throwable, but instead uses this method to suppress stack
   * traces which are printed by log4j appenders. see e.g. {@link WriterAppender}
   */
  @Override
  public boolean ignoresThrowable() {
    return (level == null) || level.isGreaterOrEqual(getNoStackTraceLevel());
  }

  private Level getNoStackTraceLevel() {
    String levelName = getCfgSrc().getProperty(CFG_SRC_KEY_LEVEL);
    return Level.toLevel(levelName, Level.ERROR);
  }

  private ConfigurationSource getCfgSrc() {
    return Utils.getComponent(ConfigurationSource.class);
  }

}
