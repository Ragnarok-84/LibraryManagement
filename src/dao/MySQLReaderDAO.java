package dao;

import model.Reader;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLReaderDAO {
    
    // === 1️⃣ Thêm độc giả mới ===
    public void addReader(Reader reader) {
        String sql = "INSERT INTO readers (id, name, phone, email, address, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, reader.getId());
            stmt.setString(2, reader.getName());
            stmt.setString(3, reader.getPhone());
            stmt.setString(4, reader.getEmail());
            stmt.setString(5, reader.getAddress());
            stmt.setString(6, reader.getStatus());
            
            stmt.executeUpdate();
            System.out.println("✅ Thêm độc giả thành công (ghi vào MySQL).");
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi thêm độc giả: " + e.getMessage());
        }
    }

    // === 2️⃣ Lấy toàn bộ danh sách độc giả ===
    public List<Reader> getAllReaders() {
        List<Reader> readers = new ArrayList<>();
        String sql = "SELECT * FROM readers ORDER BY name";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                readers.add(mapResultSetToReader(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi đọc danh sách độc giả: " + e.getMessage());
        }
        
        return readers;
    }

    // === 3️⃣ Tìm độc giả theo ID ===
    public Reader findById(String id) {
        String sql = "SELECT * FROM readers WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReader(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm độc giả: " + e.getMessage());
        }
        
        return null;
    }

    // === 4️⃣ Tìm kiếm độc giả theo từ khóa ===
    public List<Reader> searchReaders(String keyword) {
        List<Reader> readers = new ArrayList<>();
        String sql = "SELECT * FROM readers WHERE name LIKE ? OR id LIKE ? OR phone LIKE ? OR email LIKE ? ORDER BY name";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    readers.add(mapResultSetToReader(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm kiếm độc giả: " + e.getMessage());
        }
        
        return readers;
    }

    // === 5️⃣ Cập nhật thông tin độc giả ===
    public void updateReader(Reader reader) {
        String sql = "UPDATE readers SET name = ?, phone = ?, email = ?, address = ?, status = ? WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, reader.getName());
            stmt.setString(2, reader.getPhone());
            stmt.setString(3, reader.getEmail());
            stmt.setString(4, reader.getAddress());
            stmt.setString(5, reader.getStatus());
            stmt.setString(6, reader.getId());
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Cập nhật thông tin độc giả thành công.");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật độc giả: " + e.getMessage());
        }
    }

    // === 6️⃣ Xóa độc giả ===
    public void deleteReader(String id) {
        String sql = "DELETE FROM readers WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Xóa độc giả thành công.");
            } else {
                System.out.println("❌ Không tìm thấy độc giả với ID: " + id);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi xóa độc giả: " + e.getMessage());
        }
    }

    // === 7️⃣ Hàm tiện ích chuyển ResultSet thành Reader ===
    private Reader mapResultSetToReader(ResultSet rs) throws SQLException {
        Reader reader = new Reader();
        
        reader.setId(rs.getString("id"));
        reader.setName(rs.getString("name"));
        reader.setPhone(rs.getString("phone"));
        reader.setEmail(rs.getString("email"));
        reader.setAddress(rs.getString("address"));
        reader.setStatus(rs.getString("status"));
        
        return reader;
    }
}
