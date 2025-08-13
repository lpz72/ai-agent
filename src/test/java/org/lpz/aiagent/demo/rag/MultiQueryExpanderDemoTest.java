package org.lpz.aiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MultiQueryExpanderDemoTest {

    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;

    @Test
    void expand() {

        List<Query> expand = multiQueryExpanderDemo.expand("今天天气怎么样啊啊啊啊，我想出去玩啊啊啊啊");
        Assertions.assertNotNull(expand);
    }
}