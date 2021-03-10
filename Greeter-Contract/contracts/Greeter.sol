pragma solidity ^0.4.23;

import "Mortal.sol";

contract Greeter is Mortal {
    /* Define variable greeting of the type string */
    string greeting;

    /* This runs when the contract is executed */
    constructor(string _greeting) public {
        greeting = _greeting;
    }

    /* change greeting */
    function changeGreeting(string _greeting) public {
        greeting = _greeting;
    }
    
    /* Main function */
    function greet() public view returns (string) {
        return greeting;
    }
}