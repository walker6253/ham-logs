import 'package:flutter/material.dart';

/// 窗口宽度尺寸分类 — 对齐原 Android 项目的 WindowWidthSizeClass
enum WidthClass { compact, medium, expanded }

WidthClass widthClassOf(BuildContext context) {
  final w = MediaQuery.of(context).size.width;
  if (w < 600) return WidthClass.compact;
  if (w < 840) return WidthClass.medium;
  return WidthClass.expanded;
}

/// 根据宽度级别返回横向 padding
double responsiveHPadding(BuildContext context) {
  return switch (widthClassOf(context)) {
    WidthClass.expanded => 48,
    WidthClass.medium => 24,
    WidthClass.compact => 12,
  };
}

/// 统计页面专用横向 padding
double statsHPadding(BuildContext context) {
  return switch (widthClassOf(context)) {
    WidthClass.expanded => 40,
    WidthClass.medium => 24,
    WidthClass.compact => 16,
  };
}

/// 是否宽屏（非 compact）
bool isWideScreen(BuildContext context) =>
    widthClassOf(context) != WidthClass.compact;

/// 内容最大宽度（Expanded 时居中限制）
double? contentMaxWidth(BuildContext context) {
  return widthClassOf(context) == WidthClass.expanded ? 1080.0 : null;
}

/// 统计页总览卡片列数
int overviewColumns(BuildContext context) {
  return switch (widthClassOf(context)) {
    WidthClass.expanded => 6,
    WidthClass.medium => 4,
    WidthClass.compact => 2,
  };
}

/// 图表高度
double chartHeight(BuildContext context) {
  return widthClassOf(context) == WidthClass.compact ? 140.0 : 200.0;
}

/// 平板双列布局
bool useTwoColumns(BuildContext context) =>
    widthClassOf(context) == WidthClass.expanded;

/// 排版缩放系数
double typeScale(BuildContext context) {
  return switch (widthClassOf(context)) {
    WidthClass.expanded => 1.1,
    WidthClass.medium => 1.05,
    WidthClass.compact => 1.0,
  };
}
