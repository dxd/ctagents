package ctgui.original;

import edu.harvard.eecs.airg.coloredtrails.agent.events.GameStartEventListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.harvard.eecs.airg.coloredtrails.alglib.ShortestPaths;
import edu.harvard.eecs.airg.coloredtrails.shared.Scoring;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.client.ui.ColoredTrailsBoardPanel;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Path;


/**
 * Graphical Path Finder, for helping the player while making proposals.
 * @author ilke
 *
 */
public class PathFinderWindow extends JFrame implements Observer, GameStartEventListener {
	private JPathFinderPanel pathFinderPanel;
	private JPathFinderFrame pathFinderFrame;
	private ArrayList<Path> shortestpaths;
	
	public PathFinderWindow() {
		super("Colored Trails: Path Finder");
                
                GUI.getAgent().getGameStatus().addObserver(this);
                GUI.getAgent().addGameStartEventListener(this);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		pathFinderFrame = new JPathFinderFrame();
		pathFinderFrame.setBackground(Color.white);
		pathFinderPanel = new JPathFinderPanel(pathFinderFrame);

		JPanel pfp = new JPanel();
		pfp.setBackground(Color.WHITE);
		pfp.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.black), "Path Finder Controls"));
		pfp.add(pathFinderPanel);

		JPanel pff = new JPanel();
		pff.setBackground(Color.WHITE);
		pff.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createLineBorder(Color.black), "Path Finder Results"));
		pff.add(pathFinderFrame);

		Box pathFinderContainer = Box.createVerticalBox();
		pathFinderContainer.add(pfp, BorderLayout.CENTER);
		pathFinderContainer.add(pff, BorderLayout.CENTER);
		pathFinderContainer.setVisible(true);

		getContentPane().add(pathFinderContainer);
		this.setLocation(300,110);
		pack();
	}
        
        public void reset()
        {
            pathFinderPanel.reset();
        }
	
	class JPathFinderPanel extends JPanel implements ActionListener {

		private GridBagLayout gridbag = new GridBagLayout();

		private GridBagConstraints constraints = new GridBagConstraints();
		private JComboBox playerComboBox;
		private PlayerModel playerModel;
		private ChipSetInputPanel chipChooser;
		private JButton pathFinder;
		private JButton reset;
		private JPathFinderFrame pathFinderFrame;
		
		/**
		 * When an action performed on the panel, takes the necessary action
		 */
		public void actionPerformed(ActionEvent event) {
                        if( event.getSource() == playerComboBox ) {
				System.out.println( "playerComboBox changed" );
				ClientGameStatus cgs = Taskbar.getInstance().getAgent().getGameStatus();
				PlayerStatus ps = cgs.getPlayerByPerGameId(playerComboBox.getSelectedIndex());
				ChipSet cs = ps.getChips();
				cs = cs.getNegation(cs);
				
				int[] mins = new int[cgs.getGamePalette().size()];
				
				for(int i=0; i<mins.length; i++) {
					mins[i] = 0;
				}
				
				for(String color:cgs.getGamePalette().getColors()) {
					mins[cgs.getGamePalette().getNumByColor(color)-1] = cs.getNumChips(color);
				}

				chipChooser.setChipsMinsMaxs(mins, null);
				chipChooser.reset();
			}
			else {
				pathFinderFrame.erase();
				if (event.getSource() == pathFinder) {
					int id = playerModel.getPlayerIndex(playerComboBox.getSelectedIndex());
					pathFinderFrame.displayPaths(chipChooser.getChipSet(), id);
					
				} else if (event.getSource() == reset) {
					chipChooser.reset();
				}
			}
                }
    
        
        
                public void reset()
                {
                    pathFinderFrame.erase();
                    chipChooser.reset();
                }
                
		
		/**
		 * Get a box with a player label and a player selector.
		 * @return a box with a player label and a player selector
		 * @author ilke
		 */
		private JComponent getPlayerChooser() {
			ClientGameStatus cgs = Taskbar.getInstance().getAgent().getGameStatus();

			// pull down menu of the players' icons:
			playerModel = new PlayerModel(cgs, true); // withMe = true
			playerComboBox = new JComboBox(playerModel);
			playerComboBox.setRenderer(new PlayerRenderer());
			playerComboBox.setSelectedIndex(cgs.getMyPlayer().getPerGameId());
			playerComboBox.setEnabled(true);
			playerComboBox.addActionListener(this);

			JPanel playersp = new JPanel(new GridLayout(2, 1));
			
			JLabel playersl = new JLabel("Player", JLabel.CENTER);
			playersl.setOpaque(false);
			playersp.setBackground(Color.WHITE);
			playersp.add(playersl);
			playersp.add(playerComboBox);

			return playersp;
		}
		
		/**
		 * Get a box with a chip label  and a chip selector
		 * @return a box with a chip label and a chip selector
		 * @author ilke
		 */
		private JComponent getChipChooser() {
			ClientGameStatus cgs = Taskbar.getInstance().getAgent().getGameStatus();
			ChipSet negcs = cgs.getMyPlayer().getChips();
			negcs = negcs.getNegation(negcs);
			
			int[] negchips = new int[cgs.getGamePalette().size()];
			
			for(int i=0; i<negchips.length; i++) {
				negchips[i] = 0;
			}
			for(String color:cgs.getGamePalette().getColors()) {
				negchips[cgs.getGamePalette().getNumByColor(color)-1] = negcs.getNumChips(color);
			}
			
			chipChooser = new ChipSetInputPanel(negchips, null);
			//chipChooser.setBackground(Color.white);

			Box chipsp = Box.createVerticalBox();
			JPanel b = new JPanel(new BorderLayout());
			JLabel chipsl = new JLabel("Chips Changes", JLabel.CENTER);
			b.add(chipsl);
			b.setOpaque(false);
			chipsl.setOpaque(false);
			chipsp.setBackground(Color.WHITE);
			chipsp.add(b);
			chipsp.add(chipChooser);

			return chipsp;
		}

		
		/**
		 * Get relevant buttons
		 * @author ilke
		 */
		private JComponent getButtons() {
			Box buttons = Box.createVerticalBox();

			pathFinder = new JButton("Find Path");
			pathFinder.addActionListener(this);
			buttons.add(pathFinder);

			reset = new JButton("   Reset   ");
			reset.addActionListener(this);
			gridbag.setConstraints(buttons, constraints);
			buttons.add(reset);

			return buttons;
		}
		
		public JPathFinderPanel(JPathFinderFrame pathFinderFrame) {
			this.pathFinderFrame = pathFinderFrame;
			this.pathFinderFrame.erase();

			setBackground(Color.white);
			add(getPlayerChooser());
			add(getChipChooser());
			add(getButtons());

			updateUI();
		}
		
	}
	
	/**
	 * The panel contains three tables showing summary information (read
	 * # of moves, total required chips, total missing chips) of all paths and path-specific
	 * information (read chip color and direction). 
	 */
	class JPathFinderFrame extends JPanel implements ItemListener {
		
		int currentlySelectedPathNum;
		int currentlyDisplayedID;
		PathOverviewTable pathOverviewTable; 
		JTable pathOverviewJtableContainer; 
		JScrollPane pathOverviewScrollPane;
		JCheckBox displaypaths = new JCheckBox("Display Path On Board");
		ClientGameStatus cgs = Taskbar.getInstance().getAgent().getGameStatus();
		final int paletteSize = cgs.getGamePalette().size();

		ListSelectionModel rowSelectionModel;
		ListSelectionModel lsm;

		/**
		 * listens for the check box for displaying paths on the board.
		 */
		public void itemStateChanged(ItemEvent arg0) {
			ColoredTrailsBoardPanel boardPanel = Taskbar.getInstance().getBoardWindow().getBoardPanel();
			boardPanel.setPathDrawn(displaypaths.isSelected());
			boardPanel.repaint();		
		}
		

		/**
		 * listens for a row selection in overview table and loads
		 * appropriate data in detail table		
		 */
		private class overviewListSelectionListener implements ListSelectionListener {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}

				lsm = (ListSelectionModel) e.getSource();
				if (!lsm.isSelectionEmpty()) {
					currentlySelectedPathNum = lsm.getMinSelectionIndex();

					Taskbar taskbar = Taskbar.getInstance();
					taskbar.getBoardWindow().getBoardPanel().setDrawnPath((Path) shortestpaths.get(currentlySelectedPathNum));
					taskbar.getBoardWindow().getBoardPanel().repaint();
				}
				
			}
			
		}
		
		public JPathFinderFrame() {
			setBackground(Color.white);

			setPreferredSize(new Dimension(300, 220));
			setMaximumSize(new Dimension(300, 220));
			setMinimumSize(new Dimension(300, 220));

			// init path overview table
			pathOverviewTable = new PathOverviewTable();
			pathOverviewJtableContainer = new JTable(pathOverviewTable);
			pathOverviewJtableContainer.setDefaultRenderer(ChipSet.class,
					new ChipSetRenderer());
			pathOverviewJtableContainer.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			rowSelectionModel = pathOverviewJtableContainer.getSelectionModel();
			rowSelectionModel.addListSelectionListener(new overviewListSelectionListener());

			//Create the scroll pane and add the table to it. 
			pathOverviewJtableContainer.getColumnModel().getColumn(1)
					.setPreferredWidth(125);
			pathOverviewScrollPane = getScrollPaneOfTable(
					pathOverviewJtableContainer, new Dimension(250, 200));

			pathOverviewScrollPane.setPreferredSize(new Dimension(250, 180));

			displaypaths.setOpaque(false);
			displaypaths.setBackground(Color.WHITE);
			displaypaths.addItemListener(this);
		}
		
		private JScrollPane getScrollPaneOfTable(JTable table, Dimension dm) {
			table.setPreferredScrollableViewportSize(dm);
			JScrollPane js = new JScrollPane(table);
			js.setViewportView(table);
			js.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			return js;
		}

		public void erase() {
			removeAll();
			pathOverviewJtableContainer.clearSelection();
			repaint();
			setBackground(Color.white);
		}

		
		/**
		 * Display the paths for the hypotatical ChipSet chipset and
		 * the player with ID id. 
		 * @param chipSet Hypotatical chipset generated by the player via the
		 * ChipChooserPanel
		 * @param id Id of the player selected
		 */ 
		public void displayPaths(ChipSet chipSet, int id) {
			Box b = Box.createVerticalBox();
			currentlyDisplayedID = id;
			pathOverviewJtableContainer.clearSelection();
			removeAll();
			pathOverviewTable.setTableData(chipSet, id);
			b.add(pathOverviewScrollPane);
			b.add(Box.createVerticalStrut(5));
			b.add(displaypaths);
			add(b);
			updateUI();
		}

	}
	
	/**
	 * The list table in the PathFinderWindow, which lists the paths according to their
	 * ranks, and shows their number of movements, required and missing chips
	 * @author ilke
	 */
	class PathOverviewTable extends ListTable {
		final int maxNumRows = 100;
		final int MOVES = 0;
		final int MISSING_CHIPS = 1;

		public PathOverviewTable() {
			final String[] pathOverviewTitle = { "Moves", "Required Chips", "Missing Chips" };
			setColumnNames(pathOverviewTitle);
		}
		
		/**
		 * Sets the table data with the hypotatical chips added, and the id of the client
		 * @param hypotaticalAddedChips Hypotatical chips added after the proposal
		 * @param id Id of the Client
		 */
		public void setTableData(ChipSet hypotaticalAddedChips, int id) {
			ClientGameStatus cgs = Taskbar.getInstance().getAgent().getGameStatus();
			
			PlayerStatus ps = cgs.getPlayerByPerGameId(id);
			RowCol playerloc = ps.getPosition();
			RowCol goalloc = cgs.getBoard().getGoalLocations().get(0); // get the first goal
			
			
			Scoring scoring = cgs.getScoring();
			//This breaks my games and I don't like it: (mmd)
                        //so I replaced it with the above line
                        
//			//THIS IS TEMPORARY!!! AT THE MOMENT THERE IS NO SCORING OF THE GAME
//			//Weights of the colors are same for now etc.
//			HashMap<String,String> hm = new HashMap<String,String>();
//			hm.put("RGBRed", "1");
//			hm.put("RGBGreen", "1");
//			hm.put("orange1", "1");
//			hm.put("purple1", "1");
//			Scoring scoring = new Scoring(50, -10, 15, hm);
			
			
			shortestpaths = ShortestPaths.getShortestPaths(playerloc, goalloc, cgs.getBoard(), scoring, 10);
			int size = Math.min(maxNumRows, shortestpaths.size());

			Vector dataVector = new Vector();
			
			// This part is for sorting the shortest paths according to the weights of missing
			// chips among the ones that have the same pathWeight
			
			// Path[] paths = (Path[]) shortestpaths.toArray(); // throws a ClassCasting Exception
			Path[] paths = new Path[shortestpaths.size()];
			for(int i = 0; i<shortestpaths.size(); i++) {
				paths[i] = shortestpaths.get(i);
			}
			
			int  n;
			for(int m=1; m<shortestpaths.size(); m++) {		
				Path path = paths[m];
				n = m-1;
				ChipSet missingm = ps.getChips().addChipSets(ps.getChips(), 
						hypotaticalAddedChips).getMissingChips(paths[m].getRequiredChips(cgs.getBoard()));
				ChipSet missingn = ps.getChips().addChipSets(ps.getChips(), 
						hypotaticalAddedChips).getMissingChips(paths[n].getRequiredChips(cgs.getBoard()));
				while((n>=0) && 
						(paths[m].getWeight() == paths[n].getWeight()) && 
						(scoring.getChipSetWeight(missingm) < scoring.getChipSetWeight(missingn))) {
					paths[n+1] = paths[n];
					n--;
					if(n>=0) {
					missingn = ps.getChips().addChipSets(ps.getChips(), 
							hypotaticalAddedChips).getMissingChips(paths[n].getRequiredChips(cgs.getBoard()));
					}
				}
				paths[n+1] = path;
			}
			shortestpaths = new ArrayList<Path>();
			for(Path path:paths) {
				shortestpaths.add(path);
			}
			
			//
			
			for(int i=0; i<size; i++) {
				Path p = shortestpaths.get(i);
				Object moves = new Integer(p.getNumPoints() - 1);
				Object missingChips = ps.getChips().addChipSets(ps.getChips(), 
						hypotaticalAddedChips).getMissingChips(p.getRequiredChips(cgs.getBoard()));
				Object[] dataelement = {moves, p.getRequiredChips(cgs.getBoard()), missingChips};
				dataVector.add(dataelement);
			}
			data = dataVector;
		}
	}

    public void update(Observable o, Object arg) {
        String s = null;
        s = (String) arg;
        if(s == null)
            return;
        if(s.equals("NEWROUND"))
            reset();
     }

    public void gameStarted() {
        GUI.getAgent().getGameStatus().addObserver(this);
    }
	
	
	
}
