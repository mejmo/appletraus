# appletraus
Workaround that allows to run Java applets even without NPAPI support. One of our customers is still using applets 
and as of september 2015 Google Chrome opted out NPAPI support (no Java applet support) and other browsers are following
this trend, that's why we needed to replace it with very quick solution. 

## Requirements

- **valid** certificate for signing JNLP applications
- Java webserver (JRE7+)
- and of course JRE on client (JRE7+)

## How it works?

* AppletRaus instantiated with JNLP client application which represents the applet container. 
* Applet still "thinks" that it is running in browser as all JApplet methods are provided. 
* Communication between the applet container and browser context is made asynchronously between client and server 
and synchronously between browser context and server
* async2sync library used to provide synchronous calls - as same as the archaic applet method calling works

![AppletRaus](http://s27.postimg.org/4i4t961dv/Appletraus.png)

## Usage

This project was implemented very quickly, in case you are interested in help with integration, give a message. Generally
the integration consists of these steps:

1. Include `AppletRaus.js` in your HTML page where applet was previously loaded
2. Instantiate the `AppletRaus` object with
```javascript
var oAppletRaus = new AppletRaus({
    aClientOptions: {
        iDebugLevel: 4,
        sAppletJnlpUrl: "/appletraus-boot/Applet.jnlp",
        sAtmosphereEndpoint: "/appletraus-boot/appletraus"
    }
});
```
3. Include your applet JAR in client project. Set the `MainClass` attribute of the applets main class
4. Compile with maven. Maven makes automatically JAR signing.
5. Install **WAR** file in your Java/J2EE server (Tomcat, wildfly, WebSphere, ...)
6. You are ready to go!

## Q&A
### Why the applet is not running in the same cookie context then browser?
Because this way the JNLP starting of Java applications work - it is simply not the same context anymore. In this case 
you have two options. 

* If you have access to source code of the applet, you should implement method which 
can set cookie on `HttpUrlConnection` object.
* If the applet is a blackbox, this is very tricky without breaching the Oracle license. You can override `java.net.URL` class
that provides the HTTP connection for browser and then you have to run JNLP with `-Xbootclasspath/p:` parameter which tells 
Java classloader to load classes from this particular directory at first. As the classloader works the way - if I loaded 
already the class, I do not load it anymore - running JRE without this parameter would be useless, as loading it AFTER JRE
has started, would do absolutely nothing. I will not provide source code of this solution because as said, decompiling JRE/JDK
classes is not the best way how to do legal workarounds.

### Why Chrome does not work instantly and I have to click on downloaded JNLP?
Because Google decided to ignore this mime type completely. You have to click on the file so that application could start

### Why applet container (client) is not implemented in Spring boot?
Spring boot does not support JNLP at all and possibly it will never be. The problem resides in a way that Boot is handling
the source JAR in its class loader. JNLP provides network URL and not the absolute URL, as the file is actually not saved
for the executed application, but executed on-the-fly.

## Bug reports, feature requests and contact

If you found any bugs, if you have feature requests or any questions, please, either file an issue at GitHub.

## License

appletraus is published under the MIT license.


