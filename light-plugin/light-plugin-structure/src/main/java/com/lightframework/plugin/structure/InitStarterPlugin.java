package com.lightframework.plugin.structure;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;


/*** 
 * @author yg
 * @date 2023/8/31 21:19
 * @version 1.0
 */
@Mojo(name = "init-starter",aggregator = true,defaultPhase = LifecyclePhase.NONE)
public class InitStarterPlugin extends AbstractMojo {

    @Parameter(defaultValue = "${basedir}",readonly = true)
    private String basedir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            JarUtil.extract("structure/init-starter",new File(basedir,"src/main").getAbsolutePath(),false);
        } catch (IOException e) {
            this.getLog().error(e.getMessage());
        }
    }

}
