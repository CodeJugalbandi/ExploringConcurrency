 SequentialInt←{
⍝ Sequential with interchanged sides
    xy←(⍵/i),⍪(⍵×⍵)⍴i←⍳⍵ ⍝ (↑,⍳⍵ ⍵) without nested arrays
    ((⌊=⊢)0.5*⍨+/2*⍨xy)⌿xy
⍝ v18.0 : ((⌊=⊢)(+/⍢*∘2)xy)⌿xy
 }
