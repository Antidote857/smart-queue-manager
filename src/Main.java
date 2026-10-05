import java.util.List;
import java.util.Scanner;

import model.Customer;
import service.QueueManager;

public class Main {

    public static void main(String[] args) {
        QueueManager manager = new QueueManager(3);

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                displayMenu();

                String option = readLine(scanner, "Select option: ");

                if (option == null) {
                    System.out.println("\nInput closed. Goodbye!");
                    break;
                }

                try {
                    switch (option.trim()) {
                        case "1" -> addCustomer(scanner, manager, false);
                        case "2" -> addCustomer(scanner, manager, true);
                        case "3" -> serveNextCustomer(scanner, manager);
                        case "4" -> completeService(scanner, manager);
                        case "5" -> viewQueue(manager);
                        case "6" -> searchCustomer(scanner, manager);
                        case "7" -> cancelCustomer(scanner, manager);
                        case "8" -> viewCounters(manager);
                        case "9" -> {
                            System.out.println();
                            System.out.println(manager.getStatistics());
                        }
                        case "0" -> {
                            running = false;
                            System.out.println(
                                    "Thank you for using Smart Queue Manager!"
                            );
                        }
                        default -> System.out.println(
                                "Invalid option. Choose a number from 0 to 9."
                        );
                    }
                                } catch (IllegalArgumentException
                         | IllegalStateException exception) {

                    System.out.println("Error: " + exception.getMessage());
                }

                if (running) {
                    String acknowledgement = readLine(
                            scanner,
                            "\nPress Enter to return to the menu..."
                    );

                    if (acknowledgement == null) {
                        break;
                    }
                }
            }
        }
    }

    private static void displayMenu() {
        System.out.println();
        System.out.println("====================================");
        System.out.println("       SMART QUEUE MANAGER");
        System.out.println("====================================");
        System.out.println("1. Add Customer");
        System.out.println("2. Add Priority Customer");
        System.out.println("3. Serve Next Customer");
        System.out.println("4. Complete Service");
        System.out.println("5. View Queue");
        System.out.println("6. Search Customer");
        System.out.println("7. Remove Customer");
        System.out.println("8. View Counters");
        System.out.println("9. View Statistics");
        System.out.println("0. Exit");
        System.out.println();
    }

    private static String readLine(Scanner scanner, String prompt) {
        System.out.print(prompt);

        if (!scanner.hasNextLine()) {
            return null;
        }

        return scanner.nextLine();
    }

    private static void addCustomer(
            Scanner scanner,
            QueueManager manager,
            boolean priority) {

        String name = readLine(scanner, "Enter customer name: ");

        if (name == null) {
            return;
        }

        Customer customer = manager.addCustomer(name, priority);

        System.out.println("\nCustomer registered successfully.");
        System.out.println(customer);
    }

    private static Integer readCounterId(Scanner scanner) {
        String input = readLine(scanner, "Enter counter number: ");

        if (input == null) {
            return null;
        }

        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Counter number must be a whole number."
            );
        }
    }

    private static void serveNextCustomer(
            Scanner scanner,
            QueueManager manager) {

        viewCounters(manager);

        Integer counterId = readCounterId(scanner);

        if (counterId == null) {
            return;
        }

        Customer customer = manager.serveNextCustomer(counterId);

        if (customer == null) {
            System.out.println("No customers waiting.");
            return;
        }

        System.out.println(
                "Counter " + counterId
                        + " is now serving "
                        + customer.getQueueNumber()
                        + " (" + customer.getName() + ")."
        );
    }

    private static void completeService(
            Scanner scanner,
            QueueManager manager) {

        viewCounters(manager);

        Integer counterId = readCounterId(scanner);

        if (counterId == null) {
            return;
        }

        Customer customer = manager.completeService(counterId);

        System.out.println(
                "Service completed for "
                        + customer.getQueueNumber()
                        + " (" + customer.getName() + ")."
        );
    }

    private static void viewQueue(QueueManager manager) {
        if (manager.getWaitingCount() == 0) {
            System.out.println("No customers waiting.");
            return;
        }

        System.out.println("\nNORMAL QUEUE");
        printQueue(manager.getNormalQueue());

        System.out.println("\nPRIORITY QUEUE");
        printQueue(manager.getPriorityQueue());

        System.out.println(
                "\nTotal waiting: " + manager.getWaitingCount()
        );
    }

    private static void printQueue(List<Customer> customers) {
        if (customers.isEmpty()) {
            System.out.println("(Empty)");
            return;
        }

        for (Customer customer : customers) {
            System.out.println(
                    customer.getQueueNumber()
                            + " - " + customer.getName()
                            + " - " + customer.getStatus()
            );
        }
    }

    private static void searchCustomer(
            Scanner scanner,
            QueueManager manager) {

        System.out.println("\nSEARCH CUSTOMER");
        System.out.println("1. Customer ID");
        System.out.println("2. Queue number");
        System.out.println("3. Name");

        String option = readLine(scanner, "Search by: ");

        if (option == null) {
            return;
        }

        switch (option.trim()) {
            case "1" -> {
                String id = readLine(scanner, "Enter customer ID: ");

                if (id != null) {
                    printCustomer(manager.findCustomerById(id));
                }
            }
            case "2" -> {
                String number = readLine(scanner, "Enter queue number: ");

                if (number != null) {
                    printCustomer(
                            manager.findCustomerByQueueNumber(number)
                    );
                }
            }
            case "3" -> {
                String name = readLine(scanner, "Enter name or part of name: ");

                if (name == null) {
                    return;
                }

                List<Customer> matches = manager.findCustomersByName(name);

                if (matches.isEmpty()) {
                    System.out.println("No matching customers found.");
                    return;
                }

                for (Customer customer : matches) {
                    printCustomer(customer);
                }
            }
            default -> System.out.println(
                    "Invalid search option. Choose 1, 2, or 3."
            );
        }
    }

    private static void printCustomer(Customer customer) {
        if (customer == null) {
            System.out.println("Customer not found.");
            return;
        }

        System.out.println();
        System.out.println(customer);
    }

    private static void cancelCustomer(
            Scanner scanner,
            QueueManager manager) {

        String queueNumber = readLine(
                scanner,
                "Enter queue number to remove: "
        );

        if (queueNumber == null) {
            return;
        }

        Customer customer = manager.cancelCustomer(queueNumber);

        System.out.println(
                customer.getQueueNumber()
                        + " (" + customer.getName() + ")"
                        + " has left the queue."
        );
    }

    private static void viewCounters(QueueManager manager) {
        System.out.println("\nSERVICE COUNTERS");

        for (String details : manager.getCounterDetails()) {
            System.out.println(details);
        }
    }
}