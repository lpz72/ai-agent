package org.lpz.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapeWeb() {

        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://codefather.cn";
        String result = webScrapingTool.scrapeWeb(url);
        Assertions.assertNotNull(result);

    }
}