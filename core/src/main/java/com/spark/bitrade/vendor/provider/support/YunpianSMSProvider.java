package com.spark.bitrade.vendor.provider.support;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.spark.bitrade.util.HttpSend;
import com.spark.bitrade.util.MessageResult;
import com.spark.bitrade.vendor.provider.SMSProvider;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tansitao
 * @time 2018-04-05
 * 云片短信验证码发送类
 */
@Slf4j
public class YunpianSMSProvider implements SMSProvider {

    private String gateway;
    private String apikey;
    private String sign;
    private String tplId;

    public YunpianSMSProvider(String gateway, String apikey,String sign,String tplId) {
        this.gateway = gateway;
        this.apikey = apikey;
        this.sign=sign;
        this.tplId=tplId;
    }

    private static Pattern RESPONSE_PATTERN = Pattern.compile("<response><error>(-?\\d+)</error><message>(.*[\\\\u4e00-\\\\u9fa5]*)</message></response>");


    public static String getName() {
        return "yunpian";
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, String content) throws UnirestException {
        Map<String, String> params = new HashMap<>();
        params.put("apikey", apikey);
        params.put("tpl_id",tplId);
        params.put("text",content);
        params.put("tpl_value", URLEncoder.encode("#code#") + "=" + URLEncoder.encode(content));
        params.put("mobile", mobile);
        log.info("yunpianParameters===={}", params.toString());
        String resultXml= HttpSend.yunpianPost(gateway, params);
        log.info("result = {}", resultXml);
        return parseXml(resultXml);
    }

    @Override
    public MessageResult sendSingleMessage(String mobile, Long advertiseId, String userName) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("apikey", apikey);
        params.put("tpl_id",tplId);
        params.put("tpl_value", URLEncoder.encode("#advertiseId#") + "=" + URLEncoder.encode(advertiseId.toString())
                + "&" + URLEncoder.encode("#userName#") + "=" + URLEncoder.encode(userName));
        params.put("mobile", mobile);
        log.info("yunpianParameters===={}", params.toString());
        String resultXml= HttpSend.yunpianPost(gateway, params);
        log.info("result = {}", resultXml);
        return parseXml(resultXml);
    }

    @Override
    public MessageResult sendInternationalMessage(String mobile, String content) throws IOException, DocumentException {
        content = String.format("【%s】Your verification code is %s",sign, content);
        Map<String, String> params = new HashMap<>();
        params.put("apikey", apikey);
        params.put("text", content);
        params.put("mobile", mobile);
        String resultXml= HttpSend.yunpianPost(gateway, params);
        log.info("result = {}", resultXml);
        return parseXml(resultXml);
    }

    /**
     * 获取验证码信息格式
     *
     * @param code
     * @return
     */
    @Override
    public String formatVerifyCode(String code) {
        return String.format("【%s】您的验证码为：%s，请按页面提示填写，切勿泄露于他人。",sign, code);
    }

    @Override
    public MessageResult sendVerifyMessage(String mobile, String verifyCode) throws Exception {
        //String content = formatVerifyCode(verifyCode);
        return sendSingleMessage(mobile, verifyCode);
    }

    public MessageResult sendLoginMessage(String ip,String phone) throws Exception {
        String content=sendLoginMessage(ip);
        return sendSingleMessage(content,phone);
    }

    private MessageResult parseXml(String xml) {
        JSONObject myJsonObject = JSONObject.fromObject(xml);
        int code = myJsonObject.getInt("code");
        MessageResult result = new MessageResult(500, "系统错误");
        if(code == 0)
        {
            result.setCode(code);
            result.setMessage(myJsonObject.getString("msg"));
        }
        return result;
    }
}
