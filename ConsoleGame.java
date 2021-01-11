package com.hannahbella.game;

import com.hannahbella.gamecomponents.*;
import com.hannahbella.gameauxiliary.*;
import java.util.Random;
import java.util.Scanner;

/**
 * Attack on Titan-inspired Console Combat Game.
 * @author Hannah Bella C. Arceño
 * @version Alpha 1.0 
 * @since January 2020
 */
public class ConsoleGame {

	private MissionLocation missionLocation;
	private Player player;
	private Enemy enemy;
	private Comrade comrade;

	private final LocationFactory locationFactory;
	private final ComradeFactory comradeFactory;
	private static MessageGenerator messageGenerator;
	private final Random rand;
	private final Scanner scanStr;
	private final Scanner scanInt;

	public ConsoleGame() {
		locationFactory = new LocationFactory();
		comradeFactory = new ComradeFactory();
		messageGenerator = new MessageGenerator();
		scanStr = new Scanner(System.in);
		scanInt = new Scanner(System.in);
		rand = new Random();
	}

	/**
	 * This is the main method where the program starts.
	 * @param args default main method parameter
	 * @throws InterruptedException exception when game is interrupted
	 */
	public static void main(String[] args) throws InterruptedException {

		ConsoleGame game = new ConsoleGame();

		boolean doesPlayerJoin = game.welcomePlayer();

		if (doesPlayerJoin) {

			String playerName = game.askPlayerName();
			RegimentAffiliation playerAffiliation = game.askPlayerAffiliation();
			String locationName = game.chooseMissionLocation();
			
			game.initializePlayer(playerName, playerAffiliation);
			game.initializeLocationAndEnemy(locationName);
			
			playerName = locationName = null;
			playerAffiliation = null; 
			
			// Pre-game messages
			System.out.println("\nPlayer: " + game.player.getName());
			System.out.println("Regiment/Squad: " + game.player.getAffiliation().formalName + " / " + game.player.getAffiliation().popularName);
			Thread.sleep(1000);				
			System.out.println("\n" + messageGenerator.getCreatePlayerMessage());
			Thread.sleep(1000);
			System.out.println(messageGenerator.getGoToLocationMessage());
			Thread.sleep(1000);
			System.out.println(messageGenerator.getEnemyFoundMessage());
			Thread.sleep(1000);
			System.out.println("\nLocation: " + game.missionLocation.getName());	
			System.out.println("Mission: Defeat the " + game.enemy.getName() + "!");
			
			game.startGame();
		}

		game.scanInt.close();
		game.scanStr.close();
		game.quitProgram();
	}

	/**
	 * This method confirms if player wants to play the game or not.
	 * @return TRUE if player joins, FALSE otherwise
	 */
	public boolean welcomePlayer() {

		// Ask player to join or exit game
		System.out.println("Welcome to Paradis! Join the missions in defeating the titans!");
		System.out.println("\nPress:" + "\t1 -> Join Mission\n" + "\tOTHER NUMBER KEY -> Quit");
		System.out.print("\nChoice: ");

		int playerAction = scanInt.nextInt();

		boolean playerJoins = playerAction == 1 ? true : false;

		return playerJoins;
	}

	/**
	 * This method asks for the player name.
	 * @return name of player
	 */
	public String askPlayerName() {

		System.out.print("Enter your name: ");
		String playerName = scanStr.nextLine();

		return playerName;
	}

	/**
	 * This method asks the player for desired squad affiliation.
	 * It also verifies the input entered by the player before returning an object or value
	 * (ex. player enters a number not included in the choices or a string value,
	 * this method will change the value entered by the player into a random but valid value)
	 * @return player squad affiliation
	 */
	public RegimentAffiliation askPlayerAffiliation() {		
		
		// Show affiliation choices
		RegimentAffiliation[] affiliationChoices = RegimentAffiliation.values();
		
		System.out.println("\nChoose a squad: ");
		showChoices(affiliationChoices);
		
		// Get choice		
		System.out.print("\nChoice: ");
		int affiliationNum = scanInt.nextInt();
		
		// Verify choice
		if (verifyWithinListRange(affiliationNum, affiliationChoices.length)) {
			return affiliationChoices[affiliationNum - 1];

		} else {
			affiliationNum = rand.nextInt(affiliationChoices.length);
			System.out.println("CHOICE INVALID.\nYou were randomly assigned by the officer.");
			return affiliationChoices[affiliationNum];
		}		
	}
	
	/**
	 * This method asks for desired mission location of player.
	 * It also verifies the input entered by the player before returning an object or value
	 * (ex. player enters a number not included in the choices or a string value,
	 * this method will change the value entered by the player into a random but valid value)
	 * @return name of mission location
	 */
	public String chooseMissionLocation() {
		
		// Show location choices
		String[] locationChoices = locationFactory.getListLocations();
		
		System.out.println("\nChoose location:");
		showChoices(locationChoices);

		// Get choice		
		System.out.print("\nChoice: ");
		int chosenLocationNum = scanInt.nextInt();

		// Verify choice
		if (verifyWithinListRange(chosenLocationNum, locationChoices.length)) {
			return locationChoices[chosenLocationNum - 1];

		} else {
			System.out.println("CHOICE INVALID.\nYour squad captain decided to make the choice.");
			chosenLocationNum = rand.nextInt(locationChoices.length);
			return locationChoices[chosenLocationNum];
		}
	}

	/**
	 * This method instantiates the player (Object).
	 * @param playerName name of player
	 * @param playerAffiliation regiment/squad affiliation of player
	 */
	public void initializePlayer(String playerName, RegimentAffiliation playerAffiliation) {

		player = new GamePlayer();
		player.setName(playerName);
		player.setAffiliation(playerAffiliation);		
	}

	/**
	 * This method instantiates the location (Object) and enemy (Object).
	 * It uses LocationFactory to get desired location object. 
	 * @param locationName name of location
	 */
	public void initializeLocationAndEnemy(String locationName) {

		missionLocation = locationFactory.getLocation(locationName);
		enemy = missionLocation.getEnemy();
	}
	
	/**
	 * This method facilitates the flow of the real game.
	 */
	public void startGame() {

		showPlayerAndEnemyStats(); // Initial stats
		awaitUserToContinue();

		int helpRequestUsed = 0; // Counts number of help requests made by player
		boolean continueGame = true; // Indicates whether the game continues

		// Game loop
		while (continueGame) {

			int helpLeft = checkHelpLeft(helpRequestUsed); // Counts number of help requests left
			
			// Ask action from player
			System.out.println("\nWhat would you do?\n"
							+ "1 -> FIGHT!\n" 
							+ "2 -> ASK HELP (" + (helpLeft) + " left)\n"
							+ "3 -> TRY ESCAPE from enemy\n"
							+ "Other number keys -> QUIT");

			System.out.print("\nChoose Action: ");
			int playerAction = scanInt.nextInt();

			// Player fights
			if (playerAction == 1) {
				playerAttackEnemy();
				awaitUserToContinue();
				
				if (!hasWinner()) {			
					enemyAttackPlayer();
					awaitUserToContinue();
					
					healComponent(player);
					healComponent(enemy);
					awaitUserToContinue();

				} else {
					showPlayerAndEnemyStats();
					break;
				}
				
			// Player asks help
			} else if ((playerAction == 2) && (helpLeft != 0)) {
				helpRequestUsed++;

				String chosenComrade = chooseComrade();
				getComrade(chosenComrade);
				askComradeHelp();
				awaitUserToContinue();

			// Player asks for help but allowed number of help req. has been used up
			} else if ((playerAction == 2) && (helpLeft == 0)) {
				System.out.println("\nSorry. You used up allowed number of help requests.\n");
				awaitUserToContinue();

			// Player wants to try escaping from enemy
			} else if (playerAction == 3) {
				// Whether escape is successful or not, this option will close the game
				escapeFromEnemy();
				break;

			// Player wants to quit
			} else {
				System.out.println("It's sad to see you go. We hope you come back!");
				break;
			}
			showPlayerAndEnemyStats();
			
			// Check for winner
			continueGame = !hasWinner();
		}		
	}
	
	/**
	 * This method shows the HP of player and enemy.
	 */
	public void showPlayerAndEnemyStats() {

		System.out.println("\n<-------- Player & Enemy Stats -------->");
		System.out.println("Player: " + player.getName() + "\nHP: " + player.getHealthPoints());
		System.out.println("\nEnemy: " + enemy.getName() + "\nHP: " + enemy.getHealthPoints());
		System.out.println("----------------------------------------");
	}
	
	/**
	 * This method shows the choices contained in a list.
	 * @param listChoices list of choices
	 */
	public void showChoices(Object[] listChoices) {
			
		for (int i = 0; i < listChoices.length; i++) {
			System.out.println((i + 1) + ") " + listChoices[i]);
		}
	}

	/**
	 * This method lets the player attack the enemy.
	 */
	public void playerAttackEnemy() {

		int playerAttackDamage = player.getAttackPoints();
		enemy.receiveDamagePoints(playerAttackDamage);

		System.out.println("\n" + messageGenerator.getPlayerAttacksMessage());
		System.out.println("Player Attack Damage: " + playerAttackDamage + "\n");
	}

	/**
	 * This method lets the enemy attack the player.
	 */
	public void enemyAttackPlayer() {

		int enemyAttackDamage = enemy.getAttackPoints();
		player.receiveDamagePoints(enemyAttackDamage);

		System.out.println(messageGenerator.getEnemyAttacksMessage());
		System.out.println("Enemy Attack Damage: " + enemyAttackDamage + "\n");
	}

	/**
	 * This method decides if given game component (player/enemy) would be healed (HP will increase) or not.
	 * If it decides to heal component, it will generate a random int value and add it to component's HP.
	 * @param component game component (either player/enemy object)
	 */
	public void healComponent(Object component) {

		int MAX_HEAL = 15;
		boolean doesHealComponent = rand.nextBoolean();
		int amountHeal = rand.nextInt(MAX_HEAL);

		if (doesHealComponent) {

			amountHeal = amountHeal == 0 ? 1 : amountHeal;

			if (component instanceof Player) {
				player.receiveHealPoints(amountHeal);
				System.out.println("\n***\nA healing potion has been dropped for you!");
				System.out.println("Adding " + amountHeal + " point/s to your HP...\n***");

			} else {
				enemy.receiveHealPoints(amountHeal);
				System.out.println("\n***\nUh-oh! The enemy regenerated!");
				System.out.println("They got " + amountHeal + " heal point/s!\n***");
			}
		}
	}

	/**
	 * This method allows player to choose a comrade.
	 * It also verifies the input of the player. If choice is invalid, it will return a valid choice.
	 * @return name of comrade
	 */
	public String chooseComrade() {

		// Show comrade choices
		String[] comradeChoices = comradeFactory.getListComrades();	
		
		System.out.println("\nChoose comrade:");
		showChoices(comradeChoices);

		// Get choice
		System.out.print("\nChoice: ");
		int chosenComradeNum = scanInt.nextInt();

		// Verify choice
		if ( verifyWithinListRange(chosenComradeNum, comradeChoices.length) ) {
			return comradeChoices[chosenComradeNum - 1];

		} else {
			System.out.println("CHOICE INVALID.\nBut a random comrade came near you.");
			return "Random";
		}
	}

	/**
	 * This method instantiates the comrade (Object).
	 * It uses the ComradeFactory to instantiate the correct comrade object.
	 * @param comradeName name of comrade
	 */
	public void getComrade(String comradeName) {
		comrade = comradeFactory.getComrade(comradeName);
	}

	/**
	 * This method allows the player to ask help from their chosen comrade.
	 * However, help is not guaranteed.
	 * If the comrade helps, it would deal damage to the enemy.
	 */
	public void askComradeHelp() {

		boolean comradeHelps = comrade.helpPlayer();
		int helpAmount = comrade.getHelpPoints();

		if (comradeHelps) {
			
			enemy.receiveDamagePoints(helpAmount);
			
			System.out.println("\n" + comrade.getName() + " from the " + comrade.getAffiliation().popularName
							+ " decided to help you!\n");
			System.out.println(comrade.getName() + ": " + comrade.getQuote());
			System.out.println(comrade.getName() + "'s Attack Damage: " + helpAmount + "\n");

		} else {
			System.out.println("\nSorry. " + comrade.getName() + " decided not to help you."
							+ "\nThey said you can do it on your own. Good luck!\n");
		}
	}

	/**
	 * This method decides if escaping from enemy is successful or not.
	 */
	public void escapeFromEnemy() {

		boolean isEscapeSuccess = rand.nextBoolean();

		if (isEscapeSuccess) {
			System.out.println(messageGenerator.getEscapeSuccessMessage());

		} else {
			System.out.println(messageGenerator.getEscapeFailedMessage() + "\nRIP, " + player.getName());
		}
	}

	/**
	 * This method verifies if the number of choice from a numbered list is within the range of the list indices.
	 * @param choiceNum number of choice
	 * @param listLength length/size of the list
	 * @return TRUE if within range, FALSE otherwise
	 */
	public boolean verifyWithinListRange(int choiceNum, int listLength) {

		if ( (choiceNum <= listLength) && (choiceNum > 0) ) {
			return true;

		} else {
			return false;
		}
	}

	/**
	 * This method checks the amount of help left for player.
	 * It ensures that help left would not be less than zero.
	 * @param helpRequestCount total help requests made by player
	 * @return amount of help left
	 */
	public int checkHelpLeft(int helpRequestCount) {

		int MAX_HELP = 3;

		// If total help requests exceeds maximum requests allowed, just return 0
		if (helpRequestCount > MAX_HELP) {
			return 0;
			// Otherwise, return difference between max. requests and total requests used
		} else {
			return (MAX_HELP - helpRequestCount);
		}
	}

	/**
	 * This method checks if the player or the enemy is defeated.
	 * @return TRUE if one player/enemy/both is defeated, FALSE otherwise
	 */
	public boolean hasWinner() {

		// Both player and enemy dies
		if (enemy.getHealthPoints() == 0 && player.getHealthPoints() == 0) {
			
			System.out.println(	messageGenerator.getPlayerAndEnemyDefeatMessage()
							+ "\nRIP, mighty " + player.getName() + ".");
			return true;

		// Player defeated
		} else if (player.getHealthPoints() == 0) {
			
			System.out.println(messageGenerator.getEnemyDefeatsPlayerMessage());
			return true;

		// Enemy defeated
		} else if (enemy.getHealthPoints() == 0) {
			
			System.out.println(messageGenerator.getPlayerDefeatsEnemyMessage() +
						"\nThe " + player.getAffiliation().popularName + " is proud to have you!");
			return true;

		// Winner undetermined
		} else {
			return false;
		}
	}

	/**
	 * This method allows the player to pace the game on their own.
	 */
	public void awaitUserToContinue() {

		System.out.println("(Press enter to continue)");
		@SuppressWarnings("unused")
		String emptyStr = scanStr.nextLine();
		emptyStr = null;
	}

	/**
	 * This method terminates the program.
	 */
	public void quitProgram() {

		System.out.println("\nClosing game...");
		System.exit(0);
	}
}