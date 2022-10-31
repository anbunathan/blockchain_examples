package com.web3j_intro;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;
import java.math.BigDecimal;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.Security;


public class MainActivity extends AppCompatActivity {

    private Web3j web3;
    //FIXME: Add your own password here
    private final String password = "medium";
    private String walletPath;
    private File walletDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBouncyCastle();
        walletPath = getFilesDir().getAbsolutePath();

    }

    public void connectToEthNetwork(View v) {
        toastAsync("Connecting to Ethereum network...");
        // FIXME: Add your own API key here
//        web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/v3/ff788aca525342818fa643b0e2e99040"));
        web3 = Web3j.build(new HttpService("https://ropsten.infura.io/v3/ff788aca525342818fa643b0e2e99040"));
        try {
            Web3ClientVersion clientVersion = web3.web3ClientVersion().sendAsync().get();
            if(!clientVersion.hasError()){
                toastAsync("Connected!");
            }
            else {
                toastAsync(clientVersion.getError().getMessage());
            }
        } catch (Exception e) {
            toastAsync(e.getMessage());
        }
    }

    public void createWallet(View v){

        try{
            String fileName =  WalletUtils.generateLightNewWalletFile(password,new File(walletPath));
            walletDir = new File(walletPath + "/" + fileName);
            Log.d("wat", String.valueOf(walletDir));
            toastAsync("Wallet generated");
        }
        catch (Exception e){
            toastAsync(e.getMessage());
        }
    }

    public void getAddress(View v){
        try {
            Credentials credentials = WalletUtils.loadCredentials(password, walletDir);
            toastAsync("Your address is " + credentials.getAddress());
        }
        catch (Exception e){
            toastAsync(e.getMessage());
        }
    }

    public void sendTransaction(View v){
        try{
            walletDir = new File("/data/user/0/com.web3j_intro/files/UTC--2021-02-27T10-23-45.2Z--c0e7b77ab9821a3f94655d3c8ace4eb38d462fb3.json");
            Credentials credentials = WalletUtils.loadCredentials(password, walletDir);
//            Toast.makeText(this, "Your address is " + credentials.getAddress(), Toast.LENGTH_LONG).show();
//            Log.d("wat", credentials.getAddress());
            TransactionReceipt receipt = Transfer.sendFunds(web3,credentials,"0x73791D42DDb207330009e8641CA27c6CA45C973D",new BigDecimal(1000),Convert.Unit.WEI).sendAsync().get();

//            TransactionReceipt receipt = Transfer.sendFunds(web3,credentials,"0x31B98D14007bDEe637298086988A0bBd31184523",new BigDecimal(1),Convert.Unit.ETHER).sendAsync().get();
//            toastAsync("Transaction complete: " +receipt.getTransactionHash());
            toastAsync("Transaction complete: " +"Done");
        }
        catch (Exception e){
            toastAsync(e.getMessage());
            toastAsync("Transaction complete: " +"Error");
            Log.d("wat", e.getMessage());
        }
    }


    public void toastAsync(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        });
    }

    // Workaround for bug with ECDA signatures.
    private void setupBouncyCastle() {
      final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
      if (provider == null) {
        // Web3j will set up the provider lazily when it's first used.
        return;
      }
      if (provider.getClass().equals(BouncyCastleProvider.class)) {
        // BC with same package name, shouldn't happen in real life.
        return;
      }
      // Android registers its own BC provider. As it might be outdated and might not include
      // all needed ciphers, we substitute it with a known BC bundled in the app.
      // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
      // of that it's possible to have another BC implementation loaded in VM.
      Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
      Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }
}
