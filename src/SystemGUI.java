import java.sql.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import com.github.lgooddatepicker.components.DateTimePicker;    
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.table.DefaultTableModel;



public class SystemGUI extends javax.swing.JFrame {
    Connection con=null;
    PreparedStatement pst=null;
    ResultSet rs=null;
    
    public SystemGUI() {
        initComponents();
        Connect();
        setupTables();
        refreshAllTables();
        displayAppointments();
        displayAvailability();
        displayFeedback();
        displayAppointmentsArchive();
        displayFeedbackArchive();
        loadMentorWelcomeMessage(1);    
    }
    
    private void Connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // ✅ loads MySQL driver
            con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/appointment_scheduling_system", // ✅ change to your DB name
                "root",   // ✅ username
                ""        // ✅ password (empty if none)
            );
            System.out.println("✅ Database connected!");
        } catch (Exception e) {
            e.printStackTrace(); // shows error in console
            JOptionPane.showMessageDialog(this, "❌ DB Connection Error: " + e.getMessage());
        }

    }
     
    private void setupTables() {
    setupTableModel(Today1_tb);
    setupTableModel(Upcoming1_tb);
    setupTableModel(Past1_tb);
    setupTableModel(Today2_tb);
    setupTableModel(Upcoming2_tb);
    setupTableModel(Past2_tb);
}
                                                                                                        
private void setupTableModel(JTable table) {
    String[] cols = {
        "Appointment ID", "First Name", "Middle Name", "Last Name",
        "Location", "Start Time", "End Time", "Purpose", "Status"
    };
    table.setModel(new DefaultTableModel(cols, 0));
}

// =======================
// DISPLAY FUNCTIONS
// =======================
private void DisplaySessions(JTable targetTable, String sessionType) {
    try {
        String baseSQL = """
            SELECT a.AppointmentID, s.FirstName, s.MiddleName, s.LastName,
                   a.Location, a.StartTime, a.EndTime, a.Purpose, a.Status
            FROM appointment_tb a
            LEFT JOIN student_tb s ON a.StudentID = s.StudentID
            """;

        // Choose condition by session type
        String condition = "";
        switch (sessionType.toLowerCase()) {
            case "today" -> condition = "WHERE DATE(a.StartTime) = CURDATE()";
            case "upcoming" -> condition = "WHERE a.StartTime > NOW()";
            case "past" -> condition = "WHERE a.EndTime < NOW()";
        }

        String sql = baseSQL + " " + condition + " ORDER BY a.StartTime ASC";
        pst = con.prepareStatement(sql);
        rs = pst.executeQuery();

        DefaultTableModel model = (DefaultTableModel) targetTable.getModel();
        model.setRowCount(0); // clear table before adding rows

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("AppointmentID"),
                rs.getString("FirstName"),
                rs.getString("MiddleName"),
                rs.getString("LastName"),
                rs.getString("Location"),
                rs.getTimestamp("StartTime"),
                rs.getTimestamp("EndTime"),
                rs.getString("Purpose"),
                rs.getString("Status")
            });
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Display Error (" + sessionType + "): " + e.getMessage());
    }
}

private void displayAppointments() {
    
    try {
        
        String baseSQL = """
            SELECT a.AppointmentID, s.StudentID, s.LastName, s.FirstName,
                   a.Location, a.StartTime, a.EndTime, a.Purpose, a.Status
            FROM appointment_tb a
            LEFT JOIN student_tb s ON a.StudentID = s.StudentID
            """ + "ORDER BY EndTime DESC";
        pst = con.prepareStatement(baseSQL);
        rs = pst.executeQuery();
        
        DefaultTableModel model = (DefaultTableModel) Appointment_tb.getModel();
        model.setRowCount(0);
        
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("AppointmentID"), rs.getInt("StudentID"), rs.getString("LastName"),
                rs.getString("FirstName"), rs.getString("Location"),
                rs.getTimestamp("StartTime"), rs.getTimestamp("EndTime"),
                rs.getString("Purpose"), rs.getString("Status")
            });
        }   
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error displaying appointments: " + e.getMessage());
    }
}

private void displayAvailability() {
    
    try {
        String baseSQL = """
            SELECT a.AvailabilityID, s.LastName, s.FirstName,
                   a.StartTime, a.EndTime, a.Status
            FROM availability_tb a
            LEFT JOIN student_tb s ON a.StudentID = s.StudentID
            """;
        pst = con.prepareStatement(baseSQL);
        rs = pst.executeQuery();
        DefaultTableModel model = (DefaultTableModel) Availability_tb.getModel();
        model.setRowCount(0);
        
        while (rs.next()) {
            model.addRow(new Object[]{
                
                rs.getInt("AvailabilityID"), rs.getString("LastName"), rs.getString("FirstName"),
                rs.getTimestamp("StartTime"),rs.getTimestamp("EndTime"),
                rs.getString("Status")
            });
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error displaying availability:" + e.getMessage());
    }
}

private void displayFeedback() {
    String sql = """
        SELECT 
            f.FeedbackID, 
            f.AppointmentID, 
            s.LastName, 
            s.FirstName, 
            f.FeedbackNote, 
            f.FeedbackDate, 
            f.Status
        FROM feedback_tb f
        JOIN appointment_tb a ON f.AppointmentID = a.AppointmentID
        JOIN student_tb s ON a.StudentID = s.StudentID
        ORDER BY f.FeedbackID ASC
    """;

    try (PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        DefaultTableModel model = (DefaultTableModel) Feedback_tb.getModel();
        model.setRowCount(0);

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("FeedbackID"),
                rs.getInt("AppointmentID"),
                rs.getString("LastName"),
                rs.getString("FirstName"),
                rs.getString("FeedbackNote"),
                rs.getDate("FeedbackDate"),
                rs.getString("Status")
            });
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, 
            "Error displaying feedback: " + e.getMessage(),
            "Database Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}

private void displayAppointmentsArchive() {
    
    try {
        
        String baseSQL = """
            SELECT a.AppointmentID, s.LastName, s.FirstName,
                   a.Location, a.StartTime, a.EndTime, a.Purpose, a.Status
            FROM archiveappointment_tb a
            LEFT JOIN student_tb s ON a.StudentID = s.StudentID
            """;
        pst = con.prepareStatement(baseSQL);
        rs = pst.executeQuery();
        
        DefaultTableModel model = (DefaultTableModel) AppointmentArchive_tb.getModel();
        model.setRowCount(0);
        
        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("AppointmentID"), rs.getString("LastName"),
                rs.getString("FirstName"), rs.getString("Location"),
                rs.getTimestamp("StartTime"), rs.getTimestamp("EndTime"),
                rs.getString("Purpose"), rs.getString("Status")
            });
        }    
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error displaying appointments: " + e.getMessage());
    }
}

private void displayFeedbackArchive() {
        
    
    String sql = """
        SELECT 
            f.FeedbackID, 
            f.AppointmentID, 
            s.LastName, 
            s.FirstName, 
            f.FeedbackNote, 
            f.FeedbackDate, 
            f.Status
        FROM archivefeedback_tb f
        JOIN archiveappointment_tb a ON f.AppointmentID = a.AppointmentID
        JOIN student_tb s ON a.StudentID = s.StudentID
        ORDER BY f.FeedbackID ASC
    """;

    try (PreparedStatement pst = con.prepareStatement(sql);
         ResultSet rs = pst.executeQuery()) {

        DefaultTableModel model = (DefaultTableModel) feedArchive_tb.getModel();
        model.setRowCount(0);

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("FeedbackID"),
                rs.getInt("AppointmentID"),
                rs.getString("LastName"),
                rs.getString("FirstName"),
                rs.getString("FeedbackNote"),
                rs.getDate("FeedbackDate"),
                rs.getString("Status")
            });
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Error displaying archived feedback: " + e.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

// =======================
// REFRESH ALL TABLES
// =======================
private void refreshAllTables() {
    DisplaySessions(Today1_tb, "today");
    DisplaySessions(Upcoming1_tb, "upcoming");
    DisplaySessions(Past1_tb, "past");
    DisplaySessions(Today2_tb, "today");
    DisplaySessions(Upcoming2_tb, "upcoming");
    DisplaySessions(Past2_tb, "past");
}

private void loadMentorWelcomeMessage(int mentorID) {
    String sql = "SELECT FirstName FROM mentor_tb WHERE MentorID = ?";

    try (PreparedStatement pst = con.prepareStatement(sql)) {

        pst.setInt(1, mentorID);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            String firstName = rs.getString("FirstName");
            WMessage.setText("Welcome, " + firstName + "!");
        } else {
            WMessage.setText("Welcome, Mentor!");
        }

        rs.close();

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this,
            "Error fetching mentor name: " + e.getMessage(),
            "Database Error",
            JOptionPane.ERROR_MESSAGE);
    }
}
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel45 = new javax.swing.JPanel();
        jPanel59 = new javax.swing.JPanel();
        jPanel60 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        LogoIcon = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        WMessage = new javax.swing.JLabel();
        jPanel47 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        UserProf = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        MainTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel11 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        TXTTodaysSession = new javax.swing.JLabel();
        BTNSeeAll_Today = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        Today1_tb = new javax.swing.JTable();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        TXTUpcomingSession = new javax.swing.JLabel();
        BTNSeeAll_Upcoming = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        Upcoming1_tb = new javax.swing.JTable();
        jPanel15 = new javax.swing.JPanel();
        TXTPastSession = new javax.swing.JLabel();
        BTNSeeAll_Past = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        Past1_tb = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jPanel33 = new javax.swing.JPanel();
        jPanel34 = new javax.swing.JPanel();
        jPanel50 = new javax.swing.JPanel();
        jPanel52 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        Purpose = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        Status_Appointment = new javax.swing.JComboBox<>();
        jPanel49 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        Location = new javax.swing.JTextField();
        studentID = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        StartTime = new com.github.lgooddatepicker.components.DateTimePicker();
        EndTime = new com.github.lgooddatepicker.components.DateTimePicker();
        txtSearch = new javax.swing.JTextField();
        btnSearch_Appointment = new javax.swing.JButton();
        cmbSearchBy = new javax.swing.JComboBox<>();
        jPanel51 = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        btnAdd_Appointment = new javax.swing.JButton();
        jPanel43 = new javax.swing.JPanel();
        jPanel48 = new javax.swing.JPanel();
        btnUpdate_Appointment = new javax.swing.JButton();
        jPanel35 = new javax.swing.JPanel();
        jPanel44 = new javax.swing.JPanel();
        jPanel46 = new javax.swing.JPanel();
        ClearAll = new javax.swing.JButton();
        btnArchive_Appointment = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jPanel17 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        Appointment_tb = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        jScrollPane10 = new javax.swing.JScrollPane();
        jPanel28 = new javax.swing.JPanel();
        jScrollPane13 = new javax.swing.JScrollPane();
        Availability_tb = new javax.swing.JTable();
        jPanel25 = new javax.swing.JPanel();
        jPanel56 = new javax.swing.JPanel();
        txtSearchAv = new javax.swing.JTextField();
        btnSearch_Availability = new javax.swing.JButton();
        SortAvailabilitySearch = new javax.swing.JComboBox<>();
        jPanel54 = new javax.swing.JPanel();
        btnBackAvailability = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel26 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jPanel36 = new javax.swing.JPanel();
        jPanel53 = new javax.swing.JPanel();
        jPanel42 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        feeddatePicker = new com.github.lgooddatepicker.components.DatePicker();
        jLabel3 = new javax.swing.JLabel();
        feedAppointmentID = new javax.swing.JTextField();
        feedStatus = new javax.swing.JComboBox<>();
        jScrollPane11 = new javax.swing.JScrollPane();
        txtFeedbackNote = new javax.swing.JTextArea();
        jLabel15 = new javax.swing.JLabel();
        feedSortBy = new javax.swing.JComboBox<>();
        btnSearch_Feedback = new javax.swing.JButton();
        feedSearch = new javax.swing.JTextField();
        jPanel37 = new javax.swing.JPanel();
        jPanel38 = new javax.swing.JPanel();
        btnAdd_Feedback = new javax.swing.JButton();
        jPanel39 = new javax.swing.JPanel();
        jPanel40 = new javax.swing.JPanel();
        btnUpdate_Feedback = new javax.swing.JButton();
        jPanel41 = new javax.swing.JPanel();
        jPanel57 = new javax.swing.JPanel();
        jPanel58 = new javax.swing.JPanel();
        btnClearAll_Feedback = new javax.swing.JButton();
        btnArchive_Feedback = new javax.swing.JButton();
        jPanel27 = new javax.swing.JPanel();
        jScrollPane12 = new javax.swing.JScrollPane();
        jPanel32 = new javax.swing.JPanel();
        jScrollPane17 = new javax.swing.JScrollPane();
        Feedback_tb = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        FeedbackArchive_tb = new javax.swing.JTabbedPane();
        jPanel20 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jPanel29 = new javax.swing.JPanel();
        jPanel67 = new javax.swing.JPanel();
        jPanel68 = new javax.swing.JPanel();
        jScrollPane23 = new javax.swing.JScrollPane();
        Today2_tb = new javax.swing.JTable();
        btnSearch_Session4 = new javax.swing.JButton();
        cmbSearchByToday = new javax.swing.JComboBox<>();
        txtTodaySearch = new javax.swing.JTextField();
        btnBackToday = new javax.swing.JButton();
        jPanel21 = new javax.swing.JPanel();
        jScrollPane9 = new javax.swing.JScrollPane();
        jPanel31 = new javax.swing.JPanel();
        jScrollPane15 = new javax.swing.JScrollPane();
        Upcoming2_tb = new javax.swing.JTable();
        btnSearch_SessionUpcoming = new javax.swing.JButton();
        cmbSortUpcoming = new javax.swing.JComboBox<>();
        txtSearchUpcoming = new javax.swing.JTextField();
        btnBackUpcoming = new javax.swing.JButton();
        jPanel22 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jPanel30 = new javax.swing.JPanel();
        jScrollPane16 = new javax.swing.JScrollPane();
        Past2_tb = new javax.swing.JTable();
        txtSearchPast = new javax.swing.JTextField();
        btnSearch_SessionPast = new javax.swing.JButton();
        cmbSortPast = new javax.swing.JComboBox<>();
        btnBackPast = new javax.swing.JButton();
        jPanel61 = new javax.swing.JPanel();
        jScrollPane18 = new javax.swing.JScrollPane();
        jPanel62 = new javax.swing.JPanel();
        jScrollPane19 = new javax.swing.JScrollPane();
        AppointmentArchive_tb = new javax.swing.JTable();
        jPanel65 = new javax.swing.JPanel();
        btnRetrieve_Appointment = new javax.swing.JButton();
        txtSearchAppointmentArchive = new javax.swing.JTextField();
        btnSearch_AppointmentArchive = new javax.swing.JButton();
        cmbSortAppointmentArchive = new javax.swing.JComboBox<>();
        btnBackAArchive = new javax.swing.JButton();
        jPanel63 = new javax.swing.JPanel();
        jScrollPane20 = new javax.swing.JScrollPane();
        jScrollPane21 = new javax.swing.JScrollPane();
        jPanel64 = new javax.swing.JPanel();
        jScrollPane22 = new javax.swing.JScrollPane();
        feedArchive_tb = new javax.swing.JTable();
        jPanel66 = new javax.swing.JPanel();
        btnRetrieve_Feedback = new javax.swing.JButton();
        txtSearchFeedbackArchive = new javax.swing.JTextField();
        btnSearch_Session = new javax.swing.JButton();
        cmbSortFeedbackArchive = new javax.swing.JComboBox<>();
        btnBackFAchive = new javax.swing.JButton();

        jPanel45.setBackground(new java.awt.Color(51, 51, 255));
        jPanel45.setPreferredSize(new java.awt.Dimension(175, 50));

        javax.swing.GroupLayout jPanel45Layout = new javax.swing.GroupLayout(jPanel45);
        jPanel45.setLayout(jPanel45Layout);
        jPanel45Layout.setHorizontalGroup(
            jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 175, Short.MAX_VALUE)
        );
        jPanel45Layout.setVerticalGroup(
            jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel59Layout = new javax.swing.GroupLayout(jPanel59);
        jPanel59.setLayout(jPanel59Layout);
        jPanel59Layout.setHorizontalGroup(
            jPanel59Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel59Layout.setVerticalGroup(
            jPanel59Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel60Layout = new javax.swing.GroupLayout(jPanel60);
        jPanel60.setLayout(jPanel60Layout);
        jPanel60Layout.setHorizontalGroup(
            jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel60Layout.setVerticalGroup(
            jPanel60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel6.setBackground(new java.awt.Color(0, 0, 255));
        jPanel6.setPreferredSize(new java.awt.Dimension(900, 70));
        jPanel6.setLayout(new java.awt.BorderLayout());

        jPanel8.setBackground(new java.awt.Color(0, 0, 255));
        jPanel8.setPreferredSize(new java.awt.Dimension(198, 80));
        jPanel8.setLayout(new java.awt.BorderLayout());

        jPanel7.setBackground(new java.awt.Color(0, 0, 255));
        jPanel7.setPreferredSize(new java.awt.Dimension(188, 70));

        LogoIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Adobe Express - file (8).png"))); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(LogoIcon))
                    .addComponent(jLabel16))
                .addContainerGap(47, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                    .addComponent(LogoIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(64, 64, 64)
                .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.add(jPanel7, java.awt.BorderLayout.LINE_START);

        jPanel10.setBackground(new java.awt.Color(204, 204, 204));
        jPanel10.setLayout(new java.awt.BorderLayout());

        jPanel9.setBackground(new java.awt.Color(204, 204, 204));
        jPanel9.setPreferredSize(new java.awt.Dimension(300, 70));
        jPanel9.setLayout(new java.awt.BorderLayout());

        WMessage.setText("Welcome, Admin!");
        WMessage.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        WMessage.setPreferredSize(new java.awt.Dimension(200, 25));
        jPanel9.add(WMessage, java.awt.BorderLayout.CENTER);

        jPanel47.setBackground(new java.awt.Color(204, 204, 204));
        jPanel47.setPreferredSize(new java.awt.Dimension(95, 70));

        UserProf.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Adobe Express - file (9).png"))); // NOI18N
        UserProf.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                UserProfMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel47Layout = new javax.swing.GroupLayout(jPanel47);
        jPanel47.setLayout(jPanel47Layout);
        jPanel47Layout.setHorizontalGroup(
            jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel47Layout.createSequentialGroup()
                .addGroup(jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel47Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel17))
                    .addGroup(jPanel47Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jLabel19))
                    .addGroup(jPanel47Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(UserProf)))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        jPanel47Layout.setVerticalGroup(
            jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel47Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel19)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(UserProf)
                .addGap(25, 25, 25))
        );

        jPanel9.add(jPanel47, java.awt.BorderLayout.LINE_END);

        jPanel10.add(jPanel9, java.awt.BorderLayout.LINE_END);

        jPanel8.add(jPanel10, java.awt.BorderLayout.CENTER);

        jPanel6.add(jPanel8, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel6, java.awt.BorderLayout.PAGE_START);

        jPanel1.setBackground(new java.awt.Color(0, 0, 255));

        MainTabbedPane.setBackground(new java.awt.Color(0, 0, 255));
        MainTabbedPane.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        MainTabbedPane.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        MainTabbedPane.setToolTipText("");
        MainTabbedPane.setAlignmentX(0.0F);
        MainTabbedPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        MainTabbedPane.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        MainTabbedPane.setMinimumSize(new java.awt.Dimension(250, 100));
        MainTabbedPane.setPreferredSize(new java.awt.Dimension(250, 100));

        jPanel11.setLayout(new java.awt.BorderLayout());

        jPanel12.setBackground(new java.awt.Color(255, 255, 255));
        jPanel12.setPreferredSize(new java.awt.Dimension(701, 200));

        TXTTodaysSession.setText("Today's  Session");
        TXTTodaysSession.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        BTNSeeAll_Today.setText("See All...");
        BTNSeeAll_Today.setBackground(new java.awt.Color(255, 255, 0));
        BTNSeeAll_Today.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        BTNSeeAll_Today.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                BTNSeeAll_TodayMouseClicked(evt);
            }
        });
        BTNSeeAll_Today.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTNSeeAll_TodayActionPerformed(evt);
            }
        });

        Today1_tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "AppointmentID", "LastName", "FirstName", "Location", "StartTime", "EndTime", "Purpose", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(Today1_tb);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1057, Short.MAX_VALUE)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(TXTTodaysSession)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(BTNSeeAll_Today, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(22, 22, 22))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TXTTodaysSession)
                    .addComponent(BTNSeeAll_Today, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel11.add(jPanel12, java.awt.BorderLayout.PAGE_START);

        jPanel13.setMinimumSize(new java.awt.Dimension(100, 400));
        jPanel13.setPreferredSize(new java.awt.Dimension(701, 400));
        jPanel13.setLayout(new java.awt.BorderLayout());

        jPanel14.setBackground(new java.awt.Color(255, 255, 255));
        jPanel14.setPreferredSize(new java.awt.Dimension(701, 200));

        TXTUpcomingSession.setText("Upcoming  Session");
        TXTUpcomingSession.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        BTNSeeAll_Upcoming.setText("See All...");
        BTNSeeAll_Upcoming.setBackground(new java.awt.Color(255, 255, 0));
        BTNSeeAll_Upcoming.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        BTNSeeAll_Upcoming.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                BTNSeeAll_UpcomingMouseClicked(evt);
            }
        });
        BTNSeeAll_Upcoming.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTNSeeAll_UpcomingActionPerformed(evt);
            }
        });

        Upcoming1_tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "AppointmentID", "LastName", "FirstName", "Location", "StartTime", "EndTime", "Purpose", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane3.setViewportView(Upcoming1_tb);

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1053, Short.MAX_VALUE)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(TXTUpcomingSession)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(BTNSeeAll_Upcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TXTUpcomingSession)
                    .addComponent(BTNSeeAll_Upcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel13.add(jPanel14, java.awt.BorderLayout.PAGE_START);

        jPanel15.setBackground(new java.awt.Color(255, 255, 255));

        TXTPastSession.setText("Past  Session");
        TXTPastSession.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N

        BTNSeeAll_Past.setText("See All...");
        BTNSeeAll_Past.setBackground(new java.awt.Color(255, 255, 0));
        BTNSeeAll_Past.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        BTNSeeAll_Past.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                BTNSeeAll_PastMouseClicked(evt);
            }
        });
        BTNSeeAll_Past.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BTNSeeAll_PastActionPerformed(evt);
            }
        });

        Past1_tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "AppointmentID", "LastName", "FirstName", "Location", "StartTime", "EndTime", "Purpose", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane4.setViewportView(Past1_tb);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel15Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 1046, Short.MAX_VALUE)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(TXTPastSession)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(BTNSeeAll_Past, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(23, 23, 23))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TXTPastSession)
                    .addComponent(BTNSeeAll_Past, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        jPanel13.add(jPanel15, java.awt.BorderLayout.CENTER);

        jPanel11.add(jPanel13, java.awt.BorderLayout.CENTER);

        jScrollPane1.setViewportView(jPanel11);

        MainTabbedPane.addTab("Dashboard", jScrollPane1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel16.setPreferredSize(new java.awt.Dimension(703, 250));
        jPanel16.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33.setLayout(jPanel33Layout);
        jPanel33Layout.setHorizontalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel33Layout.setVerticalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel16.add(jPanel33, java.awt.BorderLayout.PAGE_START);

        jPanel34.setBackground(new java.awt.Color(255, 255, 255));

        jPanel50.setBackground(new java.awt.Color(255, 255, 255));
        jPanel50.setPreferredSize(new java.awt.Dimension(703, 170));
        jPanel50.setLayout(new java.awt.BorderLayout());

        jPanel52.setBackground(new java.awt.Color(255, 255, 255));

        jLabel12.setText("Purpose:");

        Purpose.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---Select---", "Financial", "Academic", "Mental Health" }));
        Purpose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PurposeActionPerformed(evt);
            }
        });

        jLabel13.setText("Status:");

        Status_Appointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---Select---", "Scheduled", "Rescheduled", "Cancelled", "Done" }));
        Status_Appointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Status_AppointmentActionPerformed(evt);
            }
        });

        jPanel49.setBackground(new java.awt.Color(255, 255, 255));
        jPanel49.setPreferredSize(new java.awt.Dimension(380, 140));

        jLabel2.setText("Student ID:");

        jLabel8.setText("Location:");

        Location.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LocationActionPerformed(evt);
            }
        });

        studentID.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---Select---", "1", "2", "3", "4", "5", "6", "7", "8" }));
        studentID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                studentIDActionPerformed(evt);
            }
        });

        jLabel9.setText("Start Time:");

        jLabel20.setText("End Time:");

        javax.swing.GroupLayout jPanel49Layout = new javax.swing.GroupLayout(jPanel49);
        jPanel49.setLayout(jPanel49Layout);
        jPanel49Layout.setHorizontalGroup(
            jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel49Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel49Layout.createSequentialGroup()
                            .addComponent(jLabel9)
                            .addGap(18, 18, 18)
                            .addComponent(StartTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel49Layout.createSequentialGroup()
                            .addComponent(jLabel20)
                            .addGap(18, 18, 18)
                            .addComponent(EndTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel49Layout.createSequentialGroup()
                        .addGroup(jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addGroup(jPanel49Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel8)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(studentID, 0, 250, Short.MAX_VALUE)
                            .addComponent(Location))))
                .addContainerGap(48, Short.MAX_VALUE))
        );
        jPanel49Layout.setVerticalGroup(
            jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel49Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(studentID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(Location, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(StartTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(EndTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(60, Short.MAX_VALUE))
        );

        txtSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchActionPerformed(evt);
            }
        });

        btnSearch_Appointment.setText("Search");
        btnSearch_Appointment.setBackground(new java.awt.Color(255, 255, 0));
        btnSearch_Appointment.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSearch_Appointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearch_AppointmentActionPerformed(evt);
            }
        });

        cmbSearchBy.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "--- Search by ---", "AppointmentID", "Last Name" }));
        cmbSearchBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSearchByActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel52Layout = new javax.swing.GroupLayout(jPanel52);
        jPanel52.setLayout(jPanel52Layout);
        jPanel52Layout.setHorizontalGroup(
            jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel52Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel49, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addGroup(jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13))
                .addGap(28, 28, 28)
                .addGroup(jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(Status_Appointment, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Purpose, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSearch_Appointment, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cmbSearchBy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22))
        );
        jPanel52Layout.setVerticalGroup(
            jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel52Layout.createSequentialGroup()
                .addGroup(jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel52Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel49, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel52Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel52Layout.createSequentialGroup()
                                .addGroup(jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel12)
                                    .addComponent(Purpose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel13)
                                    .addComponent(Status_Appointment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(cmbSearchBy, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                                .addComponent(btnSearch_Appointment, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtSearch, javax.swing.GroupLayout.Alignment.LEADING)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel50.add(jPanel52, java.awt.BorderLayout.CENTER);

        jPanel51.setPreferredSize(new java.awt.Dimension(685, 20));
        jPanel51.setLayout(new java.awt.BorderLayout());

        jPanel23.setBackground(new java.awt.Color(255, 255, 255));
        jPanel23.setPreferredSize(new java.awt.Dimension(175, 30));

        btnAdd_Appointment.setText("Add");
        btnAdd_Appointment.setBackground(new java.awt.Color(255, 255, 0));
        btnAdd_Appointment.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnAdd_Appointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdd_AppointmentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(btnAdd_Appointment, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(btnAdd_Appointment, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel51.add(jPanel23, java.awt.BorderLayout.LINE_START);

        jPanel43.setLayout(new java.awt.BorderLayout());

        jPanel48.setBackground(new java.awt.Color(255, 255, 255));
        jPanel48.setPreferredSize(new java.awt.Dimension(175, 30));

        btnUpdate_Appointment.setText("Update");
        btnUpdate_Appointment.setBackground(new java.awt.Color(255, 255, 0));
        btnUpdate_Appointment.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnUpdate_Appointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdate_AppointmentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel48Layout = new javax.swing.GroupLayout(jPanel48);
        jPanel48.setLayout(jPanel48Layout);
        jPanel48Layout.setHorizontalGroup(
            jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel48Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(btnUpdate_Appointment, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(42, Short.MAX_VALUE))
        );
        jPanel48Layout.setVerticalGroup(
            jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel48Layout.createSequentialGroup()
                .addContainerGap(19, Short.MAX_VALUE)
                .addComponent(btnUpdate_Appointment, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19))
        );

        jPanel43.add(jPanel48, java.awt.BorderLayout.LINE_START);

        jPanel35.setLayout(new java.awt.BorderLayout());

        jPanel44.setBackground(new java.awt.Color(255, 255, 255));
        jPanel44.setPreferredSize(new java.awt.Dimension(175, 30));

        javax.swing.GroupLayout jPanel44Layout = new javax.swing.GroupLayout(jPanel44);
        jPanel44.setLayout(jPanel44Layout);
        jPanel44Layout.setHorizontalGroup(
            jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 175, Short.MAX_VALUE)
        );
        jPanel44Layout.setVerticalGroup(
            jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );

        jPanel35.add(jPanel44, java.awt.BorderLayout.LINE_START);

        jPanel46.setBackground(new java.awt.Color(255, 255, 255));
        jPanel46.setPreferredSize(new java.awt.Dimension(160, 30));
        jPanel46.setRequestFocusEnabled(false);

        ClearAll.setText("Clear All");
        ClearAll.setBackground(new java.awt.Color(255, 255, 0));
        ClearAll.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        ClearAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClearAllActionPerformed(evt);
            }
        });

        btnArchive_Appointment.setText("Archive");
        btnArchive_Appointment.setBackground(new java.awt.Color(255, 255, 0));
        btnArchive_Appointment.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnArchive_Appointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnArchive_AppointmentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel46Layout = new javax.swing.GroupLayout(jPanel46);
        jPanel46.setLayout(jPanel46Layout);
        jPanel46Layout.setHorizontalGroup(
            jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel46Layout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addComponent(btnArchive_Appointment, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(84, 84, 84)
                .addComponent(ClearAll, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(260, Short.MAX_VALUE))
        );
        jPanel46Layout.setVerticalGroup(
            jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel46Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ClearAll, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnArchive_Appointment, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jPanel35.add(jPanel46, java.awt.BorderLayout.CENTER);

        jPanel43.add(jPanel35, java.awt.BorderLayout.CENTER);

        jPanel51.add(jPanel43, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1099, Short.MAX_VALUE)
            .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel34Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel50, javax.swing.GroupLayout.PREFERRED_SIZE, 1099, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel51, javax.swing.GroupLayout.PREFERRED_SIZE, 1099, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel34Layout.setVerticalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 250, Short.MAX_VALUE)
            .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel34Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel50, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, 0)
                    .addComponent(jPanel51, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jPanel16.add(jPanel34, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel16, java.awt.BorderLayout.PAGE_START);

        jScrollPane5.setPreferredSize(new java.awt.Dimension(1089, 650));

        jPanel17.setBackground(new java.awt.Color(255, 255, 255));
        jPanel17.setPreferredSize(new java.awt.Dimension(1087, 600));
        jPanel17.setLayout(new java.awt.BorderLayout());

        Appointment_tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "AppointmentID", "StudentID", "LastName", "FirstName", "Location", "StartTime", "EndTime", "Purpose", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        Appointment_tb.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Appointment_tbMouseClicked(evt);
            }
        });
        jScrollPane6.setViewportView(Appointment_tb);

        jPanel17.add(jScrollPane6, java.awt.BorderLayout.CENTER);

        jScrollPane5.setViewportView(jPanel17);

        jPanel2.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        MainTabbedPane.addTab("Appointment", jPanel2);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel24.setLayout(new java.awt.BorderLayout());

        jPanel28.setBackground(new java.awt.Color(255, 255, 255));
        jPanel28.setLayout(new java.awt.BorderLayout());

        Availability_tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "AvailabilityID", "LastName", "FisrtName", "StartTime", "EndTime", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        Availability_tb.setPreferredSize(new java.awt.Dimension(300, 250));
        jScrollPane13.setViewportView(Availability_tb);

        jPanel28.add(jScrollPane13, java.awt.BorderLayout.CENTER);

        jPanel25.setBackground(new java.awt.Color(255, 255, 255));
        jPanel25.setPreferredSize(new java.awt.Dimension(1097, 50));
        jPanel25.setLayout(new java.awt.BorderLayout());

        jPanel56.setBackground(new java.awt.Color(255, 255, 255));
        jPanel56.setPreferredSize(new java.awt.Dimension(400, 50));

        btnSearch_Availability.setText("Search");
        btnSearch_Availability.setBackground(new java.awt.Color(255, 255, 0));
        btnSearch_Availability.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSearch_Availability.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearch_AvailabilityActionPerformed(evt);
            }
        });

        SortAvailabilitySearch.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---Sort by---", "AvailabilityID", "LastName", " " }));
        SortAvailabilitySearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SortAvailabilitySearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel56Layout = new javax.swing.GroupLayout(jPanel56);
        jPanel56.setLayout(jPanel56Layout);
        jPanel56Layout.setHorizontalGroup(
            jPanel56Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel56Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(txtSearchAv, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch_Availability, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SortAvailabilitySearch, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel56Layout.setVerticalGroup(
            jPanel56Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel56Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel56Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSearch_Availability, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtSearchAv, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                    .addComponent(SortAvailabilitySearch))
                .addContainerGap())
        );

        jPanel25.add(jPanel56, java.awt.BorderLayout.LINE_END);

        jPanel54.setBackground(new java.awt.Color(255, 255, 255));

        btnBackAvailability.setBackground(new java.awt.Color(255, 255, 0));
        btnBackAvailability.setText("Back");
        btnBackAvailability.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnBackAvailability.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackAvailabilityActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel54Layout = new javax.swing.GroupLayout(jPanel54);
        jPanel54.setLayout(jPanel54Layout);
        jPanel54Layout.setHorizontalGroup(
            jPanel54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel54Layout.createSequentialGroup()
                .addContainerGap(595, Short.MAX_VALUE)
                .addComponent(btnBackAvailability, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        jPanel54Layout.setVerticalGroup(
            jPanel54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel54Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnBackAvailability, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel25.add(jPanel54, java.awt.BorderLayout.CENTER);

        jPanel28.add(jPanel25, java.awt.BorderLayout.PAGE_START);

        jScrollPane10.setViewportView(jPanel28);

        jPanel24.add(jScrollPane10, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel24, java.awt.BorderLayout.CENTER);

        MainTabbedPane.addTab("Student Availability", jPanel3);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel26.setPreferredSize(new java.awt.Dimension(703, 250));
        jPanel26.setLayout(new java.awt.BorderLayout());

        jPanel36.setPreferredSize(new java.awt.Dimension(703, 175));
        jPanel36.setLayout(new java.awt.BorderLayout());

        jPanel53.setBackground(new java.awt.Color(255, 255, 255));

        jPanel42.setBackground(new java.awt.Color(255, 255, 255));
        jPanel42.setPreferredSize(new java.awt.Dimension(350, 140));

        jLabel10.setText("Date:");

        jLabel14.setText("Status:");

        jLabel3.setText("AppointmentID:");

        feedStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---Select---", "Follow Up", "Done" }));

        javax.swing.GroupLayout jPanel42Layout = new javax.swing.GroupLayout(jPanel42);
        jPanel42.setLayout(jPanel42Layout);
        jPanel42Layout.setHorizontalGroup(
            jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel42Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel42Layout.createSequentialGroup()
                        .addGroup(jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(feeddatePicker, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(feedStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel42Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(feedAppointmentID, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(82, Short.MAX_VALUE))
        );
        jPanel42Layout.setVerticalGroup(
            jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel42Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(feedAppointmentID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(feeddatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(feedStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(92, Short.MAX_VALUE))
        );

        txtFeedbackNote.setColumns(20);
        txtFeedbackNote.setRows(5);
        jScrollPane11.setViewportView(txtFeedbackNote);

        jLabel15.setText("Feedback:");

        feedSortBy.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---Sort by---", "AppointmentID", "Lastname" }));
        feedSortBy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                feedSortByActionPerformed(evt);
            }
        });

        btnSearch_Feedback.setText("Search");
        btnSearch_Feedback.setBackground(new java.awt.Color(255, 255, 0));
        btnSearch_Feedback.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSearch_Feedback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearch_FeedbackActionPerformed(evt);
            }
        });

        feedSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                feedSearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel53Layout = new javax.swing.GroupLayout(jPanel53);
        jPanel53.setLayout(jPanel53Layout);
        jPanel53Layout.setHorizontalGroup(
            jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel53Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel42, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                .addGap(82, 82, 82)
                .addComponent(feedSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch_Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(feedSortBy, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );
        jPanel53Layout.setVerticalGroup(
            jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel53Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel42, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel53Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15)
                            .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(feedSortBy, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                                .addComponent(btnSearch_Feedback, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(feedSearch, javax.swing.GroupLayout.Alignment.LEADING)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel36.add(jPanel53, java.awt.BorderLayout.CENTER);

        jPanel37.setLayout(new java.awt.BorderLayout());

        jPanel38.setBackground(new java.awt.Color(255, 255, 255));
        jPanel38.setPreferredSize(new java.awt.Dimension(175, 50));

        btnAdd_Feedback.setText("Add");
        btnAdd_Feedback.setBackground(new java.awt.Color(255, 255, 0));
        btnAdd_Feedback.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnAdd_Feedback.setPreferredSize(new java.awt.Dimension(26, 19));
        btnAdd_Feedback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdd_FeedbackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel38Layout = new javax.swing.GroupLayout(jPanel38);
        jPanel38.setLayout(jPanel38Layout);
        jPanel38Layout.setHorizontalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel38Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addComponent(btnAdd_Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(43, Short.MAX_VALUE))
        );
        jPanel38Layout.setVerticalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel38Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(btnAdd_Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel37.add(jPanel38, java.awt.BorderLayout.LINE_START);

        jPanel39.setLayout(new java.awt.BorderLayout());

        jPanel40.setBackground(new java.awt.Color(255, 255, 255));
        jPanel40.setPreferredSize(new java.awt.Dimension(175, 50));

        btnUpdate_Feedback.setText("Update");
        btnUpdate_Feedback.setBackground(new java.awt.Color(255, 255, 0));
        btnUpdate_Feedback.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnUpdate_Feedback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdate_FeedbackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel40Layout = new javax.swing.GroupLayout(jPanel40);
        jPanel40.setLayout(jPanel40Layout);
        jPanel40Layout.setHorizontalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel40Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(btnUpdate_Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );
        jPanel40Layout.setVerticalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel40Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(btnUpdate_Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel39.add(jPanel40, java.awt.BorderLayout.LINE_START);

        jPanel41.setLayout(new java.awt.BorderLayout());

        jPanel57.setBackground(new java.awt.Color(255, 255, 255));
        jPanel57.setPreferredSize(new java.awt.Dimension(175, 50));

        javax.swing.GroupLayout jPanel57Layout = new javax.swing.GroupLayout(jPanel57);
        jPanel57.setLayout(jPanel57Layout);
        jPanel57Layout.setHorizontalGroup(
            jPanel57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 175, Short.MAX_VALUE)
        );
        jPanel57Layout.setVerticalGroup(
            jPanel57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 75, Short.MAX_VALUE)
        );

        jPanel41.add(jPanel57, java.awt.BorderLayout.LINE_START);

        jPanel58.setBackground(new java.awt.Color(255, 255, 255));
        jPanel58.setPreferredSize(new java.awt.Dimension(175, 50));

        btnClearAll_Feedback.setText("Clear All");
        btnClearAll_Feedback.setBackground(new java.awt.Color(255, 255, 0));
        btnClearAll_Feedback.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnClearAll_Feedback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearAll_FeedbackActionPerformed(evt);
            }
        });

        btnArchive_Feedback.setText("Archive");
        btnArchive_Feedback.setBackground(new java.awt.Color(255, 255, 0));
        btnArchive_Feedback.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnArchive_Feedback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnArchive_FeedbackActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel58Layout = new javax.swing.GroupLayout(jPanel58);
        jPanel58.setLayout(jPanel58Layout);
        jPanel58Layout.setHorizontalGroup(
            jPanel58Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel58Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addComponent(btnArchive_Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(61, 61, 61)
                .addComponent(btnClearAll_Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(289, Short.MAX_VALUE))
        );
        jPanel58Layout.setVerticalGroup(
            jPanel58Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel58Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel58Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClearAll_Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnArchive_Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel41.add(jPanel58, java.awt.BorderLayout.CENTER);

        jPanel39.add(jPanel41, java.awt.BorderLayout.CENTER);

        jPanel37.add(jPanel39, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1099, Short.MAX_VALUE)
            .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel18Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel36, javax.swing.GroupLayout.PREFERRED_SIZE, 1099, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel37, javax.swing.GroupLayout.PREFERRED_SIZE, 1099, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 250, Short.MAX_VALUE)
            .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel18Layout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(jPanel36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, 0)
                    .addComponent(jPanel37, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jPanel26.add(jPanel18, java.awt.BorderLayout.CENTER);

        jPanel4.add(jPanel26, java.awt.BorderLayout.PAGE_START);

        jPanel27.setLayout(new java.awt.BorderLayout());

        jPanel32.setBackground(new java.awt.Color(255, 255, 255));

        Feedback_tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "FeedbackID", "AppointmentID", "LastName", "FirstName", "FeedbackNote", "FeedbackDate", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        Feedback_tb.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Feedback_tbMouseClicked(evt);
            }
        });
        jScrollPane17.setViewportView(Feedback_tb);

        javax.swing.GroupLayout jPanel32Layout = new javax.swing.GroupLayout(jPanel32);
        jPanel32.setLayout(jPanel32Layout);
        jPanel32Layout.setHorizontalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 1079, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(161, Short.MAX_VALUE))
        );
        jPanel32Layout.setVerticalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane17, javax.swing.GroupLayout.DEFAULT_SIZE, 634, Short.MAX_VALUE)
                .addContainerGap())
        );

        jScrollPane12.setViewportView(jPanel32);

        jPanel27.add(jScrollPane12, java.awt.BorderLayout.CENTER);

        jPanel4.add(jPanel27, java.awt.BorderLayout.CENTER);

        MainTabbedPane.addTab("Feedback", jPanel4);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setLayout(new java.awt.BorderLayout());

        FeedbackArchive_tb.setBackground(new java.awt.Color(255, 255, 255));
        FeedbackArchive_tb.setName(""); // NOI18N

        jPanel20.setLayout(new java.awt.BorderLayout());

        jPanel29.setBackground(new java.awt.Color(255, 255, 255));
        jPanel29.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel67Layout = new javax.swing.GroupLayout(jPanel67);
        jPanel67.setLayout(jPanel67Layout);
        jPanel67Layout.setHorizontalGroup(
            jPanel67Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel67Layout.setVerticalGroup(
            jPanel67Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel29.add(jPanel67, java.awt.BorderLayout.PAGE_START);

        jPanel68.setBackground(new java.awt.Color(255, 255, 255));

        Today2_tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "AppointmentID", "LastName", "FirstName", "Location", "StartTime", "EndTime", "Purpose", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane23.setViewportView(Today2_tb);

        btnSearch_Session4.setText("Search");
        btnSearch_Session4.setBackground(new java.awt.Color(255, 255, 0));
        btnSearch_Session4.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSearch_Session4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearch_Session4ActionPerformed(evt);
            }
        });

        cmbSearchByToday.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---Sort by---", "AppointmentID", "LastName" }));
        cmbSearchByToday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSearchByTodayActionPerformed(evt);
            }
        });

        btnBackToday.setText("Back");
        btnBackToday.setBackground(new java.awt.Color(255, 255, 0));
        btnBackToday.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnBackToday.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackTodayActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel68Layout = new javax.swing.GroupLayout(jPanel68);
        jPanel68.setLayout(jPanel68Layout);
        jPanel68Layout.setHorizontalGroup(
            jPanel68Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane23, javax.swing.GroupLayout.DEFAULT_SIZE, 1234, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel68Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBackToday, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(54, 54, 54)
                .addComponent(txtTodaySearch, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch_Session4, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbSearchByToday, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(163, 163, 163))
        );
        jPanel68Layout.setVerticalGroup(
            jPanel68Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel68Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel68Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSearch_Session4, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel68Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel68Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtTodaySearch, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBackToday, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(cmbSearchByToday, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane23, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel29.add(jPanel68, java.awt.BorderLayout.CENTER);

        jScrollPane7.setViewportView(jPanel29);

        jPanel20.add(jScrollPane7, java.awt.BorderLayout.CENTER);

        FeedbackArchive_tb.addTab("Today Sessions", jPanel20);

        jPanel21.setLayout(new java.awt.BorderLayout());

        jPanel31.setBackground(new java.awt.Color(255, 255, 255));

        Upcoming2_tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "AppointmentID", "LastName", "FirstName", "Location", "StartTime", "EndTime", "Purpose", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane15.setViewportView(Upcoming2_tb);

        btnSearch_SessionUpcoming.setText("Search");
        btnSearch_SessionUpcoming.setBackground(new java.awt.Color(255, 255, 0));
        btnSearch_SessionUpcoming.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSearch_SessionUpcoming.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearch_SessionUpcomingActionPerformed(evt);
            }
        });

        cmbSortUpcoming.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---Sort by---", "AppointmentID", "LastName", " " }));
        cmbSortUpcoming.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSortUpcomingActionPerformed(evt);
            }
        });

        btnBackUpcoming.setText("Back");
        btnBackUpcoming.setBackground(new java.awt.Color(255, 255, 0));
        btnBackUpcoming.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnBackUpcoming.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackUpcomingActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 1234, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel31Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBackUpcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57)
                .addComponent(txtSearchUpcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch_SessionUpcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbSortUpcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(167, 167, 167))
        );
        jPanel31Layout.setVerticalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSearch_SessionUpcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSearchUpcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBackUpcoming, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(cmbSortUpcoming, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane15, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
                .addGap(44, 44, 44))
        );

        jScrollPane9.setViewportView(jPanel31);

        jPanel21.add(jScrollPane9, java.awt.BorderLayout.CENTER);

        FeedbackArchive_tb.addTab("Upcoming Sessions", jPanel21);

        jPanel22.setLayout(new java.awt.BorderLayout());

        jPanel30.setBackground(new java.awt.Color(255, 255, 255));

        Past2_tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "AppointmentID", "LastName", "FirstName", "Location", "StartTime", "EndTime", "Purpose", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane16.setViewportView(Past2_tb);

        btnSearch_SessionPast.setText("Search");
        btnSearch_SessionPast.setBackground(new java.awt.Color(255, 255, 0));
        btnSearch_SessionPast.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSearch_SessionPast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearch_SessionPastActionPerformed(evt);
            }
        });

        cmbSortPast.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---Sort by---", "AppointmentID", "LastName" }));
        cmbSortPast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSortPastActionPerformed(evt);
            }
        });

        btnBackPast.setText("Back");
        btnBackPast.setBackground(new java.awt.Color(255, 255, 0));
        btnBackPast.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnBackPast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackPastActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel30Layout = new javax.swing.GroupLayout(jPanel30);
        jPanel30.setLayout(jPanel30Layout);
        jPanel30Layout.setHorizontalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel30Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBackPast, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch_SessionPast, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtSearchPast, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(96, 96, 96)
                .addComponent(cmbSortPast, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(172, 172, 172))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel30Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 1234, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel30Layout.setVerticalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtSearchPast, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnBackPast, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnSearch_SessionPast, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(cmbSortPast, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 698, Short.MAX_VALUE)
                .addContainerGap())
        );

        jScrollPane8.setViewportView(jPanel30);

        jPanel22.add(jScrollPane8, java.awt.BorderLayout.CENTER);

        FeedbackArchive_tb.addTab("Past Sessions", jPanel22);

        jPanel61.setLayout(new java.awt.BorderLayout());

        AppointmentArchive_tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "AppointmentID", "LastName", "FirstName", "Location", "StartTime", "EndTime", "Purpose", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane19.setViewportView(AppointmentArchive_tb);

        jPanel65.setBackground(new java.awt.Color(255, 255, 255));

        btnRetrieve_Appointment.setText("Retrieve");
        btnRetrieve_Appointment.setBackground(new java.awt.Color(255, 255, 0));
        btnRetrieve_Appointment.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnRetrieve_Appointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRetrieve_AppointmentActionPerformed(evt);
            }
        });

        btnSearch_AppointmentArchive.setText("Search");
        btnSearch_AppointmentArchive.setBackground(new java.awt.Color(255, 255, 0));
        btnSearch_AppointmentArchive.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSearch_AppointmentArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearch_AppointmentArchiveActionPerformed(evt);
            }
        });

        cmbSortAppointmentArchive.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---Sort by---", "AppointmentID", "LastName" }));
        cmbSortAppointmentArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSortAppointmentArchiveActionPerformed(evt);
            }
        });

        btnBackAArchive.setText("Back");
        btnBackAArchive.setBackground(new java.awt.Color(255, 255, 0));
        btnBackAArchive.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnBackAArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackAArchiveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel65Layout = new javax.swing.GroupLayout(jPanel65);
        jPanel65.setLayout(jPanel65Layout);
        jPanel65Layout.setHorizontalGroup(
            jPanel65Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel65Layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addComponent(btnRetrieve_Appointment, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBackAArchive, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(56, 56, 56)
                .addComponent(txtSearchAppointmentArchive, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch_AppointmentArchive, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbSortAppointmentArchive, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(133, 133, 133))
        );
        jPanel65Layout.setVerticalGroup(
            jPanel65Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel65Layout.createSequentialGroup()
                .addGroup(jPanel65Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSearch_AppointmentArchive, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel65Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel65Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(jPanel65Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(txtSearchAppointmentArchive, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnBackAArchive, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel65Layout.createSequentialGroup()
                            .addGap(16, 16, 16)
                            .addGroup(jPanel65Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(cmbSortAppointmentArchive, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnRetrieve_Appointment, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel62Layout = new javax.swing.GroupLayout(jPanel62);
        jPanel62.setLayout(jPanel62Layout);
        jPanel62Layout.setHorizontalGroup(
            jPanel62Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel65, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel62Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 1077, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(119, Short.MAX_VALUE))
        );
        jPanel62Layout.setVerticalGroup(
            jPanel62Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel62Layout.createSequentialGroup()
                .addComponent(jPanel65, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, 539, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(95, Short.MAX_VALUE))
        );

        jScrollPane18.setViewportView(jPanel62);

        jPanel61.add(jScrollPane18, java.awt.BorderLayout.CENTER);

        FeedbackArchive_tb.addTab("Appointment Archive", jPanel61);

        jPanel63.setLayout(new java.awt.BorderLayout());

        feedArchive_tb.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "FeedbackID", "AppointmentID", "LastName", "FirstName", "FeedbackNote", "FeedbackDate", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane22.setViewportView(feedArchive_tb);

        jPanel66.setBackground(new java.awt.Color(255, 255, 255));

        btnRetrieve_Feedback.setText("Retrieve");
        btnRetrieve_Feedback.setBackground(new java.awt.Color(255, 255, 0));
        btnRetrieve_Feedback.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnRetrieve_Feedback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRetrieve_FeedbackActionPerformed(evt);
            }
        });

        txtSearchFeedbackArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchFeedbackArchiveActionPerformed(evt);
            }
        });

        btnSearch_Session.setText("Search");
        btnSearch_Session.setBackground(new java.awt.Color(255, 255, 0));
        btnSearch_Session.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSearch_Session.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearch_SessionActionPerformed(evt);
            }
        });

        cmbSortFeedbackArchive.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "---Sort by---", "AppointmentID", "LastName" }));
        cmbSortFeedbackArchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSortFeedbackArchiveActionPerformed(evt);
            }
        });

        btnBackFAchive.setText("Back");
        btnBackFAchive.setBackground(new java.awt.Color(255, 255, 0));
        btnBackFAchive.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnBackFAchive.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackFAchiveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel66Layout = new javax.swing.GroupLayout(jPanel66);
        jPanel66.setLayout(jPanel66Layout);
        jPanel66Layout.setHorizontalGroup(
            jPanel66Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel66Layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addComponent(btnRetrieve_Feedback, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBackFAchive, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(73, 73, 73)
                .addComponent(txtSearchFeedbackArchive, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch_Session, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cmbSortFeedbackArchive, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31))
        );
        jPanel66Layout.setVerticalGroup(
            jPanel66Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel66Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel66Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cmbSortFeedbackArchive)
                    .addComponent(btnSearch_Session, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRetrieve_Feedback, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                    .addComponent(btnBackFAchive, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtSearchFeedbackArchive, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel64Layout = new javax.swing.GroupLayout(jPanel64);
        jPanel64.setLayout(jPanel64Layout);
        jPanel64Layout.setHorizontalGroup(
            jPanel64Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel66, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel64Layout.createSequentialGroup()
                .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 1075, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 20, Short.MAX_VALUE))
        );
        jPanel64Layout.setVerticalGroup(
            jPanel64Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel64Layout.createSequentialGroup()
                .addComponent(jPanel66, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 537, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(95, Short.MAX_VALUE))
        );

        jScrollPane21.setViewportView(jPanel64);

        jScrollPane20.setViewportView(jScrollPane21);

        jPanel63.add(jScrollPane20, java.awt.BorderLayout.CENTER);

        FeedbackArchive_tb.addTab("Feedback Archive", jPanel63);

        jPanel5.add(FeedbackArchive_tb, java.awt.BorderLayout.CENTER);

        MainTabbedPane.addTab("Sessions", jPanel5);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1300, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(MainTabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void BTNSeeAll_TodayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTNSeeAll_TodayActionPerformed
        // TODO add your handling code here:                                                                                    
    }//GEN-LAST:event_BTNSeeAll_TodayActionPerformed

    private void BTNSeeAll_UpcomingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTNSeeAll_UpcomingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BTNSeeAll_UpcomingActionPerformed

    private void BTNSeeAll_PastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BTNSeeAll_PastActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BTNSeeAll_PastActionPerformed

    private void BTNSeeAll_TodayMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BTNSeeAll_TodayMouseClicked
       MainTabbedPane.setSelectedIndex(4);
       FeedbackArchive_tb.setSelectedIndex(0);
    }//GEN-LAST:event_BTNSeeAll_TodayMouseClicked

    private void BTNSeeAll_UpcomingMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BTNSeeAll_UpcomingMouseClicked
        MainTabbedPane.setSelectedIndex(4);
        FeedbackArchive_tb.setSelectedIndex(1);
    }//GEN-LAST:event_BTNSeeAll_UpcomingMouseClicked

    private void BTNSeeAll_PastMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BTNSeeAll_PastMouseClicked
        MainTabbedPane.setSelectedIndex(4);
        FeedbackArchive_tb.setSelectedIndex(2);
    }//GEN-LAST:event_BTNSeeAll_PastMouseClicked

    private void cmbSortFeedbackArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSortFeedbackArchiveActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSortFeedbackArchiveActionPerformed

    private void PurposeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PurposeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PurposeActionPerformed

    private void Status_AppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Status_AppointmentActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Status_AppointmentActionPerformed

    private void LocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LocationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_LocationActionPerformed

    private void btnAdd_AppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdd_AppointmentActionPerformed

        try {
            String sql = "INSERT INTO appointment_tb (StudentID, StartTime, EndTime, Location, Purpose, Status) "
            + "VALUES (?,?, ?, ?, ?, ?)";

            pst = con.prepareStatement(sql);

            pst.setInt(1, Integer.parseInt(studentID.getSelectedItem().toString()));
            String startTime = (StartTime.getDateTimeStrict().toString());
            String endTime = (EndTime.getDateTimeStrict().toString());

            pst.setString(2, startTime);
            pst.setString(3, endTime);
            pst.setString(4, Location.getText());
            pst.setString(5, Purpose.getSelectedItem().toString());
            pst.setString(6, Status_Appointment.getSelectedItem().toString());

            pst.executeUpdate();
            displayAppointments();
            refreshAllTables();
            

            JOptionPane.showMessageDialog(null, "Appointment added successfully!");

        } catch(SQLException e) {
            JOptionPane.showMessageDialog(null, "Error while adding appointment: " + e.getMessage());

        }
    }//GEN-LAST:event_btnAdd_AppointmentActionPerformed

    private void btnUpdate_AppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdate_AppointmentActionPerformed
        // TODO add your handling code here:

        try{
            String sql = "UPDATE appointment_tb SET StartTime=?, EndTime=?, Location=?,Purpose=?, Status=? WHERE StudentID=?";

            pst = con.prepareStatement(sql);

            LocalDateTime start = StartTime.getDateTimeStrict();
            LocalDateTime end = EndTime.getDateTimeStrict();

            pst.setTimestamp(1, Timestamp.valueOf(start));
            pst.setTimestamp(2, Timestamp.valueOf(end));
            pst.setString(3, Location.getText());

            pst.setString(4, Purpose.getSelectedItem().toString());
            pst.setString(5, Status_Appointment.getSelectedItem().toString());
            pst.setInt(6, Integer.parseInt(studentID.getSelectedItem().toString()));

            pst.executeUpdate();
            displayAppointments();
            refreshAllTables();
            

            JOptionPane.showMessageDialog(this, "Appointment updated successfully");

        } catch (SQLException e){

            JOptionPane.showMessageDialog(this, "Error updating appointment: " + e.getMessage());

        } catch (Exception ex){

            JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage());

        }
    }//GEN-LAST:event_btnUpdate_AppointmentActionPerformed

    private void btnArchive_AppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnArchive_AppointmentActionPerformed
        

        int selectedRow = Appointment_tb.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to archive.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get AppointmentID from the selected row
        int selectedAppointmentID = (int) Appointment_tb.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to archive this appointment?", 
            "Confirm Archive", 
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String archiveFeedbackSQL = "REPLACE INTO archivefeedback_tb " +
                            "SELECT * FROM feedback_tb WHERE AppointmentID = ?";
        String deleteFeedbackSQL = "DELETE FROM feedback_tb WHERE AppointmentID = ?";

        String archiveAppointmentSQL = "REPLACE INTO archiveappointment_tb " +
                                       "SELECT * FROM appointment_tb WHERE AppointmentID = ?";
        String deleteAppointmentSQL = "DELETE FROM appointment_tb WHERE AppointmentID = ?";

        try (PreparedStatement pstArchiveFeedback = con.prepareStatement(archiveFeedbackSQL);
             PreparedStatement pstDeleteFeedback = con.prepareStatement(deleteFeedbackSQL);
             PreparedStatement pstArchiveAppointment = con.prepareStatement(archiveAppointmentSQL);
             PreparedStatement pstDeleteAppointment = con.prepareStatement(deleteAppointmentSQL)) {

            con.setAutoCommit(false); // make it atomic (all or nothing)

            // Archive feedback first
            pstArchiveFeedback.setInt(1, selectedAppointmentID);
            pstArchiveFeedback.executeUpdate();

            // Delete feedback after archiving
            pstDeleteFeedback.setInt(1, selectedAppointmentID);
            pstDeleteFeedback.executeUpdate();

            // Archive appointment
            pstArchiveAppointment.setInt(1, selectedAppointmentID);
            pstArchiveAppointment.executeUpdate();

            // Delete appointment
            pstDeleteAppointment.setInt(1, selectedAppointmentID);
            pstDeleteAppointment.executeUpdate();

            con.commit();
            JOptionPane.showMessageDialog(this, "Appointment and feedback archived successfully!");
            refreshAllTables();
            displayAppointments();
            displayAvailability();
            displayFeedback();
            displayAppointmentsArchive();
            displayFeedbackArchive();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error archiving appointment: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnArchive_AppointmentActionPerformed

    private void ClearAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClearAllActionPerformed
        // TODO add your handling code here:
        try {
            studentID.setSelectedIndex(0);
            Location.setText("");
            StartTime.clear();
            EndTime.clear();
            Purpose.setSelectedIndex(0);
            Status_Appointment.setSelectedIndex(0);
            cmbSearchBy.setSelectedIndex(0);
            txtSearch.setText("");

            JOptionPane.showMessageDialog(this, "All fields cleared!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error clearing fields: " + ex.getMessage());
        }
        
        try {
        displayAppointments(); // your existing method that loads all rows into Appointment_tb
        } catch (Exception e) {
            // fallback: attempt to load directly if displayAppointments isn't available
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT a.AppointmentID, s.FirstName, s.LastName, a.Location, a.StartTime, a.EndTime, a.Purpose, a.Status " +
                     "FROM appointment_tb a " +
                     "LEFT JOIN student_tb s ON a.StudentID = s.StudentID " + 
                     "ORDER BY a.AppointmentID ASC")) {

                DefaultTableModel model = (DefaultTableModel) Appointment_tb.getModel();
                model.setRowCount(0);
                while (rs.next()) {
                    model.addRow(new Object[] {
                        rs.getInt("AppointmentID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Location"),
                        rs.getTimestamp("StartTime"),
                        rs.getTimestamp("EndTime"),
                        rs.getString("Purpose"),
                        rs.getString("Status")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error refreshing appointments: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
    }

    }//GEN-LAST:event_ClearAllActionPerformed

    private void txtSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchActionPerformed

    private void btnSearch_AppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearch_AppointmentActionPerformed

        String searchBy = (String) cmbSearchBy.getSelectedItem();
        String keyword = txtSearch.getText().trim();

        // Validate combo selection
        if (searchBy == null || searchBy.equals("--- Search By ---")) {
            JOptionPane.showMessageDialog(this,
                "Please select a search option (AppointmentID or Last Name).",
                "Select Search By",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate input
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter text to search.",
                "Missing Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build SQL depending on selection. Use alias a (appointment), s (student), av (availability), m (mentor)
        String sql = "";
        if (searchBy.equals("AppointmentID")) {
            // cast AppointmentID to CHAR so LIKE works on numeric ids too
            sql = "SELECT a.AppointmentID, s.StudentID, s.FirstName, s.LastName, a.Location, a.StartTime, a.EndTime, a.Purpose, a.Status " +
                  "FROM appointment_tb a " +
                  "LEFT JOIN student_tb s ON a.StudentID = s.StudentID " +
                  "WHERE CAST(a.AppointmentID AS CHAR) LIKE ? " +
                  "ORDER BY a.StartTime ASC";
        } else { // Last Name
            sql = "SELECT a.AppointmentID, s.StudentID, s.FirstName, s.LastName, a.Location, a.StartTime, a.EndTime, a.Purpose, a.Status " +
                  "FROM appointment_tb a " +
                  "LEFT JOIN student_tb s ON a.StudentID = s.StudentID " +
                  "WHERE s.LastName LIKE ? " +
                  "ORDER BY a.AppointmentID ASC";
        }

        // Execute and populate JTable (Appointment_tb)
        try (PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pst.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) Appointment_tb.getModel();
                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[] {
                        rs.getInt("AppointmentID"),
                        rs.getInt("StudentID"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("Location"),
                        rs.getTimestamp("StartTime"),
                        rs.getTimestamp("EndTime"),
                        rs.getString("Purpose"),
                        rs.getString("Status")
                    });
                }

                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this,
                        "No results found for \"" + keyword + "\".",
                        "No Results",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error during search: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSearch_AppointmentActionPerformed

    private void cmbSearchByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSearchByActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSearchByActionPerformed

    private void Appointment_tbMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Appointment_tbMouseClicked
      
        
        int row = Appointment_tb.getSelectedRow();
        if (row >= 0) {
            DefaultTableModel model = (DefaultTableModel) Appointment_tb.getModel();

            // Assuming your table columns are:
            // 0 - AppointmentID
            // 1 - LastName
            // 2 - FirstName
            // 3 - Location
            // 4 - StartTime
            // 5 - EndTime
            // 6 - Purpose
            // 7 - Status
            // If StudentID is NOT shown in the table, skip this and fetch it from DB (see below)

            // Set Location text field
            Location.setText(model.getValueAt(row, 4).toString());

            // Set StartTime and EndTime
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                String startTimeString = model.getValueAt(row, 5).toString();
                String endTimeString = model.getValueAt(row, 6).toString();

                LocalDateTime startDT = LocalDateTime.parse(startTimeString, formatter);
                LocalDateTime endDT = LocalDateTime.parse(endTimeString, formatter);

                StartTime.setDateTimeStrict(startDT);
                EndTime.setDateTimeStrict(endDT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Set Purpose and Status
            Purpose.setSelectedItem(model.getValueAt(row, 7).toString());
            Status_Appointment.setSelectedItem(model.getValueAt(row, 8).toString());

            // 🔹 Set StudentID combo box
            // Option 1: If your table model includes StudentID (even hidden)
            // studentID.setSelectedItem(model.getValueAt(row, 2).toString());

            // Option 2: If StudentID is NOT in the table, fetch it from the DB
            try {
                int appointmentID = Integer.parseInt(model.getValueAt(row, 0).toString());
                String sql = "SELECT StudentID FROM appointment_tb WHERE AppointmentID = ?";
                PreparedStatement pst = con.prepareStatement(sql);
                pst.setInt(1, appointmentID);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    String sid = rs.getString("StudentID");
                    studentID.setSelectedItem(sid);
                }
                rs.close();
                pst.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_Appointment_tbMouseClicked

    private void Feedback_tbMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Feedback_tbMouseClicked
        
          int selectedRow = Feedback_tb.getSelectedRow();
        if (selectedRow == -1) return;

        DefaultTableModel model = (DefaultTableModel) Feedback_tb.getModel();

       

        String appointmentID = model.getValueAt(selectedRow, 1).toString();
        String feedbackNote = model.getValueAt(selectedRow, 4).toString();
        String feedbackDateStr = model.getValueAt(selectedRow, 5).toString();
        String status = model.getValueAt(selectedRow, 6).toString();

        // set fields (uses the variable names from your add feedback code)
        feedAppointmentID.setText(appointmentID);
        txtFeedbackNote.setText(feedbackNote);
        feedStatus.setSelectedItem(status);

        // Parse various date formats robustly and set LGood DatePicker (feeddatePicker)
        try {
            // Trim and normalize
            String d = feedbackDateStr.trim().replace("T", " ");

            // Try ISO_LOCAL_DATE first (yyyy-MM-dd)
            try {
                java.time.LocalDate ld = java.time.LocalDate.parse(d, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
                feeddatePicker.setDate(ld);
                return;
            } catch (Exception ignored) {}

            // Try yyyy-MM-dd HH:mm[:ss] -> parse to LocalDateTime then to LocalDate
            java.time.format.DateTimeFormatter f1;
            if (d.length() == 16) { // "yyyy-MM-dd HH:mm"
                f1 = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            } else if (d.length() == 19) { // "yyyy-MM-dd HH:mm:ss"
                f1 = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            } else {
                // fallback formatter attempts
                f1 = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm[:ss]");
            }

            try {
                java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(d, f1);
                feeddatePicker.setDate(ldt.toLocalDate());
                return;
            } catch (Exception ignored) {}

            // Final fallback: try parsing as java.sql.Date / java.util.Date string
            try {
                java.util.Date utilDate = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(d.substring(0, 10));
                java.time.LocalDate ld = utilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                feeddatePicker.setDate(ld);
                return;
            } catch (Exception ignored) {}

            // If none matched, clear the picker
            feeddatePicker.clear();

        } catch (Exception ex) {
            ex.printStackTrace();
            feeddatePicker.clear();
        }
       
    }//GEN-LAST:event_Feedback_tbMouseClicked

    private void btnAdd_FeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdd_FeedbackActionPerformed
        // TODO add your handling code here:
        String appointmentIDText = feedAppointmentID.getText().trim();
        String feedbackNote = txtFeedbackNote.getText().trim();
        String status = feedStatus.getSelectedItem().toString();
        LocalDate selectedDate = feeddatePicker.getDate(); // <-- LGoodDatePicker returns LocalDate

        // ✅ Input validation
        if (appointmentIDText.isEmpty() || feedbackNote.isEmpty() || selectedDate == null || status.equals("---Select---")) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all fields correctly.",
                "Input Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int appointmentID;
        try {
            appointmentID = Integer.parseInt(appointmentIDText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid Appointment ID format.", 
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ✅ Convert LocalDate → java.sql.Date
        java.sql.Date sqlDate = java.sql.Date.valueOf(selectedDate);

        // ✅ SQL insert query
        String sql = "INSERT INTO feedback_tb (AppointmentID, FeedbackNote, FeedbackDate, Status) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, appointmentID);
            pst.setString(2, feedbackNote);
            pst.setDate(3, sqlDate);
            pst.setString(4, status);

            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Feedback added successfully!");

            // ✅ Refresh feedback table
            displayFeedback();

            // ✅ Clear fields
            feedAppointmentID.setText("");
            txtFeedbackNote.setText("");
            feeddatePicker.clear();
            feedStatus.setSelectedIndex(0);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error adding feedback: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_btnAdd_FeedbackActionPerformed

    private void btnUpdate_FeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdate_FeedbackActionPerformed
        // TODO add your handling code here:
        
        int selectedRow = Feedback_tb.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a feedback record to update.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) Feedback_tb.getModel();
        String feedbackID = model.getValueAt(selectedRow, 0).toString(); // FeedbackID column (0)

        try {
            // Get field values
            String appointmentID = feedAppointmentID.getText().trim();
            String feedbackNote = txtFeedbackNote.getText().trim();
            String status = feedStatus.getSelectedItem().toString();

            // Validate
            if (appointmentID.isEmpty() || feedbackNote.isEmpty() || status.equals("---Select---")) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.");
                return;
            }

            // Get date from DatePicker
            java.time.LocalDate selectedDate = feeddatePicker.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(this, "Please select a valid date.");
                return;
            }

            java.sql.Date sqlDate = java.sql.Date.valueOf(selectedDate);

            // SQL UPDATE query
            String sql = "UPDATE feedback_tb SET AppointmentID=?, FeedbackNote=?, FeedbackDate=?, Status=? WHERE FeedbackID=?";
            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, appointmentID);
            pst.setString(2, feedbackNote);
            pst.setDate(3, sqlDate);
            pst.setString(4, status);
            pst.setString(5, feedbackID);

            int rowsUpdated = pst.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Feedback updated successfully!");
                displayFeedback(); // your method to refresh Feedback_tb
            } else {
                JOptionPane.showMessageDialog(this, "No changes were made.");
            }

            pst.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "SQL Error: " + e.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
        
        
        
    }//GEN-LAST:event_btnUpdate_FeedbackActionPerformed

    private void feedSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feedSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_feedSearchActionPerformed

    private void feedSortByActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_feedSortByActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_feedSortByActionPerformed

    private void UserProfMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_UserProfMouseClicked
        // TODO add your handling code here:
        
        profile pf = new profile();
        pf.setVisible(true);
        pf.pack();
        pf.setLocationRelativeTo(null);
        pf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.dispose();
                   
        
    }//GEN-LAST:event_UserProfMouseClicked

    private void studentIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_studentIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_studentIDActionPerformed

    private void btnArchive_FeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnArchive_FeedbackActionPerformed
        // TODO add your handling code here:
        int selectedRow = Feedback_tb.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a feedback record to archive.");
            return;
        }

        int selectedFeedbackID = (int) Feedback_tb.getValueAt(selectedRow, 0); 

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to archive Feedback ID: " + selectedFeedbackID + "?",
                "Confirm Archive",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String archiveSQL = "REPLACE INTO archivefeedback_tb SELECT * FROM feedback_tb WHERE FeedbackID = ?";
        String deleteSQL = "DELETE FROM feedback_tb WHERE FeedbackID = ?";

        try (PreparedStatement pstArchive = con.prepareStatement(archiveSQL);
             PreparedStatement pstDelete = con.prepareStatement(deleteSQL)) {

            pstArchive.setInt(1, selectedFeedbackID);
            pstArchive.executeUpdate();

            pstDelete.setInt(1, selectedFeedbackID);
            pstDelete.executeUpdate();

            JOptionPane.showMessageDialog(this, "Feedback archived successfully!");
            
            refreshAllTables();
            displayAppointments();
            displayAvailability();
            displayFeedback();
            displayAppointmentsArchive();
            displayFeedbackArchive();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error archiving feedback: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnArchive_FeedbackActionPerformed

    private void btnClearAll_FeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearAll_FeedbackActionPerformed
        // TODO add your handling code here:
        try {
            
            
            feedAppointmentID.setText("");
            feeddatePicker.clear();
            feedStatus.setSelectedIndex(0);
            txtFeedbackNote.setText("");
            feedSearch.setText("");

            JOptionPane.showMessageDialog(this, "All fields cleared!");
            displayFeedback();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error clearing fields: " + ex.getMessage());
        }
    }//GEN-LAST:event_btnClearAll_FeedbackActionPerformed

    private void btnRetrieve_AppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRetrieve_AppointmentActionPerformed
        // TODO add your handling code here:
        int selectedRow = AppointmentArchive_tb.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to retrieve.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get AppointmentID from the selected row
        int selectedAppointmentID = (int) AppointmentArchive_tb.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to retrieve this appointment?", 
            "Confirm Retrieve", 
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

       
        String archiveAppointmentSQL = "REPLACE INTO appointment_tb " +
                                       "SELECT * FROM archiveappointment_tb WHERE AppointmentID = ?";
        String deleteAppointmentSQL = "DELETE FROM archiveappointment_tb WHERE AppointmentID = ?";

        try (PreparedStatement pstArchiveAppointment = con.prepareStatement(archiveAppointmentSQL);
             PreparedStatement pstDeleteAppointment = con.prepareStatement(deleteAppointmentSQL)) {

            con.setAutoCommit(false); // make it atomic (all or nothing)

            

            // Archive appointment
            pstArchiveAppointment.setInt(1, selectedAppointmentID);
            pstArchiveAppointment.executeUpdate();

            // Delete appointment
            pstDeleteAppointment.setInt(1, selectedAppointmentID);
            pstDeleteAppointment.executeUpdate();

            con.commit();
            JOptionPane.showMessageDialog(this, "Appointment retrieved successfully!");
            refreshAllTables();
            displayAppointments();
            displayAvailability();
            displayFeedback();
            displayAppointmentsArchive();
            displayFeedbackArchive();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error archiving appointment: " + ex.getMessage());
        }
        
    }//GEN-LAST:event_btnRetrieve_AppointmentActionPerformed

    private void btnRetrieve_FeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRetrieve_FeedbackActionPerformed
        // TODO add your handling code here:
        
        try {
        int selectedRow = feedArchive_tb.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a feedback record from the archive table first.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the selected IDs from the JTable
        int selectedFeedbackID = (int) feedArchive_tb.getValueAt(selectedRow, 0); // assuming FeedbackID is the 1st column
        int selectedAppointmentID = (int) feedArchive_tb.getValueAt(selectedRow, 1); // assuming AppointmentID is the 2nd column

        // Step 1️⃣: Check if related appointment exists in the main table
        String checkAppointmentSQL = "SELECT COUNT(*) FROM appointment_tb WHERE AppointmentID = ?";
        try (PreparedStatement pstCheck = con.prepareStatement(checkAppointmentSQL)) {
            pstCheck.setInt(1, selectedAppointmentID);
            ResultSet rs = pstCheck.executeQuery();
            rs.next();
            int appointmentExists = rs.getInt(1);
            rs.close();

            if (appointmentExists == 0) {
                JOptionPane.showMessageDialog(this,
                    "You must retrieve the corresponding appointment first before retrieving this feedback.",
                    "Retrieve Appointment First",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Step 2️⃣: Retrieve feedback if appointment exists
        String insertSQL = "INSERT IGNORE INTO feedback_tb SELECT * FROM archivefeedback_tb WHERE FeedbackID = ?";
        String deleteSQL = "DELETE FROM archivefeedback_tb WHERE FeedbackID = ?";

        try (PreparedStatement pstInsert = con.prepareStatement(insertSQL);
             PreparedStatement pstDelete = con.prepareStatement(deleteSQL)) {

            pstInsert.setInt(1, selectedFeedbackID);
            int inserted = pstInsert.executeUpdate();

            if (inserted > 0) {
                pstDelete.setInt(1, selectedFeedbackID);
                pstDelete.executeUpdate();
                JOptionPane.showMessageDialog(this, "Feedback retrieved successfully!");
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to retrieve feedback. It may already exist in the main table.",
                    "Retrieve Failed",
                    JOptionPane.WARNING_MESSAGE);
            }

            // Refresh tables after retrieval
            refreshAllTables();
            displayAppointments();
            displayAvailability();
            displayFeedback();
            displayAppointmentsArchive();
            displayFeedbackArchive();
        }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error retrieving feedback: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Unexpected error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnRetrieve_FeedbackActionPerformed

    private void btnSearch_SessionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearch_SessionActionPerformed
        // TODO add your handling code here:
        
        String searchBy = (String) cmbSortFeedbackArchive.getSelectedItem();
        String searchText = txtSearchFeedbackArchive.getText().trim();

        // Validate combo selection
        if (searchBy == null || searchBy.equals("---Sort by---")) {
            JOptionPane.showMessageDialog(this,
                "Please select a valid sort option.",
                "Invalid Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate input
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a keyword to search.",
                "Missing Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ✅ Corrected SQL base
        String sql = """
            SELECT 
                f.FeedbackID, 
                f.AppointmentID, 
                s.LastName, 
                s.FirstName, 
                f.FeedbackNote, 
                f.FeedbackDate, 
                f.Status
            FROM archivefeedback_tb f
            JOIN archiveappointment_tb a ON f.AppointmentID = a.AppointmentID
            JOIN student_tb s ON a.StudentID = s.StudentID
            WHERE 1=1
        """;

        // Add search condition
        if (searchBy.equals("AppointmentID")) {
            sql += " AND CAST(f.AppointmentID AS CHAR) LIKE ? ";
        } else if (searchBy.equals("LastName")) {
            sql += " AND s.LastName LIKE ? ";
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid search option selected.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        sql += " ORDER BY f.AppointmentID ASC";

        // Execute and populate feedArchive_tb
        try (PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, "%" + searchText + "%");

            try (ResultSet rs = pst.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) feedArchive_tb.getModel();
                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("FeedbackID"),
                        rs.getInt("AppointmentID"),
                        rs.getString("LastName"),
                        rs.getString("FirstName"),
                        rs.getString("FeedbackNote"),
                        rs.getDate("FeedbackDate"),
                        rs.getString("Status")
                    });
                }

                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this,
                        "No results found for '" + searchText + "'.",
                        "No Results",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error during search: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Unexpected error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }


        
    }//GEN-LAST:event_btnSearch_SessionActionPerformed

    private void btnSearch_AppointmentArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearch_AppointmentArchiveActionPerformed
        // TODO add your handling code here:
        
        String searchBy = (String) cmbSortAppointmentArchive.getSelectedItem();
        String searchText = txtSearchAppointmentArchive.getText().trim();

        // Validate combo selection
        if (searchBy == null || searchBy.equals("---Sort by---")) {
            JOptionPane.showMessageDialog(this,
                "Please select a valid sort option.",
                "Invalid Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate input
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a keyword to search.",
                "Missing Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Base SQL for archived appointments
        String sql = "SELECT a.AppointmentID, s.FirstName, s.MiddleName, s.LastName, a.Location, "
                   + "a.StartTime, a.EndTime, a.Purpose, a.Status "
                   + "FROM archiveappointment_tb a "
                   + "JOIN student_tb s ON a.StudentID = s.StudentID "
                   + "WHERE 1=1 ";

        // Add search condition
        if (searchBy.equals("AppointmentID")) {
            sql += "AND CAST(a.AppointmentID AS CHAR) LIKE ? ";
        } else if (searchBy.equals("LastName")) {
            sql += "AND s.LastName LIKE ? ";
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid search option selected.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        sql += "ORDER BY a.AppointmentID ASC";

        // Execute and populate AppointmentArchive_tn
        try (PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, "%" + searchText + "%");
            try (ResultSet rs = pst.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) AppointmentArchive_tb.getModel();
                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("AppointmentID"),
                        rs.getString("FirstName"),
                        rs.getString("MiddleName"),
                        rs.getString("LastName"),
                        rs.getString("Location"),
                        rs.getTimestamp("StartTime"),
                        rs.getTimestamp("EndTime"),
                        rs.getString("Purpose"),
                        rs.getString("Status")
                    });
                }

                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this,
                        "No results found for '" + searchText + "'.",
                        "No Results",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error during search: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Unexpected error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSearch_AppointmentArchiveActionPerformed

    private void cmbSortAppointmentArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSortAppointmentArchiveActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSortAppointmentArchiveActionPerformed

    private void btnSearch_SessionPastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearch_SessionPastActionPerformed
        // TODO add your handling code here:
        String searchBy = (String) cmbSortPast.getSelectedItem();
        String searchText = txtSearchPast.getText().trim();

        // Validate combo selection
        if (searchBy == null || searchBy.equals("---Sort by---")) {
            JOptionPane.showMessageDialog(this,
                "Please select a valid sort option.",
                "Invalid Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate input
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a keyword to search.",
                "Missing Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Base SQL for past sessions (appointments with StartTime date before today)
        String sql = "SELECT a.AppointmentID, s.FirstName, s.MiddleName, s.LastName, a.Location, "
                   + "a.StartTime, a.EndTime, a.Purpose, a.Status "
                   + "FROM appointment_tb a "
                   + "JOIN student_tb s ON a.StudentID = s.StudentID "
                   + "WHERE DATE(a.StartTime) < CURDATE() ";

        // Add search condition
        if (searchBy.equals("AppointmentID")) {
            sql += "AND CAST(a.AppointmentID AS CHAR) LIKE ? ";
        } else if (searchBy.equals("LastName")) {
            sql += "AND s.LastName LIKE ? ";
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid search option selected.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        sql += "ORDER BY a.AppointmentID ASC";

        // Execute and populate Past2_tb
        try (PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, "%" + searchText + "%");
            try (ResultSet rs = pst.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) Past2_tb.getModel();
                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("AppointmentID"),
                        rs.getString("FirstName"),
                        rs.getString("MiddleName"),
                        rs.getString("LastName"),
                        rs.getString("Location"),
                        rs.getTimestamp("StartTime"),
                        rs.getTimestamp("EndTime"),
                        rs.getString("Purpose"),
                        rs.getString("Status")
                    });
                }

                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this,
                        "No results found for '" + searchText + "'.",
                        "No Results",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error during search: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Unexpected error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSearch_SessionPastActionPerformed

    private void cmbSortPastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSortPastActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSortPastActionPerformed

    private void btnSearch_SessionUpcomingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearch_SessionUpcomingActionPerformed
        // TODO add your handling code here:
        
        String searchBy = (String) cmbSortUpcoming.getSelectedItem();
        String searchText = txtSearchUpcoming.getText().trim();

        // Validate combo selection
        if (searchBy == null || searchBy.equals("--- Search By ---")) {
            JOptionPane.showMessageDialog(this,
                "Please select a valid search option.",
                "Invalid Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate input
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a keyword to search.",
                "Missing Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Base SQL for upcoming sessions (appointments with StartTime date after today)
        String sql = "SELECT a.AppointmentID, s.FirstName, s.LastName, s.MiddleName, a.Location, "
                   + "a.StartTime, a.EndTime, a.Purpose, a.Status "
                   + "FROM appointment_tb a "
                   + "JOIN student_tb s ON a.StudentID = s.StudentID "
                   + "WHERE DATE(a.StartTime) > CURDATE() ";

        // Add search condition (always include the placeholder ?)
        if (searchBy.equals("AppointmentID")) {
            sql += "AND CAST(a.AppointmentID AS CHAR) LIKE ? ";
        } else if (searchBy.equals("LastName")) {
            sql += "AND s.LastName LIKE ? ";
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid search option selected.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        sql += "ORDER BY a.AppointmentID ASC";

        // Execute and populate Upcoming2_tb
        try (PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, "%" + searchText + "%");
            try (ResultSet rs = pst.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) Upcoming2_tb.getModel();
                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("AppointmentID"),
                        rs.getString("FirstName"),
                        rs.getString("MiddleName"),
                        rs.getString("LastName"),
                        rs.getString("Location"),
                        rs.getTimestamp("StartTime"),
                        rs.getTimestamp("EndTime"),
                        rs.getString("Purpose"),
                        rs.getString("Status")
                    });
                }

                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this,
                        "No results found for '" + searchText + "'.",
                        "No Results",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error during search: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Unexpected error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
        
    }//GEN-LAST:event_btnSearch_SessionUpcomingActionPerformed

    private void cmbSortUpcomingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSortUpcomingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSortUpcomingActionPerformed

    private void btnSearch_Session4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearch_Session4ActionPerformed
        // TODO add your handling code here:
        
        String searchBy = (String) cmbSortFeedbackArchive.getSelectedItem();
        String searchText = txtSearchFeedbackArchive.getText().trim();

        // Validate combo selection
        if (searchBy == null || searchBy.equals("---Sort by---")) {
            JOptionPane.showMessageDialog(this,
                "Please select a valid sort option.",
                "Invalid Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate input
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a keyword to search.",
                "Missing Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ✅ Corrected SQL query
        String sql = "SELECT a.FeedbackID, a.AppointmentID, s.LastName, s.FirstName, " +
                     "a.FeedbackNote, a.FeedbackDate, a.Status " +
                     "FROM archivefeedback_tb a " +
                     "JOIN appointment_tb ap ON a.AppointmentID = ap.AppointmentID " +
                     "JOIN student_tb s ON ap.StudentID = s.StudentID " +
                     "WHERE 1=1 ";

        // Add search condition
        if (searchBy.equals("AppointmentID")) {
            sql += "AND CAST(a.AppointmentID AS CHAR) LIKE ? ";
        } else if (searchBy.equals("LastName")) {
            sql += "AND s.LastName LIKE ? ";
        } else if (searchBy.equals("FirstName")) {
            sql += "AND s.FirstName LIKE ? ";
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid search option selected.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        sql += "ORDER BY a.AppointmentID ASC";

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, "%" + searchText + "%");

            try (ResultSet rs = pst.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) FeedbackArchive_tb.getModel();
                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("FeedbackID"),
                        rs.getInt("AppointmentID"),
                        rs.getString("LastName"),
                        rs.getString("FirstName"),
                        rs.getString("FeedbackNote"),
                        rs.getTimestamp("FeedbackDate"),
                        rs.getString("Status")
                    });
                }

                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this,
                        "No results found for '" + searchText + "'.",
                        "No Results",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Database error during search: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Unexpected error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
         
    }//GEN-LAST:event_btnSearch_Session4ActionPerformed

    private void cmbSearchByTodayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSearchByTodayActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSearchByTodayActionPerformed

    private void btnBackTodayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackTodayActionPerformed
        // TODO add your handling code here:
        
        txtTodaySearch.setText("");
        cmbSearchByToday.setSelectedIndex(0);
        refreshAllTables();
        displayAppointments();
        
    }//GEN-LAST:event_btnBackTodayActionPerformed

    private void btnBackUpcomingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackUpcomingActionPerformed
        // TODO add your handling code here:
        txtSearchUpcoming.setText("");
        cmbSortUpcoming.setSelectedIndex(0);
        refreshAllTables();
        displayAppointments();
    }//GEN-LAST:event_btnBackUpcomingActionPerformed

    private void btnBackPastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackPastActionPerformed
        // TODO add your handling code here:
        txtSearchPast.setText("");
        cmbSortPast.setSelectedIndex(0);
        refreshAllTables();
        displayAppointments();
    }//GEN-LAST:event_btnBackPastActionPerformed

    private void btnBackAArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackAArchiveActionPerformed
        // TODO add your handling code here:
        
        txtSearchAppointmentArchive.setText("");
        cmbSortAppointmentArchive.setSelectedIndex(0);
        refreshAllTables();
        displayAppointments();
        displayAppointmentsArchive();
    }//GEN-LAST:event_btnBackAArchiveActionPerformed

    private void btnBackFAchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackFAchiveActionPerformed
        // TODO add your handling code here:
        txtSearchFeedbackArchive.setText("");
        cmbSortFeedbackArchive.setSelectedIndex(0);
        refreshAllTables();
        displayAppointments();
        displayFeedbackArchive();
    }//GEN-LAST:event_btnBackFAchiveActionPerformed

    private void SortAvailabilitySearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SortAvailabilitySearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SortAvailabilitySearchActionPerformed

    private void txtSearchFeedbackArchiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchFeedbackArchiveActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSearchFeedbackArchiveActionPerformed

    private void btnSearch_FeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearch_FeedbackActionPerformed
        // TODO add your handling code here:
        String searchValue = feedSearch.getText().trim();
        String sortBy = feedSortBy.getSelectedItem().toString();

        // Determine sorting column
        String sortColumn;
        switch (sortBy) {
            case "AppointmentID" -> sortColumn = "f.AppointmentID";
            case "LastName" -> sortColumn = "s.LastName";
            default -> sortColumn = "f.FeedbackID"; // fallback
        }

        String sql = """
            SELECT 
                f.FeedbackID,
                f.AppointmentID,
                s.LastName,
                s.FirstName,
                f.FeedbackNote,
                f.FeedbackDate,
                f.Status
            FROM feedback_tb f
            JOIN appointment_tb a ON f.AppointmentID = a.AppointmentID
            LEFT JOIN student_tb s ON a.StudentID = s.StudentID
            WHERE 
                CAST(f.AppointmentID AS CHAR) LIKE ? 
                OR s.LastName LIKE ? 
                OR s.FirstName LIKE ?
                OR f.FeedbackNote LIKE ?
                OR f.Status LIKE ?
            ORDER BY %s ASC
            """.formatted(sortColumn);

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            String likeValue = "%" + searchValue + "%";
            pst.setString(1, likeValue);
            pst.setString(2, likeValue);
            pst.setString(3, likeValue);
            pst.setString(4, likeValue);
            pst.setString(5, likeValue);

            try (ResultSet rs = pst.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) Feedback_tb.getModel();
                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("FeedbackID"),
                        rs.getInt("AppointmentID"),
                        rs.getString("LastName"),
                        rs.getString("FirstName"),
                        rs.getString("FeedbackNote"),
                        rs.getDate("FeedbackDate"),
                        rs.getString("Status")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error searching feedback: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_btnSearch_FeedbackActionPerformed

    private void btnBackAvailabilityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackAvailabilityActionPerformed
        // TODO add your handling code here:
        txtSearchAv.setText("");
        SortAvailabilitySearch.setSelectedIndex(0);
        displayAvailability();
    }//GEN-LAST:event_btnBackAvailabilityActionPerformed

    private void btnSearch_AvailabilityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearch_AvailabilityActionPerformed
        // TODO add your handling code here:
        
        String searchValue = txtSearchAv.getText().trim();
        String sortBy = SortAvailabilitySearch.getSelectedItem().toString();

        // Validate combo selection
        if (sortBy.equals("---Sort by---")) {
            JOptionPane.showMessageDialog(this,
                "Please select a valid sort option.",
                "Invalid Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate search input
        if (searchValue.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a keyword to search.",
                "Missing Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Determine sorting column
        String sortColumn;
        switch (sortBy) {
            case "AvailabilityID" -> sortColumn = "a.AvailabilityID";
            case "LastName" -> sortColumn = "s.LastName";
            default -> sortColumn = "a.AvailabilityID"; // fallback
        }

        String sql = """
            SELECT 
                a.AvailabilityID,
                s.LastName,
                s.FirstName,
                a.StartTime,
                a.EndTime,
                a.Status
            FROM availability_tb a
            JOIN student_tb s ON a.StudentID = s.StudentID
            WHERE 
                CAST(a.AvailabilityID AS CHAR) LIKE ? 
                OR s.LastName LIKE ?
                OR s.FirstName LIKE ?
                OR a.Status LIKE ?
            ORDER BY %s ASC
            """.formatted(sortColumn);

        try (PreparedStatement pst = con.prepareStatement(sql)) {
            String likeValue = "%" + searchValue + "%";
            pst.setString(1, likeValue);
            pst.setString(2, likeValue);
            pst.setString(3, likeValue);
            pst.setString(4, likeValue);

            try (ResultSet rs = pst.executeQuery()) {
                DefaultTableModel model = (DefaultTableModel) Availability_tb.getModel();
                model.setRowCount(0);

                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("AvailabilityID"),
                        rs.getString("LastName"),
                        rs.getString("FirstName"),
                        rs.getTimestamp("StartTime"),
                        rs.getTimestamp("EndTime"),
                        rs.getString("Status")
                    });
                }

                if (model.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this,
                        "No results found for '" + searchValue + "'.",
                        "No Results",
                        JOptionPane.INFORMATION_MESSAGE);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error searching availability: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSearch_AvailabilityActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SystemGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SystemGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SystemGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SystemGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SystemGUI().setVisible(true);
            }
        });
        
        
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable AppointmentArchive_tb;
    private javax.swing.JTable Appointment_tb;
    private javax.swing.JTable Availability_tb;
    private javax.swing.JButton BTNSeeAll_Past;
    private javax.swing.JButton BTNSeeAll_Today;
    private javax.swing.JButton BTNSeeAll_Upcoming;
    private javax.swing.JButton ClearAll;
    private com.github.lgooddatepicker.components.DateTimePicker EndTime;
    private javax.swing.JTabbedPane FeedbackArchive_tb;
    private javax.swing.JTable Feedback_tb;
    private javax.swing.JTextField Location;
    private javax.swing.JLabel LogoIcon;
    private javax.swing.JTabbedPane MainTabbedPane;
    private javax.swing.JTable Past1_tb;
    private javax.swing.JTable Past2_tb;
    private javax.swing.JComboBox<String> Purpose;
    private javax.swing.JComboBox<String> SortAvailabilitySearch;
    private com.github.lgooddatepicker.components.DateTimePicker StartTime;
    private javax.swing.JComboBox<String> Status_Appointment;
    private javax.swing.JLabel TXTPastSession;
    private javax.swing.JLabel TXTTodaysSession;
    private javax.swing.JLabel TXTUpcomingSession;
    private javax.swing.JTable Today1_tb;
    private javax.swing.JTable Today2_tb;
    private javax.swing.JTable Upcoming1_tb;
    private javax.swing.JTable Upcoming2_tb;
    private javax.swing.JLabel UserProf;
    private javax.swing.JLabel WMessage;
    private javax.swing.JButton btnAdd_Appointment;
    private javax.swing.JButton btnAdd_Feedback;
    private javax.swing.JButton btnArchive_Appointment;
    private javax.swing.JButton btnArchive_Feedback;
    private javax.swing.JButton btnBackAArchive;
    private javax.swing.JButton btnBackAvailability;
    private javax.swing.JButton btnBackFAchive;
    private javax.swing.JButton btnBackPast;
    private javax.swing.JButton btnBackToday;
    private javax.swing.JButton btnBackUpcoming;
    private javax.swing.JButton btnClearAll_Feedback;
    private javax.swing.JButton btnRetrieve_Appointment;
    private javax.swing.JButton btnRetrieve_Feedback;
    private javax.swing.JButton btnSearch_Appointment;
    private javax.swing.JButton btnSearch_AppointmentArchive;
    private javax.swing.JButton btnSearch_Availability;
    private javax.swing.JButton btnSearch_Feedback;
    private javax.swing.JButton btnSearch_Session;
    private javax.swing.JButton btnSearch_Session4;
    private javax.swing.JButton btnSearch_SessionPast;
    private javax.swing.JButton btnSearch_SessionUpcoming;
    private javax.swing.JButton btnUpdate_Appointment;
    private javax.swing.JButton btnUpdate_Feedback;
    private javax.swing.JComboBox<String> cmbSearchBy;
    private javax.swing.JComboBox<String> cmbSearchByToday;
    private javax.swing.JComboBox<String> cmbSortAppointmentArchive;
    private javax.swing.JComboBox<String> cmbSortFeedbackArchive;
    private javax.swing.JComboBox<String> cmbSortPast;
    private javax.swing.JComboBox<String> cmbSortUpcoming;
    private javax.swing.JTextField feedAppointmentID;
    private javax.swing.JTable feedArchive_tb;
    private javax.swing.JTextField feedSearch;
    private javax.swing.JComboBox<String> feedSortBy;
    private javax.swing.JComboBox<String> feedStatus;
    private com.github.lgooddatepicker.components.DatePicker feeddatePicker;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel46;
    private javax.swing.JPanel jPanel47;
    private javax.swing.JPanel jPanel48;
    private javax.swing.JPanel jPanel49;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel50;
    private javax.swing.JPanel jPanel51;
    private javax.swing.JPanel jPanel52;
    private javax.swing.JPanel jPanel53;
    private javax.swing.JPanel jPanel54;
    private javax.swing.JPanel jPanel56;
    private javax.swing.JPanel jPanel57;
    private javax.swing.JPanel jPanel58;
    private javax.swing.JPanel jPanel59;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel60;
    private javax.swing.JPanel jPanel61;
    private javax.swing.JPanel jPanel62;
    private javax.swing.JPanel jPanel63;
    private javax.swing.JPanel jPanel64;
    private javax.swing.JPanel jPanel65;
    private javax.swing.JPanel jPanel66;
    private javax.swing.JPanel jPanel67;
    private javax.swing.JPanel jPanel68;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane20;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane22;
    private javax.swing.JScrollPane jScrollPane23;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JComboBox<String> studentID;
    private javax.swing.JTextArea txtFeedbackNote;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSearchAppointmentArchive;
    private javax.swing.JTextField txtSearchAv;
    private javax.swing.JTextField txtSearchFeedbackArchive;
    private javax.swing.JTextField txtSearchPast;
    private javax.swing.JTextField txtSearchUpcoming;
    private javax.swing.JTextField txtTodaySearch;
    // End of variables declaration//GEN-END:variables

    private void clearFields() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
