package com.crio.warmup.stock.dto;

import java.util.Comparator;

public class ClosingPriceComparator implements Comparator<TotalReturnsDto> {
    @Override
    public int compare(TotalReturnsDto s1,TotalReturnsDto s2)
    {
        return (int)(s1.getClosingPrice()-s2.getClosingPrice());
    }
    
}
