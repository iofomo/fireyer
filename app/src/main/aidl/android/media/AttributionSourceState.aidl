/*
 * Copyright (C) 2021 The Android Open Source Project
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

package android.media;

/**
 * Payload for the {@link AttributionSource} class needed to interoperate
 * with different languages.
 *
 * {@hide}
 */
parcelable AttributionSourceState {
    /** The PID that is accessing the permission protected data. */
    int pid = -1;
    /** The UID that is accessing the permission protected data. */
    int uid = -1;
    /** The package that is accessing the permission protected data. */
    String packageName;
    /** The attribution tag of the app accessing the permission protected data. */
    String attributionTag;
    /** Unique token for that source. */
    IBinder token;
    /** Permissions that should be considered revoked regardless if granted. */
    String[] renouncedPermissions;
    /** The next app to receive the permission protected data. */
    // TODO: We use an array as a workaround - the C++ backend doesn't
    // support referring to the parcelable as it expects ctor/dtor
    AttributionSourceState[] next;
}
