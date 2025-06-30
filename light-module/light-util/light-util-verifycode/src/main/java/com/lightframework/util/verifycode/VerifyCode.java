package com.lightframework.util.verifycode;

/*** 验证码
 * @author yg
 * @date 2024/5/23 16:21
 * @version 1.0
 */
public class VerifyCode {
    private String code;//验证码

    private long generateTime;//生成时间

    private int expireTimeSecond;//失效时间：秒

    public VerifyCode(String code, int expireTimeSecond) {
        this.code = code;
        this.generateTime = System.currentTimeMillis();
        this.expireTimeSecond = expireTimeSecond;
    }

    public String getCode() {
        return code;
    }

    public long getGenerateTime() {
        return generateTime;
    }

    public int getExpireTimeSecond() {
        return expireTimeSecond;
    }

    public boolean isExpired(){
        return expireTimeSecond == 0? false:System.currentTimeMillis() > generateTime + (expireTimeSecond * 1000);
    }
}
