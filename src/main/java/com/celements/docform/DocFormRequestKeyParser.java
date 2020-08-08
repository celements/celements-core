package com.celements.docform;

import static com.celements.docform.DocFormRequestKey.*;
import static com.celements.model.util.References.*;
import static com.celements.web.classes.oldcore.XWikiDocumentClass.*;
import static com.google.common.base.Strings.*;
import static com.google.common.collect.ImmutableList.*;
import static com.google.common.collect.ImmutableSet.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ImmutableDocumentReference;

import com.celements.model.classes.ClassDefinition;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.util.EntityTypeUtil;
import com.celements.model.util.ModelUtils;
import com.celements.web.classes.oldcore.XWikiDocumentClass;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.xpn.xwiki.web.Utils;

public class DocFormRequestKeyParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(DocFormRequestKeyParser.class);

  public static final String KEY_DELIM = "_";

  private static final Pattern PATTERN_FULLNAME = Pattern.compile(EntityTypeUtil.REGEX_DOC);
  private static final Pattern PATTERN_OBJNB = Pattern.compile("\\^?-?(\\d)*");

  private ImmutableSet<String> allowedDocFields;

  /**
   * used if no fullName is provided in the parsed string keys
   */
  private final ImmutableDocumentReference defaultDocRef;

  public DocFormRequestKeyParser(DocumentReference defaultDocRef) {
    this.defaultDocRef = cloneRef(defaultDocRef, ImmutableDocumentReference.class);
  }

  /**
   * Parses given map to {@link DocFormRequestParam} objects. See {@link #parse(String)}.
   */
  public List<DocFormRequestParam> parseParameterMap(Map<String, ?> map) {
    return map.keySet().stream()
        .filter(key -> !nullToEmpty(key).trim().isEmpty())
        .map(key -> {
          try {
            return parse(key).orElse(null);
          } catch (DocFormRequestParseException exc) {
            LOGGER.warn("unable to parse {}", key, exc);
            return null;
          }
        }).filter(Objects::nonNull)
        .map(key -> new DocFormRequestParam(key, map.get(key.getKeyString())))
        .sorted()
        .collect(toImmutableList());
  }

  /**
   * Parses a given key string to a {@link DocFormRequestKey} object.
   *
   * @param key
   *          syntax: "[{fullName}_]{className}_[-|^]{objNb}_{fieldName}" whereas fullName
   *          is optional and objNb can be preceded by "-" to create or "^" to delete the
   *          object
   * @return the parsed key if it matches the expected pattern
   * @throws DocFormRequestParseException
   *           if the key matches the expected pattern but cannot be parsed
   */
  public Optional<DocFormRequestKey> parse(String key) throws DocFormRequestParseException {
    List<String> keyParts = new ArrayList<>(Splitter.on(KEY_DELIM)
        .trimResults().omitEmptyStrings().splitToList(key));
    try {
      DocumentReference docRef = parseDocRefIfPresent(keyParts).orElse(defaultDocRef);
      if (isAllowedDocField(asFieldName(keyParts))) {
        return Optional.of(createDocFieldKey(key, docRef, asFieldName(keyParts)));
      } else if (isObjKey(keyParts)) {
        ClassReference classRef = new ClassReference(keyParts.remove(0));
        String objNbKeyPart = keyParts.remove(0);
        Integer objNb = parseObjNb(objNbKeyPart).orElseThrow(parseException(key));
        if (objNbKeyPart.startsWith("^")) {
          return Optional.of(createObjRemoveKey(key, docRef, classRef, objNb));
        } else {
          return Optional.of(createObjFieldKey(key, docRef, classRef, objNb,
              asFieldName(keyParts)));
        }
      } else {
        LOGGER.info("parse: skip key [{}]", key);
        return Optional.empty();
      }
    } catch (IllegalArgumentException iae) {
      throw new DocFormRequestParseException(key, iae);
    }
  }

  private Optional<DocumentReference> parseDocRefIfPresent(List<String> keyParts) {
    if ((keyParts.size() > 1) && PATTERN_FULLNAME.matcher(keyParts.get(0)).matches()) {
      String docKeyPart = keyParts.remove(0);
      if (isAllowedDocField(asFieldName(keyParts)) || isObjKey(keyParts)) {
        return Optional.of(getModelUtils().resolveRef(docKeyPart, DocumentReference.class));
      } else {
        keyParts.add(0, docKeyPart);
      }
    }
    return Optional.empty();
  }

  private boolean isAllowedDocField(String key) {
    if (allowedDocFields == null) {
      allowedDocFields = getXDocClassDef().getFields().stream()
          .filter(ImmutableSet.of(FIELD_TITLE, FIELD_CONTENT)::contains)
          .map(ClassField::getName)
          .collect(toImmutableSet());
    }
    return allowedDocFields.contains(key);
  }

  private boolean isObjKey(List<String> keyParts) {
    return (keyParts.size() > 1)
        && PATTERN_FULLNAME.matcher(keyParts.get(0)).matches()
        && PATTERN_OBJNB.matcher(keyParts.get(1)).matches();
  }

  private Optional<Integer> parseObjNb(String objNbKeyPart) {
    return Optional.ofNullable(Ints.tryParse(objNbKeyPart.replace("^", "")));
  }

  private String asFieldName(List<String> keyParts) {
    return StringUtils.join(keyParts.iterator(), KEY_DELIM);
  }

  private ClassDefinition getXDocClassDef() {
    return Utils.getComponent(ClassDefinition.class, XWikiDocumentClass.CLASS_DEF_HINT);
  }

  private ModelUtils getModelUtils() {
    return Utils.getComponent(ModelUtils.class);
  }

  private static Supplier<DocFormRequestParseException> parseException(String key) {
    return () -> new DocFormRequestParseException(key);
  }

  public static class DocFormRequestParseException extends Exception {

    private static final long serialVersionUID = 7960168302772273291L;

    private DocFormRequestParseException(String key) {
      super(key);
    }

    private DocFormRequestParseException(String key, Throwable cause) {
      super(key, cause);
    }

  }

}
