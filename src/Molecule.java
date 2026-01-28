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

    // Создание основной цепи
    public void createMainChain(int carbons) {
        mainChain.clear();

        int startX = 100;
        int y = 200;
        int step = 80;

        for (int i = 0; i < carbons; i++) {
            if (i % 2 == 0) {
                mainChain.add(new Point(startX + i * step, y));
            } else {
                mainChain.add(new Point(startX + i * step, y + 40));
            }
        }
    }

    // Добавление заместителя
    public void addSubstituent(int carbonIndex, String label) {
        if (carbonIndex < 0 || carbonIndex >= mainChain.size()) return;

        Point carbonPos = mainChain.get(carbonIndex);
        ArrayList<Point> subPoints = new ArrayList<>();

        // Направление заместителя
        int dx = 0, dy = 0;

        if (carbonIndex == 0) { // Первый атом
            dx = -40;
            dy = -40;
        } else if (carbonIndex == mainChain.size() - 1) { // Последний атом
            dx = 40;
            dy = -40;
        } else { // Средний атом
            dx = 40;
            dy = -40;
        }

        Point start = new Point(carbonPos.x, carbonPos.y);
        Point end = new Point(carbonPos.x + dx, carbonPos.y + dy);

        subPoints.add(start);
        subPoints.add(end);

        substituents.put(carbonIndex, subPoints);
        substituentLabels.put(carbonIndex, label);
    }

    // Добавление метильной группы
    public void addMethyl(int carbonIndex) {
        addSubstituent(carbonIndex, "CH3");
    }

    // Парсинг названия молекулы
    public static Molecule parseMolecule(String formula) {
        formula = formula.toLowerCase().trim();
        Molecule molecule = null;

        if (formula.contains("метан")) {
            molecule = new Molecule("Метан");
            molecule.createMainChain(1);

            if (formula.contains("хлор")) {
                molecule.addSubstituent(0, "Cl");
            }
            if (formula.contains("бром")) {
                molecule.addSubstituent(0, "Br");
            }

        } else if (formula.contains("этан")) {
            molecule = new Molecule("Этан");
            molecule.createMainChain(2);

            if (formula.contains("хлор") || formula.contains("1-хлор")) {
                molecule.addSubstituent(0, "Cl");
            }
            if (formula.contains("2-хлор")) {
                molecule.addSubstituent(1, "Cl");
            }

        } else if (formula.contains("пропан")) {
            molecule = new Molecule("Пропан");
            molecule.createMainChain(3);

            if (formula.contains("2-метил")) {
                molecule.addMethyl(1);
            }
            if (formula.contains("2-хлор")) {
                molecule.addSubstituent(1, "Cl");
            }

        } else if (formula.contains("бутан")) {
            molecule = new Molecule("Бутан");
            molecule.createMainChain(4);

            if (formula.contains("2-метил")) {
                molecule.addMethyl(1);
            }
            if (formula.contains("2,3-диметил")) {
                molecule.addMethyl(1);
                molecule.addMethyl(2);
            }
        } else {
            // По умолчанию рисуем бутан
            molecule = new Molecule("Бутан");
            molecule.createMainChain(4);
        }

        return molecule;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Рисуем основную цепь
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));

        for (int i = 0; i < mainChain.size() - 1; i++) {
            Point p1 = mainChain.get(i);
            Point p2 = mainChain.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Рисуем вершины
        g2d.setColor(Color.RED);
        for (Point p : mainChain) {
            g2d.fillOval(p.x - 3, p.y - 3, 6, 6);
        }

        // Рисуем заместители
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(2));

        for (Integer carbonIndex : substituents.keySet()) {
            ArrayList<Point> subPoints = substituents.get(carbonIndex);

            if (subPoints.size() >= 2) {
                Point p1 = subPoints.get(0);
                Point p2 = subPoints.get(1);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

                // Подпись заместителя
                String label = substituentLabels.get(carbonIndex);
                if (label != null) {
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(label, p2.x + 5, p2.y - 5);
                    g2d.setColor(Color.BLUE);
                }
            }
        }

        // Название молекулы
        g2d.setColor(Color.BLACK);
        g2d.drawString(name, 50, 50);
    }
}