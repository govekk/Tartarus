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
import java.awt.Graphics;

/**
 * Formats a gene tree for printing in graphic or pseudo-graphic
 * format. GPGenePrint extends GPGene to add an x position which
 * it computes by spreading out the nodes of the tree just enough
 * to prevent them from overlapping at all levels of the tree.
 * Note that GPGenePrint formats just a single branch of a
 * complete GP, which would frequently be too wide to print
 * on standard paper.<p>
 *
 * The three key methods of the class are the constructor, 
 * <a href="gpjpp.GPGenePrint.html#GPGenePrint">GPGenePrint(GPGene)</a>, 
 * which clones a GPGene tree onto a new tree with the extra fields, 
 * <a href="gpjpp.GPGenePrint.html#printOn">printOn()</a>, 
 * which prints the tree in pseudo-graphic format to a PrintStream, and 
 * <a href="gpjpp.GPGenePrint.html#drawOn">drawOn()</a>,
 * which prints the tree as a true graphic gif file.<p>
 *
 * Several static public fields of the class can be used to
 * adjust the appearance of the tree:
 * <a href="gpjpp.GPGenePrint#xMargin">xMargin</a>,
 * <a href="gpjpp.GPGenePrint#xSpacing">xSpacing</a>,
 * <a href="gpjpp.GPGenePrint#rectMargin">rectMargin</a>,
 * <a href="gpjpp.GPGenePrint#arcRadius">arcRadius</a>,
 * <a href="gpjpp.GPGenePrint#rowsPerNode">rowsPerNode</a>.
 *
 * @version 1.0
 */
public class GPGenePrint extends GPGene {
    /**
     * The number of blank columns printed at the left edge of the
     * tree by printOn(). Default 2.
     */
    public static double xMargin = 2.0;

    /**
     * The minimum number of columns (character widths) that separate
     * adjacent nodes. Default 3.
     */
    public static double xSpacing = 3.0;

    /**
     * The number of pixels separating the node rectangle from the
     * node text that it surrounds in a graphic tree. Default 2.
     */
    public static int rectMargin = 2;

    /**
     * The pixel radius of the arc at each corner of a node rectangle.
     * Default 10.
     */
    public static int arcRadius = 10;

    /**
     * The number of character heights per node level in a graphic
     * tree. Default 3.
     */
    public static int rowsPerNode = 3;

    /**
     * The computed x position of this node, in character units. The
     * number is non-negative when the algorithm is finished.
     * It is a floating point number that is rounded to integer
     * columns by printOn() and converted to integer pixels by
     * drawOn().
     */
    protected double x;

    /**
     * A reference to the original gene of which the GPGenePrint
     * object is an extended copy. The purpose for peer is to
     * call methods in a user subclass of GPGene. Specifically,
     * GPGenePrint calls the 
     * <a href="gpjpp.GPGene.html#geneRep">geneRep()</a> method of 
     * GPGene to get the string representation of each node. 
     * When the gene represents a randomly generated constant, 
     * geneRep() can return the constant value instead of the generic 
     * representation of the node type. 
     * See the Lawnmower problem for an example.
     */
    protected GPGene peer;

    /**
     * This constructor clones the specified gene and all its
     * children, creating a parallel tree composed of GPGenePrint
     * nodes. Each GPGenePrint node contains an x coordinate,
     * initially 0.0, and a reference to the original gene.
     */
    GPGenePrint(GPGene gpo) {
        //x = 0.0;
        peer = gpo;
        node = gpo.node;

        reserveSpace(gpo.containerSize());

        //make a copy of all container objects of gpo
        for (int i = 0; i < containerSize(); i++)
            if (gpo.container[i] == null)
                put(i, null);
            else
                put(i, new GPGenePrint((GPGene)gpo.container[i]));
    }

    /**
     * Disables the cloning operation inherited from GPGene,
     * since GPGenePrint does not need to be cloned. This
     * version of clone() always returns null.
     */
    protected synchronized Object clone() { return null; }

    /**
     * Returns a code identifying the class. GPGenePrint is
     * not stored in streams, but it is given the unique code
     * GENEPRINTID just for completeness.
     */
    public byte isA() { return GENEPRINTID; }

    /**
     * Disables the load operation inherited from GPGene, since
     * GPGenePrint does not need to be streamed. This version
     * of load() does nothing.
     */
    protected synchronized void load(DataInputStream is) { }

    /**
     * Disables the save operation inherited from GPGene, since
     * GPGenePrint does not need to be streamed. This version
     * of save() does nothing.
     */
    protected void save(DataOutputStream os) { }

    /**
     * Returns the length of the node representation string in
     * characters.
     *
     * @see gpjpp.GPGenePrint#peer
     */
    protected double getNodeWidth() { return peer.geneRep().length(); }

    /**
     * Returns the x coordinate of the right edge of the node
     * text in characters.
     */
    protected double getNodeRight() { return x+getNodeWidth()-1.0; }

    /**
     * Returns the minimum width of the arguments to this node
     * taking into account the node width of each argument and the
     * minimum spacing between nodes, 
     * <a href="gpjpp.GPGenePrint.html#xSpacing">xSpacing</a>.
     */
    protected double calcPackedContainerWidth() {
        double width = 0.0;
        for (int i = 0; i < containerSize(); i++)
            width += ((GPGenePrint)get(i)).getNodeWidth()+xSpacing;
        //don't include trailing space
        return width-xSpacing;
    }

    /**
     * Returns the x coordinate of the horizontal center of the
     * arguments to this node at their current positions.
     */
    protected double calcContainerCenter() {
        if (containerSize() == 0)
            //return node's own center
            return (x+getNodeRight())/2.0;

        GPGenePrint current = (GPGenePrint)get(0);
        double leftX = current.x;
        double rightX = current.getNodeRight();

        for (int i = 1; i < containerSize(); i++) {
            current = (GPGenePrint)get(i);
            if (current.x < leftX)
                leftX = current.x;
            double curX = current.getNodeRight();
            if (curX > rightX)
                rightX = curX;
        }
        //doesn't include trailing space
        return (rightX+leftX)/2.0;
    }

    /**
     * Calculates the x coordinates of all the nodes below this
     * level assuming that all can be packed to minimum spacing
     * and each container is centered under its parent. This is
     * a first-order approximation to the final position but 
     * generally leads to overlaps between adjacent node groups. 
     * The x coordinate of this node must be initialized before
     * calling calcPackedPos().
     *
     * @param minX  an array holding the minimum x coordinate
     *          at each level of the tree. This array is
     *          updated for every node that is processed here.
     * @param curDepth  the current node depth, where the root of
     *          the tree is depth 0.
     */
    protected void calcPackedPos(double[] minX, int curDepth) {
        //update minX at this depth
        if (x < minX[curDepth])
            minX[curDepth] = x;

        //done if it's a terminal
        if (containerSize() == 0)
            return;

        //nodes contained in this gene are centered under it
        double leftX = (x+getNodeRight()-calcPackedContainerWidth())/2.0;

        //place the children
        for (int i = 0; i < containerSize(); i++) {
            GPGenePrint current = (GPGenePrint)get(i);
            current.x = leftX;
            leftX += current.getNodeWidth()+xSpacing;

            //place the children's children
            current.calcPackedPos(minX, curDepth+1);
        }
    }

    /**
     * Adjusts the x coordinate of this node and its children whenever
     * the node overlaps the region of the node to its left. This
     * spreads the nodes to the right just enough to get rid of 
     * overlaps. Two additional adjustments are also made: terminal
     * nodes dangling at more than minimum spacing to the left or
     * right of their node groups are tightened to minimum spacing;
     * and parent nodes are recentered over their children after
     * the other adjustments have been made.<p>
     *
     * This method is called repeatedly until no further adjustments
     * are needed or an iteration limit is reached.
     *
     * @param minX  an array holding the minimum x coordinate
     *          at each level of the tree. This array is
     *          updated for every node that is processed here.
     * @param curX  an array holding the largest x coordinate
     *          used by a node at each depth level so far. This
     *          allows the detection of overlaps.
     * @param curDepth  the current node depth, where the root of
     *          the tree is depth 0.
     * @return true if the position of any node was adjusted.
     */
    protected boolean adjustOverlaps(
        double[] minX, double[] curX, int curDepth) {

        boolean ret;

        if (x < curX[curDepth]) {
            //node overlaps one to its left, move it and its children right
            shiftX(x-curX[curDepth]);
            ret = true;
        } else
            ret = false;

        //move past this node at this depth
        curX[curDepth] = getNodeRight()+xSpacing;

        //check children
        for (int i = 0; i < containerSize(); i++) {
            GPGenePrint current = (GPGenePrint)get(i);
            ret |= current.adjustOverlaps(minX, curX, curDepth+1);

            if (i < containerSize()-1) {
                //look for terminals dangling to the left
                if (current.containerSize() == 0) {
                    double nextX = ((GPGenePrint)get(i+1)).x;
                    if (nextX-current.getNodeRight() > xSpacing) {
                        current.x = nextX-current.getNodeWidth()-xSpacing;
                        ret = true;
                    }
                }
            } else if (i > 0) {
                //look for terminals dangling to the right
                if (current.containerSize() == 0) {
                    double prevX = ((GPGenePrint)get(i-1)).getNodeRight();
                    if (current.x-prevX > xSpacing+1.0) {
                        current.x = prevX+xSpacing+1.0;
                        ret = true;
                    }
                }
            }
        }

        //recenter node for shifts in its children
        double newX = calcContainerCenter()-getNodeWidth()/2.0;
        if (Math.abs(newX-x) > 0.01) {
            x = newX;
            ret = true;
            if (x < minX[curDepth])
                minX[curDepth] = x;
        }

        //return true if any node in tree required adjustment
        return ret;
    }

    /**
     * Spreads terminal nodes so that they are equally spaced within
     * the boundaries of the container.
     */
    protected void spreadTerminals() {
        //done if it's a terminal
        if (containerSize() == 0)
            return;

        //space out terminals within constraints
        int left = 0;
        for (int i = 1; i < containerSize(); i++) {
            GPGenePrint current = (GPGenePrint)get(i);
            if ((i == containerSize()-1) || (current.containerSize() != 0)) {
                //found a region of fixed positions
                int spaceCount = i-left;
                if (spaceCount > 1) {
                    //there's at least one terminal to space out
                    double insideLeft = ((GPGenePrint)get(left)).getNodeRight();
                    double freeSpace = ((GPGenePrint)get(i)).x-insideLeft;
                    for (int j = left+1; j < i; j++)
                        freeSpace -= ((GPGenePrint)get(j)).getNodeWidth();

                    //divide free space into equal parts
                    freeSpace /= spaceCount;

                    //place inside nodes at this spacing
                    for (int j = left+1; j < i; j++) {
                        insideLeft += freeSpace;
                        GPGenePrint currentj = (GPGenePrint)get(j);
                        currentj.x = insideLeft;
                        insideLeft += currentj.getNodeWidth();
                    }
                }

                //prepare for another region
                left = i;
            }
        }

         //do the same for children
        for (int i = 0; i < containerSize(); i++)
            ((GPGenePrint)get(i)).spreadTerminals();
    }

    /**
     * Returns the smallest x value anywhere in the tree.
     */
    protected double calcMinX() { return calcMinX(x); }

    /**
     * Returns the smallest x value anywhere in the tree.
     */
    protected double calcMinX(double minSoFar) {
        if (x < minSoFar)
            minSoFar = x;
        for (int i = 0; i < containerSize(); i++) {
            double d = ((GPGenePrint)get(i)).calcMinX(minSoFar);
            if (d < minSoFar)
                minSoFar = d;
        }
        return minSoFar;
    }

    /**
     * Returns the largest x value anywhere in the tree.
     */
    protected double calcMaxX() { return calcMaxX(getNodeRight()); }

    /**
     * Returns the largest x value anywhere in the tree.
     */
    protected double calcMaxX(double maxSoFar) {
        double right = getNodeRight();
        if (right > maxSoFar)
            maxSoFar = right;
        for (int i = 0; i < containerSize(); i++) {
            double d = ((GPGenePrint)get(i)).calcMaxX(maxSoFar);
            if (d > maxSoFar)
                maxSoFar = d;
        }
        return maxSoFar;
    }

    /**
     * Shifts the x coordinates of all nodes by subtracting dx.
     */
    protected void shiftX(double dx) {
        x -= dx;
        for (int i = 0; i < containerSize(); i++)
            ((GPGenePrint)get(i)).shiftX(dx);
    }

    /**
     * Calculates the x coordinates of all nodes so that none
     * overlap, otherwise-unconstrained nodes are evenly spaced,
     * and parent nodes are centered over their children as much
     * as possible. The minimum x value is 0.0 upon return.
     *
     * @return the depth of the tree.
     */
    protected int calcXPositions() {
        //create array to hold minimum x position at each level of tree
        int dep = depth();
        double[] minX = new double[dep];

        //root node is centered at x = 0
        x = -getNodeWidth()/2.0;

        //compute ideal position of each node, fill in min x at each level
        calcPackedPos(minX, 0);

        //spread out nodes to avoid overlaps
        double[] curX = new double[dep];
        int maxTries = 2*dep; //maximum tries in case of non-convergence
        do {
            for (int i = 0; i < dep; i++)
                curX[i] = minX[i];
        } while (adjustOverlaps(minX, curX, 0) && (maxTries-- > 0));

        //equally space non-edge terminals within their containers
        spreadTerminals();

        //find minimum value of x and shift all x values to 0.0 or greater
        shiftX(calcMinX());

        return dep;
    }

    /**
     * Prints spaces until curX[curDepth] equals or exceeds rx.
     */
    protected void spaceTo(
        PrintStream os, int curDepth, double[] curX, double rx) {

        while (curX[curDepth] < rx) {
            os.print(' ');
            curX[curDepth] += 1.0;
        }
    }

    /**
     * Prints all nodes at level depToPrint in left-to-right order.
     * This forms one line of pseudo-graphic output.
     */
    protected void printLevel(PrintStream os,
        int depToPrint, int curDepth, double[] curX) {
        
        if (depToPrint == curDepth) {
            spaceTo(os, curDepth, curX, Math.rint(x));
            os.print(peer.geneRep());
            curX[curDepth] += getNodeWidth();
        } else {
            for (int i = 0; i < containerSize(); i++)
                ((GPGenePrint)get(i)).printLevel(os, depToPrint, curDepth+1, curX);
        }
    }

    /**
     * Prints pseudo-graphic connectors between nodes at 
     * level depToPrint and nodes at level depToPrint+1.
     */
    protected void printConnectors(PrintStream os,
    int depToPrint, int curDepth, double[] curX) {
        if (depToPrint == curDepth) {
            //does it need connectors to a deeper level?
            if (containerSize() != 0) {
                //compute left, right, center of this node
                double lx = Math.rint(x);
                double rx = Math.rint(getNodeRight());
                double cx = Math.rint((lx+rx)/2.0);

                for (int i = 0; i < containerSize(); i++) {
                    GPGenePrint current = (GPGenePrint)get(i);
                    //compute left and right of child node
                    double clx = Math.rint(current.x);
                    double crx = Math.rint(current.getNodeRight());

                    //compute character and position for connector
                    double conx;
                    char conCh;
                    if (((conx = cx) >= clx) && (cx <= crx))
                        conCh = '|';
                    else if (((conx = rx+1.0) >= (clx-1.0)) && (conx <= crx))
                        conCh = '\\';
                    else if (((conx = lx-1.0) <= (crx+1.0)) && (conx >= clx))
                        conCh = '/';
                    else if (clx > rx) {
                        conx = clx-1.0;
                        conCh = '\\';
                    } else {
                        conx = crx+1.0;
                        conCh = '/';
                    }

                    //print connector
                    spaceTo(os, curDepth, curX, conx);
                    os.print(conCh);
                    curX[curDepth] += 1.0;
                }
            }
        } else {
            for (int i = 0; i < containerSize(); i++)
                ((GPGenePrint)get(i)).printConnectors(os, depToPrint, curDepth+1, curX);
        }
    }

    /**
     * Computes the x coordinates for all nodes in this gene tree
     * and writes the tree in pseudo-graphic format to the
     * specified PrintStream. None of the configuration variables
     * in cfg are used by default.
     */
    public void printOn(PrintStream os, GPVariables cfg) {

        //fill in the x field of all nodes
        int dep = calcXPositions();

        //print all depths, row by row
        double[] curX = new double[dep];
        for (int d = 0; d < dep; d++) {
            //print the node names
            curX[d] = 0.0;
            spaceTo(os, d, curX, xMargin);
            curX[d] = 0.0;
            printLevel(os, d, 0, curX);
            os.println();

            //print the connectors
            if (d < dep-1) {
                //no connectors below final level
                curX[d] = 0.0;
                spaceTo(os, d, curX, xMargin);
                curX[d] = 0.0;
                printConnectors(os, d, 0, curX);
                os.println();
            }
        }
    }

    //following for drawing graphics version of tree

    /**
     * Converts the x coordinate of a node to the corresponding
     * x pixel position for the start of the node text.
     */
    protected final int xText(GPDrawing ods, double x) {
        return (int)Math.round(ods.cw*x)+rectMargin;
    }

    /**
     * Converts the depth of a node to the corresponding y pixel
     * position for the node text.
     */
    protected final int yText(GPDrawing ods, int depth) {
        return (rowsPerNode*depth*ods.ch)+ods.as+rectMargin;
    }

    /**
     * Converts the x coordinate of a node to the corresponding
     * x pixel position for the left edge of the surrounding 
     * node rectangle.
     */
    protected final int xBox(GPDrawing ods, double x) {
        return xText(ods, x)-rectMargin;
    }

    /**
     * Converts the depth of a node to the corresponding
     * y pixel position for the top edge of the surrounding 
     * node rectangle.
     */
    protected final int yBox(GPDrawing ods, int depth) {
        return rowsPerNode*depth*ods.ch;
    }

    /**
     * Computes the pixel width of the node rectangle given
     * the string to appear within the rectangle.
     */
    protected final int wBox(GPDrawing ods, String nodeName) {
        return ods.stringWidth(nodeName)+2*rectMargin;
    }

    /**
     * Computes the pixel height of the node rectangle.
     */
    protected final int hBox(GPDrawing ods) {
        return ods.ch+2*rectMargin;
    }

    /**
     * Draws all the nodes recursively starting with this one, 
     * including each node's text, a rounded rectangle surrounding it, 
     * and connectors to all of its children.
     */
    protected void drawGifNode(GPDrawing ods, int curDepth) {

        //compute important coordinates
        int xt = xText(ods, x);
        int yt = yText(ods, curDepth);
        int xb = xBox(ods, x);
        int yb = yBox(ods, curDepth);
        int wb = wBox(ods, peer.geneRep());
        int pxc = xb+wb/2;
        int hb = hBox(ods);
        int pyb = yb+hb;
        int cyb = yBox(ods, curDepth+1);

        //draw node name with a box around it
        ods.getGra().drawString(peer.geneRep(), xt, yt);
        ods.getGra().drawRoundRect(xb, yb, wb, hb, arcRadius, arcRadius);

        for (int i = 0; i < containerSize(); i++) {
            GPGenePrint current = (GPGenePrint)get(i);

            //draw connector to child
            int cxc = xBox(ods, current.x)+wBox(ods, current.peer.geneRep())/2;
            ods.getGra().drawLine(pxc, pyb, cxc, cyb);

            //draw child
            current.drawGifNode(ods, curDepth+1);
        }
    }

    /**
     * Computes the x coordinates for all nodes in this gene tree
     * and writes the tree in gif format to the specified file.
     *
     * @param ods  a surface to draw on.
     * @param fname  the name of a gif file to create.
     * @param title  a title to draw on the image, usually "RPB", 
     *          "ADF0", etc. The title is always placed on the
     *          first row of the drawing, to the left of the root
     *          node if possible, otherwise at the right edge
     *          of the image.
     * @param cfg  a set of global configuration variables. Only the
     *          <a href="gpjpp.GPVariables.html#TreeFontSize">
     *          TreeFontSize</a> variable is used here, to set the font
     *          size for text within the image.
     */
    public void drawOn(GPDrawing ods, String fname, 
        String title, GPVariables cfg) throws IOException {

        //fill in the x field of all nodes
        int dep = calcXPositions();
        double maxX = calcMaxX();

        //set the font size in the image
        ods.setFontSize(cfg.TreeFontSize);

        //determine optimum image size
        int w = xText(ods, maxX+1.0)+rectMargin+1;
        int h = yBox(ods, dep-1)+hBox(ods)+1;

        //adjust width to fit title if needed
        int xb = 0;
        int sw = 0;
        boolean haveTitle = (title != null) && (title.length() != 0);
        if (haveTitle) {
            xb = xBox(ods, x);
            sw = ods.stringWidth(title)+5;
            int wb = wBox(ods, peer.geneRep());
            if ((xb <= sw) && (xb+wb+sw > w))
                w = xb+wb+sw;
        }

        //prepare drawing surface of needed size
        ods.prepImage(w, h);

        //write the title to the image
        if (haveTitle) {
            if (xb > sw)
                ods.getGra().drawString(title, 
                    xText(ods, 0.0), yText(ods, 0));
            else
                ods.getGra().drawString(title, 
                    w-sw+5, yText(ods, 0));
        }

        //write the tree to the image
        drawGifNode(ods, 0);

        //write the image to a file
        ods.writeGif(fname);
    }
}
