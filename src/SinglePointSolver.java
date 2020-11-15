import java.util.*;

/**
 * Minesweeper solver by Single Point Algorithm
 */
public class SinglePointSolver {

    private int gridRow, gridColumn;

    private SinglePointSolver() {}

    public static SinglePointSolver getSolver() {
        return new SinglePointSolver();
    }

    /**
     * Check whether the board is solvable by Single Point method
     * @param mineBoard the board to be solve
     * @param clickedSquareIndex the index of first clicked square
     * @return whether the board is solvable
     */
    public boolean isSolvable(MineBoard mineBoard, int clickedSquareIndex) {
        gridRow = mineBoard.getRow();
        gridColumn = mineBoard.getColumn();
        int mineNumber = mineBoard.getMineNumber();
        boolean mapUpdated = false;
        int totalFlagCouunt = 0;
        int totalProbedSqauresCount = 1; // include the first clicked one

        HashSet<Integer> frontierSquares = new HashSet<Integer>(2 * (gridRow + gridColumn));
        // set of squares which can provide information to probe other squares
        frontierSquares.add(clickedSquareIndex);

        while (!(mineNumber == totalFlagCouunt 
                || gridColumn * gridRow - totalProbedSqauresCount == mineNumber)) {
            mapUpdated = false;
            Integer[] keyList = frontierSquares.toArray(new Integer[0]);
            for (int key : keyList) {
                int row = (int) (key / gridColumn);
                int col = key % gridColumn;
                SquareButton square = mineBoard.getSquare(row, col);
                byte unprobedCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_UNPROBED);
                byte mineCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_MINE);
                byte flagCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_FLAG);
                if (mineCount == unprobedCount + flagCount || mineCount == flagCount) {
                    frontierSquares.remove(key);
                    for (SquareButton neighbor : mineBoard.getNeighbors(square)){
                        if (neighbor.isEnabled()) {
                            neighbor.setEnabled(false);
                            if (mineCount != flagCount) {
                                neighbor.setFlagged(true);
                                totalFlagCouunt++;
                            } else {
                                int[] position = neighbor.getPosition();
                                frontierSquares.add(position[0] * gridColumn + position[1]);
                                totalProbedSqauresCount++;
                            }
                        }
                    }
                    mapUpdated = true;
                    break;
                }
            }
            if (!mapUpdated) {
                return false;
            }
        }
        return true;
    }
}
