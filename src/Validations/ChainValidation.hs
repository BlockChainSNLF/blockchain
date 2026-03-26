module Validations.ChainValidation where

import Blockchain.Genesis (genesisBlock)
import Types.Chain (Chain (..))
import Validations.BlockValidation (isValidBlock)

isValidChain :: Int -> Chain -> Bool
isValidChain _ (Chain []) = False
isValidChain _ (Chain [b]) = b == genesisBlock
isValidChain difficulty (Chain (b1 : b2 : rest)) =
  isValidBlock difficulty b1 b2 && isValidChain difficulty (Chain (b2 : rest))