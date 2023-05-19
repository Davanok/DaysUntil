package com.example.daysuntil

inline fun <reified T> Array<T>.removeAt(index: Int): Array<T>{
    var result = emptyArray<T>()

    for (i in this.indices) {
        if (i != index) {
            val newItem = this[i]
            result += newItem
        }
    }
    return result
}