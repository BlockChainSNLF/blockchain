module Validations.ChainValidation where

import Blockchain.Genesis (genesisBlock)
import Types.Block (Block)
import Validations.BlockValidation (isValidBlock)

isValidChain :: [Block] -> Bool
isValidChain [] = False
isValidChain [b] = b == genesisBlock
isValidChain (b1 : b2 : rest) =
  isValidBlock b1 b2 && isValidChain (b2 : rest)

findFirstInvalidIndex :: [Block] -> Maybe Int
findFirstInvalidIndex [] = Nothing
findFirstInvalidIndex [b]
  | b == genesisBlock = Nothing
  | otherwise = Just 0
findFirstInvalidIndex (b1 : b2 : rest)
  | not (isValidBlock b1 b2) = Just 1
  | otherwise = fmap (+ 1) (findFirstInvalidIndex (b2 : rest))

chainLength :: [Block] -> Int
chainLength = length