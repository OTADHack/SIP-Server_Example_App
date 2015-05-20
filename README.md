SIP Server Sample APP
=========

With this sample app you will gain experience in creating a B2BUA service and test to verify that there
are two different call legs. Your test call flow should look something like this:

![DCB2BUA](https://raw.githubusercontent.com/OTADHack/SIP-Server_Example_App/master/DCB2BUA.png)


Version
----

7.0

Usage
----

Follow this steps:

+ If you didn't it, request your credentials on the [Oracle's TADHack website](http://tadhack.optaresolutions.com).
+ Send an email to tadhack@optaresolutions.com informing you want to test or develop something in OCCAS. You will receive more detailled instructions.
+ Install [maven](http://maven.apache.org/) if it's not installed.
+ Download this repository using the 'Download ZIP button' or using the clone option

```sh
git clone https://github.com/OTADHack/SIP-Server_Example_App.git
cd SIP-Server_Example_App
```
+ Deploy it

```sh
mvn package pre-integration-test -DoracleUsername=YOUR_USERNAME -DoraclePassword=YOUR_PASSWORD -DoracleServerUrl=t3://occas70.optaresolutions.com:7001 -Dupload=true
```

+ Open a browser and go to http://occas70.optaresolutions.com:7001/DizzyCommWeb/
+ Using [SJPhone](http://www.sjlabs.com/sjp.html), connect as Bob. 
+ Using [SJPhone](http://www.sjlabs.com/sjp.html), connect as Alice.
+ In the browser, click on [List all registered users](http://occas70.optaresolutions.com:7001/DizzyCommWeb/). You should see Bob and Alice connected.
+ Now, in the Bob's SJPhone, call Alice.
+ In the browser, go to [List all active calls](http://occas70.optaresolutions.com:7001/DizzyCommWeb/testPages/admin.jsp). You should see the call between Alice and Bob.
+ Click on Terminate All. The call will finish.

Documentation
----

SIP-Server Documentation can be found at [Oracle's TADHack SIP Server website](http://tadhack.optaresolutions.com/).

Support
----

If you have any doubt, ask it in [the Issues section](https://github.com/OTADHack/SIP-Server/issues).

License
----

Copyright © 2007, 2015, Oracle and/or its affiliates. All rights reserved. Usage only allowed for TADHack Developers.
