package generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import org.junit.Test;

import java.util.Collections;

public class MyBatisPlusGenerator {

    @Test
    public void generatorAll(){
//        generator("imm_common","common");
//        generator("imm_history","history");
//        generator("imm_state","state");
        generator("sim_edu","upgrade");
    }

    private void generator(String databaseName,String name){
        FastAutoGenerator.create("jdbc:mysql://192.168.33.75:3306/"+databaseName+"?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai", "root", "blessme")  //数据库连接配置，必不可少的一个配置
                .globalConfig(builder -> {  //全局配置
                    builder.author("yg") // 设置作者
//                            .enableSwagger() // 开启 swagger 模式
                            .fileOverride() // 覆盖已生成文件
//                            .disableOpenDir()
                            .dateType(DateType.ONLY_DATE)
                            .outputDir("target\\main\\java"); // 指定输出目录
                })
                .packageConfig(builder -> {   //包配置
                    builder.parent("cn.com.sim.edu.server") // 设置父包名
                            .moduleName(null) // 设置父包模块名,可以设置为空，默认在包名之下,设置成null，防止生成双斜杠问题
                            .entity(name+".model")
                            .mapper(name+".dao")
                            .service(name+".service")
                            .serviceImpl(name+".service.impl")
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml, "target\\main\\resources\\mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addTablePrefix("t_")
                            .addInclude("t_program","t_program_device","t_program_profile","t_program_version")
                            .entityBuilder()
                            .enableLombok(); // 设置过滤表前缀,忽略一些表头，如“sys_user”,填写了sys，就会忽略sys，生成user
                }).templateConfig(builder -> {
                    builder.controller(null); //不生成controller
                })
//                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }

}
