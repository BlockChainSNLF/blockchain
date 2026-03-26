module Node.MiningLoop where

import Control.Concurrent
import Node.NodeState

miningLoop :: MVar NodeState -> IO ()
miningLoop stateVar = do
  putStrLn "Mining loop started"
  threadDelay 1000000
  miningLoop stateVar