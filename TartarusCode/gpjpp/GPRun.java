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
import java.util.Properties;
import java.util.Random;

/**
 * Abstract class that encapsulates the details of running a
 * genetic programming test in a Java console application.<p>
 *
 * The user must override two methods of this class: createNodeSet(),
 * which defines the branch structure and the node types used in
 * each branch of each tree; and createPopulation(), which creates
 * an instance of a user-defined subclass of GPPopulation.<p>
 *
 * It is common to override another method of this class:
 * createVariables(), which creates an instance of GPVariables with
 * the default configuration parameters for the run. Frequently you
 * may need to add problem-specific configuration variables (the size
 * of an ant trail, e.g.), which is best done by subclassing
 * GPVariables and overriding createVariables() to create an instance
 * of your own class.<p>
 *
 * GPRun then handles numerous details of managing a test run. These
 * include:<p>
 *
 * 1. Initializing the random number generator used by gpjpp.<p>
 *
 * 2. Reading a configuration file so that most properties of the
 *    test run can be adjusted without recompiling. Also creates
 *    a default configuration file if the named one isn't found.<p>
 *
 * 3. Creating output files for run statistics and detailed
 *    population reporting.<p>
 *
 * 4. Registering all necessary classes with the stream manager.<p>
 *
 * 5. Trapping all exceptions to report a detailed stack trace
 *    in case of error.<p>
 *
 * 6. Loading a previous checkpoint stream if found, or creating
 *    an initial population of individuals.<p>
 *
 * 7. Displaying status output to the console window and also
 *    printing configurable output to report files.<p>
 *
 * 8. Streaming a checkpoint file after a configurable number of
 *    generations in case a long run needs to be interrupted.<p>
 *
 * 9. Running a configurable number of generations and terminating
 *    the run when a configurable fitness target is reached.<p>
 *
 * 10.Writing configurable reports on the best individual at the
 *    end of a run, including graphic images of its tree structure
 *    and possibly its fitness performance (such as the trail of
 *    an ant or a lawnmower).<p>
 *
 * 11.Running multiple runs until a configurable number of good
 *    runs is found, and timing each run's performance.<p>
 *
 * GPRun is divided into a number of reasonably small routines so
 * that additional aspects of its behavior can be customized by
 * creating a subclass. Nevertheless, it is not suitable for
 * writing applets or other graphics-intensive Java applications. 
 * These could be written by using the lower level classes of gpjpp 
 * directly, since these classes enforce no user interface of their own.<p>
 *
 * @version 1.0
 */
public abstract class GPRun {

    /**
     * An ID code written at the end of checkpoint streams. This
     * is primarily useful for checking to see whether a stream
     * was fully flushed in the event of a machine crash.
     */
    protected static final int RUNENDID = 0x87654321;

    /**
     * The size in bytes of the output buffer used for PrintStreams.
     * These streams are flushed at the end of every line, so
     * there is little point in using a larger buffer.
     */
    protected static int outBufSize = 128;  //buffer for GPPrintStreams

    /**
     * The size in bytes of the input or output buffer used for a
     * DataInputStream or a DataOutputStream during checkpoint
     * loading or saving. A reasonably large buffer provides better
     * performance, since these streams are composed of very many
     * small data items.
     */
    protected static int stmBufSize = 4096; //buffer for DataIOStreams

    /**
     * The base name for all input and output files used in the
     * run. To get the configuration file name, ".ini" is appended
     * to baseName. To get the statistics file name, ".stc" is
     * appended to baseName. To get the detailed population file
     * name, ".det" is appended to baseName. To get the checkpoint
     * file name, ".stm" is appended to baseName. This field is
     * passed as a parameter to the GPRun constructor.
     */
    protected String baseName;

    /**
     * The configuration variables used for the run. The instance is
     * created by the createVariables() abstract method. Its values
     * are assigned from the configuration file (baseName+".ini") if
     * found.
     */
    protected GPVariables cfg;

    /**
     * The branch and node definitions for all the GPs used during
     * the run.
     */
    protected GPAdfNodeSet adfNs;

    /**
     * The PrintStream used for the detailed population output file.
     * If cfg.PrintDetails is false, this field is null and the
     * report file is not created.
     */
    protected GPPrintStream dout;

    /**
     * The PrintStream used for statistics and summary reporting.
     * This file (baseName+".stc") is always created. It includes
     * a list of the configuration parameters, a list of the
     * branch and node definitions, the printStatistics output for
     * each generation, timing per run and per generation,
     * and a report on the best individual at the
     * end of each run. In case a run is restarted from a checkpoint,
     * additional output is appended to the existing statistics file
     * if found.
     */
    protected GPPrintStream sout;

    /**
     * The main population, instantiated in GPRun() and reused
     * repeatedly in run().
     */
    protected GPPopulation pop;

    /**
     * The secondary population. If cfg.SteadyState is true, newPop
     * is not used and remains null. Otherwise each new generation
     * is created in the run() method by calling pop.generate(newPop).
     * Then the newPop and pop references are swapped to proceed with
     * the next generation.
     */
    protected GPPopulation newPop;

    /**
     * The current generation number. It starts with 0 for the
     * initial population and increments to cfg.NumberOfGenerations.
     * When restarted from a checkpoint, the run starts again from
     * the last generation that was checkpointed.
     */
    protected int curGen;

    /**
     * The current run number.
     */
    protected int curRun;

    /**
     * The number of good runs found so far. A good run is one
     * where the best individual's fitness becomes less than
     * cfg.TerminationFitness.
     */
    protected int goodRuns;

    /**
     * The number of generations until the next checkpoint will be stored.
     * If cfg.CheckpointGens is 0, this field is not used. Otherwise
     * it is initialized to cfg.CheckpointGens and decremented after
     * each generation.
     */
    protected int cntGen;

    //==== abstract methods ====

    /**
     * Returns a properly initialized instance of GPAdfNodeSet,
     * containing the branch and node definitions for this problem.
     * You must override this method to create your own set of
     * functions and terminals. Your version can create a different
     * node set depending on whether cfg.UseADFs is true or false.
     * createNodeSet() can also refer to other user-defined
     * configuration parameters when cfg is a subclass of GPVariables
     * to further customize the node set to the problem configuration.
     */
    abstract protected GPAdfNodeSet createNodeSet(GPVariables cfg);

    /**
     * You must override this method to return a new instance
     * of a user subclass of GPPopulation. The user subclass must
     * at least override the createGP() method of GPPopulation
     * to create instances of a user subclass of GP. If the
     * configuration does not specify a steady state population,
     * createPopulation() is called twice; otherwise it is called
     * just once regardless of the number of generations or runs.
     */
    abstract protected GPPopulation createPopulation(GPVariables cfg,
        GPAdfNodeSet adfNs);

    //==== remaining methods are not abstract ====

    /**
     * This constructor allocates the basic data structures and
     * prepares for a run. GPRun() traps all exceptions. If it
     * catches one, it dumps a stack trace and calls System.exit(1).
     *
     * @param baseName  specifies the base file name for
     *          the run's configuration file, output files,
     *          and stream file.
     * @param createDefaultIni if true, and if baseName+".ini"
     *          is not found, GPRun creates a configuration file
     *          holding the default configuration values.
     */
    public GPRun(String baseName, boolean createDefaultIni) {

        //trap all exceptions to report details
        try {
            if (baseName.toLowerCase().endsWith(".ini"))
                //strip .ini from baseName (common error by this author)
                this.baseName = baseName.substring(0, baseName.length()-4);
            else
                this.baseName = baseName;

            //create random number generator used by algorithm
            GPRandom.setGenerator(createRandomGenerator());

            //create configuration class
            cfg = createVariables();
	    cfg.setBasename(baseName);

            //read configuration parameters
            readIni(this.baseName+".ini", createDefaultIni);

            //create the function and terminal descriptions
            adfNs = createNodeSet(cfg);

            //make population instances; individuals aren't created until run()
            pop = createPopulation(cfg, adfNs);
            if (!cfg.SteadyState)
                newPop = createPopulation(cfg, adfNs);

            //open output files
            boolean append = (getCheckpointFile() != null);
            sout = createOrAppend(getStatisticsName(), append);
            if (cfg.PrintDetails)
                dout = createOrAppend(getDetailName(), append);
            else
                dout = null;

            //register all user and gpjpp classes for streaming
            //also creates node type index used to stream genes
            registerAllClasses();

        } catch (Exception e) {
            //provide details on any exceptions thrown and halt
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * The main method of GPRun creates and evolves populations,
     * writes reports, loads and saves checkpoint files, and does
     * multiple runs until a configurable number of good ones is
     * found. run() traps all exceptions. If it catches one,
     * it dumps a stack trace and calls System.exit(1). run() calls
     * a number of small methods for reporting output to the console
     * and to files; these can be overridden to make small changes
     * to its behavior. However, most changes are accomplished by
     * modifying the configuration file that controls the run.
     */
    protected void run() {

        //trap all exceptions to report details
        try {
            curGen = 0;
            curRun = 1;
            goodRuns = 0;
            cntGen = cfg.CheckpointGens;

            //load checkpoint if available
            boolean loadedCheckpoint = loadCheckpoint();
            if (!loadedCheckpoint) {
                showConfiguration();
            }

            //do runs until some that reach terminating fitness are found
            do {
                //display run number
                showRunNumber(curRun, goodRuns);

                //time the run
                long timeStart = System.currentTimeMillis();

                if (loadedCheckpoint)
                    //create population next run through
                    loadedCheckpoint = false;
                else {
                    //create initial population
                    showCreation(true);
                    pop.create();
                    showCreation(false);
                }

                //show initial generation
                showGeneration(true, curGen, false);

                //time the generations
                long timeGenStart = System.currentTimeMillis();
                int gens = 0;
                boolean goodRun = false;

                //loop through the generations
                while (curGen < cfg.NumberOfGenerations) {
                    //create next generation
                    curGen++;
                    gens++;
                    pop.generate(newPop);
                    if (!cfg.SteadyState) {
                        //swap the pops
                        GPPopulation tmp = pop;
                        pop = newPop;
                        newPop = tmp;
                        //ensure no references remain to obsolete GPs
                        //newPop.clear();
                    }

                    //checkpoint this generation's population
                    boolean savedCheckpoint = saveCheckpoint();

                    //show this generation
                    showGeneration(false, curGen, savedCheckpoint);

                    goodRun = (pop.bestFitness < cfg.TerminationFitness);
                    if (goodRun) {
                        //break if terminating fitness found
                        goodRuns++;
                        break;
                    }
                }

                //read time for generation rate
                long timeGenStop = System.currentTimeMillis();

                //report on final generation
                showFinalGeneration(curGen, goodRun);

                //compute and display run timing
                long timeStop = System.currentTimeMillis();
                double secsTotal = (timeStop-timeStart)/1000.0;
                double secsPerGen;
                if (gens > 0)
                    secsPerGen = (timeGenStop-timeGenStart)/(1000.0*gens);
                else
                    secsPerGen = 0.0;
                showTiming(secsTotal, secsPerGen);

                //erase final stream file of run
                if (cfg.CheckpointGens > 0) {
                    (new File(getCheckpointName())).delete();
                }

                //update counters for next run
                curRun++;
                curGen = 0;
                cntGen = cfg.CheckpointGens;

            } while (goodRuns < cfg.GoodRuns);

            //close output files
            sout.close();
            if (cfg.PrintDetails)
                dout.close();

        } catch (Exception e) {
            System.err.println();
            System.err.println();
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Returns a valid instance of GPVariables. Override to create
     * an instance of a subclass of GPVariables that contains
     * problem-specific variables as well.
     */
    protected GPVariables createVariables() {
        return new GPVariables();
    }

    /**
     * Returns a valid instance of Random used to generate
     * all random numbers throughout the package. The default
     * implementation uses the current time to seed the
     * generator.
     */
    protected Random createRandomGenerator() {
        return new Random();
    }

    /**
     * Reads the specified file to get run configuration variables.
     * Calls GPProperties.load() to interpret the file. If the
     * file is not found and createDefaultIni is true, readIni()
     * creates a new file and writes the current values of cfg
     * to this file using GPVariables.printOn.
     *
     * @see gpjpp.GPProperties#load
     * @see gpjpp.GPVariables#printOn
     */
    protected void readIni(String iniName, boolean createDefaultIni)
        throws IOException {

        try {
            FileInputStream propf = new FileInputStream(iniName);
            GPProperties prop = new GPProperties();
            prop.load(propf);
            propf.close();
            cfg.load(prop);
        } catch (FileNotFoundException e) {
            //.ini file not found, use defaults
            if (createDefaultIni)
                try {
                    //create ini file for next time
                    dout = new GPPrintStream(
                        new FileOutputStream(iniName), true);
                    cfg.printOn(dout, cfg);
                    dout.close();
                } catch (Exception f) {
                    throw new Error("Can't create "+iniName);
                }
        }
    }

    /**
     * Creates or appends to a specified file and returns a valid
     * reference to a GPPrintStream for that file. If append is true and
     * fname exists, createOrAppend() renames fname to "tmp.tmp",
     * opens tmp.tmp for reading, creates fname anew, copies tmp.tmp
     * to fname, deletes tmp.tmp, and leaves fname open for writing.
     * Otherwise, createOrAppend() creates fname, overwriting any
     * existing file of that name without warning.
     */
    protected GPPrintStream createOrAppend(String fname, boolean append)
        throws IOException {

        FileOutputStream os = null;

        if (append) {
            File f = new File(fname);
            if (f.exists()) {
                //delete temporary file if it exists
                File nf = new File("tmp.tmp");
                if (nf.exists())
                    nf.delete();

                //rename fname to temp name
                f.renameTo(nf);

                //open old file for input
                FileInputStream is = new FileInputStream(nf);

                //create output file
                os = new FileOutputStream(fname);

                //copy input to output
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) != -1)
                    os.write(buf, 0, len);

                //close and delete old file
                is.close();
                nf.delete();
            }
        }

        if (os == null)
            //create new file
            os = new FileOutputStream(fname);

        return new GPPrintStream(
            new BufferedOutputStream(os, outBufSize), true);
    }

    /**
     * Prints a string to System.out and also to sout.
     */
    protected void echoPrint(String dispStr) {
        System.out.println(dispStr);
        sout.println(dispStr);
    }

    /**
     * Prints the configuration variables and the node definitions
     * to sout.
     */
    protected void showConfiguration() {

        //print configuration parameters
        cfg.printOn(sout, cfg);
        sout.println();

        //print tree descriptions
        adfNs.printOn(sout, cfg);
        sout.println();
    }

    /**
     * Prints the current run number and number of good runs
     * so far to System.out, sout, and dout.
     */
    protected void showRunNumber(int curRun, int goodRuns) {
        echoPrint("");
        String dispStr =
            "Run number "+curRun+" (good runs "+goodRuns+")";
        echoPrint(dispStr);
        if (cfg.PrintDetails)
            dout.println(dispStr);
    }

    /**
     * Prints a status message to System.out while the initial
     * population is being created. When the population is done,
     * finishes the status message and writes to System.out and
     * sout the number of individuals that were rejected
     * because they were too complex or were duplicates.
     */
    protected void showCreation(boolean preCreation) {

        if (preCreation) {
            System.out.print("Creating initial population... ");
        } else {
            System.out.println("Ok");

            echoPrint("Too complex "+pop.attemptedComplexCount);
            if (cfg.TestDiversity)
                echoPrint("Duplicate "+pop.attemptedDupCount);
            sout.println();
        }
    }

    /**
     * Returns the symbols displayed in the statistical display
     * when a checkpoint is finished or skipped. The default
     * implementation returns 'c' for a checkpoint and ' ' for
     * no checkpoint.
     */
    protected char checkChar(boolean savedCheckpoint) {
        return savedCheckpoint? 'c' : ' ';
    }

    /**
     * Prints information to System.out and to sout about the
     * generation just completed. GPPopulation.printStatisticsLegend()
     * is called if showLegend is true. GPPopulation.printStatistics()
     * is always called. GPPopulation.printDetails() is called only
     * if cfg.PrintDetails is true and it prints to dout.
     */
    protected void showGeneration(boolean showLegend,
        int curGen, boolean savedCheckpoint) {

        if (showLegend) {
            pop.printStatisticsLegend(System.out);
            pop.printStatisticsLegend(sout);
        }

        pop.printStatistics(curGen, checkChar(savedCheckpoint), System.out);
        pop.printStatistics(curGen, checkChar(savedCheckpoint), sout);

        if (cfg.PrintDetails) {
            //print details about this generation
            pop.printDetails(curGen,
                cfg.PrintPopulation,    //showAll
                true,                   //showBest
                true,                   //showWorst
                cfg.PrintExpression,    //showExpression
                false,                  //showTree
                dout);
        }
    }

    /**
     * Prints information about the final generation of a run.
     * Calls GPPopulation.printDetails to write information about the 
     * run's best individual to sout. If cfg.PrintTree is true,
     * a GPDrawing drawing surface is created and GP.drawOn is
     * called to draw the best individual's trees to gif files.
     * These files are named as follows:
     *     <code>baseName+curRun+branchName+".gif"</code>
     * where branchName is "RPB" for the result-producing branch
     * and "ADFn" for the ADF branches, or "" for single-branch trees.<p>
     *
     * For some Java implementations, Microsoft J++ in particular,
     * the console window loses focus temporarily while the
     * off-screen drawing window is active. Focus is returned to
     * the previous window once drawing is complete. The same
     * behavior is not seen for the Sun virtual machine running
     * under Windows 95.
     *
     * @param curGen  the final generation number, which
     *          can be cfg.NumberOfGenerations or less.
     * @param goodrun  true if the best individual's fitness was less
     *          than cfg.TerminationFitness.
     *
     * @exception java.io.IOException
     *              if an I/O error occurs while writing gif files.
     */
    protected void showFinalGeneration(int curGen, boolean goodRun)
        throws IOException {

        //print details about best individual of run
        sout.println();
        pop.printDetails(curGen,
            false,                  //showAll
            true,                   //showBest
            false,                  //showWorst
            cfg.PrintExpression,    //showExpression
            cfg.PrintTree,          //showTree
            sout);

        if (cfg.PrintTree) {
            System.out.print("Drawing best individual... ");

            //get a drawing surface (may grab focus from console window)
            GPDrawing ods = new GPDrawing();

            //make gif files of the best individual found
            //user can override GP.drawOn to print a trail or
            //  other problem-specific graphic too
            ((GP)pop.get(pop.bestOfPopulation)).drawOn(
                ods, "data/"+baseName+curRun, cfg);

            //return system resources (return focus to console window)
            ods.dispose();

            System.out.println("Ok");
            }
    }

    /**
     * Print to System.out and to sout the total elapsed seconds for
     * a run and also the number of seconds to process each generation.
     * The latter figure does not include time spent creating the
     * initial population or printing details about the final
     * generation.
     */
    protected void showTiming(double elapsedSecs,
        double secsPerGen) {

        echoPrint("Run time "+
            GPPopulation.trimFormatDouble(elapsedSecs, 9, 2)+" seconds, "+
            GPPopulation.trimFormatDouble(secsPerGen, 9, 2)+" seconds/generation");
        sout.println();
    }

    /**
     * Registers with the GPObject stream manager all classes
     * needed for checkpointing. Does nothing if cfg.CheckpointGens
     * is 0 or less. Also creates the node index used to store the
     * multitudinous genes efficiently on the stream.
     *
     * @exception java.lang.IllegalAccessException
     *              if any registered class or null constructor is
     *              not public.
     *
     * @see gpjpp.GPGene#createNodeIndex
     */
    protected void registerAllClasses() throws IllegalAccessException {

        if (cfg.CheckpointGens > 0)  {
            //create the list of all node types used to stream genes
            GPGene.createNodeIndex(adfNs);

            //register user variable class and its superclasses
            GPObject.registerClass(cfg);

            //register overall ADF node set, a branch node set, and a node
            GPObject.registerClass(adfNs);
            GPNodeSet tmpNs = (GPNodeSet)adfNs.get(0);
            GPObject.registerClass(tmpNs);
            GPNode tmpN = (GPNode)tmpNs.get(0);
            GPObject.registerClass(tmpN);

            //create a temporary GP of user type and register it
            GP tmpGP = pop.createGP(adfNs.containerSize());
            GPObject.registerClass(tmpGP);

            //create a temporary gene of user type and register it
            GPObject.registerClass(tmpGP.createGene(tmpN));

            //register user population class and its superclasses
            GPObject.registerClass(pop);
        }
    }

    /**
     * Returns the checkpoint file name, baseName+".stm" by default.
     */
    protected String getCheckpointName() { return "data/"+baseName+".stm"; }

    /**
     * Returns the statistics file name, baseName+".stc" by default.
     */
    protected String getStatisticsName() { return "data/"+baseName+".stc"; }

    /**
     * Returns the detail file name, data/baseName.det by default.
     */
    protected String getDetailName() { return "data/"+baseName+".det"; }

    /**
     * Returns an instantiated File of the stream file if
     * checkpointing is enabled and the stream file exists. Otherwise
     * returns null.
     */
    protected File getCheckpointFile() {

        if (cfg.CheckpointGens > 0) {
            File f = new File(getCheckpointName());
            if (f.exists())
                return f;
        }
        return null;
    }

    /**
     * Loads a checkpoint if cfg.CheckpointGens is greater than
     * zero and the checkpoint file is found. For a successful
     * load, the configuration variables and node definitions
     * in this run must exactly match those in force when the
     * checkpoint file was created. loadCheckpoint() generates
     * a RuntimeException with an informative message if this
     * is not the case. You can either bring the current configuration
     * in line with the original run or delete the stream file
     * to start over.<p>
     *
     * After the checkpoint is loaded, the run picks up exactly
     * where it left off when the checkpoint was stored. The only
     * difference is that the random number sequence used in the new
     * run won't match any results obtained after the original
     * checkpoint was stored. (The random number seed is not stored
     * on the stream.)
     *
     * @return true if a checkpoint was loaded; otherwise false.
     *
     * @exception java.io.FileNotFoundException
     *              if the stream file is not found (shouldn't happen).
     * @exception java.io.IOException
     *              if an I/O error occurs while reading the stream.
     * @exception java.lang.RuntimeException
     *              if the current configuration doesn't match the
     *              checkpoint's.
     */
    protected boolean loadCheckpoint()
        throws FileNotFoundException, IOException {

        File f = getCheckpointFile();
        if (f != null) {
            //open stream file for reading
            DataInputStream is =
                new DataInputStream(
                    new BufferedInputStream(
                        new FileInputStream(f), stmBufSize));

            System.out.println();
            System.out.print("Loading checkpoint population... ");

            //try to load the stream
            boolean streamOk = true;
            String msg = "";
            try {
                load(is);
            } catch (ClassNotFoundException e) {
                streamOk = false;
                msg = e.getMessage();
            } catch (InstantiationException e) {
                streamOk = false;
                msg = e.getMessage();
            } catch (IllegalAccessException e) {
                streamOk = false;
                msg = e.getMessage();
            } catch (IOException e) {
                streamOk = false;
                msg = e.getMessage();
            }

            is.close();

            if (streamOk) {
                System.out.println("Ok");

                //put a comment in the statistics file
                sout.println();
                sout.println("Restarted from checkpoint");

                return true;

            } else {
                System.out.println("Error");
                System.out.println(msg);
                System.out.print("Erase stream file or correct configuration");
                throw new RuntimeException("Stream loading error");
            }
        }
        return false;
    }

    /**
     * Saves a checkpoint to disk if cfg.CheckpointGens is greater than
     * zero and that many generations has passed since the last
     * checkpoint. If cfg.TestCheckpoint is true, then run the best of the
     * population on the test (must be overridden) Returns true if a checkpoint was saved.
     */
    protected boolean saveCheckpoint() throws IOException {

        if (cfg.CheckpointGens > 0) {
            cntGen--;
            if (cntGen <= 0) {
                
                if (cfg.TestCheckpoint) {
                    pop.testBest();
                }

                //create or overwrite stream file
                DataOutputStream os =
                    new DataOutputStream(
                        new BufferedOutputStream(
                            new FileOutputStream(getCheckpointName()),
                                stmBufSize));
                save(os);
                os.close();
                cntGen = cfg.CheckpointGens;

                //flush statistics file too
                //sout.flush();
                //sout.close();
                //sout = createOrAppend(getStatisticsName(), true);

                //data file often so big it's a waste of time to flush

                return true;
            }
        }
        return false;
    }

    /**
     * Loads a checkpoint file from the specified stream. The stream
     * contains an image of the configuration variables, the node
     * set, the current run and generation numbers, the population
     * of individuals, and a terminator code. If the configuration
     * variables or node set don't match those of the current run,
     * load() throws an InstantiationException which is caught by
     * loadCheckpoint().
     *
     * @exception java.lang.ClassNotFoundException
     *              if the class indicated by the stream's ID code
     *              is not registered with GPObject.
     * @exception java.lang.InstantiationException
     *              if an error occurs while calling new or the null
     *              constructor of the specified class.
     * @exception java.lang.IllegalAccessException
     *              if a class or its null constructor is not public.
     * @exception java.io.IOException
     *              if an error occurs while reading the stream.
     */
    protected void load(DataInputStream is)
        throws ClassNotFoundException, IOException,
            InstantiationException, IllegalAccessException {

        //load configuration variables stored on stream
        GPVariables tmpCfg =
            (GPVariables)GPObject.createRegisteredClassObject(cfg.isA());
        tmpCfg.load(is);

        //confirm it matches current configuration
        if (!tmpCfg.equals(cfg))
            throw new InstantiationException(
                "Checkpoint configuration doesn't match current ini file");

        //load node set stored on stream
        GPAdfNodeSet tmpAdfNs =
            (GPAdfNodeSet)GPObject.createRegisteredClassObject(adfNs.isA());
        tmpAdfNs.load(is);

        //confirm it matches current node set
        if (!tmpAdfNs.equals(adfNs))
            throw new InstantiationException(
                "Checkpoint node set doesn't match program node set");

        //load run state
        curRun = is.readInt();
        goodRuns = is.readInt();
        curGen = is.readInt();

        //load population
        pop.load(is);

        //check terminator ID
        if (is.readInt() != RUNENDID)
            throw new InstantiationException("Stream file is corrupt");
    }

    /**
     * Saves a checkpoint to the specified stream.
     *
     * @exception java.io.IOException
     *              if an error occurs while writing the stream.
     *
     * @see gpjpp.GPRun#load
     */
    protected void save(DataOutputStream os) throws IOException {
        //save configuration variables
        cfg.save(os);

        //save node set
        adfNs.save(os);

        //save run state
        os.writeInt(curRun);
        os.writeInt(goodRuns);
        os.writeInt(curGen);

        //save population
        pop.save(os);

        //write terminator ID
        //  (mostly used for visual inspection of stream file
        //   to confirm that whole stream was flushed)
        os.writeInt(RUNENDID);
    }

}
