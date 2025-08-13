package org.lpz.aiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {

        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "文件操作工具测试.txt";
        String result = fileOperationTool.readFile(fileName);
        Assertions.assertNotNull(result);
    }

    @Test
    void writeFile() {

        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "文件操作工具测试.txt";
        String content = "文件操作工具测试：写文件";
        String result = fileOperationTool.writeFile(fileName,content);
        Assertions.assertNotNull(result);

    }
}