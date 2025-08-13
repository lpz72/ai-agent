package org.lpz.aiagent.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * 网页抓取工具
 */
public class WebScrapingTool {

    @Tool(description = "Scrape the content of a web page")
    public String scrapeWeb(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document elements = Jsoup.connect(url).get();
            return elements.html();
        } catch (Exception e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
