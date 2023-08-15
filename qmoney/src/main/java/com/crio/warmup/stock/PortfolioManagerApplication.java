package com.crio.warmup.stock;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.ClosingPriceComparator;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {



 public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
 }




public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size()-1).getClose();
 }


public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
 
   String url = prepareUrl(trade, endDate, token);
   RestTemplate restTemplate = new RestTemplate();
    TiingoCandle[] ob= restTemplate.getForObject(url,TiingoCandle[].class);
    List<Candle> tiingocandle = Arrays.asList(ob);
    return tiingocandle;  
 
 }


public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
     throws IOException, URISyntaxException {
      List<AnnualizedReturn> annualizedReturn = new ArrayList<AnnualizedReturn>();
      List<PortfolioTrade> obj = readTradesFromJson(args[0]);
      String endDate = args[1];
      LocalDate localDate = LocalDate.parse(endDate);
      for(PortfolioTrade p: obj)
    {
      List<Candle> tiingocandle = fetchCandles(p,localDate,getToken());
    if( tiingocandle==null){
      continue;
    }
    Double purchasePrice = getOpeningPriceOnStartDate(tiingocandle);
    Double sellPrice = getClosingPriceOnEndDate(tiingocandle);
    AnnualizedReturn annualizedReturnobj = calculateAnnualizedReturns(localDate,p,purchasePrice,sellPrice);
    annualizedReturn.add(annualizedReturnobj);
  }
  Collections.sort(annualizedReturn,new Comparator<AnnualizedReturn>() {
    @Override
    public int compare(AnnualizedReturn a1,AnnualizedReturn a2)
    {
      return a2.getAnnualizedReturn().compareTo(a1.getAnnualizedReturn());
    }
  });
  
  return annualizedReturn;
 }




 public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
     PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      Double totalReturns =(sellPrice-buyPrice)/buyPrice;
      int totalDays = (int) ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
      Double totalyears =(double) (totalDays/365.24);
      Double annualisedReturns = Math.pow((1+totalReturns),(1/totalyears))-1;
      String symbol=trade.getSymbol();
     return new AnnualizedReturn(symbol, annualisedReturns,totalReturns);
 }


  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {

    File file= resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();
    PortfolioTrade[] obj = om.readValue(file,PortfolioTrade[].class);
    List<String> l = new ArrayList<String>();
    for(PortfolioTrade p: obj)
    {
      l.add(p.getSymbol());
    }
    return l;
    
  }


  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 ="/home/crio-user/workspace/aakashbisht098-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@1573f9fc";
    String functionNameFromTestFileInStackTrace = "PortfolioManagerApplication.mainReadFile()";
    String lineNumberFromTestFileInStackTrace = "29";


   return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
       toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
       lineNumberFromTestFileInStackTrace});
 }






  
  

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    List<PortfolioTrade> obj = readTradesFromJson(args[0]);
     List<TotalReturnsDto> trd=new ArrayList<TotalReturnsDto>();
    String token=getToken();
    String endDate = args[1];
    LocalDate localDate = LocalDate.parse(endDate);
    for(PortfolioTrade p: obj)
    {
    String url = prepareUrl(p, localDate, token);
    RestTemplate restTemplate = new RestTemplate();
    TiingoCandle[] ob= restTemplate.getForObject(url,TiingoCandle[].class);
    if( ob==null){
      continue;
    }
    
    TotalReturnsDto temp = new TotalReturnsDto(p.getSymbol(),ob[ob.length-1].getClose());
    trd.add(temp);
    }
   Collections.sort(trd,new ClosingPriceComparator());
   List<String> list = new ArrayList<String>();
   for(TotalReturnsDto o: trd)
   {
    System.out.println(o.getSymbol());
     list.add(o.getSymbol());
   }
   return list ;

  }

  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);
    return Arrays.asList(trades); 
  }


  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate() + "&endDate="+ endDate +"&token=" +token;
    return url;
  }

  public static String getToken(){
    return "d7ee5290251fd4882f10fde8ada179ccc1450745";
  }





  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       List<PortfolioTrade> portfoliotrades= readTradesFromJson(file);
      PortfolioManagerImpl object = new PortfolioManagerImpl(new RestTemplate());
       return object.calculateAnnualizedReturn(portfoliotrades, endDate);
  }




  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}


