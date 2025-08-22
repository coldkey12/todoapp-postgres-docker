package kz.don.todoapp.dto;

import lombok.Data;

@Data
public class ProductFilter {
    private String direction;
    private String sortBy;
    private int amount;
    private int size;
    private int page;
}
