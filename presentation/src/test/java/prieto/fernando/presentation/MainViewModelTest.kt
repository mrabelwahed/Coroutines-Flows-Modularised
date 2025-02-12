package prieto.fernando.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import prieto.fernando.core.event.Event
import prieto.fernando.core_android_test.util.buildDate
import prieto.fernando.domain.model.CompanyInfoDomainModel
import prieto.fernando.domain.model.LaunchDomainModel
import prieto.fernando.domain.model.LinksDomainModel
import prieto.fernando.domain.model.RocketDomainModel
import prieto.fernando.domain.usecase.GetCompanyInfo
import prieto.fernando.domain.usecase.GetLaunches
import prieto.fernando.presentation.mapper.CompanyInfoDomainToUiModelMapper
import prieto.fernando.presentation.mapper.LaunchesDomainToUiModelMapper
import prieto.fernando.presentation.model.CompanyInfoUiModel
import prieto.fernando.presentation.model.LaunchUiModel
import prieto.fernando.presentation.model.LinksUiModel
import prieto.fernando.presentation.model.RocketUiModel

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    private lateinit var cut: MainViewModel

    @Mock
    lateinit var getLaunches: GetLaunches

    @Mock
    lateinit var getCompanyInfo: GetCompanyInfo

    @Mock
    lateinit var companyInfoMapper: CompanyInfoDomainToUiModelMapper

    @Mock
    lateinit var launchesMapper: LaunchesDomainToUiModelMapper

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        cut = MainViewModelImpl(getLaunches, getCompanyInfo, companyInfoMapper, launchesMapper)
    }

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()


    @Test
    fun `When launches then launchUiModelRetrieved invoked with expected result`() {
        runBlockingTest {
            // Given
            val launchUiModelRetrievedTestObserver = mock<Observer<List<LaunchUiModel>>>()
            cut.launches.observeForever(launchUiModelRetrievedTestObserver)
            val launchDomainModels = listOf(
                LaunchDomainModel(
                    "missionName",
                    buildDate("2019-12-11T12:00:00.000Z"),
                    RocketDomainModel("rocketName", "rocketType"),
                    LinksDomainModel("patchLink", "wikipediaLink", "videoLink"),
                    false
                ),
                LaunchDomainModel(
                    "missionName2",
                    buildDate("2020-12-07T12:00:00.000Z"),
                    RocketDomainModel("rocketName2", "rocketType2"),
                    LinksDomainModel("patchLink2", "wikipediaLink2", "videoLink2"),
                    false
                )
            )
            val expected = listOf(
                LaunchUiModel(
                    "missionName",
                    "11-12-2019 at 12:00",
                    true,
                    "0",
                    RocketUiModel("rocketName", "rocketType"),
                    LinksUiModel("patchLink", "wikipediaLink", "videoLink"),
                    false
                ),
                LaunchUiModel(
                    "missionName2",
                    "07-12-2020 at 12:00",
                    false,
                    "361",
                    RocketUiModel("rocketName2", "rocketType2"),
                    LinksUiModel("patchLink2", "wikipediaLink2", "videoLink2"),
                    false
                )
            )
            val flow = flow {
                emit(launchDomainModels)
            }
            whenever(getLaunches.execute(-1, false)).thenReturn(flow)
            whenever(launchesMapper.toUiModel(launchDomainModels)).thenReturn(expected)

            // When
            cut.launches()
            val actualValue = cut.launches.value

            // Then
            verify(getLaunches, times(1)).execute(-1, false)
            assertEquals(expected, actualValue)
        }
    }

    @Test
    fun `When companyInfo then companyInfoUiModelRetrieved invoked with expected result`() {
        runBlockingTest {
            // Given
            val companyInfoUiModelRetrievedTestObserver = mock<Observer<CompanyInfoUiModel>>()
            cut.companyInfo.observeForever(companyInfoUiModelRetrievedTestObserver)
            val companyInfoDomainModel = CompanyInfoDomainModel(
                "name",
                "founder",
                "foundedYear",
                "employees",
                1,
                23
            )
            val expected = CompanyInfoUiModel(
                "name",
                "founder",
                "founded",
                "employees",
                1,
                23
            )
            val flow = flow {
                emit(companyInfoDomainModel)
            }
            whenever(getCompanyInfo.execute()).thenReturn(flow)
            whenever(companyInfoMapper.toUiModel(companyInfoDomainModel)).thenReturn(expected)

            // When
            cut.companyInfo()
            val actualValue = cut.companyInfo.value

            // Then
            verify(getCompanyInfo, times(1)).execute()
            assertEquals(expected, actualValue)
        }
    }

    @Test
    fun `When openLink then onOpenLink invoked with expected result`() {
        runBlockingTest {
            // Given
            val link = "Some cool space related link"
            val onOpenLinkTestObserver = mock<Observer<Event<String>>>()
            val expected = "Some cool space related link"
            cut.openLink.observeForever(onOpenLinkTestObserver)

            // When
            cut.openLink(link)
            val actualValue = cut.openLink.value?.peekContent()

            // Then
            assertEquals(expected, actualValue)
        }
    }
}
