package org.lpz.aiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceDownloadToolTest {

    @Test
    void downloadResource() {

        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "https://cdn.nlark.com/yuque/0/2025/png/40401069/1754310668372-4869b791-92f7-41f6-8628-ea29c7395e07.png?x-oss-process=image%2Fformat%2Cwebp";
        String fileName = "logo.png";
        String result = tool.downloadResource(url, fileName);
        assertNotNull(result);
    }
}