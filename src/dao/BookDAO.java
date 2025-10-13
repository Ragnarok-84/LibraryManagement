package dao;

import model.Book;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    private static final String FILE_PATH = "data/books.csv";

    // === 1️⃣ Thêm sách mới ===
    public void addBook(Book book) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            // format: isbn,title,author,year,total_copies,available_copies,borrowed_count
            bw.write(String.join(",",
                    book.getIsbn(),
                    escapeCSV(book.getTitle()),
                    escapeCSV(book.getAuthor()),
                    String.valueOf(book.getYear()),
                    String.valueOf(book.getTotalCopies()),
                    String.valueOf(book.getAvailableCopies()),
                    String.valueOf(book.getBorrowedCount())
            ));
            bw.newLine();
            System.out.println("✅ Thêm sách thành công (ghi vào CSV).");
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi thêm sách: " + e.getMessage());
        }
    }

    // === 2️⃣ Lấy toàn bộ danh sách sách ===
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = parseCSV(line);
                if (parts.length >= 7) {
                    Book b = new Book(
                            parts[0],
                            parts[1],
                            parts[2],
                            Integer.parseInt(parts[3]),
                            Integer.parseInt(parts[4]),
                            Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6])
                    );
                    books.add(b);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi đọc file CSV: " + e.getMessage());
        }

        return books;
    }

    // === 3️⃣ Tìm sách theo ISBN ===
    public Book findByISBN(String isbn) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = parseCSV(line);
                if (parts.length >= 7 && parts[0].equals(isbn)) {
                    return new Book(
                            parts[0],
                            parts[1],
                            parts[2],
                            Integer.parseInt(parts[3]),
                            Integer.parseInt(parts[4]),
                            Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6])
                    );
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi tìm sách: " + e.getMessage());
        }

        return null;
    }

    // === 4️⃣ Hàm tiện ích xử lý CSV ===
    // Xử lý dấu phẩy trong chuỗi (thêm dấu ngoặc kép nếu cần)
    private String escapeCSV(String value) {
        if (value.contains(",") || value.contains("\"")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    // Xử lý dòng có dấu phẩy trong dấu ngoặc kép
    private String[] parseCSV(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes; // Đảo trạng thái trong ngoặc kép
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString().trim());

        return result.toArray(new String[0]);
    }
}
