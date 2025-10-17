package dao;

import model.Book;
import util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    /*
    // === Hàm tiện ích: Ánh xạ dòng (row) từ ResultSet sang đối tượng Book ===
    private Book mapRowToBook(ResultSet rs) throws SQLException {
        Book book = new Book();

        // 1. Cột Khóa chính (Primary Key)
        // Giả định: ID là khóa chính, tự tăng
        // book.setId(rs.getInt("id"));

        // 2. Các cột thông tin chính
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setCategory(rs.getString("category"));
        book.setYear(rs.getInt("year"));

        // 3. Các cột số lượng (dùng getTotal/getAvailable)
        book.setTotal(rs.getInt("total"));
        book.setAvailable(rs.getInt("available"));

        // Tính BorrowedCount
        book.setBorrowedCount(book.getTotal() - book.getAvailable());

        // 4. Các trường bổ sung mới (giả định dùng snake_case trong DB)
        book.setIsbn13(rs.getString("isbn13"));
        book.setLanguageCode(rs.getString("language_code"));
        book.setNumPages(rs.getInt("num_pages"));
        book.setAverageRating(rs.getBigDecimal("average_rating"));
        book.setRatingsCount(rs.getInt("ratings_count"));
        book.setTextReviewsCount(rs.getInt("text_reviews_count"));

        // Xử lý DATE sang LocalDate
        Date pubDate = rs.getDate("publication_date");
        if (pubDate != null) {
            book.setPublicationDate(pubDate.toLocalDate());
        }

        return book;
    }*/



    // Trong BookDAO.java

    // === Hàm tiện ích: Ánh xạ dòng (row) từ ResultSet sang đối tượng Book ===
    private Book mapRowToBook(ResultSet rs) throws SQLException {
        Book book = new Book();

        // 1. Cột Khóa chính (Primary Key)
        // Ánh xạ 'bookID' từ DB vào trường ID của Book (nếu có, hoặc dùng tạm ISBN)
        // TÊN CỘT ĐƯỢC SỬA TỪ "id" THÀNH "bookID"
        // book.setId(rs.getInt("bookID")); // <-- Dùng nếu Book model có setId(int)

        // 2. Các cột thông tin chính
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));

        // TÊN CỘT ĐƯỢC SỬA TỪ "author" THÀNH "authors"
        book.setAuthor(rs.getString("authors")); // <-- SỬA: Đọc từ cột 'authors'

        // Các trường này không có trong file mẫu, nhưng tôi giữ nguyên theo cấu trúc cũ
        // Nếu các cột này KHÔNG CÓ trong DB, bạn phải REMOVE chúng.
        // Nếu có trong DB, đảm bảo tên cột là publisher/category/year.
        book.setPublisher(rs.getString("publisher"));
        book.setCategory(rs.getString("category"));
        book.setYear(rs.getInt("year"));

        // 3. Các cột số lượng
        // Tên cột trong file mẫu là ratings_count và text_reviews_count, không có total/available.
        // Nếu bạn đã tạo cột 'total' và 'available' trong DB, hãy giữ nguyên.
        book.setTotal(rs.getInt("total"));
        book.setAvailable(rs.getInt("available"));

        // Tính BorrowedCount
        book.setBorrowedCount(book.getTotal() - book.getAvailable());

        // 4. Các trường bổ sung mới
        book.setIsbn13(rs.getString("isbn13"));
        book.setLanguageCode(rs.getString("language_code"));
        book.setNumPages(rs.getInt("num_pages"));
        book.setAverageRating(rs.getBigDecimal("average_rating"));

        // Các cột số lượng đọc trực tiếp từ DB
        book.setRatingsCount(rs.getInt("ratings_count"));
        book.setTextReviewsCount(rs.getInt("text_reviews_count"));

        // Xử lý DATE sang LocalDate
        // Giả định: Bạn đã lưu trữ cột 'publication_date' dưới dạng DATE trong MySQL
        Date pubDate = rs.getDate("publication_date");
        if (pubDate != null) {
            book.setPublicationDate(pubDate.toLocalDate());
        }

        return book;
    }

    // =========================================================================
    // === 1️⃣ CREATE: Thêm sách mới vào Database ===
    // =========================================================================
    public void addBook(Book book) {
        final String SQL = "INSERT INTO books (isbn, title, author, publisher, category, year, total, available, isbn13, language_code, num_pages, average_rating, ratings_count, text_reviews_count, publication_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getPublisher());
            stmt.setString(5, book.getCategory());
            stmt.setInt(6, book.getYear());
            stmt.setInt(7, book.getTotal());
            stmt.setInt(8, book.getAvailable());

            // Binding các trường bổ sung
            stmt.setString(9, book.getIsbn13());
            stmt.setString(10, book.getLanguageCode());
            stmt.setInt(11, book.getNumPages());
            stmt.setBigDecimal(12, book.getAverageRating());
            stmt.setInt(13, book.getRatingsCount());
            stmt.setInt(14, book.getTextReviewsCount());

            // Xử lý LocalDate sang SQL DATE
            stmt.setDate(15, book.getPublicationDate() != null ? Date.valueOf(book.getPublicationDate()) : null);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Thêm sách thành công (ghi vào MySQL).");
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi thêm sách: " + e.getMessage());
        }
    }

    // =========================================================================
    // === 2️⃣ READ: Lấy toàn bộ danh sách sách từ Database ===
    // =========================================================================
    // Trong BookDAO.java

    public List<Book> getAllBooks() {
        // Cần liệt kê tất cả các cột để đảm bảo tính tường minh và dễ bảo trì
        // CÁC ĐIỂM SỬA CHỮA:
        // 1. Đổi 'id' thành 'bookID'
        // 2. Đổi 'author' thành 'authors'
        final String SQL = "SELECT bookID, isbn, title, authors, publisher, category, year, total, available, isbn13, language_code, num_pages, average_rating, ratings_count, text_reviews_count, publication_date FROM books ORDER BY title ASC";

        List<Book> books = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                // mapRowToBook() sẽ ánh xạ các cột này sang Book Model
                books.add(mapRowToBook(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi đọc sách: " + e.getMessage());
        }
        return books;
    }

    // =========================================================================
    // === 3️⃣ READ: Tìm sách theo ISBN ===
    // =========================================================================
    public Book findByISBN(String isbn) {
        // Cần liệt kê tất cả các cột
        final String SQL = "SELECT id, isbn, title, author, publisher, category, year, total, available, isbn13, language_code, num_pages, average_rating, ratings_count, text_reviews_count, publication_date FROM books WHERE isbn = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBook(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi tìm sách theo ISBN: " + e.getMessage());
        }
        return null;
    }

    // =========================================================================
    // === 4️⃣ UPDATE: Cập nhật thông tin sách ===
    // =========================================================================
    public void updateBook(Book book) {
        final String SQL = "UPDATE books SET title=?, author=?, publisher=?, category=?, year=?, total=?, available=?, isbn13=?, language_code=?, num_pages=?, average_rating=?, ratings_count=?, text_reviews_count=?, publication_date=? WHERE isbn=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getPublisher());
            stmt.setString(4, book.getCategory());
            stmt.setInt(5, book.getYear());
            stmt.setInt(6, book.getTotal());
            stmt.setInt(7, book.getAvailable());

            // Binding các trường bổ sung
            stmt.setString(8, book.getIsbn13());
            stmt.setString(9, book.getLanguageCode());
            stmt.setInt(10, book.getNumPages());
            stmt.setBigDecimal(11, book.getAverageRating());
            stmt.setInt(12, book.getRatingsCount());
            stmt.setInt(13, book.getTextReviewsCount());
            stmt.setDate(14, book.getPublicationDate() != null ? Date.valueOf(book.getPublicationDate()) : null);

            // WHERE clause
            stmt.setString(15, book.getIsbn());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi cập nhật sách: " + e.getMessage());
        }
    }

    // Trong BookDAO.java

// ... (sau các phương thức khác như updateBook)

    // =========================================================================
// === 5️⃣ READ: Tìm kiếm sách theo từ khóa (Sử dụng SQL LIKE) ===
// =========================================================================
    public List<Book> searchBooks(String query) {
        List<Book> books = new ArrayList<>();

        // Tạo chuỗi tìm kiếm Wildcard: %từ_khóa%
        String searchPattern = "%" + query.toLowerCase() + "%";

        // Câu lệnh SQL tìm kiếm trên nhiều cột, chuyển về chữ thường (LOWER) để tìm kiếm không phân biệt hoa/thường
        final String SQL = "SELECT id, isbn, title, author, publisher, category, year, total, available, isbn13, language_code, num_pages, average_rating, ratings_count, text_reviews_count, publication_date FROM books " +
                "WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(isbn) LIKE ? ORDER BY title ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            // Bind giá trị cho cả 3 dấu ? (vì chúng đều dùng cùng một chuỗi tìm kiếm)
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Sử dụng hàm ánh xạ đã có
                    books.add(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi tìm kiếm sách: " + e.getMessage());
        }
        return books;
    }


}