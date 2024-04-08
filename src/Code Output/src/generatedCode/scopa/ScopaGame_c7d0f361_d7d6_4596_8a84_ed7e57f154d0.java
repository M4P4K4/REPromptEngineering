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
    List<Card> discardPile = new ArrayList<>();
    int score = 0;
}

class Game {
    List<Card> deck = new ArrayList<>();
    List<Card> table = new ArrayList<>();
    List<Player> players = new ArrayList<>();
    int currentPlayerIndex = 0;

    Game() {
        String[] suits = {"Coins", "Cups", "Swords", "Clubs"};
        for (String suit : suits) {
            for (int i = 1; i <= 10; i++) {
                deck.add(new Card(suit, i));
            }
        }
        Collections.shuffle(deck);

        for (int i = 0; i < 3; i++) {
            players.add(new Player());
        }

        dealCards();
        currentPlayerIndex = new Random().nextInt(3);
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

    void playTurn() {
        Player currentPlayer = players.get(currentPlayerIndex);
        Card selectedCard = currentPlayer.hand.remove(0);
        boolean matchFound = false;

        for (Card tableCard : table) {
            if (tableCard.value == selectedCard.value) {
                currentPlayer.discardPile.add(tableCard);
                table.remove(tableCard);
                matchFound = true;
                break;
            }
        }

        if (!matchFound) {
            table.add(selectedCard);
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % 3;
    }

    void playRound() {
        while (!deck.isEmpty()) {
            playTurn();
        }
        for (Player player : players) {
            if (!player.hand.isEmpty()) {
                playTurn();
            }
        }
    }

    void playGame() {
        while (players.stream().noneMatch(player -> player.score >= 8)) {
            playRound();
        }
    }
}
