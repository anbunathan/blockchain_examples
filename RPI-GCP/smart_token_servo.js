const pigpio = require('pigpio-client').pigpio({
    host: 'localhost',
    port: 8888
});

const ready = new Promise((resolve, reject) => {
    pigpio.once('connected', resolve);
    pigpio.once('error', reject);
});

function wait(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

ready.then(async(info) => {
    // display information on pigpio and connection status
    console.log(JSON.stringify(info, null, 2));

    // control an motor on GPIO 17
    const motor = pigpio.gpio(17);
    await motor.modeSet('output');
    // Interaction with Ethereum
    var Web3 = require('web3')
        var web3 = new Web3()

        // connect to the local node
        web3.setProvider(new web3.providers.HttpProvider('http://localhost:8042'))

        // The contract that we are going to interact with
        var contractAddress = '0xa5c53be143769d625c1185cc67c705f039a1f876'

        // Define the ABI (Application Binary Interface)
        var ABI = JSON.parse('[{"anonymous":false,"inputs":[{"indexed":true,"name":"_from","type":"address"},{"indexed":false,"name":"_value","type":"uint256"}],"name":"OnValueChanged","type":"event"},{"constant":false,"inputs":[{"name":"recipient","type":"address"},{"name":"value","type":"uint256"}],"name":"depositToken","outputs":[{"name":"success","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":false,"inputs":[{"name":"recipient","type":"address"},{"name":"value","type":"uint256"}],"name":"withdrawToken","outputs":[{"name":"success","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[{"name":"recipient","type":"address"}],"name":"getTokens","outputs":[{"name":"value","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"}]')

        // contract object
        var contract = web3.eth.contract(ABI).at(contractAddress)

        // display initial state
        showStatus()

        // wait for an event triggered on the Smart Contract
        var onValueChanged = contract.OnValueChanged({
        _from: web3.eth.coinbase
    });

    onValueChanged.watch(function (error, result) {
        if (!error) {
            showStatus()
        }
    })
    // power the LED according the value of the token
    function showStatus() {
        web3.eth.defaultAccount = web3.eth.coinbase
            var token = contract.getTokens(web3.eth.defaultAccount)
            if (token > 1) {
                //open the door
                pulseWidth = 1500;
                motor.setServoPulsewidth(pulseWidth);
            } else {
                //close the door
                pulseWidth = 500;
                motor.setServoPulsewidth(pulseWidth);
            }
    }

}).catch(console.error);