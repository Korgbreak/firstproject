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

        if (carbons <= 0) carbons = 1;

        // Рассчитываем оптимальный размер
        int availableWidth = panelWidth - 100; // Отступы по бокам
        int maxStep = 80;
        int minStep = 30;

        int stepX = Math.max(minStep, Math.min(maxStep, availableWidth / Math.max(1, carbons - 1)));
        int stepY = stepX / 2;

        // Центрируем
        int totalWidth = (carbons - 1) * stepX;
        int startX = (panelWidth - totalWidth) / 2;
        if (startX < 50) startX = 50;

        int startY = panelHeight / 2;
        if (startY < 100) startY = 100;

        // Создаем зигзагообразную цепь
        for (int i = 0; i < carbons; i++) {
            int x = startX + i * stepX;
            int y;

            if (carbons == 1) {
                y = startY; // Для метана просто центр
            } else if (carbons == 2) {
                y = startY; // Для этана горизонтально
            } else {
                // Зигзаг для 3+ атомов
                y = (i % 2 == 0) ? startY : startY + stepY;
            }

            mainChain.add(new Point(x, y));
        }
    }

    // Добавление заместителя
    public void addSubstituent(int carbonIndex, String label) {
        if (carbonIndex < 0 || carbonIndex >= mainChain.size()) return;

        Point carbonPos = mainChain.get(carbonIndex);
        ArrayList<Point> subPoints = new ArrayList<>();

        // Определяем длину и направление
        int length = 40;
        if (mainChain.size() > 4) length = 30;

        int dx = 0, dy = 0;

        // Определяем направление в зависимости от положения атома
        if (mainChain.size() == 1) {
            // Метан - во все стороны (рисуем только один заместитель)
            dx = 0;
            dy = -length;
        } else if (carbonIndex == 0) {
            // Первый атом - влево-вверх
            dx = -length;
            dy = -length;
        } else if (carbonIndex == mainChain.size() - 1) {
            // Последний атом - вправо-вверх
            dx = length;
            dy = -length;
        } else {
            // Средние атомы - перпендикулярно цепи
            Point prev = mainChain.get(carbonIndex - 1);
            Point next = mainChain.get(carbonIndex + 1);

            // Вектор связи
            int bondDx = next.x - prev.x;
            int bondDy = next.y - prev.y;

            // Перпендикулярный вектор (поворот на 90°)
            dx = -bondDy;
            dy = bondDx;

            // Нормализуем
            double bondLength = Math.sqrt(dx*dx + dy*dy);
            if (bondLength > 0) {
                dx = (int)(dx / bondLength * length);
                dy = (int)(dy / bondLength * length);
            }

            // Направляем вверх
            if ((dy >0) && (carbonIndex % 2 == 0)) {
                dx = -dx;
                dy = -dy;
            } else if (dy >0){
                dx=-dx;
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

    public void addEthyl(int carbonIndex) {
        addSubstituent(carbonIndex, "C2H5");
    }

    public void addHydroxyl(int carbonIndex) {
        addSubstituent(carbonIndex, "OH");
    }

    // Основной метод парсинга
    public static Molecule parseMolecule(String formula, int panelWidth, int panelHeight) {
        if (formula == null || formula.trim().isEmpty()) {
            formula = "бутан";
        }

        String originalFormula = formula;
        formula = formula.toLowerCase().trim();
        Molecule molecule = null;
        int carbons = 0;

        // Определяем основу
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
        } else if (formula.contains("гептан")) {
            molecule = new Molecule("Гептан");
            carbons = 7;
        } else if (formula.contains("октан")) {
            molecule = new Molecule("Октан");
            carbons = 8;
        } else {
            // Если не распознали - используем введенное название
            molecule = new Molecule(originalFormula);
            carbons = 4; // По умолчанию
        }

        // Создаем цепь
        molecule.createMainChain(carbons, panelWidth, panelHeight);

        // Парсим заместители
        parseSubstituents(formula, molecule);

        return molecule;
    }

    private static void parseSubstituents(String formula, Molecule molecule) {
        // Удаляем пробелы для удобства парсинга
        formula = formula.replace(" ", "").replace("-", "");

        // Ищем цифры (позиции заместителей)
        for (int i = 0; i < formula.length(); i++) {
            if (Character.isDigit(formula.charAt(i))) {
                int positionStart = i;
                while (i < formula.length() && Character.isDigit(formula.charAt(i))) {
                    i++;
                }

                if (i < formula.length()) {
                    String positionStr = formula.substring(positionStart, i);
                    int position = Integer.parseInt(positionStr) - 1; // Внутренний индекс

                    // Определяем тип заместителя
                    if (i + 3 <= formula.length()) {
                        String nextChars = formula.substring(i, Math.min(i + 6, formula.length()));

                        if (nextChars.startsWith("метил")) {
                            molecule.addMethyl(position);
                            i += 4; // Пропускаем "метил"
                        } else if (nextChars.startsWith("этил")) {
                            molecule.addEthyl(position);
                            i += 3; // Пропускаем "этил"
                        } else if (nextChars.startsWith("хлор")) {
                            molecule.addSubstituent(position, "Cl");
                            i += 3; // Пропускаем "хлор"
                        } else if (nextChars.startsWith("бром")) {
                            molecule.addSubstituent(position, "Br");
                            i += 3; // Пропускаем "бром"
                        } else if (nextChars.startsWith("гидрокси") || nextChars.startsWith("окси")) {
                            molecule.addHydroxyl(position);
                            i += nextChars.startsWith("гидрокси") ? 7 : 2;
                        } else if (nextChars.startsWith("фтор")) {
                            molecule.addSubstituent(position, "F");
                            i += 3;
                        } else if (nextChars.startsWith("йод")) {
                            molecule.addSubstituent(position, "I");
                            i += 2;
                        }
                    }
                }
            }
        }

        // Обработка ди-, три-замещенных
        if (formula.contains("диметил")) {
            // Ищем позиции
            String[] parts = formula.split("диметил")[0].split(",");
            for (String part : parts) {
                part = part.replaceAll("[^0-9]", "");
                if (!part.isEmpty()) {
                    int pos = Integer.parseInt(part) - 1;
                    molecule.addMethyl(pos);
                }
            }
        }
    }

    // Метод отрисовки
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Включаем сглаживание
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Толщина линий в зависимости от размера молекулы
        int lineWidth = Math.max(2, 5 - mainChain.size() / 3);
        BasicStroke mainStroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke subStroke = new BasicStroke(lineWidth - 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        // Рисуем основную цепь
        g2d.setStroke(mainStroke);
        g2d.setColor(Color.BLACK);

        for (int i = 0; i < mainChain.size() - 1; i++) {
            Point p1 = mainChain.get(i);
            Point p2 = mainChain.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Рисуем атомы углерода
        int pointSize = Math.max(4, 9 - mainChain.size());
        g2d.setColor(new Color(200, 0, 0)); // Темно-красный
        for (Point p : mainChain) {
            g2d.fillOval(p.x - pointSize, p.y - pointSize,
                    pointSize * 2, pointSize * 2);
        }

        // Рисуем заместители
        g2d.setStroke(subStroke);
        g2d.setColor(new Color(0, 0, 180)); // Темно-синий

        Font originalFont = g2d.getFont();
        Font subFont = new Font("Arial", Font.PLAIN, Math.max(10, 14 - mainChain.size() / 2));

        for (Integer index : substituents.keySet()) {
            ArrayList<Point> points = substituents.get(index);
            if (points.size() >= 2) {
                Point p1 = points.get(0);
                Point p2 = points.get(1);

                // Линия заместителя
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

                // Подпись заместителя
                String label = substituentLabels.get(index);
                if (label != null) {
                    g2d.setFont(subFont);
                    g2d.setColor(Color.DARK_GRAY);

                    // Позиционируем текст рядом с концом линии
                    int labelX = p2.x;
                    int labelY = p2.y;

                    // Смещаем в зависимости от направления
                    if (p2.y < p1.y) { // Вверх
                        labelY = p2.y - 8;
                    } else if (p2.y > p1.y) { // Вниз
                        labelY = p2.y + 18;
                    } else { // Горизонтально
                        if (p2.x > p1.x) { // Вправо
                            labelX = p2.x + 10;
                            labelY = p2.y - 5;
                        } else { // Влево
                            labelX = p2.x - 10;
                            labelY = p2.y - 5;
                        }
                    }

                    // Центрируем текст
                    FontMetrics fm = g2d.getFontMetrics();
                    int textWidth = fm.stringWidth(label);
                    labelX -= textWidth / 2;

                    g2d.drawString(label, labelX, labelY);
                    g2d.setColor(new Color(0, 0, 180));
                    g2d.setFont(originalFont);
                }
            }
        }

        // Подпись молекулы
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.drawString(name, 30, 40);

        // Информация
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(80, 80, 80));
        g2d.drawString("Атомов углерода: " + mainChain.size(), 30, 65);
        g2d.drawString("Заместителей: " + substituents.size(), 30, 85);
    }

    // Геттер для количества атомов углерода
    public int getCarbonCount() {
        return mainChain.size();
    }
}