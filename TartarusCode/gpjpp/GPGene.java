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

/**
 * GP trees are composed of GPGene objects, which represent the 
 * function or terminal of each element of an s-expression. Each
 * tree is rooted by a GPGene object, which in turn contains GPGene
 * objects for each of its function arguments.<p>
 *
 * GPGene contains a reference to a <a href="gpjpp.GPNode.html#_top_">
 * GPNode</a> object that describes the nature of the node. The 
 * arguments to a GPGene are stored in its container, of which GPGene 
 * is a subclass.<p>
 *
 * GPGene is abstract in the sense that it does not contain a method
 * used to evaluate the fitness of a tree. It is not formally abstract
 * in Java terminology because gpjpp does not know the data type
 * embodied by the tree. A gene might represent and return a boolean,
 * a real, an integer, a vector, nothing at all, or something 
 * completely different. Only at the <a href="gpjpp.GP.html#_top_">GP</a>
 * level (a collection of tree branches forming a complete genetic 
 * program) does gpjpp enforce the requirement that the GP's fitness 
 * evaluate to a real number.<p>
 *
 * The user program typically must create a subclass of GPGene that
 * defines a GPGene fitness function called by GP.evaluate(). The 
 * example programs show how to do so in several different situations.
 *
 * @version 1.0
 */
public class GPGene extends GPContainer {

    /**
     * An array of all node types in all branches used temporarily
     * while loading the array from a stream.
     * 
     * @see gpjpp.GPGene#createNodeIndex
     */
    protected static GPNode[] allNodes;

    /**
     * A reference to the node type for this gene.
     */
    protected GPNode node;

    /**
     * Public null constructor used during stream loading only.
     */
    public GPGene() { }

    /**
     * Constructor used when trees are first created with random
     * node types.
     *
     * @param gpo  a node type that is an element of the current
     *             branch's node set.
     */
    public GPGene(GPNode gpo) {
        super(gpo.arguments());
        node = gpo;
    }

    /**
     * A constructor that is called to clone a GPGene. Used
     * whenever a tree is selected for reproduction or crossover.
     */
    public GPGene(GPGene gpo) {
        super(gpo);
        node = gpo.node;
    }

    /**
     * Implements the Cloneable interface.
     * This (or its user subclass) is called during reproduction.
     *
     * @return the cloned object.
     */
    protected synchronized Object clone() { return new GPGene(this); }

    /**
     * Returns a code identifying the class in a stream file.
     *
     * @return the ID code GENEID.
     */
    public byte isA() { return GENEID; }

    /**
     * Creates a child gene while new trees are being built. The
     * user must generally override this in a subclass to create
     * genes of user type. See the example programs.
     *
     * @param gpo  a node type that is an element of the current
     *             branch's node set.
     * @return the newly created gene with an empty container.
     */
    public GPGene createChild(GPNode gpo) { return new GPGene(gpo); }

    /**
     * A debugging/testing method to ensure that no null node or gene
     * references are found in this gene or any of its children.
     *
     * @exception java.lang.RuntimeException
     *              if a null gene or node reference is found.
     */
    public void testNull() {
        if (node == null)
            throw new RuntimeException("Null node found in gene");
        for (int i = 0; i < containerSize(); i++) {
            GPGene current = (GPGene)get(i);
            if (current == null)
                throw new RuntimeException("Null gene found in gene tree");
            //test children of the gene
            current.testNull();
        }
    }

    /** 
     * Returns true if this gene represents a function.
     */
    public boolean isFunction() { return (containerSize() > 0); }

    /** 
     * Returns true if this gene represents a terminal.
     */
    public boolean isTerminal() { return (containerSize() == 0); }

    /**
     * Returns the node reference of this gene.
     */
    public GPNode geneNode() { return node; }

    /**
     * Returns the string representation of this gene as given
     * by its node type. This method can be overridden in a user
     * subclass of GPGene if the string representation should vary
     * depending on the data value of each gene. See the Lawn
     * program for an example.
     */
    public String geneRep() { return node.rep(); }

    /**
     * Returns the total number of function genes included by 
     * this gene and all of its children. If this gene is a terminal,
     * countFunctions returns 0. Otherwise it returns at least 1
     * and recursively traces all of its children. Used internally
     * during shrink mutation.
     */
    protected int countFunctions() {
        if (isFunction()) {
            int count = 1;

            //loop through the children
            for (int i = 0; i < containerSize(); i++) 
                count += ((GPGene)get(i)).countFunctions();
            return count;

        } else
            return 0;
    }

    /**
     * Returns the number of genes attached to this one, including
     * itself. This is the complexity or length of the branch.
     */
    public int length() {
        int lengthSoFar = 1;

        //add length of children
        for (int i = 0; i < containerSize(); i++) 
            lengthSoFar += ((GPGene)get(i)).length();
        return lengthSoFar;
    }

    /**
     * Returns the largest depth of the tree attached to this gene.
     * If this gene is a terminal, the depth is 1. 
     */
    public int depth() { return depth(1); }

    /**
     * Called internally by depth() to compute the tree depth.
     */
    protected int depth(int depthSoFar) {
        int maxDepth = depthSoFar;

        for (int i = 0; i < containerSize(); i++) {
            int d = ((GPGene)get(i)).depth(depthSoFar+1);
            if (d > maxDepth)
                maxDepth = d;
        }
        return maxDepth;
    }

    /**
     * Creates the arguments to this gene and recursively creates
     * children according to the limits and methods specified. The
     * gene for which this method is called should have been created
     * by calling the <a href="gpjpp.GP.html#createGene">createGene()</a>
     * method of GP, which allocates an argument container of 
     * appropriate size and assigns the node field but doesn't fill 
     * in the children. create() is called by 
     * <a href="gpjpp.GP.html#create">GP.create()</a>.
     *
     * @param creationType  the method used to create the tree, either
     *      <a href="gpjpp.GPVariables.html#GPGROW">GPGROW</a> (use 
     *      function nodes to fill the tree to allowable depth) or 
     *      <a href="gpjpp.GPVariables.html#GPVARIABLE">GPVARIABLE</a> 
     *      (choose function and terminal nodes with 50:50 probability).
     * @param allowableDepth the maximum allowable depth of the tree
     *      starting from this level. If the allowable depth is 1,
     *      the children are always chosen to be terminals.
     * @param allowableLength the maximum allowable number of nodes
     *      in the tree starting at this level. Since create() cannot
     *      predict how many nodes will be added recursively it
     *      simply stops adding nodes if it exceeds allowableLength.
     *      A higher level routine in GPPopulation rejects the
     *      returned tree if the GP complexity exceeds a global limit.
     * @param ns the node set used to select functions and terminals
     *      for this branch of the GP tree.
     *
     * @return the total number of nodes in the created tree. If this
     *      value exceeds allowableLength, the tree will be rejected.
     *      create() ensures that it doesn't waste much time creating
     *      extra nodes.
     */
    public synchronized int create(int creationType, 
        int allowableDepth, int allowableLength, GPNodeSet ns) {

        int lengthSoFar = 1;

        for (int i = 0; i < containerSize(); i++) {
            //decide whether to create a function or terminal for this argument
            boolean chooseTerm;
            if (allowableDepth <= 1)
                //no more depth, must use terminal
                chooseTerm = true;
            else if (creationType == GPVariables.GPGROW)
                //use functions to allowableDepth
                chooseTerm = false;
            else
                //50:50 chance of choosing a function or a terminal
                chooseTerm = GPRandom.flip(50.0);

            GPNode newNode;
            if (chooseTerm)
                newNode = ns.chooseTerminal();
            else
                newNode = ns.chooseFunction();

            //create a new gene of chosen type and add it
            GPGene g = createChild(newNode);
            put(i, g);

            //if function node, call recursively to allowed depth
            if (chooseTerm)
                lengthSoFar++;
            else
                lengthSoFar +=
                    g.create(creationType, allowableDepth-1, 
                        allowableLength-lengthSoFar, ns);

            //stop early if complexity limit exceeded
            if (lengthSoFar > allowableLength)
                break;
        }
        return lengthSoFar;
    }

    /**
     * Determines whether this GPGene equals another object. It returns
     * true if obj is not null, is an instance of a GPGene (or a
     * descendant), and has the same structure and node values as this 
     * gene. This function is called when a GPPopulation is testing the 
     * diversity of the population. equals() is called only after a
     * <a href="gpjpp.GP.html#hashCode">hashCode()</a> function 
     * determines that two GPs are at least quite similar.<p>
     *
     * You might need to override this in cases where two terminal 
     * genes can have identical node types but still not be the same.
     * This occurs for node types that represent random constants, for
     * example.
     *
     * @param obj any Java object reference, including null.
     * @return true if this and obj are equivalent.
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof GPGene))
            return false;

        GPGene g = (GPGene)obj;

        //compare node type reference values
        if (node != g.node)
            return false;

        //compare the number of children
        if (containerSize() != g.containerSize())
            return false;

        //compare all children.
        for (int i = 0; i < containerSize(); i++) {
            GPGene g1 = (GPGene)get(i);
            GPGene g2 = (GPGene)g.get(i);
            if (g1 != null) {
                //recursively test subtree
                if (!g1.equals(g2))
                    return false;
            } else if (g2 != null)
                return false;
        }
        return true;
    }

    /**
     * Returns true if a gene of specified position and type can be
     * found within this gene. Called within 
     * <a href="gpjpp.GPGene.html#chooseFunctionOrTerminalNode">
     * chooseFunctionOrTerminalNode</a> and 
     * <a href="gpjpp.GPGene.html#chooseFunctionNode">
     * chooseFunctionNode</a> of this same class, which are used to 
     * select appropriate genes for crossover and mutation.
     *
     * @param ref  on entry specifies the container that holds this
     *             gene, the index of this gene within that container,
     *             and the count of genes to scan. On exit returns
     *             the container that holds the found gene and the 
     *             index of that gene within the container.
     * @param findFunction true if only function nodes are to be
     *             counted, false if function and terminal nodes
     *             are acceptable.
     *
     * @return     true if an acceptable node is found; false if
     *             there are fewer nodes than specified.
     */
    protected boolean findNthNode(
        GPGeneReference ref, boolean findFunction) {

        //return ref when count nodes of proper type have been visited
        if (!findFunction || (containerSize() > 0))
            if (--ref.count <= 0)
                return true;

        //otherwise scan children of this node
        for (int i = 0; i < containerSize(); i++) {
            ref.assignContainer(this, i);
            if (((GPGene)get(i)).findNthNode(ref, findFunction))
                return true;
        }

        //proper node not found
        return false;
    }

    /**
     * Attempts to find a random function node within this gene, not
     * considering this gene itself. If 10 random attempts don't find
     * a function, it returns a terminal as a last resort. Used for
     * <a href="gpjpp.GP.html#cross">crossover</a> and 
     * <a href="gpjpp.GP.html#swapMutation">swap mutation</a> 
     * by the GP class.
     *
     * @param ref  on entry specifies the container that holds this
     *             gene and the index of this gene within that 
     *             container. On exit returns the container that 
     *             holds the found gene, the index of that gene 
     *             within the container, and the number of genes 
     *             counted to reach the found one.
     *
     * @exception  RuntimeException
     *               if findNthNode can't find a node that should
     *               rightfully exist in the tree.
     */
    public void chooseFunctionOrTerminalNode(GPGeneReference ref) {
        //calculate the length of the subtree
        int totalLength = length();

        //loop trying to return a function
        GPContainer saveContainer = ref.container;
        int saveIndex = ref.index;
        int maxTries = 10;
        for (int i = 0; 
            i < (totalLength < maxTries? totalLength : maxTries); 
            i++) {
            //restore starting ref
            ref.assignContainer(saveContainer, saveIndex);

            //count a random distance into subtree
            int saveCount = 1+GPRandom.nextInt(totalLength);
            ref.count = saveCount;

            if (!findNthNode(ref, false))
                throw new RuntimeException("Couldn't find expected tree node");

            //return count in gene reference
            ref.count = saveCount;
            if (ref.getGene().isFunction())
                return;
        }
        //otherwise return a terminal
    }

    /**
     * Finds a random function node within this gene, not
     * considering this gene itself. If no such function exists,
     * the function returns false. Used for 
     * <a href="gpjpp.GP.html#shrinkMutation">shrink mutation</a> 
     * by the GP class.
     *
     * @param ref  on entry specifies the container that holds this
     *             gene and the index of this gene within that 
     *             container. On exit returns the container that 
     *             holds the found gene, the index of that gene 
     *             within the container, and the number of genes 
     *             counted to reach the found one.
     * @return     true if any function node can be found, else false.
     */
    public boolean chooseFunctionNode(GPGeneReference ref) {
        //choose a random number in the range of available functions
        //exclude this gene
        int totalFunctions = countFunctions();
        if (totalFunctions > 0) {
            int saveCount = 1+GPRandom.nextInt(totalFunctions);
            ref.count = saveCount;
            if (findNthNode(ref, true)) {
                //return count for mutation tracking
                ref.count = saveCount;
                return true;
            } else
                return false;
        } else
            return false;
    }

    /**
     * Must be called by the stream manager before saving and
     * loading genes. The routine initializes the 
     * <a href="gpjpp.GPGene.html#allNodes">allNodes</a> static
     * array and also stores within each GPNode its allNodes index.
     * This byte-sized index is stored on the stream 
     * to represent the type of each gene. When the stream is loaded 
     * again, the index is converted back to a node reference.
     *
     * @exception java.lang.RuntimeException
     *               if the total number of node types exceeds 255.
     */
    public static synchronized void createNodeIndex(GPAdfNodeSet adfNs) {

        //count the number of global nodes
        int count = 0;
        for (int i = 0; i < adfNs.containerSize(); i++) {
            GPNodeSet ns = (GPNodeSet)adfNs.get(i);
            if (ns != null) {
                for (int j = 0; j < ns.containerSize(); j++) {
                    GPNode n = (GPNode)ns.get(j);
                    if (n != null)
                        count++;
                }
            }
        }
        if (count > 255)
            throw new RuntimeException("At most 255 node types can be streamed");

        //allocate and initialize the allNodes array
        allNodes = new GPNode[count];
        count = 0;
        for (int i = 0; i < adfNs.containerSize(); i++) {
            GPNodeSet ns = (GPNodeSet)adfNs.get(i);
            if (ns != null) {
                for (int j = 0; j < ns.containerSize(); j++) {
                    GPNode n = (GPNode)ns.get(j);
                    if (n != null) {
                        allNodes[count] = n;
                        //store the index in the node itself
                        n.setIndex((byte)count);
                        count++;
                    }
                }
            }
        }
    }

    /**
     * Loads a GPGene from the specified stream. Reads the 
     * node index from the stream and converts it to a GPNode 
     * reference. Then loads the container of child genes.<p>
     *
     * @exception java.lang.ClassNotFoundException
     *              if the class indicated by the stream's ID code
     *              is not registered with GPObject.
     * @exception java.lang.InstantiationException
     *              if an error occurs while calling new or the null
     *              constructor of the specified class.
     * @exception java.lang.IllegalAccessException
     *              if the specified class or its null constructor is
     *              not public.
     * @exception java.io.IOException
     *              if an error occurs while reading the stream.
     *
     * @see gpjpp.GPGene#createNodeIndex
     */
    protected synchronized void load(DataInputStream is)
        throws ClassNotFoundException, IOException,
            InstantiationException, IllegalAccessException {

        //allNodes must be initialized
        // by calling createNodeIndex before getting to this point
        node = allNodes[is.readByte()];

        //load container (children)
        super.load(is);
    }

    /**
     * Saves a GPGene to the specified stream. Writes the 
     * node index value to the stream and then stores the container.
     *
     * @exception java.io.IOException
     *              if an error occurs while writing the stream.
     *
     * @see gpjpp.GPGene#createNodeIndex
     */
    protected void save(DataOutputStream os) throws IOException {

        //nodeIndex field of GPNode must be initialized
        // by calling createNodeIndex before getting to this point
        os.writeByte(node.getIndex());

        //save container (children)
        super.save(os);
    }

    /**
     * Writes a GPGene in text format to a PrintStream.
     * Each node is printed as its representation string followed
     * by its children, if any, in recursive depth-first order.
     * Parentheses are used to surround the arguments to each
     * function. The entire expression is written on a single
     * text line.
     */
    public void printOn(PrintStream os, GPVariables cfg) {

        if (isFunction())
            os.print("(");

        os.print(geneRep());

        //print all children
        for (int i = 0; i < containerSize(); i++) {
            os.print(" ");
            ((GPGene)get(i)).printOn(os, cfg);
        }

        if (isFunction())
            os.print(")");
    }

    /**
     * Writes a GPGene in text tree format to a PrintStream.
     * The <a href="gpjpp.GPGenePrint.html#_top_">GPGenePrint</a> class 
     * is used to format the tree in a pseudo-graphic format. Each node 
     * is printed as its representation string and is connected to its 
     * children on a lower text row using line drawing characters.
     *
     * @param     os  a PrintStream.
     * @param     cfg  the set of gpjpp configuration variables,
     *                 sometimes used to control the output.
     */
    public void printTree(PrintStream os, GPVariables cfg) {

        (new GPGenePrint(this)).printOn(os, cfg);
    }

    /**
     * Writes a GPGene in graphic gif file format. The 
     * <a href="gpjpp.GPGenePrint.html#_top_">GPGenePrint</a>
     * class is used to format the tree, which is drawn onto the
     * AWT-based offscreen drawing surface represented by GPDrawing.
     * This offscreen drawing is then encoded into the gif format and
     * stored in the file named by fname.
     *
     * @param ods  an instantiated drawing surface. GPDrawing is 
     *             a subclass of java.awt.Frame containing a
     *             single Canvas component whose image is dynamically
     *             sized to hold the tree.
     * @param fname  the name of the file to hold the gif image.
     *             This name should include the .gif extension.
     * @param title  a string that is drawn on the first line of the
     *             image to title it. Can be null or empty.
     * @param cfg  configuration parameters for the genetic
     *             run. The <a href="gpjpp.GPVariables.html#TreeFontSize">
     *             TreeFontSize</a> field is used to determine the font 
     *             size for text in the drawing.
     *
     * @exception java.io.IOException
     *              if an error occurs while writing the image file.
     */
    public void drawOn(GPDrawing ods, String fname, 
        String title, GPVariables cfg) throws IOException {

        (new GPGenePrint(this)).drawOn(ods, fname, title, cfg);
    }
}
