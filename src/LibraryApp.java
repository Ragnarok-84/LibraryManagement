import model.*;
import storage.ExcelManager;
import java.time.LocalDate;
import java.util.*;

public class LibraryApp {
    private static final List<Book> books = new ArrayList<>();
    private static final List<Reader> readers = new ArrayList<>();
    private static final List<BorrowRecord> records = new ArrayList<>();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        seedData(); // dữ liệu mẫu

        while (true) {
            System.out.println("\n=== QUẢN LÝ THƯ VIỆN ===");
            System.out.println("1. Xem danh sách sách");
            System.out.println("2. Mượn sách");
            System.out.println("3. Trả sách");
            System.out.println("4. Thống kê");
            System.out.println("5. Lưu ra Excel");
            System.out.println("0. Thoát");
            System.out.print("Chọn: ");
            int c = Integer.parseInt(sc.nextLine());
            switch (c) {
                case 1 -> listBooks();
                case 2 -> borrowBook();
                case 3 -> returnBook();
                case 4 -> showStats();
                case 5 -> ExcelManager.writeAll(books, readers, records);
                case 0 -> System.exit(0);
            }
        }
    }

    private static void seedData() {
        books.add(new Book("001", "Clean Code", "Robert Martin", 2008, 3));
        books.add(new Book("002", "Effective Java", "Joshua Bloch", 2018, 2));
        Reader r1 = new Reader(); r1.setId("DG001"); r1.setName("Nguyen Kien"); r1.setEmail("kientoan1chy@gmail.com");
        Reader r2 = new Reader(); r2.setId("DG002"); r2.setName("Tran Mai"); r2.setEmail("maitran@gmail.com");
        readers.add(r1); readers.add(r2);
    }

    private static void listBooks() {
        System.out.println("=== Danh sách sách ===");
        books.forEach(System.out::println);
    }

    private static void borrowBook() {
        System.out.print("Nhập ID độc giả: ");
        String rid = sc.nextLine();
        System.out.print("Nhập ISBN sách: ");
        String isbn = sc.nextLine();

        Book b = books.stream().filter(x -> x.getIsbn().equals(isbn)).findFirst().orElse(null);
        if (b == null || b.getAvailable() == 0) {
            System.out.println("Không tìm thấy hoặc sách đã hết!");
            return;
        }
        b.borrow();
        BorrowRecord rec = new BorrowRecord();
        Reader reader = readers.stream().filter(x -> x.getId().equals(rid)).findFirst().orElse(null);
        rec.setReader(reader);
        rec.setBook(b);
        rec.setBorrowDate(LocalDate.now());
        rec.setDueDate(LocalDate.now().plusDays(14));
        records.add(rec);
        System.out.println("Mượn thành công!");
    }

    private static void returnBook() {
        System.out.print("Nhập ID độc giả: ");
        String rid = sc.nextLine();
        System.out.print("Nhập ISBN sách: ");
        String isbn = sc.nextLine();

        for (BorrowRecord r : records) {
            if (r.getReader() != null && r.getBook() != null && r.getReader().getId().equals(rid) && r.getBook().getIsbn().equals(isbn) && r.getReturnDate() == null) {
                r.setReturnDate(LocalDate.now());
                books.stream()
                        .filter(b -> b.getIsbn().equals(isbn))
                        .findFirst()
                        .ifPresent(Book::returnBook);
                System.out.println("Trả thành công!");
                return;
            }
        }

        System.out.println("Không tìm thấy bản ghi mượn!");
    }

    private static void showStats() {
        books.stream().max(Comparator.comparingInt(Book::getBorrowedCount)).ifPresent(top -> System.out.println("Sách được mượn nhiều nhất: " + top.getTitle() + " (" + top.getBorrowedCount() + " lần)"));
    }
}
