package generatedCode.scopa;

import java.util.*;

class Card {
    String suit;
    int value;

    public Card(String suit, int value) {
        this.suit = suit;
        this.value = value;
    }
}

class Player {
    List<Card> hand = new ArrayList<>();
    int score = 0;
}

class Game {
    List<Card> deck = new ArrayList<>();
    List<Player> players = new ArrayList<>();
    int targetScore;

    public Game(int targetScore) {
        this.targetScore = targetScore;
        initializeDeck();
        shuffleDeck();
    }

    private void initializeDeck() {
        String[] suits = {"Coins", "Cups", "Swords", "Clubs"};
        for (String suit : suits) {
            for (int i = 1; i <= 10; i++) {
                deck.add(new Card(suit, i));
            }
        }
    }

    private void shuffleDeck() {
        Collections.shuffle(deck);
    }

    public void addPlayer(Player player) {
        if (players.size() < 4) {
            players.add(player);
        } else {
            System.out.println("Cannot add more players. Maximum of 4 players allowed.");
        }
    }

    public void startGame() {
        // Game logic goes here
    }
}
