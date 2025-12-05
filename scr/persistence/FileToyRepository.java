package persistence;

import model.GameRoom;
import model.Toy;
import util.ToyMapper;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileToyRepository implements ToyRepository {
    private static final Logger logger = LogManager.getLogger(FileToyRepository.class);
    private String catalogFilename;

    public FileToyRepository(String catalogFilename) {
        this.catalogFilename = catalogFilename;
    }

    @Override
    public List<Toy> loadCatalog() {
        List<Toy> toys = new ArrayList<>();
        File file = new File(catalogFilename);

        if (!file.exists()) {
            logger.error("КРИТИЧНА ПОМИЛКА: Файл каталогу не знайдено: {}", catalogFilename);
            System.err.println("Файл каталогу не знайдено: " + catalogFilename);
            return toys;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.isBlank()) continue;
                try {
                    Toy toy = ToyMapper.stringToToy(line);
                    toys.add(toy);
                } catch (Exception e) {
                    System.err.println("Помилка парсингу рядка: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return toys;
    }

    @Override
    public void saveRoom(GameRoom room, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Ім'я кімнати: " + room.getName());
            writer.println("Бюджет: " + room.getBudgetLimit());
            writer.println("Витрачено: " + room.getCurrentSpent());
            writer.println("--- ІГРАШКИ В КІМНАТІ ---");

            for (Toy toy : room.getToys()) {
                writer.println(ToyMapper.toyToString(toy));
            }
            System.out.println("Кімнату успішно збережено у файл: " + filename);
            logger.info("Кімнату успішно збережено у файл: {}", filename);
        } catch (IOException e) {
            System.err.println("Помилка запису у файл: " + e.getMessage());
            logger.error("Помилка запису у файл: {}", e.getMessage());
        }
    }

    @Override
    public GameRoom loadRoom(String filename) {
        File file = new File(filename);
        if (!file.exists()) return null;

        try (Scanner sc = new Scanner(file)) {

            String nameLine = sc.nextLine();
            String budgetLine = sc.nextLine();

            String name = nameLine.split(":")[1].trim();
            double budget = Double.parseDouble(budgetLine.split(":")[1].trim());

            GameRoom room = new GameRoom(name, budget);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.contains("---")) {
                    break;
                }
            }
            // читаю іграшки
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.isEmpty()) continue;
                try {
                    Toy toy = util.ToyMapper.stringToToy(line);
                    room.addToy(toy);
                } catch (Exception e) {
                    System.out.println("Помилка: " + line);
                }
            }
            logger.info("Кімнату завантажено з файлу: {}", filename);
            return room;

        } catch (Exception e) {
            logger.error("Помилка читання кімнати з файлу: " + filename, e);
            System.out.println("Помилка завантаження кімнати: " + e.getMessage());
            return null;
        }
    }
}