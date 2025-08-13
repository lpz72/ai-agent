package org.lpz.aiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    VectorStore pgVectorVectorStore;

    @Test
    void pgVectorVectorStore() {
        List<Document> documents = List.of(
                new Document("我是lpz，喜欢编程，正在学习大模型编程", Map.of("meta1", "meta1")),
                new Document("编程是件有趣的事情，深度学习同样有趣"),
                new Document("lpz是个准研究生", Map.of("meta2", "meta2")));

        // 添加文档
        pgVectorVectorStore.add(documents);

        // 相似度搜索
        List<Document> results = this.pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("介绍下lpz").topK(5).build());
        Assertions.assertNotNull(results);
    }
}