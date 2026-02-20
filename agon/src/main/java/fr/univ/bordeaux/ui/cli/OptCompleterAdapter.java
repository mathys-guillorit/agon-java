package fr.univ.bordeaux.ui.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;

/**
 * transform {@link Options} from Apache Commons Cli into a completer for JLine {@link Completer}
 *
 * @version 1 future improvements : use treemap with the origin as no default completer separated
 *     with 2 branches (first fore commands possibilities next layers for option and final layer for
 *     arguments) but it can be used in other different situations
 */
public class OptCompleterAdapter {

  private Options opts;

  public OptCompleterAdapter(Options opts) {
    this.opts = opts;
  }

  /**
   * check if option is not null
   *
   * @param o concerned option
   * @return boolean is null or not
   */
  private boolean isNull(Option o) {
    return o == null || o.getOpt() == null || o.getLongOpt() == null;
  }

  /**
   * transform options to text
   *
   * @param o option to turn into string
   * @return possible options
   */
  private ArrayList<String> optify(Option o) {
    return new ArrayList<>(Arrays.asList("-" + o.getOpt(), "--" + o.getLongOpt()));
  }

  /**
   * get the completer from possible command available options
   *
   * @param cmdName name of te command we want to complete
   * @return completer
   */
  @Nonnull
  public Completer getCompleter(String cmdName) {
    List<String> followingTxt = new ArrayList<>();
    // unknown options count
    for (Option o : opts.getOptions()) {
      if (this.isNull(o)) continue;
      followingTxt.addAll(this.optify(o));
    }
    return new ArgumentCompleter(new StringsCompleter(cmdName), new StringsCompleter(followingTxt));
  }
}
