package com.celements.init;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;

import com.celements.common.date.DateUtil;
import com.celements.common.observation.listener.AbstractLocalEventListener;
import com.celements.configuration.CelementsAllPropertiesConfigurationSource;
import com.celements.configuration.ConfigSourceUtils;
import com.google.common.collect.ImmutableList;

@Component(TimeZoneInit.NAME)
public class TimeZoneInit extends AbstractLocalEventListener<Object, Object> {

  public static final String NAME = "TimeZoneInit";

  @Requirement(CelementsAllPropertiesConfigurationSource.NAME)
  private ConfigurationSource cfgSrc;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents() {
    return ImmutableList.of(new ApplicationStartedEvent());
  }

  @Override
  protected void onEventInternal(Event event, Object source, Object data) {
    ConfigSourceUtils.getStringProperty(cfgSrc, "celements.time.zone").toJavaUtil()
        .ifPresent(zone -> {
          try {
            DateUtil.setDefaultZone(ZoneId.of(zone));
            LOGGER.info("init time zone [{}]", zone);
          } catch (DateTimeException exc) {
            LOGGER.warn("failed to init time zone [{}]", zone, exc);
          }
        });
  }

}
