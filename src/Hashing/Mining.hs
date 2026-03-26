module Hashing.Mining where

import Control.Concurrent.STM
import Hashing.Hash (hashString)
import Hashing.Serialization (serializePreBlock)
import Types.Block (Block, blockContent, createBlock, hashValue)
import Types.PreBlock (PreBlock (..), nonce)
import Types.Transaction (Transaction)

blockSize :: Int
blockSize = 5

hashPreBlock :: PreBlock -> String
hashPreBlock = hashString . serializePreBlock

isHashValidForDifficulty :: Int -> String -> Bool
isHashValidForDifficulty difficulty hashResult =
  take normalizedDifficulty hashResult == replicate normalizedDifficulty '0'
  where
    normalizedDifficulty = max 0 difficulty

buildPreBlock :: Block -> [Transaction] -> PreBlock
buildPreBlock lastBlock txs =
  PreBlock
    { index        = index (blockContent lastBlock) + 1
    , timestamp    = timestamp (blockContent lastBlock) + 1
    , transactions = take blockSize txs
    , previousHash = hashValue lastBlock
    , nonce        = 0
    }

mineBlock :: Int -> PreBlock -> Block
mineBlock difficulty = go . applyNonce 0
  where
    go candidate
      | isHashValidForDifficulty difficulty candidateHash =
          createBlock candidate candidateHash
      | otherwise =
          go (applyNonce (nonce candidate + 1) candidate)
      where
        candidateHash = hashPreBlock candidate
        
mineBlockInterruptible :: Int -> PreBlock -> TVar Bool -> IO (Maybe Block)
mineBlockInterruptible difficulty pb interrupt = go (applyNonce 0 pb)
  where
    go candidate = do
      shouldStop <- readTVarIO interrupt
      if shouldStop
        then return Nothing
        else do
          let h = hashPreBlock candidate
          if isHashValidForDifficulty difficulty h
            then return (Just (createBlock candidate h))
            else go (applyNonce (nonce candidate + 1) candidate)

applyNonce :: Int -> PreBlock -> PreBlock
applyNonce value preBlock = preBlock {nonce = value}

isBlockMinedCorrectly :: Int -> Block -> Bool
isBlockMinedCorrectly difficulty block =
  storedHash == recomputedHash && isHashValidForDifficulty difficulty storedHash
  where
    storedHash     = hashValue block
    recomputedHash = hashPreBlock (blockContent block)