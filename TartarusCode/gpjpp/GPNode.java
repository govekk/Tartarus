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
 * Stores information about one function or terminal type in 
 * a particular genetic programming problem. GPNode objects are
 * stored within the container of a 
 * <a href="gpjpp.GPNodeSet.html#_top_">GPNodeSet</a> object, 
 * representing all the node types allowed within one branch 
 * of a genetic program.
 *
 * @version 1.0
 */
public class GPNode extends GPObject {

    /**
     * Indicates a node's purpose to a user fitness evaluation method.
     * The nodeValue is often the integer value of a character such 
     * as '+' or '-', or is assigned a symbolic constant such as
     * FROG or MOW.
     */
    protected int nodeValue;

    /**
     * The number of arguments used by the node, 0 if the node is
     * a terminal instead of a function. numOfArgs must be in the 
     * range from 0 to 255 when streaming is used, since the 
     * number of arguments is stored on the stream in a single byte.
     */
    protected int numOfArgs;

    /**
     * The string usually printed by printOn when displaying 
     * s-expressions and when drawing graphic tree images.
     */
    protected String representation;

    /**
     * A unique index value used to stream genes efficiently. Its
     * value is set by the static method 
     * <a href="gpjpp.GPGene.html#createNodeIndex">
     * GPGene.createNodeIndex</a> which must be called by the stream 
     * manager before genes are streamed.
     *
     * @see gpjpp.GPNode#setIndex
     */
    protected byte nodeIndex;

    /**
     * Public null constructor used during stream loading only.
     */
    public GPNode() { }

    /**
     * The constructor called by user code to describe the functions
     * for the genetic programming problem. 
     *
     * @param nVal  an arbitrary integer value used to identify 
                    the node type. Must be unique within the branch 
                    (within its GPNodeSet).
     * @param str   a string that is usually written out to 
                    represent the node in s-expressions and
                    tree diagrams.
     * @param args  the number of arguments to the function.
     *
     * @see gpjpp.GPNode#nodeValue
     * @see gpjpp.GPNode#numOfArgs
     */
    public GPNode(int nVal, String str, int args) {
        nodeValue = nVal;
        representation = new String(str);
        numOfArgs = args;
    }

    /**
     * The constructor called by user code to describe the terminals
     * for the genetic programming problem. 
     *
     * @param nVal  an arbitrary integer value used to identify 
                    the node type. Must be unique within the branch 
                    (within its GPNodeSet).
     * @param str   a string that is usually written out to 
                    represent the node in s-expressions and
                    tree diagrams.
     *
     * @see gpjpp.GPNode#nodeValue
     * @see gpjpp.GPNode#numOfArgs
     */
    public GPNode(int nVal, String str) {
        nodeValue = nVal;
        representation = new String(str);
        numOfArgs = 0;
    }

    /**
     * A constructor that can be called to clone a GPNode. Normally
     * not used.
     */
    public GPNode(GPNode gpo) { 
        this(gpo.value(), gpo.rep(), gpo.arguments()); 
    }

    /**
     * Implements the Cloneable interface.
     * This clones a GPNode but is normally not used.
     *
     * @return the cloned object.
     */
    protected synchronized Object clone() { return new GPNode(this); }

    /**
     * Returns a code identifying the class in a stream file.
     *
     * @return the ID code NODEID.
     */
    public byte isA() { return NODEID; }

    /**
     * Returns the integer node value.
     */
    public int value() { return nodeValue; }

    /**
     * Returns the string representation of the node.
     */
    public String rep() { return representation; }

    /**
     * Returns true if the node is a function, that is, has more 
     * than zero arguments.
     */
    public boolean isFunction() { return (numOfArgs != 0); }

    /**
     * Returns true if the node is a terminal, that is, has zero
     * arguments.
     */
    public boolean isTerminal() { return (numOfArgs == 0); }

    /**
     * Returns the number of arguments to the node.
     */
    public int arguments() { return numOfArgs; }

    /**
     * Sets the node index used for streaming.
     *
     * @see gpjpp.GPGene#createNodeIndex
     */
    public void setIndex(byte index) { nodeIndex = index; }

    /**
     * Returns the node index used for streaming.
     *
     * @see gpjpp.GPGene#createNodeIndex
     * @see gpjpp.GPGene#save
     */
    public byte getIndex() { return nodeIndex; }

    /**
     * Determines whether this GPNode equals another object. It returns
     * true if obj is not null, is an instance of a GPNode (or a
     * descendant), and has the same value, number of arguments,
     * and string representation. This function is called when a
     * checkpoint is loaded by GPRun, to determine whether the 
     * program and the checkpoint are consistent.
     *
     * @param obj any Java object reference, including null.
     * @return true if this and obj are equivalent.
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof GPNode))
            return false;
        GPNode gpo = (GPNode)obj;

        if (value() != gpo.value())
            return false;
        if (arguments() != gpo.arguments())
            return false;
        if (!rep().equals(gpo.rep()))
            return false;
        return true;
    }

    /**
     * Loads a GPNode from the specified stream. Reads the 
     * nodeValue, numOfArgs, and representation fields from the
     * stream.
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
     */
    protected synchronized void load(DataInputStream is)
        throws ClassNotFoundException, IOException,
            InstantiationException, IllegalAccessException {

        nodeValue = is.readInt();
        numOfArgs = is.readInt();
        representation = is.readUTF();
    }

    /**
     * Saves a GPNode to the specified stream. Writes the 
     * nodeValue, numOfArgs, and representation fields to the
     * stream.
     *
     * @exception java.io.IOException
     *              if an error occurs while writing the stream.
     */
    protected void save(DataOutputStream os) throws IOException {

        os.writeInt(nodeValue);
        os.writeInt(numOfArgs);
        os.writeUTF(representation);
    }

    /**
     * Writes a GPNode in text format to a PrintStream.
     * The node is printed simply as its representation string.
     */
    public void printOn(PrintStream os, GPVariables cfg) {
        os.print(representation);
    }

}
