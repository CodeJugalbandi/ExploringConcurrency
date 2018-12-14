﻿ GetPrice←{
     url←'http://national-stock-service.herokuapp.com:5000/stocks/',⍵
     z←#.HttpCommand.Get url
     0≠z.rc:('unable to get price for ',⍵,': ',⍕rc)⎕SIGNAL 11
     (⎕JSON z.Data).price
 }
