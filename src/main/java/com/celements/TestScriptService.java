package com.celements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
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
    Property idProperty = mapping.getIdentifierProperty();
    String idStr = "<";
    if (idProperty != null) {
      idStr += idProperty.getName() + " (" + idProperty.getType().getName() + ") : ";
      for (Iterator<Column> iter = idProperty.getColumnIterator(); iter.hasNext();) {
        idStr += iter.next().getName() + " ";
      }
    }
    idStr += ">";
    String propStr = "[";
    for (Iterator<Property> iter = mapping.getPropertyIterator(); iter.hasNext();) {
      propStr += iter.next().getName() + " ";
    }
    propStr += "]";
    return mapping.getEntityName() + " - " + mapping.getTable().getName() + " - " + idStr + " - "
        + propStr;
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
