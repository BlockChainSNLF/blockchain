module Blockchain.Genesis where

import Hashing.Hash (hashString)
import Hashing.Serialization (serializePreBlock)
import Types.Block (Block, createBlock)
import Types.PreBlock (PreBlock (..))

genesisPreBlock :: PreBlock
genesisPreBlock =
  PreBlock
    { index = 0,
      timestamp = 0,
      transactions = [],
      previousHash = replicate 64 '0',
      nonce = 0
    }

genesisBlock :: Block
genesisBlock = createBlock genesisPreBlock
  where
    genesisHash = hashString serializedGenesis
    serializedGenesis = serializePreBlock genesisPreBlock