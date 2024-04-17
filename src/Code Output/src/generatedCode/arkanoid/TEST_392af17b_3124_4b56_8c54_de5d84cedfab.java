package generatedCode.arkanoid;

import java.util.Random;

public class Game {
    private int level;
    private int lives;
    private int ballSpeed;
    private Paddle paddle;
    private Ball ball;
    private Brick[] bricks;

    public Game() {
        this.level = 1;
        this.lives = 3; // Rule 15
        this.ballSpeed = 5;
        this.paddle = new Paddle();
        this.ball = new Ball();
        this.bricks = new Brick[50]; // Rule 8
        initializeBricks(); // Rule 8
    }

    public void initializeBricks() {
        Random rand = new Random();
        for (int i = 0; i < bricks.length; i++) { // Rule 14
            int x = rand.nextInt(180);
            int y = rand.nextInt(100);
            bricks[i] = new Brick(x, y, "blue", 1); // Rule 7
        }
    }

    public void startLevel() {
        paddle.reset(); // Rule 9
        ball.reset(); // Rule 10
        ball.setSpeed(ballSpeed);
        for (Brick brick : bricks) {
            if (brick != null) {
                brick.draw();
            }
        }
    }

    public void play() {
        while (lives > 0) { // Rule 17
            startLevel();
            while (!levelComplete() && !ball.fallenOffBoard()) {
                paddle.move();
                ball.move();
                checkCollisions();
            }
            if (ball.fallenOffBoard()) {
                lives--; // Rule 16
            } else {
                level++;
                ballSpeed++; // Rule 12
                addBricks();
            }
        }
    }

    public void checkCollisions() {
        if (ball.collidesWith(paddle)) {
            ball.bounce(); // Rule 11
        }
        for (Brick brick : bricks) {
            if (brick != null && ball.collidesWith(brick)) {
                brick.hit();
                ball.bounce(); // Rule 11
                if (brick.isDestroyed()) {
                    brick = null;
                }
            }
        }
    }

    public boolean levelComplete() {
        for (Brick brick : bricks) {
            if (brick != null) {
                return false;
            }
        }
        return true;
    }

    public void addBricks() { // Rule 13
        Random rand = new Random();
        for (int i = 0; i < 2; i++) { // Rule 14
            int x = rand.nextInt(180);
            int y = rand.nextInt(100);
            bricks[i] = new Brick(x, y, "red", 2); // Rule 7
        }
        int x = rand.nextInt(180);  // Rule 14
        int y = rand.nextInt(100);
        bricks[2] = new Brick(x, y, "gold", 3); // Rule 7
    }
}
