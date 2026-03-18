package fr.univ.bordeaux.technical.lang;

import java.util.Locale;

/**
 * Translation class Behavior
 * used to translate tokens into words and phrases in concrete classes
 */
public interface LangTranslationProvider {

  /**
   * set the translated language
   * @param locale {@link Locale}
   * @return if loading was successfully done of false if no translation found
   */
  boolean setLocale(Locale locale);

  /**
   * get the translated language
   * @return {@link Locale}
   */
  Locale getLocale();

  /**
   * translate a token into different languages words or phrases
   * @param token index token in the .properties files to get the translation
   * @return {@link String} the translation
   */
  String translate(String token);
}