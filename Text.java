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
    g.setColor(Color.WHITE);
    g.drawString(text, (int) x, (int) y);
  }
}
