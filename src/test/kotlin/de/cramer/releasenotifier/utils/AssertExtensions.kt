package de.cramer.releasenotifier.utils

import assertk.Assert
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

fun Assert<String>.parse(): Assert<Document> = transform(concatName("parsed")) { Jsoup.parse(it) }

fun Assert<Document>.select(cssQuery: String): Assert<Elements> = transform(concatName("select($cssQuery)")) { it.select(cssQuery) }

private fun Assert<*>.concatName(newName: String): String = name?.let { "$it.$newName" } ?: newName
