{-# LANGUAGE OverloadedStrings #-}
{-# LANGUAGE BlockArguments #-}

module Network.Server (startServer) where

import Web.Scotty
import Control.Monad.IO.Class (liftIO)
import Data.IORef (readIORef, modifyIORef')

import Node.State

import Validations.BlockValidation (isValidBlock)
import Network.Broadcast (broadcastBlock)
import Network.Client (fetchChain)
import Consensus.Consensus (resolveChain)
import Types.PreBlock
import Types.Transaction
import Types.Block (Block(..), createBlock)

import Network.Wai.Handler.Warp (runSettings, defaultSettings, setPort, setHost)
import Web.Scotty (scottyApp)

startServer :: NodeStateRef -> Int -> IO ()
startServer state port = do
  app <- scottyApp do

    get "/health" $
      text "OK"

    get "/chain" do
      chain <- liftIO $ getBlockchain state
      json chain

    get "/peers" do
      ps <- liftIO $ getPeers state
      json ps

    post "/peers" do
      peer <- jsonData :: ActionM Peer
      liftIO $ addPeer state peer
      text "Peer added"

    post "/mine" do
      stateData <- liftIO $ readIORef state
      let chain = blockchain stateData
      let lastBlock = last chain

      let tx = Transaction "Alice" "Bob" 10 "firma123"

      let newIndex = index (blockContent lastBlock) + 1

      let newPreBlock = PreBlock
            { index = newIndex
            , timestamp = newIndex
            , transactions = [tx]
            , previousHash = hashValue lastBlock
            , nonce = 0
            }

      let newBlock = createBlock newPreBlock

      liftIO $ modifyIORef' state (\s -> s { blockchain = chain ++ [newBlock] })

      ps <- liftIO $ getPeers state
      liftIO $ broadcastBlock ps newBlock

      json newBlock

    post "/blocks" do
      newBlock <- jsonData :: ActionM Block
      liftIO $ print newBlock
      stateData <- liftIO $ readIORef state

      let chain = blockchain stateData
      let lastBlock = last chain

      if isValidBlock lastBlock newBlock
        then do
          let newChain = chain ++ [newBlock]
          liftIO $ modifyIORef' state (\s -> s { blockchain = newChain })

          ps <- liftIO $ getPeers state
          liftIO $ broadcastBlock ps newBlock

          text "Block agregado"
        else do
          ps <- liftIO $ getPeers state
          peerChains <- liftIO $ mapM fetchChain ps

          let resolved = resolveChain chain peerChains

          liftIO $ modifyIORef' state (\s -> s { blockchain = resolved })

          text "Sync realizado"

  let settings =
        setPort port $
        setHost "0.0.0.0" defaultSettings

  runSettings settings app