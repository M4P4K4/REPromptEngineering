package generatedCode.dice;

import java.util.*;

public class DiceGame_Ground_Truth_1 {
    private static final int MAX_TURNS = 12; // Rule 7
    private static final int MAX_POINTS_DIFFERENCE = 10;
    private static final int MAX_POINTS_COLOR_CHANGE = 10;
    private static final int DICE_SIDES = 6;

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
        Random random = new Random();

        Player[] players = new Player[3]; // Rule 1
        for (int i = 0; i < 3; i++) {
            System.out.println("Enter name for player " + (i + 1) + ":");
            String name = scanner.nextLine();
            while (name.equals("Computer")) { // Rule 16
                System.out.println("Name cannot be Computer. Enter another name:");
                name = scanner.nextLine();
            }
            String color = i == 0 ? "red" : i == 1 ? "green" : "blue"; // Rule 11
            players[i] = new Player(name, color);
        }

        List<Player> turnOrder = Arrays.asList(players);
        Collections.shuffle(turnOrder); // Rule 15

        int turn = 0;
        boolean turnSkipped = false;
        while (turn < MAX_TURNS) { // Rule 7
            System.out.println("Round " + (turn + 1) + ":");
            for (Player player : turnOrder) { // Rule 3
                if (!turnSkipped) {
                    int diceCount = random.nextInt(DICE_SIDES) + 1; // Rule 4
                    if (diceCount == 1) {
                        System.out.println("You rolled a 1. Do you want to re-roll? [Y/N]");
                        String reroll = scanner.nextLine();
                        if (reroll.equals("Y") || reroll.equals("y")) {
                            diceCount = random.nextInt(DICE_SIDES) + 1; // Rule 9
                        }
                    }
                    if (diceCount == 2) diceCount *= 3; // Rule 13
                    else if (diceCount % 2 == 0) diceCount /= 2; // Rule 13
                    player.points += diceCount; // Rule 5
                    if (player.points > MAX_POINTS_COLOR_CHANGE) player.color = "purple"; // Rule 11
                    System.out.println(player.name + " rolled a " + diceCount + ". Total points: " + player.points + " Color: " + player.color); // Rule 6
                    if (player.points >= MAX_POINTS_DIFFERENCE && player.points - turnOrder.get((turnOrder.indexOf(player) + 1) % 3).points >= MAX_POINTS_DIFFERENCE && player.points - turnOrder.get((turnOrder.indexOf(player) + 2) % 3).points >= MAX_POINTS_DIFFERENCE) { // Rule 8
                        System.out.println("Congratulations " + player.name + " you won the game! You are the best!"); // Rule 10
                        return;
                    }
                    if (diceCount == 6) turnSkipped = true; // Rule 12
                    if (turnOrder.get(0).points == turnOrder.get(1).points && turnOrder.get(1).points == turnOrder.get(2).points) System.out.println("Equality!"); // Rule 14
                }
                else {
                    turnSkipped = false;
                }
            }
            turn++;
        }

        Player winner;
        if (players[0].points > players[1].points && players[0].points > players[2].points) {
            winner = players[0];
        }
        else if (players[1].points > players[2].points) {
            winner = players[1];
        }
        else {
            winner = players[2];
        }
        System.out.println("Congratulations " + winner.name + " you won the game! You are the best!"); // Rule 10
    }
}
