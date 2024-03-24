package generatedCode.scopa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

enum Suit {
    COINS, CUPS, SWORDS, CLUBS
}

class Card {
    private Suit suit;
    private int rank;

    public Card(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}

class Player {
    private String name;
    private List<Card> hand;
    private List<Card> takenCards;
    private int score;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.takenCards = new ArrayList<>();
        this.score = 0;
    }

    public String getName() {
        return name;
    }

    public List<Card> getHand() {
        return hand;
    }

    public List<Card> getTakenCards() {
        return takenCards;
    }

    public int getScore() {
        return score;
    }

    public void addToScore(int points) {
        score += points;
    }

    public void takeCard(Card card) {
        hand.add(card);
    }

    public void takeCards(List<Card> cards) {
        takenCards.addAll(cards);
    }

    public void clearHand() {
        hand.clear();
    }

    public void clearTakenCards() {
        takenCards.clear();
    }

    @Override
    public String toString() {
        return name;
    }
}

class ScopaGame_b753e227_bafc_4e23_8886_44c2bd60d78a {
    private List<Player> players;
    private List<Card> deck;
    private List<Card> table;
    private Player dealer;
    private int targetScore;

    public ScopaGame_b753e227_bafc_4e23_8886_44c2bd60d78a(int numPlayers, int targetScore) {
        this.players = new ArrayList<>();
        this.deck = new ArrayList<>();
        this.table = new ArrayList<>();
        this.targetScore = targetScore;

        for (int i = 1; i <= 10; i++) {
            deck.add(new Card(Suit.COINS, i));
            deck.add(new Card(Suit.CUPS, i));
            deck.add(new Card(Suit.SWORDS, i));
            deck.add(new Card(Suit.CLUBS, i));
        }

        this.dealer = null;

        for (int i = 1; i <= numPlayers; i++) {
            players.add(new Player("Player " + i));
        }
    }

    public void playGame() {
        while (true) {
            initializeRound();
            playRound();
            int winnerIndex = checkWinner();
            if (winnerIndex != -1) {
                System.out.println(players.get(winnerIndex).getName() + " wins the game!");
                break;
            }
        }
    }

    private void initializeRound() {
        deck.clear();
        table.clear();

        for (int i = 1; i <= 10; i++) {
            deck.add(new Card(Suit.COINS, i));
            deck.add(new Card(Suit.CUPS, i));
            deck.add(new Card(Suit.SWORDS, i));
            deck.add(new Card(Suit.CLUBS, i));
        }

        shuffleDeck();

        for (Player player : players) {
            player.clearHand();
            player.clearTakenCards();
            for (int i = 0; i < 3; i++) {
                player.takeCard(drawCard());
            }
        }

        for (int i = 0; i < 4; i++) {
            table.add(drawCard());
        }

        if (dealer == null) {
            Random random = new Random();
            dealer = players.get(random.nextInt(players.size()));
        } else {
            int dealerIndex = players.indexOf(dealer);
            dealer = players.get((dealerIndex + 1) % players.size());
        }
    }

    private void playRound() {
        Player currentPlayer = dealer;
        int numRounds = 0;

        while (numRounds < 3) {
            for (int i = 0; i < players.size(); i++) {
                currentPlayer = players.get((players.indexOf(currentPlayer) + 1) % players.size());
                playTurn(currentPlayer);
            }
            numRounds++;
        }

        while (!table.isEmpty()) {
            currentPlayer.takeCards(table);
        }

        for (Player player : players) {
            player.addToScore(countScopas(player.getTakenCards()));
            player.addToScore(countPrimiera(player.getTakenCards()));
        }
    }

    private void playTurn(Player player) {
        Card playedCard = player.getHand().remove(0);
        List<Card> matchingCards = findMatchingCards(playedCard);

        if (!matchingCards.isEmpty()) {
            player.takeCard(playedCard);
            player.takeCards(matchingCards);
            table.removeAll(matchingCards);
            table.remove(playedCard);
        } else {
            table.add(playedCard);
        }

        if (table.isEmpty()) {
            player.addToScore(1); // Scopa point
        }
    }

    private List<Card> findMatchingCards(Card card) {
        List<Card> matchingCards = new ArrayList<>();
        for (Card tableCard : table) {
            if (tableCard.getRank() == card.getRank()) {
                matchingCards.add(tableCard);
            }
        }
        return matchingCards;
    }

    private int countScopas(List<Card> takenCards) {
        int scopas = 0;
        for (Card card : takenCards) {
            if (card.getRank() == 7 && card.getSuit() == Suit.COINS) {
                scopas++;
            }
        }
        return scopas;
    }

    private int countPrimiera(List<Card> takenCards) {
        int primiera = 0;
        int[] primieraValues = {16, 12, 13, 14, 18, 21, 10};

        for (int value : primieraValues) {
            for (Card card : takenCards) {
                if (card.getRank() == value) {
                    primiera += value;
                }
            }
        }

        return primiera;
    }

    private int checkWinner() {
        int maxScore = 0;
        int winnerIndex = -1;

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.getScore() >= targetScore) {
                return i;
            }
            if (player.getScore() > maxScore) {
                maxScore = player.getScore();
                winnerIndex = i;
            }
        }

        return winnerIndex;
    }

    private Card drawCard() {
        return deck.remove(deck.size() - 1);
    }

    private void shuffleDeck() {
        Collections.shuffle(deck);
    }
}

public class Main {
    public static void main(String[] args) {
        ScopaGame_b753e227_bafc_4e23_8886_44c2bd60d78a game = new ScopaGame_b753e227_bafc_4e23_8886_44c2bd60d78a(2, 21);
        game.playGame();
    }
}
