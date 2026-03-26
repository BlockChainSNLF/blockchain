{-# LANGUAGE OverloadedStrings #-}

module Network.Client
  ( sendBlockToPeer,
    sendTx,
    fetchChain,
    sendPeer,
  )
where

import Data.Aeson (decode)
import Network.HTTP.Simple
import Node.State (Peer, peerToUrl)
import Types.Block (Block)
import Types.Transaction (Transaction)

sendBlockToPeer :: Peer -> Block -> IO ()
sendBlockToPeer peer block = do
  let url = peerToUrl peer ++ "/blocks"
  initReq <- parseRequest url

  let request =
        setRequestMethod "POST" $
          setRequestHeader "Content-Type" ["application/json"] $
            setRequestBodyJSON block $
              initReq

  _ <- httpNoBody request
  putStrLn $ "Sent block to " ++ url

sendTx :: Peer -> Transaction -> IO ()
sendTx peer tx = do
  let url = peerToUrl peer ++ "/transactions"
  req <- parseRequest url

  let request =
        setRequestMethod "POST" $
          setRequestHeader "Content-Type" ["application/json"] $
            setRequestBodyJSON tx $
              req

  _ <- httpNoBody request
  pure ()

fetchChain :: Peer -> IO [Block]
fetchChain peer = do
  let url = peerToUrl peer ++ "/chain"
  req <- parseRequest url

  response <- httpLBS req
  let body = getResponseBody response

  case decode body of
    Just chain -> return chain
    Nothing -> do
      putStrLn $ "Failed to decode chain from " ++ url
      return []

sendPeer :: Peer -> Peer -> IO ()
sendPeer target newPeer = do
  let url = peerToUrl target ++ "/peers"
  req <- parseRequest url

  let request =
        setRequestMethod "POST" $
          setRequestHeader "Content-Type" ["application/json"] $
            setRequestBodyJSON newPeer $
              req

  _ <- httpNoBody request
  putStrLn $ "Sent peer " ++ show newPeer ++ " to " ++ url