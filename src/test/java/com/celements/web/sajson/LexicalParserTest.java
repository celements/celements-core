package com.celements.web.sajson;

import static org.easymock.EasyMock.*;

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
  public void testComplicatedRulesJSON_firstDictionaryOrder() {
    mockEventHandler.openEvent(ERulesLiteral.RULES_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.RULE_DICT);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.readPropertyKey("actions");
    mockEventHandler.openEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.readPropertyKey("actionType");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("addTo");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.readPropertyKey("conditions");
    mockEventHandler.openEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.readPropertyKey("conditionType");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("titleCondition");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.readPropertyKey("rulesname");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("Rule1");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.readPropertyKey("type");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("any");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_PROPERTY);
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
    lexicalParser.openPropertyEvent("actionType");
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
    lexicalParser.openPropertyEvent("conditionType");
    lexicalParser.stringEvent("titleCondition");
    lexicalParser.closePropertyEvent();
    lexicalParser.closeDictionaryEvent();
    //end condition
    lexicalParser.closeArrayEvent();
    //end conditions
    lexicalParser.closePropertyEvent();
    //rulesname
    lexicalParser.openPropertyEvent("rulesname");
    lexicalParser.stringEvent("Rule1");
    lexicalParser.closePropertyEvent();
    //rule type
    lexicalParser.openPropertyEvent("type");
    lexicalParser.stringEvent("any");
    lexicalParser.closePropertyEvent();
    lexicalParser.closeDictionaryEvent();
    // end rule
    lexicalParser.closeArrayEvent();
    // end rules
    lexicalParser.finishEvent();
    verifyAll();
  }

  @Test
  public void testComplicatedRulesJSON_secondDictionaryOrder() {
    mockEventHandler.openEvent(ERulesLiteral.RULES_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.RULE_DICT);
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.readPropertyKey("type");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("any");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.readPropertyKey("conditions");
    mockEventHandler.openEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.readPropertyKey("conditionType");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("titleCondition");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.readPropertyKey("actions");
    mockEventHandler.openEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.openEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.readPropertyKey("actionType");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("addTo");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_DICT);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITIONS_OR_ACTIONS_ARRAY);
    mockEventHandler.closeEvent(ERulesLiteral.CONDITION_OR_ACTION_PROPERTY);
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_PROPERTY);
    mockEventHandler.readPropertyKey("rulesname");
    mockEventHandler.openEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.stringEvent("Rule1");
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_VALUE);
    mockEventHandler.closeEvent(ERulesLiteral.OBJECT_PROPERTY);
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
    lexicalParser.openPropertyEvent("actionType");
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
    lexicalParser.openPropertyEvent("conditionType");
    lexicalParser.stringEvent("titleCondition");
    lexicalParser.closePropertyEvent();
    lexicalParser.closeDictionaryEvent();
    //end condition
    lexicalParser.closeArrayEvent();
    //end conditions
    lexicalParser.closePropertyEvent();
    //rulesname
    lexicalParser.openPropertyEvent("rulesname");
    lexicalParser.stringEvent("Rule1");
    lexicalParser.closePropertyEvent();
    //rule type
    lexicalParser.openPropertyEvent("type");
    lexicalParser.stringEvent("any");
    lexicalParser.closePropertyEvent();
    lexicalParser.closeDictionaryEvent();
    // end rule
    lexicalParser.closeArrayEvent();
    // end rules
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
    OBJECT_PROPERTY(ECommand.PROPERTY_COMMAND, OBJECT_VALUE),
    CONDITION_OR_ACTION_DICT(ECommand.DICTIONARY_COMMAND, OBJECT_PROPERTY),
    CONDITIONS_OR_ACTIONS_ARRAY(ECommand.ARRAY_COMMAND, CONDITION_OR_ACTION_DICT),
    CONDITION_OR_ACTION_PROPERTY(ECommand.PROPERTY_COMMAND, CONDITIONS_OR_ACTIONS_ARRAY),
    RULE_DICT(ECommand.DICTIONARY_COMMAND, CONDITION_OR_ACTION_PROPERTY,
         CONDITION_OR_ACTION_PROPERTY, OBJECT_PROPERTY, OBJECT_PROPERTY),
    RULES_ARRAY(ECommand.ARRAY_COMMAND, RULE_DICT);
    
    
    private ERulesLiteral[] literals;
    private ECommand command;
    private int nextLiteral = 0;

    private ERulesLiteral(ECommand command, ERulesLiteral... literals) {
      this.literals = literals;
      this.command = command;
    }

    public ECommand getCommand() {
      return command;
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
  }

}
