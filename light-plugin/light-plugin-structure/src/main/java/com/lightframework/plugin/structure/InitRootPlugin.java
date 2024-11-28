package com.lightframework.plugin.structure;

import com.lightframework.common.LightException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.IOException;

/*** 
 * @author yg
 * @date 2023/8/31 21:28
 * @version 1.0
 */
@Mojo(name = "init-root",aggregator = true,defaultPhase = LifecyclePhase.NONE)
public class InitRootPlugin extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${basedir}",readonly = true)
    private String basedir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if(project.getParentFile() != null){
                throw new MojoExecutionException("Project is not root project.");
            }
            JarUtil.extract("structure/init-root",basedir,false);
        } catch (IOException e) {
            throw new LightException(e.getMessage());
        }
    }

}
