package fr.univ.bordeaux;

import fr.univ.bordeaux.ui.gui.controllers.GUIExample;
/**
 * @version Java 21 (Microsoft OpenJdk 21.0.9)<br>
 * - test programm using <code>mvn test</code> (don't require to compile before)<br>
 * - compile program using <code>mvn compile</code> <br>
 * - exec using : <code>mvn exec:java</code> (require to compile before)<br>
 * - create jar package : <code>mvn package</code>
 *
 * @short from "/agon" repertory
 */
public class Main {
  public static void main(String[] arg) throws Exception {
    System.out.println("prog principal OK + ajout JUnit");
    var a = new GUIExample();
    a.launch(GUIExample.class, arg);
  }
}
