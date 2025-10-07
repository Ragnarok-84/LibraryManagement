package manager;

import java.util.*;
import model.Reader;

public class ReaderManager {
    private final Map<Integer, Reader> readerMap; // Dễ tra cứu theo ID

    public ReaderManager() {
        this.readerMap = new HashMap<>();
    }

    // ✅ Thêm độc giả mới
    public void addReader(Reader reader) {
        if (readerMap.containsKey(reader.getId())) {
            System.out.println("❌ ID đã tồn tại! Không thể thêm độc giả mới.");
        } else {
            readerMap.put(reader.getId(), reader);
            System.out.println("✅ Đã thêm độc giả: " + reader.getName());
        }
    }

    // ✅ Xóa độc giả theo ID
    public void removeReader(int id) {
        if (readerMap.remove(id) != null) {
            System.out.println("✅ Đã xóa độc giả có ID " + id);
        } else {
            System.out.println("❌ Không tìm thấy độc giả có ID này.");
        }
    }

    // ✅ Tìm độc giả theo ID
    public Reader findReaderById(int id) {
        return readerMap.get(id);
    }

    // ✅ Hiển thị tất cả độc giả
    public void displayAllReaders() {
        if (readerMap.isEmpty()) {
            System.out.println("📭 Chưa có độc giả nào.");
        } else {
            System.out.println("📚 Danh sách độc giả:");
            for (Reader r : readerMap.values()) {
                System.out.println("ID: " + r.getId() +
                        " | Tên: " + r.getName() +
                        " | Email: " + r.getEmail());
            }
        }
    }

    // ✅ Lấy danh sách độc giả (nếu cần)
    public Collection<Reader> getAllReaders() {
        return readerMap.values();
    }
}
