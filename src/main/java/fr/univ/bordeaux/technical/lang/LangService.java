package fr.univ.bordeaux.technical.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provide translations. usage example :
 *
 * <pre>
 *   var a = new LangService(); // load JVM Local if included in our project
 *   // else it will use English as default
 *   a = new LangService(Locale.UK); // Locale is lang or country
 *   (contains lang + country language)
 *   System.out.println(a.translate("hello")); // requires the existing
 *   hello token in property files
 * </pre>
 *
 * @see <a
 *     href="https://medium.com/@medcherrou/understanding-resourcebundle-in-java-a-comprehensive-guide-2cb3b49edf03">
 *     Medium article (jan 14 2025) </a>
 */
public class LangService implements LangTranslationProvider {

  private ResourceBundle resBundle;
  private Locale locale;
  private final String defaultFilepath = "lang.default";
  private String lastMessage = "";

  /**
   * To know which {@link Locale} are supported.
   *
   * @return {@link ArrayList} of all available Locale
   */
  public static ArrayList<Locale> supportedLocales() {
    return new ArrayList<>(Arrays.asList(Locale.ENGLISH, Locale.UK, Locale.FRENCH, Locale.FRANCE));
  }

  /**
   * Load detected {@link Locale} on user's machine fallback to English. as default if we don't have
   * his specific {@link Locale}
   */
  public LangService() {
    this.locale = Locale.getDefault();
    this.loadDefaultBundle();
  }

  /**
   * Load a language using a Locale.
   *
   * @param locale {@link Locale}
   */
  public LangService(Locale locale) {
    this.locale = locale;
    this.loadDefaultBundle();
  }

  /** Try to load the bundle fallback to default if the bundle is not found. */
  private void loadDefaultBundle() {
    try {
      this.loadBundle();
    } catch (MissingResourceException e) {
      // load default
      this.locale = Locale.ENGLISH;
      this.resBundle = ResourceBundle.getBundle(this.defaultFilepath, this.locale);
      this.lastMessage = "locale not found default English is set instead";
    }
  }

  private void loadBundle() throws MissingResourceException {
    final String finalPath = "lang." + this.locale.toString();
    this.resBundle = ResourceBundle.getBundle(finalPath, this.locale);
    // getting the confirmation message outside the class
    this.lastMessage = this.translate("LangService_successLoaded");
  }

  /**
   * Translate with a token into different language some text.
   *
   * @param token index token in the .properties files to get the translation
   * @return {@link String} translated output of the token is invalid it throws and Exception
   *     ({@link MissingResourceException}) (development side only)
   */
  public String translate(String token) {
    return this.resBundle.getString(token);
  }

  ///  GETTERS & SETTERS

  /**
   * Get last error message instead of throwing Exceptions everywhere int the app (add a lot more
   * code to catch them).
   *
   * @return {@link String} message as reason of the previous error/exceptions
   */
  public String getLastMessage() {
    return this.lastMessage;
  }

  /**
   * Load a language to translate displayed text across all the project.
   *
   * @param locale {@link Locale}
   * @return true if Language si found else we juste keep the previous one
   */
  public boolean setLocale(Locale locale) {
    Locale last = this.locale;
    this.locale = locale;
    try {
      this.loadBundle();
      return true;
    } catch (MissingResourceException e) {
      this.locale = last;
      return false;
    }
  }

  /**
   * Get the current Locale.
   *
   * @return {@link Locale}
   */
  public Locale getLocale() {
    return this.locale;
  }
}
