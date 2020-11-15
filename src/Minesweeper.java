import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * A guess-free minesweeper game with GUI
 * @author purindaisuki
 */
public class Minesweeper {
    private MineBoard mineBoard;
    private int gridRow = 9;
    private int gridColumn = 9;
    private int mineNumber = 10;
    private int restMineNumber = mineNumber;

    private JFrame frame;
    private JPanel mainPanel;
    private JPanel boardPanel;
    private JButton restartButton;
    private JLabel restMineNumberLabel;
    private JLabel timerLabel;
    private ImageIcon plainIcon;
    private ImageIcon winIcon;
    private ImageIcon loseIcon;
    private ImageIcon squareIcon;

    private int time;
    private Timer timer;

    private boolean firstClicked = false;
    private boolean leftClickState = false;
    private boolean rightClickState = false;

    /**
     * Set up GUI
     */
    private void setUpGUI() {
        frame = new JFrame("Minesweeper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JMenuBar menuBar = new JMenuBar();
        JMenu difficultyMenu = new JMenu("Difficulty");
        JMenuItem beginnerItem = new JMenuItem("Beginner");
        JMenuItem intermediateItem = new JMenuItem("Intermediate");
        JMenuItem expertItem = new JMenuItem("Expert");
        beginnerItem.addActionListener((event) -> {
            gridRow = 9;
            gridColumn = 9;
            mineNumber = 10;
            resetBoard();
        });
        intermediateItem.addActionListener((event) -> {
            gridRow = 15;
            gridColumn = 13;
            mineNumber = 40;
            resetBoard();
        });
        expertItem.addActionListener((event) -> {
            gridRow = 16;
            gridColumn = 30;
            mineNumber = 99;
            resetBoard();
        });
        difficultyMenu.add(beginnerItem);
        difficultyMenu.add(intermediateItem);
        difficultyMenu.add(expertItem);
        menuBar.add(difficultyMenu);
        frame.setJMenuBar(menuBar);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.getContentPane().add(mainPanel);

        Box functionBox = new Box(BoxLayout.X_AXIS);
        timerLabel = new JLabel("000");
        timer = new Timer(1000, (event) -> {
            if (time < 1000) {
                timerLabel.setText(String.format("%03d", time));
                time++;
            }
        });

        restartButton = new JButton();
        restartButton.setPreferredSize(new Dimension(25, 25));
        restartButton.setMargin(new Insets(0, 0, 0, 0));
        restartButton.setIcon(plainIcon);
        restartButton.addActionListener((event) -> restart());

        restMineNumberLabel = new JLabel(Integer.toString(mineNumber));

        functionBox.add(timerLabel);
        functionBox.add(Box.createRigidArea(new Dimension(50, 0)));
        functionBox.add(restartButton);
        functionBox.add(Box.createRigidArea(new Dimension(50, 0)));
        functionBox.add(restMineNumberLabel);
        mainPanel.add(functionBox);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        setUpBoardPanel();
        mainPanel.add(boardPanel);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * set up the board panel
     */
    public void setUpBoardPanel() {
        boardPanel = new JPanel(new GridLayout(gridRow, gridColumn));
        mineBoard = new MineBoard(gridRow, gridColumn, mineNumber);
        for (int row = 0; row < gridRow; row++) {
            for (int col = 0; col < gridColumn; col++) {
                SquareButton square = new SquareButton();
                square.setIcon(squareIcon);
                square.setPreferredSize(new Dimension(20, 20));
                square.setMargin(new Insets(0, 0, 0, 0));
                square.setHorizontalTextPosition(SwingConstants.CENTER);
                square.addMouseListener(new MouseInputAdapter(){
                    public void mouseReleased(MouseEvent event) {
                        if (SwingUtilities.isLeftMouseButton(event)) {
                            leftClickState = false;
                            if (rightClickState && square.isEnabled() && square.isSelected()) {
                                // if right button still held, compensate the left click
                                square.setSelected(false);
                            }
                        }
                        else if (SwingUtilities.isRightMouseButton(event)) {
                            rightClickState = false;
                        }
                    }
                    public void mousePressed(MouseEvent event) {
                        if (SwingUtilities.isLeftMouseButton(event)) leftClickState = true;
                        else if (SwingUtilities.isRightMouseButton(event)) rightClickState = true;

                        // if both buttons hold at the same time
                        if (leftClickState && rightClickState) {
                            if (square.isSelected()) {
                                mineBoard.probeNeighbors(square);
                                if (mineBoard.isClear() || mineBoard.isFailed()) {
                                    gameOver(mineBoard.isClear());
                                }
                            }
                        }
                        // Only left button pressed
                        else if (leftClickState) {
                            if (square.isEnabled()) {
                                mineBoard.probe(square);
                                if (!firstClicked) {
                                    firstClicked = true;
                                    timer.start();
                                }
                                if (mineBoard.isClear() || mineBoard.isFailed()) {
                                    gameOver(mineBoard.isClear());
                                }
                            }
                        }
                        // Only right button pressed
                        else if (rightClickState){
                            if (!square.isSelected()) {
                                if (square.isFlagged()) {
                                    mineBoard.unflag(square);
                                    restMineNumber++;
                                    restMineNumberLabel.setText(Integer.toString(restMineNumber));
                                }
                                else {
                                    mineBoard.flag(square);
                                    restMineNumber--;
                                    restMineNumberLabel.setText(Integer.toString(restMineNumber));
                                }
                            }
                        }
                    }
                });
                mineBoard.setSquare(row, col, square);
                boardPanel.add(square);
            }
        }
    }

    /**
     * reset the board and restart
     */
    public void resetBoard() {
        mainPanel.remove(boardPanel);
        setUpBoardPanel();
        mainPanel.add(boardPanel);
        mainPanel.revalidate();
        frame.pack();
        frame.revalidate();
        restMineNumber = mineNumber;
        restart();
    }

    public void loadImages() {
        plainIcon = MineBoard.getScaledImageIcon(25, 25, "./images/plain.png");
        winIcon = MineBoard.getScaledImageIcon(25, 25, "./images/win.png");
        loseIcon = MineBoard.getScaledImageIcon(25, 25, "./images/lose.png");
        squareIcon = MineBoard.getScaledImageIcon(20, 20, "./images/square.png");
    }

    /**
     * Start the game
     */
    public void startGame() {
        loadImages();
        setUpGUI();
        mineBoard.initialize();
    }

    /**
     * Restart the game
     */
    public void restart() {
        time = 0;
        firstClicked = false;
        timerLabel.setText("000");

        restMineNumber = mineNumber;
        restMineNumberLabel.setText(Integer.toString(restMineNumber));

        restartButton.setIcon(plainIcon);

        mineBoard.initialize();
    }

    /**
     * End the game
     * @param result whether user win the game
     */
    private void gameOver(Boolean win) {
        timer.stop();
        if (win) {
            restartButton.setIcon(winIcon);
            restMineNumberLabel.setText("0");
        }
        else {
            restartButton.setIcon(loseIcon);
        }
        mineBoard.freezeBoard();
    }
    public static void main(String[] args) {
        new Minesweeper().startGame();
    }
}
