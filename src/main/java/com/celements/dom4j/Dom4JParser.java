package com.celements.dom4j;

import static com.celements.common.lambda.LambdaExceptionUtil.*;
import static java.nio.charset.StandardCharsets.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Optional;
import java.util.function.Predicate;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import io.sf.carte.doc.dom4j.XHTMLDocument;
import io.sf.carte.doc.dom4j.XHTMLDocumentFactory;
import io.sf.carte.doc.xml.dtd.DefaultEntityResolver;

public class Dom4JParser<D extends Document> {

  private final Class<D> docType;
  private final DocumentFactory factory;
  private boolean disableDTDs = true;

  public static Dom4JParser<Document> createParser(DocumentFactory factory) {
    return new Dom4JParser<>(Document.class, factory);
  }

  public static Dom4JParser<XHTMLDocument> createXHtmlParser() {
    return new Dom4JParser<>(XHTMLDocument.class, XHTMLDocumentFactory.getInstance());
  }

  private Dom4JParser(Class<D> docType, DocumentFactory factory) {
    this.docType = docType;
    this.factory = factory;
  }

  /**
   * DTDs is disabled by default for security reasons. This allows enabling it if actually needed.
   *
   * @see squid:S2755 - XML parsers should not be vulnerable to XXE attacks
   */
  public Dom4JParser<D> allowDTDs() {
    disableDTDs = false;
    return this;
  }

  public D readDocument(String xml) throws IOException {
    try (Reader in = new StringReader(xml)) {
      SAXReader reader = new SAXReader(factory);
      reader.setEntityResolver(new DefaultEntityResolver());
      if (disableDTDs) {
        reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      }
      return docType.cast(reader.read(new InputSource(in)));
    } catch (DocumentException | SAXException exc) {
      throw new IOException(exc);
    }
  }

  public String writeXML(D document) throws IOException {
    return writeXML(document, new OutputFormat("", false, UTF_8.name()));
  }

  public String writeXML(D document, OutputFormat format)
      throws IOException {
    try (Writer out = new StringWriter()) {
      XMLWriter writer = new XMLWriter(out, format);
      writer.write(document);
      return out.toString();
    }
  }

  public Optional<String> readAndExecute(String xml, Predicate<D> executable) throws IOException {
    return Optional.ofNullable(readDocument(xml))
        .map(docType::cast)
        .filter(executable)
        .map(rethrowFunction(this::writeXML));
  }

}
