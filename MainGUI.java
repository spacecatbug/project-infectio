package ZombieSimulator;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;

/**
 * August 2020 Semester 2 @ AUT
 * Data Structures and Algorithms: Assignment 1
 * Question 1: Zombie Simulator
 * 
 * This class maintains the GUI for the Zombie Simulator and includes a main driver method to
 * execute the program. 
 * 
 * The GUI is built using Swing JComponents. It includes buttons with action listeners so that
 * humans and zombies can be added with button clicks. It also includes a Swing Timer object -
 * Timer objects register action listeners and are used to periodically trigger actions performed.
 * The Timer in this GUI is used to repaint the GUI components to create the visual animations and
 * also to check if a human has been killed. When a human is killed, it is removed from the list
 * of humans and re-instantiates the object as a zombie thread starting from the same location to
 * represent a human turning into a zombie. 
 * 
 * @author Megan ghq8692
 */
public class MainGUI extends JPanel implements ActionListener {
    
    private JButton addHumanButton, addZombieButton;
    private DrawPanel drawPanel;
    private Timer timer;
    // Lists of humans and zombies are maintained for the GUI to draw
    public static ArrayList<Human> humanList = new ArrayList<>();
    public static ArrayList<Human> zombieList = new ArrayList<>();
    
    /**
     * MainGUI is a subclass of JPanel and builds the components required for the GUI (JComponent
     * objects), adds the required action listeners and starts a Swing Timer that is used to
     * perform repeated actions
     */
    public MainGUI() 
    {
        // Passing border layout manager to the JPanel. Border Layout manager divides
        // the JPanel into sections: north, south, center, west and east and GUI components can
        // be added to these sections
        super(new BorderLayout());
        JPanel southPanel = new JPanel();
        addHumanButton = new JButton("Add human");
        addZombieButton = new JButton("Add zombie");
        // Registering action listeners so that actions can be performed if triggered with button clicks
        addHumanButton.addActionListener(this);
        addZombieButton.addActionListener(this);
        southPanel.add(addHumanButton);
        southPanel.add(addZombieButton);
        add(southPanel,BorderLayout.SOUTH);
        
        drawPanel = new DrawPanel();
        add(drawPanel,BorderLayout.CENTER);
        
        // The Swing timer object registers an action listener with a delay in milliseconds
        // representing how often to repaint the GUI components.
        timer = new Timer(5,this);
        timer.start();
    }
    
    /**
     * ActionPerformed is invoked whenever actions occur. In MainGUI, actions occur when the buttons are
     * clicked and when the timer triggers an action performed (the timer triggers actions every 5 
     * milliseconds in this instance). 
     * 
     * When the add human/zombie button is pressed, a new human/zombie object is created
     * and is added to the MainGUI's list of humans/zombies.
     * 
     * When the timer triggers an action (every 5 milliseconds) it repaints the GUI to update it. As
     * zombies/humans move around, their locations change, so the GUI needs to update their locations to
     * create animation in the GUI. The second timer action is to check to see if any humans are killed,
     * and if they are, re-instantiate them as zombies.
     * @param e The ActionEvent object
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source.equals(addHumanButton)) {
            Human human = new Human(humanList, Double.valueOf(drawPanel.getWidth()/2), Double.valueOf(drawPanel.getHeight()/2));
            humanList.add(human);
        }
        if (source.equals(addZombieButton)) {
            Zombie zombie = new Zombie(humanList, Double.valueOf(drawPanel.getWidth()/2), Double.valueOf(drawPanel.getHeight()/2) );
            zombieList.add(zombie);
        }
        if (source.equals(timer)) {
            drawPanel.repaint();
            checkKill();
        }
    }
    
    /**
     * Checks through the list of humans to check if the boolean flag isAlive is false for any human, which
     * signals that the thread should be killed. It re-instantiates a new zombie thread in the same 
     * location as where the human thread was killed (i.e. where the human was infected). The 'killed' human
     * is removed from the MainGUI's human list and the new zombie is added to the MainGUI's zombie list.
     * 
     * This method has the keyword "synchronized" which means that it is thread safe and only one thread has
     * access to this method at a time. Synchronisation should only be isolated to critical sections of code
     * as to not completely slow down the code and reduce the process of multi-threading. Critical section of
     * code refers to a code segment where same variables are accessed multiple times by multiple threads (this
     * method controls a human being removed from the humanList and re-instantiated as a new zombie, which is 
     * then added to the zombie list). Synchronisation here avoids an concurrency error that would be caused in
     * the scenario that two zombies are very close to each other and kill a human at the same time (or almost 
     * the same time) resulting in multiple attempts to remove the single human from the humanList.
     */
    public synchronized void checkKill() {
        Human killedHuman = null;
        boolean removeHuman = false;
        for (Human human: humanList) {
                if (human.isAlive == false) {
                    killedHuman = human;
                    double humanX = human.getX();
                    double humanY = human.getY();
                    zombieList.add(new Zombie(human.others, humanX,humanY));
                    removeHuman = true;
                }
            }
            if (removeHuman && killedHuman != null) {
                humanList.remove(killedHuman);
            }
    }
    
    /**
     * Inner class that represents the window that the humans and zombies move around in.
     * It is a subclass of JPanel and the paintComponent method is overridden to define
     * that each time the human/zombie window is drawn/redrawn it needs to cycle through
     * the list of all humans and zombies and draw each graphic representing each human/
     * zombie thread.
     */
    private class DrawPanel extends JPanel 
    {
        public DrawPanel() 
        {
            super();
            setPreferredSize(new Dimension(500,500));
            setBackground(Color.WHITE);
        }
        
        @Override
        public void paintComponent(Graphics g) 
        {
            super.paintComponent(g);
            Human.worldHeight = getHeight();
            Human.worldWidth = getWidth();

            for (Human human: humanList) {
                human.draw(g);
            }
            
            for (Human zombie: zombieList) {
                zombie.draw(g);
            }
        }
    }
    
    /**
     * Main driver method that executes the program.
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Zombie Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainGUI());
        frame.pack();
        frame.setVisible(true);
    }
}
