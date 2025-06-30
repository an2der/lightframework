package com.lightframework.plugin.version;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.shared.release.versions.VersionInfo;
import org.apache.maven.shared.release.versions.VersionParseException;
import org.codehaus.plexus.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*** 
 * @author yg
 * @date 2024/2/4 15:35
 * @version 1.0
 */
public class NextVersionInfo implements VersionInfo {

    private final String strVersion;

    private final List<String> digits;

    private String annotation;

    private String annotationRevision;

    private final String buildSpecifier;

    private String annotationSeparator;

    private String annotationRevSeparator;

    private final String buildSeparator;

    private static final int DIGITS_INDEX = 1;

    private static final int ANNOTATION_SEPARATOR_INDEX = 2;

    private static final int ANNOTATION_INDEX = 3;

    private static final int ANNOTATION_REV_SEPARATOR_INDEX = 4;

    private static final int ANNOTATION_REVISION_INDEX = 5;

    private static final int BUILD_SEPARATOR_INDEX = 6;

    private static final int BUILD_SPECIFIER_INDEX = 7;

    private static final String SNAPSHOT_IDENTIFIER = "SNAPSHOT";

    private static final String DIGIT_SEPARATOR_STRING = ".";

    /** Constant <code>STANDARD_PATTERN</code> */
    public static final Pattern STANDARD_PATTERN = Pattern.compile(
            "^((?:\\d+\\.)*\\d+)" // digit(s) and '.' repeated - followed by digit (version digits 1.22.0, etc)
                    + "([-_])?" // optional - or _  (annotation separator)
                    + "([a-zA-Z]*)" // alpha characters (looking for annotation - alpha, beta, RC, etc.)
                    + "([-_])?" // optional - or _  (annotation revision separator)
                    + "(\\d*)" // digits  (any digits after rc or beta is an annotation revision)
                    + "(?:([-_])?(.*?))?$"); // - or _ followed everything else (build specifier)

    /* *
     * cmaki 02242009
     * FIX for non-digit release numbers, e.g. trunk-SNAPSHOT or just SNAPSHOT
     * This alternate pattern supports version numbers like:
     * trunk-SNAPSHOT
     * branchName-SNAPSHOT
     * SNAPSHOT
     */
    // for SNAPSHOT releases only (possible versions include: trunk-SNAPSHOT or SNAPSHOT)
    /** Constant <code>ALTERNATE_PATTERN</code> */
    public static final Pattern ALTERNATE_PATTERN = Pattern.compile("^(SNAPSHOT|[a-zA-Z]+[_-]SNAPSHOT)");

    /**
     * Constructs this object and parses the supplied version string.
     *
     * @param version the version string
     * @throws VersionParseException if an exception during parsing the input
     */
    public NextVersionInfo(String version) throws VersionParseException {
        strVersion = version;

        // FIX for non-digit release numbers, e.g. trunk-SNAPSHOT or just SNAPSHOT
        Matcher matcher = ALTERNATE_PATTERN.matcher(strVersion);
        // TODO: hack because it didn't support "SNAPSHOT"
        if (matcher.matches()) {
            annotation = null;
            digits = null;
            buildSpecifier = version;
            buildSeparator = null;
            return;
        }

        Matcher m = STANDARD_PATTERN.matcher(strVersion);
        if (m.matches()) {
            digits = parseDigits(m.group(DIGITS_INDEX));
            if (!SNAPSHOT_IDENTIFIER.equals(m.group(ANNOTATION_INDEX))) {
                annotationSeparator = m.group(ANNOTATION_SEPARATOR_INDEX);
                annotation = nullIfEmpty(m.group(ANNOTATION_INDEX));

                if (StringUtils.isNotEmpty(m.group(ANNOTATION_REV_SEPARATOR_INDEX))
                        && StringUtils.isEmpty(m.group(ANNOTATION_REVISION_INDEX))) {
                    // The build separator was picked up as the annotation revision separator
                    buildSeparator = m.group(ANNOTATION_REV_SEPARATOR_INDEX);
                    buildSpecifier = nullIfEmpty(m.group(BUILD_SPECIFIER_INDEX));
                } else {
                    annotationRevSeparator = m.group(ANNOTATION_REV_SEPARATOR_INDEX);
                    annotationRevision = nullIfEmpty(m.group(ANNOTATION_REVISION_INDEX));

                    buildSeparator = m.group(BUILD_SEPARATOR_INDEX);
                    buildSpecifier = nullIfEmpty(m.group(BUILD_SPECIFIER_INDEX));
                }
            } else {
                // Annotation was "SNAPSHOT" so populate the build specifier with that data
                buildSeparator = m.group(ANNOTATION_SEPARATOR_INDEX);
                buildSpecifier = nullIfEmpty(m.group(ANNOTATION_INDEX));
            }
        } else {
            throw new VersionParseException("Unable to parse the version string: \"" + version + "\"");
        }
    }

    /**
     * <p>Constructor for DefaultVersionInfo.</p>
     *
     * @param digits a {@link List} object
     * @param annotation a {@link String} object
     * @param annotationRevision a {@link String} object
     * @param buildSpecifier a {@link String} object
     * @param annotationSeparator a {@link String} object
     * @param annotationRevSeparator a {@link String} object
     * @param buildSeparator a {@link String} object
     */
    public NextVersionInfo(
            List<String> digits,
            String annotation,
            String annotationRevision,
            String buildSpecifier,
            String annotationSeparator,
            String annotationRevSeparator,
            String buildSeparator) {
        this.digits = digits;
        this.annotation = annotation;
        this.annotationRevision = annotationRevision;
        this.buildSpecifier = buildSpecifier;
        this.annotationSeparator = annotationSeparator;
        this.annotationRevSeparator = annotationRevSeparator;
        this.buildSeparator = buildSeparator;
        this.strVersion = getVersionString(this, buildSpecifier, buildSeparator);
    }

    @Override
    public boolean isSnapshot() {
        return ArtifactUtils.isSnapshot(strVersion);
    }

    @Override
    public VersionInfo getNextVersion() {
        NextVersionInfo version = null;
        if (digits != null) {
            List<String> digits = new ArrayList<>(this.digits);
            String annotationRevision = this.annotationRevision;
            if (StringUtils.isNumeric(annotationRevision)) {
                annotationRevision = incrementVersionString(annotationRevision);
            } else {
                digits.set(digits.size() - 1, incrementVersionString(digits.get(digits.size() - 1)));
            }

            version = new NextVersionInfo(
                    digits,
                    annotation,
                    annotationRevision,
                    buildSpecifier,
                    annotationSeparator,
                    annotationRevSeparator,
                    buildSeparator);
        }
        return version;
    }

    private VersionInfo getNextVersion(int index) {
        NextVersionInfo version = null;
        if (digits != null) {
            List<String> digits = new ArrayList<>(this.digits);
            digits.set(index, incrementVersionString(digits.get(index)));
            if(index < 2){
                for (int i = index + 1;i < digits.size();i++){
                    digits.set(i,"0");
                }
            }

            version = new NextVersionInfo(
                    digits,
                    annotation,
                    annotationRevision,
                    buildSpecifier,
                    annotationSeparator,
                    annotationRevSeparator,
                    buildSeparator);
        }
        return version;
    }

    public VersionInfo getNextMajorVersion() {
        return getNextVersion(0);
    }

    public VersionInfo getNextMinorVersion() {
        return getNextVersion(1);
    }

    public VersionInfo getNextPatchVersion() {
        return getNextVersion(digits.size() - 1);
    }

    /**
     * {@inheritDoc}
     *
     * Compares this {@link NextVersionInfo} to the supplied {@link NextVersionInfo} to determine which version is
     * greater.
     */
    @Override
    public int compareTo(VersionInfo obj) {
        NextVersionInfo that = (NextVersionInfo) obj;

        int result;
        // TODO: this is a workaround for a bug in DefaultArtifactVersion - fix there - 1.01 < 1.01.01
        if (strVersion.startsWith(that.strVersion)
                && !strVersion.equals(that.strVersion)
                && strVersion.charAt(that.strVersion.length()) != '-') {
            result = 1;
        } else if (that.strVersion.startsWith(strVersion)
                && !strVersion.equals(that.strVersion)
                && that.strVersion.charAt(strVersion.length()) != '-') {
            result = -1;
        } else {
            // TODO: this is a workaround for a bug in DefaultArtifactVersion - fix there - it should not consider case
            // in comparing the qualifier
            // NOTE: The combination of upper-casing and lower-casing is an approximation of String.equalsIgnoreCase()
            String thisVersion = strVersion.toUpperCase(Locale.ENGLISH).toLowerCase(Locale.ENGLISH);
            String thatVersion = that.strVersion.toUpperCase(Locale.ENGLISH).toLowerCase(Locale.ENGLISH);

            result = new DefaultArtifactVersion(thisVersion).compareTo(new DefaultArtifactVersion(thatVersion));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NextVersionInfo)) {
            return false;
        }

        return compareTo((VersionInfo) obj) == 0;
    }

    @Override
    public int hashCode() {
        return strVersion.toLowerCase(Locale.ENGLISH).hashCode();
    }

    /**
     * Takes a string and increments it as an integer.
     * Preserves any lpad of "0" zeros.
     *
     * @param s the version number
     * @return {@code String} increments the input {@code String} as an integer
     * and Preserves any lpad of "0" zeros.
     */
    protected String incrementVersionString(String s) {
        int n = Integer.valueOf(s).intValue() + 1;
        String value = String.valueOf(n);
        if (value.length() < s.length()) {
            // String was left-padded with zeros
            value = StringUtils.leftPad(value, s.length(), "0");
        }
        return value;
    }

    @Override
    public String getSnapshotVersionString() {
        if (strVersion.equals(Artifact.SNAPSHOT_VERSION)) {
            return strVersion;
        }

        String baseVersion = getReleaseVersionString();

        if (baseVersion.length() > 0) {
            baseVersion += "-";
        }

        return baseVersion + Artifact.SNAPSHOT_VERSION;
    }

    @Override
    public String getReleaseVersionString() {
        String baseVersion = strVersion;

        Matcher m = Artifact.VERSION_FILE_PATTERN.matcher(baseVersion);
        if (m.matches()) {
            baseVersion = m.group(1);
        }
        // MRELEASE-623 SNAPSHOT is case-insensitive
        else if (StringUtils.right(baseVersion, 9).equalsIgnoreCase("-" + Artifact.SNAPSHOT_VERSION)) {
            baseVersion = baseVersion.substring(0, baseVersion.length() - Artifact.SNAPSHOT_VERSION.length() - 1);
        } else if (baseVersion.equals(Artifact.SNAPSHOT_VERSION)) {
            baseVersion = "1.0";
        }
        return baseVersion;
    }

    @Override
    public String toString() {
        return strVersion;
    }

    /**
     * <p>getVersionString.</p>
     *
     * @param info a {@link org.apache.maven.shared.release.versions.DefaultVersionInfo} object
     * @param buildSpecifier a {@link String} object
     * @param buildSeparator a {@link String} object
     * @return a {@link String} object
     */
    protected static String getVersionString(NextVersionInfo info, String buildSpecifier, String buildSeparator) {
        StringBuilder sb = new StringBuilder();

        if (info.digits != null) {
            sb.append(joinDigitString(info.digits));
        }

        if (info.annotation != null && !info.annotation.isEmpty()) {
            sb.append(StringUtils.defaultString(info.annotationSeparator));
            sb.append(info.annotation);
        }

        if (info.annotationRevision != null && !info.annotationRevision.isEmpty()) {
            if (info.annotation == null || info.annotation.isEmpty()) {
                sb.append(StringUtils.defaultString(info.annotationSeparator));
            } else {
                sb.append(StringUtils.defaultString(info.annotationRevSeparator));
            }
            sb.append(info.annotationRevision);
        }

        if (buildSpecifier != null && !buildSpecifier.isEmpty()) {
            sb.append(StringUtils.defaultString(buildSeparator));
            sb.append(buildSpecifier);
        }

        return sb.toString();
    }

    /**
     * Simply joins the items in the list with "." period
     *
     * @return a single {@code String} of the items in the passed list, joined with a "."
     * @param digits {@code List<String>} of digits
     */
    protected static String joinDigitString(List<String> digits) {
        return digits != null ? StringUtils.join(digits.iterator(), DIGIT_SEPARATOR_STRING) : null;
    }

    /**
     * Splits the string on "." and returns a list
     * containing each digit.
     *
     * @param strDigits
     */
    private List<String> parseDigits(String strDigits) {
        return Arrays.asList(StringUtils.split(strDigits, DIGIT_SEPARATOR_STRING));
    }

    // --------------------------------------------------
    // Getters & Setters
    // --------------------------------------------------

    private static String nullIfEmpty(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    /**
     * <p>Getter for the field <code>digits</code>.</p>
     *
     * @return a {@link List} object
     */
    public List<String> getDigits() {
        return digits;
    }

    /**
     * <p>Getter for the field <code>annotation</code>.</p>
     *
     * @return a {@link String} object
     */
    public String getAnnotation() {
        return annotation;
    }

    /**
     * <p>Getter for the field <code>annotationRevision</code>.</p>
     *
     * @return a {@link String} object
     */
    public String getAnnotationRevision() {
        return annotationRevision;
    }

    /**
     * <p>Getter for the field <code>buildSpecifier</code>.</p>
     *
     * @return a {@link String} object
     */
    public String getBuildSpecifier() {
        return buildSpecifier;
    }
}
