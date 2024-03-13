package generatedCode.dice;

import java.util.*;

/**
 * commented version of:
 * a34b28b6-1362-4010-a993-ebcf28b8d715
 */
public class DiceGame20240313233640467667 {
    private static final int MAX_TURNS = 12; // Rule 7
    private static final int MAX_DIFF = 10;
    private static final int MAX_POINTS = 10;
    private static final int MAX_DICE = 6;
    private static final String[] COLORS = {"Red", "Green", "Blue", "Purple"}; // Rule 11
    private static final String COMPUTER = "Computer";
    private static final Random RANDOM = new Random();

    private String[] players = new String[3];
    private int[] points = new int[3]; // Rule 2
    private String[] colors = new String[3];
    private int turn = 0;

    public static void main(String[] args) {
        DiceGame20240313233640467667 game = new DiceGame20240313233640467667();
        game.start();
    }

    private void start() {
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < 3; i++) { // Rule 1 + 16
            System.out.println("Enter name for player " + (i + 1) + ":");
            players[i] = scanner.nextLine();
            while (players[i].equals(COMPUTER)) {
                System.out.println("Name cannot be Computer. Enter another name:");
                players[i] = scanner.nextLine();
            }
            colors[i] = COLORS[i];
        }

        while (turn < MAX_TURNS && !isGameOver()) {
            int player = turn % 3; // Rule 3?
            int dice = RANDOM.nextInt(MAX_DICE) + 1; // Rule 4
            if (dice == 1) dice = RANDOM.nextInt(MAX_DICE) + 1; // Rule 9
            if (dice == 2) dice *= 3; // Rule 13
            else if (dice % 2 == 0) dice /= 2; // Rule 13
            points[player] += dice; // Rule 5
            if (points[player] > MAX_POINTS) colors[player] = COLORS[3]; // Rule 11
            System.out.println("Player " + players[player] + " rolled a " + dice + ". Total points: " + points[player] + ". Color: " + colors[player]); // Rule 6
            if (dice == 6) turn++; // Rule 12
            turn++;
            if (points[0] == points[1] && points[1] == points[2]) System.out.println("Equality!"); // Rule 14
        }

        int winner = points[0] > points[1] ? (points[0] > points[2] ? 0 : 2) : (points[1] > points[2] ? 1 : 2);
        System.out.println("Congratulations " + players[winner] + " you won the game! You are the best!"); // Rule 10
    }

    private boolean isGameOver() {
        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 3; j++) {
                if (Math.abs(points[i] - points[j]) >= MAX_DIFF) return true; // Rule 8
            }
        }
        return false;
    }
}