package generatedCode.dice;

import java.util.*;

public class DiceGame_14f9420f_a7f3_4ac1_ace5_1859cc954245 {
    private static final int MAX_TURNS = 12;
    private static final int MAX_POINTS = 10;
    private static final String COMPUTER = "Computer";
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
            while (name.equals(COMPUTER)) {
                System.out.println("Name cannot be Computer. Enter another name:");
                name = scanner.nextLine();
            }
            players[i] = new Player(name, COLORS[i]);
        }

        List<Player> turnOrder = Arrays.asList(players);
        Collections.shuffle(turnOrder);

        int turn = 0;
        while (turn < MAX_TURNS) {
            for (Player player : turnOrder) {
                int diceCount = RANDOM.nextInt(6) + 1;
                if (diceCount == 1 && RANDOM.nextBoolean()) {
                    diceCount += RANDOM.nextInt(6) + 1;
                }
                if (diceCount == 2) {
                    diceCount *= 3;
                } else if (diceCount % 2 == 0) {
                    diceCount /= 2;
                }
                player.points += diceCount;
                if (player.points > MAX_POINTS) {
                    player.color = COLORS[3];
                }
                System.out.println(player.name + " rolled a " + diceCount + ". Total points: " + player.points + ". Color: " + player.color);
                if (player.points > MAX_POINTS * 2) {
                    System.out.println("Congratulations " + player.name + " you won the game! You are the best!");
                    return;
                }
                if (diceCount == 6) {
                    break;
                }
                turn++;
                if (turn >= MAX_TURNS) {
                    break;
                }
            }
        }
    }
}
