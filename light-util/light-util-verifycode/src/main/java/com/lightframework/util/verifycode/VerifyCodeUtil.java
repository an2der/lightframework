package com.lightframework.util.verifycode;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

public class VerifyCodeUtil {

    /**
     * 创建指定数量的随机字符串
     * @param numberFlag 是否是数字
     * @param length
     * @return
     */
    public static String createRandom(boolean numberFlag, int length) {
        String retStr = "";
        String strTable = numberFlag ? "1234567890" : "1234567890abcdefghijkmnpqrstuvwxyz";
        int len = strTable.length();
        boolean bDone = true;
        do {
            retStr = "";
            int count = 0;
            for (int i = 0; i < length; i++) {
                double dblR = Math.random() * len;
                int intR = (int) Math.floor(dblR);
                char c = strTable.charAt(intR);
                if (('0' <= c) && (c <= '9')) {
                    count++;
                }
                retStr += strTable.charAt(intR);
            }
            if (count >= 2) {
                bDone = false;
            }
        } while (bDone);
        return retStr;
    }

    private static Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if(fc>255) fc=255;
        if(bc>255) bc=255;
        int r=fc+random.nextInt(bc-fc);
        int g=fc+random.nextInt(bc-fc);
        int b=fc+random.nextInt(bc-fc);
        return new Color(r,g,b);
    }

    /**
     * 创建验证码
     * @param verifyCode
     * @param response
     * @throws IOException
     */
    public static void createVCodeImage(HttpServletResponse response,String verifyCode) throws IOException {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        int width = 120, height = 38;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        String[] fontTypes = {"\u5b8b\u4f53", "\u65b0\u5b8b\u4f53",
                "\u9ed1\u4f53", "\u6977\u4f53", "\u96b6\u4e66"};
        int fontTypesLength = fontTypes.length;
        Graphics g = image.getGraphics();
        Random random = new Random();
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        char[] rand = verifyCode.toCharArray();
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(20 + random.nextInt(100), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.setFont(new Font(fontTypes[random.nextInt(fontTypesLength)], Font.BOLD, 28 + random.nextInt(6)));
            g.drawString(rand[i] + "", 25 * i + 12, 25);
        }
        g.dispose();
        ImageIO.write(image, "JPEG", response.getOutputStream());
    }
}
