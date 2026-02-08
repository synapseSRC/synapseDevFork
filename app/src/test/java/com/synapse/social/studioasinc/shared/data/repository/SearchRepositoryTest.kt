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





    private lateinit var repository: SearchRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        whenever(supabaseClient.pluginManager).thenReturn(pluginManager)
        whenever(pluginManager.getPlugin(Postgrest)).thenReturn(postgrest)


        whenever(postgrest["news_articles"]).thenReturn(newsBuilder)

        repository = SearchRepositoryImpl(supabaseClient)
    }

    @Test
    fun `searchNews calls textSearch with WEBSEARCH`() = runTest {
        val query = "test"
        val dummyResult = mock<PostgrestResult>()
        whenever(dummyResult.data).thenReturn("[]")



        val selectCaptor = argumentCaptor<PostgrestBuilder.() -> Unit>()







        whenever(newsBuilder.select(
            columns = any(),
            head = any(),
            count = any(),
            block = selectCaptor.capture()
        )).thenReturn(dummyResult)















        val filterCaptor = argumentCaptor<PostgrestBuilder.() -> Unit>()



        doAnswer {
            val block = it.arguments[0] as (PostgrestBuilder.() -> Unit)
            block.invoke(newsBuilder)
            newsBuilder
        }.whenever(newsBuilder).filter(any())



        whenever(newsBuilder.order(
            column = any(),
            ascending = any(),
            nullsFirst = any()
        )).thenReturn(newsBuilder)
        whenever(newsBuilder.limit(
            count = any()
        )).thenReturn(newsBuilder)


        repository.searchNews(query)


        verify(newsBuilder).textSearch(
            column = eq("headline"),
            query = eq(query),
            config = eq("english"),
            type = eq(TextSearchType.WEBSEARCH)
        )
    }
}
