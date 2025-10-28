package dao;

import model.Book;
import java.sql.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class BookDAO extends BaseDAO<Book> {

    @Override
    protected Book mapRowToEntity(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookID(rs.getInt("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("authors"));
        book.setAverageRating(rs.getDouble("average_rating"));
        book.setIsbn(rs.getString("isbn"));
        book.setLanguageCode(rs.getString("language_code"));
        book.setNumPages(rs.getInt("num_pages"));
        String dateStr = rs.getString("publication_date");
        book.setPublisher(rs.getString("publisher"));
        //book.setTotal(rs.getInt("total"));
        //book.setAvailable(rs.getInt("available"));
        //book.setBorrowedCount(book.getTotal() - book.getAvailable());
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            book.setPublicationDate(LocalDate.parse(dateStr));
        } else {
            book.setPublicationDate(null);
        }


        return book;
    }



    // =========================================================================
    // === 1️⃣ CREATE: Thêm sách mới vào Database ===
    // =========================================================================
    public void addBook(Book book) throws SQLException {
        // CÁC CỘT:    (1-title, 2-authors, 3-average_rating, 4-isbn, 5-language_code, 6-num_pages, 7-publication_date, 8-publisher, 9-total, 10-available)
        final String SQL = "INSERT INTO books (title, authors, average_rating, isbn, language_code, num_pages, publication_date, publisher, total, available) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            // GÁN GIÁ TRỊ THEO ĐÚNG THỨ TỰ TRÊN
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setDouble(3, book.getAverageRating()); // Giao diện chưa có, sẽ mặc định là 0.0
            stmt.setString(4, book.getIsbn());
            stmt.setString(5, book.getLanguageCode() != null ? book.getLanguageCode() : "vi"); // Giao diện chưa có, mặc định 'vi'
            stmt.setInt(6, book.getNumPages());
            stmt.setDate(7, book.getPublicationDate() != null ? Date.valueOf(book.getPublicationDate()) : null);
            stmt.setString(8, book.getPublisher());
            stmt.setInt(9, book.getTotal());
            stmt.setInt(10, book.getAvailable());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("✅ Thêm sách thành công (ghi vào MySQL).");
            }
        }
        // Khi có lỗi SQLException, nó sẽ tự động được ném ra cho BookUI bắt
    }


    // =========================================================================
    // === 2️⃣ READ: Lấy toàn bộ danh sách sách từ Database ===
    // =========================================================================
    // Trong BookDAO.java

    public List<Book> getAllBooks() {
        final String SQL = "SELECT book_id, title, authors, average_rating,isbn, language_code, num_pages,  publication_date, publisher FROM books ORDER BY title ASC";

        List<Book> books = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                books.add(mapRowToEntity(rs));
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
        final String SQL = "SELECT book_id, title, authors, average_rating,isbn, language_code, num_pages,  publication_date, publisher, total, available FROM books WHERE isbn = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEntity(rs);
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
        String sql = "UPDATE books SET title = ?, authors = ?, isbn = ?, publisher = ?, num_pages = ?, publication_date = ?, total = ?, available = ? WHERE book_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getIsbn());
            ps.setString(4, book.getPublisher());
            ps.setInt(5, book.getNumPages());
            ps.setDate(6, Date.valueOf(book.getPublicationDate()));
            ps.setInt(7, book.getTotal());
            ps.setInt(8, book.getAvailable());
            ps.setInt(9, book.getBookID());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
        final String SQL = "SELECT book_id, isbn, title, authors, publisher, total, available, language_code, num_pages, average_rating, publication_date FROM books " +
                "WHERE LOWER(title) LIKE ? OR LOWER(authors) LIKE ? OR LOWER(isbn) LIKE ? ORDER BY title ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            // Bind giá trị cho cả 3 dấu ? (vì chúng đều dùng cùng một chuỗi tìm kiếm)
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Sử dụng hàm ánh xạ đã có
                    books.add(mapRowToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi tìm kiếm sách: " + e.getMessage());
        }
        return books;
    }


    public String getBookTitleById (int bookID) {
        final String SQL = "SELECT title FROM books WHERE book_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setInt(1, bookID );

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("title");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi lấy tên sách: " + e.getMessage());
        }

        return "(Không rõ)"; // trả về mặc định nếu không tìm thấy
    }

    public void deleteBook(int bookId) throws SQLException {
        // 1. Khai báo các câu lệnh SQL
        String deleteBorrowSql = "DELETE FROM borrow_records WHERE book_id = ?";
        String deleteBookSql = "DELETE FROM books WHERE book_id = ?";

        Connection conn = null; // Khai báo kết nối ở ngoài để quản lý transaction

        try {
            // 2. Lấy kết nối và TẮT chế độ auto-commit
            conn = getConnection();
            if (conn == null) {
                throw new SQLException("Không thể kết nối đến database.");
            }
            conn.setAutoCommit(false); // <-- BẮT ĐẦU TRANSACTION

            // 3. Xóa các bản ghi mượn sách liên quan TRƯỚC
            try (PreparedStatement stmtBorrow = conn.prepareStatement(deleteBorrowSql)) {
                stmtBorrow.setInt(1, bookId);
                stmtBorrow.executeUpdate();
                // Bạn có thể in ra console để theo dõi (không bắt buộc)
                // System.out.println("Đã xóa các bản ghi mượn sách của book_id: " + bookId);
            }

            // 4. Xóa sách
            try (PreparedStatement stmtBook = conn.prepareStatement(deleteBookSql)) {
                stmtBook.setInt(1, bookId);
                int affectedRows = stmtBook.executeUpdate();
                if (affectedRows == 0) {
                    // Nếu không xóa được sách nào (ví dụ: book_id không tồn tại)
                    // ta cũng nên rollback
                    throw new SQLException("Xóa sách thất bại, không tìm thấy book_id = " + bookId);
                }
            }

            // 5. Nếu cả hai lệnh trên thành công, LƯU (commit) transaction
            conn.commit();
            // System.out.println("Đã xóa sách và các bản ghi mượn liên quan thành công.");

        } catch (SQLException e) {
            // 6. Nếu có BẤT KỲ lỗi nào xảy ra, HỦY BỎ (rollback) toàn bộ thay đổi
            System.err.println("Lỗi SQL, đang rollback... " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // <-- HỦY BỎ TRANSACTION
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi rollback: " + ex.getMessage());
                }
            }
            // Ném lại lỗi để BookUI có thể bắt và hiển thị cho người dùng
            throw e;

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

    public int countBooks() {
        String sql = "SELECT COUNT(*) FROM books";
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
            System.err.println("Lỗi khi đếm số sách: " + e.getMessage());
            // Trả về 0 nếu có lỗi xảy ra
            count = 0;
        }
        return count;
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
    public List<Book> findAll() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY average_rating DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy danh sách sách: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Optional<Book> findByID(int id) {
        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Book book = mapRowToEntity(rs);
                    return Optional.of(book); // ✅ tìm thấy, trả về Optional chứa Book
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty(); // ❌ không tìm thấy
    }

    @Override
    protected String getTableName() {
        return "books";
    }

    @Override
    protected String getIdColumnName() {
        return "book_id";
    }

    @Override
    public void add(Book book) {
        // Câu lệnh SQL với các tham số '?' để chèn dữ liệu
        final String SQL = "INSERT INTO books (title, authors, average_rating, isbn, language_code, num_pages, publication_date, publisher, total, available) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); // Lấy kết nối từ lớp BaseDAO
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            // Gán giá trị từ đối tượng book vào các tham số của câu lệnh SQL
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getPublisher());
            stmt.setInt(5, book.getTotal());
            stmt.setInt(6, book.getAvailable());
            stmt.setString(7, book.getLanguageCode() != null ? book.getLanguageCode() : "vi");
            stmt.setInt(8, book.getNumPages());
            stmt.setDouble(9, book.getAverageRating());
            stmt.setDate(10, book.getPublicationDate() != null ? Date.valueOf(book.getPublicationDate()) : null);

            // Chuyển đổi từ LocalDate (trong model) sang java.sql.Date (cho JDBC)
            if (book.getPublicationDate() != null) {
                stmt.setDate(15, java.sql.Date.valueOf(book.getPublicationDate()));
            } else {
                stmt.setNull(15, java.sql.Types.DATE);
            }

            // Thực thi câu lệnh chèn
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi thêm sách: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void update(Book book) {
        // Câu lệnh SQL để cập nhật một bản ghi dựa vào book_id
        final String SQL = "UPDATE books SET isbn = ?, title = ?, authors = ?, publisher = ?, category = ?, total = ?, available = ?, language_code = ?, num_pages = ?, " +
                "average_rating = ?, publication_date = ? " +
                "WHERE book_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            // Gán các giá trị cần cập nhật
            stmt.setString(1, book.getIsbn());
            stmt.setString(2, book.getTitle());
            stmt.setString(3, book.getAuthor());
            stmt.setString(4, book.getPublisher());
            stmt.setInt(5, book.getTotal());
            stmt.setInt(6, book.getAvailable());
            stmt.setString(7, book.getLanguageCode() != null ? book.getLanguageCode() : "vi");
            stmt.setInt(8, book.getNumPages());
            stmt.setDouble(9, book.getAverageRating());
            stmt.setDate(10, book.getPublicationDate() != null ? Date.valueOf(book.getPublicationDate()) : null);

            // Thực thi câu lệnh cập nhật
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("⚠️ Cảnh báo: Không tìm thấy sách với ID = " + book.getBookID() + " để cập nhật.");
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL khi cập nhật sách: " + e.getMessage());
            e.printStackTrace();
        }
    }


}