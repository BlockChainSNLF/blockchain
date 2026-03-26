module Storage.Storage where
    
import Types.Block
import Types.Chain
import Blockchain.Genesis

initializeStorage :: Chain
initializeStorage = Chain [genesisBlock]

getLastBlock :: Chain -> Maybe Block
getLastBlock (Chain []) = Nothing
getLastBlock (Chain blocks) = Just (last blocks)

addBlock :: Block -> Chain -> Chain
addBlock block (Chain blocks) = Chain (blocks ++ [block])

getChain :: Chain -> [Block]
getChain (Chain blocks) = blocks

replaceChain :: Chain -> Chain -> Chain
replaceChain newChain _ = newChain


