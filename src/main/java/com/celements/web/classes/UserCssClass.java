package com.celements.web.classes;

import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.ClassReference;

import com.celements.model.classes.AbstractClassDefinition;
import com.celements.model.classes.fields.BooleanField;
import com.celements.model.classes.fields.ClassField;
import com.celements.model.classes.fields.StringField;
import com.celements.model.classes.fields.list.single.StringSingleListField;
import com.google.common.collect.ImmutableList;

@Component(UserCssClass.CLASS_DEF_HINT)
public class UserCssClass extends AbstractClassDefinition implements CelementsClassDefinition {

  public static final String SPACE_NAME = "Skins";
  public static final String DOC_NAME = "UserCSS";
  public static final String CLASS_DEF_HINT = SPACE_NAME + "." + DOC_NAME;
  public static final ClassReference CLASS_REF = new ClassReference(SPACE_NAME, DOC_NAME);

  private static final List<String> MEDIA_TYPES_LIST = ImmutableList.of("all", "aural", "braille",
      "embossed", "handheld", "print", "projection", "screen", "tty", "tv");

  public static final ClassField<String> FIELD_CSSNAME = new StringField.Builder(CLASS_DEF_HINT,
      "cssname").size(30).build();

  public static final ClassField<Boolean> FIELD_ALTERNATE = new BooleanField.Builder(CLASS_DEF_HINT,
      "alternate").displayType("yesno").build();

  public static final ClassField<String> FIELD_TITLE = new StringField.Builder(CLASS_DEF_HINT,
      "title").size(30).build();

  public static final ClassField<Boolean> FIELD_IS_RTE_CONTENT = new BooleanField.Builder(
      CLASS_DEF_HINT, "is_rte_content").displayType("yesno").build();

  public static final ClassField<String> FIELD_MEDIA = new StringSingleListField.Builder(
      CLASS_DEF_HINT, "media").values(MEDIA_TYPES_LIST).build();

  @Override
  public String getName() {
    return CLASS_DEF_HINT;
  }

  @Override
  public boolean isInternalMapping() {
    return false;
  }

  @Override
  protected String getClassSpaceName() {
    return SPACE_NAME;
  }

  @Override
  protected String getClassDocName() {
    return DOC_NAME;
  }

}
