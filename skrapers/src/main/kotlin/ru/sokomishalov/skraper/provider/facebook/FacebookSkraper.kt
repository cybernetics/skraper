/**
 * Copyright 2019-2020 the original author or authors.
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
package ru.sokomishalov.skraper.provider.facebook

import org.jsoup.nodes.Element
import ru.sokomishalov.skraper.Skraper
import ru.sokomishalov.skraper.SkraperClient
import ru.sokomishalov.skraper.client.jdk.DefaultBlockingSkraperClient
import ru.sokomishalov.skraper.fetchDocument
import ru.sokomishalov.skraper.internal.consts.DEFAULT_POSTS_ASPECT_RATIO
import ru.sokomishalov.skraper.internal.jsoup.getSingleElementByAttributeOrNull
import ru.sokomishalov.skraper.internal.jsoup.getSingleElementByClassOrNull
import ru.sokomishalov.skraper.internal.jsoup.getSingleElementByTagOrNull
import ru.sokomishalov.skraper.internal.url.uriCleanUp
import ru.sokomishalov.skraper.model.Attachment
import ru.sokomishalov.skraper.model.AttachmentType.IMAGE
import ru.sokomishalov.skraper.model.AttachmentType.VIDEO
import ru.sokomishalov.skraper.model.ImageSize
import ru.sokomishalov.skraper.model.ImageSize.*
import ru.sokomishalov.skraper.model.Post


/**
 * @author sokomishalov
 */
class FacebookSkraper @JvmOverloads constructor(
        override val client: SkraperClient = DefaultBlockingSkraperClient
) : Skraper {

    override val baseUrl: String = "https://facebook.com"

    override suspend fun getPageLogoUrl(uri: String, imageSize: ImageSize): String? {
        val type = when (imageSize) {
            SMALL -> "small"
            MEDIUM -> "normal"
            LARGE -> "large"
        }
        return "http://graph.facebook.com/${uri.uriCleanUp()}/picture?type=${type}"
    }

    override suspend fun getLatestPosts(uri: String, limit: Int): List<Post> {
        val document = client.fetchDocument("${baseUrl}/${uri.uriCleanUp()}/posts")
        val elements = document?.getElementsByClass("userContentWrapper")?.take(limit).orEmpty()

        return elements.map {
            Post(
                    id = it.getIdByUserContentWrapper(),
                    caption = it.getCaptionByUserContentWrapper(),
                    publishTimestamp = it.getPublishedAtByUserContentWrapper(),
                    attachments = it.getAttachmentsByUserContentWrapper()
            )
        }
    }

    private fun Element.getIdByUserContentWrapper(): String {
        return getElementsByAttributeValueContaining("id", "feed_subtitle")
                ?.firstOrNull()
                ?.attr("id")
                ?.substringAfter(";")
                ?.substringBefore(";")
                .orEmpty()
    }

    private fun Element.getCaptionByUserContentWrapper(): String? {
        return getSingleElementByClassOrNull("userContent")
                ?.getSingleElementByTagOrNull("p")
                ?.wholeText()
                ?.toString()
    }

    private fun Element.getPublishedAtByUserContentWrapper(): Long? {
        return getSingleElementByAttributeOrNull("data-utime")
                ?.attr("data-utime")
                ?.toLongOrNull()
                ?.times(1000)
    }

    private fun Element.getAttachmentsByUserContentWrapper(): List<Attachment> {
        val videoElement = getSingleElementByTagOrNull("video")

        return when {
            videoElement != null -> listOf(Attachment(
                    type = VIDEO,
                    url = getElementsByAttributeValueContaining("id", "feed_subtitle")
                            .firstOrNull()
                            ?.getSingleElementByTagOrNull("a")
                            ?.attr("href")
                            ?.let { "${baseUrl}${it}" }
                            .orEmpty(),
                    aspectRatio = videoElement
                            .attr("data-original-aspect-ratio")
                            .toDoubleOrNull()
                            ?: DEFAULT_POSTS_ASPECT_RATIO
            ))

            else -> getSingleElementByClassOrNull("uiScaledImageContainer")
                    ?.getSingleElementByTagOrNull("img")
                    ?.run {
                        val url = attr("src")
                        val width = attr("width").toDoubleOrNull()
                        val height = attr("height").toDoubleOrNull()

                        listOf(Attachment(
                                type = IMAGE,
                                url = url,
                                aspectRatio = when {
                                    width != null && height != null -> width / height
                                    else -> DEFAULT_POSTS_ASPECT_RATIO
                                }

                        ))
                    }
                    ?: emptyList()
        }
    }
}