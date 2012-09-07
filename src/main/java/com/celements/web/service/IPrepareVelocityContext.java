package com.celements.web.service;

import java.util.List;

import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.ComponentRole;

import com.xpn.xwiki.XWikiContext;

@ComponentRole
public interface IPrepareVelocityContext {

  public void prepareVelocityContext(VelocityContext vcontext);

  /**
   * @param context
   * 
   * @deprecated instead use prepareVelocityContext(VelocityContext)
   */
  @Deprecated
  public void prepareVelocityContext(XWikiContext context);

  public String getVelocityName();

  public int showRightPanels();

  public int showLeftPanels();

  public List<String> getRightPanels();

  public List<String> getLeftPanels();

}
