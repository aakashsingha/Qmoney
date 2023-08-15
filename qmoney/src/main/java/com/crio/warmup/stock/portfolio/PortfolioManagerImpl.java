
package com.crio.warmup.stock.portfolio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  private RestTemplate restTemplate;


  public PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate)
      {
        List<AnnualizedReturn> annualizedReturn = new ArrayList<AnnualizedReturn>();
        for(PortfolioTrade p: portfolioTrades)
    {
      List<Candle> tiingocandle = getStockQuote(p.getSymbol(),p.getPurchaseDate(),endDate);
    if( tiingocandle==null){
      continue;
    }
    Double purchasePrice = getOpeningPriceOnStartDate(tiingocandle);
    Double sellPrice = getClosingPriceOnEndDate(tiingocandle);
    AnnualizedReturn annualizedReturnobj = calculateAnnualizedReturnsRefactored(endDate,p,purchasePrice,sellPrice);
    annualizedReturn.add(annualizedReturnobj);
  }
  Collections.sort(annualizedReturn,getComparator());
        return annualizedReturn;
      }

      static Double getOpeningPriceOnStartDate(List<Candle> candles) {
        return candles.get(0).getOpen();
     }

     public static Double getClosingPriceOnEndDate(List<Candle> candles) {
      return candles.get(candles.size()-1).getClose();
   }

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  public static AnnualizedReturn calculateAnnualizedReturnsRefactored(LocalDate endDate,
     PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      Double totalReturns =(sellPrice-buyPrice)/buyPrice;
      int totalDays = (int) ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
      //System.out.println(totalDays);
      Double totalyears =(double) (totalDays/365.24);
      Double annualisedReturns = Math.pow((1+totalReturns),(1/totalyears))-1;
      String symbol=trade.getSymbol();
     return new AnnualizedReturn(symbol, annualisedReturns,totalReturns);
 }
 


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) {
        String url = buildUri(symbol, from, to);
        RestTemplate restTemplate = new RestTemplate();
        TiingoCandle[] ob= restTemplate.getForObject(url,TiingoCandle[].class);
        List<Candle> tiingocandle = Arrays.asList(ob);
     return tiingocandle;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String apikey="d7ee5290251fd4882f10fde8ada179ccc1450745";
       String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="+startDate+"&endDate="+endDate+"&token="+apikey;
            return uriTemplate;

            
  }
}
