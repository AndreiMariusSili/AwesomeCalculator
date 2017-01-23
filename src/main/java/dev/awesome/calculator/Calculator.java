
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

  // [START calculate_expression method]
  @ApiMethod(
    name = "calculate_expression",
    httpMethod = ApiMethod.HttpMethod.POST
  )
  public Result calculate_expression(Expression request)
  {
    String exp = request.getExpression();

    exp = "(" + exp + ")";
  
    while (exp.indexOf("(") != - 1)
    {
        int start = exp.lastIndexOf("(") + 1;
        int stop = start + exp.substring(start).indexOf(")");

        String subExp = exp.substring(start, stop); 

        ArrayList<String> subExpArr = parseString(subExp);
        
        String result = evaluate(subExpArr);

        String tempExp = "";

        tempExp = tempExp.concat(exp.substring(0, start-1)).concat(result).concat(exp.substring(stop+1, exp.length()));
        exp = tempExp;
    }

    Result response = new Result();
    response.setResult(exp);

    return response;
  }

  private static String evaluate(ArrayList<String> subExpArray)
  {

      for (int i = 0; i < subExpArray.size(); i++)
      {

          if (subExpArray.get(i).equals("*"))
          {
              double result = Double.parseDouble(subExpArray.get(i-1)) * Double.parseDouble(subExpArray.get(i+1));
              String resultString = Double.toString(result);

              setResult(subExpArray, resultString, i);

              i--;
              System.out.println(subExpArray);
          }

          if (subExpArray.get(i).equals("/"))
          {
              double result = Double.parseDouble(subExpArray.get(i-1)) / Double.parseDouble(subExpArray.get(i+1));
              String resultString = Double.toString(result);

              setResult(subExpArray, resultString, i);

              i--;
              System.out.println(subExpArray);
          }
      }

      for (int i = 0; i < subExpArray.size(); i++)
      {
          if (subExpArray.get(i).equals("+"))
          {
              double result = Double.parseDouble(subExpArray.get(i-1)) + Double.parseDouble(subExpArray.get(i+1));
              String resultString = Double.toString(result);
              
              setResult(subExpArray, resultString, i);

              i--;
              System.out.println(subExpArray);
          }
      }

      return subExpArray.get(0);
  }

  private static ArrayList<String> setResult(ArrayList<String> expressionArray, String resultString, int i)
  {
      expressionArray.set(i-1,resultString);
      expressionArray.remove(i);
      expressionArray.remove(i);

      return expressionArray;
  }

  private static ArrayList<String> parseString(String expression) 
  {
      String term = "";
      String[] operators = {"+", "*", "/"};

      ArrayList<String> parsedExpArr = new ArrayList<String>(Arrays.asList(expression.split("")));

      System.out.println(parsedExpArr);

      for (int i = 0; i < parsedExpArr.size(); i++)
      {
          if (parsedExpArr.get(i).equals("-") && parsedExpArr.get(i+1).equals("-"))
          {
              parsedExpArr.remove(i);
              parsedExpArr.remove(i);
              parsedExpArr.add(i, "+");
          }
          if (parsedExpArr.get(i).equals("-") && !Arrays.asList(operators).contains(parsedExpArr.get(i-1)))
          {
              parsedExpArr.add(i, "+");
              i++;
          }
      }

      System.out.println(parsedExpArr);

      ArrayList<String> subExpArr = new ArrayList<String>();

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
}
