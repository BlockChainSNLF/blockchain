module Node.TransactionLoop where

import Control.Concurrent
import Control.Concurrent.STM
import Mempool.Mempool
import Node.Channels
import Node.NodeState

transactionLoop :: MVar NodeState -> NodeChannels -> IO ()
transactionLoop stateVar channels = do
  putStrLn "Transaction loop started"
  loop
  where
    loop = do
      maybeTx <- atomically $ tryReadTQueue (inboundTransactions channels)
      case maybeTx of
        Nothing -> do
          threadDelay 100000
          loop
        Just tx -> do
          modifyMVar_ stateVar $ \st ->
            return
              st
                { nodeMempool = reorderByTip (addTransaction tx (nodeMempool st))
                }
          atomically $ writeTQueue (outboundTransactions channels) tx
          loop