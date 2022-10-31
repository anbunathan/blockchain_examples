const Gpio = require('pigpio').Gpio;

const motor = new Gpio(17, {mode: Gpio.OUTPUT});

let pulseWidth = 500;
let increment = 100;

setInterval(() => {
  motor.servoWrite(pulseWidth);

  //pulseWidth += increment;
  if (pulseWidth == 500) {
    pulseWidth = 1500;
  } else if (pulseWidth == 1500) {
    pulseWidth = 500;
  }
}, 5000);
