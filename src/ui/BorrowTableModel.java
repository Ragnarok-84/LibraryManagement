package ui;

import model.BorrowRecord;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BorrowTableModel extends AbstractTableModel {
    private final String[] columns = {
            "Mã phiếu", "Độc giả", "Sách", "Ngày mượn", "Hạn trả", "Ngày trả", "Trạng thái"
    };

    private List<BorrowRecord> records = new ArrayList<>();

    public void setRecords(List<BorrowRecord> records) {
        this.records = records != null ? records : new ArrayList<>();
        fireTableDataChanged();
    }

    public BorrowRecord getRecordAt(int row) {
        if (row >= 0 && row < records.size()) {
            return records.get(row);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return records.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        BorrowRecord r = records.get(rowIndex);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return switch (columnIndex) {
            case 0 -> r.getRecordID(); // Mã phiếu (bổ sung trong model BorrowRecord)
            case 1 -> r.getReaderID() ;
            case 2 -> r.getBookID();
            case 3 -> r.getBorrowDate() != null ? fmt.format(r.getBorrowDate()) : "";
            case 4 -> r.getDueDate() != null ? fmt.format(r.getDueDate()) : "";
            case 5 -> r.getReturnDate() != null ? fmt.format(r.getReturnDate()) : "";
            case 6 -> r.getStatus();
            default -> "";
        };
    }
}
