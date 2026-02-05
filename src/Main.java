import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Структурные формулы");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 700);

            MyPanel panel = new MyPanel();
            frame.add(panel);
            frame.setVisible(true);

            // Панель для текущего ввода
            JPanel bottomPanel = new JPanel();
            JTextField inputField = new JTextField(25);
            JButton enterButton = new JButton("Enter");

                    // Действие при вводе
                    ActionListener enterAction = e -> {
                        String command = inputField.getText();
                        if (!command.isEmpty()) {
                            inputField.setText("");
                        }
                        inputField.requestFocus();
                    };

                    inputField.addActionListener(enterAction);
                    enterButton.addActionListener(enterAction);

                    // Компоновка
                    bottomPanel.add(new JLabel("> "));
                    bottomPanel.add(inputField);
                    bottomPanel.add(enterButton);

                    panel.add(bottomPanel, BorderLayout.NORTH);

                    frame.add(panel);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setVisible(true);

                    inputField.requestFocus();
                })
            ;}

        }