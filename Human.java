package ZombieSimulator;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * August 2020 Semester 2 @ AUT
 * Data Structures and Algorithms: Assignment 1
 * Question 1: Zombie Simulator
 * 
 * This class creates Human objects as threads for the Zombie Simulator. Humans are twice the speed of
 * zombies and can only see 1/4 of the world around them. If a zombie in sight the human will change
 * direction and travel in the opposite direction away from the direction that the zombie is travelling in. 
 * If there are no zombies within a human's (limited) sight (or if there are no zombies existing at all) 
 * then the human will move around randomly.
 * 
 * If a human (or zombie)
 * 
 * @author Megan ghq8692
 */
public class Human implements Runnable {
    // Movement is represented as delta of x and y (dx and dy)
    protected double x, y;
    protected double dx, dy; 
    // Size is used to determine size in GUI and also affects if a human hits a wall and needs to 
    // turn around and also if it is touching a zombie (and gets infected)
    protected double size = 10; 
    public boolean isAlive;
    public static int worldWidth, worldHeight; // dimensions of GUI window (world bounds)
    protected double maxSpeed = 6;
    protected int sightDistance;
    protected Random generator = new Random();
    protected List<Human> others;
    private Thread thread;
    protected int totalSteps = 0;
    protected int nStepsTaken = 0;
    
    /**
     * The constructor takes in the starting location xy and also shares a resource which is list
     * of all humans. The list of all humans is used by the MainGUI class so that the information
     * for all human threads can be obtained and drawn inside the GUI.
     * @param others List of all Human threads
     * @param x X location of human in the world (GUI window)
     * @param y Y location of human in the world (GUI window)
     */
    public Human(List<Human> others, double x, double y) {
        this.others = others;
        this.x = x;
        this.y = y;
        this.sightDistance = (int) 0.25 * (worldWidth * worldHeight);
        setRandomDirection(this.maxSpeed); //sets random dx and dy limited by maxSpeed
        this.nStepsTaken = 0;
        generateNewTotalSteps();
        this.thread = new Thread(this);
        this.thread.start();
    }
    
    /**
     * Threads have a run() method (implemented here with the Runnable interface). The
     * thread is run when the start() method is called (which is done in the constructor
     * when a Human thread is instantiated. A boolean flag is used to stop/start a thread,
     * in this case the flag variable is isAlive.
     */
    @Override
    public void run() {
        this.isAlive = true;
        
        while (isAlive) {
            move();
        }
    }
    
    /**
     * Updates the Human thread's xy direction by a delta dx and dy. A random number of steps is
     * first generated to define how long a human will walk in one direction before it changes
     * direction. If a human hits a window (or world) boundary, it will travel in the opposite
     * direction to the boundary, so it will never travel outside the world.
     * The human thread also needs to sleep after each movement (of dx and dy) to delay
     * the movement and create visible animation in the GUI.
     */
    public void move() {
        // Checks if Human has hit a world boundary, if so, it will change dx or dy turn around
        checkWorldEdgeIntersect();
         
        // If no humans and time to change directions, generate random direction
        if (this.nStepsTaken >= this.totalSteps) {
            setRandomDirection(this.maxSpeed);
            generateNewTotalSteps();
        }
        
        // Keep travelling in the same direction if the total steps have been completed, regardless of whether humans exist or not
        x += dx;
        y += dy;
        
        // Threads need to sleep to delay movement for animation. Otherwise, objects would move too fast to be noticable
        try {
            Thread.sleep((int)20);
        } catch (InterruptedException e) {
            // ignore if a thread fails to sleep, continue on
        }
        this.nStepsTaken++;
    }
    
    /**
     * Sets random dx and dy values with the maxSpeed as the upper limit.
     * dx and dy will never be 0 to prevent the case where object comes to stand still.
     * @param maxSpeed maximum speed of object as Double
     */
    protected void setRandomDirection(double maxSpeed) {
        dx = generateRandRange((int) this.maxSpeed);
        dy = generateRandRange((int) this.maxSpeed);
    }
    
    /**
     * Generates random integer representing total number of steps object will
     * travel in one direction before changing directions
     * @return 
     */
    protected int setTotalSteps() {
        return generator.nextInt(20)+10;
    }
    
    /**
     * Helper function that randomly generates numbers to use for dx and dy and
     * excludes 0 to prevent objects from being stationary if dx =0 and dy=0.
     * @param range integer representing the max speed of an object
     * @return an integer used to set dx and dy attributes (object speed)
     */
    protected int generateRandRange(int range) {
        ArrayList<Integer> randNums = new ArrayList<>();
        int max = Math.abs(range);
        int min = -max;
        for (int i = max; i >= min; i-- ) {
            if (i!= 0) {
                randNums.add(i);
            }
        }
        int randNum = randNums.get(generator.nextInt(randNums.size()));
        return randNum;
    }
    
    /**
     * Generates total number of steps for human thread to walk in one direction before a
     * new direction is generated.
     */
    protected void generateNewTotalSteps() {
        this.totalSteps = setTotalSteps();
        this.nStepsTaken = 0;
    }
    
    /**
     * Helper function used to calculate the distance between objects
     * Zombies need to find the Human with the closest distance to chase
     * Humans need to find the Zombie with the closest distance to run away from
     * @param human object to calculate distance to
     * @return distance as a Double
     */
    protected double calculateDistance(Human human) {
        return Math.hypot(x-human.x, y-human.y);
    }
    
    /**
     * Checks if a Human/Zombie has reached the world boundary or is about to reach the world
     * boundary at the next move() step, and if so, it changes direction.
     */
    protected void checkWorldEdgeIntersect() {
        // If human hits top edge of world
        if (y - size + dy <= 0) 
            dy = -dy;
        
        // If human hits bottom edge of world
        if (y + size + dy >= worldHeight) 
            dy = -dy;
        
        // if human hits left edge of world
        if (x - size + dx <= 0) 
            dx = -dx;
        
        // if human hits right edge of world
        if (x + size + dx >= worldWidth) 
            dx = -dx;
    }
    
    /**
     * Sets boolean flag to false to trigger the thread to stop executing the move() method
     */
    public void kill() {
        this.isAlive = false;
    }
    
    /**
    * Draws Human in the GUI as blue-filled circles with black outlines 
    */
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillOval((int) x, (int) y, (int) size, (int) size);
        g.setColor(Color.BLACK);
        g.drawOval((int) x, (int) y, (int) size, (int) size);
    }
    
    /**
     * Resource shared amongst all Human threads. Returns a list of all Human objects currently
     * existing in the world.
     * @return Returns a list of all Human objects currently existing in the world.
     */
    public List<Human> getOthers() {
        return this.others;
    }
    
    /**
     * Returns current X location of object (implementing encapsulation)
     * @return Double value of current X location of object
     */
    public double getX() {
        return this.x;
    }
    
    /**
     * Returns current Y location of object (implementing encapsulation)
     * @return Double value of current Y location of object
     */
    public double getY() {
        return this.y;
    }
    
    /**
     * Returns double value representing the width and height of the object.
     * (in this case objects always have the same width and height values)
     * @return Returns double value representing the width and height of the object
     */
    public double getSize() {
        return this.size;
    }
    

    /**
     * Allows for the Zombie class (subclass/child class) to set the DX value of a 
     * Human if it is within the sight distance of a human (via encapsulation)
     * @param dx 
     */
    protected void setDX(double dx) {
        this.dx = dx;
    }
    
    /**
     * Allows for the Zombie class (subclass/child class) to set the DY value of a 
     * Human if it is within the sight distance of a human (via encapsulation)
     * @param dy 
     */
    protected void setDY(double dy) {
        this.dy = dy;
    }
}