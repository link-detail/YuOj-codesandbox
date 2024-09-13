package com.liu.yuojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 刘渠好
 * @since 2024/8/11 下午9:46
 * 代码沙箱涉及到的编译/运行命令
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodeSandboxCmd {

    private String compileCmd;  //编译命令

    private String  runCmd;  //运行命令
}
