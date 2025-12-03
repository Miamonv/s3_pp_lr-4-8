package main;

import console.ConsoleView;
import controller_menu.*;
import logic.GameRoomService;
import persistence.FileToyRepository;
import persistence.ToyRepository;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ConsoleView view = new ConsoleView(scanner);
        MenuController menuController = new MenuController();

        ToyRepository repository = new FileToyRepository("toys_catalog.csv");
        GameRoomService service = new GameRoomService(repository);

        // Управління кімнатою
        menuController.register("1", new CreateRoomCommand(service, scanner));
        menuController.register("2", new LoadRoomCommand(service, scanner));
        menuController.register("3", new SaveRoomCommand(service, scanner));

        // Робота з іграшками
        menuController.register("4", new AddToyCommand(service, scanner));
        menuController.register("5", new EditToyCommand(service, scanner));
        menuController.register("6", new RemoveToyCommand(service, scanner));

        // Аналіз та перегляд
        menuController.register("7", new ShowRoomInfoCommand(service));

        // Сортування та пошук
        menuController.register("8", new SortToysCommand(service, scanner));
        menuController.register("9", new FindToysCommand(service, scanner));

        menuController.register("10", new ShowCatalogCommand(service));
        menuController.register("0", new ExitCommand());

        while (true) {
            view.showMenu();
            String userInput = view.getUserInput();

            menuController.executeCommand(userInput);

            if (!userInput.equals("0")) {
                view.pressEnterToContinue();
            }
        }
    }
}