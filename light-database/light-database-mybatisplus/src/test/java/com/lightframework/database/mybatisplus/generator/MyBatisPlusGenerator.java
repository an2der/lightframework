package com.lightframework.database.mybatisplus.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import org.junit.Test;

import java.util.Collections;

public class MyBatisPlusGenerator {

    @Test
    public void generatorAll(){
        generator("imm_common","common");
        generator("imm_history","history");
        generator("imm_state","state");
    }

    private void generator(String databaseName,String name){
        FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/"+databaseName+"?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai", "root", "123456")  //数据库连接配置，必不可少的一个配置
                .globalConfig(builder -> {  //全局配置
                    builder.author("yg") // 设置作者
//                            .enableSwagger() // 开启 swagger 模式
                            .fileOverride() // 覆盖已生成文件
                            .disableOpenDir()
                            .outputDir("src\\main\\java"); // 指定输出目录
                })
                .packageConfig(builder -> {   //包配置
                    builder.parent("cn.com.cx.oms.imm") // 设置父包名
                            .moduleName(null) // 设置父包模块名,可以设置为空，默认在包名之下,设置成null，防止生成双斜杠问题
                            .entity("model."+name)
                            .mapper("dao."+name)
                            .service("service."+name)
                            .serviceImpl("service."+name+".impl")
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml, "src\\main\\resources\\mapper\\"+name)); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addTablePrefix("t_", "sys"); // 设置过滤表前缀,忽略一些表头，如“sys_user”,填写了sys，就会忽略sys，生成user
                })
//                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }

    @Test
    public void test(){
        System.out.println((Long)null);
    }
}
