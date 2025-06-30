package com.lightframework.plugin.version;

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.apache.maven.wagon.Wagon;
import org.codehaus.mojo.versions.AbstractVersionsUpdaterMojo;
import org.codehaus.mojo.versions.api.PomHelper;
import org.codehaus.mojo.versions.api.recording.ChangeRecorder;
import org.codehaus.mojo.versions.change.DefaultDependencyVersionChange;
import org.codehaus.mojo.versions.change.VersionChanger;
import org.codehaus.mojo.versions.change.VersionChangerFactory;
import org.codehaus.mojo.versions.ordering.ReactorDepthComparator;
import org.codehaus.mojo.versions.rewriting.ModifiedPomXMLEventReader;
import org.codehaus.mojo.versions.utils.ContextualLog;
import org.codehaus.mojo.versions.utils.DelegatingContextualLog;
import org.codehaus.mojo.versions.utils.RegexUtils;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.codehaus.plexus.util.StringUtils;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import static org.codehaus.plexus.util.StringUtils.isEmpty;

public class BaseNextVersionPlugin extends AbstractVersionsUpdaterMojo {

    public static final int MAJOR_LEVEL = 1;
    public static final int MINOR_LEVEL = 2;
    public static final int PATCH_LEVEL = 3;

    private static final String SNAPSHOT = "-SNAPSHOT";

    /**
     * The new version number to set.
     *
     * @since 1.0-beta-1
     */
    @Parameter(property = "newVersion")
    private String newVersion;

    /**
     * If set to {@code true}, will process all modules regardless whether they
     * match {@code groupId:artifactId:oldVersion}.
     *
     * @since 2.5
     */
    @Parameter(property = "processAllModules", defaultValue = "false")
    private boolean processAllModules;

    /**
     * <p>The <b>non-interpolated</b> groupId of the dependency/module to be selected for update.</p>
     * <p>If not set, will be equal to the non-interpolated groupId of the project file.</p>
     * <p>If you wish to update modules of a aggregator regardless of the groupId, you
     * should set {@code -DgroupId='*'} to ignore the groupId of the current project.</p>
     * <p>Alternatively, you can use {@code -DprocessAllModules=true}</p>
     * <p><u>The goal does not interpolate the properties used in groupId used in the pom.xml file.</u></p>
     * <p><i>The single quotes are only necessary on POSIX-compatible shells (Linux, MacOS, etc.).</i></p>
     *
     * @since 1.2
     */
    @Parameter(property = "groupId")
    private String groupId;

    /**
     * <p>The <b>non-interpolated</b> artifactId of the dependency/module to be selected for update.</p>
     * <p>If not set, will be equal to the non-interpolated artifactId of the project file.</p>
     * <p>If you wish to update modules of a aggregator regardless of the artifactId, you
     * should set {@code -DartifactId='*'} to ignore the artifactId of the current project.</p>
     * <p>Alternatively, you can use {@code -DprocessAllModules=true}</p>
     * <p><u>The goal does not interpolate the properties used in artifactId used in the pom.xml file.</u></p>
     * <p><i>The single quotes are only necessary on POSIX-compatible shells (Linux, MacOS, etc.).</i></p>
     *
     * @since 1.2
     */
    @Parameter(property = "artifactId")
    private String artifactId;

    /**
     * <p>The <b>non-interpolated</b> version of the dependency/module to be selected for update.</p>
     * <p>If not set, will be equal to the non-interpolated version of the project file.</p>
     * <p>If you wish to update modules of a aggregator regardless of the version, you
     * should set {@code -Dversion='*'} to ignore the version of the current project.</p>
     * <p>Alternatively, you can use {@code -DprocessAllModules=true}</p>
     * <p><u>The goal does not interpolate the properties used in version used in the pom.xml file.</u></p>
     * <p><i>The single quotes are only necessary on POSIX-compatible shells (Linux, MacOS, etc.).</i></p>
     *
     * @since 1.2
     */
    @Parameter(property = "oldVersion")
    private String oldVersion;

    /**
     * Whether matching versions explicitly specified (as /project/version) in child modules should be updated.
     *
     * @since 1.3
     */
    @Parameter(property = "updateMatchingVersions", defaultValue = "true")
    private boolean updateMatchingVersions;

    /**
     * Whether to process the parent of the project.
     *
     * @since 1.3
     */
    @Parameter(property = "processParent", defaultValue = "true")
    private boolean processParent;

    /**
     * Whether to process the project version.
     *
     * @since 1.3
     */
    @Parameter(property = "processProject", defaultValue = "true")
    private boolean processProject;

    /**
     * Whether to process the dependencies section of the project.
     *
     * @since 1.3
     */
    @Parameter(property = "processDependencies", defaultValue = "true")
    private boolean processDependencies;

    /**
     * Whether to process the plugins section of the project.
     *
     * @since 1.3
     */
    @Parameter(property = "processPlugins", defaultValue = "true")
    private boolean processPlugins;

    /**
     * Component used to prompt for input
     */
    private Prompter prompter;

    /**
     * Whether to remove <code>-SNAPSHOT</code> from the existing version.
     *
     * @since 2.10
     */
    @Parameter(property = "removeSnapshot", defaultValue = "false")
    private boolean removeSnapshot;

    /**
     * Whether to add next version number and <code>-SNAPSHOT</code> to the existing version.
     * Unless specified by <code>nextSnapshotIndexToIncrement</code>, will increment
     * the last minor index of the snapshot version, e.g. the <code>z</code> in <code>x.y.z-SNAPSHOT</code>
     *
     * @since 2.10
     */
    @Parameter(property = "nextSnapshot", defaultValue = "false")
    protected boolean nextSnapshot;

    /**
     * <p>Specifies the version index to increment when using <code>nextSnapshot</code>.
     * Will increment the (1-based, counting from the left, or the most major component) index
     * of the snapshot version, e.g. for <code>-DnextSnapshotIndexToIncrement=1</code>
     * and the version being <code>1.2.3-SNAPSHOT</code>, the new version will become <code>2.2.3-SNAPSHOT.</code></p>
     * <p>Only valid with <code>nextSnapshot</code>.</p>
     *
     * @since 2.12
     */
    @Parameter(property = "nextSnapshotIndexToIncrement")
    protected Integer nextSnapshotIndexToIncrement;

    /**
     * Whether to start processing at the local aggregation root (which might be a parent module
     * of that module where Maven is executed in, and the version change may affect parent and sibling modules).
     * Setting to false makes sure only the module (and it's submodules) where Maven is executed for is affected.
     *
     * @since 2.9
     */
    @Parameter(property = "processFromLocalAggregationRoot", defaultValue = "true")
    private boolean processFromLocalAggregationRoot;

    /**
     * Whether to update the <code>project.build.outputTimestamp<code> property in the POM when setting version.
     *
     * @since 2.10
     * @deprecated please use {@link #updateBuildOutputTimestampPolicy} instead
     */
    @Parameter(property = "updateBuildOutputTimestamp", defaultValue = "true")
    private boolean updateBuildOutputTimestamp;

    /**
     * Whether to update the <code>project.build.outputTimestamp<code> property in the POM when setting version.
     * Valid values are: <code>onchange</code>, which will only change <code>outputTimestamp</code> for changed POMs,
     * <code>always</code>, <code>never</code>.
     *
     * @since 2.12
     */
    @Parameter(property = "updateBuildOutputTimestampPolicy", defaultValue = "onchange")
    private String updateBuildOutputTimestampPolicy;

    /**
     * The changes to module coordinates. Guarded by this.
     */
    private final transient List<DefaultDependencyVersionChange> sourceChanges = new ArrayList<>();

    /**
     * The (injected) instance of {@link ProjectBuilder}
     *
     * @since 2.14.0
     */
    protected final ProjectBuilder projectBuilder;

    private int level = PATCH_LEVEL;

    @Inject
    public BaseNextVersionPlugin(
            RepositorySystem repositorySystem,
            org.eclipse.aether.RepositorySystem aetherRepositorySystem,
            ProjectBuilder projectBuilder,
            Map<String, Wagon> wagonMap,
            Map<String, ChangeRecorder> changeRecorders,
            Prompter prompter,int level) {
        super(repositorySystem, aetherRepositorySystem, wagonMap, changeRecorders);
        this.projectBuilder = projectBuilder;
        this.prompter = prompter;
        this.level = level;
    }

    private synchronized void addChange(String groupId, String artifactId, String oldVersion, String newVersion) {
        if (!newVersion.equals(oldVersion)) {
            sourceChanges.add(new DefaultDependencyVersionChange(groupId, artifactId, oldVersion, newVersion));
        }
    }

    /**
     * Called when this mojo is executed.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException when things go wrong.
     * @throws org.apache.maven.plugin.MojoFailureException   when things go wrong.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (getProject().getOriginalModel().getVersion() == null) {
            throw new MojoExecutionException("Project version is inherited from parent.");
        }

        try {
            if(level == MAJOR_LEVEL) {
                newVersion = new NextVersionInfo(getProject().getOriginalModel().getVersion()).getNextMajorVersion().toString();
            }else if(level == MINOR_LEVEL){
                newVersion = new NextVersionInfo(getProject().getOriginalModel().getVersion()).getNextMinorVersion().toString();
            }else if(level == PATCH_LEVEL){
                newVersion = new NextVersionInfo(getProject().getOriginalModel().getVersion()).getNextPatchVersion().toString();
            }else {
                newVersion = getProject().getOriginalModel().getVersion();
            }
        } catch (VersionParseException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        if (!"onchange".equals(updateBuildOutputTimestampPolicy)
                && !"always".equals(updateBuildOutputTimestampPolicy)
                && !"never".equals(updateBuildOutputTimestampPolicy)) {
            throw new MojoExecutionException(
                    "updateBuildOutputTimestampPolicy should be one of: " + "\"onchange\", \"always\", \"never\".");
        }

        try {
            final MavenProject project = processFromLocalAggregationRoot
                    ? PomHelper.getLocalRoot(projectBuilder, session, getLog())
                    : getProject();

            getLog().info("Local aggregation root: " + project.getBasedir());
            Map<File, Model> reactorModels = PomHelper.getChildModels(project, getLog());
            final SortedMap<File, Model> reactor = new TreeMap<>(new ReactorDepthComparator(reactorModels));
            reactor.putAll(reactorModels);

            // set of files to update
            final Set<File> files = new LinkedHashSet<>();

            // groupId, artifactId, oldVersion are matched against every module of the project to see if the module
            // needs to be changed as well
            // setting them to the main project coordinates in case they are not set by the user,
            // so that the main project can be selected
            Model rootModel = reactorModels.get(session.getCurrentProject().getFile());
            if (groupId == null) {
                groupId = rootModel.getGroupId();
            }
            if (artifactId == null) {
                artifactId = rootModel.getArtifactId();
            }
            if (oldVersion == null) {
                oldVersion = rootModel.getVersion();
            }

            getLog().info(String.format(
                    "Processing change of %s:%s:%s -> %s", groupId, artifactId, oldVersion, newVersion));

            Pattern groupIdRegex = processAllModules || StringUtils.isBlank(groupId) || "*".equals(groupId)
                    ? null
                    : Pattern.compile(RegexUtils.convertWildcardsToRegex(groupId, true));
            Pattern artifactIdRegex = processAllModules || StringUtils.isBlank(artifactId) || "*".equals(artifactId)
                    ? null
                    : Pattern.compile(RegexUtils.convertWildcardsToRegex(artifactId, true));
            Pattern oldVersionIdRegex = processAllModules || StringUtils.isBlank(oldVersion) || "*".equals(oldVersion)
                    ? null
                    : Pattern.compile(RegexUtils.convertWildcardsToRegex(oldVersion, true));

            for (Model m : reactor.values()) {
                String mGroupId = PomHelper.getGroupId(m);
                String mArtifactId = PomHelper.getArtifactId(m);
                String mVersion = PomHelper.getVersion(m);

                if ((groupIdRegex == null || groupIdRegex.matcher(mGroupId).matches())
                        && (artifactIdRegex == null
                        || artifactIdRegex.matcher(mArtifactId).matches())
                        && (mVersion == null
                        || oldVersionIdRegex == null
                        || oldVersionIdRegex.matcher(mVersion).matches())
                        && !newVersion.equals(mVersion)) {
                    applyChange(
                            project,
                            reactor,
                            files,
                            mGroupId,
                            m.getArtifactId(),
                            StringUtils.isBlank(oldVersion) || "*".equals(oldVersion) ? "" : mVersion);
                }
            }

            if ("always".equals(updateBuildOutputTimestampPolicy)) {
                reactor.values().stream()
                        .map(m -> PomHelper.getModelEntry(reactor, PomHelper.getGroupId(m), PomHelper.getArtifactId(m)))
                        .filter(Objects::nonNull)
                        .map(Map.Entry::getValue)
                        .map(Model::getPomFile)
                        .forEach(files::add);
            }

            // now process all the updates
            for (File file : files) {
                process(file);
            }

        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Returns the incremented version, with the nextSnapshotIndexToIncrement indicating the 1-based index,
     * from the left, or the most major version component, of the version string.
     *
     * @param version input version
     * @param nextSnapshotIndexToIncrement 1-based segment number to be incremented
     * @return version with the incremented index specified by nextSnapshotIndexToIncrement or last index
     * @throws MojoExecutionException thrown if the input parameters are invalid
     */
    protected String getIncrementedVersion(String version, Integer nextSnapshotIndexToIncrement)
            throws MojoExecutionException {
        String versionWithoutSnapshot =
                version.endsWith(SNAPSHOT) ? version.substring(0, version.indexOf(SNAPSHOT)) : version;
        List<String> numbers = new LinkedList<>(Arrays.asList(versionWithoutSnapshot.split("\\.")));

        if (nextSnapshotIndexToIncrement == null) {
            nextSnapshotIndexToIncrement = numbers.size();
        } else if (nextSnapshotIndexToIncrement < 1) {
            throw new MojoExecutionException("nextSnapshotIndexToIncrement cannot be less than 1");
        } else if (nextSnapshotIndexToIncrement > numbers.size()) {
            throw new MojoExecutionException(
                    "nextSnapshotIndexToIncrement cannot be greater than the last version index");
        }
        int snapshotVersionToIncrement = Integer.parseInt(numbers.remove(nextSnapshotIndexToIncrement - 1));
        numbers.add(nextSnapshotIndexToIncrement - 1, String.valueOf(snapshotVersionToIncrement + 1));

        return StringUtils.join(numbers.toArray(new String[0]), ".") + "-SNAPSHOT";
    }

    private static String fixNullOrEmpty(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    private void applyChange(
            MavenProject project,
            SortedMap<File, Model> reactor,
            Set<File> files,
            String groupId,
            String artifactId,
            String oldVersion) {

        getLog().debug("Applying change " + groupId + ":" + artifactId + ":" + oldVersion + " -> " + newVersion);
        // this is a triggering change
        addChange(groupId, artifactId, oldVersion, newVersion);
        // now fake out the triggering change

        Map.Entry<File, Model> current = PomHelper.getModelEntry(reactor, groupId, artifactId);
        if (current != null) {
            current.getValue().setVersion(newVersion);
            files.add(current.getValue().getPomFile());
        }

        for (Map.Entry<File, Model> sourceEntry : reactor.entrySet()) {
            final File sourcePath = sourceEntry.getKey();
            final Model sourceModel = sourceEntry.getValue();

            getLog().debug(
                    sourcePath.length() == 0
                            ? "Processing root module as parent"
                            : "Processing " + sourcePath + " as a parent.");

            final String sourceGroupId = PomHelper.getGroupId(sourceModel);
            if (sourceGroupId == null) {
                getLog().warn("Module " + sourcePath + " is missing a groupId.");
                continue;
            }
            final String sourceArtifactId = PomHelper.getArtifactId(sourceModel);
            if (sourceArtifactId == null) {
                getLog().warn("Module " + sourcePath + " is missing an artifactId.");
                continue;
            }
            final String sourceVersion = PomHelper.getVersion(sourceModel);
            if (sourceVersion == null) {
                getLog().warn("Module " + sourcePath + " is missing a version.");
                continue;
            }

            files.add(sourceModel.getPomFile());

            getLog().debug("Looking for modules which use "
                    + ArtifactUtils.versionlessKey(sourceGroupId, sourceArtifactId) + " as their parent");

            for (Map.Entry<File, Model> stringModelEntry : processAllModules
                    ? reactor.entrySet()
                    : PomHelper.getChildModels(reactor, sourceGroupId, sourceArtifactId)
                    .entrySet()) {
                final Model targetModel = stringModelEntry.getValue();
                final Parent parent = targetModel.getParent();
                getLog().debug("Module: " + stringModelEntry.getKey());
                if (parent != null && sourceVersion.equals(parent.getVersion())) {
                    getLog().debug("    parent already is "
                            + ArtifactUtils.versionlessKey(sourceGroupId, sourceArtifactId) + ":" + sourceVersion);
                } else {
                    getLog().debug("    parent is " + ArtifactUtils.versionlessKey(sourceGroupId, sourceArtifactId)
                            + ":" + (parent == null ? "" : parent.getVersion()));
                    getLog().debug("    will become " + ArtifactUtils.versionlessKey(sourceGroupId, sourceArtifactId)
                            + ":" + sourceVersion);
                }
                final boolean targetExplicit = PomHelper.isExplicitVersion(targetModel);
                if ((updateMatchingVersions || !targetExplicit) //
                        && (parent != null
                        && StringUtils.equals(parent.getVersion(), PomHelper.getVersion(targetModel)))) {
                    getLog().debug("    module is "
                            + ArtifactUtils.versionlessKey(
                            PomHelper.getGroupId(targetModel), PomHelper.getArtifactId(targetModel))
                            + ":"
                            + PomHelper.getVersion(targetModel));
                    getLog().debug("    will become "
                            + ArtifactUtils.versionlessKey(
                            PomHelper.getGroupId(targetModel), PomHelper.getArtifactId(targetModel))
                            + ":" + sourceVersion);
                    addChange(
                            PomHelper.getGroupId(targetModel),
                            PomHelper.getArtifactId(targetModel),
                            PomHelper.getVersion(targetModel),
                            sourceVersion);
                    targetModel.setVersion(sourceVersion);
                } else {
                    getLog().debug("    module is "
                            + ArtifactUtils.versionlessKey(
                            PomHelper.getGroupId(targetModel), PomHelper.getArtifactId(targetModel))
                            + ":"
                            + PomHelper.getVersion(targetModel));
                }
            }
        }
    }

    /**
     * Updates the pom file.
     *
     * @param pom The pom file to update.
     * @throws org.apache.maven.plugin.MojoExecutionException when things go wrong.
     * @throws org.apache.maven.plugin.MojoFailureException   when things go wrong.
     * @throws javax.xml.stream.XMLStreamException            when things go wrong.
     */
    protected synchronized void update(ModifiedPomXMLEventReader pom)
            throws MojoExecutionException, MojoFailureException, XMLStreamException {
        ContextualLog log = new DelegatingContextualLog(getLog());
        try {
            Model model = PomHelper.getRawModel(pom);
            log.setContext("Processing " + PomHelper.getGroupId(model) + ":" + PomHelper.getArtifactId(model));

            VersionChangerFactory versionChangerFactory = new VersionChangerFactory();
            versionChangerFactory.setPom(pom);
            versionChangerFactory.setLog(log);
            versionChangerFactory.setModel(model);

            VersionChanger changer = versionChangerFactory.newVersionChanger(
                    processParent, processProject, processDependencies, processPlugins);

            for (DefaultDependencyVersionChange versionChange : sourceChanges) {
                changer.apply(versionChange);
            }

            if (updateBuildOutputTimestamp && !"never".equals(updateBuildOutputTimestampPolicy)) {
                if ("always".equals(updateBuildOutputTimestampPolicy) || !sourceChanges.isEmpty()) {
                    // also update project.build.outputTimestamp
                    updateBuildOutputTimestamp(pom, model);
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        log.clearContext();
    }

    private void updateBuildOutputTimestamp(ModifiedPomXMLEventReader pom, Model model) throws XMLStreamException {
        String buildOutputTimestamp = model.getProperties().getProperty("project.build.outputTimestamp");

        if (buildOutputTimestamp == null || isEmpty(buildOutputTimestamp)) {
            // no Reproducible Builds output timestamp defined
            return;
        }

        if (StringUtils.isNumeric(buildOutputTimestamp)) {
            // int representing seconds since the epoch, like SOURCE_DATE_EPOCH
            buildOutputTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        } else if (buildOutputTimestamp.length() <= 1) {
            // value length == 1 means disable Reproducible Builds
            return;
        } else {
            // ISO-8601
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            buildOutputTimestamp = df.format(new Date());
        }

        PomHelper.setPropertyVersion(pom, null, "project.build.outputTimestamp", buildOutputTimestamp);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
