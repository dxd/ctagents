package edu.harvard.eecs.airg.coloredtrails.controller.events;

import edu.harvard.eecs.airg.coloredtrails.shared.types.GameStatus;

/**
 * Created by IntelliJ IDEA.
 * User: rani
 * Date: Feb 6, 2008
 * Time: 12:23:59 PM
 */
public interface GameUpdatedEventListener {
    public void gameUpdatedEventHandler(GameStatus game);
}
