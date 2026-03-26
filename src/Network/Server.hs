{-# LANGUAGE OverloadedStrings #-}
{-# LANGUAGE BlockArguments #-}

module Network.Server (startServer) where

import Web.Scotty
import Control.Monad.IO.Class (liftIO)
import Data.IORef (readIORef, modifyIORef')

import Node.State

import Validations.BlockValidation (isValidBlock)
import Network.Broadcast (broadcastBlock, broadcastTransaction)
import Network.Client (fetchChain)

import Consensus.Consensus (resolveChain)

import Types.PreBlock
import Types.Transaction
import Types.Block (Block(..))
import Types.Chain (Chain(..))
import Types.Mempool (Mempool(..))

import Storage.Storage (getChain, getLastBlock, addBlock)

import Types.Ledger
import Mempool.Mempool

import Network.Wai.Handler.Warp (runSettings, defaultSettings, setPort, setHost)
import Web.Scotty (scottyApp)

startServer :: NodeStateRef -> Int -> IO ()
startServer state port = do
  app <- scottyApp do

    get "/health" $
      text "OK"

    get "/chain" do
      chain <- liftIO $ getBlockchain state
      json (getChain chain)

    get "/peers" do
      ps <- liftIO $ getPeers state
      json ps

    post "/peers" do
      peer <- jsonData :: ActionM Peer
      liftIO $ addPeer state peer
      text "Peer added"

    post "/transactions" do
      tx <- jsonData :: ActionM Transaction

      chain <- liftIO $ getBlockchain state
      stateData <- liftIO $ readIORef state

      let baseLedger = buildLedger chain

      let Mempool mempoolTxs = mempool stateData
      let ledgerWithPending = foldl applyTx baseLedger mempoolTxs

      if isValidTx ledgerWithPending tx
        then do
          liftIO $ modifyIORef' state (\s ->
            s { mempool = addTransaction tx (mempool s) })

          ps <- liftIO $ getPeers state
          liftIO $ broadcastTransaction ps tx

          text "Transaction added"

        else text "Invalid transaction"

    post "/blocks" do
      newBlock <- jsonData :: ActionM Block

      stateData <- liftIO $ readIORef state
      let chain = blockchain stateData

      case getLastBlock chain of
        Nothing -> text "Empty chain"
        Just lastBlock -> do

          if isValidBlock lastBlock newBlock
            then do
              let newChain = addBlock newBlock chain

              liftIO $ modifyIORef' state (\s ->
                s { blockchain = newChain })

              ps <- liftIO $ getPeers state
              liftIO $ broadcastBlock ps newBlock

              text "Block added"

            else do
              ps <- liftIO $ getPeers state
              peerChains <- liftIO $ mapM fetchChain ps

              let peerChainsC = map Chain peerChains
              let resolved = resolveChain chain peerChainsC

              liftIO $ modifyIORef' state (\s ->
                s { blockchain = resolved })

              text "Sync ended"

  let settings =
        setPort port $
        setHost "0.0.0.0" defaultSettings

  runSettings settings app