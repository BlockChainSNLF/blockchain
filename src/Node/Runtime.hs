module Node.Runtime where

import Control.Concurrent
import Mempool.Mempool
import Node.NodeState
import Storage.Storage


initializeNodeState :: NodeState
initializeNodeState =
  NodeState
    { nodeChain = initializeStorage,
      nodeMempool = initializeMempool,
      miningEpoch = 0
    }





initializeRuntime :: IO (MVar NodeState)
initializeRuntime = newMVar initializeNodeState