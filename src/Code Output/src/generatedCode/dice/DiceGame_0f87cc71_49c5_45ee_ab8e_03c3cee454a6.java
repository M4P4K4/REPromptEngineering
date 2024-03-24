package generatedCode.dice;

import java.util.*;

public class DiceGame_0f87cc71_49c5_45ee_ab8e_03c3cee454a6 {
    private static final int MAX_TURNS = 12;
    private static final int POINTS_THRESHOLD = 10;

    private static final String[] COLORS = {"red", "green", "blue"};
    private static final String[] NAMES = new String[3];

    private static int[] points = new int[3];
    private static int currentPlayerIndex;
    private static boolean gameOver;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Get player names
        for (int i = 0; i < 3; i++) {
            System.out.print("Enter name for player " + (i + 1) + ": ");
            String name = scanner.nextLine();
            while (name.equalsIgnoreCase("Computer")) {
                System.out.println("Name cannot be Computer");
                System.out.print("Enter name for player " + (i + 1) + ": ");
                name = scanner.nextLine();
            }
            NAMES[i] = name;
        }

        // Randomly decide turn order
        List<Integer> turnOrder = new ArrayList<>(Arrays.asList(0, 1, 2));
        Collections.shuffle(turnOrder);

        // Game loop
        for (int turn = 1; turn <= MAX_TURNS; turn++) {
            currentPlayerIndex = turnOrder.get(turn % 3);
            int diceCount = throwDice();

            // Check if player wants to re-roll if dice count is 1
            if (diceCount == 1) {
                System.out.print(NAMES[currentPlayerIndex] + ", do you want to re-roll? (yes/no): ");
                String choice = scanner.nextLine();
                if (choice.equalsIgnoreCase("yes")) {
                    diceCount = throwDice();
                }
            }

            // Update player points
            points[currentPlayerIndex] += processDiceCount(diceCount);

            // Print dice count and player points
            System.out.println("Dice count: " + diceCount);
            printPlayerPoints();

            // Check if game should end
            if (checkGameOver()) {
                gameOver = true;
                break;
            }
        }

        // Determine winner
        if (!gameOver) {
            int maxPoints = Math.max(points[0], Math.max(points[1], points[2]));
            int winnerIndex = -1;
            for (int i = 0; i < 3; i++) {
                if (points[i] == maxPoints) {
                    if (winnerIndex == -1) {
                        winnerIndex = i;
                    } else {
                        System.out.println("Equality!");
                        return;
                    }
                }
            }
            System.out.println("Congratulations " + NAMES[winnerIndex] + " you won the game! You are the best!");
        }
    }

    private static int throwDice() {
        Random random = new Random();
        return random.nextInt(6) + 1;
    }

    private static int processDiceCount(int diceCount) {
        if (diceCount == 6) {
            currentPlayerIndex = (currentPlayerIndex + 1) % 3;
        } else if (diceCount % 2 == 0) {
            if (diceCount == 2) {
                diceCount *= 3;
            } else {
                diceCount /= 2;
            }
        }
        return diceCount;
    }

    private static void printPlayerPoints() {
        for (int i = 0; i < 3; i++) {
            String color = COLORS[i];
            if (points[i] > 11) {
                color = "purple";
            }
            System.out.println(NAMES[i] + "'s points: " + points[i] + " (" + color + ")");
        }
        System.out.println();
    }

    private static boolean checkGameOver() {
        int maxPoints = Math.max(points[0], Math.max(points[1], points[2]));
        int minPoints = Math.min(points[0], Math.min(points[1], points[2]));
        return (maxPoints - minPoints >= POINTS_THRESHOLD);
    }
}
