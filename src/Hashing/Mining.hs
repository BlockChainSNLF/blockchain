module Hashing.Mining where

import Hashing.Hash (hashString)
import Hashing.Serialization (serializePreBlock)
import Types.Block (Block, blockContent, createBlock, hashValue)
import Types.PreBlock (PreBlock, nonce)

hashPreBlock :: PreBlock -> String
hashPreBlock = hashString . serializePreBlock

isHashValidForDifficulty :: Int -> String -> Bool
isHashValidForDifficulty difficulty hashResult =
  take normalizedDifficulty hashResult == replicate normalizedDifficulty '0'
  where
    normalizedDifficulty = max 0 difficulty

mineBlock :: Int -> PreBlock -> Block
mineBlock difficulty = go . applyNonce 0
  where
    go candidate
      | isHashValidForDifficulty difficulty candidateHash = createBlock candidate candidateHash
      | otherwise = go (applyNonce (nonce candidate + 1) candidate)
      where
        candidateHash = hashPreBlock candidate

applyNonce :: Int -> PreBlock -> PreBlock
applyNonce value preBlock = preBlock {nonce = value}

isBlockMinedCorrectly :: Int -> Block -> Bool
isBlockMinedCorrectly difficulty block =
  storedHash == recomputedHash && isHashValidForDifficulty difficulty storedHash
  where
    storedHash = hashValue block
    recomputedHash = hashPreBlock (blockContent block)
