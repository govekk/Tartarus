import java.util.ArrayList;

//represents the board updates for one robot move
public class Update {
    public ArrayList<Point> points = new ArrayList<Point>();
    public ArrayList<String> types = new ArrayList<String>();

    public void addUpdate(Point point, String type) {
        points.add(point);
        types.add(type);
    }

    public String toString(){
        String str = "[";
        for(int i = 0; i < points.size(); i++){
            str += points.get(i) + " - " + types.get(i);
            if(i < points.size()-1){
                str += ", ";
            }
        }
        str += "]";
        return str;
    }

}