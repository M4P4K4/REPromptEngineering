package generatedCode.dice;

import java.util.*;

public class DiceGame_05eef439_692f_460b_b2af_71edee1ceb8c {
    private static final int MAX_TURNS = 12; // Rule 7
    private static final int MAX_POINTS = 10;
    private static final String[] COLORS = {"RED", "GREEN", "BLUE", "PURPLE"}; // Rule 11
    private static final String[] PLAYERS = new String[3]; // Rule 1
    private static final int[] POINTS = new int[3]; // Rule 2
    private static int turn = 0;
    private static boolean skipNextTurn = false;
    private static boolean doubleThrow = false;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        for (int i = 0; i < 3; i++) {
            System.out.println("Enter name for player " + (i + 1) + ":");
            String name = scanner.nextLine();
            while ("Computer".equals(name)) { // Rule 16
                System.out.println("Name cannot be 'Computer'. Enter another name:");
                name = scanner.nextLine();
            }
            PLAYERS[i] = name;
        }

        while (turn < MAX_TURNS) { // Rule 3 + 7
            for (int i = 0; i < 3; i++) {
                if (skipNextTurn) {
                    skipNextTurn = false;
                    continue;
                }

                int dice = random.nextInt(6) + 1; // Rule 4
                if (dice == 1 && random.nextBoolean()) {
                    dice += random.nextInt(6) + 1;
                }
                if (dice == 6) { // Rule 12
                    skipNextTurn = true;
                }
                if (doubleThrow) {
                    dice += random.nextInt(6) + 1;
                    doubleThrow = false;
                }
                if (dice % 2 == 0) { // Rule 13
                    dice /= 2;
                }
                if (dice == 2) { // Rule 13
                    dice *= 3;
                }

                POINTS[i] += dice; // Rule 5
                System.out.println(PLAYERS[i] + " rolled a " + dice + ". Total points: " + POINTS[i] + ". Color: " + getColor(i)); // Rule 6

                if (POINTS[i] > MAX_POINTS) {
                    System.out.println("Congratulations " + PLAYERS[i] + "! You have won the game!"); // Rule 10
                    return;
                }

                if (POINTS[0] == POINTS[1] && POINTS[1] == POINTS[2]) { // Rule 14
                    System.out.println("Equality!");
                }

                turn++;
                if (turn >= MAX_TURNS) {
                    break;
                }
            }
        }

        System.out.println("Game over. No one won.");
    }

    private static String getColor(int player) { // Rule 11
        if (POINTS[player] > MAX_POINTS) {
            return COLORS[3];
        } else {
            return COLORS[player];
        }
    }
}
