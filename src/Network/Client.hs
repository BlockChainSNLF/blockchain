{-# LANGUAGE OverloadedStrings #-}

module Network.Client
  ( sendBlockToPeer
  , fetchChain
  ) where

import Node.State
import Types.Block (Block)
import Data.Aeson (encode, decode)
import Network.HTTP.Simple

sendBlockToPeer :: Peer -> Block -> IO ()
sendBlockToPeer peer block = do
  let url = peerToUrl peer ++ "/blocks"
  initReq <- parseRequest url

  let request =
        setRequestMethod "POST"
        $ setRequestHeader "Content-Type" ["application/json"]
        $ setRequestBodyJSON block
        $ initReq

  response <- httpNoBody request
  putStrLn $ "Sent block to " ++ url

sendTx :: Peer -> Transaction -> IO ()
sendTx peer tx = do
  let url = peerToUrl peer ++ "/transactions"
  req <- parseRequest url

  let request =
        setRequestMethod "POST"
        $ setRequestHeader "Content-Type" ["application/json"]
        $ setRequestBodyJSON tx
        $ req

  _ <- httpNoBody request
  return ()

fetchChain :: Peer -> IO [Block]
fetchChain peer = do
  let url = peerToUrl peer ++ "/chain"
  req <- parseRequest url

  response <- httpLBS req
  let body = getResponseBody response

  case decode body of
    Just chain -> return chain
    Nothing -> return []