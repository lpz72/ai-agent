package org.lpz.aiagent.controller;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.lpz.aiagent.constant.FileConstant;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文件下载控制器
 * 提供PDF等文件的下载服务
 */
@RestController
@RequestMapping("/files/download")
@Slf4j
public class FileDownloadController {

    /**
     * 下载PDF文件
     * @param fileName PDF文件名
     * @return PDF文件响应
     */
    @GetMapping("/pdf/{fileName}")
    public ResponseEntity<Resource> downloadPDF(@PathVariable String fileName) {
        try {
            // 构建文件路径
            String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
            String filePath = fileDir + "/" + fileName;
            
            // 检查文件是否存在
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("PDF文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 创建文件资源
            Resource resource = new FileSystemResource(file);
            
            // 编码文件名以支持中文
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", encodedFileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            headers.setContentLength(file.length());
            
            log.info("开始下载PDF文件: {}", fileName);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (UnsupportedEncodingException e) {
            log.error("文件名编码失败: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("下载PDF文件失败: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 下载普通文件
     * @param fileName 文件名
     * @return 文件响应
     */
    @GetMapping("/file/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            // 构建文件路径
            String fileDir = FileConstant.FILE_SAVE_DIR + "/file";
            String filePath = fileDir + "/" + fileName;
            
            // 检查文件是否存在
            File file = new File(filePath);
            if (!file.exists()) {
                log.error("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // 创建文件资源
            Resource resource = new FileSystemResource(file);
            
            // 编码文件名以支持中文
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            // 根据文件扩展名设置Content-Type
            MediaType mediaType = getMediaTypeForFileName(fileName);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDispositionFormData("attachment", encodedFileName);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            headers.setContentLength(file.length());
            
            log.info("开始下载文件: {}", fileName);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (UnsupportedEncodingException e) {
            log.error("文件名编码失败: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("下载文件失败: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 根据文件名获取MediaType
     * @param fileName 文件名
     * @return MediaType
     */
    private MediaType getMediaTypeForFileName(String fileName) {
        String extension = FileUtil.extName(fileName).toLowerCase();
        
        return switch (extension) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "txt" -> MediaType.TEXT_PLAIN;
            case "json" -> MediaType.APPLICATION_JSON;
            case "xml" -> MediaType.APPLICATION_XML;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "doc", "docx" -> MediaType.parseMediaType("application/msword");
            case "xls", "xlsx" -> MediaType.parseMediaType("application/vnd.ms-excel");
            case "zip" -> MediaType.parseMediaType("application/zip");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
