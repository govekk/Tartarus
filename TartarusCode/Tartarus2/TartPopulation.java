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

import gpjpp.*;

//extend GPPopulation to create Tartarus trees
//doesn't really need to be public, but is made so for consistency

public class TartPopulation extends GPPopulation {

    //this constructor called when new populations are created
    TartPopulation(GPVariables gpVar, GPAdfNodeSet adfNs) {
        super(gpVar, adfNs);
    }

    //populations are not cloned in standard runs
    //TartPopulation(TartPopulation gpo) { super(gpo); }
    //protected Object clone() { return new TartPopulation(this); }

    //ID routine required for streams
    public byte isA() { return GPObject.USERPOPULATIONID; }

    //must override GPPopulation.createGP to create TartGP instances
    public GP createGP(int numOfGenes) { return new TartGP(numOfGenes); }

}
