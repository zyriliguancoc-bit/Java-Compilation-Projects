import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Mentor-centered Appointment Scheduling System
 * Save as: MentorSchedulerApp.java
 * Compile: javac MentorSchedulerApp.java
 * Run:     java MentorSchedulerApp
 */
public class MentorSchedulerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginApp().setVisible(true));
    }
}

/* ----------------------- Model Classes ----------------------- */

class User {
    private String name;
    private String email;
    private String password;

    public User(String name, String email, String password) {
        this.name = name == null ? "" : name;
        this.email = email == null ? "" : email;
        this.password = password == null ? "" : password;
    }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setName(String n) { name = n; }
    public void setPassword(String p) { password = p; }
}

class Student extends User {
    public Student(String n, String e, String p) { super(n, e, p); }
}

class Mentor extends User {
    public Mentor(String n, String e, String p) { super(n, e, p); }
}

class Session {
    private String studentName;
    private String studentEmail;
    private String mentorName;
    private String mentorEmail;
    private LocalDateTime dateTime;
    private String status; // upcoming / completed

    public Session(String stuN, String stuE, String menN, String menE, LocalDateTime dt, String st) {
        studentName = stuN; studentEmail = stuE;
        mentorName = menN; mentorEmail = menE;
        dateTime = dt; status = st;
    }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public String getMentorName() { return mentorName; }
    public String getMentorEmail() { return mentorEmail; }
    public LocalDateTime getDateTime() { return dateTime; }
    public String getStatus() { return status; }
    public void setStatus(String s) { status = s; }
}

class Assessment {
    private String studentEmail;
    private String[][] qa;
    public Assessment(String studentEmail, String[][] qa) {
        this.studentEmail = studentEmail; this.qa = qa;
    }
    public String getStudentEmail() { return studentEmail; }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String[] q : qa) sb.append(q[0]).append(": ").append(q[1]).append("\n");
        return sb.toString();
    }
}

/* ----------------------- Managers ----------------------- */

class UserManager {
    private ArrayList<User> users = new ArrayList<>();
    public boolean register(User u) {
        if (getUserByEmail(u.getEmail()) != null) return false;
        users.add(u); return true;
    }
    public User login(String email, String pass) {
        for (User u : users) if (u.getEmail().equals(email) && u.getPassword().equals(pass)) return u;
        return null;
    }
    public User getUserByEmail(String email) {
        for (User u : users) if (u.getEmail().equals(email)) return u;
        return null;
    }
}

class AssessmentManager {
    private ArrayList<Assessment> assessments = new ArrayList<>();
    public void saveAssessment(Assessment a) {
        assessments.removeIf(as -> as.getStudentEmail().equals(a.getStudentEmail()));
        assessments.add(a);
    }
    public Assessment getAssessmentByStudentEmail(String email) {
        for (Assessment a : assessments) if (a.getStudentEmail().equals(email)) return a;
        return null;
    }
}

class AppointmentManager {
    private Queue<Session> upcoming = new LinkedList<>();
    private Stack<Session> past = new Stack<>();
    public void addSession(Session s) { upcoming.offer(s); }
    public Queue<Session> getUpcoming() { return upcoming; }
    public Stack<Session> getPast() { return past; }

    public boolean completeSessionForMentor(String mentorEmail) {
        Iterator<Session> it = upcoming.iterator();
        while (it.hasNext()) {
            Session s = it.next();
            if (s.getMentorEmail().equals(mentorEmail)) {
                it.remove();
                s.setStatus("completed");
                past.push(s);
                return true;
            }
        }
        return false;
    }
}

/* ----------------------- GUI: Login/Register ----------------------- */

class LoginApp extends JFrame {
    private UserManager userManager = new UserManager();
    private AssessmentManager assessmentManager = new AssessmentManager();
    private AppointmentManager appointmentManager = new AppointmentManager();

    public LoginApp() {
        setTitle("Login / Register");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel p = new JPanel(new GridLayout(5,2,5,5));
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton loginBtn = new JButton("Login");
        JButton regStu = new JButton("Register Student");
        JButton regMen = new JButton("Register Mentor");

        p.add(new JLabel("Name:")); p.add(nameField);
        p.add(new JLabel("Email:")); p.add(emailField);
        p.add(new JLabel("Password:")); p.add(passField);
        p.add(loginBtn); p.add(regStu); p.add(regMen);
        add(p);

        loginBtn.addActionListener(e -> {
            User u = userManager.login(emailField.getText().trim(), new String(passField.getPassword()));
            if (u != null) {
                if (u instanceof Student) {
                    Assessment a = assessmentManager.getAssessmentByStudentEmail(u.getEmail());
                    if (a == null) {
                        new AssessmentForm((Student)u, assessmentManager).setVisible(true);
                    } else {
                        new StudentDashboard((Student)u, appointmentManager, assessmentManager).setVisible(true);
                    }
                } else {
                    new MentorDashboard((Mentor)u, userManager, appointmentManager, assessmentManager).setVisible(true);
                }
                dispose();
            } else JOptionPane.showMessageDialog(this,"Login failed.");
        });

        regStu.addActionListener(e -> {
            if (userManager.register(new Student(nameField.getText().trim(), emailField.getText().trim(), new String(passField.getPassword()))))
                JOptionPane.showMessageDialog(this,"Student registered!");
            else JOptionPane.showMessageDialog(this,"Email already used.");
        });

        regMen.addActionListener(e -> {
            if (userManager.register(new Mentor(nameField.getText().trim(), emailField.getText().trim(), new String(passField.getPassword()))))
                JOptionPane.showMessageDialog(this,"Mentor registered!");
            else JOptionPane.showMessageDialog(this,"Email already used.");
        });
    }
}

/* ----------------------- GUI: Assessment Form ----------------------- */

class AssessmentForm extends JFrame {
    public AssessmentForm(Student stu, AssessmentManager am) {
        setTitle("Assessment Questionnaire");
        setSize(400,400);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6,2,5,5));
        String[] qs = {"What subject do you struggle with?",
                       "How many study hours per day?",
                       "Preferred mentoring style?",
                       "Biggest academic challenge?",
                       "Your learning goal?"};
        JTextField[] answers = new JTextField[qs.length];
        for (int i=0;i<qs.length;i++) {
            panel.add(new JLabel(qs[i]));
            answers[i] = new JTextField();
            panel.add(answers[i]);
        }
        JButton submit = new JButton("Submit");
        panel.add(submit);
        add(panel);

        submit.addActionListener(e -> {
            String[][] qa = new String[qs.length][2];
            for (int i=0;i<qs.length;i++) {
                qa[i][0]=qs[i]; qa[i][1]=answers[i].getText().trim();
            }
            am.saveAssessment(new Assessment(stu.getEmail(), qa));
            JOptionPane.showMessageDialog(this,"Assessment saved!");
            new StudentDashboard(stu, new AppointmentManager(), am).setVisible(true);
            dispose();
        });
    }
}

/* ----------------------- GUI: Student Dashboard ----------------------- */

class StudentDashboard extends JFrame {
    private Student student;
    private AppointmentManager manager;
    private AssessmentManager am;
    private JTextArea area;

    public StudentDashboard(Student s, AppointmentManager m, AssessmentManager am) {
        student=s; manager=m; this.am=am;
        setTitle("Student Dashboard - " + s.getName());
        setSize(600,400);
        setLocationRelativeTo(null);

        area = new JTextArea(); area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> { dispose(); new LoginApp().setVisible(true); });
        add(logout, BorderLayout.SOUTH);

        display();
    }

    private void display() {
        area.setText("Welcome, "+student.getName()+"\n\n--- Assessment ---\n");
        Assessment a = am.getAssessmentByStudentEmail(student.getEmail());
        if (a!=null) area.append(a.toString());
        else area.append("No assessment yet.\n");

        area.append("\n--- Upcoming Sessions ---\n");
        for (Session s : manager.getUpcoming())
            if (s.getStudentEmail().equals(student.getEmail()))
                area.append("With "+s.getMentorName()+" on "+s.getDateTime()+" ("+s.getStatus()+")\n");

        area.append("\n--- Past Sessions ---\n");
        for (Session s : manager.getPast())
            if (s.getStudentEmail().equals(student.getEmail()))
                area.append("With "+s.getMentorName()+" on "+s.getDateTime()+" ("+s.getStatus()+")\n");
    }
}

/* ----------------------- GUI: Mentor Dashboard ----------------------- */

class MentorDashboard extends JFrame {
    private Mentor mentor;
    private UserManager um;
    private AppointmentManager manager;
    private AssessmentManager am;
    private JTextArea area;

    public MentorDashboard(Mentor m, UserManager um, AppointmentManager man, AssessmentManager am) {
        mentor=m; this.um=um; manager=man; this.am=am;
        setTitle("Mentor Dashboard - "+m.getName());
        setSize(600,400);
        setLocationRelativeTo(null);

        area=new JTextArea(); area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel bottom=new JPanel();
        JButton viewBtn=new JButton("View Student Assessment");
        JButton bookBtn=new JButton("Book Session for Student");
        JButton completeBtn=new JButton("Complete Session");
        JButton logout=new JButton("Logout");
        bottom.add(viewBtn); bottom.add(bookBtn); bottom.add(completeBtn); bottom.add(logout);
        add(bottom,BorderLayout.SOUTH);

        viewBtn.addActionListener(e->viewAssessment());
        bookBtn.addActionListener(e->bookSession());
        completeBtn.addActionListener(e->completeSession());
        logout.addActionListener(e->{dispose(); new LoginApp().setVisible(true);});

        display();
    }

    private void viewAssessment() {
        String email=JOptionPane.showInputDialog(this,"Enter student email:");
        Assessment a=am.getAssessmentByStudentEmail(email);
        if (a!=null) JOptionPane.showMessageDialog(this,"Assessment:\n"+a.toString());
        else JOptionPane.showMessageDialog(this,"No assessment found.");
    }

    private void bookSession() {
        String stuEmail=JOptionPane.showInputDialog(this,"Enter student email:");
        User u=um.getUserByEmail(stuEmail);
        if (u==null || !(u instanceof Student)) {
            JOptionPane.showMessageDialog(this,"No student found."); return;
        }
        Assessment a=am.getAssessmentByStudentEmail(stuEmail);
        if (a==null) { JOptionPane.showMessageDialog(this,"Student has no assessment yet."); return; }
        LocalDateTime dt=LocalDateTime.now().plusDays(1);
        manager.addSession(new Session(u.getName(), u.getEmail(), mentor.getName(), mentor.getEmail(), dt,"upcoming"));
        JOptionPane.showMessageDialog(this,"Session booked for "+u.getName()+" on "+dt);
        display();
    }

    private void completeSession() {
        if (manager.completeSessionForMentor(mentor.getEmail()))
            JOptionPane.showMessageDialog(this,"One session marked completed.");
        else JOptionPane.showMessageDialog(this,"No sessions to complete.");
        display();
    }

    private void display() {
        area.setText("Welcome, "+mentor.getName()+"\n\n--- Upcoming Sessions ---\n");
        for (Session s:manager.getUpcoming())
            if (s.getMentorEmail().equals(mentor.getEmail()))
                area.append("Student: "+s.getStudentName()+" | "+s.getDateTime()+" ("+s.getStatus()+")\n");
        area.append("\n--- Past Sessions ---\n");
        for (Session s:manager.getPast())
            if (s.getMentorEmail().equals(mentor.getEmail()))
                area.append("Student: "+s.getStudentName()+" | "+s.getDateTime()+" ("+s.getStatus()+")\n");
    }
}