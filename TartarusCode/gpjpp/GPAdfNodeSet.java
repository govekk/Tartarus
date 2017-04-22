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
 * all branches of a particular genetic programming problem.<p>
 *
 * GPAdfNodeSet holds a <a href="gpjpp.GPNodeSet.html#_top_">
 * GPNodeSet</a> for the result-producing branch
 * of the tree and also one for each ADF.<p>
 *
 * Because this class has no data fields of its own, its load and
 * save methods are inherited unchanged from 
 * <a href="gpjpp.GPContainer.html#_top_">GPContainer</a>.
 *
 * @version 1.0
*/
public class GPAdfNodeSet extends GPContainer {

    /**
     * Public null constructor used during stream loading only.
     */
    public GPAdfNodeSet() {}

    /**
     * The constructor called by user code to reserve space for
     * the branches in each GP. The zeroth branch is always the
     * result-producing branch. The other branches, if any, are
     * ADFs that can be called from the zeroth branch.<p>
     *
     * The supplied example programs show how to create 
     * GPAdfNodeSets with and without ADFs.
     *
     * @param numOfTrees  the number of branches in each individual.
     */
    public GPAdfNodeSet(int numOfTrees) { super(numOfTrees); }

    /**
     * A constructor that can be called to clone a GPAdfNodeSet. 
     * Normally not used.
     */
    public GPAdfNodeSet(GPAdfNodeSet gpo) { super(gpo); }

    /**
     * Implements the Cloneable interface.
     * This clones a GPAdfNodeSet but is normally not used.
     *
     * @return the cloned object.
     */
    protected synchronized Object clone() { return new GPAdfNodeSet(this); }

    /**
     * Returns a code identifying the class in a stream file.
     *
     * @return the ID code ADFNODESETID.
     */
    public byte isA() { return ADFNODESETID; }

    /**
     * Determines whether this GPAdfNodeSet equals another object. 
     * It returns true if obj is not null, is an instance of a GPAdfNodeSet 
     * (or a descendant), and contains the same GPNodeSet values. 
     * This function is called when a checkpoint is loaded by GPRun, 
     * to determine whether the program and the checkpoint are 
     * consistent.
     *
     * @param obj any Java object reference, including null.
     * @return true if this and obj are equivalent.
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof GPAdfNodeSet))
            return false;
        GPAdfNodeSet gpo = (GPAdfNodeSet)obj;

        if (containerSize() != gpo.containerSize())
            return false;

        //loop through all subtrees and compare them
        for (int i = 0; i < containerSize(); i++) {
            GPNodeSet g1 = (GPNodeSet)get(i);
            GPNodeSet g2 = (GPNodeSet)gpo.get(i);
            if (g1 != null) {
                if (!g1.equals(g2))
                    return false;
            } else if (g2 != null)
                return false;
        }
        return true;
    }

    /**
     * Writes a GPAdfNodeSet in text format to a PrintStream.
     * Each node set is labeled as "RPB" or "ADFn" depending
     * on its position within the GPAdfNodeSet.
     */
    public void printOn(PrintStream os, GPVariables cfg) {

        for (int i = 0; i < containerSize(); i++) {
            GPNodeSet current = (GPNodeSet)get(i);
            if (current != null) {
                if (i == 0)
                    os.print("RPB:  ");
                else
                    os.print("ADF"+(i-1)+": ");
                current.printOn(os, cfg);
                os.println();
            } else
                os.println("Null set");
        }
    }

}
