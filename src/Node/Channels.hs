module Node.Channels where

import Control.Concurrent.STM
import Types.Block (Block)
import Types.Transaction (Transaction)

data NodeChannels = NodeChannels
  { inboundBlocks :: TQueue Block,
    inboundTransactions :: TQueue Transaction,
    outboundBlocks :: TQueue Block,
    outboundTransactions :: TQueue Transaction
  }

initializeChannels :: IO NodeChannels
initializeChannels = do
  inBlocks <- newTQueueIO
  inTxs <- newTQueueIO
  outBlocks <- newTQueueIO
  outTxs <- newTQueueIO

  return
    NodeChannels
      { inboundBlocks = inBlocks,
        inboundTransactions = inTxs,
        outboundBlocks = outBlocks,
        outboundTransactions = outTxs
      }