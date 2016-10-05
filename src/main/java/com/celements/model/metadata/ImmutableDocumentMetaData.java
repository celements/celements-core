package com.celements.model.metadata;

import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import org.suigeneris.jrcs.rcs.Version;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.celements.model.util.References;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

@Immutable
public final class ImmutableDocumentMetaData implements DocumentMetaData {

  private final SpaceReference spaceRef;
  private final String name;
  private final String lang;
  private final Version version;

  public static class Builder {

    private final SpaceReference spaceRef;
    private final String name;
    private String lang;
    private Version version;

    public Builder(@NotNull SpaceReference spaceRef, @NotNull String name) {
      this.spaceRef = Objects.requireNonNull(spaceRef);
      this.name = Objects.requireNonNull(name);
      Preconditions.checkArgument(!name.isEmpty());
      this.lang = "";
    }

    public Builder language(@Nullable String val) {
      lang = Strings.nullToEmpty(val).trim();
      return this;
    }

    public Builder version(@Nullable String val) {
      if (!Strings.isNullOrEmpty(val)) {
        version(new Version(val));
      }
      return this;
    }

    public Builder version(@Nullable Version val) {
      version = val;
      return this;
    }

    public ImmutableDocumentMetaData build() {
      return new ImmutableDocumentMetaData(this);
    }

  }

  protected ImmutableDocumentMetaData(@NotNull Builder builder) {
    this.spaceRef = builder.spaceRef;
    this.name = builder.name;
    this.lang = builder.lang;
    this.version = builder.version;
  }

  @Override
  public DocumentReference getDocRef() {
    return new DocumentReference(name, References.cloneRef(spaceRef, SpaceReference.class));
  }

  @Override
  public String getLanguage() {
    return lang;
  }

  @Override
  public Version getVersion() {
    return version;
  }

  @Override
  public int hashCode() {
    return Objects.hash(spaceRef, name, lang, version);
  }

  @Override
  public boolean equals(Object obj) {
    boolean ret = false;
    if (this == obj) {
      ret = true;
    } else if (obj instanceof DocumentMetaData) {
      DocumentMetaData other = (DocumentMetaData) obj;
      ret = Objects.equals(this.getDocRef(), other.getDocRef()) && Objects.equals(this.lang,
          other.getLanguage()) && Objects.equals(this.version, other.getVersion());
    }
    return ret;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ImmutableDocumentMetaData [docRef=").append(getDocRef()).append(", lang=").append(
        lang).append(", version=").append(version).append("]");
    return sb.toString();
  }

}
