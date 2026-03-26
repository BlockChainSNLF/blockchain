module Node.BlockLoop where

import Control.Concurrent
import Node.NodeState
import Node.Channels
import Storage.Storage
import Control.Concurrent.STM
import Mempool.Mempool
import Validations.BlockValidation
import Types.Block
import Types.PreBlock (transactions)

blockLoop :: MVar NodeState -> TVar Bool -> NodeChannels -> IO ()
blockLoop stateVar interruptVar channels = do
  maybeBlock <- atomically $ tryReadTQueue (inboundBlocks channels)

  case maybeBlock of
    Nothing -> do
      threadDelay 100000
      blockLoop stateVar interruptVar channels
    Just newBlock -> do
      modifyMVar_ stateVar $ \st -> do
        case getLastBlock (nodeChain st) of
          Nothing ->
            return st
          Just lastBlock ->
            if isValidBlock (miningDifficulty st) lastBlock newBlock
              then do
                atomically $ writeTVar interruptVar True
                return
                  st
                    { nodeChain = addBlock newBlock (nodeChain st),
                      nodeMempool =
                        removeTransactions
                          (transactions (blockContent newBlock))
                          (nodeMempool st)
                    }
              else
                return st

      blockLoop stateVar interruptVar channels