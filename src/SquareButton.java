import javax.swing.*;

/**
 * A class inherits JToggleButton
 * to record information about the properties of a square
 * @author purindaisuki
 */
public class SquareButton extends JToggleButton{
    private boolean mine = false;
    private boolean flag = false;
    private int[] squarePosition = new int[2];

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean m) {
        mine = m;
    }

    public boolean isFlagged() {
        return flag;
    }

    public void setFlagged(boolean f) {
        flag = f;
        // Squares flagged must be disabled and vice versa
        this.setEnabled(!f);
    }

    public int[] getPosition() {
        return squarePosition;
    }

    public void setPosition(int row, int col) {
        squarePosition[0] = row;
        squarePosition[1] = col;
    }
}
