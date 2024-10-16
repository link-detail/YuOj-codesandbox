package com.liu.yuojcodesandbox.codesandbox.test;

/**
 * @author 刘渠好
 * @since 2024-10-16 21:12
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Demo1 {
    public static void main(String[] args) {
        String data = "Hello World!"+"\n";
        try {
            // 创建一个ByteArrayOutputStream
            OutputStream outputStream = new ByteArrayOutputStream();

            // 使用OutputStreamWriter将字符串写入OutputStream
            // 注意指定字符集，这里使用UTF-8
            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                writer.write(data);
                writer.flush(); // 确保所有数据都被写入到输出流中
            }

            // 将输出流中的数据转换回字符串
            String result = outputStream.toString();
            // 打印结果
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}