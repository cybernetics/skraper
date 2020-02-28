/**
 * Copyright (c) 2019-present Mikhael Sokolov
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
@file:Suppress("MemberVisibilityCanBePrivate", "BlockingMethodInNonBlockingContext")

package ru.sokomishalov.skraper.provider

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.sokomishalov.skraper.Skraper
import ru.sokomishalov.skraper.SkraperClient
import ru.sokomishalov.skraper.client.ktor.KtorSkraperClient
import ru.sokomishalov.skraper.model.PageInfo
import ru.sokomishalov.skraper.model.Post
import ru.sokomishalov.skraper.model.ProviderInfo
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


/**
 * @author sokomishalov
 */
abstract class SkraperTck {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(SkraperTck::class.java)
        private val MAPPER: ObjectMapper = ObjectMapper().setSerializationInclusion(NON_NULL)
    }

    protected abstract val skraper: Skraper
    protected abstract val path: String

    protected val client: SkraperClient = KtorSkraperClient()

    @Test
    fun `Check posts`() {
        assertPosts { getPosts(path = path) }
    }

    @Test
    fun `Check page info`() {
        assertPageInfo { getPageInfo(path = path) }
    }

    @Test
    fun `Check provider info`() {
        assertProviderInfo { getProviderInfo() }
    }

    protected fun assertPosts(action: suspend Skraper.() -> List<Post>) = runBlocking {
        val posts = logAction { skraper.action() }

        assertTrue { posts.isNotEmpty() }
        posts.forEach {
            assertNotNull(it.id)
            it.media.forEach { a ->
                assertTrue(a.url.isNotBlank())
            }
        }
    }

    protected fun assertPageInfo(action: suspend Skraper.() -> PageInfo?) = runBlocking {
        val pageInfo = logAction { skraper.action() }
        assertNotNull(pageInfo)
        assertNotNull(pageInfo.nick)
        assertTrue { pageInfo.avatarsMap.isNotEmpty() }
        pageInfo.avatarsMap.forEach {
            assertTrue { it.value.url.isNotEmpty() }
        }
    }

    protected fun assertProviderInfo(action: suspend Skraper.() -> ProviderInfo?) = runBlocking {
        val providerInfo = logAction { skraper.action() }
        assertNotNull(providerInfo)
        assertNotNull(providerInfo.name)
        assertTrue { providerInfo.logoMap.isNotEmpty() }
        providerInfo.logoMap.forEach {
            assertTrue { it.value.url.isNotEmpty() }
        }
    }

    private suspend fun <T> logAction(action: suspend Skraper.() -> T): T {
        return skraper.action().also {
            LOGGER.info(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(it))
        }
    }
}