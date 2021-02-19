// Interaction with GPIO
var Gpio = require('onoff').Gpio



// Interaction with Ethereum
var Web3 = require('web3')
var web3 = new Web3()

// connect to the local node
web3.setProvider(new web3.providers.HttpProvider('http://localhost:8042'))

// The contract that we are going to interact with
var contractAddress = '0xb929f943044eec68b7bed1787b864d0742dbcc3b'

// Define the ABI (Application Binary Interface)
var ABI = JSON.parse('[{"anonymous":false,"inputs":[{"indexed":true,"internalType":"address","name":"_from","type":"address"},{"indexed":false,"internalType":"uint256","name":"_value","type":"uint256"}],"name":"OnValueChanged","type":"event"},{"constant":false,"inputs":[{"internalType":"address","name":"recipient","type":"address"},{"internalType":"uint256","name":"value","type":"uint256"}],"name":"depositToken","outputs":[{"internalType":"bool","name":"success","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":false,"inputs":[{"internalType":"address","name":"recipient","type":"address"},{"internalType":"uint256","name":"value","type":"uint256"}],"name":"withdrawToken","outputs":[{"internalType":"bool","name":"success","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":false,"inputs":[{"internalType":"address","name":"recipient","type":"address"}],"name":"getTokens","outputs":[{"internalType":"uint256","name":"value","type":"uint256"}],"payable":false,"stateMutability":"nonpayable","type":"function"}]')

// contract object
var contract = web3.eth.contract(ABI).at(contractAddress)

// components connected to the RPi
var greenLed = new Gpio(14, 'out')
var redLed = new Gpio(15, 'out')
var button = new Gpio(18, 'in', 'rising')

// display initial state
showStatus()

// watch event on the button
button.watch(function (err, value) {
 if (err) {
 throw err
 }

showStatus()
})

// wait for an event triggered on the Smart Contract
var onValueChanged = contract.OnValueChanged({_from: web3.eth.coinbase});

onValueChanged.watch(function(error, result) {
 if (!error) {
 showStatus()
 }
})

// power the LED according the value of the token
function showStatus() {
 //web3.eth.defaultAccount = web3.eth.coinbase
 web3.personal.unlockAccount(web3.eth.accounts[1], 'blockchain')
 web3.eth.defaultAccount = web3.eth.accounts[0]
 // retrieve the value of the token
 var token = contract.getTokens(web3.eth.defaultAccount)
 
// display the LED according the value of the token
 if (token > 1) {
 // Green: you have enough token
 redLed.writeSync(0)
 greenLed.writeSync(1)
 
 } else {
 // Red: not enough token
 greenLed.writeSync(0)
 redLed.writeSync(1)
 const led = new Gpio(17, 'out');       // Export GPIO17 as an output 
 // Toggle the state of the LED connected to GPIO17 every 200ms
 const iv = setInterval(_ => led.writeSync(led.readSync() ^ 1), 200);
 }
}

// release process
process.on('SIGINT', function () {
 greenLed.unexport()
 redLed.unexport()
 button.unexport()
})