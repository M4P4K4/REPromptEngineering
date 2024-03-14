package generatedCode.dice;

import java.util.*;

public class DiceGame_b6a50411_51cf_47bb_8e86_e88c88106863 {
    private static final int MAX_TURNS = 12; // Rule 7
    private static final int MAX_DIFF = 10; // Rule 8
    private static final int MAX_POINTS = 10;
    private static final int DICE_SIDES = 6;
    private static final String[] COLORS = {"red", "green", "blue", "purple"};
    private static final Random RANDOM = new Random();

    private static class Player {
        String name;
        int points;
        String color;

        Player(String name, String color) {
            this.name = name;
            this.points = 0; // Rule 2
            this.color = color;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Player[] players = new Player[3]; // Rule 1
        for (int i = 0; i < 3; i++) {
            System.out.println("Enter player " + (i + 1) + " name:"); // Rule 16
            String name = scanner.nextLine();
            while (name.equals("Computer")) {
                System.out.println("Name cannot be Computer. Enter another name:");
                name = scanner.nextLine();
            }
            players[i] = new Player(name, COLORS[i]); // Rule 11
        }

        int turn = 0;
        while (turn < MAX_TURNS) { // Rule 3
            for (Player player : players) {
                int dice = RANDOM.nextInt(DICE_SIDES) + 1; // Rule 4
                if (dice == 1) dice = RANDOM.nextInt(DICE_SIDES) + 1; // Rule 9
                if (dice == 2) dice *= 3; // Rule 13
                else if (dice % 2 == 0) dice /= 2; // Rule 13
                player.points += dice; // Rule 5
                if (player.points > MAX_POINTS) player.color = COLORS[3]; // Rule 11
                System.out.println(player.name + " rolled a " + dice + ". Total points: " + player.points + ". Color: " + player.color); // Rule 6
                if (checkWinCondition(players)) {
                    System.out.println("Congratulations " + player.name + " you won the game! You are the best!"); // Rule 10
                    return;
                }
                if (dice == 6) break; // Rule 12. Not correct, should be turn++
                turn++;
                if (turn >= MAX_TURNS) return;
            }
        }
    }

    private static boolean checkWinCondition(Player[] players) {
        for (int i = 0; i < players.length; i++) {
            for (int j = i + 1; j < players.length; j++) {
                if (Math.abs(players[i].points - players[j].points) >= MAX_DIFF) return true;
                if (players[i].points == players[j].points) System.out.println("Equality!"); // Rule 14. Not correct, only tests 2 player
            }
        }
        return false;
    }
}
