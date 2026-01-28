import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("bimbimbambam");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(640,480);
        MyPanel panel = new MyPanel();
        frame.add(panel);
        frame.setVisible(true);
        frame.repaint();
    }
}