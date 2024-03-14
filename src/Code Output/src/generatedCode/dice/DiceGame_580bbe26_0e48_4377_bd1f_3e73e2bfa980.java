package generatedCode.dice;

import java.util.Random;
import java.util.Scanner;

/**
 * Model: gpt 3.5 turbo !
 */
public class DiceGame_580bbe26_0e48_4377_bd1f_3e73e2bfa980 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        int[] playerPoints = {0, 0, 0};
        String[] playerNames = new String[3];

        System.out.println("Welcome to the Dice Game for three players!");

        for (int i = 0; i < 3; i++) {
            System.out.print("Enter player " + (i + 1) + " name: ");
            String name = scanner.nextLine();
            while (name.equalsIgnoreCase("Computer")) {
                System.out.println("Name cannot be Computer. Please enter a different name.");
                name = scanner.nextLine();
            }
            playerNames[i] = name;
        }

        int currentPlayer = random.nextInt(3);
        int turns = 0;

        while (turns < 12) {
            int diceCount = random.nextInt(6) + 1;

            if (diceCount == 1) {
                System.out.println(playerNames[currentPlayer] + " rolled a 1. Rolling again...");
                diceCount = random.nextInt(6) + 1;
            }

            if (diceCount == 6) {
                System.out.println(playerNames[currentPlayer] + " rolled a 6. Skipping next player's turn.");
                currentPlayer = (currentPlayer + 2) % 3;
            }

            if (diceCount % 2 == 0 && diceCount != 2) {
                diceCount /= 2;
            } else if (diceCount == 2) {
                diceCount *= 3;
            }

            playerPoints[currentPlayer] += diceCount;

            System.out.println(playerNames[currentPlayer] + " rolled a " + diceCount + ". Points: " + playerPoints[currentPlayer]);

            if (playerPoints[currentPlayer] >= 10) {
                System.out.println(playerNames[currentPlayer] + " has won the game! Congratulations " + playerNames[currentPlayer] + " you are the best!");
                break;
            }

            if (playerPoints[0] == playerPoints[1] || playerPoints[1] == playerPoints[2] || playerPoints[0] == playerPoints[2]) {
                System.out.println("Equality!");
            }

            if (playerPoints[currentPlayer] > 10) {
                System.out.println(playerNames[currentPlayer] + " has turned purple!");
            }

            currentPlayer = (currentPlayer + 1) % 3;
            turns++;
        }

        System.out.println("Game over. Thank you for playing!");
    }
}
