package dao;

import model.BorrowRecord;
import model.Book;
import model.Reader;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MySQLBorrowRecordDAO {
    
    // === 1️⃣ Tạo phiếu mượn mới ===
    public void addBorrowRecord(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records (reader_id, isbn, borrow_date, due_date, return_date) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, record.getReader().getId());
            stmt.setString(2, record.getBook().getIsbn());
            stmt.setDate(3, Date.valueOf(record.getBorrowDate()));
            stmt.setDate(4, record.getDueDate() != null ? Date.valueOf(record.getDueDate()) : null);
            stmt.setDate(5, record.getReturnDate() != null ? Date.valueOf(record.getReturnDate()) : null);
            
            stmt.executeUpdate();
            System.out.println("✅ Tạo phiếu mượn thành công.");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tạo phiếu mượn: " + e.getMessage());
        }
    }

    // === 2️⃣ Lấy tất cả phiếu mượn ===
    public List<BorrowRecord> getAllBorrowRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT br.*, r.name as reader_name, r.phone, r.email, r.address, r.status as reader_status, " +
                     "b.title, b.author, b.publisher " +
                     "FROM borrow_records br " +
                     "JOIN readers r ON br.reader_id = r.id " +
                     "JOIN books b ON br.isbn = b.isbn " +
                     "ORDER BY br.borrow_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                records.add(mapResultSetToBorrowRecord(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi đọc danh sách phiếu mượn: " + e.getMessage());
        }
        
        return records;
    }

    // === 3️⃣ Tìm phiếu mượn theo độc giả ===
    public List<BorrowRecord> findByReader(String readerId) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT br.*, r.name as reader_name, r.phone, r.email, r.address, r.status as reader_status, " +
                     "b.title, b.author, b.publisher " +
                     "FROM borrow_records br " +
                     "JOIN readers r ON br.reader_id = r.id " +
                     "JOIN books b ON br.isbn = b.isbn " +
                     "WHERE br.reader_id = ? " +
                     "ORDER BY br.borrow_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, readerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToBorrowRecord(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm phiếu mượn theo độc giả: " + e.getMessage());
        }
        
        return records;
    }

    // === 4️⃣ Tìm phiếu mượn theo sách ===
    public List<BorrowRecord> findByBook(String isbn) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT br.*, r.name as reader_name, r.phone, r.email, r.address, r.status as reader_status, " +
                     "b.title, b.author, b.publisher " +
                     "FROM borrow_records br " +
                     "JOIN readers r ON br.reader_id = r.id " +
                     "JOIN books b ON br.isbn = b.isbn " +
                     "WHERE br.isbn = ? " +
                     "ORDER BY br.borrow_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapResultSetToBorrowRecord(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm phiếu mượn theo sách: " + e.getMessage());
        }
        
        return records;
    }

    // === 5️⃣ Lấy phiếu mượn chưa trả ===
    public List<BorrowRecord> getActiveBorrowRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT br.*, r.name as reader_name, r.phone, r.email, r.address, r.status as reader_status, " +
                     "b.title, b.author, b.publisher " +
                     "FROM borrow_records br " +
                     "JOIN readers r ON br.reader_id = r.id " +
                     "JOIN books b ON br.isbn = b.isbn " +
                     "WHERE br.return_date IS NULL " +
                     "ORDER BY br.due_date ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                records.add(mapResultSetToBorrowRecord(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi đọc phiếu mượn chưa trả: " + e.getMessage());
        }
        
        return records;
    }

    // === 6️⃣ Lấy sách quá hạn ===
    public List<BorrowRecord> getOverdueRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT br.*, r.name as reader_name, r.phone, r.email, r.address, r.status as reader_status, " +
                     "b.title, b.author, b.publisher " +
                     "FROM borrow_records br " +
                     "JOIN readers r ON br.reader_id = r.id " +
                     "JOIN books b ON br.isbn = b.isbn " +
                     "WHERE br.return_date IS NULL AND br.due_date < CURDATE() " +
                     "ORDER BY br.due_date ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                records.add(mapResultSetToBorrowRecord(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi đọc sách quá hạn: " + e.getMessage());
        }
        
        return records;
    }

    // === 7️⃣ Cập nhật ngày trả sách ===
    public void returnBook(long recordId) {
        String sql = "UPDATE borrow_records SET return_date = CURDATE() WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, recordId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Cập nhật ngày trả sách thành công.");
            } else {
                System.out.println("❌ Không tìm thấy phiếu mượn với ID: " + recordId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật ngày trả sách: " + e.getMessage());
        }
    }

    // === 8️⃣ Hàm tiện ích chuyển ResultSet thành BorrowRecord ===
    private BorrowRecord mapResultSetToBorrowRecord(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        
        // Tạo Reader
        Reader reader = new Reader();
        reader.setId(rs.getString("reader_id"));
        reader.setName(rs.getString("reader_name"));
        reader.setPhone(rs.getString("phone"));
        reader.setEmail(rs.getString("email"));
        reader.setAddress(rs.getString("address"));
        reader.setStatus(rs.getString("reader_status"));
        
        // Tạo Book
        Book book = new Book();
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        
        // Thiết lập BorrowRecord
        record.setReader(reader);
        record.setBook(book);
        record.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
        
        Date dueDate = rs.getDate("due_date");
        record.setDueDate(dueDate != null ? dueDate.toLocalDate() : null);
        
        Date returnDate = rs.getDate("return_date");
        record.setReturnDate(returnDate != null ? returnDate.toLocalDate() : null);
        
        return record;
    }
}
