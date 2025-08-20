package kz.don.todoapp.dto.request;

import lombok.Data;

@Data
public class ProductRequest {
    private String title;
    private String description;
    private Double price;
    private Integer quantity;
}
