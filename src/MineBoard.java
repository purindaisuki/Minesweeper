import java.util.*;
import javax.swing.*;
import java.awt.*;

/**
 * A class for mineboard and operations on it
 * @author purindaisuki
 */
public class MineBoard {
    public final static byte COUNT_NEIGHBOR_MINE = 0;
    public final static byte COUNT_NEIGHBOR_FLAG = 1;
    public final static byte COUNT_NEIGHBOR_UNPROBED = 2;

    private ImageIcon zeroIcon;
    private ImageIcon oneIcon;
    private ImageIcon twoIcon;
    private ImageIcon threeIcon;
    private ImageIcon fourIcon;
    private ImageIcon fiveIcon;
    private ImageIcon sixIcon;
    private ImageIcon sevenIcon;
    private ImageIcon eightIcon;
    private ImageIcon mineIcon;
    private ImageIcon flagIcon;
    private ImageIcon squareIcon;
    
    private boolean boardExplode = false;
    private boolean boardClear = false;

    private final int gridRow;
    private final int gridColumn;
    private final int mineNumber;
    private final SquareButton[][] squares;

    private int probedSquareNumber;

    public MineBoard(int row, int col, int mineNumber) {
        gridRow = row;
        gridColumn = col;
        this.mineNumber = mineNumber;
        squares = new SquareButton[row][col];
        loadImages();
    }

    private void loadImages() {
        zeroIcon = getScaledImageIcon(20, 20, "./images/number0.png");
        oneIcon = getScaledImageIcon(20, 20, "./images/number1.png");
        twoIcon = getScaledImageIcon(20, 20, "./images/number2.png");
        threeIcon = getScaledImageIcon(20, 20, "./images/number3.png");
        fourIcon = getScaledImageIcon(20, 20, "./images/number4.png");
        fiveIcon = getScaledImageIcon(20, 20, "./images/number5.png");
        sixIcon = getScaledImageIcon(20, 20, "./images/number6.png");
        sevenIcon = getScaledImageIcon(20, 20, "./images/number7.png");
        eightIcon = getScaledImageIcon(20, 20, "./images/number8.png");
        mineIcon = getScaledImageIcon(20, 20, "./images/mine.png");
        flagIcon = getScaledImageIcon(20, 20, "./images/flag.png");
        squareIcon = getScaledImageIcon(20, 20, "./images/square.png");
    }

    /**
     * load and scale a image
     * @param width width of the icon
     * @param height height of the icon
     * @param dir path of the image
     * @return scaled icon
     */
    public static ImageIcon getScaledImageIcon(int width, int height, String dir) {
        ImageIcon imageIcon = new ImageIcon(MineBoard.class.getResource(dir));
        Image image = imageIcon.getImage();
        image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    /**
     * Initialize the board
     * Clear all mines, flags and selected squares
     */
    public void initialize() {
        boardExplode = false;
        boardClear = false;
        probedSquareNumber = 0;
        for (int row = 0; row < gridRow; row++) {
            for (int col = 0; col < gridColumn; col++) {
                SquareButton square = squares[row][col];
                square.setMine(false);
                square.setEnabled(true);
                square.setSelected(false);
                square.setFlagged(false);
                square.setPosition(row, col);
                square.setDisabledIcon(squareIcon);
            }
        }
    }

    public void setSquare(int row, int col, SquareButton s) {
        squares[row][col] = s;
    }

    public SquareButton getSquare(int row, int col) {
        return squares[row][col];
    }

    public int getRow() {
        return gridRow;
    }

    public int getColumn() {
        return gridColumn;
    }

    public int getMineNumber() {
        return mineNumber;
    }

    /**
     * Generate map requiring no guessing
     * @param square first clicked square
     */
    public void generateSolvableMap(SquareButton clickedSquare) {
        int randRow, randCol;
        int placedMineNum = 0;
        int[] position = clickedSquare.getPosition();
        SPwCSPSolver solver = SPwCSPSolver.getSolver();
        while (true) {
            placedMineNum = 0;
            // initialize the map except for first clicked button
            initialize();
            probedSquareNumber = 1;
            clickedSquare.setSelected(true);
            clickedSquare.setEnabled(false);
            clickedSquare.setDisabledIcon(zeroIcon);
            // randomly place mines
            while (placedMineNum < mineNumber) {
                randRow = (int) (Math.random() * gridRow);
                randCol = (int) (Math.random() * gridColumn);
                SquareButton randSquare = getSquare(randRow, randCol);
                if (!(randRow >= position[0] - 1 && randRow <= position[0] + 1
                      && randCol >= position[1] - 1 && randCol <= position[1] + 1)
                    && !randSquare.isMine()) {
                    //make sure not to place mines around or at the clicked square which causes guessing
                    randSquare.setMine(true);
                    placedMineNum++;
                }
            }
            // check whether is solvable without guessing
            boolean isSolvable = solver.isSolvable(this, position[0] * gridColumn + position[1]);
            if (isSolvable) {
                break;
            }
        }
        // clear solver's operation amd set icons
        for (int row = 0; row < gridRow; row++) {
            for (int col = 0; col < gridColumn; col++) {
                if (!(row == position[0] && col == position[1])) {
                    SquareButton square = getSquare(row, col);
                    square.setEnabled(true);
                    square.setFlagged(false);
                }
            }
        }
    }

    public boolean isClear() {
        return boardClear;
    }

    public boolean isFailed() {
        return boardExplode;
    }

    /**
     * disable all squares and show mines
     */
    public void freezeBoard() {
        for (int row = 0; row < gridRow; row++) {
            for (int col = 0; col < gridColumn; col++) {
                SquareButton square = getSquare(row, col);
                square.setEnabled(false);
                square.setSelected(true);
                if (square.isMine()){
                    // display mine as flag icon when user wins, otherwise mine icon
                    if (boardClear) square.setDisabledIcon(flagIcon);
                    else square.setDisabledIcon(mineIcon);
                }
            }
        }
    }

    /**
     * get squares adjacent to the square
     * @param square The square whose neighbors are going to be return
     * @return neighborSquares squares adjacent to the square
     */
    public ArrayList<SquareButton> getNeighbors(SquareButton square) {
        ArrayList<SquareButton> neighborSquares = new ArrayList<SquareButton>();
        int[] squarePosition = square.getPosition();
        int row, col;
        for (int i = -1; i < 2; i++) {
            row = squarePosition[0] + i;
            if (row >= 0 && row < gridRow) {
                for (int j = -1; j < 2; j++) {
                    col = squarePosition[1] + j;
                    if (col >= 0 && col < gridColumn && !(i == 0 && j == 0)) {
                        neighborSquares.add(squares[row][col]);
                    }
                }
            }
        }
        return neighborSquares;
    }

    /**
     * Count properties of neighbor squares
     * @param square The square whose neighbors are going to be counted
     * @param countKey Define which property to be counted
     * @return counts of the property in neighbor squares
     */
    public byte countNeighor(SquareButton square, byte countKey) {
        byte count = 0;
        for (SquareButton neighborSquare : getNeighbors(square)) {
            if ((countKey == COUNT_NEIGHBOR_MINE && neighborSquare.isMine())
                || (countKey == COUNT_NEIGHBOR_FLAG && neighborSquare.isFlagged())
                || (countKey == COUNT_NEIGHBOR_UNPROBED && neighborSquare.isEnabled())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Probe the square
     * @param square The square to be probed
     */
    public void probe(SquareButton square) {
        square.setEnabled(false);
        square.setSelected(true);
        if (probedSquareNumber++ == 0) {
            //generate map after the first square is pressed
            generateSolvableMap(square);
        }
        if (square.isMine()) {
            boardExplode = true;
            square.setDisabledIcon(mineIcon);
        } else {
            byte mineCount = countNeighor(square, COUNT_NEIGHBOR_MINE);
            switch (mineCount) {
                case(0) :
                    square.setDisabledIcon(zeroIcon);
                    break;
                case(1) : 
                    square.setDisabledIcon(oneIcon);
                    break;
                case(2) : 
                    square.setDisabledIcon(twoIcon);
                    break;
                case(3) : 
                    square.setDisabledIcon(threeIcon);
                    break;
                case(4) : 
                    square.setDisabledIcon(fourIcon);
                    break;
                case(5) : 
                    square.setDisabledIcon(fiveIcon);
                    break;
                case(6) : 
                    square.setDisabledIcon(sixIcon);
                    break;
                case(7) : 
                    square.setDisabledIcon(sevenIcon);
                    break;
                case(8) : 
                    square.setDisabledIcon(eightIcon);
                    break;
            }
            if (probedSquareNumber + mineNumber == gridRow * gridColumn) {
                boardClear = true;
            } else if (mineCount == 0) {
                // automatically probe neighbors if there is no mine around
                probeNeighbors(square);
            }
        }
    }

    /**
     * Flag the square
     * @param square The square to be flagged
     */
    public void flag(SquareButton square) {
        square.setFlagged(true);
        square.setDisabledIcon(flagIcon);
    }

    /**
     * Unflag the square
     * @param square The square to be unflagged
     */
    public void unflag(SquareButton square) {
        square.setFlagged(false);
    }

    /**
     * Probe all unprobed neighbor squares if AFN (All-Free-Neighbor)
     * @param square The square whose neighbors are going to be probed
     */
    public void probeNeighbors(SquareButton square) {
        byte mineCount = countNeighor(square, COUNT_NEIGHBOR_MINE);
        byte flagCount = countNeighor(square, COUNT_NEIGHBOR_FLAG);
        // if AFN
        if (mineCount == flagCount) {
            for (SquareButton neighborSquare : getNeighbors(square)) {
                if (neighborSquare.isEnabled() && !neighborSquare.isSelected()) {
                    probe(neighborSquare);
                }
            }
        }
    }
}
