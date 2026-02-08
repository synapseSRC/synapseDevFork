package com.synapse.social.studioasinc.shared.data.repository

import com.synapse.social.studioasinc.shared.data.repository.SearchRepositoryImpl
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.plugins.PluginManager
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.PostgrestBuilder
import io.github.jan.supabase.postgrest.query.PostgrestResult
import io.github.jan.supabase.postgrest.query.filter.TextSearchType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SearchRepositoryTest {

    @Mock lateinit var supabaseClient: SupabaseClient
    @Mock lateinit var pluginManager: PluginManager
    @Mock lateinit var postgrest: Postgrest
    @Mock lateinit var newsBuilder: PostgrestBuilder

    // We need to mock the builder returned by filter, order, limit etc.
    // Assuming fluent API returns the builder itself or a new builder.
    // Usually it returns the same builder instance or a modified copy.

    private lateinit var repository: SearchRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        whenever(supabaseClient.pluginManager).thenReturn(pluginManager)
        whenever(pluginManager.getPlugin(Postgrest)).thenReturn(postgrest)

        // When accessing client.postgrest["news_articles"], it returns a PostgrestBuilder
        whenever(postgrest["news_articles"]).thenReturn(newsBuilder)

        repository = SearchRepositoryImpl(supabaseClient)
    }

    @Test
    fun `searchNews calls textSearch with WEBSEARCH`() = runTest {
        val query = "test"
        val dummyResult = mock<PostgrestResult>()
        whenever(dummyResult.data).thenReturn("[]")

        // Mock the select call. It takes columns, head, count, block.
        // We capture the block.
        val selectCaptor = argumentCaptor<PostgrestBuilder.() -> Unit>()

        // Mock return of select (which is PostgrestResult since decodeList is called on it? No, select returns PostgrestResult usually, but decodeList is extension on PostgrestResult?)
        // Wait, select returns PostgrestBuilder? No, select executes the query usually?
        // In Supabase-kt 3.x:
        // select(...) returns PostgrestResult (suspend function).
        // decodeList is extension on PostgrestResult.

        whenever(newsBuilder.select(any(), any(), any(), selectCaptor.capture())).thenReturn(dummyResult)

        // Also mock filter to return the builder so usage chaining works if needed.
        // filter is suspend? No, usually builder methods are not suspend, only execute/select are.
        // But select IS suspend.

        // The block passed to select runs on PostgrestBuilder.
        // Inside the block:
        // filter { ... }
        // order(...)
        // limit(...)

        // These methods are on PostgrestBuilder.
        // So we need to mock them on newsBuilder.

        // Mock filter to capture its block
        val filterCaptor = argumentCaptor<PostgrestBuilder.() -> Unit>()
        // Note: The filter lambda receiver might be PostgrestFilterBuilder, but often it's the same class or interface.
        // Let's assume it's PostgrestBuilder for now based on typical usage.

        doAnswer {
            val block = it.arguments[0] as (PostgrestBuilder.() -> Unit)
            block.invoke(newsBuilder) // Execute the filter block immediately to trigger ilike call
            newsBuilder
        }.whenever(newsBuilder).filter(any())

        // Mock other methods to avoid NullPointerException if they return something used.
        // order and limit return PostgrestBuilder.
        whenever(newsBuilder.order(any(), any(), any())).thenReturn(newsBuilder)
        whenever(newsBuilder.limit(any())).thenReturn(newsBuilder)

        // Run the repository method
        repository.searchNews(query)

        // Now verify ilike was called on newsBuilder
        verify(newsBuilder).textSearch(eq("headline"), eq(query), eq("english"), eq(TextSearchType.WEBSEARCH))
    }
}
