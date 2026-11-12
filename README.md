# Stadium Ticket Booking Simulation

> **Nhóm 2 — Dự án Mô phỏng Hệ thống Đặt vé Sân vận động**

Dự án Java chạy trên **giao diện dòng lệnh (Console/Terminal)**, mô phỏng quá trình quản lý trận đấu, khán đài, ghế ngồi, đặt vé và thanh toán. Chương trình có ba vai trò **Guest**, **Fan** và **Admin**; dữ liệu được lưu cục bộ để sử dụng lại ở lần chạy sau.

> **Lưu ý:** Đây là **hệ thống mô phỏng**, không phải website đặt vé trực tuyến. Các giao dịch thanh toán và hoàn tiền đều là **DEMO**, không kết nối ngân hàng, ví điện tử hoặc thu tiền thật.

---

## Danh sách thành viên

- **Võ Nguyên Thịnh** (QE210164)
- **Võ Thanh Nguyên** (QE200126)
- **Võ Anh Hào** (QE200005)
- **Nguyễn Việt Tùng Dương** (QE200061)
- **Lê Bách** (QE190145)

---

## Mô tả sơ lược các tính năng cơ bản

### 1. Nhóm tính năng cho Người mua (User / Fan)

- **Quản lý Tài khoản & Xác thực:**
  - Đăng ký tài khoản Fan, đăng nhập và đăng xuất.
  - Xem thông tin tài khoản: mã người dùng, tên đăng nhập, họ tên, vai trò, trạng thái và ngày tạo.
  - Thay đổi họ tên hiển thị và đổi mật khẩu (yêu cầu mật khẩu hiện tại).
  - Xem danh sách vé đã mua, lịch sử đơn hàng và lịch sử giao dịch mô phỏng.

- **Tra cứu Trận đấu:**
  - Xem các trận đấu sắp diễn ra, bao gồm thông tin hai đội, giải đấu, thời gian và trạng thái bán vé.
  - Tìm kiếm trận đấu bằng **từ khóa tên đội bóng hoặc giải đấu**.
  - Các trận đã bị hủy hoặc đã diễn ra không xuất hiện trong danh sách công khai.

- **Sơ đồ Ghế ngồi (Seat Map) trên Console:**
  - Xem danh sách khu vực/khán đài, số ghế, giá vé và trạng thái hoạt động của từng khu.
  - Hiển thị trạng thái ghế khi mở sơ đồ (dữ liệu được kiểm tra lại mỗi lần truy vấn):
    - `O` — **Available:** Ghế trống.
    - `L` — **Locked:** Ghế đang được người khác giữ.
    - `Y` — **Your Hold:** Ghế do chính Fan đang giữ.
    - `B` — **Booked:** Ghế đã được mua.
    - `-` — Khu vực đang đóng.
  - Ghế được hiển thị dưới dạng **văn bản trong Terminal**, không phải sơ đồ đồ họa có thể nhấp chuột.

- **Đặt vé & Giữ chỗ:**
  - Chọn ghế theo mã, ví dụ `A-01,A-02`.
  - Đặt **từ 1 đến tối đa 4 ghế cho một lượt thanh toán**.
  - Chỉ được giữ ghế của trận **đang mở bán** và chưa bắt đầu.
  - Ghế được giữ trong **5 phút**; sau khi hết hạn, hệ thống loại bỏ lượt giữ khi kiểm tra dữ liệu.
  - Không cho phép chọn trùng ghế, chọn ghế không hợp lệ hoặc mua ghế đã bán/đang giữ.
  - Mỗi Fan chỉ có **một lượt giữ ghế chưa hoàn tất** tại một thời điểm.
  - Có thể tự hủy lượt giữ ghế; hệ thống cũng giải phóng ghế đang giữ khi Fan đăng xuất.

- **Thanh toán mô phỏng & Xuất thông tin vé:**
  - Thanh toán DEMO bằng **ví điện tử** hoặc **chuyển khoản ngân hàng** (hai lựa chọn mô phỏng thành công).
  - Có lựa chọn **giả lập giao dịch thất bại** để kiểm tra luồng lỗi.
  - Giao dịch thành công tạo đơn vé, ghi lịch sử thanh toán và chuyển ghế sang trạng thái đã bán.
  - Hiển thị mã vé dạng `STADIUM-<mã đơn>`, danh sách ghế và mã tham chiếu giao dịch dạng `DEMO-TX-<mã>`.
  - Khi mô phỏng thất bại, hệ thống ghi nhận giao dịch thất bại và tiếp tục giữ ghế đến khi hết thời hạn hoặc được hủy.
  - **Không tạo mã QR, không xuất vé PDF và không xử lý thanh toán thật.**

- **Quản lý Vé & Hoàn tiền:**
  - Xem trạng thái đơn hàng: `PAID` (đã thanh toán) hoặc `REFUNDED` (đã hoàn tiền).
  - Fan có thể yêu cầu hủy vé/hoàn tiền DEMO nếu còn **ít nhất 24 giờ** trước giờ trận đấu.
  - Khi hoàn tiền thành công, đơn chuyển sang `REFUNDED`, ghi giao dịch hoàn tiền và ghế được phép bán lại.

### 2. Nhóm tính năng cho Khách (Guest)

- **Xem thông tin công khai:**
  - Truy cập sảnh Guest **không cần đăng nhập**.
  - Xem danh sách trận đấu sắp tới và trạng thái bán vé.
  - Xem thông tin cơ bản của từng trận: đội tham gia, giải đấu, thời gian, địa điểm hiển thị cố định là **“Sân vận động chính”** và tình trạng bán vé.
- **Giới hạn quyền:**
  - Guest **không được xem sơ đồ ghế, giữ ghế hoặc thanh toán**.
  - Muốn đặt vé, người dùng cần đăng ký/đăng nhập bằng tài khoản Fan.

### 3. Nhóm tính năng cho Quản trị viên (Admin)

- **Thống kê tổng quan (Dashboard):**
  - Thống kê số Fan, Fan đang hoạt động, số trận đấu, số trận đang mở bán.
  - Thống kê đơn đã thanh toán, đơn đã hoàn tiền và số lượt giữ ghế còn hiệu lực.
  - Hiển thị **doanh thu ròng mô phỏng** từ các bản ghi giao dịch thành công, bao gồm giá trị hoàn tiền âm.

- **Quản lý Trận đấu / Sự kiện:**
  - Xem toàn bộ trận đấu, kể cả trận đóng bán hoặc đã hủy.
  - Tạo trận đấu mới với đội chủ nhà, đội khách, giải đấu và thời gian thi đấu.
  - Chỉnh sửa thông tin trận đấu chưa bị hủy.
  - Mở hoặc đóng bán vé cho trận đấu.
  - Hủy trận đấu; đồng thời giải phóng các ghế đang giữ và hoàn tiền **DEMO** cho các đơn đã thanh toán của trận đó.

- **Quản lý Khán đài / Khu ghế:**
  - Xem các khu ghế hiện có, sức chứa, giá cơ bản và trạng thái hoạt động.
  - Thêm khu ghế mới.
  - Sửa tên khu, sức chứa, giá cơ bản và bật/tắt hoạt động của khu.
  - Thay đổi **giá vé của từng khu trong từng trận đấu**.
  - Kiểm tra để không giảm sức chứa làm mất ghế đã bán hoặc đang được giữ ngoài phạm vi mới.

- **Quản lý Tài khoản Fan:**
  - Xem danh sách Fan, tìm Fan theo tên hoặc tên đăng nhập.
  - Xem thông tin Fan và các đơn vé đã mua của Fan đó.
  - Khóa/mở khóa tài khoản Fan; khi khóa, các lượt giữ ghế của Fan đó được giải phóng.
  - Đặt lại mật khẩu cho Fan.

- **Quản lý Đơn vé, Ghế và Giao dịch:**
  - Xem toàn bộ đơn vé của các Fan và trạng thái từng đơn.
  - Xem sơ đồ ghế của trận đấu.
  - Hoàn tiền DEMO cho một đơn vé đang ở trạng thái `PAID`.
  - Xem toàn bộ nhật ký giao dịch thanh toán và hoàn tiền.
  - Xem các lượt giữ ghế đang có và chủ động giải phóng một lượt giữ ghế.
  - Đổi mật khẩu tài khoản Admin.

---

## Quy tắc hoạt động quan trọng

| Nội dung | Quy tắc đã được triển khai |
| --- | --- |
| Quyền truy cập | `GUEST`, `FAN`, `ADMIN` có các menu riêng; nghiệp vụ có kiểm tra quyền phù hợp. |
| Giới hạn mua | 1–4 ghế/lượt giữ và thanh toán. |
| Thời gian giữ ghế | 5 phút, tính từ lúc giữ thành công. |
| Lượt giữ của Fan | Tối đa 1 lượt giữ chưa hoàn tất tại một thời điểm. |
| Ghế đã bán | Không thể mua lại khi đơn còn trạng thái `PAID`. |
| Hủy vé bởi Fan | Phải còn ít nhất 24 giờ trước giờ bắt đầu trận đấu. |
| Hoàn tiền | Mô phỏng, tạo giao dịch `REFUND` và chuyển đơn sang `REFUNDED`. |
| Trạng thái trận | `OPEN` (mở bán), `CLOSED` (đóng bán), `CANCELLED` (đã hủy). |
| Mật khẩu | Lưu mã băm có salt bằng `PBKDF2WithHmacSHA256`, không lưu mật khẩu gốc dưới dạng văn bản. |
| Dữ liệu | Lưu cục bộ trong file `data/stadium.properties`. |

**Lưu ý về cập nhật ghế:** Đây là ứng dụng Console chạy trong một tiến trình. Trạng thái ghế được tính lại khi người dùng mở sơ đồ hoặc thao tác đặt vé, **không phải hệ thống cập nhật trực tiếp qua WebSocket hay nhiều máy chủ**. Quy trình kiểm tra/đặt vé được đồng bộ trong phạm vi một tiến trình Java.

---

## Công nghệ và cấu trúc dự án

- **Ngôn ngữ:** Java (mã nguồn tương thích mức biên dịch Java 8).
- **Giao diện:** Console/Terminal, hỗ trợ tiếng Việt UTF-8.
- **Tổ chức mã nguồn:** MVC kết hợp tầng Service và Repository.
- **Lưu trữ:** Java `Properties` trong file cục bộ, không sử dụng MySQL/SQL Server.
- **Thư viện:** Java Standard Library; không có thư viện ngoài, Maven hoặc Gradle trong mã nguồn được cung cấp.

```text
StadiumTicketingMVC/
├── .vscode/
│   └── settings.json
├── src/
│   ├── Main.java                 # Điểm khởi chạy chương trình
│   ├── controller/              # Điều hướng menu, xử lý thao tác người dùng
│   │   ├── AppController.java
│   │   ├── AuthController.java
│   │   ├── GuestController.java
│   │   ├── FanController.java
│   │   └── AdminController.java
│   ├── model/                   # Dữ liệu người dùng, trận đấu, ghế, đơn vé...
│   ├── view/                    # Giao diện Console cho từng vai trò
│   ├── service/                 # Xử lý đăng nhập, trận đấu, đặt vé, quản trị
│   ├── repository/              # Đọc/ghi dữ liệu vào file Properties
│   └── util/                    # Tiện ích mật khẩu, thời gian, tiền tệ
├── README.md
└── data/                        # Tự tạo khi chạy lần đầu (không có sẵn trong ZIP)
    └── stadium.properties
```

**Luồng xử lý chính:** `Main` khởi tạo `FileRepository` và `Store` → khởi tạo các `Service` → `AppController` điều hướng tới `GuestController`, `FanController` hoặc `AdminController` → `View` hiển thị/nhận dữ liệu → `Service` kiểm tra nghiệp vụ → `Store` và `FileRepository` ghi dữ liệu khi có thay đổi.

---

## Hướng dẫn chạy chương trình

### Yêu cầu

- Cài **JDK 8 trở lên** (khuyến nghị JDK 17/21).
- Có thể chạy bằng **Visual Studio Code** (Java Extension Pack) hoặc Terminal.
- Với Windows, nên chạy trong **Integrated Terminal của VS Code** hoặc PowerShell. Chức năng nhập mật khẩu trên Windows sử dụng `powershell.exe` để hiển thị dấu `*` khi gõ.

### Cách 1: Chạy trong Visual Studio Code

1. Mở **đúng thư mục `StadiumTicketingMVC`** trong VS Code.
2. Mở file `src/Main.java`.
3. Chọn **Run Java** hoặc **Run** với chế độ Terminal.
4. Chọn chức năng theo số trên menu xuất hiện.

### Cách 2: Chạy trong Windows PowerShell

Mở PowerShell tại thư mục `StadiumTicketingMVC`, sau đó chạy:

```powershell
New-Item -ItemType Directory -Force bin | Out-Null
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d bin $files
java -cp bin Main
```

### Cách 3: Chạy trong Linux/macOS Terminal

Mở Terminal tại thư mục `StadiumTicketingMVC`, sau đó chạy:

```bash
mkdir -p bin
find src -name '*.java' -print0 | xargs -0 javac -encoding UTF-8 -d bin
java -cp bin Main
```

**Chú ý:** Chương trình lưu dữ liệu vào `data/stadium.properties` **tương đối với thư mục làm việc hiện tại**. Nên chạy từ thư mục gốc dự án để dữ liệu được đặt đúng chỗ.

### Menu chính

```text
1. Vào sảnh với tư cách GUEST (không cần tài khoản)
2. Đăng nhập FAN
3. Đăng ký FAN
4. Đăng nhập ADMIN
0. Thoát
```

### Tài khoản Admin mẫu

Ở **lần chạy đầu tiên**, nếu chưa có file dữ liệu, chương trình tạo tài khoản quản trị demo:

- **Tên đăng nhập:** `admin`
- **Mật khẩu ban đầu:** `Admin@123`

Đây là tài khoản mẫu được khai báo trong `Store.java`; hãy **đổi mật khẩu qua menu Admin (mục 21)** và không sử dụng mật khẩu này cho hệ thống thật.

### Dữ liệu mẫu khi chạy lần đầu

- **3 khu ghế:** A (VIP, 16 ghế), B (24 ghế), C (32 ghế).
- **4 trận đấu mẫu:**
  - Hà Nội FC — Bình Định FC.
  - Đà Nẵng FC — Hải Phòng FC.
  - Công An Hà Nội — Nam Định FC.
  - U23 Việt Nam — U23 Thái Lan.
- Thời gian thi đấu của dữ liệu mẫu được thiết lập **tương đối theo ngày tạo dữ liệu lần đầu** (sau 2, 5, 10 và 20 ngày, lúc 19:30 theo múi giờ Việt Nam), không phải lịch thi đấu thật.

Các thông tin sau đó được lưu lại. Chạy lại ứng dụng sẽ đọc file dữ liệu hiện có thay vì khởi tạo lại.

---

## Giới hạn hiện tại của dự án

Để tránh nhầm lẫn giữa chức năng đã triển khai và chức năng dự kiến:

- **Không có** giao diện website, ứng dụng di động hoặc màn hình đặt ghế dạng kéo/thả/click chuột.
- **Không có** cổng thanh toán thật, ví điện tử thật, tích hợp ngân hàng hay thanh toán trực tuyến.
- **Không có** phát hành vé PDF, vé QR hoặc gửi vé qua email.
- **Không có** tính năng lọc nhiều tiêu chí theo thời gian, địa điểm, loại vé và khoảng giá. Fan hiện chỉ **tìm kiếm bằng từ khóa đội bóng hoặc giải đấu**.
- **Không có** cơ sở dữ liệu SQL hoặc dịch vụ backend nhiều máy chủ.
- **Không có** hệ thống đồng bộ trạng thái ghế qua mạng theo thời gian thực; dữ liệu được cập nhật theo các lần truy vấn/thao tác trong ứng dụng Console.
- Địa điểm của trận đấu trong màn hình Guest là dòng chữ cố định **“Sân vận động chính”**, không phải trường địa điểm có thể cấu hình riêng cho từng trận.
- Dữ liệu được lưu cục bộ; nên sao lưu `data/stadium.properties` và **không đưa dữ liệu người dùng thực tế lên GitHub**.

---

## Kiểm tra mã nguồn

README này được đối chiếu với các lớp trong thư mục `src/`, đặc biệt là `AppController`, `FanController`, `AdminController`, `GuestController`, `BookingService`, `EventService`, `AuthService`, `AdminService`, `Store` và `FileRepository`.

Mã nguồn đã được **biên dịch thử với tùy chọn `javac --release 8`** và chạy thử các thao tác cơ bản gồm Guest xem trận, Fan đăng ký/đăng nhập/giữ ghế/thanh toán DEMO/xem lịch sử/hoàn vé, Admin đăng nhập và xem thống kê. Việc chạy thử này **không thay thế kiểm thử toàn bộ mọi trường hợp và mọi môi trường**.

---

**Stadium Ticket Booking Simulation — Nhóm 2**
