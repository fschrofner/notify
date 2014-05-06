#Notify

An Android application that allows you to sync cross-device reminders.


###Build
If you want to build Notify yourself you need to link the included libraries and create the class `at.fhhgb.mc.notify.push.PushConstants`.  
This class should deliver the following constants:  

```
package at.fhhgb.mc.notify.push;

public class PushConstants {
	
	protected static final String VARIANT_ID = "INSERT_VARIANT_ID";
    protected static final String SECRET = "INSERT_SECRET";
    protected static final String GCM_SENDER_ID = "INSERT_GOOGLE_CLOUD_MESSAGE_SENDER_ID";
    protected static final String UNIFIED_PUSH_URL = "INSERT_SERVER_URL";
    protected static final String UNIFIED_PUSH_BROKER_URL = "INSERT_BROKER_URL/push";

}
```  

You'll get these constants from your [Aerogear Unified Server](http://aerogear.org/) setup.  
The server is needed for handling multiplatform push notifications, so you have to set one up in order to be able to use the self-built app.  
The server however is open-source too, check out their source code [here](https://github.com/aerogear/aerogear-unifiedpush-server).
We've also made a NodeJs broker, which is used to hide the master-secret from clients. Set it up and insert the URL for `UNIFIED_PUSH_BROKER_URL`.