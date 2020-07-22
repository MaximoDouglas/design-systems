package br.com.argmax.githubconsumer

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import br.com.argmax.githubconsumer.utils.FileUtils.getJsonFromFile
import br.com.argmax.githubconsumer.utils.RecyclerViewMatcher.Companion.withRecyclerView
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class NavigationTest {

    private var activityScenario: ActivityScenario<MainActivity>? = null
    private val mockWebServer = MockWebServer()

    @Before
    fun setup() {
        setupMockWebServer()
        activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.close()
    }

    @Test
    fun test_if_click_on_repository_item_navigate_to_pull_request_fragment() {
        Thread.sleep(1000)
        onView(
            withRecyclerView(R.id.select_repository_fragment_recycler_view).atPositionOnView(
                0,
                R.id.gitRepositoryCard
            )
        ).perform(click())

        onView(withId(R.id.select_git_pull_request_fragment_toolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun test_if_click_on_pull_request_item_makes_intent_to_web_browser() {
        Intents.init()

        Thread.sleep(1000)
        onView(
            withRecyclerView(R.id.select_repository_fragment_recycler_view)
                .atPositionOnView(0, R.id.gitRepositoryCard)
        ).perform(click())

        Thread.sleep(1000)
        onView(
            withRecyclerView(R.id.select_git_pull_request_fragment_recycler_view)
                .atPositionOnView(0, R.id.gitPullRequestCard)
        ).perform(click())

        intended(hasAction(Intent.ACTION_VIEW))
        intended(hasData(Uri.parse("https://github.com/CyC2018/CS-Notes/pull/957")))
        Intents.release()
    }

    private fun setupMockWebServer() {
        val endpointToRepositories = "/search/repositories?q=language%3AJava&sort=stars&page=1"
        val endpointToPullRequests = "/repos/CyC2018/CS-Notes/pulls?page=1&state=all"

        val dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                when (request.path) {
                    endpointToRepositories -> return MockResponse()
                        .setResponseCode(200)
                        .setBody(getJsonFromFile("jsonfiles/repositories/git_repository_api_response.json"))

                    endpointToPullRequests -> return MockResponse()
                        .setResponseCode(200)
                        .setBody(getJsonFromFile("jsonfiles/pullrequests/git_pull_request_api_response_cyc2018_cs_notes.json"))
                }

                return MockResponse().setResponseCode(404)
            }
        }

        mockWebServer.dispatcher = dispatcher
        mockWebServer.start(8500)
    }

}