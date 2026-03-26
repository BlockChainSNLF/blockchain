module Network.Broadcast
  ( broadcastBlock,
    broadcastTransaction,
    broadcastPeer,
  )
where

import Control.Exception (SomeException, try)
import Network.Client (sendBlockToPeer, sendPeer, sendTx)
import Node.State (Peer)
import Types.Block (Block)
import Types.Transaction (Transaction)

broadcastBlock :: [Peer] -> Block -> IO ()
broadcastBlock peers block =
  mapM_ safeSend peers
  where
    safeSend p = do
      result <- try (sendBlockToPeer p block) :: IO (Either SomeException ())
      case result of
        Left _ -> putStrLn $ "Failed to send block to " ++ show p
        Right _ -> pure ()

broadcastTransaction :: [Peer] -> Transaction -> IO ()
broadcastTransaction peers tx =
  mapM_ safeSend peers
  where
    safeSend p = do
      result <- try (sendTx p tx) :: IO (Either SomeException ())
      case result of
        Left _ -> putStrLn $ "Failed to send tx to " ++ show p
        Right _ -> pure ()

broadcastPeer :: [Peer] -> Peer -> IO ()
broadcastPeer peers newPeer =
  mapM_ safeSend peers
  where
    safeSend p = do
      result <- try (sendPeer p newPeer) :: IO (Either SomeException ())
      case result of
        Left _ -> putStrLn $ "Failed to send peer to " ++ show p
        Right _ -> pure ()