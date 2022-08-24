package Pandemic.player;
import java.util.Random;
import java.util.Scanner;

import Pandemic.Gameboard.GameBoard;
import Pandemic.Gameboard.SimulatePandemic;
import Pandemic.cities.City;
import Pandemic.variables.Disease;
import Pandemic.variables.Piece;
import Pandemic.variables.Variables;
import Pandemic.actions.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;


public class Player implements Cloneable{

  int maxStations =6;
  int thresholdQuaranteeMove =15;
  String playerName;
  int tactic;
  String playerRole;
  GameBoard pandemicBoard;
  Piece playerPiece;
  int playerAction; //number of action per turn for each player
  boolean activePlayer;
  ArrayList<City>  hand ; //hand_cards maybe from action
  String[] possibleColour = {"Red","Blue","Yellow","Black"};
  ArrayList<Action> suggestions = new ArrayList<Action>();
  ArrayList<Action> availableMoves = new ArrayList<Action>();   // list of available moves for  player
  int redArea;
  int blueArea;
  int yellowArea;
  int blackArea;
  City MaxRed;
  City MaxBlue;
  City MaxYellow;
  City MaxBlack;
  boolean redStation =false;	// If there is a station in at least one red city
  boolean blueStation =true;	// If there is a station in at least one blue city (starts with true because at the beginning of the game Atlanta has a research station)
  boolean blackStation =false;	// If there is a station in at least one black city
  boolean yellowStation =false;	// If there is a station in at least one yellow city
  
  boolean mustMove = false; 	// Flag to know if i have to make a shutter or charter flight
  boolean mustBuild = false;	// Flag to know if i have to build a station
  
  static int threshold = 100;
  
  /*
   * Constructor for objects of class Player
   */
  public Player(String pName,String pRole)
  {
      playerName = pName;
      hand = new ArrayList<City>();
      playerAction = 4;
      tactic = 50;
      playerRole = pRole;
  }

  public void setGameBoard(GameBoard currentBoard) { this.pandemicBoard = currentBoard;  }

  public void setPlayerPiece(Piece newPiece) { playerPiece = newPiece; }
  
  public ArrayList<Action> getSuggestions() { return suggestions; }

  public void setSuggestions(ArrayList<Action> suggestions) {this.suggestions = suggestions; }

  /*
  *draw @param numberOfCards  from playerPile and check if it is epidemic
  *and call @resolveEpidemic() or draw to  @hand()
  */
  public void drawCard(int numberOfCards, boolean test1)
  {
      // draws a card for the player from the board
      for (int i = 0; i<numberOfCards; i++)
      {
    	  if (test1==true)
    	  {
    		  // create instance of Random class 
    	      Random rand = new Random(); 
    	      int rand_int1 = rand.nextInt(pandemicBoard.playerPile.size()); 
    		  while(pandemicBoard.playerPile.get(rand_int1).equals(Variables.isEpidemic)) {
    			  rand_int1 = rand.nextInt(pandemicBoard.playerPile.size()); 
    			  Collections.shuffle(pandemicBoard.playerPile);
    		  }
    		//adds a new card to the players hand.
         	 hand.add((City) pandemicBoard.playerPile.get(rand_int1));
         	 pandemicBoard.playerPile.remove(rand_int1);//remove the card from PlayerDeck
         	 System.out.println(this.getPlayerName() + " draws a card");
    	  }
    	  else
    	  {
    		 if (pandemicBoard.playerPile.size()!= 0)
    		 {
	        	 //first element from array list PlayerPile
	              if (pandemicBoard.playerPile.get(0).equals(Variables.isEpidemic)) {            	  
	            	  System.out.println("-----EPIDEMIC DRAWN!-----");
	                  pandemicBoard.resolveEpidemic();//follow the steps for epidemic event
	                  pandemicBoard.playerPile.remove(0);
	                  break;
	              }
	              else 
	              {
	            	 //adds a new card to the players hand.
	            	 hand.add((City) pandemicBoard.playerPile.get(0));
	              }
	              pandemicBoard.playerPile.remove(0);//remove the card from PlayerDeck
	              System.out.println(this.getPlayerName() + " draws a card");
	          }
	          else
	          {
	              System.out.println("no more cards left");
	          }
    	  }
      }
  }


  /*
   * Count how many cards with a specific colour 
   * player has in his hands 
   * @param colour
   */
  public int getCountXCards(String colour)
  {
      int toReturn = 0;
      for (int i = 0 ; i < hand.size(); i++)
      {
          if (hand.get(i).getColour().equals(colour))
          {
              toReturn ++;
          }
      }
      return toReturn;
  }
  
  /*
   * remove a card from hand ,
   * then calls methods to put it in the discard pile()
   * and remove the card from the hand.
   */
  public void discardCard(String cardName)
  {
    int toDiscard =0;
	for (int i = 0; i < getHand().size(); i++)
	{
	   if (cardName.equals(getHand().get(i).getName())){
	    //System.out.println("found matching card in hand");
		toDiscard=i;   
	   }
	}
	pandemicBoard.addPlayerCardDiscard(hand.get(toDiscard));//remove from playerDeck to put in PlayerDiscardDeck
    hand.remove(toDiscard);
  }
  
  //discard all the cards needed for cure
  public void discardCureCards(String colour,int numberToDiscard)
  {
      for (int i = 0 ; i < numberToDiscard ; i ++)
      {
          for (int x = 0 ; x < hand.size(); x++ )
          {
              if (hand.get(x).getColour().equals(colour))
              {
                  discardCard(hand.get(x).getName());
                  break;
              }
          }
      }
  }
  
  // get PlayerAction 
  public int getPlayerAction(){ return playerAction; }
  
  // decrease the playerAction (Max=4)
  public void decreasePlayerAction(){ playerAction--;}

  // sets a players action back to 4
  public void resetPlayerAction() { playerAction=4;  }

  // return the name of player
  public String getPlayerName() { return playerName; }

  // Returns an array with the players cards in hand
  public ArrayList<City> getHand() { return hand;    }
  
  public String getPlayerRole() {return playerRole;	  }

  public void setPlayerRole(String playerRole) {this.playerRole = playerRole; }

  public Piece getPlayerPiece() { return playerPiece;	}    
  
  /**********************************************************************************
  *******These are the main (7+4) methods used to control the players actions********
  **********************************************************************************/
  
//Build research station as OPERATIONS_EXPERT

  
  //Build research station
  public boolean buildResearchStation ()
  {
	 if (playerAction>0) { 
	  buildResearchStation tmp = new buildResearchStation(playerPiece.getLocation(),getHand());
	  if (playerRole.equals("OPERATIONS_EXPERT") && !Variables.CITY_WITH_RESEARCH_STATION.contains(playerPiece.getLocation())) 
	  {
		  Variables.ADD_CITY_WITH_RESEARCH_STATION(playerPiece.getLocation());
          decreasePlayerAction();
          System.out.println("building a research station in " + playerPiece.getLocation().getName());
          suggestions.add(tmp);
          return true;
	  }
	  else
	  {
	      if (tmp.isaLegalMove())
	      {
	          discardCard(playerPiece.getLocation().getName());
	          Variables.ADD_CITY_WITH_RESEARCH_STATION(playerPiece.getLocation());
	          decreasePlayerAction();
	          //System.out.println("building a research station in " + playerPiece.getLocation().getName());
	          suggestions.add(tmp);
	          return true;
	      }
	  }
	 }
	 
      return false;
  }
  
//Treat disease action
 public boolean treatDisease (City location, String colour)
 {
	if (playerAction>0) {  
	 treatDisease tmp = new treatDisease(location,colour);
	 if (playerRole.equals("MEDIC")) {
	     if(tmp.isaLegalMove()==true && location == playerPiece.getLocation())
	     {
	    	 System.out.println("Removing all " + colour + " cube from " + location.getName());
	    	 while(location.getCubeColour(colour)!=0) {
	        	 location.removeCube(colour);
	             pandemicBoard.addCube(colour);//add in pool of cube             
	        }
	         decreasePlayerAction();
	         suggestions.add(tmp);
	         return true;
	     }
	 }
	 else {
		 if(tmp.isaLegalMove()==true && location == playerPiece.getLocation())
	     {
	         System.out.println("Removing a " + colour + " cube from " + location.getName());
	         location.removeCube(colour);
	         pandemicBoard.addCube(colour);//add in pool of cube
	         decreasePlayerAction();
	         suggestions.add(tmp);
	         return true;
	     }
	 }
	}
     return false;
 }

  // Drive action
  public boolean driveCity (City location, City destination)
  {
	if (playerAction>0) { 
	  System.out.println(getPlayerName() + " current location is in " + location);
	  System.out.println("and he wants to go in " + destination);
	  // System.out.println(location.getMaxCube());
	  // System.out.println(destination.getMaxCube());
      // System.out.println("attempting to move " + getPlayerName() + " to " + destination.getName() + " from "+ location.getName());
	  driveCity tmp = new driveCity(location,destination);
      if (tmp.isaLegalMove())
      {    	  
          System.out.println(getPlayerName() + " drives from " + location.getName() + " to " + destination.getName() + ".");
          playerPiece.setLocation(destination);
          decreasePlayerAction();
          suggestions.add( (driveCity) tmp);
          return true;
      }
      else 
      {
          System.out.println("the location isn't connected");
      }
	}
      return false;
  }
  
  // Charter Flight action
  public boolean charterFlight(City location, City destination)
  {
	if (playerAction>0) { 
	  // System.out.println(getPlayerName() + " wants to flying from " + 
	  // location.getName() + " to "+ destination.getName() + 
	  // " on a charter flight");
	  charterFlight tmp = new charterFlight(location,getHand(),destination);
      if (tmp.isaLegalMove() && playerPiece.getLocation().equals(location))
      {
          System.out.println(getPlayerName() + " takes a charter flight to " + 
          destination.getName() + " from " + location.getName() );
          discardCard(location.getName());
          playerPiece.setLocation(destination);
          decreasePlayerAction();
          suggestions.add(tmp);
          return true;
      }
	}
      return false;
  }
  
  //Direct flight
  public boolean directFlight(City location, City destination)
  {
	 if (playerAction>0) { 
	// System.out.println(getPlayerName() + " wants to flying from " + 
	// location.getName() + " to "+ destination.getName() + 
	// " on a direct flight");
	  directFlight tmp = new directFlight(destination,getHand());
	  if (tmp.isaLegalMove())
	  {
		  System.out.println(getPlayerName() + " takes a direct flight to " + 
		  destination.getName() + " from " + location.getName() );
		  discardCard(destination.getName());
		  playerPiece.setLocation(destination);
		  decreasePlayerAction();
		  suggestions.add(tmp);
          return true;
	  }
	 }
	  return false;
  }
  
  //ShuttleFlight
  public boolean shuttleFlight(City location, City destination)
  {
	 if (playerAction>0) { 
	// System.out.println(getPlayerName() + " wants to flying from " + 
	// location.getName() + " to "+ destination.getName() + 
	// " on a shuttle flight");
	 shuttleFlight tmp = new shuttleFlight(location,destination);
	 if(tmp.isaLegalMove())
	 {
		 System.out.println(getPlayerName() + " takes a shuttle flight to " + 
		 destination.getName() + " from " + location.getName() );
		 playerPiece.setLocation(destination);
		 decreasePlayerAction();
		 suggestions.add(tmp);
         return true;
	 }
	}
	 return false;
  }
  
  // Discover Cure action
  public boolean discoverCure(City location, String colour)
  { 
	if (playerAction>0) { 
	 discoverCure tmp = new discoverCure(location,getHand(),colour);
	 if (playerRole.equals("SCIENTIST")) {
	    if (tmp.isaLegalMove(1))
		{
		   System.out.println("Its possible to discover a cure");
		   discardCureCards(colour,(pandemicBoard.getNeededForCure()-1));
	       pandemicBoard.cureDisease(colour);
	       decreasePlayerAction();
	       suggestions.add(tmp);
	       return true;
		}
	 }
	 else
	 {
		if (tmp.isaLegalMove(0))
		{
		   System.out.println("Its possible to discover a cure");
		   discardCureCards(colour,pandemicBoard.getNeededForCure());
	       pandemicBoard.cureDisease(colour);
	       decreasePlayerAction();
	       suggestions.add(tmp);
	       return true;
		}
	 }
	}
	 return false;
  }
  
  
  /*
   *Below are implemented
   *1) --> OPERATIONS_EXPERT special ability (build research station without to discard a card) is implemented as if in the @BuildResearchStaion method
   *2) --> MEDIC special ability  (treat all the cubes of a specific colour) implemented in @treatDisease as if statement
   *3) --> SCIENTIST special ability is implemented as if in the classic cure disease
   *4) --> QUARANTINE_SPECIALIST special ability is implemented in @infectCityPhase in @GameBoard class 
   */  
  
  //---------------------------------------------------------------------------------
  /**********************************************************************************
  ***************** These methods are used for AI controlled players.****************
  **********************************************************************************/
  //---------------------------------------------------------------------------------
  
  
  public void makeDecision(ArrayList<City>  friend_hand,String Role, City friend_location)
  {
	  String currentRole = this.playerRole;
	  ArrayList<City> keepCards = new ArrayList<City>();
	  
	  for(int i=0; i<this.hand.size(); i++)		// keep the Cards of Player who currently Suggests
	  {
		  keepCards.add(this.hand.get(i));
	  }
	  //take Variables.Suggestions and build model for others player
      System.out.println(this.getPlayerName() + " is thinking..... ");
      
	  
      if(Role.equals("MEDIC"))
      {
    	  if(this.playerRole.equals("MEDIC")) // Decides for himself
    	  {
    		  System.out.println("===================================");
    		  int result = medicEvaluate();
    		  if(result == 0) { 
    			  System.out.println("Jack has the best suggestion"); 
    			  followSuggestion(0);
    		  }
    		  else if(result == 1) { 
    			  System.out.println("Unabomber has the best suggestion"); 
    			  followSuggestion(1);
    		  }
    		  else if(result == 2) { 
    			  System.out.println("Jesus has the best suggestion"); 
    			  followSuggestion(2);
    		  }
    		  else { 
    			  System.out.println("No good suggestions! Going to decide myself");
    			  medicStrategy();
        		  medicStrategy();
        		  medicStrategy();
        		  medicStrategy();
    		  }
    		  System.out.println("===================================");
    		  
    	  }
    	  else
    	  {
    		  double rand = Math.random();
    		  playerPiece.setLocation(friend_location);
    		  this.hand.clear();
    		  this.playerRole = "MEDIC";
    		  for(int i=0; i<friend_hand.size(); i++){
    			  this.hand.add(friend_hand.get(i));
    		  }
    		  if(rand >= 0.4)
    		  {
    			  System.out.println("medicStrategy");
    			  medicStrategy();
        		  medicStrategy();
        		  medicStrategy();
        		  medicStrategy();
    		  }
    		  else
    		  {
    			  System.out.println("makeSuggestion");
    			  makeSuggestion();
    			  makeSuggestion();
    			  makeSuggestion();
    			  makeSuggestion();
    		  }
    		  
    		  this.playerRole = currentRole;
    		  this.hand.clear();
    		  for(int i=0; i<keepCards.size(); i++){
    			  this.hand.add(keepCards.get(i));
    		  }
    		  keepCards.clear();
    	  }
      }
      else if(Role.equals("SCIENTIST"))
      {
    	  if(this.playerRole.equals("SCIENTIST")) // Decides for himself
    	  {
    		  System.out.println("===================================");
    		  int result = scientistEvaluate();
    		  if(result == 1) { 
    			  System.out.println("Unabomber has the best suggestion"); 
    			  followSuggestion(1);
    		  }
    		  else if(result == 2) { 
    			  System.out.println("Jesus has the best suggestion"); 
    			  followSuggestion(2);
    		  }
    		  else if(result == 3) { 
    			  System.out.println("Zodiac has the best suggestion"); 
    			  followSuggestion(3);
    		  }
    		  else { 
    			  System.out.println("No good suggestions! Going to decide myself");
    			  scientistStrategy();
    			  scientistStrategy();
    			  scientistStrategy();
    			  scientistStrategy();
    		  }
    		  System.out.println("===================================");  
    		  ArrayList<City> stations = pandemicBoard.getResearchLocations();
    		  System.out.println("Research stations");
    		  for(int i=0; i<stations.size(); i++)
    		  {
    			  System.out.println((i+1)+") "+stations.get(i));
    		  }
    		  evaluateBoard();
    	  }
    	  else
    	  {
    		  double rand = Math.random();
    		  playerPiece.setLocation(friend_location);
    		  this.hand.clear();
    		  this.playerRole = "SCIENTIST";
    		  for(int i=0; i<friend_hand.size(); i++){
    			  this.hand.add(friend_hand.get(i));
    		  }
    		  if(rand > 0.2)
    		  {
    			  System.out.println("scientistStrategy");
    			  scientistStrategy();
        		  scientistStrategy();
        		  scientistStrategy();
        		  scientistStrategy();
    		  }
    		  else
    		  {
    			  System.out.println("makeSuggestion");
    			  makeSuggestion();
    			  makeSuggestion();
    			  makeSuggestion();
    			  makeSuggestion();
    		  }
    		  this.playerRole = currentRole;
    		  this.hand.clear();
    		  for(int i=0; i<keepCards.size(); i++){
    			  this.hand.add(keepCards.get(i));
    		  }
    		  keepCards.clear();
    	  }
      }
      else if(Role.equals("QUARANTINE_SPECIALIST"))
      {
    	  if(this.playerRole.equals("QUARANTINE_SPECIALIST")) // Decides for himself
    	  {
    		  System.out.println("===================================");
    		  int result = quarantineEvaluate();
    		  if(result == 0) { 
    			  System.out.println("Jack has the best suggestion"); 
    			  followSuggestion(0);
    		  }
    		  else if(result == 2) { 
    			  System.out.println("Jesus has the best suggestion"); 
    			  followSuggestion(2);
    		  }
    		  else if(result == 3) { 
    			  System.out.println("Zodiac has the best suggestion"); 
    			  followSuggestion(3);
    		  }
    		  else { 
    			  System.out.println("No good suggestions! Going to decide myself");
    			  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"QUARANTINE_SPECIALIST",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"QUARANTINE_SPECIALIST",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"QUARANTINE_SPECIALIST",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"QUARANTINE_SPECIALIST",this.hand);
        		  QuarantineSpecialistMove();
        		  availableMoves.clear();
    		  }
    		  System.out.println("===================================");
    	  }
    	  else
    	  {
    		  double rand = Math.random();
    		  playerPiece.setLocation(friend_location);
    		  this.hand.clear();
    		  this.playerRole = "QUARANTINE_SPECIALIST";
    		  for(int i=0; i<friend_hand.size(); i++){
    			  this.hand.add(friend_hand.get(i));
    		  }
    		  
    		  if(rand >= 0.5)
    		  {
    			  System.out.println("Quarantine Specialist Moves");
    			  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"QUARANTINE_SPECIALIST",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"QUARANTINE_SPECIALIST",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"QUARANTINE_SPECIALIST",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"QUARANTINE_SPECIALIST",this.hand);
        		  QuarantineSpecialistMove();
        		  availableMoves.clear();
    		  }
    		  else
    		  {
    			  System.out.println("makeSuggestion");
    			  makeSuggestion();
    			  makeSuggestion();
    			  makeSuggestion();
    			  makeSuggestion();
    		  }
    		  
    		  
    		  this.playerRole = currentRole;
    		  this.hand.clear();
    		  for(int i=0; i<keepCards.size(); i++){
    			  this.hand.add(keepCards.get(i));
    		  }
    		  keepCards.clear();
    	  }
      }
      else if(Role.equals("OPERATIONS_EXPERT"))
      {
    	  if(this.playerRole.equals("OPERATIONS_EXPERT")) // Decides for himself
    	  {
    		  System.out.println("===================================");
    		  int result = operationEvaluate();
    		  if(result == 0) { 
    			  System.out.println("Jack has the best suggestion"); 
    			  followSuggestion(1);
    		  }
    		  else if(result == 1) { 
    			  System.out.println("Unabomber has the best suggestion"); 
    			  followSuggestion(2);
    		  }
    		  else if(result == 3) { 
    			  System.out.println("Zodiac has the best suggestion"); 
    			  followSuggestion(3);
    		  }
    		  else { 
    			  System.out.println("No good suggestions! Going to decide myself");
    			  
    			  /*
    			  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"OPERATIONS_EXPERT",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"OPERATIONS_EXPERT",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"OPERATIONS_EXPERT",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"OPERATIONS_EXPERT",this.hand);
        		  QuarantineSpecialistMove();
        		  */
    			  operationExpertStrategy();
    			  operationExpertStrategy();
    			  operationExpertStrategy();
    			  operationExpertStrategy();
    		  }
    		  System.out.println("===================================");
    	  }
    	  else
    	  {
    		  double rand = Math.random();
    		  playerPiece.setLocation(friend_location);
    		  this.hand.clear();
    		  this.playerRole = "OPERATIONS_EXPERT";
    		  for(int i=0; i<friend_hand.size(); i++){
    			  this.hand.add(friend_hand.get(i));
    		  }
    		  
    		  if(rand > 0.3)
    		  {
    			  /*
    			  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"OPERATIONS_EXPERT",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"OPERATIONS_EXPERT",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"OPERATIONS_EXPERT",this.hand);
        		  QuarantineSpecialistMove();
        		  
        		  availableMoves.clear();
        		  findAvailableMoves2(this.playerPiece.location,"OPERATIONS_EXPERT",this.hand);
        		  QuarantineSpecialistMove();
        		  */
    			  System.out.println("Operation expert Strategy");
    			  operationExpertStrategy();
    			  operationExpertStrategy();
    			  operationExpertStrategy();
    			  operationExpertStrategy();
    		  }
    		  else
    		  {
    			  System.out.println("makeSuggestion");
    			  makeSuggestion();
    			  makeSuggestion();
    			  makeSuggestion();
    			  makeSuggestion();
    		  }
    		  
    		  
    		  this.playerRole = currentRole;
    		  this.hand.clear();
    		  for(int i=0; i<keepCards.size(); i++){
    			  this.hand.add(keepCards.get(i));
    		  }
    		  keepCards.clear();
    	  }
      }
      else
      {
    	  System.out.println("Never gets here");
    	  boolean checkCure = checkCureWorthIt(0);
          
          if (checkCure)
          {
              System.out.println("might be worth trying to find a cure.");
              checkTryCure();
              
          }
         
          if (!checkCure && (getDistanceResearch() > 3) && (tactic > 0) )
          {
        	  
        	  
              tactic--;
              System.out.print("They are far enough from a research station to consider building one.");
              if (!buildResearchStation())
              {
                  System.out.println(" Can't find the required card.");
                  rollDice();
              }
          }
          else if (!checkCure)
          {
          
        	  rollDice();
        	  rollDice();
        	  rollDice();
        	  rollDice();
              tactic--;
          }
          if (tactic < -500 )
          {
              System.out.println("out of ideas, will drive randomly");
              driveRandom();
          }
          tactic--;
        
      }
      
  }    
  
//A function to suggest other players
 public void makeSuggestion()
 {
	 availableMoves.clear();
	 
	 if(this.playerRole.equals("MEDIC")){
		 findAvailableMoves(this.playerPiece.location,"MEDIC",this.hand);
	 }
	 else if(this.playerRole.equals("SCIENTIST")){
		 findAvailableMoves(this.playerPiece.location,"SCIENTIST",this.hand);
	 }
	 else if(this.playerRole.equals("QUARANTINE_SPECIALIST")){
		 findAvailableMoves2(this.playerPiece.location,"QUARANTINE_SPECIALIST",this.hand);
	 }
	 else{
		 findAvailableMoves2(this.playerPiece.location,"OPERATIONS_EXPERT",this.hand);
	 }
	
	 Random rand = new Random();
	 Action action = availableMoves.get(rand.nextInt(availableMoves.size()));
	 
	 City  bestDestination = null;
	 
	 ArrayList<City> cube3 = pandemicBoard.get3CubeCities();  		// Cities with 3 cubes
	 ArrayList<City> cube2 = pandemicBoard.get2CubeCities();  		// Cities with 2 cubes
	 ArrayList<City> cube1 = pandemicBoard.get1CubeCities();  		// Cities with 1 cube
	 
	 if(cube3.size() > 0){
		 bestDestination = cube3.get(rand.nextInt(cube3.size()));
	 }
	 else if(cube2.size() > 0){
		 bestDestination = cube2.get(rand.nextInt(cube2.size()));
	 }
	 else if(cube1.size() > 0){
		 bestDestination = cube1.get(rand.nextInt(cube1.size()));
	 }
		 
	 
	 if(action instanceof driveCity)   // Make action driveCity
	  {
		  driveCity(((driveCity) action).getMoveFrom(), ((driveCity) action).getMoveTo());
	  }
	  else if(action instanceof directFlight)  // Make action directFlight
	  { 
		  directFlight(this.playerPiece.location, ((directFlight) action).getMoveTo());
		  this.hand.remove(((directFlight) action).getMoveTo()); 
	  } 
	  else if(action instanceof charterFlight)  // Make action charterFlight
	  {
		  charterFlight(((charterFlight) action).getMoveFrom(), bestDestination);
		  this.hand.remove(((charterFlight) action).getMoveFrom());
	  }
	  else if(action instanceof treatDisease)   // Make action treatDisease
	  {
		  treatDisease(this.playerPiece.location, this.playerPiece.location.getColour());
	  }
	  else if(action instanceof discoverCure)
	  {
		  discoverCure(((discoverCure) action).getCurrent_city(),((discoverCure) action).getColorOfDisease());
		  int threshold = 0;
		  for(int i=0; i < this.hand.size(); i++)
		  {
			  if(this.hand.get(i).getColour().equals(tryCureCardColour(1)) && threshold < 3)
			  {
				  this.hand.remove(this.hand.get(i));
			  }
		  }
	  }
	  else if(action instanceof buildResearchStation)
	  {
			buildResearchStation();
	  }
	  else
	  {
		  driveRandom();
	  }
	 
	 availableMoves.clear();
		 
	 return;
 }

 // MEDIC evaluates suggestions of other players
 public int medicEvaluate()
 {
	 ArrayList<Action> sugg0 = Variables.Suggestions[0];	// Suggestion of Jack
	 ArrayList<Action> sugg1 = Variables.Suggestions[1];	// Suggestion of Unabomber
	 ArrayList<Action> sugg2 = Variables.Suggestions[2];	// Suggestion of Jesus
	 
	 ArrayList<City> cube3 = pandemicBoard.get3CubeCities();  // Cities with 3 cubes
	 ArrayList<City> cube2 = pandemicBoard.get2CubeCities();  // Cities with 2 cubes
	 ArrayList<City> cube1 = pandemicBoard.get1CubeCities();  // Cities with 1 cube
	 
	 int Jack_eval = 0;
	 int Unabomber_eval = 0;
	 int Jesus_eval = 0;
	 
	 int maxEval = 0;	// max Evaluation of Suggestions
	 int toReturn = -1; // Returns which player has max Evaluation greater than threshold! Returns -1 if nobody has greater than threshold 
	 
	 // Evaluate Jack's Suggestion
	 for(int i=0; i<sugg0.size(); i++)
	 {
		 Action action = sugg0.get(i);
		 
		 // Check how much important is the destination! We don't care about collecting cards 
		 if(action instanceof driveCity || action instanceof directFlight || action instanceof shuttleFlight || action instanceof charterFlight)	 
		 {
			 City destination = null;
			 
			 if(action instanceof driveCity) { destination = ((driveCity) action).getMoveTo(); }
			 else if(action instanceof directFlight) { destination = ((directFlight) action).getMoveTo(); }
			 else if(action instanceof shuttleFlight) { destination = ((shuttleFlight) action).getMoveTo(); }
			 else if(action instanceof charterFlight) { destination = ((charterFlight) action).getMoveTo(); }
			 
			 
			 if(cube3.contains(destination)) { Jack_eval+=10; }   // Destination with 3 cubes
			  if(cube2.contains(destination)) { Jack_eval+=8; }	  // Destination with 2 cubes	
			  if(cube1.contains(destination)) { Jack_eval+=5; }   // Destination with 1 cube

			  for(int n=0; n < destination.getNeighbors().size(); n++)    // Check if the neighborhood of each neighbor has lot of cubes
			  {
				  City neighbor = destination.getNeighbors().get(n);
				  if(cube3.contains(neighbor)) { Jack_eval+=6; }
				  if(cube2.contains(neighbor)) { Jack_eval+=3; }
				  if(cube1.contains(neighbor)) { Jack_eval+=1; }
			  }
		 }
		 else if(action instanceof treatDisease)	// Great priority since it's MEDIC
		 {
			 Jack_eval+=40;
		 }
		 else if(action instanceof discoverCure)	// When there is opportunity to Cure. High priority
		 {
			 Jack_eval+=50;
		 }
		 
	 }
	 
	// Evaluate Unabomber's Suggestion
	 for(int i=0; i<sugg1.size(); i++)
	 {
		 Action action = sugg1.get(i);

		 // Check how much important is the destination! We don't care about collecting cards 
		 if(action instanceof driveCity || action instanceof directFlight || action instanceof shuttleFlight || action instanceof charterFlight)	 
		 {
			 City destination = null;

			 if(action instanceof driveCity) { destination = ((driveCity) action).getMoveTo(); }
			 else if(action instanceof directFlight) { destination = ((directFlight) action).getMoveTo(); }
			 else if(action instanceof shuttleFlight) { destination = ((shuttleFlight) action).getMoveTo(); }
			 else if(action instanceof charterFlight) { destination = ((charterFlight) action).getMoveTo(); }


			 if(cube3.contains(destination)) { Unabomber_eval+=10; }   // Destination with 3 cubes
			 if(cube2.contains(destination)) { Unabomber_eval+=8; }	  // Destination with 2 cubes	
			 if(cube1.contains(destination)) { Unabomber_eval+=5; }   // Destination with 1 cube

			 for(int n=0; n < destination.getNeighbors().size(); n++)    // Check if the neighborhood of each neighbor has lot of cubes
			 {
				 City neighbor = destination.getNeighbors().get(n);
				 if(cube3.contains(neighbor)) { Unabomber_eval+=6; }
				 if(cube2.contains(neighbor)) { Unabomber_eval+=3; }
				 if(cube1.contains(neighbor)) { Unabomber_eval+=1; }
			 }
		 }
		 else if(action instanceof treatDisease)	// Great priority since it's MEDIC
		 {
			 Unabomber_eval+=40;
		 }
		 else if(action instanceof discoverCure)	// When there is opportunity to Cure. High priority
		 {
			 Unabomber_eval+=50;
		 }

	 }
	 
	// Evaluate Jesus's Suggestion
	 for(int i=0; i<sugg2.size(); i++)
	 {
		 Action action = sugg2.get(i);

		 // Check how much important is the destination! We don't care about collecting cards 
		 if(action instanceof driveCity || action instanceof directFlight || action instanceof shuttleFlight || action instanceof charterFlight)	 
		 {
			 City destination = null;

			 if(action instanceof driveCity) { destination = ((driveCity) action).getMoveTo(); }
			 else if(action instanceof directFlight) { destination = ((directFlight) action).getMoveTo(); }
			 else if(action instanceof shuttleFlight) { destination = ((shuttleFlight) action).getMoveTo(); }
			 else if(action instanceof charterFlight) { destination = ((charterFlight) action).getMoveTo(); }


			 if(cube3.contains(destination)) { Jesus_eval+=10; }   // Destination with 3 cubes
			 if(cube2.contains(destination)) { Jesus_eval+=8; }	  // Destination with 2 cubes	
			 if(cube1.contains(destination)) { Jesus_eval+=5; }   // Destination with 1 cube

			 for(int n=0; n < destination.getNeighbors().size(); n++)    // Check if the neighborhood of each neighbor has lot of cubes
			 {
				 City neighbor = destination.getNeighbors().get(n);
				 if(cube3.contains(neighbor)) { Jesus_eval+=6; }
				 if(cube2.contains(neighbor)) { Jesus_eval+=3; }
				 if(cube1.contains(neighbor)) { Jesus_eval+=1; }
			 }
		 }
		 else if(action instanceof treatDisease)	// Great priority since it's MEDIC
		 {
			 Jesus_eval+=40;
		 }
		 else if(action instanceof discoverCure)	// When there is opportunity to Cure. High priority
		 {
			 Jesus_eval+=50;
		 }

	 }
	 
	 if(Jack_eval >= Unabomber_eval)
	 {
		 maxEval = Jack_eval;
		 toReturn = 0;
	 }
	 else
	 {
		 maxEval = Unabomber_eval;
		 toReturn = 1;
	 }
	 
	 if(Jesus_eval > maxEval)
	 {
		 maxEval = Jesus_eval;
		 toReturn = 2;
	 }
	 
	 System.out.println("Jack evaluation: "+Jack_eval);
	 System.out.println("Unabomber evaluation: "+Unabomber_eval);
	 System.out.println("Jesus evaluation: "+Jesus_eval);
	 
	 if(maxEval >= threshold) 
		 return toReturn;
	 else 
		 return -1;
 }
 
//SCIENTIST evaluates suggestions of other players
 public int scientistEvaluate()
 {
	 ArrayList<Action> sugg1 = Variables.Suggestions[1];	// Suggestion of Unabomber
	 ArrayList<Action> sugg2 = Variables.Suggestions[2];	// Suggestion of Jesus
	 ArrayList<Action> sugg3 = Variables.Suggestions[3];	// Suggestion of Zodiac
	 
	 ArrayList<City> cube3 = pandemicBoard.get3CubeCities();  // Cities with 3 cubes
	 ArrayList<City> cube2 = pandemicBoard.get2CubeCities();  // Cities with 2 cubes
	 ArrayList<City> cube1 = pandemicBoard.get1CubeCities();  // Cities with 1 cube
	 
	 ArrayList<City> stations = pandemicBoard.getResearchLocations(); // Cities with a Research Station
	 
	 int Zodiac_eval = 0;
	 int Unabomber_eval = 0;
	 int Jesus_eval = 0;
	 
	 int maxEval = 0;	// max Evaluation of Suggestions
	 int toReturn = -1; // Returns which player has max Evaluation greater than threshold! Returns -1 if nobody has greater than threshold 
	 
	 int redCards = getCountXCards("Red");
	 int blueCards = getCountXCards("Blue");
	 int yellowCards = getCountXCards("Yellow");
	 int blackCards = getCountXCards("Black");
	 boolean ableCure = false;	// Shows if we have enough cards to find a Cure
	 
	 if(redCards >=4 || blueCards >=4 || yellowCards >=4 || blackCards >=4)
	 {
		 ableCure = true;
	 }
	 
	 // Evaluate Unabomber's Suggestion
	 for(int i=0; i<sugg1.size(); i++)
	 {
		 Action action = sugg1.get(i);
		 
		 if(action instanceof driveCity || action instanceof shuttleFlight )
		 {
			 City destination;
			 
			 if(action instanceof driveCity) { destination = ((driveCity) action).getMoveTo(); }
			 else { destination = ((shuttleFlight) action).getMoveTo(); }
			 
			 if(ableCure)	// If we have enough cards then priority is to visit Research Station
			 {
				 if(stations.contains(destination)) { Unabomber_eval+=80; }	// Big evaluation go to Research Station
				 else{
					 for(int n=0; n < destination.getNeighbors().size(); n++)    // Check if there is Research Station in neighborhood 
					  {
						  City neighbor = destination.getNeighbors().get(n);
						  if(stations.contains(neighbor)) { Unabomber_eval+=30; }
					  }
				 }
				 
				 if(cube3.contains(destination)) { Unabomber_eval+=3; }
				 if(cube2.contains(destination)) { Unabomber_eval+=2; }
				 if(cube1.contains(destination)) { Unabomber_eval+=1; }
			 }
			 else	// If we do not have enough cards for Cure yet. Priority Treat
			 {
				 if(cube3.contains(destination)) { Unabomber_eval+=20; }
				 if(cube2.contains(destination)) { Unabomber_eval+=10; }
				 if(cube1.contains(destination)) { Unabomber_eval+=5; }
			 }		 
		 }
		 else if(action instanceof treatDisease)
		 {
			 if(ableCure)	// We can Cure so treatment is second priority
			 {
				 Unabomber_eval+=8;
			 }
			 else	// We cannot Cure so treatment is Priority
			 {
				 Unabomber_eval+=50;
			 }
		 }
		 else if(action instanceof discoverCure)
		 {
			 Unabomber_eval+=200;	// If we can cure we do it!
		 }
	 }
	 
	// Evaluate Jesus's Suggestion
	 for(int i=0; i<sugg2.size(); i++)
	 {
		 Action action = sugg2.get(i);

		 if(action instanceof driveCity || action instanceof shuttleFlight )
		 {
			 City destination;

			 if(action instanceof driveCity) { destination = ((driveCity) action).getMoveTo(); }
			 else { destination = ((shuttleFlight) action).getMoveTo(); }

			 if(ableCure)	// If we have enough cards then priority is to visit Research Station
			 {
				 if(stations.contains(destination)) { Jesus_eval+=80; }	// Big evaluation go to Research Station
				 else{
					 for(int n=0; n < destination.getNeighbors().size(); n++)    // Check if there is Research Station in neighborhood 
					 {
						 City neighbor = destination.getNeighbors().get(n);
						 if(stations.contains(neighbor)) { Jesus_eval+=30; }
					 }
				 }

				 if(cube3.contains(destination)) { Jesus_eval+=3; }
				 if(cube2.contains(destination)) { Jesus_eval+=2; }
				 if(cube1.contains(destination)) { Jesus_eval+=1; }
			 }
			 else	// If we do not have enough cards for Cure yet. Priority Treat
			 {
				 if(cube3.contains(destination)) { Jesus_eval+=10; }
				 if(cube2.contains(destination)) { Jesus_eval+=8; }
				 if(cube1.contains(destination)) { Jesus_eval+=5; }
			 }		 
		 }
		 else if(action instanceof treatDisease)
		 {
			 if(ableCure)	// We can Cure so treatment is second priority
			 {
				 Jesus_eval+=8;
			 }
			 else	// We cannot Cure so treatment is Priority
			 {
				 Jesus_eval+=50;
			 }
		 }
		 else if(action instanceof discoverCure)
		 {
			 Jesus_eval+=200;	// If we can cure we do it!
		 }
	 }
	 
	// Evaluate Zodiac's Suggestion
	 for(int i=0; i<sugg3.size(); i++)
	 {
		 Action action = sugg3.get(i);

		 if(action instanceof driveCity || action instanceof shuttleFlight )
		 {
			 City destination;

			 if(action instanceof driveCity) { destination = ((driveCity) action).getMoveTo(); }
			 else { destination = ((shuttleFlight) action).getMoveTo(); }

			 if(ableCure)	// If we have enough cards then priority is to visit Research Station
			 {
				 if(stations.contains(destination)) { Zodiac_eval+=80; }	// Big evaluation go to Research Station
				 else{
					 for(int n=0; n < destination.getNeighbors().size(); n++)    // Check if there is Research Station in neighborhood 
					 {
						 City neighbor = destination.getNeighbors().get(n);
						 if(stations.contains(neighbor)) { Zodiac_eval+=30; }
					 }
				 }

				 if(cube3.contains(destination)) { Zodiac_eval+=3; }
				 if(cube2.contains(destination)) { Zodiac_eval+=2; }
				 if(cube1.contains(destination)) { Zodiac_eval+=1; }
			 }
			 else	// If we do not have enough cards for Cure yet. Priority Treat
			 {
				 if(cube3.contains(destination)) { Zodiac_eval+=10; }
				 if(cube2.contains(destination)) { Zodiac_eval+=8; }
				 if(cube1.contains(destination)) { Zodiac_eval+=5; }
			 }		 
		 }
		 else if(action instanceof treatDisease)
		 {
			 if(ableCure)	// We can Cure so treatment is second priority
			 {
				 Zodiac_eval+=8;
			 }
			 else	// We cannot Cure so treatment is Priority
			 {
				 Zodiac_eval+=50;
			 }
		 }
		 else if(action instanceof discoverCure)
		 {
			 Zodiac_eval+=200;	// If we can cure we do it!
		 }
	 }
	 
	 if(Zodiac_eval >= Unabomber_eval)
	 {
		 maxEval = Zodiac_eval;
		 toReturn = 3;
	 }
	 else
	 {
		 maxEval = Unabomber_eval;
		 toReturn = 1;
	 }
	 
	 if(Jesus_eval > maxEval)
	 {
		 maxEval = Jesus_eval;
		 toReturn = 2;
	 }
	 
	 System.out.println("Zodiac evaluation: "+Zodiac_eval);
	 System.out.println("Unabomber evaluation: "+Unabomber_eval);
	 System.out.println("Jesus evaluation: "+Jesus_eval);
	 
	 if(maxEval >= threshold) 
		 return toReturn;
	 else 
		 return -1;
	 
 }

//OPERATION EXPERT evaluates suggestions of other players
 public int operationEvaluate()
 {
	 ArrayList<Action> sugg0 = Variables.Suggestions[0];	// Suggestion of Jack
	 ArrayList<Action> sugg1 = Variables.Suggestions[1];	// Suggestion of Unabomber
	 ArrayList<Action> sugg3 = Variables.Suggestions[3]; 	// Suggestion of Zodiac
	  
	 ArrayList<City> cube3 = pandemicBoard.get3CubeCities();  // Cities with 3 cubes
	 ArrayList<City> cube2 = pandemicBoard.get2CubeCities();  // Cities with 2 cubes
	 ArrayList<City> cube1 = pandemicBoard.get1CubeCities();  // Cities with 1 cube
	 
	 ArrayList<City> stations = pandemicBoard.getResearchLocations(); // Cities with a Research Station
	 
	 boolean redResearch = false;
	 boolean blackResearch = false;
	 boolean yellowResearch = false;
	  
	 for(int i=0; i<stations.size(); i++)
	 {
	  if(stations.get(i).getColour().equals("Red")) { redResearch = true; }
	  else if(stations.get(i).getColour().equals("Black")) { blackResearch = true; }
	  else if(stations.get(i).getColour().equals("Yellow")) { yellowResearch = true; }
	 }
	 
	 int Jack_eval = 0;
	 int Unabomber_eval = 0;
	 int Zodiac_eval = 0;
	 
	 int maxEval = 0;	// max Evaluation of Suggestions
	 int toReturn = -1; // Returns which player has max Evaluation greater than threshold! Returns -1 if nobody has greater than threshold
	
	 // Evaluate Jack's suggestion
	 for(int i=0; i<sugg0.size(); i++)
	 {
		 Action action = sugg0.get(i);
		 
		 if(action instanceof driveCity || action instanceof charterFlight)
		 {
			 City goFrom;
			 City destination;
			 
			 if(action instanceof driveCity)
			 {
				 goFrom = ((driveCity) action).getMoveFrom();
				 destination = ((driveCity) action).getMoveTo();
			 }
			 else
			 {
				 goFrom = ((charterFlight) action).getMoveFrom();
				 destination = ((charterFlight) action).getMoveTo();
			 }
			 
			 
			 String goFromColor = goFrom.getColour();
			 String destColor = destination.getColour();
			 
			 if(!goFromColor.equals(destColor))	// Color of current City and destination is not the same
			 {
				 if(destColor.equals("Red") && !redResearch) { Jack_eval+=40; }			// Color we are heading to doesn't have Station
				 else if(destColor.equals("Black") && !blackResearch) { Jack_eval+=40; }
				 else if(destColor.equals("Yellow") && !yellowResearch) { Jack_eval+=40; }
				 
				 Jack_eval+=(destination.getNeighbors().size());	// Important if destination has a lot o neighbors
				 
				 if(cube3.contains(destination)) { Jack_eval+=10; }
				 else if(cube2.contains(destination)) { Jack_eval+=8; }
				 else if(cube1.contains(destination)) { Jack_eval+=5; }
			 }
			 else	// Color of current City is the same with destination color 
			 {
				 for(int j=0; j<destination.getNeighbors().size(); j++)	// For every neighbor of destination
				 {
					 City neighbor = destination.getNeighbors().get(j);
					 String neigColor = neighbor.getColour();
					 
					 if(!neigColor.equals(goFromColor))
					 {
						 if(neigColor.equals("Red") && !redResearch) { Jack_eval+=10; }
						 else if(neigColor.equals("Black") && !blackResearch) { Jack_eval+=10; }
						 else if(neigColor.equals("Yellow") && !yellowResearch) { Jack_eval+=10; }
					 }
					 
					 if(cube3.contains(neighbor)) { Jack_eval+=8; }
					 else if(cube2.contains(neighbor)) { Jack_eval+=5; }
					 else if(cube1.contains(neighbor)) { Jack_eval+=2; }
				 }
				 
				 if(cube3.contains(destination)) { Jack_eval+=10; }
				 else if(cube2.contains(destination)) { Jack_eval+=8; }
				 else if(cube1.contains(destination)) { Jack_eval+=3; }
			 }
		 }
		 else if(action instanceof directFlight || action instanceof shuttleFlight)
		 {
			 City destination;
			 
			 if(action instanceof directFlight) { destination = ((directFlight) action).getMoveTo(); }
			 else { destination = ((shuttleFlight) action).getMoveTo(); }
			 
			 String destColor = destination.getColour();
			 
			 boolean stationExists = false;	// Destination belongs to area without Research Station 
			 
			 if( (destColor.equals("Red") && redResearch) || (destColor.equals("Black") && blackResearch) || (destColor.equals("Yellow") && yellowResearch) )
			 {
				 stationExists = true;	// Destination belongs to area with Research Station
			 }
			 
			 if(!stationExists)	// No Research station in that area
			 {
				 Jack_eval+=40;
				 Jack_eval+=destination.getNeighbors().size();
				 
				 if(cube3.contains(destination)) { Jack_eval+=5; }
				 else if(cube2.contains(destination)) { Jack_eval+=3; }
				 else if(cube1.contains(destination)) { Jack_eval+=1; }
				 
				 for(int j=0; j<destination.getNeighbors().size(); j++)
				 {
					 City neighbor = destination.getNeighbors().get(j);
					 String neigColor = neighbor.getColour();
					 
					 if(!neigColor.equals(destColor))
					 {
						 if(neigColor.equals("Red") && !redResearch) { Jack_eval+=5; }
						 else if(neigColor.equals("Black") && !blackResearch) { Jack_eval+=5; }
						 else if(neigColor.equals("Yellow") && !yellowResearch) { Jack_eval+=5; }
					 }
					 
					 if(cube3.contains(neighbor)) { Jack_eval+=3; }
					 else if(cube2.contains(neighbor)) { Jack_eval+=2; }
					 else if(cube1.contains(neighbor)) { Jack_eval+=1; }
				 }
			 }
			 else	//  Research station exists in that area
			 {
				 if(cube3.contains(destination)) { Jack_eval+=5; }
				 else if(cube2.contains(destination)) { Jack_eval+=3; }
				 else if(cube1.contains(destination)) { Jack_eval+=1; }
				 
				 for(int j=0; j<destination.getNeighbors().size(); j++)
				 {
					 City neighbor = destination.getNeighbors().get(j);
					 String neigColor = neighbor.getColour();
					 
					 if(!neigColor.equals(destColor))
					 {
						 if(neigColor.equals("Red") && !redResearch) { Jack_eval+=5; }
						 else if(neigColor.equals("Black") && !blackResearch) { Jack_eval+=5; }
						 else if(neigColor.equals("Yellow") && !yellowResearch) { Jack_eval+=5; }
					 }
					 
					 if(cube3.contains(neighbor)) { Jack_eval+=3; }
					 else if(cube2.contains(neighbor)) { Jack_eval+=2; }
					 else if(cube1.contains(neighbor)) { Jack_eval+=1; }
				 }
			 }
		 }
		 else if(action instanceof treatDisease)
		 {
			 if(cube3.contains(((treatDisease) action).getLocation())){ Jack_eval+=8; }
			 else if(cube2.contains(((treatDisease) action).getLocation())){ Jack_eval+=5; }
			 else if(cube1.contains(((treatDisease) action).getLocation())){ Jack_eval+=2; }
		 }
		 else if(action instanceof buildResearchStation)
		 {
			 Jack_eval+=100;
		 }
		 else if(action instanceof discoverCure)
		 {
			 Jack_eval+=200;
		 }
	 }
	 
	 
	 // Evaluate Unabomber's suggestion
	 for(int i=0; i<sugg1.size(); i++)
	 {
		 Action action = sugg1.get(i);

		 if(action instanceof driveCity || action instanceof charterFlight)
		 {
			 City goFrom;
			 City destination;

			 if(action instanceof driveCity)
			 {
				 goFrom = ((driveCity) action).getMoveFrom();
				 destination = ((driveCity) action).getMoveTo();
			 }
			 else
			 {
				 goFrom = ((charterFlight) action).getMoveFrom();
				 destination = ((charterFlight) action).getMoveTo();
			 }


			 String goFromColor = goFrom.getColour();
			 String destColor = destination.getColour();

			 if(!goFromColor.equals(destColor))	// Color of current City and destination is not the same
			 {
				 if(destColor.equals("Red") && !redResearch) { Unabomber_eval+=40; }			// Color we are heading to doesn't have Station
				 else if(destColor.equals("Black") && !blackResearch) { Unabomber_eval+=40; }
				 else if(destColor.equals("Yellow") && !yellowResearch) { Unabomber_eval+=40; }

				 Unabomber_eval+=(destination.getNeighbors().size());	// Important if destination has a lot o neighbors

				 if(cube3.contains(destination)) { Unabomber_eval+=10; }
				 else if(cube2.contains(destination)) { Unabomber_eval+=8; }
				 else if(cube1.contains(destination)) { Unabomber_eval+=5; }
			 }
			 else	// Color of current City is the same with destination color 
			 {
				 for(int j=0; j<destination.getNeighbors().size(); j++)	// For every neighbor of destination
				 {
					 City neighbor = destination.getNeighbors().get(j);
					 String neigColor = neighbor.getColour();

					 if(!neigColor.equals(goFromColor))
					 {
						 if(neigColor.equals("Red") && !redResearch) { Unabomber_eval+=10; }
						 else if(neigColor.equals("Black") && !blackResearch) { Unabomber_eval+=10; }
						 else if(neigColor.equals("Yellow") && !yellowResearch) { Unabomber_eval+=10; }
					 }

					 if(cube3.contains(neighbor)) { Unabomber_eval+=8; }
					 else if(cube2.contains(neighbor)) { Unabomber_eval+=5; }
					 else if(cube1.contains(neighbor)) { Unabomber_eval+=2; }
				 }

				 if(cube3.contains(destination)) { Unabomber_eval+=10; }
				 else if(cube2.contains(destination)) { Unabomber_eval+=8; }
				 else if(cube1.contains(destination)) { Unabomber_eval+=3; }
			 }
		 }
		 else if(action instanceof directFlight || action instanceof shuttleFlight)
		 {
			 City destination;

			 if(action instanceof directFlight) { destination = ((directFlight) action).getMoveTo(); }
			 else { destination = ((shuttleFlight) action).getMoveTo(); }

			 String destColor = destination.getColour();

			 boolean stationExists = false;	// Destination belongs to area without Research Station 

			 if( (destColor.equals("Red") && redResearch) || (destColor.equals("Black") && blackResearch) || (destColor.equals("Yellow") && yellowResearch) )
			 {
				 stationExists = true;	// Destination belongs to area with Research Station
			 }

			 if(!stationExists)	// No Research station in that area
			 {
				 Unabomber_eval+=40;
				 Unabomber_eval+=destination.getNeighbors().size();

				 if(cube3.contains(destination)) { Unabomber_eval+=5; }
				 else if(cube2.contains(destination)) { Unabomber_eval+=3; }
				 else if(cube1.contains(destination)) { Unabomber_eval+=1; }

				 for(int j=0; j<destination.getNeighbors().size(); j++)
				 {
					 City neighbor = destination.getNeighbors().get(j);
					 String neigColor = neighbor.getColour();

					 if(!neigColor.equals(destColor))
					 {
						 if(neigColor.equals("Red") && !redResearch) { Unabomber_eval+=5; }
						 else if(neigColor.equals("Black") && !blackResearch) { Unabomber_eval+=5; }
						 else if(neigColor.equals("Yellow") && !yellowResearch) { Unabomber_eval+=5; }
					 }

					 if(cube3.contains(neighbor)) { Unabomber_eval+=3; }
					 else if(cube2.contains(neighbor)) { Unabomber_eval+=2; }
					 else if(cube1.contains(neighbor)) { Unabomber_eval+=1; }
				 }
			 }
			 else	//  Research station exists in that area
			 {
				 if(cube3.contains(destination)) { Unabomber_eval+=5; }
				 else if(cube2.contains(destination)) { Unabomber_eval+=3; }
				 else if(cube1.contains(destination)) { Unabomber_eval+=1; }

				 for(int j=0; j<destination.getNeighbors().size(); j++)
				 {
					 City neighbor = destination.getNeighbors().get(j);
					 String neigColor = neighbor.getColour();

					 if(!neigColor.equals(destColor))
					 {
						 if(neigColor.equals("Red") && !redResearch) { Unabomber_eval+=5; }
						 else if(neigColor.equals("Black") && !blackResearch) { Unabomber_eval+=5; }
						 else if(neigColor.equals("Yellow") && !yellowResearch) { Unabomber_eval+=5; }
					 }

					 if(cube3.contains(neighbor)) { Unabomber_eval+=3; }
					 else if(cube2.contains(neighbor)) { Unabomber_eval+=2; }
					 else if(cube1.contains(neighbor)) { Unabomber_eval+=1; }
				 }
			 }
		 }
		 else if(action instanceof treatDisease)
		 {
			 if(cube3.contains(((treatDisease) action).getLocation())){ Unabomber_eval+=8; }
			 else if(cube2.contains(((treatDisease) action).getLocation())){ Unabomber_eval+=5; }
			 else if(cube1.contains(((treatDisease) action).getLocation())){ Unabomber_eval+=2; }
		 }
		 else if(action instanceof buildResearchStation)
		 {
			 Unabomber_eval+=100;
		 }
		 else if(action instanceof discoverCure)
		 {
			 Unabomber_eval+=200;
		 }
	 }
	 
	 
	 // Evaluate Zodiac's suggestion
	 for(int i=0; i<sugg3.size(); i++)
	 {
		 Action action = sugg3.get(i);

		 if(action instanceof driveCity || action instanceof charterFlight)
		 {
			 City goFrom;
			 City destination;

			 if(action instanceof driveCity)
			 {
				 goFrom = ((driveCity) action).getMoveFrom();
				 destination = ((driveCity) action).getMoveTo();
			 }
			 else
			 {
				 goFrom = ((charterFlight) action).getMoveFrom();
				 destination = ((charterFlight) action).getMoveTo();
			 }


			 String goFromColor = goFrom.getColour();
			 String destColor = destination.getColour();

			 if(!goFromColor.equals(destColor))	// Color of current City and destination is not the same
			 {
				 if(destColor.equals("Red") && !redResearch) { Zodiac_eval+=40; }			// Color we are heading to doesn't have Station
				 else if(destColor.equals("Black") && !blackResearch) { Zodiac_eval+=40; }
				 else if(destColor.equals("Yellow") && !yellowResearch) { Zodiac_eval+=40; }

				 Zodiac_eval+=(destination.getNeighbors().size());	// Important if destination has a lot o neighbors

				 if(cube3.contains(destination)) { Zodiac_eval+=10; }
				 else if(cube2.contains(destination)) { Zodiac_eval+=8; }
				 else if(cube1.contains(destination)) { Zodiac_eval+=5; }
			 }
			 else	// Color of current City is the same with destination color 
			 {
				 for(int j=0; j<destination.getNeighbors().size(); j++)	// For every neighbor of destination
				 {
					 City neighbor = destination.getNeighbors().get(j);
					 String neigColor = neighbor.getColour();

					 if(!neigColor.equals(goFromColor))
					 {
						 if(neigColor.equals("Red") && !redResearch) { Zodiac_eval+=10; }
						 else if(neigColor.equals("Black") && !blackResearch) { Zodiac_eval+=10; }
						 else if(neigColor.equals("Yellow") && !yellowResearch) { Zodiac_eval+=10; }
					 }

					 if(cube3.contains(neighbor)) { Zodiac_eval+=8; }
					 else if(cube2.contains(neighbor)) { Zodiac_eval+=5; }
					 else if(cube1.contains(neighbor)) { Zodiac_eval+=2; }
				 }

				 if(cube3.contains(destination)) { Zodiac_eval+=10; }
				 else if(cube2.contains(destination)) { Zodiac_eval+=8; }
				 else if(cube1.contains(destination)) { Zodiac_eval+=3; }
			 }
		 }
		 else if(action instanceof directFlight || action instanceof shuttleFlight)
		 {
			 City destination;

			 if(action instanceof directFlight) { destination = ((directFlight) action).getMoveTo(); }
			 else { destination = ((shuttleFlight) action).getMoveTo(); }

			 String destColor = destination.getColour();

			 boolean stationExists = false;	// Destination belongs to area without Research Station 

			 if( (destColor.equals("Red") && redResearch) || (destColor.equals("Black") && blackResearch) || (destColor.equals("Yellow") && yellowResearch) )
			 {
				 stationExists = true;	// Destination belongs to area with Research Station
			 }

			 if(!stationExists)	// No Research station in that area
			 {
				 Zodiac_eval+=40;
				 Zodiac_eval+=destination.getNeighbors().size();

				 if(cube3.contains(destination)) { Zodiac_eval+=5; }
				 else if(cube2.contains(destination)) { Zodiac_eval+=3; }
				 else if(cube1.contains(destination)) { Zodiac_eval+=1; }

				 for(int j=0; j<destination.getNeighbors().size(); j++)
				 {
					 City neighbor = destination.getNeighbors().get(j);
					 String neigColor = neighbor.getColour();

					 if(!neigColor.equals(destColor))
					 {
						 if(neigColor.equals("Red") && !redResearch) { Zodiac_eval+=5; }
						 else if(neigColor.equals("Black") && !blackResearch) { Zodiac_eval+=5; }
						 else if(neigColor.equals("Yellow") && !yellowResearch) { Zodiac_eval+=5; }
					 }

					 if(cube3.contains(neighbor)) { Zodiac_eval+=3; }
					 else if(cube2.contains(neighbor)) { Zodiac_eval+=2; }
					 else if(cube1.contains(neighbor)) { Zodiac_eval+=1; }
				 }
			 }
			 else	//  Research station exists in that area
			 {
				 if(cube3.contains(destination)) { Zodiac_eval+=5; }
				 else if(cube2.contains(destination)) { Zodiac_eval+=3; }
				 else if(cube1.contains(destination)) { Zodiac_eval+=1; }

				 for(int j=0; j<destination.getNeighbors().size(); j++)
				 {
					 City neighbor = destination.getNeighbors().get(j);
					 String neigColor = neighbor.getColour();

					 if(!neigColor.equals(destColor))
					 {
						 if(neigColor.equals("Red") && !redResearch) { Zodiac_eval+=5; }
						 else if(neigColor.equals("Black") && !blackResearch) { Zodiac_eval+=5; }
						 else if(neigColor.equals("Yellow") && !yellowResearch) { Zodiac_eval+=5; }
					 }

					 if(cube3.contains(neighbor)) { Zodiac_eval+=3; }
					 else if(cube2.contains(neighbor)) { Zodiac_eval+=2; }
					 else if(cube1.contains(neighbor)) { Zodiac_eval+=1; }
				 }
			 }
		 }
		 else if(action instanceof treatDisease)
		 {
			 if(cube3.contains(((treatDisease) action).getLocation())){ Zodiac_eval+=8; }
			 else if(cube2.contains(((treatDisease) action).getLocation())){ Zodiac_eval+=5; }
			 else if(cube1.contains(((treatDisease) action).getLocation())){ Zodiac_eval+=2; }
		 }
		 else if(action instanceof buildResearchStation)
		 {
			 Zodiac_eval+=100;
		 }
		 else if(action instanceof discoverCure)
		 {
			 Zodiac_eval+=200;
		 }
	 }
	 
	 
	 
	 if(Zodiac_eval >= Unabomber_eval)
	 {
		 maxEval = Zodiac_eval;
		 toReturn = 3;
	 }
	 else
	 {
		 maxEval = Unabomber_eval;
		 toReturn = 1;
	 }
	 
	 if(Jack_eval > maxEval)
	 {
		 maxEval = Jack_eval;
		 toReturn = 0;
	 }
	 
	 System.out.println("Zodiac evaluation: "+Zodiac_eval);
	 System.out.println("Unabomber evaluation: "+Unabomber_eval);
	 System.out.println("Jack evaluation: "+Jack_eval);
	 
	 if(maxEval >= threshold) 
		 return toReturn;
	 else 
		 return -1;
 }
 
//QUARANTINE SPECIALIST evaluates suggestions of other players
 public int quarantineEvaluate()
 {
	 ArrayList<City> infectedDiscards = pandemicBoard.getInfectDiscardPile();
	 ArrayList<City> cube3 = pandemicBoard.get3CubeCities();  		// Cities with 3 cubes
	 ArrayList<City> cube2 = pandemicBoard.get2CubeCities();  		// Cities with 2 cubes
	 ArrayList<City> cube1 = pandemicBoard.get1CubeCities();  		// Cities with 1 cube
	 
	 ArrayList<Action> sugg0 = Variables.Suggestions[0];	// Suggestion of Jack
	 ArrayList<Action> sugg2 = Variables.Suggestions[2];	// Suggestion of Jesus
	 ArrayList<Action> sugg3 = Variables.Suggestions[3]; 	// Suggestion of Zodiac
	 
	 int redArea = 0;		// How many cube does Red area have
	 int blueArea = 0;		// How many cube does Blue area have
	 int blackArea = 0;		// How many cube does Black area have
	 int yellowArea = 0;	// How many cube does Yellow area have
	 
	 String mostNeedArea = null; // Area with most cubes 
	 
	 /**********	Calculate cubes of every area **********/ 
	 for(int i=0; i<cube3.size(); i++)
	 {
		 City city = cube3.get(i);
		 String color = city.getColour();
		 
		 if(color.equals("Red")) { redArea++; }
		 else if(color.equals("Blue")) { blueArea++; }
		 else if(color.equals("Black")) { blackArea++; }
		 else { yellowArea++; }
	 }
	 
	 for(int i=0; i<cube2.size(); i++)
	 {
		 City city = cube2.get(i);
		 String color = city.getColour();
		 
		 if(color.equals("Red")) { redArea++; }
		 else if(color.equals("Blue")) { blueArea++; }
		 else if(color.equals("Black")) { blackArea++; }
		 else { yellowArea++; }
	 }
	 
	 for(int i=0; i<cube1.size(); i++)
	 {
		 City city = cube1.get(i);
		 String color = city.getColour();
		 
		 if(color.equals("Red")) { redArea++; }
		 else if(color.equals("Blue")) { blueArea++; }
		 else if(color.equals("Black")) { blackArea++; }
		 else { yellowArea++; }
	 }
	 /**********	Calculate cubes of every area **********/ 
	 
	 int maxCubes = redArea;
	 mostNeedArea = "Red";
	 
	 if(blueArea > maxCubes){
		 maxCubes = blueArea;
		 mostNeedArea = "Blue";
	 }
	 if(yellowArea > maxCubes){
		 maxCubes = yellowArea;
		 mostNeedArea = "Yellow";
	 }
	 if(blackArea > maxCubes){
		 maxCubes = blackArea;
		 mostNeedArea = "Black";
	 }
	 
	 int Jack_eval = 0;
	 int Jesus_eval = 0;
	 int Zodiac_eval = 0;
	 
	 int maxEval = 0;	// max Evaluation of Suggestions
	 int toReturn = -1; // Returns which player has max Evaluation greater than threshold! Returns -1 if nobody has greater than threshold
	 
	 //Evaluate Jack's suggestion
	 for(int i=0; i<sugg0.size(); i++)
	 {
		 Action action = sugg0.get(i);
		 
		 if(action instanceof driveCity || action instanceof directFlight || action instanceof charterFlight || action instanceof shuttleFlight)
		 {
			 City destination;
			 
			 if(action instanceof driveCity) { destination = ((driveCity) action).getMoveTo(); }
			 else if(action instanceof directFlight) { destination = ((directFlight) action).getMoveTo(); }
			 else if(action instanceof charterFlight) { destination = ((charterFlight) action).getMoveTo(); }
			 else { destination = ((shuttleFlight) action).getMoveTo(); }
			 
			 String destColor = destination.getColour();
			 
			 if(destColor.equals(mostNeedArea)){ Jack_eval+=30; }	// City of Area with most need
			 
			 if(infectedDiscards.contains(destination)) { Jack_eval+=20; }	// City ready to be infected after Epidemic
			 
			 for(int j=0; j<destination.getNeighbors().size(); j++)
			 {
				 City neighbor =  destination.getNeighbors().get(j);
				 String neigColor = neighbor.getColour();
				 
				 if(neigColor.equals(mostNeedArea)) { Jack_eval+=4; }	// Neighbor of Destination is in most need Area
				 Jack_eval+=2;	// Number of neighbors is also important
			 }
			 
			 if(cube3.contains(destination)) { Jack_eval+=5; }
			 else if(cube2.contains(destination)) { Jack_eval+=3; }
			 else if(cube1.contains(destination)) { Jack_eval+=1; }
		 }
		 else if(action instanceof treatDisease)
		 {
			 Jack_eval+=50;
		 }
		 else if(action instanceof discoverCure)
		 {
			 Jack_eval+=200;
		 }
	 }
	 
	 
	 //Evaluate Jesus's suggestion
	 for(int i=0; i<sugg2.size(); i++)
	 {
		 Action action = sugg2.get(i);

		 if(action instanceof driveCity || action instanceof directFlight || action instanceof charterFlight || action instanceof shuttleFlight)
		 {
			 City destination;

			 if(action instanceof driveCity) { destination = ((driveCity) action).getMoveTo(); }
			 else if(action instanceof directFlight) { destination = ((directFlight) action).getMoveTo(); }
			 else if(action instanceof charterFlight) { destination = ((charterFlight) action).getMoveTo(); }
			 else { destination = ((shuttleFlight) action).getMoveTo(); }

			 String destColor = destination.getColour();

			 if(destColor.equals(mostNeedArea)){ Jesus_eval+=30; }	// City of Area with most need

			 if(infectedDiscards.contains(destination)) { Jesus_eval+=20; }	// City ready to be infected after Epidemic

			 for(int j=0; j<destination.getNeighbors().size(); j++)
			 {
				 City neighbor =  destination.getNeighbors().get(j);
				 String neigColor = neighbor.getColour();

				 if(neigColor.equals(mostNeedArea)) { Jesus_eval+=4; }	// Neighbor of Destination is in most need Area
				 Jesus_eval+=2;	// Number of neighbors is also important
			 }

			 if(cube3.contains(destination)) { Jesus_eval+=5; }
			 else if(cube2.contains(destination)) { Jesus_eval+=3; }
			 else if(cube1.contains(destination)) { Jesus_eval+=1; }
		 }
		 else if(action instanceof treatDisease)
		 {
			 Jesus_eval+=50;
		 }
		 else if(action instanceof discoverCure)
		 {
			 Jesus_eval+=200;
		 }
	 }
	 
	 
	 //Evaluate Zodiac's suggestion
	 for(int i=0; i<sugg3.size(); i++)
	 {
		 Action action = sugg3.get(i);

		 if(action instanceof driveCity || action instanceof directFlight || action instanceof charterFlight || action instanceof shuttleFlight)
		 {
			 City destination;

			 if(action instanceof driveCity) { destination = ((driveCity) action).getMoveTo(); }
			 else if(action instanceof directFlight) { destination = ((directFlight) action).getMoveTo(); }
			 else if(action instanceof charterFlight) { destination = ((charterFlight) action).getMoveTo(); }
			 else { destination = ((shuttleFlight) action).getMoveTo(); }

			 String destColor = destination.getColour();

			 if(destColor.equals(mostNeedArea)){ Zodiac_eval+=30; }	// City of Area with most need

			 if(infectedDiscards.contains(destination)) { Zodiac_eval+=20; }	// City ready to be infected after Epidemic

			 for(int j=0; j<destination.getNeighbors().size(); j++)
			 {
				 City neighbor =  destination.getNeighbors().get(j);
				 String neigColor = neighbor.getColour();

				 if(neigColor.equals(mostNeedArea)) { Zodiac_eval+=4; }	// Neighbor of Destination is in most need Area
				 Zodiac_eval+=2;	// Number of neighbors is also important
			 }

			 if(cube3.contains(destination)) { Zodiac_eval+=5; }
			 else if(cube2.contains(destination)) { Zodiac_eval+=3; }
			 else if(cube1.contains(destination)) { Zodiac_eval+=1; }
		 }
		 else if(action instanceof treatDisease)
		 {
			 Zodiac_eval+=50;
		 }
		 else if(action instanceof discoverCure)
		 {
			 Zodiac_eval+=200;
		 }
	 }
 	 
	 if(Zodiac_eval >= Jesus_eval)
	 {
		 maxEval = Zodiac_eval;
		 toReturn = 3;
	 }
	 else
	 {
		 maxEval = Jesus_eval;
		 toReturn = 2;
	 }
	 
	 if(Jack_eval > maxEval)
	 {
		 maxEval = Jack_eval;
		 toReturn = 0;
	 }
	 
	 System.out.println("Zodiac evaluation: "+Zodiac_eval);
	 System.out.println("Jesus evaluation: "+Jesus_eval);
	 System.out.println("Jack evaluation: "+Jack_eval);
	 
	 if(maxEval >= threshold) 
		 return toReturn;
	 else 
		 return -1;
 }
 
 // Follow actions of best Suggestion from friends
 public void followSuggestion(int id)
 {
	 ArrayList<Action> suggestion;	//Actions that player is going to follow
	 
	 if(this.playerName.equals("Zodiac Killer"))
	 {
		 if(id == 0)		// Jack's suggestion
		 {
			 suggestion = Variables.Suggestions[0];
		 }
		 else if(id == 1)	// Unabombers's suggestion
		 {
			 suggestion = Variables.Suggestions[1];
		 }
		 else				// Jesus's suggestion
		 {
			 suggestion = Variables.Suggestions[2];
		 }
	 }
	 else if(this.playerName.equals("Jack the Ripper"))
	 {
		 if(id == 1)		// Unabomber's suggestion
		 {
			 suggestion = Variables.Suggestions[1];
		 }
		 else if(id == 2)	// Jesus's suggestion
		 {
			 suggestion = Variables.Suggestions[2];
		 }
		 else				// Zodiac's suggestion
		 {
			 suggestion = Variables.Suggestions[3];
		 }
	 }
	 else if(this.playerName.equals("Jesus"))
	 {
		 if(id == 0)		// Jack's suggestion
		 {
			 suggestion = Variables.Suggestions[0];
		 }
		 else if(id == 1)	// Unabomber's suggestion
		 {
			 suggestion = Variables.Suggestions[1];
		 }
		 else				// Zodiac's suggestion
		 {
			 suggestion = Variables.Suggestions[3];
		 }
	 }
	 else
	 {
		 if(id == 0)		// Jack's suggestion
		 {
			 suggestion = Variables.Suggestions[0];
		 }
		 else if(id == 2)	// Jesus's suggestion
		 {
			 suggestion = Variables.Suggestions[2];
		 }
		 else				// Zodiac's suggestion
		 {
			 suggestion = Variables.Suggestions[3];
		 }
	 }
	 
	 for(int i=0; i<suggestion.size(); i++)
	 {
		 Action  action = suggestion.get(i);
		 
		 if(action instanceof driveCity)
		 {
			 driveCity(((driveCity) action).getMoveFrom(), ((driveCity) action).getMoveTo());
		 }
		 else if(action instanceof directFlight)
		 {
			 directFlight(this.playerPiece.location, ((directFlight) action).getMoveTo());
		 }
		 else if(action instanceof charterFlight)
		 {
			 charterFlight(((charterFlight) action).getMoveFrom(), ((charterFlight) action).getMoveTo());
		 }
		 else if(action instanceof shuttleFlight)
		 {
			 shuttleFlight(((shuttleFlight) action).getMoveFrom(), ((shuttleFlight) action).getMoveTo());
		 }
		 else if(action instanceof treatDisease)
		 {
			 treatDisease(((treatDisease) action).getLocation(), ((treatDisease) action).getLocation().getColour());
		 }
		 else if(action instanceof discoverCure)
		 {
			 discoverCure(((discoverCure) action).getCurrent_city(), ((discoverCure) action).getColorOfDisease());
		 }
		 else if(action instanceof buildResearchStation)
		 {
			 buildResearchStation();
		 }
		 else{
			 System.out.println("No supposed to get here!");
		 }
	 }
	 
 }
 
//Find all the available Moves for a player depending on position, cards, Role
 public void findAvailableMoves(City location, String Role, ArrayList<City> cards)
 {
	  int size = location.getNeighbors().size();
	  
	  //Find legal Drive/Ferry moves
	  for(int i=0; i < size ; i++) {
		  //System.out.println("City named " + location.getName() + " has " + location.getNeighbors().get(i) + " as a neighbor.");
		  driveCity tmp = new driveCity(location,location.getNeighbors().get(i));
		  availableMoves.add(tmp);
	  }
	  
	  //Find legal Direct Flights	  
	  for(int i=0; i<cards.size() ;i++) {
		  //System.out.println("Card of city named" + cards.get(i)+ ".");
		  directFlight tmp = new directFlight(cards.get(i),cards);
		  availableMoves.add(tmp);	  
	  }
	  
	  // Find legal Charter Flights	  
	  for(int i=0; i<cards.size() ;i++) {
		  if(location.equals(cards.get(i))) {
			  for(int j=0; j < pandemicBoard.cities.size(); j++)
			  {
				  if(!location.equals(pandemicBoard.cities.get(j)))
				  {
					  charterFlight tmp = new charterFlight(location,cards,pandemicBoard.cities.get(j));
					  availableMoves.add(tmp);
				  }
			  }		  
		  }
	  }
	  
	  // Find legal Shuttle Flights	  
	  ArrayList<City> researchStations =  pandemicBoard.getResearchLocations();
	  for(int i=0; i <researchStations.size(); i++) {
		  if(location.equals(researchStations.get(i))) {
			  //System.out.println("The current city i am, has a research station");
			  for(int j=0; j<researchStations.size();j++) {
				  if(!location.equals(researchStations.get(j)))  // Avoid action: moveFrom = moveTo
				  {
					  shuttleFlight tmp = new shuttleFlight(location,researchStations.get(j));
					  availableMoves.add(tmp);
				  }			  
			  }
		  }
	  }
	  
	  // Find if you can build a research station	  
	  if(buildResearchStationCheck (Role, location, cards)) {
		  buildResearchStation tmp = new buildResearchStation(location,cards);
		  availableMoves.add(tmp);	 
	  }
	  
	  // Find if you can treat a disease	  
	  String locationColour = location.getColour();
     if(treatDiseaseCheck(location,locationColour)) {
   	  treatDisease tmp = new treatDisease(location,locationColour);
   	  availableMoves.add(tmp);      
	  }
     
     // Find if you can discover a Cure    
     if(discoverCureCheck(Role,cards)!=null) {
   	  discoverCure tmp = new discoverCure(location,cards,discoverCureCheck(Role,cards));
   	  availableMoves.add(tmp);     
	  }
 }
 
//Find all the available Moves for a player depending on position, cards, Role
public void findAvailableMoves2(City location, String Role, ArrayList<City> cards)
{
	  int size = location.getNeighbors().size();
	  
	  //Find legal Drive/Ferry moves
	  for(int i=0; i < size ; i++) {
		  //System.out.println("City named " + location.getName() + " has " + location.getNeighbors().get(i) + " as a neighbor.");
		  driveCity tmp = new driveCity(location,location.getNeighbors().get(i));
		  availableMoves.add(tmp);
	  }
	  
	  //Find legal Direct Flights	  
	  for(int i=0; i<cards.size() ;i++) {
		  //System.out.println("Card of city named" + cards.get(i)+ ".");
		  directFlight tmp = new directFlight(cards.get(i),cards);
		  availableMoves.add(tmp);	  
	  }
	  
	  // Find legal Charter Flights	  
	  for(int i=0; i<cards.size() ;i++) {
		  if(location.equals(cards.get(i))) {
			  for(int j=0; j < pandemicBoard.cities.size(); j++)
			  {
				  if(!location.equals(pandemicBoard.cities.get(j)))
				  {
					  charterFlight tmp = new charterFlight(location,cards,pandemicBoard.cities.get(j));
					  availableMoves.add(tmp);
				  }
			  }		  
		  }
	  }
	  
	  // Find legal Shuttle Flights	  
	  ArrayList<City> researchStations =  pandemicBoard.getResearchLocations();
	  for(int i=0; i <researchStations.size(); i++) {
		  if(location.equals(researchStations.get(i))) {
			  System.out.println("The current city i am, has a research station");
			  for(int j=0; j<researchStations.size();j++) {
				  shuttleFlight tmp = new shuttleFlight(location,researchStations.get(j));
				  availableMoves.add(tmp);			  
			  }
		  }
	  }
	  
	  // Find if you can build a research station	  
	  if(buildResearchStationCheck (Role, location, cards)) {
		  buildResearchStation tmp = new buildResearchStation(location,cards);
		  availableMoves.add(tmp);	 
	  }
	  
	  // Find if you can treat a disease	  
	  String locationColour = location.getColour();
    if(treatDiseaseCheck2(location,locationColour)) {
  	  treatDisease tmp = new treatDisease(location,locationColour);
  	  availableMoves.add(tmp);      
	  }
}
 
//Print available Moves for the moment
 public void printAvailableMoves()
 {
	  for(int i=0; i < availableMoves.size(); i++)
	  {
		  System.out.print((i+1)+")"); availableMoves.get(i).printAction();
	  }
 }
 
 public boolean buildResearchStationCheck (String Role, City location, ArrayList<City> cards)
 {
	  
	  buildResearchStation tmp = new buildResearchStation(location,cards);
	  if (Role.equals("OPERATIONS_EXPERT") && !Variables.CITY_WITH_RESEARCH_STATION.contains(location)) 
	  {
         return true;
	  }
	  else
	  {
	      if (tmp.isaLegalMove())
	      {
	          return true;
	      }
	  }
     return false;
 }
 
 public boolean treatDiseaseCheck (City location, String colour)
 {
	 if(location.getMaxCube() > 1)
		 return true;
	 
	 return false;
 } 

 public String discoverCureCheck(String Role, ArrayList<City> cards)
 { 
	 int red=0, yellow=0, blue=0, black=0;
	 for(int i=0; i < cards.size(); i++)
	 {
		 if(cards.get(i).getColour().equals("Red")){ red++; }
		 else if(cards.get(i).getColour().equals("Yellow")){ yellow++; }
		 else if(cards.get(i).getColour().equals("Blue")){ blue++; }
		 else { black++; }
		 
		 if( Role.equals("SCIENTIST") )
		 {
			 if(red > 2){ return "Red"; }
			 else if(yellow > 2){ return "Yellow"; }
			 else if(blue > 2){ return "Blue"; }
			 else if(black > 2){ return "Black"; }
		 }
		 else
		 {
			 if(red > 3){ return "Red"; }
			 else if(yellow > 3){ return "Yellow"; }
			 else if(blue > 3){ return "Blue"; }
			 else if(black > 3){ return "Black"; }
		 }

	 }
	 
	 return null;
	  
 }
 
 public boolean treatDiseaseCheck2 (City location, String colour)
 {
	 treatDisease tmp = new treatDisease(location,colour);
	 if(tmp.isaLegalMove())
	 {
	    return true;
	 }
	 
	 return false;
 } 
  
  public void printSuggestions()
  {
	  ArrayList<Action> sugg0 = Variables.Suggestions[0];	// Suggestion of Jack
	  ArrayList<Action> sugg1 = Variables.Suggestions[1];	// Suggestion of Unabomber
	  ArrayList<Action> sugg2 = Variables.Suggestions[2];	// Suggestion of Jesus
	  ArrayList<Action> sugg3 = Variables.Suggestions[3]; 	// Suggestion of Zodiac 
	  
	  if(!sugg0.isEmpty())
	  {
		  System.out.println("Suggestion of Jack");
		  for(int i=0; i<sugg0.size(); i++){
			  System.out.println((i+1)+") "+sugg0.get(i));
			  if(sugg0.get(i) instanceof driveCity){
				  City goFrom = ((Pandemic.actions.driveCity) sugg0.get(i)).getMoveFrom(); // Start city
				  City goTo = ((Pandemic.actions.driveCity) sugg0.get(i)).getMoveTo(); // Destination city
				  System.out.println("Drive from "+goFrom+" to: "+goTo);
			  }
		  }
	  }
	  
	  if(!sugg1.isEmpty())
	  {
		  System.out.println("Suggestion of Unabomber");
		  for(int i=0; i<sugg1.size(); i++){
			  System.out.println((i+1)+") "+sugg1.get(i));
			  if(sugg1.get(i) instanceof driveCity){
				  City goFrom = ((Pandemic.actions.driveCity) sugg1.get(i)).getMoveFrom(); // Start city
				  City goTo = ((Pandemic.actions.driveCity) sugg1.get(i)).getMoveTo(); // Destination city
				  System.out.println("Drive from "+goFrom+" to: "+goTo);
			  }
		  }
	  }
	  
	  if(!sugg2.isEmpty())
	  {
		  System.out.println("Suggestion of Jesus");
		  for(int i=0; i<sugg2.size(); i++){
			  System.out.println((i+1)+") "+sugg2.get(i));
			  if(sugg2.get(i) instanceof driveCity){
				  City goFrom = ((Pandemic.actions.driveCity) sugg2.get(i)).getMoveFrom(); // Start city
				  City goTo = ((Pandemic.actions.driveCity) sugg2.get(i)).getMoveTo(); // Destination city
				  System.out.println("Drive from "+goFrom+" to: "+goTo);
			  }
		  }
	  }
	  

	  if(sugg3!=null)
	  {
		  System.out.println("Suggestion of Zodiac");
		  for(int i=0; i<sugg3.size(); i++){
			  System.out.println((i+1)+") "+sugg3.get(i));
			  if(sugg3.get(i) instanceof driveCity){
				  City goFrom = ((Pandemic.actions.driveCity) sugg3.get(i)).getMoveFrom(); // Start city
				  City goTo = ((Pandemic.actions.driveCity) sugg3.get(i)).getMoveTo(); // Destination city
				  System.out.println("Drive from "+goFrom+" to: "+goTo);
			  }
		  }  
		  sugg3.clear();
	  }
	  
	  sugg0.clear();
	  sugg1.clear();
	  sugg2.clear();
	 
  }

//Player will either treat disease or go to a city with 3 cubes.
  public void rollDice()
  {
      System.out.println("Wants to treat disease... ");
      if (!tryTreat(3)) {
    	  if (!go3CubeCities()) {
    		  if(!tryTreat(2))	 	 {
    			  if (!go2CubeCities()) 	{
    				  if(!tryTreat(1)) 			{
    					  if (!go1CubeCities()) 	{
    						  System.out.println("Going to drive randomly as can't think of anything.");
    						  driveRandom();
    					  }
    				  }
    			  }
    		  }
    	  }
      }                
  }

  //Check to see if the disease can be treat according to the @param threshold of cubes.
  public boolean tryTreat(int threshold)
  {
      boolean toReturn = false;
      City locationCity = playerPiece.getLocation();
      if (locationCity.getMaxCube() >= threshold)
      {
          System.out.println("As there are " + threshold + " cubes in " + locationCity.getName() + " " + this.getPlayerName() + " will try and treat disease.");
          String locationColour = locationCity.getColour();
          treatDisease(locationCity,locationColour);
          toReturn = true;
      }
      else 
      {
          System.out.println("Doesn't think it's worth trying to treat here.");
      }
      return toReturn;
      
  }
  
  public boolean checkTryCure()
  {
      if (checkCureWorthIt(0)) {
          if (discoverCure(playerPiece.getLocation(),tryCureCardColour(0))) {
              System.out.println(this.getPlayerName() + " has discovered a cure!");
              System.out.println("Yeah!!");
              System.out.println("  Yeah!!");
              System.out.println("    Yeah!!");    
              return true;
          }
          else{
              System.out.println("They need to go to a researh station.");
              tryDriveResearchStation();
          }
      }
      else{
         System.out.println("no point in trying to find a cure.");
      }
      return false;
  }
  
  //evaluate the chance for cure , (if is it worth)
  public boolean checkCureWorthIt(int r)
  {
      String toCure = tryCureCardColour(r);
      if (toCure != null)
      {
          for (int i = 0 ; i < pandemicBoard.diseases.size() ; i ++){
        	  
              Disease disease = pandemicBoard.diseases.get(i);
              if (toCure == disease.getColour() && !disease.getCured()){
                  return true;
              }
          }          
      }
      return false;      
  }
  
  //an attempt to drive to nearest research station
  public void tryDriveResearchStation()
  {
      System.out.println("Searching cities with research stations as destinations.");
      getDistances(Variables.GET_CITIES_WITH_RESEARCH_STATION());
      // System.out.println("Calculating destination");
      City toDriveTo = calculateDestination();
      //System.out.println("I'll try to drive to " + toDriveTo.getName());
      driveCity(playerPiece.getLocation(),toDriveTo); 
  }
  
  /** 
   * Check to see if the count of cards in any color equal the number required
   * for a cure
  **/
  public String tryCureCardColour(int r)
  {
      for (int i = 0; i < pandemicBoard.getNumberColours(); i ++)
      {
          if (getCountXCards(possibleColour[i]) >= pandemicBoard.getNeededForCure()-r)
          {
              return possibleColour[i];
          }           
      }
      return null;
  }
  
  //get distance 
  public int getDistanceResearch()
  {		
	  ArrayList<City> destinations = new ArrayList<City>();
	  for (City c : pandemicBoard.cities) {
		  for (City dest: Variables.GET_CITIES_WITH_RESEARCH_STATION()) {
			  if (c.getName().equals(dest.getName())) {
				  destinations.add(c);
			  }
		  }
	  }
	  
      getDistances(destinations);
      return playerPiece.getLocation().getDistance();
  }
  
  public int getDistanceResearch(City curLocation)
  {
      getDistances(Variables.GET_CITIES_WITH_RESEARCH_STATION());
      return curLocation.getDistance();
  }
  
  //get distances of the cities which a look to travel
  public void getDistances(ArrayList<City> destinations){
      pandemicBoard.resetDistances();
      setDestination(destinations);
      int distance = 1;
      int loc=-1;
      for(int i = 0 ; i < pandemicBoard.cities.size() ; i ++){
        if (pandemicBoard.cities.get(i).getName().equals( playerPiece.getLocation().getName() )){
            loc = i;
            break;
          }
      }
      while (pandemicBoard.cities.get(loc).getDistance() == 9999){
          //System.out.println("Looking for places distance of " + distance);
          for (int i = 0 ; i < pandemicBoard.cities.size() ; i ++){
        	  for (int x = 0 ; x < pandemicBoard.cities.get(i).getNeighbors().size() ; x ++){        		  
        		  //System.out.println(pandemicBoard.cities.get(i).getNeighbors().get(x).getDistance());
                  if ( pandemicBoard.cities.get(i).getDistance() == (distance-1) && pandemicBoard.cities.get(i).getNeighbors().get(x).getDistance() > distance ){
                      pandemicBoard.cities.get(i).getNeighbors().get(x).setDistance(distance);
                  }
              }
          }
          distance ++;
          
      }
  }
  
  public void setDestination(ArrayList<City> destinations)
  {
      for (int i = 0 ; i < destinations.size() ; i ++)
      {
          destinations.get(i).setDistance(0);
      }        
  }
  
  // Try to random charter flight
  public void charterRandom(){
      Random rand = new Random();
      int n = rand.nextInt(pandemicBoard.cities.size());
      charterFlight(playerPiece.getLocation(),pandemicBoard.cities.get(n));
  }
  
  //try to drive to random city until a possible city is chosen.
  public void driveRandom(){
	  Random rand1 = new Random();
	  City temp = playerPiece.getLocation();
      int n = rand1.nextInt(temp.getNeighbors().size());
      driveCity(playerPiece.getLocation(),temp.getNeighbors().get(n));
  }
  
  public City calculateDestination()
  {
      int closestDestination = 9999;
      City toReturn = new City(0,0,0,0,0);
      for (int i = 0 ; i < playerPiece.getLocation().getNeighbors().size(); i++)
      {	
          if (playerPiece.getLocation().getNeighbors().get(i).getDistance() < closestDestination)
          {
              //System.out.println("Will probably go to " + playerPiece.getLocationConnections()[i].getName());
              toReturn = playerPiece.getLocation().getNeighbors().get(i);
              closestDestination = playerPiece.getLocation().getNeighbors().get(i).getDistance();
          }
          
      }
      return toReturn;
  }
  
  //find all the cities with 3 cubes and measure distances to make a decision 
  //in which city to drive
  public boolean go3CubeCities()
  {        
	  ArrayList<City> CitiesWith3Cubes =  pandemicBoard.get3CubeCities();
      if (CitiesWith3Cubes.size() > 0)
      {
          //System.out.print("Setting 3 cube cities ");
//          for (int i = 0 ; i < CitiesWith3Cubes.size() ; i ++)
//          {
//              System.out.print("#:"+(i+1) + " " + CitiesWith3Cubes.get(i).getName());
//          }
         
          getDistances(CitiesWith3Cubes);
          //System.out.println(" as destinations.");
          City toDriveTo = calculateDestination();
          //System.out.println(this.getPlayerName() + " will go to " + toDriveTo.getName());
          driveCity(playerPiece.getLocation(),toDriveTo);
          return true;
      }
      else
      {
          System.out.println("No 3 cube cities.");
          return false;
      }
  }
  
  //find all the cities with 2 cubes 
  //measure distance and drive to best case (city)
  public boolean go2CubeCities()
  {        
	  ArrayList<City> CitiesWith2Cubes = pandemicBoard.get2CubeCities();
      if (CitiesWith2Cubes.size() > 0)
      {
          //System.out.print("Setting 2 cube cities ");
//          for (int i = 0 ; i < CitiesWith2Cubes.size() ; i ++)
//          {
//             System.out.print("#:"+(i+1) + " " + CitiesWith2Cubes.get(i).getName());
//          }
          getDistances(CitiesWith2Cubes);
          //System.out.println(" as destinations.");
          City toDriveTo = calculateDestination();
          //System.out.println(this.getPlayerName() + " will go to " + toDriveTo.getName());
          driveCity(playerPiece.getLocation(),toDriveTo);
          return true;
      }
      else
      {
          //System.out.println("No 2 cube cities.");
          return false;
      }
  } 
  
  //find all the cities with 1 cubes 
  //measure distance and drive to best case (city)
  public boolean go1CubeCities()
  {        
	  ArrayList<City> CitiesWith1Cubes = pandemicBoard.get1CubeCities();
      if (CitiesWith1Cubes.size() > 0)
      {
          //System.out.print("Setting 1 cube cities ");
//          for (int i = 0 ; i < CitiesWith1Cubes.size() ; i ++)
//          {
//              System.out.print("#:"+(i+1) + " " + CitiesWith1Cubes.get(i).getName());
//          }
          getDistances(CitiesWith1Cubes);
          //System.out.println(" as destinations.");
          City toDriveTo = calculateDestination();
          //System.out.println(this.getPlayerName() + " will go to " + toDriveTo.getName());
          driveCity(playerPiece.getLocation(),toDriveTo);
          return true;
      }
      else
      {
          //System.out.println("No 1 cube cities.");
          return false;
      }
  }

  //This function updates the number of cubes in general and the number of cubes of every color using global redArea,blueArea etc.
  //Also finds the city for each color with max number of cubes
  public int CubesOnBoard() {
	  boolean flagRed = false;
	  boolean flagBlue = false;
	  boolean flagBlack = false;
	  boolean flagYellow = false;
	  ArrayList<City> CitiesWith3Cubes =  pandemicBoard.get3CubeCities();
	  for(int i=0;i<CitiesWith3Cubes.size();i++) {
		  if(CitiesWith3Cubes.get(i).getColour().equals("Red")) {
			  redArea = redArea + 3;
			  MaxRed = CitiesWith3Cubes.get(i);
			  flagRed = true;
		  }else if(CitiesWith3Cubes.get(i).getColour().equals("Blue")) {
			  blueArea = blueArea + 3;
			  MaxBlue = CitiesWith3Cubes.get(i);
			  flagBlue = true;
		  }else if(CitiesWith3Cubes.get(i).getColour().equals("Black")) {
			  blackArea = blackArea + 3;
			  MaxBlack = CitiesWith3Cubes.get(i);
			  flagBlack = true;
		  }else {
			  yellowArea = yellowArea + 3;
			  MaxYellow = CitiesWith3Cubes.get(i);
			  flagYellow = true;
		  }
	  }
	  
	  ArrayList<City> CitiesWith2Cubes =  pandemicBoard.get2CubeCities();
	  for(int i=0;i<CitiesWith2Cubes.size();i++) {
		  if(CitiesWith2Cubes.get(i).getColour().equals("Red")) {
			  redArea = redArea + 2;
			  if(!flagRed) {
				  MaxRed = CitiesWith2Cubes.get(i);
				  flagRed = true;
			  }
		  }else if(CitiesWith2Cubes.get(i).getColour().equals("Blue")) {
			  blueArea = blueArea + 2;
			  if(!flagBlue) {
				  MaxBlue = CitiesWith2Cubes.get(i);
				  flagBlue = true;
			  }
		  }else if(CitiesWith2Cubes.get(i).getColour().equals("Black")) {
			  blackArea = blackArea + 2;
			  if(!flagBlack) {
				  MaxBlack = CitiesWith2Cubes.get(i);
				  flagBlack = true;
			  }
		  }else {
			  yellowArea = yellowArea + 2;
			  if(!flagYellow) {
				  MaxYellow = CitiesWith2Cubes.get(i);
				  flagYellow = true;
			  }
		  }
	  }
	  
	  ArrayList<City> CitiesWith1Cubes =  pandemicBoard.get1CubeCities();
	  for(int i=0;i<CitiesWith1Cubes.size();i++) {
		  if(CitiesWith1Cubes.get(i).getColour().equals("Red")) {
			  redArea = redArea + 1;
			  if(!flagRed) {
				  MaxRed = CitiesWith1Cubes.get(i);
				  flagRed = true;
			  }
		  }else if(CitiesWith1Cubes.get(i).getColour().equals("Blue")) {
			  blueArea = blueArea + 1;
			  if(!flagBlue) {
				  MaxBlue = CitiesWith1Cubes.get(i);
				  flagBlue = true;
			  }
		  }else if(CitiesWith1Cubes.get(i).getColour().equals("Black")) {
			  blackArea = blackArea + 1;
			  if(!flagBlack) {
				  MaxBlack = CitiesWith1Cubes.get(i);
				  flagBlack = true;
			  }
		  }else {
			  yellowArea = yellowArea + 1;
			  if(!flagYellow) {
				  MaxYellow = CitiesWith1Cubes.get(i);
				  flagYellow = true;
			  }
		  }
	  }
	  
	  int sum = 3*CitiesWith3Cubes.size()+2*CitiesWith2Cubes.size()+CitiesWith1Cubes.size();
	  System.out.println("The number of Cubes in this board is:"+sum);
	  
	  return (sum);
  }

  //This function returns the total number of station in the board
  public ArrayList<City> ResearchStationsOnBoard() {
	  ArrayList<City> citiesWithStations = pandemicBoard.getResearchLocations();
	  for(int i=0;i<citiesWithStations.size();i++) {
		  System.out.println("We have a station at:"+citiesWithStations.get(i));
	  }
	  return citiesWithStations;
  }

  public int numResearchStationsOnBoard() {
	  ArrayList<City> citiesWithStations = pandemicBoard.getResearchLocations();
	  for(int i=0;i<citiesWithStations.size();i++) {
		  System.out.println("We have a station at:"+citiesWithStations.get(i));
	  }
	  return citiesWithStations.size();
  }

  // Evaluate Board after every round and Update threshold
  public void evaluateBoard()
  {
	  int outbreaks = pandemicBoard.getOutbreakCount();		// How many outbreaks have been done
	  
	  ArrayList<City> stations = pandemicBoard.getResearchLocations(); // Cities with a Research Station
	  ArrayList<City> cube3 = pandemicBoard.get3CubeCities();  // Cities with 3 cubes
	  ArrayList<City> cube2 = pandemicBoard.get2CubeCities();  // Cities with 2 cubes
	  ArrayList<City> cube1 = pandemicBoard.get1CubeCities();  // Cities with 1 cube
	  
	  System.out.println("###################### Evaluate Board #################################");
	  System.out.println("Number of outbreaks: "+outbreaks);
	  System.out.println("Number of Stations: "+stations.size());
	  System.out.println("Number of 3 cube Cities: "+cube3.size());
	  System.out.println("Number of 2 cube Cities: "+cube2.size());
	  System.out.println("Number of 1 cube Cities: "+cube1.size());
	  System.out.println("###################### Evaluate Board #################################");
  }
  
  public static int max(int a, int b, int c, int d) {

	    int max = a;

	    if (b > max)
	        max = b;
	    if (c > max)
	        max = c;
	    if (d > max)
	        max = d;

	     return max;
	}

  public Action evaluateMovesQuarantine() {
		
	  System.out.println("Current Location: "+this.playerPiece.location);
	  
	  pandemicBoard.setupNeighbors();
	  
	  ArrayList<City> CitiesWith3Cubes =  pandemicBoard.get3CubeCities();
	  ArrayList<City> CitiesWith2Cubes =  pandemicBoard.get2CubeCities();
	  ArrayList<City> CitiesWith1Cubes =  pandemicBoard.get1CubeCities();
	  
	  int numOfNeig = -1;
	  City mostNeigCity = null;
	  int numOfCubes = 0;
	  for(int i=0; i<availableMoves.size(); i++) {
		  numOfCubes = 0;
		  Action action = availableMoves.get(i);
		  // For every neighbor find the # of his neighbors and save the city with the max number of neighbors
		  if(action instanceof driveCity) {
			  City goTo = ((Pandemic.actions.driveCity)action).getMoveTo();
			  //System.out.println("After action move to city: "+goTo.getName()+" which has "+goTo.getNeighbors().size()+" neighbors");
			  for(int k=0;k<CitiesWith3Cubes.size();k++) {
				  if(CitiesWith3Cubes.get(k).getName().equals(goTo.getName())) {
					 numOfCubes = numOfCubes + 3;
				 }
			  }
			  for(int k=0;k<CitiesWith2Cubes.size();k++) {
				  if(CitiesWith2Cubes.get(k).getName().equals(goTo.getName())) {
						 numOfCubes = numOfCubes + 2;
					 }
			  }
			  for(int k=0;k<CitiesWith1Cubes.size();k++) {
				  if(CitiesWith1Cubes.get(k).getName().equals(goTo.getName())) {
						 numOfCubes = numOfCubes + 1;
					 }
			  }
			  //System.out.println("After action move to city: "+goTo.getName()+" which has "+numOfCubes+" cubes");
			  if(goTo.getNeighbors().size()+numOfCubes > numOfNeig) {
				  numOfNeig = goTo.getNeighbors().size()+numOfCubes;
				  mostNeigCity = goTo;
				  //System.out.println("Most weight city is "+goTo.getName()+" with "+goTo.getNeighbors().size()+" neighbors and "+numOfCubes+" cubes");
			  }
		  }
		  // For every card in your hand check the color
		  else if( action instanceof directFlight) {
			  City goTo = ((Pandemic.actions.directFlight)action).getMoveTo();
			  //System.out.println("After action move to city: "+goTo.getName()+" which has "+goTo.getColour()+" colour");
		  }
	  }
	  
	  // Find the area with max # of cubes
	  int maxCubesArea = max(redArea,blueArea,yellowArea,blackArea);
	  String maxArea;
	  City maxCity;
	  if(maxCubesArea == redArea) {
		  maxArea = "Red";
		  maxCity = MaxRed;
	  }else if(maxCubesArea == blueArea) {
		  maxArea = "Blue";
		  maxCity = MaxBlue;
	  }else if(maxCubesArea == yellowArea) {
		  maxArea = "Yellow";
		  maxCity = MaxYellow;
	  }else {
		  maxArea = "Black";
		  maxCity = MaxBlack;
	  }
	  
	  // If it is bigger than threshold Quarantine Specialist has to move to area with max #of cubes
	  if(maxCubesArea > thresholdQuaranteeMove) {
		  // Check if you have on your hand a card with the specific color
		  for(int j=0 ; j<this.hand.size();j++) {
			  if(this.hand.get(j).getColour().equals(maxArea)) {
				  directFlight tmp = new directFlight(this.hand.get(j),getHand());
				  return tmp;
			  }
		  }
		  
	  }else {
		  //System.out.println("----------------------------------------------->I am choosing to go to the Most neig city is "+mostNeigCity.getName());
		  driveCity tmp = new driveCity(this.playerPiece.location, mostNeigCity);
		  return tmp;
	  }
	  
	  return availableMoves.get(0);
}

  public Action evaluateMovesExpert() {
		
	  System.out.println("Current Location: "+this.playerPiece.location);
	  
	  ArrayList<City> CitiesWith3Cubes =  pandemicBoard.get3CubeCities();
	  ArrayList<City> CitiesWith2Cubes =  pandemicBoard.get2CubeCities();
	  ArrayList<City> CitiesWith1Cubes =  pandemicBoard.get1CubeCities();
	  int numOfNeig = -1;
	  City mostNeigCity = null;
	  int numOfCubes = 0;
	  for(int i=0; i<availableMoves.size(); i++) {
		  numOfCubes = 0;
		  Action action = availableMoves.get(i);
		  // For every neighbor find the # of his neighbors and save the city with the max number of neighbors
		  if(action instanceof driveCity) {
			  City goTo = ((Pandemic.actions.driveCity)action).getMoveTo();
			  //System.out.println("After action move to city: "+goTo.getName()+" which has "+goTo.getNeighbors().size()+" neighbors");
			  for(int k=0;k<CitiesWith3Cubes.size();k++) {
				  if(CitiesWith3Cubes.get(k).getName().equals(goTo.getName())) {
					 numOfCubes = numOfCubes + 3;
				 }
			  }
			  for(int k=0;k<CitiesWith2Cubes.size();k++) {
				  if(CitiesWith2Cubes.get(k).getName().equals(goTo.getName())) {
						 numOfCubes = numOfCubes + 2;
					 }
			  }
			  for(int k=0;k<CitiesWith1Cubes.size();k++) {
				  if(CitiesWith1Cubes.get(k).getName().equals(goTo.getName())) {
						 numOfCubes = numOfCubes + 1;
					 }
			  }
			  //System.out.println("After action move to city: "+goTo.getName()+" which has "+numOfCubes+" cubes");
			  if(goTo.getNeighbors().size()+numOfCubes > numOfNeig) {
				  numOfNeig = goTo.getNeighbors().size()+numOfCubes;
				  mostNeigCity = goTo;
				  //System.out.println("Most weight city is "+goTo.getName()+" with "+goTo.getNeighbors().size()+" neighbors and "+numOfCubes+" cubes");
			  }
		  }
		  }
	      // d must be the distance of the nearest research station
		  int d = getDistanceResearch();
		  //System.out.println("The closer reasearch station is:"+d+" moves far..............................");
		  if(d > 4 && Variables.CITY_WITH_RESEARCH_STATION.size()< 0.6*maxStations) {
			  buildResearchStation tmp = new buildResearchStation();
			  return tmp;
		  }
		  else if(d>8 && Variables.CITY_WITH_RESEARCH_STATION.size()< maxStations) {
			  buildResearchStation tmp = new buildResearchStation();
			  return tmp;
		  }else {
			  //System.out.println("----------------------------------------------->I am choosing to go to the Most neig city is "+mostNeigCity.getName());
			  driveCity tmp = new driveCity(this.playerPiece.location, mostNeigCity);
			  return tmp;
		  }
}

  public void doAvailableMove(Action a) {
		  
		if(a instanceof buildResearchStation)
		{
			buildResearchStation();
		    //System.out.println("Now previous player's location is:"+location);
		    //System.out.println("Now player's location is:"+location);
		}
		else if(a instanceof charterFlight)
		{
			driveRandom();
		}
		else if(a instanceof directFlight)
		{
			//System.out.println("I AM GONNA MAKE A DIRECT FLIGHT!!!!!!!!!!!!!!!!!!!!!!!");
			directFlight df = (directFlight) a;
			directFlight(this.playerPiece.location, df.getMoveTo());
			
			//System.out.println("Now previous player's location is:"+location);
			//System.out.println("Now player's location is:"+location);
			
		}
		else if(a instanceof discoverCure)
		{
			discoverCure(((discoverCure) a).getCurrent_city(), ((discoverCure) a).getColorOfDisease());
		    //System.out.println("Now previous player's location is:"+location);
		    //System.out.println("Now player's location is:"+location);
		}
		else if(a instanceof driveCity)
		{
			driveCity(((driveCity) a).getMoveFrom(), ((driveCity) a).getMoveTo());
		    //System.out.println("Now previous player's location is:"+location);
		    //System.out.println("Now player's location is:"+location);
		}
		else if(a instanceof shuttleFlight)
		{
			shuttleFlight(((shuttleFlight) a).getMoveFrom(), ((shuttleFlight) a).getMoveTo());
		    //System.out.println("Now previous player's location is:"+location);
		    //System.out.println("Now player's location is:"+location);
		}
		else 
		{
			treatDisease td = (treatDisease) a;
			treatDisease(td.getLocation(), td.getColour());
		    //System.out.println("Now previous player's location is:"+location);
		    //System.out.println("Now player's location is:"+location);
		}
	}
  
  public void QuarantineSpecialistMove() {
		
		Action a;
		
		ArrayList<City> CitiesWith3Cubes =  pandemicBoard.get3CubeCities();
		ArrayList<City> CitiesWith2Cubes =  pandemicBoard.get2CubeCities();
		ArrayList<City> CitiesWith1Cubes =  pandemicBoard.get1CubeCities();
		int numOfCubes = 0;
		
		for(int k=0;k<CitiesWith3Cubes.size();k++) {
			  if(CitiesWith3Cubes.get(k).getName().equals(this.playerPiece.location.getName())) {
				 numOfCubes = numOfCubes + 3;
			  }
		 }
		 for(int k=0;k<CitiesWith2Cubes.size();k++) {
			  if(CitiesWith2Cubes.get(k).getName().equals(this.playerPiece.location.getName())) {
					 numOfCubes = numOfCubes + 2;
			  }
		 }
		 for(int k=0;k<CitiesWith1Cubes.size();k++) {
			  if(CitiesWith1Cubes.get(k).getName().equals(this.playerPiece.location.getName())) {
					 numOfCubes = numOfCubes + 1;
			  }
		 }
		
		 boolean haveCards = false;   // if the player has cards on his hand
		 if(!this.hand.isEmpty()) {
			  haveCards = true;
		 }  
		 // If you have a must move from before then you have to make this move
		 if(mustMove) {
			  //System.out.println("I have to do the must move first!!!!!!!!!!!!!!!!!!");
			  doMustMove();
			  mustMove = false;
		 }
		 // If the current location of Q.Specialist has 2 or more cubes, then Q.Specialist treats a Disease
		 else if(numOfCubes>1) {
			  //System.out.println("I am doing a treat");
			  boolean eric = treatDisease(this.playerPiece.location, this.playerPiece.location.getColour());
			  if(!eric) {
				  System.out.println("Wrong");
			  }
		 }
		 // If board has many cubes then Q.Specialist will try to move quickly to the sicker area(with charter or shuttle flight)
		 else if(haveCards && redArea+blueArea+yellowArea+blackArea>20) {
			 
			  // Check if you have a neighbor and the card of this specific neighbor.
			  // If you have it, then make a driveCity action for this city so you can make a charterFlight in the next round
			  boolean tryMoveforCharter = false;
			  for(int i=0;i<this.hand.size();i++) {
				  for(int j=0;j<this.playerPiece.location.getNeighbors().size();j++) {
					  if(this.hand.get(i).equals(this.playerPiece.location.getNeighbors().get(j)) && !tryMoveforCharter) {
						  driveCity(this.playerPiece.location, this.playerPiece.location.getNeighbors().get(j));
						  tryMoveforCharter = true;
						  mustMove = true;
					  }
				  }
			  }
			  
			  // If no action took place before
			  // Check if you have a neighbor with a station
			  // If you have go there so you can make a shuttle flight in the next round
			  boolean tryMoveforShuttle = false;
			  if(!tryMoveforCharter) {
				  ArrayList<City>stations = this.ResearchStationsOnBoard();
				  for(int i=0;i<stations.size();i++) {
					  for(int j=0;j<this.playerPiece.location.getNeighbors().size();j++) {
	    				  if(stations.get(i).equals(this.playerPiece.location.getNeighbors().get(j)) && !tryMoveforShuttle) {
	    					  driveCity(this.playerPiece.location,this.playerPiece.location.getNeighbors().get(j));
	    					  tryMoveforShuttle = true;
	    					  mustMove = true;
	    				  }
	    			  	}
				  }
			  }
			  
			  // If you can't make neither charter nor shuttle flight then call evaluateMoves to find your action
			  if(!tryMoveforCharter && !tryMoveforShuttle) {
				  a = evaluateMovesQuarantine();
				  doAvailableMove(a);
			  } 
		 }
		  else {
			  // Find the area with the max number of cubes on the board
			  boolean madeMove = false;
			  int maxCubesArea = max(redArea,blueArea,yellowArea,blackArea);
				  String maxArea;
				  if(maxCubesArea == redArea) {
					  maxArea = "Red";
				  }else if(maxCubesArea == blueArea) {
					  maxArea = "Blue";
				  }else if(maxCubesArea == yellowArea) {
					  maxArea = "Yellow";
				  }else {
					  maxArea = "Black";
				  }
			  // If Q.Specialist has the card of his current location then make a charter flight to the area with max number of cubes
				 /*for(int i=0;i<hand.size();i++) {
					  if(hand.get(i).getColour().equals(maxArea)) {
						  System.out.println("I am doing a charter flight");
						  charterFlight(this.playerPiece.location,hand.get(i));
						  madeMove = true;
					  }
				  }*/
			  // If no move has been made then call evaluateMoves() to find the move
				  if(!madeMove) {
					  a = evaluateMovesQuarantine();
					  doAvailableMove(a);
				  }
		  }
	}
  
  public void OperationsExpertMove() {
		
		Action a;
		ArrayList<City> CitiesWith3Cubes =  pandemicBoard.get3CubeCities();
		ArrayList<City> CitiesWith2Cubes =  pandemicBoard.get2CubeCities();
		ArrayList<City> CitiesWith1Cubes =  pandemicBoard.get1CubeCities();
		int numOfCubes = 0;
		City dest = null;
		
		System.out.println("Current location is:"+this.playerPiece.location.getName());
		
		// If we have a must build move then do it
		if(mustBuild) {
			//System.out.println("Do a must Build move");
			buildResearchStation();
			mustBuild = false;
			return;
		}
		
		  for(int k=0;k<CitiesWith3Cubes.size();k++) {
			  if(CitiesWith3Cubes.get(k).getName().equals(this.playerPiece.location.getName())) {
				 numOfCubes = numOfCubes + 3;
			 }
		  }
		  for(int k=0;k<CitiesWith2Cubes.size();k++) {
			  if(CitiesWith2Cubes.get(k).getName().equals(this.playerPiece.location.getName())) {
					 numOfCubes = numOfCubes + 2;
				 }
		  }
		  for(int k=0;k<CitiesWith1Cubes.size();k++) {
			  if(CitiesWith1Cubes.get(k).getName().equals(this.playerPiece.location.getName())) {
					 numOfCubes = numOfCubes + 1;
				 }
		  }
		
		  // If i can make a charter flight at the beginning 
		  // Go to the sicker neighbor and build a research Station
		  if(Variables.CITY_WITH_RESEARCH_STATION.size()< 0.5*maxStations) {
			  for(int i=0;i<this.hand.size();i++) {
				  if(this.hand.get(i).equals(this.playerPiece.location)) {
					  dest = SickCity();
				  }
			  }
		  }
		  
		  if(dest!=null) {
			  charterFlight(this.playerPiece.location,dest);
			  mustBuild = true;
			  return;
		  }
		  
		  
		  // If we are at the beginning of the game then O.Expert have to build a research station 
		  // We want to build a station somewhere(area) with no station
		  if(Variables.CITY_WITH_RESEARCH_STATION.size()< 0.5*maxStations) {
			  for(int i=0;i<this.hand.size();i++) {
				  if(!mustBuild) {
					  if(this.hand.get(i).getColour().equals("Red") && !redStation) {
						  System.out.println("Red");
						  directFlight(this.playerPiece.location, this.hand.get(i));
						  mustBuild = true;
					  }else if(this.hand.get(i).getColour().equals("Blue") && !blueStation) {
						  System.out.println("Blue");
						  directFlight(this.playerPiece.location, this.hand.get(i));
						  mustBuild = true;
					  }else if(this.hand.get(i).getColour().equals("Black") && !blackStation) {
						  System.out.println("Black");
						  directFlight(this.playerPiece.location, this.hand.get(i));
						  mustBuild = true;
					  }else if(this.hand.get(i).getColour().equals("Yellow") && !yellowStation) {
						  System.out.println("Yellow");
						  directFlight(this.playerPiece.location, this.hand.get(i));
						  mustBuild = true;
					  }
				  }
			  }
		  }
		  
		  if(mustBuild) {
			  return;
		  }
		  // If the current location of O.Expert has 3 cubes and we have less than 60% of possible research stations
		  // O.Expert treats a Disease
		  if(numOfCubes>2 && Variables.CITY_WITH_RESEARCH_STATION.size()< 0.6*maxStations) {
			  //System.out.println("I am doing a treat");
			  boolean eric = treatDisease(this.playerPiece.location, this.playerPiece.location.getColour());
			  if(!eric) {
				  //System.out.println("Wrong");
			  }
		  }
		 // If the current location of O.Expert has 2 or more and we have more than 60% of possible research stations
		 // O.Expert treats a Disease
		  else if(numOfCubes>1 && Variables.CITY_WITH_RESEARCH_STATION.size()>= 0.6*maxStations) {
			  //System.out.println("I am doing a treat");
			  boolean eric = treatDisease(this.playerPiece.location, this.playerPiece.location.getColour());
			  if(!eric) {
				//System.out.println("Wrong");
			  }
		  }
		  else {
			  if(maxStations > Variables.CITY_WITH_RESEARCH_STATION.size()) {
				  a = evaluateMovesExpert();
				  doAvailableMove(a);
			  }
		  }
		
	}

  public void doMustMove() {
		
		// Find the max Cube area
		int maxCubesArea = max(redArea,blueArea,yellowArea,blackArea);
		  String maxArea;
		  if(maxCubesArea == redArea) {
			  maxArea = "Red";
		  }else if(maxCubesArea == blueArea) {
			  maxArea = "Blue";
		  }else if(maxCubesArea == yellowArea) {
			  maxArea = "Yellow";
		  }else {
			  maxArea = "Black";
		  }
		  
		  // Look for one city with the color of max cube area's color and make a charter flight
		  City locationOfCharter = null;
		  for(int i=0;i<this.hand.size();i++) {
			  if(this.hand.get(i).equals(this.playerPiece.location)) {
				  locationOfCharter = this.SickCity();
			  }
		  }
		  
		  if(locationOfCharter != null) {
			  charterFlight(this.playerPiece.location, locationOfCharter);
			  return ;
		  }
		  
		// Look for one city with research station and the color of max cube area's color and make a shuttle flight
		  City locationOfShuttle = null;
		  ArrayList<City> stations = ResearchStationsOnBoard();
		  for(int i=0;i<stations.size();i++) {
			  if(stations.get(i).getColour().equals(maxArea)) {
				  locationOfShuttle = pandemicBoard.cities.get(i);
			  }
		  }
		  if(locationOfShuttle != null) {
			  shuttleFlight(this.playerPiece.location,locationOfShuttle);
			  return ;
		  }
		  
		  // if you did not find a city you wanted then make a random drive
		  driveRandom();
		  return;
		  
	}

  //find one very sick city
  public City SickCity()
  {
	ArrayList<City> cube3 = pandemicBoard.get3CubeCities();  // Cities with 3 cubes
	ArrayList<City> cube2 = pandemicBoard.get2CubeCities();  // Cities with 2 cubes
	ArrayList<City> cube1 = pandemicBoard.get1CubeCities();  // Cities with 1 cube
	
	int maxEval = -1000;
	City sickCity = null;
	int eval = 0;   // A measure of how useful is each action
	     
	for(int i =0;i<this.pandemicBoard.getCities().size();i++) {
		eval = 0;
		City goTo = this.pandemicBoard.getCities().get(i); // Destination city
		
		for(int j=0; j < cube3.size(); j++)
			if(cube3.get(j).getName().equals(goTo.getName())){eval+=8;}   // Neighbor with 3 cubes 
		for(int j=0; j < cube2.size(); j++)
			if(cube2.get(j).getName().equals(goTo.getName())){eval+=4;}	  // Neighbor with 2 cubes 
		for(int j=0; j < cube1.size(); j++)
			if(cube1.get(j).getName().equals(goTo.getName())){eval+=2;}   // Neighbor with 1 cube
			
		for(int j=0; j < goTo.getNeighbors().size(); j++){     // Check if the neighborhood of each neighbor has lot of cubes
			City neighbor = goTo.getNeighbors().get(j);
			for(int c=0; c < cube3.size(); c++)
				if(cube3.get(c).getName().equals(neighbor.getName())) { eval+=3; }
			for(int c=0; c < cube2.size(); c++)
				if(cube2.get(c).getName().equals(neighbor.getName())) { eval+=2; }
			for(int c=0; c < cube1.size(); c++)
				if(cube1.get(c).getName().equals(neighbor.getName())) { eval+=1; }
		}
		//System.out.println("Evaluation of move: "+eval);
		if(eval > maxEval){ 
			sickCity = goTo; 
			maxEval = eval;
		}
	}	
	
	return sickCity;
  }
  
  /**
   * Here we define the functions that correspond to
   * each player's strategy depending on their Role
   */
  
  public void medicStrategy()
  {
	  
	  System.out.println("Current Location: "+this.playerPiece.location);

	  findAvailableMoves(this.playerPiece.location,"MEDIC",this.hand);
	  //printAvailableMoves();

	  ArrayList<City> cube3 = pandemicBoard.get3CubeCities();  // Cities with 3 cubes
	  ArrayList<City> cube2 = pandemicBoard.get2CubeCities();  // Cities with 2 cubes
	  ArrayList<City> cube1 = pandemicBoard.get1CubeCities();  // Cities with 1 cube

	  int maxEval = -1000;
	  int maxWeight = -1000;		 // Max weight of need for charterFlight
	  Action bestAction = null;
	  City goTo = null;  		// For driveCity and directFlight actions
	  City betterGoTo = null;   // For charterFlight. City with most need on board

	  for(int j=0; j < availableMoves.size(); j++)
	  {
		  int eval = 0;   // A measure of how useful is each action
		  Action action = availableMoves.get(j);

		  if (action instanceof driveCity || action instanceof directFlight)   
		  {

			  if(action instanceof driveCity)   // Evaluate an action which is drive to a neighbor city
			  {
				  City goFrom = ((Pandemic.actions.driveCity) action).getMoveFrom(); // Start city
				  goTo = ((Pandemic.actions.driveCity) action).getMoveTo(); // Destination city
			  }
			  else   // Evaluate an action which is direct flight to another city
			  {
				  goTo = ((Pandemic.actions.directFlight) action).getMoveTo(); // Destination city
			  }


			  if(cube3.contains(goTo)) { eval+=8; }   // Destination with 3 cubes
			  if(cube2.contains(goTo)) { eval+=4; }	  // Destination with 2 cubes	
			  if(cube1.contains(goTo)) { eval+=2; }   // Destination with 1 cube

			  for(int n=0; n < goTo.getNeighbors().size(); n++)    // Check if the neighborhood of each neighbor has lot of cubes
			  {
				  City neighbor = goTo.getNeighbors().get(n);
				  if(cube3.contains(neighbor)) { eval+=3; }
				  if(cube2.contains(neighbor)) { eval+=2; }
				  if(cube1.contains(neighbor)) { eval+=1; }
			  }

			  if(eval > maxEval){ 
				  bestAction = action; 
				  maxEval = eval;
			  }

		  }
		  else if(action instanceof charterFlight)    // Evaluate an action which is charter flight to another city
		  {
			  /** In this case we need to find a 3 cube city
			   * which has the most cubes around in neighbor cities
			   * If no 3 cube city exists we'll find 2 cube city etc.
			   **/

			  //System.out.println("We can do a charter flight! Let's see if it's optimal");

			  if(cube3.size() > 0)     // If there is at least one 3 cube city
			  {	
				  for(int c=0; c < cube3.size(); c++){
					  City dest = cube3.get(c);
					  eval = 8;   // Because we already know it's a 3 cube city
					  for(int c1=0; c1 < dest.getNeighbors().size(); c1++){
						  City neighbor = dest.getNeighbors().get(c1);
						  if(cube3.contains(neighbor)) { eval+=3; }
						  if(cube2.contains(neighbor)) { eval+=2; }
						  if(cube1.contains(neighbor)) { eval+=1; }
					  }

					  if(eval > maxWeight)   // This city is probably worth visit
					  {
						  betterGoTo = dest;
						  maxWeight = eval;
					  }
				  }
			  }
			  else if(cube2.size() > 0)   // If there isn't any 3 cube city but there is 2 cube city
			  {
				  for(int c=0; c < cube2.size(); c++){
					  City dest = cube2.get(c);
					  eval = 4;   // Because we already know it's a 2 cube city
					  for(int c1=0; c1 < dest.getNeighbors().size(); c1++){
						  City neighbor = dest.getNeighbors().get(c1);
						  if(cube2.contains(neighbor)) { eval+=2; }
						  if(cube1.contains(neighbor)) { eval+=1; }
					  }

					  if(eval > maxWeight)   // This city is probably worth visit
					  {
						  betterGoTo = dest;
						  maxWeight = eval;
					  }
				  }
			  }
			  else if(cube1.size() > 0)   // If there isn't any 3 or 2 cube city but there is 1 cube city
			  {
				  for(int c=0; c < cube1.size(); c++){
					  City dest = cube1.get(c);
					  eval = 2;   // Because we already know it's a 1 cube city
					  for(int c1=0; c1 < dest.getNeighbors().size(); c1++){
						  City neighbor = dest.getNeighbors().get(c1);
						  if(cube1.contains(neighbor)) { eval+=1; }
					  }

					  if(eval > maxWeight)   // This city is probably worth visit
					  {
						  betterGoTo = dest;
						  maxWeight = eval;
					  }
				  }
			  }
			  else{maxWeight = 0;}	// Never really gets here

			  if(betterGoTo != null)
				  //System.out.println("City in most need is: "+betterGoTo.getName()+" Evaluation of move: "+eval);

				  if(maxWeight > maxEval){ 
					  bestAction = action; 
					  maxEval = eval;
				  }

		  }
		  else if(action instanceof treatDisease)   // Evaluation of an  action which is treat some cubes
		  {
			  eval = 20;  // Give a big evaluation so treating becomes a priority when we have the opportunity 

			  //System.out.println("Evaluation of move: "+eval);
			  if(eval > maxEval){ 
				  bestAction = action; 
				  maxEval = eval;
			  }

		  }
		  else if(action instanceof shuttleFlight)
		  {
			  City destination = ((shuttleFlight) action).getMoveTo();
			  
			  if(cube3.contains(destination)) { eval+=8; }   // Destination with 3 cubes
			  if(cube2.contains(destination)) { eval+=4; }	  // Destination with 2 cubes	
			  if(cube1.contains(destination)) { eval+=2; }   // Destination with 1 cube

			  for(int n=0; n < destination.getNeighbors().size(); n++)    // Check if the neighborhood of each neighbor has lot of cubes
			  {
				  City neighbor = destination.getNeighbors().get(n);
				  if(cube3.contains(neighbor)) { eval+=3; }
				  if(cube2.contains(neighbor)) { eval+=2; }
				  if(cube1.contains(neighbor)) { eval+=1; }
			  }

			  if(eval > maxEval){ 
				  bestAction = action; 
				  maxEval = eval;
			  }
		  }
	  }

	  // if bestAction = Direct Flight or something Discard the card HERE

	  if(bestAction instanceof driveCity)   // Make action driveCity
	  {
		  driveCity(((driveCity) bestAction).getMoveFrom(), ((driveCity) bestAction).getMoveTo());
	  }
	  else if(bestAction instanceof directFlight)  // Make action directFlight
	  { 
		  directFlight(this.playerPiece.location, ((directFlight) bestAction).getMoveTo());
		  this.hand.remove(((directFlight) bestAction).getMoveTo()); 
	  } 
	  else if(bestAction instanceof charterFlight)  // Make action charterFlight
	  {
		  charterFlight(((charterFlight) bestAction).getMoveFrom(), betterGoTo);
		  this.hand.remove(((charterFlight) bestAction).getMoveFrom());
	  }
	  else if(bestAction instanceof treatDisease)   // Make action treatDisease
	  {
		  treatDisease(this.playerPiece.location, this.playerPiece.location.getColour());
	  }
	  else if(bestAction instanceof shuttleFlight)
	  {
		  shuttleFlight(((shuttleFlight) bestAction).getMoveFrom(), ((shuttleFlight) bestAction).getMoveTo());
	  }

	  //System.out.println("Best action evaluated is: ");
	  //System.out.println("Evaluation of best move: "+maxEval);
	  //bestAction.printAction();
	  availableMoves.clear();
	  
	  
  }
  
  public void scientistStrategy()

  {
	  System.out.println("Current Location: "+this.playerPiece.location);

	  findAvailableMoves(this.playerPiece.location,"SCIENTIST",this.hand);
	  //printAvailableMoves();
	  
	  ArrayList<City> cube3 = pandemicBoard.get3CubeCities();  		// Cities with 3 cubes
	  ArrayList<City> cube2 = pandemicBoard.get2CubeCities();  		// Cities with 2 cubes
	  ArrayList<City> cube1 = pandemicBoard.get1CubeCities();  		// Cities with 1 cube
	  ArrayList<City> stations = pandemicBoard.getResearchLocations(); // Cities with a Research Station
	  
	  System.out.println("Hand of Scientist");
	  for(int i=0; i < this.hand.size(); i++)
	  {
		  System.out.println((i+1)+") "+this.hand.get(i).getName()+" , color: "+this.hand.get(i).getColour());
	  }
	  
	  boolean canCure = checkCureWorthIt(1);			// if we have enough cards to cure a disease
	  boolean inStation = stations.contains(this.playerPiece.location);	// if we are already in a Research Station
	  
	  Action bestAction = null; 	// Best action after evaluate all actions
	  City betterGoTo = null;		// Best destination if we have to change position
	  int finalDistance = 100;		// If trying to reach Research Station find city with min distance 
	  boolean cureFound = false;	// A flag if we can Cure a disease
	  int maxWeight = -1;			// Helps to find best action when we cannot yet Cures
	  
	  for(int i=0; i < availableMoves.size(); i++)
	  {
		  Action action = availableMoves.get(i);	// Current action
		  		  
		  if(canCure && !inStation && !cureFound)	//Case 1: We can cure and we need to find Station as soon as possible
		  {
			  
			  if(action instanceof driveCity)
			  {
				  City destination = ((driveCity) action).getMoveTo();
				  int dist = getDistanceResearch(destination);	// Distance of closest Research Station
				  if(dist < 3 && dist < finalDistance){
					  finalDistance = dist;
					  betterGoTo = destination;
					  bestAction = action;
				  }
			  }
			  else if(action instanceof directFlight)
			  {
				  City destination = ((directFlight) action).getMoveTo();
				  
				  // We don't want to waste a useful card
				  if( getCountXCards(tryCureCardColour(1)) > 3 || !destination.getColour().equals(tryCureCardColour(1)) )
				  {
					  int dist = getDistanceResearch(destination);	// Distance of closest Research Station
					  if(dist < finalDistance){
						  finalDistance = dist;
						  betterGoTo = destination;
						  bestAction = action;
					  }
				  }
			  }
			  else if(action instanceof charterFlight)
			  {
				  City destination = ((charterFlight) action).getMoveTo();
				  
				  // We don't want to waste a useful card
				  if( getCountXCards(tryCureCardColour(1)) > 3 || !this.playerPiece.location.getColour().equals(tryCureCardColour(1)) )
				  {
					  int dist = getDistanceResearch(destination);	// Distance of closest Research Station
					  if(dist < finalDistance){
						  finalDistance = dist;
						  betterGoTo = destination;
						  bestAction = action;
					  }
				  }
			  }
				  
		  }
		  else if(!canCure && ( !inStation || inStation ) && !cureFound)	// Case 2: We cannot cure yet
		  {
			  int eval = 0;	// Evaluation of current action
			  
			  if(action instanceof treatDisease)	// There are cubes in out location
			  {
				  eval = 30;	// Very big evaluation because treatment is priority now
				  
				  if(eval > maxWeight)
				  {
					  maxWeight = eval;
					  bestAction = action;
				  }
			  }
			  else if(action instanceof driveCity)	// Check if neighbors have cubes
			  {
				  City destination = ((driveCity) action).getMoveTo();
				  
				  if(cube3.contains(destination)){ eval+=8; }
				  else if(cube2.contains(destination)){ eval+=4; }
				  else if(cube1.contains(destination)){ eval+=2; }
				  
				  for(int c=0; c < destination.getNeighbors().size(); c++)
				  {
					  City neighbor = destination.getNeighbors().get(c);
					  if(cube3.contains(neighbor)){ eval+=3; }
					  else if(cube2.contains(neighbor)){ eval+=2; }
					  else if(cube1.contains(neighbor)){ eval+=1; }
				  }
				  
				  if(eval > maxWeight)
				  {
					  maxWeight = eval;
					  bestAction = action;
					  betterGoTo = destination;
				  }
			  }
		  }
		  else if(canCure && inStation)		// Case 3: We can cure and we are in Research Station
		  {
			  maxWeight = 1000;
			  bestAction =  action;
			  cureFound = true;		// Set flag true so we stop evaluate other actions and execute this one
		  }
		  
	  }
	  
	 // if bestAction = Direct Flight or something Discard the card HERE

	  if(bestAction instanceof discoverCure)
	  {
		  discoverCure(((discoverCure) bestAction).getCurrent_city(),((discoverCure) bestAction).getColorOfDisease());
		  //int threshold = 0;
		  //for(int i=0; i < this.hand.size(); i++)
		  //{
			//  if(this.hand.get(i).getColour().equals(tryCureCardColour(1)) && threshold < 3)
			//  {
			//	  this.hand.remove(this.hand.get(i));
			//  }
		 // }
	  }
	  else if(bestAction instanceof driveCity)   // Make action driveCity
	  {
		  //System.out.println("Before calling driveCity. Current location: "+((driveCity) bestAction).getMoveFrom());
		  driveCity(((driveCity) bestAction).getMoveFrom(), ((driveCity) bestAction).getMoveTo());
	  }
	  else if(bestAction instanceof directFlight)  // Make action directFlight
	  { 
		  directFlight(this.playerPiece.location, ((directFlight) bestAction).getMoveTo());
		  //this.hand.remove(((directFlight) bestAction).getMoveTo()); 
	  } 
	  else if(bestAction instanceof charterFlight)  // Make action charterFlight
	  {
		  charterFlight(((charterFlight) bestAction).getMoveFrom(), betterGoTo);
		  //this.hand.remove(((charterFlight) bestAction).getMoveFrom());
	  }
	  else if(bestAction instanceof treatDisease)   // Make action treatDisease
	  {
		  treatDisease(this.playerPiece.location, this.playerPiece.location.getColour());
	  }

	  //System.out.println("Best action evaluated is: ");
	  //System.out.println("Evaluation of best move: "+maxWeight);
	  //if(cureFound){System.out.println("Found a cure");}
	  //else{bestAction.printAction();}
	   	  
	  availableMoves.clear();
	  
  }
  
  public void operationExpertStrategy()
  {
	  System.out.println("Current Location: "+this.playerPiece.location);

	  findAvailableMoves2(this.playerPiece.location,"OPERATIONS_EXPERT",this.hand);
	  //printAvailableMoves();  
	  
	  ArrayList<City> cube3 = pandemicBoard.get3CubeCities();  			// Cities with 3 cubes
	  ArrayList<City> cube2 = pandemicBoard.get2CubeCities();  			// Cities with 2 cubes
	  ArrayList<City> cube1 = pandemicBoard.get1CubeCities();  			// Cities with 1 cube
	  ArrayList<City> stations = pandemicBoard.getResearchLocations();	// Cities with Research Stations
	  
	  boolean redResearch = false;
	  boolean blackResearch = false;
	  boolean yellowResearch = false;
	  
	  for(int i=0; i<stations.size(); i++)
	  {
		  if(stations.get(i).getColour().equals("Red")) { redResearch = true; }
		  else if(stations.get(i).getColour().equals("Black")) { blackResearch = true; }
		  else if(stations.get(i).getColour().equals("Yellow")) { yellowResearch = true; }
	  }
	  
	  int maxEval = -1;	// Evaluation of best move
	  Action bestAction = null;	// Action with highest evaluation
	  
	  for(int i=0; i<availableMoves.size(); i++)
	  {
		  Action action = availableMoves.get(i);
		  int currEval = 0;
		  
		  if(action instanceof driveCity || action instanceof directFlight)
		  {
			  City goFrom, destination;
			  String goFromColor, destColor;
			  
			  if(action instanceof driveCity)
			  {
				  goFrom = ((driveCity) action).getMoveFrom();
				  goFromColor = goFrom.getColour();
				  
				  destination = ((driveCity) action).getMoveTo();
				  destColor = destination.getColour();
				  //System.out.print("DRIVE CITY from: "+goFrom+" to: "+destination);
			  }
			  else
			  {
				  goFrom = this.playerPiece.location;
				  goFromColor = goFrom.getColour();
				  
				  destination = ((directFlight) action).getMoveTo();
				  destColor = destination.getColour();
				  //System.out.print("DIRECT FLIGHT from: "+goFrom+" to: "+destination);
			  }
			  
			  
			  if(!goFromColor.equals(destColor))	// If destination belongs to a different Color Area
			  {
				  if( (goFromColor.equals("Red") && !redResearch) || (goFromColor.equals("Black") && !blackResearch) || (goFromColor.equals("Yellow") && !yellowResearch) )
				  {
					  currEval -= 10;	// Moving out from Color Area without Research Station
				  }
				  
				  if( (destColor.equals("Red") && !redResearch) || (destColor.equals("Black") && !blackResearch) || (destColor.equals("Yellow") && !yellowResearch) )
				  {
					  currEval += 10;
					  currEval += destination.getNeighbors().size();	// Number of neighbors is also important
				  }
			  }
			  else		// Destination color is same as in current City
			  {
				  for(int j=0; j<destination.getNeighbors().size(); j++)	// Check neighbors of destination
				  {
					  City neighbor = destination.getNeighbors().get(j);
					  String neigColor = neighbor.getColour();
					  
					  if(!destColor.equals(neigColor))
					  {
						  if( (neigColor.equals("Red") && !redResearch) || (neigColor.equals("Black") && !blackResearch) || (neigColor.equals("Yellow") && !yellowResearch) )
						  {
							  currEval += 5;
						  }
					  }
				  }
			  }
			  
			  if(cube3.contains(destination)){ currEval+=3; }
			  else if(cube2.contains(destination)){ currEval+=2; }
			  else if(cube1.contains(destination)){ currEval+=1; }
			  
		  }
		  else if(action instanceof charterFlight)
		  {
			  City currDest = ((charterFlight) action).getMoveTo();
			  String destColor = currDest.getColour();
			  
			  if(currDest.getNeighbors().size() >=4 )	// We prefer Cities with many neighbors
			  {
				  if( (destColor.equals("Red") && !redResearch) || (destColor.equals("Black") && !blackResearch) || (destColor.equals("Yellow") && !yellowResearch) )
				  {
					  currEval += 10;
				  }
				  
				  currEval += (2*currDest.getNeighbors().size());	// Number of neighbors is important
			  }
			  
			  if(cube3.contains(currDest)) { currEval+=5; }
			  else if(cube2.contains(currDest)) { currEval+=3; }
			  else if(cube1.contains(currDest)) { currEval+=1; }
			  
			  for(int j=0; j<currDest.getNeighbors().size(); j++)
			  {
				  City neighbor = currDest.getNeighbors().get(j);
				  if(cube3.contains(neighbor)) { currEval+=3; }
				  else if(cube2.contains(neighbor)) { currEval+=2; }
				  else if(cube1.contains(neighbor)) { currEval+=1; }
			  }
			   
		  }
		  else if(action instanceof shuttleFlight)
		  {
			  City destination = ((shuttleFlight) action).getMoveTo();
			  //System.out.print("CHARTER FLIGHT to: "+destination);
			  
			  for(int j=0; j<destination.getNeighbors().size(); j++)
			  {
				  City neighbor = destination.getNeighbors().get(j);
				  String neigColor = neighbor.getColour();
				  				  
				  if( (neigColor.equals("Red") && !redResearch) || (neigColor.equals("Black") && !blackResearch) || (neigColor.equals("Yellow") && !yellowResearch) )
				  {
					  currEval += 5;
				  }
				  
				  if(cube3.contains(destination)){ currEval+=3; }
				  else if(cube2.contains(destination)){ currEval+=2; }
				  else if(cube1.contains(destination)){ currEval+=1; }
			  }
			  
			  if(cube3.contains(destination)){ currEval+=5; }
			  else if(cube2.contains(destination)){ currEval+=3; }
			  else if(cube1.contains(destination)){ currEval+=2; }
			  
		  }
		  else if(action instanceof treatDisease)
		  {
			  //System.out.print("TREAT DISEASE");
			  if(cube3.contains(this.playerPiece.location) || cube2.contains(this.playerPiece.location)) { currEval += 50; }
		  }
		  else if(action instanceof buildResearchStation)
		  {
			  City location;
			  
			  location = ((buildResearchStation) action).getCityToResearchStation();
			  String color = location.getColour();
			  
			  //System.out.print("BUILD RESEARCH STATION in: "+location);
			  if(location.getNeighbors().size() >= 4 && stations.size() < maxStations && getDistanceResearch(location) > 3 )
			  {
				  if( (color.equals("Red") && !redResearch) || (color.equals("Black") && !blackResearch) || (color.equals("Yellow") && !yellowResearch) )
				  {
					  currEval += 200;
				  }
			  }
		  }
		 
		  if(currEval > maxEval)
		  {
			  maxEval = currEval;
			  bestAction = action;
		  }
		  
	  }
	  
	  if(bestAction instanceof driveCity)
	  {
		  driveCity(((driveCity) bestAction).getMoveFrom(), ((driveCity) bestAction).getMoveTo());
	  }
	  else if(bestAction instanceof directFlight)
	  {
		  directFlight(this.playerPiece.location, ((directFlight) bestAction).getMoveTo());
	  }
	  else if(bestAction instanceof charterFlight)
	  {
		  charterFlight(((charterFlight) bestAction).getMoveFrom(), ((charterFlight) bestAction).getMoveTo());
	  }
	  else if(bestAction instanceof shuttleFlight)
	  {
		  shuttleFlight(((shuttleFlight) bestAction).getMoveFrom(), ((shuttleFlight) bestAction).getMoveTo());
	  }
	  else if(bestAction instanceof treatDisease)
	  {
		  treatDisease(((treatDisease) bestAction).getLocation(), ((treatDisease) bestAction).getColour());
	  }
	  else if(bestAction instanceof buildResearchStation)
	  {
		  buildResearchStation();
	  }
	  
	  availableMoves.clear();
  }
	
  public Object clone(GameBoard gb, Piece pc) throws CloneNotSupportedException {
		Player cloned = (Player) super.clone();
		cloned.playerName = String.valueOf(this.playerName);
		cloned.playerRole = String.valueOf(this.playerRole);
		cloned.pandemicBoard = gb;
		cloned.playerPiece = pc;
		ArrayList<City> clonedhands = new ArrayList<City>();
		for (int i=0;i<this.hand.size();i++) {
			clonedhands.add((City)this.hand.get(i).clone());
		}
		cloned.hand = clonedhands;
		cloned.suggestions = this.suggestions; //shallow copy
		return cloned;
	}
  
}
