package com.celements.velocity;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.velocity.XWikiVelocityException;

import com.xpn.xwiki.doc.XWikiDocument;

@ComponentRole
public interface VelocityService {

  @NotNull
  Optional<String> evaluate(@Nullable String text);

  @NotNull
  Optional<String> evaluate(@Nullable String text,
      @Nullable VelocityContextModifier contextModifier);

  @NotNull
  Optional<String> evaluate(@NotNull XWikiDocument templateDoc);

  @NotNull
  Optional<String> evaluate(@NotNull XWikiDocument templateDoc,
      @Nullable VelocityContextModifier contextModifier);

  /**
   * evaluates the given text as velocity script. the velocity context is cloned beforehand, thus
   * variable changes within have a local scope.
   */
  @NotNull
  String evaluateVelocityText(@Nullable String text) throws XWikiVelocityException;

  /**
   * evaluates the given text as velocity script with contextModifier to manipulate the velocity
   * context before evaluation. the velocity context is cloned beforehand, thus variable changes
   * within have a local scope.
   */
  @NotNull
  String evaluateVelocityText(@Nullable String text,
      @Nullable VelocityContextModifier contextModifier) throws XWikiVelocityException;

  /**
   * evaluates the given text as velocity script with contextModifier to manipulate the velocity
   * context before evaluation. the velocity context is cloned beforehand, thus variable changes
   * within have a local scope.
   */
  @NotNull
  String evaluateVelocityText(@NotNull XWikiDocument templateDoc, @Nullable String text,
      @Nullable VelocityContextModifier contextModifier) throws XWikiVelocityException;

}
