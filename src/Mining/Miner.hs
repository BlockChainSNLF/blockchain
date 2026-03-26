module Mining.Miner (startMiner) where

import Control.Concurrent (forkIO, threadDelay)
import Control.Monad (forever)
import Data.IORef (modifyIORef', readIORef)
import Hashing.Mining (mineBlock)
import Mempool.Mempool
import Network.Broadcast (broadcastBlock)
import Node.State
import Storage.Storage
import Types.Block (Block (..))
import Types.Ledger
import Types.Mempool (Mempool (..))
import Types.PreBlock

difficulty :: Int
difficulty = 3

blockSize :: Int
blockSize = 5

startMiner :: NodeStateRef -> IO ()
startMiner state = do
  _ <- forkIO (minerLoop state)
  return ()

minerLoop :: NodeStateRef -> IO ()
minerLoop state = forever $ do
  threadDelay (5 * 1000000)

  stateData <- readIORef state

  let chain = blockchain stateData
  let Mempool mempoolTxs = mempool stateData

  if null mempoolTxs
    then return ()
    else do
      let ledger = buildLedger chain

      let validTxs = filter (isValidTx ledger) mempoolTxs

      if null validTxs
        then return ()
        else do
          let selectedTxs = take blockSize validTxs

          case getLastBlock chain of
            Nothing -> return ()
            Just lastBlock -> do
              let pb =
                    PreBlock
                      { index = index (blockContent lastBlock) + 1,
                        timestamp = timestamp (blockContent lastBlock) + 1,
                        transactions = selectedTxs,
                        previousHash = hashValue lastBlock,
                        nonce = 0
                      }

              let newBlock = mineBlock difficulty pb

              modifyIORef'
                state
                ( \s ->
                    s
                      { blockchain = addBlock newBlock (blockchain s),
                        mempool = removeTransactions selectedTxs (mempool s)
                      }
                )

              putStrLn "Block mined"

              peerList <- getPeers state
              broadcastBlock peerList newBlock