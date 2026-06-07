import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_animate/flutter_animate.dart';
import '../../core/design/app_colors.dart';
import '../../core/design/responsive.dart';
import '../../data/database/app_database.dart';
import '../../services/update_checker.dart';
import '../../data/preferences/app_preferences.dart';
import '../../data/providers.dart';
import 'package:intl/intl.dart';

final datesProvider = FutureProvider<List<({int date, String label, int count, bool isToday})>>((ref) async {
  ref.watch(homeRefreshNotifier);
  final db = ref.watch(dbProvider);
  final items = await db.contactDao.getAllDatesWithCount();
  final today = DateTime.now();
  final todayEpoch = today.millisecondsSinceEpoch ~/ 86400000;
  return items.map((d) {
    final dt = DateTime.fromMillisecondsSinceEpoch(d.dateEpochDay * 86400000);
    String label;
    if (d.dateEpochDay == todayEpoch) {
      label = '\u4ECA\u5929'; // 今天
    } else if (d.dateEpochDay == todayEpoch - 1) {
      label = '\u6628\u5929'; // 昨天
    } else {
      label = '${dt.year}-${dt.month.toString().padLeft(2, '0')}-${dt.day.toString().padLeft(2, '0')}';
    }
    return (date: d.dateEpochDay, label: label, count: d.count, isToday: d.dateEpochDay == todayEpoch);
  }).toList();
});

// 格式化日期标签显示
String _formatDateLabel(String label, bool isToday) {
  if (isToday) return '\u4ECA\u5929';
  if (label == '\u6628\u5929') return '\u6628\u5929';
  final parts = label.split('-');
  if (parts.length == 3) {
    return '${parts[0]}\u5E74${int.parse(parts[1])}\u6708${int.parse(parts[2])}\u65E5';
  }
  return label;
}

class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});
  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  @override
  void initState() {
    super.initState();
    _checkUpdate();
  }

  Future<void> _checkUpdate() async {
    await AppPreferences.init();
    final today = DateFormat('yyyy-MM-dd').format(DateTime.now());
    if (AppPreferences.lastUpdateCheckDate != today) {
      final info = await UpdateChecker.check();
      AppPreferences.lastUpdateCheckDate = today;
      if (info.hasUpdate && mounted) {
        showDialog(context: context, builder: (ctx) => AlertDialog(
          backgroundColor: Theme.of(ctx).scaffoldBackgroundColor == AppColors.background ? AppColors.surface : Colors.white,
          title: Text('\u53D1\u73B0\u65B0\u7248\u672C v${info.latestVersion}', style: TextStyle(color: AppColors.amber)),
          content: Text('\u5F53\u524D\u7248\u672C: v${info.currentVersion}\n${info.body}', style: TextStyle(color: AppColors.textSecondary, fontSize: 12)),
          actions: [
            TextButton(onPressed: () => Navigator.pop(ctx), child: Text('\u5173\u95ED', style: TextStyle(color: AppColors.textMuted))),
          ],
        ));
      }
    }
  }

  Future<void> _pickDateAndGo() async {
    final now = DateTime.now();
    final picked = await showDatePicker(
      context: context,
      initialDate: now,
      firstDate: DateTime(2000),
      lastDate: now,
      locale: const Locale('zh'),
      builder: (ctx, child) => Theme(data: Theme.of(ctx).copyWith(
        colorScheme: Theme.of(ctx).brightness == Brightness.dark
          ? ColorScheme.dark(primary: AppColors.primary, surface: AppColors.darkSurface)
          : ColorScheme.light(primary: AppColors.primary, surface: AppColors.lightSurface),
        datePickerTheme: DatePickerThemeData(
          cancelButtonStyle: ButtonStyle(foregroundColor: WidgetStatePropertyAll(Colors.grey)),
          confirmButtonStyle: ButtonStyle(foregroundColor: WidgetStatePropertyAll(AppColors.primary)),
        ),
      ), child: child!),
    );
    if (picked != null && mounted) {
      final epoch = picked.millisecondsSinceEpoch ~/ 86400000;
      context.go('/log/$epoch');
    }
  }

  @override
  Widget build(BuildContext context) {
    final datesAsync = ref.watch(datesProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final bgColor = Theme.of(context).scaffoldBackgroundColor;
    final surfaceColor = isDark ? AppColors.surface : Colors.white;
    final textPrimary = isDark ? AppColors.textPrimary : const Color(0xFF1B1C1D);
    final textMuted = isDark ? AppColors.textMuted : const Color(0xFF777680);
    final borderColor = isDark ? AppColors.border : const Color(0xFFE0E0E8);
    final accentColor = AppColors.amber;
    final accentBgColor = isDark ? AppColors.amber.withAlpha(25) : AppColors.amber.withAlpha(20);
    final twoCols = useTwoColumns(context);
    final hPad = responsiveHPadding(context);

    return Scaffold(
      backgroundColor: bgColor,
      appBar: AppBar(
        backgroundColor: bgColor, scrolledUnderElevation: 0, surfaceTintColor: Colors.transparent,
        elevation: 0,
        title: Consumer(builder: (ctx, ref, _) {
          final callsign = AppPreferences.callsign;
          ref.watch(homeRefreshNotifier);
          final title = callsign.isNotEmpty ? '$callsign \u7684\u901A\u8054\u65E5\u5FD7' : '\u4E1A\u4F59\u65E0\u7EBF\u7535\u901A\u8054\u65E5\u5FD7';
          return Text(title, style: TextStyle(fontWeight: FontWeight.w700, fontSize: 18, color: textPrimary, fontFamily: 'monospace'));
        }),
        actions: [IconButton(icon: Icon(Icons.bar_chart, color: textPrimary), onPressed: () => context.go('/stats'))],
      ),
      body: datesAsync.when(
        data: (dates) => dates.isEmpty
          ? Center(child: Column(mainAxisSize: MainAxisSize.min, children: [
              Text('\u6682\u65E0\u901A\u8054\u8BB0\u5F55', style: TextStyle(color: textMuted, fontSize: 16)),
              const SizedBox(height: 8),
              Text('\u70B9\u51FB\u53F3\u4E0B\u89D2\u6309\u94AE\u5F00\u59CB\u8BB0\u5F55', style: TextStyle(color: textMuted.withAlpha(127), fontSize: 13)),
            ]))
          : twoCols
            ? GridView.builder(
                padding: EdgeInsets.fromLTRB(hPad, 8, hPad, 88),
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  crossAxisSpacing: 14,
                  mainAxisSpacing: 14,
                  childAspectRatio: 3.5,
                ),
                itemCount: dates.length,
                itemBuilder: (ctx, i) => _buildDateCard(dates[i], i, surfaceColor, textPrimary, borderColor, accentColor, accentBgColor),
              )
            : ListView.builder(
                padding: EdgeInsets.fromLTRB(hPad, 8, hPad, 88),
                itemCount: dates.length,
                itemBuilder: (ctx, i) => _buildDateCard(dates[i], i, surfaceColor, textPrimary, borderColor, accentColor, accentBgColor),
              ),
        loading: () => Center(child: CircularProgressIndicator(color: AppColors.amber)),
        error: (e, _) => Center(child: Text('\u52A0\u8F7D\u5931\u8D25', style: TextStyle(color: AppColors.alertRed))),
      ),
      floatingActionButton: FloatingActionButton(shape: const CircleBorder(),
        onPressed: _pickDateAndGo,
        backgroundColor: AppColors.primary,
        child: const Icon(Icons.add, color: Colors.white),
      ),
    );
  }

  Widget _buildDateCard(
    ({int date, String label, int count, bool isToday}) d,
    int index,
    Color surfaceColor,
    Color textPrimary,
    Color borderColor,
    Color accentColor,
    Color accentBgColor,
  ) {
    final displayLabel = _formatDateLabel(d.label, d.isToday);
    return Card(
      color: surfaceColor,
      margin: const EdgeInsets.only(bottom: 8),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10), side: BorderSide(color: borderColor.withAlpha(51))),
      elevation: 0,
      child: InkWell(
        borderRadius: BorderRadius.circular(10),
        onTap: () => context.go('/log/${d.date}'),
        child: Padding(padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
          child: Row(children: [
            if (d.isToday) ...[
              Container(width: 8, height: 8, decoration: BoxDecoration(color: textPrimary, shape: BoxShape.circle)),
              const SizedBox(width: 10),
            ],
            Expanded(child: Text(displayLabel, style: TextStyle(
              fontSize: 14, fontWeight: FontWeight.w400,
              color: textPrimary))),
            Container(padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
              decoration: BoxDecoration(color: accentBgColor, borderRadius: BorderRadius.circular(6)),
              child: Text('${d.count} \u6761', style: TextStyle(fontSize: 11, color: accentColor, fontFamily: 'monospace'))),
          ]),
        ),
      ),
    ).animate().fadeIn(duration: 300.ms, delay: (50 * index).ms).slideX(begin: 0.05, end: 0, duration: 300.ms, delay: (50 * index).ms);
  }
}
