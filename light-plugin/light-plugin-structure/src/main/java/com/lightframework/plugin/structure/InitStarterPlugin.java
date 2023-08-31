package com.lightframework.plugin.structure;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/*** 
 * @author yg
 * @date 2023/8/31 21:19
 * @version 1.0
 */
@Mojo(name = "init-starter",defaultPhase = LifecyclePhase.NONE)
public class InitStarterPlugin extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.getLog().info("init root plugin!");
    }

}
