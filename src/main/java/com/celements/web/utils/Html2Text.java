/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.web.utils;

import java.io.IOException;
import java.io.Reader;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class Html2Text extends HTMLEditorKit.ParserCallback {

  private StringBuffer s;
  private boolean tanglingReturn = false;

  public void parse(Reader in) throws IOException {
    s = new StringBuffer();
    ParserDelegator delegator = new ParserDelegator();
    // CHECK - TRUE ignores charset directive
    delegator.parse(in, this, Boolean.TRUE);
  }

  @Override
  public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
    if (t.breaksFlow()) {
      tanglingReturn = true;
    }
  }

  @Override
  public void handleEndTag(HTML.Tag t, int pos) {
    if (t.breaksFlow()) {
      tanglingReturn = true;
    }
  }

  @Override
  public void handleText(char[] text, int pos) {
    if ((s.length() > 0) && tanglingReturn) {
      s.append("\r\n");
    }
    s.append(new String(text));
    tanglingReturn = false;
  }

  public String getText() {
    if (s == null) {
      return "";
    }
    return s.toString();
  }

  void injectStringBuffer(StringBuffer s) {
    this.s = s;
  }

  void injectTanglingReturn(boolean tanglingReturn) {
    this.tanglingReturn = tanglingReturn;
  }

}
