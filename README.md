# Champions Battle Logger

**Floating overlay companion for Pokémon Champions — Doubles VGC**

Ghi lại turn-by-turn battle log ngay trên màn hình game, không cần thoát app.

## Tính năng

- **Floating bubble** — kéo thả được, hiển thị trên mọi ứng dụng
- **Panel Doubles** — 2 slot mỗi bên, nhập tay Pokémon + Move
- **HP tracker** — bấm -25/-50/KO để ghi damage, không cần nhập số
- **Turn log** — bấm `+ LOG TURN` để lưu turn, tự động xoá move cũ
- **Recent log** — hiện 5 turn gần nhất ngay trên panel
- **Battle History** — lưu Room database, xem lại tất cả trận đã đánh

## Cài đặt

1. Tải APK từ [Releases](https://github.com/huyq74259-source/champions-battle-logger/releases)
2. Cài đặt trên Android 13+
3. Mở app → Tab **Overlay** → bấm **Start Overlay**
4. Lần đầu: cấp quyền **"Draw over other apps"** (Settings → hiển thị trên ứng dụng khác)
5. Bấm Start lại

## Cách dùng

### Trong trận đấu

| Bước | Thao tác |
|------|---------|
| 1 | Tap floating bubble → panel mở ra |
| 2 | Nhập tên Pokémon + chiêu của cả 2 bên |
| 3 | Bấm **-25/-50/KO** để ghi damage vào HP |
| 4 | Bấm **+ LOG TURN** → turn được lưu, move bị xoá cho turn sau |
| 5 | Tap 🔽 (minimize) → thu gọn lại về bubble |
| 6 | Lặp lại mỗi turn |

### Xem lại

Vào app chính → Tab **Battle History** → scroll xem tất cả battle log.

## Giới hạn V1

- ❌ Chưa có OCR auto — phải ghi tay
- ❌ Chưa có autocomplete Pokémon/Move names
- ❌ Chưa persist HP giữa các overlay session
- ❌ Giao diện còn basic

## Build

GitHub Actions tự động build khi push lên `main`:
```yaml
name: Build Champions Battle Logger APK
```

APK artifact được upload và attach vào Release.

## License

MIT
