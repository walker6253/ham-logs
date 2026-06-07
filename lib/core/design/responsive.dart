import 'package:flutter/material.dart';

enum WidthClass { compact, medium, expanded }

/// 横屏手机视为大屏
bool _effectiveExpanded(BuildContext context) {
  final size = MediaQuery.of(context).size;
  final wc = widthClassOf(context);
  return wc == WidthClass.expanded || (size.width > size.height && wc == WidthClass.medium);
}

WidthClass widthClassOf(BuildContext context) {
  final w = MediaQuery.of(context).size.width;
  if (w < 600) return WidthClass.compact;
  if (w < 840) return WidthClass.medium;
  return WidthClass.expanded;
}

double responsiveHPadding(BuildContext context) {
  return switch (widthClassOf(context)) {
    WidthClass.expanded => 48,
    WidthClass.medium => 24,
    WidthClass.compact => 12,
  };
}

double statsHPadding(BuildContext context) {
  return _effectiveExpanded(context) ? 40 : 16;
}

bool isWideScreen(BuildContext context) {
  final size = MediaQuery.of(context).size;
  return size.width > size.height || widthClassOf(context) != WidthClass.compact;
}

double? contentMaxWidth(BuildContext context) {
  return _effectiveExpanded(context) ? 1080.0 : null;
}

int overviewColumns(BuildContext context) {
  if (_effectiveExpanded(context)) return 6;
  return switch (widthClassOf(context)) {
    WidthClass.medium => 4,
    WidthClass.compact => 2,
    WidthClass.expanded => 6,
  };
}

double chartHeight(BuildContext context) {
  return widthClassOf(context) == WidthClass.compact ? 140.0 : 200.0;
}

bool useTwoColumns(BuildContext context) => _effectiveExpanded(context);

double typeScale(BuildContext context) {
  return _effectiveExpanded(context) ? 1.1
      : widthClassOf(context) == WidthClass.medium ? 1.05 : 1.0;
}
