
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

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  public PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate)
      {
        List<AnnualizedReturn> annualizedReturn = new ArrayList<AnnualizedReturn>();
        for(PortfolioTrade p: portfolioTrades)
    {
      List<Candle> tiingocandle = getStockQuote(p.getSymbol(),p.getPurchaseDate(),endDate);
     // List<Candle> tiingocandle = fetchCandles(p,localDate,getToken());
   /*  String url = prepareUrl(p, localDate, getToken());
    RestTemplate restTemplate = new RestTemplate();
    TiingoCandle[] ob= restTemplate.getForObject(url,TiingoCandle[].class);
    List<Candle> tiingocandle = Arrays.asList(ob);*/
    if( tiingocandle==null){
      continue;
    }
    Double purchasePrice = getOpeningPriceOnStartDate(tiingocandle);//ob[0].getOpen();
    Double sellPrice = getClosingPriceOnEndDate(tiingocandle);//ob[ob.length-1].getClose();
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
  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


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
