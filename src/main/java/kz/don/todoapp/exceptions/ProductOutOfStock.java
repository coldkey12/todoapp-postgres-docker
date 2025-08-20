package kz.don.todoapp.exceptions;

public class ProductOutOfStock extends RuntimeException {
    public ProductOutOfStock(String productName) {
        super("Product '" + productName + "' is out of stock");
    }
}
