package generatedCode.dice;

import java.util.*;

public class DiceGame_35b5800f_224d_4374_a87e_815efc0046e9 {
    private static final int MAX_TURNS = 12;
    private static final int MAX_DIFF = 10;
    private static final int MAX_POINTS = 10;
    private static final String[] COLORS = {"red", "green", "blue", "purple"};
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
            while (name.equals("Computer")) {
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
                System.out.println(player.name + " rolled a " + dice + ". Their points are now " + player.points + " and their color is " + player.color + ".");
                if (checkWinCondition(players, player)) {
                    System.out.println("Congratulations " + player.name + ", you won the game! You are the best!");
                    return;
                }
                if (dice == 6) break;
                turn++;
                if (turn >= MAX_TURNS) return;
            }
        }
    }

    private static boolean checkWinCondition(Player[] players, Player current) {
        for (Player player : players) {
            if (player != current && Math.abs(player.points - current.points) >= MAX_DIFF) return true;
        }
        if (players[0].points == players[1].points && players[1].points == players[2].points) {
            System.out.println("Equality!");
        }
        return false;
    }
}
