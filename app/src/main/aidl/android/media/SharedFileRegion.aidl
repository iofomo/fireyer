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

/**
 * A shared file region.
 *
 * This type contains the required information to share a region of a file between processes over
 * AIDL.
 * An instance of this type represents a valid FD. For representing a null SharedFileRegion, use a
 * @nullable SharedFileRegion.
 * Primarily, this is intended for shared memory blocks.
 *
 * @hide
 */
parcelable SharedFileRegion {
    /** File descriptor of the region. Must be valid. */
    ParcelFileDescriptor fd;
    /** Offset, in bytes within the file of the start of the region. Must be non-negative. */
    long offset;
    /** Size, in bytes of the memory region. Must be non-negative. */
    long size;
    /** Whether the region is writeable. */
    boolean writeable;
}
