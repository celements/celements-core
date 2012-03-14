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
package com.celements.web.sajson;

import static org.easymock.EasyMock.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class LexicalParserTest {

  private IEventHandler<IGenericLiteral> mockEventHandler;
  private LexicalParser<IGenericLiteral> lexicalParser;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    mockEventHandler = createStrictMock(IEventHandler.class);
    lexicalParser = new LexicalParser<IGenericLiteral>(ERulesLiteral.RULES_ARRAY,
        mockEventHandler);
  }

  @Test
  public void testBooleanEvent_true() {
    mockEventHandler.openEvent(ERulesLiteral.RULES_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.RULE_DICT);
    mockEventHandler.openEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.readPropertyKey("isDeleted");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.booleanEvent(true);
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.closeEvent(ERulesLiteral.RULE_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.RULES_ARRAY);
    replayAll();
    lexicalParser.initEvent();
    lexicalParser.openArrayEvent();
    //rule
    lexicalParser.openDictionaryEvent();
    //isDeleted
    lexicalParser.openPropertyEvent("isDeleted");
    lexicalParser.booleanEvent(true);
    lexicalParser.closePropertyEvent();
    // end rule
    lexicalParser.closeDictionaryEvent();
    // end rules
    lexicalParser.closeArrayEvent();
    lexicalParser.finishEvent();
    verifyAll();
  }

  @Test
  public void testBooleanEvent_false() {
    mockEventHandler.openEvent(ERulesLiteral.RULES_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.RULE_DICT);
    mockEventHandler.openEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.readPropertyKey("isDeleted");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.booleanEvent(false);
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.closeEvent(ERulesLiteral.RULE_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.RULES_ARRAY);
    replayAll();
    lexicalParser.initEvent();
    lexicalParser.openArrayEvent();
    //rule
    lexicalParser.openDictionaryEvent();
    //isDeleted
    lexicalParser.openPropertyEvent("isDeleted");
    lexicalParser.booleanEvent(false);
    lexicalParser.closePropertyEvent();
    // end rule
    lexicalParser.closeDictionaryEvent();
    // end rules
    lexicalParser.closeArrayEvent();
    lexicalParser.finishEvent();
    verifyAll();
  }

  @Test
  public void testComplicatedRulesJSON_firstDictionaryOrder() {
    mockEventHandler.openEvent(ERulesLiteral.RULES_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.RULE_DICT);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.readPropertyKey("actions");
    mockEventHandler.openEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.openEvent(ERulesLiteral.CON_OR_ACT_ATTRIBUTE);
    mockEventHandler.readPropertyKey("type");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("addTo");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.CON_OR_ACT_ATTRIBUTE);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.readPropertyKey("conditions");
    mockEventHandler.openEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.openEvent(ERulesLiteral.CON_OR_ACT_ATTRIBUTE);
    mockEventHandler.readPropertyKey("type");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("titleCondition");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.CON_OR_ACT_ATTRIBUTE);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.openEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.readPropertyKey("name");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("Rule1");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.openEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.readPropertyKey("type");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("any");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.closeEvent(ERulesLiteral.RULE_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.RULES_ARRAY);
    replayAll();
    lexicalParser.initEvent();
    lexicalParser.openArrayEvent();
    //rule
    lexicalParser.openDictionaryEvent();
    //actions
    lexicalParser.openPropertyEvent("actions");
    lexicalParser.openArrayEvent();
    //action
    lexicalParser.openDictionaryEvent();
    lexicalParser.openPropertyEvent("type");
    lexicalParser.stringEvent("addTo");
    lexicalParser.closePropertyEvent();
    lexicalParser.closeDictionaryEvent();
    //end action
    lexicalParser.closeArrayEvent();
    //end actions
    lexicalParser.closePropertyEvent();
    //conditions
    lexicalParser.openPropertyEvent("conditions");
    lexicalParser.openArrayEvent();
    //condition
    lexicalParser.openDictionaryEvent();
    lexicalParser.openPropertyEvent("type");
    lexicalParser.stringEvent("titleCondition");
    lexicalParser.closePropertyEvent();
    lexicalParser.closeDictionaryEvent();
    //end condition
    lexicalParser.closeArrayEvent();
    //end conditions
    lexicalParser.closePropertyEvent();
    //rulesname
    lexicalParser.openPropertyEvent("name");
    lexicalParser.stringEvent("Rule1");
    lexicalParser.closePropertyEvent();
    //rule type
    lexicalParser.openPropertyEvent("type");
    lexicalParser.stringEvent("any");
    lexicalParser.closePropertyEvent();
    // end rule
    lexicalParser.closeDictionaryEvent();
    // end rules
    lexicalParser.closeArrayEvent();
    lexicalParser.finishEvent();
    verifyAll();
  }

  @Test
  public void testComplicatedRulesJSON_secondDictionaryOrder() {
    mockEventHandler.openEvent(ERulesLiteral.RULES_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.RULE_DICT);
    mockEventHandler.openEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.readPropertyKey("type");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("any");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.readPropertyKey("conditions");
    mockEventHandler.openEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.openEvent(ERulesLiteral.CON_OR_ACT_ATTRIBUTE);
    mockEventHandler.readPropertyKey("type");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("titleCondition");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.CON_OR_ACT_ATTRIBUTE);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.readPropertyKey("actions");
    mockEventHandler.openEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.openEvent(ERulesLiteral.CON_OR_ACT_ATTRIBUTE);
    mockEventHandler.readPropertyKey("type");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("addTo");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.CON_OR_ACT_ATTRIBUTE);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.openEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.readPropertyKey("name");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("Rule1");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.RULE_ATTRIBUTE);
    mockEventHandler.closeEvent(ERulesLiteral.RULE_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.RULES_ARRAY);
    replayAll();
    lexicalParser.initEvent();
    lexicalParser.openArrayEvent();
    //rule
    lexicalParser.openDictionaryEvent();
    //rule type
    lexicalParser.openPropertyEvent("type");
    lexicalParser.stringEvent("any");
    lexicalParser.closePropertyEvent();
    //conditions
    lexicalParser.openPropertyEvent("conditions");
    lexicalParser.openArrayEvent();
    //condition
    lexicalParser.openDictionaryEvent();
    lexicalParser.openPropertyEvent("type");
    lexicalParser.stringEvent("titleCondition");
    lexicalParser.closePropertyEvent();
    lexicalParser.closeDictionaryEvent();
    //end condition
    lexicalParser.closeArrayEvent();
    //end conditions
    lexicalParser.closePropertyEvent();
    //actions
    lexicalParser.openPropertyEvent("actions");
    lexicalParser.openArrayEvent();
    //action
    lexicalParser.openDictionaryEvent();
    lexicalParser.openPropertyEvent("type");
    lexicalParser.stringEvent("addTo");
    lexicalParser.closePropertyEvent();
    lexicalParser.closeDictionaryEvent();
    //end action
    lexicalParser.closeArrayEvent();
    //end actions
    lexicalParser.closePropertyEvent();
    //rulesname
    lexicalParser.openPropertyEvent("name");
    lexicalParser.stringEvent("Rule1");
    lexicalParser.closePropertyEvent();
    // end rule
    lexicalParser.closeDictionaryEvent();
    // end rules
    lexicalParser.closeArrayEvent();
    lexicalParser.finishEvent();
    verifyAll();
  }


  private void replayAll(Object ... mocks) {
    replay(mockEventHandler);
    replay(mocks);
  }

  private void verifyAll(Object ... mocks) {
    verify(mockEventHandler);
    verify(mocks);
  }

  private enum ERulesLiteral implements IGenericLiteral {
    OBJECT_VALUE(ECommand.VALUE_COMMAND),
    RULE_ATTRIBUTE(OBJECT_VALUE, "name", "*"),
    CON_OR_ACT_ATTRIBUTE(OBJECT_VALUE, "type"),
    CONDITION_OR_ACTION_DICT(ECommand.DICTIONARY_COMMAND, CON_OR_ACT_ATTRIBUTE),
    CONDITIONS_OR_ACTIONS_ARRAY(ECommand.ARRAY_COMMAND, CONDITION_OR_ACTION_DICT),
    CONDITION_OR_ACTION_PROPERTY(CONDITIONS_OR_ACTIONS_ARRAY, "conditions", "actions"),
    RULE_DICT(ECommand.DICTIONARY_COMMAND, CONDITION_OR_ACTION_PROPERTY, RULE_ATTRIBUTE),
    RULES_ARRAY(ECommand.ARRAY_COMMAND, RULE_DICT);
    
    
    private ERulesLiteral[] literals;
    private ECommand command;
    private int nextLiteral = 0;
    private Map<String, ERulesLiteral> propertyNameMap;
    private String[] names;

    private ERulesLiteral(ECommand command, ERulesLiteral... literals) {
      this.literals = literals;
      this.command = command;
    }

    private ERulesLiteral(ERulesLiteral literal, String... names) {
      this.command = ECommand.PROPERTY_COMMAND;
      this.literals = new ERulesLiteral[] {literal};
      this.names = names;
    }

    public ECommand getCommand() {
      return command;
    }

    public String[] getNames() {
      return names;
    }

    public IGenericLiteral getNextLiteral() {
      nextLiteral = nextLiteral + 1;
      if (nextLiteral > literals.length) {
        return null;
      }
      return literals[nextLiteral - 1];
    }

    public IGenericLiteral getFirstLiteral() {
      nextLiteral = 1;
      return literals[0];
    }

    public IGenericLiteral getPropertyLiteralForKey(String key,
        IGenericLiteral placeholder) {
      nextLiteral = 0; // properties in dictionary may occur multiple times
                      // (once for each name) and are optional
      ERulesLiteral propertyLiteral = getPropertyNameMap().get(key);
      if (propertyLiteral == null) {
        propertyLiteral = getPropertyNameMap().get("*");
      }
      return propertyLiteral;
    }

    private Map<String, ERulesLiteral> getPropertyNameMap() {
      if (propertyNameMap == null) {
        propertyNameMap = new HashMap<String, ERulesLiteral>();
        for (ERulesLiteral literal : literals) {
          checkPropertyLiteral(literal);
          for (String key : literal.getNames()) {
            if (!propertyNameMap.containsKey(key)) {
              propertyNameMap.put(key, literal);
            } else {
              throw new IllegalStateException("duplicate property literal for name ["
                  + key + "] in dictionary [" + this + "].");
            }
          }
        }
      }
      return propertyNameMap;
    }

    private void checkPropertyLiteral(ERulesLiteral literal) {
      if (literal.getCommand() != ECommand.PROPERTY_COMMAND) {
        throw new IllegalStateException("expecting only property literal inside"
            + " dictionary but found [" + literal + "].");
      }
      if (literal.getNames() == null) {
        throw new IllegalStateException("missing property names for property literal ["
            + literal + "]");
      }
    }

  }

}
