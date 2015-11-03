<?xml version="1.0" encoding="utf-8"?>
<jnlp codebase="${baseUrl}" spec="1.0+" href="/appletraus-boot/Applet.jnlp?cookie=${cookie}&sessionId=${sessionId}">
    <information>
        <vendor>Martin Formanko</vendor>
        <description>AppletRaus</description>
    </information>
    <update check="timeout" policy="always"/>
    <security>
        <all-permissions/>
    </security>
    <resources>
        <j2se version="1.7+"/>
        <jar href="/appletraus-boot/jar/appletraus-client.jar" />
    </resources>
    <application-desc main-class="com.mejmo.appletraus.client.AppletRausClientApplication">
        <argument>${cookie}</argument>
        <argument>${sessionId}</argument>
    </application-desc>
</jnlp>