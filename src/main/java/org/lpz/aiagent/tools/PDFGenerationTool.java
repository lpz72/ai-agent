package org.lpz.aiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.lpz.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * PDF生成工具
 */
@Component
@Slf4j
public class PDFGenerationTool {

//    @Tool(description = "Generate a PDF file with given content")
//    public String generatePDF(
//            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
//            @ToolParam(description = "Content to be included in the PDF") String content) {
//        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
//        String filePath = fileDir + "/" + fileName;
//        try {
//            // 创建目录
//            FileUtil.mkdir(fileDir);
//            // 创建 PdfWriter 和 PdfDocument 对象
//            try (PdfWriter writer = new PdfWriter(filePath);
//                 PdfDocument pdf = new PdfDocument(writer);
//                 Document document = new Document(pdf)) {
//                // 自定义字体（需要人工下载字体文件到特定目录）
////                String fontPath = Paths.get("src/main/resources/static/fonts/simsun.ttf")
////                        .toAbsolutePath().toString();
////                PdfFont font = PdfFontFactory.createFont(fontPath,
////                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
//                // 使用内置中文字体
//                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
//                document.setFont(font);
//                // 创建段落
//                Paragraph paragraph = new Paragraph(content);
//                // 添加段落并关闭文档
//                document.add(paragraph);
//            } catch (java.io.IOException e) {
//                throw new RuntimeException(e);
//            }
//            // 设置HTTP响应头
////            HttpHeaders headers = new HttpHeaders();
////            headers.setContentType(MediaType.APPLICATION_PDF);
////            headers.setContentDispositionFormData("attachment", fileName);
////            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
////
////            return ResponseEntity.ok()
////                    .headers(headers)
////                    .body(outputStream.toByteArray());
//            return "PDF generated successfully to: " + filePath;
//        } catch (IOException e) {
//            return "Error generating PDF: " + e.getMessage();
//        }
//    }
//@Value("${server.port:8123}")
//private String serverPort;
//
//    @Value("${server.servlet.context-path:/api}")
//    private String contextPath;

    /**
     * 生成PDF文件并返回下载链接
     * @param fileName PDF文件名（不需要包含.pdf扩展名）
     * @param content PDF内容
     * @return 包含下载链接的结果信息
     */
    @Tool(description = "Generate a PDF file with given content and provide download link")
    public String generatePDFWithDownloadLink(
            @ToolParam(description = "Name of the PDF file (without .pdf extension)") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {

        // 确保文件名以.pdf结尾
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            fileName = fileName + ".pdf";
        }

        // 添加时间戳避免文件名冲突
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String finalFileName = timestamp + "_" + fileName;

        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + finalFileName;

        try {
            // 创建目录
            FileUtil.mkdir(fileDir);

            // 生成PDF
            generatePDFFile(filePath, content);

            // 构建下载链接
//            String downloadUrl = String.format("http://localhost:%s%s/download/pdf/%s",
//                    serverPort, contextPath, finalFileName);
        
            //将这行
//            String downloadUrl = String.format("http://localhost:%s%s/download/pdf/%s",
//                    serverPort, contextPath, finalFileName);
//
// 改为相对路径
//            String downloadUrl = String.format("/api/download/pdf/%s", finalFileName);
//
// 或者动态获取当前请求的域名
//            String downloadUrl = String.format("%s/download/pdf/%s", contextPath, finalFileName);
//            String downloadUrl = String.format("http://localhost:8123/api/files/download/pdf/%s",
//                    finalFileName);
            String downloadUrl = String.format("http://agent-backend.project-learn.site/api/files/download/pdf/%s", finalFileName);
            log.info("PDF生成成功: {}, 下载链接: {}", filePath, downloadUrl);

//            return String.format("PDF文件生成成功！\n" +
//                            "文件名: %s\n" +
//                            "下载链接: %s\n" +
//                            "提示: 点击下载链接即可下载PDF文件到本地",
//                    finalFileName, downloadUrl) + "请将下载链接地址用文字替代，而不是直接暴露下载接口地址";

            return String.format("PDF文件生成成功！\n" +
                            "文件名: %s\n" +
                            "📄 [点击下载](%s)\n" +
                            "提示: 点击上方链接即可下载PDF文件到本地",
                    finalFileName, downloadUrl);
        } catch (Exception e) {
            log.error("生成PDF失败: {}", e.getMessage(), e);
            return "生成PDF失败: " + e.getMessage();
        }
    }

    /**
     * 核心PDF生成方法
     * @param filePath 文件路径
     * @param content 内容
     * @throws java.io.IOException IO异常
     */
    private void generatePDFFile(String filePath, String content) throws IOException {
        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // 使用内置中文字体
            String fontPath = Paths.get("src/main/resources/static/fonts/SimSun.ttf")
                    .toAbsolutePath().toString();
            PdfFont font = PdfFontFactory.createFont(fontPath,
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            document.setFont(font);

            // 创建段落
            Paragraph paragraph = new Paragraph(content);

            // 添加段落并关闭文档
            document.add(paragraph);
        }
    }

}
