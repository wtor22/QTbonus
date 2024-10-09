package com.quartztop.bonus.orders;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "uploaded_images")
public class UploadedImages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "path_to_images")
    String pathToImages;
    int orderId;
}
