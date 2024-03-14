package generatedCode.dice;

import java.util.*;

public class DiceGame_5b5cf1aa_f1f9_4c59_8fcf_f14d688fcc8d {
    private static final int MAX_TURNS = 12;
    private static final int MAX_POINTS = 10;
    private static final int DICE_SIDES = 6;
    private static final String[] COLORS = {"red", "green", "blue", "purple"};
    private static final String COMPUTER_NAME = "Computer";

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
        Random random = new Random();

        Player[] players = new Player[3];
        for (int i = 0; i < 3; i++) {
            System.out.println("Enter name for player " + (i + 1) + ":");
            String name = scanner.nextLine();
            while (name.equals(COMPUTER_NAME)) {
                System.out.println("Name cannot be Computer. Enter another name:");
                name = scanner.nextLine();
            }
            players[i] = new Player(name, COLORS[i]);
        }

        int turn = 0;
        while (turn < MAX_TURNS) {
            for (Player player : players) {
                int diceCount = random.nextInt(DICE_SIDES) + 1;
                if (diceCount == 1) {
                    diceCount += random.nextInt(DICE_SIDES) + 1;
                } else if (diceCount == 2) {
                    diceCount *= 3;
                } else if (diceCount % 2 == 0) {
                    diceCount /= 2;
                }

                player.points += diceCount;
                if (player.points > MAX_POINTS) {
                    player.color = COLORS[3];
                }

                System.out.println(player.name + " rolled a " + diceCount + " and now has " + player.points + " points and color " + player.color);

                if (player.points > MAX_POINTS) {
                    System.out.println("Congratulations " + player.name + " you won the game! You are the best!");
                    return;
                }

                if (players[0].points == players[1].points && players[1].points == players[2].points) {
                    System.out.println("Equality!");
                }

                if (diceCount == 6) {
                    break;
                }

                turn++;
                if (turn >= MAX_TURNS) {
                    break;
                }
            }
        }
    }
}
