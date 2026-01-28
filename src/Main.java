import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Структурные формулы");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);

            MyPanel panel = new MyPanel();
            frame.add(panel);
            frame.setVisible(true);
        });
    }
}