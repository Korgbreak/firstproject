import javax.swing.*;
import java.awt.*;
import java.util.Scanner;

public class MyPanel extends JPanel {
    private Molecule molecule;

    public MyPanel() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите название соединения:");

        String formula = scanner.nextLine();
        scanner.close();

        molecule = Molecule.parseMolecule(formula);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (molecule != null) {
            molecule.draw(g);
        }

    }
}