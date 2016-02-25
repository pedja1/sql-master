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

public interface LicenseCheckerCallback {


    public void allow(int reason);


    public void dontAllow(int reason);

    /** Application error codes. */
    public static final int ERROR_INVALID_PACKAGE_NAME = 1;
    public static final int ERROR_NON_MATCHING_UID = 2;
    public static final int ERROR_NOT_MARKET_MANAGED = 3;
    public static final int ERROR_CHECK_IN_PROGRESS = 4;
    public static final int ERROR_INVALID_PUBLIC_KEY = 5;
    public static final int ERROR_MISSING_PERMISSION = 6;

    public void applicationError(int errorCode);
}
