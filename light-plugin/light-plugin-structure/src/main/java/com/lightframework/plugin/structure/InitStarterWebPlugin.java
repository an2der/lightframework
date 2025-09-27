package com.lightframework.plugin.structure;

import cn.hutool.core.io.FileUtil;
import org.apache.maven.model.Dependency;
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
@Mojo(name = "init-starter-web",aggregator = true,defaultPhase = LifecyclePhase.NONE)
public class InitStarterWebPlugin extends InitStarterPlugin {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();
        Dependency dependency = new Dependency();
        dependency.setGroupId("com.lightframework");
        dependency.setArtifactId("light-web-core");
        addDependency(dependency,project.getFile());

        Dependency springbootDependency = new Dependency();
        springbootDependency.setGroupId("org.springframework.boot");
        springbootDependency.setArtifactId("spring-boot");
        springbootDependency.setOptional(true);
        addDependency(springbootDependency,getRootPomFile());

        Dependency springbootAutoconfigDependency = new Dependency();
        springbootAutoconfigDependency.setGroupId("org.springframework.boot");
        springbootAutoconfigDependency.setArtifactId("spring-boot-autoconfigure");
        springbootAutoconfigDependency.setOptional(true);
        addDependency(springbootAutoconfigDependency,getRootPomFile());
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
                    "import org.springframework.boot.SpringApplication;\n" +
                    "import org.springframework.boot.autoconfigure.SpringBootApplication;\n" +
                    "import org.springframework.context.annotation.ComponentScan;\n" +
                    "\n" +
                    "@SpringBootApplication\n" +
                    "@ComponentScan(basePackages = {\""+ group + "\",\"com.lightframework\"})\n" +
                    "public class Application {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        SpringApplication.run(Application.class, args);\n" +
                    "    }\n" +
                    "}\n";
            FileUtil.writeUtf8String(content,applicationClass);
        }
        replaceMainClassValue(group);
    }
}
