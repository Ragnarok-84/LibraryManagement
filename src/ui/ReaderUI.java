package ui;

import dao.ReaderDAO;
import model.Reader;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

import static ui.UiStyles.*;
import static ui.LibraryFrame.simpleChange;

public class ReaderUI extends JPanel {
    private final ReaderDAO readerDAO = new ReaderDAO();
    private final LibraryFrame parentFrame;

    public ReaderUI(LibraryFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        buildReaderPanel();
    }

    private void buildReaderPanel() {
        // Lấy toàn bộ logic từ buildReaders() của LibraryFrame cũ

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
        add(top, BorderLayout.NORTH);


        // ======= CẤU HÌNH CÁC CỘT =======
        String[] cols = {"Mã độc giả", "Tên", "Email", "Điện thoại", "Địa chỉ", "Ngày tham gia", "Trạng thái"};
        JTable table = new JTable(new javax.swing.table.DefaultTableModel(cols, 0));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setAutoCreateRowSorter(true);

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
        add(new JScrollPane(table), BorderLayout.CENTER);

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
                    parentFrame.refreshAll(); // Gọi refreshAll()
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
                JOptionPane.showMessageDialog(this, "Vui lòng chọn độc giả để xóa!");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
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
            parentFrame.refreshAll(); // Gọi refreshAll()

            JOptionPane.showMessageDialog(this, "Đã xóa độc giả thành công!");
        });
        // CẬP NHẬT ĐỘC GIẢ
        edit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn độc giả để cập nhật!");
                return;
            }

            // Lấy dữ liệu hiện tại từ JTable
            int id = Integer.parseInt(table.getValueAt(selectedRow, 0).toString());
            String name = table.getValueAt(selectedRow, 1).toString();
            String email = table.getValueAt(selectedRow, 2).toString();
            String phone = table.getValueAt(selectedRow, 3).toString();
            String address = table.getValueAt(selectedRow, 4).toString();
            // Lấy trạng thái hiện tại (cột 6) và chuyển về boolean
            boolean isActive = table.getValueAt(selectedRow, 6).toString().equals("Hoạt động");


            // Hiển thị hộp thoại cho người dùng chỉnh sửa
            String newName = JOptionPane.showInputDialog(this, "Họ tên:", name);
            if (newName == null) return;

            String newEmail = JOptionPane.showInputDialog(this, "Email:", email);
            if (newEmail == null) return;

            String newPhone = JOptionPane.showInputDialog(this, "Số điện thoại:", phone);
            if (newPhone == null) return;

            String newAddress = JOptionPane.showInputDialog(this, "Địa chỉ:", address);
            if (newAddress == null) return;

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Độc giả đang hoạt động?",
                    "Trạng thái hoạt động",
                    isActive ? JOptionPane.YES_NO_OPTION : JOptionPane.NO_OPTION, // Mặc định trạng thái hiện tại
                    JOptionPane.QUESTION_MESSAGE);
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
            parentFrame.refreshAll(); // Gọi refreshAll()

            JOptionPane.showMessageDialog(this, "✅ Cập nhật độc giả thành công!");
        });


        reload.run(); // Nạp dữ liệu lần đầu
    }
}