import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MyPanel extends JPanel {
    private Molecule molecule;
    private JComboBox<String> moleculeSelector;
    private JLabel infoLabel;

    public MyPanel() {
        setLayout(new BorderLayout());

        // Панель управления сверху
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(240, 240, 240));

        // Выпадающий список для выбора молекулы
        String[] molecules = {
                "Выберите молекулу",
                "Метан",
                "Этан",
                "Пропан",
                "Бутан",
                "Пентан",
                "Гексан",
                "2-Метилпропан",
                "2-Хлорпропан",
                "2,3-Диметилбутан",
                "1-Хлорбутан",
                "2-Бромпропан"
        };

        moleculeSelector = new JComboBox<>(molecules);
        moleculeSelector.setPreferredSize(new Dimension(200, 30));

        moleculeSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String) moleculeSelector.getSelectedItem();
                if (!selected.equals("Выберите молекулу")) {
                    molecule = Molecule.parseMolecule(selected, getWidth(), getHeight());
                    updateInfoLabel();
                    repaint();
                }
            }
        });

        // Кнопка для случайной молекулы
        JButton randomButton = new JButton("Случайная молекула");
        randomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] randomMolecules = {
                        "Метан", "Этан", "Пропан", "Бутан", "Пентан",
                        "2-Метилпропан", "2-Хлорпропан", "2,3-Диметилбутан"
                };
                int index = (int)(Math.random() * randomMolecules.length);
                moleculeSelector.setSelectedItem(randomMolecules[index]);
            }
        });

        // Информационная метка
        infoLabel = new JLabel("Выберите молекулу для отображения");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        controlPanel.add(new JLabel("Молекула: "));
        controlPanel.add(moleculeSelector);
        controlPanel.add(randomButton);
        controlPanel.add(infoLabel);

        add(controlPanel, BorderLayout.NORTH);

        // Начальная молекула для примера
        molecule = Molecule.parseMolecule("Бутан", getWidth(), getHeight());
    }

    private void updateInfoLabel() {
        if (molecule != null) {
            String info = "Цепь из " + molecule.getCarbonCount() + " атомов углерода";
            infoLabel.setText(info);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Белый фон
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Рисуем молекулу, если она есть
        if (molecule != null) {
            molecule.draw(g);
        }

        // Рисуем легенду
        drawLegend(g);
    }

    private void drawLegend(Graphics g) {
        int legendX = getWidth() - 200;
        int legendY = 100;

        g.setColor(Color.BLACK);
        g.drawString("Легенда:", legendX, legendY);

        g.setColor(Color.BLACK);
        g.drawLine(legendX, legendY + 20, legendX + 40, legendY + 20);
        g.drawString("- основная цепь", legendX + 50, legendY + 25);

        g.setColor(Color.BLUE);
        g.drawLine(legendX, legendY + 40, legendX + 40, legendY + 40);
        g.setColor(Color.BLACK);
        g.drawString("- заместители", legendX + 50, legendY + 45);

        g.setColor(Color.RED);
        g.fillOval(legendX + 15, legendY + 55, 10, 10);
        g.setColor(Color.BLACK);
        g.drawString("- атом углерода", legendX + 50, legendY + 65);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(900, 700);
    }
}