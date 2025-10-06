package com.pbl.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
/*
package com.pbl.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String authStatus;

    public enum Role {
        ROLE_USER,
        ROLE_ADMIN,
        ROLE_DOCTOR,
        ROLE_PATIENT
    }
}

// --------------------------- Patient ---------------------------
@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Patient extends User {

    private String gender;

    private LocalDate dateOfBirth;

    private String recordID;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Diagnosis> diagnoses;
}

// --------------------------- Doctor ---------------------------
@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor extends User {

    private String gender;

    private Integer yoe;

    @Column(length = 2000)
    private String introduction;

    private String avatarFilepath;

    @Column(length = 4000)
    private String medicalRecords;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Diagnosis> diagnoses;
}

// --------------------------- Articles ---------------------------
@Entity
@Table(name = "articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleID;

    private String title;

    @Column(length = 10000)
    private String content;

    private String category;

    private LocalDateTime createdAt;
}

// --------------------------- Posts ---------------------------
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorID")
    private User author;

    private String title;

    @Column(length = 5000)
    private String content;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments;
}

// --------------------------- Comments ---------------------------
@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postID")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorID")
    private User author;

    @Column(length = 2000)
    private String content;

    private LocalDateTime createdAt;
}

// --------------------------- Appointments ---------------------------
@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientID")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctorID")
    private Doctor doctor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnosisID")
    private Diagnosis diagnosis;

    private LocalDateTime time;

    private String status;

    @Column(length = 2000)
    private String note;
}

// --------------------------- Scan Images ---------------------------
@Entity
@Table(name = "scanImages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScanImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scanImageID;

    private String imageFilepath;

    @OneToOne(mappedBy = "scanImage")
    private AIDiagnosis aiDiagnosis;
}

// --------------------------- AI Diagnoses ---------------------------
@Entity
@Table(name = "AIDiagnoses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIDiagnosis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aiID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientID")
    private Patient patient;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scanImageID")
    private ScanImage scanImage;

    private String disease;

    private LocalDate dateOfDianosis;

    private String severity;
}

// --------------------------- Diagnoses ---------------------------
@Entity
@Table(name = "diagnoses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Diagnosis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dianosisID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientID")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctorID")
    private Doctor doctor;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imageID")
    private ImageEntity image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aiID")
    private AIDiagnosis aiDiagnosis;

    private String disease;

    private LocalDate dateOfDianosis;

    private String severity;

    @Column(length = 4000)
    private String doctorNotes;
}

// --------------------------- Images ---------------------------
@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageID;

    private String imageFilepath;
}

// --------------------------- Schedules ---------------------------
@Entity
@Table(name = "schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctorID")
    private Doctor doctor;

    @Column(length = 1000)
    private String busyTime;
}

 */