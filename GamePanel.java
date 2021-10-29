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

    player = new Player();
    bullets = new ArrayList<Bullet>();
    enemies = new ArrayList<Enemy>();

    for (int i = 0; i < 5; i++) {
      enemies.add(new Enemy(1, 1));
    }

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
  }

  private void gameUpdate() {
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

    // Check dead enemies
    for (int en = 0; en < enemies.size(); en++) {
      if (enemies.get(en).isDead()) {
        enemies.remove(en);
        en--;
      }
    }
  }

  private void gameRender() {
    g.setColor(new Color(0, 100, 255));
    g.fillRect(0, 0, WIDTH, HEIGHT);
    g.setColor(Color.BLACK);
    g.drawString("FPS: " + averageFPS, 10, 10);
    g.drawString("Num bullets: " + bullets.size(), 10, 20);

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
  }

  private void gameDraw() {
    Graphics g2 = this.getGraphics();
    g2.drawImage(image, 0, 0, null);
    g2.dispose();
  }

  @Override
  public void keyTyped(KeyEvent key) {
    // TODO Auto-generated method stub

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