package com.celements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

  public String getClassPrimaryKeyName(DocumentReference docRef) throws DocumentNotExistsException {
    if (webUtils.isSuperAdminUser()) {
      PersistentClass mapping = getMapping(docRef);
      return mapping.getTable().getName() + "_" + mapping.getIdentifierProperty().getName();
    }
    return null;
  }

  public List<String> getClassPropertyNames(DocumentReference docRef)
      throws DocumentNotExistsException {
    if (webUtils.isSuperAdminUser()) {
      List<String> ret = new ArrayList<>();
      @SuppressWarnings("unchecked")
      Iterator<Property> propertyIter = getMapping(docRef).getPropertyIterator();
      while (propertyIter.hasNext()) {
        ret.add(propertyIter.next().getName());
      }
      return ret;
    }
    return null;
  }

  private PersistentClass getMapping(DocumentReference docRef) throws DocumentNotExistsException {
    BaseClass bClass = modelAccess.getDocument(docRef).getXClass();
    PersistentClass mapping = sessionFactory.getConfiguration().getClassMapping(bClass.getName());
    if (mapping != null) {
      return mapping;
    } else {
      throw new IllegalArgumentException("no mapping");
    }
  }

}
