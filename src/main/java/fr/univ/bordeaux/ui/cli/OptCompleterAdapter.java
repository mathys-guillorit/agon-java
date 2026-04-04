package fr.univ.bordeaux.ui.cli;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.jline.reader.Completer;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;

/**
 * Transform {@link Options} from Apache Commons Cli into Reader completer for JLine {@link
 * Completer}.
 *
 * @version 1 future improvements : use treemap with the origin as no default completer separated
 *     with 2 branches (first fore commands possibilities next layers for option and final layer for
 *     arguments) but it can be used in other different situations
 */
public class OptCompleterAdapter {

  private Options opts;

  /**
   * Converts {@link Options} into {@link Completer}.
   *
   * @param opts options for command (to be converted into string --something)
   */
  public OptCompleterAdapter(Options opts) {
    this.opts = opts;
  }

  /**
   * Transform options to text.
   *
   * @param o option to turn into string
   * @return possible options
   */
  private ArrayList<String> optify(Option o) {
    ArrayList<String> out = new ArrayList<>();
    if (o.getOpt() != null) {
      out.add("-" + o.getOpt());
    }
    if (o.getLongOpt() != null) {
      out.add("--" + o.getLongOpt());
    }
    return out;
  }

  /**
   * Get the completer from possible command available options.
   *
   * @param cmdName name of te command we want to complete
   * @return completer
   */
  @Nonnull
  public Completer getCompleter(String cmdName) {
    List<String> predictOptsNames = new ArrayList<>();
    for (Option o : opts.getOptions()) {
      if (o == null || (o.getOpt() == null && o.getLongOpt() == null)) {
        continue;
      }
      predictOptsNames.addAll(this.optify(o));
    }
    ArgumentCompleter arcComp =
        new ArgumentCompleter(
            new StringsCompleter(cmdName), new StringsCompleter(predictOptsNames));
    arcComp.setStrict(true);
    return arcComp;
  }
}
