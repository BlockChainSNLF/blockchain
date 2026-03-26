module Main where

import System.Environment (getArgs)

import Network.Server (startServer)
import Node.State
import Storage.Storage (initializeStorage)

import Network.Client (fetchChain)
import Consensus.Consensus (resolveChain)
import Types.Chain (Chain(..))

import Mining.Miner (startMiner)

main :: IO ()
main = do
  args <- getArgs

  let port = case args of
        (p:_) -> read p
        _     -> 8080

  putStrLn $ "Node running on port " ++ show port

  state <- initNodeState ("node-" ++ show port) initializeStorage

  let bootstrapPeer = Peer "localhost" 8080

  if port /= 8080
    then do
      putStrLn "Connecting to bootstrap..."

      addPeer state bootstrapPeer

      chains <- mapM fetchChain [bootstrapPeer]
      myChain <- getBlockchain state

      let peerChains = map Chain chains
      let newChain = resolveChain myChain peerChains

      setBlockchain state newChain

      putStrLn "Synced with network"

    else putStrLn "Bootstrap node started"

  startMiner state

  startServer state port