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
 * Stores information about the functions and terminals used in 
 * one tree branch of a particular genetic programming problem.<p>
 *
 * GPNodeSet is a container that holds an arbitrary number of
 * function and terminal nodes. For improved performance during
 * some operations, all of the functions are stored at the beginning
 * of the container and all of the terminals are stored at the end.
 * There might be some null elements in the middle and the code is
 * designed to accommodate those.
 *
 * @see gpjpp.GPNode
 * @see gpjpp.GPAdfNodeSet
 *
 * @version 1.0
 */
public class GPNodeSet extends GPContainer {

    /**
     * The number of function node types supported by this branch.
     */
    protected int numFunctions;

    /**
     * The number of terminal node types supported by this branch.
     */
    protected int numTerminals;

    /**
     * Public null constructor used during stream loading only.
     */
    public GPNodeSet() { }

    /**
     * The constructor called by user code to reserve space for
     * the functions and terminals allowed in this branch.
     *
     * @param numOfNodes  the maximum number of functions and terminals
     *                    to be specified. This can exceed the actual
     *                    number added subsequently by calling putNode
     *                    but should generally equal the actual number.
     */
    public GPNodeSet(int numOfNodes) { super(numOfNodes); }

    /**
     * A constructor that can be called to clone a GPNodeSet. Normally
     * not used.
     */
    public GPNodeSet(GPNodeSet gpo) {
        super(gpo);
        numFunctions = gpo.numFunctions;
        numTerminals = gpo.numTerminals;
    }

    /**
     * Implements the Cloneable interface.
     * This clones a GPNodeSet but is normally not used.
     *
     * @return the cloned object.
     */
    protected synchronized Object clone() { return new GPNodeSet(this); }

    /**
     * Returns a code identifying the class in a stream file.
     *
     * @return the ID code NODESETID.
     */
    public byte isA() { return NODESETID; }

    /**
     * Returns the number of functions in this node set.
     *
     * @see gpjpp.GPNodeSet#numFunctions
     */
    public int functions() { return numFunctions; }

    /**
     * Returns the number of terminals in this node set.
     *
     * @see gpjpp.GPNodeSet#numTerminals
     */
    public int terminals() { return numTerminals; }

    /**
     * Adds a function or terminal node to a GPNodeSet. Functions are
     * automatically stored at the next available location at the
     * start of the container, while terminals are added at the end
     * of the container. <em>Do not call GPContainer.put() to store
     * nodes in a GPNodeSet since this may corrupt the expected 
     * ordering of the nodes.</em>
     *
     * @param gpo  a non-null GPNode instance.
     *
     * @exception  java.lang.ArrayStoreException
     *               if the container is already full.
     * @exception  java.lang.RuntimeException
     *               if a node with gpo's value is already found in
     *               the GPNodeSet.
     */
    public void putNode(GPNode gpo) {
        //check if full
        if (numFunctions+numTerminals == containerSize())
            throw new ArrayStoreException();

        //cannot duplicate node with same identification number
        if (searchForNode(gpo.value()) != null)
            throw new RuntimeException("Cannot duplicate node ID in node set");

        //put functions at the beginning, terminals at the end.
        if (gpo.isFunction())
            super.put(numFunctions++, gpo);
        else
            super.put(containerSize()-(++numTerminals), gpo);
    }

    /**
     * Determines whether this GPNodeSet equals another object. 
     * It returns true if obj is not null, is an instance of a GPNodeSet 
     * (or a descendant), and contains the same 
     * <a href="gpjpp.GPNode.html#_top_">GPNode</a> values. 
     * This function is called when a checkpoint is loaded by 
     * <a href="gpjpp.GPRun.html#_top_">GPRun</a>, 
     * to determine whether the program and the checkpoint are 
     * consistent.
     *
     * @param obj any Java object reference, including null.
     * @return true if this and obj are equivalent.
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof GPNodeSet))
            return false;
        GPNodeSet gpo = (GPNodeSet)obj;

        if (containerSize() != gpo.containerSize())
            return false;
        if (functions() != gpo.functions())
            return false;
        if (terminals() != gpo.terminals())
            return false;

        //loop through all subtrees and compare them
        for (int i = 0; i < containerSize(); i++) {
            GPNode g1 = (GPNode)get(i);
            GPNode g2 = (GPNode)gpo.get(i);
            if (g1 != null) {
                if (!g1.equals(g2))
                    return false;
            } else if (g2 != null)
                return false;
        }
        return true;
    }

    /**
     * Returns the GPNode in this set that has the specified node
     * value, or null if none is found. For internal use.
     */
    protected GPNode searchForNode(int value) {
        for (int i = 0; i < containerSize(); i++) {
            GPNode current = (GPNode)get(i);
            if ((current != null) && (current.value() == value))
                return current;
        }
        return null;
    }

    /**
     * Returns a random function node from this set. Used by
     * <a href="gpjpp.GP.html#create">GP.create()</a> and 
     * <a href="gpjpp.GPGene.html#create">GPGene.create()</a> 
     * to build new trees.
     */
    public GPNode chooseFunction() {
        return (GPNode)get(GPRandom.nextInt(numFunctions));
    }

    /**
     * Returns a random terminal node from this set. Used by
     * <a href="gpjpp.GPGene.html#create">GPGene.create()</a> 
     * to build new trees.
     */
    public GPNode chooseTerminal() {
        return (GPNode)get(containerSize()-numTerminals+
                           GPRandom.nextInt(numTerminals));
    }

    /**
     * Returns a random node from this set with the specified
     * number of arguments. Returns null if there is no such
     * node. Used for 
     * <a href="gpjpp.GP.html#swapMutation">swap mutation</a>.
     */
    public GPNode chooseNodeWithArgs(int args) {
        int num;

        //count all nodes that have the specified number of arguments
        num = 0;
        for (int i = 0; i < containerSize(); i++) {
            GPNode n = (GPNode)get(i);
            if (n != null)
                if (n.arguments() == args)
                    num++;
        }
        if (num == 0)
            return null;

        //return the node with random index
        int k = GPRandom.nextInt(num);
        num = 0;
        for (int i = 0; i < containerSize(); i++) {
            GPNode n = (GPNode)get(i);
            if (n != null)
                if (n.arguments() == args)
                    if (num++ == k)
                        return n;
        }

        //avoid compiler warnings (this code is never reached)
        return null;
    }

    /**
     * Loads a GPNodeSet from the specified stream. Reads the 
     * numFunctions and numTerminals fields from the stream
     * and then loads the container.
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

        numFunctions = is.readInt();
        numTerminals = is.readInt();
        super.load(is);
    }

    /**
     * Saves a GPNodeSet to the specified stream. Writes the 
     * numFunctions and numTerminals fields to the stream
     * and then stores the container.
     *
     * @exception java.io.IOException
     *              if an error occurs while writing the stream.
     */
    protected void save(DataOutputStream os) throws IOException {

        os.writeInt(numFunctions);
        os.writeInt(numTerminals);
        super.save(os);
    }

    /**
     * Writes a GPNodeSet in text format to a PrintStream.
     * Each node is printed as its representation string followed
     * by its number of arguments, if any.
     */
    public void printOn(PrintStream os, GPVariables cfg) {

        for (int i = 0; i < containerSize(); i++) {
            GPNode current = (GPNode)get(i);
            if (i > 0)
                os.print(" ");
            if (current != null) {
                current.printOn(os, cfg);
                if (current.isFunction())
                    os.print("("+current.arguments()+")");
            } else
                os.print("null");
        }
    }

}
