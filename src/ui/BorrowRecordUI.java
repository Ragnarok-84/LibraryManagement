package ui;

import dao.BookDAO;
import dao.BorrowRecordDAO;
import dao.ReaderDAO;
import model.BorrowRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static ui.UiStyles.*;

public class BorrowRecordUI extends JPanel {
    // Cần các DAO để tương tác DB và LibraryFrame để gọi refreshAll
    private final BorrowRecordDAO recordDAO;
    private final BookDAO bookDAO;
    private final ReaderDAO readerDAO;
    private final LibraryFrame parentFrame;

    private final BorrowTableModel tableModel = new BorrowTableModel();
    private JTable borrowTable;

    public BorrowRecordUI(LibraryFrame parentFrame, BorrowRecordDAO recordDAO, BookDAO bookDAO, ReaderDAO readerDAO) {
        this.parentFrame = parentFrame;
        this.recordDAO = recordDAO;
        this.bookDAO = bookDAO;
        this.readerDAO = readerDAO;

        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        buildBorrowPanel();
        refreshTable();
    }

    private void buildBorrowPanel() {
        // Lấy toàn bộ logic từ buildBorrow() của LibraryFrame cũ

        // ======== Nút "Tạo phiếu mượn" ========
        JButton create = new JButton("+ Tạo phiếu mượn mới");
        create.setBackground(PRIMARY);
        create.setForeground(Color.WHITE);
        add(create, BorderLayout.NORTH);

        // ======== Bảng hiển thị phiếu mượn ========
        borrowTable = new JTable(tableModel);
        borrowTable.setFillsViewportHeight(true);
        borrowTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Đặt độ rộng cột
        String[] cols = {"Mã phiếu", "Mã độc giả", "Mã sách", "Ngày mượn", "Hạn trả", "Ngày trả"};
        int[] widths = {80, 80, 80, 100, 100, 100};
        for (int i = 0; i < cols.length; i++) {
            borrowTable.getColumnModel().getColumn(i).setHeaderValue(cols[i]);
            borrowTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        add(new JScrollPane(borrowTable), BorderLayout.CENTER);

        // ======== Nút "Trả sách" ========
        JButton markReturned = new JButton("Trả sách");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(markReturned);
        add(actions, BorderLayout.SOUTH);

        // ======== Sự kiện: Trả sách ========
        markReturned.addActionListener(e -> {
            int row = borrowTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn phiếu mượn!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BorrowRecord br = tableModel.getRecordAt(row);
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
            refreshTable(); // Cập nhật lại model
            parentFrame.refreshAll(); // Gọi refreshAll()
        });

        // ======== Sự kiện: Mở dialog tạo phiếu ========
        create.addActionListener(e -> {
            BorrowDialog dialog = new BorrowDialog(parentFrame, recordDAO, bookDAO, readerDAO);
            dialog.setVisible(true);
            refreshTable(); // Cập nhật lại model sau khi đóng dialog
            parentFrame.refreshAll(); // Gọi refreshAll()
        });
    }

    // Phương thức để nạp lại dữ liệu
    public void refreshTable() {
        tableModel.setRecords(recordDAO.getAllRecordsSorted());
    }
}