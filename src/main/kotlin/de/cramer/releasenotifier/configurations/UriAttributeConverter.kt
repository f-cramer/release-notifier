package de.cramer.releasenotifier.configurations

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.net.URI

@Converter(autoApply = true)
class UriAttributeConverter : AttributeConverter<URI, String> {
    override fun convertToDatabaseColumn(attribute: URI?): String? = attribute?.toString()

    override fun convertToEntityAttribute(dbData: String?): URI? = dbData?.let { URI(it) }
}
