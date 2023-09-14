package com.lightframework.plugin.pack;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.jar.JarMojo;

import java.io.File;

@Mojo( name = "jar", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, requiresProject = true, threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME )
public class JarPlugin extends JarMojo {

    @Parameter( defaultValue = "${project.build.directory}", required = true )
    private File targetDir;

    @Parameter( defaultValue = "${project.build.finalName}", readonly = true )
    private String jarFinalName;

    @Parameter(defaultValue = "${mainClass}", readonly = true )
    private String mainClass;

    @Override
    public void execute() throws MojoExecutionException {
        if(mainClass != null && mainClass.length() > 0){
            super.execute();
        }
    }
}
