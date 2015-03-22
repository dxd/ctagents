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

package ctagents.example4;

/**
 * Runs two Simple Players on the same JVM
 * @author ilke
 *
 */
public final class PhaseLoopFrontEnd {


    public static void main(String[] args) {

        PhaseLoopProposer  agent1 = new PhaseLoopProposer();
        PhaseLoopProposer  agent2 = new PhaseLoopProposer();
        PhaseLoopResponder agent3 = new PhaseLoopResponder();

        agent1.setClientName("10");
        agent2.setClientName("20");
        agent3.setClientName("30");

        agent1.start();
        agent2.start();
        agent3.start();

    }


}