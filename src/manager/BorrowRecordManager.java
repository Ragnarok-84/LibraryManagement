package manager;

import dao.BorrowRecordDAO;
import dao.BookDAO;
import model.BorrowRecord;
import model.Book;
import model.Reader;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class BorrowRecordManager implements Borrowable {

    private final BorrowRecordDAO recordDAO;
    private final BookDAO bookDAO;

    public BorrowRecordManager(BorrowRecordDAO recordDAO, BookDAO bookDAO) {
        this.recordDAO = recordDAO;
        this.bookDAO = bookDAO;
    }

    @Override
    public BorrowRecord borrowItem(Reader reader, Book book, LocalDate dueDate) throws Exception {
        if (book.getAvailable() <= 0) {
            throw new Exception("Lỗi: Sách " + book.getTitle() + " đã hết bản có sẵn.");
        }

        // 1. Cập nhật số lượng sách có sẵn
        book.setAvailable(book.getAvailable() - 1);
        bookDAO.update(book);

        // 2. Tạo bản ghi mới
        BorrowRecord record = new BorrowRecord(
                reader.getReaderID(),
                book.getBookID(),
                LocalDate.now(),
                dueDate
        );

        // 3. Lưu bản ghi vào database
        recordDAO.add(record); // add là void, chỉ cần gọi

        // 4. Trả về bản ghi vừa tạo (để tiện xử lý ở phía trên)
        return record;
    }

    @Override
    public BorrowRecord returnItem(int recordId) throws Exception {
        // 1. Tìm bản ghi và kiểm tra
        Optional<BorrowRecord> optionalRecord = recordDAO.findByID(recordId);
        if (optionalRecord.isEmpty()) {
            throw new Exception("Lỗi: Không tìm thấy bản ghi mượn sách có ID: " + recordId);
        }
        BorrowRecord record = optionalRecord.get();

        // 2. Cập nhật bản ghi
        record.setReturnDate(LocalDate.now());
        recordDAO.update(record);

        // 3. Cập nhật số lượng sách có sẵn
        Optional<Book> optionalBook = bookDAO.findByID(record.getBookID());
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            book.setAvailable(book.getAvailable() + 1);
            bookDAO.update(book);
        } else {
            // Cơ chế xử lý nếu sách bị xóa khi đang được mượn
            System.err.println("Cảnh báo: Sách ID " + record.getBookID() + " không còn tồn tại.");
        }

        return record;
    }


    @Override
    public List<BorrowRecord> findOverdueRecords() {
        // Tim kiem cac ban ghi co ngay tra du kien < ngay hien tai va chua co ngay tra thuc te
        return recordDAO.findAll().stream()
                .filter(r -> r.getDueDate().isBefore(LocalDate.now()) && r.getReturnDate() == null)
                .collect(Collectors.toList());
    }
}
