AGENT EXAMPLES
==============

The CT system has a java interface associated with each event type than can occur during an CT game. For your CT agent to receive events of a particular type, it must implement the corresponding interface. These interfaces are found in package: edu.harvard.eecs.airg.coloredtrails.agent.events.*

In addition to implementing the event interface, an agent wishing to receive events of a certain type must register itself as a listener of the event type. This is done through the ColoredTrailsClientImpl class, found in package:
edu.harvard.eecs.airg.coloredtrails.agent.ColoredTrailsClientImpl;

The examples discussed below illustrate how to build CT agents.



==============================
Example 1: ctagents.example1.*

This example presents a minimal CT agent. The agent does not implement a behavioral strategy; it merely receives messages. We have three source files:

SimpleAgentAdaptor.java is an interface that collects the various event interfaces that the agent cares about, and also adds new methods that are to be implemented.

SimpleAgentAdaptorImpl.java is the implementation of the interface.

SimpleAgentAdaptorFrontEnd.java is an example of how you can start up the CT agent with a main() method.


How to run this example:

ant runserver
ant runagentadaptor -Dclientid=10
ant runclient2
ant runadmin -Dconfigfile=lib/adminconfig/TheAutomat.txt

We begin by launching the CT server. Once the server is running, you'll see a message on the console saying "ColoredTrails is now running...". We then launch the CT agent and assign it a CT client ID of 10. Then, we launch a GUI client, which will have an ID of 20; this GUI client will allow you to interact with the CT agent (keep in mind, though, that this CT agent does very little). The GUI client will ask you to connect to the CT server. Once all players are registered with the CT server, we can launch the CT administrator. When we launch the CT administrator, we specify which administrator command sequence we wish to use; this command sequence will give a simple two-player game. You may use other command sequences shown below, as well.



==============================
Example 2: ctagents.example2.*

This example illustrates two CT agents that play a simple negotiation game. In this game, one agent makes a proposal to exchange chips and the other agent responds to the proposal, either accepting or rejecting it. We have five source files:

SimplePlayerAgentAdaptor.java is an interface that collects the various event interfaces that the proposer and responder agents care about.

SimplePlayer.java is a common superclass to the proposer and responder agents, which implements common functionality.

SimplePlayerProposer.java is the subclass that implements the proposer behavior (see below for details).

SimplePlayerResponder.java is the subclass that implements the responder behavior (see below for details).

SimplePlayerFrontEnd.java allows you to launch the CT agents from the command line.

SPProposerFrontEnd.java launches only the proposer agent.

SPResponderFrontEnd.java launches only the responder agent.

The CT agents in this example operate as follows. The proposer searches for the best path to the goal, given the chips it has and the chips the responder has; the proposer then asks for any chips it needs to take that path and offers all extra chips in return. When the responder receives a proposal, it checks if it can reach the goal with its chipset after the exchange; if the responder is able to reach the goal after the proposed exchange, then the responder accepts the proposal, otherwise it rejects it. As soon as a proposal is accepted, the communication phase ends and the agents do the necessary chip transfers and movements in the appropriate game phases. If all (or a defined number of) paths to the goal are tried and no proposals are accepted, or the communication phase times-out, then the agents do nothing.

The game configuration class associated with this example is found in gameconfigs.SimplePlayerConfig. The command sequence given to the CT administrator to run this game is found in lib/adminconfig/SimplePlayer.txt.


How to run this example:

ant runserver
ant ex2agents
ant runadmin -Dconfigfile=lib/adminconfig/SimplePlayer.txt

We begin by launching the CT server. Then, we launch the two CT agents, which proceed to register with the CT server. Finally, we launch the CT administrator, specifying which administrator command sequence we wish to use; this command sequence will give the simple two-player game described above. Be careful not to run the administrator before the agents have registered with the CT server; when a client registers, you'll see a message like "Colored Trails Server received a subcription message from client: 10". The different phases of the game are timed; the entire game takes about one minute to complete.



Example 3: ctagents.example3.*
==============================

This example is another illustration of two CT agents that play a simple negotiation game. The primary differences in this example concern how the agents are constructed and the strategies they use. We have four source files:

AgentAdaptorPR.java is an interface that collects the various event interfaces that the proposer and responder agents care about.

Proposer.java implements the proposer agent (there is no common superclass, unlike in Example 2).

Responder.java implements the responder agent (there is no common superclass, unlike in Example 2).

PRFrontEnd.java allows you to launch the CT agents from the command line.

The CT agents in this example operate as follows. The proposer agent examines all exchanges that are beneficial for the responder (i.e., increase responder's score) and, among them, proposes the most beneficial one for itself. If there is no mutually beneficial exchange, then the agents do nothing.

The game configuration class associated with this example is found in gameconfigs.ProposerResponderConfig. The command sequence given to the CT administrator to run this game is found in lib/adminconfig/ProposerResponder.txt


How to run this example:

ant runserver
ant ex3agents
ant runadmin -Dconfigfile=lib/adminconfig/ProposerResponder.txt

We begin by launching the CT server. Then, we launch the two CT agents, which proceed to register with the CT server. Finally, we launch the CT administrator, specifying which administrator command sequence we wish to use; this command sequence will give the simple two-player game described above. If you wish to interact with one of the CT agents, then do the following.

If you want to play the role of the proposer (and therefore play against the computer responder), then use:

ant runserver
ant ex3agentsR
ant runclient1
ant runadmin -Dconfigfile=lib/adminconfig/ProposerResponder.txt

If you want to play the role of the responder (and therefore play against the computer proposer), then use:

ant runserver
ant ex3agentsP
ant runclient2
ant runadmin -Dconfigfile=lib/adminconfig/ProposerResponder.txt

The ant commands 'runclient1' and 'runclient2' automatically create GUI clients (for people to use) with pre-determined client IDs; the command 'runclient1' creates a client with ID = 10, the command 'runclient2' gives ID = 20.