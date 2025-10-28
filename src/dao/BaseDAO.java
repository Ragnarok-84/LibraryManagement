package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class BaseDAO<T> implements DAO<T> {

    // ===== ⚡ Cung cấp kết nối DB dùng chung =====
    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }

    // ===== 🧭 Các phương thức abstract để lớp con override =====
    protected abstract T mapRowToEntity(ResultSet rs) throws SQLException;

    protected abstract String getTableName();
    protected abstract String getIdColumnName();

    // ===== ✅ Xóa bản ghi theo ID =====
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

    // ===== ⚡ findAll() sẽ được override tùy từng bảng, nhưng có thể implement mặc định =====
    @Override
    public List<T> findAll() {
        throw new UnsupportedOperationException("Lớp con phải override findAll()!");
    }

    // ===== ⚡ findById() mặc định (nếu cần) =====
    @Override
    public Optional<T> findByID(int id) {
        throw new UnsupportedOperationException("Lớp con phải override findById()!");
    }
}
