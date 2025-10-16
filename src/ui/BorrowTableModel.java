package ui;


import model.BorrowRecord;
import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class BorrowTableModel extends AbstractTableModel {
    private final String[] cols = {"Độc giả", "Sách", "Ngày mượn", "Hạn trả", "Trạng thái"};
    private final List<BorrowRecord> data;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    public BorrowTableModel(List<BorrowRecord> data) { this.data = data; }


    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }


    @Override public Object getValueAt(int r, int c) {
        BorrowRecord br = data.get(r);
        return switch (c) {
            case 0 -> br.getReader().getName();
            case 1 -> br.getBook().getTitle();
            case 2 -> br.getBorrowDate() == null ? "" : br.getBorrowDate().format(fmt);
            case 3 -> br.getDueDate() == null ? "" : br.getDueDate().format(fmt);
            case 4 -> br.getStatus();
            default -> "";
        };
    }


    public BorrowRecord getAt(int r) { return data.get(r); }
}