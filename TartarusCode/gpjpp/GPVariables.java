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

/**
 * Holds all global configuration parameters for a genetic programming
 * run. All parameters have default values as described below.
 * The <a href="gpjpp.GPRun.html#_top_">GPRun</a> class also 
 * automatically reads a configuration file in Java property or 
 * Windows .ini format which can be used to set configuration 
 * parameters without recompiling an application.<p>
 *
 * It is common to create a user subclass of GPVariables in order to
 * add problem-specific configuration parameters. Examples include the
 * size and filename for an ant trail, or the number of address lines
 * that form the input to a boolean multiplexer. In other cases it
 * may be useful to add non-configuration variables to a subclass of
 * GPVariables. gpjpp passes a reference to the global variables to all 
 * methods that are likely to need them, including the 
 * <a href="gpjpp.GP.html#evaluate">evaluate()</a>
 * method of GP and all of the printOn() methods. Examples that
 * add to GPVariables in this way include the ant "automaton" that
 * responds to command functions and eats food, and the set of
 * data and address inputs for the multiplexer problem.
 *
 * @version 1.0
 */
public class GPVariables extends GPObject {

    //enumerated type for tree creation
    /**
     * A tree creation type that selects each non-root node with a 
     * 50:50 chance of becoming a function or terminal and that 
     * continues building every tree to 
     * <a href="gpjpp.GPVariables.html#MaximuDepthForCreation">
     * MaximumDepthForCreation</a>.
     */
    public final static int GPVARIABLE = 0;

    /**
     * A tree creation type that makes every node but those on the
     * bottom level a function and that builds every tree to
     * MaximumDepthForCreation.
     */
    public final static int GPGROW = 1;

    /**
     * A tree creation type that builds alternating trees using
     * the GPRAMPEDVARIABLE and GPRAMPEDGROW methods. The default
     * method.
     */
    public final static int GPRAMPEDHALF = 2;

    /**
     * A tree creation type that is like GPVARIABLE but starts with
     * a tree depth of two and increases the depth by one for each
     * tree until it reaches MaximumDepthForCreation, at which point
     * it cycles back to two. Note that if 
     * <a href="gpjpp.GPVariables.html#TestDiversity">TestDiversity</a>
     * is enabled many of the shallow trees are duplicates, so the 
     * distribution is skewed toward deeper trees than one might expect.
     */
    public final static int GPRAMPEDVARIABLE = 3;

    /**
     * A tree creation type that is like GPGROW but starts with
     * a tree depth of two and increases the depth by one for each
     * tree until it reaches MaximumDepthForCreation, at which point
     * it cycles back to two. Note that if TestDiversity is enabled
     * many of the shallow trees are duplicates, so the distribution
     * is skewed toward deeper trees than one might expect.
     */
    public final static int GPRAMPEDGROW = 4;

    /**
     * An array of strings used to detect and print the creation
     * type in configuration files and status reports. The values
     * are "Variable", "Grow", "RampedHalf", "RampedVariable", 
     * and "RampedGrow".
     */
    protected final static String[] CreationStr =
        {"Variable", "Grow", "RampedHalf",
         "RampedVariable", "RampedGrow"};

    //enumerated type for selection

    /**
     * A fitness-based selection type that uses a roulette
     * algorithm. The default method.
     *
     * @see gpjpp.GPPopulation#probabilisticSelection
     */
    public final static int GPPROBABILISTICSELECTION = 0;

    /**
     * A fitness-based selection type that uses a tournament
     * algorithm.
     *
     * @see gpjpp.GPPopulation#tournamentSelection
     */
    public final static int GPTOURNAMENTSELECTION = 1;

    /**
     * A fitness-based selection type that uses greedy
     * over-selection. When this type is enabled, demetic
     * grouping is always disabled.
     *
     * @see gpjpp.GPPopulation#greedySelection
     */
    public final static int GPGREEDYSELECTION = 2;

    /**
     * An array of strings used to detect and print the selection
     * type in configuration files and status reports. The values
     * are "Probabilistic", "Tournament", and "Greedy".
     */
    protected final static String[] SelectionStr =
        {"Probabilistic", "Tournament", "Greedy"};

    //variables controlling a run, with defaults
    //===================================================================

    /**
     * basename for output files
     */
    public String baseName = "tartarus";
    
    /**
     * The number of individuals (GP instances) in a population.
     * If SteadyState is false, two populations of this size are
     * created and filled. If SteadyState is true, only one
     * population is created, and weaker individuals are replaced by 
     * new individuals as they are created. Default 500.
     */
    public int PopulationSize = 500;

    /**
     * The maximum number of generations in a run. If the best
     * individual's standardized fitness drops below 
     * <a href="gpjpp.GPVariables.html#TerminationFitness">
     * TerminationFitness</a>, the run will terminate sooner. 
     * Default 51.
     */
    public int NumberOfGenerations = 51;

    /**
     * The strategy used to 
     * <a href="gpjpp.GPPopulation.html#create">create</a> 
     * generation 0. Default
     * <a href="gpjpp.GPVariables.html#GPRAMPEDHALF">GPRAMPEDHALF</a>.
     */
    public int CreationType = GPRAMPEDHALF;

    /**
     * The largest depth allowed for newly created trees.
     * Default 6. The minimum tree depth is always 2.
     */
    public int MaximumDepthForCreation = 6;

    /**
     * The largest depth allowed for trees after 
     * <a href="gpjpp.GP.html#cross">crossover</a>.
     * Default 17. Note that MaximumDepthForCrossover
     * must always be at least as large as MaximumDepthForCreation.
     */
    public int MaximumDepthForCrossover = 17;

    /**
     * The largest complexity (number of nodes) allowed in any
     * individual GP, including the main branch and all ADFs.
     * Although keeping a tight rein on complexity can speed
     * the algorithm and cut memory requirements, be cautious
     * not to limit the complexity too much. This can lead to
     * lack of diversity in the population and to wasted time
     * attempting to create and evolve new individuals. 
     * Default 200.
     *
     * @see gpjpp.GPPopulation#create
     */
    public int MaximumComplexity = 200;

    /**
     * The strategy used to select individuals with a probability
     * related to their fitness. Default
     * <a href="gpjpp.GPVariables.html#GPPROBABILISTICSELECTION">
     * GPPROBABILISTICSELECTION</a>.
     */
    public int SelectionType = GPPROBABILISTICSELECTION;

    /**
     * Number of randomly selected individuals forming a "tournament"
     * when SelectionType is 
     * <a href="gpjpp.GPVariables.html#GPTOURNAMENTSELECTION">
     * GPTOURNAMENTSELECTION</a>. Default 7.
     */
    public int TournamentSize = 7;

    /**
     * Percent probability that a crossover operation will occur
     * while <a href="gpjpp.GPPopulation.html#evolve">evolving</a>
     * a new generation. Default 90.0.
     */
    public double CrossoverProbability = 90.0;

    /**
     * Percent probability that a creation operation will occur
     * while <a href="gpjpp.GPPopulation.html#evolve">evolving</a>
     * a new generation. Default 0.0.
     */
    public double CreationProbability = 0.0;

    /**
     * Percent probability that 
     * <a href="gpjpp.GP.html#swapMutation">swap mutation</a> 
     * will occur for each individual added to a new generation. 
     * Default 0.0.
     */
    public double SwapMutationProbability = 0.0;

    /**
     * Percent probability that 
     * <a href="gpjpp.GP.html#shrinkMutation">shrink mutation</a> 
     * will occur for each individual added to a new generation. 
     * Default 0.0.
     */
    public double ShrinkMutationProbability = 0.0;

    /**
     * A run terminates if the best individual in any generation
     * has standardized fitness below this level. Default 0.0.
     * Because fitness must always be non-negative, the default
     * value will never cause a run to stop before NumberOfGenerations.
     */
    public double TerminationFitness = 0.0;

    /**
     * <a href="gpjpp.GPRun.html#_top_">GPRun</a> does multiple runs 
     * until this many of them terminate with the best individual's 
     * fitness below TerminationFitness. Set it to 0 if you want just 
     * a single run regardless of results. Default 5.
     */
    public int GoodRuns = 5;

    /**
     * A boolean that can be tested by user functions to determine
     * whether to add ADFs to the branch definition and whether to
     * evaluate them in fitness functions. Provides a convenient
     * way to test performance without recompiling. This 
     * configuration variable is not used by gpjpp itself. 
     * Default false.
     */
    public boolean UseADFs = false;

    /**
     * Determines whether gpjpp tests the diversity of genetic
     * populations. If true, gpjpp guarantees that there are no
     * duplicate individuals in generation 0 and then tracks
     * and reports the diversity of succeeding generations. If
     * false, gpjpp does not verify or measure diversity. 
     * Default true.
     */
    public boolean TestDiversity = true;

    /**
     * A boolean that can be tested by the user evaluate() function
     * to determine whether to incorporate complexity into the
     * fitness calculation. When this is done, solutions tend to
     * be parsimonious and average population complexity
     * tends to remain lower. Provides a convenient way to test
     * population statistics without recompiling. This 
     * configuration variable is not used by gpjpp itself. 
     * Default true.
     */
    public boolean ComplexityAffectsFitness = true;

    /**
     * The number of generations between checkpoints saved by
     * <a href="gpjpp.GPRun.html#_top_">GPRun</a>. If zero, 
     * checkpointing is not performed at all, in which case runs
     * cannot be restarted after a crash or system shutdown. If
     * one, a checkpoint is saved after every generation. 
     * Default 0.
     */
    public int CheckpointGens = 0;

    /**
     * Determines whether 
     * <a href="gpjpp.GPPopulation.html#demeticMigration">
     * demetic migration</a> is performed. If true,
     * the overall population is subdivided into groups of size
     * <a href="gpjpp.GPVariables.html#DemeSize">DemeSize</a>. 
     * These groups are isolated for the purpose of fitness-based 
     * selection. After all the demetic groups of a new
     * generation are created, one individual from each group is
     * fitness-selected and swapped with a fitness-selected individual
     * from the next group, with each swap controlled by the 
     * probability <a href="gpjpp.GPVariables.html#DemeticMigProbability">
     * DemeticMigProbability</a>. Default false.
     */

    /**
     * If set to true, every checkpoint generation, the best individual is tested.
     * The test method must be overriden.
    */
    public boolean TestCheckpoint = false;

    public boolean DemeticGrouping = false;

    /**
     * The number of individuals in each demetic group, if 
     * <a href="gpjpp.GPVariables.html#DemeticGrouping">DemeticGrouping</a>
     * is true. <a href="gpjpp.GPVariables.html#PopulationSize">
     * PopulationSize</a> must be an exact integer multiple of DemeSize. 
     * Default 100.
     */
    public int DemeSize = 100;

    /**
     * Percent probability that demetic migration will occur
     * for each demetic group, if 
     * <a href="gpjpp.GPVariables.html#DemeticGrouping">DemeticGrouping</a>
     * is true. Default 100.0.
     */
    public double DemeticMigProbability = 100.0;

    /**
     * Determines whether the best individual is automatically
     * added to the next generation, when SteadyState is false.
     * Default false.
     *
     * @see gpjpp.GPPopulation#generate
     */
    public boolean AddBestToNewPopulation = false;

    /**
     * Determines whether each new generation is created in
     * isolation from the previous generation (SteadyState = false) or
     * is created by replacing the weaker individuals one by one
     * (SteadyState = true). Steady state operation reduces
     * peak memory usage by a factor of two. Default false.
     *
     * @see gpjpp.GPPopulation#generate
     */
    public boolean SteadyState = false;

    /**
     * Determines whether a detail file is generated by GPRun.
     * The detail file includes the population index, heritage,
     * fitness, complexity, depth, and optionally the s-expression
     * for individuals after each generation. Default false.
     */
    public boolean PrintDetails = false;

    /**
     * Determines whether the detail file includes every individual
     * in the population (PrintPopulation = true) or just the best
     * and worst individuals in the population (PrintPopulation = 
     * false). Default false.
     */
    public boolean PrintPopulation = false;

    /**
     * Determines whether the detail file shows the s-expression for
     * each selected individual after each generation. Also determines
     * whether the statistics file includes the s-expression for
     * the best individual of each run. Default true.
     *
     * @see gpjpp.GPRun#showGeneration
     */
    public boolean PrintExpression = true;

    /**
     * Determines whether the statistics file shows a pseudo-graphic
     * tree for the best individual of each run. Also determines
     * whether gif files are created showing the best individual
     * of each run in true graphic format. Default true.
     *
     * @see gpjpp.GPRun#showFinalGeneration
     */
    public boolean PrintTree = true;

    /**
     * The font size in points used to draw the text in graphic
     * trees enabled by PrintTree. The typeface is always Courier,
     * a monospaced font. Default 12.
     */
    public int TreeFontSize = 12;

    //===================================================================

    /**
     * Public null constructor used to create a set of variables
     * with default values and also during stream loading.
     */
    public GPVariables() { /*gets default values above*/ }

    /**
     * A constructor that can be called to clone GPVariables. Normally
     * not used.
     */
    public GPVariables(GPVariables gpo) {
        PopulationSize = gpo.PopulationSize;
        NumberOfGenerations = gpo.NumberOfGenerations;
        CreationType = gpo.CreationType;
        MaximumDepthForCreation = gpo.MaximumDepthForCreation;
        MaximumDepthForCrossover = gpo.MaximumDepthForCrossover;
        MaximumComplexity = gpo.MaximumComplexity;
        SelectionType = gpo.SelectionType;
        TournamentSize = gpo.TournamentSize;
        DemeticGrouping = gpo.DemeticGrouping;
        DemeSize = gpo.DemeSize;
        CrossoverProbability = gpo.CrossoverProbability;
        CreationProbability = gpo.CreationProbability;
        SwapMutationProbability = gpo.SwapMutationProbability;
        ShrinkMutationProbability = gpo.ShrinkMutationProbability;
        DemeticMigProbability = gpo.DemeticMigProbability;
        TerminationFitness = gpo.TerminationFitness;
        GoodRuns = gpo.GoodRuns;
        AddBestToNewPopulation = gpo.AddBestToNewPopulation;
        SteadyState = gpo.SteadyState;
        CheckpointGens = gpo.CheckpointGens;
        TestCheckpoint = gpo.TestCheckpoint;
        PrintDetails = gpo.PrintDetails;
        PrintPopulation = gpo.PrintPopulation;
        PrintExpression = gpo.PrintExpression;
        PrintTree = gpo.PrintTree;
        TreeFontSize = gpo.TreeFontSize;
        UseADFs = gpo.UseADFs;
        TestDiversity = gpo.TestDiversity;
        ComplexityAffectsFitness = gpo.ComplexityAffectsFitness;
    }

    /**
     * Implements the Cloneable interface.
     * This clones GPVariables but is normally not used.
     *
     * @return the cloned object.
     */
    protected synchronized Object clone() { 
        return new GPVariables(this); 
    }

    /**
     * Returns a code identifying the class in a stream file.
     *
     * @return the ID code VARIABLESID.
     */
    public byte isA() { return VARIABLESID; }

    /**
     * Determines whether this set of variables equals another object. 
     * It returns true if obj is not null, is an instance of GPVariables
     * (or a descendant), and contains the same field values. 
     * This function is called when a checkpoint is loaded by GPRun, 
     * to determine whether the program and the checkpoint are 
     * consistent. One can think of various harmless changes 
     * made to GPVariables that would still allow a checkpoint to
     * continue (increasing the number of generations, for example),
     * but this is not allowed here.
     *
     * @param obj any Java object reference, including null.
     * @return true if this and obj are equivalent.
     */
    public boolean equals(Object obj) {

        if ((obj == null) || !(obj instanceof GPVariables))
            return false;
        GPVariables cfg = (GPVariables)obj;

        if (PopulationSize != cfg.PopulationSize) return false;
        if (NumberOfGenerations != cfg.NumberOfGenerations) return false;
        if (CreationType != cfg.CreationType) return false;
        if (MaximumDepthForCreation != cfg.MaximumDepthForCreation) return false;
        if (MaximumDepthForCrossover != cfg.MaximumDepthForCrossover) return false;
        if (MaximumComplexity != cfg.MaximumComplexity) return false;
        if (SelectionType != cfg.SelectionType) return false;
        if (TournamentSize != cfg.TournamentSize) return false;
        if (DemeticGrouping != cfg.DemeticGrouping) return false;
        if (DemeSize != cfg.DemeSize) return false;
        if (CrossoverProbability != cfg.CrossoverProbability) return false;
        if (CreationProbability != cfg.CreationProbability) return false;
        if (SwapMutationProbability != cfg.SwapMutationProbability) return false;
        if (ShrinkMutationProbability != cfg.ShrinkMutationProbability) return false;
        if (DemeticMigProbability != cfg.DemeticMigProbability) return false;
        if (TerminationFitness != cfg.TerminationFitness) return false;
        if (GoodRuns != cfg.GoodRuns) return false;
        if (AddBestToNewPopulation != cfg.AddBestToNewPopulation) return false;
        if (SteadyState != cfg.SteadyState) return false;
        if (CheckpointGens != cfg.CheckpointGens) return false;
        if (TestCheckpoint != cfg.TestCheckpoint) return false;
        if (PrintDetails != cfg.PrintDetails) return false;
        if (PrintPopulation != cfg.PrintPopulation) return false;
        if (PrintExpression != cfg.PrintExpression) return false;
        if (PrintTree != cfg.PrintTree) return false;
        if (TreeFontSize != cfg.TreeFontSize) return false;
        if (UseADFs != cfg.UseADFs) return false;
        if (TestDiversity != cfg.TestDiversity) return false;
        if (ComplexityAffectsFitness != cfg.ComplexityAffectsFitness) return false;

        return true;
    }

    /**
     * An internal routine used to convert a property string
     * to an int. If the numeric conversion fails, the default
     * value is used.
     */
    protected int getInt(Properties props, String proStr, int def) {

        int i;
        try {
            i = Integer.parseInt(props.getProperty(proStr));
        } catch (NumberFormatException e) {
            i = def;
        }
        return i;
    }

    /**
     * An internal routine used to convert a property string
     * to a boolean. Acceptable boolean values in a configuration
     * file are 0/non-zero, false/true, and FALSE/TRUE. If the
     * conversion fails, the default value is used.
     */
    protected boolean getBoolean(Properties props, String proStr, 
        boolean def) {

        String s = props.getProperty(proStr);
        if (s == null)
            return def;
        try {
            //check for non-zero=true and 0=false
            return (Integer.parseInt(s) != 0)? true : false;
        } catch (NumberFormatException e) {
            //check for TRUE/true and FALSE/false
            return Boolean.valueOf(s).booleanValue();
        }
    }

    /**
     * An internal routine used to convert a property string
     * to a double. If the numeric conversion fails, the default
     * value is used.
     */
    protected double getDouble(Properties props, String proStr, 
        double def) {

        double d;
        try {
            d = Double.valueOf(props.getProperty(proStr)).doubleValue();
        } catch (NumberFormatException e) {
            d = def;
        }
        return d;
    }

    protected void setBasename(String name) { baseName = name; }

    /**
     * An internal routine used to convert a property string
     * to an enumerated type with int values. Either the integer
     * value or the string representation is acceptable, and string
     * matching is not case sensitive. If the conversion fails, 
     * the default value is used.
     */
    protected int getEnumeratedType(
        Properties props, String[] EnumStr, String proStr, int def) {

        String s = props.getProperty(proStr);
        if (s == null)
            return def;
        try {
            //use integer value if specified
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            //check string values
            for (int i = 0; i < EnumStr.length; i++)
                if (s.equalsIgnoreCase(EnumStr[i]))
                    return i;
            return def;
        }
    }

    /**
     * An internal routine that calls getEnumerated type to convert 
     * a property string to the int value for a creation type.
     *
     * @see gpjpp.GPVariables#CreationStr
     */
    protected int getCreationType(
        Properties props, String proStr, int def) {

        return getEnumeratedType(props, CreationStr, proStr, def);
    }

    /**
     * An internal routine that calls getEnumerated type to convert 
     * a property string to the int value for a selection type.
     *
     * @see gpjpp.GPVariables#SelectionStr
     */
    protected int getSelectionType(
        Properties props, String proStr, int def) {

        return getEnumeratedType(props, SelectionStr, proStr, def);
    }

    /**
     * Loads the values from a Properties container (read from a
     * configuration file) into the GPVariables fields. If the
     * property strings are invalid, default values for the affected
     * fields remain unchanged. If props is null, nothing happens.
     *
     * @see gpjpp.GPVariables#getInt
     * @see gpjpp.GPVariables#getBoolean
     * @see gpjpp.GPVariables#getDouble
     * @see gpjpp.GPVariables#getCreationType
     * @see gpjpp.GPVariables#getSelectionType
     */
    public synchronized void load(Properties props) {

        if (props == null)
            return;

        PopulationSize =
            getInt(props, "PopulationSize", PopulationSize);
        DemeSize =
            getInt(props, "DemeSize", DemeSize);
        NumberOfGenerations =
            getInt(props, "NumberOfGenerations", NumberOfGenerations);
        CreationType =
            getCreationType(props, "CreationType", CreationType);
        MaximumDepthForCreation =
            getInt(props, "MaximumDepthForCreation", MaximumDepthForCreation);
        MaximumDepthForCrossover =
            getInt(props, "MaximumDepthForCrossover", MaximumDepthForCrossover);
        MaximumComplexity = 
            getInt(props, "MaximumComplexity", MaximumComplexity);
        SelectionType =
            getSelectionType(props, "SelectionType", SelectionType);
        TournamentSize =
            getInt(props, "TournamentSize", TournamentSize);
        DemeticGrouping =
            getBoolean(props, "DemeticGrouping", DemeticGrouping);
        CrossoverProbability =
            getDouble(props, "CrossoverProbability", CrossoverProbability);
        CreationProbability =
            getDouble(props, "CreationProbability", CreationProbability);
        SwapMutationProbability =
            getDouble(props, "SwapMutationProbability", SwapMutationProbability);
        ShrinkMutationProbability =
            getDouble(props, "ShrinkMutationProbability", ShrinkMutationProbability);
        DemeticMigProbability =
            getDouble(props, "DemeticMigProbability", DemeticMigProbability);
        TerminationFitness =
            getDouble(props, "TerminationFitness", TerminationFitness);
        GoodRuns = 
            getInt(props, "GoodRuns", GoodRuns);
        AddBestToNewPopulation =
            getBoolean(props, "AddBestToNewPopulation", AddBestToNewPopulation);
        SteadyState =
            getBoolean(props, "SteadyState", SteadyState);
        CheckpointGens =
            getInt(props, "CheckpointGens", CheckpointGens);
        TestCheckpoint =
            getBoolean(props, "TestCheckpoint", TestCheckpoint);
        PrintDetails = 
            getBoolean(props, "PrintDetails", PrintDetails);
        PrintPopulation =
            getBoolean(props, "PrintPopulation", PrintPopulation);
        PrintExpression =
            getBoolean(props, "PrintExpression", PrintExpression);
        PrintTree =
            getBoolean(props, "PrintTree", PrintTree);
        TreeFontSize =
            getInt(props, "TreeFontSize", TreeFontSize);
        UseADFs =
            getBoolean(props, "UseADFs", UseADFs);
        TestDiversity =
            getBoolean(props, "TestDiversity", TestDiversity);
        ComplexityAffectsFitness =
            getBoolean(props, "ComplexityAffectsFitness", ComplexityAffectsFitness);
    }

    /**
     * Loads the GPVariables from the specified stream.
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
     *              if the ID code read next in the stream doesn't 
     *              match the ID code returned by isA().
     */
    protected synchronized void load(DataInputStream is)
        throws ClassNotFoundException, IOException,
            InstantiationException, IllegalAccessException {

        //confirm that ID indicates GPVariables
        if (is.readByte() != isA())
            throw new RuntimeException("Invalid stream");

        PopulationSize = is.readInt();
        NumberOfGenerations = is.readInt();
        CrossoverProbability = is.readDouble();
        CreationProbability = is.readDouble();
        CreationType = is.readInt();
        MaximumDepthForCreation = is.readInt();
        MaximumDepthForCrossover = is.readInt();
        MaximumComplexity = is.readInt();
        SelectionType = is.readInt();
        TournamentSize = is.readInt();
        DemeticGrouping = is.readBoolean();
        DemeSize = is.readInt();
        DemeticMigProbability = is.readDouble();
        TerminationFitness = is.readDouble();
        GoodRuns = is.readInt();
        SwapMutationProbability = is.readDouble();
        ShrinkMutationProbability = is.readDouble();
        AddBestToNewPopulation = is.readBoolean();
        SteadyState = is.readBoolean();
        CheckpointGens = is.readInt();
        TestCheckpoint = is.readBoolean();
        PrintDetails = is.readBoolean();
        PrintPopulation = is.readBoolean();
        PrintExpression = is.readBoolean();
        PrintTree = is.readBoolean();
        TreeFontSize = is.readInt();
        UseADFs = is.readBoolean();
        TestDiversity = is.readBoolean();
        ComplexityAffectsFitness = is.readBoolean();
    }

    /**
     * Writes the GPVariables to the specified stream.
     *
     * @exception java.io.IOException
     *              if an error occurs while writing the stream.
     */
    protected void save(DataOutputStream os) throws IOException {

        os.writeByte(isA());
        os.writeInt(PopulationSize);
        os.writeInt(NumberOfGenerations);
        os.writeDouble(CrossoverProbability);
        os.writeDouble(CreationProbability);
        os.writeInt(CreationType);
        os.writeInt(MaximumDepthForCreation);
        os.writeInt(MaximumDepthForCrossover);
        os.writeInt(MaximumComplexity);
        os.writeInt(SelectionType);
        os.writeInt(TournamentSize);
        os.writeBoolean(DemeticGrouping);
        os.writeInt(DemeSize);
        os.writeDouble(DemeticMigProbability);
        os.writeDouble(TerminationFitness);
        os.writeInt(GoodRuns);
        os.writeDouble(SwapMutationProbability);
        os.writeDouble(ShrinkMutationProbability);
        os.writeBoolean(AddBestToNewPopulation);
        os.writeBoolean(SteadyState);
        os.writeInt(CheckpointGens);
        os.writeBoolean(TestCheckpoint);
        os.writeBoolean(PrintDetails);
        os.writeBoolean(PrintPopulation);
        os.writeBoolean(PrintExpression);
        os.writeBoolean(PrintTree);
        os.writeInt(TreeFontSize);
        os.writeBoolean(UseADFs);
        os.writeBoolean(TestDiversity);
        os.writeBoolean(ComplexityAffectsFitness);
    }

    /**
     * Writes the GPVariables fields in the format of a 
     * Properties text file.
     */
    public void printOn(PrintStream os, GPVariables cfg) {

        os.println("PopulationSize            = "+PopulationSize);
        os.println("NumberOfGenerations       = "+NumberOfGenerations);
        os.println("CrossoverProbability      = "+CrossoverProbability);
        os.println("CreationProbability       = "+CreationProbability);
        os.println("CreationType              = "+CreationStr[CreationType]);
        os.println("MaximumDepthForCreation   = "+MaximumDepthForCreation);
        os.println("MaximumDepthForCrossover  = "+MaximumDepthForCrossover);
        os.println("MaximumComplexity         = "+MaximumComplexity);
        os.println("SelectionType             = "+SelectionStr[SelectionType]);
        os.println("TournamentSize            = "+TournamentSize);
        os.println("DemeSize                  = "+DemeSize);
        os.println("DemeticMigProbability     = "+DemeticMigProbability);
        os.println("SwapMutationProbability   = "+SwapMutationProbability);
        os.println("ShrinkMutationProbability = "+ShrinkMutationProbability);
        os.println("TerminationFitness        = "+TerminationFitness);
        os.println("GoodRuns                  = "+GoodRuns);
        os.println("DemeticGrouping           = "+DemeticGrouping);
        os.println("AddBestToNewPopulation    = "+AddBestToNewPopulation);
        os.println("SteadyState               = "+SteadyState);
        os.println("PrintDetails              = "+PrintDetails);
        os.println("PrintPopulation           = "+PrintPopulation);
        os.println("PrintExpression           = "+PrintExpression);
        os.println("PrintTree                 = "+PrintTree);
        os.println("TreeFontSize              = "+TreeFontSize);
        os.println("UseADFs                   = "+UseADFs);
        os.println("TestDiversity             = "+TestDiversity);
        os.println("ComplexityAffectsFitness  = "+ComplexityAffectsFitness);
        os.println("CheckpointGens            = "+CheckpointGens);
        os.println("TestCheckpoint            = "+TestCheckpoint);
    }

}
