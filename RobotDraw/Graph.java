/* Code by Sherri Goings
For Darwin Final Project in CS201 (Data Structures)
Last Modified for Fall 2011 Term
--------------------------------------------------------------------------------------------------
The WorldMap class handles all of the graphics in the Darwin simluation. It depends on the 
Draw class for the actual drawing, and also the Point class and Direction enum for other operations. 
--------------------------------------------------------------------------------------------------*/

import java.util.*;

public class Graph {
    private Vertex[][] vertices;

    public Graph(int width, int height) {
        vertices = new Vertex[width][height];
    }

    public void addVertex(Vertex v) {
        int x = v.getPoint().x;
        int y = v.getPoint().y;
        vertices[x][y] = v;

        Point edgeP1 = new Point(x-1,y);
        if (x>0 && hasVertex(edgeP1)) 
            addEdge(v.getPoint(),edgeP1);

        Point edgeP2 = new Point(x,y-1);
        if (y>0 && hasVertex(edgeP2))
            addEdge(v.getPoint(),edgeP2);
    }

    public Vertex getVertex(Point point) {
        return vertices[point.x][point.y];
    }

    public void printGraph() {
        for (Vertex[] cols : vertices) {
            for (Vertex v : cols) {
                if (v==null)
                    continue;
                System.out.print(v+": ");
                for (Vertex e : v.getEdges()) 
                    System.out.print(e+", ");
                System.out.println();
            }
        }
    }

    public void addEdge(Point Pa, Point Pb) {
        addEdge(vertices[Pa.x][Pa.y],vertices[Pb.x][Pb.y]);
    }

    public void addEdge(Vertex Va, Vertex Vb) {	
        if (!Va.getEdges().contains(Vb))
            Va.getEdges().add(Vb);
        if (!Vb.getEdges().contains(Va))
            Vb.getEdges().add(Va);
    }

    public boolean getEdge(Vertex Va, Vertex Vb) {
        if (Va.getEdges().contains(Vb))
            return true;
        return false;
    }

    public boolean hasVertex(Point p) {
        if (vertices[p.x][p.y]!=null)
            return true;
        return false;
    }
}