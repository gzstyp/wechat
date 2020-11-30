package com.fwtai.controller;

import com.fwtai.tool.ToolWechat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 微信公众号api
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020-11-29 16:32
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
@RestController
@RequestMapping("/api")
public class ApiController{

    //结合 https://mp.weixin.qq.com/advanced/advanced?action=dev&t=advanced/dev&token=1629591418&lang=zh_CN 基本配置接入
    // 微信公众帐号测试号申请系统 http://mp.weixin.qq.com/debug/cgi-bin/sandboxinfo?action=showinfo&t=sandbox/index
    @RequestMapping("/switchInto")
    public void switchInto(final HttpServletRequest request,final HttpServletResponse response){
        final String signature = request.getParameter("signature");
        final String timestamp = request.getParameter("timestamp");
        final String nonce = request.getParameter("nonce");
        final String echostr = request.getParameter("echostr");
        //校验证签名
        if(ToolWechat.check(timestamp,nonce,signature)){
            System.out.println("接入成功");
            writer(response,echostr);
        }else {
            System.out.println("接入失败");
        }
    }

    @RequestMapping("/getTicket")
    public void getTicket(final HttpServletResponse response){
        final String ticket = ToolWechat.getQrCodeTicket();
        writer(response,ticket);
    }

    private void writer(final HttpServletResponse response,final String msg){
        response.setContentType("text/html;charset=utf-8");
        response.setHeader("Cache-Control","no-cache");
        final PrintWriter out;
        try{
            out = response.getWriter();
            out.print(msg);
            out.flush();
            out.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}