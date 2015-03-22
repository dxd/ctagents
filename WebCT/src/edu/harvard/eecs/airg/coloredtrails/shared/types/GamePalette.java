/*
	Colored Trails
	
	Copyright (C) 2006-2007, President and Fellows of Harvard College.  All Rights Reserved.
	
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

package edu.harvard.eecs.airg.coloredtrails.shared.types;

//import edu.harvard.eecs.airg.coloredtrails.shared.Utility;

import java.io.Serializable;
//import java.util.Hashtable;
import java.util.ArrayList;
//import java.util.TreeSet;
import java.util.Observable;
import java.util.Random;
import java.util.List;


/**
<b>Description</b>
Represents a palette (a set of different colors as a string) for a game.
<p>
 *
 * @author Monira Sarne (add new class)
   @author Sevan G. Ficici
 */

public class GamePalette extends Observable implements Serializable {
	
    private List<String> colors;
    
	// CAN ADD A INSTANCE METHOD VERSION OF THIS
	private static Random localrand = new Random();  // (SGF)
	
    public static void main( String args[] ) {
    	boolean pastTest = true;
    	
    	System.out.println( "Starting Test" );
    	GamePalette gp = new GamePalette();
    	gp.add( "Red" );
    	gp.addColor( "Blue" );
    	
    	if( ! gp.contains( "Blue" ) )
    		pastTest = false;
    	
    	if( ! gp.contains( "Red" ) )
    		pastTest = false;
    	
		System.out.println( "Past contains" + pastTest );

    	if( gp.size() != 2 )
    		pastTest = false;
    	
    	System.out.println( "Past size" + pastTest );

    	if( gp.getNumByColor("Red") != 1 )
    		pastTest = false;
    	
    	if( gp.getNumByColor("Blue") != 2 )
    		pastTest = false;
    	
    	if( gp.getNumByColor("X") != 0 )
    		pastTest = false;
    	
    	System.out.println( "Past getNumByColor " + pastTest );
    	
    	if( gp.indexOf("Red") != 0 )
    		pastTest = false;
    	
    	if( gp.indexOf("Blue") != 1 )
    		pastTest = false;
    	    	
    	System.out.println( "Past indexOf " + pastTest );
    	
    	List<String> list = gp.getColors();
    	if( !( list.contains( "Red") && list.contains( "Blue") && ! list.contains("X") ) )
    		pastTest = false;
    	
    	System.out.println( "Past getColorListArray " + pastTest );
    	
    	if( ! gp.getColorByNum(1).equals( "Red") )
    		pastTest = false;
    	
    	if( ! gp.getColorByNum(2).equals( "Blue") )
    		pastTest = false;
    	
    	System.out.println( "Past getColorByNum " + pastTest );
    	
    	if( ! gp.get(0).equals( "Red") )
    		pastTest = false;
    	
    	if( ! gp.get(1).equals( "Blue") )
    		pastTest = false;
    	
    	System.out.println( "Past get " + pastTest );
    	/*
    	TreeSet<String> tree = gp.getColorListTreeSet();
    	if( !( tree.contains( "Red") && tree.contains( "Blue") && ! tree.contains("X") ) )
    		pastTest = false;
    	
    	System.out.println( "Past getColorListTreeSet " + pastTest );*/
    	
    	if( pastTest == true )
    		System.out.println( "GamePalette Successfully past test." );
    	else

    		System.out.println( "GamePalette DID NOT PASS TEST." );
    }

    public GamePalette() {
    	colors = new ArrayList<String>();
    }

    public GamePalette(GamePalette g){
        colors = new ArrayList<String>( g.colors );
    }

    /**
     * Add a new color to the pallet of the game using string.
     * The color names are defined in the global color map class.
     * The pallet is represent as a hashtable when the value is sequential number that can be use for creating
     *  a randomly pallet
     *
     * @param color - The name of the color to be added (from the global color class).
     */
    @Deprecated
     public void addColor (String color) {
        add( color );
     }
    
    /**
     * Add a new color to the pallet of the game using string.
     * The color names are defined in the global color map class.
     * The pallet is represent as a hashtable when the value is sequential number that can be use for creating
     *  a randomly pallet
     *
     * @param color - The name of the color to be added (from the global color class).
     */
	public void add(String color) {
		if( contains(color) )
			return;
		colors.add( color );
	}
	
	/**
		Returns the number of colors currently in the palette
	*/
	public int size() {
		return colors.size();
	}


    /**
        * Get the number(Value of hashtable) of a given color.
        *
        * @param  color - given color
        * @return The seq number in the hashtable.
        * 
        * @deprecated use indexOf()
        */
	@Deprecated
       public int getNumByColor(String color) {
           if( ! contains( color ) ) {
               return 0;
           }
    	   return indexOf( color ) + 1;
       }
	
	public int indexOf( String color ) {
		return colors.indexOf( color );
	}

     /**
     * Get a list of all colors in this chipset.
     *
     * @return A list of all colors in this chipset.
     */
     public List<String> getColors() {
         return new ArrayList<String>(colors);
     }

    /**
     * Get a list of all colors in this chipset.
     *
     * @return A list of all colors in this chipset.
     *//*
     public TreeSet<String> getColorListTreeSet() {
         return new TreeSet<String>(colors);
     }*/
     
    /**
     * Get a Color by a number
     * @param  number - given number
     * @return The color (value) of the number
     * 
     * @deprecated use get( int index )
     */
     @Deprecated
     public String getColorByNum(int number) {
    	 return get( number - 1 );
     }
     
     public String get( int index ) {
    	 return colors.get( index );
     }
     
     public boolean contains( String color) {
    	 return colors.contains(color);
     }

          /**
     * Turn the type into a string suitable for display.
     *
     * @return A string suitable for display on the admin client
     * or for debugging.
     */
     public String toString() {
        String str = "{ GamePalette ";
        for (String color : colors ) {
            str += " '" + color + "':" + colors.indexOf(color);
        }
        str += " }";
        return str;
     }
     
     public String getRandomColor() {
    	 return colors.get( localrand.nextInt(colors.size()));
     }

}

