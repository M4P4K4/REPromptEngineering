package generatedCode.dice;

import java.util.*;

public class DiceGame_7eb24db1_b65c_45d6_a2d7_8881b25c562d {
    private static final int MAX_TURNS = 12;
    private static final int MAX_DIFF = 10;
    private static final int MAX_POINTS = 10;
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
                int dice = RANDOM.nextInt(6) + 1;
                if (dice == 1) dice = RANDOM.nextInt(6) + 1;
                if (dice == 2) dice *= 3;
                else if (dice % 2 == 0) dice /= 2;
                player.points += dice;
                if (player.points > MAX_POINTS) player.color = COLORS[3];
                System.out.println(player.name + " rolled a " + dice + ". Total points: " + player.points + ". Color: " + player.color);
                if (player.points >= MAX_DIFF) {
                    System.out.println("Congratulations " + player.name + " you won the game! You are the best!");
                    return;
                }
                if (dice == 6) break;
                turn++;
                if (turn >= MAX_TURNS) return;
            }
            if (players[0].points == players[1].points && players[1].points == players[2].points) {
                System.out.println("Equality!");
            }
        }
    }
}
