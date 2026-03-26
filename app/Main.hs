module Main where

import System.Environment (getArgs)
import Control.Concurrent (forkIO, threadDelay)

import Network.Server (startServer)
import Node.State
import Blockchain.Genesis (genesisBlock)

import Network.Client (fetchChain)
import Consensus.Consensus (resolveChain)

main :: IO ()
main = do
  args <- getArgs

  let port = case args of
        (p:_) -> read p
        _     -> 8080

  putStrLn $ "Node running on port " ++ show port

  state <- initNodeState ("node-" ++ show port) [genesisBlock]

  let bootstrapPeer = Peer "localhost" 8080

  if port /= 8080
    then do
      putStrLn "Connecting to bootstrap..."

      addPeer state bootstrapPeer

      chains <- mapM fetchChain [bootstrapPeer]
      myChain <- getBlockchain state

      let newChain = resolveChain myChain chains

      setBlockchain state newChain

      putStrLn "Synced with network"

    else putStrLn "Bootstrap node started"

  startServer state port