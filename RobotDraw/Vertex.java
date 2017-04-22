import java.util.*;

public class Vertex {
    private Point point;
    private LinkedList<Vertex> edges;
    private boolean visited;
    private Vertex prev;
    
    public Vertex(Point p) {
        point = p;
        edges = new LinkedList<Vertex>();
        prev = null;
        visited = false;
    }
    
    public String toString() {
        return "("+point.x+","+point.y+")";
    }
    
    public LinkedList<Vertex> getEdges() {
        return edges;
    }

    public void setPrev(Vertex pre) {
        prev = pre;
    }

    public Vertex getPrev() {
        return prev;
    }

    public boolean isVisited() { return visited; }
    
    public void visit() { visited = true; }

    public Point getPoint() { return point; } 
}