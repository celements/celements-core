package com.celements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
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

  public List<List<String>> getMappings() {
    if (webUtils.isSuperAdminUser()) {
      List<List<String>> ret = new ArrayList<>();
      for (Iterator<?> iter = getHibConfig().getClassMappings(); iter.hasNext();) {
        ret.add(getInfo((PersistentClass) iter.next()));
      }
      return ret;
    }
    return null;
  }

  public List<String> getClassPrimaryKeyName(DocumentReference docRef)
      throws DocumentNotExistsException {
    if (webUtils.isSuperAdminUser()) {
      PersistentClass mapping = getMapping(docRef);
      return getInfo(mapping);
    }
    return null;
  }

  private List<String> getInfo(PersistentClass mapping) {
    List<String> ret = new ArrayList<>();
    ret.add(mapping.getEntityName());
    ret.add(mapping.getTable().getName());
    if (mapping.getIdentifier() instanceof SimpleValue) {
      SimpleValue identifier = (SimpleValue) mapping.getIdentifier();
      ret.add(identifier.getType().getName());
      for (Iterator<?> iter = identifier.getColumnIterator(); iter.hasNext();) {
        ret.add(((Column) iter.next()).getName());
      }
    }
    return ret;
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
