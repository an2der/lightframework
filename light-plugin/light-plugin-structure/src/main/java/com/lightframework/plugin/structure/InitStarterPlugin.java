package com.lightframework.plugin.structure;

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
@Mojo(name = "init-starter",aggregator = true,defaultPhase = LifecyclePhase.NONE)
public class InitStarterPlugin extends AbstractMojo {

    @Parameter(defaultValue = "${basedir}",readonly = true)
    private String basedir;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;



    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            JarUtil.extract("structure/init-starter",new File(basedir,"src/main").getAbsolutePath(),false);
            addDependency();
        } catch (IOException e) {
            this.getLog().error(e.getMessage());
        }
    }

    private void addDependency(){
        File pomFile = new File(basedir,"pom.xml");
        if(!pomFile.exists()){
            throw new RuntimeException(pomFile.getAbsolutePath() + " file does not exist!");
        }
        FileInputStream fileInputStream = null;
        Model model;
        try {
            fileInputStream = new FileInputStream(pomFile);
            model = new MavenXpp3Reader().read(fileInputStream);
        } catch (Exception e) {
            throw new RuntimeException("Parse pom.xml error in " + pomFile.getAbsolutePath(),e);
        }finally {
            try {
                if(fileInputStream != null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
            }
        }
        boolean existLightCore = false;
        String lightGroupId = "com.lightframework";
        String lightArtifactId = "light-core";
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
                throw new RuntimeException("Error Write to pom.xml file " + pomFile.getAbsolutePath(),e);
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
}
