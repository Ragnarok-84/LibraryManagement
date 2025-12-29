package dao;

import model.BorrowRecord;
import util.DBConnection;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class BorrowRecordDAO extends BaseDAO<BorrowRecord> {

    // === Ánh xạ dòng sang đối tượng BorrowRecord ===
    protected BorrowRecord mapRowToEntity(ResultSet rs) throws SQLException {
        BorrowRecord br = new BorrowRecord();
        br.setRecordID(rs.getInt("record_id"));
        br.setReaderID(rs.getInt("reader_id"));
        br.setBookID(rs.getInt("book_id"));
        String borrowStr = rs.getString("borrow_date");
        if (borrowStr != null && !borrowStr.isEmpty()) {
            br.setBorrowDate(LocalDate.parse(borrowStr));
        } else {
            br.setBorrowDate(null);
        }

        String dueStr = rs.getString("due_date");
        if (dueStr != null && !dueStr.isEmpty()) {
            br.setDueDate(LocalDate.parse(dueStr));
        } else {
            br.setDueDate(null);
        }

        String returnStr = rs.getString("return_date");
        if (returnStr != null && !returnStr.isEmpty()) {
            br.setReturnDate(LocalDate.parse(returnStr));
        } else {
            br.setReturnDate(null);
        }

        br.setStatus(rs.getString("status"));
        return br;
    }

    // ===============================================================
    // CREATE: Thêm bản ghi mượn sách
    // ===============================================================
    public void addBorrowRecord(BorrowRecord record) {
        final String SQL = "INSERT INTO borrow_records (reader_id, book_id, borrow_date, due_date, return_date, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, record.getReaderID());
            stmt.setInt(2, record.getBookID());
            stmt.setDate(3, Date.valueOf(record.getBorrowDate()));
            stmt.setDate(4, Date.valueOf(record.getDueDate()));
            if (record.getReturnDate() != null) {
                stmt.setDate(5, Date.valueOf(record.getReturnDate()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            stmt.setString(6, record.getStatus());

            stmt.executeUpdate();
            System.out.println("Thêm bản ghi mượn thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm bản ghi mượn: " + e.getMessage());
        }
    }

    // ===============================================================
    // READ: Lấy danh sách tất cả bản ghi mượn
    // ===============================================================
    public List<BorrowRecord> getAllRecords() {
        List<BorrowRecord> list = new ArrayList<>();
        final String SQL = "SELECT * FROM borrow_records ORDER BY borrow_date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                list.add(mapRowToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đọc danh sách mượn: " + e.getMessage());
        }
        return list;
    }

    // ===============================================================
    // UPDATE: Cập nhật trả sách (hoặc trạng thái)
    // ===============================================================
    public void updateReturnStatus(int recordID, LocalDate returnDate, String status) {
        final String SQL = "UPDATE borrow_records SET return_date=?, status=? WHERE record_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setDate(1, returnDate != null ? Date.valueOf(returnDate) : null);
            stmt.setString(2, status);
            stmt.setInt(3, recordID);
            stmt.executeUpdate();

            System.out.println("Cập nhật trạng thái mượn/trả thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật trạng thái mượn: " + e.getMessage());
        }
    }

    // ===============================================================
    // MARK RETURNED: Dành cho UI gọi khi ấn nút "Trả sách"
    // ===============================================================
    public void markReturned(int recordID) {
        // SỬA: WHERE recordID=? -> WHERE record_id=?
        // Thêm update sách: Có thể bạn muốn tăng số lượng sách available lên 1 ở đây (nếu cần)
        final String SQL = "UPDATE borrow_records SET return_date = CURDATE(), status = 'RETURNED' WHERE record_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, recordID);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                System.out.println("Đánh dấu trả sách thành công cho recordID=" + recordID);
            } else {
                System.out.println("Không tìm thấy recordID: " + recordID + " (Kiểm tra lại tên cột ID trong DB)");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đánh dấu trả sách: " + e.getMessage());
        }
    }

    // ===============================================================
    // READ: Lấy lịch sử mượn của một độc giả
    // ===============================================================
    public List<BorrowRecord> getRecordsByReader(int readerID) {
        List<BorrowRecord> list = new ArrayList<>();
        final String SQL = "SELECT * FROM borrow_records WHERE readerID=? ORDER BY borrow_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, readerID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy lịch sử mượn: " + e.getMessage());
        }
        return list;
    }

    // ===============================================================
    // DELETE: Xóa bản ghi mượn
    // ===============================================================
    public void deleteRecord(int recordID) {
        // SỬA: WHERE recordID=? -> WHERE record_id=?
        final String SQL = "DELETE FROM borrow_records WHERE record_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, recordID);
            stmt.executeUpdate();
            System.out.println("Xóa bản ghi mượn thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi xóa bản ghi mượn: " + e.getMessage());
        }
    }

    // ===============================================================
// REPORTS: Top sách và độc giả
// ===============================================================
    public Map<String, Integer> getTopBooks(int limit) {
        Map<String, Integer> result = new LinkedHashMap<>();
        final String SQL = """
        SELECT b.title, COUNT(*) AS borrow_count
        FROM borrow_records br
        JOIN books b ON br.book_id = b.book_id
        GROUP BY b.title
        ORDER BY borrow_count DESC
        LIMIT ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("title"), rs.getInt("borrow_count"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy top sách: " + e.getMessage());
        }
        return result;
    }

    public Map<String, Integer> getTopReaders(int limit) {
        Map<String, Integer> result = new LinkedHashMap<>();
        final String SQL = """
        SELECT r.name, COUNT(*) AS borrow_count
        FROM borrow_records br
        JOIN readers r ON br.reader_id = r.reader_id
        GROUP BY r.name
        ORDER BY borrow_count DESC
        LIMIT ?
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("name"), rs.getInt("borrow_count"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy top độc giả: " + e.getMessage());
        }
        return result;
    }

    // ===============================================================
// COUNT & REPORT HELPERS
// ===============================================================

    public int countBorrowing() {
        final String SQL = "SELECT COUNT(*) FROM borrow_records WHERE return_date IS NULL";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đếm số lượt mượn: " + e.getMessage());
        }
        return 0;
    }



    // Lấy danh sách bản ghi quá hạn (chưa trả và quá due_date)
    public List<BorrowRecord> getOverdueRecords() {
        List<BorrowRecord> list = new ArrayList<>();
        final String SQL = "SELECT * FROM borrow_records WHERE return_date IS NULL AND due_date < CURRENT_DATE ORDER BY due_date ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {
            while (rs.next()) list.add(mapRowToEntity(rs));
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy danh sách quá hạn: " + e.getMessage());
        }
        return list;
    }

    // Lấy các bản ghi gần đây nhất (dù trả hay chưa)
    public List<BorrowRecord> getRecentRecords(int limit) {
        List<BorrowRecord> list = new ArrayList<>();
        final String SQL = "SELECT * FROM borrow_records ORDER BY borrow_date DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRowToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy bản ghi gần đây: " + e.getMessage());
        }
        return list;
    }

    public List<BorrowRecord> getAllRecordsSorted() {
        List<BorrowRecord> list = new ArrayList<>();
        final String SQL = """
        SELECT br.record_id, br.reader_id, br.book_id,
               r.name AS reader_name, b.title AS book_title,
               br.borrow_date, br.due_date, br.return_date
        FROM borrow_records br
        JOIN readers r ON br.reader_id = r.reader_id
        JOIN books b ON br.book_id = b.book_id
        ORDER BY br.record_id ASC
    """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                BorrowRecord br = new BorrowRecord();
                br.setRecordID(rs.getInt("record_id"));
                br.setReaderID(rs.getInt("reader_id"));
                br.setBookID(rs.getInt("book_id"));
                br.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
                br.setDueDate(rs.getDate("due_date").toLocalDate());

                Date ret = rs.getDate("return_date");
                if (ret != null) br.setReturnDate(ret.toLocalDate());

                list.add(br);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy danh sách phiếu mượn: " + e.getMessage());
        }
        return list;
    }



    @Override
    public List<BorrowRecord> findAll() {
        List<BorrowRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM borrow_records ";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách bản ghi: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<BorrowRecord> findByID(int id) {
        String sql = "SELECT * FROM borrow_records WHERE record_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BorrowRecord br = mapRowToEntity(rs);
                    return Optional.of(br);
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
        return "borrow_records";
    }

    @Override
    protected String getIdColumnName() {
        return "record_id";
    }

    @Override
    public void add(BorrowRecord borrowRecord) {
        // Logic để thêm một cuốn sách vào cơ sở dữ liệu
    }

    @Override
    public void update(BorrowRecord borrowRecord){

    }



}
