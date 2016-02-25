/*
 * Copyright (C) 2010 The Android Open Source Project
 *
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
 */

package com.google.android.vending.licensing;


import android.content.Context;

/**
 * Default policy. All policy decisions are based off of response data received
 * from the licensing service. Specifically, the licensing server sends the
 * following information: response validity period, error retry period, and
 * error retry count.
 * <p>
 * These values will vary based on the the way the application is configured in
 * the Android Market publishing console, such as whether the application is
 * marked as free or is within its refund period, as well as how often an
 * application is checking with the licensing service.
 * <p>
 * Developers who need more fine grained control over their application's
 * licensing policy should implement a custom Policy.
 */
public class ServerManagedPolicy implements Policy {

    /**
     * @param context The context for the current application
     * @param obfuscator An obfuscator to be used with preferences.
     */
    public ServerManagedPolicy(Context context, Obfuscator obfuscator) {

    }

}
