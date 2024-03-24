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
    List<Card> pile = new ArrayList<>();
    int score = 0;
}

class Game {
    List<Card> deck = new ArrayList<>();
    List<Player> players = new ArrayList<>();
    List<Card> table = new ArrayList<>();
    int targetScore;
    int dealerIndex = 0;

    Game(int numPlayers, int targetScore) {
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
        for (int i = 0; i < 4; i++) {
            table.add(deck.remove(deck.size() - 1));
        }
    }

    void playRound() {
        while (!players.get(0).hand.isEmpty()) {
            for (Player player : players) {
                Card card = player.hand.remove(0);
                for (Card tableCard : table) {
                    if (tableCard.value == card.value) {
                        player.pile.add(table.remove(table.indexOf(tableCard)));
                        break;
                    }
                }
                table.add(card);
            }
        }
        if (!table.isEmpty()) {
            players.get(players.size() - 1).pile.addAll(table);
            table.clear();
        }
    }

    void tallyScores() {
        for (Player player : players) {
            player.score += player.pile.size();
            for (Card card : player.pile) {
                if (card.suit.equals("Coins")) {
                    player.score++;
                }
            }
        }
    }

    void playGame() {
        while (players.get(0).score < targetScore) {
            dealCards();
            playRound();
            tallyScores();
            dealerIndex = (dealerIndex + 1) % players.size();
        }
    }
}
