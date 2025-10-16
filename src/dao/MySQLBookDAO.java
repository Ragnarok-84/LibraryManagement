package dao;

import model.Book;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MySQLBookDAO {
    
    // === 1️⃣ Thêm sách mới ===
    public void addBook(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, publisher, category, year, total, available, borrowed_count, " +
                     "isbn13, language_code, num_pages, average_rating, ratings_count, text_reviews_count, publication_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getPublisher());
            stmt.setString(5, book.getCategory());
            stmt.setInt(6, book.getYear());
            stmt.setInt(7, book.getTotal());
            stmt.setInt(8, book.getAvailable());
            stmt.setInt(9, book.getBorrowedCount());
            
            // Các trường bổ sung (có thể null)
            stmt.setString(10, book.getIsbn13());
            stmt.setString(11, book.getLanguageCode());
            stmt.setInt(12, book.getNumPages());
            stmt.setBigDecimal(13, book.getAverageRating());
            stmt.setInt(14, book.getRatingsCount());
            stmt.setInt(15, book.getTextReviewsCount());
            stmt.setDate(16, book.getPublicationDate() != null ? Date.valueOf(book.getPublicationDate()) : null);
            
            stmt.executeUpdate();
            System.out.println("✅ Thêm sách thành công (ghi vào MySQL).");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi thêm sách: " + e.getMessage());
        }
    }

    // === 2️⃣ Lấy toàn bộ danh sách sách ===
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY title";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi đọc danh sách sách: " + e.getMessage());
        }
        
        return books;
    }

    // === 3️⃣ Tìm sách theo ISBN ===
    public Book findByISBN(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm sách: " + e.getMessage());
        }
        
        return null;
    }

    // === 4️⃣ Cập nhật số lượng sách (mượn/trả) ===
    public void updateStock(String isbn, int newAvailable, int newBorrowedCount) {
        String sql = "UPDATE books SET available = ?, borrowed_count = ? WHERE isbn = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, newAvailable);
            stmt.setInt(2, newBorrowedCount);
            stmt.setString(3, isbn);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Cập nhật số lượng sách thành công.");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật số lượng sách: " + e.getMessage());
        }
    }

    // === 5️⃣ Tìm kiếm sách theo từ khóa ===
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? ORDER BY title";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm kiếm sách: " + e.getMessage());
        }
        
        return books;
    }

    // === 6️⃣ Lấy top sách được mượn nhiều ===
    public List<Book> getTopBorrowedBooks(int limit) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books WHERE borrowed_count > 0 ORDER BY borrowed_count DESC LIMIT ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy top sách mượn nhiều: " + e.getMessage());
        }
        
        return books;
    }

    // === 7️⃣ Hàm tiện ích chuyển ResultSet thành Book ===
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setCategory(rs.getString("category"));
        book.setYear(rs.getInt("year"));
        book.setTotal(rs.getInt("total"));
        book.setAvailable(rs.getInt("available"));
        book.setBorrowedCount(rs.getInt("borrowed_count"));
        
        // Các trường bổ sung
        book.setIsbn13(rs.getString("isbn13"));
        book.setLanguageCode(rs.getString("language_code"));
        book.setNumPages(rs.getInt("num_pages"));
        book.setAverageRating(rs.getBigDecimal("average_rating"));
        book.setRatingsCount(rs.getInt("ratings_count"));
        book.setTextReviewsCount(rs.getInt("text_reviews_count"));
        
        Date pubDate = rs.getDate("publication_date");
        book.setPublicationDate(pubDate != null ? pubDate.toLocalDate() : null);
        
        return book;
    }
}
