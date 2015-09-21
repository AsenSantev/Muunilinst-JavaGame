import game.InputHandler;
import game.JTetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TetrisMain {
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Tetris by Team Muunilinst");
        JComponent container = (JComponent) frame.getContentPane();
        container.setLayout(new BorderLayout());

        // Set the metal look and feel
        try {
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        final int pixels = 16;
        JTetris.tetris = new JTetris(JTetris.WIDTH * pixels + 2, (JTetris.HEIGHT + JTetris.TOP_SPACE) * pixels + 2);
        InputHandler.processInput(JTetris.tetris);
        container.add(JTetris.tetris, BorderLayout.CENTER);

        Container panel = JTetris.tetris.createControlPanel();

        // Add the quit button last so it's at the bottom
        container.add(panel, BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);

        // Quit on window close
        frame.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
    }
}