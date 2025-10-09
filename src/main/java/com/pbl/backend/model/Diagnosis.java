package com.pbl.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "diagnoses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Diagnosis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diagnosisID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recordID")
    private MedicalRecord medicalRecord;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctorID")
    private Doctor doctor;

    //@OneToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "imageID")
    //private ImageEntity image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aiID")
    private AIDiagnosis aiDiagnosis;

    private String disease;

    private LocalDate dateOfDiagnosis;

    private String severity;

    @Column(length = 4000)
    private String doctorNotes;
}
