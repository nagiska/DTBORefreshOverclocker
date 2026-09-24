package io.mo.dtbooverclocker.ui

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 启动冒烟测试：覆盖免责声明、四个 Tab、设置/关于/备份回滚等全部已迁移到 Miuix 的界面，
 * 用于在 CI 中及早捕获界面层运行时崩溃（闪退）。
 */
@RunWith(AndroidJUnit4::class)
class AppSmokeTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchAcceptDisclaimerAndVisitAllScreens() {
        // 1. 首次启动弹出免责声明，等待 5 秒倒计时结束
        compose.waitUntil(timeoutMillis = 20_000) {
            compose.onAllNodesWithText("同意并继续").fetchSemanticsNodes().isNotEmpty()
        }
        // 2. 勾选风险确认复选框
        compose.onNode(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)).performClick()
        compose.waitForIdle()
        // 3. 同意并继续
        compose.onNodeWithText("同意并继续").performClick()
        compose.waitForIdle()

        // 4. 功能模块 Tab（无工作区时应显示引导卡片）
        compose.onNode(hasText("功能模块") and hasClickAction()).performClick()
        compose.waitForIdle()
        compose.onNodeWithText("还没有工作区").assertIsDisplayed()

        // 5. 设备树 Tab（标题在顶栏副标题/页面标题/底栏多处出现，取首个即可）
        compose.onNode(hasText("设备树") and hasClickAction()).performClick()
        compose.waitForIdle()
        compose.onAllNodesWithText("设备树").onFirst().assertIsDisplayed()

        // 6. 设置 Tab
        compose.onNode(hasText("设置") and hasClickAction()).performClick()
        compose.waitForIdle()

        // 7. 设置 -> 关于（目标卡片在长页面下方，需先滚动到可见区域）
        compose.onNode(hasText("关于 DTBO Studio") and hasClickAction()).performClick()
        compose.waitForIdle()
        compose.onNodeWithText("免责声明与风险须知").performScrollTo().assertIsDisplayed()
        compose.onNode(hasContentDescription("返回设置")).performClick()
        compose.waitForIdle()

        // 8. 设置 -> 备份与回滚
        compose.onNode(hasText("备份与恢复") and hasClickAction()).performClick()
        compose.waitForIdle()
        compose.onNodeWithText("暂无备份镜像").assertIsDisplayed()
        compose.onNode(hasContentDescription("返回主页")).performClick()
        compose.waitForIdle()

        // 9. 高级设置（完整设置页）
        compose.onNode(hasText("高级设置") and hasClickAction()).performClick()
        compose.waitForIdle()
        compose.onNodeWithText("环境状态").assertIsDisplayed()
        compose.onNode(hasContentDescription("返回主页")).performClick()
        compose.waitForIdle()

        // 10. 返回概览 Tab
        compose.onNode(hasText("概览") and hasClickAction()).performClick()
        compose.waitForIdle()
        compose.onNodeWithText("镜像来源").assertIsDisplayed()
    }
}
