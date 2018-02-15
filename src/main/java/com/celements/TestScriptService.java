package com.celements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.store.hibernate.HibernateSessionFactory;

@Component("test")
public class TestScriptService implements ScriptService {

  @Requirement
  private IWebUtilsService webUtils;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private HibernateSessionFactory sessionFactory;

  public List<String> getMappings() {
    if (webUtils.isSuperAdminUser()) {
      List<String> ret = new ArrayList<>();
      for (Iterator<PersistentClass> iter = getHibConfig().getClassMappings(); iter.hasNext();) {
        PersistentClass mapping = iter.next();
        ret.add(getInfo(mapping));
      }
      return ret;
    }
    return null;
  }

  public String getClassPrimaryKeyName(DocumentReference docRef) throws DocumentNotExistsException {
    if (webUtils.isSuperAdminUser()) {
      PersistentClass mapping = getMapping(docRef);
      return getInfo(mapping);
    }
    return null;
  }

  private String getInfo(PersistentClass mapping) {
    String propStr = "[";
    for (Iterator<Property> iter = mapping.getPropertyIterator(); iter.hasNext();) {
      propStr += iter.next().getName() + " ";
    }
    propStr += "]";
    return mapping.getEntityName() + " - " + mapping.getTable().getName() + " - " + getId1(mapping)
        + " " + getId2(mapping) + " - " + propStr;
  }

  private String getId1(PersistentClass mapping) {
    String idStr = "{";
    Property idProperty = mapping.getIdentifierProperty();
    if (idProperty != null) {
      idStr += idProperty.getName() + " of " + idProperty.getType().getName() + " : ";
      for (Iterator<Column> iter = idProperty.getColumnIterator(); iter.hasNext();) {
        idStr += iter.next().getName() + " ";
      }
    }
    idStr += "}";
    return idStr;
  }

  private String getId2(PersistentClass mapping) {
    String idStr = "{";
    KeyValue identifier = mapping.getIdentifier();
    if (identifier instanceof SimpleValue) {
      SimpleValue idProperty = (SimpleValue) identifier;
      idStr += idProperty.getTypeName() + " of " + idProperty.getType().getName() + " : ";
      for (Iterator<Column> iter = idProperty.getColumnIterator(); iter.hasNext();) {
        idStr += iter.next().getName() + " ";
      }
    } else {
      idStr += identifier;
    }
    idStr += "}";
    return idStr;
  }

  private PersistentClass getMapping(DocumentReference docRef) throws DocumentNotExistsException {
    BaseClass bClass = modelAccess.getDocument(docRef).getXClass();
    PersistentClass mapping = getHibConfig().getClassMapping(bClass.getName());
    if (mapping != null) {
      return mapping;
    } else {
      throw new IllegalArgumentException("no mapping");
    }
  }

  private Configuration getHibConfig() {
    return sessionFactory.getConfiguration();
  }

}
