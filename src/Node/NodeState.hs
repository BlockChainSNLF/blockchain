module Node.NodeState where

import Types.Chain
import Types.Mempool

data NodeState = NodeState
  { nodeChain :: Chain,
    nodeMempool :: Mempool,
    miningEpoch :: Int
  }
  deriving (Show, Eq)