package ch.treasurekeep.rest;

import ch.treasurekeep.data.LogRepository;
import ch.treasurekeep.model.Log;
import ch.treasurekeep.model.RiskColumn;
import ch.treasurekeep.service.SettingsService;
import ch.treasurekeep.service.interactivebrokers.InteractiveBrokersService;
import ch.treasurekeep.service.interactivebrokers.callbacks.PortfolioRiskStatusCallback;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * The PortfolioRiskController offers a GET request that returns a very basic
 * html-page giving a visual overview of the risks a portfolio is currently being exposed
 */
@RestController
public class PortfolioRiskController {

    private InteractiveBrokersService interactiveBrokersService;
    private SettingsService settingsService;
    private LogRepository logRepository;

    public PortfolioRiskController(InteractiveBrokersService interactiveBrokersService, SettingsService settingsService, LogRepository logRepository) {
        this.interactiveBrokersService = interactiveBrokersService;
        this.settingsService = settingsService;
        this.logRepository = logRepository;
    }


    @GetMapping(value = "riskReport", produces = "text/html")
    public String getRiskReport() {
        try {
            List<RiskColumn> result = new ArrayList<>();
            List<String> messages = new ArrayList<>();
            String baseCurrencyAtStart = settingsService.getSettings().getBaseCurrency();
            CountDownLatch countDownLatch = new CountDownLatch(1);
            interactiveBrokersService.createRiskReport(new PortfolioRiskStatusCallback.Callback() {
                @Override
                public void callback(List<RiskColumn> riskColumns) {
                    result.addAll(riskColumns);
                    try{ countDownLatch.countDown(); }
                    catch (Exception e) { Thread.currentThread().interrupt(); }
                }

                @Override
                public void error(String message) {
                    System.out.println(message);//TODO remove
                    PortfolioRiskController.this.logRepository.insert(new Log(PortfolioRiskController.class.getName(), message));
                    try{ countDownLatch.countDown(); }
                    catch (Exception e) { Thread.currentThread().interrupt(); }
                    messages.add(message);
                }
            });
                countDownLatch.await();
                if(!baseCurrencyAtStart.equals(this.settingsService.getSettings().getBaseCurrency())) {
                    throw new IllegalStateException("During this request the base-currency changed, please retry");
                }
            return messages.size() == 0 ? produceHtmlUI(result, baseCurrencyAtStart) : messages.get(0);
        }
        catch (Exception e ) {
            this.logRepository.insert(new Log(PortfolioRiskController.class.getName(), e.getMessage()));
            return e.getMessage();
        }
    }

    private String produceHtmlUI(List<RiskColumn> columns, String baseCurrency){
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>Risk-Report</title>\n" +
                "    <link rel=\"stylesheet\" href=\"style.css\">\n" +
                "    <script src=\"script.js\"></script>\n" +
                "  </head>\n" +
                "  <body>");
        sb.append("<table style=\"width:100%\">");
        sb.append("  <tr>\n" +
                "    <th>Account</th>\n" +
                "    <th>Symbol</th>\n" +
                "    <th>Exchange</th>\n" +
                "    <th align=\"right\" >avgPrice</th>\n" +
                "    <th>market-price</th>\n" +
                "    <th>Stop</th>\n" +
                "    <th>Quantity</th>\n" +
                "    <th>MarketValue</th>\n" +
                "    <th>Profit</th>\n" +
                "    <th>Risk(" + baseCurrency + ")</th>\n" +
                "    <th>Risk (%)</th>\n" +
                "    <th>% of Total (\" + baseCurrency + \")</th>\n" +
                "  </tr>");
        for(RiskColumn col : columns) {
            String color = StringUtils.isEmpty(col.getSymbol()) ? "pink" : (col.getStop() != null && col.getStop() == 0 ? "yellow" : "white");
            String colorRight = StringUtils.isEmpty(col.getSymbol()) ? "pink" : "lightgrey";
            sb.append("<tr>");
            sb.append("<td color=\"" + color                        + "\">" + (col.getAccount() != null ? col.getAccount() : "")                 + "</td>");
            sb.append("<td bgcolor=\""+ color                       + "\">" + (col.getSymbol() != null ? col.getSymbol() : "")                   + "</td>");
            sb.append("<td bgcolor=\""+ color                       + "\">" + (col.getExchange() != null ? col.getExchange() : "")               + "</td>");
            sb.append("<td align=\"right\" bgcolor=\""+ color       + "\">" + (formatNumber(col.getAvgPrice(), 2, col.getCurrency()))      + "</td>");
            sb.append("<td align=\"right\" bgcolor=\""+ color       + "\">" + (formatNumber(col.getCurrent(), 2,  col.getCurrency()))      + "</td>");
            sb.append("<td bgcolor=\""+ color                       + "\">" + (formatNumber(col.getStop(), 2, col.getCurrency()))          + "</td>");
            sb.append("<td align=\"right\" bgcolor=\""+ color       + "\">" + (col.getQuantity() != null ? col.getQuantity() : "")               + "</td>");
            sb.append("<td align=\"right\" bgcolor=\""+ color       + "\">" + (formatNumber(col.getMarketValue(), 2, col.getCurrency()))   + "</td>");
            sb.append("<td align=\"right\" bgcolor=\""+ color       + "\">" + (formatNumber(col.getProfit(), 2, col.getCurrency()))        + "</td>");
            sb.append("<td align=\"right\" bgcolor=\"" + colorRight + "\">" + (formatNumber(col.getRiskInBasecurrency(), 2, baseCurrency)) + "</td>");
            sb.append("<td align=\"right\" bgcolor=\"" + colorRight + "\">" + (formatNumber(col.getRiskInPercentage(), 2, "%"))      + "</td>");
            sb.append("<td align=\"right\" bgcolor=\"" + colorRight + "\">" + (formatNumber(col.getPercentageOfTotalRisk(), 2, "%")) + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        sb.append(" </body>\n" + "</html>");

        return sb.toString();
    }

    private String formatNumber(Double value, int scale, String post) {
        if(value == null) {
            return "";
        }
        double scaleDivisor = 1;
        for(int i =0; i < scale; i++) {
            scaleDivisor *= 10;
        }
        double scaled = Math.round(value*scaleDivisor)/scaleDivisor;
        String scaledString = Double.toString(scaled);
        while(scaledString.split("\\.")[1].length() < scale) {
            scaledString = scaledString + "0";
        }
        return post == null ? scaledString : (scaledString + " " + post);
    }
}
