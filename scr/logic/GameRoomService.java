package logic;

import model.*;
import persistence.ToyRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GameRoomService {
    private GameRoom activeRoom;
    private List<Toy> catalog;
    private ToyRepository repository;

    public GameRoomService(ToyRepository repository) {
        this.repository = repository;
        this.catalog = repository.loadCatalog(); // завантажуємо каталог при старті
    }

    // управління кімнатою
    public void createRoom(String name, double budget) {
        this.activeRoom = new GameRoom(name, budget);
    }

    public GameRoom getActiveRoom() {
        return activeRoom;
    }

    // логіка відбору іграшок (+ показ каталогу)
    public List<Toy> getToysForChild(int age) {
        if (activeRoom == null) return List.of();
        double moneyLeft = activeRoom.getRemainingBudget();

        return catalog.stream()
                .filter(t -> age >= t.getMinAge() && age <= t.getMaxAge())
                .filter(t -> t.getPrice() <= moneyLeft)
                .collect(Collectors.toList());
    }

    public void addToyToRoom(Toy toy) {
        activeRoom.addToy(toy);
    }

    // сортування іграшок у кімнаті
    // за ціною (зростання)
    public void sortRoomByPrice() {
        if (activeRoom != null) {
            activeRoom.getToys().sort(Comparator.comparingDouble(Toy::getPrice));
        }
    }

    // за розміром (Small -> Large)
    public void sortRoomBySize() {
        if (activeRoom != null) {
            activeRoom.getToys().sort(Comparator.comparing(Toy::getSize));
        }
    }

    // збереження
    public void saveCurrentRoom(String filename) {
        if (activeRoom != null) {
            repository.saveRoom(activeRoom, filename);
        }
    }

    public boolean loadRoomFromFile(String filename) {
        GameRoom loadedRoom = repository.loadRoom(filename);
        if (loadedRoom != null) {
            this.activeRoom = loadedRoom;
            return true;
        }
        return false;
    }

    public Toy removeToyFromRoom(int index) {
        if (activeRoom != null) {
            Toy toyToRemove = activeRoom.getToys().get(index);
            activeRoom.getToys().remove(index);
            // повертаємо гроші в бюджет
            activeRoom.decreaseSpent(toyToRemove.getPrice());
            return toyToRemove;
        }
        return null;
    }

    public void showTransportsBySpeed() {
        activeRoom.getToys().stream()
                .filter(t -> t instanceof model.Transport)
                .map(t -> (model.Transport) t)
                .sorted((t1, t2) -> Integer.compare(t2.getMaxSpeed(), t1.getMaxSpeed()))
                .forEach(t -> System.out.println(t.getName() + " - " + t.getMaxSpeed() + " км/год"));
    }

    public void showDollsByHairColor() {
        if (activeRoom == null) return;

        System.out.println("--- ЛЯЛЬКИ (Сортування за кольором волосся) ---");

        activeRoom.getToys().stream()
                .filter(t -> t instanceof Doll) // тільки ляльки
                .map(t -> (Doll) t)
                .sorted((d1, d2) -> d1.getHairColor().compareTo(d2.getHairColor()))
                .forEach(d -> System.out.println(d.getName() + " - Волосся: " + d.getHairColor()));
    }

    public void updateToyPrice(Toy toy, double newPrice) throws Exception {
        double oldPrice = toy.getPrice();
        double difference = newPrice - oldPrice;

        if (difference > 0 && activeRoom.getRemainingBudget() < difference) {
            throw new Exception("Не вистачає бюджету для збільшення ціни!");
        }

        activeRoom.increaseSpent(difference);
        toy.setPrice(newPrice);
    }

    public List<Toy> findToysByRange(double minPrice, double maxPrice, int age) {
        if (activeRoom == null) return List.of();

        return activeRoom.getToys().stream()
                .filter(t -> t.getPrice() >= minPrice && t.getPrice() <= maxPrice)
                .filter(t -> age >= t.getMinAge() && age <= t.getMaxAge())
                .collect(Collectors.toList());
    }

    public void showWholeCatalog() {
        if (catalog == null || catalog.isEmpty()) {
            System.out.println("Каталог порожній або файл не завантажено.");
            return;
        }

        System.out.println("\n========================================");
        System.out.println(" ВЕСЬ КАТАЛОГ МАГАЗИНУ (" + catalog.size() + " позицій)");
        System.out.println("========================================");

        var groupedCatalog = catalog.stream()
                .collect(Collectors.groupingBy(toy -> {
                    if (toy instanceof model.Transport) return "ТРАНСПОРТ";
                    if (toy instanceof model.Lego) return "LEGO";
                    if (toy instanceof model.Doll) return "ЛЯЛЬКИ";
                    return "ІНШЕ";
                }));

        // Виводимо групи
        for (var entry : groupedCatalog.entrySet()) {
            System.out.println("\n" + entry.getKey() + ":");
            System.out.println("----------------------------------------");
            List<Toy> toys = entry.getValue();

            for (Toy t : toys) {
                System.out.printf("  • %-25s | %8.2f грн | %d-%d років | %s\n",
                        t.getName(), t.getPrice(), t.getMinAge(), t.getMaxAge(), t.getSize());
            }
        }
        System.out.println("\n========================================");
    }
}