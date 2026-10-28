# 🩺 ECG & Vital Signs AI Monitor (Android / Jetpack Compose)
## Tích hợp On-Device TensorFlow Lite & Tương thích Toàn diện Hệ thống Vi điều khiển ESP32-S3
### Giao diện Thuần Android Phong cách Material You 3 (Material Design 3)

Ứng dụng di động Android hiện đại được xây dựng bằng **Kotlin**, **Jetpack Compose (Material You 3)**, **Coroutines/Flow**, **TensorFlow Lite On-Device** và **Room Database** để kết nối, nhận dữ liệu thời gian thực và giám sát toàn diện hệ thống **ESP32-S3 ECG & Sinh hiệu Đa thông số AI**.

---

### 🌟 Điểm nổi bật & Tính năng mới (Bản cập nhật v3.0 Material You 3)

1. **Thiết kế Thuần Android với Phong cách Material You 3**:
   - **Tự động đổi màu Dynamic Color**: Tự động trích xuất màu sắc hài hòa từ hình nền thiết bị (Android 12+ / API 31+) qua `dynamicDarkColorScheme()` & `dynamicLightColorScheme()`.
   - **Bảng màu y tế dự phòng (Clinical Fallback)**: Tông màu Clinical Jade & Deep Slate chuẩn tương phản WCAG AA cho các thiết bị chạy Android cũ hơn.
   - **Tràn viền toàn diện (Edge-to-Edge)**: Kích hoạt `enableEdgeToEdge()` chuẩn Android 14/15, thanh trạng thái và điều hướng trong suốt.
   - **Thanh Navigation Bar chuẩn M3**: Sử dụng 100% Vector Icons Material (`MonitorHeart`, `ShowChart`, `TrendingUp`, `Settings`) thay thế ký tự thô.

2. **Đồ thị Sóng Điện tim Y tế Thích ứng 2 Chế độ (Adaptive Medical Canvas)**:
   - **Chế độ Tối (Dark Mode)**: Màn hình monitor y sinh phòng cấp cứu ICU (nền xanh đen `#081216`, lưới quang phổ Cyan phân giải 0.04s & 0.2s).
   - **Chế độ Sáng (Light Mode)**: Giấy in điện tim y tế cổ điển (nền `#FFFBF8`, lưới hồng phấn milivolt 1mm & 5mm chuẩn xác).
   - **Thước chuẩn độ tín hiệu 1 mV ($1\text{ mV} = 10\text{ mm}$)** và tem thông số chuẩn y tế `125 Hz • 25 mm/s • 10 mm/mV`.
   - **Đèn LED nháy nhịp tim (R-Peak Pulse)**: Phản hồi nhịp tim đập thời gian thực với hiệu ứng co giãn động `animateFloatAsState`.

3. **Kiến trúc Dual AI Engine (Chuyển đổi linh hoạt Toggle)**:
   - **Nguồn 1 - ESP32 Hardware AI**: Tiếp nhận kết quả phân loại từ mô hình CNN 3-nhánh chạy trên vi điều khiển qua gói tin BLE.
   - **Nguồn 2 - Android On-Device TFLite AI**: Chạy suy luận trực tiếp trên điện thoại bằng các mô hình định lượng INT8 siêu nhẹ, độc lập với vi điều khiển.
   - Người dùng có thể dễ dàng chuyển đổi nguồn hiển thị trên UI bằng 1 cú chạm.

4. **Tích hợp Top 5 Mô hình AI Benchmark từ MATLAB (`assets/models/`)**:
   - ⚡ **`kachuee_residual_int8.tflite`** (187 mẫu @ 125Hz): Mạng Deep Residual 5 khối (Kachuee et al., 2018), độ chính xác **99.21%**, Macro F1 **0.9418**, dung lượng 90.2 KB.
   - 🥇 **`model_3_branch_f32_leaky_int8.tflite`** (375 mẫu @ 125Hz): Top 1 Toàn diện MaxPool, Macro F1 **0.9401**, dung lượng 42.6 KB.
   - ⚙️ **`model_4_branch_f16_relu_int8.tflite`** (375 mẫu @ 125Hz): Chuẩn TinyML cân bằng tối ưu giữa độ chính xác và tài nguyên, F1 **0.9299**, dung lượng 34.8 KB.
   - 🪶 **`model_4_branch_f8_relu_int8.tflite`** (375 mẫu @ 125Hz): Quán quân siêu nhẹ chỉ 27.9 KB, suy luận thần tốc dưới 3 ms.
   - 🌊 **`model_3_branch_f32_relu_avgpool_int8.tflite`** (375 mẫu @ 125Hz): Top 1 AveragePooling làm mượt đặc trưng, S-beat F1 **90.85%**.

5. **Đồng bộ Chuỗi Tiền xử lý & Cắt nhịp 1:1 từ MATLAB**:
   - **Dò đỉnh Pan-Tompkins thời gian thực (`PanTompkinsDetector.kt`)**: Khớp 1:1 với `pan_tompkins_qrs.m` (Bandpass HP 0.95 + LP 0.15, vi phân trễ 2 mẫu, cửa sổ MWI 18 mẫu, ngưỡng động thích nghi $Th=30$, tinh chỉnh cực trị đỉnh R trong $[-15, +5]$ mẫu).
   - **Cắt nhịp thích ứng (`EcgSegmenter.kt`)**: Khớp 1:1 với `segment_heartbeats.m` (tự động cắt 375 mẫu hoặc 187 mẫu theo mô hình được chọn và chuẩn hóa Min-Max $[0.0, 1.0]$).

6. **Chế độ Chạy thử Ngoại tuyến (Demo Simulation Mode)**:
   - Cho phép người dùng bật/tắt nhanh chế độ Demo (`🧪 Demo`) ngay trên thanh tiêu đề hoặc Cài đặt.
   - Tự động phát sóng ECG giả lập tần số 125 Hz (gồm cả nhịp bình thường và nhịp ngoại tâm thất PVC định kỳ), chạy qua Pan-Tompkins và kích hoạt On-Device TFLite để kiểm thử toàn diện mọi tính năng mà không cần phần cứng ESP32.

7. **Phân tích & So sánh Đa Đồ thị ECG (3 Kênh thời gian thực)**:
   - **Đồ thị 1 (Tín hiệu thô - Raw)**: Vẽ trực tiếp giá trị ADC thô từ cảm biến AD8232 (Đỏ san hô).
   - **Đồ thị 2 (Lọc từ ESP32)**: Tín hiệu đã lọc thời gian thực trên chip ESP32-S3 (Notch 50 Hz + chống trôi nền + làm mượt - Xanh ngọc y sinh).
   - **Đồ thị 3 (Lọc trên Android)**: Tín hiệu chạy qua bộ lọc số `RealtimeEcgFilter` cục bộ của Android để đối chiếu và kiểm chứng chất lượng bộ lọc vi điều khiển (Xanh dương kỹ thuật).

8. **Giám sát Sinh hiệu Toàn diện (MAX30102)**:
   - Nồng độ oxy trong máu **SpO₂ (%)**.
   - Nhịp mạch quang học **PPG Pulse (BPM)**.
   - Phát hiện nhịp tim đập thời gian thực (Heart Beat indicator ❤️).

9. **Linh vật Ami Mascot tương tác động**:
   - Tự động thay đổi biểu cảm GIF sinh động theo trạng thái kết nối và sức khỏe (`AmiCompanionCard` thích ứng co giãn).

10. **Lưu trữ Cục bộ (Room Database) & Xuất CSV chuẩn SAF**:
    - Lưu trữ các phiên đo ECG vào SQLite cục bộ qua Room DB (`ecg_sessions`).
    - Phân tích và biểu diễn xu hướng nhịp tim trung bình trong 7 ngày gần nhất.
    - Xuất dữ liệu toàn bộ phiên đo chuẩn 100% định dạng `in/data` qua hộp thoại Storage Access Framework (SAF).

---

### 📂 Kiến trúc Mã nguồn Mô-đun Clean Compose

```text
app/src/main/java/com/example/ecgmonitor/
├── MainActivity.kt                # Activity khởi chạy, Edge-to-Edge, TopAppBar, BottomBar Scaffold
├── EcgViewModel.kt                # Quản lý StateFlow, BLE Telemetry, TFLite AI, Pan-Tompkins & Room DB
├── ble/                           # GATT Client quét và kết nối với ESP32-S3, parser gói tin 125 Hz
├── data/                          # Bảng Room Database ecg_sessions
├── domain/                        # Thuật toán dò đỉnh Pan-Tompkins, cắt nhịp, lọc số, TFLite Engine
│
├── ui/
│   ├── theme/                     # Hệ thống Theme Material You 3
│   │   ├── Color.kt               # Bảng màu y tế M3, Lưới ECG Option A (ICU Dark & Medical Pink Paper)
│   │   ├── Type.kt                # Thang đo Typography M3 chuẩn Roboto
│   │   ├── Shape.kt               # Thang đo bo góc M3 chuẩn từ 4dp đến 28dp
│   │   └── Theme.kt               # Dynamic Color (Android 12+) + Fallback bảng màu y tế lâm sàng
│   │
│   ├── components/                # Các thành phần giao diện dùng chung
│   │   ├── EcgWaveformView.kt     # Đồ thị sóng điện tim y tế (Medical Grid, Xung 1mV, R-Peak, Anti-aliasing)
│   │   ├── AmiCompanionCard.kt    # Thẻ linh vật Ami (GIF biểu cảm động + khung lời khuyên co giãn)
│   │   ├── BleDeviceSheet.kt      # ModalBottomSheet quét & kết nối BLE (100% Vector Icons)
│   │   ├── VitalMetricCard.kt     # Thẻ chỉ số sinh hiệu KPI (ECG BPM, SpO2 %, Mạch PPG)
│   │   └── AiClassificationCard.kt # Thẻ Dual AI & Phân loại nhịp 5 lớp AAMI EC57 (N, S, V, F, Q)
│   │
│   └── screens/                   # 4 Màn hình chính chuyên biệt
│       ├── HomeScreen.kt          # Màn hình Tổng quan Sinh hiệu & AI
│       ├── ChartsScreen.kt        # Màn hình Phân tích 3 Kênh Đồ thị ECG (Lưu DB & Xuất CSV SAF)
│       ├── TrendsScreen.kt        # Màn hình Xu hướng Nhịp tim 7 Ngày & Lịch sử phiên đo
│       └── SettingsScreen.kt      # Màn hình Cài đặt (Demo, Mô hình TFLite, Theme Sáng/Tối, Ngôn ngữ)
```

---

### 🚀 Cách 1: Tự động Build APK trên Cloud qua GitHub Actions (0 MB trên máy)

Dự án đã được tích hợp sẵn workflow tại `.github/workflows/build_apk.yml`. Bạn chỉ cần đẩy code lên GitHub theo các bước:

```bash
git add .
git commit -m "feat: Upgrade to pure Android Material You 3 and modular Compose architecture"
git push -u origin main
```

1. Truy cập vào trang Repository trên GitHub của bạn.
2. Nhấn vào tab **Actions**.
3. Chọn lần chạy mới nhất (workflow **Build Android ECG AI APK**).
4. Đợi khoảng 2–3 phút khi có dấu tích xanh ✅, cuộn xuống phần **Artifacts** và bấm tải **`ECG-AI-Monitor-Debug-APK`**.
5. Cài đặt trực tiếp file `app-debug.apk` lên điện thoại!

---

### 💻 Cách 2: Mở và chạy dự án cục bộ trong Android Studio

1. **Mở dự án**: Mở Android Studio, chọn **Open** và dẫn tới thư mục dự án.
2. **Cấu hình môi trường**:
   - **JDK**: Java 17.
   - **Android SDK**: Compile SDK 35 (hỗ trợ Android 8.0 / API 26 trở lên).
   - Chọn **Sync Project with Gradle Files**.
3. **Chạy ứng dụng**:
   - Kết nối điện thoại Android qua USB (bật chế độ Developer Options & USB Debugging) hoặc chạy trên Emulator.
   - Nhấn **Run 'app'** (Shift + F10).
