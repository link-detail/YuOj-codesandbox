package com.liu.yuojcodesandbox.codesandbox.test;

import cn.hutool.core.util.StrUtil;

/**
 * @author 刘渠好
 * @since 2024-10-16 21:12
 */

public class Demo1 {
    public static void main(String[] args) {
        String input="1 2";
        String[] s = input.split(" ");
        String join = StrUtil.join("你好", s);
        System.out.println (join);

    }
}