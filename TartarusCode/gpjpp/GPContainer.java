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
 * The superclass of most GP objects, holding a fixed-size array
 * of references to GPObject subclasses. For example, GPGene is a 
 * container holding its argument nodes. GP is a container holding 
 * its result-producing and ADF branches (themselves GPGene trees). 
 * GPPopulation is a container holding all the individual GPs 
 * in the population, etc.<p>
 *
 * GPContainer provides a convenient location to centralize the 
 * streaming operations for its subclasses.<p>
 *
 * GPContainer is generally not instantiated directly.<p>
 *
 * Several of the GPContainer methods are marked "final" so that an
 * optimizing compiler can inline these simple methods, causing a
 * substantial improvement to gpjpp performance. Except for this
 * desire for optimization, these methods don't need to be final.<p>
 *
 * No explicit array index checking or memory allocation checking 
 * is required because Java performs these checks automatically 
 * and throws exceptions if errors are detected.
 *
 * @version 1.0
 */
public class GPContainer extends GPObject {

    /**
     * An array that is allocated once during the lifetime of the
     * container. It doesn't need to be resized. Use the get() and
     * put() methods to access elements of the array.
     *
     * @see gpjpp.GPContainer#get
     * @see gpjpp.GPContainer#put
     */
    protected GPObject[] container; //array of objects

    /**
     * Public null constructor used during stream loading only.
     */
    public GPContainer() { reserveSpace(0); }

    /**
     * The constructor called by many GPContainer subclasses to 
     * allocate a container of specified size.
     *
     * @param numObjects  the number of references the container can hold.
     */
    public GPContainer(int numObjects) { reserveSpace(numObjects); }

    /**
     * Constructor called to clone a deep copy of another container.
     * This is used during the genetic evolution process to make
     * unique copies of selected individuals.
     *
     * @param gpc  the container to copy.
     */
    public GPContainer(GPContainer gpc) {
        if (gpc == null)
            return;

        reserveSpace(gpc.containerSize());

        //make a copy of all container objects of gpc
        for (int i = 0; i < containerSize(); i++) {
            GPObject current = gpc.container[i];
            if (current == null)
                put(i, null);
            else
                put(i, (GPObject)current.clone());
        }
    }

    /**
     * Implements the Cloneable interface.
     * This clones any GPContainer subclass that doesn't have its
     * own data fields.
     *
     * @return the cloned object.
     */
    protected synchronized Object clone() { 
        return new GPContainer(this); 
    }

    /**
     * Returns a code identifying the class in a stream file.
     *
     * @return the ID code CONTAINERID.
     */
    public byte isA() { return CONTAINERID; }

    /**
     * Reserves space for the specified number of object references.
     * Note that Java allows allocating a zero-size array and that 
     * the array is filled with nulls after allocation.
     *
     * @param num  the number of places to reserve
     */
    public final void reserveSpace(int num) { 
        container = new GPObject[num]; 
    }

    /**
     * Returns the number of elements the container can hold.
     *
     * @return the container capacity.
     */
    public final int containerSize() { return container.length; }

    /**
     * Stores an object reference at the specified location in the
     * container.
     *
     * @param n  the container location. Must be in the range from
     *           0 to containerSize()-1.
     * @param gpo the object reference to store. null is ok.
     */
    public final void put(int n, GPObject gpo) { container[n] = gpo; }

    /**
     * Returns the object reference from the specified location in the
     * container.
     *
     * @param n  the container location. Must be in the range from
     *           0 to containerSize()-1.
     * @return   the object reference at that location. 
                 May be null.
     */
    public final GPObject get(int n) { return container[n]; }

    /**
     * Sets all container elements to null to ensure that no 
     * references remain to objects currently held by this container.
     * This was used during testing of garbage collection but is not
     * normally called by gpjpp.
     */
    public void clear() {
        for (int i = 0; i < containerSize(); i++)
            put(i, null);
    }

    /**
     * Loads a GPContainer from the specified stream. Calls
     * createRegisteredClassObject to construct the objects
     * contained within the container.
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
     * @exception java.lang.RuntimeException
     *              if the class ID code found next in the stream
     *              does not match the isA() code for the calling
     *              object.
     */
    protected synchronized void load(DataInputStream is)
        throws ClassNotFoundException, IOException,
        InstantiationException, IllegalAccessException {

        //confirm that ID indicates right kind of container
        if (is.readByte() != isA())
            throw new RuntimeException("Invalid stream");

        //read container size from stream and reserve space
        if (this instanceof GPPopulation)
            reserveSpace(is.readInt());
        else
            reserveSpace(is.readByte());

        //load all members of the container
        for (int i = 0; i < containerSize(); i++) {
            //test if element was a null pointer or an object
            byte id = is.readByte();
            if (id != NULLID) {
                //create that element depending on the ID that was saved
                container[i] = createRegisteredClassObject(id);
                //let the object load itself
                container[i].load(is);
            }
        }
    }

    /**
     * Saves a GPContainer to the specified stream. It saves the
     * container by recursively calling the save method of each
     * object contained within the container. Any null elements
     * in the container are represented by the NULLID constant alone.
     *
     * @param     os  a formatted output stream.
     *
     * @exception java.lang.RuntimeException
     *              if a container other than 
     *              <a href="gpjpp.GPPopulation#_top_">GPPopulation</a>
     *              has more than 255 elements. This allows save() to 
     *              store most container sizes in a single byte, leading 
     *              to a reduction in overall stream size of almost 4x.
     */
    protected void save(DataOutputStream os) throws IOException {

        //save the container ID and size
        os.writeByte(isA());

        if (this instanceof GPPopulation)
            os.writeInt(containerSize());
        else {
            //max 255 elements in all containers but population
            if (containerSize() > 0xFF)
                throw new RuntimeException(
                    "Streamed containers (except populations) are limited to 255 elements");
            os.writeByte(containerSize());
        }

        //save all members of the container
        for (int i = 0; i < containerSize(); i++) {
            GPObject current = container[i];
            if (current == null)
                //no element
                os.writeByte(NULLID);
            else {
                //save ID and element itself
                os.writeByte(current.isA());
                current.save(os);
            }
        }
    }

    /**
     * Writes a GPContainer in text format to a PrintStream.
     * Every supplied GPContainer subclass overrides this method
     * to write the container in a non-generic format.
     */
    public void printOn(PrintStream os, GPVariables cfg) {

        os.println("Container has "+containerSize()+" elements:");

        //print all members of the container
        for (int i = 0; i < containerSize(); i++)
            if (container[i] == null)
                os.print("(null) ");
            else
                container[i].printOn(os, cfg);
    }
}
