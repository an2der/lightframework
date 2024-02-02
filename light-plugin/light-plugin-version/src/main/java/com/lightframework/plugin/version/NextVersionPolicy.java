package com.lightframework.plugin.version;

import org.apache.maven.shared.release.policy.PolicyException;
import org.apache.maven.shared.release.policy.version.VersionPolicy;
import org.apache.maven.shared.release.policy.version.VersionPolicyRequest;
import org.apache.maven.shared.release.policy.version.VersionPolicyResult;
import org.apache.maven.shared.release.versions.DefaultVersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Named("NextVersionPolicy")
public class NextVersionPolicy implements VersionPolicy {
    @Override
    public VersionPolicyResult getReleaseVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        String developmentVersion =
                new DefaultVersionInfo(request.getVersion()).getNextVersion().toString();
        return new VersionPolicyResult().setVersion(developmentVersion);
    }

    @Override
    public VersionPolicyResult getDevelopmentVersion(VersionPolicyRequest request) throws PolicyException, VersionParseException {
        String developmentVersion =
                new DefaultVersionInfo(request.getVersion()).getNextVersion().toString();
        return new VersionPolicyResult().setVersion(developmentVersion);
    }
}
