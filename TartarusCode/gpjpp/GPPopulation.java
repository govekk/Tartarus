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
import java.util.Hashtable;

/**
 * Stores a fixed-size array of genetic programs of type 
 * <a href="gpjpp.GP.html#_top_">GP</a>.
 * GPPopulation has methods for evolving this population by
 * fitness-based selection and reproduction, crossover, mutation,
 * and demetic migration. It also has methods for printing
 * the status of the population at various degrees of detail.
 * And it includes a Hashtable-based data structure for measuring
 * and enforcing diversity of the individuals in the population.<p>
 *
 * GPPopulation includes references to the 
 * <a href="gpjpp.GPVariables.html#_top_">GPVariables</a> configuration
 * of the run and the 
 * <a href="gpjpp.GPAdfNodeSet.html#_top_">GPAdfNodeSet</a> set of node 
 * types. These references are passed as parameters to lower level 
 * classes when needed.<p>
 *
 * GPPopulation has several public data fields that can be used
 * by the calling program to obtain the status of the population. These
 * include bestOfPopulation (the index of the individual with the
 * best fitness), worstOfPopulation, attemptedDupCount (the number
 * of duplicate individuals detected but rejected when creating the
 * initial population), and others.<p>
 *
 * Although GPPopulation represents a single population of individuals,
 * it is designed to work with either a single population (steady
 * state evolution) or two populations (generational evolution). This
 * behavior is implemented in the 
 * <a href="gpjpp.GPPopulation.html#generate">generate()</a> method.<p>
 *
 * The user must always create a subclass of GPPopulation for use 
 * in solving a particular problem. This subclass must override 
 * the <a href="gpjpp.GPPopulation.html#createGP">createGP()</a> method, 
 * which creates a GP of user type. That GP subclass must in turn 
 * override the <a href="gpjpp.GP.html#evaluate">evaluate()</a> method 
 * to compute fitness for the target problem.<p>
 *
 * Otherwise, the typical details of using GPPopulation are 
 * encapsulated in the <a href="gpjpp.GPRun.html#_top_">GPRun</a> class.
 *
 * @version 1.0
 */
public class GPPopulation extends GPContainer {

    /**
     * Specifies the minimum tree depth for newly created individuals.
     * Default value 2.
     */
    protected static int minTreeDepth = 2;

    /**
     * The sum of the adjusted fitness over an entire range.
     * Used for probabilistic and greedy selection only.
     */
    protected double sumAdjFitness;

    /**
     * The cutoff fitness for an individual to get into group I.
     * Used for greedy selection only.
     */
    protected double cutoffAdjFitness;

    /**
     * The sum of the adjusted fitness for group I individuals.
     * Used for greedy selection only.
     */
    protected double sumG1AdjFitness;

    /**
     * The sum of the adjusted fitness for group II individuals.
     * Used for greedy selection only.
     */
    protected double sumG2AdjFitness;

    /**
     * The sum of the standardized fitness over an entire range.
     */
    protected double sumFitness;

    /**
     * A reference to the global configuration variables for the run.
     */
    protected GPVariables cfg;

    /**
     * A reference to the functions and terminals for the run.
     */
    protected GPAdfNodeSet adfNs;

    /**
     * Average fitness of the entire population. Used in
     * probabilistic selection and reported by printStatistics().
     * Calculated in calculateStatistics().
     */
    protected double avgFitness;

    /**
     * Average complexity of the entire population. Reported by
     * printStatistics() and calculated in calculateStatistics().
     */
    protected double avgLength;

    /**
     * Average tree depth of the entire population. Reported by
     * printStatistics() and calculated in calculateStatistics().
     */
    protected double avgDepth;

    /**
     * Best standardized fitness found in the entire population. 
     * Reported by printStatistics() and calculated in 
     * calculateStatistics().
     */
    public double bestFitness;

    /**
     * Worst standardized fitness found in the entire population. 
     * Reported by printStatistics() and calculated in 
     * calculateStatistics().
     */
    public double worstFitness;

    /**
     * The index of the individual with the best standardized fitness.
     * In case of ties, the lower complexity wins. The GP associated
     * with this index is obtained by 
     * <code>(GP)pop.get(bestOfPopulation)</code>.
     */
    public int bestOfPopulation;

    /**
     * The index of the individual with the worst standardized fitness.
     * In case of ties, the higher complexity wins. The GP associated
     * with this index is obtained by 
     * <code>(GP)pop.get(worstOfPopulation)</code>.
     */
    public int worstOfPopulation;

    /**
     * The highest complexity found anywhere in the population. This
     * may differ from the complexity of the individual that has the
     * worst fitness. longestLength is calculated in printStatistics()
     * but is not printed in any standard reports.
     */
    public int longestLength;

    /**
     * A table of the unique GPs found in the population. This table
     * is created and updated only if the 
     * <a href="gpjpp.GPVariables.html#TestDiversity">TestDiversity</a>
     * configuration option is enabled. Each unique GP is entered in 
     * the table along with a count of the times it appears in the 
     * population. When TestDiversity is enabled, the initial 
     * population is guaranteed to be 100% diverse by rejecting any 
     * individuals that already appear in the table. For successive 
     * generations, duplicates are allowed but the diversity is 
     * reported by printStatistics(). The Hashtable approach to 
     * tracking diversity is fast enough that the option can be left 
     * on at all times.
     */
    protected Hashtable uniqueGPs;

    /**
     * The number of duplicate individuals in the current generation.
     * For the initial generation, this number is always 0. dupCount
     * is not printed in any standard reports, but the diversity
     * ratio (1-dupCount/popSize) is reported by printStatistics().
     */
    public int dupCount;

    /**
     * The number of duplicate individuals that were created in
     * generation 0 but rejected. When diversity checking is enabled
     * generation 0 is guaranteed to be 100% diverse.
     * attemptedDupCount is printed among the standard output of the
     * GPRun class.
     */
    public int attemptedDupCount;

    /**
     * The number of individuals that were created for generation 0
     * but rejected because their complexity exceeded the global
     * configuration variable 
     * <a href="gpjpp.GPVariables.html#MaximumComplexity">
     * MaximumComplexity</a>. attemptedComplexCount
     * is printed among the standard output of the GPRun class.
     */
    public int attemptedComplexCount;


    /**
     * Public null constructor used during stream loading only.
     */
    public GPPopulation() {}

    /**
     * Constructor used when populations are first created.
     * This constructor creates a container capable of holding the 
     * population individuals, but does not create the individuals.
     * It also performs a number of consistency checks on the
     * configuration variables and node set.
     *
     * @param cfg  the global configuration variables
     * @param adfNs the set of node types for all branches
     *
     * @exception java.lang.RuntimeException
     *              if any problems are found in the configuration.
     *              The exception message provides more details.
     *
     * @see gpjpp.GPPopulation#create
     */
    public GPPopulation(GPVariables cfg, GPAdfNodeSet adfNs) {

        //make sure there's something to do
        if (cfg.NumberOfGenerations < 1)
            throw new RuntimeException("Number of generations must be at least 1");
        if (cfg.PopulationSize < 2)
            throw new RuntimeException("Population size must be at least 2");

        //check for possibility of infinite loop during crossover
        if (cfg.MaximumDepthForCrossover < cfg.MaximumDepthForCreation)
            throw new RuntimeException("MaximumDepthForCrossover cannot be less than MaximumDepthForCreation");

        //check tournament selection parameters
        if (cfg.SelectionType == GPVariables.GPTOURNAMENTSELECTION)
            if (cfg.TournamentSize < 2)
                throw new RuntimeException("Tournament size must be at least 2");

        //disable demetic grouping for greedy over-selection
        if (cfg.SelectionType == GPVariables.GPGREEDYSELECTION)
            cfg.DemeticGrouping = false;

        //check demetic grouping parameters
        if (cfg.DemeticGrouping) {
            int demeSize = cfg.DemeSize;
            int popSize = cfg.PopulationSize;
            if (demeSize < 2)
                throw new RuntimeException("Demetic group size must be greater than 1");
            if (demeSize > popSize)
                throw new RuntimeException("Demetic group size must not exceed population size");
            if (popSize % demeSize != 0)
                throw new RuntimeException("Population must be an integer multiple of deme size");
        }

        //confirm that the user has initialized the node sets correctly.
        if (adfNs.containerSize() < 1)
            throw new RuntimeException("Node set is empty");
        for (int tree = 0; tree < adfNs.containerSize(); tree++) {
            GPNodeSet ns = (GPNodeSet)adfNs.get(tree);
            if ((ns == null) || (ns.containerSize() == 0))
                throw new RuntimeException("Node set "+tree+" is undefined");
            if (ns.functions() == 0)
                throw new RuntimeException("No functions are available");
            if (ns.terminals() == 0)
                throw new RuntimeException("No terminals are available");
            for (int i = 0; i < ns.containerSize(); i++)
                if (ns.get(i) == null)
                    throw new RuntimeException("RPB/ADF tree "+tree+" is missing functions or terminals");
        }

        //save configuration and node set references
        this.cfg = cfg;
        this.adfNs = adfNs;

        //reserve space for the genetic programs
        reserveSpace(cfg.PopulationSize);

        //allocate diversity checker
        clearUniqueGPs();
    }

    /**
     * A constructor that can be called to clone a population. 
     * Normally not used.
     */
    public GPPopulation(GPPopulation gpo) {
        super(gpo);

        adfNs = gpo.adfNs;
        cfg = gpo.cfg;
        avgLength = gpo.avgLength;
        avgDepth = gpo.avgDepth;

        //compute the diversity
        buildUniqueGPs();
    }

    /**
     * Implements the Cloneable interface.
     * This clones a GPPopulation but is normally not used.
     *
     * @return the cloned object.
     */
    protected synchronized Object clone() { 
        return new GPPopulation(this); 
    }

    /**
     * Returns a code identifying the class in a stream file.
     *
     * @return the ID code POPULATIONID.
     */
    public byte isA() { return POPULATIONID; }

    /**
     * Creates the GP used for a new individual. The
     * user must override this in a subclass to create
     * GPs of user type. See the example programs.
     *
     * @param numOfTrees  the number of branches in the genetic program.
     * @return the newly created GP.
     */
    public GP createGP(int numOfTrees) { return new GP(numOfTrees); }

    /**
     * Computes the size of the hash table used for diversity checking.
     * It equals an odd number just larger than twice the population
     * size, which should generally avoid having to expand the diversity
     * table after it is created.
     */
    protected int getUniqueGPsSize() { return (2*cfg.PopulationSize+1); }

    /**
     * Creates or clears the diversity table and the 
     * <a href="gpjpp.GPPopulation.html#dupCount">dupCount</a> and
     * <a href="gpjpp.GPPopulation.html#attemptedDupCount">
     * attemptedDupCount</a> fields. If 
     * <a href="gpjpp.GPVariables.html#TestDiversity">TestDiversity</a>
     * is false, does nothing.
     */
    protected void clearUniqueGPs() {
        if (cfg.TestDiversity) {
            if (uniqueGPs == null)
                uniqueGPs = new Hashtable(getUniqueGPsSize());
            else
                uniqueGPs.clear();
            dupCount = 0;
            attemptedDupCount = 0;
        }
    }

    /**
     * Adds the specified GP to the diversity table, or increments
     * its count if already in the table. If 
     * <a href="gpjpp.GPVariables.html#TestDiversity">TestDiversity</a>
     * is false, does nothing.
     */
    protected void updateUniqueGPs(GP gp) {
        if (cfg.TestDiversity) {
            //try to add element to table
            GPInteger count = (GPInteger)uniqueGPs.put(gp, new GPInteger(1));
            if (count != null) {
                //element already present, increment it
                count.setValue(count.intValue()+1);
                dupCount++;
            }
        }
    }

    /**
     * Clears the diversity table and calls updateUniqueGPs for each
     * individual in the population. If 
     * <a href="gpjpp.GPVariables.html#TestDiversity">TestDiversity</a>
     * is false, does nothing.
     */
    protected void buildUniqueGPs() {
        if (cfg.TestDiversity) {
            clearUniqueGPs();
            for (int i = 0; i < containerSize(); i++)
                updateUniqueGPs((GP)get(i));
        }
    }

    /** 
     * Returns true if the specified GP is not already in the 
     * diversity table or if cfg.TestDiversity is false. If the
     * GP is already in the table, returns false and increments
     * attemptedDupCount. Used when creating an initial population.
     */
    protected boolean checkForDiversity(GP gp) {
        if (cfg.TestDiversity && (gp != null)) {
            //try adding GP to table
            GPInteger count = (GPInteger)uniqueGPs.put(gp, new GPInteger(1));
            if (count != null) {
                //already in table
                attemptedDupCount++;
                return false;
            }
        }
        return true;
    }

    /**
     * Creates all of the individuals in an initial population. If
     * cfg.TestForDiversity is true, tries up to 50 times per 
     * individual to create a unique GP. Also tries up to 50 times per
     * individual to create GPs whose complexity is less than
     * cfg.MaximumComplexity and increments attemptedComplexCount for
     * each individual that fails.<p>
     *
     * The depth of each GP is guaranteed to fall in the range
     * GPPopulation.minTreeDepth (2) to cfg.MaximumDepthForCreation
     * (6 by default).<p>
     *
     * create() calls <a href="gpjpp.GP.html#create">GP.create()</a>
     * to create each individual in the population.<p>
     *
     * create() uses one of several tree-building strategies depending
     * on the value of the configuration variable 
     * <a href="gpjpp.GPVariables.html#CreationType">CreationType</a>. 
     * If this variable equals GPRAMPEDHALF (the default), alternating
     * individuals are created using GPGROW (function nodes to
     * maximum depth) and GPVARIABLE (nodes chosen with a 50:50
     * probability of being a function or a terminal). With
     * GPRAMPEDHALF, the depth of successive individuals is ramped from
     * minTreeDepth to MaximumDepthForCreation and back again.<p>
     *
     * If CreationType equals GPRAMPEDVARIABLE, all individuals are
     * created using the GPVARIABLE strategy and the depth of
     * successive individuals is ramped from minTreeDepth to 
     * MaximumDepthForCreation.<p>
     *
     * If CreationType equals GPRAMPEDGROW, all individuals are 
     * created using the GPGROW strategy and the depth of successive
     * individuals is ramped.<p>
     *
     * If CreationType equals GPGROW, all individuals are created
     * using the GPGROW strategy with depth MaximumDepthForCreation.<p>
     *
     * If CreationType equals GPVARIABLE, all individuals are created
     * using the GPVARIABLE strategy with depth MaximumDepthForCreation.<p>
     *
     * If a unique individual is not found in 50/4 tries, the tree
     * depth is incremented for each try thereafter, up to a maximum
     * of MaximumDepthForCreation.<p>
     *
     * If an individual of acceptable complexity is not found in 50/4
     * tries, the tree depth is decremented for each try thereafter,
     * down to a minimum of minTreeDepth.<p>
     *
     * If no acceptable individual is found after 50 tries, a
     * RuntimeException is thrown.<p>
     *
     * After each individual is created, its fitness is calculated
     * by calling <a href="gpjpp.GP.html#evaluate">GP.evaluate()</a>. 
     * After all individuals are created, 
     * <a href="gpjpp.GPPopulation.html#calculateStatistics">
     * calculateStatistics()</a> is called.
     */
    public void create() {

        //clear diversity checker
        clearUniqueGPs();
        attemptedComplexCount = 0;

        int treeDepth = minTreeDepth;
        int creationTries = 50;

        for (int i = 0; i < containerSize(); i++) {

            //create a new GP, or the user's subclass of it
            GP newGP = createGP(adfNs.containerSize());

            //compute desired creation type and tree depth
            int creationType;
            int depth = treeDepth;

            switch (cfg.CreationType) {
                case GPVariables.GPRAMPEDHALF:
                    //use grow or variable every other element
                    if (i % 2 != 0)
                        creationType = GPVariables.GPGROW;
                    else
                        creationType = GPVariables.GPVARIABLE;
                    break;

                case GPVariables.GPRAMPEDVARIABLE:
                    creationType = GPVariables.GPVARIABLE;
                    break;

                case GPVariables.GPRAMPEDGROW:
                    creationType = GPVariables.GPGROW;
                    break;

                case GPVariables.GPGROW:
                    creationType = GPVariables.GPGROW;
                    depth = cfg.MaximumDepthForCreation;
                    break;

                case GPVariables.GPVARIABLE:
                    creationType = GPVariables.GPVARIABLE;
                    depth = cfg.MaximumDepthForCreation;
                    break;

                default:
                    throw new RuntimeException("Invalid creation type");
            }

            //attempt to build a unique tree of allowable complexity
            int tries = 0;
            do {
                newGP.create(creationType, depth, 
                    cfg.MaximumComplexity, adfNs);

                if (newGP.length() > cfg.MaximumComplexity) {
                    //GP is too complex
                    attemptedComplexCount++;

                    //decrease depth
                    if ((tries >= creationTries/4) && (depth > minTreeDepth))
                        depth--;

                } else {
                    //test whether GP is unique
                    if (checkForDiversity(newGP)) {
                        //evaluate and store new GP
                        newGP.stdFitness = newGP.evaluate(cfg);
                        newGP.calcAdjustedFitness();
                        put(i, newGP);
                        break;
                    }

                    //increase depth if struggling to find a unique GP
                    if ((tries >= creationTries/4) &&
                        (depth < cfg.MaximumDepthForCreation))
                        depth++;
                }

                //tries prevents an infinite loop while finding a valid GP
                tries++;
                if (tries >= creationTries)
                    throw new RuntimeException("Unable to create valid GP in "+creationTries+" tries");

            } while (true);

            //increase depth for ramped schemes
            if (++treeDepth > cfg.MaximumDepthForCreation)
                treeDepth = minTreeDepth;
        }

        //evaluate the entire population
        calculateStatistics();
    }

    /**
     * Selects one or two individuals from the specified range of
     * the population by using a tournament algorithm. numToSelect
     * individuals are randomly selected from the specified range
     * and their indexes stored in the selected array. Standardized
     * fitness is used to define "best" and complexity is used to
     * break ties. The tournament size is given by the configuration
     * variable <a href="gpjpp.GPVariables.html#TournamentSize">
     * TournamentSize</a>.
     *
     * @param selected  an array that, upon return, must contain
     *          the indices of the selected individuals.
     * @param numToSelect  the number of individuals to select.
     *          The value must be 1 or 2.
     * @param selectWorst  true if the routine is to select the
     *          worst individuals from the range, false to select
     *          the best.
     * @param range  the range of indices from which to select. Unless
     *          demetic migration is enabled, this is the entire
     *          population.
     *
     * @exception java.lang.RuntimeException
     *          if numToSelect is greater than 2.
     */
    protected void tournamentSelection(int[] selected, int numToSelect,
        boolean selectWorst, GPPopulationRange range) {

        //method designed to select only 1 or 2 elements
        if (numToSelect > 2)
            throw new RuntimeException("numToSelect must not exceed 2");

        //arbitrarily initialize two best or worst elements
        int i;
        int first = range.getRandom();
        int second = range.getRandom();
        GP firstGP = (GP)get(first);
        GP secondGP = (GP)get(second);
        int ith;
        GP ithGP;

        if (selectWorst) {
            //select two worst elements from tournament
            if (firstGP.betterThan(secondGP)) {
                int tmp = first;
                first = second;
                second = tmp;
                GP tmpGP = firstGP;
                firstGP = secondGP;
                secondGP = tmpGP;
            }
            for (i = 2; i < cfg.TournamentSize; i++) {
                ith = range.getRandom();
                ithGP = (GP)get(ith);
                if (!ithGP.betterThan(firstGP)) {
                    second = first;
                    secondGP = firstGP;
                    first = ith;
                    firstGP = ithGP;
                } else if (secondGP.betterThan(ithGP)) {
                    second = ith;
                    secondGP = ithGP;
                }
            }
        } else {
            //select two best elements from tournament
            if (secondGP.betterThan(firstGP)) {
                int tmp = first;
                first = second;
                second = tmp;
                GP tmpGP = firstGP;
                firstGP = secondGP;
                secondGP = tmpGP;
            }
            for (i = 2; i < cfg.TournamentSize; i++) {
                ith = range.getRandom();
                ithGP = (GP)get(ith);
                if (!firstGP.betterThan(ithGP)) {
                    //ith is better than or same as first, demote first
                    second = first;
                    secondGP = firstGP;
                    first = ith;
                    firstGP = ithGP;
                } else if (ithGP.betterThan(secondGP)) {
                    //ith is better than second, replace second
                    second = ith;
                    secondGP = ithGP;
                }
            }
        }

        //store the selected indices
        selected[0] = first;
        if (numToSelect == 2)
            selected[1] = second;
    }

    /**
     * Sums the standardized and adjusted fitnesses over a
     * specified range and stores the results in the fields
     * sumFitness and sumAdjFitness. Used internally.
     */
    protected void sumFitnessRange(GPPopulationRange range) {

        sumFitness = 0.0;
        sumAdjFitness = 0.0;
        for (int i = range.startIx; i < range.endIx; i++) {
            GP gp = (GP)get(i);
            sumFitness += gp.stdFitness;
            sumAdjFitness += gp.adjFitness;
        }
    }

    /**
     * Selects one or more individuals from the specified range of
     * the population by using a roulette algorithm. Each individual
     * in the specified range has a probability of selection equal
     * to its fitness divided by the sum total fitness of all 
     * individuals in the range. Complexity does not play a role 
     * in the selection unless it has already been factored into the 
     * fitness. All fitness values must be non-negative or this method 
     * may hang or produce unexpected results. When selectWorst is false, 
     * adjusted fitness is used; otherwise standardized fitness is used.
     *
     * @param selected  an array that, upon return, must contain
     *          the indices of the selected individuals.
     * @param numToSelect  the number of individuals to select.
     *          The value must be 1 or more.
     * @param selectWorst  true if the routine is to select the
     *          worst individuals from the range, false to select
     *          the best.
     * @param range  the range of indices from which to select. Unless
     *          demetic migration is enabled, this is the entire
     *          population.
     */
    protected void probabilisticSelection(int[] selected, 
        int numToSelect, boolean selectWorst, GPPopulationRange range) {

        int i;

        //first time through, sum all fitnesses
        if (range.firstSelection) {
            range.firstSelection = false;
            sumFitnessRange(range);
        }

        //select requested number of members, usually 1 or 2
        for (int n = 0; n < numToSelect; n++) {
            double rand = GPRandom.nextDouble();
            double sum = 0.0;

            if (selectWorst) {
                rand *= sumFitness;
                for (i = range.startIx; i < range.endIx; i++) {
                    sum += ((GP)get(i)).stdFitness;
                    if (sum >= rand)
                        break;
                }
            } else {
                rand *= sumAdjFitness;
                for (i = range.startIx; i < range.endIx; i++) {
                    sum += ((GP)get(i)).adjFitness;
                    if (sum >= rand)
                        break;
                }
            }

            //this shouldn't happen, but just in case...
            if (i >= range.endIx)
                i = range.endIx-1;

            //add selected population member to array of indices
            selected[n] = i;
        }
    }

    /**
     * Calculates Koza's c constant for greedy over-selection.
     * Returns 0.32 for range size 1000 or less, and 320/size
     * for any larger range size.
     */
    protected double calcGroupIFraction(int size) {
        if (size <= 1000)
            return 0.32;
        else
            return (double)320.0/size;
    }

    /**
     * Computes the adjusted fitness boundary that separates
     * group I from group II individuals using Koza's form of
     * greedy over-selection. First calculates the fitness
     * proportion c, which Koza defines to be 0.32 at population
     * size 1000, 0.16 at 2000, 0.08 at 4000, and so on. For
     * populations less than 1000, a constant value of 0.32 is
     * used. Then uses binary search to find a boundary such
     * that the sum of adjusted fitness for individuals whose
     * fitness is greater than the boundary equals c times the
     * total adjusted fitness. The search doesn't need to be
     * too accurate since c is largely arbitrary anyway, so
     * the search stops when the normalized sum is within 0.005
     * of c.<p>
     *
     * The routine stores values in the fields cutoffAdjFitness, 
     * sumG1AdjFitness, and sumG2AdjFitness. It assumes that
     * sumAdjFitness has already been calculated by calling
     * sumFitnessRange().
     *
     * @param range  the range of indices from which to select. Demetic 
     *          migration is always disabled when greedy selection
     *          is enabled, so this is the entire population. The
     *          routine would work for restricted ranges, but
     *          this would run counter to Koza's assumptions.
     *
     * @see gpjpp.GPPopulation#calcGroupIFraction
     */
    protected void calcCutoffFitness(GPPopulationRange range) {

        //calculate Koza's c constant
        double c = calcGroupIFraction(range.endIx-range.startIx);

        //perform a binary search
        double l = 0.0;
        double r = 1.0;
        int tries = 0;
        do {
            cutoffAdjFitness = (l+r)/2.0;

            //sum the adjusted fitness in group I
            sumG1AdjFitness = 0.0;
            for (int i = range.startIx; i < range.endIx; i++) {
                double f = ((GP)get(i)).adjFitness;
                if (f > cutoffAdjFitness)
                    //GP is in group I
                    sumG1AdjFitness += f;
            }

            //compute normalized sum fitness in group I
            double sumG1NormFitness = sumG1AdjFitness/sumAdjFitness;

            //done if close to c
            if (Math.abs(sumG1NormFitness-c) < 0.005)
                break;

            if (sumG1NormFitness < c)
                //need to lower fitness cutoff
                r = cutoffAdjFitness;
            else
                l = cutoffAdjFitness;

            //limit number of loops in case of chunky distribution
            if (++tries > 20)
                break;
        } while (true);

        sumG2AdjFitness = sumAdjFitness-sumG1AdjFitness;
    }

    /**
     * Selects one or more individuals from the specified range of
     * the population by using Koza's "greedy over-selection". This
     * divides the population into two groups based on an adjusted
     * fitness boundary, such that the sum of the adjusted fitness 
     * for the first, higher-fitness group has a specified proportion 
     * c of the total adjusted fitness. By Koza's definition c equals 
     * 0.32 for population size 1000, 0.16 for 2000, 0.08 for 4000, 
     * and so on. Then, 80% of the time the method returns an 
     * individual from group I using probabilistic selection and
     * the other 20% it returns an individual from group II using
     * probabilistic selection.
     *
     * @param selected  an array that, upon return, must contain
     *          the indices of the selected individuals.
     * @param numToSelect  the number of individuals to select.
     *          The value must be 1 or more.
     * @param selectWorst  true if the routine is to select the
     *          worst individuals from the range, false to select
     *          the best.
     * @param range  the range of indices from which to select. Demetic 
     *          migration is always disabled when greedy selection
     *          is enabled, so this is the entire population.
     *
     * @see gpjpp.GPPopulation#sumFitnessRange
     * @see gpjpp.GPPopulation#calcCutoffFitness
     */
    protected void greedySelection(int[] selected, int numToSelect,
        boolean selectWorst, GPPopulationRange range) {

        int i;

        if (range.firstSelection) {
            range.firstSelection = false;

            //sum all fitnesses, just like probabilisticSelection
            sumFitnessRange(range);

            //calculate cutoff fitness and the adjusted fitness sums
            // for groups I and II
            calcCutoffFitness(range);
        }

        //select requested number of members, usually 1 or 2
        for (int n = 0; n < numToSelect; n++) {
            double rand = GPRandom.nextDouble();
            double sum = 0.0;

            if (selectWorst) {
                //no greediness for worst GPs
                // probabilistic finds lethals very well
                rand *= sumFitness;
                for (i = range.startIx; i < range.endIx; i++) {
                    sum += ((GP)get(i)).stdFitness;
                    if (sum >= rand)
                        break;
                }

            } else if (GPRandom.flip(80.0)) {
                //select from group I 80% of the time for best GPs
                rand *= sumG1AdjFitness;
                for (i = range.startIx; i < range.endIx; i++) {
                    double f = ((GP)get(i)).adjFitness;
                    if (f > cutoffAdjFitness) {
                        sum += f;
                        if (sum >= rand)
                            break;
                    }
                }

            } else {
                //select from group II the other 20%
                rand *= sumG2AdjFitness;
                for (i = range.startIx; i < range.endIx; i++) {
                    double f = ((GP)get(i)).adjFitness;
                    if (f <= cutoffAdjFitness) {
                        sum += f;
                        if (sum >= rand)
                            break;
                    }
                }
            }

            //this shouldn't happen, but just in case...
            if (i >= range.endIx)
                i = range.endIx-1;

            //add selected population member to array of indices
            selected[n] = i;
        }
    }

    /**
     * Calls one of the available selection methods based on the
     * configuration variable 
     * <a href="gpjpp.GPVariables.html#SelectionType">SelectionType</a>. 
     * Override this method if you want to implement a new selection 
     * technique.
     *
     * @exception java.lang.RuntimeException 
     *          if numToSelect is less than 1, or
     *          if the specified range is empty, or
     *          if an unknown selection method is specified.
     *
     * @see gpjpp.GPPopulation#tournamentSelection
     * @see gpjpp.GPPopulation#probabilisticSelection
     * @see gpjpp.GPPopulation#greedySelection
     */
    protected void selectIndices(int[] selected, int numToSelect,
        boolean selectWorst, GPPopulationRange range) {

        if (numToSelect < 1)
            throw new RuntimeException("numToSelect cannot be less than 1");
        if (range.endIx-range.startIx < 1)
            throw new RuntimeException("Selection range is empty");

        switch (cfg.SelectionType) {
            case GPVariables.GPTOURNAMENTSELECTION:
                tournamentSelection(
                    selected, numToSelect, selectWorst, range);
                break;

            case GPVariables.GPPROBABILISTICSELECTION:
                probabilisticSelection(
                    selected, numToSelect, selectWorst, range);
                break;

            case GPVariables.GPGREEDYSELECTION:
                greedySelection(
                    selected, numToSelect, selectWorst, range);
                break;

            default:
                throw new RuntimeException("Unknown selection method");
        }
    }

     //used only in select; avoid reallocating temporaries each call
    private int[] selected = new int[2];
    private GPContainer cont1 = new GPContainer(1);
    private GPContainer cont2 = new GPContainer(2);

    /**
     * Returns one or more individuals from the specified range
     * of this population using a fitness-based selection method.
     * The returned individuals are cloned copies from this
     * population so that they can be modified (via crossover or
     * mutation) without disturbing the existing population. select()
     * also updates the heritage fields of the cloned individuals
     * to indicate their parent(s). This method uses two preallocated
     * (private) containers of length 1 and 2 to return the individuals
     * in order to avoid allocating new arrays every time the routine
     * is called.
     *
     * @param numToSelect  the number of individuals to select.
     *          The value must be 1 or 2. One is used to select
     *          an individual to reproduce; two is used to select
     *          two parents for crossover.
     * @param range the range of indices from which to select. Unless
     *          demetic migration is enabled, this is the entire
     *          population.
     * @return an array of GP references holding the selected 
     *          individuals. The length of the array exactly
     *          matches the number of individuals in it.
     *
     * @exception java.lang.RuntimeException
     *          if numToSelect is not 1 or 2.
     */
    protected synchronized GPContainer select(
        int numToSelect, GPPopulationRange range) {

        if ((numToSelect != 1) && (numToSelect != 2))
            throw new RuntimeException("Invalid numToSelect");

        //select the best ones
        selectIndices(selected, numToSelect, false, range);

        //put copies in container to return
        GP newCopy;
        switch (numToSelect) {
            case 1: //copy
                newCopy = (GP)((GP)get(selected[0])).clone();
                newCopy.dadIndex = selected[0];
                newCopy.dadCross = -1;
                cont1.put(0, newCopy);
                return cont1;

            case 2: //crossover
                newCopy = (GP)((GP)get(selected[0])).clone();
                newCopy.dadIndex = selected[0];
                newCopy.mumIndex = selected[1];
                cont2.put(0, newCopy);

                newCopy = (GP)((GP)get(selected[1])).clone();
                newCopy.dadIndex = selected[1];
                newCopy.mumIndex = selected[0];
                cont2.put(1, newCopy);
                return cont2;
        }

        //shouldn't get here
        return null;
    }

    /**
     * Calls <a href="gpjpp.GPPopulation.html#select">select()</a> to 
     * select exactly two parents for use in crossover.
     */
    protected GPContainer selectParents(GPPopulationRange range) {
        return select(2, range);
    }

    //variable used among multiple calls to evolve
    private int treeDepth = 2;

    /**
     * Based on the configuration variables 
     * <a href="gpjpp.GPVariables.html#CrossoverProbability">
     * CrossoverProbability</a> and
     * <a href="gpjpp.GPVariables.html#CreationProbability">
     * CreationProbability</a>, creates two individuals using
     * crossover or one new individual using creation (a brand-new
     * GP) or reproduction (a fitness-selected copy of an existing
     * individual). The new individuals are guaranteed to have 
     * depth no more than 
     * <a href="gpjpp.GPVariables.html#MaximumDepthForCrossover">
     * MaximumDepthForCrossover</a> or 
     * <a href="gpjpp.GPVariables.html#MaximumDepthForCreation">
     * MaximumDepthForCreation</a> and complexity no more than 
     * <a href="gpjpp.GPVariables.html#MaximumComplexity">
     * MaximumComplexity</a>.<p>
     *
     * Brand-new individuals are created using the 
     * <a href="gpjpp.GPVariables.html#GPVARIABLE">GPVARIABLE</a>
     * strategy with depth ramped between calls to this method.<p>
     *
     * Crossover is considered first, then (if crossover did not occur)
     * creation of a new individual, then (if creation did not occur)
     * reproduction of an existing individual.<p>
     *
     * Mutation does not occur in this method but rather as an
     * independent step in 
     * <a href="gpjpp.GPPopulation.html#generate">generate()</a>. 
     * Thus an individual passed on via reproduction could still 
     * undergo mutation.
     *
     * @param range the range of indices from which to select. Unless
     *          demetic migration is enabled, this is the entire
     *          population.
     * @return an array of GP references holding the selected 
     *          individuals. The length of the array is 2 for
     *          individuals created via crossover and 1 for individuals
     *          generated by creation or reproduction.
     *
     * @exception java.lang.RuntimeException
     *          if an individual of acceptable complexity cannot be
     *          created in 50 attempts.
     */
    protected synchronized GPContainer evolve(GPPopulationRange range) {
        GPContainer gpCont;

        if (GPRandom.flip(cfg.CrossoverProbability)) {
            //crossover, select copies of parents into gpCont
            gpCont = selectParents(range);

            //call first parent's crossover function
            GP dad = (GP)gpCont.get(0);
            gpCont = dad.cross(gpCont, cfg.MaximumDepthForCrossover,
                cfg.MaximumComplexity);

        } else if (GPRandom.flip(cfg.CreationProbability)) {
            //create a new GP with correct number of subtrees.
            GP newGP = createGP(adfNs.containerSize());

            do {
                int creationAttempts = 50;
                int attempts = 0;

                //use simple variable grow method
                newGP.create(GPVariables.GPVARIABLE, 
                    treeDepth, cfg.MaximumComplexity, adfNs);

                if (newGP.length() <= cfg.MaximumComplexity)
                    break;
                else if ((attempts >= creationAttempts/4) &&
                    (treeDepth > minTreeDepth))
                        treeDepth--;

                attempts++;
                if (attempts >= creationAttempts)
                    throw new RuntimeException("Unable to create valid GP in "+creationAttempts+" tries");
            } while (true);

            //increase tree depth after creating a valid GP
            if (++treeDepth > cfg.MaximumDepthForCreation)
                treeDepth = minTreeDepth;

            //use one-item container and put new GP into it
            gpCont = cont1;
            gpCont.put(0, newGP);

        } else
            //fitness-select one individual to copy to next generation
            gpCont = select(1, range);

        return gpCont;
    }

    /**
     * Adds the best individual from this generation to the next.
     * This method does nothing unless the configuration variable
     * <a href="gpjpp.GPVariables.html#AddBestToNewPopulation">
     * AddBestToNewPopulation</a> is true. It can be called only when
     * a steady state population is not in use and therefore newPop
     * exists.
     *
     * @param newPop the population for the next generation.
     */
    protected synchronized void addBest(GPPopulation newPop) {
        if (cfg.AddBestToNewPopulation) {
            GP newGP = (GP)((GP)get(bestOfPopulation)).clone();
            newGP.dadIndex = bestOfPopulation;
            newPop.put(bestOfPopulation, newGP);
            newPop.updateUniqueGPs(newGP);
        }
    }

    //used only in generate; avoid reallocating each time
    private int[] badGPs = new int[2];
    private GPPopulationRange range = new GPPopulationRange();

    /**
     * Generates the next generation of individuals using genetic
     * processes. If the configuration variable 
     * <a href="gpjpp.GPVariables.html#SteadyState">SteadyState</a> is
     * true, the newPop parameter is ignored and the next generation
     * is created by replacing the worst (fitness-based) individuals
     * in the current generation with new individuals. Otherwise,
     * newPop is filled in from scratch with new individuals. In either
     * case, 
     * <a href="gpjpp.GPVariables.html#PopulationSize">PopulationSize</a> 
     * individuals are created and added to the new generation.<p>
     *
     * If <a href="gpjpp.GPVariables.html#DemeticGrouping">
     * DemeticGrouping</a> is true, the overall population is divided
     * into "demes" of size 
     * <a href="gpjpp.GPVariables.html#DemeSize">DemeSize</a> and each 
     * of these is treated individually as a subpopulation.<p>
     *
     * If <a href="gpjpp.GPVariables.html#AddBestToNewPopulation">
     * AddBestToNewPopulation</a> is true and SteadyState is
     * false, the best individual in this population is automatically
     * added to the new population at its same index location.<p>
     *
     * After each new individual or pair of individuals is generated 
     * by <a href="gpjpp.GPPopulation.html#evolve">evolve()</a>, 
     * generate() calls 
     * <a href="gpjpp.GP.html#mutate">mutate()</a> to possibly modify 
     * the new individuals further.<p>
     *
     * If <a href="gpjpp.GPVariables.html#TestDiversity">TestDiversity</a>
     * is true, the diversity table is updated while the new population 
     * is built to keep track of duplicates. No duplicates are rejected 
     * within generate(), however.<p>
     *
     * After the new population is generated, 
     * <a href="gpjpp.GPPopulation.html#demeticMigration">
     * demeticMigration()</a> is called if this feature is activated. 
     * Population statistics are always calculated for the new 
     * generation.
     *
     * @param newPop  an instantiated new population to fill.
     */
    public void generate(GPPopulation newPop) {

        //divide population into groups if demetic migration enabled
        int demeSize;
        if (cfg.DemeticGrouping)
            demeSize = cfg.DemeSize;
        else
            demeSize = containerSize();

        if (!cfg.SteadyState) {
            //clear the duplicate checker
            newPop.clearUniqueGPs();

            //if requested, take best and add to new generation
            addBest(newPop);
        }

        //build new generation, deme by deme
        for (int demeStart = 0; demeStart < containerSize(); demeStart += demeSize) {
            range.startIx = demeStart;
            range.endIx = demeStart+demeSize;
            range.firstSelection = true;

            //continue until the whole deme or population is full
            for (int n = 0; n < demeSize; ) {
                //make 1 or 2 new GPs via creation, crossover, reproduction
                GPContainer gpCont = evolve(range);

                //select bad GPs to replace for steady state
                if (cfg.SteadyState && (gpCont.containerSize() != 0))
                    selectIndices(badGPs, gpCont.containerSize(), true, range);

                //add GPs to next population
                for (int i = 0; i < gpCont.containerSize(); i++) {
                    //avoid overwriting best of previous generation
                    if (!cfg.SteadyState && cfg.AddBestToNewPopulation)
                        if (demeStart+n == bestOfPopulation) {
                            n++;
                            if (n >= demeSize)
                                break;
                        }

                    //get the GP from the evolve container
                    GP newGP = (GP)gpCont.get(i);

                    //apply mutation here, not in evolve
                    newGP.mutate(cfg, adfNs);

                    //calculate fitness of new GP
                    newGP.stdFitness = newGP.evaluate(cfg);
                    newGP.calcAdjustedFitness();

                    if (cfg.SteadyState) {
                        //update fitness sums for probabilistic selection
                        GP badGP = (GP)get(badGPs[i]);
                        sumFitness += newGP.stdFitness-badGP.stdFitness;
                        sumAdjFitness += newGP.adjFitness-badGP.adjFitness;

                        //replace bad old GP
                        put(badGPs[i], newGP);
                    } else {
                        newPop.put(demeStart+n, newGP);
                        newPop.updateUniqueGPs(newGP);
                    }
                    n++;
                    if (n >= demeSize)
                        break;
                } //for i < gpCont.containerSize
            } //for n < demeSize
        } //for demeStart < containerSize

        if (cfg.SteadyState)
            //count the duplicates in the updated population
            buildUniqueGPs();

        //if demetic grouping is used, let members migrate into other demes
        if (cfg.DemeticGrouping)
            if (cfg.SteadyState)
                demeticMigration();
            else
                newPop.demeticMigration();

        //calculate statistics of the new generation
        if (cfg.SteadyState)
            calculateStatistics();
        else
            newPop.calculateStatistics();
    }

    //used only in demeticMigration; avoid reallocating each call
    private int[] r1 = new int[1];
    private int[] r2 = new int[1];
    private GPPopulationRange range1 = new GPPopulationRange();
    private GPPopulationRange range2 = new GPPopulationRange();

    /**
     * Based on the configuration variable 
     * <a href="gpjpp.GPVariables.html#DemeticMigProbability">
     * DemeticMigProbability</a>, fitness-selects a good individual 
     * from each deme and exchanges it with a good individual from the 
     * next deme. This process occurs for each adjacent pair of demes, 
     * and the last deme is considered to be adjacent to the first deme.
     */
    protected synchronized void demeticMigration() {

        //for each deme exchange best member with next deme
        for (int demeStart = 0; demeStart < containerSize();
            demeStart += cfg.DemeSize) {
            if (GPRandom.flip(cfg.DemeticMigProbability)) {
                //set selection range
                range1.firstSelection = true;
                range1.startIx = demeStart;
                range1.endIx = range1.startIx+cfg.DemeSize;

                //second range is just beyond first, but wraps at end
                range2.firstSelection = true;
                range2.startIx = range1.endIx;
                if (range2.startIx >= containerSize())
                    range2.startIx = 0;
                range2.endIx = range2.startIx+cfg.DemeSize;

                //select the best of each deme
                selectIndices (r1, 1, false, range1);
                selectIndices (r2, 1, false, range2);

                //exchange population members
                GPObject p1 = get(r1[0]);
                GPObject p2 = get(r2[0]);
                put(r2[0], p1);
                put(r1[0], p2);
            }
        }
    }

    /**
     * Calculates statistics about a complete population, including
     * the best, average, and worst fitness, complexity, and depth.
     * Also stores the indices of the best and worst individuals.
     *
     * @exception java.lang.RuntimeException
     *              if a null GP is unexpectedly found in the population.
     */
    protected void calculateStatistics() {
        //search for the best, worst, and longest
        GP worst = (GP)get(0);
        GP best = (GP)get(0);
        worstOfPopulation = 0;
        bestOfPopulation = 0;

        bestFitness = best.stdFitness;
        worstFitness = bestFitness;

        longestLength = worst.length();
        int worstLen = longestLength;
        int bestLen = longestLength;

        int sumLength = 0;
        int sumDepth = 0;
        int sumFit = 0;


        for (int i = 0; i < containerSize(); i++) {
            GP current = (GP)get(i);
            if (current == null)
                throw new RuntimeException("Null GP found in population");

            //test for nulls anywhere in the GP
            //current.testNull();

            int curLen = current.length();
            if (curLen > longestLength)
                longestLength = curLen;
            sumLength += curLen;

            sumDepth += current.depth();

            double curFitness = current.stdFitness;
            //System.out.println(curFitness);
            sumFit += curFitness;

            if ((worstFitness < curFitness) ||
                ((worstFitness == curFitness) && (worstLen < curLen))) {
                worstOfPopulation = i;
                worst = current;
                worstFitness = curFitness;
                worstLen = curLen;
            }

            if ((bestFitness > curFitness) ||
                ((bestFitness == curFitness) && (bestLen > curLen))) {
                bestOfPopulation = i;
                best = current;
                bestFitness = curFitness;
                bestLen = curLen;
            }
        }

        //get averages
        avgFitness = (double)sumFit/containerSize();
        avgLength = (double)sumLength/containerSize();
        avgDepth = (double)sumDepth/containerSize();
    }

    /**
     * Converts an integer to a string right-justified in a field
     * of specified width.
     */
    protected static String formatInt(int i, int width) {
        String s = String.valueOf(i);
        while (s.length() < width)
            s = " "+s;
        return s;
    }

    /**
     * Returns a string of asterisks with specified width.
     */
    protected static String overflow(int width) {

        StringBuffer sb = new StringBuffer(width);
        for (int i = 1; i < width; i++)
            sb.append("*");
        return sb.toString();
    }

    /**
     * Converts a double to a string right justified in a field of
     * specified width. Returns an overflow string if the number cannot
     * be formatted as specified. Uses scientific notation whenever
     * java.lang.String.valueOf(double) returns a number in scientific
     * format. Rounds the number to the specified number of decimal
     * places. Not industrial strength but works as needed for
     * GPPopulation and GPRun reports.
     */
    public static String formatDouble(double d, int width, int places) {

        if (Double.isNaN(d) || Double.isInfinite(d))
            return overflow(width);

        String s;
        double pow10 = Math.pow(10.0, places);

        d = Math.rint(d*pow10)/pow10; //round to places
        if ((d < 1.0e+006) && (d > Math.pow(10.0, width-1-places)-Math.pow(10.0, -places)))
            //fixed-point value won't fit in field
            return overflow(width);

        s = String.valueOf(d);
        int ePos = s.indexOf('e');
        if (ePos < 0)
            ePos = s.indexOf('E');
        if (ePos < 0) {
            //no exponent in string
            int dpos = s.indexOf('.');
            if (dpos < 0)
                s = s+".";                          //add decimal point
            else if (dpos+places+1 < s.length())
                s = s.substring(0, dpos+places+1);  //truncate places
            dpos = s.indexOf('.');
            while (s.length()-dpos < places+1)
                s = s+"0";                          //zero-pad to places
        } else if (width > 6) {
            //scientific notation
            String sExp = s.substring(ePos, s.length());
            String sMant;
            if (ePos-2 > places)
                sMant = s.substring(0, places+2);
            else
                sMant = s.substring(0, ePos);
            s = sMant+sExp;
        } else
            return overflow(width);

        while (s.length() < width)
            s = " "+s;                              //left-pad to width
        return s;
    }

    /**
     * Calls formatDouble to create a formatted string and then strips
     * any leading blanks.
     */
    public static String trimFormatDouble(double d, int width, int places) {

        String s = formatDouble(d, width, places);
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) != ' ')
                return s.substring(i, s.length());
        return "";
    }

    /**
     * Prints a legend line for the standard statistics report to the
     * specified PrintStream.
     */
    public void printStatisticsLegend(PrintStream sout) {

        sout.print  (" Gen|              Fitness           |   Complexity    |    Depth   ");
        if (cfg.TestDiversity)
            sout.print("|Variety");
        sout.println();
        sout.print  ("    |      Best    Average      Worst|   B      A     W|  B    A   W");
                    //xxxx|xxxxxxx.xx xxxxxxx.xx xxxxxxx.xx|xxxx xxxx.x  xxxx| xx xx.x  xx| x.xxx
        if (cfg.TestDiversity)
            sout.print("|");
        sout.println();
    }               

    /**
     * Prints one generation's statistics to the specified PrintStream.
     * The output is on one line and includes the generation number;
     * the best, average, and worst standardized fitness; the complexity
     * of the best, average, and worst individuals; the depth of the
     * best, average, and worst individuals; and the diversity of the
     * population if cfg.TestDiversity is true. If this generation has
     * been saved to a stream by GPRun's checkpointing facility, the 
     * letter 'c' is printed at the end of the line.
     *
     * @param generation  the generation number.
     * @param chk  the checkpoint character ('c' or ' ').
     * @param sout  the statistics PrintStream
     */
    public void printStatistics(int generation, char chk, 
        PrintStream sout) {

        sout.print(formatInt(generation, 4));

        sout.print("|");
        sout.print(formatDouble(((GP)get(bestOfPopulation)).stdFitness, 10, 2));
        sout.print(" ");
        sout.print(formatDouble(avgFitness, 10, 2));
        sout.print(" ");
        sout.print(formatDouble(((GP)get(worstOfPopulation)).stdFitness, 10, 2));

        sout.print("|");
        sout.print(formatInt(((GP)get(bestOfPopulation)).length(), 4));
        sout.print(" ");
        sout.print(formatDouble(avgLength, 6, 1));
        sout.print("  ");
        sout.print(formatInt(((GP)get(worstOfPopulation)).length(), 4));
        //sout.print(formatDouble(longestLength, 6, 1));

        sout.print("| ");
        sout.print(formatInt(((GP)get(bestOfPopulation)).depth(), 2));
        sout.print(" ");
        sout.print(formatDouble(avgDepth, 4, 1));
        sout.print("  ");
        sout.print(formatInt(((GP)get(worstOfPopulation)).depth(), 2));

        if (cfg.TestDiversity) {
            sout.print("| ");
            sout.print(formatDouble(1.0-((double)dupCount/containerSize()), 5, 3));
        }

        sout.print(" ");
        sout.print(chk);
        sout.println();
    }

    /**
     * Formats a string that shows the heritage of a crossover,
     * reproduction, or mutation operation.
     *
     * @param len   the length of the string to return.
     * @param index the population index of the parent.
     * @param tree  the branch number in which crossover or 
     *              mutation occurred. Branch 0 is converted to
     *              "RPB", branch 1 is converted to "ADF0", and so on.
     * @param cut   the s-expression index at which crossover or
     *              mutation occurred.
     */
    protected String formatParentage(int len, int index, int tree, 
        int cut) {

        String sIndex;
        String sTree;
        String sCut;

        StringBuffer sb = new StringBuffer(len);
        int pos = len;
        for (int i = 0; i < pos; i++)
            sb.append(' ');

        if (index >= 0) {
            sIndex = String.valueOf(index);
            pos -= sIndex.length();
        } else
            sIndex = "";

        if (tree >= 0) {
            if (tree == 0)
                sTree = "RPB";
            else
                sTree = "ADF"+(tree-1);
            pos -= sTree.length();
        } else
            sTree = "";

        if ((index >= 0) && (tree >= 0))
            pos--;

        if (cut >= 0) {
            sCut = String.valueOf(cut);
            pos -= sCut.length()+1;
        } else
            sCut = "";

        //transfer substrings to display buffer
        for (int i = 0; i < sIndex.length(); i++)
            sb.setCharAt(pos++, sIndex.charAt(i));
        if ((index >= 0) && (tree >= 0))
            sb.setCharAt(pos++, ':');
        for (int i = 0; i < sTree.length(); i++)
            sb.setCharAt(pos++, sTree.charAt(i));
        if (cut >= 0)
            sb.setCharAt(pos++, ':');
        for (int i = 0; i < sCut.length(); i++)
            sb.setCharAt(pos++, sCut.charAt(i));

        return sb.toString();
    }

    /**
     * If cfg.TestCheckpoint is true, tests the best individual at each checkpoint.
    */
    public void testBest(){
        GP current = (GP)get(bestOfPopulation);
        current.testBest(cfg);
    }


    /**
     * Prints details about a particular individual to the specified
     * PrintStream. The first line shows 'B' if the individual is the
     * best in its population, or 'W' if it is the worst. The line
     * goes on to show the population index (num) of the individual, its
     * reproduction or crossover heritage, its mutation heritage, and
     * its standardized fitness, complexity, and depth.<p>
     *
     * If showExpression is true, the GP's printOn() method is called
     * to print the s-expression. If showTree is true, the GP's 
     * printTree() method is called to print the expression in 
     * pseudo-graphic text format.
     */
    public void printIndividual(GP current, int num, 
        boolean showExpression, boolean showTree, PrintStream dout) {

        if (num == bestOfPopulation)
            dout.print("B");
        else if (num == worstOfPopulation)
            dout.print("W");
        else
            dout.print(" ");

        dout.print(formatInt(num, 4));

        //display creation, copy, crossover history
        dout.print(formatParentage(14, current.dadIndex, current.crossTree, current.dadCross));
        dout.print(formatParentage(14, current.mumIndex, current.crossTree, current.mumCross));

        //display mutation history
        dout.print(formatParentage(9, -1, current.swapTree, current.swapPos));
        dout.print(formatParentage(9, -1, current.shrinkTree, current.shrinkPos));

        dout.print(" "+formatDouble(current.stdFitness, 10, 2));
        dout.print(" "+formatInt(current.length(), 4));
        dout.print(" "+formatInt(current.depth(), 4));
        dout.println();

        if (showExpression) {
            dout.println();
            current.printOn(dout, cfg);
        }
        if (showTree) {
            dout.println();
            current.printTree(dout, cfg);
        }
    }

    /**
     * Prints details about some or all of the population. It starts by
     * printing a columnar header, then calls printIndividual() for
     * all individuals, the best, and/or the worst depending on the
     * parameters passed to the method.
     */
    public void printDetails(int generation, 
        boolean showAll, boolean showBest, boolean showWorst,
        boolean showExpression, boolean showTree, PrintStream dout) {

        if (showAll || showBest || showWorst) {
            dout.println("========================================================================");
            dout.println("Generation:"+generation+" best:"+bestOfPopulation+" worst:"+worstOfPopulation);
            dout.println("  GP#           dad           mum oper mut shrk mut    fitness  len  dep");
            dout.println("===== ============= ============= ======== ======== ========== ==== ====");
                        //Bnnnn iiii:adf0:ppp iiii:adf0:ppp adf0:ppp adf0:ppp nnnnnnn.n llll dddd
        }

        GP current;
        if (showAll)
            for (int i = 0; i < containerSize(); i++) {
                current = (GP)get(i);
                printIndividual(current, i, showExpression, showTree, dout);
            }

        if (showBest && !showAll) {
            current = (GP)get(bestOfPopulation);
            printIndividual(current, bestOfPopulation, showExpression,
                showTree, dout);
        }

        if (showWorst && !showAll) {
            current = (GP)get(worstOfPopulation);
            printIndividual(current, worstOfPopulation, showExpression,
                showTree, dout);
        }
    }

    /**
     * Loads a GPPopulation from the specified stream. Reads all
     * of the GPs from the stream, then rebuilds the diversity table and 
     * recalculates the population statistics. Note that GPPopulation
     * doesn't have a save() method because its superclass GPContainer
     * does everything that's necessary in its save() method.
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

        //load population container
        super.load(is);

        //recalculate remaining variables
        buildUniqueGPs();
        calculateStatistics();
    }

    /**
     * Writes every GP in text format to a PrintStream by
     * simply calling <a href="gpjpp.GP.html#printOn">GP.printOn()</a>
     * for every individual in the population.
     */
    public void printOn(PrintStream os, GPVariables cfg) {

        for (int i = 0; i < containerSize(); i++)
            ((GP)get(i)).printOn(os, cfg);
    }
}

//used by Hashtable to count duplicates
//like java.lang.Integer, but allows value to be changed
class GPInteger {
    private int value;

    GPInteger() {}

    GPInteger(int value) { this.value = value; }

    public int intValue() { return value; }

    public void setValue(int value) { this.value = value; }

    public int hashCode() { return value; }

    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof GPInteger))
            return value == ((GPInteger)obj).intValue();
        return false;
    }
}

//used to track subranges of entire population
class GPPopulationRange {
    public int startIx;
    public int endIx;
    public boolean firstSelection;

    public GPPopulationRange() {}

    public GPPopulationRange(int startIx, int endIx) {
        this.startIx = startIx;
        this.endIx = endIx;
        firstSelection = true;
    }

    //return a random element within the range
    public int getRandom() {
        return startIx+GPRandom.nextInt(endIx-startIx);
    }

}
