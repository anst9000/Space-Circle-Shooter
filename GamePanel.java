import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable, KeyListener {
  // FIELDS
  public static int WIDTH = 400;
  public static int HEIGHT = 400;

  private Thread thread;
  private boolean running;

  private BufferedImage image;
  private Graphics2D g;

  private int FPS = 30;
  private double averageFPS;

  public static Player player;
  public static ArrayList<Bullet> bullets;
  public static ArrayList<Enemy> enemies;
  public static ArrayList<PowerUp> powerUps;
  public static ArrayList<Explosion> explosions;
  public static ArrayList<Text> texts;

  private long waveStartTimer;
  private long waveStartTimerDiff;
  private long waveNumber;
  private boolean waveStart;
  private int waveDelay = 2000;

  private long slowDownTimer;
  private long slowDownTimerDiff;
  private int slowDownLength = 6000;

  // Constructor
  public GamePanel() {
    super();
    setPreferredSize(new Dimension(WIDTH, HEIGHT));
    setFocusable(true);
    requestFocus();
  }

  // FUNCTIONS
  public void addNotify() {
    super.addNotify();

    if (thread == null) {
      thread = new Thread(this);
      thread.start();
    }

    addKeyListener(this);
  }

  public void run() {
    running = true;
    image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    g = (Graphics2D) image.getGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    player = new Player();
    bullets = new ArrayList<Bullet>();
    enemies = new ArrayList<Enemy>();
    powerUps = new ArrayList<PowerUp>();
    explosions = new ArrayList<Explosion>();
    texts = new ArrayList<Text>();

    waveStartTimer = 0;
    waveStartTimerDiff = 0;
    waveStart = true;
    waveNumber = 0;

    long startTime;
    long URDTimeMillis;
    long waitTime;
    long totalTime = 0;

    int frameCount = 0;
    int maxFrameCount = 30;

    long targetTime = 1000 / FPS;

    // GAME LOOP
    while (running) {
      startTime = System.nanoTime();

      gameUpdate();
      gameRender();
      gameDraw();

      URDTimeMillis = (System.nanoTime() - startTime) / 1000000;
      waitTime = targetTime - URDTimeMillis;

      try {
        Thread.sleep(waitTime);
      } catch (Exception e) {
      }

      totalTime += System.nanoTime() - startTime;
      frameCount++;

      if (frameCount == maxFrameCount) {
        averageFPS = 1000.0 / ((totalTime / frameCount) / 1000000);
        frameCount = 0;
        totalTime = 0;
      }
    }

    g.setColor(new Color(0, 100, 255));
    g.fillRect(0, 0, WIDTH, HEIGHT);
    g.setColor(Color.WHITE);
    g.setFont(new Font("Century Gothic", Font.PLAIN, 16));

    String text = "G A M E   O V E R";
    int length = (int) g.getFontMetrics().getStringBounds(text, g).getWidth();
    g.drawString(text, (WIDTH - length) / 2, HEIGHT / 2);

    text = "Score " + player.getScore();
    length = (int) g.getFontMetrics().getStringBounds(text, g).getWidth();
    g.drawString(text, (WIDTH - length) / 2, HEIGHT / 2 + 30);
    gameDraw();
  }

  private void gameUpdate() {
    // New wave
    if (waveStartTimer == 0 && enemies.size() == 0) {
      waveNumber++;
      waveStart = false;
      waveStartTimer = System.nanoTime();
    } else {
      waveStartTimerDiff = (System.nanoTime() - waveStartTimer) / 1000000;
      if (waveStartTimerDiff > waveDelay) {
        waveStart = true;
        waveStartTimer = 0;
        waveStartTimerDiff = 0;
      }
    }

    // Create enemies
    if (waveStart && enemies.size() == 0) {
      creaetNewEnemies();
    }

    // Player update
    player.update();

    // Bullets update
    for (int i = 0; i < bullets.size(); i++) {
      boolean remove = bullets.get(i).update();

      if (remove) {
        bullets.remove(i);
        i--;
      }
    }

    // Enemies update
    for (int i = 0; i < enemies.size(); i++) {
      enemies.get(i).update();
    }

    // Power Up update
    for (int pu = 0; pu < powerUps.size(); pu++) {
      boolean remove = powerUps.get(pu).update();
      if (remove) {
        powerUps.remove(pu);
        pu--;
      }
    }

    // Explosion update
    for (int i = 0; i < explosions.size(); i++) {
      boolean remove = explosions.get(i).update();
      if (remove) {
        explosions.remove(i);
        i--;
      }
    }

    // Text update
    for (int i = 0; i < texts.size(); i++) {
      boolean remove = texts.get(i).update();
      if (remove) {
        texts.remove(i);
        i--;
      }
    }

    // Bullet-Enemy collision
    checkBulletEnemyCollision();

    // Check dead enemies
    checkDeadEnemies();

    // Check dead player
    if (player.isDead()) {
      running = false;
    }

    // Check Player-Enemy collision
    checkPlayerEnemyCollision();

    // Check Player-PowerUp collision
    checkPlayerPowerUpCollision();
  }

  private void checkPlayerPowerUpCollision() {
    int px = player.getx();
    int py = player.gety();
    int pr = player.getr();

    for (int pu = 0; pu < powerUps.size(); pu++) {
      PowerUp powerUp = powerUps.get(pu);
      double x = powerUp.getx();
      double y = powerUp.gety();
      double r = powerUp.getr();

      double dx = px - x;
      double dy = py - y;
      double dist = Math.sqrt(dx * dx + dy * dy);

      // Collected Power Up
      if (dist < pr + r) {
        int type = powerUp.getType();
        if (type == 1) {
          player.gainLife();
          texts.add(new Text(player.getx(), player.gety(), 2000, "Extra Life"));
        }
        if (type == 2) {
          player.increasePower(1);
          texts.add(new Text(player.getx(), player.gety(), 2000, "Power"));
        }
        if (type == 3) {
          player.increasePower(2);
          texts.add(new Text(player.getx(), player.gety(), 2000, "Double Power"));
        }
        if (type == 4) {
          slowDownTimer = System.nanoTime();
          for (int j = 0; j < enemies.size(); j++) {
            enemies.get(j).setSlow(true);
          }
          texts.add(new Text(player.getx(), player.gety(), 2000, "Slow Down"));
        }

        powerUps.remove(pu);
        pu--;
      }
    }

    // Slowdown update
    if (slowDownTimer != 0) {
      slowDownTimerDiff = (System.nanoTime() - slowDownTimer) / 1000000;
      if (slowDownTimerDiff > slowDownLength) {
        slowDownTimer = 0;
        for (int j = 0; j < enemies.size(); j++) {
          enemies.get(j).setSlow(false);
        }
      }
    }
  }

  private void checkPlayerEnemyCollision() {
    if (!player.isRecovering()) {
      int px = player.getx();
      int py = player.gety();
      int pr = player.getr();

      for (int i = 0; i < enemies.size(); i++) {
        Enemy enemy = enemies.get(i);
        double ex = enemy.getx();
        double ey = enemy.gety();
        double er = enemy.getr();

        double dx = px - ex;
        double dy = py - ey;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < pr + er) {
          player.loseLife();
        }
      }
    }
  }

  private void checkBulletEnemyCollision() {
    // Bullet-Enemy collision
    for (int bu = 0; bu < bullets.size(); bu++) {
      Bullet bullet = bullets.get(bu);
      double bx = bullet.getx();
      double by = bullet.gety();
      double br = bullet.getr();

      for (int en = 0; en < enemies.size(); en++) {
        Enemy enemy = enemies.get(en);
        double ex = enemy.getx();
        double ey = enemy.gety();
        double er = enemy.getr();

        double dx = bx - ex;
        double dy = by - ey;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < br + er) {
          enemy.hit();
          bullets.remove(bu);
          bu--;
          break;
        }
      }
    }
  }

  private void checkDeadEnemies() {
    for (int en = 0; en < enemies.size(); en++) {
      if (enemies.get(en).isDead()) {
        Enemy enemy = enemies.get(en);

        // Chance for power up
        double rand = Math.random();
        if (rand < 0.005) {
          powerUps.add(new PowerUp(1, enemy.getx(), enemy.gety()));
        } else if (rand < 0.025) {
          powerUps.add(new PowerUp(4, enemy.getx(), enemy.gety()));
        } else if (rand < 0.075) {
          powerUps.add(new PowerUp(3, enemy.getx(), enemy.gety()));
        } else if (rand < 0.125) {
          powerUps.add(new PowerUp(2, enemy.getx(), enemy.gety()));
        }

        player.addScore(enemy.getType() + enemy.getRank());
        enemies.remove(en);
        en--;

        enemy.explode();
        explosions.add(new Explosion(enemy.getx(), enemy.gety(), enemy.getr(), enemy.getr() + 15));
      }
    }
  }

  private void gameRender() {
    // Background draw
    g.setColor(new Color(0, 100, 255));
    g.fillRect(0, 0, WIDTH, HEIGHT);

    // Draw slowdown screen
    if (slowDownTimer != 0) {
      g.setColor(new Color(255, 255, 255, 64));
      g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    // Player draw
    player.draw(g);

    // Bullets draw
    for (int i = 0; i < bullets.size(); i++) {
      bullets.get(i).draw(g);
    }

    // Enemies draw
    for (int i = 0; i < enemies.size(); i++) {
      enemies.get(i).draw(g);
    }

    // Power Ups draw
    for (int i = 0; i < powerUps.size(); i++) {
      powerUps.get(i).draw(g);
    }

    // Draw explosions
    for (int i = 0; i < explosions.size(); i++) {
      explosions.get(i).draw(g);
    }

    // Draw texts
    for (int i = 0; i < texts.size(); i++) {
      texts.get(i).draw(g);
    }

    // Draw wave number
    if (waveStartTimer != 0) {
      g.setFont(new Font("Century Gothic", Font.PLAIN, 18));
      String screenText = "- W A V E   " + waveNumber + "   -";
      int length = (int) g.getFontMetrics().getStringBounds(screenText, g).getWidth();
      int alpha = (int) (255 * Math.sin(3.14 * waveStartTimerDiff / waveDelay));
      if (alpha > 255)
        alpha = 255;

      g.setColor(new Color(255, 255, 255, alpha));
      g.drawString(screenText, WIDTH / 2 - length / 2, HEIGHT / 2);
    }

    // Draw player lives
    for (int life = 0; life < player.getLives(); life++) {
      g.setColor(Color.WHITE);
      g.fillOval(20 + (20 * life), 20, player.getr() * 2, player.getr() * 2);
      g.setStroke(new BasicStroke(3));
      g.setColor(Color.WHITE.darker());
      g.drawOval(20 + (20 * life), 20, player.getr() * 2, player.getr() * 2);
      g.setStroke(new BasicStroke(1));
    }

    // Draw player power
    g.setColor(new Color(225, 225, 0));
    g.fillRect(20, 40, player.getPower() * 8, 8);
    g.setColor(new Color(225, 225, 0).darker());
    g.setStroke(new BasicStroke(2));

    for (int i = 0; i < player.getRequiredPower(); i++) {
      g.drawRect(20 + 8 * i, 40, 8, 8);
    }
    g.setStroke(new BasicStroke(1));

    // Draw player score
    g.setColor(Color.WHITE);
    g.setFont(new Font("Century Gothic", Font.PLAIN, 14));
    g.drawString("Score: " + player.getScore(), WIDTH - 100, 30);

    // Draw slowdown meter
    if (slowDownTimer != 0) {
      g.setColor(Color.WHITE);
      g.drawRect(20, 60, 100, 8);
      g.fillRect(20, 60, (int) (100 - 100.0 * slowDownTimerDiff / slowDownLength), 8);
    }
  }

  private void gameDraw() {
    Graphics g2 = this.getGraphics();
    g2.drawImage(image, 0, 0, null);
    g2.dispose();
  }

  private void creaetNewEnemies() {
    enemies.clear();

    if (waveNumber == 1) {
      for (int i = 1; i <= 3; i++) {
        for (int j = 1; j <= 2; j++) {
          enemies.add(new Enemy(i, 1));
        }
      }
    }

    if (waveNumber == 2) {
      for (int i = 1; i <= 3; i++) {
        for (int j = 1; j <= 2; j++) {
          enemies.add(new Enemy(i, 1));

          if (i % 2 == 0) {
            enemies.add(new Enemy(1, 2));
          }
        }
      }
    }

    if (waveNumber == 3) {
      for (int i = 1; i <= 3; i++) {
        enemies.add(new Enemy(i, 2));

        for (int j = 1; j <= 2; j++) {
          enemies.add(new Enemy(i, 1));
        }

        if (i % 2 == 0) {
          enemies.add(new Enemy(1, 2));
        }
        if (i % 1 == 0) {
          enemies.add(new Enemy(1, 3));
        }
      }
    }

    if (waveNumber == 4) {
      for (int i = 1; i <= 3; i++) {
        enemies.add(new Enemy(i, 1));
        enemies.add(new Enemy(i, 2));
        enemies.add(new Enemy(i, 3));
      }
    }

    if (waveNumber == 5) {
      for (int i = 1; i <= 3; i++) {
        enemies.add(new Enemy(i, 1));
        enemies.add(new Enemy(i, 2));
        enemies.add(new Enemy(i, 3));

        if (i % 2 == 0) {
          enemies.add(new Enemy(1, 3));
        }
      }
    }

    if (waveNumber == 6) {
      for (int i = 1; i <= 3; i++) {
        enemies.add(new Enemy(i, 1));
        enemies.add(new Enemy(i, 2));
        enemies.add(new Enemy(i, 3));

        if (i % 1 == 0) {
          enemies.add(new Enemy(1, 3));
        }
        if (i % 2 == 0) {
          enemies.add(new Enemy(1, 4));
        }
      }
    }

    if (waveNumber == 7) {
      for (int i = 1; i <= 3; i++) {
        enemies.add(new Enemy(i, 1));
        enemies.add(new Enemy(i, 2));
        enemies.add(new Enemy(i, 3));
        enemies.add(new Enemy(i, 4));
      }
    }

    if (waveNumber == 8) {
      for (int i = 1; i <= 3; i++) {
        enemies.add(new Enemy(i, 1));
        enemies.add(new Enemy(i, 2));
        enemies.add(new Enemy(i, 3));
        enemies.add(new Enemy(i, 4));

        if (i % 2 == 0) {
          enemies.add(new Enemy(1, 4));
        }
      }
    }

    if (waveNumber == 9) {
      for (int i = 1; i <= 3; i++) {
        enemies.add(new Enemy(i, 1));
        enemies.add(new Enemy(i, 2));
        enemies.add(new Enemy(i, 3));
        enemies.add(new Enemy(i, 4));

        if (i % 2 == 0) {
          enemies.add(new Enemy(1, 4));
        }

        if (i % 1 == 0) {
          enemies.add(new Enemy(2, 4));
        }
      }
    }

    if (waveNumber == 10) {
      for (int i = 1; i <= 3; i++) {
        for (int j = 1; j <= 2; j++) {
          enemies.add(new Enemy(i, 1));
          enemies.add(new Enemy(i, 2));
          enemies.add(new Enemy(i, 3));
          enemies.add(new Enemy(i, 4));

          if (i % 2 == 0) {
            enemies.add(new Enemy(1, 4));
            enemies.add(new Enemy(2, 4));
            enemies.add(new Enemy(3, 4));
          }
        }
      }
    }

    if (waveNumber > 10) {
      running = false;
    }
  }

  @Override
  public void keyTyped(KeyEvent key) {

  }

  @Override
  public void keyPressed(KeyEvent key) {
    int keyCode = key.getKeyCode();
    if (keyCode == KeyEvent.VK_LEFT) {
      player.setLeft(true);
    }
    if (keyCode == KeyEvent.VK_RIGHT) {
      player.setRight(true);
    }
    if (keyCode == KeyEvent.VK_UP) {
      player.setUp(true);
    }
    if (keyCode == KeyEvent.VK_DOWN) {
      player.setDown(true);
    }
    if (keyCode == KeyEvent.VK_SPACE) {
      player.setFiring(true);
    }
  }

  @Override
  public void keyReleased(KeyEvent key) {
    int keyCode = key.getKeyCode();

    if (keyCode == KeyEvent.VK_LEFT) {
      player.setLeft(false);
    }
    if (keyCode == KeyEvent.VK_RIGHT) {
      player.setRight(false);
    }
    if (keyCode == KeyEvent.VK_UP) {
      player.setUp(false);
    }
    if (keyCode == KeyEvent.VK_DOWN) {
      player.setDown(false);
    }
    if (keyCode == KeyEvent.VK_SPACE) {
      player.setFiring(false);
    }
  }
}