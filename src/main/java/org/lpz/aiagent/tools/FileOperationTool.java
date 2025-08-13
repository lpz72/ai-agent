package org.lpz.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import org.lpz.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具类
 */
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileName) {
        String filepath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filepath);
        } catch (IORuntimeException e) {
            return "Error reading file:" + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(@ToolParam(description = "Name of the file to write") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {
        String filepath = FILE_DIR + "/" + fileName;
        try {
            //创建目录，若已存在，则不创建
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content,filepath);
            return "File written successfully to: " + filepath;
        } catch (IORuntimeException e) {
            return "Error writing to file: " + e.getMessage();
        }

    }

}
