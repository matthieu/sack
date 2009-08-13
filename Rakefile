gem "buildr", "~>1.3"
require "buildr"
require "buildr/scala"

HTTPCLIENT = "commons-httpclient:commons-httpclient:jar:3.1"
COMMONS    = struct(
    :logging => "commons-logging:commons-logging:jar:1.1",
    :codec   => "commons-codec:commons-codec:jar:1.3"
)
JETTY      = group("jetty", "jetty-util", "servlet-api-2.5", :under=>"org.mortbay.jetty", :version=>"6.1.11")

repositories.remote << "http://repo1.maven.org/maven2"

desc "A Scala Rack."
define "sack" do
  project.version = "0.1"
  project.group = "web"

  compile.with JETTY
  test.with HTTPCLIENT, COMMONS.logging, COMMONS.codec
  
  package :jar

  cp = Buildr.artifacts(JETTY, package(:jar)) << "#{ENV['SCALA_HOME']}/lib/scala-library.jar"
  task(:turtle=>package(:jar)) do
    Java.java "sack.TurtleApp", :classpath=>cp
  end
  task(:chicken=>package(:jar)) do
    Java.java "sack.ChickenApp", :classpath=>cp
  end
  task(:pig=>package(:jar)) do
    Java.java "sack.PigApp", :classpath=>cp
  end
end
