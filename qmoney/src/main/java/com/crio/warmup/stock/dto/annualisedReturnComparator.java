package com.crio.warmup.stock.dto;
import java.util.Comparator;
public class annualisedReturnComparator implements Comparator<AnnualizedReturn>{
    @Override
    public int compare(AnnualizedReturn a1,AnnualizedReturn a2)
    {
        return (int)(a2.getAnnualizedReturn() - a1.getAnnualizedReturn());
    }
}
