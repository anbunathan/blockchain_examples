const HDWalletProvider = require("@truffle/hdwallet-provider");
const Web3 = require("web3");
const compiledFactory = require("./build/CampaignFactory.json");

const provider = new HDWalletProvider(

  // Replace with your recovery phrase from metamask
  "cause bread climb spawn street mammal service until ripple cigar fence camera",

  // Replace with your API endpoint from infura
  "wss://goerli.infura.io/ws/v3/90048e116b0d4356a77cf4e3c7d78343"
);
const web3 = new Web3(provider);

const deploy = async () => {
  const accounts = await web3.eth.getAccounts();

  console.log("Attempting to deploy from account", accounts[0]);

  const result = await new web3.eth.Contract(
    JSON.parse(compiledFactory.interface)
  )
    .deploy({ data: compiledFactory.bytecode })
    .send({ gas: '1000000', gasPrice: '5000000000', from: accounts[0] });

  console.log("Contract deployed to", result.options.address);
  provider.engine.stop();
};
deploy();
