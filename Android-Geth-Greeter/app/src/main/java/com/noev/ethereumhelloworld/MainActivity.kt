package com.noev.ethereumhelloworld

import android.databinding.*
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.util.concurrent.TimeUnit

const val MINIMUM_GAS_LIMIT = 21000
//const val PRIVATE_KEY_ROPSTEN = "914e0d54701284133143769df155e9c13d16a4ae82696457444c58d7442760d5" //todo: You have to create an ethereum account on the Ropsten network (in metamask) and put your private key here
//const val ROPSTEN_INFURA_URL = "https://rinkeby.infura.io/v3/ff788aca525342818fa643b0e2e99040" //todo: You have to register on the Infura website and put your api key here
//const val ROPSTEN_INFURA_URL = "https://rinkeby.infura.io/v3/90048e116b0d4356a77cf4e3c7d78343"
//const val CONTRACT_ADDRESS = "0x33B27EC0BE04B7ab4069F3b46Daa2d83516B4007"

//Geth Ethereum running on LocalPC
const val geth_url = "http://192.168.0.105:8545"
const val CONTRACT_ADDRESS = "0x71661b9addda8dd8ddcfa81ed2e52c4be82f06ea"
const val PRIVATE_KEY_GETH = "0xb6b7e9c10b6e9e7f20f341b52883dec6801e2bcff1016bcc1d70f71ab5b06160"

//        const val PRIVATE_KEY_ROPSTEN = Credentials.create("f9319fe162c31947c0ca8fd649a536b7ca311b5f210afdc48b62fd7d18ce53e4")
//        const val CONTRACT_ADDRESS = "0x8394cDf176A4A52DA5889f7a99c4f7AD2BF59088"
//const val ROPSTEN_INFURA_URL = "https://rinkeby.infura.io/v3/01eb8f7b5e514832af8e827c23784d23"

class MainActivity : AppCompatActivity() {

    val isLoading = ObservableBoolean()
    val textReadFromContract = ObservableField<String>()
    val gasPrice = ObservableInt(10)
    val userText = ObservableField<String>()
    val gasLimit = ObservableInt(MINIMUM_GAS_LIMIT)

    private var web3j: Web3j? = null

//    private val credentials = Credentials.create(PRIVATE_KEY_ROPSTEN)
    private val credentials = Credentials.create(PRIVATE_KEY_GETH)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ViewDataBinding>(this, R.layout.activity_main)
        binding.setVariable(BR.viewModel, this)

        CoroutineScope(Dispatchers.Main).launch(Dispatchers.IO) {
            isLoading.set(true)
            val result = initializeWeb3J()
            isLoading.set(false)
            toast(result)
        }
    }

    fun writeButtonClicked() {
        CoroutineScope(Dispatchers.Main).launch(Dispatchers.IO) {
            isLoading.set(true)
            val result = writeToContract()
            isLoading.set(false)
            toast(result)
        }
    }

    fun readButtonClicked() {
        CoroutineScope(Dispatchers.Main).launch(Dispatchers.IO) {
            isLoading.set(true)
            val result = readFromContract()
            isLoading.set(false)
            textReadFromContract.set(result)
        }
    }

    private fun writeToContract(): String {
        val greetingToWrite = userText.get()
        val result: String
        result = try {
            val greeter = Greeter.load(CONTRACT_ADDRESS, web3j, credentials, getGasPrice(), getGasLimit())
            val transactionReceipt = greeter.changeGreeting(greetingToWrite).sendAsync().get(3, TimeUnit.MINUTES)
            "Successful transaction. Gas used: " + transactionReceipt.gasUsed
        } catch (e: Exception) {
            "Error during transaction. Error: " + e.message
            Log.d("wat", "Error during transaction. Error: "+e.message).toString();
        }
        return result
    }

    private fun readFromContract(): String {
        val result: String
        result = try {
            val greeter = Greeter.load(CONTRACT_ADDRESS, web3j, credentials, getGasPrice(), getGasLimit())
            val greeting = greeter.greet().sendAsync()
            greeting.get()
        } catch (e: Exception) {
            "Error reading the smart contract. Error: " + e.message
        }
        return result
    }

    private fun initializeWeb3J(): String {
        val infuraHttpService: HttpService
        val result: String
        val account: String
        result = try {
//            infuraHttpService = HttpService(ROPSTEN_INFURA_URL)
            infuraHttpService = HttpService(geth_url)
            web3j = Web3j.build(infuraHttpService)
            "Success initializing web3j/infura"
//             let acc = web3j.ethAccounts().send().accounts.get(0)

        } catch (e: Exception) {
            val exception = e.toString()
            "Error initializing web3j/infura. Error: $exception"
        }

        return result
    }

    private fun getGasPrice(): BigInteger {
        val gasPriceGwei = gasPrice.get()
        val gasPriceWei = BigInteger.valueOf(gasPriceGwei + 1000000000L)
        Log.d("wat", "getGasPrice: $gasPriceGwei")
        return gasPriceWei
    }

    private fun getGasLimit(): BigInteger {
        return gasLimit.get().bigInteger()
    }

    private fun Int.bigInteger(): BigInteger {
        return BigInteger(toString())
    }

    private fun toast(text: String) {
        runOnUiThread {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        }
    }
}
