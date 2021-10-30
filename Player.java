import java.awt.*;

public class Player {
  // FIELDS
  private int x; // X-position
  private int y; // Y-position
  private int r; // Radius

  private int dx;
  private int dy;
  private int speed;

  private boolean left;
  private boolean right;
  private boolean up;
  private boolean down;

  private boolean firing;
  private long firingTimer;
  private long firingDelay;

  private boolean recovering;
  private long recoverTimer;

  private int lives;
  private Color colorWhite;
  private Color colorRed;

  private int score;
  private int powerLevel;
  private int power;
  private int[] requiredPower = { 1, 2, 3, 4, 5 };

  // CONSTRUCTOR
  public Player() {
    x = GamePanel.WIDTH / 2;
    y = GamePanel.HEIGHT / 2;
    r = 5;

    dx = 0;
    dy = 0;
    speed = 5;

    lives = 3;
    colorWhite = Color.WHITE;
    colorRed = Color.RED;

    firing = false;
    firingTimer = System.nanoTime();
    firingDelay = 200;

    recovering = false;
    recoverTimer = 0;

    score = 0;
  }

  // FUNCTIONS
  public int getx() {
    return x;
  }

  public int gety() {
    return y;
  }

  public int getr() {
    return r;
  }

  public int getScore() {
    return score;
  }

  public int getLives() {
    return lives;
  }

  public boolean isRecovering() {
    return recovering;
  }

  public void setLeft(boolean b) {
    left = b;
  }

  public void setRight(boolean b) {
    right = b;
  }

  public void setUp(boolean b) {
    up = b;
  }

  public void setDown(boolean b) {
    down = b;
  }

  public void setFiring(boolean b) {
    firing = b;
  }

  public void addScore(int sc) {
    score += sc;
  }

  public void gainLife() {
    lives++;
  }

  public void loseLife() {
    lives--;
    recovering = true;
    recoverTimer = System.nanoTime();
  }

  public void increasePower(int amount) {
    power += amount;
    if (power >= requiredPower[powerLevel]) {
      power -= requiredPower[powerLevel];
      powerLevel++;
    }
  }

  public int getPowerLevel() {
    return powerLevel;
  }

  public int getPower() {
    return power;
  }

  public int getRequiredPower() {
    return requiredPower[powerLevel];
  }

  public void update() {
    if (left)
      dx = -speed;
    if (right)
      dx = speed;
    if (up)
      dy = -speed;
    if (down)
      dy = speed;

    x += dx;
    y += dy;

    if (x < r)
      x = r;
    if (y < r)
      y = r;
    if (x > GamePanel.WIDTH)
      x = GamePanel.WIDTH - r;
    if (y > GamePanel.HEIGHT)
      y = GamePanel.HEIGHT - r;

    dx = 0;
    dy = 0;

    // Firing
    if (firing) {
      long elapsed = (System.nanoTime() - firingTimer) / 1000000;

      if (elapsed > firingDelay) {
        firingTimer = System.nanoTime();

        if (powerLevel < 2) {
          GamePanel.bullets.add(new Bullet(270, x, y));
        } else if (powerLevel < 4) {
          GamePanel.bullets.add(new Bullet(272, x + 5, y));
          GamePanel.bullets.add(new Bullet(268, x - 5, y));
        } else {
          GamePanel.bullets.add(new Bullet(270, x, y));
          GamePanel.bullets.add(new Bullet(273, x + 5, y));
          GamePanel.bullets.add(new Bullet(267, x - 5, y));
        }
      }
    }

    if (recovering) {
      long elapsed = (System.nanoTime() - recoverTimer) / 1000000;
      if (elapsed > 2000) {
        recovering = false;
        recoverTimer = 0;
      }
    }
  }

  public void draw(Graphics2D g) {
    if (recovering) {
      g.setColor(colorRed);
      g.fillOval(x - r, y - r, 2 * r, 2 * r);

      g.setStroke(new BasicStroke(3));
      g.setColor(colorRed.darker());
      g.drawOval(x - r, y - r, 2 * r, 2 * r);
      g.setStroke(new BasicStroke(1));
    } else {
      g.setColor(colorWhite);
      g.fillOval(x - r, y - r, 2 * r, 2 * r);

      g.setStroke(new BasicStroke(3));
      g.setColor(colorWhite.darker());
      g.drawOval(x - r, y - r, 2 * r, 2 * r);
      g.setStroke(new BasicStroke(1));
    }
  }
}
