package de.cramer.releasenotifier.services

import de.cramer.releasenotifier.utils.Message
import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.h4
import kotlinx.html.html
import kotlinx.html.li
import kotlinx.html.stream.appendHTML
import kotlinx.html.ul
import org.springframework.stereotype.Service
import java.io.StringWriter
import java.net.URI

@Service
class HtmlMessageGenerator {

    fun <Context> generate(sources: Collection<Source<Context>>, subject: String, header: String): Message {
        val writer = StringWriter()
        writer.appendHTML().html {
            body {
                h3 { text(header) }
                sources.forEach { source ->
                    div {
                        val context = source.generateContext(null)
                        h4 {
                            generateMessageHeader(source, context)
                        }
                        generateMessageChildren(source, context)
                    }
                }
            }
        }
        return Message(subject, writer.toString(), true)
    }

    private fun <Context> FlowContent.generateMessageHeader(source: Source<Context>, context: Context) {
        val text = source.getText(context)
        val url = source.getUrl(context)
        if (url != null) {
            a(href = url.toASCIIString()) {
                text(text)
            }
        } else {
            text(text)
        }
    }

    private fun <Context> FlowContent.generateMessageChildren(source: Source<Context>, context: Context) {
        val children = source.getChildren(context)
        if (children.isEmpty()) {
            return
        }

        ul {
            children.forEach { child ->
                val childContext = child.generateContext(context)
                li {
                    generateMessageHeader(child, childContext)
                    generateMessageChildren(child, childContext)
                }
            }
        }
    }

    interface Source<Context> {
        fun getText(context: Context): String
        fun getUrl(context: Context): URI?
        fun getChildren(context: Context): Collection<Source<Context>>
        fun generateContext(parentContext: Context?): Context
    }
}
