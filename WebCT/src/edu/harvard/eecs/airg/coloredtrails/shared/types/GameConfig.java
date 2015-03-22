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

package edu.harvard.eecs.airg.coloredtrails.shared.types;

import edu.harvard.eecs.airg.coloredtrails.shared.InitializableObject;

import java.io.Serializable;

/**
<b>Description</b>
A container type which helps wrap metadata around the 
GameConfigDetailsRunnable type for use by the admin client when
describing game configurations and by the server for storing and
retrieving the configuration.

[sgf: Instances of this class are made as an administrator informs 
the system of various game configuration classes?]

[sgf: How used by server for storing and retrieving?]

 @author Paul Heymann (ct3@heymann.be)
*/
public class GameConfig extends InitializableObject implements Serializable {
	/** name of the configuration class */
    public String configDetailsClassName = new String();
    /** the configuration class */
    public Class configDetailsClass = null;
    /** bytes of the configuration class */
    public byte[] configDetailsClassBytes = null;

    public GameConfig(String configClassName,
                      Class configClass,
                      byte[] bytes) {
        setPrivateValues(configClassName, configClass, bytes);
    }



    /**
     * Get the name of the config details class.
     *
     * @return The name of the config details class.
     */
    public String getConfigDetailsClassName() {
        return configDetailsClassName;
    }

    /**
     * Get a new instance of the config details class which this
     * gameconfig represents.
     *
     * @return A new instance of the config details class.
     */
    public GameConfigDetailsRunnable getRunnable() {
        try {
            return (GameConfigDetailsRunnable)
                    configDetailsClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    protected void setPrivateValues(String configClassName,
                                 Class configClass,
                                 byte[] bytes) {
        this.configDetailsClassName = configClassName;
        this.configDetailsClass = configClass;
        this.configDetailsClassBytes = bytes;
        setInitialized(true);
    }



    public String toString() {
        throwIfNotInitialized();
        return "Class Name: {" + configDetailsClassName + "}\n";
    }
}
