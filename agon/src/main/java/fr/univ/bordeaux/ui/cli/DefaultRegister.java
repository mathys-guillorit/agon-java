package fr.univ.bordeaux.ui.cli;

import fr.univ.bordeaux.application.commands.AgonRegister;
import org.apache.commons.cli.Option;

import javax.annotation.Nonnull;

/**
 * allowed options before real game loop
 * (restrained)
 */
public class DefaultRegister extends AgonRegister<Option> {

  private static DefaultRegister instance;

  private DefaultRegister() {
    super();
  }

  @Nonnull
  public static DefaultRegister getInstance() {
    if (instance == null) {
      instance = new DefaultRegister();
    }
    return instance;
  }


}
