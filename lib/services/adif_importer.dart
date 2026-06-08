import '../data/database/app_database.dart';

class AdifImporter {
  static List<ContactRecord> parse(String content) {
    final records = <ContactRecord>[];
    final lines = content.split('\n');
    bool inHeader = true; final fields = <String, String>{};
    for (final line in lines) {
      final t = line.trim(); if (t.isEmpty) continue;
      if (inHeader) { if (t.toUpperCase().contains('<EOH>')) inHeader = false; continue; }
      if (t.toUpperCase().contains('<EOR>')) { final c = _buildContact(fields); if (c != null) records.add(c); fields.clear(); continue; }
      _parseFields(t, fields);
    }
    return records;
  }
  static void _parseFields(String line, Map<String, String> fields) {
    int i = 0;
    while (i < line.length) {
      if (line[i] != '<') { i++; continue; }
      final end = line.indexOf('>', i); if (end == -1) break;
      final tag = line.substring(i + 1, end);
      // 分割标签获取名称和长度，支持可选类型标识符（如 QSO_DATE:8:D）
      final parts = tag.split(':');
      if (parts.length < 2) { i = end + 1; continue; }
      final name = parts[0].trim().toUpperCase();
      // 提取长度数字（跳过可选类型标识符如 :D, :T, :N）
      final len = int.tryParse(parts[1].trim()) ?? 0;
      final vs = end + 1;
      // 防止字段长度声明过大而吃掉下一个标签
      final nextTag = line.indexOf('<', vs);
      var ve = (vs + len).clamp(0, line.length);
      if (nextTag != -1 && ve > nextTag) {
        ve = nextTag;
      }
      if (name.isNotEmpty) fields[name] = line.substring(vs, ve).trim();
      i = ve;
    }
  }
  static ContactRecord? _buildContact(Map<String, String> f) {
    final call = f['CALL'] ?? ''; if (call.isEmpty) return null;
    final dateStr = f['QSO_DATE'] ?? f['QSO_DATE_OFF'] ?? '';
    // 直接解析日期字符串（避免 intl 包 DateFormat 的兼容性问题）
    final epochDay = _parseDateStr(dateStr) ?? DateTime.now().toUtc().millisecondsSinceEpoch ~/ 86400000;
    final freq = double.tryParse(f['FREQ'] ?? f['FREQ_RX'] ?? '0') ?? 0;
    // 解析通联时间（TIME_ON），合并 QSO_DATE + TIME_ON 生成有意义的 createdAt
    final createdAt = _parseCreatedAt(dateStr, f['TIME_ON'] ?? '');
    return ContactRecord(id: 0, dateEpochDay: epochDay, callsign: call.toUpperCase().trim(),
      frequencyMHz: freq, mode: (f['MODE'] ?? '').trim(), rstSent: (f['RST_SENT'] ?? f['RST_S'] ?? '').trim(),
      rstReceived: (f['RST_RCVD'] ?? f['RST_R'] ?? '').trim(), powerTx: (f['TX_PWR'] ?? '').trim(),
      powerRx: (f['RX_PWR'] ?? '').trim(), notes: (f['COMMENT'] ?? f['NOTES'] ?? f['QSLMSG'] ?? '').trim(), createdAt: createdAt);
  }
  /// 解析日期字符串，支持 yyyyMMdd / yyyy-MM-dd / yyyy/MM/dd 等格式
  /// 使用 DateTime.utc 以与导出保持一致
  static int? _parseDateStr(String s) {
    if (s.isEmpty) return null;
    // 提取前8位数字
    String digits = '';
    for (int i = 0; i < s.length && digits.length < 8; i++) {
      final ch = s[i];
      if (ch.codeUnitAt(0) >= 48 && ch.codeUnitAt(0) <= 57) {
        digits += ch;
      }
    }
    if (digits.length < 8) return null;
    final y = int.tryParse(digits.substring(0, 4));
    final m = int.tryParse(digits.substring(4, 6));
    final d = int.tryParse(digits.substring(6, 8));
    if (y == null || m == null || d == null) return null;
    if (m < 1 || m > 12 || d < 1 || d > 31) return null;
    // 使用 UTC 午夜（导出时也使用 isUtc: true）
    return DateTime.utc(y, m, d).millisecondsSinceEpoch ~/ 86400000;
  }
  /// 解析 QSO_DATE + TIME_ON 生成 createdAt 时间戳（毫秒）
  /// TIME_ON 格式通常为 HHmmss 或 HHmm
  /// 返回 UTC 毫秒时间戳
  static int _parseCreatedAt(String dateStr, String timeStr) {
    // 先解析日期
    int? y, m, d;
    String dateDigits = '';
    for (int i = 0; i < dateStr.length && dateDigits.length < 8; i++) {
      final ch = dateStr[i];
      if (ch.codeUnitAt(0) >= 48 && ch.codeUnitAt(0) <= 57) dateDigits += ch;
    }
    if (dateDigits.length >= 8) {
      y = int.tryParse(dateDigits.substring(0, 4));
      m = int.tryParse(dateDigits.substring(4, 6));
      d = int.tryParse(dateDigits.substring(6, 8));
    }
    y ??= DateTime.now().year;
    m ??= DateTime.now().month;
    d ??= DateTime.now().day;
    // 解析时间
    int hh = 0, mm = 0, ss = 0;
    String timeDigits = '';
    for (int i = 0; i < timeStr.length && timeDigits.length < 6; i++) {
      final ch = timeStr[i];
      if (ch.codeUnitAt(0) >= 48 && ch.codeUnitAt(0) <= 57) timeDigits += ch;
    }
    if (timeDigits.length >= 4) {
      hh = int.tryParse(timeDigits.substring(0, 2)) ?? 0;
      mm = int.tryParse(timeDigits.substring(2, 4)) ?? 0;
      if (timeDigits.length >= 6) ss = int.tryParse(timeDigits.substring(4, 6)) ?? 0;
    }
    if (m < 1 || m > 12) m = 1;
    if (d < 1 || d > 31) d = 1;
    if (hh < 0 || hh > 23) hh = 0;
    if (mm < 0 || mm > 59) mm = 0;
    if (ss < 0 || ss > 59) ss = 0;
    return DateTime.utc(y!, m!, d!, hh, mm, ss).millisecondsSinceEpoch;
  }
}
