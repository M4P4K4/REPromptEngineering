package groundtruth;

import java.util.*;

class ScopaGame {
    private static final int SCORE_TO_WIN = 8;

    List<Card> deck = new ArrayList<>();
    List<Card> table = new ArrayList<>();
    List<Player> players = new ArrayList<>();
    Player lastDrawnCards = null;
    int currentPlayerIndex = 0;

    private enum Suit {
        Coins,
        Cups,
        Swords,
        Clubs
    }

    private static class Card {
        Suit suit;
        int value;

        Card(Suit suit, int value) {
            this.suit = suit;
            this.value = value;
        }

        public String toString() {
            return value + " of " + suit;
        }
    }

    private static class Player {
        List<Card> hand = new ArrayList<>();
        List<Card> discardPile = new ArrayList<>();
        int score = 0;
        int number;

        Player (int number) {
            this.number = number;
        }
    }

    ScopaGame() {
        for (int i = 1; i <= 3; i++) {
            players.add(new Player(i));
        }

        this.playGame();
    }

    public static void main(String[] args) {
        new ScopaGame();
    }

    void initializeDeck() {
        deck.clear();
        table.clear();

        for (Suit suit : Suit.values()) {
            for (int i = 1; i <= 10; i++) {
                deck.add(new Card(suit, i));
            }
        }
        Collections.shuffle(deck);
    }

    void initializeNewRound() {
        initializeDeck();
        dealCards();
        currentPlayerIndex = new Random().nextInt(3);
    }

    void dealCards() {
        for (Player player : players) {
            player.hand.clear();
            player.discardPile.clear();
            drawCards(player);
        }
        for (int i = 0; i < 4; i++) {
            table.add(deck.removeLast());
        }
    }

    void drawCards(Player player) {
        for (int i = 0; i < 3; i++) {
            if (!deck.isEmpty())
                player.hand.add(deck.removeLast());
        }
        lastDrawnCards = player;
    }

    void playTurn(Player currentPlayer) {
        List<Card> matchingCards = new ArrayList<>();
        int sizeTableCards = table.size();

        if (currentPlayer.hand.isEmpty()) {
            drawCards(currentPlayer);
        }
        Card selectedCard = selectCard(currentPlayer);
        currentPlayer.hand.remove(selectedCard);

        for (Card tableCard : table) {
            if (tableCard.value == selectedCard.value) {
                currentPlayer.discardPile.add(selectedCard);
                matchingCards.add(tableCard);
            }
        }

        for (Card matchingCard : matchingCards) {
            currentPlayer.discardPile.add(matchingCard);
            table.remove(matchingCard);
        }

        if (matchingCards.size() == sizeTableCards) {
            currentPlayer.score++;
        }
        else if (matchingCards.isEmpty()) {
            table.add(selectedCard);
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % 3;
    }

    private Card selectCard(Player player) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Cards currently on the table: " + getCardListAsString(table, false));
        System.out.println("Choose a card from your hand: " + getCardListAsString(player.hand, true));
        int cardNr = Integer.parseInt(scanner.nextLine());
        while (cardNr < 0 || cardNr >= player.hand.size()) {
            System.out.println("Select a value between 0 and " + (player.hand.size() - 1) + ". Choose another card:");
            cardNr = Integer.parseInt(scanner.nextLine());
        }
        return player.hand.get(cardNr);
    }

    void playRound() {
        while (!deck.isEmpty()) {
            playTurn(players.get(currentPlayerIndex));
        }
    }

    void playGame() {
        while (players.stream().noneMatch(player -> player.score >= SCORE_TO_WIN)) {
            initializeNewRound();
            playRound();
            while (!table.isEmpty()) {
                lastDrawnCards.discardPile.add(table.removeLast());
            }
            countPoints();
        }
        for (Player player : players) {
            if (player.score >= SCORE_TO_WIN) {
                System.out.println("Congratulation Player " + player.number + ", you won the game!");
            }
        }
    }

    void countPoints() {
        assignPointMostCards();
        assignPointMostCoinCards();
        assignPointsSevenOfCoins();
        assignPointsHighestCards();
    }

    private void assignPointMostCards() {
        List<Player> winner = new ArrayList<>();
        for (Player player : players) {
            if (winner.isEmpty() || player.discardPile.size() == winner.size()) {
                winner.add(player);
            }
            else if (player.discardPile.size() > winner.getFirst().discardPile.size()) {
                winner.clear();
                winner.add(player);
            }
        }
        for (Player player : winner) {
            player.score += 1;
        }
    }

    private void assignPointMostCoinCards() {
        List<Player> winner = new ArrayList<>();
        int winnerAmount = 0;
        for (Player player : players) {
            int playerAmount = 0;
            for (Card card : player.discardPile) {
                if (card.suit.equals(Suit.Coins)) {
                    playerAmount++;
                }
            }
            if (playerAmount == winnerAmount) {
                winner.add(player);
            }
            else if (playerAmount > winnerAmount) {
                winner.clear();
                winner.add(player);
                winnerAmount = playerAmount;
            }
        }
        for (Player player : winner) {
            player.score++;
        }
    }

    private void assignPointsSevenOfCoins() {
        for (Player player : players) {
            for (Card card : player.discardPile) {
                if (card.suit.equals(Suit.Coins) && card.value == 7) {
                    player.score++;
                }
            }
        }
    }

    private void assignPointsHighestCards() {
        List<Player> winner = new ArrayList<>();
        int winnerAmount = 0;
        for (Player player : players) {
            int playerAmount = 0;
            for (Card card : player.discardPile) {
                playerAmount += card.value;
            }
            if (playerAmount == winnerAmount) {
                winner.add(player);
            }
            else if (playerAmount > winnerAmount) {
                winner.clear();
                winner.add(player);
                winnerAmount = playerAmount;
            }
        }
        for (Player player : winner) {
            player.score++;
        }
    }

    private String getCardListAsString(List<Card> cards, Boolean numbered) {
        StringBuilder allCards = new StringBuilder();
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            if (numbered) {
                allCards.append("[").append(i).append("] ");
            }
            allCards.append(card.toString());
            if (i != cards.size() - 1) {
                allCards.append(", ");
            }
        }
        return allCards.toString().trim();
    }
}
