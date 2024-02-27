package com.lightframework.plugin.version;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.wagon.Wagon;
import org.codehaus.mojo.versions.api.recording.ChangeRecorder;
import org.codehaus.plexus.components.interactivity.Prompter;

import javax.inject.Inject;
import java.util.Map;

/*** 升级主版本号
 * @author yg
 * @date 2024/2/4 16:03
 * @version 1.0
 */
@Mojo(name = "next-major-version", aggregator = true, threadSafe = true)
public class NextMajorVersionPlugin extends BaseNextVersionPlugin {

    @Inject
    public NextMajorVersionPlugin(RepositorySystem repositorySystem, org.eclipse.aether.RepositorySystem aetherRepositorySystem, ProjectBuilder projectBuilder, Map<String, Wagon> wagonMap, Map<String, ChangeRecorder> changeRecorders, Prompter prompter) {
        super(repositorySystem, aetherRepositorySystem, projectBuilder, wagonMap, changeRecorders, prompter,BaseNextVersionPlugin.MAJOR_LEVEL);
    }


}