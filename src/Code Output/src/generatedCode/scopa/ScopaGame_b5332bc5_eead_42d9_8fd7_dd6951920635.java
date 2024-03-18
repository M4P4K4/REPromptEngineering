package generatedCode.scopa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ScopaGame_b5332bc5_eead_42d9_8fd7_dd6951920635 {
    private static final int SCORE_LIMIT = 21;
    private static final int NUM_PLAYERS = 2;
    private static final int NUM_CARDS_PER_HAND = 3;
    private static final int NUM_CARDS_ON_TABLE = 4;

    private static final List<String> SUITS = Arrays.asList("Coins", "Cups", "Swords", "Clubs");
    private static final List<String> RANKS = Arrays.asList("Ace", "2", "3", "4", "5", "6", "7", "Fante", "Cavallo", "Re");
    private static final List<Integer> POINTS = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    private List<String> deck;
    private List<String> tableCards;
    private List<List<String>> playerHands;
    private List<List<String>> playerTakenCards;
    private List<Integer> playerScores;
    private int currentPlayerIndex;
    private int mazziereIndex;

    public ScopaGame_b5332bc5_eead_42d9_8fd7_dd6951920635() {
        deck = new ArrayList<>();
        tableCards = new ArrayList<>();
        playerHands = new ArrayList<>();
        playerTakenCards = new ArrayList<>();
        playerScores = new ArrayList<>();
        currentPlayerIndex = 0;
        mazziereIndex = 0;
    }

    public void playGame() {
        initializeGame();

        while (!isGameOver()) {
            playRound();
            calculateRoundScores();
            printRoundScores();
            checkGameEnd();
            prepareNextRound();
        }

        printGameWinner();
    }

    private void initializeGame() {
        createDeck();
        shuffleDeck();
        dealInitialCards();
        determineMazziere();
    }

    private void createDeck() {
        for (String suit : SUITS) {
            for (String rank : RANKS) {
                deck.add(rank + " of " + suit);
            }
        }
    }

    private void shuffleDeck() {
        Random random = new Random();
        for (int i = deck.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            String temp = deck.get(i);
            deck.set(i, deck.get(j));
            deck.set(j, temp);
        }
    }

    private void dealInitialCards() {
        for (int i = 0; i < NUM_PLAYERS; i++) {
            playerHands.add(new ArrayList<>());
            playerTakenCards.add(new ArrayList<>());
            playerScores.add(0);
        }

        for (int i = 0; i < NUM_CARDS_PER_HAND; i++) {
            for (int j = 0; j < NUM_PLAYERS; j++) {
                String card = deck.remove(deck.size() - 1);
                playerHands.get(j).add(card);
            }
        }

        for (int i = 0; i < NUM_CARDS_ON_TABLE; i++) {
            tableCards.add(deck.remove(deck.size() - 1));
        }
    }

    private void determineMazziere() {
        Random random = new Random();
        mazziereIndex = random.nextInt(NUM_PLAYERS);
        currentPlayerIndex = (mazziereIndex + 1) % NUM_PLAYERS;
        System.out.println("Player " + (mazziereIndex + 1) + " is the mazziere.");
    }

    private void playRound() {
        System.out.println("Round " + (playerHands.get(0).size() / NUM_CARDS_PER_HAND));

        while (!playerHands.get(currentPlayerIndex).isEmpty()) {
            System.out.println("Player " + (currentPlayerIndex + 1) + "'s turn");
            printTableCards();

            String playedCard = getPlayerCardChoice();
            if (isCardOnTable(playedCard)) {
                takeMatchingCards(playedCard);
            } else {
                placeCardOnTable(playedCard);
            }

            currentPlayerIndex = (currentPlayerIndex + 1) % NUM_PLAYERS;
        }
    }

    private String getPlayerCardChoice() {
        Scanner scanner = new Scanner(System.in);
        String card;

        while (true) {
            System.out.print("Choose a card to play: ");
            System.out.println(playerHands.get(currentPlayerIndex)); // manually implemented
            card = scanner.nextLine();

            if (playerHands.get(currentPlayerIndex).contains(card)) {
                break;
            } else {
                System.out.println("Invalid card choice. Try again.");
            }
        }

        playerHands.get(currentPlayerIndex).remove(card);
        return card;
    }

    private boolean isCardOnTable(String card) {
        return tableCards.contains(card);
    }

    private void takeMatchingCards(String card) {
        playerTakenCards.get(currentPlayerIndex).add(card);
        tableCards.remove(card);

        for (String tableCard : tableCards) {
            if (getCardValue(card) == getCardValue(tableCard)) {
                playerTakenCards.get(currentPlayerIndex).add(tableCard);
            }
        }

        tableCards.removeAll(playerTakenCards.get(currentPlayerIndex));
        playerScores.set(currentPlayerIndex, playerScores.get(currentPlayerIndex) + 1);
        System.out.println("Player " + (currentPlayerIndex + 1) + " earned a Scopa!");
    }

    private void placeCardOnTable(String card) {
        tableCards.add(card);
    }

    private void calculateRoundScores() {
        for (int i = 0; i < NUM_PLAYERS; i++) {
            int scopaPoints = countScopaPoints(playerTakenCards.get(i));
            int cardPoints = playerTakenCards.get(i).size();
            int coinsPoints = countCoinsPoints(playerTakenCards.get(i));
            int settebelloPoints = countSettebelloPoints(playerTakenCards.get(i));
            int primieraPoints = countPrimieraPoints(playerTakenCards.get(i));

            playerScores.set(i, playerScores.get(i) + scopaPoints + cardPoints + coinsPoints + settebelloPoints + primieraPoints);
        }
    }

    private int countScopaPoints(List<String> takenCards) {
        int scopaPoints = 0;
        for (String card : takenCards) {
            if (card.equals("Scopa")) {
                scopaPoints++;
            }
        }
        return scopaPoints;
    }

    private int countCoinsPoints(List<String> takenCards) {
        int coinsPoints = 0;
        for (String card : takenCards) {
            if (card.contains("Coins")) {
                coinsPoints++;
            }
        }
        return coinsPoints;
    }

    private int countSettebelloPoints(List<String> takenCards) {
        if (takenCards.contains("7 of Coins")) {
            return 1;
        }
        return 0;
    }

    private int countPrimieraPoints(List<String> takenCards) {
        int primieraPoints = 0;
        for (String suit : SUITS) {
            int maxRankValue = 0;
            for (String card : takenCards) {
                if (card.contains(suit)) {
                    int rankValue = getCardRankValue(card);
                    if (rankValue > maxRankValue) {
                        maxRankValue = rankValue;
                    }
                }
            }
            primieraPoints += getPrimieraPoints(maxRankValue);
        }
        return primieraPoints;
    }

    private int getCardValue(String card) {
        String rank = card.split(" ")[0];
        return POINTS.get(RANKS.indexOf(rank));
    }

    private int getCardRankValue(String card) {
        String rank = card.split(" ")[0];
        return RANKS.indexOf(rank);
    }

    private int getPrimieraPoints(int rankValue) {
        switch (rankValue) {
            case 7:
                return 21;
            case 6:
                return 18;
            case 1:
                return 16;
            case 4:
                return 14;
            case 3:
                return 13;
            case 2:
                return 12;
            default:
                return 10;
        }
    }

    private void printTableCards() {
        System.out.println("Table cards: " + tableCards);
    }

    private void printRoundScores() {
        System.out.println("Round scores:");
        for (int i = 0; i < NUM_PLAYERS; i++) {
            System.out.println("Player " + (i + 1) + ": " + playerScores.get(i));
        }
    }

    private void checkGameEnd() {
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (playerScores.get(i) >= SCORE_LIMIT) {
                System.out.println("Player " + (i + 1) + " wins the game!");
                System.exit(0);
            }
        }
    }

    private void prepareNextRound() {
        for (int i = 0; i < NUM_PLAYERS; i++) {
            playerHands.get(i).addAll(playerTakenCards.get(i));
            playerTakenCards.get(i).clear();
        }

        if (deck.size() < NUM_PLAYERS * NUM_CARDS_PER_HAND) {
            deck.addAll(tableCards);
            tableCards.clear();
        }

        for (int i = 0; i < NUM_CARDS_PER_HAND; i++) {
            for (int j = 0; j < NUM_PLAYERS; j++) {
                String card = deck.remove(deck.size() - 1);
                playerHands.get(j).add(card);
            }
        }

        currentPlayerIndex = (mazziereIndex + 1) % NUM_PLAYERS;
    }

    private boolean isGameOver() {
        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (playerHands.get(i).size() > 0) {
                return false;
            }
        }
        return true;
    }

    private void printGameWinner() {
        int maxScore = 0;
        int winnerIndex = -1;

        for (int i = 0; i < NUM_PLAYERS; i++) {
            if (playerScores.get(i) > maxScore) {
                maxScore = playerScores.get(i);
                winnerIndex = i;
            }
        }

        System.out.println("Player " + (winnerIndex + 1) + " wins the game!");
    }

    public static void main(String[] args) {
        ScopaGame_b5332bc5_eead_42d9_8fd7_dd6951920635 game = new ScopaGame_b5332bc5_eead_42d9_8fd7_dd6951920635();
        game.playGame();
    }
}
