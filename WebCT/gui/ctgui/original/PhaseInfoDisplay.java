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

import edu.harvard.eecs.airg.coloredtrails.client.ClientGameStatus;
import edu.harvard.eecs.airg.coloredtrails.shared.types.Phases;
import edu.harvard.eecs.airg.coloredtrails.shared.types.GamePhase;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
	<b>Description</b>
 * A 5 phase display showing the previous phase, the current phase,
 * and the next three phases.
	<p>
	An instance of this class is used in .client.ui.BoardWindow class.
	<p>
	(sgf) The term "5 phase display" refers to the number of
	phases that are simultaneously displayed, and has nothing to do
	with the number of phases the game has.
  
 * @author Paul Heymann (ct3@heymann.be)
 */
public class PhaseInfoDisplay extends JPanel implements ActionListener {
    private JPanel infoDisplayPanel;
    private final Border blackline = BorderFactory.createLineBorder(Color.BLACK);
    private JTextField[] phaseslabels = new JTextField[10];
    private JTextField timeTitle;
    private JTextField phasesTitle;
    private JTextField phase5time;
    private JTextField phase2title;
    private JTextField phase3title;
    private JTextField phase1title;
    private JTextField phase4title;
    private JTextField phase5title;
    private JTextField phase1time;
    private JTextField phase2time;
    private JTextField phase3time;
    private JTextField phase4time;

    public PhaseInfoDisplay() {
        super();

        phaseslabels[0] = phase1title;
        phaseslabels[1] = phase2title;
        phaseslabels[2] = phase3title;
        phaseslabels[3] = phase4title;
        phaseslabels[4] = phase5title;
        phaseslabels[5] = phase1time;
        phaseslabels[6] = phase2time;
        phaseslabels[7] = phase3time;
        phaseslabels[8] = phase4time;
        phaseslabels[9] = phase5time;

        for (JTextField jtf : phaseslabels) {
            jtf.setBorder(blackline);
            jtf.setFont(new Font("SansSerif", Font.BOLD, 12));
            jtf.setBackground(Color.WHITE);
            jtf.setForeground(Color.LIGHT_GRAY);
        }

        phase2title.setForeground(Color.BLACK);
        phase2time.setForeground(Color.BLACK);

        timeTitle.setBorder(blackline);
        phasesTitle.setBorder(blackline);
        timeTitle.setBackground(new Color(230, 230, 230));
        phasesTitle.setBackground(new Color(230, 230, 230));
        timeTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        phasesTitle.setFont(new Font("SansSerif", Font.BOLD, 12));

        setBackground(Color.WHITE);
        add(infoDisplayPanel);

        setCurrentPhaseTitle("Current Phase");
        setCurrentPhaseTime("--");
        setPastPhaseTitle("Previous Phase");
        setPastPhaseTime("--");
        String[][] lastlabels = new String[3][2];
        lastlabels[0][0] = "Next Phase";
        lastlabels[1][0] = "Next Phase";
        lastlabels[2][0] = "Next Phase";
        lastlabels[0][1] = "--";
        lastlabels[1][1] = "--";
        lastlabels[2][1] = "--";
        setNextThreePhases(lastlabels);

        (new Timer(1000, this)).start();
    }

    public void actionPerformed(ActionEvent e) {
        updateData();
    }

    /**
     * Convert a number of seconds to a string representation.
     * @param secs The number of seconds.
     * @return The string value, e.g., 1:04.
     */
    public static String secsToTextTime(int secs) {
        if (secs == 0) {
            return "-:--";
        }
        String modifier = "";
        if (secs < 0) {
            modifier = "-";
            secs *= -1;
            //return("∞");
        }
        int modsecs = secs % 60;
        secs -= modsecs;
        int minutes = secs / 60;
        if (modsecs > 9) {
            return modifier + minutes + ":" + modsecs;
        } else {
            return modifier + minutes + ":0" + modsecs;
        }
    }

    /**
     * Set the title of the previous phase.
     * @param title New title.
     */
    public void setPastPhaseTitle(String title) {
        phase1title.setText(title);
    }

    /**
     * Set the time value of the previous phase.
     * @param time New text time.
     */
    public void setPastPhaseTime(String time) {
        phase1time.setText(time);
    }

    /**
     * Set the title of the current phase.
     * @param title New title.
     */
    public void setCurrentPhaseTitle(String title) {
        phase2title.setText("> " + title + " <");
    }

    /**
     * Set the time value of the current phase.
     * @param time New text time.
     */
    public void setCurrentPhaseTime(String time) {
	    phase2time.setText("> " + time + " <");
    }

    /**
     * Set the name and times for the next three phases.
     * @param phases An array of arrays with the first element signifying
     * the title of a phase and the second element signifying the time,
     * e.g., phases[0][1] is the time string of the next phase,
     * phases[1][0] is the phase name of the phase after that, and so
     * forth.
     */
    public void setNextThreePhases(String[][] phases) {
        phase3title.setText(phases[0][0]);
        phase3time.setText(phases[0][1]);
        phase4title.setText(phases[1][0]);
        phase4time.setText(phases[1][1]);
        phase5title.setText(phases[2][0]);
        phase5time.setText(phases[2][1]);
    }

    /**
     * Update the phase display to match the underlying data.
     */
    public void updateData() {

        ClientGameStatus gs = GUI.getAgent().getGameStatus();

     
        if(gs != null && gs.getPhases() != null ) {
            Phases ph = gs.getPhases();

	        if(ph.contains(ph.getCurrentPhaseName())) {
                    GamePhase currentPhase = ph.getCurrentPhase();
                    GamePhase nextPhase = ph.getNextPhase();
                    if( (nextPhase == null) && (ph.getCurrentSecsLeft() < 0) ) {
                        setCurrentPhaseTime("-:--");
                        setCurrentPhaseTitle("");
                        setPastPhaseTitle( currentPhase.getName() );
                        setPastPhaseTime(secsToTextTime(ph.getPhaseDuration()));
                    }
                    else {
                        setCurrentPhaseTitle( currentPhase.getName() );
                        setCurrentPhaseTime(secsToTextTime(ph.getCurrentSecsLeft()));
                            setPastPhaseTitle(ph.getPreviousPhaseName());
                        setPastPhaseTime(secsToTextTime(ph.getPhaseDuration(ph.getPreviousPhaseName())));
                    }
                    String[][] phases = new String[3][2];
                    GamePhase tempPhase = nextPhase;
                    //String tempPhaseName = nextPhase.getName();
                    for (int i = 0; i < 3; i++) {
                        String name = tempPhase == null ? "" : tempPhase.getName();
                        phases[i][0] = name;
                        phases[i][1] = secsToTextTime(ph.getPhaseDuration(name));

                        if( tempPhase != null ) { //then there are no new phases.
                            tempPhase = ph.getNextPhase(tempPhase);
                        }
                }
                setNextThreePhases(phases);
            }
            repaint();
        
        }
    }
/*
    public void updateData() {
        Phases ph = ClientData.getInstance().getGame().getPhases();
        ClientGameStatus gs = ClientData.getInstance().getGame();

        if (ph.isPhaseByName(gs.getPhases().getCurrentPhase())) {
            if (ph.getNextPhaseName(gs.getPhases()
                    .getCurrentPhase())
                    .equals("") &&
                    gs.getPhases().getCurrentSecsLeft() < 0) {
                setCurrentPhaseTime("-:--");
                setCurrentPhaseTitle("");
                setPastPhaseTitle(gs.getPhases().getCurrentPhase());
                setPastPhaseTime(secsToTextTime(ph.getPhaseTime(
                        gs.getPhases().getCurrentPhase())));
            } else {
                setCurrentPhaseTitle(gs.getPhases().getCurrentPhase());
                setCurrentPhaseTime(secsToTextTime(
                        gs.getPhases().getCurrentSecsLeft()));
                setPastPhaseTitle(ph.getPreviousPhaseName(
                        gs.getPhases().getCurrentPhase()));
                setPastPhaseTime(secsToTextTime(ph.getPhaseTime(ph.getPreviousPhaseName(
                        gs.getPhases().getCurrentPhase()))));
            }
            String[][] phases = new String[3][2];
            String tempPhaseName =
                    ph.getNextPhaseName(gs.getPhases().getCurrentPhase());
            for (int i = 0; i < 3; i++) {
                phases[i][0] = tempPhaseName;
                phases[i][1] =
                        secsToTextTime(ph.getPhaseTime(tempPhaseName));

                tempPhaseName = ph.getNextPhaseName(tempPhaseName);
            }
            setNextThreePhases(phases);
        }
        repaint();
    }
*/
    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// !!! IMPORTANT !!!
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * !!! IMPORTANT !!!
     * DO NOT edit this method OR call it in your code!
     */
    private void $$$setupUI$$$() {
        infoDisplayPanel = new JPanel();
        infoDisplayPanel.setLayout(
                new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), 0, 0));
        phasesTitle = new JTextField();
        phasesTitle.setEditable(false);
        phasesTitle.setEnabled(true);
        phasesTitle.setHorizontalAlignment(0);
        phasesTitle.setRequestFocusEnabled(false);
        phasesTitle.setText("Phases");
        infoDisplayPanel.add(phasesTitle,
                new GridConstraints(0, 0, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(170, -1), null));
        timeTitle = new JTextField();
        timeTitle.setEditable(false);
        timeTitle.setHorizontalAlignment(0);
        timeTitle.setText("Time Left");
        infoDisplayPanel.add(timeTitle,
                new GridConstraints(0, 1, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_BOTH,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null,
                        new Dimension(80, -1), null));
        phase1title = new JTextField();
        phase1title.setEditable(false);
        phase1title.setEnabled(true);
        phase1title.setHorizontalAlignment(0);
        phase1title.setText("  ");
        infoDisplayPanel.add(phase1title,
                new GridConstraints(1, 0, 1, 1,
                        GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(170, -1), null));
        phase2title = new JTextField();
        phase2title.setEditable(false);
        phase2title.setHorizontalAlignment(0);
        phase2title.setText("  ");
        infoDisplayPanel.add(phase2title,
                new GridConstraints(2, 0, 1, 1,
                        GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(170, -1), null));
        phase3title = new JTextField();
        phase3title.setEditable(false);
        phase3title.setEnabled(true);
        phase3title.setHorizontalAlignment(0);
        phase3title.setText("  ");
        infoDisplayPanel.add(phase3title,
                new GridConstraints(3, 0, 1, 1,
                        GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(170, -1), null));
        phase4title = new JTextField();
        phase4title.setEditable(false);
        phase4title.setEnabled(true);
        phase4title.setHorizontalAlignment(0);
        phase4title.setText("  ");
        infoDisplayPanel.add(phase4title,
                new GridConstraints(4, 0, 1, 1,
                        GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(170, -1), null));
        phase5title = new JTextField();
        phase5title.setEditable(false);
        phase5title.setEnabled(true);
        phase5title.setHorizontalAlignment(0);
        phase5title.setText("  ");
        infoDisplayPanel.add(phase5title,
                new GridConstraints(5, 0, 1, 1,
                        GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(170, -1), null));
        phase1time = new JTextField();
        phase1time.setEditable(false);
        phase1time.setEnabled(true);
        phase1time.setHorizontalAlignment(0);
        phase1time.setText("  ");
        infoDisplayPanel.add(phase1time,
                new GridConstraints(1, 1, 1, 1,
                        GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(80, -1), null));
        phase2time = new JTextField();
        phase2time.setEditable(false);
        phase2time.setHorizontalAlignment(0);
        phase2time.setText("  ");
        infoDisplayPanel.add(phase2time,
                new GridConstraints(2, 1, 1, 1,
                        GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(80, -1), null));
        phase3time = new JTextField();
        phase3time.setEditable(false);
        phase3time.setEnabled(true);
        phase3time.setHorizontalAlignment(0);
        phase3time.setText("  ");
        infoDisplayPanel.add(phase3time,
                new GridConstraints(3, 1, 1, 1,
                        GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(80, -1), null));
        phase4time = new JTextField();
        phase4time.setEditable(false);
        phase4time.setEnabled(true);
        phase4time.setHorizontalAlignment(0);
        phase4time.setText("  ");
        infoDisplayPanel.add(phase4time,
                new GridConstraints(4, 1, 1, 1,
                        GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(80, -1), null));
        phase5time = new JTextField();
        phase5time.setEditable(false);
        phase5time.setEnabled(true);
        phase5time.setHorizontalAlignment(0);
        phase5time.setText("  ");
        infoDisplayPanel.add(phase5time,
                new GridConstraints(5, 1, 1, 1,
                        GridConstraints.ANCHOR_WEST,
                        GridConstraints.FILL_HORIZONTAL,
                        GridConstraints.SIZEPOLICY_WANT_GROW,
                        GridConstraints.SIZEPOLICY_FIXED, null,
                        new Dimension(80, -1), null));
    }
}
