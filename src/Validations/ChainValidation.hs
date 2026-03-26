module Validations.ChainValidation where

import Blockchain.Genesis (genesisBlock)
import Types.Chain (Chain (..))
import Validations.BlockValidation (isValidBlock)

isValidChain :: Chain -> Bool
isValidChain (Chain []) = False
isValidChain (Chain [b]) = b == genesisBlock
isValidChain (Chain (b1 : b2 : rest)) =
  isValidBlock b1 b2 && isValidChain (Chain (b2 : rest))