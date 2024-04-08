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
    List<Card> table = new ArrayList<>();
    int targetScore;

    public Game(int numPlayers, int targetScore) {
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

    public void playRound() {
        for (Player player : players) {
            for (int i = 0; i < 3; i++) {
                player.hand.add(deck.remove(deck.size() - 1));
            }
        }
        for (int i = 0; i < 4; i++) {
            table.add(deck.remove(deck.size() - 1));
        }
        for (Player player : players) {
            Card card = player.hand.remove(0); // Assume player always plays first card
            int index = table.indexOf(card);
            if (index != -1) {
                player.score += table.remove(index).value;
                player.score += card.value;
            } else {
                table.add(card);
            }
        }
        if (table.isEmpty()) {
            players.get(players.size() - 1).score++; // Last player gets a "Scopa"
        }
        for (Player player : players) {
            while (!player.hand.isEmpty()) {
                player.hand.add(deck.remove(deck.size() - 1));
            }
        }
        if (!deck.isEmpty()) {
            playRound();
        } else {
            for (Card card : table) {
                players.get(players.size() - 1).score += card.value; // Last player gets remaining cards
            }
            table.clear();
        }
    }

    public void playGame() {
        while (true) {
            playRound();
            for (Player player : players) {
                if (player.score >= targetScore) {
                    System.out.println("Player " + players.indexOf(player) + " wins!");
                    return;
                }
            }
        }
    }
}
