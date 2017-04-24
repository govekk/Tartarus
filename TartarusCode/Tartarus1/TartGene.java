// Implementation of Tartarus Problem
// Author: Sherri Goings
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

import java.awt.Point;
import java.io.*;
import gpjpp.*;
import java.util.Random;

//extend GPGene to evaluate Tartarus

public class TartGene extends GPGene {
    //public null constructor required during stream loading only

    public TartGene() {}

    //this constructor called when new genes are created
    TartGene(GPNode gpo) { super(gpo); }

    //this constructor called when genes are cloned during reproduction
    TartGene(TartGene gpo) { super(gpo); }

    //called when genes are cloned during reproduction
    protected Object clone() { return new TartGene(this); }

    //ID routine required for streams
    public byte isA() { return GPObject.USERGENEID; }

    //must override GPGene.createChild to create TartGene instances
    public GPGene createChild(GPNode gpo) { return new TartGene(gpo); }

    //called by TartGP.evaluate() for main branch of each GP
    int evaluate(TartVariables cfg, TartGP gp) {

        int arg1, arg2, arg3, result;
        switch (node.value()) {
            
        case Grid.ZERO:
            return 0;
            
        case Grid.ONE: 
            return 1;
            
        case Grid.TWO: 
            return 2;
            
        case Grid.UR:
            return cfg.dozerGrid.sensor(1, -1);

        case Grid.MR:
            return cfg.dozerGrid.sensor(0, -1);

        case Grid.LR:
            return cfg.dozerGrid.sensor(-1, -1);

        case Grid.UM:
            return cfg.dozerGrid.sensor(1, 0);

        case Grid.LM:
            return cfg.dozerGrid.sensor(-1, 0);

        case Grid.UL:
            return cfg.dozerGrid.sensor(1, 1);

        case Grid.ML:
            return cfg.dozerGrid.sensor(0, 1);

        case Grid.LL:
            return cfg.dozerGrid.sensor(-1, 1);

        case Grid.INC:
            return ( ( (TartGene)get(0) ).evaluate(cfg, gp) + 1) % 3;

        case Grid.DEC:
            result = ( (TartGene)get(0) ).evaluate(cfg, gp) - 1;  
            if (result<0) result = 2;
            return result % 3;

        case Grid.ADD:
            result = ( (TartGene)get(0) ).evaluate(cfg, gp) + ( (TartGene)get(1) ).evaluate(cfg, gp);
            if (result<0) result = 2;
            return result % 3;

        case Grid.SUB:
            result = ( (TartGene)get(0) ).evaluate(cfg, gp) - ( (TartGene)get(1) ).evaluate(cfg, gp);
            if (result<0) result = 2;
            return result % 3;

        case Grid.MAX:
            arg1 = ( (TartGene)get(0) ).evaluate(cfg, gp);
            arg2 = ( (TartGene)get(1) ).evaluate(cfg, gp);
            if (arg1 > arg2) return arg1;
            else return arg2;

        case Grid.MIN:
            arg1 = ( (TartGene)get(0) ).evaluate(cfg, gp);
            arg2 = ( (TartGene)get(1) ).evaluate(cfg, gp);
            if (arg1 < arg2) return arg1;
            else return arg2;

        case Grid.ITE:
            arg1 = ( (TartGene)get(0) ).evaluate(cfg, gp);
            arg2 = ( (TartGene)get(1) ).evaluate(cfg, gp);
            arg3 = ( (TartGene)get(2) ).evaluate(cfg, gp);
            if (arg1 == 0) return arg3;
            else return arg2;

        case Grid.RAND:
            Random rgen = new Random();
            return rgen.nextInt(3);

        case Grid.SENS:
            return cfg.dozerGrid.sensor(2, 0);

        case Grid.OURS:
            int oneFront = cfg.dozerGrid.sensor(1, 0);
            int twoFront = cfg.dozerGrid.sensor(2, 0);
            if (oneFront + twoFront < 2) {
                return 2;
            } else {
                return 0;
            }

        default:
            throw new RuntimeException("Undefined function type "+node.value());
        }
    }

}
