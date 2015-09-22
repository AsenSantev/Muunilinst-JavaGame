package game;

import enums.Controls;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class InputHandler {

    public static void processInput(final JTetris tetris) {
        // LEFT
        tetris.getInputMap(tetris.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
                '4'), "left");

        tetris.getInputMap(tetris.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
                'j'), "left");

        tetris.getActionMap().put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tetris.tick(Controls.LEFT);
            }
        });

        // RIGHT
        tetris.getInputMap(tetris.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
                '6'), "right");

        tetris.getInputMap(tetris.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
                'l'), "right");

        tetris.getActionMap().put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tetris.tick(Controls.RIGHT);
            }
        });

        // ROTATE
        tetris.getInputMap(tetris.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
                '5'), "rotate");


        tetris.getInputMap(tetris.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
                'k'), "rotate");

        tetris.getActionMap().put("rotate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tetris.tick(Controls.ROTATE);
            }
        });

        // DROP
        tetris.getInputMap(tetris.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
                '0'), "drop");

        tetris.getInputMap(tetris.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(
                'n'), "drop");

        tetris.getActionMap().put("drop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tetris.tick(Controls.DROP);
            }
        });

        // Create the Timer object and have it send
        // tick(DOWN) periodically
        tetris.timer = new Timer(tetris.DELAY, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tetris.tick(Controls.DOWN);
            }
        });
    }
}
