 Portfolio←{
     codes←'GOOG' 'AAPL' 'YHOO' 'MSFT' 'ORCL' 'AMZN' 'GOOG'
     quantity←10 20 30 40 40 50 90

     starttime←⎕AI[2]
     price←GetPrice¨codes
     networth←price+.×quantity
     ⎕←'Sequential net worth: 'networth('elapsed ms: ',⍕⎕AI[2]-starttime)

     starttime←⎕AI[2]
     price←GetPrice #.IÏ codes
     networth←price+.×quantity
     ⎕←'Parallel net worth: 'networth('elapsed ms: ',⍕⎕AI[2]-starttime)
 }
