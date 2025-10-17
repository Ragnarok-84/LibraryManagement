package ui;

import dao.BookDAO;
import dao.ReaderDAO;
import dao.BorrowRecordDAO;
import model.Book;
import model.Reader;
import model.BorrowRecord;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class BorrowDialog extends JDialog {
    private JComboBox<Reader> readerCombo;
    private JComboBox<Book> bookCombo;
    private JTextField daysField;
    private final BorrowRecordDAO borrowRecordDAO;
    private final BookDAO bookDAO;
    private final ReaderDAO readerDAO;

    public BorrowDialog(JFrame parent, BorrowRecordDAO borrowRecordDAO, BookDAO bookDAO, ReaderDAO readerDAO) {
        super(parent, "Tạo Phiếu Mượn", true);
        this.borrowRecordDAO = borrowRecordDAO;
        this.bookDAO = bookDAO;
        this.readerDAO = readerDAO;

        initUI();
        setSize(450, 250);
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));

        List<Reader> readers = readerDAO.getAllReaders();
        List<Book> books = bookDAO.getAllBooks();

        readerCombo = new JComboBox<>(readers.toArray(new Reader[0]));
        bookCombo = new JComboBox<>(books.toArray(new Book[0]));
        daysField = new JTextField("7");

        form.add(new JLabel("Độc giả:"));
        form.add(readerCombo);
        form.add(new JLabel("Sách:"));
        form.add(bookCombo);
        form.add(new JLabel("Số ngày mượn:"));
        form.add(daysField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("Xác nhận");
        JButton cancelBtn = new JButton("Hủy");
        buttons.add(okBtn);
        buttons.add(cancelBtn);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> handleBorrow());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void handleBorrow() {
        Reader reader = (Reader) readerCombo.getSelectedItem();
        Book book = (Book) bookCombo.getSelectedItem();
        int days;

        try {
            days = Integer.parseInt(daysField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số ngày mượn không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(days);

        BorrowRecord record = new BorrowRecord();
        record.setReader(reader);
        record.setBook(book);
        record.setBorrowDate(borrowDate);
        record.setDueDate(dueDate);

        borrowRecordDAO.addBorrowRecord(record);

        JOptionPane.showMessageDialog(this, "✅ Tạo phiếu mượn thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }
}
