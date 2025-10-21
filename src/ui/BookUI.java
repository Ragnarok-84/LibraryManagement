package ui;

import dao.BookDAO;
import model.Book;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

import static ui.UiStyles.*;
import static ui.LibraryFrame.simpleChange; // Thêm import cho simpleChange

public class BookUI extends JPanel { // Thay đổi từ JFrame sang JPanel
    private final BookDAO bookDAO = new BookDAO();
    private final LibraryFrame parentFrame; // Thêm tham chiếu đến LibraryFrame

    public BookUI(LibraryFrame parentFrame) { // Nhận LibraryFrame qua constructor
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0)); // Bỏ border ở đây vì LibraryFrame đã có

        // Gọi phương thức xây dựng giao diện từ LibraryFrame cũ
        // Chuyển logic từ buildBooks() của LibraryFrame cũ vào đây
        buildBookPanel();
    }

    private void buildBookPanel() {
        // ======= THANH TÌM KIẾM + NÚT THÊM =======
        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", "Tìm kiếm theo tên sách, tác giả hoặc ISBN...");

        JButton add = new JButton("+ Thêm sách mới");
        add.setBackground(PRIMARY);
        add.setForeground(Color.WHITE);

        JButton edit = new JButton("✏️ Cập nhật sách");
        edit.setBackground(new Color(255, 180, 0));
        edit.setForeground(Color.WHITE);

        JButton delete = new JButton("🗑 Xóa sách");
        delete.setBackground(Color.RED);
        delete.setForeground(Color.WHITE);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.add(add);
        btnPanel.add(delete);
        btnPanel.add(edit);

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.add(search, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ======= CẤU HÌNH BẢNG HIỂN THỊ SÁCH =======
        String[] cols = {
                "Mã sách", "Tên sách", "Tác giả", "ISBN",
                "NXB", "Số trang", "Ngày XB", "Tổng", "Khả dụng"
        };

        JTable table = new JTable(new javax.swing.table.DefaultTableModel(cols, 0));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // ======= ĐIỀU CHỈNH CHIỀU RỘNG CỘT =======
        table.getColumnModel().getColumn(0).setPreferredWidth(70);   // Mã sách
        table.getColumnModel().getColumn(1).setPreferredWidth(280);  // Tên sách
        table.getColumnModel().getColumn(2).setPreferredWidth(180);  // Tác giả
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // ISBN
        table.getColumnModel().getColumn(4).setPreferredWidth(160);  // Nhà xuất bản
        table.getColumnModel().getColumn(5).setPreferredWidth(90);   // Số trang
        table.getColumnModel().getColumn(6).setPreferredWidth(110);  // Ngày xuất bản
        table.getColumnModel().getColumn(7).setPreferredWidth(70);   // Tổng
        table.getColumnModel().getColumn(8).setPreferredWidth(80);   // Khả dụng

        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ======= NẠP DỮ LIỆU TỪ DATABASE =======
        Runnable reload = () -> {
            javax.swing.table.DefaultTableModel model =
                    (javax.swing.table.DefaultTableModel) table.getModel();
            model.setRowCount(0);

            List<Book> books = search.getText().isEmpty()
                    ? bookDAO.getAllBooks()
                    : bookDAO.searchBooks(search.getText());

            for (Book b : books) {
                model.addRow(new Object[]{
                        b.getBookID(),
                        b.getTitle(),
                        b.getAuthor(),
                        b.getIsbn(),
                        b.getPublisher(),
                        b.getNumPages(),
                        b.getPublicationDate(),
                        b.getTotal(),
                        b.getAvailable()
                });
            }
        };
        search.getDocument().addDocumentListener(simpleChange(reload));

        // ======= NÚT THÊM SÁCH (Giữ nguyên logic) =======
        add.addActionListener(e -> {
            JTextField titleField = new JTextField();
            JTextField authorField = new JTextField();
            JTextField isbnField = new JTextField(String.valueOf(System.currentTimeMillis()).substring(0, 10));
            JTextField publisherField = new JTextField();
            JTextField pagesField = new JTextField();
            JTextField dateField = new JTextField("2025-01-01");
            JTextField totalField = new JTextField("1");

            JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
            panel.add(new JLabel("Tên sách:"));
            panel.add(titleField);
            panel.add(new JLabel("Tác giả:"));
            panel.add(authorField);
            panel.add(new JLabel("ISBN:"));
            panel.add(isbnField);
            panel.add(new JLabel("Nhà xuất bản:"));
            panel.add(publisherField);
            panel.add(new JLabel("Số trang:"));
            panel.add(pagesField);
            panel.add(new JLabel("Ngày xuất bản (yyyy-MM-dd):"));
            panel.add(dateField);
            panel.add(new JLabel("Tổng số lượng:"));
            panel.add(totalField);

            int result = JOptionPane.showConfirmDialog(
                    null,
                    panel,
                    "Thêm sách mới",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                try {
                    Book b = new Book();
                    b.setTitle(titleField.getText());
                    b.setAuthor(authorField.getText());
                    b.setIsbn(isbnField.getText());
                    b.setPublisher(publisherField.getText());
                    b.setNumPages(Integer.parseInt(pagesField.getText()));
                    b.setPublicationDate(LocalDate.parse(dateField.getText()));
                    int total = Integer.parseInt(totalField.getText());
                    b.setTotal(total);
                    b.setAvailable(total);

                    bookDAO.addBook(b);
                    reload.run();
                    parentFrame.refreshAll(); // Gọi refreshAll()
                    JOptionPane.showMessageDialog(null, "✅ Thêm sách thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Lỗi khi thêm sách: " + ex.getMessage());
                }
            }
        });

        // ======= NÚT XÓA SÁCH (Giữ nguyên logic) =======
        delete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sách cần xóa!");
                return;
            }

            int bookId = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn xóa sách này?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                bookDAO.deleteBook(bookId);
                reload.run();
                parentFrame.refreshAll(); // Gọi refreshAll()
                JOptionPane.showMessageDialog(this, "Đã xóa sách thành công!");
            }
        });

        // CẬP NHẬT SÁCH (Giữ nguyên logic)
        edit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để cập nhật!");
                return;
            }

            // Lấy dữ liệu hiện tại từ JTable (theo đúng thứ tự cột)
            int id = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
            String title = table.getValueAt(selectedRow, 1).toString();
            String author = table.getValueAt(selectedRow, 2).toString();
            String isbn = table.getValueAt(selectedRow, 3).toString();
            String publisher = table.getValueAt(selectedRow, 4).toString();
            int numPages = Integer.parseInt(table.getValueAt(selectedRow, 5).toString());
            String pubDate = table.getValueAt(selectedRow, 6).toString();
            int total = Integer.parseInt(table.getValueAt(selectedRow, 7).toString());
            int available = Integer.parseInt(table.getValueAt(selectedRow, 8).toString());


            // Hiển thị hộp thoại cho người dùng chỉnh sửa
            String newTitle = JOptionPane.showInputDialog(this, "Tên sách:", title);
            if (newTitle == null) return;

            String newAuthor = JOptionPane.showInputDialog(this, "Tác giả:", author);
            if (newAuthor == null) return;

            String newIsbn = JOptionPane.showInputDialog(this, "ISBN:", isbn);
            if (newIsbn == null) return;

            String newPublisher = JOptionPane.showInputDialog(this, "Nhà xuất bản:", publisher);
            if (newPublisher == null) return;

            String newPubDate = JOptionPane.showInputDialog(this, "Ngày xuất bản (yyyy-mm-dd):", pubDate);
            if (newPubDate == null) return;

            int newPages;
            try {
                newPages = Integer.parseInt(JOptionPane.showInputDialog(this, "Số trang:", numPages));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Số trang không hợp lệ!");
                return;
            }

            int newTotal;
            try {
                newTotal = Integer.parseInt(JOptionPane.showInputDialog(this, "Tổng số lượng:", total));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ!");
                return;
            }

            int newAvailable;
            try {
                newAvailable = Integer.parseInt(JOptionPane.showInputDialog(this, "Sách còn lại:", available));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Số lượng sách còn không hợp lệ!");
                return;
            }

            // Tạo đối tượng Book mới
            Book b = new Book();
            b.setBookID(id);
            b.setTitle(newTitle);
            b.setAuthor(newAuthor);
            b.setIsbn(newIsbn);
            b.setPublisher(newPublisher);
            b.setPublicationDate(LocalDate.parse(newPubDate));
            b.setNumPages(newPages);
            b.setTotal(newTotal);
            b.setAvailable(newAvailable);

            // Gọi DAO để cập nhật
            bookDAO.updateBook(b);

            reload.run();
            parentFrame.refreshAll(); // Gọi refreshAll()

            JOptionPane.showMessageDialog(this, "✅ Cập nhật sách thành công!");
        });


        reload.run(); // Nạp dữ liệu lần đầu
    }
}