package generatedCode.dice;

import java.util.*;

public class DiceGame_1d00b246_7a24_4165_9e84_b34008239c59 {
    private static final int MAX_TURNS = 12;
    private static final int MAX_POINTS = 10;
    private static final int DICE_SIDES = 6;
    private static final String[] COLORS = {"Red", "Green", "Blue", "Purple"};
    private static final Random RANDOM = new Random();

    private static class Player {
        String name;
        int points;
        String color;

        Player(String name, String color) {
            this.name = name;
            this.points = 0;
            this.color = color;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Player[] players = new Player[3];

        for (int i = 0; i < 3; i++) {
            System.out.println("Enter name for player " + (i + 1) + ":");
            String name = scanner.nextLine();
            while (name.equalsIgnoreCase("Computer")) {
                System.out.println("Name cannot be Computer. Enter another name:");
                name = scanner.nextLine();
            }
            players[i] = new Player(name, COLORS[i]);
        }

        int turn = 0;
        while (turn < MAX_TURNS) {
            for (Player player : players) {
                int diceCount = RANDOM.nextInt(DICE_SIDES) + 1;
                if (diceCount == 1) {
                    diceCount += RANDOM.nextInt(DICE_SIDES) + 1;
                } else if (diceCount == 2) {
                    diceCount *= 3;
                } else if (diceCount % 2 == 0) {
                    diceCount /= 2;
                }
                player.points += diceCount;
                if (player.points > MAX_POINTS) {
                    player.color = COLORS[3];
                }
                System.out.println(player.name + " rolled a " + diceCount + ". Total points: " + player.points + ". Color: " + player.color);
                if (diceCount == 6) {
                    break;
                }
                if (checkEquality(players)) {
                    System.out.println("Equality!");
                }
                if (checkWinner(players, player)) {
                    System.out.println("Congratulations " + player.name + " you won the game! You are the best!");
                    return;
                }
            }
            turn++;
        }
    }

    private static boolean checkEquality(Player[] players) {
        return players[0].points == players[1].points && players[1].points == players[2].points;
    }

    private static boolean checkWinner(Player[] players, Player currentPlayer) {
        for (Player player : players) {
            if (player != currentPlayer && currentPlayer.points > player.points + MAX_POINTS) {
                return true;
            }
        }
        return false;
    }
}
