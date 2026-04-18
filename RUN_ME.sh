#!/bin/bash
# ============================================================
#  ONE-CLICK SETUP + RUN  (Git Bash on Windows)
#  Double-click or:  bash RUN_ME.sh
# ============================================================

DIR="$(cd "$(dirname "$0")" && pwd)"
DB_USER="root"
DB_PASS="MrDash@4816"
JAR="$DIR/lib/mysql-connector-j-9.6.0.jar"
SRC="$DIR/src"
OUT="$DIR/out"

echo "============================================"
echo "  Course Registration System"
echo "============================================"
echo ""

# STEP 1 — Setup Database
echo "[1/3] Setting up database..."
mysql -u"$DB_USER" -p"$DB_PASS" < "$DIR/database.sql" 2>/dev/null
if [ $? -ne 0 ]; then
    echo ""
    echo "  ERROR: Could not connect to MySQL."
    echo "  Make sure MySQL is running and try again."
    read -p "Press Enter to exit..."
    exit 1
fi
echo "  Database ready!"
echo ""

# STEP 2 — Compile
echo "[2/3] Compiling Java files..."
mkdir -p "$OUT"
find "$SRC" -name "*.java" > "$DIR/sources.txt"
javac -cp "$JAR" -d "$OUT" @"$DIR/sources.txt" 2>&1
if [ $? -ne 0 ]; then
    echo ""
    echo "  ERROR: Compilation failed. See errors above."
    read -p "Press Enter to exit..."
    exit 1
fi
echo "  Compilation successful!"
echo ""

# STEP 3 — Run
echo "[3/3] Launching application..."
echo ""
echo "  Student logins:  S001 / S002 / S003"
echo "  Faculty logins:  smith123 / patel123 / lee123 / rao123 / chen123"
echo ""
java -cp "$OUT:$JAR" com.university.Main
