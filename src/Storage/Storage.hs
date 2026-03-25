module Storage.Storage where

import Block.Block

type Chain = Chain [Block]

getLastBlock :: Chain -> Block
getLastBlock [] = error "Empty chain"
getLastBlock chain = last chain