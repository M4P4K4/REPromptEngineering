package generatedCode.dice;

import java.util.*;

public class DiceGame_7efc59e8_2b96_4f05_ae52_bc7ad97a3bae {
    private static final int MAX_TURNS = 12;
    private static final int MAX_POINTS_DIFF = 10;
    private static final int MAX_POINTS_COLOR_CHANGE = 10;
    private static final int DICE_SIDES = 6;

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
            while (name.equals("Computer")) {
                System.out.println("Name cannot be Computer. Enter another name:");
                name = scanner.nextLine();
            }
            String color = i == 0 ? "red" : i == 1 ? "green" : "blue";
            players[i] = new Player(name, color);
        }

        int turn = 0;
        while (turn < MAX_TURNS) {
            for (int i = 0; i < 3; i++) {
                if (turn >= MAX_TURNS) break;

                int dice = random.nextInt(DICE_SIDES) + 1;
                if (dice == 1) dice = random.nextInt(DICE_SIDES) + 1;
                if (dice == 2) dice *= 3;
                else if (dice % 2 == 0) dice /= 2;
                if (dice == 6) i++;

                players[i].points += dice;
                if (players[i].points > MAX_POINTS_COLOR_CHANGE) players[i].color = "purple";

                System.out.println("Player " + players[i].name + " (" + players[i].color + ") rolled a " + dice + ". Total points: " + players[i].points);

                if (players[i].points >= MAX_POINTS_DIFF && players[i].points - 10 > players[(i + 1) % 3].points && players[i].points - 10 > players[(i + 2) % 3].points) {
                    System.out.println("Congratulations " + players[i].name + ", you won the game! You are the best!");
                    return;
                }

                if (players[0].points == players[1].points && players[0].points == players[2].points) {
                    System.out.println("Equality!");
                }

                turn++;
            }
        }
    }
}
