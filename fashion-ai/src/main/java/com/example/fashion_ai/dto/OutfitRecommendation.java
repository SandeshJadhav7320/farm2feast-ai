
package com.example.fashion_ai.dto;

import java.util.List;

public class OutfitRecommendation {

    private List<Outfit> outfits;

    public OutfitRecommendation() {
    }

    public List<Outfit> getOutfits() {
        return outfits;
    }

    public void setOutfits(List<Outfit> outfits) {
        this.outfits = outfits;
    }
}
