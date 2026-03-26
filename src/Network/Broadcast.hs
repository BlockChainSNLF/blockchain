module Network.Broadcast (broadcastBlock) where

import Node.State
import Network.Client (sendBlockToPeer)
import Types.Block (Block)
import Control.Exception (try, SomeException)

broadcastBlock :: [Peer] -> Block -> IO ()
broadcastBlock peers block =
  mapM_ safeSend peers
  where
    safeSend p = do
      result <- try (sendBlockToPeer p block) :: IO (Either SomeException ())
      case result of
        Left _  -> putStrLn "Peer fallen"
        Right _ -> return ()