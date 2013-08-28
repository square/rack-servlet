Rack Servlet [![Build Status](https://travis-ci.org/square/rack-servlet.png?branch=master)](https://travis-ci.org/square/rack-servlet)
============

Embed JRuby Rack applications in your Java container.

Download
--------

In a Maven project, add `rack-servlet` and `jruby-complete` to your `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>com.squareup.rack</groupId>
    <artifactId>rack-servlet</artifactId>
    <version>${rack.servlet.version}</version>
  </dependency>
  <dependency>
    <groupId>org.jruby</groupId>
    <artifactId>jruby-complete</artifactId>
    <version>${jruby.version}</version>
  </dependency>
</dependencies>
```

Quick Start
-----------

```java
// Use JRuby to build your Rack application:
ScriptingContainer ruby = new ScriptingContainer();
IRubyObject application = ruby.parse("lambda { |env| [200, {}, ['Hello, World!']] }").run();

// Build a RackServlet with that Rack application:
Servlet servlet = new RackServlet(new JRubyRackApplication(application));

// Install that servlet in your container...
// (See our examples directory for concrete code.)
```

In Depth
--------

- **Gems**: You can use the [gem-maven-plugin] to put gems in your `pom.xml` on
  your test classpath. For production deployments, you'll need to be [a little
  more clever][corner-maven-gems].
- **Frameworks**: We're successfully running [Sinatra][sinatra] applications on
  Rack Servlet. We've not yet tried Rails. At any rate, you'll need to [make
  separate arrangements][corner-sequel-hibernate] for any database connections
  you may need.
- **Logging**: `rack.logger` and `rack.errors` use [slf4j], so that you can [choose
  your logging backend][slf4j-backend]. Log messages are written to
  `com.squareup.rack.RackLogger` and `com.squareup.rack.RackErrors`,
  respectively.

Support
-------

- **Examples**: Our [examples] directory has some good concrete code to get you
  started.
- **Stack Overflow**: Use the [rack-servlet][stack-overflow-tag] tag to [ask
  any questions][stack-overflow-ask]. We'll keep an eye on it.
- **GitHub Issues**: If you've found a bug, please [file an issue][github-issues].

Alternatives
------------

Rack Servlet grew out of our desire to embed Rack applications in Square's Java
service container, in order to take advantage of our sophisticated
infrastructure.

There are other options in the "run Rack applications on the JVM" space, all of
which are shaped somewhat differently:

- [Kirk][kirk] is a Rack server, like Mongrel or Unicorn.
- [Warbler][warbler] bundles your Rack application into a WAR file.
- [jruby-rack] is close in intent to Rack Servlet. It offers amazing support
  for instantiating and pooling your Rack application objects, though its
  custom embedding story is less clear.

[corner-maven-gems]: http://corner.squareup.com/2013/07/maven-gems-and-a-jruby-repl.html
[corner-sequel-hibernate]: http://corner.squareup.com/2013/06/sequel-on-hibernate.html
[examples]: https://github.com/square/rack-servlet/tree/master/examples
[gem-maven-plugin]: https://github.com/torquebox/jruby-maven-plugins
[github-issues]: https://github.com/square/rack-servlet/issues
[jruby-rack]: https://github.com/jruby/jruby-rack
[kirk]: https://github.com/strobecorp/kirk
[sinatra]: http://www.sinatrarb.com/
[slf4j-backend]: http://www.slf4j.org/manual.html#swapping
[slf4j]: http://www.slf4j.org/
[stack-overflow-ask]: http://stackoverflow.com/questions/ask?tags=rack-servlet
[stack-overflow-tag]: http://stackoverflow.com/questions/tagged/rack-servlet
[warbler]: https://github.com/jruby/warbler
