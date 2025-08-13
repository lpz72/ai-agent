package org.lpz.aiagent.rag;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LoveAppDocumentLoader {

    //注入spring内置的资源解析类
    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载所有的md资源
     * @return
     */
    public List<Document> loadMarkdowns() {
        List<Document> documents = new ArrayList<>();
        //加载多篇md文档
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:/document/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                // 提取文档倒数第3个字和第2个字作为标签
                String status = fileName.substring(fileName.length() - 6, fileName.length() - 4);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", fileName)
                        .withAdditionalMetadata("status", status)
                        .build();
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                documents.addAll(markdownDocumentReader.get());

            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败",e);
        }
       return documents;
    }

}
