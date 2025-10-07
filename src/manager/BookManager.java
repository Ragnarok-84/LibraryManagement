package manager;

import java.util.*;
import model.Book;

public class BookManager {
    private final Map<String, Book> bookMap; // Tra cứu theo ISBN (mã sách duy nhất)

    public BookManager() {
        this.bookMap = new HashMap<>();
    }

    // ✅ Thêm sách mới
    public void addBook(Book book) {
        if (bookMap.containsKey(book.getIsbn())) {
            System.out.println("❌ ISBN đã tồn tại! Không thể thêm sách mới.");
        } else {
            bookMap.put(book.getIsbn(), book);
            System.out.println("✅ Đã thêm sách: " + book.getTitle());
        }
    }

    // ✅ Xóa sách theo ISBN
    public void removeBook(String isbn) {
        if (bookMap.remove(isbn) != null) {
            System.out.println("✅ Đã xóa sách có ISBN " + isbn);
        } else {
            System.out.println("❌ Không tìm thấy sách có ISBN này.");
        }
    }

    // ✅ Tìm sách theo ISBN
    public Book findBookByIsbn(String isbn) {
        return bookMap.get(isbn);
    }

    // ✅ Hiển thị tất cả sách
    public void displayAllBooks() {
        if (bookMap.isEmpty()) {
            System.out.println("📭 Chưa có sách nào trong thư viện.");
        } else {
            System.out.println("📚 Danh sách sách hiện có:");
            for (Book b : bookMap.values()) {
                System.out.println("ISBN: " + b.getIsbn() +
                        " | Tên: " + b.getTitle() +
                        " | Tác giả: " + b.getAuthor() +
                        " | Năm: " + b.getYear() +
                        " | Sẵn có: " + b.getAvailableCopies());
            }
        }
    }

    // ✅ Lấy danh sách tất cả sách (nếu cần dùng ở nơi khác)
    public Collection<Book> getAllBooks() {
        return bookMap.values();
    }
}
