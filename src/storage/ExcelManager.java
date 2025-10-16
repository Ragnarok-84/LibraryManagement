package storage;

import model.*;
import model.Reader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class ExcelManager {
    private static final String FILE_PATH = "library.xlsx";

    // === Ghi toàn bộ dữ liệu ===
    public static void writeAll(List<Book> books, List<Reader> readers, List<BorrowRecord> records) {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Books
        XSSFSheet bookSheet = workbook.createSheet("Books");
        Row h1 = bookSheet.createRow(0);
        h1.createCell(0).setCellValue("ISBN");
        h1.createCell(1).setCellValue("Title");
        h1.createCell(2).setCellValue("Author");
        h1.createCell(3).setCellValue("Year");
        h1.createCell(4).setCellValue("Available");
        h1.createCell(5).setCellValue("BorrowedCount");

        int rowId = 1;
        for (Book b : books) {
            Row r = bookSheet.createRow(rowId++);
            r.createCell(0).setCellValue(b.getIsbn());
            r.createCell(1).setCellValue(b.getTitle());
            r.createCell(2).setCellValue(b.getAuthor());
            r.createCell(3).setCellValue(b.getYear());
            r.createCell(4).setCellValue(b.getAvailable());
            r.createCell(5).setCellValue(b.getBorrowedCount());
        }

        // Readers
        XSSFSheet readerSheet = workbook.createSheet("Readers");
        Row h2 = readerSheet.createRow(0);
        h2.createCell(0).setCellValue("ID");
        h2.createCell(1).setCellValue("Name");
        int rid = 1;
        for (Reader r : readers) {
            Row row = readerSheet.createRow(rid++);
            row.createCell(0).setCellValue(r.getId());
            row.createCell(1).setCellValue(r.getName());
        }

        // Borrow Records
        XSSFSheet recordSheet = workbook.createSheet("Records");
        Row h3 = recordSheet.createRow(0);
        h3.createCell(0).setCellValue("ReaderID");
        h3.createCell(1).setCellValue("ISBN");
        h3.createCell(2).setCellValue("BorrowDate");
        h3.createCell(3).setCellValue("DueDate");
        h3.createCell(4).setCellValue("Returned");

        int recid = 1;
        for (BorrowRecord r : records) {
            Row row = recordSheet.createRow(recid++);
            row.createCell(0).setCellValue(r.getReader() == null ? "" : r.getReader().getId());
            row.createCell(1).setCellValue(r.getBook() == null ? "" : r.getBook().getIsbn());
            row.createCell(2).setCellValue(r.getBorrowDate() == null ? "" : r.getBorrowDate().toString());
            row.createCell(3).setCellValue(r.getDueDate() == null ? "" : r.getDueDate().toString());
            row.createCell(4).setCellValue(r.getReturnDate() == null ? "No" : "Yes");
        }

        try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
            workbook.write(fos);
            workbook.close();
            System.out.println("✅ Đã lưu toàn bộ dữ liệu vào " + FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
