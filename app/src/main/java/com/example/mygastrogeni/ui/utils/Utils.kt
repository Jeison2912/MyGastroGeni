package com.example.mygastrogeni.ui.utils

fun String.capitalizeWords(): String = split(" ").joinToString(" ") {
    it.replaceFirstChar { char -> char.uppercaseChar() }
}