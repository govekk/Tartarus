import java.util.ArrayList;

public class RobotRun {
    public int fitness;
    public int generation = -1;
    public int gridNum;
    private int treeIndex = 0;
    public ArrayList<Update> updates = new ArrayList<Update>();
    //stores which steps are the beginning of a new tree evaluation
    public ArrayList<Integer> treeEvals = new ArrayList<Integer>();

    public void addUpdate(Update up){
        updates.add(up);
    }

    public void addTree(Integer step) {
        treeEvals.add(step);
    }

    public Integer getNextTreeEval(){
        if(treeEvals.isEmpty()){
            return -1;
        }
        treeIndex++;
        return treeEvals.get(0);
    }

    public void reset(){
        treeIndex = 0;
    }

}