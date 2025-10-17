package dao;

import model.BorrowRecord;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAO {

    // === Ánh xạ dòng sang đối tượng BorrowRecord ===
    private BorrowRecord mapRowToRecord(ResultSet rs) throws SQLException {
        BorrowRecord br = new BorrowRecord();
        br.setRecordID(rs.getInt("recordID"));
        br.setReaderID(rs.getInt("readerID"));
        br.setBookID(rs.getInt("bookID"));
        br.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
        br.setDueDate(rs.getDate("due_date").toLocalDate());
        Date ret = rs.getDate("return_date");
        if (ret != null) br.setReturnDate(ret.toLocalDate());
        br.setStatus(rs.getString("status"));
        return br;
    }

    // ===============================================================
    // 1️⃣ CREATE: Thêm bản ghi mượn sách
    // ===============================================================
    public void addBorrowRecord(BorrowRecord record) {
        final String SQL = "INSERT INTO borrow_records (readerID, bookID, borrow_date, due_date, return_date, status) VALUES (?, ?, ?, ?, ?, ?)";

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
            System.out.println("✅ Thêm bản ghi mượn thành công!");
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi thêm bản ghi mượn: " + e.getMessage());
        }
    }

    // ===============================================================
    // 2️⃣ READ: Lấy danh sách tất cả bản ghi mượn
    // ===============================================================
    public List<BorrowRecord> getAllRecords() {
        List<BorrowRecord> list = new ArrayList<>();
        final String SQL = "SELECT * FROM borrow_records ORDER BY borrow_date DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                list.add(mapRowToRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi đọc danh sách mượn: " + e.getMessage());
        }
        return list;
    }

    // ===============================================================
    // 3️⃣ UPDATE: Cập nhật trả sách (hoặc trạng thái)
    // ===============================================================
    public void updateReturnStatus(int recordID, LocalDate returnDate, String status) {
        final String SQL = "UPDATE borrow_records SET return_date=?, status=? WHERE recordID=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setDate(1, returnDate != null ? Date.valueOf(returnDate) : null);
            stmt.setString(2, status);
            stmt.setInt(3, recordID);
            stmt.executeUpdate();

            System.out.println("✅ Cập nhật trạng thái mượn/trả thành công!");
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi cập nhật trạng thái mượn: " + e.getMessage());
        }
    }

    // ===============================================================
    // 4️⃣ READ: Lấy lịch sử mượn của một độc giả
    // ===============================================================
    public List<BorrowRecord> getRecordsByReader(int readerID) {
        List<BorrowRecord> list = new ArrayList<>();
        final String SQL = "SELECT * FROM borrow_records WHERE readerID=? ORDER BY borrow_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, readerID);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRecord(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi lấy lịch sử mượn: " + e.getMessage());
        }
        return list;
    }

    // ===============================================================
    // 5️⃣ DELETE: Xóa bản ghi mượn
    // ===============================================================
    public void deleteRecord(int recordID) {
        final String SQL = "DELETE FROM borrow_records WHERE recordID=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, recordID);
            stmt.executeUpdate();
            System.out.println("✅ Xóa bản ghi mượn thành công!");
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi xóa bản ghi mượn: " + e.getMessage());
        }
    }
}
