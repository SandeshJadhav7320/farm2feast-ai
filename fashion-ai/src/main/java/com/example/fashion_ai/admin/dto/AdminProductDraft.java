
package com.example.fashion_ai.admin.dto;

import java.math.BigDecimal;

public class AdminProductDraft {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String category;

    public AdminProductDraft() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isComplete() {
        return name != null && !name.isBlank()
                && description != null && !description.isBlank()
                && price != null && price.signum() > 0
                && quantity != null && quantity >= 0
                && category != null && !category.isBlank();
    }
}
