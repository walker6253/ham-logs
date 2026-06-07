path = r"lib\features\log_entry\log_entry_screen.dart"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# Fix: change SizedBox(width: bw * 0.xx) to Expanded(flex: xx)
# Left column
old_left = "        SizedBox(\n          width: bw * 0.55,\n          child: Column(children: ["
new_left = "        Expanded(\n          flex: 55,\n          child: Column(children: ["
content = content.replace(old_left, new_left)

# Right column  
old_right = "        SizedBox(\n          width: bw * 0.45,\n          child: _buildContactList(contactsAsync, surfaceColor, borderColor, textPrimary, textSecondary, textMuted, isDark),"
new_right = "        Expanded(\n          flex: 45,\n          child: _buildContactList(contactsAsync, surfaceColor, borderColor, textPrimary, textSecondary, textMuted, isDark),"
content = content.replace(old_right, new_right)

# Also need to remove the now-unused bw parameter from _wideLayout
old_sig = "  Widget _wideLayout(double bw, AsyncValue<List<ContactRecord>> contactsAsync, Color bgColor, Color surfaceColor, Color surfaceLightColor, Color textPrimary, Color textSecondary, Color textMuted, Color borderColor, bool isDark) {"
new_sig = "  Widget _wideLayout(AsyncValue<List<ContactRecord>> contactsAsync, Color bgColor, Color surfaceColor, Color surfaceLightColor, Color textPrimary, Color textSecondary, Color textMuted, Color borderColor, bool isDark) {"
content = content.replace(old_sig, new_sig)

# Update call site
old_call = "return _wideLayout(bw, contactsAsync, bgColor, surfaceColor, surfaceLightColor, textPrimary, textSecondary, textMuted, borderColor, isDark);"
new_call = "return _wideLayout(contactsAsync, bgColor, surfaceColor, surfaceLightColor, textPrimary, textSecondary, textMuted, borderColor, isDark);"
content = content.replace(old_call, new_call)

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
print("Done")