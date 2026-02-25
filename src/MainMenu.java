import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Химический редактор - Главное меню");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(new Color(230, 240, 255));

        // Заголовок
        JLabel titleLabel = new JLabel("Химический редактор", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(0, 0, 100));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        // Подзаголовок
        JLabel subTitleLabel = new JLabel("Выберите режим работы:", SwingConstants.CENTER);
        subTitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subTitleLabel.setForeground(new Color(80, 80, 80));
        subTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Панель с кнопками
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 30, 30));
        buttonPanel.setBackground(new Color(230, 240, 255));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 150, 20, 150));

        // Кнопка 2D конструктора
        JButton builderButton = new JButton("2D Конструктор молекул");
        builderButton.setFont(new Font("Arial", Font.BOLD, 18));
        builderButton.setBackground(new Color(70, 130, 180));
        builderButton.setForeground(Color.WHITE);
        builderButton.setFocusPainted(false);

        builderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JFrame frame = new JFrame("2D Конструктор молекул");
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.setSize(1000, 700);
                        frame.setLocationRelativeTo(null);

                        MoleculeBuilder builder = new MoleculeBuilder();
                        frame.add(builder);
                        frame.setVisible(true);
                    }
                });
            }
        });

        // Кнопка просмотра формул (ваша старая программа)
        JButton viewerButton = new JButton("Просмотр готовых формул");
        viewerButton.setFont(new Font("Arial", Font.PLAIN, 16));
        viewerButton.setBackground(new Color(90, 150, 90));
        viewerButton.setForeground(Color.WHITE);
        viewerButton.setFocusPainted(false);

        viewerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JFrame programFrame = new JFrame("Просмотр формул");
                        programFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        programFrame.setSize(900, 700);
                        programFrame.setLocationRelativeTo(null);

                        MyPanel panel = new MyPanel();
                        programFrame.add(panel);
                        programFrame.setVisible(true);
                    }
                });
            }
        });

        // Кнопка выхода
        JButton exitButton = new JButton("Выход");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 14));
        exitButton.setBackground(new Color(180, 90, 90));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.add(builderButton);
        buttonPanel.add(viewerButton);
        buttonPanel.add(exitButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(subTitleLabel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }
}