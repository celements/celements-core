package com.celements.model.classes;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.classes.fields.ClassField;
import com.google.common.base.Optional;

@ComponentRole
public interface ClassDefinition {

  public static final String CFG_SRC_KEY = "celements.classdefinition.blacklist";

  /**
   * @return the name of the component and class definition, used for blacklisting
   */
  public String getName();

  /**
   * @return the document reference on which the class is defined, using current wiki
   */
  @NotNull
  public DocumentReference getClassRef();

  /**
   * @param wikiRef
   * @return the document reference on which the class is defined, using given wiki
   */
  @NotNull
  public DocumentReference getClassRef(@NotNull WikiReference wikiRef);

  /**
   * @return true if the class definition is blacklisted
   */
  public boolean isBlacklisted();

  /**
   * @return true if the class is mapped internally (hibernate mapping)
   */
  public boolean isInternalMapping();

  /**
   * @return a list of all fields defining this class
   */
  @NotNull
  public List<ClassField<?>> getFields();

  /**
   * @param name
   * @return the defined field for the given name
   */
  @NotNull
  public Optional<ClassField<?>> getField(@NotNull String name);

  /**
   * @param name
   * @param token
   * @return the defined field for the given name
   */
  @NotNull
  public <T> Optional<ClassField<T>> getField(@NotNull String name, @NotNull Class<T> token);

}
