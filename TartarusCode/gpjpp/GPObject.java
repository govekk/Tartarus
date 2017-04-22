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
 * Abstract class used to define cloning and streaming capabilities.
 * All gpjpp classes that can be streamed are subclasses of GPObject.
 *
 * @version 1.0
 */
public abstract class GPObject implements Cloneable {

    /**
     * This do-nothing constructor gets called implicitly by
     * subclasses of GPObject.
     */
    protected GPObject() {}

    /**
     * Implements the Cloneable interface.
     * GPGene and GP classes are cloned during reproduction
     * and crossover.
     *
     * @return the cloned object.
     */
    abstract protected Object clone();

    /**
     * Returns a code identifying the class in a stream file.
     * Every class in a streamed gpjpp program must override
     * isA() and return a unique byte. Predefined ID values are
     * provided for the built-in gpjpp classes and the classes
     * a user is expected to provide.
     *
     * @return the unique ID code.
     * @see gpjpp.GPObject#OBJECTID
     */
    abstract public byte isA();

    /**
     * Loads a GPObject subclass from the specified stream. The
     * object calling this method should have been previously
     * constructed using its null constructor.
     *
     * @param     is  a formatted input stream.
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
    abstract protected void load(DataInputStream is)
        throws ClassNotFoundException, IOException,
            InstantiationException, IllegalAccessException;

    /**
     * Saves a GPObject subclass to the specified stream.
     *
     * @param     os  a formatted output stream.
     *
     * @exception java.io.IOException
     *              if an error occurs while writing the stream.
     */
    abstract protected void save(DataOutputStream os)
        throws IOException;

    /**
     * Writes a GPObject subclass in text format to a PrintStream.
     * Every gpjpp subclass overrides this method to print a text
     * representation of the class' current state.
     *
     * Because printOn writes to a PrintStream, it does not generate
     * IOExceptions. PrintStream traps exceptions, which can be 
     * checked via its checkError() method.
     *
     * @param     os  a PrintStream.
     * @param     cfg  the set of gpjpp configuration variables,
     *                 sometimes used to control the output.
     */
    abstract public void printOn(PrintStream os, GPVariables cfg);

    //ID codes used for all classes

    /**
     * An ID code used to identify null object references in 
     * gpjpp streams.
     */
    final protected static byte NULLID = 0;

    /**
     * An ID code used to identify GPObject references in 
     * gpjpp streams. This code is not written to streams
     * under normal circumstances, since GPObject is an
     * abstract class.
     */
    final protected static byte OBJECTID = 1;

    /**
     * An ID code used to identify 
     * <a href="gpjpp.GPContainer.html#_top_">GPContainer</a> 
     * references in gpjpp streams. This code is not written to streams
     * under normal circumstances, since GPContainer subclasses
     * provide their own ID codes.
     */
    final protected static byte CONTAINERID = 2;

    /**
     * An ID code used to identify 
     * <a href="gpjpp.GPNode.html#_top_">GPNode</a> references in 
     * gpjpp streams. A GPNode defines the identifying code,
     * number of arguments, and string representation for
     * every function and terminal in a genetic program.
     */
    final protected static byte NODEID = 3;

    /**
     * An ID code used to identify 
     * <a href="gpjpp.GPNodeSet.html#_top_">GPNodeSet</a> references in 
     * gpjpp streams. A GPNodeSet contains all the GPNode
     * values for a particular branch (main result-producing
     * or ADF) of a genetic program.
     */
    final protected static byte NODESETID = 4;

    /**
     * An ID code used to identify 
     * <a href="gpjpp.GPAdfNodeSet.html#_top_">GPAdfNodeSet</a> 
     * references in gpjpp streams. A GPAdfNodeSet contains all the 
     * GPNodeSet containers for a genetic program.
     */
    final protected static byte ADFNODESETID = 5;

    /**
     * An ID code used to identify 
     * <a href="gpjpp.GPVariables.html#_top_">GPVariables</a> 
     * references in gpjpp streams. A GPVariables contains all the 
     * settings that specify the behavior of a genetic programming test.
     * A user subclass of GPVariables is often, but not always,
     * defined to specify additional problem-specific variables.
     */
    final protected static byte VARIABLESID = 6;

    /**
     * An ID code used to identify 
     * <a href="gpjpp.GPGene.html#_top_">GPGene</a> references in 
     * gpjpp streams. A GPGene is one node of a genetic program
     * parse tree and includes a reference to a 
     * <a href="gpjpp.GPNode.html#_top_">GPNode</a> and
     * a container holding references to the arguments of the
     * node, if any. This ID code may appear hundreds of thousands
     * of times in a stream. A user subclass of GPGene is almost
     * always defined to implement a problem-specific fitness
     * calculation for each gene tree.
     */
    final protected static byte GENEID = 7;

    /**
     * An ID code used to identify 
     * <a href="gpjpp.GP.html#_top_">GP</a> references in 
     * gpjpp streams. GP is a container holding references to
     * a <a href="gpjpp.GPGene.html#_top_">GPGene</a> for each branch 
     * of the genetic program, including one for the main 
     * result-producing branch and zero or more
     * for the ADFs of the program. There is a GP for each member
     * of the population. A user subclass of GP is always defined
     * to implement a problem-specific fitness calculation for each 
     * genetic program.
     */
    final protected static byte GPID = 8;

    /**
     * An ID code used to identify 
     * <a href="gpjpp.GPPopulation.html#_top_">GPPopulation</a> 
     * references in gpjpp streams. GPPopulation is a container holding 
     * references to a GP for each individual in the population. 
     * A user subclass of GPPopulation is always defined to create GP 
     * subclass instances when the program needs them.
     */
    final protected static byte POPULATIONID = 9;

    /**
     * An ID code used to identify 
     * <a href="gpjpp.GPGenePrint.html#_top_">GPGenePrint</a> 
     * references in gpjpp streams. This code is not written to streams
     * under normal circumstances, and the GPGenePrint load and save
     * methods do nothing.
     */
    final protected static byte GENEPRINTID = 10;

    //for registering user subclasses

    /**
     * An ID code normally used to identify the user subclass of
     * GPGene.
     */
    final public static byte USERGENEID = 20;

    /**
     * An ID code normally used to identify the user subclass of
     * GP.
     */
    final public static byte USERGPID = 21;

    /**
     * An ID code normally used to identify the user subclass of
     * GPPopulation.
     */
    final public static byte USERPOPULATIONID = 22;

    /**
     * An ID code normally used to identify the user subclass of
     * GPVariables. For any stream classes besides those 
     * discussed here, use ID codes larger than USERVARIABLESID.
     */
    final public static byte USERVARIABLESID = 23;

    //arrays used to map ID codes to object classes in streams
    final private static int MAXIMUMCLASSNUM = 20;
    private static byte[] ids = new byte[MAXIMUMCLASSNUM];
    private static Class[] loadSaveClasses = new Class[MAXIMUMCLASSNUM];
    private static int registered = 0;

    /**
     * Returns index of existing registered class, -1 if not found.
     * For internal use.
     */
    protected static int findRegisteredClass(byte id) {

        for (int i = 0; i < registered; i++)
            if (ids[i] == id)
                return i;
        return -1;
    }

    /**
     * Creates an object of specified ID and returns it.
     * This method is called by appropriate load() methods in the
     * gpjpp class hierarchy.
     *
     * @param id  the registered ID code for the class type to create.
     * @return    an object of the specified type, created via its
     *            null constructor. Returns null if the ID code does
     *            not specify a registered type.
     * @exception java.lang.InstantiationException
     *              if an error occurs while calling new or the null
     *              constructor of the specified class.
     * @exception java.lang.IllegalAccessException
     *              if the specified class or its null constructor is
     *              not public.
     */
    public static GPObject createRegisteredClassObject(byte id)
        throws InstantiationException, IllegalAccessException {

        int index = findRegisteredClass(id);
        if (index < 0)
            return null;
        else
            return (GPObject)loadSaveClasses[index].newInstance();
    }

    /**
     * Registers a single class. For internal use.
     */
    protected static void registerClass(byte id, Class c) {

        if (findRegisteredClass(id) < 0) {
            //class not already registered
            if (registered == MAXIMUMCLASSNUM)
                //array full
                throw new ArrayStoreException();

            //save ID and class of object
            ids[registered] = id;
            loadSaveClasses[registered++] = c;
        }
    }

    /**
     * Registers the class of the specified object and 
     * all of its non-abstract superclasses up to but
     * not including GPObject.
     *
     * @param gpo  an instance of the class to register
     * @exception java.lang.IllegalAccessException
     *              if a superclass of the specified class or
     *              its null constructor is not public.
     */
    public static void registerClass(GPObject gpo)
        throws IllegalAccessException {

        byte id = gpo.isA();
        Class c = gpo.getClass();

        do {
            registerClass(id, c);
            c = c.getSuperclass();
            if (c == null)
                break;
            Object tmp;
            try {
                tmp = c.newInstance();
            } catch (InstantiationException e) {
                break;
            }
            id = ((GPObject)tmp).isA();
        } while (true);
    }
}
