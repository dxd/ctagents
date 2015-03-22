/*
 * Created on Nov 29, 2004
 */

package ctgui.original;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MoodPanel extends JDialog
                             implements ActionListener {
    
    static JLabel indifferentLabel =  new JLabel (createImageIcon("smiley3.gif"));; 
    static JLabel modestLabel =  new JLabel (createImageIcon("smiley1.gif"));; 
    static JLabel discouragedLabel = new JLabel (createImageIcon("smiley2.gif"));
    
    
   
    JButton b ; 
    JPanel radioPanel ; 
    private String result;

    public MoodPanel(Frame f) {
    	//getContentPane()
       super(f, "How do you feel right now? ", true) ; 
       //this.setBounds(100, 100, 300, 50);

        //Create the radio buttons.
        
        JRadioButton modestButton = new JRadioButton() ; 
        modestButton.setMnemonic(KeyEvent.VK_B);
        modestButton.setSelected(false);
        modestButton.setActionCommand("modest");
        JPanel modestPanel= new JPanel() ;
        JPanel modestPanel2 = new JPanel () ; 
        modestPanel.setLayout (new BorderLayout () ) ; 
        modestPanel2.add(modestButton) ;
        modestPanel.add(modestLabel, BorderLayout.NORTH) ;
        modestPanel.add(modestPanel2, BorderLayout.SOUTH) ;

        
        JRadioButton indifferentButton = new JRadioButton() ; 
        indifferentButton.setMnemonic(KeyEvent.VK_B);
        indifferentButton.setSelected(false);
        indifferentButton.setActionCommand("indifferent");
        JPanel indifferentPanel= new JPanel() ;
        JPanel indifferentPanel2 = new JPanel () ; 
        indifferentPanel.setLayout (new BorderLayout () ) ; 
        indifferentPanel2.add(indifferentButton) ;
        indifferentPanel.add(indifferentLabel, BorderLayout.NORTH) ;
        indifferentPanel.add(indifferentPanel2, BorderLayout.SOUTH) ;

        
        JRadioButton discouragedButton = new JRadioButton() ; 
        discouragedButton.setMnemonic(KeyEvent.VK_B);
        discouragedButton.setSelected(false);
        discouragedButton.setActionCommand("discouraged");
        JPanel discouragedPanel= new JPanel() ;
        JPanel discouragedPanel2 = new JPanel () ; 
        discouragedPanel.setLayout (new BorderLayout () ) ; 
        discouragedPanel2.add(discouragedButton) ;
        discouragedPanel.add(discouragedLabel, BorderLayout.NORTH) ;
        discouragedPanel.add(discouragedPanel2, BorderLayout.SOUTH) ;

        
        
        //Group the radio buttons.
       final  ButtonGroup group = new ButtonGroup();
        group.add(modestButton);
        group.add(indifferentButton);
        group.add(discouragedButton);
  

        //Register a listener for the radio buttons.
        
        modestButton.addActionListener(this);
        indifferentButton.addActionListener(this);
        discouragedButton.addActionListener(this);
        
        
      
        //The preferred size is hard-coded to be the width of the
        //widest image and the height of the tallest image.
        //A real program would compute this.
      //  picture.setPreferredSize(new Dimension(177, 122));
        this.setPreferredSize(new Dimension(200, 150));


        //Put the radio buttons in a column in a panel.
        radioPanel = new JPanel(new FlowLayout () ) ; 
        
        		
      
        radioPanel.add(modestPanel);
        radioPanel.add(indifferentPanel);
        radioPanel.add(discouragedPanel);
     
        
        
        //setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
        
        b = new JButton ("OK") ;
        b.addActionListener (new ActionListener () {
            public void actionPerformed (ActionEvent ev) {
                ButtonModel m ; 
                if (group.getSelection() != null) {
                    result = group.getSelection().getActionCommand();
                    setVisible(false) ;
                    dispose () ; 
                }
            }
        }) ;
		
    
    
    }
    
    

    /** Listens to the radio buttons. */
    public void actionPerformed(ActionEvent e) {
        
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        path = "images/" + path;
        java.net.URL imgURL  = ClassLoader.getSystemResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
   public  void createAndShowGUI() {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

      
        //Create and set up the content pane.
       getContentPane().setLayout (new BorderLayout () ) ;
       getContentPane().add(radioPanel, BorderLayout.LINE_START);
     
       JPanel p = new JPanel () ; 
       p.add(b) ; 
       
       getContentPane().add(p, BorderLayout.SOUTH);
        //rame.getContentPane().add (new BorderLayout () ) ;
        //JComponent newContentPane = new MoodPanel();
        //frame.setOpaque(true); //content panes must be opaque
      //  frame.setContentPane(newContentPane);

        //Display the window.
       pack();
       setVisible(true);
    }
   
   public String getResult(){
       return(result);
   }
}
