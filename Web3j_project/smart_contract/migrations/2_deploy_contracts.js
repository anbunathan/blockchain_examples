var ManageOrder = artifacts.require("./ManageOrder.sol");
module.exports = function(deployer) {
deployer.deploy(ManageOrder);
};