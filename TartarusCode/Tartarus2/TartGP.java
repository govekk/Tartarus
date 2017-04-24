// Tartarus Implementation
// Copyright (c) 2013, Sherri Goings
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


import java.io.*;
import gpjpp.*;
import java.util.Random;

//extend GP for lawn mowing
//class must be public for stream loading; otherwise non-public ok

public class TartGP extends GP {

    //public null constructor required during stream loading only
    public TartGP() {}

    //this constructor called when new GPs are created
    TartGP(int genes) { super(genes); }

    //this constructor called when GPs are cloned during reproduction
    TartGP(TartGP gpo) { super(gpo); }

    //called when GPs are cloned during reproduction
    protected Object clone() { return new TartGP(this); }

    //ID routine required for streams
    public byte isA() { return GPObject.USERGPID; }

    //must override GP.createGene to create LawnGene instances
    public GPGene createGene(GPNode gpo) { return new TartGene(gpo); }

    //must override GP.evaluate to return standard fitness
    public double evaluate(GPVariables cfg) {

        TartVariables tcfg = (TartVariables)cfg;

        double totFit = 0;
        // test GP on N random boards
        for (int k=0; k<tcfg.NumTestGrids; k++) {
            //create new random grid
            tcfg.createGrid();
            
            //evaluate main tree for max steps (set in .ini file) of the dozer
	    tcfg.dozerGrid.setSteps(0);
	    int done = 0;
            while (done == 0) {
                // evaluate tree recursively starting with the root node
		// if returns 0, means went through entire tree without reaching
		// max steps, so start over again with root
		// when max steps is reached, returns 1, quit evaluation
                done = ((TartGene)get(0)).evaluate(tcfg, this);
	    }
            totFit += tcfg.dozerGrid.calcFitness();
        }
        totFit = totFit/tcfg.NumTestGrids;
        if (cfg.ComplexityAffectsFitness)
            //add length into fitness to promote small trees
            totFit += length()/1000.0;

        //return standard fitness
        return totFit;
    }

    //optionally override GP.printOn to print tartarus-specific data
    public void printOn(PrintStream os, GPVariables cfg) {
        super.printOn(os, cfg);
    }

    //method called on best of population at each checkpoint
    public void testBest(GPVariables cfg){
	TartVariables tcfg = (TartVariables)cfg;
        BufferedWriter out = null;
        try{
            //sets it to append mode
            out = new BufferedWriter(new FileWriter("data/"+tcfg.baseName+"_simTime.txt", true));
        }catch(IOException exception){
            System.out.println("Error opening file");
        }

        Random rand = new Random(tcfg.randomSeed);
        TartGene gene = (TartGene)get(0);
        int curGridFit = 0;

        try{
            //starting new generation
            out.write("New Generation  " + tcfg.CheckpointGens + "\n");
        }catch(IOException exception){
            System.out.println("Error writing to file");
        }
        // run this genome on some number of test grids
        for (int j=0; j<tcfg.NumCheckpointTestGrids; j++) {
            //create new random grid but will be the same across different gens
            tcfg.createGrid(rand.nextInt());

            //initialize the simulation file with the blocks and dozer
            tcfg.dozerGrid.initSimulationFile(out);
	    
            //evaluate main tree for max steps of the dozer, printing grid after each move
	    tcfg.dozerGrid.setSteps(0);
	    int done = 0;
            while (done == 0) {
                // evaluate tree recursively starting with the root node
		// if returns 0, means went through entire tree without reaching
		// max steps, so start over again with root
		// when max steps is reached, returns 1, quit evaluation
                done = ((TartGene)get(0)).evaluate(tcfg, null, null, out);
		try{
                    //finished one tree eval
                    out.write("-------\n");
                }catch(IOException exception){
                    System.out.println("Error writing to file");
                }
	    }
	    
            curGridFit = tcfg.dozerGrid.calcFitness();
            tcfg.dozerGrid.outputFitness(out, curGridFit);
        }
        try{
            out.flush();
            out.close();
        }catch(IOException exception){
            System.out.println("Error closing file");
        }
    }

    public void printTree(PrintStream os, GPVariables cfg) {
        //super.printTree(os, cfg);
	TartVariables tcfg = (TartVariables)cfg;

        BufferedWriter out = null;
        try{
            out = new BufferedWriter(new FileWriter("data/"+tcfg.baseName+"_simBest.txt"));
        }catch(IOException exception){
            System.out.println("Error writing to file");
        }
        
        // write grid at each step for this genome
        TartGene gene = (TartGene)get(0);
        int curGridFit = 0;
	
         // run this genome on some number of test grids, printing the resulting grid at each step
        for (int i=0; i<tcfg.NumTestGrids; i++) {
            //create new random grid
            tcfg.createGrid();

            //initialize the simulation file with the blocks and dozer
            tcfg.dozerGrid.initSimulationFile(out);

            //evaluate main tree for max steps of the dozer, writing grid after each move
	    tcfg.dozerGrid.setSteps(0);
	    int done = 0;
            while (done == 0) {
                // evaluate tree recursively starting with the root node
		// if returns 0, means went through entire tree without reaching
		// max steps, so start over again with root
		// when max steps is reached, returns 1, quit evaluation
                done = ((TartGene)get(0)).evaluate(tcfg, null, os, out);
		try{
                    //finished one tree eval
                    out.write("-------\n");
                }catch(IOException exception){
                    System.out.println("Error writing to file");
                }
	    }
	    
            curGridFit = tcfg.dozerGrid.calcFitness();
            tcfg.dozerGrid.outputFitness(out, curGridFit);
	}
        try{
            out.flush();
            out.close();
        }catch(IOException exception){
            System.out.println("Error closing file");
        }
	
    }
}
