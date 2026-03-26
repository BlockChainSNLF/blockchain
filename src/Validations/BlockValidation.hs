module Validations.BlockValidation where

import Hashing.Hash (hashString)
import Hashing.Serialization (serializePreBlock)
import Types.Block (Block (..))
import Types.PreBlock (PreBlock (..))

-- Coordinar con Nico (Parte 2). Debe ser igual al valor usado al minar.
difficulty :: Int
difficulty = 3

requiredPrefix :: String
requiredPrefix = replicate difficulty '0'

hasValidHash :: Block -> Bool
hasValidHash block =
  hashValue block == hashString (serializePreBlock (blockContent block))

meetsProofOfWork :: Block -> Bool
meetsProofOfWork block =
  take difficulty (hashValue block) == requiredPrefix

linksTo :: Block -> Block -> Bool
linksTo prevBlock nextBlock =
  previousHash (blockContent nextBlock) == hashValue prevBlock
    && index (blockContent nextBlock) == index (blockContent prevBlock) + 1

isValidBlock :: Block -> Block -> Bool
isValidBlock prevBlock newBlock =
  hasValidHash newBlock
    && meetsProofOfWork newBlock
    && linksTo prevBlock newBlock

data BlockError
  = InvalidHash
  | FailedPoW
  | BrokenLink
  deriving (Show, Eq)

validateBlockDetailed :: Block -> Block -> [BlockError]
validateBlockDetailed prevBlock newBlock =
  [InvalidHash | not (hasValidHash newBlock)]
    ++ [FailedPoW | not (meetsProofOfWork newBlock)]
    ++ [BrokenLink | not (linksTo prevBlock newBlock)]