import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

// =====================================================
// Student class (encapsulation + saara business logic yahan)
// =====================================================
class Student {
    private int studentId;
    private String studentName;
    private String email;
    private String courseName;
    private double programmingScore;
    private double mathScore;
    private List<String> technicalSkills = new ArrayList<>();

    public Student(int studentId, String studentName, String email, String courseName,
                   double programmingScore, double mathScore, List<String> skills) {
        setStudentId(studentId);
        setStudentName(studentName);
        setEmail(email);
        setCourseName(courseName);
        setProgrammingScore(programmingScore);
        setMathScore(mathScore);
        if (skills != null) {
            for (String s : skills) {
                addSkill(s);
            }
        }
    }

    // ----- Getters -----
    public int getStudentId()            { return studentId; }
    public String getStudentName()       { return studentName; }
    public String getEmail()             { return email; }
    public String getCourseName()        { return courseName; }
    public double getProgrammingScore()  { return programmingScore; }
    public double getMathScore()         { return mathScore; }
    public List<String> getTechnicalSkills() {
        return Collections.unmodifiableList(technicalSkills);
    }

    // ----- Setters with validation (Req 8) -----
    public void setStudentId(int id) {
        if (id <= 0) throw new IllegalArgumentException("Student ID must be positive.");
        this.studentId = id;
    }

    public void setStudentName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be empty.");
        this.studentName = name.trim();
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@") || !email.contains("."))
            throw new IllegalArgumentException("Invalid email address.");
        this.email = email.trim();
    }

    public void setCourseName(String course) {
        if (course == null || course.trim().isEmpty())
            throw new IllegalArgumentException("Course cannot be empty.");
        this.courseName = course.trim();
    }

    public void setProgrammingScore(double score) {
        checkScore(score);
        this.programmingScore = score;
    }

    public void setMathScore(double score) {
        checkScore(score);
        this.mathScore = score;
    }

    private void checkScore(double score) {
        if (score < 0 || score > 100)
            throw new IllegalArgumentException("Score must be between 0 and 100.");
    }

    public boolean addSkill(String skill) {
        if (skill == null || skill.trim().isEmpty()) return false;
        String cleaned = skill.trim();
        for (String s : technicalSkills) {
            if (s.equalsIgnoreCase(cleaned)) return false;
        }
        technicalSkills.add(cleaned);
        return true;
    }

    // ----- Req 1: average -----
    public double getAverageScore() {
        return (programmingScore + mathScore) / 2.0;
    }

    // ----- Req 2: performance category -----
    public String getCategory() {
        double avg = getAverageScore();
        if (avg >= 85)      return "Excellent";
        else if (avg >= 70) return "Good";
        else if (avg >= 50) return "Satisfactory";
        else                return "Needs Improvement";
    }

    // ----- Req 3: stronger subject -----
    public String getStrongerSubject() {
        if (programmingScore > mathScore)      return "Programming";
        else if (mathScore > programmingScore) return "Mathematics";
        else                                   return "Equal";
    }

    // Skills ko lowercase sorted set me convert (comparison ke liye)
    public Set<String> getSkillSet() {
        Set<String> set = new TreeSet<>();
        for (String s : technicalSkills) {
            set.add(s.toLowerCase());
        }
        return set;
    }

    public void displayDetails() {
        System.out.println("----------------------------------");
        System.out.println("ID        : " + studentId);
        System.out.println("Name      : " + studentName);
        System.out.println("Email     : " + email);
        System.out.println("Course    : " + courseName);
        System.out.println("Programming: " + programmingScore + " | Math: " + mathScore);
        System.out.printf("Average   : %.2f (%s)%n", getAverageScore(), getCategory());
        System.out.println("Stronger  : " + getStrongerSubject());
        System.out.println("Skills    : " + technicalSkills);
    }
}

// =====================================================

// =====================================================
public class StudentJDBC {

   
    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String URL  = "jdbc:mysql://localhost:3306/student_db";
    private static final String USER = "root";
    private static final String PASS = "Divya@23#4";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    
    private static List<Student> loadStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        Map<Integer, List<String>> skillMap = new HashMap<>();

        try (Connection con = getConnection(); Statement st = con.createStatement()) {

            try (ResultSet rs = st.executeQuery("SELECT student_id, skill_name FROM student_skills")) {
                while (rs.next()) {
                    int id = rs.getInt("student_id");
                    if (!skillMap.containsKey(id)) {
                        skillMap.put(id, new ArrayList<String>());
                    }
                    skillMap.get(id).add(rs.getString("skill_name"));
                }
            }

            try (ResultSet rs = st.executeQuery("SELECT * FROM students ORDER BY student_id")) {
                while (rs.next()) {
                    int id = rs.getInt("student_id");
                    List<String> skills = skillMap.get(id);
                    list.add(new Student(
                            id,
                            rs.getString("student_name"),
                            rs.getString("email"),
                            rs.getString("course_name"),
                            rs.getDouble("programming_score"),
                            rs.getDouble("math_score"),
                            skills));
                }
            }
        }
        return list;
    }

    // ---------- JDBC: student insert karo (Req 8 ke saath) ----------
    private static void addStudent(Scanner sc) {
        try {
            System.out.print("Student ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Name: ");
            String name = sc.nextLine();
            System.out.print("Email: ");
            String email = sc.nextLine();
            System.out.print("Course: ");
            String course = sc.nextLine();
            System.out.print("Programming score (0-100): ");
            double prog = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Math score (0-100): ");
            double math = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Skills (comma separated): ");
            String skillLine = sc.nextLine();

            List<String> skills = new ArrayList<>();
            for (String s : skillLine.split(",")) {
                skills.add(s);
            }

            // Student object banate hi validation chal jata hai (exception throw hoga)
            Student s = new Student(id, name, email, course, prog, math, skills);

            // Duplicate ID / email check Java me
            for (Student existing : loadStudents()) {
                if (existing.getStudentId() == s.getStudentId()) {
                    System.out.println("Error: Student ID " + id + " already exists.");
                    return;
                }
                if (existing.getEmail().equalsIgnoreCase(s.getEmail())) {
                    System.out.println("Error: Email already registered.");
                    return;
                }
            }

            insertStudent(s);
            System.out.println("Student added successfully.");

        } catch (NumberFormatException e) {
            System.out.println("Error: Enter valid numbers for ID and scores.");
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ---------- Req 1, 2, 3 ----------
    private static void showReport(List<Student> students) {
        System.out.printf("%-6s %-16s %-8s %-20s %-12s%n",
                "ID", "Name", "Average", "Category", "Stronger");
        for (Student s : students) {
            System.out.printf("%-6d %-16s %-8.2f %-20s %-12s%n",
                    s.getStudentId(), s.getStudentName(), s.getAverageScore(),
                    s.getCategory(), s.getStrongerSubject());
        }
    }

    // ---------- Req 4: search by ID / email / course ----------
    private static void searchStudent(List<Student> students, Scanner sc) {
        System.out.println("Search by: 1. ID  2. Email  3. Course");
        System.out.print("Choice: ");
        String choice = sc.nextLine().trim();
        System.out.print("Enter value: ");
        String value = sc.nextLine().trim();

        boolean found = false;
        for (Student s : students) {
            boolean match = false;
            if (choice.equals("1")) {
                try {
                    match = s.getStudentId() == Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    System.out.println("ID must be a number.");
                    return;
                }
            } else if (choice.equals("2")) {
                match = s.getEmail().equalsIgnoreCase(value);
            } else if (choice.equals("3")) {
                match = s.getCourseName().toLowerCase().contains(value.toLowerCase());
            } else {
                System.out.println("Invalid choice.");
                return;
            }
            if (match) {
                s.displayDetails();
                found = true;
            }
        }
        if (!found) System.out.println("No student found.");
    }

    // ---------- Req 5: merit list (average descending) ----------
    private static void meritList(List<Student> students) {
        List<Student> sorted = new ArrayList<>(students); // original list ko mat chhedo
        Collections.sort(sorted, new Comparator<Student>() {
            @Override
            public int compare(Student a, Student b) {
                return Double.compare(b.getAverageScore(), a.getAverageScore()); // descending
            }
        });

        System.out.printf("%-6s %-6s %-16s %-8s%n", "Rank", "ID", "Name", "Average");
        int rank = 0;
        double prevAvg = -1;
        for (int i = 0; i < sorted.size(); i++) {
            Student s = sorted.get(i);
            if (s.getAverageScore() != prevAvg) {
                rank = i + 1; // same average -> same rank
                prevAvg = s.getAverageScore();
            }
            System.out.printf("%-6d %-6d %-16s %-8.2f%n",
                    rank, s.getStudentId(), s.getStudentName(), s.getAverageScore());
        }
    }

    // ---------- Req 6a: common skills share karne wale students ----------
    private static void commonSkills(List<Student> students) {
        boolean any = false;
        for (int i = 0; i < students.size(); i++) {
            for (int j = i + 1; j < students.size(); j++) {
                Set<String> common = new TreeSet<>(students.get(i).getSkillSet());
                common.retainAll(students.get(j).getSkillSet()); // intersection
                if (!common.isEmpty()) {
                    System.out.println(students.get(i).getStudentName() + " & "
                            + students.get(j).getStudentName() + " -> " + common);
                    any = true;
                }
            }
        }
        if (!any) System.out.println("No students share common skills.");
    }

    // ---------- Req 6b: bilkul same skill set wale students ----------
    private static void identicalSkillSets(List<Student> students) {
        Map<Set<String>, List<String>> groups = new LinkedHashMap<>();
        for (Student s : students) {
            Set<String> key = s.getSkillSet();
            if (key.isEmpty()) continue;
            if (!groups.containsKey(key)) {
                groups.put(key, new ArrayList<String>());
            }
            groups.get(key).add(s.getStudentName());
        }

        boolean any = false;
        for (Map.Entry<Set<String>, List<String>> e : groups.entrySet()) {
            if (e.getValue().size() > 1) {
                System.out.println(e.getKey() + " -> " + e.getValue());
                any = true;
            }
        }
        if (!any) System.out.println("No students have identical skill sets.");
    }

    // ---------- Req 7: programming me weak, math >= 75 ----------
    private static void weakProgrammingStrongMath(List<Student> students) {
        boolean any = false;
        for (Student s : students) {
            if (s.getProgrammingScore() < 50 && s.getMathScore() >= 75) {
                System.out.println(s.getStudentName() + " (ID " + s.getStudentId() + ") | Programming: "
                        + s.getProgrammingScore() + " | Math: " + s.getMathScore());
                any = true;
            }
        }
        if (!any) System.out.println("No matching students.");
    }

    // ---------- JDBC: ek student (aur uske skills) DB me insert karo ----------
    private static void insertStudent(Student s) throws SQLException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false); // transaction: sab ya kuch nahi
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO students (student_id, student_name, email, course_name, "
                      + "programming_score, math_score) VALUES (?,?,?,?,?,?)")) {
                    ps.setInt(1, s.getStudentId());
                    ps.setString(2, s.getStudentName());
                    ps.setString(3, s.getEmail());
                    ps.setString(4, s.getCourseName());
                    ps.setDouble(5, s.getProgrammingScore());
                    ps.setDouble(6, s.getMathScore());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO student_skills (student_id, skill_name) VALUES (?,?)")) {
                    for (String skill : s.getTechnicalSkills()) {
                        ps.setInt(1, s.getStudentId());
                        ps.setString(2, skill);
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---------- JDBC: database + tables Java se hi banao (SQL file ki zaroorat nahi) ----------
    private static boolean setupDatabase() {
        try (Connection con = DriverManager.getConnection(SERVER_URL, USER, PASS);
             Statement st = con.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS student_db");
        } catch (SQLException e) {
            System.out.println("Cannot connect to MySQL server: " + e.getMessage());
            System.out.println("Check: MySQL running hai? Username/password sahi hai? Connector JAR add kiya?");
            return false;
        }

        try (Connection con = getConnection(); Statement st = con.createStatement()) {
            // Req 8: PRIMARY KEY (duplicate ID nahi) + CHECK (score 0-100)
            st.executeUpdate("CREATE TABLE IF NOT EXISTS students ("
                    + "student_id INT PRIMARY KEY, "
                    + "student_name VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(100) NOT NULL UNIQUE, "
                    + "course_name VARCHAR(100) NOT NULL, "
                    + "programming_score DOUBLE NOT NULL, "
                    + "math_score DOUBLE NOT NULL, "
                    + "CONSTRAINT chk_prog CHECK (programming_score BETWEEN 0 AND 100), "
                    + "CONSTRAINT chk_math CHECK (math_score BETWEEN 0 AND 100))");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS student_skills ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "student_id INT NOT NULL, "
                    + "skill_name VARCHAR(50) NOT NULL, "
                    + "UNIQUE (student_id, skill_name), "
                    + "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE)");

            seedSampleData();
            return true;
        } catch (SQLException e) {
            System.out.println("Setup error: " + e.getMessage());
            return false;
        }
    }

    // Table khaali ho tabhi sample data daalo (baar-baar run karne pe duplicate nahi banega)
    private static void seedSampleData() throws SQLException {
        if (!loadStudents().isEmpty()) {
            return;
        }
        insertStudent(new Student(101, "Riya Sharma", "riya@example.com", "B.Tech CSE", 88, 92,
                java.util.Arrays.asList("Java", "C++", "React")));
        insertStudent(new Student(102, "Aman Verma", "aman@example.com", "B.Tech IT", 75, 68,
                java.util.Arrays.asList("Python", "SQL")));
        insertStudent(new Student(103, "Neha Gupta", "neha@example.com", "B.Tech CSE", 45, 80,
                java.util.Arrays.asList("Java", "Python")));
        insertStudent(new Student(104, "Karan Singh", "karan@example.com", "BCA", 60, 55,
                java.util.Arrays.asList("Python", "SQL")));
        insertStudent(new Student(105, "Pooja Mishra", "pooja@example.com", "B.Tech IT", 30, 40,
                java.util.Arrays.asList("C++")));
        insertStudent(new Student(106, "Rahul Yadav", "rahul@example.com", "BCA", 92, 78,
                java.util.Arrays.asList("Java", "C++", "React")));
        System.out.println("Sample data inserted.");
    }

    // ---------- Menu ----------
    public static void main(String[] args) {
        if (!setupDatabase()) {
            return; // DB connect nahi hua, aage chalane ka fayda nahi
        }
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== STUDENT MANAGEMENT (JDBC) =====");
            System.out.println("1. Add student");
            System.out.println("2. Average, category & stronger subject");
            System.out.println("3. Search student (ID / email / course)");
            System.out.println("4. Merit list");
            System.out.println("5. Students with common skills");
            System.out.println("6. Students with identical skill sets");
            System.out.println("7. Weak in programming, math >= 75");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String ch = sc.nextLine().trim();

            if (ch.equals("0")) {
                System.out.println("Bye!");
                break;
            }
            if (ch.equals("1")) {
                addStudent(sc);
                continue;
            }

            try {
                List<Student> students = loadStudents(); // har baar fresh data DB se
                switch (ch) {
                    case "2": showReport(students); break;
                    case "3": searchStudent(students, sc); break;
                    case "4": meritList(students); break;
                    case "5": commonSkills(students); break;
                    case "6": identicalSkillSets(students); break;
                    case "7": weakProgrammingStrongMath(students); break;
                    default:  System.out.println("Invalid choice.");
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            }
        }
        sc.close();
    }
}