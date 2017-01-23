
package dev.awesome.calculator;

/** The message bean that will be used in the echo request and response. */
public class Expression {

  private String expression;

  public String getExpression() {
    return this.expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }
}