// gpjpp (genetic programming package for Java)
// Copyright (c) 1997, Kim Kokkonen
//
// This program is free software; you can redistribute it and/or 
// modify it under the terms of version 2 of the GNU General Public 
// License as published by the Free Software Foundation.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// Send comments, suggestions, problems to kimk@turbopower.com

package gpjpp;

import java.io.*;
import java.awt.*; //Frame, Canvas, Graphics, Image, Font, FontMetrics
import Acme.JPM.Encoders.*;

/**
 * Used to draw graphic images of the results of the genetic
 * algorithm. Most commonly, it is used to draw a graphic rendition
 * of genetic program trees. It can also be used to draw other
 * images such as the path followed by a mower in the lawnmower
 * problem.
 *
 * Because the rest of gpjpp is designed without any user interface,
 * this class attempts to shoehorn graphics into the library with
 * minimal side effects, which turned out to be surprisingly 
 * difficult. The technique chosen was to create a subclass
 * of Java's standard AWT Frame class, to add a single Canvas to
 * this frame, and to create an off-screen Image where the drawing 
 * is performed relative to the Canvas. Then the freeware class
 * Acme.JPM.Encoders.GifEncoder is used to write the graphic image to
 * a gif file for viewing in a web browser, drawing program, or
 * word processor.<p>
 *
 * In my experience using Java console applications running under 
 * the Microsoft J++ 1.1 VM in Windows 95, creating a GPDrawing
 * object causes the console window to temporarily lose focus until
 * the GPDrawing.dispose() method (inherited from Frame) is called.
 * See <a href="gpjpp.GPRun.html#showFinalGeneration">
 * GPRun.showFinalGeneration</a> for an example. Although the effect 
 * can be disconcerting, a mouse click returns focus to any
 * window you desire without disturbing the drawing process. 
 * Numerous experiments have not uncovered a solution. The same effect 
 * does <em>not</em> occur under the Sun VM.<p>
 *
 * The <a href="gpjpp.GPGenePrint.html#_top_">GPGenePrint</a> and 
 * LawnGP classes show examples of drawing images with GPDrawing. 
 * If you just want to create gif files of the genetic trees, 
 * set <a href="gpjpp.GPVariables.html#PrintTree">PrintTree</a> 
 * to true in your configuration file and don't worry about the rest.
 *
 * @version 1.0
 */
public class GPDrawing extends Frame {
    /**
     * The typeface used to display text in genetic trees. This
     * should be a monospace font. Courier was chosen as a default
     * because it is a standard Java font that should be available 
     * on all platforms. There's nothing that limits you to just one
     * font for other applications, but GPDrawing enables this 
     * one and gets various font metrics that are useful 
     * while drawing.
     */
    public static String fontName = "Courier";

    /**
     * The default font size used for the font. This can be changed
     * while constructing the object or by calling 
     * <a href="gpjpp.GPDrawing.html#setFontSize">setFontSize</a>.
     * Default value 12.
     */
    public static int defFontSize = 12;

    /**
     * The canvas on which the image is drawn.
     */
    protected Canvas cvs;

    /**
     * The active font for the image.
     */
    protected Font fnt;

    /**
     * Font metrics for the active font.
     */
    protected FontMetrics fm;

    /**
     * An off-screen image associated with the canvas on which
     * the drawing is created.
     */
    protected Image img;

    /**
     * A graphics context whose methods are called to produce the 
     * off-screen image.
     *
     * @see gpjpp.GPDrawing#getGra
     */
    protected Graphics gra;

    /**
     * The current font size.
     */
    protected int fontSize;

    /**
     * The width of the widest character in the current font.
     * It's public so that users of the class can get the value
     * efficiently.
     */
    public int cw;

    /**
     * The height of the current font.
     * It's public so that users of the class can get the value
     * efficiently.
     */
    public int ch;

    /**
     * The maximum ascent of any character in the current font.
     * It's public so that users of the class can get the value
     * efficiently.
     */
    public int as;

    /**
     * Public null constructor for this class. It creates
     * a Frame with the title "gif drawing", adds a Canvas
     * to it, creates the peer components, creates the
     * default font at default size, and gets the 
     * <a href="gpjpp.GPDrawing.html#cw">cw</a>, 
     * <a href="gpjpp.GPDrawing.html#ch">ch</a>, and 
     * <a href="gpjpp.GPDrawing.html#as">as</a>
     * font metrics. The frame remains hidden and
     * disabled.
     */
    public GPDrawing() {
        super("gif drawing");

        //create a canvas to draw on
        cvs = new Canvas();

        //add canvas to frame
        add(cvs);

        //create peer components
        addNotify();

        //create a default font
        setFontSize(defFontSize);
    }

    /**
     * Changes the font size. The typeface is specified by the
     * <a href="gpjpp.GPDrawing.html#fontName">fontName</a>
     * field of the class. Updates the 
     * <a href="gpjpp.GPDrawing.html#fm">fm</a>,
     * <a href="gpjpp.GPDrawing.html#cw">cw</a>, 
     * <a href="gpjpp.GPDrawing.html#ch">ch</a>, and 
     * <a href="gpjpp.GPDrawing.html#as">as</a>
     * fields of the class.
     */
    public void setFontSize(int fontSize) {
        if (fontSize != this.fontSize) {
            this.fontSize = fontSize;
            fnt = new Font(fontName, Font.PLAIN, fontSize);
            if (fnt == null)
                throw new RuntimeException("Courier font not available");

            //get font metrics needed by gif tree
            fm = getFontMetrics(fnt);
            cw = fm.getMaxAdvance();
            ch = fm.getHeight();
            as = fm.getMaxAscent();
        }
    }

    /**
     * Prepares an off-screen drawing image of the specified
     * pixel width and height. The image is cleared to a white
     * background, the default font is enabled, and the foreground
     * color is set to black.
     */
    public void prepImage(int w, int h) {
        prepImage(w, h, this.fontSize);
    }

    /**
     * Sets a new font size and prepares an off-screen drawing 
     * image.
     *
     * @see gpjpp.GPDrawing#prepImage(int, int)
     */
    public void prepImage(int w, int h, int fontSize) {
        //create properly sized font if needed
        setFontSize(fontSize);

        //create image of desired size
        img = cvs.createImage(w, h);
        if (img == null)
            throw new RuntimeException("Unable to create image");
        if (gra != null)
            gra.dispose();
        gra = img.getGraphics();

        //clear the image and set default colors and font
        gra.setColor(Color.white);
        gra.fillRect(0, 0, w, h);
        gra.setColor(Color.black);
        gra.setFont(fnt);
    }

    /**
     * Returns the pixel width of a text string when displayed
     * in the default font.
     */
    public int stringWidth(String s) { return fm.stringWidth(s); }

    /**
     * Returns the graphics context on which drawing is done.
     * See java.awt.Graphics for a complete list of available
     * drawing methods. 
     * <a href="gpjpp.GPDrawing.html#prepImage(int, int)">prepImage()</a>
     * must be called first.
     */
    public Graphics getGra() { return gra; }

    /**
     * Writes the current off-screen image to a gif file.
     *
     * @param fname  the name of the gif file to create.
     * @exception IOException
     *              if an error occurs while writing the file.
     */
    public void writeGif(String fname) throws IOException {

        OutputStream os = new BufferedOutputStream(
            new FileOutputStream(fname), 1024);
        (new GifEncoder(img, os)).encode();
        os.close();
    }
}
