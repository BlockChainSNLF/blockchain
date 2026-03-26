module Network.Broadcast
  ( broadcastBlock
  , broadcastTransaction
  , broadcastPeer
  ) where

import Node.State (Peer)
import Network.Client (sendBlockToPeer, sendTx, sendPeer)
import Types.Block (Block)
import Types.Transaction (Transaction)
import Control.Exception (try, SomeException)

broadcastBlock :: [Peer] -> Block -> IO ()
broadcastBlock peers block =
  mapM_ safeSend peers
  where
    safeSend p = do
      result <- try (sendBlockToPeer p block) :: IO (Either SomeException ())
      case result of
        Left _  -> putStrLn $ "Failed to send block to " ++ show p
        Right _ -> return ()

broadcastTransaction :: [Peer] -> Transaction -> IO ()
broadcastTransaction peers tx =
  mapM_ safeSend peers
  where
    safeSend p = do
      result <- try (sendTx p tx) :: IO (Either SomeException ())
      case result of
        Left _  -> putStrLn $ "Failed to send tx to " ++ show p
        Right _ -> return ()

broadcastPeer :: [Peer] -> Peer -> IO ()
broadcastPeer peers newPeer =
  mapM_ safeSend peers
  where
    safeSend p = do
      result <- try (sendPeer p newPeer) :: IO (Either SomeException ())
      case result of
        Left _  -> putStrLn $ "Failed to send peer to " ++ show p
        Right _ -> return ()