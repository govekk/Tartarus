/*************************************************************************
 *  Original code obtained from cs.princeton.edu, COS126.
 *  Modified by Sherri Goings for Data Structures (CS201) at Carleton College.
 *  Last modified Fall 2011.
 *
 *  Drawing library with many functions that provide a basic capability for
 *  creating drawings with your programs. It uses a simple graphics model that
 *  allows you to create drawings consisting of points, lines, and curves
 *  in a window on your computer and to save the drawings to a file.
 *************************************************************************/

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.util.LinkedList;


public final class Draw implements MouseListener, KeyListener {

    // pre-defined colors
    public static final Color BLACK      = Color.BLACK;
    public static final Color BLUE       = Color.BLUE;
    public static final Color CYAN       = Color.CYAN;
    public static final Color DARK_GRAY  = Color.DARK_GRAY;
    public static final Color GRAY       = Color.GRAY;
    public static final Color GREEN      = Color.GREEN;
    public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;
    public static final Color MAGENTA    = Color.MAGENTA;
    public static final Color ORANGE     = Color.ORANGE;
    public static final Color PINK       = Color.PINK;
    public static final Color RED        = Color.RED;
    public static final Color WHITE      = Color.WHITE;
    public static final Color YELLOW     = Color.YELLOW;

    // default colors
    private static final Color DEFAULT_PEN_COLOR   = BLACK;
    private static final Color DEFAULT_CLEAR_COLOR = WHITE;

    // current pen color
    private static Color penColor;

    // default canvas size is SIZE-by-SIZE
    private static final int DEFAULT_SIZE = 512;
    private static int width  = DEFAULT_SIZE;
    private static int height = DEFAULT_SIZE;

    // default pen radius
    private static final double DEFAULT_PEN_RADIUS = 0.002;

    // current pen radius
    private static double penRadius;

    // for synchronization
    private static Object mouseLock = new Object();
    private static Object keyLock = new Object();

    // default font
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 16);

    // current font
    private static Font font;

    // double buffered graphics
    private static BufferedImage offscreenImage, onscreenImage;
    private static Graphics2D offscreen, onscreen;

    // singleton for callbacks: avoids generation of extra .class files
    private static Draw std = new Draw();

  // the frame for drawing to the screen
    private static JFrame frame;

    // mouse state
    private static boolean mousePressed = false;
    private static boolean mouseClicked = false;
    private static double mouseX = 0;
    private static double mouseY = 0;

    // keyboard state
    private static LinkedList<Character> keysTyped = new LinkedList<Character>();

    // not instantiable
    private Draw() { }

    /**
     * Set the window size to w-by-h pixels.
     *
     * @param w the width as a number of pixels
     * @param h the height as a number of pixels
     * @throws a RunTimeException if the width or height is 0 or negative
     */
    public static void initGraphicsWindow(int w, int h, String title) {
        if (w < 1 || h < 1) throw new RuntimeException("width and height must be positive");
        width = w;
        height = h;

        frame = new JFrame();
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        onscreenImage  = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        offscreen = offscreenImage.createGraphics();
        onscreen  = onscreenImage.createGraphics();
        offscreen.setPaintMode();
        offscreen.setColor(DEFAULT_CLEAR_COLOR);
        offscreen.fillRect(0, 0, width, height);
        setPenColor(DEFAULT_PEN_COLOR);
        setPenRadius(DEFAULT_PEN_RADIUS);
        setFont(DEFAULT_FONT);
        clear();

        // add antialiasing
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        offscreen.addRenderingHints(hints);

        // frame stuff
        ImageIcon icon = new ImageIcon(onscreenImage);
        JLabel draw = new JLabel(icon);
        draw.addMouseListener(std);

        frame.setContentPane(draw);
        frame.addKeyListener(std);    // JLabel cannot get keyboard focus
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        frame.setTitle(title);
        frame.pack();
        frame.requestFocusInWindow();
        frame.setVisible(true);
    }

    public static int width() { return width; }
    public static int height() { return height; }
    public static void close() {
	//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	//frame.close();
    }

    /**
     * Clear the screen to the default color (white).
     */
    public static void clear() { clear(DEFAULT_CLEAR_COLOR); }

    /**
     * Clear the screen to the given color.
     * @param color the Color to make the background
     */
    public static void clear(Color color) {
        offscreen.setColor(color);
        offscreen.fillRect(0, 0, width, height);
        offscreen.setColor(penColor);
        draw();
    }

    /**
     * Get the current pen radius.
     */
    public static double getPenRadius() { return penRadius; }

    /**
     * Set the radius of the pen to the given size.
     * @param r the radius of the pen
     * @throws RuntimeException if r is negative
     */
    public static void setPenRadius(double r) {
        if (r < 0) throw new RuntimeException("pen radius must be positive");
        penRadius = r * DEFAULT_SIZE;
        BasicStroke stroke = new BasicStroke((float) penRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        // BasicStroke stroke = new BasicStroke((float) penRadius);
        offscreen.setStroke(stroke);
    }

    /**
     * Get the current pen color.
     */
    public static Color getPenColor() { return penColor; }

    /**
     * Set the pen color to the given color. The available pen colors are
     * BLACK, BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, MAGENTA,
     * ORANGE, PINK, RED, WHITE, and YELLOW.
     * @param color the Color to make the pen
     */
    public static void setPenColor(Color color) {
        penColor = color;
        offscreen.setColor(penColor);
    }

    /**
     * Get the current font.
     */
    public static Font getFont() { return font; }

    /**
     * Set the font to the given value.
     * @param f the font to make text
     */
    public static void setFont(Font f) { font = f; }


   /*************************************************************************
    *  Drawing images.
    *************************************************************************/
   public static void image(BufferedImage img, int destx1, int desty1, int destx2, int desty2) {
        offscreen.drawImage(img, destx1, desty1, destx2, desty2, 0, 0, img.getWidth(), img.getHeight(), null);
        draw();
   }

   public static void image(BufferedImage img, int destx1, int desty1, int destx2, int desty2, int srcx1, int srcy1, int srcx2, int srcy2) {
        offscreen.drawImage(img, destx1, desty1, destx2, desty2, srcx1, srcy1, srcx2, srcy2, null);
        draw();
   }

   /*************************************************************************
    *  Drawing geometric shapes.
    *************************************************************************/

    /**
     * Draw a line from (x0, y0) to (x1, y1).
     * @param x0 the x-coordinate of the starting point
     * @param y0 the y-coordinate of the starting point
     * @param x1 the x-coordinate of the destination point
     * @param y1 the y-coordinate of the destination point
     */
    public static void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Line2D.Double(x0, y0, x1, y1));
        draw();
    }

    /**
     * Draw one pixel at (x, y).
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     */
    private static void pixel(double x, double y) {
        offscreen.fillRect((int) Math.round(x), (int) Math.round(y), 1, 1);
    }

    /**
     * Draw a point at (x, y).
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     */
    public static void point(double x, double y) {
        double r = penRadius;
        if (r <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(x-r, y-r, 2*r, 2*r));
        draw();
    }

    /**
     * Draw a circle of radius r, centered on (x, y).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @throws RuntimeException if the radius of the circle is negative
     */
    public static void circle(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("circle radius can't be negative");
        if (r <= 1) pixel(x, y);
        else offscreen.draw(new Ellipse2D.Double(x - r, y - r, 2*r, 2*r));
        draw();
    }

    /**
     * Draw filled circle of radius r, centered on (x, y).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @throws RuntimeException if the radius of the circle is negative
     */
    public static void filledCircle(double x, double y, double r) {
       if (r < 0) throw new RuntimeException("circle radius can't be negative");
       if (r <= 1) pixel(x, y);
       else offscreen.fill(new Ellipse2D.Double(x - r, y - r, 2*r, 2*r));
       draw();
   }

    /**
     * Draw a square of side length 2r, centered on (x, y).
     * @param x the x-coordinate of the center of the square
     * @param y the y-coordinate of the center of the square
     * @param r radius is half the length of any side of the square
     * @throws RuntimeException if r is negative
     */
    public static void square(double x, double y, double r) {
        if (r < 0) throw new RuntimeException("square side length can't be negative");
        if (r <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(x - r, y - r, 2*r, 2*r));
        draw();
    }

    /**
     * Draw a filled square of side length 2r, centered on (x, y).
     * @param x the x-coordinate of the center of the square
     * @param y the y-coordinate of the center of the square
     * @param r radius is half the length of any side of the square
     * @throws RuntimeException if r is negative
     */
    public static void filledSquare(double x, double y, double r) {
      if (r < 0) throw new RuntimeException("square side length can't be negative");
      if (r <= 1) pixel(x, y);
      else offscreen.fill(new Rectangle2D.Double(x - r, y - r, 2*r, 2*r));
      draw();
  }


    /**
     * Draw a rectangle of given half width and half height, centered on (x, y).
     * @param x the x-coordinate of the center of the rectangle
     * @param y the y-coordinate of the center of the rectangle
     * @param halfWidth is half the width of the rectangle
     * @param halfHeight is half the height of the rectangle
     * @throws RuntimeException if halfWidth or halfHeight is negative
     */
    public static void rectangle(double x, double y, double halfWidth, double halfHeight) {
        if (halfWidth  < 0) throw new RuntimeException("half width can't be negative");
        if (halfHeight < 0) throw new RuntimeException("half height can't be negative");
        double ws = 2*halfWidth;
        double hs = 2*halfHeight;
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else offscreen.draw(new Rectangle2D.Double(x - ws/2, y - hs/2, ws, hs));
        draw();
    }

    /**
     * Draw a filled rectangle of given half width and half height, centered on (x, y).
     * @param x the x-coordinate of the center of the rectangle
     * @param y the y-coordinate of the center of the rectangle
     * @param halfWidth is half the width of the rectangle
     * @param halfHeight is half the height of the rectangle
     * @throws RuntimeException if halfWidth or halfHeight is negative
     */
    public static void filledRectangle(double x, double y, double halfWidth, double halfHeight) {
      if (halfWidth  < 0) throw new RuntimeException("half width can't be negative");
      if (halfHeight < 0) throw new RuntimeException("half height can't be negative");
      double ws = 2*halfWidth;
      double hs = 2*halfHeight;
      if (ws <= 1 && hs <= 1) pixel(x, y);
      else offscreen.fill(new Rectangle2D.Double(x - ws/2, y - hs/2, ws, hs));
      draw();
  }


    /**
     * Draw a polygon with the given (x[i], y[i]) coordinates.
     * @param x an array of all the x-coordindates of the polygon
     * @param y an array of all the y-coordindates of the polygon
     */
    public static void polygon(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) x[0], (float) y[0]);
        for (int i = 0; i < N; i++)
            path.lineTo((float) x[i], (float) y[i]);
        path.closePath();
        offscreen.draw(path);
        draw();
    }

    /**
     * Draw a filled polygon with the given (x[i], y[i]) coordinates.
     * @param x an array of all the x-coordindates of the polygon
     * @param y an array of all the y-coordindates of the polygon
     */
    public static void filledPolygon(double[] x, double[] y) {
        int N = x.length;
        GeneralPath path = new GeneralPath();
        path.moveTo((float) x[0], (float) y[0]);
        for (int i = 0; i < N; i++)
            path.lineTo((float) x[i], (float) y[i]);
        path.closePath();
        offscreen.fill(path);
        draw();
    }


   /*************************************************************************
    *  Drawing text.
    *************************************************************************/

    /**
     * Write the given text string in the current font, centered on (x, y).
     * @param x the center x-coordinate of the text
     * @param y the center y-coordinate of the text
     * @param s the text
     */
    public static void text(double x, double y, String s) {
        offscreen.setFont(font);
        FontMetrics metrics = offscreen.getFontMetrics();
        int ws = metrics.stringWidth(s);
        int hs = metrics.getDescent();
        offscreen.drawString(s, (float) (x - ws/2.0), (float) (y + hs));
        draw();
    }

    /**
     * Write the given text string in the current font, centered on (x, y) and
     * rotated by the specified number of degrees  
     * @param x the center x-coordinate of the text
     * @param y the center y-coordinate of the text
     * @param s the text
     * @param degrees is the number of degrees to rotate counterclockwise
     */
    public static void text(double x, double y, String s, double degrees) {
        offscreen.rotate(Math.toRadians(-degrees), x, y);
        text(x, y, s);
        offscreen.rotate(Math.toRadians(+degrees), x, y);
    }

    // draw onscreen 
    private static void draw() {
        onscreen.drawImage(offscreenImage, 0, 0, null);
        frame.repaint();
    }


   /*************************************************************************
    *  Mouse interactions.
    *************************************************************************/

    /* Is the mouse being pressed?
    @return true or false */
    public static boolean mousePressed() {
        synchronized (mouseLock) {
            return mousePressed;
        }
    }

   /* Was the mouse clicked?
      Reset to not clicked
      @return true or false */
      public static boolean mouseClicked() {
        synchronized (mouseLock) {
           boolean clicked = mouseClicked;
           mouseClicked = false;
           return clicked;
       }
   }

     /* What is the x-coordinate of the mouse?
     @return the value of the x-coordinate of the mouse */
     public static double mouseX() {
        synchronized (mouseLock) {
            return mouseX;
        }
    }
    
     /* What is the y-coordinate of the mouse?
     @return the value of the y-coordinate of the mouse */
     public static double mouseY() {
        synchronized (mouseLock) {
            return mouseY;
        }
    }

    //These methods cannot be called directly.
    public void mouseClicked(MouseEvent e) {
       synchronized (mouseLock) {
        mouseX = e.getX();
        mouseY = e.getY();
        mouseClicked = true;
    }
}
public void mouseEntered(MouseEvent e) { }
public void mouseExited(MouseEvent e) { }
public void mousePressed(MouseEvent e) {
    synchronized (mouseLock) {
        mouseX = e.getX();
        mouseY = e.getY();
        mousePressed = true;
    }
}
public void mouseReleased(MouseEvent e) {
    synchronized (mouseLock) {
        mousePressed = false;
    }
}


   /*************************************************************************
    *  Keyboard interactions.
    *************************************************************************/

    /**
     * Has the user typed a key?
     * @return true if the user has typed a key, false otherwise
     */
    public static boolean hasNextKeyTyped() {
        synchronized (keyLock) {
            return !keysTyped.isEmpty();
        }
    }

    /**
     * What is the next key that was typed by the user?
     * @return the next key typed
     */
    public static char nextKeyTyped() {
        synchronized (keyLock) {
            return keysTyped.removeLast();
        }
    }

    /**
     * This method cannot be called directly.
     */
    public void keyTyped(KeyEvent e) {
        synchronized (keyLock) {
            keysTyped.addFirst(e.getKeyChar());
        }
    }

    /**
     * This method cannot be called directly.
     */
    public void keyPressed(KeyEvent e) { }

    /**
     * This method cannot be called directly.
     */
    public void keyReleased(KeyEvent e) { }


    /**
     * Test client.
     */
    public static void main(String[] args) {
       
    }

}
