package com.lightframework.plugin.structure;

import cn.hutool.core.io.FileUtil;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;
import java.nio.file.Paths;


/*** 
 * @author yg
 * @date 2023/8/31 21:19
 * @version 1.0
 */
@Mojo(name = "init-starter-simple-app",aggregator = true,defaultPhase = LifecyclePhase.NONE)
public class InitStarterSimpleAppPlugin extends InitStarterPlugin {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        addDependency("light-starter-simple-app");
        Model model = getModel(project.getFile());
        String group = model.getGroupId() != null && model.getGroupId().trim().length() > 0?model.getGroupId().trim():model.getParent().getGroupId().trim();
        String packagePath = group.replaceAll("\\.", "/").trim();
        File applicationClass = Paths.get(basedir,"src/main/java",packagePath,"Application.java").toFile();
        if(!applicationClass.exists()){
            if (!applicationClass.getParentFile().exists()){
                applicationClass.getParentFile().mkdirs();
            }
            String content = "package " + group + ";\n" +
                    "\n" +
                    "import com.lightframework.starter.simple.app.SimpleApplication;\n" +
                    "import org.springframework.boot.autoconfigure.SpringBootApplication;\n" +
                    "import org.springframework.context.annotation.ComponentScan;\n" +
                    "\n" +
                    "@SpringBootApplication\n" +
                    "@ComponentScan(basePackages = {\""+ group + "\",\"com.lightframework\"})\n" +
                    "public class Application {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        SimpleApplication.run(Application.class,args);\n" +
                    "    }\n" +
                    "}\n";
            FileUtil.writeUtf8String(content,applicationClass);
        }

        replaceMainClassValue(group);

    }
}
