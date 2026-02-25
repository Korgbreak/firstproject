import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class MoleculeBuilder extends JPanel {

    // Константы
    private static final double BOND_LENGTH = 50.0; // Фиксированная длина связи
    private static final double ROTATION_STEP = Math.toRadians(15); // 15 градусов
    private static final int ATOM_RADIUS = 8;
    private static final int HOVER_RADIUS = 15;

    // Класс для атома
    class Atom {
        Point2D.Double position;
        String element;
        int id;
        List<Bond> bonds;

        Atom(double x, double y, String element, int id) {
            this.position = new Point2D.Double(x, y);
            this.element = element;
            this.id = id;
            this.bonds = new ArrayList<>();
        }

        void draw(Graphics2D g2d) {
            int size = element.equals("CH3") ? 16 : 20;

            // Цвет атома
            g2d.setColor(getElementColor(element));
            g2d.fillOval((int)position.x - size/2, (int)position.y - size/2, size, size);

            // Обводка
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawOval((int)position.x - size/2, (int)position.y - size/2, size, size);

            // Символ элемента
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String symbol = element;
            int textX = (int)position.x - fm.stringWidth(symbol)/2;
            int textY = (int)position.y + fm.getAscent()/2 - 2;
            g2d.drawString(symbol, textX, textY);
        }

        Color getElementColor(String element) {
            switch(element) {
                case "C": return new Color(80, 80, 80);
                case "H": return new Color(255, 255, 255);
                case "O": return new Color(220, 40, 40);
                case "N": return new Color(40, 40, 220);
                case "Cl": return new Color(40, 180, 40);
                case "Br": return new Color(160, 80, 0);
                case "F": return new Color(0, 200, 200);
                case "S": return new Color(200, 200, 0);
                default: return new Color(180, 100, 180);
            }
        }
    }

    // Класс для связи
    class Bond {
        Atom atom1;
        Atom atom2;
        int order; // 1 - одинарная, 2 - двойная, 3 - тройная

        Bond(Atom a1, Atom a2, int order) {
            this.atom1 = a1;
            this.atom2 = a2;
            this.order = order;
            a1.bonds.add(this);
            a2.bonds.add(this);
        }

        void draw(Graphics2D g2d) {
            Point2D.Double p1 = atom1.position;
            Point2D.Double p2 = atom2.position;

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));

            if (order == 1) {
                g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
            } else if (order == 2) {
                // Двойная связь
                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double angle = Math.atan2(dy, dx);
                double perpX = Math.sin(angle) * 5;
                double perpY = -Math.cos(angle) * 5;

                g2d.drawLine((int)(p1.x + perpX), (int)(p1.y + perpY),
                        (int)(p2.x + perpX), (int)(p2.y + perpY));
                g2d.drawLine((int)(p1.x - perpX), (int)(p1.y - perpY),
                        (int)(p2.x - perpX), (int)(p2.y - perpY));
            } else if (order == 3) {
                // Тройная связь
                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double angle = Math.atan2(dy, dx);
                double perpX = Math.sin(angle) * 6;
                double perpY = -Math.cos(angle) * 6;

                g2d.drawLine((int)(p1.x + perpX/2), (int)(p1.y + perpY/2),
                        (int)(p2.x + perpX/2), (int)(p2.y + perpY/2));
                g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
                g2d.drawLine((int)(p1.x - perpX/2), (int)(p1.y - perpY/2),
                        (int)(p2.x - perpX/2), (int)(p2.y - perpY/2));
            }
        }
    }

    // Поля класса
    private List<Atom> atoms;
    private List<Bond> bonds;
    private int nextAtomId;
    private Atom selectedAtom;
    private Atom hoveredAtom;
    private Point2D.Double previewPoint;
    private double previewAngle = 0;

    private String currentElement = "C";
    private int currentBondOrder = 1;
    private boolean showIds = false;
    private boolean rotateMode = false;

    private Point lastMousePos;
    private Atom rotationCenterAtom;
    private List<Double> originalAngles;
    private List<Double> originalDistances;

    public MoleculeBuilder() {
        atoms = new ArrayList<>();
        bonds = new ArrayList<>();
        nextAtomId = 0;

        setBackground(Color.WHITE);
        setFocusable(true);
        requestFocusInWindow();

        // Добавляем первый атом для примера
        addAtom(400, 300, "CH4");

        setupMouseListeners();
        setupControlPanel();
    }

    private void setupMouseListeners() {
        // Обработчик кликов мыши
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
                requestFocusInWindow();

                // Поиск атома под курсором
                hoveredAtom = findAtomAt(e.getPoint());

                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (rotateMode) {
                        // Режим вращения
                        if (hoveredAtom != null) {
                            startRotation(hoveredAtom);
                        }
                    } else {
                        // Обычный режим
                        if (hoveredAtom != null) {
                            selectedAtom = hoveredAtom;
                            updatePreview(e.getPoint());
                        } else {
                            // Создаем новый атом
                            addAtom(e.getX(), e.getY(), currentElement);
                        }
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // Правая кнопка - удалить атом
                    if (hoveredAtom != null) {
                        removeAtom(hoveredAtom);
                    }
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedAtom != null && !rotateMode) {
                    // Создаем связь
                    createBondFromPreview(e.getPoint());
                }

                if (rotateMode) {
                    endRotation();
                }

                selectedAtom = null;
                previewPoint = null;
                repaint();
            }
        });

        // Обработчик движения мыши
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (rotateMode && rotationCenterAtom != null) {
                    // Дискретное вращение на 15 градусов
                    double dx = e.getX() - lastMousePos.x;
                    double angle = dx * 0.01; // Чувствительность
                    rotateMoleculeDiscrete(angle);
                } else if (selectedAtom != null) {
                    // Обновляем предварительный просмотр связи
                    updatePreview(e.getPoint());
                }

                hoveredAtom = findAtomAt(e.getPoint());
                lastMousePos = e.getPoint();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                hoveredAtom = findAtomAt(e.getPoint());
                repaint();
            }
        });
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(240, 240, 240));
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        // Кнопки элементов
        String[] elements = {"C", "H", "O", "N", "F", "Cl", "Br"};
        ButtonGroup elementGroup = new ButtonGroup();
        for (String element : elements) {
            JToggleButton btn = new JToggleButton(element);
            btn.setSelected(element.equals("C"));
            btn.addActionListener(e -> currentElement = element);
            elementGroup.add(btn);
            controlPanel.add(btn);
        }

        controlPanel.add(new JLabel("  "));

        // Кнопки типов связей
        JToggleButton singleBtn = new JToggleButton("—");
        singleBtn.setSelected(true);
        singleBtn.addActionListener(e -> currentBondOrder = 1);
        controlPanel.add(singleBtn);

        JToggleButton doubleBtn = new JToggleButton("=");
        doubleBtn.addActionListener(e -> currentBondOrder = 2);
        controlPanel.add(doubleBtn);

        JToggleButton tripleBtn = new JToggleButton("≡");
        tripleBtn.addActionListener(e -> currentBondOrder = 3);
        controlPanel.add(tripleBtn);

        ButtonGroup bondGroup = new ButtonGroup();
        bondGroup.add(singleBtn);
        bondGroup.add(doubleBtn);
        bondGroup.add(tripleBtn);

        controlPanel.add(new JLabel("  "));

        // Кнопки управления
        JButton clearBtn = new JButton("Очистить");
        clearBtn.addActionListener(e -> {
            atoms.clear();
            bonds.clear();
            nextAtomId = 0;
            addAtom(400, 300, "CH3");
            repaint();
        });
        controlPanel.add(clearBtn);

        JToggleButton rotateToggle = new JToggleButton("Вращение (R)");
        rotateToggle.addActionListener(e -> rotateMode = rotateToggle.isSelected());
        controlPanel.add(rotateToggle);

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
    }

    private Atom findAtomAt(Point p) {
        for (Atom atom : atoms) {
            double dist = Math.hypot(p.x - atom.position.x, p.y - atom.position.y);
            if (dist < HOVER_RADIUS) {
                return atom;
            }
        }
        return null;
    }

    private Atom addAtom(double x, double y, String element) {
        // Проверяем, не слишком близко к существующим атомам
        for (Atom atom : atoms) {
            double dist = Math.hypot(x - atom.position.x, y - atom.position.y);
            if (dist < BOND_LENGTH * 0.7) {
                return null; // Слишком близко
            }
        }

        Atom atom = new Atom(x, y, element, nextAtomId++);
        atoms.add(atom);
        return atom;
    }

    private void removeAtom(Atom atom) {
        // Удаляем все связи с этим атомом
        Iterator<Bond> bondIter = bonds.iterator();
        while (bondIter.hasNext()) {
            Bond bond = bondIter.next();
            if (bond.atom1 == atom || bond.atom2 == atom) {
                bondIter.remove();
            }
        }
        atoms.remove(atom);
    }

    private void updatePreview(Point mousePos) {
        if (selectedAtom == null) return;

        // Вычисляем угол от выбранного атома к мыши
        double dx = mousePos.x - selectedAtom.position.x;
        double dy = mousePos.y - selectedAtom.position.y;
        previewAngle = Math.atan2(dy, dx);

        // Округляем до ближайших 15 градусов
        double steps = Math.round(previewAngle / ROTATION_STEP);
        previewAngle = steps * ROTATION_STEP;

        // Вычисляем точку на фиксированном расстоянии
        double previewX = selectedAtom.position.x + Math.cos(previewAngle) * BOND_LENGTH;
        double previewY = selectedAtom.position.y + Math.sin(previewAngle) * BOND_LENGTH;

        previewPoint = new Point2D.Double(previewX, previewY);
    }

    private void createBondFromPreview(Point mousePos) {
        if (selectedAtom == null || previewPoint == null) return;

        // Проверяем, есть ли атом в точке предпросмотра
        Atom targetAtom = findAtomAt(new Point((int)previewPoint.x, (int)previewPoint.y));

        if (targetAtom != null && targetAtom != selectedAtom) {
            // Проверяем, не существует ли уже связь
            boolean bondExists = false;
            for (Bond bond : bonds) {
                if ((bond.atom1 == selectedAtom && bond.atom2 == targetAtom) ||
                        (bond.atom1 == targetAtom && bond.atom2 == selectedAtom)) {
                    bondExists = true;
                    break;
                }
            }

            if (!bondExists) {
                bonds.add(new Bond(selectedAtom, targetAtom, currentBondOrder));
            }
        } else {
            // Создаем новый атом в точке предпросмотра
            Atom newAtom = addAtom(previewPoint.x, previewPoint.y, currentElement);
            if (newAtom != null) {
                bonds.add(new Bond(selectedAtom, newAtom, currentBondOrder));
            }
        }
    }

    private void startRotation(Atom center) {
        rotationCenterAtom = center;
        originalAngles = new ArrayList<>();
        originalDistances = new ArrayList<>();

        for (Atom atom : atoms) {
            if (atom != center) {
                double dx = atom.position.x - center.position.x;
                double dy = atom.position.y - center.position.y;
                originalAngles.add(Math.atan2(dy, dx));
                originalDistances.add(Math.hypot(dx, dy));
            } else {
                originalAngles.add(0.0);
                originalDistances.add(0.0);
            }
        }
    }

    private void rotateMoleculeDiscrete(double deltaAngle) {
        if (rotationCenterAtom == null || originalAngles == null) return;

        // Накопленный угол (округляем до шага 15°)
        double totalRotation = 0;

        for (int i = 0; i < atoms.size(); i++) {
            Atom atom = atoms.get(i);
            if (atom == rotationCenterAtom) continue;

            double originalAngle = originalAngles.get(i);
            double distance = originalDistances.get(i);

            // Новый угол с дискретным шагом
            double newAngle = originalAngle + deltaAngle;
            double steps = Math.round(newAngle / ROTATION_STEP);
            newAngle = steps * ROTATION_STEP;

            // Вычисляем новую позицию
            double newX = rotationCenterAtom.position.x + Math.cos(newAngle) * distance;
            double newY = rotationCenterAtom.position.y + Math.sin(newAngle) * distance;

            atom.position.setLocation(newX, newY);
            totalRotation = deltaAngle;
        }
    }

    private void endRotation() {
        rotationCenterAtom = null;
        originalAngles = null;
        originalDistances = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Включаем сглаживание
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Рисуем сетку
            g2d.setColor(new Color(240, 240, 240));
            g2d.setStroke(new BasicStroke(1));
            for (int x = 0; x < getWidth(); x += 20) {
                g2d.drawLine(x, 0, x, getHeight());
            }
            for (int y = 0; y < getHeight(); y += 20) {
                g2d.drawLine(0, y, getWidth(), y);
        }
        // Рисуем связи
        for (Bond bond : bonds) {
            bond.draw(g2d);
        }

        // Рисуем атомы
        for (Atom atom : atoms) {
            atom.draw(g2d);
        }

        // Подсветка выбранного атома
        if (hoveredAtom != null) {
            g2d.setColor(new Color(255, 255, 0, 80));
            int size = 20;
            g2d.fillOval((int)hoveredAtom.position.x - size/2,
                    (int)hoveredAtom.position.y - size/2, size, size);
        }

        // Предварительный просмотр связи
        if (selectedAtom != null && previewPoint != null) {
            // Пунктирная линия
            g2d.setColor(new Color(0, 100, 255, 150));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 0, new float[]{8, 4}, 0));
            g2d.drawLine((int)selectedAtom.position.x, (int)selectedAtom.position.y,
                    (int)previewPoint.x, (int)previewPoint.y);

            // Круг на месте предполагаемого атома
            g2d.setColor(new Color(0, 255, 0, 100));
            g2d.fillOval((int)previewPoint.x - 10, (int)previewPoint.y - 10, 20, 20);
        }
    }
}