package com.university;

import java.sql.*;
import java.util.*;

/**
 * DatabaseManager — MySQL-backed data layer.
 * All changes are persisted to course_db immediately.
 */
public class DatabaseManager {

    // ── connection config ──────────────────────────────────────────────────
    private static final String URL  = "jdbc:mysql://localhost:3306/course_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "MrDash@4816";

    private static Connection conn;

    // ══════════════════════════════════════════════════════════════════════
    //  CONNECTION
    // ══════════════════════════════════════════════════════════════════════

    public static void connect() throws SQLException {
    if (conn == null || conn.isClosed()) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");  // 🔥 ADD THIS LINE
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found!", e);
        }

        conn = DriverManager.getConnection(URL, USER, PASS);
    }
}
    public static void disconnect() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SEED DATA  (runs once; skips if already present)
    // ══════════════════════════════════════════════════════════════════════

    public static void seedIfEmpty() throws SQLException {
        connect();

        // --- students ---
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                st.executeUpdate("INSERT INTO students (roll_number, name, email, password) VALUES " +
                    "('S001','Alice Johnson','alice@uni.edu','alice123')," +
                    "('S002','Bob Martinez','bob@uni.edu','bob123')," +
                    "('S003','Carol White','carol@uni.edu','carol123')");
            }
        }

        // --- faculty ---
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM faculty")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                st.executeUpdate("INSERT INTO faculty (name, password) VALUES " +
                    "('Dr. Smith','smith123')," +
                    "('Dr. Patel','patel123')," +
                    "('Dr. Lee','lee123')," +
                    "('Dr. Rao','rao123')," +
                    "('Dr. Chen','chen123')");
            }
        }

        // --- courses ---
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM courses")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                st.executeUpdate("INSERT INTO courses (course_name, faculty_name, capacity, prerequisites) VALUES " +
                    "('Data Structures','Dr. Smith',30,'None')," +
                    "('Algorithms','Dr. Smith',25,'Data Structures')," +
                    "('Database Systems','Dr. Patel',35,'None')," +
                    "('Web Development','Dr. Patel',40,'None')," +
                    "('Machine Learning','Dr. Lee',20,'Algorithms')," +
                    "('Computer Networks','Dr. Lee',30,'None')," +
                    "('Operating Systems','Dr. Rao',28,'None')," +
                    "('Software Engineering','Dr. Rao',32,'None')," +
                    "('Computer Graphics','Dr. Chen',22,'None')," +
                    "('Cybersecurity','Dr. Chen',25,'None')");
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  STUDENT AUTH
    // ══════════════════════════════════════════════════════════════════════

    /** Returns student name if roll number exists, else null */
    public static String loginStudent(String rollNumber) throws SQLException {
        connect();
        String sql = "SELECT name FROM students WHERE roll_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("name") : null;
            }
        }
    }

    public static int getStudentId(String rollNumber) throws SQLException {
        connect();
        String sql = "SELECT id FROM students WHERE roll_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : -1;
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  FACULTY AUTH
    // ══════════════════════════════════════════════════════════════════════

    public static boolean loginFaculty(String name, String password) throws SQLException {
        connect();
        String sql = "SELECT id FROM faculty WHERE name = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static List<String> getFacultyNames() throws SQLException {
        connect();
        List<String> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT name FROM faculty ORDER BY name")) {
            while (rs.next()) list.add(rs.getString("name"));
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  COURSES
    // ══════════════════════════════════════════════════════════════════════

    /** Returns all courses as Object[][] for JTable: {id, name, faculty, enrolled, capacity, prerequisites} */
    public static Object[][] getAllCoursesTable() throws SQLException {
        connect();
        String sql = """
            SELECT c.id, c.course_name, c.faculty_name,
                   (SELECT COUNT(*) FROM registrations r WHERE r.course_id = c.id) AS enrolled,
                   c.capacity, c.prerequisites
            FROM courses c ORDER BY c.id
            """;
        List<Object[]> rows = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("course_name"),
                    rs.getString("faculty_name"),
                    rs.getInt("enrolled"),
                    rs.getInt("capacity"),
                    rs.getString("prerequisites")
                });
            }
        }
        return rows.toArray(new Object[0][]);
    }

    /** Courses for a specific faculty member */
    public static Object[][] getCoursesForFaculty(String facultyName) throws SQLException {
        connect();
        String sql = """
            SELECT c.id, c.course_name,
                   (SELECT COUNT(*) FROM registrations r WHERE r.course_id = c.id) AS enrolled,
                   c.capacity, c.prerequisites
            FROM courses c WHERE c.faculty_name = ? ORDER BY c.id
            """;
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, facultyName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("course_name"),
                        rs.getInt("enrolled"),
                        rs.getInt("capacity"),
                        rs.getString("prerequisites")
                    });
                }
            }
        }
        return rows.toArray(new Object[0][]);
    }

    public static boolean updateCourseCapacity(int courseId, int newCapacity) throws SQLException {
        connect();
        // Cannot reduce below current enrollment
        String checkSql = "SELECT COUNT(*) FROM registrations WHERE course_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (newCapacity < rs.getInt(1)) return false;
            }
        }
        String sql = "UPDATE courses SET capacity = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newCapacity);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
        return true;
    }

    public static void updateCoursePrerequisites(int courseId, String prereqs) throws SQLException {
        connect();
        String sql = "UPDATE courses SET prerequisites = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prereqs.isBlank() ? "None" : prereqs.trim());
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ENROLLMENTS
    // ══════════════════════════════════════════════════════════════════════

    /** Returns course IDs the student is already enrolled in */
    public static Set<Integer> getEnrolledCourseIds(int studentId) throws SQLException {
        connect();
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT course_id FROM registrations WHERE student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt("course_id"));
            }
        }
        return ids;
    }

    /**
     * Enroll student in a course.
     * Returns: "OK" | "ALREADY_ENROLLED" | "FULL"
     */
    public static String enrollStudent(int studentId, int courseId) throws SQLException {
        connect();
        // Already enrolled?
        String chk = "SELECT id FROM registrations WHERE student_id=? AND course_id=?";
        try (PreparedStatement ps = conn.prepareStatement(chk)) {
            ps.setInt(1, studentId); ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return "ALREADY_ENROLLED";
            }
        }
        // Full?
        String full = "SELECT capacity, (SELECT COUNT(*) FROM registrations WHERE course_id=c.id) AS enrolled FROM courses c WHERE c.id=?";
        try (PreparedStatement ps = conn.prepareStatement(full)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("enrolled") >= rs.getInt("capacity"))
                    return "FULL";
            }
        }
        // Insert
        String ins = "INSERT INTO registrations (student_id, course_id) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            ps.setInt(1, studentId); ps.setInt(2, courseId);
            ps.executeUpdate();
        }
        return "OK";
    }

    /**
     * Drop a course.
     * Returns true if a row was deleted, false if not enrolled.
     */
    public static boolean dropCourse(int studentId, int courseId) throws SQLException {
        connect();
        String sql = "DELETE FROM registrations WHERE student_id=? AND course_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId); ps.setInt(2, courseId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Enrolled courses for a student: {id, course_name, faculty, prerequisites} */
    public static Object[][] getStudentEnrollments(int studentId) throws SQLException {
        connect();
        String sql = """
            SELECT c.id, c.course_name, c.faculty_name, c.prerequisites
            FROM courses c
            JOIN registrations r ON r.course_id = c.id
            WHERE r.student_id = ?
            ORDER BY c.course_name
            """;
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("course_name"),
                        rs.getString("faculty_name"),
                        rs.getString("prerequisites")
                    });
                }
            }
        }
        return rows.toArray(new Object[0][]);
    }

    /** Roster of students enrolled in a course: {roll_number, name, email} */
    public static Object[][] getCourseRoster(int courseId) throws SQLException {
        connect();
        String sql = """
            SELECT s.roll_number, s.name, s.email
            FROM students s
            JOIN registrations r ON r.student_id = s.id
            WHERE r.course_id = ?
            ORDER BY s.name
            """;
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getString("roll_number"),
                        rs.getString("name"),
                        rs.getString("email")
                    });
                }
            }
        }
        return rows.toArray(new Object[0][]);
    }
}
