package ecommerce.address.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5)
    private String street;

    @NotBlank
    @Size(min = 5)
    private String buildingName;

    @NotBlank
    @Size(min = 4)
    private String city;

    @NotBlank
    @Size(min = 2)
    private String state;

    @NotBlank
    @Size(min = 2)
    private String country;

    @NotBlank
    @Size(min = 5)
    private String pinCode;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}