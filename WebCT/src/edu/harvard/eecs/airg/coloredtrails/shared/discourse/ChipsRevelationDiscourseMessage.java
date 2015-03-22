package edu.harvard.eecs.airg.coloredtrails.shared.discourse;

import edu.harvard.eecs.airg.coloredtrails.shared.types.ChipSet;



/**
 * Discourse Message for chip revelation. 
 * used for adding uncertanity to the game. 
 * each player can choose how many of his chips the other players will see
 * @author Hen Barshak 
 */
public class ChipsRevelationDiscourseMessage extends DiscourseMessage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8732569987805134283L;
	ChipSet revelationChips;
	
	public ChipsRevelationDiscourseMessage(int senderID, int receiverID, int messageId, ChipSet cs) {
		super(senderID, receiverID, "chipsrevelation", messageId);

        this.revelationChips = cs;
	}
	
	public ChipSet getRevelationChips() {
        return this.revelationChips;
    }
}
