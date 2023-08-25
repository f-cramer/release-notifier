package de.cramer.releasenotifier.utils

import net.datafaker.Faker
import net.datafaker.providers.base.Internet
import java.net.URI
import java.util.function.Supplier

fun <T> Faker.collection(length: IntRange, supplier: () -> T): List<T> {
    val javaSupplier = Supplier { supplier() }
    return collection(listOf(javaSupplier))
        .len(length.first, length.last)
        .generate()
}

fun Internet.uri(): URI = URI(url())
