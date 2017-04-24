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
import java.util.Properties;
import java.nio.file.*;
import gpjpp.*;

//Tartarus test
class Tartarus extends GPRun {

    //must override GPRun.createVariables to return Tartarus specific variables
    protected GPVariables createVariables() {
        return new TartVariables();
    }

    // must override GPRun.createNodeSet to return 
    // initialized set of functions & terminals
    protected GPAdfNodeSet createNodeSet(GPVariables cfg) {
        GPNodeSet ns0 = new GPNodeSet(12);
 
        //MAIN TREE
//        ns0.putNode(new GPNode(Grid.LFT, "left"));
        ns0.putNode(new GPNode(Grid.RGT, "right"));
        ns0.putNode(new GPNode(Grid.FWD, "forward"));
        ns0.putNode(new GPNode(Grid.UR, "ur", 3));
        ns0.putNode(new GPNode(Grid.UM, "um", 3));
        ns0.putNode(new GPNode(Grid.UL, "ul", 3));
        ns0.putNode(new GPNode(Grid.MR, "mr", 3));
        ns0.putNode(new GPNode(Grid.ML, "ml", 3));
        ns0.putNode(new GPNode(Grid.LR, "lr", 3));
        ns0.putNode(new GPNode(Grid.LM, "lm", 3));
        ns0.putNode(new GPNode(Grid.LL, "ll", 3));
        ns0.putNode(new GPNode(Grid.prog2, "prog2", 2));
        ns0.putNode(new GPNode(Grid.prog3, "prog3", 3));
//        ns0.putNode(new GPNode(Grid.ahead2, "ahead2", 3));

	// ADF - Do NOT change
	GPAdfNodeSet adfNs = new GPAdfNodeSet(1);
	adfNs.put(0, ns0);
        return adfNs;
    }

    //must override GPRun.createPopulation to return 
    //  lawn-specific population
    protected GPPopulation createPopulation(GPVariables cfg, 
        GPAdfNodeSet adfNs) {
        return new TartPopulation(cfg, adfNs);
    }

    //construct this test case
    Tartarus(String baseName) {
	super(baseName, true);
    }
    

    //main application function
    public static void main(String[] args) {
        //compute base file name from command line parameter
	String baseName;
        if (args.length == 1)
            baseName = args[0];
        else
            baseName = "tartarus";

        //clear simulation files if exist
        try {
            Path path = Paths.get("data/"+baseName+"_simTime.txt");
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.out.println("Error deleting simTime files");
            e.printStackTrace();
        }
        try {
            Path path = Paths.get("data/"+baseName+"_simBest.txt");
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.out.println("Error deleting simTime files");
            e.printStackTrace();
        }

        //construct the test case
        Tartarus test = new Tartarus(baseName);

        //run the test
        test.run();

        //make sure all threads are killed
        System.exit(0);
    }
}
