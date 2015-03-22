package edu.harvard.eecs.airg.coloredtrails.controller;

import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;

/**
 * @author Rani
 */
public interface Control {
    public void sendConfig(String configdir, String className);
    public void newGame(String ConfigClassName, String players);
    public void listPlayers();
}
