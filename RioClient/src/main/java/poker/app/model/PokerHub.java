package poker.app.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import exceptions.DeckException;
import netgame.common.Hub;
import pokerBase.Action;
import pokerBase.Card;
import pokerBase.CardDraw;
import pokerBase.Deck;
import pokerBase.GamePlay;
import pokerBase.GamePlayPlayerHand;
import pokerBase.Player;
import pokerBase.Rule;
import pokerBase.Table;
import pokerEnums.eAction;
import pokerEnums.eCardDestination;
import pokerEnums.eDrawCount;
import pokerEnums.eGame;
import pokerEnums.eGameState;

public class PokerHub extends Hub {

	private Table HubPokerTable = new Table();
	private GamePlay HubGamePlay;
	private int iDealNbr = 0;
	private eGameState eGameState;

	public PokerHub(int port) throws IOException {
		super(port);
	}

	protected void playerConnected(int playerID) {

		if (playerID == 2) {
			shutdownServerSocket();
		}
	}

	protected void playerDisconnected(int playerID) {
		shutDownHub();
	}

	protected void messageReceived(int ClientID, Object message) {

		if (message instanceof Action) {
			
			Action run = (Action) message;
			switch (run.getAction()) {
			
			case GameState:
				sendToAll(HubPokerTable);
				break;
			
			
			case TableState:
				resetOutput();
				sendToAll(HubPokerTable);
				break;
				
			case Sit:
				resetOutput();
				HubPokerTable.AddPlayerToTable(run.getPlayer()); //Adds player to the table
				sendToAll(HubPokerTable); //Updates the table for all players
				break;
				
			case Leave:
				resetOutput();
				HubPokerTable.RemovePlayerFromTable(run.getPlayer()); //Removes player from table
				sendToAll(HubPokerTable); //Updates the table for all player
				break;
				
			case StartGame:
				resetOutput();
				
				eGame newGame = run.geteGame();
				Rule rule = new Rule(newGame);
				
				//Randomly picks the starting player
				HashMap<UUID, Player> players = new HashMap<UUID, Player>();
				int firstplayer = (int)Math.round(Math.random());
				Player[] player = players.values().toArray(new Player[1]);
				Player p = player[firstplayer];
				
				HubGamePlay = new GamePlay(rule, p.getPlayerID());
				
				HashMap<Integer, Player> playerPosition = new HashMap<Integer, Player>();
				HubGamePlay.setGamePlayers(HubPokerTable.getHashPlayers());
				
				//Initializes the deck
				int Jokers = rule.GetNumberOfJokers();
				Deck deck = new Deck(Jokers, rule.GetWildCards());
				HubGamePlay.setGameDeck(deck);
				
				//Sets the order of players
				HubGamePlay.setiActOrder(GamePlay.GetOrder(p.getiPlayerPosition()));

				HubGamePlay.setPlayerNextToAct(HubGamePlay.getPlayerByPosition(p.getiPlayerPosition()));
				sendToAll(HubGamePlay);
				break;
			}
			
					
		}

		System.out.println("Message Received by Hub");
		
		sendToAll("Sending Message Back to Client");
	}

}
