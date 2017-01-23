
package dev.awesome.calculator;

import com.google.api.server.spi.auth.EspAuthenticator;
import com.google.api.server.spi.auth.common.User;
import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiIssuer;
import com.google.api.server.spi.config.ApiIssuerAudience;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.UnauthorizedException;

import java.util.*;

// [START calculator_api_annotation]
@Api(
    name = "calculator",
    version = "v1",
    namespace =
      @ApiNamespace(
        ownerDomain = "calculator.qlouder.dev",
        ownerName = "calculator.qlouder.dev",
        packagePath = ""
      )
    )
// [END echo_api_annotation]

public class Calculator {

  // [START calculate method]
  @ApiMethod(
    name = "calculate",
    httpMethod = ApiMethod.HttpMethod.POST
  )
  /**
   * Formats and calculates the given expression.
   * @param  {Request} request [The expression sent by the client.]
   * @return {Response} response [The result of of the expression.]
   */
  public Response calculate(Request request)
  {
    String expression = request.getExpression();
    Response response = new Response();

    //prepare for processing.
    expression = "(" + expression + ")";
  
    //process expression
    response.setResult(process(expression));

    return response;
  }
  // [END calculate method]

  /**
   * Process each subexpression starting from the inner most parantheses.
   * @param  {String} exp [The expression to be processed.]
   * @return {String} exp [The processed expression.]
   */
  private static String process(String exp)
  {
    //when there are no more paranthese, all calculations have been processed.
    while (exp.indexOf("(") != - 1)
    {
        //get beginning and end of inner most expression between paratheses.
        int start = exp.lastIndexOf("(") + 1;
        int stop = start + exp.substring(start).indexOf(")");

        String subExp = exp.substring(start, stop); 

        ArrayList<String> subExpArr = parseString(subExp);
        
        String result = evaluate(subExpArr);

        if (result.equals("error"))
        {
          exp = "Error";
          break;
        }
        else
        {
          String tempExp = new String();

          tempExp = tempExp.concat(exp.substring(0, start-1)).concat(result).concat(exp.substring(stop+1, exp.length()));
          exp = tempExp;
        }
    }

    return exp;
  }

  /**
   * Breaks a subexpression into its component parts: numbers/funtions and operations
   * @param  String expression [Expression to be evaluated]
   * @return ArrayList<String> subExpArr [An array of expression parts]
   */
  private static ArrayList<String> parseString(String expression) 
  {
      // operator "-" is left since we are treating it as a part of a number
      String term = "";
      String[] operators = {"+", "*", "/"};

      //split the expression into individual characters
      ArrayList<String> parsedExpArr = new ArrayList<String>(Arrays.asList(expression.split("")));

      //quick fix for a weird API error that would randomly prepend and empty string to the expression;
      if (parsedExpArr.get(0).equals(""))
      {
        parsedExpArr.remove(0);
      }

      //handle occurences of "-" sign
      for (int i = 0; i < parsedExpArr.size(); i++)
      {
          //change double negatives to positives
          if (parsedExpArr.get(i).equals("-") && parsedExpArr.get(i+1).equals("-"))
          {
              parsedExpArr.remove(i);
              parsedExpArr.remove(i);
              parsedExpArr.add(i, "+");
          }
          //add "+" before negative number if the number is not at the beginning of expression or if number is not a function argument
          else if (parsedExpArr.get(i).equals("-"))
          {
              if (i != 0)
              {
                  if (!Arrays.asList(operators).contains(parsedExpArr.get(i-1)) && !parsedExpArr.get(i-1).equals("t") && !parsedExpArr.get(i-1).equals("w") && !parsedExpArr.get(i-1).equals("E"))
                  {
                      parsedExpArr.add(i, "+");
                      i++;
                  }
              }
          }
      }

      ArrayList<String> subExpArr = new ArrayList<String>();

      //condense character array into numbers/functions and operators array
      for (int i = 0; i < parsedExpArr.size(); i++)
      {
          if(Arrays.asList(operators).contains(parsedExpArr.get(i)))
          {
              subExpArr.add(term);
              subExpArr.add(parsedExpArr.get(i));
              term = "";
          }
          else
          {
              term = term.concat(parsedExpArr.get(i));
              if (i == parsedExpArr.size()-1)
              {
                  subExpArr.add(term);
              }
          }
      }

      return subExpArr;
  }

  /**
   * Evaulate the parsed epression into a stringified number.
   * @param  ArrayList<String> subExpArray [parsed expression array]
   * @return String [result of parsed expression]
   */
  private static String evaluate(ArrayList<String> subExpArray)
  {
      // first evaluate functions
      for (int i = 0; i < subExpArray.size(); i++)
      {
          //evaluate square root functions
          if (subExpArray.get(i).matches("(.*)sqrt(.*)"))
          {
              int sign = 1;
              if (subExpArray.get(i).substring(0,1).equals("-"))
              {
                  sign = -1;
              }

              double argument = Double.parseDouble(subExpArray.get(i).substring(subExpArray.get(i).indexOf("t") + 1,subExpArray.get(i).length()));

              if (argument < 0)
              {
                  return "error";
              }

              double result = sign * Math.sqrt(argument);
              String resultString = Double.toString(result);

              setResult(subExpArray, resultString, i, true);
          }

          //evaluate power functions
          else if (subExpArray.get(i).matches("(.*)pow(.*)"))
          {
              double base = Double.parseDouble(subExpArray.get(i).substring(0, subExpArray.get(i).indexOf("p")));
              double exponent = Double.parseDouble(subExpArray.get(i).substring(subExpArray.get(i).indexOf("w")+1, subExpArray.get(i).length()));

              if (base == 0 && exponent == 0)
              {
                  return "error";
              }

              double result = Math.pow(base,exponent);
              String resultString = Double.toString(result);

              setResult(subExpArray, resultString, i, true);
          }

          //evaluate fatorial
          else if (subExpArray.get(i).matches("(.*)!"))
          {
              double argument = Double.parseDouble(subExpArray.get(i).substring(0,subExpArray.get(i).length()-1));
              double result = factorial(argument);

              if (result == -1)
              {
                  return "error";
              }

              String resultString = Double.toString(result);

              setResult(subExpArray, resultString, i, true);
          }

          //evaluate scientific notation
          else if (subExpArray.get(i).matches("(.*)E(.*)"))
          {
              double number = Double.parseDouble(subExpArray.get(i).substring(0, subExpArray.get(i).indexOf("E")));
              double exponent = Double.parseDouble(subExpArray.get(i).substring(subExpArray.get(i).indexOf("E")+1, subExpArray.get(i).length()));
              double result = number * Math.pow(10,exponent);
              String resultString = Double.toString(result);

              setResult(subExpArray, resultString, i, true);
          }

          //evaluate percentage notation
          else if (subExpArray.get(i).matches("(.*)%"))
          {
              double argument = Double.parseDouble(subExpArray.get(i).substring(0,subExpArray.get(i).indexOf("%")));

              double result = argument / 100;
              String resultString = Double.toString(result);

              setResult(subExpArray, resultString, i, true);
          }
      }

      //then evaluate multiplications and divisions
      for (int i = 0; i < subExpArray.size(); i++)
      {
          //evaluate multiplications
          if (subExpArray.get(i).equals("*"))
          {
              double result = Double.parseDouble(subExpArray.get(i-1)) * Double.parseDouble(subExpArray.get(i+1));
              String resultString = Double.toString(result);

              setResult(subExpArray, resultString, i, false);

              i--;
          }

          //evaluate divisions
          else if (subExpArray.get(i).equals("/"))
          {
              if (subExpArray.get(i+1).equals("0"))
              {
                  return "error";
              }
              double result = Double.parseDouble(subExpArray.get(i-1)) / Double.parseDouble(subExpArray.get(i+1));
              String resultString = Double.toString(result);

              setResult(subExpArray, resultString, i, false);

              i--;
          }
      }

      //finally evaluate additions with positive or negative numbers
      for (int i = 0; i < subExpArray.size(); i++)
      {
          if (subExpArray.get(i).equals("+"))
          {
              double result = Double.parseDouble(subExpArray.get(i-1)) + Double.parseDouble(subExpArray.get(i+1));
              String resultString = Double.toString(result);
              
              setResult(subExpArray, resultString, i, false);

              i--;
          }
      }

      return subExpArray.get(0);
  }

  /**
   * Replaces the functions/operations in the subexpression array with the evaluated value
   * @param  ArrayList<String> expressionArray [the initial subexpression array]
   * @param  String resultString    [the resulting value of an operation/function]
   * @param  i [the position of the function/operation]
   * @param  function [whether what was evaluated is a function or an operation]
   * @return subExpressionArray [the subexpression array with the the function/operation replaced by its value]
   */
  private static ArrayList<String> setResult(ArrayList<String> subExpressionArray, String resultString, int i, boolean function)
  {
      //if a function was evaluated, simply replace the function by its value
      if (function)
      {
          subExpressionArray.set(i,resultString);
      }

      //if an operation was evaluated, replace the first term by the result and delete the next two terms
      else
      {
          subExpressionArray.set(i-1,resultString);
          subExpressionArray.remove(i);
          subExpressionArray.remove(i);
      }

      return subExpressionArray;
  }

  /**
   * Evaluate a factorial recursively.
   * @param  integer n [the argument of the factorial]
   * @return function/int [the result of the factorial function step]
   */
  private static double factorial(double n)
  {
      if (n - (int) n != 0)
      {
          return -1;
      }
      else if (n < 0)
      {
        return -1;
      }
      if (n == 1)
      {
          return 1;
      }

      return factorial(n-1) * n;
  }
}
