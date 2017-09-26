package ramo.klevis.ml.emailspam;

import org.apache.spark.mllib.evaluation.MulticlassMetrics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

/**
 * Created by klevis.ramo on 9/26/2017.
 */
public class EmailUI {

    private static final int FRAME_WIDTH = 1000;
    private static final int FRAME_HEIGHT = 600;
    private JProgressBar progressBar;
    private JPanel mainPanel;
    private JFrame mainFrame;
    private LogisticRegression logisticRegression;
    private JTextArea textArea;

    public EmailUI(LogisticRegression logisticRegression) {
        this.logisticRegression = logisticRegression;
        initUI();
    }

    private void initUI() {
        mainFrame = createMainFrame();

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new GridLayout(15, 1));
        JButton test = createTestButton();
        buttonPanel.add(test);
        JButton train = createTrainButton();
        buttonPanel.add(train);
        mainPanel.add(buttonPanel, BorderLayout.WEST);

        textArea = new JTextArea();
        textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        mainPanel.add(textArea, BorderLayout.CENTER);

        addSignature(mainPanel);

        mainFrame.add(mainPanel);

        mainFrame.setVisible(true);
    }

    private void addSignature(JPanel mainFrame) {
        JLabel signature = new JLabel("ramok.tech", JLabel.HORIZONTAL);
        signature.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 16));
        signature.setForeground(Color.DARK_GRAY);
        mainFrame.add(signature, BorderLayout.SOUTH);
    }

    private JButton createTestButton() {
        JButton test = new JButton("Test");
        test.addActionListener(action -> {

            try {
                if (!logisticRegression.isTrained()) {
                    JOptionPane.showMessageDialog(mainFrame, "Please train the algorithm first");
                    return;
                }
                JOptionPane.showMessageDialog(mainFrame, "" + (logisticRegression.test(textArea.getText()) == 1d ? "SPAM" : "Not SPAM"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return test;
    }

    private JButton createTrainButton() {
        JButton train = new JButton("Train");
        train.addActionListener(action -> {
            showProgressBar();

            Runnable runnable = () -> {
                try {
                    MulticlassMetrics execute = logisticRegression.execute();
                    JOptionPane.showMessageDialog(mainFrame, "Algorithm trained with accuracy : "+execute.accuracy());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    progressBar.setVisible(false);
                }
            };
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.start();

        });
        return train;
    }

    private void showProgressBar() {
        SwingUtilities.invokeLater(() -> {
            progressBar = createProgressBar(mainFrame);
            progressBar.setString("Please wait it may take one or two minutes");
            progressBar.setStringPainted(true);
            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            mainPanel.add(progressBar, BorderLayout.NORTH);
            mainFrame.repaint();
        });
    }

    private JProgressBar createProgressBar(JFrame mainFrame) {
        JProgressBar jProgressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        mainFrame.add(jProgressBar, BorderLayout.NORTH);
        return jProgressBar;
    }

    private JFrame createMainFrame() {
        JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Email Spam Detector");
        mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        mainFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
        return mainFrame;
    }
}
