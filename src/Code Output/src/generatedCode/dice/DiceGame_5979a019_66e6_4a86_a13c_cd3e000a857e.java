package generatedCode.dice;

import java.util.Random;

public class DiceGame_5979a019_66e6_4a86_a13c_cd3e000a857e {
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
                if (doubleTurn) {
                    doubleTurn = false;
                    turns[j]++;
                }
                turns[j]++;
                int dice = throwDice();
                if (dice == 1 && turns[j] < 2) {
                    dice += throwDice();
                }
                if (dice == 6) {
                    skipNextTurn = true;
                }
                if (dice % 2 == 0) {
                    dice /= 2;
                }
                if (dice == 2) {
                    dice *= 3;
                }
                scores[j] += dice;
                System.out.println("Player " + (j + 1) + " rolled a " + dice + ". Total score: " + scores[j]);
                if (scores[j] > 10) {
                    System.out.println("Player " + (j + 1) + "'s score is now purple!");
                }
                if (scores[0] == scores[1] && scores[1] == scores[2]) {
                    System.out.println("Equality!");
                }
                if (scores[j] > scores[(j + 1) % 3] + scores[(j + 2) % 3]) {
                    System.out.println("Player " + (j + 1) + " has significantly more points than the others. Game over!");
                    return;
                }
            }
        }
        int winner = scores[0] > scores[1] ? (scores[0] > scores[2] ? 1 : 3) : (scores[1] > scores[2] ? 2 : 3);
        System.out.println("Congratulations Player " + winner + ", you are the winner!");
    }

    private int throwDice() {
        return random.nextInt(6) + 1;
    }

    public static void main(String[] args) {
        new DiceGame_5979a019_66e6_4a86_a13c_cd3e000a857e().play();
    }
}
