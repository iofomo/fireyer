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

import android.media.SharedFileRegion;

/**
 * CreateRecordResponse contains all output arguments returned by AudioFlinger to AudioRecord
 * when calling createRecord() including arguments that were passed as I/O for update by
 * CreateRecordRequest.
 *
 * {@hide}
 */
parcelable CreateRecordResponse {
    /** Bitmask, indexed by AudioInputFlags. */
    int flags;
    long frameCount;
    long notificationFrameCount;
    /** Interpreted as audio_port_handle_t. */
    int selectedDeviceId;
    int sessionId;
    int sampleRate;
    /** Interpreted as audio_io_handle_t. */
    int inputId;
    SharedFileRegion cblk;
    SharedFileRegion buffers;
    /** Interpreted as audio_port_handle_t. */
    int portId;
    /** The newly created record. */
    IBinder audioRecord;
}
