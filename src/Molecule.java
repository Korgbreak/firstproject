import java.awt.*;
import java.util.*;

public class Molecule {
    private String name;
    private ArrayList<Point> mainChain;
    private HashMap<Integer, ArrayList<Point>> substituents;
    private HashMap<Integer, String> substituentLabels;

    public Molecule(String name) {
        this.name = name;
        this.mainChain = new ArrayList<>();
        this.substituents = new HashMap<>();
        this.substituentLabels = new HashMap<>();
    }

    // Автоматическое создание цепи с масштабированием
    public void createMainChain(int carbons, int panelWidth, int panelHeight) {
        mainChain.clear();

        // Рассчитываем размеры
        int baseStep;
        if (carbons <= 2) {
            baseStep = 100; // Большой шаг для маленьких молекул
        } else if (carbons <= 4) {
            baseStep = 80; // Средний шаг
        } else {
            baseStep = Math.max(40, 400 / carbons); // Автоматическое уменьшение
        }

        int stepX = baseStep;
        int stepY = baseStep / 2;

        // Центрируем
        int totalWidth = (carbons - 1) * stepX;
        int startX = (panelWidth - totalWidth) / 2;
        if (startX < 50) startX = 50;

        int startY = panelHeight / 2;
        if (startY < 100) startY = 100;

        // Создаем цепь
        for (int i = 0; i < carbons; i++) {
            int x = startX + i * stepX;
            int y = (i % 2 == 0) ? startY : startY + stepY;
            mainChain.add(new Point(x, y));
        }

        // Корректируем для очень длинных цепей
        if (carbons > 6) {
            scaleChain(0.8, panelWidth, panelHeight);
        }
    }

    // Масштабирование цепи
    private void scaleChain(double factor, int panelWidth, int panelHeight) {
        if (mainChain.isEmpty()) return;

        // Центр цепи
        int centerX = panelWidth / 2;
        int centerY = panelHeight / 2;

        // Масштабируем
        for (Point p : mainChain) {
            int dx = p.x - centerX;
            int dy = p.y - centerY;
            p.x = centerX + (int)(dx * factor);
            p.y = centerY + (int)(dy * factor);
        }
    }

    // Добавление заместителя
    public void addSubstituent(int carbonIndex, String label) {
        if (carbonIndex < 0 || carbonIndex >= mainChain.size()) return;

        Point carbonPos = mainChain.get(carbonIndex);
        ArrayList<Point> subPoints = new ArrayList<>();

        // Длина заместителя зависит от размера молекулы
        int length = 40;
        if (mainChain.size() > 4) length = 30;

        int dx = 0, dy = -length; // По умолчанию вверх

        // Для крайних атомов - диагонально
        if (carbonIndex == 0) {
            dx = -length;
            dy = -length;
        } else if (carbonIndex == mainChain.size() - 1) {
            dx = length;
            dy = -length;
        } else {
            // Для средних атомов - перпендикулярно цепи
            dx = 0;
            dy = 40;

            // Чередуем направления
            if (carbonIndex % 2 == 0) {
                dy = -dy;
            }
        }

        Point start = carbonPos;
        Point end = new Point(carbonPos.x + dx, carbonPos.y + dy);

        subPoints.add(start);
        subPoints.add(end);

        substituents.put(carbonIndex, subPoints);
        substituentLabels.put(carbonIndex, label);
    }

    public void addMethyl(int carbonIndex) {
        addSubstituent(carbonIndex, "CH3");
    }

    // Статический метод для создания молекул
    public static Molecule parseMolecule(String formula, int panelWidth, int panelHeight) {
        formula = formula.toLowerCase().trim();
        Molecule molecule = null;
        int carbons = 0;

        // Определяем основное название и количество атомов
        if (formula.contains("метан")) {
            molecule = new Molecule("Метан");
            carbons = 1;
        } else if (formula.contains("этан")) {
            molecule = new Molecule("Этан");
            carbons = 2;
        } else if (formula.contains("пропан")) {
            molecule = new Molecule("Пропан");
            carbons = 3;
        } else if (formula.contains("бутан")) {
            molecule = new Molecule("Бутан");
            carbons = 4;
        } else if (formula.contains("пентан")) {
            molecule = new Molecule("Пентан");
            carbons = 5;
        } else if (formula.contains("гексан")) {
            molecule = new Molecule("Гексан");
            carbons = 6;
        } else {
            // По умолчанию
            molecule = new Molecule(formula);
            carbons = 4;
        }

        // Создаем цепь
        molecule.createMainChain(carbons, panelWidth, panelHeight);

        // Добавляем заместители
        if (formula.contains("метил")) {
            if (formula.contains("2-метил")) {
                molecule.addMethyl(1);
            } else if (formula.contains("3-метил")) {
                molecule.addMethyl(2);
            } else {
                molecule.addMethyl(1); // По умолчанию на позицию 2
            }
        }

        if (formula.contains("хлор")) {
            if (formula.contains("1-хлор")) {
                molecule.addSubstituent(0, "Cl");
            } else if (formula.contains("2-хлор")) {
                molecule.addSubstituent(1, "Cl");
            } else if (formula.contains("3-хлор")) {
                molecule.addSubstituent(2, "Cl");
            }
        }

        if (formula.contains("бром")) {
            if (formula.contains("2-бром")) {
                molecule.addSubstituent(1, "Br");
            }
        }

        if (formula.contains("диметил")) {
            if (formula.contains("2,3-диметил")) {
                molecule.addMethyl(1);
                molecule.addMethyl(2);
            }
        }

        return molecule;
    }

    // Метод отрисовки
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Включаем сглаживание
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Толщина линий
        int lineWidth = Math.max(2, 4 - mainChain.size() / 3);
        g2d.setStroke(new BasicStroke(lineWidth));

        // Рисуем основную цепь (черная)
        g2d.setColor(Color.BLACK);
        for (int i = 0; i < mainChain.size() - 1; i++) {
            Point p1 = mainChain.get(i);
            Point p2 = mainChain.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Рисуем атомы углерода (красные точки)
        int pointSize = Math.max(4, 8 - mainChain.size());
        g2d.setColor(Color.RED);
        for (Point p : mainChain) {
            g2d.fillOval(p.x - pointSize, p.y - pointSize,
                    pointSize * 2, pointSize * 2);
        }

        // Рисуем заместители (синие линии)
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(lineWidth - 1));

        for (Integer index : substituents.keySet()) {
            ArrayList<Point> points = substituents.get(index);
            if (points.size() >= 2) {
                Point p1 = points.get(0);
                Point p2 = points.get(1);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

                // Подписываем заместитель
                String label = substituentLabels.get(index);
                if (label != null) {
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 12));

                    int labelX = p2.x;
                    int labelY = p2.y;

                    // Позиционируем текст
                    if (p2.y < p1.y) {
                        labelY = p2.y - 5;
                    } else {
                        labelY = p2.y + 15;
                    }

                    // Центрируем
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(label);
                    labelX -= textWidth / 2;

                    g2d.drawString(label, labelX, labelY);
                    g2d.setColor(Color.BLUE);
                }
            }
        }

        // Подписываем название молекулы
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(name, 20, 30);

        // Информация о молекуле
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.drawString("Цепь из " + mainChain.size() + " атомов C", 20, 50);
    }

    // Геттер для количества атомов углерода
    public int getCarbonCount() {
        return mainChain.size();
    }
}