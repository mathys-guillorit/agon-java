package fr.univ.bordeaux.application.commands;

import javax.annotation.Nonnull;

/**
 * register command to access it without command limits only one {@link CmdRegister} can be created
 * at the same time (modular)
 */
public class CmdRegister extends AgonRegister<ICmd> {

  private static CmdRegister instance;
  /** initialize variables */
  private CmdRegister() {
    super();
  }

  /**
   * get the register
   *
   * @return {@link CmdRegister} that contains {@link String} associated {@link ICmd}
   */
  @Nonnull
  public static CmdRegister getInstance() {
    synchronized (CmdRegister.class) {
      if (CmdRegister.instance == null) {
        CmdRegister.instance = new CmdRegister();
      }
      return CmdRegister.instance;
    }
  }


  @Override
  public void reset() {
    super.reset();
  }

  @Override
  public void remove(String key){
    super.remove(key);
  }

}
