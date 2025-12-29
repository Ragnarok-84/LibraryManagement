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
    public Reader getReaderById(int id) {
        String sql = "SELECT * FROM readers WHERE reader_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Reader r = new Reader();
                r.setReaderID(rs.getInt("reader_id"));
                r.setName(rs.getString("name"));
                r.setEmail(rs.getString("email"));
                r.setPhone(rs.getString("phone"));
                r.setAddress(rs.getString("address"));
                r.setActive(rs.getBoolean("active"));
                return r;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===============================================================
    // CREATE: Thêm độc giả mới
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
                System.out.println("Thêm độc giả thành công!");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm độc giả: " + e.getMessage());
        }
    }

    // ===============================================================
    // READ: Lấy danh sách tất cả độc giả
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
            System.err.println("Lỗi SQL khi lấy danh sách độc giả: " + e.getMessage());
        }
        return readers;
    }

    // ===============================================================
    // READ: Tìm độc giả theo email hoặc tên
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
            System.err.println("Lỗi SQL khi tìm độc giả: " + e.getMessage());
        }
        return readers;
    }

    // ===============================================================
    // UPDATE: Cập nhật thông tin độc giả
    // ===============================================================
    public void updateReader(Reader r) {
        String sql = "UPDATE readers SET name=?, email=?, phone=?, address=?, active=? WHERE reader_id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getName());
            ps.setString(2, r.getEmail());
            ps.setString(3, r.getPhone());
            ps.setString(4, r.getAddress());
            ps.setBoolean(5, r.isActive());
            ps.setInt(6, r.getReaderID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // ===============================================================
    // DELETE: Xóa độc giả
    // ===============================================================
    public void deleteReader(int readerID) {
        // 1. Khai báo các câu lệnh SQL
        String deleteBorrowSql = "DELETE FROM borrow_records WHERE reader_id = ?";
        String deleteReaderSql = "DELETE FROM readers WHERE reader_id = ?";

        Connection conn = null; // Khai báo kết nối ở ngoài để quản lý transaction

        try {
            // 2. Lấy kết nối và TẮT chế độ auto-commit
            conn = getConnection();
            if (conn == null) {
                // Xử lý nếu không lấy được kết nối (tùy chọn ném lỗi hoặc log)
                System.err.println("Không thể kết nối đến database.");
                return;
            }
            conn.setAutoCommit(false); // <-- BẮT ĐẦU TRANSACTION

            // 3. Xóa các bản ghi mượn sách liên quan TRƯỚC (bảng borrow_records)
            try (PreparedStatement stmtBorrow = conn.prepareStatement(deleteBorrowSql)) {
                stmtBorrow.setInt(1, readerID);
                stmtBorrow.executeUpdate();
                // System.out.println("Đã xóa lịch sử mượn của độc giả ID: " + readerID);
            }

            // 4. Xóa độc giả (bảng readers)
            try (PreparedStatement stmtReader = conn.prepareStatement(deleteReaderSql)) {
                stmtReader.setInt(1, readerID);
                int affectedRows = stmtReader.executeUpdate();

                if (affectedRows == 0) {
                    // Nếu không xóa được (ID không tồn tại), có thể ném lỗi để rollback nếu muốn chặt chẽ
                    throw new SQLException("Xóa thất bại, không tìm thấy reader_id = " + readerID);
                }
            }

            // 5. Nếu cả hai lệnh trên thành công, LƯU (commit) transaction
            conn.commit();
            System.out.println("Đã xóa độc giả và toàn bộ lịch sử mượn thành công (ID=" + readerID + ")");

        } catch (SQLException e) {
            // 6. Nếu có BẤT KỲ lỗi nào xảy ra, HỦY BỎ (rollback) toàn bộ thay đổi
            System.err.println("Lỗi SQL khi xóa độc giả, đang rollback... " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // <-- HỦY BỎ TRANSACTION
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi rollback: " + ex.getMessage());
                }
            }
        } finally {
            // 7. Luôn dọn dẹp kết nối và bật lại auto-commit
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Trả lại trạng thái auto-commit mặc định
                    conn.close(); // Đóng kết nối
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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
            System.err.println("Lỗi SQL khi lấy tên độc giả: " + e.getMessage());
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
            System.err.println("Lỗi khi lấy danh sách đọc giả: " + e.getMessage());
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
        return Optional.empty(); // không tìm thấy
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi xóa " + getTableName() + ": " + e.getMessage());
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
        // Câu lệnh SQL để chèn một bản ghi mới
        final String SQL = "INSERT INTO readers (name, email, phone, address, join_date, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection(); // Giả sử phương thức này lấy kết nối DB
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            // Gán các giá trị từ đối tượng reader vào câu lệnh
            stmt.setString(1, reader.getName());
            stmt.setString(2, reader.getEmail());
            stmt.setString(3, reader.getPhone());
            stmt.setString(4, reader.getAddress());

            // Chuyển đổi từ LocalDate sang java.sql.Date và kiểm tra null
            if (reader.getJoinDate() != null) {
                stmt.setDate(5, java.sql.Date.valueOf(reader.getJoinDate()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }

            stmt.setBoolean(6, reader.isActive());

            // Thực thi câu lệnh
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm độc giả: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void update(Reader reader) {
        // Câu lệnh SQL để cập nhật bản ghi dựa trên reader_id
        final String SQL = "UPDATE readers SET name = ?, email = ?, phone = ?, address = ?, " +
                "join_date = ?, is_active = ? " +
                "WHERE reader_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            // Gán các giá trị mới
            stmt.setString(1, reader.getName());
            stmt.setString(2, reader.getEmail());
            stmt.setString(3, reader.getPhone());
            stmt.setString(4, reader.getAddress());

            if (reader.getJoinDate() != null) {
                stmt.setDate(5, java.sql.Date.valueOf(reader.getJoinDate()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }

            stmt.setBoolean(6, reader.isActive());

            // Gán giá trị cho điều kiện WHERE
            stmt.setInt(7, reader.getReaderID());

            // Thực thi và kiểm tra xem có dòng nào được cập nhật không
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("⚠️ Cảnh báo: Không tìm thấy độc giả với ID = " + reader.getReaderID() + " để cập nhật.");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật độc giả: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
