package generatedCode.dice;

import java.util.*;

public class DiceGame_152ad5f9_f1c8_40e7_b0dc_147b246111a3 {
    private static final int MAX_TURNS = 12;
    private static final int MAX_POINTS_DIFFERENCE = 10;
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
        String[] colors = {"red", "green", "blue"};

        for (int i = 0; i < 3; i++) {
            System.out.println("Enter name for player " + (i + 1) + ":");
            String name = scanner.nextLine();
            while (name.equals("Computer")) {
                System.out.println("Name cannot be Computer. Enter another name:");
                name = scanner.nextLine();
            }
            players[i] = new Player(name, colors[i]);
        }

        List<Player> turnOrder = Arrays.asList(players);
        Collections.shuffle(turnOrder);

        int turn = 0;
        while (turn < MAX_TURNS) {
            for (Player player : turnOrder) {
                int diceCount = random.nextInt(DICE_SIDES) + 1;
                if (diceCount == 1) diceCount = random.nextInt(DICE_SIDES) + 1;
                if (diceCount == 2) diceCount *= 3;
                else if (diceCount % 2 == 0) diceCount /= 2;
                player.points += diceCount;
                if (player.points > MAX_POINTS_COLOR_CHANGE) player.color = "purple";
                System.out.println(player.name + " rolled a " + diceCount + ". Total points: " + player.points + ". Color: " + player.color);
                if (player.points >= MAX_POINTS_DIFFERENCE && player.points - turnOrder.get((turnOrder.indexOf(player) + 1) % 3).points >= MAX_POINTS_DIFFERENCE && player.points - turnOrder.get((turnOrder.indexOf(player) + 2) % 3).points >= MAX_POINTS_DIFFERENCE) {
                    System.out.println("Congratulations " + player.name + ", you won the game! You are the best!");
                    return;
                }
                if (turnOrder.get(0).points == turnOrder.get(1).points && turnOrder.get(1).points == turnOrder.get(2).points) {
                    System.out.println("Equality!");
                }
                if (diceCount == 6) break;
                turn++;
                if (turn >= MAX_TURNS) break;
            }
        }
    }
}
