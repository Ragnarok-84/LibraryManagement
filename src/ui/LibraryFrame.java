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

        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", "Tìm kiếm theo tên sách, tác giả hoặc ISBN...");
        JButton add = new JButton("+ Thêm sách mới");
        add.setBackground(PRIMARY);
        add.setForeground(Color.WHITE);
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.add(search, BorderLayout.CENTER);
        top.add(add, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Tên sách", "Tác giả", "ISBN", "NXB", "Tổng", "Khả dụng"};
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(cols, 0));
        table.setFillsViewportHeight(true);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable reload = () -> {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) table.getModel();
            model.setRowCount(0);
            List<Book> books = search.getText().isEmpty()
                    ? bookDAO.getAllBooks()
                    : bookDAO.searchBooks(search.getText());
            for (Book b : books)
                model.addRow(new Object[]{b.getTitle(), b.getAuthor(), b.getIsbn(), b.getPublisher(), b.getTotal(), b.getAvailable()});
        };
        search.getDocument().addDocumentListener(simpleChange(reload));

        add.addActionListener(e -> {
            Book b = new Book();
            b.setTitle("Sách mới");
            b.setAuthor("Tác giả");
            b.setIsbn(String.valueOf(System.currentTimeMillis()).substring(0, 10));
            b.setPublisher("NXB");
            b.setTotal(1);
            b.setAvailable(1);
            b.setCategory("Khác");
            bookDAO.addBook(b);
            reload.run();
            refreshAll();
        });

        reload.run();
        return root;
    }

    // ========== READERS ==========
    private JPanel buildReaders() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", "Tìm kiếm tên, mã độc giả, điện thoại hoặc email...");
        JButton add = new JButton("+ Thêm độc giả mới");
        add.setBackground(PRIMARY);
        add.setForeground(Color.WHITE);
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.add(search, BorderLayout.CENTER);
        top.add(add, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Tên", "Điện thoại", "Email", "Địa chỉ", "Trạng thái"};
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(cols, 0));
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        Runnable reload = () -> {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) table.getModel();
            model.setRowCount(0);
            List<Reader> readers = readerDAO.searchReaders(search.getText());
            for (Reader r : readers)
                model.addRow(new Object[]{ r.getReaderID(),r.getName(), r.getPhone(), r.getEmail(), r.getAddress(), r.getJoinDate(), true});
        };
        search.getDocument().addDocumentListener(simpleChange(reload));

        add.addActionListener(e -> {
            Reader r = new Reader();
            r.setName("Độc giả mới");
            r.setEmail("example@email.com");
            r.setPhone("0123456789");
            r.setAddress("New York");
            r.setJoinDate(LocalDate.of(2025, 10, 10));
            r.setActive(true);

            readerDAO.addReader(r);
            reload.run();
            refreshAll();
        });

        reload.run();
        return root;
    }

    // ========== BORROW ==========
    private JPanel buildBorrow() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);

        // Nút tạo phiếu mượn
        JButton create = new JButton("+ Tạo phiếu mượn mới");
        create.setBackground(PRIMARY);
        create.setForeground(Color.WHITE);
        root.add(create, BorderLayout.NORTH);

        // Bảng hiển thị danh sách phiếu mượn
        BorrowTableModel tableModel = new BorrowTableModel();
        tableModel.setRecords(recordDAO.getAllRecords());
        borrowTable = new JTable(tableModel);
        borrowTable.setFillsViewportHeight(true);
        root.add(new JScrollPane(borrowTable), BorderLayout.CENTER);

        // Nút "Trả sách"
        JButton markReturned = new JButton("Trả sách");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(markReturned);
        root.add(actions, BorderLayout.SOUTH);

        // Sự kiện khi nhấn "Trả sách"
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

            // Cập nhật trạng thái
            recordDAO.markReturned(br.getRecordID()); // đảm bảo BorrowRecord có getId()
            ((BorrowTableModel) borrowTable.getModel()).setRecords(recordDAO.getAllRecords());
            refreshAll();
        });

        // Sự kiện mở BorrowDialog
        create.addActionListener(e -> {
            BorrowDialog dialog = new BorrowDialog(this, recordDAO, bookDAO, readerDAO);
            dialog.setVisible(true);
            // Sau khi đóng dialog, refresh lại bảng
            ((BorrowTableModel) borrowTable.getModel()).setRecords(recordDAO.getAllRecords());
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
