module Node.MiningLoop where

import Control.Concurrent
import Hashing.Mining
import Node.NodeState
import Types.Block
import Types.PreBlock
import Types.Transaction
import Mempool.Mempool
import Storage.Storage
import Control.Concurrent.STM
import Node.Channels


miningLoop :: MVar NodeState -> TVar Bool -> NodeChannels -> IO ()
miningLoop stateVar interruptVar channels = do
  st <- readMVar stateVar
  let txs = getTransactions (nodeMempool st) blockSize

  case getLastBlock (nodeChain st) of
    Nothing -> do
      threadDelay 1000000
      miningLoop stateVar interruptVar channels
    Just lastBlock ->
      if null txs
        then do
          threadDelay 1000000
          miningLoop stateVar interruptVar channels
        else do
          atomically $ writeTVar interruptVar False

          let candidate = buildPreBlock lastBlock txs
          result <- mineBlockInterruptible (miningDifficulty st) candidate interruptVar

          case result of
            Nothing -> miningLoop stateVar interruptVar channels
            Just newBlock -> do
              if isBlockMinedCorrectly (miningDifficulty st) newBlock
                then do
                  putStrLn $ "Mined new block: " ++ show (index (blockContent newBlock))
                  modifyMVar_ stateVar $ \s ->
                    return
                      s
                        { nodeChain = addBlock newBlock (nodeChain s),
                          nodeMempool = removeTransactions txs (nodeMempool s)
                        }
                  atomically $ writeTQueue (outboundBlocks channels) newBlock
                else
                  putStrLn "Mined block is invalid, discarding."

              miningLoop stateVar interruptVar channels

buildPreBlock :: Block -> [Transaction] -> PreBlock
buildPreBlock lastBlock txs =
  PreBlock
    { index = index (blockContent lastBlock) + 1,
      timestamp = timestamp (blockContent lastBlock) + 1,
      transactions = take blockSize txs,
      previousHash = hashValue lastBlock,
      nonce = 0
    }