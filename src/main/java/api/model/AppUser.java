package api.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

@Data // Create getters and setters
@NoArgsConstructor
@Entity
@Table(name="AUT_USUARIO", schema = "AUT")
public class AppUser {

    @Id
    @Column(name="ID")
    private Integer id;
    
    @Column(name="USERNAME", unique = true, nullable = false)
    @Size(min = 4, max = 255, message = "Minimum username length: 4 characters")
    private String username;

    @Column(name="EMAIL",unique = true, nullable = false)
    private String email;
    
    @Column(name="PASSWORD_HASH",unique = true, nullable = false)
    @Size(min = 8, message = "Minimum password length: 8 characters")
    private String password;
    
    @Transient
    @ElementCollection(fetch = FetchType.EAGER)
    List<AppUserRole> appUserRoles;

}
