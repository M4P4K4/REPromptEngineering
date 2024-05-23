package groundtruth;

import java.util.*;

public class DiceGame {
    private static final int MAX_TURNS = 12;
    private static final int MAX_POINTS_DIFFERENCE = 10;
    private static final int MAX_POINTS_COLOR_CHANGE = 10;
    private static final int DICE_SIDES = 6;

    private static final String COLOR_RED = "red";
    private static final String COLOR_GREEN = "green";
    private static final String COLOR_BLUE = "blue";
    private static final String COLOR_PURPLE = "purple";

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
            while (name.equalsIgnoreCase("Computer")) {
                System.out.println("Name cannot be Computer. Enter another name:");
                name = scanner.nextLine();
            }
            String color = getColorForPlayer(i);
            players[i] = new Player(name, color);
        }

        List<Player> turnOrder = Arrays.asList(players);
        Collections.shuffle(turnOrder);

        int turn = 0;
        boolean turnSkipped = false;
        while (turn < MAX_TURNS) {
            System.out.println("Round " + (turn + 1) + ":");
            for (Player player : turnOrder) {
                if (!turnSkipped) {
                    int diceCount = random.nextInt(DICE_SIDES) + 1;
                    if (diceCount == 1) {
                        System.out.println("You rolled a 1. Do you want to re-roll? [Y/N]");
                        String reroll = scanner.nextLine();
                        if (reroll.equalsIgnoreCase("Y")) {
                            diceCount = random.nextInt(DICE_SIDES) + 1;
                        }
                    }
                    diceCount = applyDiceModifiers(diceCount);
                    player.points += diceCount;
                    if (player.points > MAX_POINTS_COLOR_CHANGE) {
                        player.color = COLOR_PURPLE;
                    }
                    System.out.println(player.name + " rolled a " + diceCount + ". Total points: " + player.points + " Color: " + player.color);
                    if (checkForWinner(players)) {
                        System.out.println("Congratulations " + player.name + " you won the game! You are the best!");
                        return;
                    }
                    if (diceCount == 6) {
                        turnSkipped = true;
                    }
                    if (checkForEquality(turnOrder)) {
                        System.out.println("Equality!");
                    }
                } else {
                    turnSkipped = false;
                }
            }
            turn++;
        }

        Player winner = determineWinner(players);
        System.out.println("Congratulations " + winner.name + " you won the game! You are the best!");
    }

    private static String getColorForPlayer(int index) {
        return index == 0 ? COLOR_RED : index == 1 ? COLOR_GREEN : COLOR_BLUE;
    }

    private static int applyDiceModifiers(int diceCount) {
        if (diceCount == 2) {
            return diceCount * 3;
        } else if (diceCount % 2 == 0) {
            return diceCount / 2;
        }
        return diceCount;
    }

    private static boolean checkForWinner(Player[] players) {
        for (Player player : players) {
            if (isWinner(player, players)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWinner(Player player, Player[] players) {
        for (Player opponent : players) {
            if (opponent != player && player.points - opponent.points >= MAX_POINTS_DIFFERENCE) {
                boolean allOpponentsLose = true;
                for (Player otherOpponent : players) {
                    if (otherOpponent != player && otherOpponent != opponent &&
                            player.points - otherOpponent.points < MAX_POINTS_DIFFERENCE) {
                        allOpponentsLose = false;
                        break;
                    }
                }
                if (allOpponentsLose) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkForEquality(List<Player> turnOrder) {
        return turnOrder.get(0).points == turnOrder.get(1).points &&
                turnOrder.get(1).points == turnOrder.get(2).points;
    }

    private static Player determineWinner(Player[] players) {
        Player winner = players[0];
        for (Player player : players) {
            if (player.points > winner.points) {
                winner = player;
            }
        }
        return winner;
    }
}
