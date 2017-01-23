
CalculatorApp = angular.module("CalculatorApp", ["ui.materialize"])

//Google Cloud Endpoints callback function.
function init() {
    window.init();
}

// Set variables on application initialization.
CalculatorApp.run(['$rootScope', function($scope) {
    $scope.expression = ""
    $scope.response = ""
    $scope.result = ""
    $scope.operators = ["+", "*", "/"]
}])

CalculatorApp.controller("mainCtrl", function($scope) {

    /*
     * Goolge Cloud Endpoints intitialisation.
     */
    window.init= function() {
        $scope.$apply($scope.load_calculator);
    };

    $scope.load_calculator = function() {
        gapi.client.load('calculator', 'v1', function() {
            $scope.is_backend_ready = true;
        }, 'https://awesome-calculator.appspot.com/_ah/api')
    }

    $scope.submit = function(expression) {
        $scope.loading = true
        request = {
            expression: expression
        }

        gapi.client.calculator.calculate(request).execute( function(response) {
            $scope.history = $scope.expression.concat("=")
            $scope.expression = response.result.result
            $scope.$apply()
            $scope.loading = false
        })
    }

    /*
     *  Concatenates the user's input to the current expression string, givne it is valid.
     *  @param {String} input   [the user's input]
     */
    $scope.concat = function(input)
    {
        if($scope.expression == $scope.result && !$scope.operators.includes(input))
        {
            $scope.expression = input
        }
        else if (!($scope.operators.includes(input) && $scope.expression == ""))
        {
            var length = $scope.expression.length
            var lastChar = $scope.expression.substring(length-1, length)
            var operators = $scope.operators

            if ((input == lastChar || (lastChar == "-" && operators.includes(input))) && input != "(" && input != ")")
            {
                return
            }
            else if (input == "(" && (!operators.includes(lastChar) && lastChar !="-"))
            {
                return
            }
            else if(input == ")" && lastChar == "(")
            {
                return
            }
            else if ($scope.checkOperator(input, length))
            {
                $scope.expression = $scope.expression.substring(0, $scope.expression.length-1)
                $scope.expression = $scope.expression.concat(input);
            }
            else 
            {
                $scope.expression = $scope.expression.concat(input);
            }
        }
    }

    /*
     *  Concatenates the answer to the previous expression to the current expression string, given it is valid.
     */
    $scope.concatAns = function()
    {
        if ($scope.expression == $scope.result)
        {
            return
        }
        $scope.expression = $scope.expression.concat($scope.result);
    }

    /**
     * Checks if current operator should be replaced by input
     * @param  {String} input   [the user's input]
     * @param  {integer} length [the length of the expression]
     * @return {boolean}        [whether to repalce or not]
     */
    $scope.checkOperator = function(input, length)
    {
        if ($scope.operators.includes($scope.expression.substring(length-1, length)) && $scope.operators.includes(input))
        {
            return true
        }
        else if ($scope.expression.substring(length-1, length) == "+" && input =="-")
        {
            return true
        }
        else if ($scope.expression.substring(length-1, length) == "-" && $scope.operators.includes(input))
        {
            return true
        }
        return false
    }

    /**
     * Delete last character of expression or reset calculator.
     */
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


