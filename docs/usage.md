Jada > [Documentation](README.md) >

# Usage

This document describes how to use Jada in your own projects.

All the examples are based on the Maven build system.

In the `pom.xml` file of your project, add to maven-javadoc-plugin the following
configuration (replace `${jada.version}` with the latest release version of Jada):

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <artifactId>maven-javadoc-plugin</artifactId>
        <version>${maven-javadoc-plugin.version}</version>
        <configuration>
          <doclet>org.pdfclown.jada.core.Jada</doclet>
          <docletArtifacts>
            <artifact>
              <groupId>org.pdfclown</groupId>
              <artifactId>jada-core</artifactId>
              <version>${jada.version}</version>
            </artifact>
          </docletArtifacts>
          <additionalOptions>
            <option>-jada-doclet Standard</option>
            <option>-jada-dir ${rootdir}/src/main/javadoc/jada</option>
            <!--
              TIP: Uncomment the following option to list all the available options on next
              `mvn javadoc` execution.
            -->
            <!--
              <option>-help</option>
            -->
          </additionalOptions>
          <additionalJOptions>
            <option>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</option>
            <option>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</option>
            <option>-J--add-exports=jdk.javadoc/jdk.javadoc.internal.tool=ALL-UNNAMED</option>
          </additionalJOptions>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

That's all!

## Snapshot artifacts

In case you want to give a try to the latest, unreleased implementation of Jada, you can consume SNAPSHOT dependencies (updated on a daily basis) via Maven Central Portal Snapshots repository.

For the purpose:

1. add Maven Central Portal Snapshots repository to your global configuration (`~/.m2/settings.xml`):

    ```xml
    <settings>
      . . .
      <profiles>
        . . .
        <profile>
          <id>central-snapshots</id>
          <repositories>
            <repository>
              <id>central-portal-snapshots</id>
              <name>Central Portal Snapshots</name>
              <url>https://central.sonatype.com/repository/maven-snapshots/</url>
              <releases>
                <enabled>false</enabled>
              </releases>
              <snapshots>
                <enabled>true</enabled>
                <checksumPolicy>fail</checksumPolicy>
              </snapshots>
            </repository>
          </repositories>
        </profile>
      </profiles>

      <activeProfiles>
        <activeProfile>central-snapshots</activeProfile>
      </activeProfiles>
    </settings>
    ```

2. in the maven-javadoc-plugin configuration described here above, assign to the `version` element of the Jada dependencies the current SNAPSHOT version (in the project, see `revision` parameter at `.mvn/maven.config`).

Alternatively, you can build the SNAPSHOT artifacts by yourself: see ["Building"](building.md) (in such case, you obviously don't need the Maven Central Portal Snapshots repository).
