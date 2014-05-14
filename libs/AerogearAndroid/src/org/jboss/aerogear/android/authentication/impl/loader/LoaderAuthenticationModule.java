/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.authentication.impl.loader;

import org.jboss.aerogear.android.authentication.AuthenticationModule;

/**
 * Sometimes a AuthenticationManager will actually be wrapped in a Loader.  
 * Classes which do so implement this interface which gives them access to some 
 * shared Bundle Parameters.
 */
public interface LoaderAuthenticationModule extends AuthenticationModule {

    public static final String CALLBACK = "org.jboss.aerogear.android.authentication.loader.AuthenticationModuleAdapter.CALLBACK";
    public static final String METHOD = "org.jboss.aerogear.android.authentication.loader.AuthenticationModuleAdapter.METHOD";
    public static final String USERNAME = "org.jboss.aerogear.android.authentication.loader.AuthenticationModuleAdapter.USERNAME";
    public static final String PASSWORD = "org.jboss.aerogear.android.authentication.loader.AuthenticationModuleAdapter.PASSWORD";
    public static final String PARAMS = "org.jboss.aerogear.android.authentication.loader.AuthenticationModuleAdapter.PARAMS";

}
