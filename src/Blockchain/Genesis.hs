module Blockchain.Genesis where

import Hashing.Mining (hashPreBlock)
import Types.Block (Block, createBlock)
import Types.PreBlock (PreBlock (..))
import Types.Transaction (Transaction (..))

genesisPreBlock :: PreBlock
genesisPreBlock =
  PreBlock
    { index = 0,
      timestamp = 0,
      transactions =
        [Transaction "system" "alice" 100 "genesis" 0],
      previousHash = replicate 64 '0',
      nonce = 0
    }

genesisBlock :: Block
genesisBlock =
  createBlock genesisPreBlock (hashPreBlock genesisPreBlock)