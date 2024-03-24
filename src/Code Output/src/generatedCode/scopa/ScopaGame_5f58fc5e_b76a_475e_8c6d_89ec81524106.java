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
    String name;
    int score;

    Player(String name) {
        this.name = name;
    }

    void addScore(int score) {
        this.score += score;
    }
}

class Game {
    List<Card> deck;
    List<Player> players;
    int targetScore;

    Game(int targetScore, String... playerNames) {
        this.targetScore = targetScore;
        this.players = new ArrayList<>();
        for (String name : playerNames) {
            this.players.add(new Player(name));
        }
        this.deck = new ArrayList<>();
        String[] suits = {"Coins", "Cups", "Swords", "Clubs"};
        for (String suit : suits) {
            for (int i = 1; i <= 10; i++) {
                this.deck.add(new Card(suit, i));
            }
        }
        Collections.shuffle(this.deck);
    }

    void play() {
        while (true) {
            for (Player player : players) {
                if (deck.isEmpty()) {
                    return;
                }
                Card card = deck.remove(deck.size() - 1);
                player.addScore(card.value);
                System.out.println(player.name + " drew a " + card.value + " of " + card.suit + ". Score: " + player.score);
                if (player.score >= targetScore) {
                    System.out.println(player.name + " wins!");
                    return;
                }
            }
        }
    }

    public static void main(String[] args) {
        Game game = new Game(21, "Player1", "Player2", "Player3", "Player4");
        game.play();
    }
}
