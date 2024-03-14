package generatedCode.dice;

import java.util.Random;

public class DiceGame_de2a5a41_25df_4f72_869a_4cdd98a39d7e {
    private int[] scores = new int[3];
    private int[] turns = new int[3];
    private boolean skipNextTurn = false;
    private boolean doubleTurn = false;
    private Random random = new Random();

    public void play() {
        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 3; j++) {
                if (skipNextTurn) {
                    skipNextTurn = false;
                    continue;
                }
                int dice = throwDice();
                if (dice == 1 && random.nextBoolean()) {
                    dice += throwDice();
                }
                if (dice == 6) {
                    skipNextTurn = true;
                }
                if (doubleTurn) {
                    dice += throwDice();
                    doubleTurn = false;
                }
                if (dice == 2) {
                    dice *= 3;
                }
                if (dice % 2 == 0) {
                    dice /= 2;
                }
                scores[j] += dice;
                turns[j]++;
                printScore(j);
                if (scores[j] > 10) {
                    System.out.println("Player " + (j + 1) + " has more than 10 points. They turn purple!");
                }
                if (scores[j] > scores[(j + 1) % 3] + scores[(j + 2) % 3]) {
                    System.out.println("Player " + (j + 1) + " wins!");
                    return;
                }
                if (scores[0] == scores[1] && scores[1] == scores[2]) {
                    System.out.println("Equality!");
                }
            }
        }
        int winner = scores[0] > scores[1] ? (scores[0] > scores[2] ? 0 : 2) : (scores[1] > scores[2] ? 1 : 2);
        System.out.println("Player " + (winner + 1) + " wins!");
    }

    private int throwDice() {
        return random.nextInt(6) + 1;
    }

    private void printScore(int player) {
        System.out.println("Player " + (player + 1) + " threw the dice and got " + scores[player] + " points.");
    }

    public static void main(String[] args) {
        new DiceGame_de2a5a41_25df_4f72_869a_4cdd98a39d7e().play();
    }
}
