/* Code by Sherri Goings
For Darwin Final Project in CS201 (Data Structures)
Last Modified for Fall 2011 Term
--------------------------------------------------------------------------------------------------
The Point class stores a 2D point with integer x and y values. Also a toString() to 
get a String representation of the Point.
--------------------------------------------------------------------------------------------------*/

public class Point {
    public int x;
    public int y;

    public Point(int xx, int yy) {
        x = xx;
        y = yy;
    }

// returns a formatted String representation of the current Point
    public String toString() {
        return "("+x+","+y+")";
    }

    public boolean equals(Point p2) {
        if (x==p2.x && y==p2.y)
            return true;
        return false;
    }
}