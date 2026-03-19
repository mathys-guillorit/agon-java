package fr.univ.bordeaux.ui.cli.tools;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import org.jline.keymap.KeyMap;
import org.jline.reader.Binding;
import org.jline.reader.LineReader;
import org.jline.terminal.Attributes;
import org.jline.terminal.Cursor;
import org.jline.terminal.MouseEvent;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.utils.ColorPalette;
import org.jline.utils.InfoCmp;
import org.jline.utils.NonBlockingReader;

public class FakeTerminal implements Terminal {
  private final OutputStream outputStream;
  private final PrintWriter writer;

  public FakeTerminal(OutputStream outputStream) {
    this.outputStream = outputStream;
    this.writer = new PrintWriter(outputStream);
  }

  @Override
  public String getName() {
    return "FakeTerminal";
  }

  @Override
  public SignalHandler handle(Signal signal, SignalHandler handler) {
    return null;
  }

  @Override
  public PrintWriter writer() {
    return this.writer;
  }

  @Override
  public Charset encoding() {
    return null;
  }

  @Override
  public void flush() {
    this.writer.flush();
  }

  @Override
  public Size getSize() {
    return new Size(80, 24);
  }

  @Override
  public void setSize(Size size) {}

  @Override
  public int getWidth() {
    return Terminal.super.getWidth();
  }

  @Override
  public int getHeight() {
    return Terminal.super.getHeight();
  }

  @Override
  public Size getBufferSize() {
    return Terminal.super.getBufferSize();
  }

  @Override
  public void close() {
    // Ne fait rien
  }

  // Implémente les autres méthodes avec des valeurs par défaut
  @Override
  public InputStream input() {
    return new java.io.ByteArrayInputStream(new byte[0]);
  }

  @Override
  public OutputStream output() {
    return null;
  }

  @Override
  public boolean canPauseResume() {
    return false;
  }

  @Override
  public void pause() {}

  @Override
  public void pause(boolean wait) throws InterruptedException {}

  @Override
  public void resume() {}

  @Override
  public boolean paused() {
    return false;
  }

  @Override
  public Attributes enterRawMode() {
    return null;
  }

  @Override
  public String getType() {
    return "dumb";
  }

  @Override
  public boolean puts(InfoCmp.Capability capability, Object... params) {
    return false;
  }

  @Override
  public boolean getBooleanCapability(InfoCmp.Capability capability) {
    return false;
  }

  @Override
  public Integer getNumericCapability(InfoCmp.Capability capability) {
    return 0;
  }

  @Override
  public String getStringCapability(InfoCmp.Capability capability) {
    return "";
  }

  @Override
  public Cursor getCursorPosition(IntConsumer discarded) {
    return null;
  }

  @Override
  public boolean hasMouseSupport() {
    return false;
  }

  @Override
  public boolean trackMouse(MouseTracking tracking) {
    return false;
  }

  @Override
  public MouseTracking getCurrentMouseTracking() {
    return null;
  }

  @Override
  public MouseEvent readMouseEvent() {
    return null;
  }

  @Override
  public MouseEvent readMouseEvent(IntSupplier reader) {
    return null;
  }

  @Override
  public MouseEvent readMouseEvent(String prefix) {
    return null;
  }

  @Override
  public MouseEvent readMouseEvent(IntSupplier reader, String prefix) {
    return null;
  }

  @Override
  public boolean hasFocusSupport() {
    return false;
  }

  @Override
  public boolean trackFocus(boolean tracking) {
    return false;
  }

  @Override
  public ColorPalette getPalette() {
    return null;
  }

  @Override
  public int getDefaultForegroundColor() {
    return Terminal.super.getDefaultForegroundColor();
  }

  @Override
  public int getDefaultBackgroundColor() {
    return Terminal.super.getDefaultBackgroundColor();
  }

  @Override
  public void raise(Terminal.Signal signal) {}

  @Override
  public NonBlockingReader reader() {
    return null;
  }

  @Override
  public boolean echo() {
    return false;
  }

  @Override
  public boolean echo(boolean echo) {
    return false;
  }

  @Override
  public Attributes getAttributes() {
    return null;
  }

  @Override
  public void setAttributes(Attributes attr) {}
}
