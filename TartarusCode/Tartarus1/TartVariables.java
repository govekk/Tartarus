// Tartarus implementation
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
import java.util.Properties;
import java.util.Random;
import gpjpp.*;

//extension of GPVariables for Tartarus-specific stuff

public class TartVariables extends GPVariables {

    //number of cells in trail grid
    public int WorldHorizontal = 6;
    public int WorldVertical = 6;

    //number of random Tartarus grids to test each genome on
    public int NumTestGrids = 10;
    
    //number of moves dozer makes on each grid 
    public int NumSteps = 80;

    //number of blocks to place on the grid
    public int NumBlocks = 5;

    // number of grids to test Dozer on for simulation visualization output only
    // has no effect on evolution itself
    public int NumCheckpointTestGrids = 1;

    // random seed, currently only used for simulation visualization
    public int randomSeed;

    // simulation grid, not read from file but created during run
    // included here for easy access in any class/function
    public Grid dozerGrid;

    //public null constructor required for stream loading
    public TartVariables() {
        Random rand = new Random();
        randomSeed = rand.nextInt();
        System.out.println("RAND: " + randomSeed);
    }

    //ID routine required for streams
    public byte isA() { return GPObject.USERVARIABLESID; }

    public void createGrid() {
        dozerGrid = new Grid(WorldHorizontal, WorldVertical, NumBlocks);
    }

    public void createGrid(int seed) {
        dozerGrid = new Grid(WorldHorizontal, WorldVertical, NumBlocks, seed);
    }

    //get values from properties
    public void load(Properties props) {

        if (props == null)
            return;
        super.load(props);
        WorldHorizontal = getInt(props, "WorldHorizontal", WorldHorizontal);
        WorldVertical = getInt(props, "WorldVertical", WorldVertical);
        NumTestGrids = getInt(props, "NumTestGrids", NumTestGrids);
        NumBlocks = getInt(props, "NumBlocks", NumBlocks);
        NumSteps = getInt(props, "NumSteps", NumSteps);
        NumCheckpointTestGrids = getInt(props, "NumCheckpointTestGrids", NumCheckpointTestGrids);
    }

    //get values from a stream
    protected void load(DataInputStream is)
        throws ClassNotFoundException, IOException,
            InstantiationException, IllegalAccessException {

        super.load(is);
        WorldHorizontal = is.readInt();
        WorldVertical = is.readInt();
        NumTestGrids = is.readInt();
        NumBlocks = is.readInt();
        NumSteps = is.readInt();
        NumCheckpointTestGrids = is.readInt();
    }

    //save values to a stream
    protected void save(DataOutputStream os) throws IOException {

        super.save(os);
        os.writeInt(WorldHorizontal);
        os.writeInt(WorldVertical);
        os.writeInt(NumTestGrids);
        os.writeInt(NumBlocks);
        os.writeInt(NumSteps);
        os.writeInt(NumCheckpointTestGrids);
    }

    //write values to a text file
    public void printOn(PrintStream os, GPVariables cfg) {

        super.printOn(os, cfg);
        os.println("WorldHorizontal           = "+WorldHorizontal);
        os.println("WorldVertical             = "+WorldVertical);
        os.println("NumTestGrids              = "+NumTestGrids);
        os.println("NumBlocks                 = "+NumBlocks);
        os.println("NumSteps                  = "+NumSteps);
        os.println("NumCheckpointTestGrids    = "+NumCheckpointTestGrids);
    }
}
