{-# LANGUAGE DeriveGeneric #-}

module Node.State
  ( Peer (..),
    NodeState (..),
    NodeStateRef,
    initNodeState,
    getBlockchain,
    setBlockchain,
    getPeers,
    addPeer,
    peerToUrl,
  )
where

import Data.Aeson (FromJSON, ToJSON)
import Data.IORef
import GHC.Generics (Generic)
import Types.Chain (Chain (..))
import Types.Mempool (Mempool (..))

type NodeStateRef = IORef NodeState

data Peer = Peer
  { peerHost :: String,
    peerPort :: Int
  }
  deriving (Show, Eq, Generic)

instance ToJSON Peer

instance FromJSON Peer

data NodeState = NodeState
  { nodeId :: String,
    blockchain :: Chain,
    mempool :: Mempool,
    peers :: [Peer]
  }

initNodeState :: String -> Chain -> IO NodeStateRef
initNodeState nid genesisChain =
  newIORef $
    NodeState
      { nodeId = nid,
        blockchain = genesisChain,
        mempool = Mempool [],
        peers = []
      }

getBlockchain :: NodeStateRef -> IO Chain
getBlockchain ref = blockchain <$> readIORef ref

setBlockchain :: NodeStateRef -> Chain -> IO ()
setBlockchain ref newChain =
  modifyIORef' ref (\s -> s {blockchain = newChain})

getPeers :: NodeStateRef -> IO [Peer]
getPeers ref = peers <$> readIORef ref

addPeer :: NodeStateRef -> Peer -> IO ()
addPeer ref peer =
  modifyIORef'
    ref
    ( \s ->
        if peer `elem` peers s
          then s
          else s {peers = peer : peers s}
    )

peerToUrl :: Peer -> String
peerToUrl (Peer host port) =
  "http://" ++ host ++ ":" ++ show port