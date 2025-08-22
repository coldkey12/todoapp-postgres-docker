package kz.don.todoapp.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductResponse {
    private UUID id;
    private String title;
    private String description;
    private Long quantity;
    private double price;
}
