package edu.harvard.eecs.airg.coloredtrails.shared.types;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Holds data needed for the configuration file when it is run from the
 * controller
 * 
 * @author Yael Blumberg
 */
public class SignalingArguments implements Serializable {

	private static final long serialVersionUID = 554395634288628674L;

	public String experimentId = "";
	public ArrayList<GameStatus> gameToPlay = null;
}
