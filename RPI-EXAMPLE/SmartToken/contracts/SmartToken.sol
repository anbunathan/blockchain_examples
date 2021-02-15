pragma solidity ^0.5.16; 

contract SmartToken { 
    mapping(address => uint) tokens; 
    event OnValueChanged(address indexed _from, uint _value); 
    
    function depositToken(address recipient, uint value) public returns (bool success) { 
        tokens[recipient] += value; 
        emit OnValueChanged(recipient, tokens[recipient]); 
        return true; 
    }
    
    function withdrawToken(address recipient, uint value) public returns (bool success) { 
        if (int(tokens[recipient] - value) < 0) { 
            tokens[recipient] = 0; 
        } else { 
            tokens[recipient] -= value; 
        } 
        emit OnValueChanged(recipient, tokens[recipient]); 
        return true; 
    } 
    
    function getTokens(address recipient) public returns (uint value) { 
        return tokens[recipient]; 
    } 
}
