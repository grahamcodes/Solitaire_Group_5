import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;


/**
 * CardListener.java
 *
 * @author  Jake
 * @version Mar 18, 2014
 */
public class CardListener extends MouseInputAdapter {
  //=====================================================================
  private int score;
  private int mode;
  //=====================================================================
  private GameBoard board;
  
  private Deck deck;
  
  private Pile[] tableauPiles, foundationPiles;
  private Pile   stockpile; // the pile of the cards the user has drawn from the deck
  private ScoreBoard scoreboard;
  
  private Pile origPile;
  
  private int lastX, lastY;
  
  /**
   * Constructor for a Card Listener
   * @param board the game board in which to manipulate when the user clicks/drags/drops cards
   */
  public CardListener(GameBoard board) {
    this.board = board;
    this.scoreboard = board.getScoreBoard();
    this.mode = board.getMode();
    deck = board.getDeck();
    tableauPiles = board.getTableauPiles();
    foundationPiles = board.getFoundationPiles();
    stockpile  = board.getStockpile();
    lastX = 0;
    lastY = 0;
    origPile = null;
    this.score = scoreboard.getScore();
  }
  
  @Override
  public void mouseClicked(MouseEvent e) {
//    if (e.getClickCount() == 2 && !e.isConsumed()) {
//      e.consume();
//      Pile p = board.selectedPile;
//      if (Card.getSuitIndex(p.getCardOnBottom().getSuit()) == Card.getSuitIndex(foundationPiles[i].getCardOnTop().getSuit())) {
//        // the faces must be in ascending order
//        if (Card.getFaceIndex(p.getCardOnBottom().getFace()) == Card.getFaceIndex(foundationPiles[i].getCardOnTop().getFace()) + 1) {
//          foundationPiles[i].addToPile(p.getCardOnBottom());
//          origPile.turnTopCardUp();
//          validDrop = true;
//        }
//      }
//    }
//    stockpile.updateTop3();
//    board.selectedPile = null;
//    origPile = null;
//    // Check for victory
//    if (foundationPiles[0].size() == 13
//            && foundationPiles[1].size() == 13
//            && foundationPiles[2].size() == 13
//            && foundationPiles[3].size() == 13) {
//      System.out.println("YOU WIN"); // <-----------------------------------VICTORY!!!
//    }
//    board.repaint();
  }
  
  @Override
  /**
   * Selects a card when it is clicked
   */
  public void mousePressed(MouseEvent e) {
    board.selectedPile = getPileClicked(e);
    if (board.selectedPile != null) {
      lastX = e.getX();
      lastY = e.getY();
    } else { // if no pile was clicked, check if the deck was
      if (deck.hasBeenClicked(e)) {
        if (deck.size() == 0) {
          // Deck cycle does not continue if playing with Vegas rules.
          if (board.getMode() == 2) {
            scoreboard.updateVegasFinalLabel();
          } else {
            deck.addToDeck(stockpile);
          }
        } else {
          // Conditions for flip 3 or flip 1 rules.
          if (board.getMode() == 3) {
            for (int i = 0; i < 3; i++) {
              Card c = deck.getCardOnTop();
              if (c != null) {
                stockpile.addToPile(c);
                deck.removeCardOnTop();
              }
            }
          } else {
            Card c = deck.getCardOnTop();
            if (c != null) {
              stockpile.addToPile(c);
              deck.removeCardOnTop();
            }
          }
          stockpile.turnAllCardsUp();
        }
      }
    }
    board.repaint();
  }
  
  @Override
  /**
   * Moves the card as it is dragged by the mouse
   */
  public void mouseDragged(MouseEvent e) {
    if (board.selectedPile != null) {
      int newX = board.selectedPile.getX() + (e.getX() - lastX);
      int newY = board.selectedPile.getY() + (e.getY() - lastY);
      board.selectedPile.setLocation(newX, newY);
      lastX = e.getX();
      lastY = e.getY();
    }
    board.repaint();
  }
  
  @Override
  /**
   * Drops a card on a pile only if it has the right face and color
   */
  public void mouseReleased(MouseEvent e) {
    Pile p = board.selectedPile;
    if (p != null) {
      boolean validDrop = false;
      // check to see if the selectedPile has been dropped on a main pile
      for (int i = 0; i < tableauPiles.length; i++) {
        if (tableauPiles[i].droppedOnPile(p)) {
          if (tableauPiles[i].isEmpty()) {
            if (p.getCardOnBottom().getFace().equals("K")) {
              tableauPiles[i].addToPile(p);
              // This handles 5 points for a king onto an empty tableau from stockpile
              if (mode != 2) {
                if (origPile == stockpile) {
                  score += 5;
                  scoreboard.setScore(score);
                  scoreboard.updateLabel();
                }
              }
              origPile.turnTopCardUp();
              validDrop = true;
            }
          } else { // if not empty
            // only add if the colors are NOT the same
            if (!p.getCardOnBottom().getColor().equals(tableauPiles[i].getCardOnTop().getColor())) {
              // now ensure the faces are descending
              if (Card.getFaceIndex(p.getCardOnBottom().getFace()) + 1 == Card.getFaceIndex(tableauPiles[i].getCardOnTop().getFace())) {
                tableauPiles[i].addToPile(p);
                // This area handles 5 points for placing a non-king on tableau from the stockpile
                // 5 points for taking from the stockpile
                if (mode != 2) {
                  if (origPile == stockpile) {
                    score += 5;
                    scoreboard.setScore(score);
                    scoreboard.updateLabel();
                  }

                  // 5 points for flipping a tableau card
                  if (origPile != stockpile
                          && !origPile.isEmpty()
                          && origPile != foundationPiles[0]
                          && origPile != foundationPiles[1]
                          && origPile != foundationPiles[2]
                          && origPile != foundationPiles[3]
                          && origPile.getCardOnTop().faceDown) {
                    score += 5;
                    scoreboard.setScore(score);
                    scoreboard.updateLabel();
                  }
                }
                origPile.turnTopCardUp();
                validDrop = true;
                break;
              }
            }
          } // end isEmpty() condition
        }
      }
      
      // if the drop is still invalid, check if it's been dropped on a suit pile instead
      if (!validDrop) {
        for (int i = 0; i < foundationPiles.length; i++) {
          if (foundationPiles[i].droppedOnPile(p)) {
            if (foundationPiles[i].isEmpty()) {
              if (p.size() == 1) {
                if (p.getCardOnBottom().getFace().equals("A")) {
                  foundationPiles[i].addToPile(p.getCardOnBottom());

                  // 10 points for an ace to the suit pile
                  if (origPile != foundationPiles[0]
                          && origPile != foundationPiles[1]
                          && origPile != foundationPiles[2]
                          && origPile != foundationPiles[3]) {
                    if (origPile != stockpile
                            && !origPile.isEmpty()
                            && origPile.getCardOnTop().faceDown) {
                      if (mode != 2) {
                        score += 15;
                        scoreboard.setScore(score);
                        scoreboard.updateLabel();
                      } else {
                        score += 5;
                        scoreboard.setScore(score);
                        scoreboard.updateVegasLabel();
                      }
                    } else {
                      if (mode != 2) {
                        score += 10;
                        scoreboard.setScore(score);
                        scoreboard.updateLabel();
                      } else {
                        score += 5;
                        scoreboard.setScore(score);
                        scoreboard.updateVegasLabel();
                      }
                    }
                  }
                  origPile.turnTopCardUp();
                  validDrop = true;

                }
              }
            } else {
              if (p.size() == 1) { // only single cards can be added to suit piles
                // the suits must be the same for cards being added
                if (Card.getSuitIndex(p.getCardOnBottom().getSuit()) == Card.getSuitIndex(foundationPiles[i].getCardOnTop().getSuit())) {
                  // the faces must be in ascending order
                  if (Card.getFaceIndex(p.getCardOnBottom().getFace()) == Card.getFaceIndex(foundationPiles[i].getCardOnTop().getFace()) + 1) {
                    foundationPiles[i].addToPile(p.getCardOnBottom());

                    // 10 points for a non-ace to the suit pile
                    if (origPile != foundationPiles[0]
                            && origPile != foundationPiles[1]
                            && origPile != foundationPiles[2]
                            && origPile != foundationPiles[3]) {
                      if (origPile != stockpile
                              && !origPile.isEmpty()
                              && origPile.getCardOnTop().faceDown) {
                        if (mode != 2) {
                          score += 15;
                          scoreboard.setScore(score);
                          scoreboard.updateLabel();
                        } else {
                          score += 5;
                          scoreboard.setScore(score);
                          scoreboard.updateVegasLabel();
                        }
                      } else {
                        if (mode != 2) {
                          score += 10;
                          scoreboard.setScore(score);
                          scoreboard.updateLabel();
                        } else {
                          score += 5;
                          scoreboard.setScore(score);
                          scoreboard.updateVegasLabel();
                        }
                      }
                    }
                    origPile.turnTopCardUp();
                    validDrop = true;
                  }
                }
              }
            } // end isEmpty() condition
            
            
          }
        }
      }
      
      if (!validDrop) {
        if (p.size() == 1) {
          origPile.addToPile(p.getCardOnBottom());
        } else {
          origPile.addToPile(p);
        }
      }
    }
    stockpile.updateTop3();
    board.selectedPile = null;
    origPile = null;
    // Check for victory
    if (foundationPiles[0].size() == 13
            && foundationPiles[1].size() == 13
            && foundationPiles[2].size() == 13
            && foundationPiles[3].size() == 13) {
      if (mode != 2) {
        scoreboard.updateVictoryLabel();
      }
    }
    board.repaint();
  }
  
  @Override
  public void mouseMoved(MouseEvent e) {
    
  }
  
  /**
   * Returns the card that was clicked or null if no card was clicked
   * @param e the mouse event to check
   * @return the card that was clicked or null if no card was clicked
   */
  private Pile getPileClicked(MouseEvent e) {
    Pile clicked = null;
    origPile = null;
    // check the tableau piles and then the foundation piles
    for (int i = 0; i < tableauPiles.length; i++) {
      if ((clicked = tableauPiles[i].pileHasBeenClicked(e)) != null) {
        origPile = tableauPiles[i];
        return clicked;
      }
    }
    
    for (int i = 0; i < foundationPiles.length; i++) {
      if ((clicked = foundationPiles[i].pileHasBeenClicked(e)) != null) {
        origPile = foundationPiles[i];
        return clicked;
      }
    }
    
    if ((clicked = stockpile.pileHasBeenClicked(e)) != null) origPile = stockpile;
    return clicked;
  }
  
}