 Portfolio←{

     starttime←⎕AI[3]
     codes←'GOOG' 'AAPL' 'YHOO' 'MSFT' 'ORCL' 'AMZN' 'GOOG'
     quantity←10 20 30 40 40 50 90
     price←GetPrice #.IÏ codes
     networth←price+.×quantity
     ⎕←'Net worth: 'networth('elapsed ms: ',⍕⎕AI[3]-startime)
 }
