module Main where

import System.Environment (getArgs)

import Network.Server (startServer)
import Node.State
import Storage.Storage
import Mining.Miner

main :: IO ()
main = do
  args <- getArgs
  let port = if null args then 8080 else read (head args)

  putStrLn $ "Node running on port " ++ show port

  state <- initNodeState ("node-" ++ show port) initializeStorage

  startMiner state
  startServer state port