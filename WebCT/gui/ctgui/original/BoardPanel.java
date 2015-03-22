/*
	Colored Trails
	
	Copyright (C) 2006, President and Fellows of Harvard College.  All Rights Reserved.
	
	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package ctgui.original;

import edu.harvard.eecs.airg.coloredtrails.agent.events.GameStartEventListener;
import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.client.ClientPlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.client.ui.ColoredTrailsBoardPanel;
import edu.harvard.eecs.airg.coloredtrails.shared.GlobalColorMap;
import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Path;
import edu.harvard.eecs.airg.coloredtrails.shared.types.PlayerStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Square;
import edu.harvard.eecs.airg.coloredtrails.shared.types.RowCol;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Goal;
import net.javage.twoD.TransparencyMap;
import ooga.graphics.sprites.Sprite;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
/**
<b>Description</b>
* This panel is in charge of drawing the CT game board and handling
* drag attempts by the player (player tokens may be dragged if
* movement is allowed).  It is updated based on the current state of
* the players in the game, and the colors of the squares in the game
* Board.
<p>
Method drawPlayers() and .PanelBoardMouseListener.mouseReleased()
use .shared.types.PlayerStatus.movesAllowed() method, via 
.client.ClientGameStatus.myPlayerCanMove()
<p>
A BoardPanel instance is used in the .client.ui.BoardWindow class.

<p>

<b>Observes</b>
BoardPanel observes ClientGameStatus (which extends GameStatus)

<p>
*
* @author Paul Heymann (ct3@heymann.be)
@author Sevan G. Ficici (bug fixes)
@author rdl

*/
public class BoardPanel extends JPanel implements Observer, ColoredTrailsBoardPanel, GameStartEventListener
{
	private ClientGameStatus game = null;
	public static int FIELDSIZE = 40;
	private int cols = 0;
	private int rows = 0;
	private Path curdrawpath;
	private boolean ispathdrawn = false;
	private final String[] goalimages =
		new String[]{"images/goalA.gif", 
	                 "images/goalB.gif", 
	                 "images/goalO.gif", 
	                 "images/goal.gif"};
	private ArrayList validRectsToMoveIn = new ArrayList();
	private Point currentDragPoint = new Point();
	private boolean currentlyDragging = false;
	private Sprite currentAction = null;
	private int movingplayerid = -1;
        private int boardSize = 6;  
        
	/** associates a Sprite instance for each player name */
	private Hashtable<String,Sprite> playersprites = new Hashtable<String,Sprite>();
	
	private Logger log = Logger.getRootLogger();
	
	private PanelBoardMouseListener pbml = new PanelBoardMouseListener(this);
	private BoardPanelKeyListener bpkl = new BoardPanelKeyListener(this);
	/**
	 * Set the gamestatus from whose Board this BoardPanel will display
	 * itself.
	 *
	 * @param game The game to display.
	 */
	public void setGame(ClientGameStatus game) {
	    this.game = game;
	    game.addObserver(this);
	
	    cols = game.getBoard().getCols();
	    rows = game.getBoard().getRows();
            
            System.out.println("BoardPanel.setGame() called");
	}
	
	public BoardPanel() {
	    super();
	    RowCol[] points = {new RowCol(0, 0), new RowCol(0, 1)};
	    curdrawpath = new Path(points);
	    setPreferredSize(new Dimension(boardSize * 41, boardSize * 41));
	    setMaximumSize(new Dimension(boardSize * 41, boardSize * 41));
	    setMinimumSize(new Dimension(boardSize * 41, boardSize * 41));
	    
	    setBackground(Color.WHITE);
	    setVisible(true);
	    
	    addMouseListener(pbml);
	    addMouseMotionListener(pbml);
        // Add a KeyListener to the BoardPanel object.
        addKeyListener(bpkl);
	}
	
	public BoardPanel(ClientGameStatus game) {
	    this();
	    setGame(game);
	    repaint();
	}
	
	
	/**
		Returns the Sprite of the specified player;
		we abstract this functionality so that it can be backed by various
		approaches--right now, we use a Hashtable
	*/
	private Sprite getPlayerSprite(ClientPlayerStatus player)
	{
		return playersprites.get(Integer.toString(player.getPerGameId()));
		// alternative, if we get it to work
	//	return player.getSprite();
	}
	
	
	/**
		Sets the Sprite of the specified player
	*/
	private void setPlayerSprite(ClientPlayerStatus player, Sprite sp)
	{
		playersprites.put(Integer.toString(player.getPerGameId()), sp);
	//	player.setSprite(sp);  // PROBABLY CAN REMOVE THIS
	}
	
	
	/**
	 * Fill a particular square with smaller squares of alternating colors,
	 * usually to properly shade the board.
	 */
	public void fillRectShaded(Graphics2D g2, int x, int y, int blocksize,
	                           int squaresize, Color c1, Color c2) {
	    boolean flip1 = true;
	    boolean flip2 = true;
	    int xorig = x;
	    int yorig = y;
	    for (x = xorig; x < xorig + squaresize; x += blocksize) {
	        flip1 = flip2;
	        flip2 = !flip2;
	        for (y = yorig; y < yorig + squaresize; y += blocksize) {
	            if (flip1) {
	                g2.setColor(c1);
	                flip1 = !flip1;
	            } else {
	                g2.setColor(c2);
	                flip1 = !flip1;
	            }
	            g2.fill(new Rectangle(x, y, blocksize, blocksize));
	        }
	    }
	    g2.setColor(Color.BLACK);
	}
	
	
	/**
	 * Draw the background squares of the board appropriately.
	 */
	public void drawBackground(Graphics2D g2)
	{
		// if the board size changed from what we last knew...
	    if (cols != game.getBoard().getCols() || rows != game.getBoard().getRows())
	    {
	        cols = game.getBoard().getCols();
	        rows = game.getBoard().getRows();
	        setPreferredSize(new Dimension(cols * FIELDSIZE + 1, rows * FIELDSIZE + 1));
	        setMaximumSize(new Dimension(cols * FIELDSIZE + 1, rows * FIELDSIZE + 1));
	        setMinimumSize(new Dimension(cols * FIELDSIZE + 1, rows * FIELDSIZE + 1));
	        setVisible(true);
	    }
	
	    ArrayList tempValidRects = new ArrayList();
	
	    for (int col = 0; col < cols; col++) {
	        for (int row = 0; row < rows; row++) {
	            Rectangle boardsquare =
	            		new Rectangle(col * FIELDSIZE, row * FIELDSIZE, FIELDSIZE, FIELDSIZE);
	        	Color squarecolor =
	        		GlobalColorMap.getColorByName(game.getBoard().getSquare(new RowCol(row, col)).getColor());
	
	        	if (currentlyDragging && boardsquare.contains(currentDragPoint))
	            {
	                fillRectShaded(g2, col * FIELDSIZE, row * FIELDSIZE, 10, FIELDSIZE, 
	                        	   squarecolor, squarecolor.darker().darker().darker());
	                tempValidRects.add(boardsquare);
	            }
	            else if (movingplayerid == GUI.getAgent().getPerGameID() &&
	                    currentlyDragging &&
	                    (game.myPlayerCanMove() &&
	                    isMyNeighbor(new RowCol(row, col)) &&
	                    game.getMyPlayer().getChips().contains(new ChipSet(game.getBoard()
	                    .getSquare(new RowCol(row, col)).getColor())) ||
	                    (game.getMyPlayer().getPosition().equals(new RowCol(row, col)))))
	            {           	
	                 fillRectShaded(g2, col * FIELDSIZE, row * FIELDSIZE, 10, FIELDSIZE,
	                		 		squarecolor, squarecolor.darker());
	                 tempValidRects.add(boardsquare);
	            }
	            else
	            {
	                g2.setColor(squarecolor);
	                g2.fill(boardsquare);
	            }


                int mypgid = GUI.getAgent().getPerGameID();
                int goalType = -1;

                
                if (game.getBoard().getSquare(new RowCol(row, col)).hasGoal())
	            {
	                int gt = 0;
	                if (game.getBoard().getSquare(new RowCol(row, col)).hasGoal(0) &&
	                    game.getBoard().getSquare(new RowCol(row, col)).hasGoal(1))
	                  gt = 2;
                  else
                  {
                    if (game.getBoard().getSquare(new RowCol(row, col)).hasGoal(0))
                      goalType = 0;
                    else if (!game.getBoard().getSquare(new RowCol(row, col)).hasGoal(Goal.DEFAULT_GOAL_TYPE))
                      goalType = 1;
                    else
                      goalType = Goal.DEFAULT_GOAL_TYPE;
                    
                    if (goalType ==mypgid)
                        gt=0;
                    else if (goalType == Goal.DEFAULT_GOAL_TYPE)
                        gt=3;
                    else
                        gt=1;
                  }

	                  
	                Toolkit tk = Toolkit.getDefaultToolkit();
	                Image goal = tk.getImage(ClassLoader.getSystemResource(goalimages[gt]));
	                
	                int imgheight = goal.getHeight(null);
	                int imgwidth = goal.getWidth(null);
	                
	                g2.drawImage(goal,
	                        col * FIELDSIZE + FIELDSIZE / 2 - imgwidth / 2,
	                        row * FIELDSIZE + FIELDSIZE / 2 - imgheight / 2,
	                        imgwidth, imgheight, this);
	            }
	
	            g2.setColor(Color.BLACK);
	            g2.draw(boardsquare);
	        }
	    }
	    
	    validRectsToMoveIn = tempValidRects;
	    g2.setColor(Color.BLACK);
	}
	
	
	/**
	 * Determines whether a particular RowCol is a N,S,W,E neighbor
	 * of the current player.
	 *
	 * @param otherpos The other position which may or may not be a
	 *                 neighbor of the player.
	 * @return Whether or not the position is a N,S,W,E neighbor of the
	 *         player.
	 */
	public boolean isMyNeighbor(RowCol otherpos) {
	    RowCol mypos = GUI.getAgent()
	            .getGameStatus()
	            .getMyPlayer()
	            .getPosition();
	    return (Math.abs(mypos.row - otherpos.row) +
	            Math.abs(mypos.col - otherpos.col) <=
	            1);
	}
	
	
	/**
	 * Paint the board by drawing the background and the sprites.
	 */
	public void paintComponent(Graphics g)
	{
	    super.paintComponent(g);
	
	    if (game == null)
	        return;
	
	    Graphics2D g2 = (Graphics2D) g;
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
	    drawBackground(g2);
	    drawPath(g2);
	    drawPlayers(g2);
	}
	
	
	/**
	 * Repaint when updates are received from observed objects.
	 */
	public void update(Observable o, Object arg) {
            String s;
            if(arg instanceof String){
                s = (String) arg;
                if(s.equals("NEWROUND")){
                    ArrayList<ClientPlayerStatus> players = game.getClientPlayers();
	    
                    // the pergameID of the player viewing this GUI/running on this JVM
                    int mypgid = GUI.getAgent().getPerGameID();

                    // for each player...
                    for (ClientPlayerStatus player : players) {
                        int playerpgid = player.getPerGameId();  // this player's perGameId

                        // if the player has a null sprite, then assign correct sprite
                        if (playerpgid == mypgid)
                            setPlayerSprite(player, Icons.getMyBoardSprite(this));
                        else
                            setPlayerSprite(player, Icons.getPlayerBoardSprite(playerpgid, this));

                        // if this is me, and I can move, and I am moving, then DON'T set location
                        if (!(playerpgid == mypgid &&         // if NOT (this is me...
                              game.myPlayerCanMove() &&       // and I can move...
                              movingplayerid == playerpgid))  //  and I am moving)...
                                setSpriteLocation(getPlayerSprite(player), player.getPosition(), playerpgid+mypgid);

                    }
                }
            }
            //System.out.println("BoardPanel.update called");
            repaint();

	}
	
	
	/**
	 * Draw the path pointed to by curdrawpath if ispathdrawn is set.
	 *
	 * @param g2 A graphics object associated with the Panel.
	 */
	public void drawPath(Graphics2D g2) {
	    if (ispathdrawn) {
	        for (int i = 0; i < curdrawpath.getNumPoints()-1; i++) {
	            RowCol start = curdrawpath.getPoint(i);
	            RowCol end = curdrawpath.getPoint(i + 1);
	
	            Line2D.Double l = new Line2D.Double(
	                    start.col * FIELDSIZE + FIELDSIZE / 2,
	                    start.row * FIELDSIZE + FIELDSIZE / 2,
	                    end.col * FIELDSIZE + FIELDSIZE / 2,
	                    end.row * FIELDSIZE + FIELDSIZE / 2);
	            g2.draw(l);
	            g2.setColor(Color.GRAY);
	
	            l.setLine(start.col * FIELDSIZE + FIELDSIZE / 2 + 1,
	                    start.row * FIELDSIZE + FIELDSIZE / 2 + 1,
	                    end.col * FIELDSIZE + FIELDSIZE / 2 + 1,
	                    end.row * FIELDSIZE + FIELDSIZE / 2 + 1);
	            g2.draw(l);
	            g2.setColor(Color.BLACK);
	        }
	    }
	}
	
	
	/**
	 * Set a path to be drawn on the board.
	 *
	 * @param p
	 */
	public void setDrawnPath(Path p) {
	    curdrawpath = p;
	}
	
	
	/**
	 * Set whether curdrawpath is drawn on the board or not.
	 *
	 * @param pathdrawn Whether to draw the curdrawpath or not.
	 */
	public void setPathDrawn(boolean pathdrawn) {
	    ispathdrawn = pathdrawn;
	}
	
	
	/**
	 * Draw the various sprites on top of the board, e.g. the tokens.
	 */
	public void drawPlayers(Graphics2D g2)
	{
		// the players in the game
	    ArrayList<ClientPlayerStatus> players = game.getClientPlayers();
	    
	    // the pergameID of the player viewing this GUI/running on this JVM
	    int mypgid = GUI.getAgent().getPerGameID();
		
	    // for each player...
	    for (ClientPlayerStatus player : players)
	    {
			int playerpgid = player.getPerGameId();  // this player's perGameId
			
			// if the player has a null sprite, then assign correct sprite
	        if (getPlayerSprite(player) == null)
	            if (playerpgid == mypgid)
	            	setPlayerSprite(player, Icons.getMyBoardSprite(this));
	            else
	            	setPlayerSprite(player, Icons.getPlayerBoardSprite(playerpgid, this));
			
			// if this is me, and I can move, and I am moving, then DON'T set location
	        if (!(playerpgid == mypgid &&         // if NOT (this is me...
	              game.myPlayerCanMove() &&       // and I can move...
	              movingplayerid == playerpgid))  //  and I am moving)...
	        	setSpriteLocation(getPlayerSprite(player), player.getPosition(), playerpgid+mypgid);
	        
	        getPlayerSprite(player).paintComponent(g2, this);
	    }
	}
	
	
	/**
	 * Set a sprite's location given a row,col point.
	 * Sprites are randomly staggered in the square (hopefully
	 * repainting won't lead to weirdness).
	 */
	public void setSpriteLocation(Sprite s, RowCol p, int seed)
	{
		int spriteWidth = (int)s.getBounds().getWidth();
		int spriteHeight = (int)s.getBounds().getHeight();
		
		Random rnd = new Random(seed);
	    int coloffset = rnd.nextInt(FIELDSIZE - spriteWidth);
	    int rowoffset = rnd.nextInt(FIELDSIZE - spriteHeight);
	    
	    s.setSpriteMiddle(new Point(p.col * FIELDSIZE + coloffset + spriteWidth/2,
	    							p.row * FIELDSIZE + rowoffset + spriteHeight/2));
	}
	
	
	/**
	 * Get the top sprite at the position under point, presumably when
	 * someone is clicking the board.
	 */
	public ClientPlayerStatus getTopIntersectedBoardObject(Point p)
	{
	    ArrayList<ClientPlayerStatus> players = game.getClientPlayers();
	    
	    for (ClientPlayerStatus player : players)
	    {
	        Sprite s = getPlayerSprite(player);
	        TransparencyMap tm = s.getTransparencyMap();
	        TransparencyMap click = new TransparencyMap(1, 1);
	        click.setX(p.x);
	        click.setY(p.y);
	        click.setStatus(0, 0, true);
			
	        if (tm.collide(click))
	        	return player;
		}
	    
	    return null;
	}
	
	
	/**
	 * A mouse listener which deals with keeping the mouse from going
	 * out of bounds while dragging, and dealing with general clicking,
	 * releasing, and dragging.
	 */
	class PanelBoardMouseListener extends MouseInputAdapter {
	    private BoardPanel pb;
	
	    public PanelBoardMouseListener(BoardPanel pb) {
	        this.pb = pb;
	    }
	
	    /**
	     * Return a MouseEvent when which is restricted to the proper
	     * area of validRectsToMoveIn.
	     *
	     * @param e The MouseEvent to change if it is out of bounds.
	     * @return The changed MouseEvent.
	     */
	    public MouseEvent keepInBounds(MouseEvent e) {
	        for (int i=0; i<validRectsToMoveIn.size(); i++)
	        {
	            Rectangle validrect = (Rectangle)validRectsToMoveIn.get(i);
	            if (validrect.contains(e.getPoint()))
	                return e;
	        }
	
	        Point translation = new Point(currentDragPoint.x - e.getX(),
	                                      currentDragPoint.y - e.getY());
	        e.translatePoint(translation.x, translation.y);
	
	        return e;
	    }
	
	    public void mousePressed(MouseEvent e)
	    {	
	        ClientPlayerStatus player = getTopIntersectedBoardObject(e.getPoint());
	
	        if (player != null)
	        {
	            currentAction = getPlayerSprite(player);
	            currentlyDragging = true;
	            movingplayerid = player.getPerGameId();
	            currentDragPoint = e.getPoint();
	            e = keepInBounds(e);
	            currentAction.mousePressed(e);
	            repaint();
	        }
	    }
	
	    public void mouseDragged(MouseEvent e)
	    {
	        if (currentAction != null) {
	            e = keepInBounds(e);
	            currentDragPoint = e.getPoint();
	            currentAction.mouseDragged(e);
	            repaint();
	        }
	    }
	
	    public void mouseReleased(MouseEvent e)
	    {
	        if (currentAction != null && currentlyDragging) {
	            e = keepInBounds(e);
	            currentDragPoint = e.getPoint();
	            currentAction.mouseReleased(e);
	            currentlyDragging = false;
	            movingplayerid = -1;
	
	            if (game.myPlayerCanMove() && !inOriginalSquare()) {
	                Sprite s = currentAction;
	                Rectangle bounds = s.getBounds();
	                int col = (bounds.x + bounds.width / 2) / FIELDSIZE;
	                int row = (bounds.y + bounds.height / 2) / FIELDSIZE;
	                Taskbar.getInstance().getAgent().communication.sendMoveRequest(new RowCol(row, col));
	            }
	            currentAction = null;
	
	            repaint();
	        }
	    }

        public void mouseClicked(MouseEvent e) {
            pb.requestFocusInWindow(); // Needed for KeyEvents to be triggered.
        }

	    void mouseEntered(KeyEvent e) {
	        pb.requestFocusInWindow(); // Needed for KeyEvents to be triggered.
	    }
	}
	
	
	/**
	 * Check that the board object given is in it's square according to
	 * the rectangle "orig".  Defaults to true.
	 */
	public boolean playerInOriginalSquare(ClientPlayerStatus player, Rectangle orig)
	{
	    if (getPlayerSprite(player) != null)
	    {
	        Sprite s = getPlayerSprite(player);
	        return (orig.contains(s.getSpriteMiddle()));
	    }
	    return true;
	}
	
	
	/**
	 * Check if our board object is in its original square.
	 */
	public boolean inOriginalSquare() {
	    ClientPlayerStatus me = new ClientPlayerStatus(
	    		game.getPlayerByPerGameId(
	    		GUI.getAgent().getPerGameID()));
	
	    return playerInOriginalSquare(me, getMyCurrentRect());
	}
	
	
	/**
	 * Get the current rectangle that the player is, as a positioned
	 * Rectangle.
	 */
	public Rectangle getMyCurrentRect() {
		
	    RowCol loc = GUI.getAgent()
	            .getGameStatus()
	            .getPlayerByPerGameId(GUI.getAgent()
	            .getPerGameID())
	            .getPosition();
	    
	    System.err.println("current rowcol: " + loc);
	    
	    return new Rectangle(loc.col * FIELDSIZE,
	            loc.row * FIELDSIZE, FIELDSIZE, FIELDSIZE);
	}

    public void gameStarted() {
        GUI.getAgent().getGameStatus().addObserver(this);
    }

    /**
     * KeyAdapter class that makes it possible to move around the
     * players using the keyboard arrows.
     * @author Bart Kamphorst (Bart.Kamphorst@phil.uu.nl)
     */
    class BoardPanelKeyListener extends KeyAdapter {
        private BoardPanel pb;

	    public BoardPanelKeyListener(BoardPanel pb) {
	        this.pb = pb;
	    }

        public RowCol getCurrentPlayerPos() {
             RowCol loc = GUI.getAgent()
	            .getGameStatus()
	            .getPlayerByPerGameId(GUI.getAgent()
	            .getPerGameID())
	            .getPosition();
             return loc;
        }

        /**
         * Move the player across the board using the arrow keys.
         * @param KeyEvent ke
         */
        @Override
        public void	keyReleased(KeyEvent ke) {

            RowCol playerPos = getCurrentPlayerPos();
            RowCol newPos;

                if (ke.getKeyCode() == KeyEvent.VK_UP) {
                    if ( (playerPos.row - 1 ) >= 0 ) {
                        newPos = new RowCol( (playerPos.row - 1), playerPos.col);
                        Taskbar.getInstance().getAgent().communication.sendMoveRequest(newPos);
                    }
                }
                else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                    if ( (playerPos.row + 1 ) < pb.rows ) {
                        newPos = new RowCol( (playerPos.row + 1), playerPos.col);
                        Taskbar.getInstance().getAgent().communication.sendMoveRequest(newPos);
                    }
                }
                else if (ke.getKeyCode() == KeyEvent.VK_LEFT) {
                    if ( (playerPos.col - 1 ) >= 0 ) {
                        newPos = new RowCol( playerPos.row, (playerPos.col - 1) );
                        Taskbar.getInstance().getAgent().communication.sendMoveRequest(newPos);
                    }
                }
                else if (ke.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if ( (playerPos.col + 1 ) < pb.cols ) {
                        newPos = new RowCol( playerPos.row, (playerPos.col + 1) );
                        Taskbar.getInstance().getAgent().communication.sendMoveRequest(newPos);
                    }
                }
            repaint();

        }

    }
}