import java.util.*;

/**
 * Minesweeper solver by Double Set Single Point Algorithm
 */
public class DSSPSolver {

    private int gridRow, gridColumn;

    private DSSPSolver() {}

    public static DSSPSolver getSolver() {
        return new DSSPSolver();
    }

    /**
     * Check whether the board is solvable by Double Set Single Point method
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
        int totalProbedSqauresCount = 0;

        HashSet<Integer> squaresToProbe = new HashSet<Integer>(2 * (gridRow + gridColumn));
        // set of covered squares to be probed
        HashSet<Integer> frontierSquares = new HashSet<Integer>(2 * (gridRow + gridColumn));
        // set of squares which can provide information to probe other squares
        squaresToProbe.add(clickedSquareIndex);

        while (!(mineNumber == totalFlagCouunt 
                || gridColumn * gridRow - totalProbedSqauresCount == mineNumber)) {
            mapUpdated = false;
            while (squaresToProbe.size() > 0) {
                mapUpdated = true;
                Integer[] keyList = squaresToProbe.toArray(new Integer[0]);
                for (int key : keyList) {
                    int row = (int) (key / gridColumn);
                    int col = key % gridColumn;
                    SquareButton square = mineBoard.getSquare(row, col);
                    byte mineCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_MINE);
                    byte flagCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_FLAG);
                    squaresToProbe.remove(key);
                    square.setEnabled(false);
                    totalProbedSqauresCount++;
                    if (mineCount == flagCount) {
                        for (SquareButton neighbor : mineBoard.getNeighbors(square)){
                            if (neighbor.isEnabled()) {
                                int[] position = neighbor.getPosition();
                                squaresToProbe.add(position[0] * gridColumn + position[1]);
                            }
                        }
                    } else {
                        frontierSquares.add(key);
                    }
                    if (squaresToProbe.size() == 0) break;
                }
            }
            Integer[] keyList = frontierSquares.toArray(new Integer[0]);
            for (int key : keyList) {
                int row = (int) (key / gridColumn);
                int col = key % gridColumn;
                SquareButton square = mineBoard.getSquare(row, col);
                byte mineCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_MINE);
                byte flagCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_FLAG);
                byte unprobedCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_UNPROBED);
                if (mineCount == unprobedCount + flagCount) {
                    mapUpdated = true;
                    frontierSquares.remove(key);
                    for (SquareButton neighbor : mineBoard.getNeighbors(square)){
                        if (neighbor.isEnabled()) {
                            neighbor.setEnabled(false);
                            neighbor.setFlagged(true);
                            totalFlagCouunt++;
                        }
                    }
                }
            }
            keyList = frontierSquares.toArray(new Integer[0]);
            for (int key : keyList) {
                int row = (int) (key / gridColumn);
                int col = key % gridColumn;
                SquareButton square = mineBoard.getSquare(row, col);
                byte mineCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_MINE);
                byte flagCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_FLAG);
                if (mineCount == flagCount) {
                    mapUpdated = true;
                    frontierSquares.remove(key);
                    for (SquareButton neighbor : mineBoard.getNeighbors(square)){
                        if (neighbor.isEnabled()) {
                            int[] position = neighbor.getPosition();
                            squaresToProbe.add(position[0] * gridColumn + position[1]);
                        }
                    }
                }
            }
            if (!mapUpdated) {
                return false;
            }
        }
        return true;
    }
}
