package org.tensorflow.lite.examples.gestureBC;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.0.1.
 */
public class SmartToken_sol_SmartToken extends Contract {
    private static final String BINARY = "6060604052341561000f57600080fd5b6102408061001e6000396000f3006060604052600436106100565763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663338b5dea811461005b578063450efe21146100915780639e281a98146100c2575b600080fd5b341561006657600080fd5b61007d600160a060020a03600435166024356100e4565b604051901515815260200160405180910390f35b341561009c57600080fd5b6100b0600160a060020a0360043516610142565b60405190815260200160405180910390f35b34156100cd57600080fd5b61007d600160a060020a036004351660243561015d565b600160a060020a03821660008181526020819052604080822080548501908190559192917f65dce515c55eb8d94c3ec733006907bb1a555ef5fb2ab7227019830d4f90a259915190815260200160405180910390a250600192915050565b600160a060020a031660009081526020819052604090205490565b600160a060020a0382166000908152602081905260408120548290038190121561019f57600160a060020a0383166000908152602081905260408120556101bf565b600160a060020a0383166000908152602081905260409020805483900390555b600160a060020a03831660008181526020819052604090819020547f65dce515c55eb8d94c3ec733006907bb1a555ef5fb2ab7227019830d4f90a259915190815260200160405180910390a2506001929150505600a165627a7a72305820f19c15870346ac8d50ebe649db6603da466512403028231ec874980df24298c50029";

    public static final String FUNC_DEPOSITTOKEN = "depositToken";

    public static final String FUNC_GETTOKENS = "getTokens";

    public static final String FUNC_WITHDRAWTOKEN = "withdrawToken";

    public static final Event ONVALUECHANGED_EVENT = new Event("OnValueChanged", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected SmartToken_sol_SmartToken(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected SmartToken_sol_SmartToken(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected SmartToken_sol_SmartToken(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected SmartToken_sol_SmartToken(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<TransactionReceipt> depositToken(String recipient, BigInteger value) {
        final Function function = new Function(
                FUNC_DEPOSITTOKEN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(recipient), 
                new org.web3j.abi.datatypes.generated.Uint256(value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> getTokens(String recipient) {
        final Function function = new Function(FUNC_GETTOKENS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(recipient)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> withdrawToken(String recipient, BigInteger value) {
        final Function function = new Function(
                FUNC_WITHDRAWTOKEN, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(recipient), 
                new org.web3j.abi.datatypes.generated.Uint256(value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public List<OnValueChangedEventResponse> getOnValueChangedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(ONVALUECHANGED_EVENT, transactionReceipt);
        ArrayList<OnValueChangedEventResponse> responses = new ArrayList<OnValueChangedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OnValueChangedEventResponse typedResponse = new OnValueChangedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<OnValueChangedEventResponse> onValueChangedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, OnValueChangedEventResponse>() {
            @Override
            public OnValueChangedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(ONVALUECHANGED_EVENT, log);
                OnValueChangedEventResponse typedResponse = new OnValueChangedEventResponse();
                typedResponse.log = log;
                typedResponse._from = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse._value = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<OnValueChangedEventResponse> onValueChangedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ONVALUECHANGED_EVENT));
        return onValueChangedEventFlowable(filter);
    }

    @Deprecated
    public static SmartToken_sol_SmartToken load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new SmartToken_sol_SmartToken(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static SmartToken_sol_SmartToken load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new SmartToken_sol_SmartToken(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static SmartToken_sol_SmartToken load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new SmartToken_sol_SmartToken(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static SmartToken_sol_SmartToken load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new SmartToken_sol_SmartToken(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<SmartToken_sol_SmartToken> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SmartToken_sol_SmartToken.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<SmartToken_sol_SmartToken> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SmartToken_sol_SmartToken.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<SmartToken_sol_SmartToken> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(SmartToken_sol_SmartToken.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<SmartToken_sol_SmartToken> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(SmartToken_sol_SmartToken.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static class OnValueChangedEventResponse {
        public Log log;

        public String _from;

        public BigInteger _value;
    }
}
