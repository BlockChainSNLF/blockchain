module Blockchain.Genesis where

import Types.PreBlock
import Types.Block

import Hashing.Serialization    
import Hashing.Hash

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

genesisBlock :: Block
genesisBlock = createBlock genesisPreBlock (hashString serializedGenesis)

