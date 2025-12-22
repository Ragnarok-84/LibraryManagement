📚 LibraryManagement – Hệ thống Quản lý Thư viện

LibraryManagement là một hệ thống quản lý thư viện số giúp quản lý sách, người dùng và các hoạt động mượn – trả một cách đơn giản, trực quan và hiệu quả.

✨ Chức năng chính

📚 Quản lý sách

+ Thêm, sửa, xóa sách

+ Quản lý thông tin: tên sách, tác giả, ISBN, tình trạng mượn

👥 Quản lý người dùng / thành viên

+ Đăng ký và quản lý thông tin thành viên

+ Theo dõi lịch sử mượn sách

🔄 Mượn – trả sách

+ Quy trình mượn và trả sách đơn giản

+ Tự động theo dõi hạn trả và tình trạng quá hạn

🔍 Tìm kiếm & lọc

+ Tìm kiếm sách hoặc người dùng theo nhiều tiêu chí

+ Giúp truy xuất thông tin nhanh chóng

🎨 Giao diện thân thiện

+ Giao diện rõ ràng, dễ sử dụng

+ Thiết kế bằng CSS, hoạt động tốt trên nhiều thiết bị

🛠️ Hướng dẫn cài đặt
Yêu cầu môi trường

Đảm bảo máy bạn đã cài:

Java JDK 11 trở lên

Apache Maven 3.6 trở lên

Các bước cài đặt

1️⃣ Clone project từ GitHub
git clone https://github.com/Ragnarok-84/LibraryManagement.git

2️⃣ Di chuyển vào thư mục project
cd LibraryManagement

3️⃣ Build project bằng Maven

Lệnh này sẽ tải các thư viện cần thiết và tạo file .jar trong thư mục target/:

mvn clean install

4️⃣ Chạy chương trình
java -jar target/LibraryManagement-1.0-SNAPSHOT.jar


⚠️ Tên file .jar có thể khác tùy cấu hình trong pom.xml

🚀 Cách sử dụng

Khởi động ứng dụng

Sau khi chạy, chương trình sẽ mở giao diện đồ họa hoặc khởi động hệ thống backend.

[INFO] Starting Library Management Application...
[INFO] GUI initialized.

Các thao tác cơ bản

Thêm sách mới: Vào mục Quản lý sách, nhập thông tin sách

Thêm thành viên: Vào mục Quản lý người dùng, nhập thông tin thành viên

Mượn sách: Chọn thành viên và sách còn trống → xác nhận mượn

