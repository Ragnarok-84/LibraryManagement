package dao;

import model.Reader;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReaderDAO extends BaseDAO<Reader> {

    // === Ánh xạ dòng từ ResultSet sang đối tượng Reader ===
    @Override
    protected Reader mapRowToEntity(ResultSet rs) throws SQLException {
        Reader reader = new Reader();
        reader.setReaderID(rs.getInt("reader_ID"));
        reader.setName(rs.getString("name"));
        reader.setEmail(rs.getString("email"));
        reader.setPhone(rs.getString("phone"));
        reader.setAddress(rs.getString("address"));
        String dateStr = rs.getString("join_date"); // hoặc "membership_date"
        if (dateStr != null && !dateStr.isEmpty()) {
            reader.setJoinDate(LocalDate.parse(dateStr));
        } else {
            reader.setJoinDate(null); // hoặc giá trị mặc định nào đó
        }

        reader.setActive(rs.getBoolean("active"));
    return reader;
    }

    // ===============================================================
    // 1️⃣ CREATE: Thêm độc giả mới
    // ===============================================================
    public void addReader(Reader reader) {
        final String SQL = "INSERT INTO readers (name, email, phone, address, join_date, active) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, reader.getName());
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
        final String SQL = "SELECT * FROM readers ORDER BY name ASC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                readers.add(mapRowToEntity(rs));
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

        final String SQL = "SELECT * FROM readers WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? ";


        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, search);
            stmt.setString(2, search);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    readers.add(mapRowToEntity(rs));
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
        final String SQL = "UPDATE readers SET name=?, email=?, phone=?, address=?, active=? WHERE readerID=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, reader.getName());
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
        final String SQL = "DELETE FROM readers WHERE reader_id=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, readerID);
            stmt.executeUpdate();
            System.out.println("✅ Đã xóa độc giả ID=" + readerID);
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi xóa độc giả: " + e.getMessage());
        }
    }


    public String getReaderNameById(int readerID) {
        final String SQL = "SELECT name FROM readers WHERE reader_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, readerID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi lấy tên độc giả: " + e.getMessage());
        }

        return "(Không rõ)"; // trả về mặc định nếu không tìm thấy
    }

    public int countReaders() {
        String sql = "SELECT COUNT(*) FROM readers";
        int count = 0;

        try (Connection con = getConnection(); // Lấy kết nối
             Statement stmt = con.createStatement(); // Tạo Statement
             ResultSet rs = stmt.executeQuery(sql)) { // Thực thi truy vấn

            if (rs.next()) {
                // Lấy giá trị từ cột đầu tiên (COUNT(*))
                count = rs.getInt(1);
            }

        } catch (Exception e) {
            // Xử lý lỗi kết nối hoặc truy vấn (ví dụ: ghi log)
            System.err.println("Lỗi khi đếm số người đọc: " + e.getMessage());
            // Trả về 0 nếu có lỗi xảy ra
            count = 0;
        }
        return count;
    }

    @Override
    public List<Reader> findAll() {
        List<Reader> list = new ArrayList<>();
        String sql = "SELECT * FROM readers ";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy danh sách đọc giả: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Reader> findByID(int id) {
        String sql = "SELECT * FROM readers WHERE reader_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Reader reader = mapRowToEntity(rs);
                    return Optional.of(reader);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty(); // ❌ không tìm thấy
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi xóa " + getTableName() + ": " + e.getMessage());
        }
    }

    @Override
    protected String getTableName() {
        return "readers";
    }

    @Override
    protected String getIdColumnName() {
        return "reader_id";
    }

    @Override
    public void add(Reader reader) {
        // Logic để thêm một cuốn sách vào cơ sở dữ liệu
    }

    @Override
    public void update(Reader reader){

    }


}
