module Validations.BlockValidation where

import Hashing.Mining (isBlockMinedCorrectly)
import Types.Block (Block (..))
import Types.PreBlock (PreBlock (..))

linksTo :: Block -> Block -> Bool
linksTo prevBlock nextBlock =
  previousHash (blockContent nextBlock) == hashValue prevBlock
    && index (blockContent nextBlock) == index (blockContent prevBlock) + 1

isValidBlock :: Int -> Block -> Block -> Bool
isValidBlock difficulty prevBlock newBlock =
  isBlockMinedCorrectly difficulty newBlock
    && linksTo prevBlock newBlock