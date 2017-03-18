package com.celements.model.classes.fields;

import static com.google.common.base.Preconditions.*;

import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.model.classes.ClassDefinition;
import com.google.common.base.Optional;
import com.xpn.xwiki.objects.PropertyInterface;

@Immutable
public class ConstructedClassField<T> extends AbstractParentClassField<T> {

  private final ClassReference classRef;
  private final Class<T> type;

  public static class Builder<T> extends AbstractParentClassField.Builder<Builder<T>, T> {

    private final ClassReference classRef;
    private final Class<T> type;

    public Builder(@NotNull ClassReference classRef, @NotNull String name, @NotNull Class<T> type) {
      super(name);
      this.classRef = checkNotNull(classRef);
      this.type = checkNotNull(type);
    }

    @Override
    public ConstructedClassField<T> build() {
      return new ConstructedClassField<>(getThis());
    }

    @Override
    public Builder<T> getThis() {
      return this;
    }

  }

  protected ConstructedClassField(@NotNull Builder<T> builder) {
    super(builder);
    this.classRef = builder.classRef;
    this.type = builder.type;
  }

  @Override
  public ClassDefinition getClassDef() {
    return new DummyClassDefinition(classRef);
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public PropertyInterface getXField() {
    throw new UnsupportedOperationException();
  }

  private class DummyClassDefinition implements ClassDefinition {

    private final ClassReference classRef;

    private DummyClassDefinition(ClassReference classRef) {
      this.classRef = checkNotNull(classRef);
    }

    @Override
    public String getName() {
      return classRef.toString();
    }

    @Override
    public ClassReference getClassReference() {
      return classRef;
    }

    @Deprecated
    @Override
    public DocumentReference getClassRef() {
      return classRef.getDocumentReference();
    }

    @Deprecated
    @Override
    public DocumentReference getClassRef(WikiReference wikiRef) {
      return classRef.getDocumentReference(wikiRef);
    }

    @Override
    public boolean isBlacklisted() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isInternalMapping() {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<ClassField<?>> getFields() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ClassField<?>> getField(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<ClassField<T>> getField(String name, Class<T> token) {
      throw new UnsupportedOperationException();
    }

  }

}
