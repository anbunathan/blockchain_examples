#!/bin/bash

geth --identity "miner2" --networkid 42 --datadir "~/RPI/miner2" --nodiscover --mine --rpc --rpcport "8043" --port "30304" --allow-insecure-unlock --unlock 0 --password "~/RPI/password1.sec" --rpcapi "db,eth,net,web3,personal,miner,admin,txpool,debug"