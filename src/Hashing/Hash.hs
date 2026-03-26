module Hashing.Hash where

import qualified Crypto.Hash.SHA256 as SHA256
import qualified Data.ByteString.Base16 as Base16
import qualified Data.ByteString.Char8 as BS

hashString :: String -> String
hashString str =
  BS.unpack (Base16.encode (SHA256.hash (BS.pack str)))