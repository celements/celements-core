package com.celements.dom4j;

import static java.text.MessageFormat.format;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.ExceptionAsserter;
import com.google.common.collect.Streams;

public class Dom4JParserTest extends AbstractComponentTest {

  private Dom4JParser<Document> parser;

  private String xmlContent = "<a>A</a><b>B</b><c>C</c>";
  private String xml = format("<root>{0}</root>", xmlContent);

  @Before
  public void prepare() throws Exception {
    parser = Dom4JParser.createParser(DocumentFactory.getInstance());
  }

  @Test
  public void test_writeDoc() throws Exception {
    Optional<String> ret = parser.readAndExecute(xml, document -> {
      assertDomTree(document);
      return Stream.of(document);
    });
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xml, ret.orElse(""));
  }

  @Test
  public void test_writeRoot() throws Exception {
    Optional<String> ret = parser.readAndExecute(xml, document -> {
      assertDomTree(document);
      return Stream.of(document.getRootElement());
    });
    assertEquals(xml, ret.orElse(""));
  }

  @Test
  public void test_noWrite() throws Exception {
    Optional<String> ret = parser.readAndExecute(xml, document -> {
      assertDomTree(document);
      return Stream.empty();
    });
    assertFalse(ret.isPresent());
  }

  @Test
  public void test_subWrite() throws Exception {
    Optional<String> ret = parser.readAndExecute(xml, document -> {
      assertDomTree(document);
      return Streams.stream(document.getRootElement().elementIterator());
    });
    assertEquals(xmlContent, ret.orElse(""));
  }

  @Test
  public void test_disallowDTD() throws Exception {
    xml = "<!DOCTYPE note SYSTEM \"note.dtd\">" + xml;
    new ExceptionAsserter<IOException>(IOException.class) {

      @Override
      protected void execute() throws Exception {
        parser.readAndExecute(xml, Stream::of);
      }
    }.evaluate();
  }

  @Test
  public void test_allowDTD() throws Exception {
    xml = "<!DOCTYPE note SYSTEM \"note.dtd\">" + xml;
    Optional<String> ret = parser.allowDTDs().readAndExecute(xml, Stream::of);
    assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xml, ret.orElse(""));
  }

  private void assertDomTree(Document document) {
    assertEquals("root", document.getRootElement().getName());
    List<Element> elems = Streams.stream(document.getRootElement().elementIterator())
        .collect(Collectors.toList());
    assertEquals(3, elems.size());
    assertEquals("a", elems.get(0).getName());
    assertEquals("A", elems.get(0).getText());
    assertEquals("b", elems.get(1).getName());
    assertEquals("B", elems.get(1).getText());
    assertEquals("c", elems.get(2).getName());
    assertEquals("C", elems.get(2).getText());
  }

}
