
CalculatorApp = angular.module("CalculatorApp", ["ui.materialize"])

    function init() {
        window.init();
    }

CalculatorApp.run(['$rootScope', function($scope) {
    $scope.expression = ""
    $scope.response = ""
    $scope.result = ""
    $scope.operators = ["+", "-", "*", "/"]
}])

CalculatorApp.controller("mainCtrl", function($scope) {
    window.init= function() {
        $scope.$apply($scope.load_calculator);
    };

    $scope.load_calculator = function() {
        gapi.client.load('calculator', 'v1', function() {
            $scope.is_backend_ready = true;
        }, 'https://awesome-calculator.appspot.com/_ah/api')
    }

    $scope.submit = function(expression) {
        request = {
            expression: expression
        }

        gapi.client.calculator.calculate_expression(request).execute( function(response) {
            $scope.history = $scope.expression.concat("=")
            $scope.expression = response.result.result
            $scope.$apply()
        })
    }

    $scope.concat = function(term)
    {
        if($scope.expression == $scope.result && !$scope.operators.includes(term))
        {
            $scope.expression = term
        }
        else if (!($scope.operators.includes(term) && $scope.expression == ""))
        {
            var length = $scope.expression.length

            if (term == $scope.expression.substring(length-1, length) && term != "(" && term != ")")
            {
                return
            }
            else if ($scope.checkOperator(term, length))
            {
                $scope.expression = $scope.expression.substring(0, $scope.expression.length-1)
                $scope.expression = $scope.expression.concat(term);
            }
            else 
            {
                $scope.expression = $scope.expression.concat(term);
            }
        }
    }

    $scope.checkOperator = function(term, length)
    {
        if(term == "+" && $scope.expression.substring(length-1, length) == "-")
        {
            return true
        }
        else if(term == "-" && $scope.expression.substring(length-1, length) == "+")
        {
            return true
        }
        else if (term == "*" && $scope.expression.substring(length-1, length) == "/")
        {
            return true
        }
        else if (term == "/" && $scope.expression.substring(length-1, length) == "*")
        {
            return true;
        }
        return false
    }

    $scope.concatAns = function()
    {
        if ($scope.expression == $scope.result)
        {
            return
        }
        $scope.expression = $scope.expression.concat($scope.result);
    }

    $scope.delete = function() {
        if ($scope.expression != "")
        {
            $scope.expression = $scope.expression.substring(0, $scope.expression.length-1)
        }
        else
        {
            $scope.expression = ""
            $scope.response = ""
            $scope.history = ""
        }
    }
})


