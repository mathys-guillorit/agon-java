package fr.univ.bordeaux;

public class Example {

  private int value;
  private int rd;

  public Example() {
    value = 0;
    rd = this.randint(1, 100);
  }

  public void compute() {
    this.value = this.rd / this.value;
  }

  private int randint(int min, int max) {
    return (int) (Math.random() * (max - min + 1)) + min;
  }

  public int add(int a, int b) {
    return a + b;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public int getRd() {
    return rd;
  }

  public void setRd(int rd) {
    this.rd = rd;
  }
}
