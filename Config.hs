module Config (NodeConfig(..), loadConfig) where

import System.Environment (lookupEnv)

data NodeConfig = NodeConfig
  { nodeHost :: String
  , nodePort :: Int
  }

loadConfig :: IO NodeConfig
loadConfig = do
  host <- lookupEnv "NODE_HOST"
  port <- lookupEnv "NODE_PORT"

  let h = maybe "127.0.0.1" id host
  let p = maybe 8080 read port

  return $ NodeConfig h p