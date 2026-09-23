package io.mo.dtbooverclocker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.mo.dtbooverclocker.model.CustomTimingParams
import io.mo.dtbooverclocker.model.PatchMode
import io.mo.dtbooverclocker.model.PatchStrategy
import io.mo.dtbooverclocker.model.TimingCandidate
import java.util.Locale
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun OverclockPreviewCard(
    candidate: TimingCandidate,
    targetHz: Int,
    strategy: PatchStrategy,
    mode: PatchMode = PatchMode.OVERWRITE_EXISTING,
    customParams: CustomTimingParams? = null,
    modifier: Modifier = Modifier
) {
    val sim = TimingUtils.calculateSimulation(candidate, targetHz, strategy, customParams)

    val (riskColor, riskBgColor, riskIcon) = when (sim.risk) {
        OverclockRisk.SAFE -> Triple(
            Color(0xFF2E7D32),
            Color(0xFFE8F5E9),
            Icons.Default.CheckCircle
        )
        OverclockRisk.MODERATE -> Triple(
            Color(0xFFED6C02),
            Color(0xFFFFF3E0),
            Icons.Default.Speed
        )
        OverclockRisk.EXTREME -> Triple(
            Color(0xFFD32F2F),
            Color(0xFFFFEBEE),
            Icons.Default.Warning
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 12.dp,
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "超频效果实时推演",
                        style = MiuixTheme.textStyles.title3,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 风险评级微标
                Surface(
                    color = riskBgColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            riskIcon,
                            contentDescription = null,
                            tint = riskColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            sim.risk.label,
                            style = MiuixTheme.textStyles.footnote2,
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        )
                    }
                }
            }

            if (mode == PatchMode.APPEND_NEW) {
                Surface(
                    color = MiuixTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "新增独立档位模式：保留原 ${sim.originalHz} Hz 档位，追加 ${sim.targetHz} Hz",
                            style = MiuixTheme.textStyles.footnote2,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // 核心对比表：刷新率与时钟
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 刷新率对比
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (mode == PatchMode.APPEND_NEW) "新增刷新率 (原档保留)" else "刷新率变换",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "${sim.originalHz} Hz",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceSecondary
                        )
                        Text(
                            " ➔ ",
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.outline
                        )
                        Text(
                            "${sim.targetHz} Hz",
                            style = MiuixTheme.textStyles.title2,
                            fontWeight = FontWeight.Bold,
                            color = MiuixTheme.colorScheme.primary
                        )
                    }
                    val sign = if (sim.hzDelta >= 0) "+" else ""
                    Text(
                        "$sign${sim.hzDelta} Hz ($sign${String.format(Locale.US, "%.1f", sim.hzPercentage)}%)",
                        style = MiuixTheme.textStyles.footnote2,
                        fontWeight = FontWeight.SemiBold,
                        color = riskColor
                    )
                }

                // 像素时钟对比
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        "预估 Pixel Clock",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        TimingUtils.formatClockCompact(sim.estimatedClockHz),
                        style = MiuixTheme.textStyles.title2,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MiuixTheme.colorScheme.primary
                    )
                    Text(
                        "原频 ${TimingUtils.formatClockCompact(sim.originalClockHz)} (×${String.format(Locale.US, "%.2f", sim.clockMultiplier)})",
                        style = MiuixTheme.textStyles.footnote2,
                        fontFamily = FontFamily.Monospace,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                }
            }

            // 若有垂直消隐行数变动
            if (sim.estimatedVfp != null && sim.estimatedVbp != null) {
                HorizontalDivider(color = MiuixTheme.colorScheme.outline.copy(alpha = 0.5f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "消隐行数适配",
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.onSurfaceSecondary
                    )
                    Text(
                        "VFP: ${sim.originalVfp}→${sim.estimatedVfp} 行 · VBP: ${sim.originalVbp}→${sim.estimatedVbp} 行",
                        style = MiuixTheme.textStyles.footnote2,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 说明与风险提示
            val showRiskDescription = sim.risk.description.isNotBlank()
            val showCalculationNote = sim.calculationNote.isNotBlank()
            if (showRiskDescription || showCalculationNote) {
                Surface(
                    color = MiuixTheme.colorScheme.surface.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (showRiskDescription) {
                            Text(
                                sim.risk.description,
                                style = MiuixTheme.textStyles.footnote2,
                                color = riskColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (showCalculationNote) {
                            Text(
                                sim.calculationNote,
                                style = MiuixTheme.textStyles.footnote2,
                                color = MiuixTheme.colorScheme.onSurfaceSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
