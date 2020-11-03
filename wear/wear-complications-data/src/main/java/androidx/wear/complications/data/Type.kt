/*
 * Copyright 2020 The Android Open Source Project
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

package androidx.wear.complications.data

import androidx.annotation.RestrictTo

/** The possible complication data types. */
public enum class ComplicationType(private val wireType: Int) {
    NO_DATA(WireComplicationData.TYPE_NO_DATA),
    EMPTY(WireComplicationData.TYPE_EMPTY),
    NOT_CONFIGURED(WireComplicationData.TYPE_NOT_CONFIGURED),
    SHORT_TEXT(WireComplicationData.TYPE_SHORT_TEXT),
    LONG_TEXT(WireComplicationData.TYPE_LONG_TEXT),
    RANGED_VALUE(WireComplicationData.TYPE_RANGED_VALUE),
    MONOCHROMATIC_IMAGE(WireComplicationData.TYPE_ICON),
    SMALL_IMAGE(WireComplicationData.TYPE_SMALL_IMAGE),
    BACKGROUND_IMAGE(WireComplicationData.TYPE_LARGE_IMAGE),
    NO_PERMISSION(WireComplicationData.TYPE_NO_PERMISSION);

    /**
     * Converts this value to the integer value used for serialization.
     *
     * This is only needed internally to convert to the underlying communication protocol.
     *
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public fun asWireComplicationType(): Int = wireType

    public companion object {
        /**
         * Converts the integer value used for serialization into a [ComplicationType].
         *
         * This is only needed internally to convert to the underlying communication protocol.
         *
         * @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY)
        @JvmStatic
        public fun fromWireType(wireType: Int): ComplicationType =
            when (wireType) {
                NO_DATA.wireType -> NO_DATA
                EMPTY.wireType -> EMPTY
                NOT_CONFIGURED.wireType -> NOT_CONFIGURED
                SHORT_TEXT.wireType -> SHORT_TEXT
                LONG_TEXT.wireType -> LONG_TEXT
                RANGED_VALUE.wireType -> RANGED_VALUE
                MONOCHROMATIC_IMAGE.wireType -> MONOCHROMATIC_IMAGE
                SMALL_IMAGE.wireType -> SMALL_IMAGE
                BACKGROUND_IMAGE.wireType -> BACKGROUND_IMAGE
                NO_PERMISSION.wireType -> NO_PERMISSION
                else -> EMPTY
            }

        /**
         * Converts an array of [ComplicationType] to an array of integers with the corresponding
         * wire types.
         *
         * This is only needed internally to convert to the underlying communication protocol.
         *
         * Needed to access this conveniently in Java.
         *
         * @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @JvmStatic
        public fun toWireTypes(types: Collection<ComplicationType>): IntArray = types.asWireTypes()

        /**
         * Converts an array of integer values used for serialization into the corresponding array
         * of [ComplicationType].
         *
         * This is only needed internally to convert to the underlying communication protocol.
         *
         * Needed to access this conveniently in Java.
         *
         * @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @JvmStatic
        public fun fromWireTypes(types: IntArray): Array<ComplicationType> =
            types.asApiComplicationTypes()

        /**
         * Converts an array of integer values used for serialization into the corresponding list
         * of [ComplicationType].
         *
         * @hide
         */
        @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
        @JvmStatic
        public fun fromWireTypeList(types: IntArray): List<ComplicationType> =
            types.map { fromWireType(it) }
    }
}

/**
 * Converts an array of [ComplicationType] to an array of integers with the corresponding
 * wire types.
 *
 * This is only needed internally to convert to the underlying communication protocol.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun Collection<ComplicationType>.asWireTypes(): IntArray =
    this.map { it.asWireComplicationType() }.toIntArray()

/**
 * Converts an array of integer values uses for serialization into the corresponding array
 * of [ComplicationType] to .
 *
 * This is only needed internally to convert to the underlying communication protocol.
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun IntArray.asApiComplicationTypes(): Array<ComplicationType> =
    this.map { ComplicationType.fromWireType(it) }.toTypedArray()
