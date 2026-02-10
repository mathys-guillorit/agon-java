package fr.univ.bordeaux;

import fr.univ.bordeaux.ui.cli.*;

/**
 * @version Java 21 (Microsoft OpenJdk 21.0.9)<br>
 * - test programm using <code>mvn test</code> (don't require to compile before)<br>
 * - compile program using <code>mvn compile</code> <br>
 * - exec using : <code>mvn exec:java</code> (require to compile before)<br>
 * - create jar package : <code>mvn package</code>
 *
 * from "/agon" repertory
 */
public class Main {
  public static void main(String[] arg) {
    AgonShell agonShell = new AgonShell();
    agonShell.loop();
    agonShell.test();
    
  }
}
