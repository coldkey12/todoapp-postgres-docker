package kz.don.todoapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.HashMap;

@Data
public class CartRequest {
    private HashMap<String, Integer> products;

    @NotBlank
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-z]+$", message = "Firstname must contain only latin letters")
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-z]+$", message = "Second name must contain only latin letters")
    private String secondName;

    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-z]+$", message = "Second name must contain only latin letters")
    private String patronymic;

    @Pattern(regexp = "\\d{12}", message = "IIN must be 12 digits")
    private String IIN;

    @Size(min = 5, max = 255)
    @Pattern(
            regexp = "^(?=.*(Almaty|Astana))[A-Za-z0-9 ,.\\-/#]+$",
            message = "Address can only contain latin letters, digits, spaces, and ,.-/#, city must be Almaty or Astana"
    )
    private String address;

    @Pattern(regexp = "^\\+7\\d{10}$")
    @NotBlank
    private String number;

}
