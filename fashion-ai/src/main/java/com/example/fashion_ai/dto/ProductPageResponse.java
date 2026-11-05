package com.example.fashion_ai.dto;

import java.util.List;

public class ProductPageResponse {

    private List<StoreProduct> content;
    private Long nextCursor;
    private boolean hasNext;

    public ProductPageResponse() {
    }

    public List<StoreProduct> getContent() {
        return content;
    }

    public void setContent(List<StoreProduct> content) {
        this.content = content;
    }

    public Long getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(Long nextCursor) {
        this.nextCursor = nextCursor;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
}