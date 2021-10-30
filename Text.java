import java.awt.*;

public class Text {

  // FIELDS
  private double x;
  private double y;
  private long time;
  private String text;
  private long start;

  // CONSTRUCTOR
  public Text(double x, double y, long time, String text) {
    this.x = x;
    this.y = y;
    this.time = time;
    this.text = text;
    start = System.nanoTime();
  }

  public boolean update() {
    long elapsed = (System.nanoTime() - start) / 1000000;

    return elapsed > time;
  }

  public void draw(Graphics2D g) {
    g.setFont(new Font("Century Gothic", Font.PLAIN, 12));

    long elapsed = (System.nanoTime() - start) / 1000000;
    int alpha = (int) (255 * Math.sin(3.14 * elapsed / time));

    if (alpha > 255)
      alpha = 255 - 10;
    g.setColor(new Color(255, 255, 255, alpha));
    int length = (int) g.getFontMetrics().getStringBounds(text, g).getWidth();
    g.drawString(text, (int) (x - (length / 2)), (int) y);
  }
}
