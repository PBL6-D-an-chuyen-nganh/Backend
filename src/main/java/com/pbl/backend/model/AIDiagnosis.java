package com.pbl.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    //@OneToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "scanImageID")
    //private ScanImage scanImage;

    private String disease;

    private LocalDate dateOfDiagnosis;

    private String severity;
}
