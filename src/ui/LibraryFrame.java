package ui;


import model.Book;
import model.Reader;
import model.BorrowRecord;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


import static ui.UiStyles.*;
public class LibraryFrame extends JFrame {
    private final JPanel content = new JPanel(new CardLayout());


    // In-memory data (you can wire your DAO/Manager later)
    private final List<Book> books = new ArrayList<>();
    private final List<Reader> readers = new ArrayList<>();
    private final List<BorrowRecord> records = new ArrayList<>();


    // Refs for refresh
    private JLabel statBooks, statReaders, statBorrowing;
    private JPanel overdueList, recentList;
    private JTable borrowTable;


    public LibraryFrame() {
        super("Thư Viện – Hệ thống quản lý");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 700));
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());


        seedData();
        buildSidebar();
        buildPages();
        showPage("dashboard");
    }
    private void buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(SIDEBAR_BG);
        side.setPreferredSize(new Dimension(220, 0));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(16,16,16,16));


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
    //dashboard
    private JPanel buildDashboard() {
        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BorderLayout(0, 12));


// metric row
        JPanel metrics = new JPanel(new GridLayout(1,3,12,0));
        metrics.setOpaque(false);
        metrics.add(metric("Tổng số sách", statBooks = new JLabel("0"), PRIMARY));
        metrics.add(metric("Độc giả", statReaders = new JLabel("0"), SUCCESS));
        metrics.add(metric("Đang mượn", statBorrowing = new JLabel("0"), INFO));
        root.add(metrics, BorderLayout.NORTH);
        // alert
        JPanel alert = new JPanel(new BorderLayout());
        alert.setBackground(new Color(255,243,244));
        alert.setBorder(new EmptyBorder(12,12,12,12));
        alert.add(new JLabel("⚠️ Cảnh báo sách quá hạn – Kiểm tra và xử lý ngay"), BorderLayout.CENTER);
        root.add(alert, BorderLayout.CENTER);
        // split under
        JPanel split = new JPanel(new GridLayout(1,2,12,12));
        split.setOpaque(false);
        // overdue
        JPanel overBox = groupBox("Sách quá hạn");
        overdueList = new JPanel();
        overdueList.setLayout(new BoxLayout(overdueList, BoxLayout.Y_AXIS));
        JScrollPane overScroll = new JScrollPane(overdueList);
        overScroll.setBorder(BorderFactory.createEmptyBorder());
        overBox.add(overScroll, BorderLayout.CENTER);
        split.add(overBox);
        // recent
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
        card.setBorder(new EmptyBorder(16,16,16,16));
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
        p.setBorder(new EmptyBorder(12,12,12,12));
        JLabel cap = new JLabel(title);
        cap.setFont(H2);
        p.add(cap, BorderLayout.NORTH);
        return p;
    }
    // ---------------- Books ----------------
    private JPanel buildBooks() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setOpaque(false);
        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", "Tìm kiếm theo tên sách, tác giả hoặc ISBN...");
        JButton add = new JButton("+ Thêm sách mới");
        add.setBackground(PRIMARY); add.setForeground(Color.WHITE);
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.add(search, BorderLayout.CENTER);
        top.add(add, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);


        String[] cols = {"Tên sách", "Tác giả", "ISBN", "NXB", "Tổng", "Khả dụng"};
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(cols, 0));
        table.setFillsViewportHeight(true);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        // events
        Runnable reload = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase();
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) table.getModel();
            model.setRowCount(0);
            books.stream()
                    .filter(b -> q.isEmpty() || b.getTitle().toLowerCase().contains(q) || b.getAuthor().toLowerCase().contains(q) || b.getIsbn().toLowerCase().contains(q))
                    .forEach(b -> model.addRow(new Object[]{b.getTitle(), b.getAuthor(), b.getIsbn(), b.getPublisher(), b.getTotal(), b.getAvailable()}));
        };
        search.getDocument().addDocumentListener(simpleChange(reload));
        add.addActionListener(e -> {
            Book b = new Book();
            b.setTitle("Sách mới"); b.setAuthor("Tác giả"); b.setIsbn(String.valueOf(System.currentTimeMillis()));
            b.setPublisher("NXB"); b.setTotal(1); b.setAvailable(1); b.setCategory("Khác");
            books.add(b); reload.run(); refreshAll();
        });
        reload.run();
        return root;
    }
    // ---------------- Readers ----------------
    private JPanel buildReaders() {
        JPanel root = new JPanel(new BorderLayout(0,12)); root.setOpaque(false);
        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", "Tìm kiếm tên, mã độc giả, điện thoại hoặc email...");
        JButton add = new JButton("+ Thêm độc giả mới"); add.setBackground(PRIMARY); add.setForeground(Color.WHITE);
        JPanel top = new JPanel(new BorderLayout(8,0)); top.add(search, BorderLayout.CENTER); top.add(add, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);


        String[] cols = {"Mã", "Tên", "Điện thoại", "Email", "Địa chỉ", "Trạng thái"};
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(cols, 0));
        root.add(new JScrollPane(table), BorderLayout.CENTER);


        Runnable reload = () -> {
            String q = search.getText() == null ? "" : search.getText().toLowerCase();
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) table.getModel();
            model.setRowCount(0);
            readers.stream()
                    .filter(r -> q.isEmpty() || r.getName().toLowerCase().contains(q) || r.getId().toLowerCase().contains(q) ||
                            (r.getEmail()!=null && r.getEmail().toLowerCase().contains(q)) || (r.getPhone()!=null && r.getPhone().toLowerCase().contains(q)))
                    .forEach(r -> model.addRow(new Object[]{r.getId(), r.getName(), r.getPhone(), r.getEmail(), r.getAddress(), r.getStatus()}));
        };
        search.getDocument().addDocumentListener(simpleChange(reload));
        add.addActionListener(e -> { Reader r = new Reader(); r.setId("DG" + (readers.size()+1)); r.setName("Độc giả mới"); r.setStatus("Hoạt động"); readers.add(r); reload.run(); refreshAll(); });
        reload.run();
        return root;
    }
    // ---------------- Borrow ----------------
    private JPanel buildBorrow() {
        JPanel root = new JPanel(new BorderLayout(0,12)); root.setOpaque(false);
        JButton create = new JButton("+ Tạo phiếu mượn mới"); create.setBackground(PRIMARY); create.setForeground(Color.WHITE);
        root.add(create, BorderLayout.NORTH);


        borrowTable = new JTable(new BorrowTableModel(records));
        borrowTable.setFillsViewportHeight(true);
        root.add(new JScrollPane(borrowTable), BorderLayout.CENTER);


        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton markReturned = new JButton("Trả sách");
        actions.add(markReturned);
        root.add(actions, BorderLayout.SOUTH);


        create.addActionListener(e -> {
            if (books.isEmpty() || readers.isEmpty()) return;
            Book b = books.stream().filter(x -> x.getAvailable() > 0).findFirst().orElse(null);
            if (b == null) return;
            BorrowRecord rec = new BorrowRecord();
            if (readers.isEmpty()) return;
            rec.setReader(readers.get(0));
            rec.setBook(b);
            rec.setBorrowDate(LocalDate.now());
            rec.setDueDate(LocalDate.now().plusDays(14));
            b.setAvailable(b.getAvailable()-1);
            records.add(0, rec);
            ((BorrowTableModel)borrowTable.getModel()).fireTableDataChanged();
            refreshAll();
        });


        markReturned.addActionListener(e -> {
            int row = borrowTable.getSelectedRow();
            if (row < 0) return;
            BorrowRecord br = ((BorrowTableModel) borrowTable.getModel()).getAt(row);
            if (br.getReturnDate() == null) {
                br.setReturnDate(LocalDate.now());
                br.getBook().setAvailable(br.getBook().getAvailable()+1);
                ((BorrowTableModel) borrowTable.getModel()).fireTableRowsUpdated(row, row);
                refreshAll();
            }
        });


        return root;
    }
    // ---------------- Reports (simple placeholders) ----------------
    private JPanel buildReports() {
        JPanel root = new JPanel(new BorderLayout(0,12)); root.setOpaque(false);
        root.add(new JLabel("Báo cáo (demo) – có thể tích hợp JFreeChart hoặc JavaFX Chart nếu cần"), BorderLayout.NORTH);


        JTextArea area = new JTextArea();
        area.setEditable(false);
        root.add(new JScrollPane(area), BorderLayout.CENTER);


// simple text report for now
        Runnable reload = () -> {
            StringBuilder sb = new StringBuilder();
            sb.append("Top sách được mượn nhiều:\n");
            java.util.List<java.util.Map.Entry<String, Long>> topBooks = records.stream()
                    .collect(Collectors.groupingBy(r -> r.getBook().getTitle(), Collectors.counting()))
                    .entrySet().stream().sorted((a,b)->Long.compare(b.getValue(), a.getValue())).limit(5)
                    .collect(Collectors.toList());
            for (java.util.Map.Entry<String, Long> e : topBooks) sb.append(" - ").append(e.getKey()).append(": ").append(e.getValue()).append(" lượt\n");
            sb.append("\nTop độc giả tích cực:\n");
            java.util.List<java.util.Map.Entry<String, Long>> topReaders = records.stream()
                    .collect(Collectors.groupingBy(r -> r.getReader().getName(), Collectors.counting()))
                    .entrySet().stream().sorted((a,b)->Long.compare(b.getValue(), a.getValue())).limit(5)
                    .collect(Collectors.toList());
            for (java.util.Map.Entry<String, Long> e : topReaders) sb.append(" - ").append(e.getKey()).append(": ").append(e.getValue()).append(" lượt\n");
            area.setText(sb.toString());
        };
        reload.run();
        return root;
    }
    // ---------------- Helpers ----------------
    private static javax.swing.event.DocumentListener simpleChange(Runnable run) {
        return new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e){run.run();}
            public void removeUpdate(javax.swing.event.DocumentEvent e){run.run();}
            public void changedUpdate(javax.swing.event.DocumentEvent e){run.run();}
        };
    }
    private void refreshAll() {
// metrics
        statBooks.setText(String.valueOf(books.stream().mapToInt(Book::getTotal).sum()));
        statReaders.setText(String.valueOf(readers.size()));
        statBorrowing.setText(String.valueOf(records.stream().filter(r -> r.getReturnDate() == null).count()));


// overdue
        if (overdueList != null) {
            overdueList.removeAll();
            records.stream()
                    .filter(r -> r.getReturnDate()==null && r.getDueDate()!=null && r.getDueDate().isBefore(LocalDate.now()))
                    .sorted(Comparator.comparing(BorrowRecord::getDueDate).reversed())
                    .forEach(r -> overdueList.add(makeLine(r.getBook().getTitle(), r.getReader().getName(), "Quá hạn")));
            overdueList.revalidate(); overdueList.repaint();
        }
// recent
        if (recentList != null) {
            recentList.removeAll();
            records.stream().sorted(Comparator.comparing(BorrowRecord::getBorrowDate).reversed()).limit(10)
                    .forEach(r -> recentList.add(makeLine(r.getBook().getTitle(), r.getReader().getName(), r.getStatus())));
            recentList.revalidate(); recentList.repaint();
        }
    }
    private JComponent makeLine(String title, String sub, String badge) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel t = new JLabel(title); t.setFont(BODY);
        JLabel s = new JLabel(sub); s.setFont(new Font("Segoe UI", Font.ITALIC, 12)); s.setForeground(Color.GRAY);
        JLabel b = new JLabel(badge); b.setOpaque(true); b.setBackground(new Color(238,242,255)); b.setForeground(PRIMARY); b.setBorder(new EmptyBorder(2,8,2,8));
        JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false);
        row.add(t, BorderLayout.WEST); row.add(b, BorderLayout.EAST);
        p.add(row); p.add(s); p.setBorder(new EmptyBorder(6,0,6,0));
        return p;
    }
    private void seedData() {
// Books
        Book b1 = new Book(); b1.setTitle("Đắc Nhân Tâm"); b1.setAuthor("Dale Carnegie"); b1.setIsbn("978-604-2-14696-5"); b1.setPublisher("NXB Tổng hợp TPHCM"); b1.setTotal(10); b1.setAvailable(7); b1.setCategory("Kỹ năng sống");
        Book b2 = new Book(); b2.setTitle("Sapiens: Lược Sử Loài Người"); b2.setAuthor("Yuval Noah Harari"); b2.setIsbn("978-604-2-26153-8"); b2.setPublisher("NXB Thế Giới"); b2.setTotal(8); b2.setAvailable(5); b2.setCategory("Lịch sử");
        Book b3 = new Book(); b3.setTitle("Lập Trình Python Cho Người Mới Bắt Đầu"); b3.setAuthor("Nguyễn Văn A"); b3.setIsbn("978-604-2-33445-2"); b3.setPublisher("NXB Khoa học Kỹ thuật"); b3.setTotal(15); b3.setAvailable(12); b3.setCategory("Công nghệ");
        books.add(b1); books.add(b2); books.add(b3);


// Readers
        Reader r1 = new Reader(); r1.setId("DG001"); r1.setName("Nguyễn Văn An"); r1.setPhone("0901234567"); r1.setEmail("nguyenvanan@email.com"); r1.setAddress("123 Lê Lợi, Q1, TP.HCM"); r1.setStatus("Hoạt động");
        Reader r2 = new Reader(); r2.setId("DG002"); r2.setName("Trần Thị Bình"); r2.setPhone("0912345678"); r2.setEmail("tranthibinh@email.com"); r2.setAddress("456 Nguyễn Huệ, Q3, TP.HCM"); r2.setStatus("Hoạt động");
        Reader r3 = new Reader(); r3.setId("DG003"); r3.setName("Lê Văn Cường"); r3.setPhone("0923456789"); r3.setEmail("levancuong@email.com"); r3.setAddress("789 Trần Hưng Đạo, Q5, TP.HCM"); r3.setStatus("Hoạt động");
        readers.add(r1); readers.add(r2); readers.add(r3);


// Records
        BorrowRecord p1 = new BorrowRecord(); p1.setReader(r1); p1.setBook(b1); p1.setBorrowDate(LocalDate.of(2024,12,1)); p1.setDueDate(LocalDate.of(2024,12,15));
        BorrowRecord p2 = new BorrowRecord(); p2.setReader(r2); p2.setBook(b2); p2.setBorrowDate(LocalDate.of(2024,11,20)); p2.setDueDate(LocalDate.of(2024,12,4));
        BorrowRecord p3 = new BorrowRecord(); p3.setReader(r3); p3.setBook(b3); p3.setBorrowDate(LocalDate.of(2024,11,15)); p3.setDueDate(LocalDate.of(2024,11,29)); p3.setReturnDate(LocalDate.of(2024,11,28));
        records.add(p1); records.add(p2); records.add(p3);
    }
    public static void launch() {
        SwingUtilities.invokeLater(() -> new LibraryFrame().setVisible(true));
    }
}