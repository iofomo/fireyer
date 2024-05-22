/*
 * Copyright (C) 2020 The Android Open Source Project
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

import android.media.AudioAttributesInternal;
import android.media.AudioClient;
import android.media.AudioConfigBase;

/**
 * CreateRecordRequest contains all input arguments sent by AudioRecord to AudioFlinger
 * when calling createRecord() including arguments that will be updated by AudioFlinger
 * and returned in CreateRecordResponse object.
 *
 * {@hide}
 */
parcelable CreateRecordRequest {
    AudioAttributesInternal attr;
    AudioConfigBase config;
    AudioClient clientInfo;
    /** Interpreted as audio_unique_id_t. */
    int riid;
    int maxSharedAudioHistoryMs;
    /** Bitmask, indexed by AudioInputFlags. */
    int flags;
    long frameCount;
    long notificationFrameCount;
    /** Interpreted as audio_port_handle_t. */
    int selectedDeviceId;
    int sessionId;
}
