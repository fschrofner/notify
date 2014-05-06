package at.fhhgb.mc.notify.push;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.aerogear.android.unifiedpush.PushConfig;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.Registrations;

import android.app.Application;

public class PushApplication extends Application {
	
	 // used for 'selective send' to target a specific user
    // it can be any arbitary value (e.g. name, email etc)
    private final String MY_ALIAS = "devtest";

    private PushRegistrar registration;

    @Override
    public void onCreate() {
        super.onCreate();

        Registrations registrations = new Registrations();

        try {
            PushConfig config = new PushConfig(new URI(PushConstants.UNIFIED_PUSH_URL), 
            		PushConstants.GCM_SENDER_ID);
            config.setVariantID(PushConstants.VARIANT_ID);
            config.setSecret(PushConstants.SECRET);
            config.setAlias(MY_ALIAS);

            registration = registrations.push("unifiedpush", config);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // Accessor method for Activities to access the 'PushRegistrar' object
    public PushRegistrar getRegistration() {
        return registration;
    }
}
