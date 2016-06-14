package com.celements.model.classes;

import java.util.Arrays;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;

import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.number.IntField;

import groovy.lang.Singleton;

@Singleton
@Component(TestClassDefinition.NAME)
public class TestClassDefinition implements ClassDefinition {

  public static final String NAME = "TestClass";

  public static final DocumentReference CLASS_REF = new DocumentReference("", "classes", "test");

  public static final ClassField<String> FIELD_MY_STRING = getFieldMyString();
  public static final ClassField<Integer> FIELD_MY_INT = getFieldMyInt();

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public DocumentReference getClassRef() {
    return CLASS_REF;
  }

  @Override
  public boolean isInternalMapping() {
    return true;
  }

  @Override
  public List<ClassField<?>> getFields() {
    return Arrays.<ClassField<?>>asList(FIELD_MY_STRING, FIELD_MY_INT);
  }

  private static ClassField<String> getFieldMyString() {
    StringField ret = new StringField(CLASS_REF, "myString");
    ret.setSize(30);
    return ret;
  }

  private static ClassField<Integer> getFieldMyInt() {
    IntField ret = new IntField(CLASS_REF, "myInt");
    ret.setSize(30);
    return ret;
  }

}
