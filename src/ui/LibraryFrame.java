package ui;

import dao.BookDAO;
import dao.ReaderDAO;
import dao.BorrowRecordDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


import static ui.UiStyles.*;

public class LibraryFrame extends JFrame {
    private final JPanel content = new JPanel(new CardLayout());

    // DAO thay cho danh sách tạm
    private final BookDAO bookDAO = new BookDAO();
    private final ReaderDAO readerDAO = new ReaderDAO();
    private final BorrowRecordDAO recordDAO = new BorrowRecordDAO();

    // Refs cho refresh (giữ nguyên)
    private JLabel statBooks, statReaders, statBorrowing;
    private JPanel overdueList, recentList;

    // Refs cho các UI mới
    private BorrowRecordUI borrowRecordUI; // Thêm tham chiếu để gọi refreshTable nếu cần

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

        // Sử dụng BookUI mới
        content.add(new BookUI(this), "books");

        // Sử dụng ReaderUI mới
        content.add(new ReaderUI(this), "readers");

        // Sử dụng BorrowRecordUI mới và lưu tham chiếu
        borrowRecordUI = new BorrowRecordUI(this, recordDAO, bookDAO, readerDAO);
        content.add(borrowRecordUI, "borrow");

        content.add(buildReports(), "reports");
    }

    private void showPage(String name) {
        ((CardLayout) content.getLayout()).show(content, name);
        refreshAll();

        // Gọi refreshTable cho BorrowRecordUI khi mở trang mượn/trả
        if (name.equals("borrow") && borrowRecordUI != null) {
            borrowRecordUI.refreshTable();
        }
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
    protected void refreshAll() {
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

    protected static javax.swing.event.DocumentListener simpleChange(Runnable run) {
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
