package generatedCode.dice;

import java.util.*;

public class DiceGame_275f2f9f_670c_496d_8e80_90b654c445aa {
    private static final int MAX_TURNS = 12;
    private static final int MAX_POINTS = 10;
    private static final String[] COLORS = {"red", "green", "blue", "purple"};
    private static final Random RANDOM = new Random();

    private String[] players = new String[3];
    private int[] points = new int[3];
    private int[] colors = new int[3];
    private int turn = 0;

    public static void main(String[] args) {
        new DiceGame_275f2f9f_670c_496d_8e80_90b654c445aa().play();
    }

    private void play() {
        Scanner scanner = new Scanner(System.in);
        for (int i = 0; i < 3; i++) {
            System.out.println("Enter name for player " + (i + 1) + ":");
            String name = scanner.nextLine();
            while (name.equalsIgnoreCase("Computer")) {
                System.out.println("Name cannot be Computer. Enter another name:");
                name = scanner.nextLine();
            }
            players[i] = name;
            colors[i] = i;
        }

        while (turn < MAX_TURNS) {
            int player = RANDOM.nextInt(3);
            int dice = RANDOM.nextInt(6) + 1;
            System.out.println(players[player] + " throws a " + dice);

            if (dice == 1 && RANDOM.nextBoolean()) {
                System.out.println(players[player] + " throws again");
                dice = RANDOM.nextInt(6) + 1;
                System.out.println(players[player] + " throws a " + dice);
            }

            if (dice == 2) {
                dice *= 3;
            } else if (dice % 2 == 0) {
                dice /= 2;
            }

            points[player] += dice;
            System.out.println(players[player] + " has " + points[player] + " points");

            if (points[player] > MAX_POINTS) {
                colors[player] = 3;
            }

            if (points[0] == points[1] && points[1] == points[2]) {
                System.out.println("Equality!");
            }

            if (dice == 6) {
                System.out.println("Next player's turn is skipped");
                turn++;
            }

            turn++;
        }

        int winner = 0;
        for (int i = 1; i < 3; i++) {
            if (points[i] > points[winner]) {
                winner = i;
            }
        }

        System.out.println("Congratulations " + players[winner] + " you won the game! You are the best!");
    }
}
