import java.io.*;
import java.lang.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;

/* Class the simulates a robot trying to push blocks to the edges */
public class RobotSim {
    private static final int size = 80;
    private final int width = 6;
    private final int height = 6;
    private double pause = 0.1;
    private boolean tart2 = true;
    private BufferedImage[] robots = new BufferedImage[4];
    private BufferedImage block = null;
    private BufferedImage back = null;
    private ArrayList<RobotRun> runs = new ArrayList<RobotRun>();

    public void init(String filename, int gender, double pause, int tart){
        
        this.pause = pause;

        Draw.initGraphicsWindow((width+2)*size, (height+2)*size, "DozerWorld" );
        Draw.setPenColor(Draw.BLACK);
        
        //display instructions
        Draw.setFont(new Font("SansSerif", Font.PLAIN, 20));
        Draw.text(4*size,3.6*size, "Use > and < arrow keys to move between the runs,");
        Draw.text(4*size,4*size, "r repeats the current run, and q quits");
        Draw.setFont(new Font("SansSerif", Font.PLAIN, 16));
        Draw.text(4*size,4.4*size, "(click to start)");
        
        while(!Draw.mousePressed()){
            pause(0.02);   
        }

        Draw.setPenColor(Draw.WHITE);
        Draw.filledRectangle(4*size,4*size, (width+2)*size/2, (height+2)*size/2);

        try {
            back = ImageIO.read(new File("background" + gender + ".gif"));
        } catch (IOException e) {
            System.out.println("Problem Reading Background");
            return;
        }

        Draw.setPenColor(Draw.BLACK);
        Draw.setFont(new Font("SansSerif", Font.PLAIN, 20));
        if(tart <= 2){
            Draw.text(4*size,0.3*size, "Tartarus " + tart);
            if(tart == 1){
                tart2 = false;
            }
        } else {
            Draw.text(4*size,0.3*size, "Neural Network");
            tart2 = false;
        }
        Draw.setFont(new Font("SansSerif", Font.PLAIN, 16));
        Draw.setPenColor(Draw.BLUE);

        //load robot and block images
        BufferedImage robot = null;
        try {
            block = ImageIO.read(new File("block" + gender + ".gif"));
            for(int i = 0; i < 4; i++) {
                robot = ImageIO.read(new File("robot"+ gender + "-" + i + ".gif"));
                robots[i] = robot;
            }
        } catch (IOException e) {
            System.out.println("Problem Reading Block or Robot");
            return;
        }
        loadRobotFile(filename, tart);
        simulateRobot();
        System.exit(0);
    }

    public void updateSquare(Point p, String type) {
        Draw.image(back, size*(p.x+1)+2, size*(p.y+1)+2, size*(p.x+2)-2, size*(p.y+2)-2, size*(p.x)+2, size*(p.y)+2, size*(p.x+1)-2, size*(p.y+1)-2);  
        if (type.equals("b")) {
            Draw.image(block, (p.x+1)*size+3, (p.y+1)*size+3, (p.x+2)*size-2, (p.y+2)*size-2);
        } else if(type.equals("<") || type.equals("^") || type.equals(">") || type.equals("v")) { //draw robot
            Direction dir = null;
            char dirtype = type.charAt(0);
            switch(dirtype){
                case '^':
                    dir = Direction.NORTH;
                    break;
                case '>':
                    dir = Direction.EAST;
                    break;
                case 'v':
                    dir = Direction.SOUTH;
                    break;
                case '<':
                    dir = Direction.WEST;
                    break;
            }
            drawRobot(p, dir);
        }    
    }

    private void drawRobot(Point p, Direction dir) {
        Draw.image(robots[dir.ordinal()], (p.x+1)*size+3, (p.y+1)*size+3, (p.x+2)*size-2, (p.y+2)*size-2);
    }

    public void resetGrid() {
        for (int i=1; i<width+1; i++) {
            for (int j=1; j<height+1; j++) {
                Draw.image(back, size*i, size*j, size*(i+1), size*(j+1), size*(i-1), size*(j-1), size*i, size*j);
                Draw.setPenColor(Draw.BLACK);
                Draw.square(size*i+size/2,size*j+size/2, size/2);
            }
        }
    }

    public void pause() {
        try { 
            Thread.currentThread().sleep((int)(pause*1000));
        } catch (InterruptedException e) {
            System.out.println("Error sleeping");
        }
    }

    public void pause(double pauseTime) {
        try { 
            Thread.currentThread().sleep((int)(pauseTime*1000));
        } catch (InterruptedException e) {
            System.out.println("Error sleeping");
        }
    }

    /* Updates the grids, tree evals, and steps */
    public void updateStats(int gridNum, int treeNum, int steps) {
        Draw.setPenColor(Draw.WHITE);
        Draw.filledRectangle(4*size,(height+1.5)*size, 2*size, 10);
        Draw.setPenColor(Draw.BLUE);
        if(tart2){
            Draw.text(4*size,(height+1.5)*size, "Grid:  " + gridNum + "   Steps:  " + steps + "   Tree Evals:  " + treeNum);
        } else {
             Draw.text(4*size,(height+1.5)*size, "Grid:  " + gridNum + "   Steps:  " + steps);
        }
    }

    public void updateGridFit(int gridFit) {
        Draw.setPenColor(Draw.WHITE);
        Draw.filledRectangle(7*size,0.7*size, 100, 20);
        Draw.setPenColor(Draw.BLUE);
        Draw.text(7*size,0.7*size, "Fitness: " + gridFit);
    }

    public void updateGeneration(int generation) {
        Draw.setPenColor(Draw.WHITE);
        Draw.filledRectangle(size,0.7*size, 100, 20);
        Draw.setPenColor(Draw.BLUE);
        Draw.text(size,0.7*size, "Gen: " + generation);
    }

    public void updateMessage(String message) {
        Draw.setFont(new Font("SansSerif", Font.PLAIN, 12));
        Draw.text(4*size,0.7*size, message);
        Draw.setFont(new Font("SansSerif", Font.PLAIN, 16));
    }

    public void clearMessage() {
        Draw.setPenColor(Draw.WHITE);
        Draw.filledRectangle(4.*size,0.8*size, 100, 15);
    }

    /* Takes the output tartarus output file and simulates the robot on each grid */
    public void loadRobotFile(String filename, int tart) {
        Scanner in = null;
        try {
            in = new Scanner(new File(filename));
        } catch (IOException e) {
            System.out.println("Error reading " + filename);
            return;
        }
        
        String line = null;
        Update curUpdate = new Update();
        RobotRun curRun = new RobotRun();
        int x, y;
        int steps = -1;
        int grid = 0;
        int gridFit = 0;
        int generation = 0;
        while(in.hasNextLine()) {
            line = in.nextLine();
            //represents end of one complete dozer move
            if(line.equals("*******")){
                steps++;
                curRun.addUpdate(curUpdate);
                curUpdate = new Update();
            } else if(line.equals("-------")) { //finished tree eval
                curRun.addTree(steps);
            } else if(line.indexOf("New Generation") != -1) {
                generation += Integer.parseInt(line.substring(line.length()-3).trim());
                grid = 0;
            } else if (line.indexOf("Fitness =") != -1) {
                curRun.fitness = Integer.parseInt(line.substring(line.length()-2).trim());
                runs.add(curRun);
                curRun = new RobotRun();
            } else if(line.equals("New Grid")) { //new grid
                grid++;
                steps = -1;
                curRun.gridNum = grid;
                curRun.generation = generation;
            } else {
                x = Integer.parseInt(line.substring(0,1));
                y = Integer.parseInt(line.substring(2,3));
                curUpdate.addUpdate(new Point(x,y), line.substring(4,5));
            }
        }
    }

    public void simulateRobot() {
        int curRun = 0;
        RobotRun run;
        boolean keyPressed = false;
        int steps, trees, grid, gridFit, generation, nextTree;
        while(curRun >= 0 && curRun < runs.size()) {
            run = runs.get(curRun);
            run.reset();
            resetGrid();
            steps = -1;
            trees = 1;
            nextTree = run.getNextTreeEval();
            gridFit = run.fitness;
            generation = run.generation;
            grid = run.gridNum;
            updateStats(grid, trees, steps);
            updateGeneration(generation);
            updateGridFit(gridFit);
            for(Update up : run.updates){
                if (Draw.hasNextKeyTyped()) {
                    char key = Draw.nextKeyTyped();
                    if (key == 'r') {
                        keyPressed = true;
                        break;
                    } else if(key == '.') {
                        curRun++;
                        keyPressed = true;
                        break;
                    } else if(key == ',') {
                        curRun--;
                        keyPressed = true;
                        break;
                    } else if(key == 'q') {
                        return;
                    }
                }
                for(int i = 0; i < up.points.size(); i++) {
                    updateSquare(up.points.get(i), up.types.get(i));
                }
                if(steps == nextTree){
                    trees++;
                    nextTree = run.getNextTreeEval();
                }
                steps++;
                updateStats(grid, trees, steps);
                pause();
            }
            if(!keyPressed) {
                updateMessage("Press <, >, or r to continue");
            }
            while(!keyPressed){
                pause(0.5);
                if(Draw.hasNextKeyTyped()){
                    char key = Draw.nextKeyTyped();
                    if(key == 'r'){
                        keyPressed = true;
                    } else if (key == '.') {
                        keyPressed = true;
                        curRun++;
                    } else if (key == ',') {
                        keyPressed = true;
                        curRun--;
                    } else if (key == 'q') {
                        return;
                    }
                }
            }
            clearMessage();
            keyPressed = false;
        }
    }

    public static void main(String[] args) {
        int gender = 1;
        double pause = 0.5;
        int tart = 2;
        String filename = null;
        if(args.length < 1){
            System.out.println("Must provide filename and integer");
            System.out.println("Optional arguments: java RobotSim <filename> <tartarus integer (1 or 2)> <gender (1 or 2)> <double pause time sec> \n Default: java RobotSim <filename> <integer> 1 0.5");
            System.out.println("For optional arguments, must provide all 2");
            return;
        } else if (args.length == 2) {
            filename = args[0];
            tart = Integer.parseInt(args[1]);
        } else if(args.length == 4) {
            filename = args[0];
            tart = Integer.parseInt(args[1]);
            gender = Integer.parseInt(args[2]);
            pause = Double.parseDouble(args[3]);
        } else {
            System.out.println("Optional arguments: java RobotSim <filename> <tartarus integer (1 or 2)> <gender (1 or 2)> <double pause time sec> \n Default: java RobotSim <filename> <integer> 1 0.5");
        }
        RobotSim m = new RobotSim();
        m.init(filename, gender, pause, tart);
    }

}
