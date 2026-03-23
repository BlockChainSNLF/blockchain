module Blockchain.Genesis where

import Types.PreBlock

import Hashing.Serialization    

genesisPreBlock :: PreBlock
genesisPreBlock = PreBlock {
    index = 0,
    timestamp = 0,
    transactions = [],
    previousHash = "0000000000000000000000000000000000000000000000000000000000000000",
    nonce = 0
}


serializedGenesis :: String
serializedGenesis = serializePreBlock genesisPreBlock

