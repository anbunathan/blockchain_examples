import web3 from "./web3";
import CampaignFactory from "./build/CampaignFactory.json";

const instance = new web3.eth.Contract(
    JSON.parse(CampaignFactory.interface),

    // Replace with your contract address that was deployed
    '0xb832e86A125D6c507870Eaa5C4206a6a85C82017'
)

export default instance;