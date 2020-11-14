import javax.swing.*;

public class SquareButton extends JToggleButton{
    private boolean mine = false;
    private boolean flag = false;
    private int[] squarePosition = new int[2];

    public boolean isMine() {
        //check whether is a mine
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
