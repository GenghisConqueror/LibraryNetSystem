import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

abstract class LibraryItem {
    protected int id;
    protected String title;
    protected String author;
    protected boolean available;

    public LibraryItem(int id, String title, String author, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.available = available;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return available; }

    public void setAvailable(boolean available) { this.available = available; }

    public abstract String getExtra();
}

class Book extends LibraryItem {
    private int pageCount;
    public Book(int id, String title, String author, boolean available, int pageCount) {
        super(id, title, author, available);
        this.pageCount = pageCount;
    }
    @Override
    public String getExtra() { return String.valueOf(pageCount); }
}

interface Playable { void play(); }

class Audiobook extends LibraryItem implements Playable {
    private String duration;
    public Audiobook(int id, String title, String author, boolean available, String duration) {
        super(id, title, author, available);
        this.duration = duration;
    }
    @Override
    public String getExtra() { return duration; }
    @Override
    public void play() {
        System.out.println("[INFO] Playing audiobook: " + title + " by " + author + " (" + duration + ")");
    }
}

class EMagazine extends LibraryItem {
    private String issueNumber;
    public EMagazine(int id, String title, String author, boolean available, String issueNumber) {
        super(id, title, author, available);
        this.issueNumber = issueNumber;
    }
    @Override
    public String getExtra() { return issueNumber; }
}

class Logger {
    private static final String LOG_FILE = "errors.log";
    public static void logError(String msg) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write("[" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + "] ERROR: " + msg + "\n");
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to write log: " + e.getMessage());
        }
    }
}

public class LibraryNetSystem {
    private static final String INVENTORY_FILE = "inventory.txt";
    private static final String BORROWED_FILE = "borrowed.txt";
    private static List<LibraryItem> inventory = new ArrayList<>();

    public static void main(String[] args) {
        loadInventory();

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n==== LibraNet System ====");
            System.out.println("[1] Admin Login");
            System.out.println("[2] User Login");
            System.out.println("[3] Exit");
            System.out.print("Choose option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> adminMenu(sc);
                case 2 -> userMenu(sc);
                case 3 -> { System.out.println("[INFO] Exiting... Goodbye!"); return; }
                default -> System.out.println("[WARN] Invalid choice. Try again.");
            }
        }
    }

    // ================= ADMIN MENU =================
    private static void adminMenu(Scanner sc) {
        while (true) {
            System.out.println("\n==== Admin Dashboard ====");
            System.out.println("[1] View Inventory");
            System.out.println("[2] View Overdue Items");
            System.out.println("[3] Add Item");
            System.out.println("[4] Remove Item");
            System.out.println("[5] Back");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt(); sc.nextLine();
            switch (choice) {
                case 1 -> viewInventory();
                case 2 -> viewOverdueItems();
                case 3 -> addItem(sc);
                case 4 -> removeItem(sc);
                case 5 -> { return; }
                default -> System.out.println("[WARN] Invalid choice.");
            }
        }
    }

    // ================= USER MENU =================
    private static void userMenu(Scanner sc) {
        while (true) {
            System.out.println("\n==== User Dashboard ====");
            System.out.println("[1] View Catalog");
            System.out.println("[2] Borrow Item");
            System.out.println("[3] Return Item");
            System.out.println("[4] View My Due Items");
            System.out.println("[5] Back");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt(); sc.nextLine();
            switch (choice) {
                case 1 -> viewInventory();
                case 2 -> borrowItem(sc);
                case 3 -> returnItem(sc);
                case 4 -> viewUserDueItems();
                case 5 -> { return; }
                default -> System.out.println("[WARN] Invalid choice.");
            }
        }
    }

    // ================= CORE FUNCTIONS =================
    private static void loadInventory() {
        try (BufferedReader br = new BufferedReader(new FileReader(INVENTORY_FILE))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                String type = parts[1];
                String title = parts[2];
                String author = parts[3];
                boolean available = Boolean.parseBoolean(parts[4]);
                String extra = parts[5];

                switch (type) {
                    case "Book" -> inventory.add(new Book(id, title, author, available, Integer.parseInt(extra)));
                    case "Audiobook" -> inventory.add(new Audiobook(id, title, author, available, extra));
                    case "EMagazine" -> inventory.add(new EMagazine(id, title, author, available, extra));
                }
            }
        } catch (Exception e) {
            Logger.logError("Failed to load inventory: " + e.getMessage());
        }
    }

    private static void saveInventory() {
        try (FileWriter fw = new FileWriter(INVENTORY_FILE)) {
            fw.write("ID,Type,Title,Author,Availability,Extra\n");
            for (LibraryItem item : inventory) {
                String type = item instanceof Book ? "Book" :
                        item instanceof Audiobook ? "Audiobook" : "EMagazine";
                fw.write(item.getId() + "," + type + "," + item.getTitle() + "," +
                        item.getAuthor() + "," + item.isAvailable() + "," + item.getExtra() + "\n");
            }
        } catch (IOException e) {
            Logger.logError("Failed to save inventory: " + e.getMessage());
        }
    }

    private static void viewInventory() {
        System.out.println("\n==== Inventory ====");
        for (LibraryItem item : inventory) {
            System.out.printf("[%d] %-20s by %-15s | Available: %-5s | Extra: %s\n",
                    item.getId(), item.getTitle(), item.getAuthor(), item.isAvailable(), item.getExtra());
        }
    }

    private static void addItem(Scanner sc) {
        try {
            System.out.print("Enter Item ID: "); int id = sc.nextInt(); sc.nextLine();
            System.out.print("Enter Title: "); String title = sc.nextLine();
            System.out.print("Enter Author: "); String author = sc.nextLine();
            System.out.print("Enter Type (Book/Audiobook/EMagazine): "); String type = sc.nextLine();

            if (type.equalsIgnoreCase("Book")) {
                System.out.print("Enter Page Count: "); int pages = sc.nextInt(); sc.nextLine();
                inventory.add(new Book(id, title, author, true, pages));
            } else if (type.equalsIgnoreCase("Audiobook")) {
                System.out.print("Enter Duration (e.g., 15h30m): "); String duration = sc.nextLine();
                inventory.add(new Audiobook(id, title, author, true, duration));
            } else if (type.equalsIgnoreCase("EMagazine")) {
                System.out.print("Enter Issue Number: "); String issue = sc.nextLine();
                inventory.add(new EMagazine(id, title, author, true, issue));
            }
            saveInventory();
            System.out.println("[INFO] Item added successfully.");
        } catch (Exception e) {
            Logger.logError("Failed to add item: " + e.getMessage());
        }
    }

    private static void removeItem(Scanner sc) {
        System.out.print("Enter ID to remove: "); int id = sc.nextInt(); sc.nextLine();
        inventory.removeIf(item -> item.getId() == id);
        saveInventory();
        System.out.println("[INFO] Item removed successfully.");
    }

    private static void borrowItem(Scanner sc) {
        System.out.print("Enter Item ID to borrow: "); int id = sc.nextInt(); sc.nextLine();
        for (LibraryItem item : inventory) {
            if (item.getId() == id && item.isAvailable()) {
                item.setAvailable(false);
                saveInventory();
                saveBorrowed("U001", id); // Demo: fixed UserID
                System.out.println("[INFO] Borrowed successfully. Due in 7 days.");
                return;
            }
        }
        System.out.println("[WARN] Item not available.");
    }

    private static void returnItem(Scanner sc) {
        System.out.print("Enter Item ID to return: "); int id = sc.nextInt(); sc.nextLine();
        for (LibraryItem item : inventory) {
            if (item.getId() == id && !item.isAvailable()) {
                item.setAvailable(true);
                saveInventory();
                updateBorrowed(id);
                System.out.println("[INFO] Returned successfully.");
                return;
            }
        }
        System.out.println("[WARN] Invalid return request.");
    }

    private static void saveBorrowed(String userId, int itemId) {
        try (FileWriter fw = new FileWriter(BORROWED_FILE, true)) {
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = borrowDate.plusDays(7);
            fw.write(userId + "," + itemId + "," + borrowDate + "," + dueDate + ",ACTIVE\n");
        } catch (IOException e) {
            Logger.logError("Failed to save borrowed: " + e.getMessage());
        }
    }

    private static void updateBorrowed(int itemId) {
        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(BORROWED_FILE))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (Integer.parseInt(parts[1]) == itemId && parts[4].equals("ACTIVE")) {
                        parts[4] = "RETURNED";
                        line = String.join(",", parts);
                    }
                    lines.add(line);
                }
            }
            try (FileWriter fw = new FileWriter(BORROWED_FILE)) {
                for (String l : lines) fw.write(l + "\n");
            }
        } catch (IOException e) {
            Logger.logError("Failed to update borrowed: " + e.getMessage());
        }
    }

    private static void viewOverdueItems() {
        System.out.println("\n==== Overdue Items ====");
        try (BufferedReader br = new BufferedReader(new FileReader(BORROWED_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[4].equals("ACTIVE")) {
                    LocalDate due = LocalDate.parse(parts[3]);
                    if (due.isBefore(LocalDate.now())) {
                        System.out.println("[OVERDUE] ItemID: " + parts[1] + " | UserID: " + parts[0] + " | Due: " + due);
                    }
                }
            }
        } catch (IOException e) {
            Logger.logError("Failed to view overdue items: " + e.getMessage());
        }
    }

    private static void viewUserDueItems() {
        System.out.println("\n==== Your Due Items ====");
        try (BufferedReader br = new BufferedReader(new FileReader(BORROWED_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals("U001") && parts[4].equals("ACTIVE")) {
                    LocalDate due = LocalDate.parse(parts[3]);
                    long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), due);
                    if (daysLeft >= 0) {
                        System.out.println("[INFO] ItemID: " + parts[1] + " | Due in " + daysLeft + " days (" + due + ")");
                    } else {
                        System.out.println("[WARN] ItemID: " + parts[1] + " | OVERDUE since " + due);
                    }
                }
            }
        } catch (IOException e) {
            Logger.logError("Failed to view user due items: " + e.getMessage());
        }
    }
}
