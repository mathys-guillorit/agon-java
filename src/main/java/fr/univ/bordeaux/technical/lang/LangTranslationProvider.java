package fr.univ.bordeaux.technical.lang;

import java.util.Locale;

public interface LangTranslationProvider {
  void setLocale(Locale locale);
  Locale getLocale();
  String translate(String key);
}
