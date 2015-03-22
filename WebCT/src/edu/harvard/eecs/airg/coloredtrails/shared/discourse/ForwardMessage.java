package edu.harvard.eecs.airg.coloredtrails.shared.discourse;

/**
 * ForwardMessage is a discourse message sent by the server to
 * clients that contain another discourse message.
 * The intended ussage for ForwardMessage is to notify third
 * party clients about message communications not intended
 * for them.
 */
public class ForwardMessage extends DiscourseMessage {

	/** The id of the server, who is the sender of the message */
	private static final int SERVER_ID = -1;
	/** the id of the message */
	private static final int MESSAGE_ID = -1;
	/** The type of the message */
	private static final String MESSAGE_TYPE = "Forward Proposal Message";

	/** The forwarded message */
	protected final DiscourseMessage message;

	/**
	 * The constructor.
	 * @param toPerGameId The id of the client that is to receive this message.
	 * @param message The Discourse Message that is to be forwarded.
	 */
	public ForwardMessage( int toPerGameId, DiscourseMessage message ) {
		super( SERVER_ID, toPerGameId, MESSAGE_TYPE, MESSAGE_ID );

	    this.message = message;
	}

	/**
	 * Gets the forwarded message.
	 * @return The forwarded message.
	 */
	public DiscourseMessage getMessage() {
		return message;
	}
}
