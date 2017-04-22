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

import java.util.Random;

/**
 * Manages a static instance of the standard Random class, and
 * adds a couple of random number methods that are of use to gpjpp.
 * GPRandom cannot be instantiated; all members are static. It is
 * designed this way in order to avoid having to pass an instance
 * variable to various and dispersed methods of gpjpp.
 *
 * @version 1.0
 */
public class GPRandom {

    //not intended to be instantiated since all members are static
    private GPRandom() {}

    /**
     * A static instance of a Random that is used throughout the
     * gpjpp package. This must be valid before creating a
     * population. It is instantiated automatically by the 
     * <a href="gpjpp.GPRun.html#_top_">GPRun</a> class.
     */
    static protected Random r = null;

    /**
     * Sets the static field r to an instance of Random.
     */
    static public void setGenerator(Random ran) { r = ran; }

    /**
     * Returns a random integer uniformly distributed in the range
     * 0 to limit-1. limit must 0 or greater.
     */
    static public int nextInt(int limit) {
        return (int)Math.floor(limit*r.nextDouble());
    }

    /**
     * Returns true if a random real in the range from 0.0 to 100.0
     * is less than the specified percent. Thus, if percent is 50.0,
     * the result has equal probability of returning true or false.
     */
    static public boolean flip(double percent) {
        return (r.nextDouble()*100.0 < percent)? true : false;
    }

    /**
     * Returns a random double uniformly distributed in the range
     * 0.0 to 1.0. Simply calls r.nextDouble().
     */
    static public double nextDouble() {
        return (r.nextDouble());
    }
}
