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
        String[] suits = {"Coins", "Cups", "Swords", "Clubs"};
        for (String suit : suits) {
            for (int i = 1; i <= 10; i++) {
                deck.add(new Card(suit, i));
            }
        }
        Collections.shuffle(deck);
        for (int i = 0; i < numPlayers; i++) {
            players.add(new Player());
        }
        dealCards();
    }

    public void dealCards() {
        for (Player player : players) {
            for (int i = 0; i < 3; i++) {
                player.hand.add(deck.remove(deck.size() - 1));
            }
        }
        for (int i = 0; i < 4; i++) {
            table.add(deck.remove(deck.size() - 1));
        }
    }

    public void playRound() {
        for (Player player : players) {
            Card card = player.hand.remove(0);
            int sum = 0;
            for (Card tableCard : table) {
                sum += tableCard.value;
            }
            if (sum == card.value || table.contains(card)) {
                player.score += card.value;
                table.remove(card);
            } else {
                table.add(card);
            }
            if (table.isEmpty()) {
                player.score += 1; // Scopa
            }
        }
        if (deck.isEmpty()) {
            Player lastPlayer = players.get(players.size() - 1);
            for (Card card : table) {
                lastPlayer.score += card.value;
            }
            table.clear();
        } else {
            dealCards();
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

public class Main {
    public static void main(String[] args) {
        Game game = new Game(2, 21);
        game.playGame();
    }
}