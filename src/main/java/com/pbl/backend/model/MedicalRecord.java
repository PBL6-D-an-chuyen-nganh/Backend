package com.pbl.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordID;

//    @OneToOne(mappedBy = "medicalRecord")
//    private Patient patient;

//    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
//    private List<Diagnosis> diagnoses;

}
