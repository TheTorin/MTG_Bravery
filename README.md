# MTG_Bravery
Create a completely random deck for use in Commander for Magic: The Gathering. Requires Cockatrice.

Once the program is run, the final decklist is copied onto the CLIPBOARD.
Simply paste the deck into Cockatrice via Deck Editor -> Load Deck From Clipboard

Notes:
Allows ALL SETS (except for UNH and UGL)
Uses Cockatrice's XML file containing all cards
Should work on all platforms (Untested, however)

KNOWN BUGS:
Program does not check for commanders with Partner. It is possible to receive only one Partner commander.
The program will sometimes give the wrong side of a flip card, since both sides are stored in the XML file (e.x Giving "Chalice of Death" instead of "Chalice of Life")
Related to above, the program will sometimes give BOTH sides of a flip card.
