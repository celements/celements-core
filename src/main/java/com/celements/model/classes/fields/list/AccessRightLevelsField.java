package com.celements.model.classes.fields.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.validation.constraints.NotNull;

import com.celements.rights.access.EAccessLevel;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.xpn.xwiki.objects.classes.LevelsClass;

@Immutable
public final class AccessRightLevelsField extends EnumListField<EAccessLevel> {

  private static final Function<EAccessLevel, String> GET_ACCESS_LEVEL_ENUM_IDENTIFIERS = new Function<EAccessLevel, String>() {

    @Override
    public String apply(EAccessLevel accessLvl) {
      return accessLvl.getIdentifier();
    }
  };

  public static class Builder extends EnumListField.Builder<EAccessLevel> {

    public Builder(@NotNull String classDefName, @NotNull String name) {
      super(classDefName, name, EAccessLevel.class);
      separator(",");
    }

    @Override
    public Builder getThis() {
      return this;
    }

    @Override
    public AccessRightLevelsField build() {
      return new AccessRightLevelsField(getThis());
    }

  }

  protected AccessRightLevelsField(@NotNull EnumListField.Builder<EAccessLevel> builder) {
    super(builder);
  }

  @Override
  public Object serialize(List<EAccessLevel> value) {
    Object ret = null;
    if (value != null) {
      StringBuilder sb = new StringBuilder();
      for (EAccessLevel val : value) {
        if (sb.length() > 0) {
          sb.append(getSeparator());
        }
        sb.append(val.getIdentifier());
      }
      ret = sb.toString();
    }
    return ret;
  }

  @Override
  protected List<EAccessLevel> resolveList(List<?> list) {
    List<EAccessLevel> ret = new ArrayList<>();
    for (Object elem : (Collection<?>) list) {
      ret.add(EAccessLevel.getAccessLevel(elem.toString()));
    }
    return Collections.unmodifiableList(ret);
  }

  @Override
  protected LevelsClass getListClass() {
    LevelsClass element = new LevelsClass();
    element.fromString(Joiner.on(getSeparator()).join(FluentIterable.from(Arrays.asList(
        enumType.getEnumConstants())).transform(GET_ACCESS_LEVEL_ENUM_IDENTIFIERS)));
    return element;
  }

}
