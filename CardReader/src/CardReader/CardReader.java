package CardReader;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CardReader
{
  public static boolean validSet(Element e)		//Function to see if card is legal in EDH. Allowing all sets except UNH and UGL
  {
    String banType = "vanguard scheme phenomenon conspiracy";	//These types of cards are not allowed
    String firstSet = e.getElementsByTagName("set").item(0).getTextContent();
    String validType = e.getElementsByTagName("type").item(0).getTextContent().toLowerCase();
    if ((firstSet.contains("UNH")) || (firstSet.contains("UGL"))) {		//Unhinged and Unglued are not allowed
      return false;
    }
    if (banType.contains(validType)) {
      return false;
    }
    if ((validType.startsWith("plane")) && (!validType.contains("planeswalker"))) {
      return false;
    }
    return true;
  }
  
  public static String colorIdentity(Element e)		//Find the color identity of a given card.
  {
    String clrs = "";
    char[] colorList = { 'W', 'U', 'B', 'R', 'G' };
    //Add the colors that Cockatrice considers the card to be. Works off MANA COST TO PLAY (?), does not consider text
    NodeList colors = e.getElementsByTagName("color");
    for (int tmp = 0; tmp < colors.getLength(); tmp++)
    {
      String clr = colors.item(tmp).getFirstChild().getTextContent();
      clrs = clrs + clr;
    }
    String txt = e.getElementsByTagName("text").item(0).getTextContent();
    int index = txt.indexOf('{');	//Within Cockatrice, all symbols are written as {W}, {U}, e.t.c
    while (index >= 0)	//Manually check the card text to look for hidden symbols
    {
      String symb = Character.toString(txt.charAt(index + 1));
      if ((new String(colorList).contains(symb)) && (!clrs.contains(symb))) {
        clrs = clrs + Character.toString(txt.charAt(index + 1));
      }
      index = txt.indexOf('{', index + 1);
    }	//Double checking the mana cost for correct colors. Some cards aren't correct?
    String mCost = e.getElementsByTagName("manacost").item(0).getTextContent();
    if ((!clrs.contains("W")) && (mCost.contains("W"))) {
      clrs = clrs + "W";
    }
    if ((!clrs.contains("R")) && (mCost.contains("R"))) {
      clrs = clrs + "R";
    }
    if ((!clrs.contains("G")) && (mCost.contains("G"))) {
      clrs = clrs + "G";
    }
    if ((!clrs.contains("B")) && (mCost.contains("B"))) {
      clrs = clrs + "B";
    }
    if ((!clrs.contains("U")) && (mCost.contains("U"))) {
      clrs = clrs + "U";
    }
    String cardType = e.getElementsByTagName("type").item(0).getTextContent().toLowerCase();
    if ((clrs.length() == 0) && (!cardType.equals("basic land")))
    {
      clrs = clrs + "None";		//Colorless cards
    }
    else if (cardType.equals("basic land"))
    {
      String cardName = e.getElementsByTagName("name").item(0).getTextContent().toLowerCase();
      if (cardName.contains("island")) {
        clrs = "U";
      } else if (cardName.contains("swamp")) {
        clrs = "B";
      } else if (cardName.contains("mountain")) {
        clrs = "R";
      } else if (cardName.contains("forest")) {
        clrs = "G";
      } else if (cardName.contains("plains")) {
        clrs = "W";
      }
    }
    return clrs;
  }
  
  public static boolean isLegendary(Element cmdr)	//Check for legendary creature card. Used for finding a commander.
  {
    boolean lgnd = false;
    String type = cmdr.getElementsByTagName("type").item(0).getTextContent().toLowerCase();
    if ((type.contains("legendary")) && (type.contains("creature"))) {
      lgnd = true;
    }
    return lgnd;
  }
  
  public static void main(String[] args)
  {
    boolean bcmdr = false;
    //The path for the xml file containing all MTG cards
    String path = System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Local" + File.separator + "Cockatrice" + File.separator + "Cockatrice" + File.separator + "cards.xml";
    Document document = null;	//Java is annoying. Either this, or wrap all code in try block.
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.parse(new File(path));
      document.getDocumentElement().normalize();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    NodeList temp = document.getElementsByTagName("cards");
    Element element = (Element)temp.item(0);
    NodeList cards = element.getElementsByTagName("card");
      
    int cmdrNum = ThreadLocalRandom.current().nextInt(0, cards.getLength());	//Start at a random point in the file to start searching
    while (!bcmdr)	//Loop to find commander to use
    {
      Node cmdr = cards.item(cmdrNum);
      Element eCmdr = (Element)cmdr;
      if ((validSet(eCmdr)) && (isLegendary(eCmdr))) {
        bcmdr = true;
      }
      if ((!bcmdr) && (cmdrNum + 1 != cards.getLength())) {		//Make sure the next card we check is still a valid card
        cmdrNum++;
      } else if (!bcmdr) {
        cmdrNum = 0;
      }
    }
    Node cmdr = cards.item(cmdrNum);
    Element eCmdr = (Element)cmdr;
    String CommanderName = eCmdr.getElementsByTagName("name").item(0).getTextContent();
    String Colors = colorIdentity(eCmdr);
      
    int cardNum = ThreadLocalRandom.current().nextInt(60, 70);	//Decide how many non-land cards to generate for the player
      
    ArrayList<String> fCards = new ArrayList<String>();
    for (int x = 0; x < cardNum; x++)
    {
      int tCardNum = ThreadLocalRandom.current().nextInt(0, cards.getLength());	//Start search at random point
      Node tempCard = cards.item(tCardNum);
      Element eTempCard = (Element)tempCard;
      if (validSet(eTempCard))
      {
        String tempType = eTempCard.getElementsByTagName("type").item(0).getTextContent().toLowerCase();
        //Not land, and not already added into our list of cards
        if ((!tempType.contains("land")) || (!fCards.contains(eTempCard.getElementsByTagName("name").item(0).getTextContent())))
        {
          String tempCardClr = colorIdentity(eTempCard);
          if ((Colors.contains(tempCardClr)) || (tempCardClr.equals("None")))
          {
            String cardName = eTempCard.getElementsByTagName("name").item(0).getTextContent();
            fCards.add(cardName);
          }
          else
          {
            x--;
          }
        }
        else
        {
          x--;
        }
      }
      else
      {
        x--;	//If it fails, must decrement x to 'search again' for a card
      }
    }
    int landNum = 99 - cardNum;
    for (int t = 0; t < landNum; t++)
    {
      int tLandNum = ThreadLocalRandom.current().nextInt(0, cards.getLength());
      Node tempLand = cards.item(tLandNum);
      Element eTempLand = (Element)tempLand;
      if (validSet(eTempLand))
      {
        String landType = eTempLand.getElementsByTagName("type").item(0).getTextContent().toLowerCase();
        if (landType.contains("land"))
        {
          String tempLandClr = colorIdentity(eTempLand);
          if ((Colors.contains(tempLandClr)) || (tempLandClr.equals("None")))
          {
        	//If not already in our card list, or if basic land, add it in.
            if ((landType.contains("basic")) || (!fCards.contains(eTempLand.getElementsByTagName("name").item(0).getTextContent())))
            {
              String landName = eTempLand.getElementsByTagName("name").item(0).getTextContent();
              fCards.add(landName);
            }
            else
            {
              t--;
            }
          }
          else {
            t--;
          }
        }
        else
        {
          t--;
        }
      }
      else
      {
        t--;
      }
    }
    //Start to create clipboard text
    String clipboardText = "//Random Deck\n\n//Your Commander is: " + CommanderName + "\n\n" + "1 " + CommanderName + "\n";
    //Insert full decklist into clipboard text so that Cockatrice can read it
    for (int i = 0; i < fCards.size(); i++) {
      clipboardText = clipboardText + "1 " + (String)fCards.get(i) + "\n";
    }
    //Add clipboard text to the clipboard of the user for easy pasting.
    StringSelection stringSelection = new StringSelection(clipboardText);
    Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
    clpbrd.setContents(stringSelection, null);
  }
}
