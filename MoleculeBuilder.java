import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class MoleculeBuilder extends JPanel {

    // Константы
    private static final double BOND_LENGTH = 80;
    private static final double ROTATION_STEP = Math.toRadians(30);
    private static final int HOVER_RADIUS = 20;
    private static final int BOND_HOVER_RADIUS = 15;

    // Класс для вершины (углерод) - невидимый
    class Vertex {
        Point2D.Double position;
        int id;
        List<Bond> bonds;

        Vertex(double x, double y, int id) {
            this.position = new Point2D.Double(x, y);
            this.id = id;
            this.bonds = new ArrayList<>();
        }
    }

    // Класс для функциональной группы (видимый атом)
    class FunctionalGroup {
        Point2D.Double position;
        String symbol;
        Vertex attachedTo;
        int id;

        FunctionalGroup(double x, double y, String symbol, Vertex attachedTo, int id) {
            this.position = new Point2D.Double(x, y);
            this.symbol = symbol;
            this.attachedTo = attachedTo;
            this.id = id;
        }

        void draw(Graphics2D g2d) {
            int size = 18;
            if (symbol.equals("OH") || symbol.equals("NH2")) {
                size = 26;
            }

            g2d.setColor(getGroupColor(symbol));
            g2d.fillOval((int)position.x - size/2, (int)position.y - size/2, size, size);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial Rounded MT", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (int)position.x - fm.stringWidth(symbol)/2;
            int textY = (int)position.y + fm.getAscent()/2 - 2;
            g2d.drawString(symbol, textX, textY);
        }

        Color getGroupColor(String symbol) {
            switch(symbol) {
                case "OH": return new Color(200, 50, 50);
                case "NH2": return new Color(68, 93, 219);
                case "O": return new Color(200, 50, 50);
                case "N": return new Color(68, 93, 219);
                case "Cl": return new Color(50, 180, 50);
                case "Br": return new Color(160, 80, 0);
                case "F": return new Color(0, 180, 180);
                default: return new Color(180, 100, 180);
            }
        }
    }

    // Класс для связи
    class Bond {
        Vertex vertex1;
        Vertex vertex2;
        FunctionalGroup group;
        int order;

        Bond(Vertex v1, Vertex v2, int order) {
            this.vertex1 = v1;
            this.vertex2 = v2;
            this.order = order;
            this.group = null;
            v1.bonds.add(this);
            v2.bonds.add(this);
        }

        Bond(Vertex v, FunctionalGroup g, int order) {
            this.vertex1 = v;
            this.group = g;
            this.order = order;
            v.bonds.add(this);
        }

        Point2D.Double getCenterPoint() {
            Point2D.Double p1 = vertex1.position;
            Point2D.Double p2 = (group != null) ? group.position : vertex2.position;
            double centerX = (p1.x + p2.x) / 2;
            double centerY = (p1.y + p2.y) / 2;
            return new Point2D.Double(centerX, centerY);
        }

        double getLength() {
            Point2D.Double p1 = vertex1.position;
            Point2D.Double p2 = (group != null) ? group.position : vertex2.position;
            return Math.hypot(p2.x - p1.x, p2.y - p1.y);
        }

        boolean isNear(Point p) {
            Point2D.Double p1 = vertex1.position;
            Point2D.Double p2 = (group != null) ? group.position : vertex2.position;
            double dist = distanceToSegment(p.x, p.y, p1.x, p1.y, p2.x, p2.y);
            return dist < BOND_HOVER_RADIUS;
        }

        private double distanceToSegment(double px, double py, double x1, double y1, double x2, double y2) {
            double dx = x2 - x1;
            double dy = y2 - y1;

            if (dx == 0 && dy == 0) {
                return Math.hypot(px - x1, py - y1);
            }

            double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
            t = Math.max(0, Math.min(1, t));

            double projX = x1 + t * dx;
            double projY = y1 + t * dy;

            return Math.hypot(px - projX, py - projY);
        }

        void draw(Graphics2D g2d) {
            Point2D.Double p1 = vertex1.position;
            Point2D.Double p2 = (group != null) ? group.position : vertex2.position;

            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));

            if (order == 1) {
                g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
            } else if (order == 2) {
                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double angle = Math.atan2(dy, dx);
                double perpX = Math.sin(angle) * 5;
                double perpY = -Math.cos(angle) * 5;

                g2d.drawLine((int)(p1.x + perpX), (int)(p1.y + perpY - 2),
                        (int)(p2.x + perpX), (int)(p2.y + perpY - 2));
                g2d.drawLine((int)(p1.x), (int)(p1.y),
                        (int)(p2.x), (int)(p2.y));
            }
        }
    }

    // Поля класса
    private List<Vertex> vertices;
    private List<Bond> bonds;
    private List<FunctionalGroup> groups;
    private int nextId;

    private Vertex selectedVertex;
    private Vertex hoveredVertex;
    private FunctionalGroup hoveredGroup;
    private Bond hoveredBond;
    private Point2D.Double previewPoint;
    private double previewAngle = 0;

    private String currentGroup = null;
    private int currentBondOrder = 1;
    private boolean rotateMode = false;

    private Point lastMousePos;
    private Vertex rotationCenter;
    private Map<Vertex, Double> originalAngles;
    private Map<Vertex, Double> originalDistances;

    public MoleculeBuilder() {
        vertices = new ArrayList<>();
        bonds = new ArrayList<>();
        groups = new ArrayList<>();
        nextId = 0;

        setBackground(Color.WHITE);
        setFocusable(true);
        requestFocusInWindow();

        Vertex start = new Vertex(400, 300, nextId++);
        vertices.add(start);

        setupMouseListeners();
        setupControlPanel();
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
                requestFocusInWindow();

                findHovered(e.getPoint());

                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (rotateMode) {
                        if (hoveredVertex != null) {
                            startRotation(hoveredVertex);
                        }
                    } else {
                        if (hoveredVertex != null) {
                            selectedVertex = hoveredVertex;
                            updatePreview(e.getPoint());
                        } else if (hoveredGroup != null) {
                            // Можно выбрать группу для редактирования
                        } else if (hoveredBond != null) {
                            cycleBondOrder(hoveredBond);
                        } else {
                            Vertex newVertex = addVertex(e.getX(), e.getY());
                            if (newVertex != null) {
                                vertices.add(newVertex);
                            }
                        }
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (hoveredGroup != null) {
                        removeGroup(hoveredGroup);
                    } else if (hoveredVertex != null && vertices.size() > 1) {
                        removeVertex(hoveredVertex);
                    } else if (hoveredBond != null) {
                        decreaseBondOrder(hoveredBond);
                    }
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedVertex != null && !rotateMode && previewPoint != null) {
                    createBondOrGroup(e.getPoint());
                }

                selectedVertex = null;
                previewPoint = null;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (rotateMode && rotationCenter != null) {
                    double dx = e.getX() - lastMousePos.x;
                    rotateMolecule(dx * 0.01);
                } else if (selectedVertex != null) {
                    updatePreview(e.getPoint());
                }

                findHovered(e.getPoint());
                lastMousePos = e.getPoint();
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                findHovered(e.getPoint());
                repaint();
            }
        });
    }

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(240, 240, 240));
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        // Кнопка возврата в меню
        JButton menuButton = new JButton("← Меню");
        menuButton.setFont(new Font("Arial", Font.BOLD, 12));
        menuButton.setBackground(new Color(255,255,255));
        menuButton.setFocusPainted(false);
        menuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(MoleculeBuilder.this);
                frame.dispose();
                new MainMenu();
            }
        });
        controlPanel.add(menuButton);

        // Кнопка для углерода
        JToggleButton carbonBtn = new JToggleButton("C (цепь)");
        carbonBtn.setSelected(true);
        carbonBtn.addActionListener(e -> currentGroup = null);
        controlPanel.add(carbonBtn);

        // Кнопки функциональных групп
        String[] groups = {"OH", "NH2", "O", "N", "Cl", "Br", "F"};
        ButtonGroup groupBtnGroup = new ButtonGroup();
        groupBtnGroup.add(carbonBtn);

        for (String group : groups) {
            JToggleButton btn = new JToggleButton(group);
            btn.addActionListener(e -> currentGroup = group);
            groupBtnGroup.add(btn);
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

        ButtonGroup bondGroup = new ButtonGroup();
        bondGroup.add(singleBtn);
        bondGroup.add(doubleBtn);

        controlPanel.add(new JLabel("  "));

        // Кнопка очистки
        JButton clearBtn = new JButton("Очистить");
        clearBtn.addActionListener(e -> {
            vertices.clear();
            bonds.clear();
            nextId = 0;
            vertices.add(new Vertex(400, 300, nextId++));
            repaint();
        });
        controlPanel.add(clearBtn);

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
    }

    private void findHovered(Point p) {
        hoveredVertex = null;
        hoveredGroup = null;
        hoveredBond = null;

        for (FunctionalGroup g : groups) {
            double dist = Math.hypot(p.x - g.position.x, p.y - g.position.y);
            if (dist < 20) {
                hoveredGroup = g;
                return;
            }
        }

        for (Vertex v : vertices) {
            double dist = Math.hypot(p.x - v.position.x, p.y - v.position.y);
            if (dist < 25) {
                hoveredVertex = v;
                return;
            }
        }

        for (Bond bond : bonds) {
            if (bond.isNear(p)) {
                hoveredBond = bond;
                return;
            }
        }
    }

    private Vertex addVertex(double x, double y) {
        for (Vertex v : vertices) {
            double dist = Math.hypot(x - v.position.x, y - v.position.y);
            if (dist < BOND_LENGTH * 0.7) {
                return null;
            }
        }
        return new Vertex(x, y, nextId++);
    }

    private void removeVertex(Vertex v) {
        Iterator<Bond> bondIter = bonds.iterator();
        while (bondIter.hasNext()) {
            Bond bond = bondIter.next();
            if (bond.vertex1 == v || bond.vertex2 == v) {
                bondIter.remove();
            }
        }

        Iterator<FunctionalGroup> groupIter = groups.iterator();
        while (groupIter.hasNext()) {
            FunctionalGroup g = groupIter.next();
            if (g.attachedTo == v) {
                groupIter.remove();
            }
        }

        vertices.remove(v);
    }

    private void removeGroup(FunctionalGroup g) {
        Iterator<Bond> bondIter = bonds.iterator();
        while (bondIter.hasNext()) {
            Bond bond = bondIter.next();
            if (bond.group == g) {
                bondIter.remove();
            }
        }
        groups.remove(g);
    }

    private void updatePreview(Point mousePos) {
        if (selectedVertex == null) return;

        double dx = mousePos.x - selectedVertex.position.x;
        double dy = mousePos.y - selectedVertex.position.y;
        previewAngle = Math.atan2(dy, dx);

        double steps = Math.round(previewAngle / ROTATION_STEP);
        previewAngle = steps * ROTATION_STEP;

        double previewX = selectedVertex.position.x + Math.cos(previewAngle) * BOND_LENGTH;
        double previewY = selectedVertex.position.y + Math.sin(previewAngle) * BOND_LENGTH;

        previewPoint = new Point2D.Double(previewX, previewY);
    }

    private void createBondOrGroup(Point mousePos) {
        if (selectedVertex == null || previewPoint == null) return;

        Vertex targetVertex = null;
        FunctionalGroup targetGroup = null;

        for (Vertex v : vertices) {
            double dist = Math.hypot(previewPoint.x - v.position.x,
                    previewPoint.y - v.position.y);
            if (dist < 20) {
                targetVertex = v;
                break;
            }
        }

        for (FunctionalGroup g : groups) {
            double dist = Math.hypot(previewPoint.x - g.position.x,
                    previewPoint.y - g.position.y);
            if (dist < 20) {
                targetGroup = g;
                break;
            }
        }

        if (targetVertex != null && targetVertex != selectedVertex) {
            bonds.add(new Bond(selectedVertex, targetVertex, currentBondOrder));
        } else if (targetGroup != null) {
            bonds.add(new Bond(selectedVertex, targetGroup, currentBondOrder));
        } else {
            if (currentGroup == null) {
                Vertex newVertex = addVertex(previewPoint.x, previewPoint.y);
                if (newVertex != null) {
                    vertices.add(newVertex);
                    bonds.add(new Bond(selectedVertex, newVertex, currentBondOrder));
                }
            } else {
                FunctionalGroup newGroup = new FunctionalGroup(
                        previewPoint.x, previewPoint.y,
                        currentGroup, selectedVertex, nextId++);
                groups.add(newGroup);
                bonds.add(new Bond(selectedVertex, newGroup, currentBondOrder));
            }
        }
    }

    private void cycleBondOrder(Bond bond) {
        bond.order = (bond.order == 1) ? 2 : 1;
        System.out.println("Изменен порядок связи на: " + bond.order);
        repaint();
    }

    private void decreaseBondOrder(Bond bond) {
        if (bond.order > 1) {
            bond.order--;
            System.out.println("Уменьшен порядок связи до: " + bond.order);
            repaint();
        }
    }

    private void startRotation(Vertex center) {
        rotationCenter = center;
        originalAngles = new HashMap<>();
        originalDistances = new HashMap<>();

        for (Vertex v : vertices) {
            if (v != center) {
                double dx = v.position.x - center.position.x;
                double dy = v.position.y - center.position.y;
                originalAngles.put(v, Math.atan2(dy, dx));
                originalDistances.put(v, Math.hypot(dx, dy));
            }
        }
    }

    private void rotateMolecule(double deltaAngle) {
        if (rotationCenter == null || originalAngles == null) return;

        for (Vertex v : vertices) {
            if (v == rotationCenter) continue;

            Double originalAngle = originalAngles.get(v);
            Double distance = originalDistances.get(v);
            if (originalAngle == null || distance == null) continue;

            double newAngle = originalAngle + deltaAngle;
            double steps = Math.round(newAngle / ROTATION_STEP);
            newAngle = steps * ROTATION_STEP;

            double newX = rotationCenter.position.x + Math.cos(newAngle) * distance;
            double newY = rotationCenter.position.y + Math.sin(newAngle) * distance;

            v.position.setLocation(newX, newY);

            for (FunctionalGroup g : groups) {
                if (g.attachedTo == v) {
                    double gDx = g.position.x - v.position.x;
                    double gDy = g.position.y - v.position.y;
                    double gAngle = Math.atan2(gDy, gDx);
                    double gDist = Math.hypot(gDx, gDy);

                    g.position.x = v.position.x + Math.cos(gAngle) * gDist;
                    g.position.y = v.position.y + Math.sin(gAngle) * gDist;
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Подсветка связи
        if (hoveredBond != null) {
            Point2D.Double center = hoveredBond.getCenterPoint();
            double length = hoveredBond.getLength();

            g2d.setColor(new Color(150, 150, 150, 80));
            int ellipseWidth = (int)(length * 0.8);
            int ellipseHeight = 30;

            Point2D.Double p1 = hoveredBond.vertex1.position;
            Point2D.Double p2 = (hoveredBond.group != null) ?
                    hoveredBond.group.position : hoveredBond.vertex2.position;

            double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);

            AffineTransform old = g2d.getTransform();
            g2d.translate(center.x, center.y);
            g2d.rotate(angle);
            g2d.fillOval(-ellipseWidth/2, -ellipseHeight/2, ellipseWidth, ellipseHeight);
            g2d.setTransform(old);
        }

        // Рисуем связи
        for (Bond bond : bonds) {
            bond.draw(g2d);
        }

        // Рисуем функциональные группы
        for (FunctionalGroup group : groups) {
            group.draw(g2d);
        }

        // Точки для углеродов
        g2d.setColor(new Color(100, 100, 100, 100));
        for (Vertex v : vertices) {
            g2d.fillOval((int) v.position.x - 2, (int) v.position.y - 2, 4, 4);
            if (v.bonds.size() == 0) {
                g2d.drawString("CH4", (int) v.position.x - 2, (int) v.position.y - 2);
            }
        }

        // Подсветка вершин и групп
        if (hoveredVertex != null) {
            g2d.setColor(new Color(255, 255, 0, 80));
            g2d.fillOval((int)hoveredVertex.position.x - 12,
                    (int)hoveredVertex.position.y - 12, 24, 24);
        }
        if (hoveredGroup != null) {
            g2d.setColor(new Color(255, 255, 0, 80));
            g2d.fillOval((int)hoveredGroup.position.x - 15,
                    (int)hoveredGroup.position.y - 15, 30, 30);
        }

        // Предварительный просмотр
        if (selectedVertex != null && previewPoint != null) {
            g2d.setColor(new Color(0, 100, 255, 150));
            g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 0, new float[]{8, 4}, 0));
            g2d.drawLine((int)selectedVertex.position.x, (int)selectedVertex.position.y,
                    (int)previewPoint.x, (int)previewPoint.y);

            if (currentGroup == null) {
                g2d.setColor(new Color(0, 255, 0, 100));
                g2d.fillOval((int)previewPoint.x - 4, (int)previewPoint.y - 4, 8, 8);
            } else {
                g2d.setColor(new Color(255, 0, 255, 100));
                g2d.fillRect((int)previewPoint.x - 10, (int)previewPoint.y - 10, 20, 20);
                g2d.setColor(Color.MAGENTA);
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                g2d.drawString(currentGroup, (int)previewPoint.x - 15,
                        (int)previewPoint.y - 15);
            }
        }


        }
}