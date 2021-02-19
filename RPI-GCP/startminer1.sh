#!/bin/bash

geth --identity "miner1" --networkid 42 --datadir "~/RPI/miner1" --nodiscover --mine --rpc --rpcport "8042" --port "30303" --allow-insecure-unlock --unlock 0 --password "~/RPI/password.sec" --ipcpath "~/Library/Ethereum/geth.ipc" --rpcapi "db,eth,net,web3,personal,miner,admin,txpool,debug"