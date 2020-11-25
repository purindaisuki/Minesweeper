import java.util.*;

/**
 * Minesweeper solver by Single Point Algorithm
 * and Constraint Satisfication Problem strategy
 * @author purindaisuki
 */
public class SPwCSPSolver {

    private SPwCSPSolver() {}

    public static SPwCSPSolver getSolver() {
        return new SPwCSPSolver();
    }
    
    /**
     * A class for representing a set of constraints
     * on the number of mines a set of squares contain
     */
    private class Constraints implements Set<Integer> {
        // mineNumber is the sum of mines in the squares (represented as their indices)
        private byte mineNumber;
        private ArrayList<Integer> squaresIndices;

        public Constraints() {
            this((byte) 0);
        }

        public Constraints(byte number) {
            mineNumber = number;
            squaresIndices = new ArrayList<Integer>();
        }

        public byte getMineNumber() {
            return mineNumber;
        }

        public void setMineNumber(byte number) {
            mineNumber = number;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 47 * hash + Objects.hashCode(this.squaresIndices != null ? this.squaresIndices.hashCode() : 0);
            hash = 47 * hash + Objects.hashCode(this.mineNumber);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Constraints other = (Constraints) obj;
            if (mineNumber != other.mineNumber) {
                return false;
            }
            if (!squaresIndices.equals(other.squaresIndices)) {
                return false;
            }
            return true;
        }

        public int size() {
            return squaresIndices.size();
        }
    
        public boolean isEmpty() {
            return squaresIndices.isEmpty();
        }
    
        public boolean contains(Object o) {
            return squaresIndices.contains((Integer) o);
        }
    
        public Iterator<Integer> iterator() {
            return squaresIndices.iterator();
        }
    
        public Object[] toArray() {
            return squaresIndices.toArray();
        }
    
        public <T> T[] toArray(T[] a) {
            return squaresIndices.toArray(a);
        }
    
        public boolean add(Integer e) {
            return squaresIndices.add(e);
        }
    
        public boolean remove(Object o) {
            return squaresIndices.remove((Integer) o);
        }
    
        public boolean containsAll(Collection<?> c) {
            return squaresIndices.containsAll(c);
        }
    
        public boolean addAll(Collection<? extends Integer> c) {
            return squaresIndices.addAll(c);
        }

        public boolean retainAll(Collection<?> c) {
            return squaresIndices.retainAll(c);
        }

        public boolean removeAll(Collection<?> c) {
            return squaresIndices.removeAll(c);
        }
    
        public void clear() {
            squaresIndices.clear();
        }
    }

    /**
     * Check whether the board is solvable by Single Point method and CSP Strategy
     * @param mineBoard the board to be solve
     * @param clickedSquareIndex the index of first clicked square
     * @return whether the board is solvable
     */
    public boolean isSolvable(MineBoard mineBoard, int clickedSquareIndex) {
        int gridRow = mineBoard.getRow();
        int gridColumn = mineBoard.getColumn();
        int mineNumber = mineBoard.getMineNumber();
        boolean mapUpdated = false;
        int totalFlagCount = 0;
        int totalProbedSqauresCount = 1; // include the first clicked one

        HashSet<Integer> frontierSquares = new HashSet<Integer>(2 * (gridRow + gridColumn));
        // set of squares which can provide information to probe other squares
        frontierSquares.add(clickedSquareIndex);

        while (!(mineNumber == totalFlagCount 
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
                                totalFlagCount++;
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
            if (mapUpdated) {
                // if SP sucesses, keep using it since it's faster than CSP
                continue;
            }

            //if SP fails, use CSP
            HashSet<Constraints> constraintsSet = new HashSet<Constraints>(2 * (gridRow + gridColumn));
            // generate constraints from info provided by frontier squares
            for (int key : keyList) {
                int row = (int) (key / gridColumn);
                int col = key % gridColumn;
                SquareButton square = mineBoard.getSquare(row, col);
                byte mineCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_MINE);
                byte flagCount = mineBoard.countNeighor(square, MineBoard.COUNT_NEIGHBOR_FLAG);
                Constraints squareConstraints = new Constraints((byte) (mineCount - flagCount));
                for (SquareButton neighbor : mineBoard.getNeighbors(square)) {
                    if (neighbor.isEnabled()) {
                        int[] position = neighbor.getPosition();
                        int index = position[0] * gridColumn + position[1];
                        squareConstraints.add(index);
                    }
                }
                if (!squareConstraints.isEmpty()) {
                    constraintsSet.add(squareConstraints);
                }
            }
            // decompose constraints according to their overlaps and differences
            boolean constraintsSetUpdated = true;
            while (constraintsSetUpdated) {
                constraintsSetUpdated = false;
                Constraints[] constraintsList = constraintsSet.toArray(new Constraints[0]);
                for (Constraints constraints1 : constraintsList) {
                    for (Constraints constraints2 : constraintsList) {
                        if (!constraints1.equals(constraints2)) {
                            // if constraints1 is included in constraints2
                            if (isProperSubset(constraints1, constraints2)) {
                                Constraints diffConstraints = new Constraints();
                                for (int entry2 : constraints2) {
                                    if (!constraints1.contains(entry2)) {
                                        diffConstraints.add(entry2);
                                    }
                                }
                                // decompose the constraint
                                byte diffMineNumber = (byte) (constraints2.getMineNumber() - constraints1.getMineNumber());
                                diffConstraints.setMineNumber(diffMineNumber);
                                constraintsSetUpdated = constraintsSet.add(diffConstraints);
                                constraintsSet.remove(constraints2);
                            }
                        }
                    }
                }
            }
            // solve variables if All-Free-Neighbor or All-Mine-Neighbor
            for (Constraints constraints : constraintsSet) {
                byte mines = constraints.getMineNumber();
                if (mines == 0 || mines == constraints.size()) {
                    for (int squareIndex : constraints) {
                        int row = (int) (squareIndex / gridColumn);
                        int col = squareIndex % gridColumn;
                        SquareButton square = mineBoard.getSquare(row, col);
                        if (square.isEnabled()){
                            square.setEnabled(false);
                            if (mines == 0) {
                                // if AFN
                                frontierSquares.add(squareIndex);
                                totalProbedSqauresCount++;
                            } else {
                                // if AMN
                                square.setFlagged(true);
                                totalFlagCount++;
                            }
                        }
                    }
                    mapUpdated = true;
                }
            }
            if (!mapUpdated) {
                // if both SP and CSP fail, return unsolvable
                return false;
            }
        }
        return true;
    }

    /**
     * Return whether a set is a proper subset of another one
     * @param <T> type of entry in the set
     * @param set1 
     * @param set2
     * @return true if set1 is a proper subset of set2
     */
    public <T> boolean isProperSubset(Set<T> set1, Set<T> set2) {
        if (set1.size() > set2.size()) {
            return false;
        }
        boolean properSubset = true;
        for (T entry1 : set1) {
            if (!set2.contains(entry1)) {
                properSubset = false;
                break;
            }
        }
        return properSubset;
    }
}
