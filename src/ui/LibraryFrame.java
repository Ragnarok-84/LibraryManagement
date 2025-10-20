package ui;

import dao.BookDAO;
import dao.ReaderDAO;
import dao.BorrowRecordDAO;

import model.Book;
import model.Reader;
import model.BorrowRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ui.UiStyles.*;

public class LibraryFrame extends JFrame {
    private final JPanel content = new JPanel(new CardLayout());

    // DAO thay cho danh sách tạm
    private final BookDAO bookDAO = new BookDAO();
    private final ReaderDAO readerDAO = new ReaderDAO();
    private final BorrowRecordDAO recordDAO = new BorrowRecordDAO();

    // Refs cho refresh
    private JLabel statBooks, statReaders, statBorrowing;
    private JPanel overdueList, recentList;
    private JTable borrowTable;

    public LibraryFrame() {
        super("📚 Hệ thống quản lý thư viện");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        buildSidebar();
        buildPages();
        showPage("dashboard");
    }

    private void buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(SIDEBAR_BG);
        side.setPreferredSize(new Dimension(220, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("📚 Thư Viện");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        side.add(title);
        side.add(Box.createVerticalStrut(12));

        side.add(navBtn("Trang chủ", () -> showPage("dashboard")));
        side.add(navBtn("Quản lý sách", () -> showPage("books")));
        side.add(navBtn("Quản lý độc giả", () -> showPage("readers")));
        side.add(navBtn("Mượn/Trả sách", () -> showPage("borrow")));
        side.add(navBtn("Báo cáo thống kê", () -> showPage("reports")));

        add(side, BorderLayout.WEST);
    }

    private JComponent navBtn(String text, Runnable onClick) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setFont(BODY);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        b.addActionListener(e -> onClick.run());
        return b;
    }

    private void buildPages() {
        content.setBorder(new EmptyBorder(16, 16, 16, 16));
        add(content, BorderLayout.CENTER);

        content.add(buildDashboard(), "dashboard");
        content.add(buildBooks(), "books");
        content.add(buildReaders(), "readers");
        content.add(buildBorrow(), "borrow");
        content.add(buildReports(), "reports");
    }

    private void showPage(String name) {
        ((CardLayout) content.getLayout()).show(content, name);
        refreshAll();
    }

    // ========== DASHBOARD ==========
    private JPanel buildDashboard() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

        JPanel metrics = new JPanel(new GridLayout(1, 3, 12, 0));
        metrics.setOpaque(false);
        metrics.add(metric("Tổng số sách", statBooks = new JLabel("0"), PRIMARY));
        metrics.add(metric("Độc giả", statReaders = new JLabel("0"), SUCCESS));
        metrics.add(metric("Đang mượn", statBorrowing = new JLabel("0"), INFO));
        root.add(metrics, BorderLayout.NORTH);

        JPanel alert = new JPanel(new BorderLayout());
        alert.setBackground(new Color(255, 243, 244));
        alert.setBorder(new EmptyBorder(12, 12, 12, 12));
        alert.add(new JLabel("⚠️ Cảnh báo sách quá hạn – Kiểm tra và xử lý ngay"), BorderLayout.CENTER);
        root.add(alert, BorderLayout.CENTER);

        JPanel split = new JPanel(new GridLayout(1, 2, 12, 12));
        split.setOpaque(false);

        JPanel overBox = groupBox("Sách quá hạn");
        overdueList = new JPanel();
        overdueList.setLayout(new BoxLayout(overdueList, BoxLayout.Y_AXIS));
        JScrollPane overScroll = new JScrollPane(overdueList);
        overScroll.setBorder(BorderFactory.createEmptyBorder());
        overBox.add(overScroll, BorderLayout.CENTER);
        split.add(overBox);

        JPanel recentBox = groupBox("Mượn sách gần đây");
        recentList = new JPanel();
        recentList.setLayout(new BoxLayout(recentList, BoxLayout.Y_AXIS));
        JScrollPane recentScroll = new JScrollPane(recentList);
        recentScroll.setBorder(BorderFactory.createEmptyBorder());
        recentBox.add(recentScroll, BorderLayout.CENTER);
        split.add(recentBox);

        root.add(split, BorderLayout.SOUTH);
        return root;
    }

    private JPanel metric(String title, JLabel value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        JLabel t = new JLabel(title);
        t.setFont(BODY);
        value.setFont(new Font("Segoe UI", Font.BOLD, 28));
        value.setForeground(accent);
        card.add(t, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel groupBox(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel cap = new JLabel(title);
        cap.setFont(H2);
        p.add(cap, BorderLayout.NORTH);
        return p;
    }

    // ========== BOOKS ==========
    private JPanel buildBooks() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

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
        root.add(top, BorderLayout.NORTH);



        // ======= CẤU HÌNH BẢNG HIỂN THỊ SÁCH =======
        // Gợi ý: thêm cả “Mã sách” (book_id) và “Số trang” (num_pages) cho đầy đủ
        String[] cols = {
                "Mã sách", "Tên sách", "Tác giả", "ISBN",
                "NXB", "Số trang", "Ngày XB", "Tổng", "Khả dụng"
        };

        JTable table = new JTable(new javax.swing.table.DefaultTableModel(cols, 0));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // để tự set kích thước cột

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
        root.add(new JScrollPane(table), BorderLayout.CENTER);

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

        // ======= NÚT THÊM SÁCH =======
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
                    refreshAll();

                    JOptionPane.showMessageDialog(null, "✅ Thêm sách thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Lỗi khi thêm sách: " + ex.getMessage());
                }
            }
        });
        // ======= NÚT XÓA SÁCH =======
        delete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow(); // table là JTable hiển thị danh sách sách
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Vui lòng chọn sách cần xóa!");
                return;
            }

            // Lấy ID sách từ cột đầu tiên (ví dụ cột 0 là book_id)
            int bookId = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());


            int confirm = JOptionPane.showConfirmDialog(
                    null,
                    "Bạn có chắc muốn xóa sách này?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                bookDAO.deleteBook(bookId);
                reload.run(); // cập nhật lại danh sách
                refreshAll();
                JOptionPane.showMessageDialog(null, "Đã xóa sách thành công!");
            }
        });
        // CẬP NHẬT SÁCH
        edit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(root, "Vui lòng chọn sách để cập nhật!");
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
            String newTitle = JOptionPane.showInputDialog(root, "Tên sách:", title);
            if (newTitle == null) return;

            String newAuthor = JOptionPane.showInputDialog(root, "Tác giả:", author);
            if (newAuthor == null) return;

            String newIsbn = JOptionPane.showInputDialog(root, "ISBN:", isbn);
            if (newIsbn == null) return;

            String newPublisher = JOptionPane.showInputDialog(root, "Nhà xuất bản:", publisher);
            if (newPublisher == null) return;

            String newPubDate = JOptionPane.showInputDialog(root, "Ngày xuất bản (yyyy-mm-dd):", pubDate);
            if (newPubDate == null) return;

            int newPages;
            try {
                newPages = Integer.parseInt(JOptionPane.showInputDialog(root, "Số trang:", numPages));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(root, "Số trang không hợp lệ!");
                return;
            }

            int newTotal;
            try {
                newTotal = Integer.parseInt(JOptionPane.showInputDialog(root, "Tổng số lượng:", total));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(root, "Số lượng không hợp lệ!");
                return;
            }

            int newAvailable;
            try {
                newAvailable = Integer.parseInt(JOptionPane.showInputDialog(root, "Sách còn lại:", available));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(root, "Số lượng sách còn không hợp lệ!");
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
            refreshAll();

            JOptionPane.showMessageDialog(root, "✅ Cập nhật sách thành công!");
        });



        reload.run();
        return root;
    }

    // ========== READERS ==========
    private JPanel buildReaders() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

        // ======= THANH TÌM KIẾM + NÚT THÊM =======
        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText",
                "Tìm kiếm theo tên, mã độc giả, điện thoại hoặc email...");

        JButton add = new JButton("+ Thêm độc giả");
        add.setBackground(PRIMARY);
        add.setForeground(Color.WHITE);

        JButton edit = new JButton("✏️ Cập nhật độc giả");
        edit.setBackground(new Color(255, 180, 0));
        edit.setForeground(Color.WHITE);

        JButton delete = new JButton("🗑 Xóa độc giả");
        delete.setBackground(Color.RED);
        delete.setForeground(Color.WHITE);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.add(add);
        btnPanel.add(delete);
        btnPanel.add(edit);

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.add(search, BorderLayout.CENTER);
        top.add(btnPanel, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);


        // ======= CẤU HÌNH CÁC CỘT =======
        String[] cols = {"Mã độc giả", "Tên", "Email", "Điện thoại", "Địa chỉ", "Ngày tham gia", "Trạng thái"};
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(cols, 0));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setAutoCreateRowSorter(true); // cho phép sắp xếp khi click tiêu đề

        // ======= CẤU HÌNH ĐỘ RỘNG CỘT =======
        table.getColumnModel().getColumn(0).setPreferredWidth(90);   // Mã độc giả
        table.getColumnModel().getColumn(1).setPreferredWidth(180);  // Tên
        table.getColumnModel().getColumn(2).setPreferredWidth(200);  // Email
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // Điện thoại
        table.getColumnModel().getColumn(4).setPreferredWidth(250);  // Địa chỉ
        table.getColumnModel().getColumn(5).setPreferredWidth(120);  // Ngày tham gia
        table.getColumnModel().getColumn(6).setPreferredWidth(90);   // Trạng thái (Hoạt động / Ngưng)

        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        // ======= NẠP DỮ LIỆU TỪ DATABASE =======
        Runnable reload = () -> {
            javax.swing.table.DefaultTableModel model =
                    (javax.swing.table.DefaultTableModel) table.getModel();
            model.setRowCount(0);

            List<Reader> readers = readerDAO.searchReaders(search.getText());
            for (Reader r : readers) {
                model.addRow(new Object[]{
                        r.getReaderID(),
                        r.getName(),
                        r.getEmail(),
                        r.getPhone(),
                        r.getAddress(),
                        r.getJoinDate(),
                        r.isActive() ? "Hoạt động" : "Ngưng"
                });
            }
        };
        search.getDocument().addDocumentListener(simpleChange(reload));

        // ======= NÚT THÊM MỚI =======
        add.addActionListener(e -> {
            JTextField nameField = new JTextField();
            JTextField emailField = new JTextField();
            JTextField phoneField = new JTextField();
            JTextField addressField = new JTextField();
            JCheckBox activeCheck = new JCheckBox("Hoạt động", true);

            JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
            panel.add(new JLabel("Tên độc giả:"));
            panel.add(nameField);
            panel.add(new JLabel("Email:"));
            panel.add(emailField);
            panel.add(new JLabel("Số điện thoại:"));
            panel.add(phoneField);
            panel.add(new JLabel("Địa chỉ:"));
            panel.add(addressField);
            panel.add(new JLabel("Trạng thái:"));
            panel.add(activeCheck);

            int result = JOptionPane.showConfirmDialog(
                    null,
                    panel,
                    "Thêm độc giả mới",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                try {
                    Reader r = new Reader();
                    r.setName(nameField.getText().trim());
                    r.setEmail(emailField.getText().trim());
                    r.setPhone(phoneField.getText().trim());
                    r.setAddress(addressField.getText().trim());
                    r.setJoinDate(LocalDate.now());
                    r.setActive(activeCheck.isSelected());

                    if (r.getName().isEmpty()) {
                        throw new Exception("Tên độc giả không được để trống!");
                    }

                    readerDAO.addReader(r);
                    reload.run();
                    refreshAll();
                    JOptionPane.showMessageDialog(null, "✅ Thêm độc giả thành công!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "❌ Lỗi khi thêm độc giả: " + ex.getMessage());
                }
            }
        });
        // ========== XÓA ĐỘC GIẢ ==========
        delete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(root, "Vui lòng chọn độc giả để xóa!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    root,
                    "Bạn có chắc muốn xóa độc giả này?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            // Lấy ID độc giả
            int readerId = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());

            // Xóa trong database
            readerDAO.deleteReader(readerId);

            // Làm mới giao diện
            reload.run();
            refreshAll();

            JOptionPane.showMessageDialog(root, "Đã xóa độc giả thành công!");
        });
        // CẬP NHẬT ĐỘC GIẢ
        edit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(root, "Vui lòng chọn độc giả để cập nhật!");
                return;
            }

            // Lấy dữ liệu hiện tại từ JTable
            int id = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
            String name = table.getValueAt(selectedRow, 1).toString();
            String email = table.getValueAt(selectedRow, 2).toString();
            String phone = table.getValueAt(selectedRow, 3).toString();
            String address = table.getValueAt(selectedRow, 4).toString();
            boolean active = Boolean.parseBoolean(table.getValueAt(selectedRow, 5).toString());

            // Hiển thị hộp thoại cho người dùng chỉnh sửa
            String newName = JOptionPane.showInputDialog(root, "Họ tên:", name);
            if (newName == null) return;

            String newEmail = JOptionPane.showInputDialog(root, "Email:", email);
            if (newEmail == null) return;

            String newPhone = JOptionPane.showInputDialog(root, "Số điện thoại:", phone);
            if (newPhone == null) return;

            String newAddress = JOptionPane.showInputDialog(root, "Địa chỉ:", address);
            if (newAddress == null) return;

            int confirm = JOptionPane.showConfirmDialog(root,
                    "Độc giả đang hoạt động?",
                    "Trạng thái hoạt động",
                    JOptionPane.YES_NO_OPTION);
            boolean newActive = (confirm == JOptionPane.YES_OPTION);

            // Tạo đối tượng Reader mới
            Reader r = new Reader();
            r.setReaderID(id);
            r.setName(newName);
            r.setEmail(newEmail);
            r.setPhone(newPhone);
            r.setAddress(newAddress);
            r.setActive(newActive);

            // Gọi DAO để cập nhật
            readerDAO.updateReader(r);

            reload.run();
            refreshAll();

            JOptionPane.showMessageDialog(root, "✅ Cập nhật độc giả thành công!");
        });




        reload.run();
        return root;
    }


    // ========== BORROW ==========
    private JPanel buildBorrow() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

        // ======== Nút "Tạo phiếu mượn" ========
        JButton create = new JButton("+ Tạo phiếu mượn mới");
        create.setBackground(PRIMARY);
        create.setForeground(Color.WHITE);
        root.add(create, BorderLayout.NORTH);

        // ======== Bảng hiển thị phiếu mượn ========
        BorrowTableModel tableModel = new BorrowTableModel();
        tableModel.setRecords(recordDAO.getAllRecordsSorted()); // lấy theo ID tăng dần
        borrowTable = new JTable(tableModel);
        borrowTable.setFillsViewportHeight(true);
        borrowTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // kiểm soát chiều rộng thủ công

        // Đặt độ rộng cột
        String[] cols = {"Mã phiếu", "Tên độc giả", "Tên sách", "Ngày mượn", "Hạn trả", "Ngày trả"};
        int[] widths = {80, 200, 250, 100, 100, 100};
        for (int i = 0; i < cols.length; i++) {
            borrowTable.getColumnModel().getColumn(i).setHeaderValue(cols[i]);
            borrowTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        root.add(new JScrollPane(borrowTable), BorderLayout.CENTER);

        // ======== Nút "Trả sách" ========
        JButton markReturned = new JButton("Trả sách");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(markReturned);
        root.add(actions, BorderLayout.SOUTH);

        // ======== Sự kiện: Trả sách ========
        markReturned.addActionListener(e -> {
            int row = borrowTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu mượn!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BorrowRecord br = ((BorrowTableModel) borrowTable.getModel()).getRecordAt(row);
            if (br == null) return;

            if (br.getReturnDate() != null) {
                JOptionPane.showMessageDialog(this, "Phiếu này đã được trả!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận trả sách cho phiếu #" + br.getRecordID() + "?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            // Cập nhật DB và refresh bảng
            recordDAO.markReturned(br.getRecordID());
            tableModel.setRecords(recordDAO.getAllRecordsSorted());
            refreshAll();
        });

        // ======== Sự kiện: Mở dialog tạo phiếu ========
        create.addActionListener(e -> {
            BorrowDialog dialog = new BorrowDialog(this, recordDAO, bookDAO, readerDAO);
            dialog.setVisible(true);
            tableModel.setRecords(recordDAO.getAllRecordsSorted());
        });

        return root;
    }



    // ========== REPORTS ==========
    private JPanel buildReports() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        root.add(new JScrollPane(area), BorderLayout.CENTER);

        JButton refresh = new JButton("↻ Làm mới báo cáo");
        refresh.setBackground(PRIMARY);
        refresh.setForeground(Color.WHITE);
        root.add(refresh, BorderLayout.NORTH);

        Runnable reload = () -> {
            StringBuilder sb = new StringBuilder();
            sb.append("📚 Top 5 sách được mượn nhiều:\n");
            recordDAO.getTopBooks(5).forEach((bookTitle, count) ->
                    sb.append(String.format(" • %-50s %d lượt\n", bookTitle, count)));

            sb.append("\n👤 Top 5 độc giả tích cực:\n");
            recordDAO.getTopReaders(5).forEach((readerName, count) ->
                    sb.append(String.format(" • %-50s %d lượt\n", readerName, count)));

            area.setText(sb.toString());
        };

        // Nút làm mới
        refresh.addActionListener(e -> reload.run());

        // Gọi lần đầu khi mở tab
        reload.run();

        return root;
    }


    // ========== REFRESH ==========
    private void refreshAll() {
        // Đếm tổng số sách, độc giả, và số lượt mượn đang hoạt động
        statBooks.setText(String.valueOf(bookDAO.countBooks()));
        statReaders.setText(String.valueOf(readerDAO.countReaders()));
        statBorrowing.setText(String.valueOf(recordDAO.countBorrowing()));

        // Làm mới danh sách quá hạn
        if (overdueList != null) {
            overdueList.removeAll();
            recordDAO.getOverdueRecords().forEach(r -> {
                String bookTitle = bookDAO.getBookTitleById(r.getBookID());
                String readerName = readerDAO.getReaderNameById(r.getReaderID());
                overdueList.add(makeLine(bookTitle, readerName, "Quá hạn"));
            });
            overdueList.revalidate();
            overdueList.repaint();
        }

        // Làm mới danh sách gần đây
        if (recentList != null) {
            recentList.removeAll();
            recordDAO.getRecentRecords(10).forEach(r -> {
                String bookTitle = bookDAO.getBookTitleById(r.getBookID());
                String readerName = readerDAO.getReaderNameById(r.getReaderID());
                recentList.add(makeLine(bookTitle, readerName, r.getStatus()));
            });
            recentList.revalidate();
            recentList.repaint();
        }
    }


    private JComponent makeLine(String title, String sub, String badge) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(BODY);
        JLabel s = new JLabel(sub);
        s.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        s.setForeground(Color.GRAY);
        JLabel b = new JLabel(badge);
        b.setOpaque(true);
        b.setBackground(new Color(238, 242, 255));
        b.setForeground(PRIMARY);
        b.setBorder(new EmptyBorder(2, 8, 2, 8));
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(t, BorderLayout.WEST);
        row.add(b, BorderLayout.EAST);
        p.add(row);
        p.add(s);
        p.setBorder(new EmptyBorder(6, 0, 6, 0));
        return p;
    }

    private static javax.swing.event.DocumentListener simpleChange(Runnable run) {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { run.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { run.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { run.run(); }
        };
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> new LibraryFrame().setVisible(true));
    }
}
