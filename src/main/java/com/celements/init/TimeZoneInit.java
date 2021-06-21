package com.celements.init;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;

import com.celements.common.date.DateUtil;
import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.celements.configuration.ConfigSourceUtils;

@Component(TimeZoneInit.NAME)
public class TimeZoneInit extends AbstractLocalEventListener<Object, Object> {

  public static final String NAME = "TimeZoneInit";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return Arrays.asList(new ApplicationStartedEvent());
  }

  @Override
  protected void onEventInternal(Event event, Object source, Object data) {
    ConfigSourceUtils.getStringProperty("celements.time.zone").toJavaUtil().ifPresent(zone -> {
      try {
        DateUtil.setDefaultZone(ZoneId.of(zone));
        LOGGER.info("init time zone [{}]", zone);
      } catch (DateTimeException exc) {
        LOGGER.warn("failed to init time zone [{}]", zone, exc);
      }
    });
  }

}
