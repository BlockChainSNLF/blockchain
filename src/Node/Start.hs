module Node.Start where

import Control.Concurrent (forkIO, threadDelay)
import Control.Concurrent.MVar
import Mempool.Mempool
import Node.NodeState
import Node.Runtime
import Node.TransactionLoop
import Node.BlockLoop
import Node.MiningLoop

startNode :: IO ()
startNode = do
  stateVar <- initializeRuntime

  _ <- forkIO (transactionLoop stateVar)
  _ <- forkIO (blockLoop stateVar)
  _ <- forkIO (miningLoop stateVar)

  putStrLn "Node started"
  keepAlive
  where
    keepAlive = threadDelay 1000000 >> keepAlive