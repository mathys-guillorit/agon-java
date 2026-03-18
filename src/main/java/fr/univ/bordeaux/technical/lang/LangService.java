package fr.univ.bordeaux.technical.lang;


import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @see
 * <a href="https://medium.com/@medcherrou/understanding-resourcebundle-in-java
 * -a-comprehensive-guide-2cb3b49edf03">
 *   Medium article (jan 14 2025)
 *   </a>
 */
public class LangService {

  private ResourceBundle resBundle;
  private Locale locale;

  /**
   * load english as default
   */
  public LangService(){
    this.locale = Locale.getDefault();
    this.loadBundle();
  }

  public LangService(Locale locale){
    this.locale = locale;
    this.loadBundle();
  }

  private void loadBundle(){
    final String filepath = "lang.default";
    try {
      this.resBundle = ResourceBundle.getBundle(
        "lang."+this.locale.toString(),
        this.locale
      );
      System.out.println("locale found and loaded successfully");
    } catch (MissingResourceException e) {
      // load default
      this.locale = Locale.ENGLISH;
      this.resBundle = ResourceBundle.getBundle(
        filepath, this.locale
      );
      System.out.println("locale not found default English is set instead");
    }
  }

  public void translate(String token){
    System.out.println(this.resBundle.getString(token));
  }


}
