package ramo.klevis.ml.emailspam;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.optimization.L1Updater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by klevis.ramo on 9/26/2017.
 */
public class EmailUI implements Serializable {

    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 400;
    private JProgressBar progressBar;
    private JPanel mainPanel;
    private JFrame mainFrame;
    private JTextArea textArea;
    private int featureSize;
    private ClassificationAlgorithm classificationAlgorithm;

    public EmailUI(int featureSize) {
        this.featureSize = featureSize;
        initUI();
    }

    private void initUI() {
        mainFrame = createMainFrame();

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new GridLayout(10, 1));
        JButton test = createTestButton();
        buttonPanel.add(test);
        JButton train = createTrainButton();
        buttonPanel.add(train);
        buttonPanel.add(createSGDTrainButton());
        buttonPanel.add(createSVMTrainButton());
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
        JButton test = new JButton("Test Your Email");
        test.addActionListener(action -> {

            try {
                if (classificationAlgorithm == null || !classificationAlgorithm.isTrained()) {
                    JOptionPane.showMessageDialog(mainFrame, "Please train the algorithm first");
                    return;
                }
                JOptionPane.showMessageDialog(mainFrame, "" + (classificationAlgorithm.test(textArea.getText()) == 1d ? "SPAM" : "Not SPAM"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return test;
    }

    private JButton createTrainButton() {
        JButton train = new JButton("Train with LR LBFGS");
        train.addActionListener(action -> {
            showProgressBar();

            Runnable runnable = () -> {
                try {
                    if (classificationAlgorithm != null) {
                        classificationAlgorithm.dispose();
                    }
                    long start = System.currentTimeMillis();
                    LogisticRegressionWithLBFGS model = new LogisticRegressionWithLBFGS();
                    model.setNumClasses(2);
                    classificationAlgorithm = new ClassificationAlgorithm(featureSize, model);
                    MulticlassMetrics execute = classificationAlgorithm.execute();
                    long time = System.currentTimeMillis() - start;
                    JOptionPane.showMessageDialog(mainFrame, "Algorithm trained with accuracy : " + execute.accuracy() + " in " + time / 1000 + " seconds");
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

    private JButton createSGDTrainButton() {
        JButton train = new JButton("Train with LR SGD");
        train.addActionListener(action -> {
            showProgressBar();

            Runnable runnable = () -> {
                try {
                    if (classificationAlgorithm != null) {
                        classificationAlgorithm.dispose();
                    }
                    long start = System.currentTimeMillis();
                    LogisticRegressionWithSGD model = new LogisticRegressionWithSGD();
                    classificationAlgorithm = new ClassificationAlgorithm(featureSize, model);
                    MulticlassMetrics execute = classificationAlgorithm.execute();
                    long time = System.currentTimeMillis() - start;
                    JOptionPane.showMessageDialog(mainFrame, "Algorithm trained with accuracy : " + execute.accuracy() + " in " + time / 1000 + " seconds");
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


    private JButton createSVMTrainButton() {
        JButton train = new JButton("Train with SVM SGD");
        train.addActionListener(action -> {
            showProgressBar();

            Runnable runnable = () -> {
                try {
                    if (classificationAlgorithm != null) {
                        classificationAlgorithm.dispose();
                    }
                    long start = System.currentTimeMillis();
                    classificationAlgorithm = new ClassificationAlgorithm(featureSize, new SVMWithSGD());
                    MulticlassMetrics execute = classificationAlgorithm.execute();
                    long time = System.currentTimeMillis() - start;
                    JOptionPane.showMessageDialog(mainFrame, "Algorithm trained with accuracy : " + execute.accuracy() + " in " + time / 1000 + " seconds");
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
