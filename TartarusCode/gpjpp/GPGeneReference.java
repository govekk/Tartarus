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

/**
 * Encapsulates the input and output parameters to the tree search
 * routines used for crossover and mutation. This class is needed
 * because Java cannot compute the address of a variable. The
 * functions in gpc++ that take the address of a pointer in order to
 * update a tree cannot be directly ported to Java. Instead, gpjpp
 * maintains the container and index within the container of each
 * gene being searched. This effectively provides the address of the
 * gene itself and makes it possible to replace the gene with a
 * gene branch from another source.<p>
 *
 * This class is for internal use.
 *
 * @version 1.0
 */
public class GPGeneReference {
    /**
     * The container that holds a particular gene.
     */
    public GPContainer container;
    
    /**
     * The index of a particular gene within its container.
     */
    public int index;

    /**
     * Used by a search function to count the number of
     * genes to pass or the number that were passed.
     */
    public int count;

    /**
     * Instantiates a gene reference for later use.
     */
    public GPGeneReference() {}

    /**
     * Instantiates a gene reference with a known container and index.
     */
    public GPGeneReference(GPContainer container, int index) {
        this.container = container;
        this.index = index;
    }

    /**
     * Clones another gene reference.
     */
    public GPGeneReference(GPGeneReference ref) {
        container = ref.container;
        index = ref.index;
        count = ref.count;
    }

    /**
     * Assigns another gene reference to this one.
     */
    public void assignRef(GPGeneReference ref) {
        container = ref.container;
        index = ref.index;
        count = ref.count;
    }

    /**
     * Assigns a particular container and index to this gene reference.
     */
    public void assignContainer(GPContainer container, int index) {
        this.container = container;
        this.index = index;
    }

    /**
     * Returns the gene referenced by this class.
     */
    public GPGene getGene() { return (GPGene)container.get(index); }

    /**
     * Puts a different gene into the place of this class.
     */
    public void putGene(GPGene g) { container.put(index, g); }
}
