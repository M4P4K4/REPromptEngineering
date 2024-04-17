import pygame
import random

# Game constants
SCREEN_WIDTH = 200
SCREEN_HEIGHT = 300
PADDLE_WIDTH = 20
PADDLE_HEIGHT = 10
BALL_DIAMETER = 10
BRICK_WIDTH = 20
BRICK_HEIGHT = 10
BRICK_COLORS = [(0, 0, 255), (255, 0, 0), (255, 215, 0)]
LIVES = 3

# Initialize Pygame
pygame.init()

# Set up display
screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))

# Set up assets
paddle = pygame.Rect(SCREEN_WIDTH // 2, SCREEN_HEIGHT - PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT)
ball = pygame.Rect(0, 0, BALL_DIAMETER, BALL_DIAMETER)
bricks = []

# Set up game variables
lives = LIVES
level = 1
ball_speed = 5

def reset_level():
    global bricks, ball, paddle, ball_speed, level
    bricks = [pygame.Rect(random.randint(0, SCREEN_WIDTH - BRICK_WIDTH), random.randint(0, SCREEN_HEIGHT // 3 - BRICK_HEIGHT), BRICK_WIDTH, BRICK_HEIGHT) for _ in range(50 + level * 3)]
    ball.topleft = (0, 0)
    paddle.centerx = SCREEN_WIDTH // 2
    ball_speed = 5 + level

def draw():
    screen.fill((0, 0, 0))
    pygame.draw.rect(screen, (255, 255, 255), paddle)
    pygame.draw.circle(screen, (255, 255, 255), ball.center, BALL_DIAMETER // 2)
    for brick in bricks:
        pygame.draw.rect(screen, BRICK_COLORS[brick.height // BRICK_HEIGHT - 1], brick)
    pygame.display.flip()

def update():
    global lives, level
    keys = pygame.key.get_pressed()
    if keys[pygame.K_LEFT]:
        paddle.move_ip(-5, 0)
    if keys[pygame.K_RIGHT]:
        paddle.move_ip(5, 0)
    paddle.clamp_ip(screen.get_rect())
    ball.move_ip(ball_speed, ball_speed)
    if ball.colliderect(paddle) or ball.left < 0 or ball.right > SCREEN_WIDTH:
        ball_speed = -ball_speed
    elif ball.top < 0:
        ball_speed = abs(ball_speed)
    elif ball.bottom > SCREEN_HEIGHT:
        lives -= 1
        if lives > 0:
            reset_level()
        else:
            level = 1
            lives = LIVES
            reset_level()
    for brick in bricks:
        if ball.colliderect(brick):
            bricks.remove(brick)
            ball_speed = -ball_speed
            break
    if not bricks:
        level += 1
        reset_level()

reset_level()

# Game loop
while True:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            pygame.quit()
            sys.exit()
    draw()
    update()
    pygame.time.delay(1000 // 60)
