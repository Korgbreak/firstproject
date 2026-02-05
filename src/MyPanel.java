import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MyPanel extends JPanel {
    private Molecule molecule;
    private JTextField inputField;
    private JLabel infoLabel;
    private JButton drawButton;

    public MyPanel() {
        setLayout(new BorderLayout());

        // Панель управления сверху
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(240, 240, 240));
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Текстовое поле для ввода
        JLabel inputLabel = new JLabel("Введите название соединения:");
        inputField = new JTextField(20);
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));

        // Кнопка для рисования
        drawButton = new JButton("Нарисовать");
        drawButton.setFont(new Font("Arial", Font.BOLD, 14));

        // Информационная метка
        infoLabel = new JLabel("Введите название и нажмите 'Нарисовать'");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Примеры подсказок
        JLabel examplesLabel = new JLabel("Примеры: метан, этан, пропан, 2-метилпропан, 2-хлорпропан");
        examplesLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        examplesLabel.setForeground(Color.GRAY);

        // Добавляем компоненты
        controlPanel.add(inputLabel);
        controlPanel.add(inputField);
        controlPanel.add(drawButton);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(infoLabel);

        // Панель для примеров
        JPanel examplesPanel = new JPanel();
        examplesPanel.setBackground(new Color(250, 250, 250));
        examplesPanel.add(examplesLabel);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(examplesPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Обработчик кнопки
        drawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawMolecule();
            }
        });

        // Обработчик нажатия Enter в текстовом поле
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawMolecule();
            }
        });

        // Начальная молекула
        molecule = Molecule.parseMolecule("бутан", getWidth(), getHeight());

        // Фокус на поле ввода
        SwingUtilities.invokeLater(() -> inputField.requestFocus());
    }

    private void drawMolecule() {
        String formula = inputField.getText().trim();
        if (!formula.isEmpty()) {
            molecule = Molecule.parseMolecule(formula, getWidth(), getHeight());
            updateInfoLabel(formula);
            repaint();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Пожалуйста, введите название соединения",
                    "Внимание", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateInfoLabel(String formula) {
        if (molecule != null) {
            String info = formula + " - цепь из " + molecule.getCarbonCount() + " атомов C";
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
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Легенда:", legendX, legendY);

        g.setFont(new Font("Arial", Font.PLAIN, 12));

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

        // Примеры использования
        g.setColor(Color.DARK_GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.drawString("Примеры ввода:", legendX, legendY + 90);
        g.drawString("• метан", legendX + 10, legendY + 110);
        g.drawString("• этан", legendX + 10, legendY + 125);
        g.drawString("• пропан", legendX + 10, legendY + 140);
        g.drawString("• 2-метилпропан", legendX + 10, legendY + 155);
        g.drawString("• 2-хлорпропан", legendX + 10, legendY + 170);
        g.drawString("• 2,3-диметилбутан", legendX + 10, legendY + 185);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(900, 700);
    }
}