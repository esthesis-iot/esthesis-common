package esthesis.common.banner;

import esthesis.common.git.GitUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class to display a common banner in components.
 */
public class BannerUtil {

  private BannerUtil() {
  }

  //@formatter:off
  private static final String BANNER = """
****************************************************
https://esthes.is               esthesis@eurodyn.com

           _   _               _       _       _
  ___  ___| |_| |__   ___  ___(_)___  (_) ___ | |_
 / _ \\/ __| __| '_ \\ / _ \\/ __| / __| | |/ _ \\| __|
|  __/\\__ \\ |_| | | |  __/\\__ \\ \\__ \\ | | (_) | |_
 \\___||___/\\__|_| |_|\\___||___/_|___/ |_|\\___/ \\__|
""";
	//@formatter:on

  @SuppressWarnings("java:S106")
  public static void showBanner(String... title) {
    // Display the common banner.
    System.out.print(BANNER);

    // Display the title if any.
    if (title.length > 0) {
      System.out.println("\n" + title[0]);
    }

    // Prepare version information.
    GitUtil gitUtil = new GitUtil();

    String gitBuildTime = gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_BUILD_TIME);
    if (StringUtils.isNotBlank(gitBuildTime)) {
      gitBuildTime = gitBuildTime.replaceAll("(.)(?=..$)", "$1:");
    } else {
      gitBuildTime = "unknown";
    }

    String gitComitIdFull = gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_COMMIT_ID_FULL);
    if (StringUtils.isBlank(gitComitIdFull)) {
      gitComitIdFull = "unknown";
    }

    String gitVersion = gitUtil.getGitProperty(GitUtil.GIT_PROPERTY_VERSION);
    if (StringUtils.isBlank(gitVersion)) {
      gitVersion = "unknown";
    }

    String localTime = java.time.OffsetDateTime.now().format(
        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));

    String defaultLocale = java.util.Locale.getDefault().toString();

    // Display version information.
    System.out.println("Version       : " + gitVersion);
    System.out.println("Commit ID     : " + gitComitIdFull);
    System.out.println("Build time    : " + gitBuildTime);
    System.out.println("Date/Time     : " + localTime);
    System.out.println("Default Locale: " + defaultLocale);
    System.out.println("OS Name       : " + System.getProperty("os.name"));
    System.out.println("OS Version    : " + System.getProperty("os.version"));
    System.out.println("OS Arch       : " + System.getProperty("os.arch"));
    System.out.println("Java Version  : " + System.getProperty("java.version"));
    System.out.println("User Name     : " + System.getProperty("user.name"));

    System.out.println("****************************************************\n");
  }
}
