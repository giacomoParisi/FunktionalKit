package com.giacomoparisi.android.funktional.architecture.core.utils

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Giacomo Parisi on 22/08/17.
 * https://github.com/giacomoParisi
 */

/**
 * Delegate property that access to the SharedPreferences for save the value in the setter
 * and load the value in the get
 *
 * @param defaultValue The default value to return if the key is not present in the SharedPreferences
 * @param key The key used for store the value in the SharedPreferences
 * @param getter The getter function from SharedPreferences that need to be invoked
 * @param setter The setter function from SharedPreferences that need to be invoked
 *
 */
private inline fun <T> SharedPreferences.delegate(
        defaultValue: T,
        key: String?,
        crossinline getter: SharedPreferences.(String, T) -> T,
        crossinline setter: SharedPreferences.Editor.(String, T) -> SharedPreferences.Editor
): ReadWriteProperty<Any, T> {

    return object : ReadWriteProperty<Any, T> {

        override fun getValue(thisRef: Any, property: KProperty<*>): T = getter(key
                ?: property.name, defaultValue)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = edit().setter(key
                ?: property.name, value).apply()

    }
}

/**
 *
 * SharedPreferences Delegate for Int property
 *
 * @param defaultValue The default value to return if the key is not present in the SharedPreferences
 * @param key The key used for store the value in the SharedPreferences
 */
fun SharedPreferences.int(defaultValue: Int = -1, key: String? = null) = delegate(
        defaultValue = defaultValue,
        key = key,
        getter = SharedPreferences::getInt,
        setter = SharedPreferences.Editor::putInt)

/**
 *
 * SharedPreferences Delegate for Long property
 *
 * @param defaultValue The default value to return if the key is not present in the SharedPreferences
 * @param key The key used for store the value in the SharedPreferences
 */
fun SharedPreferences.long(defaultValue: Long = -1, key: String? = null) = delegate(
        defaultValue = defaultValue,
        key = key,
        getter = SharedPreferences::getLong,
        setter = SharedPreferences.Editor::putLong)

/**
 *
 * SharedPreferences Delegate for Float property
 *
 * @param defaultValue The default value to return if the key is not present in the SharedPreferences
 * @param key The key used for store the value in the SharedPreferences
 */
fun SharedPreferences.float(defaultValue: Float = -1f, key: String? = null) = delegate(
        defaultValue = defaultValue,
        key = key,
        getter = SharedPreferences::getFloat,
        setter = SharedPreferences.Editor::putFloat)


/**
 *
 * SharedPreferences Delegate for Boolean property
 *
 * @param defaultValue The default value to return if the key is not present in the SharedPreferences
 * @param key The key used for store the value in the SharedPreferences
 */
fun SharedPreferences.boolean(defaultValue: Boolean = false, key: String? = null) = delegate(
        defaultValue = defaultValue,
        key = key,
        getter = SharedPreferences::getBoolean,
        setter = SharedPreferences.Editor::putBoolean)

/**
 *
 * SharedPreferences Delegate for String property
 *
 * @param defaultValue The default value to return if the key is not present in the SharedPreferences
 * @param key The key used for store the value in the SharedPreferences
 */
fun SharedPreferences.string(defaultValue: String = "", key: String? = null) = delegate(
        defaultValue = defaultValue,
        key = key,
        getter = SharedPreferences::getString,
        setter = SharedPreferences.Editor::putString)