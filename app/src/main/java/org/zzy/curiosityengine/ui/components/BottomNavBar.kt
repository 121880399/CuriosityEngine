package org.zzy.curiosityengine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.zzy.curiosityengine.R
import org.zzy.curiosityengine.ui.theme.Primary

/**
 * 底部导航栏组件
 * @param currentRoute 当前路由
 * @param onHomeClick 主页点击回调
 * @param onHistoryClick 历史记录点击回调
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)  // 增加高度以确保文字显示完全
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 主页按钮
            NavItem(
                icon = Icons.Default.Home,
                label = stringResource(id = R.string.home_tab),
                isSelected = currentRoute == "home",
                onClick = onHomeClick
            )
            
            // 历史记录按钮
            NavItem(
                icon = Icons.Default.History,
                label = stringResource(id = R.string.history_tab),
                isSelected = currentRoute == "history",
                onClick = onHistoryClick
            )
        }
    }
}

/**
 * 导航项
 * @param icon 图标
 * @param label 标签
 * @param isSelected 是否选中
 * @param onClick 点击回调
 */
@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val textColor = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val backgroundColor = if (isSelected) Primary.copy(alpha = 0.1f) else Color.Transparent
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)  // 增加水平内边距，确保文字有足够空间
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor
            )
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}