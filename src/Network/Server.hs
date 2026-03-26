{-# LANGUAGE OverloadedStrings #-}

module Network.Server where

import Web.Scotty
import Control.Monad.IO.Class
import Data.IORef

import Node.State
import Types.Transaction
import Types.Block
import Types.Chain
import Types.Mempool

import Storage.Storage
import Ledger.Ledger
import Network.Broadcast
import Network.Client
import Consensus.Consensus
import Validations.BlockValidation

startServer state port = scotty port $ do

  get "/chain" $ do
    c <- liftIO $ getBlockchain state
    json (getChain c)

  get "/peers" $ do
    ps <- liftIO $ getPeers state
    json ps

  post "/peers" $ do
    p <- jsonData
    liftIO $ addPeer state p
    text "peer added"

  post "/transactions" $ do
    tx <- jsonData

    s <- liftIO $ readIORef state
    let ledger = buildLedger (blockchain s)

    if isValidTx ledger tx
      then do
        liftIO $ modifyIORef' state (\st ->
          st { mempool = addTransaction tx (mempool st) })
        ps <- liftIO $ getPeers state
        liftIO $ broadcastTransaction ps tx
        text "ok"
      else text "invalid"

  post "/blocks" $ do
    b <- jsonData
    s <- liftIO $ readIORef state

    case getLastBlock (blockchain s) of
      Nothing -> text "error"
      Just lastB ->
        if isValidBlock lastB b
          then do
            liftIO $ modifyIORef' state (\st ->
              st { blockchain = addBlock b (blockchain st) })
            text "block added"
          else text "invalid block"

  let settings =
        setPort port $
        setHost "0.0.0.0" defaultSettings

  runSettings settings app