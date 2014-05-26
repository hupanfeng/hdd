package com.hdd.common.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 随机校验码生成工具
 * 
 * @author david
 * @version 1.0
 * 
 */
public class RandomCodeUtil {
    /**
     * 
     * 生成随机码存入session中，并将随机码以图像的方式输出至客户端
     * 
     * @param request
     * @param response
     * @throws IOException
     * @author david
     * 
     *         完成日期: 2013-7-17
     */
    public static void genRandom(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 首先设置页面不缓存
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 定义图片的宽度和高度
        int width = 100, height = 36;
        // 创建一个图像对象
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 得到图像的环境对象
        Graphics g = image.createGraphics();

        Random random = new Random();
        // 用随机颜色填充图像背景
        // g.setColor(getRandColor(180, 250));
        g.fillRect(0, 0, width, height);
        for (int i = 0; i < 5; i++) {
            g.setColor(getRandColor(50, 100));
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g.drawOval(x, y, 4, 4);
        }

        char[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        // 随机字符串
        String sRand = "";

        int fontHeight = height - 2;
        int fontX = width / (4);
        int fontY = height - 4;
        // 设置字体，下面准备画随机数
        g.setFont(new Font("", Font.PLAIN, fontHeight));
        for (int i = 0; i < 4; i++) {
            // 生成四个随机字符
            String rand = String.valueOf(codeSequence[random.nextInt(34)]);
            sRand += rand;
            // 生成随机颜色
            g.setColor(new Color(20 + random.nextInt(80), 20 + random.nextInt(100), 20 + random.nextInt(90)));
            // 将随机字符画在图像上
            g.drawString(rand, fontX * i, fontY);

            // // 生成干扰线
            // for (int k = 0; k < 12; k++) {
            // int x = random.nextInt(width);
            // int y = random.nextInt(height);
            // int xl = random.nextInt(9);
            // int yl = random.nextInt(9);
            // g.drawLine(x, y, x + xl, y + yl);
            // }
        }
        HttpSession session = request.getSession();
        // 将生成的随机数字字符串写入Session
        session.setAttribute("randcode", sRand);
        // 使图像生效
        g.dispose();
        // 输出图像到页面
        ImageIO.write(image, "JPEG", response.getOutputStream());
    }

    /**
     * 产生一个随机的颜色
     * 
     * @param fc
     *            颜色分量最小值
     * @param bc
     *            颜色分量最大值
     * @return
     */
    private static Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
