package com.celements.filebase.references;

import static com.google.common.base.Preconditions.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;

import org.xwiki.model.reference.DocumentReference;

import com.celements.model.util.ModelUtils;
import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.google.errorprone.annotations.Immutable;
import com.xpn.xwiki.web.Utils;

@Immutable
public final class FileReference implements Serializable {

  private static final long serialVersionUID = 1L;

  @NotThreadSafe
  public static final class Builder {

    private static final String ATTACHMENT_LINK_REGEX = "([\\w\\-]*:)?([\\w\\-]*\\.[\\w\\-]*){1};.*";
    private static final Supplier<Pattern> ATTACHMENT_LINK_PATTERN = Suppliers
        .memoize(() -> Pattern.compile(ATTACHMENT_LINK_REGEX));
    private static final String ON_DISK_LINK_REGEX = "^:[/\\w\\-\\.]*";
    private static final Supplier<Pattern> ON_DISK_LINK_PATTERN = Suppliers
        .memoize(() -> Pattern.compile(ON_DISK_LINK_REGEX));

    private String name;
    private FileReferenceType type;
    private DocumentReference docRef;
    private String fullPath;
    private String queryString;

    @NotNull
    private static String getAttachmentName(@NotEmpty String link) {
      return link.split(";")[1];
    }

    @NotNull
    private static String getPathFileName(@NotEmpty String link) {
      String[] linkParts = link.split("/");
      return linkParts[linkParts.length - 1];
    }

    private static boolean isAttachmentLink(@Nullable String link) {
      if (link != null) {
        return ATTACHMENT_LINK_PATTERN.get().matcher(link.trim()).matches();
      }
      return false;
    }

    private static boolean isOnDiskLink(@Nullable String link) {
      if (link != null) {
        return ON_DISK_LINK_PATTERN.get().matcher(link.trim()).matches();
      }
      return false;
    }

    @NotNull
    private static DocumentReference getPageDocRef(@NotNull String link) {
      return Utils.getComponent(ModelUtils.class).resolveRef(link.split(";")[0],
          DocumentReference.class);
    }

    private static FileReferenceType getTypeOfLink(@NotEmpty String link) {
      if (isOnDiskLink(link)) {
        return FileReferenceType.ON_DISK;
      } else if (isAttachmentLink(link)) {
        return FileReferenceType.ATTACHMENT;
      }
      return FileReferenceType.EXTERNAL;
    }

    public Builder setFileName(@NotNull String fileName) {
      checkNotNull(fileName);
      this.name = fileName;
      return this;
    }

    public Builder setType(@NotNull FileReferenceType type) {
      checkNotNull(type);
      this.type = type;
      return this;
    }

    public void setDocRef(@NotNull DocumentReference docRef) {
      checkNotNull(docRef);
      this.docRef = docRef;
    }

    public void setFullPath(@NotNull String fullPath) {
      checkNotNull(fullPath);
      this.fullPath = fullPath;
    }

    public void setQueryString(@NotNull String queryString) {
      checkNotNull(queryString);
      this.queryString = queryString;
    }

    public FileReference build() {
      return new FileReference(this);
    }

  }

  private final String name;
  private final FileReferenceType type;
  private final DocumentReference docRef;
  private final String fullPath;
  private final String queryString;

  public FileReference(Builder builder) {
    this.name = builder.name;
    this.type = builder.type;
    this.fullPath = builder.fullPath;
    this.docRef = builder.docRef;
    this.queryString = builder.queryString;
  }

  public String getName() {
    return name;
  }

  public FileReferenceType getType() {
    return type;
  }

  public DocumentReference getDocRef() {
    return docRef;
  }

  public String getFullPath() {
    return fullPath;
  }

  public String getQueryString() {
    return queryString;
  }

  public UriBuilder getUri() {
    return UriBuilder.fromPath(fullPath).replaceQuery(queryString);
  }

  public boolean isAttachmentReference() {
    return type == FileReferenceType.ATTACHMENT;
  }

  public boolean isOnDiskReference() {
    return type == FileReferenceType.ON_DISK;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, docRef, fullPath);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return (obj instanceof FileReference)
        && Objects.equals(((FileReference) obj).name, this.name)
        && Objects.equals(((FileReference) obj).type, this.type)
        && Objects.equals(((FileReference) obj).docRef, this.docRef)
        && Objects.equals(((FileReference) obj).fullPath, this.fullPath);
  }

  @Override
  public String toString() {
    return "FileReference [name=" + name + ", type=" + type + ", docRef=" + docRef + ", fullPath="
        + fullPath + "]";
  }

  public static Builder of(@NotEmpty String link) {
    checkArgument(!Strings.isNullOrEmpty(link), "link may not be empty");
    final String[] linkParts = link.split("\\?");
    Builder builder = new Builder();
    builder.setType(Builder.getTypeOfLink(linkParts[0]));
    if (builder.type == FileReferenceType.ATTACHMENT) {
      builder.setFileName(Builder.getAttachmentName(linkParts[0]));
      builder.setDocRef(Builder.getPageDocRef(linkParts[0]));
    } else {
      builder.setFileName(Builder.getPathFileName(linkParts[0]));
      builder.setFullPath(linkParts[0]);
    }
    if (linkParts.length > 1) {
      builder.setQueryString(linkParts[1]);
    }
    return builder;
  }

}
