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

//package com.crio.warmup.stock;
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



  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

 // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
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
  //return Collections.emptyList();
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
   /*  String url = prepareUrl(p, localDate, getToken());
    RestTemplate restTemplate = new RestTemplate();
    TiingoCandle[] ob= restTemplate.getForObject(url,TiingoCandle[].class);
    List<Candle> tiingocandle = Arrays.asList(ob);*/
    if( tiingocandle==null){
      continue;
    }
    Double purchasePrice = getOpeningPriceOnStartDate(tiingocandle);//ob[0].getOpen();
    Double sellPrice = getClosingPriceOnEndDate(tiingocandle);//ob[ob.length-1].getClose();
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
  //Collections.sort(annualizedReturn,new annualisedReturnComparator());
  return annualizedReturn;
   // return Collections.emptyList();
 }


// TODO: CRIO_TASK_MODULE_CALCULATIONS
 //  Return the populated list of AnnualizedReturn for all stocks.
 //  Annualized returns should be calculated in two steps:
 //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
 //      1.1 Store the same as totalReturns
 //   2. Calculate extrapolated annualized returns by scaling the same in years span.
 //      The formula is:
 //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
 //      2.1 Store the same as annualized_returns
 //  Test the same using below specified command. The build should be successful.
 //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

 public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
     PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      Double totalReturns =(sellPrice-buyPrice)/buyPrice;
      int totalDays = (int) ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
      //System.out.println(totalDays);
      Double totalyears =(double) (totalDays/365.24);
      Double annualisedReturns = Math.pow((1+totalReturns),(1/totalyears))-1;
      String symbol=trade.getSymbol();
     return new AnnualizedReturn(symbol, annualisedReturns,totalReturns);
 }

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Task:
  //       - Read the json file provided in the argument[0], The file is available in the classpath.
  //       - Go through all of the trades in the given file,
  //       - Prepare the list of all symbols a portfolio has.
  //       - if "trades.json" has trades like
  //         [{ "symbol": "MSFT"}, { "symbol": "AAPL"}, { "symbol": "GOOGL"}]
  //         Then you should return ["MSFT", "AAPL", "GOOGL"]
  //  Hints:
  //    1. Go through two functions provided - #resolveFileFromResources() and #getObjectMapper
  //       Check if they are of any help to you.
  //    2. Return the list of all symbols in the same order as provided in json.

  //  Note:
  //  1. There can be few unused imports, you will need to fix them to make the build pass.
  //  2. You can use "./gradlew build" to check if your code builds successfully.

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
    // return Collections.emptyList();
  }


  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.


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






  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>
  
  

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    //File file= resolveFileFromResources(args[0]);
   // ObjectMapper om = getObjectMapper();
    //PortfolioTrade[] obj = om.readValue(file,PortfolioTrade[].class);
    List<PortfolioTrade> obj = readTradesFromJson(args[0]);
    //System.out.println(obj.size());
     List<TotalReturnsDto> trd=new ArrayList<TotalReturnsDto>();
   // List<PortfolioTrade> portfolioTradres = readTradesFromJson(args[0]);
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
    //Double closingprice=ob[ob.length-1].getClose();
    TotalReturnsDto temp = new TotalReturnsDto(p.getSymbol(),ob[ob.length-1].getClose());
    trd.add(temp);
    }
  /*   Double closingprice=0.0;
    for(TiingoCandle o:ob)
    {
      closingprice=o.getClose();
    }
    int c=0;
    trd.get(c).setSymbol(p.getSymbol());
    trd.get(c).setClosingPrice(closingprice);
    c++;
  }*/
   Collections.sort(trd,new ClosingPriceComparator());
   List<String> list = new ArrayList<String>();
   for(TotalReturnsDto o: trd)
   {
    System.out.println(o.getSymbol());
     list.add(o.getSymbol());
   }
   return list ;

   // return Collections.emptyList();
  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);
    return Arrays.asList(trades); 
   // return Collections.emptyList();
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate() + "&endDate="+ endDate +"&token=" +token;
    // return Collections.emptyList();
    return url;
     //return null;
  }

  public static String getToken(){
    return "d7ee5290251fd4882f10fde8ada179ccc1450745";
  }


 // public static String readfileName(String filename) throws IOException, URISyntaxException {
  //  return new String(Files.readAllBytes(resolveFileFromResources(filename).toPath()), "UTF-8");
//  }




// TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       List<PortfolioTrade> portfoliotrades= readTradesFromJson(file);
       //String contents = readFileAsString(file);
      // ObjectMapper objectMapper = getObjectMapper();
      PortfolioManagerImpl object = new PortfolioManagerImpl(new RestTemplate());
       return object.calculateAnnualizedReturn(portfoliotrades, endDate);
  }




  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());



    //printJsonObject(mainCalculateSingleReturn(args));
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}


