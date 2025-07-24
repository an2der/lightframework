package com.lightframework.plugin.structure;

import cn.hutool.core.io.FileUtil;
import com.lightframework.common.LightException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.List;


/*** 
 * @author yg
 * @date 2023/8/31 21:19
 * @version 1.0
 */
//@Mojo(name = "init-starter",aggregator = true,defaultPhase = LifecyclePhase.NONE)
public class InitStarterPlugin extends AbstractMojo {

    @Parameter(defaultValue = "${basedir}",readonly = true)
    protected String basedir;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;



    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            JarUtil.extract("structure/init-starter",new File(basedir,"src/main").getAbsolutePath(),false);
        } catch (IOException e) {
            throw new LightException(e.getMessage());
        }
    }

    protected void  addDependency(String lightArtifactId){
        File pomFile = getRootPomFile();
        if(!pomFile.exists()){
            throw new LightException(pomFile.getAbsolutePath() + " file does not exist!");
        }
        Model model = getModel(pomFile);
        boolean existLightCore = false;
        String lightGroupId = "com.lightframework";
        for (Dependency dependency : model.getDependencies()) {
            if(lightArtifactId.equals(dependency.getArtifactId()) && lightGroupId.equals(dependency.getGroupId())){
                existLightCore = true;
            }
        }
        if(!existLightCore) {
            MavenXpp3Writer writer = new MavenXpp3Writer();
            Dependency dependency = new Dependency();
            dependency.setGroupId(lightGroupId);
            dependency.setArtifactId(lightArtifactId);
            model.addDependency(dependency);
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(pomFile);
                writer.write(outputStream, model);
            } catch (Exception e) {
                throw new LightException("Error Write to pom.xml file " + pomFile.getAbsolutePath(),e);
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    protected Model getModel(File pomFile){
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(pomFile);
            return new MavenXpp3Reader().read(fileInputStream);
        } catch (Exception e) {
            throw new LightException("Parse pom.xml error in " + pomFile.getAbsolutePath(),e);
        }finally {
            try {
                if(fileInputStream != null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
            }
        }
    }

    protected File getRootPomFile(){
        // 获取根项目文件路径
        MavenProject rootProject = project;
        while (rootProject.getParentFile() != null) {
            rootProject = rootProject.getParent();
        }
        return rootProject.getFile();
    }

    protected void replaceMainClassValue(String group){
        File filtersFile = new File(basedir,"src/main/filters/build.properties");

        List<String> strings = FileUtil.readUtf8Lines(filtersFile);
        for (int i = 0; i < strings.size(); i++) {
            String line = strings.get(i);
            if (line.startsWith("package.mainClass=")) {
                strings.set(i, "package.mainClass="+group+".Application");
            }
        }
        FileUtil.writeUtf8Lines(strings, filtersFile);
    }
}
