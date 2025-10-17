package dao;

import model.Reader;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReaderDAO {

    // === Ánh xạ dòng từ ResultSet sang đối tượng Reader ===
    private Reader mapRowToReader(ResultSet rs) throws SQLException {
        Reader r = new Reader();
        r.setReaderID(rs.getInt("readerID"));
        r.setFullName(rs.getString("full_name"));
        r.setEmail(rs.getString("email"));
        r.setPhone(rs.getString("phone"));
        r.setAddress(rs.getString("address"));
        r.setJoinDate(rs.getDate("join_date").toLocalDate());
        r.setActive(rs.getBoolean("active"));
        return r;
    }

    // ===============================================================
    // 1️⃣ CREATE: Thêm độc giả mới
    // ===============================================================
    public void addReader(Reader reader) {
        final String SQL = "INSERT INTO readers (full_name, email, phone, address, join_date, active) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, reader.getFullName());
            stmt.setString(2, reader.getEmail());
            stmt.setString(3, reader.getPhone());
            stmt.setString(4, reader.getAddress());
            stmt.setDate(5, Date.valueOf(reader.getJoinDate()));
            stmt.setBoolean(6, reader.isActive());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✅ Thêm độc giả thành công!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi thêm độc giả: " + e.getMessage());
        }
    }

    // ===============================================================
    // 2️⃣ READ: Lấy danh sách tất cả độc giả
    // ===============================================================
    public List<Reader> getAllReaders() {
        List<Reader> readers = new ArrayList<>();
        final String SQL = "SELECT * FROM readers ORDER BY full_name ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                readers.add(mapRowToReader(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi lấy danh sách độc giả: " + e.getMessage());
        }
        return readers;
    }

    // ===============================================================
    // 3️⃣ READ: Tìm độc giả theo email hoặc tên
    // ===============================================================
    public List<Reader> searchReaders(String keyword) {
        List<Reader> readers = new ArrayList<>();
        String search = "%" + keyword.toLowerCase() + "%";

        final String SQL = "SELECT * FROM readers WHERE LOWER(full_name) LIKE ? OR LOWER(email) LIKE ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, search);
            stmt.setString(2, search);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    readers.add(mapRowToReader(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi tìm độc giả: " + e.getMessage());
        }
        return readers;
    }

    // ===============================================================
    // 4️⃣ UPDATE: Cập nhật thông tin độc giả
    // ===============================================================
    public void updateReader(Reader reader) {
        final String SQL = "UPDATE readers SET full_name=?, email=?, phone=?, address=?, active=? WHERE readerID=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, reader.getFullName());
            stmt.setString(2, reader.getEmail());
            stmt.setString(3, reader.getPhone());
            stmt.setString(4, reader.getAddress());
            stmt.setBoolean(5, reader.isActive());
            stmt.setInt(6, reader.getReaderID());

            stmt.executeUpdate();
            System.out.println("✅ Cập nhật độc giả thành công!");

        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi cập nhật độc giả: " + e.getMessage());
        }
    }

    // ===============================================================
    // 5️⃣ DELETE: Xóa độc giả
    // ===============================================================
    public void deleteReader(int readerID) {
        final String SQL = "DELETE FROM readers WHERE readerID=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, readerID);
            stmt.executeUpdate();
            System.out.println("✅ Đã xóa độc giả ID=" + readerID);
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi xóa độc giả: " + e.getMessage());
        }
    }
}
