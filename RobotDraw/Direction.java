
public enum Direction { 
    // constants for the allowed values of a direction
    NORTH, EAST, SOUTH, WEST; 

    public static Direction getAt(int ord) {
        return Direction.values()[ord];
    }

    // gets the direction to the right of this direction
    public Direction getRight() {
        if (this == WEST)
            return NORTH;
        else
            return Direction.values()[this.ordinal() + 1];
    }

    // gets the direction to the left of this direction
    public Direction getLeft() {
        if (this == NORTH)
            return WEST;
        else
            return Direction.values()[this.ordinal() - 1];
    }

    // shows example usage of the enum direction
    public static void main(String[] args) {

        /* note there's no constructor for an enum, it is by definition
        a static class.  You simply set a variable of type Direction
        to be one of the allowable values as follows to create a Direction instance. */
        Direction dir1 = Direction.NORTH;

        /* the provided getAt(index) function gets the (index+1)th direction
        from the list of allowed Direction values in the order defined
        in the enum above. So dir2 will be the Direction SOUTH
        */ 
        Direction dir2 = Direction.getAt(2);

        /* Every enum type inherits by default from Java's base enum class,
        in the same way as every class you define automatically inherits
        from class Object. The Java enum base class has a toString() method
        that simply returns the constant value of the enum instance as a string. */
        System.out.println(dir1);
        String dir2AsString = dir2.toString();
        System.out.println(dir2AsString);

        /* I have provided you with functions to get the direction to the right or
        left of a given direction. These are not static and so must be called on
        an Object of type Direction, as follows. */
        Direction dir3 = dir1.getRight();
        Direction dir4 = dir1.getLeft();
        System.out.println(dir3 + " " + dir4);    

        // For more information on enum types in general, look at the Java tutorial
        // section on enums here: http://download.oracle.com/javase/tutorial/java/javaOO/enum.html 
    }
}