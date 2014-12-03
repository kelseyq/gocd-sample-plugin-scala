name := "curl-plugin-scala"

version := "1.0"

scalaVersion := "2.10.4"

val goVersion = "14.1.0"

packageOptions in (Compile, packageBin) +=
  Package.ManifestAttributes( "Go-Version" -> goVersion )

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "." + artifact.extension
}

assemblyJarName in assembly := name.value + ".jar"

libraryDependencies += "com.thoughtworks.go" %  "go-plugin-api" % "current"  % "provided" from "http://www.thoughtworks.com/products/docs/go/current/help/resources/go-plugin-api-current.jar"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"

scalariformSettings