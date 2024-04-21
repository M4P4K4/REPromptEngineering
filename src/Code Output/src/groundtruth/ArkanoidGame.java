package groundtruth;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ArkanoidGame {
    private final int BOARD_WIDTH = 200;
    private final int BOARD_HEIGHT = 300;
    private final int PADDLE_WIDTH = 20;
    private final int PADDLE_HEIGHT = 10;
    private final int BALL_DIAMETER = 10;
    private final int BALL_ANGLE = 45;
    private final int BRICK_WIDTH = 20;
    private final int BRICK_HEIGHT = 10;
    private final int START_LIVES = 3;
    private final int MAX_LEVEL = 33;
    private final int AMOUNT_BLUE_BRICKS = 50;

    private int currentLevel;
    private int currentLives;
    private Board board;
    private Paddle paddle;
    private Ball ball;
    private List<Brick> bricks = new ArrayList<Brick>();

    private enum BRICK_COLOR {
        BLUE,
        RED,
        GOLD
    }

    private class Board {
        private List<BoardElement> boardElements;
        private int width;
        private int height;

        public Board (int width, int height) {
            this.width = width;
            this.height = height;
        }

        public void setElement(BoardElement element) {
            boardElements.add(element);
        }

        public void removeElement(BoardElement element) {
            boardElements.remove(element);
        }
    }

    private class BoardElement {
        public Point position;
        private int width;
        private int height;

        public BoardElement(Point position, int width, int height) {
            this.position = position;
            this.width = width;
            this.height = height;

            board.setElement(this);
        }
    }

    private class Brick extends BoardElement {
        BRICK_COLOR color;
        Size size;
        int hitsRequired;

        public enum Size {
            SINGLE (1),
            DOUBLE (2);

            final int value;

            Size(int value) {
                this.value = value;
            }
        }

        public Brick(BRICK_COLOR color, Point position, Size size) {
            super(position, BRICK_WIDTH * size.value, BRICK_HEIGHT);

            this.color = color;
            this.size = size;

            if (color == BRICK_COLOR.RED)
                this.hitsRequired = 2;
            else if (color == BRICK_COLOR.GOLD)
                this.hitsRequired = 3;
            else
                this.hitsRequired = 1;
        }

        public boolean hit() {
            hitsRequired--;
            return isDestroyed();
        }

        public boolean isDestroyed() {
            boolean isDestroyed = false;
            if (hitsRequired <= 0) {
                isDestroyed = true;
                board.removeElement(this);
            }
            return isDestroyed;
        }
    }

    private class Paddle extends BoardElement {
        public Paddle() {
            super(new Point(BOARD_WIDTH / 2 - PADDLE_WIDTH/2, BOARD_HEIGHT-PADDLE_HEIGHT), PADDLE_WIDTH, PADDLE_HEIGHT);
        }

        public void moveRight() {
            // TODO assume this function works
        }

        public void moveLeft() {
            // TODO assume this function works
        }

        private void reset() {
            // TODO assume this function works
        }
    }

    private class Ball extends BoardElement {
        private Direction currentDirection;
        private int angle = BALL_ANGLE;

        private enum Direction {
            DOWN,
            UP
        }

        public Ball() {
            super(new Point(0,0), BALL_DIAMETER, BALL_DIAMETER);
            currentDirection = Direction.DOWN;
        }

        public void move() {
            position = calculateMovement();

            if (collidesWithElement()) {
                if (currentDirection == Direction.DOWN) {
                    currentDirection = Direction.UP;
                } else {
                    currentDirection = Direction.DOWN;
                }

                checkForBrickCollision();
            }
        }

        private boolean collidesWithElement() {
            // TODO assume this function works
            return false;
        }

        private Brick collidesWithBrick() {
            // TODO assume this function works
            return null;
        }

        private void reset() {
            // TODO assume this function works
        }

        private void checkForBrickCollision() {
            Brick collidedBrick = collidesWithBrick();
            if(collidedBrick != null) {
                collidedBrick.hit();
            }
        }
    }

    public ArkanoidGame() {
        board = new Board(BOARD_WIDTH, BOARD_HEIGHT);
        currentLevel = 1;
        currentLives = START_LIVES;
        ball = new Ball();
        paddle = new Paddle();

        while (currentLives > 0 && currentLevel < MAX_LEVEL) {
            startRound();
        }
        if (currentLives == 0) {
            System.out.println("Game Over");
        }
        else if (currentLevel  == MAX_LEVEL) {
            System.out.println("You Won!");
        }
    }

    private void startRound() {
        initializeBricks();

        ball.reset();
        paddle.reset();

        while (!levelComplete()) {
            play();
        }

        if (roundWon()) {
            currentLevel++;
        }
        else {
            currentLives--;
        }
    }

    private boolean roundWon() {
        // TODO assume this function works
        return false;
    }

    private void play() {
        // TODO assume this function works
    }

    private boolean levelComplete() {
        // TODO assume this function works
        return false;
    }

    private void initializeBricks() {
        int amountRedBricks = 2 * (currentLevel - 1);
        int amountGoldBricks = (currentLevel - 1);

        for (int i = 0; i < AMOUNT_BLUE_BRICKS; i++) {
            setBrick(BRICK_COLOR.BLUE);
        }
        for (int i = 0; i < amountRedBricks; i++) {
            setBrick(BRICK_COLOR.RED);
        }
        for (int i = 0; i < amountGoldBricks; i++) {
            setBrick(BRICK_COLOR.GOLD);
        }
    }

    private void setBrick(BRICK_COLOR color) {
        int x = (int) (Math.random() * BOARD_WIDTH);
        int y = (int) (Math.random() * BOARD_HEIGHT / 3);
        Brick.Size size = (Math.random() >= 0.1) ? Brick.Size.SINGLE : Brick.Size.DOUBLE;

        bricks.add(new Brick(color, new Point(x, y), size));
    }

    public static void main(String[] args) {
        new ArkanoidGame();
    }

    private Point calculateMovement() {
        // TODO assume this function works
        return null;
    }
}
