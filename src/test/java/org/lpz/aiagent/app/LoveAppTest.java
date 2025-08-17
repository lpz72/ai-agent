package org.lpz.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void TestChat() {

        String chatId = UUID.randomUUID().toString();

        //第一轮
        String message = "你好，我是程序员李鹏振";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        //第二轮
        message = "我想让我的另一半（java）更爱我";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        //第三轮
        message = "我的另一半叫什么来着，上面我刚给你讲过";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();

        String message = "你好，我是程序员李鹏振";
        LoveApp.LoveReport answer = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();

//        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String message = "我太胖了，想要在短时间内减肥，怎么办？";
        String answer = loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
//        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");
//
//        // 测试网页抓取：恋爱案例分析
//        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");
//
//        // 测试资源下载：图片下载
//        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");
//
//        // 测试终端操作：执行代码
//        testMessage("执行 Python3 脚本来生成数据分析报告");

        // 测试文件操作：保存用户档案
        testMessage("保存我的恋爱档案为文件，要求为txt文件");

        // 测试 PDF 生成
        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();

        //测试地图MCP
//        String message = "我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点";
//        String answer = loveApp.doChatWithMcp(message, chatId);
        //测试图片搜索MCP
        String message = "我想找一张适合做手机壁纸的星空情侣图片";
        String answer = loveApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);

    }
}