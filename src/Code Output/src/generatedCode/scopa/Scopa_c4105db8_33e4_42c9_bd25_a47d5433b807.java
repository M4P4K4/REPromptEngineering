package generatedCode.scopa;

import java.util.*;

class Card {
    String suit;
    int value;

    Card(String suit, int value) {
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

    Game(int targetScore, int numPlayers) {
        this.targetScore = targetScore;
        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player());
        }
        String[] suits = {"Coins", "Cups", "Swords", "Clubs"};
        for (String suit : suits) {
            for (int i = 1; i <= 10; i++) {
                deck.add(new Card(suit, i));
            }
        }
        Collections.shuffle(deck);
    }

    void dealCards() {
        for (Player player : players) {
            for (int i = 0; i < 3; i++) {
                player.hand.add(deck.remove(deck.size() - 1));
            }
        }
    }

    void playRound() {
        dealCards();
        // game logic goes here
    }

    void playGame() {
        while (players.stream().noneMatch(p -> p.score >= targetScore)) {
            playRound();
        }
    }
}
