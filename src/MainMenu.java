import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainMenu extends JFrame {

    private BufferedImage backgroundImage;

    public MainMenu() {
        setTitle("MolChemView");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        try {
            // Вариант 1: из ресурсов JAR
            InputStream is = getClass().getResourceAsStream("/background.jpg");
            if (is == null) {
                // Вариант 2: из папки src
                is = getClass().getResourceAsStream("background.jpg");
            }
            if (is == null) {
                // Вариант 3: из файловой системы
                File imgFile = new File("background.jpg");
                if (imgFile.exists()) {
                    backgroundImage = ImageIO.read(imgFile);
                }
            } else {
                backgroundImage = ImageIO.read(is);
            }

            if (backgroundImage != null) {
                System.out.println("Background loaded successfully");
            } else {
                System.out.println("Background image not found");
            }
        } catch (Exception e) {
            System.out.println("Failed to load background: " + e.getMessage());
            backgroundImage = null;
        }

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    g.setColor(new Color(0, 0, 0, 120));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("MolChemView", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial Rounded MT", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridLayout(3, 1, 30, 30));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 150, 20, 150));

        JButton builderButton = new JButton("Редактор молекул");
        builderButton.setFont(new Font("Bahnschrift", Font.BOLD, 20));
        builderButton.setBackground(new Color(70, 130, 180));
        builderButton.setForeground(Color.WHITE);
        builderButton.setFocusPainted(false);
        builderButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        builderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JFrame frame = new JFrame("Редактор молекул");
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

        JButton viewerButton = new JButton("Просмотр формул");
        viewerButton.setFont(new Font("Bahnschrift", Font.PLAIN, 20));
        viewerButton.setBackground(new Color(90, 150, 90));
        viewerButton.setForeground(Color.WHITE);
        viewerButton.setFocusPainted(false);
        viewerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

        JButton exitButton = new JButton("Выход");
        exitButton.setFont(new Font("Bahnschrift", Font.PLAIN, 20));
        exitButton.setBackground(new Color(180, 90, 90));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonPanel.add(builderButton);
        buttonPanel.add(viewerButton);
        buttonPanel.add(exitButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }
}