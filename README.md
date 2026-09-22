-Stadium-Ticket-Booking-Simulation
Github nhóm 2 Danh sách thành viên nhóm 2 bao gồm: - Võ Nguyên Thịnh(QE210164) - Võ Thành Nguyên(QE200126) - Võ Anh Hào(QE200005) - Nguyễn Việt Tùng Dương(QE200061) - Lê Bách(QE190145) Mô tả sơ lược về tính năng cơ bản của ứng dụng ticket booking

Nhóm tính năng cho Người mua (User / Fan)
Quản lý Tài khoản & Xác thực:
Đăng ký, đăng nhập, đổi mật khẩu và quản lý thông tin cá nhân.
Quản lý danh sách vé đã mua và lịch sử giao dịch.
Tra cứu Sự kiện / Trận đấu:
Danh sách sự kiện, trận đấu đang mở bán.
Tìm kiếm và lọc theo thời gian, địa điểm, đội bóng, loại vé hoặc khoảng giá.
Sơ đồ Ghế ngồi Trực quan (Seat Map):
Hiển thị cấu trúc khán đài/khu vực (SVIP, VIP, Thường, Đứng).
Hiển thị trạng thái ghế theo thời gian thực: Trống (Available), Đang chọn/Giữ chỗ (Locked), và Đã bán (Booked).
Đặt vé & Giữ chỗ:
Chọn ghế trực tiếp trên sơ đồ.
Ràng buộc số lượng vé tối đa cho mỗi lượt mua (ví dụ: tối đa 4 vé/giao dịch) để hạn chế phe vé.
Khóa ghế tạm thời (Timeout/TTL - Time To Live, ví dụ giữ ghế trong 5-10 phút để người dùng hoàn tất thao tác).
Xác nhận & Xuất vé:
Xác nhận thông tin đơn hàng và tạo mã giao dịch duy nhất.
Xuất vé điện tử chứa mã nhận diện duy nhất (hoặc mã QR) phục vụ việc soát vé tại cổng.
Nhóm tính năng Xử lý Hệ thống Lõi (Engine & Concurrency)
Cơ chế Chống bán trùng ghế (Anti-Double Booking):
Áp dụng các cơ chế khóa (Locking mechanisms) như Synchronized, File Lock, Pessimistic Locking hoặc Optimistic Locking (dùng trường version).
Đảm bảo chuỗi thao tác "Đọc trạng thái Kiểm tra Đổi trạng thái Ghi dữ liệu" xảy ra dưới dạng Atomic Transaction (nguyên tử), không bị race condition khi hàng nghìn luồng cùng truy cập.
Tự động giải phóng ghế quá hạn:
Hết thời gian giữ chỗ tạm thời mà người dùng không xác nhận, hệ thống tự động chuyển trạng thái ghế từ LOCKED về lại AVAILABLE.
Hàng chờ xử lý (Queue System / Throttling):
Điều phối lượng truy cập trong các đợt mở bán hot (Flash Sale) để tránh làm nghẽn nguồn lưu trữ (Database/File CSV).
Nhóm tính năng dành cho Quản trị viên (Admin Panel)
Quản lý Địa điểm & Sơ đồ sân:
Khởi tạo và thiết lập sơ đồ sân vận động, cấu hình các khán đài, hàng ghế và tổng số lượng ghế.
Quản lý Trận đấu / Sự kiện:
Tạo mới, chỉnh sửa hoặc hủy sự kiện; thiết lập bảng giá chi tiết cho từng khu vực ghế.
Báo cáo & Thống kê:
Thống kê tỷ lệ lấp đầy sân (Occupancy Rate).
Thống kê tổng doanh thu, số vé đã bán, số vé còn tồn theo từng trận đấu hoặc theo thời gian real-time.
Nhóm tính năng Mô phỏng & Đo lường Hiệu năng (Simulator / Analytics)
Công cụ Mô phỏng Tải cao (Simulator Tool):
Khởi tạo đồng thời luồng đại diện cho người dùng (Fan Threads) cùng bấm đặt vé tại một thời điểm chính xác (sử dụng CountDownLatch và ExecutorService).
Đo lường & Xuất báo cáo Thực nghiệm:
Thông lượng (Throughput / TPS): Số lượng giao dịch đặt vé thành công xử lý được trong mỗi giây.
Tỷ lệ lỗi (Double Booking Rate %): Phần trăm số ghế bị đè/bán trùng khi chạy thử nghiệm dưới áp lực tải lớn.
