package fr.univ.bordeaux.technical.lang;

import jdk.jfr.Description;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LangServiceTest  {

  @Test
  @Description("behavior when Language is not supported")
  void unsupportedLanguage() {
    var lang = new LangService(Locale.KOREAN);
    assertEquals(Locale.ENGLISH, lang.getLocale());
    lang.setLocale(Locale.KOREAN);
    assertEquals(Locale.ENGLISH, lang.getLocale());
  }

  @Test
  @Description("just check behavior of normal usage for UK Locale (2 blocs)")
  void initDefaultLanguageAsEnglishTest(){
    var lang = new LangService();
    if (lang.getLocale() != Locale.ENGLISH) {
      assertTrue(
              LangService.supportedLocales().contains(lang.getLocale()),
              "Language/\"Locale\" is not supported by our app"
      );
      return;
    }
    final String expected = "locale found and loaded successfully";
    final String comparer = "("+lang.getLocale()+")";
    assertEquals(
      expected, lang.getLastMessage(),
            "expected="+comparer+"currrent=("+lang.getLocale()+")"
    );
  }

  @Test
  @Description("just check normal behavior usage for languages (1 & 2 blocs")
  void initLanguagesTest(){
    var lang = new LangService(Locale.UK);
    assertEquals(Locale.UK, lang.getLocale());
    final String expected = "locale found and loaded successfully";
    final String errMsg = "\n\tlast message expected:\n\""+expected;
    final String s = "("+lang.getLocale()+")";
    assertEquals(
      expected,
      lang.getLastMessage(),
      errMsg+"\"\n\tbut it's\n"+lang.getLastMessage()+"\""+s
    );
    assertEquals("no", lang.translate("no"));
    lang = new LangService(Locale.ENGLISH);
    assertEquals(Locale.ENGLISH, lang.getLocale());
  }

  @Test
  @Description("token no present in all translation files")
  void tokenDoesNotExistsInTranslationFilesTest(){
    var lang = new LangService();
    final String token = "2191372391273120371208130183120381301823";
    ArrayList<Locale> locales = new ArrayList<>(Arrays.asList(
      Locale.FRENCH, Locale.ENGLISH
    ));
    for (Locale locale : locales){
      lang.setLocale(locale);
      assertThrows(
        MissingResourceException.class,
        () -> lang.translate(token)
      );
    }
  }

  @Test
  @Description("multi word token")
  void multiWordTokenExceptionTest() {
    var lang = new LangService(Locale.FRENCH);
    final String token = "multi word" ;
    MissingResourceException exept = assertThrows(
      MissingResourceException.class,
      () -> lang.translate(token)
    );
    assertTrue(
      exept.getMessage().contains("PropertyResourceBundle, key "+token)
    );
  }



}
