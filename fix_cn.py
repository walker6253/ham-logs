import re

with open(r"lib\features\log_entry\log_entry_screen.dart", "r", encoding="utf-8") as f:
    content = f.read()

# Map of garbled -> correct Chinese
fixes = {
    "\u6dc6\u6f5e\u5b9b\u9385\u7c3c\u52bb": "\u4fdd\u5b58\u6210\u529f",
    "\u6dc6\u6f5e\u5b9b\u95ac\u6d93\u52c8": "\u4fdd\u5b58\u901a\u8054",
    "\u6f81\u756b\u6b7a": "\u5929\u7ebf",
    "\u6d5b\u5a6d\u65e5\u95ac\u6d93\u52c8": "\u4eca\u65e5\u901a\u8054",
    "\u93c3\u728b\u5377\u9365\u3086\u8a58\u97c1": "\u65e0\u5386\u53f2\u8bb0\u5f55",
    "\u93c3\u6682\u748b\u73ab\u93b1": "\u6682\u65e0\u8bb0\u5f55",
    "\u9365\u8a3d\u6f7a\u6f8a\u89e6": "\u52a0\u8f7d\u5931\u8d25",
    "\u93c3\u6761": "\u6761",
    "\u7e9c\u7867\u5220\u9664": "\u786e\u5b9a\u5220\u9664",
}

# Try a different approach: find and replace each garbled pattern
# The issue is that these are raw bytes, so let me use latin-1 encoding tricks
# Actually the simpler approach: just check each line and replace if it contains garbled text

import re

# Find all string literals with non-ASCII and fix common patterns
# This is a heuristic approach

count = 0
for old, new in fixes.items():
    if old in content:
        content = content.replace(old, new)
        count += 1

print(f"Fixed {count} items")
with open(r"lib\features\log_entry\log_entry_screen.dart", "w", encoding="utf-8") as f:
    f.write(content)
print("Done")