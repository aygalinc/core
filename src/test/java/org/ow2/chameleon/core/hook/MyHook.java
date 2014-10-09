/*
 * #%L
 * OW2 Chameleon - Core
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.ow2.chameleon.core.hook;


import org.ow2.chameleon.core.ChameleonConfiguration;

public class MyHook extends DefaultHook {

    public static boolean initCalled;
    public static boolean configuredCalled;
    public static boolean shuttingDownCalled;

    /**
     * Callback called when Chameleon just starts. Nothing important was done so far.
     */
    @Override
    public void initializing() {
        initCalled = true;
    }

    /**
     * Callback called when the Chameleon instance is configured but not yet created.
     *
     * @param configuration the configuration, that can be modified.
     */
    @Override
    public void configured(ChameleonConfiguration configuration) {
        configuredCalled = configuration != null;
    }

    /**
     * Callback called when the Chameleon instance is stopped just before leaving.
     */
    @Override
    public void shuttingDown() {
        shuttingDownCalled = true;
    }

}
